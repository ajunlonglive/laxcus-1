/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import com.laxcus.application.manage.*;
import com.laxcus.gui.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;

/**
 * 桌面应用坞
 * 显示当前的应用
 * 
 * @author scott.liang
 * @version 1.0 10/5/2021
 * @since laxcus 1.0
 */
public class RayDock extends LightForm {
	
	private static final long serialVersionUID = 2340037168878569382L;

	/** 应用坞高度固定是56个像素 **/
	private final static int DOCK_HEIGHT = 56;
	
	/** FRAME按纽之间的间隔像素 **/
	final static int DOCK_BUTTON_GAP = 4;
	
//	class DockMouseAdapter extends MouseAdapter {
//		
//		DesktopDock dock;
//
//		/** 拖放 **/
//		private boolean dragged;
//
//		/** 坐标 **/
//		private Point axis;
//		
//		public DockMouseAdapter(DesktopDock e){
//			super();
//			dragged = false;
//			dock = e;
//		}
//		
//		// 取鼠标的坐标
////		point = SwingUtilities.convertPoint((DesktopButton) source, e.getPoint(), ((DesktopButton) source).getParent());
//
//		public void mousePressed(MouseEvent e) {
//			dragged = true;
//			axis = new Point(e.getX(), e.getY());
//			setCursor(new Cursor(Cursor.MOVE_CURSOR));
//		}
//		
////		public void mousePressed(MouseEvent e) {
////			Object source = e.getSource();
////			if (!Laxkit.isClassFrom(source, Component.class)) {
////				return;
////			}
////			
////			dragged = true;
////			Component component = (Component) source;
////			axis = SwingUtilities.convertPoint(component, e.getPoint(), dock);
////			
//////			axis = new Point(e.getX(), e.getY());
//////			axis = new Point(e.getXOnScreen(), e.getYOnScreen());
////			
////			setCursor(new Cursor(Cursor.MOVE_CURSOR));
////		}
//
//		public void mouseReleased(MouseEvent e) {
//			dragged = false;
//			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//		}
//
//		public void mouseDragged(MouseEvent e) {
//			if (dragged) {
//				int x = e.getXOnScreen() - axis.x;
//				int y = e.getYOnScreen() - axis.y;
//				setLocation(x, y);
//			}
//		}
//		
////		public void mouseDragged(MouseEvent e) {
////			if (!dragged) {
////				return;
////			}
////			
////			Object source = e.getSource();
////			if (!Laxkit.isClassFrom(source, Component.class)) {
////				return;
////			}
////			Component but = (Component) source;
////			
////			Point newPoint = SwingUtilities.convertPoint(but, e.getPoint(), dock);
////			int x = newPoint.x - axis.x;
////			int y = newPoint.y - axis.y;
////			dock.setLocation(x, y);
////			
////			axis = newPoint;
//////			dock.setLocation(newPoint.x, newPoint.y);
////		}
//
//	}
	
	class DockMouseAdapter extends MouseAdapter {

		/** 拖放 **/
		private boolean dragged;

		/** 坐标 **/
		private Point axis;

		public DockMouseAdapter(){
			super();
			dragged = false;
		}
		
		public void mousePressed(MouseEvent e) {
			dragged = true;
			axis = new Point(e.getX(), e.getY());
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}

		public void mouseReleased(MouseEvent e) {
			dragged = false;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseDragged(MouseEvent e) {
			if (dragged) {
				int x = e.getXOnScreen() - axis.x;
				int y = e.getYOnScreen() - axis.y;
				setLocation(x, y);
			}
		}
	}

//	class DockFrameAdapter extends InternalFrameAdapter {
//		
//		public void internalFrameActivated(InternalFrameEvent e) {
////			LightFrame frame = (LightFrame) e.getSource();
////			PlatformDesktop desktop = PlatfromKit.getPlatformDesktop();
////			// 去通知任务栏，通知激活
////			if (desktop != null && frame != null) {
////				desktop.activate(frame);
////			}
//		}
//
//		public void internalFrameDeactivated(InternalFrameEvent e) {
////			LightFrame frame = (LightFrame) e.getSource();
////			PlatformDesktop desktop = PlatfromKit.getPlatformDesktop();
////			if (desktop != null && frame != null) {
////				desktop.deactivate(frame);
////			}
//		}
//	}
	
//	class DockComponentAdapter implements ComponentListener {
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
//		 */
//		@Override
//		public void componentHidden(ComponentEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
//		 */
//		@Override
//		public void componentMoved(ComponentEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
//		 */
//		@Override
//		public void componentResized(ComponentEvent e) {
//			// TODO Auto-generated method stub
//			doComponentResized(e);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
//		 */
//		@Override
//		public void componentShown(ComponentEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
//	
//	public void doComponentResized(ComponentEvent e) {
//		Rectangle r = ((Component) e.getSource()).getBounds();
//		
//		this.setBounds(r.x, r.y, r.width, 56);
//		validate();
//	}
	
