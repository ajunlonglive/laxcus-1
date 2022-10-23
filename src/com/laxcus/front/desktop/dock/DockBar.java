/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dock;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.panel.*;
import com.laxcus.gui.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 * 应用坞面板
 * 图标按纽在这里显示...
 * 
 * 组件包括：
 * 1. 左按纽
 * 2. DockBanner
 * 3. 右按纽
 * 
 * @author scott.liang
 * @version 1.0 10/5/2021
 * @since laxcus 1.0
 */
class DockBar extends DesktopPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = -1068425501453649017L;

	@Override
	public void actionPerformed(ActionEvent e) {
		// 执行点击后的处理
		click(e.getSource());
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		showPopupMenu(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		showPopupMenu(e);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 左侧鼠标点击
	 *
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class LeftButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 向左移动界面
			doLeftArrow();
		}
	}

	/**
	 * 右侧鼠标点击
	 *
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class RightButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 向右移动界面
			doRightArrow();
		}
	}
	
	/** 按纽 **/
	private ArrayList<DockButton> array = new ArrayList<DockButton>();

	/** 左按纽 **/
	private DockArrowButton cmdLeft;

	/** 左按纽 **/
	private DockArrowButton cmdRight;

	/** 安放按纽的任务条 **/
	private DockBanner banner;
	
	/** 弹出菜单 **/
	private JPopupMenu rockMenu;
	
	/** 菜单项 **/
	private DockMenuItem muStart;
	private DockMenuItem muRemove;
	
	private DockMenuItem muTop;
	private DockMenuItem muTopAndFull;
	private DockMenuItem muMiddle;
	private DockMenuItem muBottom;
	private DockMenuItem muBottomAndFull;
	
	// 最前或者最后
	private DockMenuItem muFrontBack;
	
	private DockMenuItem muHide;
	private DockMenuItem muEdge;
	
	/** 所属父类 **/
	private DesktopDock parent;
	
	/**
	 * 构造默认的应用坞面板
	 */
	public DockBar(DesktopDock d) {
		super();
		parent = d;
	}

	/**
	 * 初始化按纽
	 */
	private void createArrowButtons() {
		// 左侧箭头按纽
		cmdLeft = new DockArrowButton();
		ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.LeftIcon");
		cmdLeft.setIcon(icon, 12, -16);
		// 右侧箭头按纽
		cmdRight = new DockArrowButton();
		icon = (ImageIcon) UIManager.getIcon("DockBar.RightIcon");
		cmdRight.setIcon(icon, 12, -16);
		
		// 提示文本
		cmdLeft.setToolTipText(UIManager.getString("DockBar.LeftTooltipText"));
		cmdRight.setToolTipText(UIManager.getString("DockBar.RightTooltipText"));
		
		// 鼠标单点事件
		cmdLeft.addActionListener(new LeftButtonAdapter());
		cmdRight.addActionListener(new RightButtonAdapter());
	}
	
	class MenuItemAdapter implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Laxkit.isClassFrom(e.getSource(), DockMenuItem.class)) {
				doClickMenu(e);
			}
		}
	}
	
	private void doClickMenu(ActionEvent event) {
		Object object = event.getSource();
		if (Laxkit.isClassFrom(object, DockMenuItem.class)) {
			DockMenuItem source = (DockMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}
	
	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = getClass().getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
	}
	
	/**
	 * 显示弹出菜单
	 * @param event
	 */
	private void showPopupMenu(MouseEvent event) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!event.isPopupTrigger()) {
			return;
		}
		
		Object source = event.getSource();

		// 如果是DOCK按纽...
		if (source instanceof DockButton) {
			DockButton button = (DockButton) source;
			muRemove.setEnabled(button.canDelete()); // 允许删除或者否。用户按纽可以删除，系统级不行！

			muStart.setEnabled(true);
			muRemove.setDockButton(button);
			muStart.setDockButton(button);
		} else {
			// 判断有浮起的按纽，设置菜单项状态
			muRemove.setEnabled(false);
			muStart.setEnabled(false);
			muRemove.setDockButton(null);
			muStart.setDockButton(null);

			// // 显示
			// int newX = event.getX();
			// int newY = event.getY();
			// rockMenu.show(rockMenu.getInvoker(), newX, newY);
		}
		
		int newX = event.getXOnScreen();
		int newY = event.getYOnScreen();
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		rockMenu.show(desktop, newX, newY);
	}

	/**
	 * 判断是字符
	 * @param w
	 * @return
	 */
	private boolean isWord(char w) {
		return (w >= 'A' && w <= 'Z') || (w >= 'a' && w <= 'z');
	}
	
	/**
	 * 返回助记符
	 * @param mnemonic
	 * @return
	 */
	private char getMnemonic(String mnemonic) {
		if (mnemonic == null) {
			return 0;
		}
		String str = UIManager.getString(mnemonic);
		if (str == null || str.length() == 0) {
			return 0;
		}

		str = str.trim();
		if (str.length() > 0) {
			return str.charAt(0);
		}
		return 0;
	}
	
