/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 轻量桌面窗体，继承自JInternalFrame实例，它是LAXCUS分布式操作系统桌面环境的基础类。<br>
 * 桌面上的对话框和窗口都从它派生。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public abstract class LightForm extends JInternalFrame {

	private static final long serialVersionUID = -7374238682918969700L;

	/** 大图标，32*32像素 **/
	private Icon bigIcon;

	/** 默认隐藏 **/
	private volatile boolean hiddenTitle;

	/** UI界面 **/
	private BasicInternalFrameUI oldUI;

	/** 显示边框，默认是真 **/
	private volatile boolean showBorder;

	/** 自定义最小化图标 **/
	private Icon minimizeIcon;

	/** 自定义最大化图标 **/
	private Icon maximizeIcon;

	/** 自定义恢复图标 **/
	private Icon restoreIcon;

	/** 自定义关闭图标 **/
	private Icon closeIcon;

	/** 允许刷新UI **/
	private boolean refreshUI;


	/**
	 * 构造默认的桌面窗体
	 */
	public LightForm() {
		super();
		// 显示边框，默认是“真”
		showBorder = true;
		// 默认不刷新
		refreshUI = false;

		// 默认的高度
		setDefaultTitleHeight();

		// 取出UI
		oldUI = (BasicInternalFrameUI) getUI();
		// 隐藏标题栏
		hiddenTitle = false;
	}

	/**
	 * 构造桌面窗体，指定标题
	 * @param title
	 */
	public LightForm(String title) {
		this();
		setTitle(title);
	}

	/**
	 * 构造桌面窗体
	 * @param title
	 * @param resizable
	 */
	public LightForm(String title, boolean resizable) {
		this(title);
		setResizable(resizable);
	}

	/**
	 * 构造桌面窗体
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public LightForm(String title, boolean resizable, boolean closable) {
		this(title, resizable);
		setClosable(closable);
	}

	/**
	 * 构造桌面窗体
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public LightForm(String title, boolean resizable, boolean closable, boolean maximizable) {
		this(title, resizable, closable);
		setMaximizable(maximizable);
	}

	/**
	 * 构造桌面窗体
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public LightForm(String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		this(title, resizable, closable, maximizable);
		setIconifiable(iconifiable);
	}

	/**
	 * 设置自定义最小化图标 <br>
	 * 注意！设置图标后使用“updateUI”方法更新图标才能生效。
	 * @param e
	 */
	public void setMinimizeIcon(Icon e) {
		minimizeIcon = e;
	}

	/**
	 * 设置自定义最大化图标
	 * 注意！设置图标后使用“updateUI”方法更新图标才能生效。
	 * @param e
	 */
	public void setMaximizeIcon(Icon e) {
		maximizeIcon = e;
	}

	/**
	 * 设置自定义恢复图标
	 * 注意！设置图标后使用“updateUI”方法更新图标才能生效。
	 * @param e
	 */
	public void setRestoreIcon(Icon e) {
		restoreIcon = e;
	}

	/**
	 * 设置自定义关闭图标
	 * 注意！设置图标后使用“updateUI”方法更新图标才能生效。
	 * @param e
	 */
	public void setCloseIcon(Icon e) {
		closeIcon = e;
	}

	/**
	 * 返回自定义最小化图标
	 * @return
	 */
	public Icon getMinimizeIcon() {
		return minimizeIcon;
	}

	/**
	 * 返回自定义最大化图标
	 * @return
	 */
	public Icon getMaximizeIcon() {
		return maximizeIcon;
	}

	/**
	 * 返回自定义恢复图标
	 * @return
	 */
	public Icon getRestoreIcon() {
		return restoreIcon;
	}

	/**
	 * 返回自定义关闭图标
	 * @return
	 */
	public Icon getCloseIcon() {
		return closeIcon;
	}
	
	/**
	 * 判断有重新定义的图标
	 * @return 返回真或者假
	 */
	public boolean hasRedefineIcon() {
		return minimizeIcon != null || maximizeIcon != null
				|| restoreIcon != null || closeIcon != null;
	}

	/**
	 * 设置大图标
	 * @param icon
	 */
	public void setFrameBigIcon(Icon icon) {
		bigIcon = icon;
	}

	/**
	 * 返回小图标
	 * @return
	 */
	public Icon getFrameBigIcon() {
		return bigIcon;
	}

	/**
	 * 线程延时等待。单位：毫秒。
	 * @param timeout 超时时间
	 */
	public synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 查找在UIManager中的图标
	 * @param key 键值
	 * @return 返回图标实例，否则空指针
	 */
	protected ImageIcon getImageIcon(String key) {
		Object object = UIManager.get(key);
		if (object != null && Laxkit.isClassFrom(object, ImageIcon.class)) {
			return (ImageIcon) object;
		}
		return null;
	}

	//	/**
	//	 * 查找保存在JAR包中的图像
	//	 * @param jarPath JAR包中的文件路径
	//	 * @param width 输出图像宽度
	//	 * @param height 输出图像高度
	//	 * @return 返回对象实例，失败是空指针
	//	 */
	//	protected ImageIcon findImage(String jarPath, int width, int height) {
	//		ResourceLoader loader = new ResourceLoader();
	//		return loader.findImage(jarPath, width, height);
	//	}
	//
	//	/**
	//	 * 查找保存在JAR包中的图像，默认是16*16的尺寸
	//	 * @param jarPath JAR包中的文件路径
	//	 * @return 返回图像实例
	//	 */
	//	protected ImageIcon findImage(String jarPath) {
	//		return findImage(jarPath, 16, 16);
	//	}

	/*
	 * 设置标题，先设置字体再设置标题
	 */
	@Override
	public void setTitle(String text) {
		if (text == null) {
			text = "";
		}
		// 先设置字体
		Font font = FontKit.findFont(this, text);
		if (font != null) {
			setFont(font);
		}
		// 设置标题
		super.setTitle(text);
	}

	/**
	 * 判断是METAL界面
	 * @return
	 */
	protected boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}

	/**
	 * 判断是NIMBUS界面
	 * @return
	 */
	protected boolean isNimbusUI() {
		return GUIKit.isNimbusUI();
	}

	/**
	 * 设置METAL界面的标题栏高度
	 * @param height 高度
	 */
	protected void setMetalTitleHeight(int height) {
		if (height < 1) {
			throw new IllegalArgumentException("must be > 0");
		}

		InternalFrameUI ui = getUI();
		boolean success = (ui != null && Laxkit.isClassFrom(ui, MetalInternalFrameUI.class));
		if (success) {
			MetalInternalFrameUI mui = (MetalInternalFrameUI) ui;
			JComponent north = mui.getNorthPane();
			if (north != null) {
				Dimension d = new Dimension(20, height);
				north.setPreferredSize(d);
			}
		}
	}

	/**
	 * 设置以28像素的标题栏高
	 */
	protected void setDefaultTitleHeight() {
		if (isMetalUI()) {
			boolean maxTitle = GUIKit.isHighScreen();
			String input = UIManager.getString(maxTitle ? "MaxMetalTitleHeight" : "MetalTitleHeight");
			int value = ConfigParser.splitInteger(input, (maxTitle ? 32 : 28));
			setMetalTitleHeight(value);
		}
	}

	//	@Override
	//	public Insets getInsets() {
	//		if (showBorder) {
	//			return super.getInsets();
	//		} else {
	//			return new Insets(0, 0, 0, 0);
	//		}
	//	}

	/**
	 * 设置显示边框
	 * @param b 是或者否
	 */
	public void setShowBorder(boolean b) {
		if (showBorder != b) {
			showBorder = b;
			// 注意：下面这个方法调用updateUI，子类updateUI方法调用setDefaultBorder，更新边框
			reloadUI();
		}
	}

	/**
	 * 确认显示边框
	 * @return 是或者否
	 */
	public boolean isShowBorder() {
		return showBorder;
	}

	/**
	 * 隐藏标题栏
	 */
	public boolean isHiddenTitle() {
		return hiddenTitle;
	}

	/**
	 * 隐藏标题栏
	 */
	public void hideTitlePane() {
		((BasicInternalFrameUI) getUI()).setNorthPane(null);
		putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
		hiddenTitle = true;

		validate();
		repaint();
	}

	/**
	 * 显示标题栏
	 */
	public void showTitlePane() {
		setUI(oldUI);
		setDefaultTitleHeight();

		putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);
		hiddenTitle = false;

		// 生效，重新绘制
		validate();
		repaint();
	}

	/**
	 * 判断刷新
	 * @return
	 */
	public boolean isRefreshUI() {
		return refreshUI;
	}

	/**
	 * 设置刷新处理。当启动或者重新启动时
	 * @param b
	 */
	public void setRefreshUI(boolean b) {
		refreshUI = b;
	}

