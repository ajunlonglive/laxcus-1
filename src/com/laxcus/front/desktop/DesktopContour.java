/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 按纽矩形
 * 
 * @author scott.liang
 * @version 1.0 7/30/2021
 * @since laxcus 1.0
 */
final class DesktopContour implements Classable, Cloneable, Comparable<DesktopContour> {
	
	public int x;
	
	public int y;
	
	public int width ;
	
	public int height;

	/**
	 * 
	 */
	public DesktopContour() {
		super();
		x = 0;
		y =0;
		this.width =0;
		this.height =0;
	}
	
	public DesktopContour(int x, int y, int width, int height) {
		super();
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
	}

	private DesktopContour(DesktopContour that) {
		this();
		this.x = that.x;
		this.y = that.y;
		this.width = that.width;
		this.height = that.height;
	}

	public DesktopContour(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	public void setX(int i){
		x = i;
	}
	public int getX() {
		return x;
	}
	
	public void setY(int i){
		y = i;
	}
	public int getY() {
		return y;
	}
	
	public void setWidth(int i){
		width = i;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int i){
		height = i;
	}
	
	public int getHeight() {
		return height;
	}
	
	public DesktopContour duplicate() {
		return new DesktopContour(this);
	}
	
	public int hashCode() {
		return ( x ^ y ^ width ^ height);
	}
	
	public boolean equals(Object o) {
		if(o == null || o.getClass() != this.getClass()) {
			return false;
		}
		return this.compareTo((DesktopContour)o) ==0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DesktopContour that) {
		if (that == null) {
			return 1;
		}
		// 坐标判断
		int ret = Laxkit.compareTo(x, that.x);
		if (ret == 0) {
			ret = Laxkit.compareTo(y, that.y);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(width, that.width);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(height, that.height);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		
		writer.writeInt(x);
		writer.writeInt(y);
		writer.writeInt(width);
		writer.writeInt(height);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		
		x = reader.readInt();
		y = reader.readInt();
		width= reader.readInt();
		height = reader.readInt();
		return reader.getSeek() - seek;
	}

}
