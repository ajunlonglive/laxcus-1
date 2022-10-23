/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.frame;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.gui.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * LAXCUS图形桌面的轻量级窗口
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public abstract class LightFrame extends LightForm {

	private static final long serialVersionUID = 4078470150644907693L;

	class LightFrameAdapter extends InternalFrameAdapter {
		
		public void internalFrameIconified(InternalFrameEvent e) {
			// 窗口被图标化
		}

		public void internalFrameDeiconified(InternalFrameEvent e) {
			// 窗口被取消图标化
		}

		public void internalFrameActivated(InternalFrameEvent e) {
			LightFrame frame = (LightFrame) e.getSource();
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			// 去通知任务栏，通知激活
			if (desktop != null && frame != null) {
				desktop.activate(frame);
			}
		}

		public void internalFrameDeactivated(InternalFrameEvent e) {
			LightFrame frame = (LightFrame) e.getSource();
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			if (desktop != null && frame != null) {
				desktop.deactivate(frame);
			}
		}
	}

	/** 已经加载或者否 **/
	private volatile boolean loaded;

	/**
	 * 构造默认的轻量级窗口
	 */
	public LightFrame() {
		super();

		// 记录事件 
		addInternalFrameListener(new LightFrameAdapter());
		// 设置默认的边框
		setDefaultBorder();

		// 没有加载
		loaded = false;
	}

	/**
	 * 构造轻量级窗口，指定标题
	 * @param title 标题
	 */
	public LightFrame(String title) {
		this();
		super.setTitle(title);
	}

	/**
	 * 构造轻量级窗口，指定参数
	 * @param title
	 * @param resizable
	 */
	public LightFrame(String title, boolean resizable) {
		this(title);
		setResizable( resizable);
	}

	/**
	 * 构造轻量级窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public LightFrame(String title, boolean resizable, boolean closable) {
		this(title, resizable);
		setClosable(closable);
	}

	/**
	 * 构造轻量级窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public LightFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
		this(title, resizable, closable);
		setMaximizable(maximizable);
	}

	/**
	 * 构造轻量级窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public LightFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		this(title, resizable, closable, maximizable);
		setIconifiable(iconifiable);
	}


	/**
	 * 判断是隐藏状态
	 * @return 返回真或者假
	 */
	public boolean isHidden() {
		return !isVisible();
	}

	/**
	 * 设置默认的边框，分为Nimbus/Metal界面
	 */
	protected void setDefaultBorder() {
		if (isShowBorder()) {
			// METAL界面，浮凸出的效果
			if (isMetalUI()) {
				Color c = super.getBackground();
				ESL esl = new RGB(c).toESL();
//				Color b = (Skins.isGraySkin() ? esl.toDraker(30).toColor() : esl.toBrighter(50).toColor());
				Color b = (Skins.isGraySkin() ? esl.toDraker(60).toColor() : esl.toBrighter(50).toColor());
				BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
				setBorder(line);
			}
		} else {
			setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#isIcon()
	 */
	@Override
	public boolean isIcon() {
		// 没有加载时，由上层判断
		if (!loaded) {
			return false;
		}
		return (!(super.isVisible() && super.isShowing()));
	}
	
	/**
	 * 隐藏关联的对话框
	 */
	private void hideLightDialog() {
		Container c = getParent();
		boolean success = (c != null && Laxkit.isClassFrom(c, JDesktopPane.class));
		if (!success) {
			return;
		}
		
		JDesktopPane pane = (JDesktopPane) c;
		JInternalFrame[] elements = pane.getAllFrames();
		int size = (elements == null ? 0 : elements.length);
		// 逐个检查
		for (int i = 0; i < size; i++) {
			JInternalFrame element = elements[i];			
			// 不是对话窗口实例，忽略它
			if (!Laxkit.isClassFrom(element, LightDialog.class)) {
				continue;
			}

			// 对话窗口实例
			LightDialog dialog = (LightDialog) element;
			LightFrame frame = dialog.getAttachFrame();
			if (frame == null) {
				continue;
			}
			// 绑定这个父类句柄，并且是可视状态，隐藏它
			if (frame == this) {
				if (dialog.isVisible()) {
					dialog.setVisible(false);
				}
			}
		}
	}
	
	/**
	 * 显示关联的对话框
	 */
	private void showLightDialog() {
		Container c = getParent();
		boolean success = (c != null && Laxkit.isClassFrom(c, JDesktopPane.class));
		if (!success) {
			return;
		}

		JDesktopPane pane = (JDesktopPane) c;
		// 逐个检查
		JInternalFrame[] elements = pane.getAllFrames();
		int size = (elements == null ? 0 : elements.length);
		for (int i = 0; i < size; i++) {
			JInternalFrame element = elements[i];
			// 不是对话窗口实例，忽略它
			if (!Laxkit.isClassFrom(element, LightDialog.class)) {
				continue;
			}

			LightDialog dialog = (LightDialog) element;
			LightFrame frame = dialog.getAttachFrame();
			if (frame == null) {
				continue;
			}
			// 绑定这个父类句柄，并且是隐藏状态，显示它
			if (frame == this) {
				if (!dialog.isVisible()) {
					dialog.setVisible(true);
				}
			}
		}
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
				// 隐藏它的关联对话窗口
				hideLightDialog();
				// 隐藏窗口
				super.setVisible(false);
			}
		} else {
			if (!isVisible()) {
				// 显示窗口
				super.setVisible(true);
				// 显示它的关联对话窗口
				showLightDialog();
			}
		}
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
//		// 注册到桌面
//		desktop.register(this);
//
//		// 按照这个顺序做，可以正常显示
//		// 1. 调用超类显示
//		super.setVisible(true);
//		// 2. 增加到桌面面板上
//		desktop.add(this, new Integer(FormLayer.FRAME));
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
//		refreshUI();
//
//		// 保存句柄
//		desktop.addFrame(this);
//
//		// 已经加载
//		loaded = true;
//	}

	/**
	 * 显示窗口
	 */
	private void doShow() {
		// 重装标题栏面板
		reloadTitlePane();

		// 取得桌面实例
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		if (desktop == null) {
			throw new HeadlessException("cannot be find desktop pane!");
		}

		// 注册到桌面
		desktop.register(this);
		// 2. 增加到桌面面板上
		desktop.add(this, new Integer(FormLayer.FRAME));

		// 1. 验证此容器及其所有子组件
		desktop.validate();
		// 2. 设置为选择状态
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
		// 3. 调用超类显示
		super.setVisible(true);

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

		// 保存句柄
		desktop.addFrame(this);

		// 刷新UI
		refreshUI();

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
		Rectangle r = (isVisible() ? getBounds() : null);
		if (r != null) {
			r = new Rectangle(r.x, r.y, r.width, r.height);
		}

		// 取消注册
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		if (desktop != null) {
			desktop.unregister(this);
		}

		// 在关闭窗口前释放资源
		release0();
		
		// 销毁绑定的对话框窗口
		LightDialog[] as = desktop.findLightDialog(this);
		int size = (as != null ? as.length : 0);
		// 关闭关联窗口
		for (int i = 0; i < size; i++) {
			LightDialog dlg = as[i];
			dlg.closeWindow();
		}

		// 取消可视
		setVisible(false);
		// 销毁
		dispose();

		// 删除句柄
		desktop.removeFrame(this);

		// 在关闭窗口后释放资源
		release1();

		// 释放，不再加载
		loaded = false;

		// 重新绘制窗口
		if (r != null && desktop != null) {
			desktop.repaint(r.x, r.y, r.width, r.height);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.LightForm#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 边框
		setDefaultBorder();
	}

	/**
	 * 在关闭窗口之前释放与窗口关联的资源，比如关联的对话窗口
	 */
	protected abstract void release0();

	/**
	 * 在关闭窗口之后释放非窗口资源，比如线程之类的资源
	 */
	protected abstract void release1();

}

///** 默认隐藏 **/
//private volatile boolean hiddenTitle;

///** UI界面 **/
//private BasicInternalFrameUI oldUI;

///** UI界面 **/
//private JComponent titlePane;



///**
// * 设置NIMBUS界面
// */
//private void setNimbusBorder() {
//	if (isFixedNimbusBorder()) {
//		JInternalFrame dlg = new JInternalFrame();
//		Border border = dlg.getBorder();
//		if (border != null) {
//			Border m = getBorder();
//			if (m == null || m.getClass() != border.getClass()) {
//				setBorder(border);
//			}
//		}
//	} else {
//		// NIMBUS界面，TOP是0个像素，这时不会有横线，否则会出现。LEFT/BOTTOM/RIGHT保持5个像素，这时界面感觉最佳。
//		setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
//	}
//}

///**
// * 设置默认的边框，分为Nimbus/Metal界面
// */
//protected void setDefaultBorder() {
//	// METAL界面，浮凸出的效果
//	if (isMetalUI()) {
//		Color c = super.getBackground();
//		ESL esl = new RGB(c).toESL();
//		Color b = (Skins.isGraySkin() ? esl.toDraker(30).toColor() : esl.toBrighter(50).toColor());
//		BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
//		setBorder(line);
//	}
//	// NIMBUS界面
//	else if (isNimbusUI()) {
//		setNimbusBorder();
//	}
//}

//// 取出UI
//oldUI = (BasicInternalFrameUI) getUI();
//titlePane = oldUI.getNorthPane(); // ((BasicInternalFrameUI) getUI()).getNorthPane();

//hiddenTitle = false;


///** 
// * Returns whether the <code>JInternalFrame</code> is currently iconified.
// *
// * @return <code>true</code> if this internal frame is iconified
// */ 
//public boolean isIcon() {
//    return isIcon;
//}

///** 
// * Iconifies or de-iconifies this internal frame,
// * if the look and feel supports iconification.
// * If the internal frame's state changes to iconified,
// * this method fires an <code>INTERNAL_FRAME_ICONIFIED</code> event.
// * If the state changes to de-iconified,
// * an <code>INTERNAL_FRAME_DEICONIFIED</code> event is fired.
// *
// * @param b a boolean, where <code>true</code> means to iconify this internal frame and
// *          <code>false</code> means to de-iconify it
// * @exception PropertyVetoException when the attempt to set the 
// *            property is vetoed by the <code>JInternalFrame</code>
// *
// * @see InternalFrameEvent#INTERNAL_FRAME_ICONIFIED
// * @see InternalFrameEvent#INTERNAL_FRAME_DEICONIFIED
// *
// * @beaninfo
// *           bound: true
// *     constrained: true
// *     description: The image displayed when this internal frame is minimized.
// */
//public void setIconX(boolean b) throws PropertyVetoException {
//    if (isIcon == b) {
//        return;
//    }
//
///* If an internal frame is being iconified before it has a
//   parent, (e.g., client wants it to start iconic), create the
//   parent if possible so that we can place the icon in its
//   proper place on the desktop. I am not sure the call to
//   validate() is necessary, since we are not going to display
//   this frame yet */
//    firePropertyChange("ancestor", null, getParent());
//
//    Boolean oldValue = isIcon ? Boolean.TRUE : Boolean.FALSE; 
//    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
//    fireVetoableChange(IS_ICON_PROPERTY, oldValue, newValue);
//    isIcon = b;
//    firePropertyChange(IS_ICON_PROPERTY, oldValue, newValue);
//if (b)
//  fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ICONIFIED);
//else
//  fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED);
//}


// 旧的

///*
// * (non-Javadoc)
// * @see javax.swing.JInternalFrame#setIcon(boolean)
// */
//@Override
//public void setIcon(boolean icon) {
//	// 图标化，隐藏窗口；否则否
//	if (icon) {
//		if (isVisible()) {
//			// normal bounds to maximized state.
//			if (!isMaximum()) {
//				setNormalBounds(getBounds());
//			}
//			
//			super.setVisible(false);
//		}
//	} else {
//		if (!isVisible()) {
//			super.setVisible(true);
//		}
//	}
//}


//desktop.setDesktopManager(arg0);

//DesktopManager manager = desktop.getDesktopManager();
//System.out.printf("DesktopManager class is %s\n", manager.getClass().getName());

//desktop.add(this, JLayeredPane.DEFAULT_LAYER);


//Container parent = getParent();
//System.out.printf("%s 父类窗口 %s\n", getClass().getName(), (parent == null ? "没有" : parent
//		.getClass().getName()));


//public boolean isHiddenTitle() {
//return hiddenTitle;
//}
//
//public void hideTitlePane() {
//((BasicInternalFrameUI)getUI()).setNorthPane(null);
//putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
//hiddenTitle = true;
//
//validate();
//repaint();
//}
//
//public void showTitlePane() {
//setUI(oldUI);
//setDefaultTitleHeight();
//
////oldUI.setNorthPane(titlePane);
//
//putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);
//hiddenTitle = false;
//
//// 生效，重新绘制
//validate();
//repaint();
//}


//// 隐藏
//if (hiddenTitle) {
//	hideTitlePane();
//}