//	/**
//	 * 生成菜单项
//	 * @param key
//	 * @param mnemonic
//	 * @param method
//	 * @return
//	 */
//	private DockMenuItem createMenuItem(String key, String mnemonic, String method) {
//		DockMenuItem item = new DockMenuItem();
//		FontKit.setButtonText(item, UIManager.getString(key));
//		// 助记符
//		char w = getMnemonic(mnemonic);
//		if (isWord(w)) {
//			item.setMnemonic(w);
//		}
//		// 方法名
//		item.setName(UIManager.getString(method)); 
//		// 菜单事件
//		item.addActionListener(new MenuItemAdapter());
//		
//		item.setBorder(new EmptyBorder(3, 4, 3, 4));
//
//		return item;
//	}
	
	/**
	 * 生成菜单项
	 * @param key
	 * @param mnemonic
	 * @param method
	 * @return
	 */
	private DockMenuItem createMenuItem(String textKey, String mnemonicKey, String methodKey) {
		return (DockMenuItem) MenuBuilder.createMenuItem(new DockMenuItem(),
				null, textKey, mnemonicKey, null, methodKey,
				new MenuItemAdapter(), null);

		//		FontKit.setButtonText(item, UIManager.getString(key));
		//		// 助记符
		//		char w = getMnemonic(mnemonic);
		//		if (isWord(w)) {
		//			item.setMnemonic(w);
		//		}
		//		// 方法名
		//		item.setName(UIManager.getString(method)); 
		//		// 菜单事件
		//		item.addActionListener(new MenuItemAdapter());
		//		
		//		item.setBorder(new EmptyBorder(3, 4, 3, 4));
		//
		//		return item;
	}
	
	/**
	 * 更新显示单元
	 * @param item
	 * @param key
	 * @param mnemonic
	 * @param method
	 */
	private void updateMenuItem(DockMenuItem item, String key, String mnemonic, String method) {
		FontKit.setButtonText(item, UIManager.getString(key)); 
		// 助记符
		char w = getMnemonic(mnemonic);
		if (isWord(w)) {
			item.setMnemonic(w);
		}
		// 方法名
		item.setName(UIManager.getString(method)); 
	}
	
	class CancelButtonRollover extends SwingEvent {
		DockButton button;

		CancelButtonRollover(DockButton e) {
			super();
			button = e;
		}

		public void process() {
			button.cancel();
		}
	}
	
	void doMenuStart() {
		// 从DockBanner上找到弹起的按纽
		DockButton button = muStart.getDockButton(); 
		if (button == null) {
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			String title = UIManager.getString("DockBar.Error.NotfoundTitleText");
			String content = UIManager.getString("DockBar.Error.StartFailedText");
			MessageBox.showFault(desktop, title, content);
			return;
		}
		
//		startApplication(button);
		
//		// 启动应用
//		RunApplicationThread rt = new RunApplicationThread(button);
//		rt.execute();
		
		
		addThread(new RunApplicationThread(button));
		
		// 清除
		muStart.setDockButton(null);
		muRemove.setDockButton(null);
		
		// 重新绘制
		addThread(new CancelButtonRollover(button));
	}
	
	void doMenuRemove() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		
		// 从DockBanner上找到弹起的按纽
		DockButton button = muRemove.getDockButton(); 
		if (button == null) {
			String title = UIManager.getString("DockBar.Error.NotfoundTitleText");
			String content = UIManager.getString("DockBar.Error.StartFailedText");
			MessageBox.showFault(desktop, title, content);
			return;
		}
		
		// 清除
		muStart.setDockButton(null);
		muRemove.setDockButton(null);
		
		// 用显示图标
		Icon icon = button.getIcon();
		// 确认删除应用软件
		String title = UIManager.getString("DockBar.Message.DeleteTitleText");
		String content = UIManager.getString("DockBar.Message.DeleteContentText");
		
		boolean yes = MessageBox.showYesNoDialog(desktop, title, icon, content);
		if (!yes) {
			return;
		}
		
		// 删除按纽
		boolean success = removeButton(button);
		if (!success) {
			title = UIManager.getString("DockBar.Error.DeleteTitleText");
			content = UIManager.getString("DockBar.Error.DeleteContentText");
			MessageBox.showFault(desktop, title, content);
		}
	}
	
	/**
	 * 找到应用坞实例
	 * @param component
	 * @return
	 */
	private DesktopDock findDesktopDock(Component component) {
		Component parent = component.getParent();
		if (parent == null) {
			return null;
		}
		if (Laxkit.isClassFrom(parent, DesktopDock.class)) {
			return (DesktopDock) parent;
		}
		return findDesktopDock(parent);
	}
	
	/**
	 * 隐藏应用坞
	 */
	void doMenuHideDocker() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.setVisible(false);
		}
	}
	

	/**
	 * 应用坞置于窗口最前面或者后面
	 */
	void doMenuFrontBack() {
		int layer = parent.blockTo();
		// 小于0是无效
		if (layer < 0) {
			return;
		}

		// 新的选项是前端，显示置于后端；否则相反
//		if (layer == JLayeredPane.POPUP_LAYER.intValue()) {
		if (layer == FormLayer.DOCK) {
			updateMenuItem(muFrontBack, "DockBar.MenuMoveToBackText",
					"DockBar.MenuMoveToBackMWord",
					"DockBar.MenuMoveToBackMethod");
		} else {
			updateMenuItem(muFrontBack, "DockBar.MenuMoveToFrontText",
					"DockBar.MenuMoveToFrontMWord",
					"DockBar.MenuMoveToFrontMethod");
		}
	}
	
	private void modifyEdgeMenu(boolean b) {
		// 显示/隐藏系统区
		if (b) {
			updateMenuItem(muEdge,"DockBar.MenuHideEdgeText",
					"DockBar.MenuHideEdgeMWord", "DockBar.MenuHideEdgeMethod");
		} else {
			updateMenuItem(muEdge, "DockBar.MenuShowEdgeText",
					"DockBar.MenuShowEdgeMWord", "DockBar.MenuShowEdgeMethod");
		}
	}

	void doMenuShowHideEdge() {
		DesktopDock dock = findDesktopDock(this);
		if (dock == null) {
			return;
		}
		// 显示
		boolean show = dock.isShowEdgeBar();
		dock.doExchangeEdge();

		// 参数保存到内存中
		setShowEdge(!show);
		// 高速菜单显示文本
		modifyEdgeMenu(!show);
	}
	
	/**
	 * 放到顶部
	 */
	void doMoveToTop() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.doMoveToTop();
		}
	}

	/**
	 * 放到顶部和平铺
	 */
	void doMoveToTopFull() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.doMoveToTopFull();
			// 调整宽度
			dock.modifyWidth(true);
		}
	}
	
	/**
	 * DOCKER居中部署
	 */
	void doMoveToMiddle() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.doMoveMiddle();
		}
	}

	/**
	 * 置于下方
	 */
	void doMoveToBottom() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.doMoveToBottom();
		}
	}

	/**
	 * 置于下方并且平铺
	 */
	void doMoveToBottomFull() {
		DesktopDock dock = findDesktopDock(this);
		if (dock != null) {
			dock.doMoveToBottomFull();
		}
	}
	
	//	DockBar.MoveToTop 移到上方 [T]
	//	                        DockBar.MoveToTopAndFull 移到上方并平铺 [L]
	//	                        DockBar.MoveToBottom 移到上方 [B]
	//	                        DockBar.MoveToBottomAndFull 移到上方并平铺 [C]
	//	private DockMenuItem muTop;
	//	private DockMenuItem muTopAndFull;
	//	private DockMenuItem muBottom;
	//	private DockMenuItem muBottomAndFull;
	
