/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import com.laxcus.util.classable.*;

/**
 * 
 * @author scott.liang
 * @version 1.0 2021-8-3
 * @since laxcus 1.0
 */
public class WDocument extends WElement {

	/** 打开的命令 **/
	private String openCommand;
	
	/** JAR包中的位置 **/
	private String JURI;
	
	/** 磁盘位置 **/
	private String URI;
	
	/**
	 * 
	 */
	public WDocument() {
		super();
	}

	/**
	 * @param that
	 */
	private WDocument(WDocument that) {
		super(that);
		this.openCommand = that.openCommand;
		this.JURI = that.JURI;
		this.URI = that.URI;
	}

	/**
	 * 设置打开的命令
	 * @param s
	 */
	public void setOpenCommand(String s) {
		openCommand = s;
	}

	/**
	 * 返回打开的命令
	 * @return
	 */
	public String getOpenCommand() {
		return openCommand;
	}
	

	/**
	 * 设置文档在JAR包中的位置
	 * @param s
	 */
	public void setJURI(String s) {
		JURI = s;
	}

	/**
	 * 返回文档在JAR包中的位置
	 * @return
	 */
	public String getJURI() {
		return JURI;
	}
	
	/**
	 * 设置磁盘位置
	 * @param s
	 */
	public void setURI(String s) {
		URI = s;
	}

	/**
	 * 返回磁盘位置
	 * @return
	 */
	public String getURI() {
		return URI;
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#duplicate()
	 */
	@Override
	public WDocument duplicate() {
		return new WDocument(this);
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (JURI != null) {
			return String.format("JURI: %s", JURI);
		} else if (URI != null) {
			return String.format("URI: %s", URI);
		} else {
			return "";
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeString(openCommand);
		writer.writeString(JURI);
		writer.writeString(URI);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		this.openCommand = reader.readString();
		this.JURI = reader.readString();
		this.URI = reader.readString();
	}

}