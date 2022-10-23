/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.tip.*;
import com.laxcus.watch.*;
import com.laxcus.watch.window.*;

/**
 * WATCH站点主操作面板 <br>
 * 位于主窗口的右侧，包括输入命令和显示命令结果
 * 
 * @author scott.liang
 * @version 1.12 8/12/2013
 * @since laxcus 1.0
 */
public class WatchRightPanel extends JPanel implements WatchCommandAuditor {

	private static final long serialVersionUID = -5434244261386896064L;

	/** 命令输入面板 **/
	private WatchCommandPane inputter = new WatchCommandPane();

	/** 选项卡面板，位于底部 **/
	private WatchMixedPanel mixed = new WatchMixedPanel();

	/** 命令转发器 **/
	private WatchCommandDispatcher dispatcher = new WatchCommandDispatcher();

	/**
	 * 构造WATCH站点主面板
	 */
	public WatchRightPanel() {
		super();
		// 设置命令核准接口
		dispatcher.setCommandAuditor(this);
		// 设置显示面板
		dispatcher.setDisplay(mixed);
	}
	
	/**
	 * 返回面板的分割位置
	 * @return 返回大于0的正整数，否则是-1
	 */
	public int getDividerLocation() {
		int count = getComponentCount();
		if (count > 0) {
			Component sub = getComponent(0);
			if (sub.getClass() == JSplitPane.class) {
				JSplitPane pane = (JSplitPane) sub;
				int location = pane.getDividerLocation();
				return location;
			}
		}
		return -1;
	}

	/**
	 * 从指定的JAR配置文档中增加命令标记
	 * @param name JAR档案文件名，以“/”为分隔符。
	 */
	public void addCommandTokens(String name) {
		inputter.loadArchive(name);
	}

	/**
	 * 混合面板
	 * @return
	 */
	public WatchMixedPanel getMixPanel() {
		return mixed;
	}

	/**
	 * 返回命令面板
	 * @return
	 */
	public WatchCommandPane getCommandPanel() {
		return inputter;
	}

	/**
	 * 返回消息提示面板
	 * @return
	 */
	public WatchMixedMessagePanel getMessagePanel() {
		return mixed.getMessagePanel();
	}

	/**
	 * 返回表格面板
	 * @return
	 */
	public WatchMixedTablePanel getTablePanel() {
		return mixed.getTablePanel();
	}

	/**
	 * 返回节点运行时面板
	 * @return
	 */
	public WatchMixedRuntimePanel getRuntimePanel() {
		return mixed.getRuntimePanel();
	}

	/**
	 * 返回日志面板
	 * @return
	 */
	public WatchMixedLogPanel getLogPanel() {
		return mixed.getLogPanel();
	}

//	/**
//	 * 格式发布参数（可以文件名或者是目录）
//	 * @param files 文件或者目录
//	 * @return 合并后的字符串
//	 */
//	private String format(File[] files) {
//		StringBuilder buf = new StringBuilder();
//		for (int i = 0; i < files.length; i++) {
//			if (i > 0) buf.append(" , ");
//			buf.append(files[i].getAbsolutePath());
//		}
//		return buf.toString();
//	}

//	/**
//	 * 发布分布任务组件
//	 * @param files 文件数组
//	 */
//	public void doPublishMultiTask(File[] files) {
//		String syntax = "PUBLISH TASK " + format(files);
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//		dispatcher.runPublishMultiTask(syntax);
//	}
//
//	/**
//	 * 发布码位计算器
//	 * @param files 文件数组
//	 */
//	public void doPublishMultiScaler(File[] files) {
//		String syntax = "PUBLISH SCALER " + format(files);
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//
//		dispatcher.runPublishMultiScaler(syntax);
//	}
//
//	/**
//	 * 发布快捷组件
//	 * @param files 文件数组
//	 */
//	public void doPublishMultiSwift(File[] files) {
//		String syntax = "PUBLISH SWIFT " + format(files);
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//
//		dispatcher.runPublishMultiSwift(syntax);
//	}
	

	/**
	 * 格式发布参数（可以文件名或者是目录）
	 * @param file 文件实例
	 * @return 字符串
	 */
	private String canonical(File file) {
		return Laxkit.canonical(file);
	}

//	/**
//	 * 发布系统分布任务组件
//	 * @param syntax 命令语句
//	 */
//	public void doPublishMultiTaskComponent(String syntax) {
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//		dispatcher.runPublishMultiTaskComponent(syntax);
//	}
//
//	/**
//	 * 发布系统分布任务组件的应用附件
//	 * @param syntax 命令语句
//	 */
//	public void doPublishMultiTaskAssistComponent(String syntax) {
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//		dispatcher.runPublishMultiTaskAssistComponent(syntax);
//	}
//
//	/**
//	 * 发布系统分布任务组件的动态链接库
//	 * @param syntax 命令语句
//	 */
//	public void doPublishMultiTaskLibraryComponent(String syntax) {
//		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
//		dispatcher.runPublishMultiTaskLibraryComponent(syntax);
//	}
	
