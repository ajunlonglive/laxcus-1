/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.frame.help;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.front.desktop.frame.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;
import com.laxcus.util.help.*;
import com.laxcus.util.help.dialog.*;
import com.laxcus.util.skin.*;

/**
 * 帮助文档窗口
 * 
 * @author scott.liang
 * @version 1.1 4/27/2018
 * @since laxcus 1.0
 */
public class DesktopHelpFrame extends DesktopFrame {

	private static final long serialVersionUID = 5666302250621358120L;
	
	/** 句柄 **/
	static DesktopHelpFrame selfHandle;

	/**
	 * 返回句柄
	 * @return
	 */
	public static DesktopHelpFrame getInstance() {
		return DesktopHelpFrame.selfHandle;
	}

	/** 显示HTML文档的面板 **/
	private CommonHelpPanel htmlPanel = new CommonHelpPanel();

	/**
	 * 构造默认的帮助文档窗口
	 */
	public DesktopHelpFrame() {
		// super("帮助文件");
		super();
	}
	
	/**
	 * 关闭窗口
	 */
	private void exit() {
		//		String title = findCaption("MessageBox/Exit/Help/Title/title");
		//		String content = findCaption("MessageBox/Exit/Help/Message/title");

		//		HelpFrame.ExitTitle 	退出 - 帮助
		//		HelpFrame.ExitContent 您确定退出系统桌面的帮助窗口 ？	

		String title = UIManager.getString("HelpFrame.ExitTitle");
		String content = UIManager.getString("HelpFrame.ExitContent");
		boolean exit = MessageBox.showYesNoDialog(this, title, content);
		// 判断是关闭窗口
		if (!exit) {
			return;
		}

		// 分割线
		htmlPanel.writeDeviderLocation();
		
		// 关闭窗口
		closeWindow();
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.desktop.component.DesktopFrame#closeWindow()
//	 */
//	@Override
//	public void closeWindow() {
//		// 保存位置
//		Rectangle rect = getBounds();
//		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "HelpFrame/Bound", rect);
//		
////		DesktopProperties.writeHelpBound(rect);
//		
//		// 关闭窗口
//		setVisible(false);
//		dispose();
//		
//		// 取消加载
//		DesktopHelpFrame.setLoad(false);
//	}
	
	class WindowCloseAdapter extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			exit();	
		}
	}
	
	/**
	 * 显示命令
	 * @param command
	 */
	public void showCommand(String command) {
		htmlPanel.showCommand(command);
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
	private void setHelpIcon(Icon close, Icon open, Icon command, Icon search, Icon go) {
		htmlPanel.setHelpIcon(close, open, command, search, go);
	}
	
	private void initIcon() {
		Icon closeIcon = UIManager.getIcon("HelpFrame.CloseIcon"); // 关闭图标
		Icon openIcon = UIManager.getIcon("HelpFrame.OpenIcon");// 打开图标
		Icon commandIcon = UIManager.getIcon("HelpFrame.CommandIcon");// 命令图标
		ImageIcon searchIcon = (ImageIcon)UIManager.getIcon("HelpFrame.SearchIcon");// 搜索图标
		searchIcon = ImageUtil.brighter(searchIcon, 30); // 调亮图标
		Icon goIcon = UIManager.getIcon("HelpFrame.GoIcon");// 命令图标

		setHelpIcon(closeIcon, openIcon, commandIcon, searchIcon, goIcon);
	}

	/**
	 * 生成面板
	 * @return
	 */
	private JPanel initPane() {
		// 初始化参数
		htmlPanel.init();
		
		initIcon();

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		// pane.setBorder(new EmptyBorder(5, 3, 5, 3));
		
		pane.add(htmlPanel, BorderLayout.CENTER);
		pane.setBorder(new EmptyBorder(5, 3, 3, 3));

		return pane;
	}
	
//	/**
//	 * 设置默认窗口
//	 */
//	private void setDefautBounds() {
//		Dimension d = PlatformKit.getPlatformDesktop().getSize();
//		int w = d.width / 2;
//		int h = d.height / 2;
//		int x = (d.width - w) / 2;
//		int y = (d.height - h) / 2;
//		Rectangle r = new Rectangle(x, y, w, h);
//		setBounds(r);
//	}
	
	/**
	 * 设置默认窗口
	 */
	private void setDefautBounds() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) ((double) d.width * 0.618);
		int h = (int) ((double) d.height * 0.618);
		int x = (d.width - w) / 2;
		int y = (d.height - h) / 2;
		Rectangle r = new Rectangle(x, y, w, h);
		setBounds(r);
	}
	
	private void writeBounds() {
		Rectangle rect = getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "HelpFrame/Bound", rect);
	}
	
	private Rectangle readBounds() {
		return RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "HelpFrame/Bound");
	}
	
	/**
	 * 显示窗口
	 * @param title
	 * @param image
	 */
	public void showWindow() {
		Rectangle rect = readBounds();
		if (rect != null) {
			this.setBounds(rect);
		} else {
			setDefautBounds();
		}
		
		// 设置面板
		setContentPane(initPane());

		// 图标和标题
		setTitle(UIManager.getString("HelpFrame.Title"));
		setFrameIcon(UIManager.getIcon("HelpFrame.TitleIcon"));
		setFrameBigIcon(UIManager.getIcon("HelpFrame.TitleBigIcon"));
		
//		setFrameIcon(findImage("conf/desktop/image/frame/help.png", 16, 16)); 
//		setTitle(findCaption("Window/Frame/Help/title"));
		
//		HelpFrame.Title 运行日志
//		HelpFrame.TitleIcon [ICON 16*16] conf/desktop/image/frame/help/help.png
//		HelpFrame.ExitTitle 	退出 - 帮助
//		HelpFrame.ExitContent 您确定退出系统桌面的帮助窗口 ？	
		
		// 内部事件
		addInternalFrameListener(new WindowCloseAdapter());
		
//		// 加载成功
//		DesktopHelpFrame.setLoad(true);
		
		// 最小化
		setIconifiable(true);
		// 可关闭
		setClosable(true);
		// 可改变大小
		setResizable(true);
		// 最大化
		setMaximizable(true);
		
		// 销毁窗口
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		DesktopHelpFrame.selfHandle = this;
		
		// 显示
		setVisible(true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.frame.LightFrame#releaseBefore()
	 */
	@Override
	protected void release0() {
		// 保存位置
		writeBounds();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.frame.LightFrame#releaseAfter()
	 */
	@Override
	protected void release1() {
		
		DesktopHelpFrame.selfHandle = null;
	}

}