package edu.umass.nrc.warren.cavitynesting;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class Selection {

	private LinkedList values;
	
	public Selection() { 
		values = new LinkedList();
	}
	
	public Selection(Collection vs) { 
		this();
		for(Object v : vs) { add(v); }
	}
	
	public void add(Object obj) { 
		if(obj != null) { 
			if(obj.getClass().isArray()) { 
				for(int i = 0; i < Array.getLength(obj); i++) { 
					add(Array.get(obj, i));
				}
			} else if (obj instanceof Collection) { 
				Collection c = (Collection)obj;
				for(Object v : c) { add(v); }
			} else { 
				values.add(obj);
			}
		}
	}
	
	public Selection join(String fieldName, Selection inner) { 
		Selection s = new Selection();
		for(Object value : inner.values) { 
			Class cls = value.getClass();
			try {
				Field f = cls.getField(fieldName);
				Object fvalue = f.get(value);
				if(values.contains(fvalue)) { 
					s.add(value);
				}
				
			} catch (SecurityException e) {
				// do nothing.
			} catch (NoSuchFieldException e) {
				// do nothing.
			} catch (IllegalArgumentException e) {
				// do nothing.
			} catch (IllegalAccessException e) {
				// do nothing.
			}
		}
		return s;
	}
	
	public Selection isA(Class cls) { 
		Selection s = new Selection();
		for(Object v : values) { 
			if(CavityDBObject.isSubclass(v.getClass(), cls)) { 
				s.add(v);
			}
		}
		return s;
	}
	
	public Selection call(String methodName, Object... args) { 
		Class[] params = new Class[args.length];
		for(int i = 0; i < args.length; i++) { params[i] = args[i].getClass(); }

		Selection s = new Selection();
		for(Object val : values) { 
			Class cls = val.getClass();
			try {
				Method m = cls.getMethod(methodName, params);
				Object returned =  m.invoke(val, args);
				s.add(returned);

			} catch (SecurityException e) {
				// do nothing.
			} catch (NoSuchMethodException e) {
				// do nothing.
			} catch (IllegalAccessException e) {
				// do nothing.
			} catch (InvocationTargetException e) {
				// do nothing.
			}
		}
		
		return s;		

	}
	
	public Selection get(String fieldName) { 
		Selection s = new Selection();
		for(Object val : values) { 
			Class cls = val.getClass();
			try {
				Field f = cls.getField(fieldName);
				Object fval = f.get(val);
				s.add(fval);

			} catch (NoSuchFieldException e) {
				// do nothing.
			} catch (IllegalAccessException e) {
				// do nothing.
			}
		}
		
		return s;		
	}
	
	public Selection fieldEquals(String fieldName, Object fieldValue) { 
		Selection s = new Selection();
		for(Object val : values) { 
			Class cls = val.getClass();
			try {
				Field f = cls.getField(fieldName);
				Object fval = f.get(val);
				if(fieldValue == null) { 
					if(fval==null) { 
						s.add(val);
					}
				} else { 
					if(fval != null && fval.equals(fieldValue)) { 
						s.add(val);
					}
				}

			} catch (NoSuchFieldException e) {
				// do nothing.
			} catch (IllegalAccessException e) {
				// do nothing.
			}
		}
		
		return s;
	}
	
	public <T> T find(String fieldName, Object fieldValue) { 
		for(Object v : values) { 
			Class cls = v.getClass();
			try {
				Field f = cls.getField(fieldName);
				Object fvalue = f.get(v);
				if(fieldValue == null) { 
					if(fvalue == null) { 
						return (T)v;
					}
				} else { 
					if(fvalue != null && fvalue.equals(fieldValue)) {
						return (T)v;
					}
				}
				
			} catch (SecurityException e) {
				// do nothing
			} catch (NoSuchFieldException e) {
				// do nothing
			} catch (IllegalArgumentException e) {
				// do nothing
			} catch (IllegalAccessException e) {
				// do nothing
			}
		}
		return null;
	}
	
	public <T> Collection<T> values() { 
		return new LinkedList<T>(values);
	}
}
