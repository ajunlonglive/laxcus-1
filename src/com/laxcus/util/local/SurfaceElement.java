/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.local;

import java.util.*;

import com.laxcus.util.*;

/**
 * XML标签单元
 * 
 * @author scott.liang
 * @version 1.0 9/21/2018
 * @since laxcus 1.0
 */
public class SurfaceElement implements Comparable<SurfaceElement> {
	
	/** XML路径，做为关键字 **/
	private String path;
	
	/** 属性参数 **/
	private ArrayList<SurfaceAttribute> array = new ArrayList<SurfaceAttribute>();
	
	/** 内容 **/
	private String content;

	/**
	 * 构造默认的XML标签单元
	 */
	private SurfaceElement() {
		super();
	}
	
	/**
	 * 构造XML标签单元，指定路径
	 * @param xmlPath XML路径
	 */
	public SurfaceElement(String xmlPath) {
		this();
		setPath(xmlPath);
	}

	/**
	 * 设置XML标签单元
	 * @param s
	 */
	public void setPath(String s) {
		path = s;
	}

	/**
	 * 返回XML标签单元
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 查找属性
	 * @param name
	 * @return
	 */
	public SurfaceAttribute findAttribute(String name) {
		for (SurfaceAttribute e : array) {
			if (name.compareTo(e.getName()) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 保存属性
	 * @param e
	 */
	public void addAttribute(SurfaceAttribute e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 输出全部属性
	 * @return
	 */
	public List<SurfaceAttribute> getAttributes() {
		return new ArrayList<SurfaceAttribute>(array);
	}

	/**
	 * 设置文本内容
	 * @param s
	 */
	public void setContent(String s) {
		content = s;
	}

	/**
	 * 返回文本内容
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SurfaceElement that) {
		if(that == null) {
			return 1;
		}
		return path.compareTo(that.path);
	}

}