//	private void createMenu() {
////		String[] texts = new String[] {
////				UIManager.getString("DockBar.StartAppText"),
////				UIManager.getString("DockBar.RemoveAppText"), 
////				UIManager.getString("DockBar.MoveToTop"), 
////				UIManager.getString("DockBar.MoveToTopAndFull"), 
////				UIManager.getString("DockBar.MoveToMiddle"),
////				UIManager.getString("DockBar.MoveToBottom"), 
////				UIManager.getString("DockBar.MoveToBottomAndFull"), 
////				UIManager.getString("DockBar.MoveToBottomAndFull"), 
////				UIManager.getString("DockBar.HideText"),
////				UIManager.getString("DockBar.EdgeHideText")};
////		char[] keys = new char[] { 'S', 'R', 'T', 'L', 'M', 'B', 'C', 'H', 'Q' };
////		String[] methods = new String[] { 
////				"doMenuStart", "doMenuRemove" ,
////				"doMoveToTop","doMoveToTopFull", "doMoveToMiddle", "doMoveToBottom","doMoveToBottomFull",
////				"doMenuHideDocker", "doMenuShowHideEdge"};
////
////		muStart = createMenuItem(texts[0], keys[0], methods[0]);
////		muRemove = createMenuItem(texts[1], keys[1], methods[1]);
////		muTop = createMenuItem(texts[2], keys[2], methods[2]);
////		muTopAndFull = createMenuItem(texts[3], keys[3], methods[3]);
////		muMiddle = createMenuItem(texts[4], keys[4], methods[4]);
////		
////		muBottom = createMenuItem(texts[5], keys[5], methods[5]);
////		muBottomAndFull = createMenuItem(texts[6], keys[6], methods[6]);
////		muHide = createMenuItem(texts[7], keys[7], methods[7]);
////		muEdge = createMenuItem(texts[8], keys[8], methods[8]);
//		
////		private DockMenuItem createMenuItem(String text, char key, String method) {
////		muFrontOrBack = createMenuItem("DockBar.MoveToFrontText", (char) 0 , "doMenuFrontBack");
////		int layer = parent.readLayer();
////		String key = (layer == JLayeredPane.POPUP_LAYER ? "DockBar.MoveToBackText" : "DockBar.MoveToFrontText");
////		String text = UIManager.getString(key);
////		muFrontOrBack.setText(text);
//
//		muStart = createMenuItem("DockBar.MenuStartAppText", "DockBar.MenuStartAppMWord", "DockBar.MenuStartAppMethod");
//		muRemove = createMenuItem("DockBar.MenuRemoveAppText", "DockBar.MenuRemoveAppMWord", "DockBar.MenuRemoveAppMethod");
//		
//		muTop = createMenuItem("DockBar.MenuMoveToTopText", "DockBar.MenuMoveToTopMWord", "DockBar.MenuMoveToTopMethod");
//		muTopAndFull = createMenuItem("DockBar.MenuMoveToTopFullText", "DockBar.MenuMoveToTopFullMWord", "DockBar.MenuMoveToTopFullMethod");
//		muMiddle = createMenuItem("DockBar.MenuMoveToMiddleText", "DockBar.MenuMoveToMiddleMWord", "DockBar.MenuMoveToMiddleMethod");
//		muBottom = createMenuItem("DockBar.MenuMoveToBottomText","DockBar.MenuMoveToBottomMWord","DockBar.MenuMoveToBottomMethod");
//		muBottomAndFull = createMenuItem("DockBar.MenuMoveToBottomAndFullText", "DockBar.MenuMoveToBottomAndFullMWord", "DockBar.MenuMoveToBottomAndFullMethod");
//		   
//		// 置前或者置后
//		int layer = parent.readLayer();
//		if (layer == JLayeredPane.POPUP_LAYER) {
//			muFrontBack = createMenuItem("DockBar.MenuMoveToFrontText", "DockBar.MenuMoveToFrontMWord", "DockBar.MenuMoveToFrontMethod");
//		} else {
//			muFrontBack = createMenuItem("DockBar.MenuMoveToBackText","DockBar.MenuMoveToBackMWord","DockBar.MenuMoveToBackMethod");
//		}
//		
//		// 隐藏应用坞
//		muHide = createMenuItem("DockBar.MenuHideDockerText", "DockBar.MenuHideDockerMWord", "DockBar.MenuHideDockerMethod");
//		
//		// 显示/隐藏系统区
//		boolean showEdge = isShowEdge();
//		if (showEdge) {
//			muEdge = createMenuItem("DockBar.MenuHideEdgeText",
//					"DockBar.MenuHideEdgeMWord", "DockBar.MenuHideEdgeMethod");
//		} else {
//			muEdge = createMenuItem("DockBar.MenuShowEdgeText",
//					"DockBar.MenuShowEdgeMWord", "DockBar.MenuShowEdgeMethod");
//		}
//
//		// 生成菜单
//		rockMenu = new JPopupMenu();
//		rockMenu.add(muStart);
//		rockMenu.add(muRemove);
//		
//		rockMenu.addSeparator();
//		rockMenu.add(muTop);
//		rockMenu.add(muTopAndFull);
//		rockMenu.add(muMiddle);
//		rockMenu.add(muBottom);
//		rockMenu.add(muBottomAndFull);
//
//		rockMenu.addSeparator();
//		rockMenu.add(muFrontBack);
//		
//		rockMenu.addSeparator();
//		rockMenu.add(muHide);
//		rockMenu.add(muEdge);
//		
////		// 隐藏/显示系统区
////		String ts = (showEdge ? "DockBar.EdgeHideText" : "DockBar.EdgeShowText");
////		char w = (showEdge ? 'Q' : 'W');
////		ts = UIManager.getString(ts);
////		if (ts != null) {
////			muEdge.setText(ts);
////			muEdge.setMnemonic(w);
////		}
//	}
	
	private void createMenu() {
		// 启动/移除应用
		muStart = createMenuItem("DockBar.MenuStartAppText", "DockBar.MenuStartAppMWord", "DockBar.MenuStartAppMethod");
		muRemove = createMenuItem("DockBar.MenuRemoveAppText", "DockBar.MenuRemoveAppMWord", "DockBar.MenuRemoveAppMethod");
		// 布置到上面/下面/中间
		muTop = createMenuItem("DockBar.MenuMoveToTopText", "DockBar.MenuMoveToTopMWord", "DockBar.MenuMoveToTopMethod");
		muTopAndFull = createMenuItem("DockBar.MenuMoveToTopFullText", "DockBar.MenuMoveToTopFullMWord", "DockBar.MenuMoveToTopFullMethod");
		muMiddle = createMenuItem("DockBar.MenuMoveToMiddleText", "DockBar.MenuMoveToMiddleMWord", "DockBar.MenuMoveToMiddleMethod");
		muBottom = createMenuItem("DockBar.MenuMoveToBottomText","DockBar.MenuMoveToBottomMWord","DockBar.MenuMoveToBottomMethod");
		muBottomAndFull = createMenuItem("DockBar.MenuMoveToBottomAndFullText", "DockBar.MenuMoveToBottomAndFullMWord", "DockBar.MenuMoveToBottomAndFullMethod");

		// 置于窗口前端或者后端
		int layer = parent.readLayer();
//		if (layer == JLayeredPane.POPUP_LAYER) {
		if (layer == FormLayer.DOCK) {
			muFrontBack = createMenuItem("DockBar.MenuMoveToBackText", "DockBar.MenuMoveToBackMWord","DockBar.MenuMoveToBackMethod");
		} else {
			muFrontBack = createMenuItem("DockBar.MenuMoveToFrontText", "DockBar.MenuMoveToFrontMWord", "DockBar.MenuMoveToFrontMethod");
		}
		
		// 隐藏应用坞
		muHide = createMenuItem("DockBar.MenuHideDockerText", "DockBar.MenuHideDockerMWord", "DockBar.MenuHideDockerMethod");
		
		// 显示/隐藏系统区
		boolean showEdge = isShowEdge();
		if (showEdge) {
			muEdge = createMenuItem("DockBar.MenuHideEdgeText", "DockBar.MenuHideEdgeMWord", "DockBar.MenuHideEdgeMethod");
		} else {
			muEdge = createMenuItem("DockBar.MenuShowEdgeText", "DockBar.MenuShowEdgeMWord", "DockBar.MenuShowEdgeMethod");
		}

		// 生成菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(muStart);
		rockMenu.add(muRemove);
		
		rockMenu.addSeparator();
		rockMenu.add(muTop);
		rockMenu.add(muTopAndFull);
		rockMenu.add(muMiddle);
		rockMenu.add(muBottom);
		rockMenu.add(muBottomAndFull);

		rockMenu.addSeparator();
		rockMenu.add(muFrontBack);
		
		rockMenu.addSeparator();
		rockMenu.add(muHide);
		rockMenu.add(muEdge);
	}
	
	public boolean isShowEdge() {
		return RTKit.readBoolean(RTEnvironment.ENVIRONMENT_SYSTEM,
				"DesktopDock/DockBar/ShowSystemEdge", true);
	}

	public void setShowEdge(boolean show) {
		RTKit.writeBoolean(RTEnvironment.ENVIRONMENT_SYSTEM,
				"DesktopDock/DockBar/ShowSystemEdge", show);
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		createMenu();
		
		// 初始化按纽
		createArrowButtons();
		// 部署面板
		banner = new DockBanner();
		banner.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		banner.setLayout(new FlowLayout(FlowLayout.LEFT, DesktopDock.DOCK_BUTTON_GAP, 0));
		banner.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 设置为banner
		rockMenu.setInvoker(banner);
		// banner增加鼠标事件
		banner.addMouseListener(this);

		// 中心面板
		setDefaultBorder();
		setLayout(new BorderLayout(0, 0));
		add(cmdLeft, BorderLayout.WEST);
		add(banner, BorderLayout.CENTER);
		add(cmdRight, BorderLayout.EAST);

		cmdLeft.addMouseListener(this);
		cmdRight.addMouseListener(this);
		
		cmdLeft.setVisible(false);
		cmdRight.setVisible(false);

		setOpaque(false);
		
		cmdLeft.setOpaque(false);
		cmdRight.setOpaque(false);
		banner.setOpaque(false);
		
		// // 记录高度
		// bannerHeight = banner.getHeight();
		
//		addButtons();
		
//		startTestButtonsThread();
	}
	
	/**
	 * 生成按纽
	 * @param icon
	 * @param tooltip
	 * @return
	 */
	private DockButton createButton(ImageIcon icon, String tooltip) {
		DockButton button = new DockButton();
		if (tooltip != null) {
			button.setToolTipText(tooltip);
		}
//		button.setIcon(icon, 28, -30);
		button.setIcon(icon, PlatformButton.DOCK_BUTTON_BRIGHTER, PlatformButton.DOCK_BUTTON_DARK);
		button.addActionListener(this);
		button.addMouseListener(this);
		button.setOpaque(false); // 透明...
		return button;
	}
	
