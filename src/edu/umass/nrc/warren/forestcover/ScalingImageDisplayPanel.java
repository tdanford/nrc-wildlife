package edu.umass.nrc.warren.forestcover;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

public class ScalingImageDisplayPanel extends JPanel {
	
	public static void main(String[] args) throws IOException { 
		File dir = new ForestCoverProperties("wake").getBaseDir();
		File f = new File(dir, "wilders_ridge/composite.png");
		BufferedImage image = ImageIO.read(f);
		
		int iw = image.getWidth(), ih = image.getHeight();
		double aspect = (double)iw / (double)ih;
		
		int ph = 300;
		int pw = (int)(aspect * ph);
		
		new ScalingImageDisplayPanel(ph, pw, image).new DisplayFrame();
	}

	private BufferedImage image;
	
	private double imageScale;
	private int xOffset, yOffset;  // These are in the scale of the panel, not the image.
	
	private int panelWidth, panelHeight;
	
	private int imageWidth, imageHeight;
	private double imageAspect; // == imageWidth / imageHeight;
	
	private Point dragStart;
	private int dx, dy;
	
	public ScalingImageDisplayPanel(int w, int h, BufferedImage im) {
		
		setPreferredSize(new Dimension(w, h));
		panelWidth = w;
		panelHeight = h;
		
		image = im;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		imageAspect = (double)imageWidth / (double)imageHeight;
		
		dragStart = null;
		dx = dy = 0;
		
		fit();
		
		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				dragStart = e.getPoint();
			}

			public void mouseReleased(MouseEvent e) {
				dragStart = null;
				xOffset += dx; yOffset += dy;
				dx = dy = 0;
			} 			
		});
		
		addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				Point pt = e.getPoint();
				dx = pt.x - dragStart.x;
				dy = pt.y - dragStart.y;
				repaint();
			}

			public void mouseMoved(MouseEvent e) {
			} 
		});
	}
	
	public void zoomToFit() { 
		panelWidth = getWidth();
		panelHeight = getHeight();
		fit();
	}
	
	public void zoom(double amt) {
		imageScale *= amt;
		xOffset = (int)Math.round((double)xOffset * amt);
		yOffset = (int)Math.round((double)yOffset * amt);
	}
	
	private void fit() {
		double aspect = (double)panelWidth / (double)panelHeight;
		imageScale = 1.0;
		xOffset = 0; 
		yOffset = 0;
		
		int imh = 0, imw = 0;
	
		if(aspect >= imageAspect) { 
			// scale to fit imageHeight
			imageScale = (double)panelHeight / (double)imageHeight;
			imh = panelHeight;
			imw = (int)Math.floor((double)imageWidth * imageScale);
			yOffset = 0;
			xOffset = (panelWidth - imw)/2;
			
		} else { 
			// scale to fit imageWidth
			imageScale = (double)panelWidth / (double)imageWidth;
			imw = panelWidth;
			imh = (int)Math.floor((double)imageHeight * imageScale);
			yOffset = (panelHeight - imh) / 2;
			xOffset = 0;
		}
	}
	
	protected void paintComponent(Graphics g) { 
		super.paintComponent(g);
		
		//zoomToFit();
		
		Graphics2D g2 = (Graphics2D)g;
		g2.translate(xOffset + dx, yOffset + dy);
		g2.scale(imageScale, imageScale);
		
		g2.drawImage(image, 0, 0, imageWidth, imageHeight, null);

		g2.scale(1.0/imageScale, 1.0/imageScale);
		g2.translate(- (xOffset + dx), - (yOffset + dy));
	}
	
	public class DisplayFrame extends JFrame { 
		
		public DisplayFrame() { 
			super("Image Display");
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			Container c = (Container)getContentPane();
			c.setLayout(new BorderLayout());
			c.add(ScalingImageDisplayPanel.this, BorderLayout.CENTER);
			
			JPanel buttons = new JPanel(new FlowLayout());
			c.add(buttons, BorderLayout.SOUTH);
			
			JButton zoomIn, zoomOut;
			buttons.add(zoomOut = new JButton("-"));
			buttons.add(zoomIn = new JButton("+"));
			
			zoomIn.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) { 
					zoom(2.0);
					ScalingImageDisplayPanel.this.repaint();
				}
			});
			zoomOut.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) { 
					zoom(0.5);
					ScalingImageDisplayPanel.this.repaint();
				}
			});
			
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
				}
			});
			
			addWindowStateListener(new WindowStateListener() {
				public void windowStateChanged(WindowEvent e) {
				}
			});
			
			addWindowListener(new WindowListener(){
				public void windowActivated(WindowEvent e) {
				}

				public void windowClosed(WindowEvent e) {
				}

				public void windowClosing(WindowEvent e) {
				}

				public void windowDeactivated(WindowEvent e) {
				}

				public void windowDeiconified(WindowEvent e) {
				}

				public void windowIconified(WindowEvent e) {
				}

				public void windowOpened(WindowEvent e) {
				} 
			});
			
			setVisible(true);
			pack();
		}
		
		public void resize() { 
			ScalingImageDisplayPanel p = ScalingImageDisplayPanel.this;
			p.zoomToFit();
			p.repaint();			
		}
	}
	
}
