package edu.umass.nrc.warren.cavitynesting;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.*;

import org.sqlite.JDBC;

public class CavityNestingDB {
	
	static { 
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private String dbPath;
	private Connection cxn;
	
	public CavityNestingDB(String p) throws SQLException { 
		dbPath = p;
		cxn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);		
	}
	
	public void close() throws SQLException { 
		cxn.close();
	}
	
	public Statement statement() throws SQLException { 
		return cxn.createStatement();
	}

	public <T extends CavityDBObject> Selection select(Class<T> cls) throws SQLException {  
		Statement stmt = cxn.createStatement();
		Collection<T> list = CavityDBObject.loadAll(cls, stmt);
		stmt.close();
		return new Selection(list);
	}
}