//	DockBar.FishIcon [ICON 32*32] conf/desktop/image/bar/Dock/fish.png
//	DockBar.AppsIcon [ICON 32*32] conf/desktop/image/bar/Dock/apps.png
//	DockBar.BugIcon [ICON 32*32] conf/desktop/image/bar/Dock/bug.png
//	DockBar.ComputerIcon [ICON 32*32] conf/desktop/image/bar/Dock/computer.png
//	DockBar.LabelIcon [ICON 32*32] conf/desktop/image/bar/Dock/label.png
//	DockBar.WirelessIcon [ICON 32*32] conf/desktop/image/bar/Dock/wireless.png
//	DockBar.ClockIcon [ICON 32*32] conf/desktop/image/bar/dock/clock.png
//	DockBar.EarthIcon [ICON 32*32] conf/desktop/image/bar/dock/earth.png
//	DockBar.PhoneIcon [ICON 32*32] conf/desktop/image/bar/dock/phone.png
//	DockBar.BlueIcon [ICON 32*32] conf/desktop/image/bar/dock/blue.png
	
//	DockBar.FireIcon [ICON 32*32] conf/desktop/image/bar/dock/fire.png
//	DockBar.FlowerIcon [ICON 32*32] conf/desktop/image/bar/dock/flower.png
//	DockBar.BallIcon [ICON 32*32] conf/desktop/image/bar/dock/ball.png
//	DockBar.DatabaseIcon [ICON 32*32] conf/desktop/image/bar/dock/database.png
//	DockBar.SupportIcon [ICON 32*32] conf/desktop/image/bar/dock/support.png

//	DockBar.HeloIcon [ICON 32*32] conf/desktop/image/bar/dock/helo.png
//	DockBar.PadIcon [ICON 32*32] conf/desktop/image/bar/dock/pad.png
//	DockBar.SdiskIcon [ICON 32*32] conf/desktop/image/bar/dock/sdisk.png
//	DockBar.GoodIcon [ICON 32*32] conf/desktop/image/bar/dock/good.png
//	DockBar.SmileIcon [ICON 32*32] conf/desktop/image/bar/dock/smile.png
//	DockBar.BookIcon [ICON 32*32] conf/desktop/image/bar/dock/book.png
	
