/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * LAXCUS启动/停止脚本文件生成器
 * 
 * @author scott.liang
 * @version 1.0 5/24/2020
 * @since laxcus 1.0
 */
public class WatchCreateRunStopScriptDialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -1294400416652848215L;

	/** 属性，保存到UIManager的KEY值 **/
	private final static String KEY_PATH = WatchCreateRunStopScriptDialog.class.getSimpleName() + "_PATH"; 
	
	private final static String KEY_RECT = WatchCreateRunStopScriptDialog.class.getSimpleName() + "_BOUND"; 
	
	/** 保存脚本文件的目录 **/
	private JButton cmdExport = new JButton();
	
	/** 保存脚本 **/
	private JLabel lblExport = new JLabel();
	
	/** 生成脚本 **/
	private JButton cmdCreate = new JButton();

	/** 退同窗口 **/
	private JButton cmdExit = new JButton();

	/** 重置全部参数 **/
	private JButton cmdReset = new JButton();
	
	/* 全部选中 **/
	private JCheckBox cmdAll = new JCheckBox();
	
	/** 以下是标签和对应的目录 **/
	
	// 回显目录
	private JTextField txtEcho = new JTextField();

	// 日志目录
	private JTextField txtLog = new JTextField();
	
	// TIG目录
	private JTextField txtTig = new JTextField();
	
	// 资源目录
	private JTextField txtResource = new JTextField();

	// 数据存储目录
	private JTextField txtStore = new JTextField();

	// 分布计算的中间数据目录
	private JTextField txtMiddle = new JTextField();

	// 应用软件部署目录
	private JTextField txtDeploy = new JTextField();
	
	// 启动间隔时间
	private JTextField txtStartInterval = new JTextField();

	// 启动间隔时间
	private JTextField txtStopInterval = new JTextField();
	
	/** 以下是节点按纽 **/
	
	// TOP集群
	private JCheckBox chkTop = new JCheckBox();
	private JCheckBox chkTop1 = new JCheckBox();
	private JCheckBox chkTop2 = new JCheckBox();
	private JCheckBox chkTopLog = new JCheckBox();
	
	// BANK集群
	private JCheckBox chkBank = new JCheckBox();
	private JCheckBox chkBank1 = new JCheckBox();
	private JCheckBox chkBank2 = new JCheckBox();
	private JCheckBox chkBankLog = new JCheckBox();
	private JCheckBox chkAccount = new JCheckBox();
	private JCheckBox chkHash = new JCheckBox();
	private JCheckBox chkGate = new JCheckBox();
	private JCheckBox chkEntrance = new JCheckBox();
	
	// HOME集群
	private JCheckBox chkHome = new JCheckBox();
	private JCheckBox chkHome1 = new JCheckBox();
	private JCheckBox chkHome2 = new JCheckBox();
	private JCheckBox chkHomeLog = new JCheckBox();
	private JCheckBox chkDataMaster = new JCheckBox();
	private JCheckBox chkDataSlave = new JCheckBox();
	private JCheckBox chkBuild = new JCheckBox();
	private JCheckBox chkWork = new JCheckBox();
	private JCheckBox chkCall = new JCheckBox();
	
	/** 最后一次的目录 **/
	private File root;
	
	/**
	 * @param frame
	 * @param modal
	 */
	public WatchCreateRunStopScriptDialog(Frame parent, boolean modal) {
		super(parent, modal);
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;

		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}

	/**
	 * 触发操作
	 * 
	 * @param e 激活事件
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdReset) {
			reset();
		} else if (e.getSource() == cmdExit) {
			// 关闭
			boolean success = exit();
			if (success) {
				save();
				dispose();
			}
		} else if (e.getSource() == cmdExport) {
			// 选中目录
			choice();
		} else if (e.getSource() == cmdCreate) {
			// 检查和关闭它！
			boolean success = check();
			if (success) {
				success = confirm();
			}
			if (success) {
				// 生成
				create();
				// 保存选中的目录
				save();
			}
		} else if(e.getSource() == cmdAll) {
			all();
		} else {
			// 执行站点按纽操作
			todo();
		}
	}
	
	/**
	 * 执行分析，打开/关闭文本框
	 */
	private void todo() {
		int echo = 0;
		int log = 0;
		int tig = 0;
		int resource = 0;
		int store = 0;
		
		int middle = 0;
		int deploy = 0;

		// TOP 集群
		if (chkTop.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkTop1.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkTop2.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkTopLog.isSelected()) {
			echo++;
			log++;
			tig++;
		}
		
		// BANK集群
		if (chkBank.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkBank1.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkBank2.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkBankLog.isSelected()) {
			echo++;
			log++;
			tig++;
		}
		if (chkAccount.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkHash.isSelected()) {
			echo++;
			log++;
			tig++;
		}
		if (chkGate.isSelected()) {
			echo++;
			log++;
			tig++;
		}
		if (chkEntrance.isSelected()) {
			echo++;
			log++;
			tig++;
		}

		// HOME集群
		if (chkHome.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkHome1.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkHome2.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
		}
		if (chkHomeLog.isSelected()) {
			echo++;
			log++;
			tig++;
		}
		if (chkDataMaster.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
			deploy++;
			middle++;
			store++;
		}
		if (chkDataSlave.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
			deploy++;
			middle++;
			store++;
		}
		if (chkBuild.isSelected()) {
			echo++;
			log++;
			tig++;
			resource++;
			deploy++;
			middle++;
		}
		if (chkWork.isSelected()) {
			echo++;
			log++;
			tig++;

			deploy++;
			middle++;
		}
		if (chkCall.isSelected()) {
			echo++;
			log++;
			tig++;

			deploy++;
			middle++;
		}

		// 打开/关闭文本框
		txtEcho.setEnabled(echo > 0);
		txtLog.setEnabled(log > 0);
		txtTig.setEnabled(tig > 0);
		txtResource.setEnabled(resource > 0);
		
		// HOME集群专属
		txtMiddle.setEnabled(middle > 0);
		txtDeploy.setEnabled(deploy > 0);
		txtStore.setEnabled(store > 0);
	}
	
	/**
	 * 选中全部
	 */
	private void all() {
		boolean yes = cmdAll.isSelected();
		
		// 给选择按纽设置事件
		JCheckBox[] boxes = new JCheckBox[] { chkTop, chkTop1, chkTop2, chkTopLog, 
				chkBank, chkBank1, chkBank2, chkBankLog, 
				chkAccount, chkHash, chkGate, chkEntrance, 
				chkHome, chkHome1, chkHome2, chkHomeLog, 
				chkDataMaster, chkDataSlave, chkBuild, chkWork, chkCall };
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].setSelected(yes);
		}
		
		JTextField[] fields = new JTextField[] { txtEcho, txtLog, txtTig, txtResource, txtStore,
				txtMiddle, txtDeploy };
		for (int i = 0; i < fields.length; i++) {
			fields[i].setEnabled(yes);
		}
	}
	
	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private void reset() {
		cmdAll.setSelected(false);
		lblExport.setText("");
		
		// 清空
		JTextField[] fields = new JTextField[] { txtEcho, txtLog, txtTig, txtResource, txtStore,
				txtMiddle, txtDeploy };
		for (int i = 0; i < fields.length; i++) {
			fields[i].setText("");
		}
		all();
	}
	
	/**
	 * 判断文件框无效！
	 * 两个条件：1. 文本有效，2.没有输入文字
	 * @param sub
	 * @return
	 */
	private boolean isDisable(JTextField sub) {
		String text = sub.getText();
		return sub.isEnabled() && text.trim().isEmpty();
	}
	
	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private boolean check() {
		if(isDisable(txtEcho)) {
			txtEcho.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtLog)) {
			txtLog.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtTig)) {
			txtTig.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtResource)) {
			txtResource.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtStore)) {
			txtStore.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtMiddle)) {
			txtMiddle.requestFocus();
			warning();
			return false;
		}
		if(isDisable(txtDeploy)) {
			txtDeploy.requestFocus();
			warning();
			return false;
		}
		
		// 判断写入目录
		String text = lblExport.getText();
		if (text.trim().isEmpty()) {
			cmdExport.requestFocus();
			warning();
			return false;
		}
		
		return true;
	}

	/**
	 * 生成脚本文件
	 * @return 成功返回真，否则假
	 */
	private boolean create() {
		// 顺序
		boolean success = sequenceCreate();
		if (!success) {
			return false;
		}
		// 倒序
		success = reverseCreate();
		if (!success) {
			return false;
		}

		// 提示完成
		String title = findCaption("Dialog/script/finished/title");
		String sub = findContent("Dialog/script/finished");
		String content = String.format(sub, lblExport.getText());
		MessageDialog.showMessageBox(this, title,
				JOptionPane.INFORMATION_MESSAGE, content,
				JOptionPane.DEFAULT_OPTION);
		return true;
	}
	
	/**
	 * 文件写入磁盘
	 * @param file
	 * @param content
	 * @return 成功返回真，否则假
	 */
	private boolean write(File file, String content) {
		try {
			byte[] b = new UTF8().encode(content);
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			Logger.error(e);
		}
		return false;
	}
	
	/**
	 * 格式一个节点启动单元
	 * @param buff
	 * @param tag
	 */
	private void formatRunElement(StringBuilder buff, String tag) {
		// 间隔20秒启动
		if (buff.length() > 0) {
			String input = txtStartInterval.getText();
			long time = ConfigParser.splitInteger(input, 20);
			buff.append(String.format("\nsleep %ds\n\n", time));
		}

		StringBuilder sub = new StringBuilder();
		sub.append(String.format("# this is %s site\n", tag));
		sub.append(String.format("cd $LAXCUS_ROOT/%s/bin\n", tag));
		sub.append("echo $LAXCUS_INTO`pwd`\n");
		sub.append("./run.sh\n");
		
		// 保存单元信息
		buff.append(sub.toString());
	}
	
	private String formatPath(String tag, JTextField filed) {
		String path = filed.getText().trim();
		// 如果是路径分隔符，过滤掉！
		int len = path.length();
		if (len > 0) {
			char last = path.charAt(len - 1);
			if (last == '/') {
				path = path.substring(0, len - 1);
			}
		}
		return String.format("export %s=\"%s\"\n", tag, path);
	}
	
	/**
	 * 生成运行脚本前缀
	 * @param suffix 后缀信息
	 * @return 字符串
	 */
	private String buildRunScript(String suffix) {
		StringBuilder buff = new StringBuilder();
		
		buff.append("#!/bin/sh\n\n");
		
		buff.append("# laxcus root path \n");
		buff.append("LXRTPATH=`pwd`\n");
		buff.append("source $LXRTPATH/address.sh\n\n");
		
		buff.append("LAXCUS_ROOT=$LXRTPATH\n");
		buff.append("LAXCUS_INTO=\"into \"\n\n");
		
		// 安全沙箱
		buff.append("if [ \"$1\" = \"-sandbox\" ]; then\n");
		buff.append("	export LAXCUS_SANDBOX=\"yes\"\n");
		buff.append("else\n");
		buff.append("	export LAXCUS_SANDBOX=\"no\"\n");
		buff.append("fi\n\n");
		
		// 生成指入目录
		if (txtEcho.isEnabled()) {
			String text = formatPath("LAXCUS_ECHO_ROOT", txtEcho);
			buff.append(text);
		}
		if (txtLog.isEnabled()) {
			String text = formatPath("LAXCUS_LOG_DIR", txtLog);
			buff.append(text);
		}
		if (txtTig.isEnabled()) {
			String text = formatPath("LAXCUS_TIG_DIR", txtTig);
			buff.append(text);
		}
		if (txtResource.isEnabled()) {
			String text = formatPath("LAXCUS_CONFIG_ROOT", txtResource);
			buff.append(text);
		}
		if (txtStore.isEnabled()) {
			String text = formatPath("LAXCUS_STORE_ROOT", txtStore);
			buff.append(text);
		}
		if (txtMiddle.isEnabled()) {
			String text = formatPath("LAXCUS_MIDDLE_ROOT", txtMiddle);
			buff.append(text);
		}
		if (txtDeploy.isEnabled()) {
			String text = formatPath("LAXCUS_DEPLOY_ROOT", txtDeploy);
			buff.append(text);
		}
		
		// 转入根目录
		buff.append("\n\n");
		buff.append("cd /\n");
		buff.append("cd $LAXCUS_ROOT\n\n");
		
		// 写入后缀数据
		buff.append(suffix);
		
		return buff.toString();
	}

	private void formatStopElement(StringBuilder buff, String tag) {
		// 统一间隔10秒启动
		if (buff.length() > 0) {
			String input = txtStopInterval.getText();
			int time = ConfigParser.splitInteger(input, 10);
			buff.append(String.format("\nsleep %ds\n\n", time));
		}

		StringBuilder sub = new StringBuilder();
		sub.append(String.format("# this is %s site\n", tag));
		sub.append(String.format("cd $LAXCUS_ROOT/%s/bin\n", tag));
		sub.append("echo $LAXCUS_INTO`pwd`\n");
		sub.append("./stop.sh\n");
		
		buff.append(sub.toString());
	}
	
	/**
	 * 停止脚本前缀
	 * @param suffix 后缀信息
	 * @return 整个文档
	 */
	private String buildStopScript(String suffix) {
		StringBuilder buff = new StringBuilder();
		
		buff.append("#!/bin/sh\n\n");
		
		buff.append("# laxcus root path \n");
		buff.append("LXRTPATH=`pwd`\n");
		buff.append("source $LXRTPATH/address.sh\n\n"); // 启动地址
		
		buff.append("LAXCUS_ROOT=$LXRTPATH\n");
		buff.append("LAXCUS_INTO='into  '\n\n");
		
		buff.append("cd /\n");
		buff.append("cd $LAXCUS_ROOT\n\n");
		
		//  写入后缀信息
		buff.append(suffix);
		
		return buff.toString();
	}

	/**
	 * 生成运行脚本
	 * @return
	 */
	private boolean sequenceCreate() {
		StringBuilder suffix = new StringBuilder();
		
		// 启动
		if (chkTop.isSelected()) {
			formatRunElement(suffix, "top");
		}
		if (chkTop1.isSelected()) {
			formatRunElement(suffix, "top1");
		}
		if (chkTop2.isSelected()) {
			formatRunElement(suffix, "top2");
		}
		if (chkTopLog.isSelected()) {
			formatRunElement(suffix, "toplog");
		}
		
		// 启动
		if (chkBank.isSelected()) {
			formatRunElement(suffix, "bank");
		}
		if (chkBank1.isSelected()) {
			formatRunElement(suffix, "bank1");
		}
		if (chkBank2.isSelected()) {
			formatRunElement(suffix, "bank2");
		}
		if (chkBankLog.isSelected()) {
			formatRunElement(suffix, "banklog");
		}
		if (chkAccount.isSelected()) {
			formatRunElement(suffix, "account");
		}
		if (chkHash.isSelected()) {
			formatRunElement(suffix, "hash");
		}
		if (chkGate.isSelected()) {
			formatRunElement(suffix, "gate");
		}
		if (chkEntrance.isSelected()) {
			formatRunElement(suffix, "entrance");
		}
		
		// 启动
		if (chkHome.isSelected()) {
			formatRunElement(suffix, "home");
		}
		if (chkHome1.isSelected()) {
			formatRunElement(suffix, "home1");
		}
		if (chkHome2.isSelected()) {
			formatRunElement(suffix, "home2");
		}
		if (chkHomeLog.isSelected()) {
			formatRunElement(suffix, "homelog");
		}
		if (chkDataMaster.isSelected()) {
			formatRunElement(suffix, "data");
		}
		if (chkDataSlave.isSelected()) {
			formatRunElement(suffix, "slave");
		}
		if (chkBuild.isSelected()) {
			formatRunElement(suffix, "build");
		}
		if (chkWork.isSelected()) {
			formatRunElement(suffix, "work");
		}
		if (chkCall.isSelected()) {
			formatRunElement(suffix, "call");
		}
		
		// 前缀内容
		String content = buildRunScript(suffix.toString());

		// 写入磁盘
		File file = createRunOS();
		return write(file, content);
	}
	
	/**
	 * 生成停止脚本文件
	 * @return 返回真或者假
	 */
	private boolean reverseCreate() {
		StringBuilder suffix = new StringBuilder();
		
		// 停止HOME
		if (chkCall.isSelected()) {
			formatStopElement(suffix, "call");
		}
		if (chkWork.isSelected()) {
			formatStopElement(suffix, "work");
		}
		if (chkBuild.isSelected()) {
			formatStopElement(suffix, "build");
		}
		if (chkDataSlave.isSelected()) {
			formatStopElement(suffix, "slave");
		}
		if (chkDataMaster.isSelected()) {
			formatStopElement(suffix, "data");
		}
		if (chkHomeLog.isSelected()) {
			formatStopElement(suffix, "homelog");
		}
		if (chkHome2.isSelected()) {
			formatStopElement(suffix, "home2");
		}
		if (chkHome1.isSelected()) {
			formatStopElement(suffix, "home1");
		}
		if (chkHome.isSelected()) {
			formatStopElement(suffix, "home");
		}
		
		// 停止BANK
		if (chkEntrance.isSelected()) {
			formatStopElement(suffix, "entrance");
		}
		if (chkGate.isSelected()) {
			formatStopElement(suffix, "gate");
		}
		if (chkHash.isSelected()) {
			formatStopElement(suffix, "hash");
		}
		if (chkAccount.isSelected()) {
			formatStopElement(suffix, "account");
		}
		if (chkBankLog.isSelected()) {
			formatStopElement(suffix, "banklog");
		}
		if (chkBank2.isSelected()) {
			formatStopElement(suffix, "bank2");
		}
		if (chkBank1.isSelected()) {
			formatStopElement(suffix, "bank1");
		}
		if (chkBank.isSelected()) {
			formatStopElement(suffix, "bank");
		}
		
		// 停止TOP
		if (chkTopLog.isSelected()) {
			formatStopElement(suffix, "toplog");
		}
		if (chkTop2.isSelected()) {
			formatStopElement(suffix, "top2");
		}
		if (chkTop1.isSelected()) {
			formatStopElement(suffix, "top1");
		}
		if (chkTop.isSelected()) {
			formatStopElement(suffix, "top");
		}
		
		// 整个脚本文件
		String content = buildStopScript(suffix.toString());

		//		// 合并
		//		StringBuilder buff = new StringBuilder();
		//		buff.append(prefix);
		//		buff.append(suffix);

		// 写入磁盘
		File file = createStopOS();
		return write(file, content);
	}

	/**
	 * 保存根目录
	 */
	private void save() {
		if (root != null) {
			UITools.putProperity(KEY_PATH, root.toString());
		}

		Rectangle rect = getBounds();
		Rectangle sub = new Rectangle(rect);
		UITools.putProperity(KEY_RECT, sub);
	}
	
	/**
	 * 打开窗口，选择磁盘目录
	 */
	private void choice() {
		String title = findCaption("Dialog/script/directory/title");
		String buttonText = findCaption("Dialog/script/directory/button");
		
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // .FILES_ONLY);
		chooser.setDialogTitle(title); 
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		if (root != null) {
			chooser.setCurrentDirectory(root);
		}
		// 没有定义，从系统中取
		else {
			Object memory = UITools.getProperity(KEY_PATH);
			if (memory != null && memory.getClass() == String.class) {
				File path = new File((String) memory);
				if (path.exists() && path.isDirectory()) {
					chooser.setCurrentDirectory(path);
				}
			}
		}
		
		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}
		// 判断目录有效，显示它。否则清除
		File dir = chooser.getSelectedFile();
		boolean success = (dir.exists() && dir.isDirectory());
		if (success) {
			root = dir;
			// 保存到集合
			FontKit.setLabelText(lblExport, dir.toString());
		}
	}
	
	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/script/exit/message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 参数警告！
	 */
	private void warning(){
		// 提示错误
		String title = findCaption("Dialog/script/warning/title");
		String content = findContent("Dialog/script/warning");
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 生成启动文件
	 * @return
	 */
	private File createRunOS() {
		String dir = lblExport.getText();
		return new File(dir, "runos.sh");
	}

	/**
	 * 生成停止文件
	 * @return
	 */
	private File createStopOS() {
		String dir = lblExport.getText();
		return new File(dir, "stopos.sh");
	}
	
	/**
	 * 询问覆盖...
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	private boolean override(File file) {
		// 提示错误
		String title = findCaption("Dialog/script/override/title");
		String content = findContent("Dialog/script/override");
		String format = String.format(content, file.toString());
		
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, format,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 确认生成脚本文件
	 * @return 返回真或者假
	 */
	private boolean __confirm() {
		// 提示错误
		String title = findCaption("Dialog/script/confirm/title");
		String content = findContent("Dialog/script/confirm");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}
	
	/**
	 * 确认？
	 * @return
	 */
	private boolean confirm() {
		// 启动文件
		File file = createRunOS();
		boolean exists = (file.exists() && file.isFile());
		if (exists) {
			// 如果不覆盖，返回假
			if (!override(file)) {
				return false;
			}
		}

		// 停止文件
		file = createStopOS();
		exists = (file.exists() && file.isFile());
		if (exists) {
			// 如果不覆盖，返回假
			if (!override(file)) {
				return false;
			}
		}
		// 最后确认
		return __confirm();
	}
	
//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return WatchLauncher.getInstance().findCaption(xmlPath);
//	}
//
//	/**
//	 * 解析内容
//	 * @param xmlPath
//	 * @return 抽取的文本
//	 */
//	private String findContent(String xmlPath) {
//		return WatchLauncher.getInstance().findContent(xmlPath);
//	}

	/**
	 * 初始化组件
	 */
	private void initControls() {
		setButtonText(cmdExport, findCaption("Dialog/script/button/save/title"));
		cmdExport.setMnemonic('S');
		setButtonText(cmdCreate, findCaption("Dialog/script/button/build/title"));
		cmdCreate.setMnemonic('B');
		setButtonText(cmdReset, findCaption("Dialog/script/button/reset/title"));
		cmdReset.setMnemonic('R');
		setButtonText(cmdExit, findCaption("Dialog/script/button/exit/title"));
		cmdExit.setMnemonic('X');
		setButtonText(cmdAll, findCaption("Dialog/script/button/all/title"));
		cmdAll.setMnemonic('A');
		
		// TOP CLUSTER
		setButtonText(chkTop, findCaption("Dialog/script/checkbox/top/title"));
		setButtonText(chkTop1, findCaption("Dialog/script/checkbox/top1/title"));
		setButtonText(chkTop2, findCaption("Dialog/script/checkbox/top2/title"));
		setButtonText(chkTopLog, findCaption("Dialog/script/checkbox/top-log/title"));
		// BANK CLUSTER
		setButtonText(chkBank, findCaption("Dialog/script/checkbox/bank/title"));
		setButtonText(chkBank1, findCaption("Dialog/script/checkbox/bank1/title"));
		setButtonText(chkBank2, findCaption("Dialog/script/checkbox/bank2/title"));
		setButtonText(chkBankLog, findCaption("Dialog/script/checkbox/bank-log/title"));
		setButtonText(chkAccount, findCaption("Dialog/script/checkbox/account/title"));
		setButtonText(chkHash, findCaption("Dialog/script/checkbox/hash/title"));
		setButtonText(chkGate, findCaption("Dialog/script/checkbox/gate/title"));
		setButtonText(chkEntrance, findCaption("Dialog/script/checkbox/entrance/title"));
		// HOME CLUSTER
		setButtonText(chkHome, findCaption("Dialog/script/checkbox/home/title"));
		setButtonText(chkHome1, findCaption("Dialog/script/checkbox/home1/title"));
		setButtonText(chkHome2, findCaption("Dialog/script/checkbox/home2/title"));
		setButtonText(chkHomeLog, findCaption("Dialog/script/checkbox/home-log/title"));
		setButtonText(chkDataMaster, findCaption("Dialog/script/checkbox/data-master/title"));
		setButtonText(chkDataSlave, findCaption("Dialog/script/checkbox/data-slave/title"));
		setButtonText(chkBuild, findCaption("Dialog/script/checkbox/build/title"));
		setButtonText(chkWork, findCaption("Dialog/script/checkbox/work/title"));
		setButtonText(chkCall, findCaption("Dialog/script/checkbox/call/title"));
		
		// 设置文本提示
		setToolTipText(txtEcho, findCaption("Dialog/script/text-field/echo/title"));
		setToolTipText(txtLog, findCaption("Dialog/script/text-field/log/title"));
		setToolTipText(txtTig, findCaption("Dialog/script/text-field/tig/title"));
		setToolTipText(txtResource, findCaption("Dialog/script/text-field/resource/title"));
		setToolTipText(txtStore, findCaption("Dialog/script/text-field/store/title"));
		setToolTipText(txtMiddle, findCaption("Dialog/script/text-field/middle/title"));
		setToolTipText(txtDeploy, findCaption("Dialog/script/text-field/deploy/title"));
		setToolTipText(txtStartInterval, findCaption("Dialog/script/text-field/start-time/title"));
		setToolTipText(txtStopInterval, findCaption("Dialog/script/text-field/stop-time/title"));
	}
	
	/**
	 * 建立事件监听
	 */
	private void initListeners() {
		// 给按纽设置事件
		JButton[] buttons = new JButton[] { cmdExport, cmdCreate, cmdReset,
				cmdExit };
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].addActionListener(this);
		}

		// 给选择按纽设置事件
		JCheckBox[] boxes = new JCheckBox[] {cmdAll, chkTop, chkTop1, chkTop2, chkTopLog, 
				chkBank, chkBank1, chkBank2, chkBankLog, 
				chkAccount, chkHash, chkGate, chkEntrance, 
				chkHome, chkHome1, chkHome2, chkHomeLog, 
				chkDataMaster, chkDataSlave, chkBuild, chkWork, chkCall };
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].addActionListener(this);
		}
	}
	
	/**
	 * 间隔时间
	 * @param field 文本框
	 * @return 面板
	 */
	private JPanel createIntervalPanel(JTextField field, int defaultValue) {
		field.setColumns(8);
		field.setDocument(new DigitDocument(field, 5));
		field.setText(String.valueOf(defaultValue));
		
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(0, 0));
		sub.add(field, BorderLayout.WEST);
		sub.add(new JPanel(), BorderLayout.CENTER);
		return sub;
	}

	/**
	 * 建立数据文件面板
	 * @return JPanel
	 */
	private JPanel createDirectoryPanel() {
		// 回显目录
		JLabel lblEcho = new JLabel();
		setLabelText(lblEcho, findCaption("Dialog/script/label/echo/title"));
		// 日志目录
		JLabel lblLog = new JLabel();
		setLabelText(lblLog, findCaption("Dialog/script/label/log/title"));
		
		JLabel lblTig = new JLabel();
		setLabelText(lblTig, findCaption("Dialog/script/label/tig/title"));
		
		// 资源目录
		JLabel lblResource = new JLabel();
		setLabelText(lblResource, findCaption("Dialog/script/label/resource/title"));
		// 数据存储目录
		JLabel lblStore = new JLabel();
		setLabelText(lblStore, findCaption("Dialog/script/label/store/title"));
		// 分布计算的中间数据目录
		JLabel lblMiddle = new JLabel();
		setLabelText(lblMiddle, findCaption("Dialog/script/label/middle/title"));
		// 应用软件部署目录
		JLabel lblDeploy = new JLabel();
		setLabelText(lblDeploy, findCaption("Dialog/script/label/deploy/title"));
		// 启动间隔时间
		JLabel lblStartTime = new JLabel();
		setLabelText(lblStartTime, findCaption("Dialog/script/label/start-time/title"));
		// 停止间隔时间
		JLabel lblStopTime = new JLabel();
		setLabelText(lblStopTime, findCaption("Dialog/script/label/stop-time/title"));

		// 标签
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(9, 1, 0, 10));
		p1.add(lblEcho);
		p1.add(lblLog);
		p1.add(lblTig);
		p1.add(lblResource);
		p1.add(lblStore);
		p1.add(lblMiddle);
		p1.add(lblDeploy);
		p1.add(lblStartTime);
		p1.add(lblStopTime);

		// 文本框
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(9, 1, 0, 10));
		// 提示
		JTextField[] fields = new JTextField[] { txtEcho, txtLog, txtTig,
				txtResource, txtStore, txtMiddle, txtDeploy };
		for (int i = 0; i < fields.length; i++) {
			p2.add(fields[i]);
		}
		// 间隔
		p2.add(createIntervalPanel(txtStartInterval, 20));
		p2.add(createIntervalPanel(txtStopInterval, 5));
		
		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(5, 0));
		p3.add(p1, BorderLayout.WEST);
		p3.add(p2, BorderLayout.CENTER);
		
		JScrollPane scroll = new JScrollPane(p3);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		// 面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(new JPanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));

		return panel;
	}
	
	/**
	 * 生成一个保存文件的面板
	 * @return JPanel实例
	 */
	private JPanel createSaveToPanel() {
		// 分左右两端位置
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 0));
		panel.add(cmdExport, BorderLayout.WEST);
		panel.add(lblExport, BorderLayout.CENTER);
		panel.add(cmdAll, BorderLayout.EAST);
		
		return panel;
	}
	
	/**
	 * 生成TOP集群面板
	 * @return JPanel
	 */
	private JPanel createTopClusterPanel() {
		// 子界面
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(4, 1, 0, 5));
		sub.add(chkTop);
		sub.add(chkTop1);
		sub.add(chkTop2);
		sub.add(chkTopLog);

		String caption = findCaption("Dialog/script/label/top-cluster/title");
		JLabel lable = new JLabel(caption);
		// 分上下排列
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 3));
		panel.add(lable, BorderLayout.NORTH);
		panel.add(sub, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 生成BANK集群面板
	 * @return JPanel
	 */
	private JPanel createBankClusterPanel() {
		// 子界面
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(8, 1, 0, 5));
		sub.add(chkBank);
		sub.add(chkBank1);
		sub.add(chkBank2);
		sub.add(chkBankLog);
		sub.add(chkAccount);
		sub.add(chkHash);
		sub.add(chkGate);
		sub.add(chkEntrance);

		String caption = findCaption("Dialog/script/label/bank-cluster/title");
		JLabel lable = new JLabel(caption);
		// 分上下排列
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 3));
		panel.add(lable, BorderLayout.NORTH);
		panel.add(sub, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 生成HOME集群面板
	 * @return JPanel
	 */
	private JPanel createHomeClusterPanel() {
		// 子界面
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(9, 1, 0, 5));
		sub.add(chkHome);
		sub.add(chkHome1);
		sub.add(chkHome2);
		sub.add(chkHomeLog);
		sub.add(chkDataMaster);
		sub.add(chkDataSlave);
		sub.add(chkBuild);
		sub.add(chkWork);
		sub.add(chkCall);

		String caption = findCaption("Dialog/script/label/home-cluster/title");
		JLabel lable = new JLabel(caption);
		// 分上下排列
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 3));
		panel.add(lable, BorderLayout.NORTH);
		panel.add(sub, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 生成节点面板
	 * @return
	 */
	private JPanel createSitePanel() {
		JPanel topPanel = createTopClusterPanel();
		JPanel bankPanel = createBankClusterPanel();
		JPanel homePanel = createHomeClusterPanel();
		
		topPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		bankPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		homePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		JPanel sub = new JPanel();
		BoxLayout layout = new BoxLayout(sub, BoxLayout.Y_AXIS);
		sub.setLayout(layout);
		sub.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
		sub.add(topPanel);
		sub.add(bankPanel);
		sub.add(homePanel);

		JScrollPane scroll = new JScrollPane(sub);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setPreferredSize(new Dimension(132, 10));

		// 分左右两端位置
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0,0));
		panel.add(scroll, BorderLayout.CENTER);

		return panel;
	}
	
