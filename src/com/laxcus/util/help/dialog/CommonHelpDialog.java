/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.help.*;

/**
 * 帮助文档窗口
 * 
 * @author scott.liang
 * @version 1.1 4/27/2018
 * @since laxcus 1.0
 */
public class CommonHelpDialog extends JFrame {

	private static final long serialVersionUID = 5666302250621358120L;
	
	/** 已经启动或者否 **/
	private static volatile boolean loaded;
	
	/**
	 * 判断加载或者否
	 * @return 返回真或者否
	 */
	public static boolean isLoaded() {
		return CommonHelpDialog.loaded;
	}
	
	/**
	 * 加载窗口
	 * @param b
	 */
	private static void setLoad(boolean b) {
		CommonHelpDialog.loaded = b;
	}

	/** 显示HTML文档的面板 **/
	private CommonHelpPanel htmlPanel = new CommonHelpPanel();

	/**
	 * 构造默认的帮助文档窗口
	 */
	public CommonHelpDialog() {
		super();
	}

	/**
	 * 销毁图形资源
	 */
	private void destroy() {
		dispose();
	}
	
	/**
	 * 关闭窗口
	 */
	private void exit() {
		// 关闭窗口
		setVisible(false);
		destroy();
		
		// 取消加载
		CommonHelpDialog.setLoad(false);
	}
	
	class WindowCloseAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			exit();
		}
	}

	/**
	 * 设置命令解释上下文
	 * @param e 命令解释上下文
	 */
	public void setCommentContext(CommentContext e) {
		htmlPanel.setCommentContext(e);
	}

	/**
	 * 返回命令解释上下文
	 * @return 命令解释上下文
	 */
	public CommentContext getCommentContext() {
		return htmlPanel.getCommentContext();
	}
	
	/**
	 * 设置帮助图标
	 * @param close
	 * @param open
	 * @param command
	 * @param search
	 * @param go
	 */
	public void setHelpIcon(Icon close, Icon open, Icon command, Icon search, Icon go) {
		htmlPanel.setHelpIcon(close, open, command, search, go);
	}

	/**
	 * 生成面板
	 * @return
	 */
	private JPanel initPane() {
		// 初始化参数
		htmlPanel.init();

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
//		pane.setBorder(new EmptyBorder(5, 3, 5, 3));
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));
		pane.add(htmlPanel, BorderLayout.CENTER);
//		pane.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		return pane;
	}
	
	/**
	 * 显示窗口
	 * @param title
	 * @param image
	 */
	public void showDialog(String title, Image image) {
		JPanel pane = initPane();

//		Container canvas = getContentPane();
//		canvas.setLayout(new BorderLayout(0, 0));
//		canvas.add(pane, BorderLayout.CENTER);
		
		// 设置面板
		setContentPane(pane);

		// 设置对话框在图形窗口的区域
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (size.getWidth() * 0.68);
		int height = (int) (size.getHeight() * 0.8);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		setBounds(new Rectangle(x, y, width, height));

		setTitle(title);
		setIconImage(image);
		
		addWindowListener(new WindowCloseAdapter());

		setMinimumSize(new Dimension(380, 180));
		
		setVisible(true);
		
		toFront();
		
		// 加载成功
		CommonHelpDialog.setLoad(true);
	}

}