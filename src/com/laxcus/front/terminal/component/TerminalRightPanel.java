/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.command.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.establish.*;
import com.laxcus.front.meet.*;
import com.laxcus.front.terminal.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.tip.*;

/**
 * 终端操作窗口 <br>
 * 
 * 位于右侧，终端界面的主操作窗口，用来输入命令和显示结果。
 * 
 * @author scott.liang
 * @version 1.5 12/3/2013
 * @since laxcus 1.0
 */
public class TerminalRightPanel extends JPanel implements /*ActionListener,*/ MeetCommandAuditor {

	private static final long serialVersionUID = -2887894579862229607L;

	/** 命令输入面板 **/
	private TerminalCommandPane inputter = new TerminalCommandPane();

	/** 选项卡面板 **/
	private TerminalMixedPanel selector = new TerminalMixedPanel();

	/** 命令分派器 **/
	private MeetCommandDispatcher dispatcher = new MeetCommandDispatcher();

	/**
	 * 从指定的JAR配置文档中增加命令标记
	 * @param xmlPath JAR档案文件名，以“/”为分隔符。
	 */
	public void addCommandTokens(String xmlPath) {
		inputter.loadArchive(xmlPath);
	}

	/**
	 * 构造右侧面板
	 */
	public TerminalRightPanel() {
		super();
		dispatcher.setCommandAuditor(this);
		dispatcher.setDisplay(selector);
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
	 * 选项卡界面
	 * @return
	 */
	public TerminalMixedPanel getMixedPanel() {
		return selector;
	}

	/**
	 * 返回命令窗口
	 * @return
	 */
	public TerminalCommandPane getCommandPanel() {
		return inputter;
	}

	/**
	 * 返回提示面板
	 * @return
	 */
	public TerminalMessagePanel getMessagePanel() {
		return selector.getMessagePanel();
	}

	/**
	 * 返回表格面板
	 * @return
	 */
	public TerminalTablePanel getTablePanel() {
		return selector.getTablePanel();
	}

	/**
	 * 返回图形面板
	 * @return
	 */
	public TerminalGraphPanel getGraphPanel() {
		return selector.getGraphPanel();
	}

	/**
	 * 返回日志面板
	 * @return
	 */
	public TerminalLogPanel getLogPanel() {
		return selector.getLogPanel();
	}

	/**
	 * 返回选项页面板
	 * @return
	 */
	public TerminalMixedPanel getTabPanel() {
		return selector;
	}

//	/* (non-Javadoc)
//	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//	 */
//	@Override
//	public void actionPerformed(ActionEvent e) {
//
//	}

	/**
	 * 清除提示信息
	 */
	public void doClearNote() {
		selector.clearPrompt();
	}

	/**
	 * 清除执行结果记录
	 */
	public void doClearTable() {
		selector.clearShowItems();
	}

	/**
	 * 清除全部图形记录
	 */
	public void doClearGraph() {
		selector.clearGraph();
	}

	/**
	 * 清除全部日志记录
	 */
	public void doClearLog() {
		selector.clearLog();
	}

	/**
	 * 清除全部显示
	 */
	public void clear() {
		// 清除命令窗口的显示
		inputter.clear();
		// 清除下面的窗口显示
		doClearNote();
		doClearTable();
		doClearGraph();
		doClearLog();
	}

	/**
	 * 格式发布参数（可以文件名或者是目录）
	 * @param file 文件实例
	 * @return 字符串
	 */
	private String format(File file) {
		return file.getAbsolutePath();
	}

	/**
	 * 上传数据文件
	 * @param syntax 命令语句
	 */
	public void doImportEntity(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runImportEntity(syntax);
	}

	/**
	 * 下载数据块
	 * @param syntax 命令语句
	 */
	public void doExportEntity(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runExportEntity(syntax);
	}

	/**
	 * 检测文件字符集
	 * @param syntax 命令语句
	 */
	public void doCheckEntityCharset(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runCheckEntityCharset(syntax);
	}

	/**
	 * 检测数据内容
	 * @param syntax 命令语句
	 */
	public void doCheckEntityContent(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runCheckEntityContent(syntax);
	}
	
	/**
	 * 启动边缘应用
	 * @param syntax 输入语句
	 */
	public void doRunTubService(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runRunTubService(syntax);
	}
	
	/**
	 * 停止边缘应用
	 * @param syntax 输入语句
	 */
	public void doStopTubService(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runStopTubService(syntax);
	}

	/**
	 * 显示运行中的应用
	 * @param syntax 输入语句
	 */
	public void doPrintTubService(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runPrintTubService(syntax);
	}

	/**
	 * 显示边缘应用，无论是否运行
	 * @param syntax 输入语句
	 */
	public void doShowTubContainer(String syntax) {
		inputter.replaceText(syntax);
		dispatcher.runShowTubContainer(syntax);
	}

//	/**
//	 * 发布分布任务组件
//	 * @param path 文件实例
//	 */
//	public void doPublishTaskComponent(String input) {
//		inputter.replaceText(input); // 在命令窗口显示新的命令文本
//		dispatcher.runPublishTaskComponent(input);
//	}
//
//	/**
//	 * 发布分布任务组件附件
//	 * @param input 输入语句
//	 */
//	public void doPublishTaskAssistComponent(String input) {
//		inputter.replaceText(input); // 在命令窗口显示新的命令文本
//		dispatcher.runPublishTaskAssistComponent(input);
//	}
//	/**
//	 * 发布分布任务组件动态链接库
//	 * @param input 输入语句
//	 */
//	public void doPublishTaskLibraryComponent(String input) {
//		inputter.replaceText(input); // 在命令窗口显示新的命令文本
//		dispatcher.runPublishTaskLibraryComponent(input);
//	}
	
	/**
	 * 生成分布计算云计算应用包
	 * @param input 输入语句
	 */
	public void doBuildConductPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runBuildConductPackage(input);
	}

