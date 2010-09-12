package edu.umass.nrc.warren.cavitynesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**

CREATE TABLE "cavitydata_nest" (
    "id" integer NOT NULL PRIMARY KEY,
    "cavity_id" integer NOT NULL REFERENCES "cavitydata_cavity" ("id"),
    "nestid" varchar(40) NULL,
    "observerinitials" varchar(20) NULL,
    "findmethod_id" integer NULL REFERENCES "cavitydata_nestfindmethods" ("id"),
    "species_id" integer NULL REFERENCES "cavitydata_birdspecies" ("id"),
    "nestfate_id" integer NULL REFERENCES "cavitydata_nestfate" ("id"),
    "notes" text NULL
);

 * @author tdanford
 *
 */
public class Nest extends CavityDBObject {
	
	public Integer id;
	public Integer cavity_id;
	public String nestid;
	public String observerinitials;
	public Integer findmethod_id;
	public Integer species_id;
	public Integer nestfate_id;
	public String notes;

	public Nest() {
	}

	public Nest(ResultSet rs) throws SQLException {
		super(rs);
	}
	
	public int hashCode() { return id.hashCode(); }
	
	public boolean equals(Object o) { 
		return o instanceof Nest && ((Nest)o).id.equals(id);
	}
	
	public Collection<Visit> loadVisits(Statement stmt) throws SQLException { 
		return loadByKey("id", Visit.class, "nest_id", stmt);
	}
	
	public String toString() { return nestid; }
}
