package edu.umass.nrc.warren.forestcover;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ImageDisplay extends JFrame {
	
	public static void main(String[] args) throws IOException { 
		File dir = new ForestCoverProperties("wake").getBaseDir();
		File f = new File(dir, "wilders_ridge/composite.png");
		ImageDisplay disp = new ImageDisplay(f);
	}

	private BufferedImage image;
	
	private int imageWidth, imageHeight;
	private double imageAspect;  // == imageWidth / imageHeight;
	
	private JCheckBoxMenuItem isEditingPolygons;
	private ImagePanel panel;
	
	public ImageDisplay(File f) throws IOException {
		super("Image Display");
		
		image = ImageIO.read(f);
		//image = JAI.create("fileload", f.getAbsolutePath());
		//image = new BufferedImage(rimage.getWidth(), rimage.getHeight(), BufferedImage.TYPE_INT_RGB);
		//image.setData(rimage.getData());
		
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		imageAspect = (double)imageWidth / (double)imageHeight;
		
		int fHeight = 300;
		int fWidth = (int)(imageAspect * fHeight);
		
		Container c = (Container)getContentPane();
		c.setLayout(new BorderLayout());
		c.add(panel = new ImagePanel(fWidth, fHeight), BorderLayout.CENTER);
		
		setJMenuBar(createJMenuBar());
		
		JPanel options = new JPanel(new FlowLayout());
		//c.add(options, BorderLayout.SOUTH);
		//options.add(isSelectingPolygons = new JCheckBox("Selecting?", false));
		options.setBorder(new TitledBorder("Options"));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		pack();
	}
	
	private JMenuBar createJMenuBar() { 
		JMenuBar bar = new JMenuBar();
		JMenu menu;
		JMenuItem item;
		
		bar.add(menu = new JMenu("File"));
		menu.add(item = new JMenuItem("Exit"));
		item.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				ImageDisplay.this.dispose();
			}
		});
		
		bar.add(menu = new JMenu("Edit"));
		menu.add(isEditingPolygons = new JCheckBoxMenuItem("Editing Polygons", false));
		isEditingPolygons.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				panel.clearSelection();
			}
		});
		
		return bar;
	}
	
	private class ImagePanel extends JPanel {
		
		// polygon coordinates, which are stored in the image 
		// coordinate space.
		private ArrayList<int[]> polyX, polyY;
		
		private ArrayList<Integer> currentX, currentY;
		
		private double imageScale;
		private int w, h;
		private int xOffset, yOffset;
		
		private int selectedPolygon;
		
		public ImagePanel(int initialWidth, int initialHeight) {
			
			setPreferredSize(new Dimension(initialWidth, initialHeight));
			
			polyX = new ArrayList<int[]>();
			polyY = new ArrayList<int[]>();
			currentX = new ArrayList<Integer>();
			currentY = new ArrayList<Integer>();
			w = h = 0;
			selectedPolygon = -1;
			
			addMouseListener(new MouseAdapter() { 
				public void mouseClicked(MouseEvent e) { 
					int count = e.getClickCount();
					if(count == 1) {
						if(!isEditingPolygons.isSelected()) { 
							Point p = e.getPoint();
							selectedPolygon = findSelectedPolygon(p);
							if(selectedPolygon != -1) { 
								Polygon poly = getImagePolygon(selectedPolygon);
								double area = area(poly);
								System.out.println(String.format("Polygon Area: %.2f", area));
							}

						} else { 
							addPointToCurrent(e.getPoint());
						}
						
					} else if (count == 2) { 
						saveCurrentPolygon();
					}
					
					repaint();
				}
			});
		}
		
		public double area(Polygon p) { 
			double area = 0.0;
			
			PathIterator itr = p.getPathIterator(null);
			double xi, yi, xj, yj, xstart, ystart;
			xi = yi = xj = yj = xstart = ystart = 0.0;
			
			double[] coords = new double[6];
			
			while(!itr.isDone()) { 
				itr.next();
				int value = itr.currentSegment(coords);
				switch(value) { 
				case PathIterator.SEG_MOVETO:
					xstart = coords[0]; ystart = coords[1];
					xi = xstart; yi = ystart;
					break;
				case PathIterator.SEG_LINETO:
					xj = coords[0]; yj = coords[1];
					area += (xi * yj);
					area -= (xj * yi);
					xi = xj; yi = yj;
					break;
				case PathIterator.SEG_CLOSE:
					xj = xstart; yj = ystart;
					area += (xi * yj);
					area -= (xj * yi);
					break;
					
				default: 
					throw new IllegalArgumentException(
							String.format("Illegal path type: %d", value));
				}
			}
			
			return Math.abs(area / 2.0);
		}
		
		public void clearSelection() { 
			selectedPolygon = -1;
			repaint();
		}
		
		private int findSelectedPolygon(Point imPoint, int startIdx) { 
			for(int i = startIdx; i < polyX.size(); i++) { 
				Polygon p = getImagePolygon(i);
				if(p.contains(imPoint)) { 
					return i;
				}
			}
			return -1;
		}
		
		public int findSelectedPolygon(Point panelPoint) { 
			Point imPoint = new Point(panelToImageX(panelPoint.x), panelToImageY(panelPoint.y));
			if(selectedPolygon == -1 || !getImagePolygon(selectedPolygon).contains(imPoint)) { 
				return findSelectedPolygon(imPoint, 0);
			} else { 
				int sel = findSelectedPolygon(imPoint, selectedPolygon+1);
				if(sel == -1) { 
					return findSelectedPolygon(imPoint, 0);
				} else { 
					return sel;
				}
			}
		}
		
		public void addPointToCurrent(Point p) { 
			currentX.add(panelToImageX(p.x));
			currentY.add(panelToImageY(p.y));
		}
		
		public void saveCurrentPolygon() { 
			if(!currentX.isEmpty()) { 
				int[] xs = new int[currentX.size()], 
					ys = new int[currentY.size()];
				for(int i = 0; i < xs.length; i++) { 
					xs[i] = currentX.get(i);
					ys[i] = currentY.get(i);
				}
				polyX.add(xs);
				polyY.add(ys);
				currentX.clear(); 
				currentY.clear();
			}
		}
		
		public int imageToPanelX(int imx) { 
			return xOffset + (int)Math.round((double)imx * imageScale);
		}
		
		public int imageToPanelY(int imy) { 
			return yOffset + (int)Math.round((double)imy * imageScale);
		}
		
		public int panelToImageX(int px) { 
			return (int)Math.round((double)(px - xOffset) / imageScale);
		}
		
		public int panelToImageY(int py) { 
			return (int)Math.round((double)(py - yOffset) / imageScale);
		}
		
		public int getNumPolygons() { return polyX.size(); }
		
		public Polygon getImagePolygon(int i) { 
			return new Polygon(polyX.get(i), polyY.get(i), polyX.get(i).length);
		}
		
		public Polygon getCurrentPanelPolygon() { 
			int[] xs = new int[currentX.size()], ys = new int[currentY.size()];
			
			for(int j = 0; j < xs.length; j++) { 
				xs[j] = imageToPanelX(currentX.get(j));
				ys[j] = imageToPanelY(currentY.get(j));
			}
			
			Polygon poly = new Polygon(xs, ys, xs.length);
			return poly;			
		}
		
		public Polygon getPanelPolygon(int[] polyx, int[] polyy) { 
			int[] xs = new int[polyx.length], ys = new int[polyy.length];
			
			for(int j = 0; j < xs.length; j++) { 
				xs[j] = imageToPanelX(polyx[j]);
				ys[j] = imageToPanelY(polyy[j]);
			}
			
			Polygon poly = new Polygon(xs, ys, xs.length);
			return poly;
		}

		protected void paintComponent(Graphics gg) {
			super.paintComponent(gg);
			Graphics2D g = (Graphics2D)gg;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			w = getWidth(); h = getHeight();
			if(w == 0 || h == 0) { return; }
			
			double aspect = (double)w / (double)h;
			imageScale = 1.0;
			xOffset = 0; 
			yOffset = 0;
			
			int imh = 0, imw = 0;
		
			if(aspect >= imageAspect) { 
				// scale to fit imageHeight
				imageScale = (double)h / (double)imageHeight;
				imh = h;
				imw = (int)Math.floor((double)imageWidth * imageScale);
				yOffset = 0;
				xOffset = (w - imw)/2;
				
			} else { 
				// scale to fit imageWidth
				imageScale = (double)w / (double)imageWidth;
				imw = w;
				imh = (int)Math.floor((double)imageHeight * imageScale);
				yOffset = (h - imh) / 2;
				xOffset = 0;
				
			}
			
			g.drawImage(image, xOffset, yOffset, imw, imh, null);

			int polyAlpha = 100;
			Color polyFill = Color.orange;
			polyFill = new Color(polyFill.getRed(), polyFill.getGreen(), polyFill.getBlue(), polyAlpha);
			Color polyLine = Color.black;
			
			Color polySel = Color.red;
			polySel = new Color(polySel.getRed(), polySel.getGreen(), polySel.getBlue(), polyAlpha);
			
			Stroke polyStroke = new BasicStroke((float)2.0);
			Stroke selStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
					1.0f, new float[] { 5.0f, 5.0f }, 0.0f);
			Stroke oldStroke = g.getStroke();
			
			for(int i = 0; i < polyX.size(); i++) { 
				int[] polyx = polyX.get(i), polyy = polyY.get(i);
				Polygon poly = getPanelPolygon(polyx, polyy);
				
				g.setColor(i == selectedPolygon ? polySel : polyFill);
				g.fill(poly);
				g.setColor(polyLine);
				g.setStroke(i == selectedPolygon ? selStroke : polyStroke);
				g.draw(poly);
				g.setStroke(oldStroke);
			}

			polyFill = Color.yellow;
			polyFill = new Color(polyFill.getRed(), polyFill.getGreen(), polyFill.getBlue(), polyAlpha);

			if(!currentX.isEmpty()) { 
				Polygon poly = getCurrentPanelPolygon();

				g.setColor(polyFill);
				g.fill(poly);
				g.setColor(polyLine);
				g.setStroke(polyStroke);
				g.draw(poly);
				g.setStroke(oldStroke);
			}
		}
	}
}
