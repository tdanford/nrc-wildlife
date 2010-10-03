package edu.umass.nrc.warren.forestcover;

import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

public class TIFFConverter {
	
	public static void main(String[] args) throws IOException { 
		File dir = new ForestCoverProperties(args[0]).getBaseDir();
		
		final Pattern p = Pattern.compile("^.*\\.tiff?$");
		
		File[] lst = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return p.matcher(name.toLowerCase()).matches();
			} 
		});
		
		for(File f : lst) { 
			TIFFConverter converter = new TIFFConverter(f);
			converter.savePNG();
		}
	}
	
	private RenderedImage rendered;
	private BufferedImage buffered;
	
	private String root; 
	private File path, input;

	public TIFFConverter(File input) {
		
		this.input = input;
		if(input.isDirectory() || !input.canRead()) { 
			throw new IllegalArgumentException(input.getAbsolutePath());
		}
		
		Pattern p = Pattern.compile("(.*)\\.(?:t|T)(?:i|I)(?:f|F)(?:f|F)?");
		Matcher m = p.matcher(input.getName());
		if(!m.matches()) { 
			throw new IllegalArgumentException(input.getName());
		}
		root = m.group(1);
		path = input.getParentFile();
		
		rendered = JAI.create("fileload", input.getAbsolutePath());
		buffered = new BufferedImage(rendered.getWidth(), rendered.getHeight(), BufferedImage.TYPE_INT_RGB);
		buffered.setData(rendered.getData());
	}
	
	public void savePNG() throws IOException { 
		File output = new File(path, String.format("%s.png", root));
		ImageIO.write(rendered, "PNG", output);
		System.out.println(String.format("%s -> %s", input.getAbsolutePath(), output.getAbsolutePath()));
	}
	
}
