/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.*;
import java.security.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.gui.*;
import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * LAXCUS桌面的对话窗口，绑定一个LightFrame
 * 
 * 在对话框里，屏蔽“最小化”和“最大化”按纽，只保留关闭按纽。
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public abstract class LightDialog extends LightForm {

	private static final long serialVersionUID = -5095601168496115089L;

	/**
	 * 模态等待器
	 *
	 * @author scott.liang
	 * @version 1.0 9/8/2021
	 * @since laxcus 1.0
	 */
	class ModalShine {
		/** 退出 **/
		private volatile boolean exit;

		public ModalShine() {
			super();
			exit = false;
		}

		public boolean isExit() {
			return exit;
		}

		/**
		 * 由外部调用唤醒
		 */
		public void done() {
			exit = true;
			stopModal();
		}
	}

	/** 参数名称 **/
	final static String EXCHANGE_VALUE_PROPERTY = "EXCHANGE_LIGHTDIALOG_VALUE";

	/** 选择值 **/
	private transient Object selectedValue;

	/** 模态 **/
	private boolean modal;

	/** 模态等待器实例 **/
	private ModalShine shine = new ModalShine();

	/** 绑定的窗口 **/
	private LightFrame attachFrame;



	/**
	 * 构造轻量级对话框，指定标题
	 * @param title 标题
	 */
	public LightDialog(String title) {
		this();
		setTitle(title);
	}

	/**
	 * 构造默认的轻量级对话框
	 */
	public LightDialog() {
		super();
		//  默认最小化和最大化按纽失效
		super.setIconifiable(false);
		super.setMaximizable(false);
		// 模态为假
		setModal(false);
		//  设置边框
		setDefaultBorder();
	}

	/**
	 * 构造轻量级对话框，指定参数
	 * @param title
	 * @param resizable
	 */
	public LightDialog(String title, boolean resizable) {
		this(title);
		setResizable(resizable);
	}

	/**
	 * 构造轻量级对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public LightDialog(String title, boolean resizable, boolean closable) {
		this(title, resizable);
		setClosable(closable);
	}

	/**
	 * 构造轻量级对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public LightDialog(String title, boolean resizable, boolean closable, boolean maximizable) {
		this(title, resizable, closable);
		setMaximizable(maximizable);
	}

	/**
	 * 构造轻量级对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public LightDialog(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		this(title, resizable, closable, maximizable);
		setIconifiable(iconifiable);
	}

	/**
	 * 判断窗口是模态
	 * @return
	 */
	public boolean isModal() {
		return modal;
	}

	/**
	 * 设置为模态状态
	 * @param b 模态或者否
	 */
	private void setModal(boolean b) {
		modal = b;
	}

	/**
	 * 返回绑定的桌面窗口
	 * @return LightFrame实例，没有是空指针
	 */
	public LightFrame getAttachFrame() {
		return attachFrame;
	}

	/**
	 * 设置绑定的桌面窗口
	 * @param e 实例或者空指针
	 */
	private void setAttachFrame(LightFrame e) {
		//		System.out.printf("LightDialog.setAttachFrame, bind %s\n", e.getTitle());
		attachFrame = e;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#isIconifiable()
	 */
	@Override
	public boolean isIconifiable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#setIconifiable(boolean)
	 */
	@Override
	public void setIconifiable(boolean b) {

	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#isMaximizable()
	 */
	@Override
	public boolean isMaximizable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#setMaximizable(boolean)
	 */
	@Override
	public void setMaximizable(boolean b) {

	}

	/*
	 * 如果是模态，赋值后，让上级来释放。非模态，关闭窗口，释放资源。
	 * 
	 * @see com.laxcus.ui.LightForm#closeWindow()
	 */
	@Override
	public void closeWindow() {
		if (isModal()) {
			setSelectedValue(null);
		} else {
			// 关闭窗口
			closeDialog();
		}
	}

	/**
	 * 设置选择值
	 * @param newValue
	 */
	public final void setSelectedValue(Object newValue) {
		Object oldValue = selectedValue;

		selectedValue = newValue;
		// 当处于模态时，通知更改了属性
		if (isModal()) {
			firePropertyChange(EXCHANGE_VALUE_PROPERTY, oldValue, selectedValue);
		}
	}

	/**
	 * 返回选择值
	 * @return
	 */
	public Object getSelectedValue() {
		return selectedValue;
	}

	/**
	 * 当点击对话框标题栏的“关闭”按纽时
	 * 只针针模态窗口
	 *
	 * @author scott.liang
	 * @version 1.0 6/14/2021
	 * @since laxcus 1.0
	 */
	class ModalDialogAdapter extends InternalFrameAdapter {

		/** 对话窗口 **/
		private LightDialog dialog;

		public ModalDialogAdapter(LightDialog d) {
			super();
			dialog = d;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.event.InternalFrameAdapter#internalFrameClosing(javax.swing.event.InternalFrameEvent)
		 */
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			// 判断关闭结果，处理以下两种，“DO_NOTHING_ON_CLOSE”被忽略
			int who = getDefaultCloseOperation();
			if (who == WindowConstants.DISPOSE_ON_CLOSE) {
				dialog.setSelectedValue(null);
			} else if (who == WindowConstants.HIDE_ON_CLOSE) {
				dialog.setVisible(false);
			}
		}
	}

	/**
	 * 销毁窗口
	 */
	private void closeDialog() {
		// 父类实例
		Container c = getParent();
		// 取出窗口范围
		Rectangle r = (isVisible() ? getBounds() : null);
		if (r != null) {
			r = new Rectangle(r.x, r.y, r.width, r.height);
		}

		// 结束可视和销毁
		setVisible(false);
		dispose(); // 销毁

		// 重新绘制窗口
		if (c != null && r != null) {
			c.repaint(r.x, r.y, r.width, r.height);
		}
	}

	class LightPropertyChangeAdapter implements PropertyChangeListener {

		/** 对话框 **/
		private LightDialog dialog;

		public LightPropertyChangeAdapter(LightDialog d) {
			super();
			dialog = d;
		}

		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			boolean success = (dialog.isVisible() && event.getPropertyName().equals(LightDialog.EXCHANGE_VALUE_PROPERTY)) ;
			if (!success) {
				return;
			}
			// Use reflection to get Container.stopLWModal().
			try {
				Object obj;
				obj = AccessController.doPrivileged(new LightPrivilegedAction(
						Container.class, "stopLWModal"));
				if (obj != null) {
					((Method)obj).invoke(dialog, (Object[])null);
				}
				// 次级唤醒
				shine.done();
			} catch (IllegalAccessException ex) {
				Logger.error(ex);
			} catch (IllegalArgumentException ex) {
				Logger.error(ex);
			} catch (InvocationTargetException ex) {
				Logger.error(ex);
			}

			// 销毁窗口
			closeDialog();
		}
	}

	class LightPrivilegedAction implements PrivilegedAction<Object> {

		/** 类定义 **/
		private Class<?> clazz;

		/** 方法 **/
		private String methodName;

		public LightPrivilegedAction(Class<?> c, String method) {
			clazz = c;
			methodName = method;
		}

		/*
		 * (non-Javadoc)
		 * @see java.security.PrivilegedAction#run()
		 */
		@Override
		public Object run() {
			Method method = null;
			try {
				method = clazz.getDeclaredMethod(methodName, (Class[])null);
			} catch (NoSuchMethodException ex) {
			}
			if (method != null) {
				method.setAccessible(true);
			}
			return method;
		}
	}

	/**
	 * 递归找到父窗口
	 * @param parent 父类实例
	 * @return 返回父窗口或者空指针
	 */
	protected JInternalFrame findInternalFrameForComponent(Component parent) {
		if (parent == null) {
			return null;
		}
		if (Laxkit.isClassFrom(parent, JInternalFrame.class)) {
			// if (parent instanceof JInternalFrame) {
			return (JInternalFrame) parent;
		}
		return findInternalFrameForComponent(parent.getParent());
	}

	/**
	 * 递归找到父窗口
	 * @param parent 父类实例
	 * @return  返回父窗口或者空指针
	 */
	protected LightFrame findLightFrameForComponent(Component parent) {
		if (parent == null) {
			return null;
		}
		if (Laxkit.isClassFrom(parent, LightFrame.class)) {
			return (LightFrame) parent;
		}
		return findLightFrameForComponent(parent.getParent());
	}

	/**
	 * 递归找到父对话窗口
	 * @param parent 父类实例
	 * @return  返回父对话窗口或者空指针
	 */
	protected LightDialog findLightDialogForComponent(Component parent) {
		if (parent == null) {
			return null;
		}
		if (Laxkit.isClassFrom(parent, LightDialog.class)) {
			return (LightDialog) parent;
		}
		return findLightDialogForComponent(parent.getParent());
	}

	//	/**
	//	 * 查找父级的LightFrame
	//	 * @param parent 当前实例
	//	 * @return 返回实例，或者空指针
	//	 */
	//	private LightFrame findParentFrameX(Component parent) {
	//		if (parent == null) {
	//			return null;
	//		}
	//
	//		if (parent instanceof LightFrame) {
	//			return ((LightFrame) parent);
	//		}
	//		return findParentFrame(parent.getParent());
	//	}

	/**
	 * 根据默认的范围参数，设置它
	 * @param defaultRect 默认的范围值
	 * @param parent 绑定的窗口
	 */
	protected void setDefaultBounds(Rectangle defaultRect, Component parent) {
		// 找到父窗口
		JInternalFrame frame = findInternalFrameForComponent(parent);
		if (frame == null) {
			setBounds(defaultRect);
			return;
		}

		// 计算空间位置
		Rectangle frm = frame.getBounds();
		//		int gapx = (dlg.width < frm.width ? (frm.width - dlg.width) / 2 : 0);
		//		int gapy = (dlg.height < frm.height ? (frm.height - dlg.height) / 2 : 0);

		int gapx = (defaultRect.width < frm.width ? (frm.width - defaultRect.width) / 2 : -((defaultRect.width - frm.width) / 2));
		int gapy = (defaultRect.height < frm.height ? (frm.height - defaultRect.height) / 2 : -((defaultRect.height - frm.height) / 2));
		int x = frm.x + gapx;
		int y = frm.y + gapy;

		// 最小是0
		if (x < 0) x = 0;
		if (y < 0) y = 0;

		// 超过显示范围时...
		Dimension dim = PlatformKit.getPlatformDesktop().getSize(); 
		if (x + defaultRect.width > dim.width) {
			x = dim.width - defaultRect.width;
		}
		if (y + defaultRect.height > dim.height) {
			y = dim.height - defaultRect.height;
		}

		// 设置显示范围
		Rectangle rect = new Rectangle(x, y, defaultRect.width, defaultRect.height);
		setBounds(rect);
	}

	/**
	 * 找到桌面组件
	 * @param component
	 * @return 返回桌面组件
	 */
	protected JDesktopPane findDesktopPaneForComponent(Component component) {
		if (component == null) {
			return PlatformKit.getPlatformDesktop();
		}

		// 判断是桌面
		if (Laxkit.isClassFrom(component, JDesktopPane.class)) {
			return (JDesktopPane) component;
		}

		return findDesktopPaneForComponent(component.getParent());
	}

	/**
	 * 返回桌面组件
	 * @param component 组件
	 * @return 返回容器实例
	 */
	protected Container getDesktopPaneForComponent(Component component) {
		Container parent = findDesktopPaneForComponent(component);

		if (parent == null && (component == null || 
				(parent = component.getParent()) == null)) {
			String s = "LightDialog: parent component does not have a valid parent";
			String name = (component == null ? "null" : component.getClass().getName());
			throw new RuntimeException(s + " | class " + name);
		}
		return parent;
	}

	/**
	 * 在显示后进行初始化，由子类来做具体实现，这里是空方法
	 */
	protected void doDefaultOnShow() {

	}

	/**
	 * 查找父级的LightDialog
	 * @param parent 当前实例
	 * @return 返回实例，或者空指针
	 */
	private LightDialog findParentDialog(Component parent) {
		if (parent == null) {
			return null;
		}

		if (parent instanceof LightDialog) {
			return ((LightDialog) parent);
		}
		return findParentDialog(parent.getParent());
	}

	/**
	 * 查找父级的LightFrame
	 * @param parent 当前实例
	 * @return 返回实例，或者空指针
	 */
	private LightFrame findParentFrame(Component parent) {
		if (parent == null) {
			return null;
		}

		if (parent instanceof LightFrame) {
			return ((LightFrame) parent);
		}
		return findParentFrame(parent.getParent());
	}

	/**
	 * 找到上级的模态窗口，在它的级别上加1，做为自己的模态窗口层级号，绑定在桌面上。
	 * @param parent 父类窗口
	 * @return 返回模态窗口层级号
	 */
	private int doNextModalLayout(Component parent) {		
		// 来自对话框
		LightDialog dialg = findParentDialog(parent);
		if (dialg != null) {
			return dialg.getLayer() + 1;
		}

		// 来自窗口
		LightFrame frame = findParentFrame(parent);
		if (frame != null) {
			int layer = JLayeredPane.getLayer(frame);
			// 如果窗口此时是全屏幕状态，返回比全屏更高级别
			if (layer >= FormLayer.FULL_SCREEN) {
				return layer + 1;
			} else {
				return layer + 1;
			}
		}

		// 默认为模态窗口级别，高于LightFrame的FRAME
		return FormLayer.MODAL_DIALOG;
	}

	/**
	 * 找到上级的模态窗口，在它的级别上加1，做为自己的模态窗口层级号，绑定在桌面上。
	 * @param parent 父类窗口
	 * @return 返回模态窗口层级号
	 */
	private int doNextNormalLayout(Component parent) {		
		// 来自对话框
		LightDialog dialg = findParentDialog(parent);
		if (dialg != null) {
			return dialg.getLayer() + 1;
		}

		// 来自窗口
		LightFrame frame = findParentFrame(parent);
		if (frame != null) {
			int layer = JLayeredPane.getLayer(frame);
			// 如果窗口此时是全屏幕状态，返回比全屏更高级别
			if (layer >= FormLayer.FULL_SCREEN) {
				return layer + 1;
			} else {
				return layer + 1; // 加1
			}
		}

		// 标准级别，和LightFrame的FRAME同级
		return FormLayer.NORMAL_DIALOG;
	}

	/**
	 * 启动二级模态窗口
	 */
	private synchronized void startModal() {
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				EventQueue eq = getToolkit().getSystemEventQueue();
				do {
					if (shine.isExit()) {
						break;
					}

					AWTEvent ae = eq.getNextEvent();
					Object obj = ae.getSource();
					if (ae instanceof ActiveEvent) {
						((ActiveEvent) ae).dispatch();
					} else if (obj instanceof Component) {
						((Component) obj).dispatchEvent(ae);
					} else if (obj instanceof MenuComponent) {
						((MenuComponent) obj).dispatchEvent(ae);
					} else {
						//						System.out.println("not found source!");
					}
				} while(true);
			} else {
				do {
					if (shine.isExit()) {
						break;
					}
					wait();
				} while (true);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}
	}

	private synchronized void stopModal() {
		try {
			notifyAll();
		} catch (Throwable e) {
			Logger.fatal(e);
		}
	}

	/**
	 * 显示为模态形式，进入等待，返回结果
	 * @param bind 绑定状态
	 * @param focus 焦点对象，允许空指针
	 * @return 返回结果对象
	 */
	protected Object showModalDialog(Component bind, Component focus) {
		// 重装标题栏面板
		reloadTitlePane();
		
		// 返回它的上级容器
		Container parent = getDesktopPaneForComponent(bind);
		// 设置父窗口的图标
		setDefaultIcon(bind);
		// 关联的父窗口
		doAttachFrame(bind);

		// 关闭模态窗口事件
		addInternalFrameListener(new ModalDialogAdapter(this));
		// 触发参数更换事件
		addPropertyChangeListener(new LightPropertyChangeAdapter(this));

		Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		// 增加到面板上
		if (Laxkit.isClassFrom(parent, JDesktopPane.class)) {
			int layer = doNextModalLayout(bind); // 获得级别
			parent.add(this, new Integer(layer)); // 加入桌面
			setLayer(new Integer(layer)); // 记录它
		} else {
			parent.add(this, BorderLayout.CENTER);
		}

		// 失效重置
		parent.validate(); 
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}

		// 显示
		setVisible(true);

		// 显示上层界面
		if (isVisible() && !isShowing()) {
			Container c = getParent();
			while (c != null) {
				if (c.isVisible() == false) {
					c.setVisible(true);
				}
				c = c.getParent();
			}
		}

		// 默认的焦点组件
		if (focus != null) {
			focus.requestFocus();
		}

		// 在显示后初始化
		doDefaultOnShow();

		// 更新UI，放在最后处理，看是否有异常出现。
		refreshUI();
		
		// 使用引用：Container.startLWModal，进入模态状态 
		try {
			Object obj;
			obj = AccessController.doPrivileged(new LightPrivilegedAction(
					Container.class, "startLWModal"));
			if (obj != null) {
				// 设置为模态
				setModal(true);
				// 调用
				((Method) obj).invoke(this, (Object[]) null);
			}
		} catch (IllegalAccessException ex) {
			Logger.error(ex);
		} catch (IllegalArgumentException ex) {
			Logger.error(ex);
		} catch (InvocationTargetException ex) {
			Logger.error(ex);
		}

		// 1. 如果父类窗口已经是模态时，即"Container.startLWModal"方法生效进入状态，这时作用是以“备用”身份进入模态
		// 2. 如果"Container.startLWModal"没有启动，这时以正式身份进入模态。
		startModal();

		// 如果绑定关联是JInternalFrame，设置为焦点的“选择”状态
		if (bind instanceof JInternalFrame) {
			try {
				((JInternalFrame) bind).setSelected(true);
			} catch (java.beans.PropertyVetoException e) {
			}
		}

		if (fo != null && fo.isShowing()) {
			fo.requestFocus();
		}

		// 退出模态
		setModal(false);

		// 返回值
		return getSelectedValue();
	}

	/**
	 * 设置父类的图标
	 * @param parent
	 * @return 成功返回真，否则假
	 */
	private boolean setDefaultIcon(Component parent) {
		// 找到父窗口
		JInternalFrame frame = findInternalFrameForComponent(parent);
		if (frame != null) {
			Icon icon = frame.getFrameIcon();
			if (icon != null) {
				setFrameIcon(icon);
			}

			// 赋值窗口
			if (Laxkit.isClassFrom(frame, LightFrame.class)) {
				LightFrame light = (LightFrame) frame;
				setMinimizeIcon(light.getMinimizeIcon());
				setMaximizeIcon(light.getMaximizeIcon());
				setRestoreIcon(light.getRestoreIcon());
				setCloseIcon(light.getCloseIcon());
			}

			return true;
		}

		return false;
	}

	/**
	 * 设置绑定的父窗口
	 * @param parent 父类实例
	 * @return 找到返回真，否则假
	 */
	private boolean doAttachFrame(Component parent) {
		// 找到父窗口
		LightFrame frame = findLightFrameForComponent(parent);
		// 没有时，找到它的上级对话框，从对话框中找到父窗口
		if (frame == null) {
			LightDialog dlg = findLightDialogForComponent(parent);
			if (dlg != null) {
				frame = dlg.getAttachFrame();
			}
		}
		// 存在，保存它
		if (frame != null) {
			setAttachFrame(frame);
			return true;
		}
		return false;
	}

	/**
	 * 设置默认的边框
	 */
	protected void setDefaultBorder() {
		// METAL界面，底部留空6个像素
		if (isShowBorder()) {
			if (isMetalUI()) {
				Color c = super.getBackground();
				ESL esl = new RGB(c).toESL();
				if (Skins.isGraySkin()) {
					c = esl.toDraker(30).toColor();
				} else {
					c = esl.toBrighter(50).toColor();
				}
				LineBorder line = new LineBorder(c, 2, false); // 2个像素，不要钝角
				CompoundBorder border = new CompoundBorder(line, new EmptyBorder(0, 0, 2, 0));
				setBorder(border);
			}
		} else {
			setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}

	//	/**
	//	 * 设置默认的边框，分为Nimbus/Metal界面
	//	 */
	//	protected void setDefaultBorder() {
	//		if (isShowBorder()) {
	//			// METAL界面，浮凸出的效果
	//			if (isMetalUI()) {
	//				Color c = super.getBackground();
	//				ESL esl = new RGB(c).toESL();
	//				Color b = (Skins.isGraySkin() ? esl.toDraker(25).toColor() : esl.toBrighter(52).toColor());
	//				BevelBorder line = new BevelBorder(BevelBorder.RAISED, b, b);
	//				setBorder(line);
	//			}
	//		} else {
	//			setBorder(new EmptyBorder(0, 0, 0, 0));
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gui.LightForm#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 显示边框或者否
		setDefaultBorder();
	}
	
