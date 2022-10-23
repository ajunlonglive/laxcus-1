/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 按纽矩形
 * 
 * @author scott.liang
 * @version 1.0 7/30/2021
 * @since laxcus 1.0
 */
final class RayContour implements Classable, Cloneable, Comparable<RayContour> {
	
	public int x;
	
	public int y;
	
	public int width ;
	
	public int height;

	/**
	 * 构造默认的按纽矩形
	 */
	public RayContour() {
		super();
		x = 0;
		y = 0;
		width = 0;
		height = 0;
	}
	
	/**
	 * 构造按纽矩形
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public RayContour(int x, int y, int width, int height) {
		super();
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	private RayContour(RayContour that) {
		this();
		x = that.x;
		y = that.y;
		width = that.width;
		height = that.height;
	}

	public RayContour(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	public void setX(int i) {
		x = i;
	}

	public int getX() {
		return x;
	}

	public void setY(int i) {
		y = i;
	}

	public int getY() {
		return y;
	}

	public void setWidth(int i) {
		width = i;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int i) {
		height = i;
	}

	public int getHeight() {
		return height;
	}
	
	public RayContour duplicate() {
		return new RayContour(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ( x ^ y ^ width ^ height);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || o.getClass() != getClass()) {
			return false;
		}
		return compareTo((RayContour)o) ==0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RayContour that) {
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
		width = reader.readInt();
		height = reader.readInt();
		return reader.getSeek() - seek;
	}

}