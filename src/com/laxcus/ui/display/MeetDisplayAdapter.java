/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

import java.util.*;

import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;

/**
 * 交互式显示接口适配器 <br>
 * 子类可派生这个方法，类似MouseAdapter、KeyAdapter。
 * 
 * @author scott.liang
 * @version 1.0 1/18/2022
 * @since laxcus 1.0
 */
public class MeetDisplayAdapter implements MeetDisplay {

	/** 异步处理结果监听器 **/
	private ProductListener listener;
	
	/**
	 * 构造默认的交互式显示接口适配器
	 */
	public MeetDisplayAdapter() {
		super();
	}
	
	/**
	 * 构造交互式显示接口适配器，指定异步处理结果监听器
	 * @param l 异步处理结果监听器
	 */
	public MeetDisplayAdapter(ProductListener l) {
		this();
		setProductListener(l);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#isUsabled()
	 */
	@Override
	public boolean isUsabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#approveLicence(java.lang.String)
	 */
	@Override
	public boolean approveLicence(String text) {
		// 如果是监听器，默认是接受协议
		if (listener != null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String, boolean)
	 */
	@Override
	public void message(String text, boolean sound) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String)
	 */
	@Override
	public void warning(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String, boolean)
	 */
	@Override
	public void warning(String text, boolean sound) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String, boolean)
	 */
	@Override
	public void fault(String text, boolean sound) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearPrompt()
	 */
	@Override
	public void clearPrompt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getTitleCellCount()
	 */
	@Override
	public int getTitleCellCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem item) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#showTable(com.laxcus.util.display.show.ShowTitle, java.util.Collection)
	 */
	@Override
	public void showTable(ShowTitle title, Collection<ShowItem> items) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearShowItems()
	 */
	@Override
	public void clearShowItems() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setStatusText(java.lang.String)
	 */
	@Override
	public void setStatusText(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#flash(com.laxcus.util.display.graph.GraphItem)
	 */
	@Override
	public void flash(GraphItem item) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearGraph()
	 */
	@Override
	public void clearGraph() {
		// TODO Auto-generated method stub

	}

	/**
	 * 设置监听器
	 * @param e
	 */
	public void setProductListener(ProductListener e) {
		listener = e;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getProductListener()
	 */
	@Override
	public ProductListener getProductListener() {
		return listener;
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.ui.display.MeetDisplay#ratify(java.lang.String)
//	 */
//	@Override
//	public boolean ratify(String content) {
//		// TODO Auto-generated method stub
//		return false;
//	}

}