	/**
	 * 部署系统分布应用
	 * @param syntax
	 */
	public void doDeployConductPackage(String syntax) {
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runDeployConductPackage(syntax);
	}

	/**
	 * 部署系统分布应用
	 * @param syntax
	 */
	public void doDeployEstablishPackage(String syntax) {
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runDeployEstablishPackage(syntax);
	}
	
	/**
	 * 部署系统分布应用
	 * @param syntax
	 */
	public void doDeployContactPackage(String syntax) {
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runDeployContactPackage(syntax);
	}
	
	/**
	 * 生成文件的EACH散列码
	 * @param file 磁盘文件
	 */
	public void doEachFile(File file) {
		String syntax = "BUILD EACH " + canonical(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildEach(syntax);
	}

	/**
	 * 生成文件的MD5散列码
	 * @param file 磁盘文件
	 */
	public void doMD5File(File file) {
		String syntax = "BUILD MD5 " + canonical(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA1散列码
	 * @param file 磁盘文件
	 */
	public void doSHA1File(File file) {
		String syntax = "BUILD SHA1 " + canonical(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA256散列码
	 * @param file 磁盘文件
	 */
	public void doSHA256File(File file) {
		String syntax = "BUILD SHA256 " + canonical(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA512散列码
	 * @param file 磁盘文件
	 */
	public void doSHA512File(File file) {
		String syntax = "BUILD SHA512 " + canonical(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.watch.window.WatchCommandAuditor#confirm()
	 */
	@Override
	public boolean confirm() {
		return confirm(null, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.watch.window.WatchCommandAuditor#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String title) {
		return confirm(title, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.watch.window.WatchCommandAuditor#confirm(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean confirm(String title, String content) {
		if (title == null || title.trim().isEmpty()) {
			title = WatchLauncher.getInstance().findCaption(
					"MessageBox/CommandAuditor/Title/title");
		}
		if (content == null || content.trim().isEmpty()) {
			content = WatchLauncher.getInstance().findCaption(
					"MessageBox/CommandAuditor/Message/title");
		}

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/mail.png", 32, 32);
		
		Frame frame = WatchLauncher.getInstance().getWindow();
		int who = MessageDialog.showMessageBox(frame, title,
				JOptionPane.QUESTION_MESSAGE, icon, content, JOptionPane.YES_NO_OPTION);
		
		return (who == JOptionPane.YES_OPTION);
	}

//	/* (non-Javadoc)
//	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//	 */
//	@Override
//	public void actionPerformed(ActionEvent e) {
//
//	}

//	/**
//	 * 建立命令输入窗口
//	 * @return
//	 */
//	private JScrollPane createCommandPane() {
//		// 初始化
//		inputter.init();
//		
//		// 提示
//		String tooltip = WatchLauncher.getInstance().findCaption("Window/CommandPanel/title");
//		FontKit.setToolTipText(inputter, tooltip);
//		inputter.setFocusAccelerator('S');
//		// order: top, left, bottom, right
//		inputter.setBorder(new EmptyBorder(5, 3, 5, 3));
//		// TAB制表符，2个空格
//		inputter.setTabSize(2);
//		
//		JScrollPane pane = new JScrollPane(inputter);
//		
//		// 或者保持最小高度
//		pane.setMinimumSize(new Dimension(10, 60));
//
//		return pane;
//	}
	
	/**
	 * 建立命令输入窗口
	 * @return
	 */
	private JPanel createCommandPane() {
		// 初始化
		inputter.init();
		
		// 提示
		String tooltip = WatchLauncher.getInstance().findCaption("Window/CommandPanel/title");
		FontKit.setToolTipText(inputter, tooltip);
		inputter.setFocusAccelerator('S');
		// TAB制表符，2个空格
		inputter.setTabSize(2);
		
		// order: top, left, bottom, right
//		inputter.setBorder(new EmptyBorder(5, 3, 5, 3));
		
		
		JScrollPane jsp = new JScrollPane(inputter);
		// 或者保持最小高度
		jsp.setMinimumSize(new Dimension(10, 60));
		
		// 放在面板里
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jsp, BorderLayout.CENTER);
		panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return panel;
	}
	
//	/**
//	 * 建立命令输入窗口
//	 * @return
//	 */
//	private JScrollPane createCommandPane() {
//		// 初始化
//		inputter.init();
//		
//		// 提示
//		String tooltip = WatchLauncher.getInstance().findCaption("Window/CommandPanel/title");
//		FontKit.setToolTipText(inputter, tooltip);
//		inputter.setFocusAccelerator('S');
////		// order: top, left, bottom, right
////		inputter.setBorder(new EmptyBorder(5, 3, 5, 3));
//		// TAB制表符，2个空格
//		inputter.setTabSize(2);
//		
//		JScrollPane pane = new JScrollPane(inputter);
//		
//		// 或者保持最小高度
//		pane.setMinimumSize(new Dimension(10, 60));
//
//		// order: top, left, bottom, right
//		inputter.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
////		pane.setBorder(BorderFactory.createEmptyBorder());
//		pane.setBorder(new javax.swing.plaf.metal.MetalBorders.ScrollPaneBorder());
//		
//		Border b = pane.getBorder();
//		if(b!=null){
//		System.out.printf("scroll pane border class is %s\n", b.getClass().getName());
//		}
//		
//		return pane;
//	}
	
	/**
	 * 建立窗口
	 * @param rect 窗口范围
	 */
	public void init(Rectangle rect) {
		// 初始化命令窗口
		JComponent north = createCommandPane();
		
		// 下面的选择窗口
		mixed.init();
		
		// 切分面板
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, north, mixed);
		pane.setContinuousLayout(true);
		pane.setOneTouchExpandable(false);
		pane.setDividerSize(4); // 间隔条用4个像素
		// 分割位置
		Integer pixel = WatchProperties.readCenterPaneDeviderLocation();
		if (pixel != null && pixel.intValue() > 0) {
			pane.setDividerLocation(pixel.intValue());
		} else {
			// 默认的范围
			int height = rect.height / 4;
			if (height < 100) height = 100;
			pane.setDividerLocation(height);
		}
		
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 快捷键
		initTextKey();
		
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}
	
//	private void initTextKey() {
//		// 检测
//		inputter.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.SHIFT_DOWN_MASK, true), "SHIFT F4");
//		inputter.getActionMap().put("SHIFT F4", new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				check();
//			}
//		});
//		
//		// 执行
//		inputter.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK, true), "SHIFT F5");
//		inputter.getActionMap().put("SHIFT F5", new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				execute();
//			}
//		});
//	}
	
	/**
	 * 显示菜单栏
	 * @return 返回真或者假
	 */
	private boolean isShowMenubar() {
		return WatchLauncher.getInstance().getWindow().isShowMenubar();
	}

	/**
	 * 执行触发判断
	 */
	private void initTextKey() {
		// 检测
		inputter.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, true), "F4");
		inputter.getActionMap().put("F4", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				// 如果显示菜单，忽略它
				if (isShowMenubar()) {
					return;
				}
				check();
			}
		});
		
		// 执行
		inputter.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true), "F5");
		inputter.getActionMap().put("F5", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				// 如果显示菜单，这个触发可以忽略
				if (isShowMenubar()) {
					return;
				}
				execute();
			}
		});
	}
	
	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 打开帮助对话框
	 */
	private void doHelp() {
		WatchLauncher.getInstance().getWindow().doHelp();
	}
	

	/**
	 * 检查命令
	 */
	private void doCheckCommand() {
		// 判断命令是空值
		String input = inputter.getText().trim();
		if (input.length() == 0) {
			return;
		}

		// 如果是帮助在本地显示
		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
			String text = WatchLauncher.getInstance().message(MessageTip.CORRECT_SYNTAX);
			mixed.message(text, false);
			return;
		}

		// 转给代理检查命令
		dispatcher.check(input);
	}

