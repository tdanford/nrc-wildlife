package edu.umass.nrc.warren.cavitynesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
CREATE TABLE "cavitydata_visit" (
    "id" integer NOT NULL PRIMARY KEY,
    "nest_id" integer NOT NULL REFERENCES "cavitydata_nest" ("id"),
    "date" datetime NULL,
    "observerinitials" varchar(20) NULL,
    "eggs" varchar(20) NULL,
    "nestlings" varchar(20) NULL,
    "fledglings" varchar(20) NULL,
    "fledgedcertainty" varchar(20) NULL,
    "status_id" integer NULL REFERENCES "cavitydata_neststatus" ("id"),
    "stage_id" integer NULL REFERENCES "cavitydata_neststage" ("id"),
    "notes" text NULL
);
 * @author tdanford
 *
 */
public class Visit extends CavityDBObject {
	
	static DateFormat format_date;
	
	static { 
		format_date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	}
	
	public Integer id;
	public Integer nest_id;
	public Date date;  // datetime?
	public String observerinitials;
	public String eggs;
	public String nestlings;
	public String fledglings;
	public String fledgedcertainty;
	public Integer status_id;
	public Integer stage_id;
	public String notes;

	public Visit() {
	}

	public Visit(ResultSet rs) throws SQLException {
		super(rs);
	}

	public int hashCode() { return id.hashCode(); }
	public boolean equals(Object o) { return o instanceof Visit && ((Visit)o).id.equals(id); }
	
	public String toString() { return format_date.format(date); } 
}
