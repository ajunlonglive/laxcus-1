/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.*;

/**
 * 云端应用软件浏览窗口
 * 浏览全部：云端数据库、共享表、分布任务组件。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class TerminalRemoteSoftwarePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	/** 云端应用软件列表面板 **/
	private TerminalRemoteSoftwareListPanel listPanel = new TerminalRemoteSoftwareListPanel();

	/** 云端应用软件明细面板 **/
	private TerminalRemoteSoftwareDetailPanel detailPanel = new TerminalRemoteSoftwareDetailPanel();

	/**
	 * 构造面板
	 */
	public TerminalRemoteSoftwarePanel() {
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
	public TerminalRemoteSoftwareListPanel getListPanel() {
		return listPanel;
	}

	/**
	 * 返回运行时状态面板
	 * @return
	 */
	public TerminalRemoteSoftwareDetailPanel getDetailPanel() {
		return detailPanel;
	}
	
	/**
	 * 清除全部记录
	 */
	public void clear() {
		listPanel.clear();
		detailPanel.clear();
	}
	
	/**
	 * 设置当前字体
	 * @param e 字体实例
	 */
	public void setSelectFont(Font e) {
		listPanel.setSelectFont(e);
		detailPanel.setSelectFont(e);
	}

	/**
	 * 初始化界面
	 */
	public void init() {
		listPanel.setParentPanel(this);
		detailPanel.setParentPanel(this);
		
		listPanel.init();
		detailPanel.init();
		
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, detailPanel);
		pane.setDividerSize(4); // 间隔条用4个像素
		pane.setContinuousLayout(true);
		pane.setOneTouchExpandable(false);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 分割线
		Integer divide = TerminalProperties.readRemoteSoftwarePaneDeviderLocation();
		if (divide != null && divide.intValue() > 0) {
			pane.setDividerLocation(divide.intValue());
		} else {
			pane.setResizeWeight(0.8);
		}
		
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}

}