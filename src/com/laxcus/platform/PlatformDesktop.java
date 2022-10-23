/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.listener.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;

/**
 * LAXCUS桌面，支持修改背景图像和背景图片。任何一个FRAME，只能有一个桌面
 * 
 * @author scott.liang
 * @version 1.0 5/31/2021
 * @since laxcus 1.0
 */
public abstract class PlatformDesktop extends JDesktopPane implements FrameBar {

	private static final long serialVersionUID = -754179740429464131L;

	/** 背景方案 **/
	private DesktopWall desktopWall;

	/** 单锁 **/
	private MutexLock lock = new MutexLock();

	/** 保存窗口句柄 **/
	private ArrayList<LightFrame> frames = new ArrayList<LightFrame>();

	/** 隐藏状态的焦点窗口 **/
	private LightFrame focusHideFrame;

	/** 隐藏的窗口 **/
	private ArrayList<LightFrame> hideFrames = new ArrayList<LightFrame>();

	/** 平台事件保存器 **/
	private PlatformMulticaster multicaster = new PlatformMulticaster();

//	/**
//	 * 构造桌面
//	 * @param system 是系统桌面或者否，系统桌面在运行环境中具有唯一性。如果是系统桌面，会记录它。
//	 */
//	public PlatformDesktop(boolean system) {
//		super();
//		if (system) {
//			PlatformKit.setPlatformDesktop(this);
//		}
//	}

//	/**
//	 * 构造桌面，默认是系统桌面
//	 */
//	protected PlatformDesktop() {
//		this();
//	}

	/**
	 * 构造平台桌面，同步注册平台的系统事件
	 * 
	 * @param systemListeners 系统事件
	 */
	public PlatformDesktop(PlatformListener[] systemListeners) {
		super();
		// 记录
		PlatformKit.setPlatformDesktop(this);
		// 注册平台的系统事件
		if (systemListeners != null) {
			for (PlatformListener ls : systemListeners) {
				multicaster.register(ls);
			}
		}
	}

	/**
	 * 注册平台事件监听接口，只接受用户级的事件
	 * @param l PlatformListener派生接口实例
	 * @return 成功返回真，否则假
	 */
	public boolean addPlatformListener(PlatformListener l) {
		// 如果不是用户接口，返回假
		if (!PlatformListenerChecker.isUserListener(l)) {
			return false;
		}
		// 注册
		return multicaster.register(l);
	}

	/**
	 * 注销平台事件监听接口
	 * @param l PlatformListener派生接口实例
	 * @return 成功返回真，否则假
	 */
	public boolean removePlatformListener(PlatformListener l) {
		return multicaster.unregister(l);
	}

	/**
	 * 返回平台事件监听接口实例
	 * @param <T>
	 * @param clazz PlatformListener的子类接口
	 * @return 返回当前全部匹配的
	 */
	public <T extends PlatformListener> T[] findListeners(Class<?> clazz) {
		return multicaster.findListeners(clazz);
	}

	/**
	 * 返回监听接口实例集合中的第一个，通常都是系统级监听实例
	 * @param <T> 类类型
	 * @param clazz PlatformListener的子类接口
	 * @return 返回实例，没有是空指针
	 */
	public <T extends PlatformListener> T findListener(Class<?> clazz) {
		return multicaster.findListener(clazz);
	}
	
	/**
	 * 增加窗口
	 * @param frame
	 * @return
	 */
	public boolean addFrame(LightFrame frame) {
		boolean success = false;
		// 锁定
		lock.lockSingle();
		try {
			for (LightFrame member : frames) {
				if (member == frame) {
					return false;
				}
			}
			// 保存
			success = frames.add(frame);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}
		return success;
	}