	/** 已经加载或者否 **/
	private volatile boolean loaded;

	private DockMouseAdapter mouseListener;

	/** 构造应用坞面板 **/
	private DockBar dockBar;
	
	/** 边缘条面板 **/
	private EdgeBar edgeBar;

	/**
	 * 判断是隐藏状态
	 * @return 返回真或者假
	 */
	public boolean isHidden() {
		return !isVisible();
	}
	
//	public Dimension getPreferredSize() {
//		Dimension d = super.getPreferredSize();
//		d.height = 56;
////		this.firePropertyChange(arg0, arg1, arg2);
//		this.componentResized();
//		return d;
//	}
	
	/**
	 * 构造默认的桌面应用坞
	 */
	public RayDock() {
		super();
		mouseListener = new DockMouseAdapter();
		// 记录事件 
//		addInternalFrameListener(new DockFrameAdapter());
//		// 鼠标事件，用来拖放窗口
//		this.addComponentListener(new DockComponentAdapter());
		
//		// 设置默认的边框
//		setDefaultBorder();
		
		// 应用坞最小宽度
		String value = UIManager.getString("DockBar.MinWidth");
		dockMinWidth = ConfigParser.splitInteger(value, 230);
		
		// 没有加载
		loaded = false;
	}

//	/**
//	 * @param title
//	 */
//	public DesktopDock(String title) {
//		super(title);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param title
//	 * @param resizable
//	 */
//	public DesktopDock(String title, boolean resizable) {
//		super(title, resizable);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param title
//	 * @param resizable
//	 * @param closable
//	 */
//	public DesktopDock(String title, boolean resizable, boolean closable) {
//		super(title, resizable, closable);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param title
//	 * @param resizable
//	 * @param closable
//	 * @param maximizable
//	 */
//	public DesktopDock(String title, boolean resizable, boolean closable,
//			boolean maximizable) {
//		super(title, resizable, closable, maximizable);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param title
//	 * @param resizable
//	 * @param closable
//	 * @param maximizable
//	 * @param iconifiable
//	 */
//	public DesktopDock(String title, boolean resizable, boolean closable,
//			boolean maximizable, boolean iconifiable) {
//		super(title, resizable, closable, maximizable, iconifiable);
//		// TODO Auto-generated constructor stub
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.gui.LightForm#closeWindow()
//	 */
//	@Override
//	public void closeWindow() {
//		// TODO Auto-generated method stub
//
//	}

//	/**
//	 * 设置NIMBUS界面
//	 */
//	private void setNimbusBorder() {
//		if (isFixedNimbusBorder()) {
//			JInternalFrame dlg = new JInternalFrame();
//			Border border = dlg.getBorder();
//			if (border != null) {
//				Border m = getBorder();
//				if (m == null || m.getClass() != border.getClass()) {
//					setBorder(border);
//				}
//			}
//		} else {
//			// NIMBUS界面，TOP是0个像素，这时不会有横线，否则会出现。LEFT/BOTTOM/RIGHT保持5个像素，这时界面感觉最佳。
//			setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
//		}
//	}

//	/**
//	 * 设置默认的边框，分为Nimbus/Metal界面
//	 */
//	protected void setDefaultBorderX() {
//		// NIMBUS界面
//		if (isNimbusUI()) {
//			// //
//			// NIMBUS界面，TOP是0个像素，这时不会有横线，否则会出现。LEFT/BOTTOM/RIGHT保持5个像素，这时界面感觉最佳。
//			// setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
//
//			// setNimbusBorder();
//
//			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
//		}
//		// METAL界面，浮凸出的效果
//		else if (isMetalUI()) {
//			Color c = super.getBackground();
//			ESL esl = new RGB(c).toESL();
//			Color b = esl.toBrighter(50).toColor();
//			BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
//			setBorder(line);
//		}
//	}
	
//	private void setDefaultBorder() {
////		Color c = super.getBackground();
//		ESL esl = new RGB(c).toESL();
//		Color b = esl.toBrighter(50).toColor();
////		BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
//		LineBorder line = new LineBorder(b, 6);
//		CompoundBorder cb = new CompoundBorder( line, new EmptyBorder(6,6,6,6));
//		setBorder(line);
////		BevelBorder line = new BevelBorder(BevelBorder.RAISED, c,c);
////		setBorder(line);
//	}
	
//	private void setDefaultBorder() {
//		if (isNimbusUI()) {
//			// 面板的背景
//			Color c = UIManager.getColor("Panel.background");
//			if (c == null) {
//				return;
//			}
//			ESL esl = new RGB(c).toESL();
//			Color b = esl.toDraker(20).toColor();
//			LineBorder line = new LineBorder(b, 6);
//			if (dockBar != null) {
//				dockBar.setBackground(b);
//			}
//			setBorder(line);
//		} else if (isMetalUI()) {
////			Color c = MetalLookAndFeel.getWindowTitleBackground();
////			ESL esl = new RGB(c).toESL();
////			Color b = esl.toBrighter(20).toColor();
////			b = new Color(c.getRGB());
////			LineBorder line = new LineBorder(b, 6);
////			if (dockBar != null) {
////				dockBar.setBackground(b);
////			}
////			setBorder(line);
//			
////			Color c = MetalLookAndFeel.getWindowBackground();
//			
//			Color c = UIManager.getColor("Panel.background");
//			ESL esl = new RGB(c).toESL();
//			Color b = esl.toBrighter(18).toColor();
//			b = new Color(c.getRGB());
//			LineBorder line = new LineBorder(b, 6);
//			if (dockBar != null) {
//				dockBar.setBackground(b);
//			}
//			setBorder(line);
//		}
//		// 不透明是假的
//		setOpaque(false);
//		
//		////		if (banner == null) {
//		////			return;
//		////		}
//		////		LookAndFeel lf =	UIManager.getLookAndFeel();
//		//		// 面板的背景
//		//		Color c = UIManager.getColor("Panel.background");
//		//		if(c == null) {
//		//			return;
//		//		}
//		//		Color color = new Color(c.getRGB());
//		//		
//		//
//		////		Color c = getBackground();
//		////		ESL esl = new RGB(c).toESL();
//		////		Color b = esl.toBrighter(50).toColor();
//		////		BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
//		//		LineBorder line = new LineBorder(color, 6);
//		////		CompoundBorder cb = new CompoundBorder( line, new EmptyBorder(6,6,6,6));
//		//		setBorder(line);
//		////		BevelBorder line = new BevelBorder(BevelBorder.RAISED, c,c);
//		////		setBorder(line);
//	}
	
