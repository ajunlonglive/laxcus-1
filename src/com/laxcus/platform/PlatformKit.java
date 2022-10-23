/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import javax.swing.*;

import com.laxcus.gui.tray.*;
import com.laxcus.platform.listener.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.sound.*;

/**
 * 桌面平台工具 <br>
 * 不允许继承！
 * 
 * @author scott.liang
 * @version 1.0 6/19/2021
 * @since laxcus 1.0
 */
public final class PlatformKit {
	
	/** 桌面面板，在每个运行中都具有唯一性 **/
	private static PlatformDesktop desktop;
	
	/**
	 * 设置句柄
	 * @param e
	 */
	protected static boolean setPlatformDesktop(PlatformDesktop e) {
		if (PlatformKit.desktop == null) {
			PlatformKit.desktop = e;
			return true;
		}
		return false;
	}
	
	/**
	 * 输出桌面句柄
	 * @return
	 */
	public static PlatformDesktop getPlatformDesktop() {
		return PlatformKit.desktop;
	}

	/**
	 * 注册平台事件监听接口，注册必须是用户级监听接口
	 * 
	 * @param l PlatformListener派生接口实例
	 * @return 成功返回真，否则假
	 */
	public static boolean addPlatformListener(PlatformListener l) {
		return PlatformKit.desktop.addPlatformListener(l);
	}

	/**
	 * 注销平台事件监听接口
	 * @param l PlatformListener派生接口实例
	 * @return 成功返回真，否则假
	 */
	public static boolean removePlatformListener(PlatformListener l) {
		return PlatformKit.desktop.removePlatformListener(l);
	}
	
	/**
	 * 返回平台事件监听接口实例
	 * @param <T>
	 * @param clazz PlatformListener的子类接口
	 * @return 返回当前全部匹配的，没有是空数组
	 */
	public static <T extends PlatformListener> T[] findListeners(Class<?> clazz) {
		return PlatformKit.desktop.findListeners(clazz);
	}
	
	/**
	 * 返回平台监听接口实例的第一个
	 * @param <T> 类类型
	 * @param clazz PlatformListener子类类型
	 * @return 返回实例，没有是空指针
	 */
	public static <T extends PlatformListener> T findListener(Class<?> clazz) {
		return PlatformKit.desktop.findListener(clazz);
	}

	/**
	 * 找到根窗口
	 * @param parent
	 * @return
	 */
	private static JFrame findRootFrame(Component parent) {
		if (parent == null) {
			return null;
		}
		if (Laxkit.isClassFrom(parent, JFrame.class)) {
			return (JFrame) parent;
		}
		return findRootFrame(parent.getParent());
	}
	
	/**
	 * 压缩图标
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	private static ImageIcon compressImage(Image image, int width, int height) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		
		// 平滑缩小图象
		if (w != width || h != height) {
			Image img = image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
			return new ImageIcon(img);
		} else {
			return new ImageIcon(image);
		}
	}
	

	/** 主窗口图标 **/
	private static ImageIcon platformIcon;
	
	/**
	 * 返回运行环境的主窗口图标
	 * @return 图标实例，没有是空指针
	 */
	public static ImageIcon getPlatformIcon() {
		if (PlatformKit.platformIcon != null) {
			return PlatformKit.platformIcon;
		}
		if (PlatformKit.desktop == null) {
			return null;
		}
		
		JFrame frame = PlatformKit.findRootFrame(PlatformKit.desktop);
		if (frame == null) {
			return null;
		}
		// 显示图标
		Image image = frame.getIconImage();
		// 压缩图像
		PlatformKit.platformIcon = PlatformKit.compressImage(image, 16, 16);
		return PlatformKit.platformIcon;
	}
	
//	/** 命令分派器 **/
//	private static CommandDispatcher dispatcher;
//	
//	/**
//	 * 设置命令分派器。只能够设置一次！
//	 * 
//	 * @param e CommandDispatcher实例
//	 */
//	public static boolean setCommandDispatcher(CommandDispatcher e) {
//		if (PlatformKit.dispatcher == null) {
//			PlatformKit.dispatcher = e;
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 返回命令分派器
//	 * @return CommandDispatcher实例
//	 */
//	public static CommandDispatcher getCommandDispatcher() {
//		return PlatformKit.dispatcher;
//	}

//	/** 资源辅助器 **/
//	private static ResourceAssistor assistor;
//	
//	/**
//	 * 设置资源辅助器。只能够设置一次！
//	 * 
//	 * @param e ResourceAssistor实例
//	 */
//	public static boolean setResourceAssistorX(ResourceAssistor e) {
//		if (PlatformKit.assistor == null) {
//			PlatformKit.assistor = e;
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 返回资源辅助器
//	 * @return ResourceAssistor实例
//	 */
//	public static ResourceAssistor getResourceAssistor() {
//		return PlatformKit.assistor;
//	}
	
