/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.script;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.choice.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 * LAXCUS启动/停止脚本文件生成器
 * 
 * @author scott.liang
 * @version 1.0 5/24/2020
 * @since laxcus 1.0
 */
public class RayControlScriptDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -1294400416652848215L;

	/** 句柄 **/
	static RayControlScriptDialog selfHandle;

	/**
	 * 返回句柄
	 * @return 句柄
	 */
	public static RayControlScriptDialog getInstance() {
		return RayControlScriptDialog.selfHandle;
	}

	/** 保存脚本文件的目录 **/
	private FlatButton cmdExport = new FlatButton();

	/** 保存脚本 **/
	private JLabel lblExport = new JLabel();

	/** 生成脚本 **/
	private FlatButton cmdCreate = new FlatButton();

	/** 退同窗口 **/
	private FlatButton cmdExit = new FlatButton();

	/** 重置全部参数 **/
	private FlatButton cmdReset = new FlatButton();

	/* 全部选中 **/
	private JCheckBox cmdAll = new JCheckBox();

	/** 以下是标签和对应的目录 **/

	// 回显目录
	private FlatTextField txtEcho = new FlatTextField();

	// 日志目录
	private FlatTextField txtLog = new FlatTextField();

	// TIG目录
	private FlatTextField txtTig = new FlatTextField();
	
	// BILL目录
	private FlatTextField txtBill = new FlatTextField();

	// 资源目录
	private FlatTextField txtResource = new FlatTextField();

	// 数据存储目录
	private FlatTextField txtStore = new FlatTextField();

	// 分布计算的中间数据目录
	private FlatTextField txtMiddle = new FlatTextField();

	// 应用软件部署目录
	private FlatTextField txtDeploy = new FlatTextField();

	// 最大SOCKET
	private FlatTextField txtMaxSockets = new FlatTextField();
	
	// 启动间隔时间
	private FlatTextField txtStartInterval = new FlatTextField();

	// 启动间隔时间
	private FlatTextField txtStopInterval = new FlatTextField();

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

	//	/** 最后一次的目录 **/
	//	private File rootX;

	/**
	 * 
	 */
	public RayControlScriptDialog() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		click(e);
	}

	/**
	 * 关闭窗口
	 */
	public void closeWindow() {
		super.closeWindow();
		RayControlScriptDialog.selfHandle = null;
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
				writeBounds();
				// 调用父类，关闭窗口
				closeWindow();
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
				writeBounds();
			}
		} else if(e.getSource() == cmdAll) {
			all();
		} else {
			// 执行站点按纽操作
			todo();
		}
	}

	/**
	 * 设置按纽文本
	 * @param button
	 * @param text
	 */
	private void setButtonText(AbstractButton button, String text) {
		// FontKit.setButtonText(button, text);
		button.setText(text);
	}

	private void setToolTipText(JComponent jc, String tooltip) {
		jc.setToolTipText(tooltip);
	}

	private void setLabelText(JLabel label, String text) {
		label.setText(text);
	}

	/**
	 * 执行分析，打开/关闭文本框
	 */
	private void todo() {
		int echo = 0;
		int log = 0;
		int tig = 0;
		int bill = 0;
		int resource = 0;
		int store = 0;

		int middle = 0;
		int deploy = 0;

		// TOP 集群
		if (chkTop.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkTop1.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkTop2.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkTopLog.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}

		// BANK集群
		if (chkBank.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkBank1.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkBank2.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkBankLog.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}
		if (chkAccount.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkHash.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}
		if (chkGate.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}
		if (chkEntrance.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}

		// HOME集群
		if (chkHome.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkHome1.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkHome2.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
		}
		if (chkHomeLog.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
		}
		if (chkDataMaster.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
			deploy++;
			middle++;
			store++;
		}
		if (chkDataSlave.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
			deploy++;
			middle++;
			store++;
		}
		if (chkBuild.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			resource++;
			deploy++;
			middle++;
		}
		if (chkWork.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			
			deploy++;
			middle++;
		}
		if (chkCall.isSelected()) {
			echo++;
			log++;
			tig++;
			bill++;
			
			deploy++;
			middle++;
		}

		// 打开/关闭文本框
		txtEcho.setEnabled(echo > 0);
		txtLog.setEnabled(log > 0);
		txtTig.setEnabled(tig > 0);
		txtBill.setEnabled(bill>0);
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

		FlatTextField[] fields = new FlatTextField[] { txtEcho, txtLog, txtTig, txtBill, txtResource, txtStore,
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
		FlatTextField[] fields = new FlatTextField[] { txtEcho, txtLog, txtTig, txtBill, txtResource, txtStore,
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
	private boolean isDisable(FlatTextField sub) {
		String text = sub.getText();
		return sub.isEnabled() && text.trim().isEmpty();
	}
	
	/**
	 * 判断是数字
	 * @param sub
	 * @return
	 */
	private boolean isMaxSockets(FlatTextField sub) {
		String text = sub.getText().trim();
		if (text.length() > 0) {
			int value = Integer.parseInt(text);
			return value > 0;
		}
		return false;
		// return text.matches("^\\s*([0-9]+?)\\s*$");
	}

	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private boolean check() {
		if (isDisable(txtEcho)) {
			warning();
			txtEcho.requestFocus();
			return false;
		}
		if (isDisable(txtLog)) {
			warning();
			txtLog.requestFocus();
			return false;
		}
		if (isDisable(txtTig)) {
			warning();
			txtTig.requestFocus();
			return false;
		}
		if (isDisable(txtBill)) {
			warning();
			txtBill.requestFocus();
			return false;
		}
		if (isDisable(txtResource)) {
			warning();
			txtResource.requestFocus();
			return false;
		}
		if (isDisable(txtStore)) {
			warning();
			txtStore.requestFocus();
			return false;
		}
		if (isDisable(txtMiddle)) {
			warning();
			txtMiddle.requestFocus();
			return false;
		}
		if (isDisable(txtDeploy)) {
			warning();
			txtDeploy.requestFocus();
			return false;
		}
		// 不是数字时
		if (!isMaxSockets(txtMaxSockets)) {
			warning();
			txtMaxSockets.requestFocus();
			return false;
		}

		// 判断写入目录
		String text = lblExport.getText();
		if (text.trim().isEmpty()) {
			warning();
			cmdExport.requestFocus();
			return false;
		}

		return true;
	}

	//	/**
	//	 * 生成脚本文件
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean create() {
	//		// 顺序
	//		boolean success = sequenceCreate();
	//		if (!success) {
	//			return false;
	//		}
	//		// 倒序
	//		success = reverseCreate();
	//		if (!success) {
	//			return false;
	//		}
	//
	//		// 提示完成
	//		String title = findCaption("Dialog/script/finished/title");
	//		String sub = findContent("Dialog/script/finished");
	//		String content = String.format(sub, lblExport.getText());
	//		MessageDialog.showMessageBox(this, title,
	//				JOptionPane.INFORMATION_MESSAGE, content,
	//				JOptionPane.DEFAULT_OPTION);
	//		return true;
	//	}

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
		String title = UIManager.getString("ControlScriptDialog.FinishedTitle");
		String sub = UIManager.getString("ControlScriptDialog.FinishedContent");
		String content = String.format(sub, lblExport.getText());
		MessageBox.showInformation(this, title, content);
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

	private String formatPath(String tag, FlatTextField filed) {
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

	private String formatPath(String tag, int value) {
		return String.format("export %s=%d\n", tag, value);
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
		if (txtBill.isEnabled()) {
			String text = formatPath("LAXCUS_BILL_DIR", txtBill);
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
		if (txtMaxSockets.isEnabled()) {
			String str = txtMaxSockets.getText().trim();
			if (str.length() > 0) {
				int value = Integer.parseInt(str);
				String text = formatPath("LAXCUS_MAX_UDP_SOCKETS", value);
				buff.append(text);
			}
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

	//	/**
	//	 * 保存根目录
	//	 */
	//	private void save() {
	//		if (root != null) {
	//			UITools.putProperity(KEY_PATH, root.toString());
	//		}
	//
	//		Rectangle rect = getBounds();
	//		Rectangle sub = new Rectangle(rect);
	//		UITools.putProperity(KEY_RECT, sub);
	//	}

	//	private void save() {
	//		
	//		writeBounds();
	//	}

	//	/**
	//	 * 打开窗口，选择磁盘目录
	//	 */
	//	private void choice() {
	//		String title = findCaption("Dialog/script/directory/title");
	//		String buttonText = findCaption("Dialog/script/directory/button");
	//		
	//		JFileChooser chooser = new JFileChooser();
	//		chooser.setMultiSelectionEnabled(true);
	//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // .FILES_ONLY);
	//		chooser.setDialogTitle(title); 
	//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	//		chooser.setApproveButtonText(buttonText);
	//		if (root != null) {
	//			chooser.setCurrentDirectory(root);
	//		}
	//		// 没有定义，从系统中取
	//		else {
	//			Object memory = UITools.getProperity(KEY_PATH);
	//			if (memory != null && memory.getClass() == String.class) {
	//				File path = new File((String) memory);
	//				if (path.exists() && path.isDirectory()) {
	//					chooser.setCurrentDirectory(path);
	//				}
	//			}
	//		}
	//		
	//		int val = chooser.showOpenDialog(this);
	//		// 显示窗口
	//		if (val != JFileChooser.APPROVE_OPTION) {
	//			return;
	//		}
	//		// 判断目录有效，显示它。否则清除
	//		File dir = chooser.getSelectedFile();
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		if (success) {
	//			root = dir;
	//			// 保存到集合
	//			FontKit.setLabelText(lblExport, dir.toString());
	//		}
	//	}


	//	/**
	//	 * 打开窗口，选择磁盘目录
	//	 */
	//	private void choice() {
	//		String title = UIManager.getString("ControlScriptDialog.DirectoryTitle");
	//		String buttonText = UIManager.getString("ControlScriptDialog.DirectoryButtonOkayText");
	//		
	//		JFileChooser chooser = new JFileChooser();
	//		chooser.setMultiSelectionEnabled(true);
	//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // .FILES_ONLY);
	//		chooser.setDialogTitle(title); 
	//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	//		chooser.setApproveButtonText(buttonText);
	//		if (root != null) {
	//			chooser.setCurrentDirectory(root);
	//		}
	//		// 没有定义，从系统中取
	//		else {
	//			Object memory = UITools.getProperity(KEY_PATH);
	//			if (memory != null && memory.getClass() == String.class) {
	//				File path = new File((String) memory);
	//				if (path.exists() && path.isDirectory()) {
	//					chooser.setCurrentDirectory(path);
	//				}
	//			}
	//		}
	//		
	//		int val = chooser.showOpenDialog(this);
	//		// 显示窗口
	//		if (val != JFileChooser.APPROVE_OPTION) {
	//			return;
	//		}
	//		// 判断目录有效，显示它。否则清除
	//		File dir = chooser.getSelectedFile();
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		if (success) {
	//			root = dir;
	//			// 保存到集合
	//			FontKit.setLabelText(lblExport, dir.toString());
	//		}
	//	}

	/**
	 * 保存范围
	 */
	private void writeRoot(File f) {
		RTKit.writeFile(RTEnvironment.ENVIRONMENT_SYSTEM, "ControlScriptDialog/Root", f);
	}

	/**
	 * 读范围
	 * @return
	 */
	private File readRoot() {
		return RTKit.readFile(RTEnvironment.ENVIRONMENT_SYSTEM,"ControlScriptDialog/Root");
	}

	/**
	 * 打开窗口，选择磁盘目录
	 */
	private void choice() {
		String title = UIManager.getString("ControlScriptDialog.DirectoryTitle");
		String buttonText = UIManager.getString("ControlScriptDialog.DirectoryButtonOkayText");

		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setShowCharsetEncode(false);
		dialog.setMultiSelectionEnabled(false);
		dialog.setDialogType(DialogOption.OPEN_DIALOG);
		dialog.setFileSelectionMode(DialogOption.DIRECTORIES_ONLY); 
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);
		// 已经保存的目录
		File file = readRoot();
		if (file != null) {
			File parent = file.getParentFile();
			if (parent != null && parent.isDirectory()) {
				dialog.setCurrentDirectory(parent);
			}
		}

		// 显示对话框
		File[] files = dialog.showDialog(this);
		boolean success = (files != null && files.length > 0);
		if (success) {
			File dir = files[0];
			success = (dir.exists() && dir.isDirectory());
			if (success) {
				// 保存到集合
				FontKit.setLabelText(lblExport, Laxkit.canonical(dir));
				// 写入
				writeRoot(dir);
			}
		}
	}

	//	/**
	//	 * 退出运行
	//	 */
	//	private boolean exit() {
	//		String title = getTitle();
	//		String content = findCaption("Dialog/script/exit/message/title");
	//
	//		ResourceLoader loader = new ResourceLoader();
	//		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
	//
	//		int who = MessageDialog.showMessageBox(this, title,
	//				JOptionPane.QUESTION_MESSAGE, icon, content,
	//				JOptionPane.YES_NO_OPTION);
	//		return (who == JOptionPane.YES_OPTION) ;
	//	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = UIManager.getString("ControlScriptDialog.ExitContent");
		return MessageBox.showYesNoDialog(this, title, content);
	}

	//	/**
	//	 * 参数警告！
	//	 */
	//	private void warning(){
	//		// 提示错误
	//		String title = findCaption("Dialog/script/warning/title");
	//		String content = findContent("Dialog/script/warning");
	//		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	//	}

	/**
	 * 参数警告！
	 */
	private void warning() {
		SoundKit.playWarning();
		// 提示错误
		String title = UIManager.getString("ControlScriptDialog.WarningTitle");
		String content = UIManager.getString("ControlScriptDialog.WarningContent");
		MessageBox.showInformation(this, title, content);
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

	//	/**
	//	 * 询问覆盖...
	//	 * @param file 磁盘文件
	//	 * @return 返回真或者假
	//	 */
	//	private boolean override(File file) {
	//		// 提示错误
	//		String title = findCaption("Dialog/script/override/title");
	//		String content = findContent("Dialog/script/override");
	//		String format = String.format(content, file.toString());
	//		
	//		ResourceLoader loader = new ResourceLoader();
	//		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
	//
	//		int who = MessageDialog.showMessageBox(this, title,
	//				JOptionPane.QUESTION_MESSAGE, icon, format,
	//				JOptionPane.YES_NO_OPTION);
	//		return (who == JOptionPane.YES_OPTION) ;
	//	}

	/**
	 * 询问覆盖...
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	private boolean override(File file) {
		// 提示错误
		String title = UIManager.getString("ControlScriptDialog.OverrideTitle");
		String content = UIManager.getString("ControlScriptDialog.OverrideContent");
		content = String.format(content, Laxkit.canonical(file));

		return MessageBox.showYesNoDialog(this, title, content);
	}

	//	/**
	//	 * 确认生成脚本文件
	//	 * @return 返回真或者假
	//	 */
	//	private boolean __confirm() {
	//		// 提示错误
	//		String title = findCaption("Dialog/script/confirm/title");
	//		String content = findContent("Dialog/script/confirm");
	//
	//		ResourceLoader loader = new ResourceLoader();
	//		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
	//
	//		int who = MessageDialog.showMessageBox(this, title,
	//				JOptionPane.QUESTION_MESSAGE, icon, content,
	//				JOptionPane.YES_NO_OPTION);
	//		return (who == JOptionPane.YES_OPTION) ;
	//	}

	/**
	 * 确认生成脚本文件
	 * @return 返回真或者假
	 */
	private boolean __confirm() {
		// 提示错误
		String title = UIManager.getString("ControlScriptDialog.ConfirmTitle");
		String content = UIManager.getString("ControlScriptDialog.ConfirmContent");
		return MessageBox.showYesNoDialog(this, title, content);
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

	//	/**
	//	 * 初始化组件
	//	 */
	//	private void initControls() {
	//		setButtonText(cmdExport, findCaption("Dialog/script/button/save/title"));
	//		cmdExport.setMnemonic('S');
	//		setButtonText(cmdCreate, findCaption("Dialog/script/button/build/title"));
	//		cmdCreate.setMnemonic('B');
	//		setButtonText(cmdReset, findCaption("Dialog/script/button/reset/title"));
	//		cmdReset.setMnemonic('R');
	//		setButtonText(cmdExit, findCaption("Dialog/script/button/exit/title"));
	//		cmdExit.setMnemonic('X');
	//		setButtonText(cmdAll, findCaption("Dialog/script/button/all/title"));
	//		cmdAll.setMnemonic('A');
	//		
	//		// TOP CLUSTER
	//		setButtonText(chkTop, findCaption("Dialog/script/checkbox/top/title"));
	//		setButtonText(chkTop1, findCaption("Dialog/script/checkbox/top1/title"));
	//		setButtonText(chkTop2, findCaption("Dialog/script/checkbox/top2/title"));
	//		setButtonText(chkTopLog, findCaption("Dialog/script/checkbox/top-log/title"));
	//		// BANK CLUSTER
	//		setButtonText(chkBank, findCaption("Dialog/script/checkbox/bank/title"));
	//		setButtonText(chkBank1, findCaption("Dialog/script/checkbox/bank1/title"));
	//		setButtonText(chkBank2, findCaption("Dialog/script/checkbox/bank2/title"));
	//		setButtonText(chkBankLog, findCaption("Dialog/script/checkbox/bank-log/title"));
	//		setButtonText(chkAccount, findCaption("Dialog/script/checkbox/account/title"));
	//		setButtonText(chkHash, findCaption("Dialog/script/checkbox/hash/title"));
	//		setButtonText(chkGate, findCaption("Dialog/script/checkbox/gate/title"));
	//		setButtonText(chkEntrance, findCaption("Dialog/script/checkbox/entrance/title"));
	//		// HOME CLUSTER
	//		setButtonText(chkHome, findCaption("Dialog/script/checkbox/home/title"));
	//		setButtonText(chkHome1, findCaption("Dialog/script/checkbox/home1/title"));
	//		setButtonText(chkHome2, findCaption("Dialog/script/checkbox/home2/title"));
	//		setButtonText(chkHomeLog, findCaption("Dialog/script/checkbox/home-log/title"));
	//		setButtonText(chkDataMaster, findCaption("Dialog/script/checkbox/data-master/title"));
	//		setButtonText(chkDataSlave, findCaption("Dialog/script/checkbox/data-slave/title"));
	//		setButtonText(chkBuild, findCaption("Dialog/script/checkbox/build/title"));
	//		setButtonText(chkWork, findCaption("Dialog/script/checkbox/work/title"));
	//		setButtonText(chkCall, findCaption("Dialog/script/checkbox/call/title"));
	//		
	//		// 设置文本提示
	//		setToolTipText(txtEcho, findCaption("Dialog/script/text-field/echo/title"));
	//		setToolTipText(txtLog, findCaption("Dialog/script/text-field/log/title"));
	//		setToolTipText(txtTig, findCaption("Dialog/script/text-field/tig/title"));
	//		setToolTipText(txtResource, findCaption("Dialog/script/text-field/resource/title"));
	//		setToolTipText(txtStore, findCaption("Dialog/script/text-field/store/title"));
	//		setToolTipText(txtMiddle, findCaption("Dialog/script/text-field/middle/title"));
	//		setToolTipText(txtDeploy, findCaption("Dialog/script/text-field/deploy/title"));
	//		setToolTipText(txtStartInterval, findCaption("Dialog/script/text-field/start-time/title"));
	//		setToolTipText(txtStopInterval, findCaption("Dialog/script/text-field/stop-time/title"));
	//	}



	/**
	 * 初始化组件
	 */
	private void initControls() {
		setButtonText(cmdExport, UIManager.getString("ControlScriptDialog.ButtonSaveText"));
		cmdExport.setMnemonic('S');
		setButtonText(cmdCreate, UIManager.getString("ControlScriptDialog.ButtonBuildText"));
		cmdCreate.setMnemonic('B');
		setButtonText(cmdReset, UIManager.getString("ControlScriptDialog.ButtonResetText"));
		cmdReset.setMnemonic('R');
		setButtonText(cmdExit, UIManager.getString("ControlScriptDialog.ButtonExitText"));
		cmdExit.setMnemonic('X');
		setButtonText(cmdAll, UIManager.getString("ControlScriptDialog.ButtonAllText"));
		cmdAll.setMnemonic('A');

		// TOP CLUSTER
		setButtonText(chkTop, UIManager.getString("ControlScriptDialog.CheckboxTopText"));
		setButtonText(chkTop1, UIManager.getString("ControlScriptDialog.CheckboxTop1Text"));
		setButtonText(chkTop2, UIManager.getString("ControlScriptDialog.CheckboxTop2Text"));
		setButtonText(chkTopLog, UIManager.getString("ControlScriptDialog.CheckboxTopLogText"));
		// BANK CLUSTER
		setButtonText(chkBank, UIManager.getString("ControlScriptDialog.CheckboxBankText"));
		setButtonText(chkBank1, UIManager.getString("ControlScriptDialog.CheckboxBank1Text"));
		setButtonText(chkBank2, UIManager.getString("ControlScriptDialog.CheckboxBank2Text"));
		setButtonText(chkBankLog, UIManager.getString("ControlScriptDialog.CheckboxBankLogText"));
		setButtonText(chkAccount, UIManager.getString("ControlScriptDialog.CheckboxAccountText"));
		setButtonText(chkHash, UIManager.getString("ControlScriptDialog.CheckboxHashText"));
		setButtonText(chkGate, UIManager.getString("ControlScriptDialog.CheckboxGateText"));
		setButtonText(chkEntrance, UIManager.getString("ControlScriptDialog.CheckboxEntranceText"));
		// HOME CLUSTER
		setButtonText(chkHome, UIManager.getString("ControlScriptDialog.CheckboxHomeText"));
		setButtonText(chkHome1, UIManager.getString("ControlScriptDialog.CheckboxHome1Text"));
		setButtonText(chkHome2, UIManager.getString("ControlScriptDialog.CheckboxHome2Text"));
		setButtonText(chkHomeLog, UIManager.getString("ControlScriptDialog.CheckboxHomeLogText"));
		setButtonText(chkDataMaster, UIManager.getString("ControlScriptDialog.CheckboxDataMasterText"));
		setButtonText(chkDataSlave, UIManager.getString("ControlScriptDialog.CheckboxDataSlaveText"));
		setButtonText(chkBuild, UIManager.getString("ControlScriptDialog.CheckboxBuildText"));
		setButtonText(chkWork, UIManager.getString("ControlScriptDialog.CheckboxWorkText"));
		setButtonText(chkCall, UIManager.getString("ControlScriptDialog.CheckboxCallText"));

		// 设置文本提示

		//		ControlScriptDialog.TextFieldPathText  Linux目录格式

		setToolTipText(txtEcho, UIManager.getString("ControlScriptDialog.TextFieldEchoText"));
		setToolTipText(txtLog, UIManager.getString("ControlScriptDialog.TextFieldLogText"));
		setToolTipText(txtTig, UIManager.getString("ControlScriptDialog.TextFieldTigText"));
		setToolTipText(txtBill, UIManager.getString("ControlScriptDialog.TextFieldBillText"));
		setToolTipText(txtResource, UIManager.getString("ControlScriptDialog.TextFieldResourceText"));
		setToolTipText(txtStore, UIManager.getString("ControlScriptDialog.TextFieldStoreText"));
		setToolTipText(txtMiddle, UIManager.getString("ControlScriptDialog.TextFieldMiddleText"));
		setToolTipText(txtDeploy, UIManager.getString("ControlScriptDialog.TextFieldDeployText"));
		setToolTipText(txtMaxSockets, UIManager.getString("ControlScriptDialog.TextFieldMaxSocketText"));
		setToolTipText(txtStartInterval, UIManager.getString("ControlScriptDialog.TextFieldStartTimeText"));
		setToolTipText(txtStopInterval, UIManager.getString("ControlScriptDialog.TextFieldStopTimeText"));
	}

	/**
	 * 建立事件监听
	 */
	private void initListeners() {
		// 给按纽设置事件
		FlatButton[] buttons = new FlatButton[] { cmdExport, cmdCreate, cmdReset,
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
	private JPanel createIntervalPanel(FlatTextField field, int defaultValue) {
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
		setLabelText(lblEcho, UIManager.getString("ControlScriptDialog.LabelEchoText"));
		// 日志目录
		JLabel lblLog = new JLabel();
		setLabelText(lblLog, UIManager.getString("ControlScriptDialog.LabelLogText"));

		JLabel lblTig = new JLabel();
		setLabelText(lblTig, UIManager.getString("ControlScriptDialog.LabelTigText"));

		JLabel lblBill = new JLabel();
		setLabelText(lblBill, UIManager.getString("ControlScriptDialog.LabelBillText"));

		// 资源目录
		JLabel lblResource = new JLabel();
		setLabelText(lblResource, UIManager.getString("ControlScriptDialog.LabelResourceText"));
		// 数据存储目录
		JLabel lblStore = new JLabel();
		setLabelText(lblStore, UIManager.getString("ControlScriptDialog.LabelStoreText"));
		// 分布计算的中间数据目录
		JLabel lblMiddle = new JLabel();
		setLabelText(lblMiddle, UIManager.getString("ControlScriptDialog.LabelMiddleText"));
		// 应用软件部署目录
		JLabel lblDeploy = new JLabel();
		setLabelText(lblDeploy, UIManager.getString("ControlScriptDialog.LabelDeployText"));
		
		// 启动间隔时间
		JLabel lblMaxSockets = new JLabel();
		setLabelText(lblMaxSockets, UIManager.getString("ControlScriptDialog.LabelMaxSocketsText"));
		
		// 启动间隔时间
		JLabel lblStartTime = new JLabel();
		setLabelText(lblStartTime, UIManager.getString("ControlScriptDialog.LabelStartTimeText"));
		// 停止间隔时间
		JLabel lblStopTime = new JLabel();
		setLabelText(lblStopTime, UIManager.getString("ControlScriptDialog.LabelStopTimeText"));

		// 标签
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(11, 1, 0, 10));
		p1.add(lblEcho);
		p1.add(lblLog);
		p1.add(lblTig);
		p1.add(lblBill);
		p1.add(lblResource);
		p1.add(lblStore);
		p1.add(lblMiddle);
		p1.add(lblDeploy);
		p1.add(lblMaxSockets);
		p1.add(lblStartTime);
		p1.add(lblStopTime);

		// 文本框
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(11, 1, 0, 10));
		// 提示
		FlatTextField[] fields = new FlatTextField[] { txtEcho, txtLog, txtTig, txtBill,
				txtResource, txtStore, txtMiddle, txtDeploy };
		for (int i = 0; i < fields.length; i++) {
			p2.add(fields[i]);
		}
		// 间隔
		p2.add(createIntervalPanel(txtMaxSockets, 1024));
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

		String caption = UIManager.getString("ControlScriptDialog.LabelTopClusterText");
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

		String caption = UIManager.getString("ControlScriptDialog.LabelBankClusterText");
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

		String caption = UIManager.getString("ControlScriptDialog.LabelHomeClusterText");
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
		String leftTitle = UIManager.getString("ControlScriptDialog.PanelDirectoryText");
		JPanel center = createDirectoryPanel();
		center.setBorder(UITools.createTitledBorder(leftTitle, 3));

		// 右侧面板
		String rightTitle = UIManager.getString("ControlScriptDialog.PanelSiteText");
		JPanel right = createSitePanel();
		right.setBorder(UITools.createTitledBorder(rightTitle, 3));

		String title = UIManager.getString("ControlScriptDialog.RemarkTooltip");
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
		panel.setBorder(new EmptyBorder(4,4,4,4));
		//		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	//	/**
	//	 * 确定范围
	//	 * @return
	//	 */
	//	private Rectangle getBound() {
	//		// 面板范围
	//		Object obj = UITools.getProperity(KEY_RECT);
	//		if (obj != null && obj.getClass() == Rectangle.class) {
	//			return (Rectangle) obj;
	//		}
	//
	//		// 初始化它
	//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	//		int width = 680;
	//		int height = 480;
	//		int x = (size.width - width) / 2;
	//		int y = (size.height - height) / 2;
	//		return new Rectangle(x, y, width, height);
	//	}

	//	/**
	//	 * 显示窗口
	//	 */
	//	public void showDialog() {
	//		JPanel pane = createRootPanel();
	//		// 全部无效
	//		reset();
	//		
	//		// 设置面板
	//		setContentPane(pane);
	//
	//		Rectangle rect = getBound();
	//		setBounds(rect);
	//		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	//
	//		setMinimumSize(new Dimension(300, 360));
	//		setAlwaysOnTop(true);
	//
	//		// 标题
	//		String title = findCaption("Dialog/script/title");
	//		setTitle(title);
	//		
	//		// 检查对话框字体
	//		checkDialogFonts();
	//		
	//		setVisible(true);
	//	}

	/**
	 * 保存范围
	 */
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "ControlScriptDialog/Bound", rect);
	}

	/**
	 * 读范围
	 * @return
	 */
	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"ControlScriptDialog/Bound");
		if (bounds == null) {
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			int width = 680;
			int height = 480;
			int x = (size.width - width) / 2;
			int y = (size.height - height) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
			return new Rectangle(x, y, width, height);
		}
		return bounds;
	}

	private void initDialog() {
		setTitle(UIManager.getString("ControlScriptDialog.Title"));
		setFrameIcon(UIManager.getIcon("ControlScriptDialog.TitleIcon"));

		// 位置
		setMinimumSize(new Dimension(300, 150));

		JPanel panel = createRootPanel();
		// 全部无效
		reset();

		setContentPane(panel);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.desktop.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component bind, boolean modal) {
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		setBounds(readBounds());

		// 赋值句柄
		RayControlScriptDialog.selfHandle = this;

		// 显示窗口
		if (modal) {
			return showModalDialog(bind);
		} else {
			return showNormalDialog(bind);
		}
	}

}