	/**
	 * 发布分布计算云计算应用包
	 * @param input 输入语句
	 */
	public void doDeployConductPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runDeployConductPackage(input);
	}
	
	/**
	 * 删除分布计算云计算应用包
	 * @param input 输入语句
	 */
	public void doDropConductPackage(String input, boolean show) {
		if(show) inputter.replaceText(input);
		dispatcher.runDropConductPackage(input);
	}
	
	/**
	 * 生成分布数据构建应用包
	 * @param input 输入语句
	 */
	public void doBuildEstablishPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runBuildEstablishPackage(input);
	}

	/**
	 * 发布分布数据构建应用包
	 * @param input 输入语句
	 */
	public void doDeployEstablishPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runDeployEstablishPackage(input);
	}
	
	/**
	 * 删除数据构建云计算应用包
	 * @param input 输入语句
	 */
	public void doDropEstablishPackage(String input, boolean show) {
		if(show) inputter.replaceText(input);
		dispatcher.runDropEstablishPackage(input);
	}

	/**
	 * 生成快捷计算应用包
	 * @param input 输入语句
	 */
	public void doBuildContactPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runBuildContactPackage(input);
	}

	/**
	 * 发布快捷计算应用包
	 * @param input 输入语句
	 */
	public void doDeployContactPackage(String input) {
		inputter.replaceText(input);
		dispatcher.runDeployContactPackage(input);
	}
	
	/**
	 * 删除迭代计算应用包
	 * @param input 输入语句
	 */
	public void doDropContactPackage(String input, boolean show) {
		if(show) inputter.replaceText(input);
		dispatcher.runDropContactPackage(input);
	}

	/**
	 * 运行分布计算
	 * @param cmd
	 */
	public boolean runConduct(Conduct cmd) {
		return dispatcher.runConduct(cmd);
	}

	/**
	 * 运行构建分布数据
	 * @param cmd
	 */
	public boolean runEstablish(Establish cmd) {
		return dispatcher.runEstablish(cmd);
	}
	
	/**
	 * 运行迭代计算
	 * @param cmd
	 */
	public boolean runContact(Contact cmd) {
		return dispatcher.runContact(cmd);
	}
	
	/**
	 * 生成文件的EACH散列码
	 * @param file 磁盘文件
	 */
	public void doEachFile(File file) {
		String syntax = "BUILD EACH " + format(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildEach(syntax);
	}

	/**
	 * 更新网络数据。即清除旧记录，从网络上获取新的记录。
	 */
	public boolean doRefreshCyber() {
		return dispatcher.runRefreshCyber();
	}

	/**
	 * 生成文件的MD5散列码
	 * @param file 磁盘文件
	 */
	public void doMD5File(File file) {
		String syntax = "BUILD MD5 " + format(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA1散列码
	 * @param file 磁盘文件
	 */
	public void doSHA1File(File file) {
		String syntax = "BUILD SHA1 " + format(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA256散列码
	 * @param file 磁盘文件
	 */
	public void doSHA256File(File file) {
		String syntax = "BUILD SHA256 " + format(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/**
	 * 生成文件的SHA512散列码
	 * @param file 磁盘文件
	 */
	public void doSHA512File(File file) {
		String syntax = "BUILD SHA512 " + format(file);
		inputter.replaceText(syntax); // 显示在图形窗口的命令栏
		dispatcher.runBuildHash(syntax);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.interactive.FrontCommandAuditor#confirm()
	 */
	@Override
	public boolean confirm() {
		return confirm(null, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String title) {
		return confirm(title, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#confirm(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean confirm(String title, String content) {
		// 空值用默认
		if (title == null || title.trim().isEmpty()) {
			title = TerminalLauncher.getInstance().findCaption(
			"MessageBox/CommandAuditor/Title/title");
		}
		if (content == null || content.trim().isEmpty()) {
			content = TerminalLauncher.getInstance().findCaption(
			"MessageBox/CommandAuditor/Message/title");
		}

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage(
				"conf/front/terminal/image/message/mail.png", 32, 32);

		Frame frame = TerminalLauncher.getInstance().getWindow();
		int who = MessageDialog.showMessageBox(frame, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);

		return (who == JOptionPane.YES_OPTION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#shift(com.laxcus.command.Command)
	 */
	@Override
	public void shift(Command cmd) {
		if (cmd.getClass() == RunTask.class) {
			RunTask sub = (RunTask) cmd;
			// 转发命令
			TerminalWindow window = TerminalLauncher.getInstance().getWindow();
			TerminalRemoteSoftwareListPanel panel = window.getRemoteSoftwareListPanel();
			panel.shift(sub);
		}
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 打开帮助窗口
	 */
	private void doHelp() {
		TerminalLauncher.getInstance().getWindow().doHelp();
	}

	/**
	 * 检查终端输入的命令
	 */
	private void doCheck() {
		String input = inputter.getText().trim();
		if (input.length() == 0) {
			return;
		}
		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
			String text = TerminalLauncher.getInstance().message(MessageTip.CORRECT_SYNTAX);
			selector.message(text, true);
			return;
		}
		dispatcher.check(input);
	}

	/**
	 * 执行操作
	 */
	private void doExecute() {
		String input = inputter.getText().trim();
		if (input.length() == 0) {
			return;
		}

		// 打开帮助窗口
		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
			addThread(new HelpThread());
			return;
		}

		dispatcher.submit(input);
	}

	class HelpThread extends SwingEvent {
		HelpThread(){super();}
		public void process() {
			doHelp();
		}
	}

	class CheckThread extends SwingEvent {
		CheckThread() { super(); }
		public void process() {
			doCheck();
		}
	}

	class ExecuteThread extends SwingEvent {
		ExecuteThread() { super(); }
		public void process() {
			doExecute();
		}
	}

	/**
	 * 检查终端输入的命令
	 */
	public void check() {
		//		String input = inputter.getText().trim();
		//		if (input.length() == 0) {
		//			return;
		//		}
		//
		//		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
		//			String text = TerminalLauncher.getInstance().message(MessageTip.CORRECT_SYNTAX);
		//			selector.message(text);
		//			return;
		//		}
		//
		//		dispatcher.check(input);

		addThread(new CheckThread());
	}

	/**
	 * 执行操作
	 */
	public void execute() {
		//		String input = inputter.getText().trim();
		//		if (input.length() == 0) return;
		//
		//		// 打开帮助窗口
		//		if (input.matches("^\\s*(?i)(HELP)\\s*$")) {
		////			FrontStreamAdapter invoker = new FrontStreamAdapter();
		////			String s = invoker.help();
		////			selector.message(s);
		//			
		//			TerminalLauncher.getInstance().getFrame().doHelp();
		//			
		//			return;
		//		}
		//
		//		dispatcher.submit(input);

		addThread(new ExecuteThread());
	}

	/**
	 * 建立窗口
	 * @param 窗口范围
	 */
	public void init(Rectangle rect) {
		JComponent north = createCommandPane();
		selector.init();
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, north, selector);
		pane.setDividerSize(4); // 间隔条用4个像素
		pane.setContinuousLayout(true);
		pane.setOneTouchExpandable(false);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		// 确定位置！
		Integer pixel = TerminalProperties.readCenterPaneDeviderLocation();
		if (pixel != null && pixel.intValue() > 0) {
			pane.setDividerLocation(pixel.intValue());
		} else {
			int height = rect.height / 4;
			if (height < 100) height = 100;
			pane.setDividerLocation(height);
		}
		
		initTextKey();
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}
	
//	/**
//	 * 快捷键执行
//	 */
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
		return TerminalLauncher.getInstance().getWindow().isShowMenubar();
	}

	/**
	 * 快捷键执行
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
	 * 建立命令输入窗口
	 * @return
	 */
	private JPanel createCommandPane() {
		// 初始化
		inputter.init();

		String title = TerminalLauncher.getInstance().findCaption("Window/CommandPanel/title");
		inputter.setToolTipText(title);
		inputter.setFocusAccelerator('S');
		// TAB制表符，2个空格
		inputter.setTabSize(2);
		
		JScrollPane jsp = new JScrollPane(inputter);
		// 保证最低高度
		jsp.setMinimumSize(new Dimension(10, 80));
		// 放在面板里
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jsp, BorderLayout.CENTER);
		panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		return panel;
	}
	
	/**
	 * 触发焦点到命令文本栏
	 *
	 * @author scott.liang
	 * @version 1.0 1/25/2020
	 * @since laxcus 1.0
	 */
	class FocusCommand extends SwingEvent {
		FocusCommand(){ super(); }
		public void process() {
			// 如果没有获得焦点，转向它
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