	private void setSelectFrame(JInternalFrame frame, boolean selected) {
		try {
			frame.setSelected(selected);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
	}

//	/**
//	 * 切换桌面上的隐藏/显示窗口 <br><br>
//	 * 规则：<br>
//	 * 1. 如果当前有窗口显示时，记录这些窗口，然后隐藏它们 <br>
//	 * 2. 如果当前没有窗口，但是隐藏记录有窗口，显示隐藏的窗口 <br>
//	 */
//	public void doFlexFrames() {
//		// 锁定处理
//		lock.lockSingle();
//		try {
//			// 统计当前显示的窗口
//			int count = 0;
//			for (LightFrame frame : frames) {
//				// 统计可视窗口
//				if (frame.isVisible()) {
//					count++;
//				}
//			}
//
//			// 如果有显示的，重新记录这些显示，然后隐藏它们
//			if (count > 0) {
//				// 清除旧的
//				hideFrames.clear();
//				focusHideFrame = null;
//
//				// 取出桌面上的窗口
//				for (LightFrame frame : frames) {
//					// 不是可视状态，忽略它
//					if (!frame.isVisible()) {
//						continue;
//					}
//					// 如果是选中状态，取消选中
//					if (frame.isSelected()) {
//						setSelectFrame(frame, false);
//						focusHideFrame = frame;
//					}
//					// 隐藏窗口
//					frame.setVisible(false);
//					// 保存这个窗口
//					hideFrames.add(frame);
//				}
//			}
//			// 如果记录中有显示的窗口，显示这些隐藏的窗口
//			else if (hideFrames.size() > 0) {
//				// 从最后一个开始显示
//				int size = hideFrames.size();
//				for (int i = size - 1; i >= 0; i--) {
//					LightFrame frame = hideFrames.get(i);
//					frame.setVisible(true);
//				}
//				if (focusHideFrame != null) {
//					setSelectFrame(focusHideFrame, true);
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			lock.unlockSingle();
//		}
//	}
	
	/**
	 * 隐藏关联的对话窗口
	 * @param parent 父类窗口
	 */
	private void hideLightDialog(LightFrame parent) {
		JInternalFrame[] elements = getAllFrames();
		int size = (elements == null ? 0 : elements.length);
		
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
			if (frame == parent) {
				if (dialog.isVisible()) {
					dialog.setVisible(false);
				}
			}
		}
	}
	
