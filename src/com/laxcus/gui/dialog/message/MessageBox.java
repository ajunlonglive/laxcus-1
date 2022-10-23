/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.message;

import java.awt.*;
import javax.swing.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.font.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;

/**
 * 模态对话框管理器
 * 
 * 生成各种模态对话框，选择其中一个结果值
 * 
 * @author scott.liang
 * @version 1.0 6/13/2021
 * @since laxcus 1.0
 */
public class MessageBox {
	
	/**
	 * 选择字体
	 * @param parent 父类组件，可以是JDesktopPane或者JInternalFrame
	 * @param font 默认的字体
	 * @return 返回新的字体值
	 */
	public static Font choiceFont(Component parent, Font font) { 
		// 生成对话框
		FontDialog dialog = new FontDialog(font);
		dialog.setRefreshUI(false); // 不要刷新
		// 以模态显示结果
		Object value = dialog.showDialog(parent, true);
		// 判断和输出结果
		if (value != null && Laxkit.isClassFrom(value, Font.class)) {
			return (Font) value;
		}
		return null;
	}

	/**
	 * 返回内部窗体的标题图标
	 * @param component 组件
	 * @return 返回图标实例，没有是空指针
	 */
	public static Icon findFrameIcon(Component component) {
		if (component == null) {
			return null;
		} else if (Laxkit.isClassFrom(component, JInternalFrame.class)) {
			JInternalFrame frame = (JInternalFrame) component;
			return frame.getFrameIcon();
		}
		// 找到父类
		return MessageBox.findFrameIcon(component.getParent());
	}
	
	/**
	 * 显示对话框
	 * @param parent
	 * @param title
	 * @param iconId
	 * @param icon
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showDialog(Component parent, String title, Icon titleIcon, 
			int iconId, Icon icon, String content, int buttonId)  { 
		
		// 找到图标
		if (titleIcon == null) {
			titleIcon = findFrameIcon(parent);
		}

		// 标题图标
		if (titleIcon == null) {
			titleIcon = PlatformKit.getPlatformIcon();
		}
		
		LightMessageDialog dialog = new LightMessageDialog();
		dialog.setRefreshUI(false); // 不要刷新
		// 初始化对话框
		dialog.initDialog(title, titleIcon, iconId, icon, content, buttonId);
		// 以模态显示对话框
		Object value = dialog.showDialog(parent, true);
		// 输出选择值
		if (value != null && value.getClass() == java.lang.Integer.class) {
			return ((java.lang.Integer) value).intValue();
		}
		return -1;
	}
	
	/**
	 * 显示一个内部窗体的图标
	 * @param frame 内部窗体
	 * @param icon 左侧图标
	 * @param title 标题
	 * @param content 显示的内容
	 * @return 退出返回真，否则假
	 */
	public static int showYesNoCancelDialog(Component frame, String title, String content) {
		// 显示模态对话
		return showDialog(frame, title, null,
				DialogOption.QUESTION_MESSAGE, null, content, DialogOption.YES_NO_CANCEL_OPTION);
	}
	
	/**
	 * 显示一个内部窗体的图标
	 * @param frame 内部窗体
	 * @param icon 左侧图标
	 * @param title 标题
	 * @param content 显示的内容
	 * @return 退出返回真，否则假
	 */
	public static boolean showYesNoDialog(Component frame, String title, String content) {
		// 显示模态对话
		int who = showDialog(frame, title, null,
				DialogOption.QUESTION_MESSAGE, null, content, DialogOption.YES_NO_OPTION);
		return who == DialogOption.YES_OPTION;
	}
	
	/**
	 * 显示一个内部窗体的图标
	 * @param frame 内部窗体
	 * @param title 标题
	 * @param icon 左侧图标
	 * @param content 显示的内容
	 * @return 退出返回真，否则假
	 */
	public static boolean showYesNoDialog(Component frame, String title, Icon icon, String content) {
		// 显示模态对话
		int who = showDialog(frame, title, null,
				DialogOption.QUESTION_MESSAGE, icon, content, DialogOption.YES_NO_OPTION);
		return who == DialogOption.YES_OPTION;
	}

	/**
	 * 显示OKAY
	 * @param parent
	 * @param title
	 * @param content
	 */
	public static void showInformation(Component parent, String title, String content) {
		showDialog(parent, title, null, DialogOption.INFORMATION_MESSAGE, null, content, DialogOption.DEFAULT_OPTION); // DialogOption.OK_OPTION);
	}
	
	/**
	 * 显示失败的对话框
	 * @param parent
	 * @param title
	 * @param content
	 */
	public static void showFault(Component parent, String title, String content) {
		showDialog(parent, title, null, DialogOption.ERROR_MESSAGE,
				null, content, DialogOption.DEFAULT_OPTION); // DialogOption.OK_OPTION);
	}
	
	/**
	 * 显示警告对话框
	 * @param parent
	 * @param title
	 * @param content
	 */
	public static void showWarning(Component parent, String title,
			String content) {
		showDialog(parent, title, null, DialogOption.WARNING_MESSAGE, null,
				content, DialogOption.DEFAULT_OPTION); // DialogOption.OK_OPTION);
	}

}