	/**
	 * 启动命令
	 */
	private void doLaunchCommand() {
		String input = inputter.getText().trim();
		// 空值不处理
		if (input.length() == 0) {
			return;
		}

		// 打印帮助信息
		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
			addThread(new HelpThread());
			return;
		}

		// 提交和执行命令
		dispatcher.submit(input);
	}
	
	class HelpThread extends SwingEvent {
		HelpThread(){ super(); }
		public void process() {
			doHelp();
		}
	}

	class CheckCommandThread extends SwingEvent {
		CheckCommandThread() { super(); }
		public void process() {
			doCheckCommand();
		}
	}

	class LaunchCommandThread extends SwingEvent {
		LaunchCommandThread() { super(); }
		public void process() {
			doLaunchCommand();
		}
	}

	/**
	 * 检查命令语法格式
	 */
	public void check() {
		addThread(new CheckCommandThread());
	}

	/**
	 * 启动命令
	 */
	public void execute() {
		addThread(new LaunchCommandThread());
	}

	/**
	 * 清除全部文本
	 */
	public void clear() {
		inputter.clear();
		mixed.clear();
	}
	
	/**
	 * 触发焦点到命令文本栏
	 *
	 * @author scott.liang
	 * @version 1.0 1/23/2020
	 * @since laxcus 1.0
	 */
	class FocusCommand extends SwingEvent {
		FocusCommand() { super(); }
		public void process() {
			// 如果没有获得焦点！
			if (!inputter.hasFocus()) {
				inputter.requestFocus();
			}
		}
	}
	
	/**
	 * 向命令窗口移动焦点
	 */
	public void focusCommand() {
		addThread(new FocusCommand());
	}
}