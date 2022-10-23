/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.build;

import java.io.*;

import javax.swing.*;

import com.laxcus.util.*;

/**
 * 比较单元
 * 
 * @author scott.liang
 * @version 1.0 7/26/2021
 * @since laxcus 1.0
 */
class BuildItem implements Comparable<BuildItem> {

	/** JAR属性文件 **/
	private static final String JAR_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.JAR)\\s*$";
	
	private static final String LIB_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.DLL|\\.SO)\\s*$";
	
	private static final String TEXT_SUFFIX = "^\\s*([\\w\\W]+)(\\.)(?i)(TXT|TEXT)\\s*$";
	
	private static final String ICON_SUFFIX= "^\\s*([\\w\\W]+)(\\.)(?i)(PNG|GIF|JPG|JPEG)\\s*$";

	private File file;
	
	/** 图标 **/
	private Icon icon;

	public BuildItem(File file) {
		super();
		setFile(file);
	}

	public void setFile(File e) {
		file = e;
	}

	public File getFile() {
		return file;
	}

	public String getFilename() {
		return Laxkit.canonical(file);
	}

	public String getSuffix() {
		String filename = getFilename();
		int last = filename.lastIndexOf('.');
		if (last < 1) {
			return "";
		}
		return filename.substring(last + 1);
	}
	
	
	public boolean isJar() {
		String name = getFilename();
		return name.matches(JAR_SUFFIX);
	}

	public boolean isLibrary() {
		String name = getFilename();
		return name.matches(LIB_SUFFIX);
	}

	public boolean isText() {
		String name = getFilename();
		return name.matches(TEXT_SUFFIX);
	}

	public boolean isIcon() {
		String name = getFilename();
		return name.matches(ICON_SUFFIX);
	}

	public void setIcon(Icon e) {
		icon = e;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return file.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		return compareTo((BuildItem) o) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BuildItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(file, that.file);
	}

}