	/** 托盘管理器 **/
	private static TrayManager trayManager;

	/**
	 * 设置托盘管理器
	 * @param e
	 */
	public static boolean setTrayManager(TrayManager e) {
		if (PlatformKit.trayManager == null) {
			PlatformKit.trayManager = e;
			return true;
		}
		return false;
	}

	/**
	 * 返回托盘管理器
	 * @return
	 */
	public static TrayManager getTrayManager() {
		return PlatformKit.trayManager;
	}
	
	/** 系统应用存储目录 */
	private static File systemStoreRoot;
	
	/** 用户应用存储目录 **/
	private static File userStoreRoot;
	
	/** 系统临时存储目录 **/
	private static File systemTemporaryRoot;

	/**
	 * 设置系统应用存储根目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 系统应用存储根目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public static boolean setSystemStoreRoot(File path) {
		// 只能在启动时设置一次
		if (systemStoreRoot != null) {
			return false;
		}

		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				systemStoreRoot = path.getCanonicalFile();
			} catch (IOException e) {
				success = false;
			}
		}
		
		// 返回结果
		return success;
	}

	/**
	 * 设置系统应用存储根目录
	 * @param path 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public static boolean setSystemStoreRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setSystemStoreRoot(new File(path));
	}

	/**
	 * 在指定存储根目录之下，通过后缀目录建立一个新目录，做为系统应用的存储根目录
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public static boolean setSystemStoreRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setSystemStoreRoot(new File(path, subpath));
	}

	/**
	 * 返回系统应用存储根目录
	 * @return File实例
	 */
	public static File getSystemStoreRoot() {
		return systemStoreRoot;
	}

	/**
	 * 设置用户应用存储根目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 用户应用存储根目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public static boolean setUserStoreRoot(File path) {
		// 只能在启动时设置一次
		if (userStoreRoot != null) {
			return false;
		}
		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				userStoreRoot = path.getCanonicalFile();
			} catch (IOException e) {
				success = false;
			}
		}

		// 返回结果
		return success;
	}

	/**
	 * 设置用户应用存储根目录
	 * @param path 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public static boolean setUserStoreRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setUserStoreRoot(new File(path));
	}

	/**
	 * 在指定存储根目录之下，通过后缀目录建立一个新目录，做为用户应用的存储根目录
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public static boolean setUserStoreRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setUserStoreRoot(new File(path, subpath));
	}

	/**
	 * 返回用户应用存储根目录
	 * @return File实例
	 */
	public static File getUserStoreRoot() {
		return userStoreRoot;
	}
	
	/**
	 * 设置系统临时存储目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 系统临时存储目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public static boolean setSystemTemporaryRoot(File path) {
		// 只能在启动时设置一次
		if(systemTemporaryRoot != null) {
			return false;
		}
		
		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				systemTemporaryRoot = path.getCanonicalFile();
			} catch (IOException e) {
				success = false;
			}
		}
		
		// 返回结果
		return success;
	}

	/**
	 * 设置系统临时存储目录
	 * @param path 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public static boolean setSystemTemporaryRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setSystemTemporaryRoot(new File(path));
	}

	/**
	 * 在指定存储根目录之下，通过后缀目录建立一个新目录，做为系统应用的存储根目录
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public static boolean setSystemTemporaryRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setSystemTemporaryRoot(new File(path, subpath));
	}

	/**
	 * 返回系统临时存储目录
	 * @return File实例
	 */
	public static File getSystemTemporaryRoot() {
		return systemTemporaryRoot;
	}
		
	/** 系统注册环境 **/
	private static RTEnvironment evnironment = RTEnvironment.getInstance();
	
	/**
	 * 输出系统注册环境
	 * @return RTEnvironment
	 */
	public static RTEnvironment getRTEnvironment() {
		return PlatformKit.evnironment;
	}
	
	/**
	 * 在允许播放声音前提下，发出beep的声音
	 */
	public static void beep() {
		if (SoundPlayer.getInstance().isPlay()) {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/**
	 * 根据传入的文件或者SRL实例，启动关联应用，打开文件
	 * @param o
	 */
	public static void open(Object o) {
		DesktopListener as = PlatformKit.findListener(DesktopListener.class);
		if (as != null) {
			as.open(o);
		}
	}

	/**
	 * 设置桌面壁纸
	 * @param o
	 * @param layout
	 * @return 成功返回真，否则假
	 */
	public static boolean setWallPaper(Object o, int layout) {
		DesktopListener as = PlatformKit.findListener(DesktopListener.class);
		if (as != null) {
			return as.setWallPaper(o, layout);
		}
		return false;
	}

}