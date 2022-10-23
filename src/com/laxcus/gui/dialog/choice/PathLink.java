/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.choice;

import java.io.*;

import com.laxcus.util.*;

/**
 * 路径链
 * 
 * @author scott.liang
 * @version 1.0 9/6/2021
 * @since laxcus 1.0
 */
final class PathLink implements Comparable<PathLink> {

	File path;

	String name;

	/**
	 * 构造默认的路径链
	 */
	public PathLink() {
		super();
	}
	
	/**
	 * 构造路径链
	 * @param path
	 * @param name
	 */
	public PathLink(File path, String name) {
		this();
		setPath(path);
		setName(name);
	}

	public void setPath(File e) {
		path = e;
	}

	public File getPath() {
		return path;
	}

	public void setName(String s) {
		name = s;
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PathLink that) {
		if (path == null) {
			return 1;
		}
		return Laxkit.compareTo(path, that.path);
	}

}