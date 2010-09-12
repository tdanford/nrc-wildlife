package edu.umass.nrc.warren.cavitynesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
CREATE TABLE "cavitydata_cavity" (
    "id" integer NOT NULL PRIMARY KEY,
    "tree_id" integer NOT NULL REFERENCES "cavitydata_tree" ("id"),
    "cavityid" varchar(40) NULL,
    "observerinitials" varchar(20) NULL,
    "location_id" integer NULL REFERENCES "cavitydata_cavitylocations" ("id"),
    "heightfromground" varchar(20) NULL,
    "distfromtrunk" varchar(20) NULL,
    "disttobranchend" varchar(20) NULL,
    "cavitydiam" varchar(20) NULL,
    "branchdiam" varchar(20) NULL,
    "orientation" varchar(10) NULL,
    "notes" text NULL
);

 * @author tdanford
 *
 */
public class Cavity extends CavityDBObject {
	
	public Integer id;
	public Integer tree_id;
	public String cavityid;
	public String observerinitials;
	public Integer location_id;
	public String heightfromground;
	public String distfromtrunk;
	public String disttobranchend;
	public String cavitydiam;
	public String branchdiam;
	public String orientation;
	public String notes;

	public Cavity() {
	}

	public Cavity(ResultSet rs) throws SQLException {
		super(rs);
	}
	
	public String toString() { return cavityid; }
	
	public int hashCode() { return id.hashCode(); }
	
	public boolean equals(Object o) { return o instanceof Cavity && ((Cavity)o).id.equals(id); }
	
	public Collection<Nest> loadNests(Statement stmt) throws SQLException { 
		return loadByKey("id", Nest.class, "cavity_id", stmt);
	}

}
