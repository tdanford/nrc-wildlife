package edu.umass.nrc.warren.forestcover;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * 
 * An example SDW File
 * <blockquote>
 * 0.500000000000000
 * 0.000000000000000
 * 0.000000000000000
 * -0.500000000000000
 * 2100000.250000000000000
 * 699999.750000000000000
 * </blockquote>
 *
 * SDW files are metadata files that accompany MrSID files, one SDW file for each SID file.
 * 
 * As far as I can tell, they contain coordinate and scaling information -- the first and fourth
 * lines are (I believe) scales, and the fifth and sixth lines are (respectively) the X and Y coordinates.
 * 
 * These example coordinates, above, are the X and Y offsets for a file that is (at mag 1.0) 5000x5000 -- 
 * the coordinates themselves appear to be at the 0.5 magnification (therefore, the "next file over" has 
 * an X-coordinate that is + 2500 from this one).  
 * 
 * This class, SDWMetadata, simply parses the file so that I can use the coordinates later to piece together
 * multiple tiles into a single picture.
 * 
 * @author Timothy Danford
 */
public class SDWMetadata {
	
	public static SDWMetadata[] findMetadata(File dir, String prefix) throws IOException { 
		final Pattern p = Pattern.compile(String.format("%s.*\\.sdw", prefix));
		if(!dir.isDirectory() || !dir.canRead()) { 
			throw new IllegalArgumentException(dir.getAbsolutePath());
		}
		File[] fs = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return p.matcher(name).matches();
			} 
		});
		
		SDWMetadata[] metas = new SDWMetadata[fs.length];
		for(int i = 0; i < metas.length; i++) { 
			metas[i] = new SDWMetadata(fs[i]);
		}
		return metas;
	}

	private double magnification;
	private int x, y;
	private File sdw;
	private String filenameRoot;
	
	private static Pattern dotPattern = Pattern.compile("^([^\\.]+)\\.(.*)$");
	
	public SDWMetadata(File f) throws IOException { 
		sdw = f;
		Matcher m = dotPattern.matcher(f.getName());
		if(!m.matches()) { throw new IllegalArgumentException(f.getName()); }
		filenameRoot = m.group(1);
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		magnification = Double.parseDouble(br.readLine().trim());
		br.readLine(); 
		br.readLine();
		br.readLine();
		x = (int)Math.floor(Double.parseDouble(br.readLine().trim()));
		y = (int)Math.floor(Double.parseDouble(br.readLine().trim()));
		br.close();
	}
	
	public int x() { return x; }
	public int y() { return y; }
	
	public double mag() { return magnification; }
	
	public File getFile() { return sdw; }
	
	public File getFile(String extension) { 
		return new File(sdw.getParentFile(), String.format("%s.%s", filenameRoot, extension));
	}
}