	/**
	 * 设置默认边框
	 */
	protected void setDefaultBorder() {
		// 没有边框时，不能缩放窗口了!
		setBorder(new EmptyBorder(0, 0, 0, 0));
		
//		setBorder(new EmptyBorder(1,1,1,1));

		//		if (isNimbusUI()) {
		//			setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		//		} else if (isMetalUI()) {
		//			setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		//		}

		//		Color color = UIManager.getColor("Panel.background");
		//		if (color != null) {
		////			if (isMetalUI()) {
		////				ESL esl = new RGB(color).toESL();
		////				color = esl.toBrighter(10).toColor();
		////			}
		//			
		//			ESL esl = new RGB(color).toESL();
		//			color = esl.toDraker(5).toColor();
		//		}
		//		if (color != null) {
		//			// LineBorder line = new LineBorder(color, 8, true);
		////			LineBorder line = new LineBorder(color, 2, true);
		//			
		//			SoftBevelBorder line = new SoftBevelBorder(BevelBorder.RAISED, color, color, color, color);
		//			setBorder(line);
		////			if (dockBar != null) {
		////				dockBar.setBackground(color);
		////				dockBar.setOpaque(false); // 要求透明
		////			}
		//		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#setIcon(boolean)
	 */
	@Override
	public void setIcon(boolean icon) {
		// 图标化，隐藏窗口；否则否
		if (icon) {
			if (isVisible()) {
				// normal bounds to maximized state.
				if (!isMaximum()) {
					setNormalBounds(getBounds());
				}
				super.setVisible(false);
			}
		} else {
			if (!isVisible()) {
				super.setVisible(true);
			}
		}
	}
	
	/**
	 * 读取状态
	 * @return
	 */
	public int readLayer() {
		final String key = "DesktopDock/Layer";
		// 读一个整数，默认是DOCK，有窗口前面
		return RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, key, FormLayer.DOCK);

		//		// 如果没有定义时
		//		if (!RTKit.hasInteger(RTEnvironment.ENVIRONMENT_SYSTEM, key)) {
		//			return JLayeredPane.POPUP_LAYER;
		//		}
		//		// 读一个整数
		//		return RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, key);
	}

