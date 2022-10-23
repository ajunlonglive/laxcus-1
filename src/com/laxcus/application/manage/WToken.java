/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 应用软件令牌(software token)，是一个基础类
 * 对标BootItem的转换结果！
 * 
 * @author scott.liang
 * @version 1.0 8/3/2021
 * @since laxcus 1.0
 */
public abstract class WToken implements Classable, Cloneable, Comparable<WToken> {

	/** 标题 **/
	private String title;

	/** 提示 **/
	private String tooltip;

	/** 图标 **/
	private ImageIcon icon;
	
	/**
	 * 构造应用软件令牌
	 */
	protected WToken() {
		super();
	}
	
	/**
	 * 生成应用软件令牌的数据副本
	 * @param that
	 */
	protected WToken(WToken that) {
		this();
		title = that.title;
		tooltip = that.tooltip;
		icon = that.icon;
	}
	
	/**
	 * 设置名称，显示在界面上的
	 * @param s
	 */
	public void setTitle(String s) {
		title = s;
	}

	/**
	 * 返回名称
	 * @return
	 */
	public String getTitle(){
		return title;
	}	

	/**
	 * 设置工具提示，显示在界面上的
	 * @param s
	 */
	public void setToolTip(String s) {
		tooltip = s;
	}

	/**
	 * 返回工具提示
	 * @return
	 */
	public String getToolTip(){
		return tooltip;
	}	

	public void setIcon(ImageIcon e){
		icon = e;
	}
	
	public ImageIcon getIcon(){
		return icon;
	}

	/**
	 * 返回图像
	 * @return
	 */
	public java.awt.Image getImage() {
		if(icon != null) {
			return icon.getImage();
		}
		return null;
	}
	
	/**
	 * 图标的字节数组
	 * @return
	 */
	private byte[] getIconArray() {
		if (icon == null) {
			return null;
		}
		// 尺寸
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		// 生成一个新图像
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D gra = bi.createGraphics();
		bi = gra.getDeviceConfiguration().createCompatibleImage(width, height, java.awt.Transparency.TRANSLUCENT);
		bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(bi, "PNG", out);
			out.flush();
			// 生成图像
			return out.toByteArray();
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((WToken) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WToken that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(title, that.title);
	}

	/**
	 * WToken子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return WToken子类实例
	 */
	public abstract WToken duplicate();

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		// 命令版本号
		ClassWriter writer = new ClassWriter();
		// 标题
		writer.writeString(title);
		// 提示
		writer.writeString(tooltip);
		// 图标
		writer.writeByteArray(getIconArray());
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);
		
		// 字节流写入可类化存储器
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析命令参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取令牌字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 标题
		title = reader.readString();
		// 提示
		tooltip = reader.readString();
		// 图标的字节数组
		byte[] bs = reader.readByteArray();
		if (bs != null) {
			icon = new ImageIcon(bs);
		}

		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);

		// 返回读取的数据长度
		return r.getSeek() - seek;
	}

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}