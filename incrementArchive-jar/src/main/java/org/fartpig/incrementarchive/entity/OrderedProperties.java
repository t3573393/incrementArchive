package org.fartpig.incrementarchive.entity;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class OrderedProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7762814437307741988L;

	private Map<Object, Object> linkMap = new LinkedHashMap<Object, Object>();

	public synchronized Object put(Object key, Object value) {
		return linkMap.put(key, value);
	}

	public synchronized boolean contains(Object key) {
		return linkMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return linkMap.containsValue(value);
	}

	public synchronized Enumeration<Object> elements() {
		throw new UnsupportedOperationException(
				"Enumerations are so old-school, use the keySet() or entrySet instead ");
	}

	public Set<Map.Entry<Object, Object>> entrySet() {
		return linkMap.entrySet();
	}

	public synchronized void clear() {
		linkMap.clear();
	}

	public synchronized boolean containsKey(Object key) {
		return linkMap.containsKey(key);
	}

}