	/**
	 * 写入状态
	 * @param layer
	 */
	private boolean writeLayer(int layer) {
		return RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopDock/Layer", layer);
	}
	
	/**
	 * 切换和绑定到另一个状态
	 * @return 返回真的绑定状态，负数是无效
	 */
	public int blockTo() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		// 当前的层次
		int who = PlatformDesktop.getLayer(this);
		int layer = (who == FormLayer.DOCK ? FormLayer.DEFAULT : FormLayer.DOCK);

		// 找到当前窗口的下标位置
		int index = desktop.getIndexOf(this);
		if (index == -1) {
			return -1;
		}
		
		// 不可视
		super.setVisible(false);
		// 删除
		desktop.remove(index);
		// 重新加入，注意：必须是Integer对象，否则会失效
		desktop.add(this, new Integer(layer));
		
		// 改成可视
		super.setVisible(true);
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
		
		// 写入内存中
		writeLayer(layer);
		
		// 当前层次
		return layer;
	}

//	/**
//	 * 显示窗口
//	 */
//	private void doShow() {
//		// 取得桌面实例
//		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
//		if (desktop == null) {
//			throw new HeadlessException("cannot be find desktop pane!");
//		}
//		
////		desktop.setDesktopManager(arg0);
//		
////		DesktopManager manager = desktop.getDesktopManager();
////		System.out.printf("DesktopManager class is %s\n", manager.getClass().getName());
//		
////		// 注册到桌面
////		desktop.register(this);
//		
//		// 保存的定义
//		int layer = readLayer();
//		
//		System.out.printf("启动层次是：%d\n", layer);
//
//		// 按照这个顺序做，可以正常显示
//		// 1. 调用超类显示
//		super.setVisible(true);
//		// 2. 增加到桌面面板上，在窗口和对话框之上
//		// 注意！必须是Integer对象，否则会失效！
//		desktop.add(this, new Integer(layer));
//		
////		desktop.add(this, JLayeredPane.POPUP_LAYER);
//		// 3. 设置为选择状态
//		try {
//			setSelected(true);
//		} catch (java.beans.PropertyVetoException e) {
//			Logger.error(e);
//		}
//		
//		// 验证此容器及其所有子组件
//		desktop.validate(); 
//		
////		Container parent = getParent();
////		System.out.printf("%s 父类窗口 %s\n", getClass().getName(), (parent == null ? "没有" : parent
////				.getClass().getName()));
//
//		// 显示上层界面
//		if (super.isVisible() && !isShowing()) {
//			Container c = getParent();
//			while (c != null) {
//				if (c.isVisible() == false) {
//					c.setVisible(true);
//				}
//				c = c.getParent();
//			}
//		}
//		
////		// 保存句柄
////		desktop.addFrame(this);
//		
//		// 已经加载
//		loaded = true;
//	}
	
