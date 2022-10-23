/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;

import com.laxcus.gui.tray.*;
import com.laxcus.platform.*;

/**
 * 托盘控制器
 * 显示/关闭托盘窗口
 * 
 * @author scott.liang
 * @version 1.0 2/20/2022
 * @since laxcus 1.0
 */
class TrayController {
	
	private DesktopStatusBar statusBar;

	/** 托盘窗口 **/
	private TrayWindow window;

	/**
	 * 构造托盘控制器
	 */
	public TrayController(DesktopStatusBar e) {
		super();
		statusBar = e;
	}

	/**
	 * 判断是显示状态
	 * @return
	 */
	public boolean isShowing() {
		return window != null && window.isVisible();
	}

	/**
	 * 判断已经隐藏
	 * @return
	 */
	public boolean isHided() {
		return window == null;
	}

	/**
	 * 隐藏
	 */
	public void hide() {
		if (window != null) {
			window.setVisible(false);
			window = null;
		}
	}

	/**
	 * 弹出窗口
	 * @param event
	 * @param invoker
	 */
	public boolean show(Component invoker) {
		if (isShowing()) {
			hide();
		}

		// 拿到管理器
		DesktopTrayManager manager = (DesktopTrayManager) PlatformKit.getTrayManager();
		// 输出
		Tray[] trays = manager.toArray();
		int size = (trays != null ? trays.length : 0);
		if (size == 0) {
			statusBar.setTrayStatus(true);
			return false;
		}

		// 生成新的实例
		window = new TrayWindow();
		window.show(invoker, trays);
		return true;
	}

	/**
	 * 重新绘制
	 */
	public void redraw() {
		if (isShowing()) {
			Component invoker = window.getInvoker();
			show(invoker);
		}
	}

}


//	/** 按纽 **/
//	private ArrayList<LightFrame> array = new ArrayList<LightFrame>();

//	class ActionAdapter implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			//			System.out.printf("KAO, IS %s\n", e.getSource().getClass().getName());
//		}
//	}
//
//	class ButtonMouseAdapter extends MouseAdapter {
//
//		public void mouseClicked(MouseEvent e) {
//			TrayPopupMenu menu = new TrayPopupMenu();
//			//			menu.add(new JMenuItem("一畦春韭绿，十里稻花香"));
//			//			menu.add(new JMenuItem("春江潮水连海平，海上明月共潮生"));
//			//			menu.add(new JMenuItem("万里念将归"));
//
//			menu.add(new JMenuItem("肃肃秋风起，悠悠万里行"));
//			menu.add(new JMenuItem("万里何所成，横漠筑长城"));
//			menu.addSeparator();
//			menu.add(new JMenuItem("暮江春不动，春花满正开"));
//			menu.add(new JMenuItem("流波将月去，潮水带星来"));
//			menu.addSeparator();
//			menu.add(new JMenuItem("长江悲已滞，万里念将归"));
//			menu.add(new JMenuItem("况属高风晚，山山黄叶飞"));
//			menu.addSeparator();
//			menu.add(new JMenuItem("千山鸟飞绝，万迹人踪灭"));
//			menu.add(new JMenuItem("孤舟蓑荘翁，独钩寒江雪"));
//
//			JComponent sub = (JComponent)e.getSource();
//			menu.show(sub, e.getX(), e.getY());
//		}
//	}

//	class DesktopTray extends Tray {
//
//		/**
//		 * 生成实例
//		 * @param icon
//		 * @param tooltip
//		 */
//		public DesktopTray(Icon icon, String tooltip) {
//			super(icon, tooltip);
//			addActionListener(new ActionAdapter());
//			addMouseListener(new ButtonMouseAdapter());
//		}
//
//		//		/* (non-Javadoc)
//		//		 * @see com.laxcus.gui.tray.Tray#getTrayPopupMenu()
//		//		 */
//		//		@Override
//		//		public TrayPopupMenu getPopupMenu() {
//		//			TrayPopupMenu menu = new TrayPopupMenu();
//		////			menu.add(new JMenuItem("一畦春韭绿，十里稻花香"));
//		////			menu.add(new JMenuItem("春江潮水连海平，海上明月共潮生"));
//		////			menu.add(new JMenuItem("万里念将归"));
//		//			
//		//			menu.add(new JMenuItem("肃肃秋风起，悠悠万里行"));
//		//			menu.add(new JMenuItem("万里何所成，横漠筑长城"));
//		//			menu.addSeparator();
//		//			menu.add(new JMenuItem("暮江春不动，春花满正开"));
//		//			menu.add(new JMenuItem("流波将月去，潮水带星来"));
//		//			menu.addSeparator();
//		//			menu.add(new JMenuItem("长江悲已滞，万里念将归"));
//		//			menu.add(new JMenuItem("况属高风晚，山山黄叶飞"));
//		//			return menu;
//		//		}
//
//		//		/* (non-Javadoc)
//		//		 * @see com.laxcus.gui.tray.Tray#getTrayMenu()
//		//		 */
//		//		@Override
//		//		public TrayPopupMenu getTrayPopupMenu() {
//		//			TrayPopupMenu menu = new TrayPopupMenu();
//		////			menu.setIconTextGap(4);
//		////			menu.setIcon(getFrameIcon());
//		////			menu.setText(UIReader.getString("Picture.TrayTitle")); 
//		//			
//		//			
//		//			return menu;
//		//		}
//
//	}

