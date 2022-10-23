/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.watch.*;

/**
 * LAXCUS集群站点浏览窗口
 * 
 * @author scott.liang
 * @version 1.0 10/23/2019
 * @since laxcus 1.0
 */
public class WatchSiteBrowserPanel extends JPanel {

	private static final long serialVersionUID = 7833467079805287379L;

	/** 站点浏览面板 **/
	public WatchSiteBrowserListPanel listPanel = new WatchSiteBrowserListPanel();

	/** 状态面板 **/
	private WatchSiteBrowserDetailPanel detailPanel = new WatchSiteBrowserDetailPanel();

	/**
	 * 构造浏览窗口界面
	 */
	public WatchSiteBrowserPanel() {
		super();
	}
	
	/**
	 * 返回面板的分割位置
	 * @return 返回大于0的正整数，否则是-1
	 */
	public int getDividerLocation() {
		int count = getComponentCount();
		if (count > 0) {
			Component sub = getComponent(0);
			if (sub.getClass() == JSplitPane.class) {
				JSplitPane pane = (JSplitPane) sub;
				int location = pane.getDividerLocation();
				return location;
			}
		}
		return -1;
	}

	/**
	 * 返回节点面板
	 * @return
	 */
	public WatchSiteBrowserListPanel getListPanel() {
		return listPanel;
	}

	/**
	 * 返回运行时状态面板
	 * @return
	 */
	public WatchSiteBrowserDetailPanel getDetailPanel() {
		return detailPanel;
	}
	
	/**
	 * 设置选择的字体
	 * @param e 字体实例
	 */
	public void setSelectFont(Font e) {
		listPanel.setSelectFont(e);
		detailPanel.setSelectFont(e);
	}
	
	/**
	 * 清除全部记录
	 */
	public void clear() {
		listPanel.clear();
		detailPanel.clear();
	}
	
	/**
	 * 推送节点
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	public boolean pushSite(Node node) {
		return listPanel.pushSite(node);
	}
	
	/**
	 * 删除节点
	 * @param node
	 * @return 成功返回真，否则假
	 */
	public boolean dropSite(Node node) {
		detailPanel.drop(node);
		return listPanel.dropSite(node);
	}
	
	/**
	 * 调整运行时参数
	 * @param cmd 站点运行时
	 */
	public void modify(SiteRuntime cmd) {
		detailPanel.modify(cmd);
	}
	
	/**
	 * 初始化界面
	 */
	public void init() {
		listPanel.setParntPanel(this);
		detailPanel.setParntPanel(this);
		
		listPanel.init();
		detailPanel.init();
		
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, detailPanel);

		pane.setContinuousLayout(true);
		pane.setOneTouchExpandable(false);
		pane.setDividerSize(4); // 间隔条用4个像素
//		pane.setResizeWeight(0.05);
//		pane.setResizeWeight(0.8);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		// 分割线
		Integer divide = WatchProperties.readSiteBrowserPaneDeviderLocation();
		if (divide != null && divide.intValue() > 0) {
			pane.setDividerLocation(divide.intValue());
		} else {
			pane.setResizeWeight(0.8);
		}

		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}

//	/**
//	 * @param arg0
//	 */
//	public WatchTrackerPanel(LayoutManager arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param arg0
//	 */
//	public WatchTrackerPanel(boolean arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param arg0
//	 * @param arg1
//	 */
//	public WatchTrackerPanel(LayoutManager arg0, boolean arg1) {
//		super(arg0, arg1);
//		// TODO Auto-generated constructor stub
//	}

}