	/**
	 * 显示窗口
	 */
	private void doShow() {
		// 取得桌面实例
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		if (desktop == null) {
			throw new HeadlessException("cannot be find desktop pane!");
		}
		
		// 保存的定义
		int layer = readLayer();
		// 按照这个顺序做，可以正常显示
		// 1. 调用超类显示
		super.setVisible(true);
		// 2. 增加到桌面面板上，在窗口和对话框之上
		// 注意！必须是Integer对象，否则会失效！
		desktop.add(this, new Integer(layer));
		// 3. 设置为选择状态
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
		
		// 验证此容器及其所有子组件
		desktop.validate(); 
		
		// 显示上层界面
		if (super.isVisible() && !isShowing()) {
			Container c = getParent();
			while (c != null) {
				if (c.isVisible() == false) {
					c.setVisible(true);
				}
				c = c.getParent();
			}
		}
		
		// 已经加载
		loaded = true;
	}
	
	/**
	 * 设置成可视化
	 * @param b 可视或者否
	 */
	@Override
	public void setVisible(boolean b) {
		if (b) {
			// 如果已经加载，这时调用父类函数，显示窗口；否则执行显示处理
			if (loaded) {
				super.setVisible(true);
			} else {
				doShow();
			}
		} else {
			super.setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.LightForm#closeWindow()
	 */
	@Override
	public void closeWindow() {
		// 如果处于可视状态，返回窗口范围
		Rectangle r = super.getBounds();
		// 记录位置
		if (r != null) {
			r = new Rectangle(r.x, r.y, r.width, r.height);
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopDock/Bound", r);
		}

		// 取消注册
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
//		if (desktop != null) {
////			desktop.unregister(this);
//		}
		
//		// 在关闭窗口前释放资源
//		release0();
		
//		System.out.printf("关闭窗口：[%s]\n", getTitle());

		// 取消可视
		setVisible(false);
		// 销毁
		dispose();

//		// 删除句柄
//		desktop.removeFrame(this);
//
//		// 在关闭窗口后释放资源
//		release1();

		// 释放，不再加载
		loaded = false;
		
		// 重新绘制窗口
		if (r != null && desktop != null) {
			desktop.repaint(r.x, r.y, r.width, r.height);
		}
	}
	
//	private void initXXX() {
//		dockBar = new DockBar();
//		dockBar.init();
//		
////		dockBar.setOpaque(false);
//		
//		// 鼠标事件...
////		addMouseListener(mouseListener);
////		addMouseMotionListener(mouseListener);
//		
//		dockBar.addMouseListener(mouseListener);
//		dockBar.addMouseMotionListener(mouseListener);
//		
////		dockBar.banner.addMouseListener(mouseListener);
////		dockBar.banner.addMouseMotionListener(mouseListener);
//		
////		dockBar.addSubMouseAdapter(mouseListener);
//		
//		// 设置面板
//		setContentPane(dockBar);
//	}
	
	private void init() {
		dockBar = new DockBar(this);
		dockBar.init();

		edgeBar = new EdgeBar();
		edgeBar.init();
		
//		// 显示或者否
//		boolean show = dockBar.isShowEdge();
//		edgeBar.setVisible(show);

		//		dockBar.setOpaque(false);

		// 鼠标事件...
		//		addMouseListener(mouseListener);
		//		addMouseMotionListener(mouseListener);

		dockBar.addMouseListener(mouseListener);
		dockBar.addMouseMotionListener(mouseListener);

		//		DockMouseAdapter md = new DockMouseAdapter();
		//		edgeBar.addMouseListener(md);
		//		edgeBar.addMouseMotionListener(md);

		//		dockBar.banner.addMouseListener(mouseListener);
		//		dockBar.banner.addMouseMotionListener(mouseListener);

		//		dockBar.addSubMouseAdapter(mouseListener);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(dockBar, BorderLayout.CENTER);
		panel.add(edgeBar, BorderLayout.EAST);
		// 设置面板
		setContentPane(panel);
	}
	
	// 高度是固定的...
//	static final int DOCK_WIDTH = 600;
	
	
	/**
	 * 计算最佳宽度
	 * @return
	 */
	private int getPreferredWidth() {
		// 显示在窗口下方
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		
		// 计算宽度，最小值
		int dockWidth = (d.width <= 1024 ? d.width / 2 : d.width / 3);
		if (dockWidth <= 600) {
			dockWidth = 600; // 最小是600个像素
		}
		
		// 计算X坐标
		int x = (d.width - dockWidth) / 2;
		if (x < 0) x = 0;
		if (x + dockWidth >= d.width) {
			dockWidth = d.width - 20;
		}
		return dockWidth;
	}
	
	/**
	 * 设置默认的范围
	 */
	private void setDefaultBounds() {
		// 显示在窗口下方
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();

		// 最佳宽度
		int dockWidth = getPreferredWidth();
		int x = (d.width - dockWidth) / 2;
		if (x < 0) x = 0;

		// Y坐标
		// int y = d.height - DOCK_HEIGHT - 16;

		// Y坐标，固定是0
		int y = 0;
		Rectangle r = new Rectangle(x, y, dockWidth, DOCK_HEIGHT);
		setBounds(r);
	}
	
	/**
	 * 设置在屏幕上的显示位置...
	 */
	private void setScreenBounds() {
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopDock/Bound");
		if (rect != null) {
//			int dockWidth = getPreferredWidth();
			
			// 可视尺寸
			Dimension d = PlatformKit.getPlatformDesktop().getSize();
			if (rect.width > d.width) {
				rect.x = 0;
				rect.width = d.width;
			} else if (rect.x + rect.width > d.width) {
				rect.x = d.width - rect.width;
			}
			if (rect.height != DOCK_HEIGHT) {
				rect.height = DOCK_HEIGHT;
			}
			setBounds(rect);
		} else {
			// 显示在中心
			setDefaultBounds();
		}
	}
	
	/**
	 * 显示窗口
	 */
	public void showWindow(RayEdgeListener listener) {
		setScreenBounds();

		// 初始化参数
		init();
		
		edgeBar.setEdgeListener(listener);

		//		panel.init();

		//		// 设置面板
		//		setContentPane(banner);

		//		// 内部事件
		//		addInternalFrameListener(new WindowCloseAdapter());

		// 图标和标题
		//		setFrameIcon(findImage("conf/ray/image/frame/log.png", 16, 16));
		//		setTitle(findCaption("Window/Frame/Log/title"));

		//		// 图标和标题
		//		setFrameIcon(UIManager.getIcon("DesktopDock.TitleIcon"));
		//		setTitle(UIManager.getString("DesktopDock.Title"));

		// 清除标题栏
		//		InternalFrameUI ui = getUI();
		//		if (Laxkit.isClassFrom(ui, BasicInternalFrameUI.class)) {
		//			((BasicInternalFrameUI) ui).setNorthPane(null);
		//		}
		
		// 清除标题栏
		doNoneTitle();
		setDefaultBorder();
		
		// 不可改变大小
		setResizable(false);

		// 最小化
		setIconifiable(false);
		// 可关闭
		setClosable(false);
		// 最大化
		setMaximizable(false);

		// 销毁窗口时触发事件
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//		DesktopLogTrustor.getInstance().setLogTransmitter(panel);

		// 句柄
		//		DesktopLogFrame.selfHandle = this;
		
		// 显示或者否
		boolean show = dockBar.isShowEdge();
		edgeBar.setVisible(show);
		
		// 显示窗口
		setVisible(true);
	}
	
	/**
	 * 清除标题
	 */
	private void doNoneTitle() {
		// 清除标题栏
		InternalFrameUI ui = getUI();
		if (Laxkit.isClassFrom(ui, BasicInternalFrameUI.class)) {
			((BasicInternalFrameUI) ui).setNorthPane(null);
		}
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JInternalFrame#setUI(javax.swing.plaf.InternalFrameUI)
//	 */
//	@Override
//	public void setUI(InternalFrameUI ui) {
//		System.out.printf("UI class is %s %s\n", ui.getClass().getName(), ui.getClass().getSuperclass().getName());
//		// 判断，更新UI
//		if (isMetalUI() && Laxkit.isClassFrom(ui, BasicInternalFrameUI.class)) {
//			//			System.out.println();
//			//			((BasicInternalFrameUI) ui).setNorthPane(null);
//
//			javax.swing.plaf.metal.MetalInternalFrameUI metal = new javax.swing.plaf.metal.MetalInternalFrameUI(this);
//			metal.setNorthPane(null);
//			super.setUI(metal);
//			System.out.println("Update Metal UI");
//		} else{
//			super.setUI(ui);
//		}
//	}
	

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gui.LightForm#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 清除标题栏
		doNoneTitle();
		// 设置边框
		setDefaultBorder();
	}

	/**
	 * 判断有这个按纽
	 * @param key
	 * @return
	 */
	public boolean hasButton(WKey key) {
		if (dockBar == null) {
			return false;
		}
		return dockBar.hasButton(key);
	}

	/**
	 * 删除按纽
	 * @param key
	 * @return
	 */
	public boolean deleteButton(WKey key) {
		if (dockBar == null) {
			return false;
		}
		return dockBar.deleteButton(key);
	}

	/**
	 * 增加按纽
	 * @param item
	 * @return
	 */
	public boolean addButton(RayDockButtonItem item) {
		if (dockBar == null) {
			return false;
		}
		return dockBar.addButton(item);
	}
	
	class AdjustWidthThread extends SwingEvent {
		boolean up;

		AdjustWidthThread(boolean b) {
			super(true); // 同步处理
			up = b;
		}

		public void process() {
			if (up) {
				doUpWidth();
			} else {
				doDownWidth();
			}
		}
	}
	
	/** 增加/缩小的尺寸 **/
	private static final int ADJUST_GAP = 4;

	/** DOCK最小宽度 **/
	private int dockMinWidth = 230;

	/**
	 * 增加宽度
	 */
	private void doUpWidth() {
		Rectangle rect = getBounds();
		rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);

		// 增加宽度
		rect.width += ADJUST_GAP;
		Dimension d = PlatformKit.getPlatformDesktop().getSize();
		if (rect.width >= d.width) {
			rect.x = 0;
			rect.width = d.width;
		} else {
			// 超过显示范围时...
			if (rect.x + rect.width >= d.width) {
				rect.x -= ADJUST_GAP; // X减少
				if (rect.x < 0) rect.x = 0;
			}
		}

		setBounds(rect);
		// 加宽
		dockBar.doUpWidth();
		
//		addThread(new UpWidth());
	}
	
//	class UpWidth extends SwingEvent {
//		UpWidth() {
//			super(true);
//		}
//
//		@Override
//		public void process() {
//			dockBar.doUpWidth();
//		}
//	}
	
	/**
	 * 缩小宽度
	 */
	private void doDownWidth() {
		Rectangle r = getBounds();
		r = new Rectangle(r.x, r.y, r.width, r.height);
		if (r.width <= dockMinWidth) {
			return;
		}

		// 缩小
		r.width -= ADJUST_GAP;
		if (r.width < dockMinWidth) {
			r.width = dockMinWidth;
		}

		setBounds(r);
		dockBar.doDownWidth();
		
//		// 放入线程处理
//		addThread(new DownWidth());
	}
	
//	class DownWidth extends SwingEvent {
//		DownWidth() {
//			super(true);
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.util.event.SwingEvent#process()
//		 */
//		@Override
//		public void process() {
//			dockBar.doDownWidth();
//		}
//	}
	
	/**
	 * 调整宽度
	 * @param up
	 */
	public void modifyWidth(boolean up) {
		addThread(new AdjustWidthThread(up));
	}
	
	/**
	 * 放到顶部
	 */
	public void doMoveToTop() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		int x = (d.width - r.width) / 2;
		r = new Rectangle(x, 0, r.width, RayDock.DOCK_HEIGHT);
		setBounds(r);
	}

