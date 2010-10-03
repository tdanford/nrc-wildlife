package edu.umass.nrc.warren.forestcover;

import java.util.*;
import java.io.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.swing.*;

public class PlotReconstruction {
	
	public static void main(String[] args) throws IOException {
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		File dir = new ForestCoverProperties(args[0]).getBaseDir();
		dir = new File(dir, "wilders_ridge");
		SDWMetadata[] metas = SDWMetadata.findMetadata(dir, "");
		PlotReconstruction reconstruct = new PlotReconstruction(2500, 2500, metas);
		
		reconstruct.new PlotFrame();
		
		//reconstruct.loadImages();
	}

	private int tileWidth, tileHeight;
	private int minX, maxX, minY, maxY;
	private int compositeWidth, compositeHeight;
	
	private SDWMetadata[] metadata;
	private BufferedImage composite;
	private Graphics2D compositeGraphics;
	
	private LinkedList<ImageLoadingListener> listeners;
	
	public PlotReconstruction(int w, int h, SDWMetadata... metas) { 
		tileWidth = w;
		tileHeight = h;
		metadata = metas.clone();
		listeners = new LinkedList<ImageLoadingListener>();

		minX = maxX = minY = maxY = 0;
		
		for(int i = 0; i < metadata.length; i++) { 
			if(i == 0) { 
				minX = maxX = metadata[i].x();
				minY = maxY = metadata[i].y();
			} else { 
				minX = Math.min(minX, metadata[i].x());
				maxX = Math.max(maxX, metadata[i].x());
				minY = Math.min(minY, metadata[i].y());
				maxY = Math.max(maxY, metadata[i].y());
			}
		}

		compositeWidth = maxX - minX + tileWidth;
		compositeHeight = maxY - minY + tileHeight;
		composite = new BufferedImage(compositeWidth, compositeHeight, BufferedImage.TYPE_INT_RGB);
		compositeGraphics = composite.createGraphics();
		
		compositeGraphics.setColor(Color.white);
		compositeGraphics.fillRect(0, 0, compositeWidth, compositeHeight);
	}
	
	public void loadImages() { 
		Runnable r = new Runnable() { 
			public void run() { 
				for(int i = 0; i < metadata.length; i++) { 
					loadImage(i);
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	private void loadImage(int i) {
		RenderedImage rendered = JAI.create("fileload", metadata[i].getFile("tif").getAbsolutePath());
		BufferedImage image = new BufferedImage(rendered.getWidth(), rendered.getHeight(), BufferedImage.TYPE_INT_RGB);
		image.setData(rendered.getData());

		rendered = null;
		renderImage(compositeGraphics, compositeWidth, compositeHeight, image, metadata[i]);
		
		for(ImageLoadingListener ll : listeners) { 
			ll.imageLoaded(i);
		}
	}
	
	public Action loadImagesAction() { 
		return new AbstractAction("Load Images") { 
			public void actionPerformed(ActionEvent e) { 
				loadImages();
			}
		};
	}
	
	public Action saveCompositeAction() { 
		return new AbstractAction("Save Composite Image") { 
			public void actionPerformed(ActionEvent e) { 
				File output = new File("composite.png");
				try {
					ImageIO.write(composite, "PNG", output);
				} catch (IOException e1) {
					e1.printStackTrace(System.err);
				}
			}
		};
	}
	
	public class PlotFrame extends JFrame {
		
		private PlotPanel pp;

		public PlotFrame() { 
			super("Plot Coverage");
			pp = new PlotPanel();
			pp.setOpaque(true);
			setContentPane(pp);
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			pp.setPreferredSize(new Dimension(800, 400));
			
			setJMenuBar(createMenu());
			
			setVisible(true);
			pack();
		}
		
		public void dispose() { 
			super.dispose();
			pp.dispose();
		}
		
		public JMenuBar createMenu() { 
			JMenuBar bar = new JMenuBar();
			JMenu menu = null;
			JMenuItem item = null;
			
			bar.add(menu = new JMenu("File"));
			menu.add(item = new JMenuItem(loadImagesAction()));
			menu.add(item = new JMenuItem(saveCompositeAction()));
			
			menu.add(new JSeparator());
			menu.add(item = new JMenuItem("Quit PlotReconstructor"));
			item.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) { 
					dispose();
				}
			});
			
			return bar;
		}
	}
	
	public class PlotPanel extends JPanel implements ImageLoadingListener { 
		public PlotPanel() { 
			super();
			PlotReconstruction.this.listeners.add(this);
		}
		
		protected void paintComponent(Graphics g) { 
			super.paintComponent(g);
			int w = getWidth(), h = getHeight();
			if(w > 0 && h > 0) { 
				paintCoverage((Graphics2D)g, w, h);
			}
		}
		
		public void dispose() {
			PlotReconstruction.this.listeners.remove(this);
		}

		public void imageLoaded(int idx) {
			repaint();
		}
	}
	
	public static interface ImageLoadingListener { 
		public void imageLoaded(int idx);
	}
	
	public void renderImage(Graphics2D g, int w, int h, Image im, SDWMetadata m) { 
		int sw = maxX + tileWidth - minX;
		int sh = maxY + tileHeight - minY;

		double scaleX = (double)w / (double)sw;
		double scaleY = (double)h / (double)sh;

		int x1 = m.x()-minX, x2 = m.x()-minX + tileWidth;
		int y1 = m.y()-minY, y2 = m.y()-minY + tileHeight;

		int gx1 = (int)Math.round((double)x1 * scaleX);
		int gx2 = (int)Math.round((double)x2 * scaleX);

		int gy2 = h-(int)Math.round((double)y1 * scaleY);
		int gy1 = h-(int)Math.round((double)y2 * scaleY);

		int gw = gx2-gx1, gh = gy2-gy1;

		if(im == null) { 
			g.setColor(new Color(255, 0, 0, 150));
			g.fillRect(gx1, gy1, gw, gh);

			g.setColor(Color.black);
			g.drawRect(gx1, gy1, gw, gh);

			g.drawString( m.getFile().getName(), gx1 + 2, gy1 + gh/2);

		} else { 
			g.drawImage(im, gx1, gy1, gw, gh, null);
		}
	}
	
	public void paintCoverage(Graphics2D g, int w, int h) {
		g.drawImage(composite, 0, 0, w, h, null);		
	}
}
