/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.util.*;

/**
 * 唯一路径阶段
 * 
 * @author scott.liang
 * @version 1.0 2019-10-24
 * @since laxcus 1.0
 */
final class PathType implements Comparable<PathType> {

	private String path;

	private int family;

	/**
	 * 构造唯一路径阶段
	 * @param s1
	 * @param s2
	 */
	public PathType(String path, int family) {
		super();
		this.setPath(path);
		this.setFamily(family);
	}
	
	public void setPath(String e){
		path = e;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setFamily(int who) {
		this.family = who;
	}
	
	public int getFamily(){
		return this.family;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PathType that) {
		int ret = Laxkit.compareTo(path, that.path);
		if (ret == 0) {
			ret = Laxkit.compareTo(family, that.family);
		}
		return ret;
	}

}