	/**
	 * 放到顶部和平铺
	 */
	public void doMoveToTopFull() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		r = new Rectangle(0, 0, d.width, RayDock.DOCK_HEIGHT);
		setBounds(r);
		// 调整宽度
		modifyWidth(true);
	}
	
	/**
	 * 不转变Y轴，居中部署
	 */
	public void doMoveMiddle() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		int x = (d.width - r.width) / 2;
		// 重置
		r = new Rectangle(x, r.y, r.width, RayDock.DOCK_HEIGHT);
		setBounds(r);
	}

	/**
	 * 置于下方
	 */
	public void doMoveToBottom() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		int x = (d.width - r.width) / 2;
		int y = d.height - r.height;
		// 重置
		r = new Rectangle(x, y, r.width, RayDock.DOCK_HEIGHT);
		setBounds(r);
	}

	public void doMoveToBottomFull() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		int y = d.height - r.height;
		// 重置
		r = new Rectangle(0, y, d.width, RayDock.DOCK_HEIGHT);
		setBounds(r);
		// 重置联署
		modifyWidth(true);
	}
	
	/**
	 * 判断DOCK位于底部
	 * @return 返回真或者假
	 */
	public boolean isBottom() {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		Dimension d = desktop.getSize();
		Rectangle r = getBounds();
		// 最低坐标
		int bottomY = d.height - r.height;
		return r.y >= bottomY;
	}
	
