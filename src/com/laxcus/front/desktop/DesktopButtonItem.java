/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.application.manage.*;
import com.laxcus.util.classable.*;

/**
 * 按纽单元
 * 
 * @author scott.liang
 * @version 1.0 7/29/2021
 * @since laxcus 1.0
 */
final class DesktopButtonItem extends WProgram { 

	//	/** 启动命令 **/
	//	private String command;
	//
	//	/** 文本 **/
	//	private String title;
	//
	//	/** 提示 **/
	//	private String tooltip;
	//
	//	/** 图标 **/
	//	private ImageIcon icon;

	/** 生成时间 **/
	private long createTime;

	/** 按纽位置 **/
	private DesktopContour desktopContour;

	/**
	 * 构造默认的按纽单元
	 */
	public DesktopButtonItem() {
		super();
		// 建立时间
		createTime = System.currentTimeMillis();
	}
	
	/**
	 * 构造按纽单元副本
	 * @param that 按纽单元
	 */
	private DesktopButtonItem(DesktopButtonItem that) {
		super(that);

		//		command = that.command;
		//		title = that.title;
		//		tooltip = that.tooltip;
		//		icon = that.icon;

		createTime = that.createTime;
		desktopContour = that.desktopContour;
	}
	
	/**
	 * 生成按纽单元实例
	 * @param w
	 */
	public DesktopButtonItem(WProgram w) {
		super(w);
		createTime = System.currentTimeMillis();
	}

	/**
	 * 从可类化读取器中读取参数
	 * @param reader 可类化读取器
	 */
	public DesktopButtonItem(ClassReader reader) {
		this();
		resolve(reader);
	}

//	public void setCommand(String s) {
//		command = s;
//	}
//	
//	public String getCommand(){
//		return command;
//	}
//	
//	public void setTitle(String s) {
//		title = s;
//	}
//	
//	public String getTitle(){
//		return title;
//	}
//	
//	public void setTooltip(String s){
//		tooltip = s;
//	}
//	
//	public String getTooltip(){
//		return tooltip;
//	}
//	
//	public void setIcon(ImageIcon e){
//		icon = e;
//	}
//	
//	public ImageIcon getIcon() {
//		return icon;
//	}
	
	public long getCreateTime(){
		return createTime;
	}
	
	public void setContour(DesktopContour e) {
		desktopContour = e;
	}
	
	public DesktopContour getContour(){
		return desktopContour;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.application.manage.WProgram#duplicate()
	 */
	@Override
	public DesktopButtonItem duplicate(){
		return new DesktopButtonItem(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(createTime);
		writer.writeObject(desktopContour);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		createTime = reader.readLong();
		desktopContour = new DesktopContour(reader);
	}
	
//	private byte[] getIconArray() {
//		int width = icon.getIconWidth();
//		int height = icon.getIconHeight();
//
//		// 生成一个新图像
//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		Graphics2D gra = bi.createGraphics();
//		bi = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//		bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);
//
//		try {
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			ImageIO.write(bi, "PNG", out);
//			out.flush();
//			// 生成图像
//			return out.toByteArray();
//		} catch (IOException e) {
//
//		}
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
//	 */
//	@Override
//	public int build(ClassWriter writer) {
//		int size = writer.size();
//		
//		writer.writeString(command);
//		writer.writeString(title);
//		writer.writeString(tooltip);
//		writer.writeByteArray(getIconArray());
//		writer.writeLong(createTime);
//		writer.writeObject(contour);
//		
//		return writer.size() - size;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	public int resolve(ClassReader reader) {
//		int seek = reader.getSeek();
//
//		command = reader.readString();
//		title = reader.readString();
//		tooltip = reader.readString();
//		byte[] b = reader.readByteArray();
//		if (b != null) {
//			icon = new ImageIcon(b);
//		}
//		createTime = reader.readLong();
//		contour = new Contour(reader);
//
//		return reader.getSeek() - seek;
//	}
	
	
}
