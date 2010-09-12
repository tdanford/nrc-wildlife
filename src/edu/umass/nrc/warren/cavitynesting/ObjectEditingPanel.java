package edu.umass.nrc.warren.cavitynesting;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.lang.reflect.*;

public class ObjectEditingPanel extends JPanel {
	
	private CavityDBObject object;

	public ObjectEditingPanel(CavityDBObject obj) { 
		super(new BorderLayout());
		object = obj;
		
		init();
	}
	
	private void init() { 
		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		add(list, BorderLayout.CENTER);
		
		for(Field f : object.getClass().getFields()) { 
			int mod = f.getModifiers();
			if(Modifier.isPublic(mod) && !Modifier.isStatic(mod)) { 
				Class fieldType = f.getType();
				String fieldName = f.getName();
				try {
					Object fieldValue = f.get(object);
					JComponent entryElement;
					
					if((CavityDBObject.isSubclass(fieldType, String.class)) && 
							fieldValue != null && ((String)fieldValue).length() > 40) {
						
						JTextArea textArea = new JTextArea();
						textArea.setColumns(40);
						textArea.setRows(10);
						textArea.setText(fieldValue.toString());
						entryElement = textArea;
						
					} else { 
						JTextField textEntryField = new JTextField();
						if(fieldValue != null) { textEntryField.setText(String.valueOf(fieldValue)); }
						entryElement = textEntryField;
					}
					
					JPanel pair = new JPanel(new FlowLayout());
					pair.add(new JLabel(fieldName));
					pair.add(entryElement);
					
					list.add(pair);
					
				} catch (IllegalAccessException e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}
	
	public CavityDBObject getObject() { return object; }
}

class ObjectEditingFrame extends JFrame { 

	public ObjectEditingFrame(ObjectEditingPanel panel) { 
		super(panel.getObject().toString());
		JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(true);
		setContentPane(content);
		content.add(panel, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		pack();
	}
}
