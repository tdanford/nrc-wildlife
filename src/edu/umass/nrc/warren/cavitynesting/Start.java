/*
 * Tree-based Browser and Editor for the Cavity Nesting Database.
 * 
 * Timothy Danford
 * tdanford@gmail.com 
 */
package edu.umass.nrc.warren.cavitynesting;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.*;

import org.sqlite.JDBC;

public class Start {

	public static void main(String[] args) throws SQLException {
		CavityNestingDB db = null;
		try {
			String path = args.length > 0 ? args[0] : "/Users/tdanford/Documents/Personal/cavitynesting.db";
			db = new CavityNestingDB(path);
			
			new BrowserFrame(new Browser(db));
			
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
	}
}