//	DockBar.ToolsIcon [ICON 32*32] conf/desktop/image/bar/dock/tools.png
//	DockBar.BoxIcon [ICON 32*32] conf/desktop/image/bar/dock/box.png
//	DockBar.ZipIcon [ICON 32*32] conf/desktop/image/bar/dock/zip.png
//	DockBar.WaterIcon [ICON 32*32] conf/desktop/image/bar/dock/water.png
	
//	DockBar.64Icon [ICON 32*32] conf/desktop/image/bar/dock/64.png
//	DockBar.ChatIcon [ICON 32*32] conf/desktop/image/bar/dock/chat.png
//	DockBar.SoundIcon [ICON 32*32] conf/desktop/image/bar/dock/sound.png

	
//	private void addTestButtons() {
////		System.out.println("add dock buttons");
//		
//		String[] links = new String[]{ "DockBar.SunIcon","DockBar.FishIcon","DockBar.AppsIcon",
//				"DockBar.BugIcon","DockBar.ComputerIcon","DockBar.LabelIcon","DockBar.WirelessIcon",
//				"DockBar.ClockIcon","DockBar.EarthIcon", "DockBar.PhoneIcon","DockBar.BlueIcon",
//				"DockBar.FireIcon", "DockBar.FlowerIcon", "DockBar.BallIcon","DockBar.DatabaseIcon", 
//				"DockBar.SupportIcon",
//				
//				"DockBar.HeloIcon","DockBar.PadIcon","DockBar.SdiskIcon",
//				"DockBar.GoodIcon","DockBar.SmileIcon","DockBar.BookIcon",
//				"DockBar.ToolsIcon","DockBar.BoxIcon","DockBar.ZipIcon","DockBar.WaterIcon",
//				"DockBar.64Icon","DockBar.ChatIcon"};
//		String[] tooltips = new String[]{ "太阳照在三个和尚上...", "小鱼","应用软件", "早起的虫子有鸟吃",
//				"苹果计算机", "兰色", "无线服务",
//				"时时在流失中...", "地球到月亮的距离是多少？", "拿起手机打电话...", "江春入旧年",
//				"冬天一把火","花开花落两由支","一堆球","大数据存储","在线人工支持服务",
//				"喂...","手持设备","软盘不是盘",
//				"非常好！","笑不笑","书中自有黄金屋",
//				"工具箱","沙盒","软件压缩","天气预告",
//				"64位应用软件开发","你说啥，啥也不说了"};
//		for(int i =0; i < links.length; i++) {
////		for(int i =0; i < 5; i++){
//			ImageIcon icon = (ImageIcon)UIManager.getIcon(links[i]);
//			if (icon == null) {
////				System.out.printf("FUCK %s\n", links[i]);
//				continue;
//			}
//			DockButton button = createButton(icon, tooltips[i]);
//			addButton(button);
////			banner.add(button);
////			array.add(button);
//		}
//	}
	
//	class AddTestButtonsThread extends SwingEvent {
//		AddTestButtonsThread(){
//			super();
//		}
//		public void process() {
//			addTestButtons();
//		}
//	}
	
