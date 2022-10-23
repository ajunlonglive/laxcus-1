/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform;

import java.awt.*;
import java.io.*;

/**
 * 桌面背景方案
 * 包含背景颜色、背景图像、背景图像布局的三组参数
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
public final class DesktopWall {
	
	/** 不定义 **/
	public static final int NONE = 0;
	
	/** 图像拉伸方式铺满整个屏幕 **/
	public static final int FULL = 1;
	
	/** 图像居中，不拉伸  **/
	public static final int MIDDLE = 2;
	
	/** 图像不采用拉伸，而是以平铺方式，由多个图像贴满整个屏幕 **/
	public final static int MULTI = 3;
	
	/** 背景颜色  **/
	protected Color color;
	
	/** 背景图像 **/
	protected Image image;
	
	/** 磁盘文件 **/
	protected File file;
	
	/** 背景图像的布局，见上面三个参数 **/
	private int layout;

	/**
	 * 构造默认的桌面背景方案
	 */
	public DesktopWall() {
		super();
		layout = DesktopWall.NONE;
	}
	
	/**
	 * 生成桌面背景方案复本
	 * @param that
	 */
	private DesktopWall(DesktopWall that) {
		this();
		color = that.color;
		image = that.image;
		file = that.file;
		layout = that.layout;
	}

	
	/**
	 * 构造桌面背景方案
	 * @param c 颜色
	 * @param image 图像
	 * @param layout 图像布局
	 */
	public DesktopWall(Color c, Image image, int layout) {
		this();
		setColor(c);
		setImage(image);
		setLayout(layout);
	}
	
	/**
	 * 设置背景颜色
	 * @param c 颜色
	 */
	public void setColor(Color c) {
		color = c;
	}

	/**
	 * 返回背景颜色
	 * @return Color
	 */
	public Color getColor() {
		return color;
	}

//	/**
//	 * 设置背景图像
//	 * @param i
//	 */
//	public void setImage(ImageIcon i) {
//		image = i;
//	}
//
//	/**
//	 * 返回背景图像
//	 * @return
//	 */
//	public ImageIcon getImage() {
//		return image;
//	}
	
	/**
	 * 设置背景图像
	 * @param e
	 */
	public void setImage(Image e) {
		image = e;
	}

	/**
	 * 返回背景图像
	 * @return
	 */
	public Image getImage() {
		return image;
	}
	
	/**
	 * 设置磁盘文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回磁盘文件
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 设置图像布局样式
	 * @param who
	 */
	public void setLayout(int who) {
		layout = who;
	}

	/**
	 * 返回图像布局样式
	 * @return
	 */
	public int getLayout() {
		return layout;
	}

	/**
	 * 判断是平铺
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return image != null && layout == DesktopWall.FULL;
	}

	/**
	 * 判断是布局在中心
	 * @return 返回真或者假
	 */
	public boolean isCenter() {
		return image != null && layout == DesktopWall.MIDDLE;
	}

	/**
	 * 判断是层叠布置
	 * @return 返回真或者假
	 */
	public boolean isMulti() {
		return image != null && layout == DesktopWall.MULTI;
	}

	/**
	 * 生成一个副本
	 * @return 副本
	 */
	public DesktopWall duplicate() {
		return new DesktopWall(this);
	}
}
