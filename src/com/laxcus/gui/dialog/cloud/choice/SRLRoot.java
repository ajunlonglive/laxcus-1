/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 根单元，可能是目录或者磁盘
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
final class SRLRoot implements Comparable<SRLRoot> {

	/** 存储资源定位器 **/
	private SRL srl;

	/** 描述文本 **/
	private String description;

	/**
	 * 构造默认的根单元
	 */
	public SRLRoot(SRL srl) {
		super();
		this.setSRL(srl);
	}

	/**
	 * 设置描述文本
	 * @param s
	 */
	public void setDescription(String s) {
		if (s != null && s.trim().length() > 0) {
			description = s;
		}
	}

	/**
	 * 返回描述文本
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置根目录或者磁盘
	 * @param f
	 */
	public void setSRL(SRL f) {
		srl = f;
	}

	/**
	 * 返回根目录或者磁盘
	 * @return
	 */
	public SRL getSRL() {
		return srl;
	}
	
	/**
	 * 返回节点
	 * @return
	 */
	public Node getNode() {
		if (srl == null) {
			return null;
		}
		return srl.getNode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SRLRoot that) {
		if (that == null) {
			return 1;
		}
		return srl.compareTo(that.srl);
	}

}