//	private void startTestButtonsThread() {
//		AddTestButtonsThread rt = new AddTestButtonsThread();
////		rt.setIntervalTime(500);
//		rt.setTouchTime(System.currentTimeMillis() + 5000);
//		addThread(rt);
//	}
	
	// 边框占位是6个像素，这是固定值，对应下面CompoundBorder的左侧间隔像素
	final int borderWidth = 6;
	
	/**
	 * 定义边框，NIMBUS界面无边框，METAL界面是一条线
	 */
	private void setDefaultBorder() {
		if (isMetalUI()) {
			Color c = UIManager.getColor("Panel.background");
			if (c == null) {
				c = getBackground();
			}
			// 如果是灰色，颜色加深处理
			if (Skins.isGraySkin()) {
				Color light = new ESL(120, 18, 70).toColor(); // 与背景图片颜色接近一致
				LineBorder outside = new LineBorder(light, 1, true);
				EmptyBorder inside = new EmptyBorder(0, 4, 0, 0);
				setBorder(new CompoundBorder(outside, inside));
			} else {
				ESL esl = new RGB(c).toESL();
				Color light = esl.toBrighter(20).toColor();
				LineBorder outside = new LineBorder(light, 1, true);
				EmptyBorder inside = new EmptyBorder(0, 4, 0, 0);
				setBorder(new CompoundBorder(outside, inside));
			}
		}
	}
	
	/**
	 * 更新按纽
	 */
	private void updateButtonUI() {
		// 更新
		if (cmdLeft != null) {
			cmdLeft.updateUI();
		}
		if (cmdRight != null) {
			cmdRight.updateUI();
		}
		for (DockButton button : array) {
			button.updateUI();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 边框
		setDefaultBorder();
		// 更新
		if (cmdLeft != null && cmdRight != null) {
			updateButtonUI();
		}
		
		if (rockMenu != null) {
			// 同时修改字体和UI。注意，只更新菜单本身！
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
	}

	/**
	 * 判断对应的按纽存在
	 * @param key KEY对象
	 * @return 返回真或者假
	 */
	public boolean hasButton(WKey key) {
		return findButton(key) != null;
	}

	/**
	 * 删除DOCK按纽
	 * @param button
	 * @return
	 */
	private boolean removeButton(DockButton button) {
		// 从面板中删除
		banner.remove(button);
		
		// 从内存中删除
		array.remove(button);
		
		// 找到第一个显示的应用
		DockButton first = null;
		if (banner.getComponentCount() > 0) {
			first = (DockButton) banner.getComponent(0);
		}
		int buttonIndex = (first != null ? array.indexOf(first) : -1);
		
		// 判断显示...
		boolean showing = isArrowShowing();
		// 如果在显示状态，重新计算它的显示结果
		if (showing) {
			int buttons = array.size();
			int showButtons = doButtons(getWidth() - borderWidth);
			// 如果实际按纽数量小于可显示的按纽数量，左右箭头按纽消失
			if (buttons <= showButtons) {
				cmdLeft.setVisible(false);
				cmdRight.setVisible(false);

				// 删除全部后，重新加载
				banner.removeAll();
				// 重新加载
				for (DockButton b : array) {
					banner.add(b);
				}
			} else {
				// 重新排列
				repaintRemoveButtons(buttonIndex);
			}
		}

		// 刷新
		revalidate();
		repaint();
		return true;
	}

	/**
	 * 增加宽度
	 */
	void doUpWidth() {
		// 找到第一个显示的应用
		DockButton first = null;
		int components = banner.getComponentCount();
		if (components > 0) {
			first = (DockButton) banner.getComponent(0);
		}
		if (first == null) {
			return;
		}
		// 找到数组中显示的第一个图标按纽
		int buttonIndex = array.indexOf(first);
		if (buttonIndex < 0) {
			return;
		}
		
		final int elements = array.size();
		
		// 当前可视按纽数量大于等于成员数
		int buttons = 0;
		// 可视范围数量大于等于成员数，下标从0开始（这是要减去Border的间隔空间）
		if (doButtons(getWidth() - borderWidth) >= elements) {
			buttonIndex = 0;
			buttons = doButtons(getWidth() - borderWidth);

			cmdLeft.setVisible(false);
			cmdRight.setVisible(false);
		} else {
			int w = banner.getWidth();
			buttons = doButtons(w);
			if (buttonIndex + buttons >= elements) {
				buttonIndex = elements - buttons;
				if (buttonIndex < 0) buttonIndex = 0;
			}
		}

		// 最后显示成员下标
		int endIndex = (buttonIndex + buttons >= elements ? elements : buttonIndex + buttons);

		// 清除全部按纽
		banner.removeAll();
		for (int index = buttonIndex; index < endIndex; index++) {
			DockButton button = array.get(index);
			banner.setButtonSize(button);
			banner.add(button);
		}
		
		// 可视组件数
		components = banner.getComponentCount();
		
//		System.out.printf("Width:%d, Banner width:%d, Show components:%d, Elements:%d \n", getWidth(), banner.getWidth(), components, elements);

		// 可视组件大于等于成员数时，左右箭头不视化
		if (components >= elements) {
			cmdLeft.setVisible(false);
			cmdRight.setVisible(false);
		} else {
			cmdLeft.setVisible(true);
			cmdRight.setVisible(true);
			doLeftEnabled();
			doRightEnabled();
		}
		
		// 刷新
		revalidate();
		repaint();
	}
	
	/**
	 * 收缩宽度
	 */
	void doDownWidth() {
		// 找到第一个显示的应用
		DockButton first = null;
		int components = banner.getComponentCount();
		if (components > 0) {
			first = (DockButton) banner.getComponent(0);
		}
		if (first == null) {
			return;
		}
		// 找到数组中显示的第一个图标按纽
		int buttonIndex = array.indexOf(first);
		if (buttonIndex < 0) {
			return;
		}

		// 1. 计算当前显示图标
		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);
		
		// 一致时，不处理
		if (buttons >= components) {
			return;
		}
		
		// 全部成员
		int elements = array.size();
		int endIndex = (buttonIndex + buttons >= elements ? elements : buttonIndex + buttons);
		
		// 清除全部按纽
		banner.removeAll();
		for (int index = buttonIndex; index < endIndex; index++) {
			DockButton button = array.get(index);
			banner.setButtonSize(button);
			banner.add(button);
		}
		
		// 可视组件数
		components = banner.getComponentCount();

		// 可视组件大于等于成员数时，左右箭头不视化
		if (components >= elements) {
			cmdLeft.setVisible(false);
			cmdRight.setVisible(false);
		} else {
			cmdLeft.setVisible(true);
			cmdRight.setVisible(true);
			doLeftEnabled();
			doRightEnabled();
		}
		
		// 刷新
		revalidate();
		repaint();
	}

	/**
	 * 删除对应的按纽
	 * @param key
	 * @return
	 */
	public boolean deleteButton(WKey key) {
		DockButton button = findButton(key);
		// 按纽没有找到，忽略
		if (button == null) {
			return false;
		}
		return removeButton(button);
	}

	/**
	 * 增加一个按纽
	 * @param item
	 * @return
	 */
	public boolean addButton(DesktopDockButtonItem item) {
		DockButton button =	createButton(item.getIcon(), item.getToolTip());
		button.setItem(item);

		addButton(button);
		return true;
	}
	
	/** 以下是操作方法 **/

	/**
	 * 根据窗口句柄找到按纽实例
	 * @param frame 窗口实例
	 * @return 返回按纽实例
	 */
	private DockButton findButton(WKey key) {
		// 找到对应的按纽
		for (DockButton but : array) {
			DesktopDockButtonItem item = but.getItem();
			if (item == null) {
				continue;
			}
			if (Laxkit.compareTo(item.getKey(), key) == 0) {
				return but;
			}
		}
		return null;
	}

	/**
	 * 增加一个按纽
	 */
	private void addButton(DockButton button){
		// 保存按纽到数组中
		array.add(button);
		
		// 判断左右按纽显示
		boolean showing = isArrowShowing();
		if (!showing) {
			int buttons = array.size();
			int showButtons = doButtons(getWidth() - borderWidth);
			
//			System.out.printf("Dockbar width:%d, real dockbar width:%d, buttons:%d, show buttons:%d \n", 
//					getWidth(), getWidth() - borderWidth, buttons, showButtons);
			
			// 实际按纽数量大于可显示按纽数量时，左右箭头按纽显示，新的按纽不加入，重新计算位置
			if (buttons > showButtons) {
				cmdLeft.setVisible(true);
				cmdRight.setVisible(true);
				cmdLeft.setEnabled(true);
				cmdRight.setEnabled(true);
				// 计算有效
				doLeftEnabled();
				doRightEnabled();
			} else {
				// 上面条件不成立时，按纽加到面板中显示
				banner.add(button);
			}
		} else {
			// 新增按纽后，重新计算左右按纽的有效
			doLeftEnabled();
			doRightEnabled();
		}
		
		// 刷新
		revalidate();
		repaint();
	}
	
	/**
	 * 判断左右箭头按纽是显示状态，任何一侧显示皆可
	 * @return 返回真或者假
	 */
	private boolean isArrowShowing() {
		boolean left = (cmdLeft.isVisible() && cmdLeft.isShowing());
		boolean right = (cmdRight.isVisible() && cmdRight.isShowing());
		return left || right;
	}

//	/**
//	 * 根据面板长度，计算可显示按纽数目
//	 * @param barWidth
//	 * @return 返回可以显示的按纽数目
//	 */
//	private int doButtons(int barWidth) {
//		int buttons = 0;
//		int width = 0;
//		
//		do {
//			// 记录间隔
//			if (width > 0) {
//				width += DesktopDock.DOCK_BUTTON_GAP; 
//			}
//			width += DockBanner.BUTTON_WIDTH;
//			// 超过宽度，退出
//			if (width > barWidth) {
//				break;
//			}
//			buttons++;
//		} while(true);
//		
//		return buttons;
//	}
	
	/**
	 * 根据面板长度，计算可显示按纽数目
	 * @param barWidth
	 * @return
	 */
	private int doButtons(int barWidth) {
		// 可以显示的按纽数目
		return barWidth / (DockBanner.BUTTON_WIDTH + DesktopDock.DOCK_BUTTON_GAP);
	}
	
	/**
	 * 在可视状态下
	 */
	private void doLeftEnabled() {
		if (!cmdLeft.isVisible()) {
			return;
		}

		if (banner.getComponentCount() < 1 || array.size() < 1) {
			return;
		}

		Component first = banner.getComponent(0);
		DockButton button = array.get(0);
		// 已经是第一个时，左侧按纽无效
		boolean success = (first == button);
		if (success) {
			cmdLeft.setEnabled(false);
		} else {
			cmdLeft.setEnabled(true);
		}
	}
	
	private void doRightEnabled() {
		if (!cmdRight.isVisible()) {
			return;
		}

		if (banner.getComponentCount() < 1 || array.size() < 1) {
			return;
		}

		Component[] components = banner.getComponents();
		Component last = components[components.length - 1];
		DockButton button = array.get(array.size() - 1);
		// 已经是最后时，左侧按纽无效
		boolean success = (last == button);
		if (success) {
			cmdRight.setEnabled(false);
		} else {
			cmdRight.setEnabled(true);
		}
	}

	/**
	 * 向左移动FRAME按纽，确定最左侧的按纽，减法，向前移动
	 */
	private void doLeftArrow() {
		// 没有，忽略它
		if (banner.getComponentCount() == 0) {
			return;
		}
		
		Component begin = banner.getComponent(0);
		// 确定第一个显示按纽在队列数组中的下标
		int index = -1;
		int size = array.size();
		for (int i = 0; i < size; i++) {
			DockButton button = array.get(i);
			// 第一个显示的按纽
			if (button == begin) {
				index = i;
				break;
			}
		}
		if (index < 0) {
			// 这是错误，忽略
//			System.out.println("没有找到按纽位置，这是一个错误！");
			return;
		} else if (index == 0) {
			// 已经移到最前面，忽略
//			System.out.println("向左移动，已经在最左边！");
			return;
		}
		
		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);
		
		// 左侧的按纽索引
		int beginIndex = (index - buttons >= 0 ? index - buttons : 0);
		int endIndex = (beginIndex + buttons <= size ? beginIndex + buttons : size);
		
		// 清除全部按纽
		banner.removeAll();
		// 增加显示一个按纽
		for (int i = beginIndex; i < endIndex; i++) {
			DockButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);
		}
		
		doLeftEnabled();
		doRightEnabled();
		
		revalidate();
		repaint();
	}
	
	/**
	 * 向右移动FRAME按纽，确定显示的最右侧按纽，加法，向后移动
	 */
	private void doRightArrow() {
		// 没有，忽略它
		if (banner.getComponentCount() == 0) {
			return;
		}
		
		Component[] components = banner.getComponents();
		Component last = components[components.length - 1];
		// 最后一个显示按纽在队列数组中的下标
		int index = -1;
		int size = array.size();
		for (int i = 0; i < size; i++) {
			DockButton button = array.get(i);
			// 找到最后显示的按纽，然后向后移动一个，这是新的开始按纽
			if (button == last) {
				index = i + 1; 
				break;
			}
		}
		
		if (index < 0) {
//			System.out.println("向右移动，没有找到最后的可视按纽");
			return;
		} else if (index >= size) {
			// 已经在最后了，这里不要显示
//			System.out.println("向右移动，已经在最后了");
			return;
		}
		
		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);
		
		// 结束位置
		int endIndex = (index + buttons >= size ? size : index + buttons);
		int beginIndex = (endIndex - buttons >= 0 ? endIndex - buttons : 0);
		
		// 清除全部按纽
		banner.removeAll();
		// 增加显示一个按纽
		for (int i = beginIndex; i < endIndex; i++) {
			DockButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);
		}
		
		doLeftEnabled();
		doRightEnabled();
		
		revalidate();
		repaint();
	}
	
	/**
	 * 删除后重新组件
	 */
	private void repaintRemoveButtons(int index) {
		int size = array.size();
		// 没有了，忽略！
		if (size == 0) {
			return;
		}
		// 定位下标
		if (index < 0) {
			index = 0;
		} else if (index >= size) {
			index = 0;
		}
		
		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);
		
		// 结束位置
		int endIndex = (index + buttons >= size ? size : index + buttons);
		int beginIndex = (endIndex - buttons >= 0 ? endIndex - buttons : 0);

		// 清除全部按纽
		banner.removeAll();
		// 增加显示按纽
		for (int i = beginIndex; i < endIndex; i++) {
			DockButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);
		}
		
		// 新增按纽后，重新计算左右按纽的有效
		doLeftEnabled();
		doRightEnabled();
	}
	
	/** 以下是执行应用 **/

	/**
	 * 启动应用
	 * @param input 输入命令
	 * @return 成功返回真，否则假
	 */
	private int startApplication(WKey key) {
		try {
			return ApplicationStarter.start(key);
		} catch (SecurityException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return -1;
	}

	private void startApplication(DockButton but) {
		DesktopDockButtonItem item = but.getItem();
		if (item == null) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			desktop.playSound(SoundTag.ERROR);
			MessageBox.showFault(desktop, title, content);
			return;
		}

		int ret = startApplication(item.getKey());
		if (ret != 0) {
			String title = UIManager.getString("ApplicationStart.startFaultTitle");
			String content = UIManager.getString("ApplicationStart.startFaultContent");
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			desktop.playSound(SoundTag.ERROR);
			MessageBox.showFault(desktop, title, content);
		}
	}
	
	class RunApplicationThread extends SwingEvent {
		DockButton button;

		public RunApplicationThread(DockButton e) {
			super();
			button = e;
		}

		@Override
		public void process() {
			startApplication(button);
		}
	}
	