//	/**
//	 * 刷新
//	 */
//	private void reloadUI() {
//		if (isRefreshUI()) {
//			__reloadUI();
//		}
//	}

	/**
	 * 显示非模态的对话窗口，显示层高于Frame
	 * @param bind 绑定对象
	 * @param focus 焦点组件
	 * @return 返回一个空对象
	 */
	protected Object showNormalDialog(Component bind, Component focus) {
		// 重装标题栏面板
		reloadTitlePane();
		
		// 返回它的上级容器
		Container parent = getDesktopPaneForComponent(bind);
		// 设置父窗口的图标
		setDefaultIcon(bind);
		// 找到关联的父窗口
		doAttachFrame(bind);

		// 增加到面板上
		if (Laxkit.isClassFrom(parent, JDesktopPane.class)) {
			int layer = doNextNormalLayout(bind); // 获得级别
			// 加入桌面
			parent.add(this, new Integer(layer));
			setLayer(new Integer(layer));
		} else {
			parent.add(this, BorderLayout.CENTER);
		}

		// 失效重置
		parent.validate();
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}

		// 显示
		setVisible(true);

		// 显示上层界面
		if (isVisible() && !isShowing()) {
			Container c = getParent();
			while (c != null) {
				if (c.isVisible() == false) {
					c.setVisible(true);
				}
				c = c.getParent();
			}
		}
		// 默认的焦点组件
		if (focus != null) {
			focus.requestFocus();
		}

		// 最后更新UI
		refreshUI();

		// 返回空指针
		return null;
	}

	/**
	 * 显示为模态的对话窗口，窗口将进入等待，返回结果
	 * @param parent 绑定窗口
	 * @return 返回结果对象
	 */
	protected Object showModalDialog(Component parent) {
		return showModalDialog(parent, null);
	}

	/**
	 * 显示为非模态的对话窗口，在桌面的显示层高于LightFrame
	 * @param parent 绑定窗口
	 * @return 返回一个空对象
	 */
	protected Object showNormalDialog(Component parent) {
		return showNormalDialog(parent, null);
	}

	/**
	 * 显示对话窗口，返回结果值。
	 * 如果不是模态，窗口显示后返回一个空指针
	 * 如果是模态，窗口显示后进入等待状态，返回一个结果对象。结果对象由子类定义。
	 * 
	 * @param parent 图形桌面句柄或者或者LightForm子类句柄
	 * @param modal 模态或者否
	 * @return 返回结果，或者是空指针
	 */
	public abstract Object showDialog(Component parent, boolean modal);

}

//	class CloseThread extends SwingEvent {
//		public CloseThread(){
//			super();
//		}
//
//		public void process() {
//			closeDialog();
//		}
//	}


// 没有选中的鼠标时
//		addMouseListener(new NotSelectMouseAdapeter());

//		// 面板
//		Container cc = getContentPane();
//		if (cc != null) {
//			cc.addMouseListener(new NotSelectMouseAdapeter());
//		}


//		// 设置模态的焦点窗口
//		PlatformKit.setModalDialog(this);


//		// 取消模态的焦点窗口
//		PlatformKit.cancelModalDialog(this);


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
//		// NIMBUS界面，TOP保持2个像素，这里不会有黑点，否则会出现。LEFT/BOTTOM/RIGHT保持4个像素，这时界面感觉最佳。
//		setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
//	}
//}


// 边框说明：Nimbus/Metal界面
// Nimbus的Border是EmptyBorder(2,4,4,4), Metal的Border是复合边框

//		// NIMBUS界面
//		if (isNimbusUI()) {
//			// 修改/调整边框
//			setNimbusBorder();
//		}

