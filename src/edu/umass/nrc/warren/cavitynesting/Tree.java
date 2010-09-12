package edu.umass.nrc.warren.cavitynesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
CREATE TABLE "cavitydata_tree" (
    "id" integer NOT NULL PRIMARY KEY,
    "plot_id" integer NOT NULL REFERENCES "cavitydata_plot" ("id"),
    "treeid" varchar(50) NOT NULL,
    "species_id" integer NOT NULL REFERENCES "cavitydata_treespecies" ("id"),
    "canopyheight" varchar(20) NULL,
    "height" decimal NULL,
    "gps" varchar(50) NULL,
    "crowndimensionEW" varchar(20) NULL,
    "crowndimensionNS" varchar(20) NULL,
    "pctdead" integer NULL,
    "dbh" decimal NULL,
    "smalldeadbranches" varchar(50) NULL,
    "mediumdeadbranches" varchar(50) NULL,
    "largedeadbranches" varchar(50) NULL,
    "slopeaspect" varchar(20) NULL,
    "positionslope" varchar(20) NULL,
    "observerinitials" varchar(20) NOT NULL,
    "notes" text NULL,
    "address" varchar(100) NULL,
    "tag" integer NULL,
    "numcavities" integer NULL,
    "date" date NULL,
    "decayclass_id" integer NULL REFERENCES "cavitydata_decayclass" ("id"),
    "hazard_species_id" integer NOT NULL REFERENCES "cavitydata_treespecies" ("id"),
    "hazard_dbh" decimal NULL,
    "hazard_probtarget" integer NULL,
    "hazard_defectivesize" integer NULL,
    "hazard_probfail" integer NULL,
    "hazard_speciesrating" integer NULL,
    "hazard_observer" varchar(20) NULL,
    "hazard_action" text NULL,
    "hazard_date" date NULL,
    "hazard_notes" text NULL
);

 * @author tdanford
 *
 */
public class Tree extends CavityDBObject {
	
	static { 
		
	}
	
	public Integer id;
	public Integer plot_id;
	public String treeid;
	public Integer species_id;
	public String canopyheight;
	public Double height;
	public String gps;
	public String crowndimensionEW, crowndimensionNS;
	public Integer pctdead;
	public Double dbh;
	public String smalldeadbranches, mediumdeadbranches, largedeadbranches;
	public String slopeaspect;
	public String positionslope;
	public String observerinitials;
	public String notes;
	public String address;
	public Integer tag;
	public Integer numcavities;
	public Date date;
	public Integer decayclass_id;
	public Integer hazard_species_id;
	public Double hazard_dbh;
	public Integer hazard_probtarget;
	public Integer hazard_defectivesize;
	public Integer hazard_probfail;
	public Integer hazard_speciesrating;
	public String hazard_observer;
	public String hazard_action;
	public Date hazard_date;
	public String hazard_notes;
	
	public Tree() {
	}

	public Tree(ResultSet rs) throws SQLException {
		super(rs);
	}
	
	public String toString() { return treeid; }
	
	public int hashCode() { return id.hashCode(); }
	
	public boolean equals(Object o) { 
		if(!(o instanceof Tree)) { return false; }
		return ((Tree)o).id.equals(id);
	}
	
	public Collection<Cavity> loadCavities(Statement stmt) throws SQLException { 
		return loadByKey("id", Cavity.class, "tree_id", stmt);
	}

}