//	/**
//	 * 在托盘窗口可视状态下，注销一个图标
//	 * @param tray
//	 */
//	public void unregister(Tray tray) {
//		if (window != null && window.isVisible()) {
//			window.remove(tray);
//		}
//	}

//	/**
//	 * 弹出窗口
//	 * @param event
//	 * @param invoker
//	 */
//	public boolean show(MouseEvent event) {
//		if (isShowing()) {
//			hide();
//		}
//
//		int size = array.size();
//		if (size == 0) {
//			return false;
//		}
//
////		int count = 0;
////		JPopupMenu rockMenu = new JPopupMenu();
//		
//		
////		ruckMenu.
////		for (int i = 0; i < size; i++) {
////			LightFrame frame = array.get(i);
////			TrayMenu[] elements = frame.createTrayMenu();
////			for (TrayMenu menu : elements) {
////				rockMenu.add(menu);
////				count++;
////			}
////		}
////		// 没有返回
////		if (count == 0) {
////			return;
////		}
//
//		//		for (int i = 0; i < 20; i++) {
//		//			String title = String.format("他年我若为青帝，报与桃花一处开 %d", i + 1);
//		//			rockMenu.add(new JMenuItem(title));
//		//			if (i > 0 && i % 2 == 0) {
//		//				rockMenu.addSeparator();
//		//			}
//		//		}
//
//		
//		Tray[] trays = new Tray[size];
//		for (int i = 0; i < size; i++) {
//			Icon icon = UIManager.getIcon("DesktopWindow.TitleIcon");
//			String tooltip = String.format("当前托盘索引是 %s", i + 1);
//			trays[i] = new DesktopTray(icon, tooltip);
//		}
//		// 显示
//		JComponent invoker = (JComponent) event.getSource();
//		window = new TrayWindow();
//		window.show(invoker, trays);
//		return true;
//
////		int newX = event.getX();
////		int newY = event.getY();
////		rockMenu.show(invoker, newX, newY);
//	}

//	/**
//	 * 弹出窗口
//	 * @param event
//	 * @param invoker
//	 */
//	public void show(MouseEvent event) {
////		int size = array.size();
////		if (size == 0) {
////			return;
////		}
//
//		JPopupMenu rockMenu = new JPopupMenu();
//		for (int i = 0; i < 20; i++) {
//			String title = String.format("他年我若为青帝，报与桃花一处开 %d", i + 1);
//			rockMenu.add(new JMenuItem(title));
//			if (i > 0 && i % 2 == 0) {
//				rockMenu.addSeparator();
//			}
//		}
//
//		JComponent invoker = (JComponent)event.getSource();
//		int newX = event.getX();
//		int newY = event.getY();
//		rockMenu.show(invoker, newX, newY);
//	}

//	/**
//	 * 弹出窗口
//	 * @param event
//	 * @param invoker
//	 */
//	public void show(MouseEvent event) {
//		int size = array.size();
//		if (size == 0) {
//			return;
//		}
//
//		int count = 0;
////		JPopupMenu rockMenu = new JPopupMenu();
//		
//		TrayPopupMenu rockMenu = new TrayPopupMenu();
//		for (int i = 0; i < size; i++) {
//			LightFrame frame = array.get(i);
//			TrayMenu[] elements = frame.createTrayMenu();
//			for (TrayMenu menu : elements) {
//				rockMenu.add(menu);
//				count++;
//			}
//		}
//		// 没有返回
//		if (count == 0) {
//			return;
//		}
//
//		//		for (int i = 0; i < 20; i++) {
//		//			String title = String.format("他年我若为青帝，报与桃花一处开 %d", i + 1);
//		//			rockMenu.add(new JMenuItem(title));
//		//			if (i > 0 && i % 2 == 0) {
//		//				rockMenu.addSeparator();
//		//			}
//		//		}
//
//		JComponent invoker = (JComponent) event.getSource();
//
//		int newX = event.getX();
//		int newY = event.getY();
//		rockMenu.show(invoker, newX, newY);
//	}


//	/**
//	 * 判断窗口存在
//	 * @param e
//	 * @return
//	 */
//	public boolean hasFrame(LightFrame e) {
//		return array.contains(e);
//	}
//
//	/**
//	 * 增加窗口
//	 * @param e
//	 * @return
//	 */
//	public boolean addFrame(LightFrame e) {
//		if (hasFrame(e)) {
//			return false;
//		}
//		return array.add(e);
//	}
//
//	/**
//	 * 删除窗口
//	 * @param e
//	 * @return
//	 */
//	public boolean removeFrame(LightFrame e) {
//		return array.remove(e);
//	}