//	private void paintNimbusComponent(Graphics g) {
//		// 宽度和高度
//		int width = getWidth();
//		int height = getHeight();
//
//		ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallIcon");
//
//		Image image = icon.getImage();
//		int imgWidth = image.getWidth(null);
//		int imgHeight = image.getHeight(null);
//
//		// 拉伸铺满
////		Color old = g.getColor();
//
////		g.drawImage(image, 0, 0, width - 1, height - 1, 0, 0, imgWidth - 1,
////				imgHeight - 1, null);
//		
//		for (int y = 0; y < height; y += imgHeight) {
//			for (int x = 0; x < width; x += imgWidth) {
//				g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
//			}
//		}
//		
////		g.setColor(old);
//	}
//	
//	protected void paintComponent(Graphics g) {
//		if(this.isNimbusUI()) {
//			paintNimbusComponent(g);
//		} else {
//			super.paintComponent(g);
//		}
//	}
	
	public void writeUserButtons(){
		dockBar.writeUserButtons();
	}
	
	/**
	 * 加载默认的EDGE按纽
	 */
	public void loadDefaultEdgeButtons() {
		edgeBar.loadDefaultButtons();
	}

	/**
	 * 加载默认的DOCK按纽
	 */
	public void loadDefaultDockButtons() {
		dockBar.loadDefaultButtons();
	}

	
