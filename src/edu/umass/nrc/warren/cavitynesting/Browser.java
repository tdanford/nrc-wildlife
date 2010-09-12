package edu.umass.nrc.warren.cavitynesting;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

class NodeDisplay extends JPanel {
	
	private JEditorPane editorPane;
	
	public NodeDisplay() { 
		super(new BorderLayout());
		//setPreferredSize(new Dimension(300, 300));
		editorPane = new JEditorPane();
		editorPane.setEditorKit(new HTMLEditorKit());
		add(new JScrollPane(editorPane), BorderLayout.CENTER);
	}
	
	public void display(CavityDBObject obj) { 
		String str = obj.asString();
		//System.out.println(String.format("Displaying:\n%s", str));
		editorPane.setText(str);
		revalidate();
	}
}

public class Browser extends JPanel {
	
	private CavityNestingDB db;
	private DefaultMutableTreeNode top;
	private JTree tree;
	private NodeDisplay displayer;

	public Browser(CavityNestingDB cndb) throws SQLException { 
		super(new BorderLayout());
		setPreferredSize(new Dimension(300, 500));
		db = cndb;
		displayer = new NodeDisplay();
		
		top = new DefaultMutableTreeNode("CavityNestingDB");
		tree = new JTree(top);
		DefaultTreeSelectionModel selModel = new DefaultTreeSelectionModel();
		selModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(selModel);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if(e.isAddedPath()) { 
					TreePath path = e.getPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					Object value = node.getUserObject(); 

					//System.out.println(String.valueOf(value));
					if(value instanceof CavityDBObject) { 
						displayer.display((CavityDBObject)value);
					} else { 
						//System.out.println(value.getClass().getSimpleName());
					}
				}
			} 
		});
		
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(splitter, BorderLayout.CENTER);
		splitter.add(new JScrollPane(tree));
		splitter.add(displayer);
		
		populate();
	}
	
	public void closeBrowser() { 
		try {
			db.close();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private void populate() throws SQLException { 
		Collection<Plot> plots = db.select(Plot.class).values();
		for(Plot p : plots) { 
			top.add(populatePlot(p));
		}
	}
	
	private MutableTreeNode populatePlot(Plot p) throws SQLException { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(p);
		//tree.add(populateAttributes(p));
		Statement stmt = db.statement();
		for(Tree t : p.loadTrees(stmt)) { 
			tree.add(populateTree(t));
		}
		stmt.close();
		return tree;
	}

	private MutableTreeNode populateTree(Tree t) throws SQLException { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(t);
		//tree.add(populateAttributes(t));
		Statement stmt = db.statement();
		for(Cavity c : t.loadCavities(stmt)) { 
			tree.add(populateCavity(c));
		}
		stmt.close();
		return tree;
	}

	private MutableTreeNode populateCavity(Cavity c) throws SQLException { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(c);
		//tree.add(populateAttributes(c));
		Statement stmt = db.statement();
		for(Nest n : c.loadNests(stmt)) { 
			tree.add(populateNest(n));
		}
		stmt.close();
		return tree;
	}

	private MutableTreeNode populateNest(Nest n) throws SQLException { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(n);
		//tree.add(populateAttributes(n));
		Statement stmt = db.statement();
		for(Visit v : n.loadVisits(stmt)) { 
			tree.add(populateVisit(v));
		}
		stmt.close();
		return tree;
	}

	private MutableTreeNode populateVisit(Visit v) throws SQLException { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(v);
		//tree.add(populateAttributes(v));
		return tree;
	}
	
	private MutableTreeNode populateAttributes(CavityDBObject obj) { 
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode("attrs");
		Class cls = obj.getClass();
		for(Field f : cls.getFields()) { 
			int mod = f.getModifiers();
			if(Modifier.isPublic(mod) && !Modifier.isStatic(mod)) { 
				String fieldName = f.getName();
				try {
					Object value = f.get(obj);
					tree.add(new DefaultMutableTreeNode(String.format("%s=%s", fieldName, String.valueOf(value))));
					
				} catch (IllegalAccessException e) {
					// do nothing.
				}
			}
		}
		return tree;		
	}
	
	public void editSelected() { 
		TreePath selected = tree.getSelectionPath();
		if(selected != null) { 
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)selected.getLastPathComponent();
			Object obj = node.getUserObject();
			if(obj instanceof CavityDBObject) { 
				CavityDBObject dbObj = (CavityDBObject)obj;
				new ObjectEditingFrame(new ObjectEditingPanel(dbObj));
			}
		}
	}
}

class BrowserFrame extends JFrame { 
	
	private Browser browser;
	private JMenuBar menubar;
	
	public BrowserFrame(Browser b) { 
		super("Cavity Nesting DB Browser");
		browser = b;
		
		JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(true);
		setContentPane(content);
		
		content.add(browser, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				browser.closeBrowser();
			}
		});
		
		createJMenuBar();
		setJMenuBar(menubar);
		
		setVisible(true);
		pack();
	}
	
	private void createJMenuBar() {
		menubar = new JMenuBar();
		JMenu menu = null;
		JMenuItem item = null;
		
		menubar.add(menu = new JMenu("Edit"));
		menu.add(item = new JMenuItem("Edit Object..."));
		
		item.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				browser.editSelected();
			}
		});
	}
}