//	/**
//	 * 设置中心面板
//	 * @return
//	 */
//	private JPanel createCenterPanel() {
//		// 保存脚本的面板，位于底部
//		JPanel bottom = createSaveToPanel();
//
//		// 显示目录的面板
//		String leftTitle = getCaption("Dialog/script/panel/directory/title");
//		JPanel center = createDirectoryPanel();
//		center.setBorder(UITools.createTitledBorder(leftTitle, 3));
//
//		// 右侧面板
//		String rightTitle = getCaption("Dialog/script/panel/site/title");
//		JPanel right = createSitePanel();
//		right.setBorder(UITools.createTitledBorder(rightTitle, 3));
//
//		String title = findContent("Dialog/script/tooltip/remark");
//		if (title != null && title.trim().length() > 0) {
//			center.setToolTipText(title);
//			right.setToolTipText(title);
//		}
//		
//		// 生成面板
//		JPanel left = new JPanel();
//		left.setLayout(new BorderLayout(2, 5));
//		left.add(center, BorderLayout.CENTER);
//		left.add(bottom, BorderLayout.SOUTH);
//
//		// 文件面板
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(5, 0));
//		panel.add(left, BorderLayout.CENTER);
//		panel.add(right, BorderLayout.EAST);
//
//		return panel;
//	}
	
	/**
	 * 设置中心面板
	 * @return
	 */
	private JPanel createCenterPanel() {
		// 保存脚本的面板，位于底部
		JPanel bottom = createSaveToPanel();

		// 显示目录的面板
		String leftTitle = findCaption("Dialog/script/panel/directory/title");
		JPanel center = createDirectoryPanel();
		center.setBorder(UITools.createTitledBorder(leftTitle, 3));

		// 右侧面板
		String rightTitle = findCaption("Dialog/script/panel/site/title");
		JPanel right = createSitePanel();
		right.setBorder(UITools.createTitledBorder(rightTitle, 3));

		String title = findContent("Dialog/script/tooltip/remark");
		if (title != null && title.trim().length() > 0) {
			center.setToolTipText(title);
			right.setToolTipText(title);
		}
		
		// 生成面板
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(0, 5));
		sub.add(center, BorderLayout.CENTER);
		sub.add(bottom, BorderLayout.SOUTH);
		
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		left.add(sub);
		
		// 文件面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(left, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);

		return panel;
	}
	
	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 3, 8, 0));
		right.add(cmdCreate);
		right.add(cmdReset);
		right.add(cmdExit);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(right, BorderLayout.EAST);

		return bottom;
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel createRootPanel() {
		// 初始化显示控件
		initControls();
		// 建立事件监听
		initListeners();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 5));
		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 面板范围
		Object obj = UITools.getProperity(KEY_RECT);
		if (obj != null && obj.getClass() == Rectangle.class) {
			return (Rectangle) obj;
		}

		// 初始化它
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 680;
		int height = 480;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		JPanel pane = createRootPanel();
		// 全部无效
		reset();
		
		// 设置面板
		setContentPane(pane);

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 360));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/script/title");
		setTitle(title);
		
		// 检查对话框字体
		checkDialogFonts();
		
		setVisible(true);
	}

}