//	/**
//	 * 显示默认的按纽
//	 * 先加载边缘侧，再加载应用按纽
//	 */
//	public void showDefaultButtons() {
//		edgeBar.showDefaultButtons();
//		dockBar.showDefaultButtons();
//	}
	
	class EdgaVisible extends SwingEvent {
		boolean visible;

		EdgaVisible(boolean b) {
			super(true);
			visible = b;
		}

		public void process() {
			edgeBar.setVisible(visible);
			// if (visible) {
			// // edgeBar.revalidate();
			// // edgeBar.repaint();
			//
			// SwingUtilities.updateComponentTreeUI(edgeBar);
			// }
		}
	}
	
	class ModifyWidth extends SwingEvent {

		boolean up;

		ModifyWidth(boolean b) {
			super(true);
			up = b;
		}

		public void process() {
			if (up) {
				dockBar.doUpWidth();
			} else {
				dockBar.doDownWidth();
			}
		}
	}
	
	class ModifyEdge extends SwingEvent {
		ModifyEdge() {
			super(true);
		}

		public void process() {
			SwingUtilities.updateComponentTreeUI(edgeBar);
		}
	}
	
	public boolean isShowEdgeBar() {
		return edgeBar.isVisible();
	}
	
	public void doExchangeEdge() {
		if (edgeBar != null) {
			boolean show = edgeBar.isVisible();
			// 显示或者隐藏
			addThread(new EdgaVisible(!show));
			// 调整尺寸
			addThread(new ModifyWidth(!show));
			// 更新
			if (!show) {
				addThread(new ModifyEdge());
			}

			//			if (b) {
			//				// modifyWidth(false);
			//				addThread(new ModifyWidth(false));
			//			} else {
			//				// modifyWidth(true);
			//				addThread(new ModifyWidth(true));
			//			}
		}
	}
}