//	class ClickThread extends WorkThread {
//		DockButton button;
//
//		public ClickThread(DockButton e) {
//			super();
//			button = e;
//		}
//
//		@Override
//		public void process() {
//			startApplication(button);
//			setInterrupted(true);
//		}
//	}
	
//	class RunApplicationThread extends SwingWorker<Integer, Object> {
//		
//		DockButton button;
//
//		public RunApplicationThread(DockButton e) {
//			super();
//			button = e;
//		}
//		
//		/**
//		 * 启动应用
//		 * @param input 输入命令
//		 * @return 成功返回真，否则假
//		 */
//		private int startApplication(WKey key) {
//			try {
//				return ApplicationStarter.start(key);
//			} catch (SecurityException e) {
//				Logger.error(e);
//			} catch (IllegalArgumentException e) {
//				Logger.error(e);
//			} catch (IOException e) {
//				Logger.error(e);
//			} catch (InstantiationException e) {
//				Logger.error(e);
//			} catch (IllegalAccessException e) {
//				Logger.error(e);
//			} catch (NoSuchMethodException e) {
//				Logger.error(e);
//			} catch (InvocationTargetException e) {
//				Logger.error(e);
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			}
//			return -1;
//		}
//
//		/* (non-Javadoc)
//		 * @see javax.swing.SwingWorker#doInBackground()
//		 */
//		@Override
//		protected Integer doInBackground() throws Exception {
//			DesktopDockButtonItem item = button.getItem();
//			if (item == null) {
//				return null;
//			}
//			// 启动应用
//			int ret = startApplication(item.getKey());
//			if (ret != 0) {
//				return new Integer(ret);
//			}
//			return new Integer(ret);
//		}
//		
//		/*
//		 * (non-Javadoc)
//		 * @see javax.swing.SwingWorker#done()
//		 */
//		@Override
//		protected void done() {
//			Integer ret = null;
//			try {
//				ret = get();
//			} catch (Exception e) {
//				Logger.error(e);
//			}
//			// 存在两种错误结果：1. 没有找到应用，2. 应用启动错误
//			if (ret == null) {
//				String title = UIManager.getString("ApplicationStart.notfoundTitle");
//				String content = UIManager.getString("ApplicationStart.notfoundContent");
//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			} else if (ret.intValue() != 0) {
//				String title = UIManager.getString("ApplicationStart.startFaultTitle");
//				String content = UIManager.getString("ApplicationStart.startFaultContent");
//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			}
//		}
//	}

	/**
	 * 单点/回车启动应用程序
	 * @param source
	 */
	private void click(Object source) {
		boolean success = (source != null && Laxkit.isClassFrom(source, DockButton.class));
		if (!success) {
			return;
		}
		// 启动应用软件
		DockButton button = (DockButton) source;

		//		// 启动应用
		//		RunApplicationThread rt = new RunApplicationThread(button);
		//		rt.execute();

		// 重新绘制图片
		addThread(new CancelButtonRollover(button));
		// 启动应用
		addThread(new RunApplicationThread(button));
	}
	
	/**
	 * 绘制METAL背景色
	 * @param g
	 */
	private boolean paintMetalComponent(Graphics g, String key) {
		ImageIcon icon = (ImageIcon) UIManager.getIcon(key); 
		if (icon == null) {
			return false;
		}
		
		// 宽度和高度
		int width = getWidth();
		int height = getHeight();
		
		Image image = icon.getImage();
		int imgWidth = image.getWidth(null);
		int imgHeight = image.getHeight(null);
		
		for (int y = 0; y < height; y += imgHeight) {
			for (int x = 0; x < width; x += imgWidth) {
				g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
			}
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// 如果是灰色，定义为
		boolean b = false;
		if (Skins.isGraySkin()) {
			b = paintMetalComponent(g, "DockBar.WallWhiteIcon");
		} else if (Skins.isBronzSkin()) {
			b = paintMetalComponent(g, "DockBar.WallBronzIcon");
		} else if (Skins.isCyanoSkin()) {
			b = paintMetalComponent(g, "DockBar.WallCyanoIcon");
		} else if (Skins.isDarkSkin()) {
			b = paintMetalComponent(g, "DockBar.WallDarkIcon");
		}
		// 不成功，绘制默认的
		if (!b) {
			super.paintComponent(g);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		// 特别注意！这里必须保持一个空方法，不会任何处理，否则VM会调用它的父类，这时窗口边缘会出现“毛刺”现象，影响了整体美观效果。
	}
	
	public void writeUserButtons() {
		File dir = DesktopSystem.createRuntimeDirectory();
		if (dir == null) {
			return;
		}
		// 配置目录下的字体文件
		File file = new File(dir, "docks.conf");
		
		try {
			int size = array.size();
			// 找到合适的对象
			ArrayList<DesktopDockButtonItem> a = new ArrayList<DesktopDockButtonItem>();
			for (int i = 0; i < size; i++) {
				DockButton but = array.get(i);
				DesktopDockButtonItem item = but.getItem();
				// 单元有效且是用户应用时，才写入
				if (item != null && item.isUser()) {
					a.add(item);
				}
			}

			// 写入有效的成员
			size = a.size();
			ClassWriter writer = new ClassWriter();
			writer.writeInt(size);
			for (DesktopDockButtonItem item : a) {
				writer.writeObject(item);
			}

			// 写入磁盘
			byte[] b = writer.effuse();
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.flush();
			out.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * 读取用户成员
	 * @return
	 */
	private DesktopDockButtonItem[] readUserElements() {
		File dir = DesktopSystem.createRuntimeDirectory();
		if (dir == null) {
			return new DesktopDockButtonItem[0];
		}
		// 配置目录下的字体文件
		File file = new File(dir, "docks.conf");
		// 没有这个文件，忽略它
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return new DesktopDockButtonItem[0];
		}
		
		ArrayList<DesktopDockButtonItem> array = new ArrayList<DesktopDockButtonItem>();
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();

			ClassReader reader = new ClassReader(b);
			int size = reader.readInt();
			for(int i =0; i < size; i++){
				DesktopDockButtonItem item = new DesktopDockButtonItem(reader);
				array.add(item);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		
		// 输出
		int size = array.size();
		DesktopDockButtonItem[] a = new DesktopDockButtonItem[size];
		return array.toArray(a);
	}
	
	/**
	 * 显示已经加载的DOCK按纽
	 */
	private void showDefaultButtons() {
		// 读取本地的保存...
		DesktopDockButtonItem[] buttons = readUserElements();

		// 显示全部按纽
		for (DesktopDockButtonItem item : buttons) {
			addButton(item);
		}
	}
	
	/**
	 * 加载显示用户按纽
	 */
	public void loadDefaultButtons() {
		showDefaultButtons();
	}

}