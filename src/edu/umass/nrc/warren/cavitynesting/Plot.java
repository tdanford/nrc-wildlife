package edu.umass.nrc.warren.cavitynesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
CREATE TABLE "cavitydata_plot" (
    "id" integer NOT NULL PRIMARY KEY,
    "plotid" varchar(50) NOT NULL,
    "gps" varchar(50) NOT NULL,
    "address" varchar(100) NOT NULL,
    "observerinitials" varchar(20) NOT NULL,
    "notes" text NOT NULL
);

 * @author tdanford
 *
 */
public class Plot extends CavityDBObject {
	
	public Integer id;
	public String plotid;
	public String gps;
	public String address;
	public String observerinitials;
	public String notes;

	public Plot() {
	}

	public Plot(ResultSet rs) throws SQLException {
		super(rs);
	}
	
	public Collection<Tree> loadTrees(Statement stmt) throws SQLException { 
		return loadByKey("id", Tree.class, "plot_id", stmt);
	}
	
	public String toString() { return plotid; }
	
	public int hashCode() { return id.hashCode(); }
	
	public boolean equals(Object o) { 
		return o instanceof Plot && ((Plot)o).id.equals(id);
	}
}