//    private static void updateComponentTreeUI0(Component c) {
//        if (c instanceof JComponent) {
//            JComponent jc = (JComponent) c;
//            jc.updateUI();
//            JPopupMenu jpm =jc.getComponentPopupMenu();
//            if(jpm != null && jpm.isVisible() && jpm.getInvoker() == jc) {
//                updateComponentTreeUI(jpm);
//            }
//        }
//        Component[] children = null;
//        if (c instanceof JMenu) {
//            children = ((JMenu)c).getMenuComponents();
//        }
//        else if (c instanceof Container) {
//            children = ((Container)c).getComponents();
//        }
//        if (children != null) {
//            for(int i = 0; i < children.length; i++) {
//                updateComponentTreeUI0(children[i]);
//            }
//        }
//    }

//	public static void updateComponentTreeUI(Component c) {
//		updateComponentTreeUI0(c);
//		c.invalidate();
//		c.validate();
//		c.repaint();
//	}

	/**
	 * 更新UI界面
	 */
	protected void __reloadUI() {
		SwingUtilities.updateComponentTreeUI(this);
	}

	class ReloadUI extends SwingEvent {
		
		public ReloadUI(){
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.event.SwingEvent#process()
		 */
		@Override
		public void process() {
			__reloadUI();
		}
	}

	/**
	 * 更新UI界面
	 */
	private void reloadUI() {
		addThread(new ReloadUI());
	}

	/**
	 * 刷新UI
	 * 调用SwingUtilities.updateComponentTreeUI来实现
	 */
	protected void refreshUI() {
		// 更新UI
		if (refreshUI) {
			reloadUI();
		}
	}
	
	/**
	 * 重新加载标题栏面板
	 */
	protected void reloadTitlePane() {
		// 更新UI
		if (hasRedefineIcon()) {
			updateUI();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 隐藏
		if (hiddenTitle) {
			hideTitlePane();
		} else {
			// 设置高度
			setDefaultTitleHeight();
		}
	}

	/**
	 * 关闭窗口。<br>
	 * 由子类实现，除了用户关闭外，当系统结束时，调用这个方法关闭窗口，柔性释放资源。<br>
	 */
	public abstract void closeWindow();
}


///** 使用系统固定的Nimbus边框 **/
//private boolean fixedNimbusBorder;


//	/**
//	 * 设置使用系统固定的Nimbus边框
//	 * @param b 真或者假
//	 */
//	public void setFixedNimbusBorder(boolean b) {
//		fixedNimbusBorder = b;
//	}
//
//	/**
//	 * 判断是使用系统固定的Nimbus边框
//	 * @return 真或者假
//	 */
//	public boolean isFixedNimbusBorder() {
//		return fixedNimbusBorder;
//	}


//public ComponentUI UIDefaults.getUI(JComponent target) {
//
//		Object cl = get("ClassLoader");
//		ClassLoader uiClassLoader = 
//			(cl != null) ? (ClassLoader)cl : target.getClass().getClassLoader();
//			Class uiClass = getUIClass(target.getUIClassID(), uiClassLoader);
//			Object uiObject = null;
//
//			if (uiClass == null) {
//				getUIError("no ComponentUI class for: " + target);
//			}
//			else {
//				try {
//					Method m = (Method)get(uiClass);
//					if (m == null) {
//						Class acClass = javax.swing.JComponent.class;
//						m = uiClass.getMethod("createUI", new Class[]{acClass}); //  从类中找到对应的方法
//						put(uiClass, m);
//					}
//					uiObject = MethodUtil.invoke(m, null, new Object[]{target}); // 调用方法，生成实例
//				}
//				catch (NoSuchMethodException e) {
//					getUIError("static createUI() method not found in " + uiClass);
//				}
//				catch (Exception e) {
//					getUIError("createUI() failed for " + target + " " + e);
//				}
//			}
//
//			return (ComponentUI)uiObject;
//	}


//protected void setNimbusTitleHeight(int height) {
//	if (height < 1) {
//		throw new IllegalArgumentException("must be > 0");
//	}
//	
//	InternalFrameUI ui = getUI();
//	System.out.printf("ui is %s\n", ui.getClass().getName());
//	
////	javax.swing.plaf.metal.SynthInternalFrameUI
//	
//	if (ui.getClass() == javax.swing.plaf.synth.SynthInternalFrameUI.class) {
//		SynthInternalFrameUI bui = (SynthInternalFrameUI) ui;
//		JComponent north = bui.getNorthPane();
//		if (north != null) {
//			Dimension d = new Dimension(20, height);
//			north.setMinimumSize(d);
//			north.setPreferredSize(d);
//			
//			System.out.printf("set mini height %d\n", height);
//		}
//	}
//}

///**
// * 将一批线程加入分派器
// * @param threads 线程数组
// */
//protected void addThreads(Collection<SwingEvent> threads) {
//	SwingDispatcher.invokeThreads(threads);
//}


//private void throwable() {
//	throw new NullPointerException("ducks all!");
//}

//public void setUI(InternalFrameUI ui){
//	SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//	String d = style.format(new java.util.Date(System.currentTimeMillis()));
//	System.out.printf("%s setUI %s [%s]\n", d, ui.getClass().getName(), getTitle());
//	
//	try {
//		throwable();
//		System.out.println();
//	} catch (Throwable e) {
//		e.printStackTrace();
//	}
//	
//	super.setUI(ui);
//}

//private void asserts() {
//	try {
//		InternalFrameUI ui = (InternalFrameUI)UIManager.getUI(this);
//		throwable();
//	} catch (Throwable e) {
//		e.printStackTrace();
//		System.out.println();
//	}
//}

//maxIcon =
//    style.getIcon(context,"InternalFrameTitlePane.maximizeIcon");
//minIcon =
//    style.getIcon(context,"InternalFrameTitlePane.minimizeIcon");
//iconIcon =
//    style.getIcon(context,"InternalFrameTitlePane.iconifyIcon");
//closeIcon =
//    style.getIcon(context,"InternalFrameTitlePane.closeIcon");

//private void reloadNimbusTitleIcons() {
//	Icon icon = UIManager.getIcon("NimbusIconifyIcon");
//	UIManager.getDefaults().put("InternalFrame.iconifyIcon", icon);
//	UIManager.getDefaults().put("InternalFrameTitlePane.iconifyIcon", icon);
//
//	icon = UIManager.getIcon("NimbusCloseIcon");
//	UIManager.getDefaults().put("InternalFrame.closeIcon", icon);
//	UIManager.getDefaults().put("InternalFrameTitlePane.closeIcon", icon);
//
//	icon = UIManager.getIcon("NimbusMinimizeIcon");
//	UIManager.getDefaults().put("InternalFrame.minimizeIcon", icon);
//	UIManager.getDefaults().put("InternalFrameTitlePane.minimizeIcon", icon);
//
//	icon = UIManager.getIcon("NimbusMaximizeIcon");
//	UIManager.getDefaults().put("InternalFrame.maximizeIcon", icon);
//	UIManager.getDefaults().put("InternalFrameTitlePane.maximizeIcon", icon);
//	
//	System.out.printf("%s 更新UI\n", getTitle());
//}
//
//private void printIcon() {
//	Icon icon = UIManager.getIcon("InternalFrameTitlePane.closeIcon");
//	if(icon != null){
//		System.out.printf("icon %s, w:%d h:%d\n", icon.getClass().getName(), icon.getIconWidth(), icon.getIconHeight());
//	} else{
//		System.out.println("null icon !");
//	}
//}

///*
// * 
// */
//@Override
//public void setUI(InternalFrameUI ui){
////	SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
////	String d = style.format(new java.util.Date(System.currentTimeMillis()));
////	System.out.printf("%s setUI %s %s [%s]\n", d, LightKit.getUIID(), ui.getClass().getName(), getTitle());
//	
////	// 在这里更新UI界面，用新的
////	if(ui != null  && ui.getClass() == javax.swing.plaf.metal.MetalInternalFrameUI.class) {
////		javax.swing.plaf.metal.MetalInternalFrameUI metal = new javax.swing.plaf.metal.MetalInternalFrameUI(this);
////		super.setUI(metal);
////		
//////		asserts();
////	}  else {
////		super.setUI(ui);
////	}
//	
//	if (isMetalUI()) {
//		FlatInternalFrameUI light = new FlatInternalFrameUI(this);
//		super.setUI(light);
//	} else {
////		reloadNimbusTitleIcons();
////		printIcon();
//		
//		super.setUI(ui);
//	}
//}

