package edu.umass.nrc.warren.forestcover;

import java.io.File;
import java.util.*;

public class ForestCoverProperties {

	private ResourceBundle bundle;
	
	public ForestCoverProperties(String propertiesFile) { 
		bundle = ResourceBundle.getBundle(String.format("edu.umass.nrc.warren.forestcover.%s", propertiesFile));
	}
	
	public File getBaseDir() { 
		return new File(bundle.getString("base_dir"));
	}
}