	/**
	 * 显示关联的对话窗口
	 * @param parent 父类窗口
	 */
	private void showLightDialog(LightFrame parent) {
		JInternalFrame[] elements = this.getAllFrames();
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
			if (frame == parent) {
				if (!dialog.isVisible()) {
					dialog.setVisible(true);
				}
			}
		}
	}
	
	/**
	 * 切换桌面上的隐藏/显示窗口 <br><br>
	 * 规则：<br>
	 * 1. 如果当前有窗口显示时，记录这些窗口，然后隐藏它们 <br>
	 * 2. 如果当前没有窗口，但是隐藏记录有窗口，显示隐藏的窗口 <br>
	 */
	public void doFlexFrames() {
		// 锁定处理
		lock.lockSingle();
		try {
			// 统计当前显示的窗口
			int count = 0;
			for (LightFrame frame : frames) {
				// 统计可视窗口
				if (frame.isVisible()) {
					count++;
				}
			}

			// 如果有显示的，重新记录这些显示，然后隐藏它们
			if (count > 0) {
				// 清除旧的
				hideFrames.clear();
				focusHideFrame = null;

				// 取出桌面上的窗口
				for (LightFrame frame : frames) {
					// 不是可视状态，忽略它
					if (!frame.isVisible()) {
						continue;
					}
					
					// 找到关联且处于显示状态的LightDialog，隐藏它
					hideLightDialog(frame);
					// 如果是选中状态，取消选中
					if (frame.isSelected()) {
						setSelectFrame(frame, false);
						focusHideFrame = frame;
					}
					// 隐藏窗口
					frame.setVisible(false);
					// 保存这个窗口
					hideFrames.add(frame);
				}
			}
			// 如果记录中有显示的窗口，显示这些隐藏的窗口
			else if (hideFrames.size() > 0) {
				// 从最后一个开始显示
				int size = hideFrames.size();
				for (int i = size - 1; i >= 0; i--) {
					LightFrame frame = hideFrames.get(i);
					frame.setVisible(true);
					// 显示关联的对话窗口
					showLightDialog(frame);
				}
				if (focusHideFrame != null) {
					setSelectFrame(focusHideFrame, true);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}
	}
	
	/**
	 * 找到绑空LightFrame的子窗口
	 * @param frame 父类窗口
	 * @return 返回LightDialog数组
	 */
	public LightDialog[] findLightDialog(LightFrame frame) {
		ArrayList<LightDialog> array = new ArrayList<LightDialog>();

		// 锁定
		lock.lockSingle();
		try {
			JInternalFrame[] jfs = getAllFrames();
			int size = (jfs != null ? jfs.length : 0);
			for (int i = 0; i < size; i++) {
				JInternalFrame jf = jfs[i];
				if (Laxkit.isClassFrom(jf, LightDialog.class)) {
					LightDialog dialog = (LightDialog) jf;
					LightFrame parent = dialog.getAttachFrame();
					// 如果匹配，返回真
					if (parent != null && parent == frame) {
						array.add(dialog);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}

		// 输出全部
		LightDialog[] as = new LightDialog[array.size()];
		return array.toArray(as);
	}

	/**
	 * 删除窗口
	 * @param frame
	 * @return
	 */
	public boolean removeFrame(LightFrame frame) {
		int count = 0;
		// 锁定
		lock.lockSingle();
		try {
			// 使用备份对象，防止“java.util.ConcurrentModificationException”发生
			ArrayList<LightFrame> a = new ArrayList<LightFrame>();
			a.addAll(frames);
			// 逐个比较
			for (LightFrame member : a) {
				if (member == frame) {
					boolean success = frames.remove(frame);
					if (success) {
						// 清除伸缩记录中的窗口
						hideFrames.remove(frame);
						if (frame == focusHideFrame) {
							focusHideFrame = null;
						}
						// 统计值
						count++;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}
		return (count > 0);
	}

	/**
	 * 输出窗口数组
	 * @return LightFrame数组
	 */
	public LightFrame[] getFrames() {
		lock.lockMulti();
		try {
			LightFrame[] a = new LightFrame[frames.size()];
			return frames.toArray(a);
		} finally {
			lock.unlockMulti();
		}
	}

	/**
	 * 判断窗口存在
	 * @param frame
	 * @return
	 */
	public boolean hasFrame(LightFrame frame) {
		lock.lockSingle();
		try {
			for (LightFrame member : frames) {
				if (member == frame) {
					return true;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}
		return false;
	}

	/**
	 * 返回显示的窗口
	 * @return LightFrame数组
	 */
	public LightFrame[] getShowFrames() {
		ArrayList<LightFrame> array = new ArrayList<LightFrame>();

		JInternalFrame[] frames = getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			// 只处理LightFrame窗口
			if (Laxkit.isClassFrom(frames[i], LightFrame.class)) {
				array.add((LightFrame) frames[i]);
			}
		}

		// 输出全部...
		LightFrame[] a = new LightFrame[array.size()];
		return array.toArray(a);
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 设置桌面背景方案
	 * @param e
	 */
	public void setDesktopWall(DesktopWall e) {
		if (e != null) {
			desktopWall = e.duplicate();
		} else {
			desktopWall = null;
		}
		repaint();
	}

	/**
	 * 返回桌面背景颜色
	 * @return Color实例
	 */
	public Color getDesktopWallColor() {
		if (desktopWall == null) {
			return null;
		}
		return desktopWall.getColor();
	}

	/**
	 * 绘制背景，绘制顺序：
	 * 1. 图片，又分为铺满和叠加
	 * 2. 颜色
	 * 
	 * @param g
	 */
	protected void paintComponent(Graphics g) {
		// 宽度和高度
		int desktopWidth = getWidth();
		int desktopHeight = getHeight();

		//		Color color = null;
		//		if (desktopWall != null) {
		//			color = desktopWall.color;
		//		}
		//		if (color == null) {
		//			if (GUIKit.isNimbusUI()) {
		//				color = new Color(58, 110, 165); // 桌面背景颜色
		//			} else {
		//				color = getBackground();
		//			}
		//		}
		
		
		// 定义的桌面颜色
		Color color = (desktopWall != null ? desktopWall.color : null);
		// 根据主题，选择桌面默认的颜色
		if (color == null) {
			MetalTheme theme = MetalLookAndFeel.getCurrentTheme();
			if (theme != null) {
				color = theme.getDesktopColor();
			}
		}
		// 最后无可奈何的默认颜色
		if (color == null) {
			color = new Color(58, 110, 165);
		}

		Color old = g.getColor();

		// 填充成指定颜色
		g.setColor(new Color(color.getRGB()));
		g.fillRect(0, 0, desktopWidth, desktopHeight);

		// 三种布局方式
		boolean success = (desktopWall != null && desktopWall.image != null);
		if (success) {
			Image image = desktopWall.getImage();
			int imgWidth = image.getWidth(null);
			int imgHeight = image.getHeight(null);

			// 如果与全屏尺寸不符合
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			if (desktopWidth != dim.width || desktopHeight != dim.height) {
				desktopWidth = dim.width;
				desktopHeight = dim.height;
			}

			// 拉伸铺满
			if (desktopWall.isFull()) {
				//				// 以全屏方式绘制图像
				//				g.drawImage(image, 0, 0, desktopWidth - 1, desktopHeight - 1,
				//						0, 0, imgWidth - 1, imgHeight - 1, null);

				// 以全屏方式绘制图像
				g.drawImage(image, 0, 0, desktopWidth, desktopHeight, 
						0, 0, imgWidth - 1, imgHeight - 1, null);
			}
			// 居中
			else if (desktopWall.isCenter()) {
				int x = (desktopWidth > imgWidth ? (desktopWidth - imgWidth) / 2 : 0); // X坐标
				int y = (desktopHeight > imgHeight ? (desktopHeight - imgHeight) / 2 : 0); // Y坐标
				g.drawImage(image, x, y, imgWidth, imgHeight, null);
			}
			// 多个图像
			else if (desktopWall.isMulti()) {
				for (int y = 0; y < desktopHeight; y += imgHeight) {
					for (int x = 0; x < desktopWidth; x += imgWidth) {
						g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
					}
				}
			}
		}

		// 恢复为原来的颜色
		g.setColor(old);
	}
	
	/**
	 * 播放声音
	 * @param who
	 */
	public abstract void playSound(int who);


}