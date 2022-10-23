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
import java.util.regex.*;

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
import com.laxcus.util.net.*;
import com.laxcus.util.skin.*;

/**
 * LAXCUS启动/停止脚本文件生成器
 * 
 * @author scott.liang
 * @version 1.0 5/24/2020
 * @since laxcus 1.0
 */
public class RayAddressScriptDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -1294400416652848215L;

	/** 句柄 **/
	static RayAddressScriptDialog selfHandle;

	/**
	 * 返回句柄
	 * @return 句柄
	 */
	public static RayAddressScriptDialog getInstance() {
		return RayAddressScriptDialog.selfHandle;
	}
	
//	/** 属性，保存到UIManager的KEY值 **/
//	private final static String EXPORT_PATH = RayAddressScriptDialog.class.getSimpleName() + "_EXPORT_PATH";
//	
//	/** 属性，保存到UIManager的KEY值 **/
//	private final static String IMPORT_PATH = RayAddressScriptDialog.class.getSimpleName() + "_IMPORT_PATH";
//	/** 窗口范围 **/
//	private final static String KEY_RECT = RayAddressScriptDialog.class.getSimpleName() + "_RECT"; 

	/** address.sh 里面的正则表达式. eg: export LOCAL_PRIVATE_IP="192.168.1.103" **/
	private final static String EXPORT_REGEX="^\\s*(?i)(?:export)\\s+(?i)([\\w\\W]+?)\\s*=\\s*\\\"\\s*([\\w\\W]+?)\\\"\\s*$";
	
	/** 导入address.sh的按纽 **/
	private FlatButton cmdImport = new FlatButton();

	/** 导入address.sh的目录 **/
	private JLabel lblImport = new JLabel();
	
	/** 保存address.sh的按纽 **/
	private FlatButton cmdExport = new FlatButton();

	/** 保存address.sh的目录 **/
	private JLabel lblExport = new JLabel();

	/** 生成脚本 **/
	private FlatButton cmdCreate = new FlatButton();

	/** 退同窗口 **/
	private FlatButton cmdExit = new FlatButton();

	/** 重置全部参数 **/
	private FlatButton cmdReset = new FlatButton();

	/* 全部选中 **/
	private JCheckBox cmdAll = new JCheckBox();

	/** 以下是地址(IP和端口)的标签 **/

	/** 网段IP **/
	private FlatTextField txtPrivateIP = new FlatTextField();
	private FlatTextField txtGatewayIP = new FlatTextField();

	/** TOP 集群 **/
	private JCheckBox chkTop = new JCheckBox();
	private FlatTextField txtTopIP = new FlatTextField();
	private FlatTextField txtTopPort = new FlatTextField();

	private JCheckBox chkTop1 = new JCheckBox();
	private FlatTextField txtTop1IP = new FlatTextField();
	private FlatTextField txtTop1Port = new FlatTextField();

	private JCheckBox chkTop2 = new JCheckBox();
	private FlatTextField txtTop2IP = new FlatTextField();
	private FlatTextField txtTop2Port = new FlatTextField();

	private JCheckBox chkTopLog = new JCheckBox();
	private FlatTextField txtTopLogPort = new FlatTextField();

	//	private JCheckBox chkTopLogBank = new JCheckBox();
	private JLabel lblTopLogBank = new JLabel();
	private FlatTextField txtTopLogBankPort = new FlatTextField();
	private FlatTextField txtTopTigBankPort = new FlatTextField();
	private FlatTextField txtTopBillBankPort = new FlatTextField();

	//	private JCheckBox chkTopLogHome = new JCheckBox();
	private JLabel lblTopLogHome = new JLabel();
	private FlatTextField txtTopLogHomePort = new FlatTextField();
	private FlatTextField txtTopTigHomePort = new FlatTextField();
	private FlatTextField txtTopBillHomePort = new FlatTextField();

	/** BANK管理节点 **/
	private JCheckBox chkBank = new JCheckBox();
	private FlatTextField txtBankIP = new FlatTextField();
	private FlatTextField txtBankPort = new FlatTextField();

	private JCheckBox chkBank1 = new JCheckBox();
	private FlatTextField txtBank1IP = new FlatTextField();
	private FlatTextField txtBank1Port = new FlatTextField();

	private JCheckBox chkBank2 = new JCheckBox();
	private FlatTextField txtBank2IP = new FlatTextField();
	private FlatTextField txtBank2Port = new FlatTextField();

	private JCheckBox chkBankLog = new JCheckBox();

	private JLabel lblBankLogAccount = new JLabel();
	private FlatTextField txtBankLogAccountPort = new FlatTextField();
	private FlatTextField txtBankTigAccountPort = new FlatTextField();
	private FlatTextField txtBankBillAccountPort = new FlatTextField();

	private JLabel lblBankLogHash = new JLabel();
	private FlatTextField txtBankLogHashPort = new FlatTextField();
	private FlatTextField txtBankTigHashPort = new FlatTextField();
	private FlatTextField txtBankBillHashPort = new FlatTextField();

	private JLabel lblBankLogGate = new JLabel();
	private FlatTextField txtBankLogGatePort = new FlatTextField();
	private FlatTextField txtBankTigGatePort = new FlatTextField();
	private FlatTextField txtBankBillGatePort = new FlatTextField();

	private JLabel lblBankLogEntrance = new JLabel();
	private FlatTextField txtBankLogEntrancePort = new FlatTextField();
	private FlatTextField txtBankTigEntrancePort = new FlatTextField();
	private FlatTextField txtBankBillEntrancePort = new FlatTextField();

	private JCheckBox chkAccount = new JCheckBox();
	private JCheckBox chkHash = new JCheckBox();
	private JCheckBox chkGate = new JCheckBox();
	private JCheckBox chkEntrance = new JCheckBox();

	private FlatTextField txtBankLogPort = new FlatTextField();
	private FlatTextField txtAccountPort = new FlatTextField();
	private FlatTextField txtHashPort = new FlatTextField();
	private FlatTextField txtGatePort = new FlatTextField();
	private FlatTextField txtEntrancePort = new FlatTextField();

	private FlatTextField txtGateSuckerPort = new FlatTextField();
	private FlatTextField txtGateDispatcherPort = new FlatTextField();
	private FlatTextField txtEntranceSuckerPort = new FlatTextField();
	private FlatTextField txtEntranceDispatcherPort = new FlatTextField();

	/** HOME管理节点 **/
	private JCheckBox chkHome = new JCheckBox();
	private FlatTextField txtHomeIP = new FlatTextField();
	private FlatTextField txtHomePort = new FlatTextField();

	private JCheckBox chkHome1 = new JCheckBox();
	private FlatTextField txtHome1IP = new FlatTextField();
	private FlatTextField txtHome1Port = new FlatTextField();

	private JCheckBox chkHome2 = new JCheckBox();
	private FlatTextField txtHome2IP = new FlatTextField();
	private FlatTextField txtHome2Port = new FlatTextField();

	private JCheckBox chkHomeLog = new JCheckBox();

	private JLabel lblHomeLogData = new JLabel();
	private FlatTextField txtHomeLogDataPort = new FlatTextField();
	private FlatTextField txtHomeTigDataPort = new FlatTextField();
	private FlatTextField txtHomeBillDataPort = new FlatTextField();

	private JLabel lblHomeLogBuild = new JLabel();
	private FlatTextField txtHomeLogBuildPort = new FlatTextField();
	private FlatTextField txtHomeTigBuildPort = new FlatTextField();
	private FlatTextField txtHomeBillBuildPort = new FlatTextField();

	private JLabel lblHomeLogWork = new JLabel();
	private FlatTextField txtHomeLogWorkPort = new FlatTextField();
	private FlatTextField txtHomeTigWorkPort = new FlatTextField();
	private FlatTextField txtHomeBillWorkPort = new FlatTextField();

	private JLabel lblHomeLogCall = new JLabel();
	private FlatTextField txtHomeLogCallPort = new FlatTextField();
	private FlatTextField txtHomeTigCallPort = new FlatTextField();
	private FlatTextField txtHomeBillCallPort = new FlatTextField();

	private JCheckBox chkDataMaster = new JCheckBox();
	private JCheckBox chkDataSlave = new JCheckBox();
	private JCheckBox chkBuild = new JCheckBox();
	private JCheckBox chkWork = new JCheckBox();
	private JCheckBox chkCall = new JCheckBox();

	private FlatTextField txtHomeLogPort = new FlatTextField();
	private FlatTextField txtDataMasterPort = new FlatTextField();
	private FlatTextField txtDataSlavePort = new FlatTextField();
	private FlatTextField txtBuildPort = new FlatTextField();
	private FlatTextField txtWorkPort = new FlatTextField();
	private FlatTextField txtCallPort = new FlatTextField();

	private FlatTextField txtCallSuckerPort = new FlatTextField();
	private FlatTextField txtCallDispatcherPort = new FlatTextField();

//	/** 导出目录 **/
//	private File exportRoot;
//	
//	/** 导入目录 **/
//	private File importRoot;

	/**
	 * @param frame
	 * @param modal
	 */
	public RayAddressScriptDialog() {
		super();
	}
	
	/**
	 * 关闭窗口
	 */
	public void closeWindow() {
		super.closeWindow();
		RayAddressScriptDialog.selfHandle = null;
	}
	
	/**
	 * 设置按纽文本
	 * @param button
	 * @param text
	 */
	private void setButtonText(AbstractButton button, String text) {
		button.setText(text);
	}

	private void setToolTipText(JComponent jc, String tooltip) {
		jc.setToolTipText(tooltip);
	}

//	private void setLabelText(JLabel label, String text) {
//		label.setText(text);
//	}
	
	/**
	 * 设置标签文本
	 * @param label 标签
	 * @param horizontalAlignment 排列位置
	 * @param text 显示文本 
	 */
	protected void setLabelText(JLabel label, int horizontalAlignment, String text) {
		label.setHorizontalAlignment(horizontalAlignment);
		label.setText(text);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		click(e);
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
				closeWindow();
			}
		} else if(e.getSource() == cmdImport) {
			importFrom();
		} else if (e.getSource() == cmdExport) {
			exportTo();
		} else if (e.getSource() == cmdCreate) {
			// 检查和关闭它！
			boolean success = check();
			if (success) {
				success = confirm();
			}
			if (success) {
				// 生成
				create();
			}
		} else if(e.getSource() == cmdAll) {
			all();
		}

		// 处理集群节点的点击，TOP主节点的选择受到子节点的控制
		// TOP集群两个节点，不考虑TOP主节点
		else if (e.getSource() == chkTop1) {
			doTop1Site();
		} else if (e.getSource() == chkTop2) {
			doTop2Site();
		}
		// BANK集群三个管理节点
		else if (e.getSource() == chkBank) {
			doBankSite();
		} else if (e.getSource() == chkBank1) {
			doBank1Site();
		} else if (e.getSource() == chkBank2) {
			doBank2Site();
		}
		// HOME集群三个管理节点
		else if (e.getSource() == chkHome) {
			doHomeSite();
		} else if (e.getSource() == chkHome1) {
			doHome1Site();
		} else if (e.getSource() == chkHome2) {
			doHome2Site();
		}
		// 其它选择按纽
		else {
			// 执行站点按纽操作
			adjust();
		}
	}

	/**
	 * 处理TOP1从节点
	 */
	private void doTop1Site() {
		boolean selected = chkTop1.isSelected();
		if (selected) {
			chkTop.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理TOP2从节点
	 */
	private void doTop2Site() {
		boolean selected = chkTop2.isSelected();
		if (selected) {
			chkTop.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理BANK主节点
	 */
	private void doBankSite() {
		boolean selected = chkBank.isSelected();
		// 选中BANK节点，TOP节点也在打开。
		// 取消BANK节点，下属节点全部关闭
		if (selected) {
			chkTop.setSelected(true);
		} else {
			// 关闭下属的节点
			chkBankLog.setSelected(false);
			chkAccount.setSelected(false);
			chkHash.setSelected(false);
			chkGate.setSelected(false);
			chkEntrance.setSelected(false);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理BANK1从节点
	 */
	private void doBank1Site() {
		boolean selected = chkBank1.isSelected();
		if (selected) {
			chkTop.setSelected(true);
			chkBank.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理BANK2从节点
	 */
	private void doBank2Site() {
		boolean selected = chkBank2.isSelected();
		if (selected) {
			chkTop.setSelected(true);
			chkBank.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理HOME主节点
	 */
	private void doHomeSite() {
		boolean selected = chkHome.isSelected();
		// 选中HOME节点，TOP节点也在打开。
		// 取消HOME节点，下属节点全部关闭
		if (selected) {
			chkTop.setSelected(true);
		} else {
			// 关闭下属的节点
			chkHomeLog.setSelected(false);
			chkDataMaster.setSelected(false);
			chkDataSlave.setSelected(false);
			chkBuild.setSelected(false);
			chkWork.setSelected(false);
			chkCall.setSelected(false);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理HOME1从节点
	 */
	private void doHome1Site() {
		boolean selected = chkHome1.isSelected();
		if (selected) {
			chkTop.setSelected(true);
			chkHome.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 处理HOME2从节点
	 */
	private void doHome2Site() {
		boolean selected = chkHome2.isSelected();
		if (selected) {
			chkTop.setSelected(true);
			chkHome.setSelected(true);
		}
		// 执行调整操作
		adjust();
	}

	/**
	 * 执行分析，打开/关闭文本框
	 */
	private void adjust() {
		// 确定两个IP地址框有效
		int inside = 0;
		int gateway = 0;
		int top = 0;
		int bank = 0;
		int home = 0;

		// top cluster
		if (chkTopLog.isSelected()) {
			inside++;
			top++; // TOP主节点要打开
		}

		// bank cluster
		if (chkBankLog.isSelected()) {
			inside++;
			bank++; // BANK主节点要打开
		}
		if (chkAccount.isSelected()) {
			inside++;
			bank++;
		}
		if (chkHash.isSelected()) {
			inside++;
			bank++;
		}
		if (chkGate.isSelected()) {
			bank++;
			inside++;
			gateway++;
		}
		if (chkEntrance.isSelected()) {
			bank++;
			inside++;
			gateway++;
		}

		// home cluster
		if (chkHomeLog.isSelected()) {
			home++; // HOME主节点要打开
			inside++;
		}
		if (chkDataMaster.isSelected()) {
			home++;
			inside++;
		}
		if (chkDataSlave.isSelected()) {
			home++;
			inside++;
		}
		if (chkBuild.isSelected()) {
			inside++;
		}
		if (chkWork.isSelected()) {
			home++;
			inside++;
			gateway++;
		}
		if (chkCall.isSelected()) {
			home++;
			inside++;
			gateway++;
		}

		// 有效！
		txtPrivateIP.setEnabled(inside > 0);
		txtGatewayIP.setEnabled(gateway > 0);

		if (top > 0) {
			if (!chkTop.isSelected())
				chkTop.setSelected(true);
		}
		if (bank > 0) {
			if (!chkBank.isSelected())
				chkBank.setSelected(true);
		}
		if (home > 0) {
			if (!chkHome.isSelected())
				chkHome.setSelected(true);
		}

		// 调整文本框的显示
		adjustTextFields();
	}

	/**
	 * 调整文本框
	 */
	private void adjustTextFields() {
		// TOP 集群
		txtTopIP.setEnabled(chkTop.isSelected());
		txtTopPort.setEnabled(chkTop.isSelected());
		txtTop1IP.setEnabled(chkTop1.isSelected());
		txtTop1Port.setEnabled(chkTop1.isSelected());
		txtTop2IP.setEnabled(chkTop2.isSelected());
		txtTop2Port.setEnabled(chkTop2.isSelected());

		txtTopLogPort.setEnabled(chkTopLog.isSelected());
		txtTopLogBankPort.setEnabled(chkTopLog.isSelected());
		txtTopTigBankPort.setEnabled(chkTopLog.isSelected());
		txtTopBillBankPort.setEnabled(chkTopLog.isSelected());
		txtTopLogHomePort.setEnabled(chkTopLog.isSelected());
		txtTopTigHomePort.setEnabled(chkTopLog.isSelected());
		txtTopBillHomePort.setEnabled(chkTopLog.isSelected());

		// BANK集群
		txtBankIP.setEnabled(chkBank.isSelected());
		txtBankPort.setEnabled(chkBank.isSelected());
		txtBank1IP.setEnabled(chkBank1.isSelected());
		txtBank1Port.setEnabled(chkBank1.isSelected());
		txtBank2IP.setEnabled(chkBank2.isSelected());
		txtBank2Port.setEnabled(chkBank2.isSelected());

		txtBankLogPort.setEnabled(chkBankLog.isSelected());
		txtBankLogAccountPort.setEnabled(chkBankLog.isSelected());
		txtBankTigAccountPort.setEnabled(chkBankLog.isSelected());
		txtBankBillAccountPort.setEnabled(chkBankLog.isSelected());
		
		txtBankLogHashPort.setEnabled(chkBankLog.isSelected());
		txtBankTigHashPort.setEnabled(chkBankLog.isSelected());
		txtBankBillHashPort.setEnabled(chkBankLog.isSelected());
		
		txtBankLogGatePort.setEnabled(chkBankLog.isSelected());
		txtBankTigGatePort.setEnabled(chkBankLog.isSelected());
		txtBankBillGatePort.setEnabled(chkBankLog.isSelected());
		
		txtBankLogEntrancePort.setEnabled(chkBankLog.isSelected());
		txtBankTigEntrancePort.setEnabled(chkBankLog.isSelected());
		txtBankBillEntrancePort.setEnabled(chkBankLog.isSelected());

		txtAccountPort.setEnabled(chkAccount.isSelected());
		txtHashPort.setEnabled(chkHash.isSelected());
		txtGatePort.setEnabled(chkGate.isSelected());
		txtGateSuckerPort.setEnabled(chkGate.isSelected());
		txtGateDispatcherPort.setEnabled(chkGate.isSelected());
		txtEntrancePort.setEnabled(chkEntrance.isSelected());
		txtEntranceSuckerPort.setEnabled(chkEntrance.isSelected());
		txtEntranceDispatcherPort.setEnabled(chkEntrance.isSelected());

		// HOME集群
		txtHomeIP.setEnabled(chkHome.isSelected());
		txtHomePort.setEnabled(chkHome.isSelected());
		txtHome1IP.setEnabled(chkHome1.isSelected());
		txtHome1Port.setEnabled(chkHome1.isSelected());
		txtHome2IP.setEnabled(chkHome2.isSelected());
		txtHome2Port.setEnabled(chkHome2.isSelected());

		txtHomeLogPort.setEnabled(chkHomeLog.isSelected());
		txtHomeLogDataPort.setEnabled(chkHomeLog.isSelected());
		txtHomeTigDataPort.setEnabled(chkHomeLog.isSelected());
		txtHomeBillDataPort.setEnabled(chkHomeLog.isSelected());
		
		txtHomeLogBuildPort.setEnabled(chkHomeLog.isSelected());
		txtHomeTigBuildPort.setEnabled(chkHomeLog.isSelected());
		txtHomeBillBuildPort.setEnabled(chkHomeLog.isSelected());
		
		txtHomeLogWorkPort.setEnabled(chkHomeLog.isSelected());
		txtHomeTigWorkPort.setEnabled(chkHomeLog.isSelected());
		txtHomeBillWorkPort.setEnabled(chkHomeLog.isSelected());
		
		txtHomeLogCallPort.setEnabled(chkHomeLog.isSelected());
		txtHomeTigCallPort.setEnabled(chkHomeLog.isSelected());
		txtHomeBillCallPort.setEnabled(chkHomeLog.isSelected());

		txtDataMasterPort.setEnabled(chkDataMaster.isSelected());
		txtDataSlavePort.setEnabled(chkDataSlave.isSelected());
		txtBuildPort.setEnabled(chkBuild.isSelected());
		txtWorkPort.setEnabled(chkWork.isSelected());
		txtCallPort.setEnabled(chkCall.isSelected());
		txtCallSuckerPort.setEnabled(chkCall.isSelected());
		txtCallDispatcherPort.setEnabled(chkCall.isSelected());
	}

	/**
	 * 选中全部
	 */
	private void all() {
		boolean yes = cmdAll.isSelected();

		// 给选择按纽设置事件
		JCheckBox[] boxes = new JCheckBox[] {
				// top cluster
				chkTop, chkTop1, chkTop2, chkTopLog, 
				// bank cluster
				chkBank, chkBank1, chkBank2, chkBankLog, 
				chkAccount, chkHash, chkGate, chkEntrance, 
				// home cluster
				chkHome, chkHome1, chkHome2, chkHomeLog, 
				chkDataMaster, chkDataSlave, chkBuild, chkWork, chkCall };
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].setSelected(yes);
		}

		// 有效/无效
		FlatTextField[] fields = new FlatTextField[] { txtPrivateIP, txtGatewayIP,
				// top cluster
				txtTopIP, txtTop1IP, txtTop2IP, txtTopPort, txtTop1Port, txtTop2Port, txtTopLogPort,
				txtTopLogBankPort, txtTopTigBankPort, txtTopBillBankPort, 
				txtTopLogHomePort, txtTopTigHomePort, txtTopBillHomePort,
				// bank cluster
				txtBankIP, txtBank1IP, txtBank2IP, txtBankPort, txtBank1Port, txtBank2Port, txtBankLogPort,
				txtBankLogAccountPort, txtBankTigAccountPort, txtBankBillAccountPort,
				txtBankLogHashPort, txtBankTigHashPort, txtBankBillHashPort,
				txtBankLogGatePort, txtBankTigGatePort, txtBankBillGatePort,
				txtBankLogEntrancePort, txtBankTigEntrancePort, txtBankBillEntrancePort,
				txtAccountPort, txtHashPort, 
				txtGatePort, txtGateSuckerPort, txtGateDispatcherPort,
				txtEntrancePort, txtEntranceSuckerPort, txtEntranceDispatcherPort,
				// home cluster
				txtHomeIP, txtHome1IP, txtHome2IP, txtHomePort, txtHome1Port, txtHome2Port, txtHomeLogPort, 
				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeBillDataPort,
				txtHomeLogBuildPort, txtHomeTigBuildPort, txtHomeBillBuildPort,
				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeBillWorkPort, 
				txtHomeLogCallPort, txtHomeTigCallPort, txtHomeBillCallPort,
				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort, 
				txtCallPort, txtCallSuckerPort, txtCallDispatcherPort };
		for (int i = 0; i < fields.length; i++) {
			fields[i].setEnabled(yes);
		}
	}

	/**
	 * 清除参数
	 * @param import_export
	 */
	private void reset(boolean import_export) {
		// 重置
		cmdAll.setSelected(false);
		if (import_export) {
			lblExport.setText("");
			lblImport.setText("");
		}

		// 清空
		FlatTextField[] fields = new FlatTextField[] { txtPrivateIP, txtGatewayIP,
				// top cluster
				txtTopIP, txtTop1IP, txtTop2IP, txtTopPort, txtTop1Port, txtTop2Port, txtTopLogPort,
				txtTopLogBankPort, txtTopTigBankPort, txtTopBillBankPort, 
				txtTopLogHomePort, txtTopTigHomePort, txtTopBillHomePort,
				// bank cluster
				txtBankIP, txtBank1IP, txtBank2IP, txtBankPort, txtBank1Port, txtBank2Port, txtBankLogPort,
				txtBankLogAccountPort, txtBankTigAccountPort, txtBankBillAccountPort,
				txtBankLogHashPort, txtBankTigHashPort, txtBankBillHashPort,
				txtBankLogGatePort, txtBankTigGatePort, txtBankBillGatePort, 
				txtBankLogEntrancePort, txtBankTigEntrancePort, txtBankBillEntrancePort,
				txtAccountPort, txtHashPort, 
				txtGatePort, txtGateSuckerPort, txtGateDispatcherPort,
				txtEntrancePort, txtEntranceSuckerPort, txtEntranceDispatcherPort,
				// home cluster
				txtHomeIP, txtHome1IP, txtHome2IP, txtHomePort, txtHome1Port, txtHome2Port, txtHomeLogPort, 
				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeBillDataPort, 
				txtHomeLogBuildPort, txtHomeTigBuildPort, txtHomeBillBuildPort,
				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeBillWorkPort, 
				txtHomeLogCallPort, txtHomeTigCallPort, txtHomeBillCallPort,
				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort, 
				txtCallPort, txtCallSuckerPort, txtCallDispatcherPort };
		for (int i = 0; i < fields.length; i++) {
			fields[i].setText("");
		}
		// 恢复空
		all();
	}
	
	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private void reset() {
		reset(true);
	}

	/**
	 * 判断文件框无效！
	 * 一种可能：不是IP地址
	 * @param sub
	 * @return
	 */
	private boolean isDisableIP(FlatTextField sub) {
		// 非有效状态，条件不成立
		if (!sub.isEnabled()) {
			return false;
		}

		// 两种无效状态：空值，不是IP地址，返回假
		String text = sub.getText().trim();
		return (text.isEmpty() || !Address.isIPStyle(text));
	}

	/**
	 * 判断是有效的端口号
	 * @param sub
	 * @return
	 */
	private boolean isDisablePort(FlatTextField sub) {
		// 非有效状态，条件不成立
		if (!sub.isEnabled()) {
			return false;
		}

		String text = sub.getText().trim();
		// 不是整数，返回假
		if (!ConfigParser.isInteger(text)) {
			return true;
		}
		// 解析端口号
		int port = ConfigParser.splitInteger(text, -1);
		if (port < 0 || port >= 0xffff) {
			return true;
		}
		return false;
	}

	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private boolean check() {
		// 两个IP地址
		if (isDisableIP(txtPrivateIP)) {
			txtPrivateIP.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtGatewayIP)) {
			txtGatewayIP.requestFocus();
			warning();
			return false;
		}

		// 检查TOP集群
		if (isDisableIP(txtTopIP)) {
			txtTopIP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopPort)) {
			txtTopPort.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtTop1IP)) {
			txtTop1IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTop1Port)) {
			txtTop1Port.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtTop2IP)) {
			txtTop2IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTop2Port)) {
			txtTop2Port.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopLogPort)) {
			txtTopLogPort.requestFocus();
			warning();
			return false;
		}

		if (isDisablePort(txtTopLogBankPort)) {
			txtTopLogBankPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopTigBankPort)) {
			txtTopTigBankPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopBillBankPort)) {
			txtTopBillBankPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopLogHomePort)) {
			txtTopLogHomePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopTigHomePort)) {
			txtTopTigHomePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtTopBillHomePort)) {
			txtTopBillHomePort.requestFocus();
			warning();
			return false;
		}

		// 检查BANK集群
		if (isDisableIP(txtBankIP)) {
			txtBankIP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankPort)) {
			txtBankPort.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtBank1IP)) {
			txtBank1IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBank1Port)) {
			txtBank1Port.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtBank2IP)) {
			txtBank2IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBank2Port)) {
			txtBank2Port.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankLogPort)) {
			txtBankLogPort.requestFocus();
			warning();
			return false;
		}

		if (isDisablePort(txtBankLogAccountPort)) {
			txtBankLogAccountPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankTigAccountPort)) {
			txtBankTigAccountPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankBillAccountPort)) {
			txtBankBillAccountPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankLogHashPort)) {
			txtBankLogHashPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankTigHashPort)) {
			txtBankTigHashPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankBillHashPort)) {
			txtBankBillHashPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankLogGatePort)) {
			txtBankLogGatePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankTigGatePort)) {
			txtBankTigGatePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankBillGatePort)) {
			txtBankBillGatePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankLogEntrancePort)) {
			txtBankLogEntrancePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankTigEntrancePort)) {
			txtBankTigEntrancePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBankBillEntrancePort)) {
			txtBankBillEntrancePort.requestFocus();
			warning();
			return false;
		}

		if (isDisablePort(txtAccountPort)) {
			txtAccountPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHashPort)) {
			txtHashPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtGatePort)) {
			txtGatePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtGateSuckerPort)) {
			txtGateSuckerPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtGateDispatcherPort)) {
			txtGateDispatcherPort.requestFocus();
			warning();
			return false;
		}

		if (isDisablePort(txtEntrancePort)) {
			txtEntrancePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtEntranceSuckerPort)) {
			txtEntranceSuckerPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtEntranceDispatcherPort)) {
			txtEntranceDispatcherPort.requestFocus();
			warning();
			return false;
		}

		// 检查HOME集群
		if (isDisableIP(txtHomeIP)) {
			txtHomeIP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomePort)) {
			txtHomePort.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtHome1IP)) {
			txtHome1IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHome1Port)) {
			txtHome1Port.requestFocus();
			warning();
			return false;
		}
		if (isDisableIP(txtHome2IP)) {
			txtHome2IP.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHome2Port)) {
			txtHome2Port.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeLogPort)) {
			txtHomeLogPort.requestFocus();
			warning();
			return false;
		}

		if (isDisablePort(txtHomeLogDataPort)) {
			txtHomeLogDataPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeTigDataPort)) {
			txtHomeTigDataPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeBillDataPort)) {
			txtHomeBillDataPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeLogBuildPort)) {
			txtHomeLogBuildPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeTigBuildPort)) {
			txtHomeTigBuildPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeBillBuildPort)) {
			txtHomeBillBuildPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeLogWorkPort)) {
			txtHomeLogWorkPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeTigWorkPort)) {
			txtHomeTigWorkPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeBillWorkPort)) {
			txtHomeBillWorkPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeLogCallPort)) {
			txtHomeLogCallPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeTigCallPort)) {
			txtHomeTigCallPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtHomeBillCallPort)) {
			txtHomeBillCallPort.requestFocus();
			warning();
			return false;
		}
		
		if (isDisablePort(txtDataMasterPort)) {
			txtDataMasterPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtDataSlavePort)) {
			txtDataSlavePort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtBuildPort)) {
			txtBuildPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtWorkPort)) {
			txtWorkPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtCallPort)) {
			txtCallPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtCallSuckerPort)) {
			txtCallSuckerPort.requestFocus();
			warning();
			return false;
		}
		if (isDisablePort(txtCallDispatcherPort)) {
			txtCallDispatcherPort.requestFocus();
			warning();
			return false;
		}
		
		// 检查重复的端口号
		FlatTextField[] ports = new FlatTextField[] {
				// top cluster
				txtTopPort, txtTop1Port, txtTop2Port, txtTopLogPort, 
				txtTopLogBankPort, txtTopTigBankPort, txtTopBillBankPort, 
				txtTopLogHomePort, txtTopTigHomePort, txtTopBillHomePort,
				// bank cluster
				txtBankPort, txtBank1Port, txtBank2Port, txtBankLogPort, 
				txtBankLogAccountPort, txtBankTigAccountPort, txtBankBillAccountPort,
				txtBankLogHashPort, txtBankTigHashPort, txtBankBillHashPort,
				txtBankLogGatePort, txtBankTigGatePort, txtBankBillGatePort, 
				txtBankLogEntrancePort, txtBankTigEntrancePort, txtBankBillEntrancePort,
				txtAccountPort, txtHashPort, txtGatePort, txtEntrancePort, 
				txtGateSuckerPort, txtGateDispatcherPort,
				txtEntranceSuckerPort, txtEntranceDispatcherPort,
				// home cluster
				txtHomePort, txtHome1Port, txtHome2Port, txtHomeLogPort, 
				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeBillDataPort, 
				txtHomeLogBuildPort, txtHomeTigBuildPort, txtHomeBillBuildPort,
				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeBillWorkPort,
				txtHomeLogCallPort, txtHomeTigCallPort, txtHomeBillCallPort,
				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort, txtCallPort,
				txtCallSuckerPort, txtCallDispatcherPort };
		// 保存端口号
		java.util.TreeSet<Integer> array = new java.util.TreeSet<Integer>();
		for(int i =0; i < ports.length; i++) {
			FlatTextField sub = ports[i];
			// 非有效状态，忽略
			if (!sub.isEnabled()) {
				continue;
			}

			// 重复
			String text = sub.getText().trim();
			int port = ConfigParser.splitInteger(text, -1);
			if (array.contains(port)) {
				sub.requestFocus();
				repeatPort(port);
				return false;
			}
			// 保存端口
			array.add(port);
		}
		// 全部端口无效，忽略它!
		if (array.isEmpty()) {
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

//	/**
//	 * 生成脚本文件
//	 * @return 成功返回真，否则假
//	 */
//	private boolean create() {
//		// 顺序
//		boolean success = createScript();
//		if (!success) {
//			return false;
//		}
//
//		// 提示完成
//		String title = UIManager.getString("AddressScriptDialog.FinishedTitle");
//		String sub = UIManager.getString("AddressScriptDialog.FinishedContent");
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
		boolean success = createScript();
		if (!success) {
			return false;
		}

		// 提示完成
		String title = UIManager.getString("AddressScriptDialog.FinishedTitle");
		String sub = UIManager.getString("AddressScriptDialog.FinishedContent");
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
	 * 生成"address.sh"脚本文件
	 * @param suffix 后缀信息
	 * @return 整个文档
	 */
	private String buildAddressScript(String suffix) {
		StringBuilder buff = new StringBuilder();

		// 写入前缀
		buff.append("#!/bin/sh\n\n");
		
		// 保存后缀
		buff.append(suffix);

		return buff.toString();
	}
	
	/**
	 * 生成"check.sh"脚本文件
	 * @param suffix 后缀信息
	 * @return 整个文档
	 */
	private String buildCheckScript(String suffix) {
		StringBuilder buff = new StringBuilder();

		// 写入前缀
		buff.append("#!/bin/sh\n\n");

		buff.append("PWD_PATH=`pwd`\n");
		buff.append("source $PWD_PATH/address.sh\n\n");

		// 保存后缀
		buff.append(suffix);

		return buff.toString();
	}

	/**
	 * 导入环境
	 * @param buff
	 * @param tag
	 * @param filed
	 */
	private void buildIP(StringBuilder buff, String tag, FlatTextField filed) {
		String info = filed.getText().trim();
		String s = String.format("export %s=\"%s\"\n", tag, info);

		// 保存单元信息
		buff.append(s);
	}
	
	/**
	 * 生成端口
	 * @param tag
	 * @param label
	 * @param field
	 * @param address
	 * @param check
	 */
	private void buildPort(String tag, String label, FlatTextField field, 
			StringBuilder address, StringBuilder check, StringBuilder checkport) {
		String info = field.getText().trim();
		// address.sh信息
		String s = String.format("export %s=\"%s\"\n", tag, info);
		address.append(s);
		
		// 判断是TOP/BANK/HOME节点时...
		if (label.matches("^\\s*(?i)(top|bank|home)\\s*$")) {
			String text = String.format("echo \"check %s cluster...\"\n\n", label);
			check.append(text);
			checkport.append(text);
		}

		// check.sh信息
		check.append(String.format("helo=`netstat -tunlp|grep $%s`\n", tag));
		check.append("if [ \"$helo\" = \"\" ]; then\n");
		check.append(String.format("	echo \" $%s is idle (%s)\"\n", tag, label));
		check.append("else\n");
		check.append(String.format("	echo \" $%s is bound! (%s)\"\n", tag, label));
		check.append("fi\n\n");

		// checkport.sh 信息
		checkport.append(String.format("helo=`lsof -i:$%s`\n", tag));
		checkport.append("if [ \"$helo\" = \"\" ]; then\n");
		checkport.append(String.format("	echo \" $%s is idle (%s)\"\n", tag, label));
		checkport.append("else\n");
		checkport.append(String.format("	echo \" $%s is bound! (%s)\"\n", tag, label));
		checkport.append("fi\n\n");
	}
	
	/**
	 * 导入文本
	 * @param key
	 * @param value
	 */
	private void importText(String key, String value) {
		if (key.equals("LOCAL_PRIVATE_IP")) {
			txtPrivateIP.setText(value);
		} else if (key.equals("LOCAL_PUBLIC_IP")) {
			txtGatewayIP.setText(value);
		}

		// top cluster
		else if(key.equals("HUB_TOP_IP")) {
			txtTopIP.setText(value);
		}
		else if(key.equals("HUB_TOP_PORT")) {
			txtTopPort.setText(value);
		}
		else if(key.equals("HUB_TOP1_IP")) {
			txtTop1IP.setText(value);
		}
		else if(key.equals("HUB_TOP1_PORT")) {
			txtTop1Port.setText(value);
		}
		else if(key.equals("HUB_TOP2_IP")) {
			txtTop2IP.setText(value);
		}
		else if(key.equals("HUB_TOP2_PORT")) {
			txtTop2Port.setText(value);
		}
		else if(key.equals("SUB_TOPLOG_PORT")) {
			txtTopLogPort.setText(value);
		}
		else if(key.equals("SUB_TOPLOG_BANK_PORT")) {
			txtTopLogBankPort.setText(value);
		}
		else if(key.equals("SUB_TOPTIG_BANK_PORT")) {
			txtTopTigBankPort.setText(value);
		}
		else if(key.equals("SUB_TOPBILL_BANK_PORT")) {
			txtTopBillBankPort.setText(value);
		}
		else if(key.equals("SUB_TOPLOG_HOME_PORT")) {
			txtTopLogHomePort.setText(value);
		}
		else if(key.equals("SUB_TOPTIG_HOME_PORT")) {
			txtTopTigHomePort.setText(value);
		}
		else if(key.equals("SUB_TOPBILL_HOME_PORT")) {
			txtTopBillHomePort.setText(value);
		}
		
		// bank cluster
		else if(key.equals("HUB_BANK_IP")) {
			txtBankIP.setText(value);
		}
		else if(key.equals("HUB_BANK_PORT")) {
			txtBankPort.setText(value);
		}
		else if(key.equals("HUB_BANK1_IP")) {
			txtBank1IP.setText(value);
		}
		else if(key.equals("HUB_BANK1_PORT")) {
			txtBank1Port.setText(value);
		}
		else if(key.equals("HUB_BANK2_IP")) {
			txtBank2IP.setText(value);
		}
		else if(key.equals("HUB_BANK2_PORT")) {
			txtBank2Port.setText(value);
		}
		else if(key.equals("SUB_BANKLOG_PORT")) {
			txtBankLogPort.setText(value);
		}
		else if(key.equals("SUB_BANKLOG_ACCOUNT_PORT")) {
			txtBankLogAccountPort.setText(value);
		}
		else if(key.equals("SUB_BANKTIG_ACCOUNT_PORT")) {
			txtBankTigAccountPort.setText(value);
		}
		else if(key.equals("SUB_BANKBILL_ACCOUNT_PORT")) {
			txtBankBillAccountPort.setText(value);
		}
		else if(key.equals("SUB_BANKLOG_HASH_PORT")) {
			txtBankLogHashPort.setText(value);
		}
		else if(key.equals("SUB_BANKTIG_HASH_PORT")) {
			txtBankTigHashPort.setText(value);
		}
		else if(key.equals("SUB_BANKBILL_HASH_PORT")) {
			txtBankBillHashPort.setText(value);
		}
		else if(key.equals("SUB_BANKLOG_GATE_PORT")) {
			txtBankLogGatePort.setText(value);
		}
		else if(key.equals("SUB_BANKTIG_GATE_PORT")) {
			txtBankTigGatePort.setText(value);
		}
		else if(key.equals("SUB_BANKBILL_GATE_PORT")) {
			txtBankBillGatePort.setText(value);
		}
		else if(key.equals("SUB_BANKLOG_ENTRANCE_PORT")) {
			txtBankLogEntrancePort.setText(value);
		}
		else if(key.equals("SUB_BANKTIG_ENTRANCE_PORT")) {
			txtBankTigEntrancePort.setText(value);
		}
		else if(key.equals("SUB_BANKBILL_ENTRANCE_PORT")) {
			txtBankBillEntrancePort.setText(value);
		}
		else if(key.equals("SUB_ACCOUNT_PORT")) {
			txtAccountPort.setText(value);
		}
		else if(key.equals("SUB_HASH_PORT")) {
			txtHashPort.setText(value);
		}
		else if(key.equals("SUB_GATE_PORT")) {
			txtGatePort.setText(value);
		}
		else if(key.equals("SUB_GATE_SUCKER_PORT")) {
			txtGateSuckerPort.setText(value);
		}
		else if(key.equals("SUB_GATE_DISPATCHER_PORT")) {
			txtGateDispatcherPort.setText(value);
		}
		else if(key.equals("SUB_ENTRANCE_PORT")) {
			txtEntrancePort.setText(value);
		}
		else if(key.equals("SUB_ENTRANCE_SUCKER_PORT")) {
			txtEntranceSuckerPort.setText(value);
		}
		else if(key.equals("SUB_ENTRANCE_DISPATCHER_PORT")) {
			txtEntranceDispatcherPort.setText(value);
		}

		// home cluster
		else if(key.equals("HUB_HOME_IP")) {
			txtHomeIP.setText(value);
		}
		else if(key.equals("HUB_HOME_PORT")) {
			txtHomePort.setText(value);
		}
		else if(key.equals("HUB_HOME1_IP")) {
			txtHome1IP.setText(value);
		}
		else if(key.equals("HUB_HOME1_PORT")) {
			txtHome1Port.setText(value);
		}
		else if(key.equals("HUB_HOME2_IP")) {
			txtHome2IP.setText(value);
		}
		else if(key.equals("HUB_HOME2_PORT")) {
			txtHome2Port.setText(value);
		}
		else if(key.equals("SUB_HOMELOG_PORT")) {
			txtHomeLogPort.setText(value);
		}
		else if(key.equals("SUB_HOMELOG_DATA_PORT")) {
			txtHomeLogDataPort.setText(value);
		}
		else if(key.equals("SUB_HOMETIG_DATA_PORT")) {
			txtHomeTigDataPort.setText(value);
		}
		else if(key.equals("SUB_HOMEBILL_DATA_PORT")) {
			txtHomeBillDataPort.setText(value);
		}
		else if(key.equals("SUB_HOMELOG_BUILD_PORT")) {
			txtHomeLogBuildPort.setText(value);
		}
		else if(key.equals("SUB_HOMETIG_BUILD_PORT")) {
			txtHomeTigBuildPort.setText(value);
		}
		else if(key.equals("SUB_HOMEBILL_BUILD_PORT")) {
			txtHomeBillBuildPort.setText(value);
		}
		else if(key.equals("SUB_HOMELOG_WORK_PORT")) {
			txtHomeLogWorkPort.setText(value);
		}
		else if(key.equals("SUB_HOMETIG_WORK_PORT")) {
			txtHomeTigWorkPort.setText(value);
		}
		else if(key.equals("SUB_HOMEBILL_WORK_PORT")) {
			txtHomeBillWorkPort.setText(value);
		}
		else if(key.equals("SUB_HOMELOG_CALL_PORT")) {
			txtHomeLogCallPort.setText(value);
		}
		else if(key.equals("SUB_HOMETIG_CALL_PORT")) {
			txtHomeTigCallPort.setText(value);
		}
		else if(key.equals("SUB_HOMEBILL_CALL_PORT")) {
			txtHomeBillCallPort.setText(value);
		}
		else if(key.equals("SUB_DATA_PORT")) {
			txtDataMasterPort.setText(value);
		}
		else if(key.equals("SUB_SLAVE_PORT")) {
			txtDataSlavePort.setText(value);
		}
		else if(key.equals("SUB_BUILD_PORT")) {
			txtBuildPort.setText(value);
		}
		else if(key.equals("SUB_WORK_PORT")) {
			txtWorkPort.setText(value);
		}
		else if(key.equals("SUB_CALL_PORT")) {
			txtCallPort.setText(value);
		}
		else if(key.equals("SUB_CALL_SUCKER_PORT")) {
			txtCallSuckerPort.setText(value);
		}
		else if(key.equals("SUB_CALL_DISPATCHER_PORT")) {
			txtCallDispatcherPort.setText(value);
		}
	}

	/**
	 * 生成脚本文件
	 * @return
	 */
	private boolean createScript() {
		StringBuilder address = new StringBuilder();
		StringBuilder check = new StringBuilder();// 检测端口
		StringBuilder checkport = new StringBuilder();// 检测端口

		// IP地址
		if (txtPrivateIP.isEnabled()) {
			buildIP(address, "LOCAL_PRIVATE_IP", txtPrivateIP);
		}
		if (txtGatewayIP.isEnabled()) {
			buildIP(address, "LOCAL_PUBLIC_IP", txtGatewayIP);
		}
		address.append("\n");

		// TOP集群
		if (txtTopIP.isEnabled()) {
			buildIP(address, "HUB_TOP_IP", txtTopIP);
		}
		if (txtTopPort.isEnabled()) {
			buildPort("HUB_TOP_PORT", "top", txtTopPort, address, check, checkport);
		}
		if (txtTop1IP.isEnabled()) {
			buildIP(address, "HUB_TOP1_IP", txtTop1IP);
		}
		if (txtTop1Port.isEnabled()) {
			buildPort("HUB_TOP1_PORT", "top1", txtTop1Port, address, check, checkport);
		}
		if (txtTop2IP.isEnabled()) {
			buildIP(address, "HUB_TOP2_IP", txtTop2IP);
		}
		if (txtTop2Port.isEnabled()) {
			buildPort("HUB_TOP2_PORT", "top2", txtTop2Port, address, check, checkport);
		}
		if (txtTopLogPort.isEnabled()) {
			buildPort("SUB_TOPLOG_PORT", "top log", txtTopLogPort, address, check, checkport);
		}

		if (txtTopLogBankPort.isEnabled()) {
			buildPort("SUB_TOPLOG_BANK_PORT", "top log/bank", txtTopLogBankPort, address, check, checkport);
		}
		if (txtTopTigBankPort.isEnabled()) {
			buildPort( "SUB_TOPTIG_BANK_PORT", "top tig/bank",  txtTopTigBankPort, address, check, checkport);
		}
		if (txtTopBillBankPort.isEnabled()) {
			buildPort( "SUB_TOPBILL_BANK_PORT", "top bill/bank",  txtTopBillBankPort, address, check, checkport);
		}
		if (txtTopLogHomePort.isEnabled()) {
			buildPort( "SUB_TOPLOG_HOME_PORT", "top log/home" ,txtTopLogHomePort, address, check, checkport);
		}
		if (txtTopTigHomePort.isEnabled()) {
			buildPort( "SUB_TOPTIG_HOME_PORT", "top tig/home", txtTopTigHomePort, address, check, checkport);
		}
		if (txtTopBillHomePort.isEnabled()) {
			buildPort( "SUB_TOPBILL_HOME_PORT", "top bill/home", txtTopBillHomePort, address, check, checkport);
		}
		
		address.append("\n");
		check.append("\n");

		// BANK集群
		if (txtBankIP.isEnabled()) {
			buildIP(address, "HUB_BANK_IP", txtBankIP);
		}
		if (txtBankPort.isEnabled()) {
			buildPort( "HUB_BANK_PORT", "bank", txtBankPort, address, check, checkport);
		}
		if (txtBank1IP.isEnabled()) {
			buildIP(address, "HUB_BANK1_IP", txtBank1IP);
		}
		if (txtBank1Port.isEnabled()) {
			buildPort( "HUB_BANK1_PORT",  "bank1", txtBank1Port, address, check, checkport);
		}
		if (txtBank2IP.isEnabled()) {
			buildIP(address, "HUB_BANK2_IP", txtBank2IP);
		}
		if (txtBank2Port.isEnabled()) {
			buildPort( "HUB_BANK2_PORT",  "bank2", txtBank2Port, address, check, checkport);
		}
		if (txtBankLogPort.isEnabled()) {
			buildPort( "SUB_BANKLOG_PORT", "bank log", txtBankLogPort, address, check, checkport);
		}
		if (txtBankLogAccountPort.isEnabled()) {
			buildPort("SUB_BANKLOG_ACCOUNT_PORT", "bank log/account", txtBankLogAccountPort, address, check, checkport);
		}
		if (txtBankTigAccountPort.isEnabled()) {
			buildPort("SUB_BANKTIG_ACCOUNT_PORT", "bank tig/account", txtBankTigAccountPort, address, check, checkport);
		}
		if (txtBankBillAccountPort.isEnabled()) {
			buildPort("SUB_BANKBILL_ACCOUNT_PORT", "bank bill/account", txtBankBillAccountPort, address, check, checkport);
		}
		if (txtBankLogHashPort.isEnabled()) {
			buildPort("SUB_BANKLOG_HASH_PORT", "bank log/hash", txtBankLogHashPort, address, check, checkport);
		}
		if (txtBankTigHashPort.isEnabled()) {
			buildPort("SUB_BANKTIG_HASH_PORT", "bank tig/hash", txtBankTigHashPort, address, check, checkport);
		}
		if (txtBankBillHashPort.isEnabled()) {
			buildPort("SUB_BANKBILL_HASH_PORT", "bank bill/hash", txtBankBillHashPort, address, check, checkport);
		}
		if (txtBankLogGatePort.isEnabled()) {
			buildPort("SUB_BANKLOG_GATE_PORT", "bank log/gate", txtBankLogGatePort, address, check, checkport);
		}
		if (txtBankTigGatePort.isEnabled()) {
			buildPort("SUB_BANKTIG_GATE_PORT", "bank tig/gate", txtBankTigGatePort, address, check, checkport);
		}
		if (txtBankBillGatePort.isEnabled()) {
			buildPort("SUB_BANKBILL_GATE_PORT", "bank bill/gate", txtBankBillGatePort, address, check, checkport);
		}
		if (txtBankLogEntrancePort.isEnabled()) {
			buildPort("SUB_BANKLOG_ENTRANCE_PORT", "bank log/entrance", txtBankLogEntrancePort, address, check, checkport);
		}
		if (txtBankTigEntrancePort.isEnabled()) {
			buildPort("SUB_BANKTIG_ENTRANCE_PORT", "bank tig/entrance", txtBankTigEntrancePort, address, check, checkport);
		}
		if (txtBankBillEntrancePort.isEnabled()) {
			buildPort("SUB_BANKBILL_ENTRANCE_PORT", "bank bill/entrance", txtBankBillEntrancePort, address, check, checkport);
		}
		
		if (txtAccountPort.isEnabled()) {
			buildPort("SUB_ACCOUNT_PORT", "account", txtAccountPort, address, check, checkport);
		}
		if (txtHashPort.isEnabled()) {
			buildPort("SUB_HASH_PORT", "hash", txtHashPort, address, check, checkport);
		}
		if (txtGatePort.isEnabled()) {
			buildPort("SUB_GATE_PORT", "gate", txtGatePort, address, check, checkport);
		}
		if (txtGateSuckerPort.isEnabled()) {
			buildPort("SUB_GATE_SUCKER_PORT", "gate mi receiver", txtGateSuckerPort, address, check, checkport);
		}
		if (txtGateDispatcherPort.isEnabled()) {
			buildPort("SUB_GATE_DISPATCHER_PORT", "gate mo sender", txtGateDispatcherPort, address, check, checkport);
		}
		if (txtEntrancePort.isEnabled()) {
			buildPort("SUB_ENTRANCE_PORT", "entrance", txtEntrancePort, address, check, checkport);
		}
		if (txtEntranceSuckerPort.isEnabled()) {
			buildPort("SUB_ENTRANCE_SUCKER_PORT", "entrance mi receiver", txtEntranceSuckerPort, address, check, checkport);
		}
		if (txtEntranceDispatcherPort.isEnabled()) {
			buildPort("SUB_ENTRANCE_DISPATCHER_PORT", "entrance mo sender", txtEntranceDispatcherPort, address, check, checkport);
		}
		
		address.append("\n");
		check.append("\n");

		// HOME集群
		if (txtHomeIP.isEnabled()) {
			buildIP(address, "HUB_HOME_IP", txtHomeIP);
		}
		if (txtHomePort.isEnabled()) {
			buildPort("HUB_HOME_PORT", "home", txtHomePort, address, check, checkport);
		}
		if (txtHome1IP.isEnabled()) {
			buildIP(address, "HUB_HOME1_IP", txtHome1IP);
		}
		if (txtHome1Port.isEnabled()) {
			buildPort("HUB_HOME1_PORT", "home1", txtHome1Port, address, check, checkport);
		}
		if (txtHome2IP.isEnabled()) {
			buildIP(address, "HUB_HOME2_IP", txtHome2IP);
		}
		if (txtHome2Port.isEnabled()) {
			buildPort("HUB_HOME2_PORT", "home2", txtHome2Port, address, check, checkport);
		}
		if (txtHomeLogPort.isEnabled()) {
			buildPort("SUB_HOMELOG_PORT", "home log", txtHomeLogPort, address, check, checkport);
		}
		if (txtHomeLogDataPort.isEnabled()) {
			buildPort("SUB_HOMELOG_DATA_PORT", "home log/data", txtHomeLogDataPort, address, check, checkport);
		}
		if (txtHomeTigDataPort.isEnabled()) {
			buildPort("SUB_HOMETIG_DATA_PORT", "home tig/data", txtHomeTigDataPort, address, check, checkport);
		}
		if (txtHomeBillDataPort.isEnabled()) {
			buildPort("SUB_HOMEBILL_DATA_PORT", "home bill/data", txtHomeBillDataPort, address, check, checkport);
		}
		if (txtHomeLogBuildPort.isEnabled()) {
			buildPort("SUB_HOMELOG_BUILD_PORT", "home log/build", txtHomeLogBuildPort, address, check, checkport);
		}
		if (txtHomeTigBuildPort.isEnabled()) {
			buildPort("SUB_HOMETIG_BUILD_PORT", "home tig/build", txtHomeTigBuildPort, address, check, checkport);
		}
		if (txtHomeBillBuildPort.isEnabled()) {
			buildPort("SUB_HOMEBILL_BUILD_PORT", "home bill/build", txtHomeBillBuildPort, address, check, checkport);
		}
		if (txtHomeLogWorkPort.isEnabled()) {
			buildPort("SUB_HOMELOG_WORK_PORT", "home log/work", txtHomeLogWorkPort, address, check, checkport);
		}
		if (txtHomeTigWorkPort.isEnabled()) {
			buildPort("SUB_HOMETIG_WORK_PORT", "home tig/work", txtHomeTigWorkPort, address, check, checkport);
		}
		if (txtHomeBillWorkPort.isEnabled()) {
			buildPort("SUB_HOMEBILL_WORK_PORT", "home bill/work", txtHomeBillWorkPort, address, check, checkport);
		}
		if (txtHomeLogCallPort.isEnabled()) {
			buildPort("SUB_HOMELOG_CALL_PORT", "home log/call", txtHomeLogCallPort, address, check, checkport);
		}
		if (txtHomeTigCallPort.isEnabled()) {
			buildPort("SUB_HOMETIG_CALL_PORT", "home tig/call", txtHomeTigCallPort, address, check, checkport);
		}
		if (txtHomeBillCallPort.isEnabled()) {
			buildPort("SUB_HOMEBILL_CALL_PORT", "home bill/call", txtHomeBillCallPort, address, check, checkport);
		}
		
		if (txtDataMasterPort.isEnabled()) {
			buildPort("SUB_DATA_PORT", "data mster", txtDataMasterPort, address, check, checkport);
		}
		if (txtDataSlavePort.isEnabled()) {
			buildPort("SUB_SLAVE_PORT", "data slave", txtDataSlavePort, address, check, checkport);
		}
		if (txtBuildPort.isEnabled()) {
			buildPort("SUB_BUILD_PORT", "build", txtBuildPort, address, check, checkport);
		}
		if (txtWorkPort.isEnabled()) {
			buildPort("SUB_WORK_PORT", "work", txtWorkPort, address, check, checkport);
		}
		if (txtCallPort.isEnabled()) {
			buildPort("SUB_CALL_PORT", "call", txtCallPort, address, check, checkport);
		}
		if (txtCallSuckerPort.isEnabled()) {
			buildPort("SUB_CALL_SUCKER_PORT", "call mi receiver", txtCallSuckerPort, address, check, checkport);
		}
		if (txtCallDispatcherPort.isEnabled()) {
			buildPort("SUB_CALL_DISPATCHER_PORT", "call mo sender", txtCallDispatcherPort, address, check, checkport);
		}
		
		// 生成两个脚本文件
		String addressScript = buildAddressScript(address.toString());		
		String checkScript = buildCheckScript(check.toString());
		String checkPortScript = buildCheckScript(checkport.toString());

		// 写入磁盘
		File file = createAddressFile();
		boolean b1 = write(file, addressScript);
		
		file = createCheckFile();
		boolean b2 = write(file, checkScript);
		
		file = createCheckPortFile();
		boolean b3 = write(file, checkPortScript);
		
		return b1 && b2 && b3;
	}

//	/**
//	 * 保存根目录
//	 */
//	private void save() {
//		if (exportRoot != null) {
//			UITools.putProperity(EXPORT_PATH, exportRoot.toString());
//		}
//		if (importRoot != null) {
//			UITools.putProperity(IMPORT_PATH, importRoot.toString());
//		}
//
//		Rectangle rect = getBounds();
//		Rectangle sub = new Rectangle(rect);
//		UITools.putProperity(KEY_RECT, sub);
//	}
	
	
	
//	private void loadConfig(File file) throws IOException {
//		FileReader in = new FileReader(file);
//		BufferedReader buff = new BufferedReader(in);
//
//		// 解析参数
//		Pattern pattern = Pattern.compile(EXPORT_REGEX);
//		do {
//			String line = buff.readLine();
//			if (line == null) {
//				break;
//			}
//			// 判断语法
//			Matcher matcher = pattern.matcher(line);
//			if (!matcher.matches()) {
//				System.out.printf("ERROR [%s]\n", line);
//				continue;
//			}
//			String key = matcher.group(1);
//			String value = matcher.group(2);
//			importText(key, value);
//		} while (true);
//
//		buff.close();
//		in.close();
//	}
	
	private void loadConfig(File file) throws IOException {
		// 读出字节数组，转成UTF8编码
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		String str = new UTF8().decode(b);

		// 读取数据
		BufferedReader buff = new BufferedReader(new StringReader(str));
		// 解析参数
		Pattern pattern = Pattern.compile(EXPORT_REGEX);
		do {
			String line = buff.readLine();
			if (line == null) {
				break;
			}
			// 判断语法
			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				continue;
			}
			String key = matcher.group(1);
			String value = matcher.group(2);
			importText(key, value);
		} while (true);

		buff.close();
		in.close();
	}
	
	
	
//	/**
//	 * 从指定文件导入磁盘文件
//	 */
//	private void importFrom() {
//		String title = UIManager.getString("AddressScriptDialog.ChoiceImportTitle");
//		String buttonText = UIManager.getString("AddressScriptDialog.ChoiceImportButtonText");
//		
//		String description = UIManager.getString("AddressScriptDialog.ChoiceImportDescriptionText");
//		String extensions = UIManager.getString("AddressScriptDialog.ChoiceImportExtensionText");
//		
//		FileNameExtensionFilter filter = new FileNameExtensionFilter(
//				description, extensions);
//
//		JFileChooser chooser = new JFileChooser();
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setFileFilter(filter);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle(title); 
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//		chooser.setApproveButtonText(buttonText); 
//		
//		// 目录
//		if (importRoot != null) {
//			chooser.setCurrentDirectory(importRoot);
//		}
//		// 没有定义，从系统中取
//		else {
//			Object memory = UITools.getProperity(IMPORT_PATH);
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
//		// 选中文件
//		File file = chooser.getSelectedFile();
//		boolean success = (file.exists() && file.isFile());
//		if (!success) {
//			return;
//		}
//
//		// 保存目录
//		importRoot = file.getParentFile();
//		// 保存到集合
//		FontKit.setLabelText(lblImport, file.toString());
//
//		// 不清除路径标签
//		reset(false);
//
//		// 导入记录
//		try {
//			loadConfig(file);
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//	}

	/**
	 * 保存范围
	 */
	private void writeImportRoot(File f) {
		RTKit.writeFile(RTEnvironment.ENVIRONMENT_SYSTEM, "AddressScriptDialog/ImportRoot", f);
	}

	/**
	 * 读范围
	 * @return
	 */
	private File readImportRoot() {
		return RTKit.readFile(RTEnvironment.ENVIRONMENT_SYSTEM,"AddressScriptDialog/ImportRoot");
	}
	
	/**
	 * 从指定文件导入磁盘文件
	 */
	private void importFrom() {
		String title = UIManager.getString("AddressScriptDialog.ChoiceImportTitle");
		String buttonText = UIManager.getString("AddressScriptDialog.ChoiceImportButtonText");
		String description = UIManager.getString("AddressScriptDialog.ChoiceImportDescriptionText");
		String extensions = UIManager.getString("AddressScriptDialog.ChoiceImportExtensionText");
		
		DiskFileMatcher f1 = new DiskFileMatcher(description, extensions);
		
		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setAcceptAllFileFilterUsed(true);
		dialog.setShowCharsetEncode(false);
		dialog.addFileMatcher(f1);
		dialog.setSelectFileMatcher(f1);
		dialog.setMultiSelectionEnabled(false); // 选择一个
		dialog.setDialogType(DialogOption.OPEN_DIALOG);
		dialog.setFileSelectionMode(DialogOption.FILES_ONLY);
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);
		
		File file = readImportRoot();
		if (file != null) {
			File parent = file.getParentFile();
			if (parent != null && parent.isDirectory()) {
				dialog.setCurrentDirectory(parent);
			}
		}
		
		File[] files = dialog.showDialog(this);
		boolean success = (files != null && files.length > 0);
		if (success) {
			file = files[0];
			success = (file.exists() && file.isFile());
			if (success) {
				writeImportRoot(file);
				// 保存到集合
				FontKit.setLabelText(lblImport, file.toString());
				// 不清除路径标签
				reset(false);
				// 导入记录
				try {
					loadConfig(file);
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
	}
	
	
//	/**
//	 * 打开窗口，选择磁盘目录
//	 */
//	private void exportTo() {
//		String title = UIManager.getString("AddressScriptDialog.ChoiceExportTitle");
//		String buttonText = UIManager.getString("AddressScriptDialog.ChoiceExportButtonText");
//
//		JFileChooser chooser = new JFileChooser();
//		chooser.setMultiSelectionEnabled(true);
//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // .FILES_ONLY);
//		chooser.setDialogTitle(title); 
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//		chooser.setApproveButtonText(buttonText);
//		if (exportRoot != null) {
//			chooser.setCurrentDirectory(exportRoot);
//		}
//		// 没有定义，从系统中取
//		else {
//			Object memory = UITools.getProperity(EXPORT_PATH);
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
//			exportRoot = dir;
//			// 保存到集合
//			FontKit.setLabelText(lblExport, dir.toString());
//		}
//	}

	/**
	 * 保存范围
	 */
	private void writeExportRoot(File f) {
		RTKit.writeFile(RTEnvironment.ENVIRONMENT_SYSTEM, "AddressScriptDialog/ExportRoot", f);
	}

	/**
	 * 读范围
	 * @return
	 */
	private File readExportRoot() {
		return RTKit.readFile(RTEnvironment.ENVIRONMENT_SYSTEM,"AddressScriptDialog/ExportRoot");
	}
	
	/**
	 * 打开窗口，选择磁盘目录
	 */
	private void exportTo() {
		String title = UIManager.getString("AddressScriptDialog.ChoiceExportTitle");
		String buttonText = UIManager.getString("AddressScriptDialog.ChoiceExportButtonText");

		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setShowCharsetEncode(false);
		dialog.setMultiSelectionEnabled(false);
		dialog.setDialogType(DialogOption.OPEN_DIALOG);
		dialog.setFileSelectionMode(DialogOption.DIRECTORIES_ONLY); 
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);
		
		File file = readExportRoot();
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
				// 写入
				writeExportRoot(dir);
				// 显示
				FontKit.setLabelText(lblExport, Laxkit.canonical(dir));
			}
		}
	}
	
//	/**
//	 * 退出运行
//	 */
//	private boolean exit() {
//		String title = getTitle();
//		String content = findCaption("Dialog/address/exit/message/title");
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
		String content = UIManager.getString("AddressScriptDialog.ExitText");
		return MessageBox.showYesNoDialog(this, title, content);
	}
	
//	/**
//	 * 参数警告！
//	 */
//	private void warning(){
//		// 提示错误
//		String title = findCaption("Dialog/address/warning/title");
//		String content = findContent("Dialog/address/warning");
//		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
//	}

	
	/**
	 * 参数警告！
	 */
	private void warning(){
		// 提示错误
		String title = UIManager.getString("AddressScriptDialog.WarningTitle");
		String content = UIManager.getString("AddressScriptDialog.WarningContent");
		MessageBox.showWarning(this, title, content);
	}
	
//	/**
//	 * 端口号重复警告
//	 */
//	private void repeatPort(int port){
//		// 提示错误
//		String title = findCaption("Dialog/address/repeat-port/title");
//		String content = findContent("Dialog/address/repeat-port");
//		// 格式化
//		content = String.format(content, port);
//		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
//	}
	
	/**
	 * 端口号重复警告
	 */
	private void repeatPort(int port){
		// 提示错误
		String title = UIManager.getString("AddressScriptDialog.RepeatPortTitle");
		String content = UIManager.getString("AddressScriptDialog.RepeatPortContent");
		// 格式化
		content = String.format(content, port);
		MessageBox.showWarning(this, title, content);
	}

	/**
	 * 生成启动文件
	 * @return
	 */
	private File createAddressFile() {
		String dir = lblExport.getText();
		return new File(dir, "address.sh");
	}

	/**
	 * 生成启动文件
	 * @return
	 */
	private File createCheckFile() {
		String dir = lblExport.getText();
		return new File(dir, "check.sh");
	}
	
	/**
	 * 生成启动文件
	 * @return
	 */
	private File createCheckPortFile() {
		String dir = lblExport.getText();
		return new File(dir, "checkport.sh");
	}
	
//	/**
//	 * 询问覆盖...
//	 * @param file 磁盘文件
//	 * @return 返回真或者假
//	 */
//	private boolean override(File file) {
//		// 提示错误
//		String title = findCaption("Dialog/address/override/title");
//		String content = findContent("Dialog/address/override");
//		String format = String.format(content, file.toString());
//
////		ResourceLoader loader = new ResourceLoader();
////		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
//
//		int who = MessageDialog.showMessageBox(this, title,
//				JOptionPane.QUESTION_MESSAGE, format,
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
		String title = UIManager.getString("AddressScriptDialog.OverrideTitle");
		String content = UIManager.getString("AddressScriptDialog.OverrideContent");
		content = String.format(content, Laxkit.canonical(file));
		return MessageBox.showYesNoDialog(this, title, content);
	}

//	/**
//	 * 确认生成脚本文件
//	 * @return 返回真或者假
//	 */
//	private boolean __confirm() {
//		// 提示错误
//		String title = findCaption("Dialog/address/confirm/title");
//		String content = findContent("Dialog/address/confirm");
//
////		ResourceLoader loader = new ResourceLoader();
////		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
//
//		int who = MessageDialog.showMessageBox(this, title,
//				JOptionPane.QUESTION_MESSAGE, null, content,
//				JOptionPane.YES_NO_OPTION);
//		return (who == JOptionPane.YES_OPTION) ;
//	}

	
	/**
	 * 确认生成脚本文件
	 * @return 返回真或者假
	 */
	private boolean __confirm() {
		// 提示错误
		String title = UIManager.getString("AddressScriptDialog.ConfirmTitle");
		String content = UIManager.getString("AddressScriptDialog.ConfirmContent");
		return MessageBox.showYesNoDialog(this, title, content);
	}
	
	/**
	 * 确认？
	 * @return
	 */
	private boolean confirm() {
		// 地址文件
		File file = createAddressFile();
		boolean exists = (file.exists() && file.isFile());
		if (exists) {
			// 如果不覆盖，返回假
			if (!override(file)) {
				return false;
			}
		}

		// 检测文件
		file = createCheckFile();
		exists = (file.exists() && file.isFile());
		if (exists) {
			// 如果不覆盖，返回假
			if (!override(file)) {
				return false;
			}
		}

		// 检测文件
		file = createCheckPortFile();
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
//		setButtonText(cmdImport, findCaption("Dialog/address/button/import/title"));
//		cmdImport.setMnemonic('I');
//		setButtonText(cmdExport, findCaption("Dialog/address/button/export/title"));
//		cmdExport.setMnemonic('S');
//		setButtonText(cmdCreate, findCaption("Dialog/address/button/build/title"));
//		cmdCreate.setMnemonic('B');
//		setButtonText(cmdReset, findCaption("Dialog/address/button/reset/title"));
//		cmdReset.setMnemonic('R');
//		setButtonText(cmdExit, findCaption("Dialog/address/button/exit/title"));
//		cmdExit.setMnemonic('X');
//		setButtonText(cmdAll, findCaption("Dialog/address/button/all/title"));
//		cmdAll.setMnemonic('A');
//
//		// TOP CLUSTER
//		setButtonText(chkTop, findCaption("Dialog/address/checkbox/top/title"));
//		setButtonText(chkTop1, findCaption("Dialog/address/checkbox/top1/title"));
//		setButtonText(chkTop2, findCaption("Dialog/address/checkbox/top2/title"));
//		setButtonText(chkTopLog, findCaption("Dialog/address/checkbox/top-log/title"));
//		setLabelText(lblTopLogBank, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/top-log-tig-bank/title"));
//		setLabelText(lblTopLogHome, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/top-log-tig-home/title"));
//
//		// BANK CLUSTER
//		setButtonText(chkBank, findCaption("Dialog/address/checkbox/bank/title"));
//		setButtonText(chkBank1, findCaption("Dialog/address/checkbox/bank1/title"));
//		setButtonText(chkBank2, findCaption("Dialog/address/checkbox/bank2/title"));
//		setButtonText(chkBankLog, findCaption("Dialog/address/checkbox/bank-log/title"));
//		setButtonText(chkAccount, findCaption("Dialog/address/checkbox/account/title"));
//		setButtonText(chkHash, findCaption("Dialog/address/checkbox/hash/title"));
//		setButtonText(chkGate, findCaption("Dialog/address/checkbox/gate/title"));
//		setButtonText(chkEntrance, findCaption("Dialog/address/checkbox/entrance/title"));
//		setLabelText(lblBankLogAccount, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/bank-log-tig-account/title"));
//		setLabelText(lblBankLogHash, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/bank-log-tig-hash/title"));
//		setLabelText(lblBankLogGate, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/bank-log-tig-gate/title"));
//		setLabelText(lblBankLogEntrance, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/bank-log-tig-entrance/title"));
//
//		// HOME CLUSTER
//		setButtonText(chkHome, findCaption("Dialog/address/checkbox/home/title"));
//		setButtonText(chkHome1, findCaption("Dialog/address/checkbox/home1/title"));
//		setButtonText(chkHome2, findCaption("Dialog/address/checkbox/home2/title"));
//		setButtonText(chkHomeLog, findCaption("Dialog/address/checkbox/home-log/title"));
//		setButtonText(chkDataMaster, findCaption("Dialog/address/checkbox/data-master/title"));
//		setButtonText(chkDataSlave, findCaption("Dialog/address/checkbox/data-slave/title"));
//		setButtonText(chkBuild, findCaption("Dialog/address/checkbox/build/title"));
//		setButtonText(chkWork, findCaption("Dialog/address/checkbox/work/title"));
//		setButtonText(chkCall, findCaption("Dialog/address/checkbox/call/title"));
//		setLabelText(lblHomeLogData, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/home-log-tig-data/title"));
//		setLabelText(lblHomeLogBuild, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/home-log-tig-build/title"));
//		setLabelText(lblHomeLogWork, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/home-log-tig-work/title"));
//		setLabelText(lblHomeLogCall, SwingConstants.LEFT, findCaption("Dialog/address/checkbox/home-log-tig-call/title"));
//
//
////		AddressScriptDialog.IPText  IPv4或者IPv6地址格式
////		AddressScriptDialog.PortText  TCP和UDP端口号
//		
//		// 设置端口，只允许输入数字
//		String title = UIManager.getString("AddressScriptDialog.PortText");
//		FlatTextField[] ports = new FlatTextField[] {
//				// top cluster
//				txtTopPort, txtTop1Port, txtTop2Port, txtTopLogPort, 
//				txtTopLogBankPort, txtTopTigBankPort, txtTopLogHomePort, txtTopTigHomePort,
//				// bank cluster
//				txtBankPort, txtBank1Port, txtBank2Port, txtBankLogPort, 
//				txtBankLogAccountPort, txtBankTigAccountPort, txtBankLogHashPort, txtBankTigHashPort,
//				txtBankLogGatePort, txtBankTigGatePort, txtBankLogEntrancePort, txtBankTigEntrancePort,
//				txtAccountPort, txtHashPort, txtGatePort, txtEntrancePort, 
//				txtGateSuckerPort, txtGateDispatcherPort,
//				txtEntranceSuckerPort, txtEntranceDispatcherPort,
//				// home cluster
//				txtHomePort, txtHome1Port, txtHome2Port, txtHomeLogPort, 
//				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeLogBuildPort, txtHomeTigBuildPort,
//				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeLogCallPort, txtHomeTigCallPort,
//				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort, txtCallPort,
//				txtCallSuckerPort, txtCallDispatcherPort };
//		for (int i = 0; i < ports.length; i++) {
//			ports[i].setDocument(new DigitDocument(ports[i], 5));
//			ports[i].setColumns(8);
//			setToolTipText(ports[i], title);
//		}
//
//		// IP 地址
//		title = UIManager.getString("AddressScriptDialog.IPText");
//		FlatTextField[] addresses = new FlatTextField[] { txtPrivateIP, txtGatewayIP,
//				txtTopIP, txtTop1IP, txtTop2IP, txtBankIP, txtBank1IP,
//				txtBank2IP, txtHomeIP, txtHome1IP, txtHome2IP };
//		for (int i = 0; i < addresses.length; i++) {
//			addresses[i].setDocument(new GraphDocument(addresses[i], 56));
//			addresses[i].setColumns(10);
//			setToolTipText(addresses[i], title);
//		}
//	}
	
	/**
	 * 初始化组件
	 */
	private void initControls() {
		setButtonText(cmdImport, UIManager.getString("AddressScriptDialog.ButtonImportText"));
		cmdImport.setMnemonic('I');
		setButtonText(cmdExport, UIManager.getString("AddressScriptDialog.ButtonExportText"));
		cmdExport.setMnemonic('S');
		setButtonText(cmdCreate, UIManager.getString("AddressScriptDialog.ButtonBuildText"));
		cmdCreate.setMnemonic('B');
		setButtonText(cmdReset, UIManager.getString("AddressScriptDialog.ButtonResetText"));
		cmdReset.setMnemonic('R');
		setButtonText(cmdExit, UIManager.getString("AddressScriptDialog.ButtonExitText"));
		cmdExit.setMnemonic('X');
		setButtonText(cmdAll, UIManager.getString("AddressScriptDialog.ButtonAllText"));
		cmdAll.setMnemonic('A');

		// TOP CLUSTER
		setButtonText(chkTop, UIManager.getString("AddressScriptDialog.CheckboxTopText"));
		setButtonText(chkTop1, UIManager.getString("AddressScriptDialog.CheckboxTop1Text"));
		setButtonText(chkTop2, UIManager.getString("AddressScriptDialog.CheckboxTop2Text"));
		setButtonText(chkTopLog, UIManager.getString("AddressScriptDialog.CheckboxTopLogText"));
		setLabelText(lblTopLogBank, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxTopLogTigBankText"));
		setLabelText(lblTopLogHome, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxTopLogTigHomeText"));

		
		// BANK CLUSTER
		setButtonText(chkBank, UIManager.getString("AddressScriptDialog.CheckboxBankText"));
		setButtonText(chkBank1, UIManager.getString("AddressScriptDialog.CheckboxBank1Text"));
		setButtonText(chkBank2, UIManager.getString("AddressScriptDialog.CheckboxBank2Text"));
		setButtonText(chkBankLog, UIManager.getString("AddressScriptDialog.CheckboxBankLogText"));
		setButtonText(chkAccount, UIManager.getString("AddressScriptDialog.CheckboxAccountText"));
		setButtonText(chkHash, UIManager.getString("AddressScriptDialog.CheckboxHashText"));
		setButtonText(chkGate, UIManager.getString("AddressScriptDialog.CheckboxGateText"));
		setButtonText(chkEntrance, UIManager.getString("AddressScriptDialog.CheckboxEntranceText"));
		setLabelText(lblBankLogAccount, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxBankLogTigAccountText"));
		setLabelText(lblBankLogHash, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxBankLogTigHashText"));
		setLabelText(lblBankLogGate, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxBankLogTigGateText"));
		setLabelText(lblBankLogEntrance, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxBankLogTigEntranceText"));

		
		// HOME CLUSTER
		setButtonText(chkHome, UIManager.getString("AddressScriptDialog.CheckboxHomeText"));
		setButtonText(chkHome1, UIManager.getString("AddressScriptDialog.CheckboxHome1Text"));
		setButtonText(chkHome2, UIManager.getString("AddressScriptDialog.CheckboxHome2Text"));
		
		setButtonText(chkHomeLog, UIManager.getString("AddressScriptDialog.CheckboxHomeLogText"));
		setButtonText(chkDataMaster, UIManager.getString("AddressScriptDialog.CheckboxDataMasterText"));
		setButtonText(chkDataSlave, UIManager.getString("AddressScriptDialog.CheckboxDataSlaveText"));
		setButtonText(chkBuild, UIManager.getString("AddressScriptDialog.CheckboxBuildText"));
		setButtonText(chkWork, UIManager.getString("AddressScriptDialog.CheckboxWorkText"));
		setButtonText(chkCall, UIManager.getString("AddressScriptDialog.CheckboxCallText"));
		setLabelText(lblHomeLogData, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxHomeLogTigDataText"));
		setLabelText(lblHomeLogBuild, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxHomeLogTigBuildText"));
		setLabelText(lblHomeLogWork, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxHomeLogTigWorkText"));
		setLabelText(lblHomeLogCall, SwingConstants.LEFT, UIManager.getString("AddressScriptDialog.CheckboxHomeLogTigCallText"));


//		AddressScriptDialog.IPText  IPv4或者IPv6地址格式
//		AddressScriptDialog.PortText  TCP和UDP端口号
		
		// 设置端口，只允许输入数字
		String title = UIManager.getString("AddressScriptDialog.PortText");
		FlatTextField[] ports = new FlatTextField[] {
				// top cluster
				txtTopPort, txtTop1Port, txtTop2Port, txtTopLogPort, 
				txtTopLogBankPort, txtTopTigBankPort, txtTopBillBankPort, 
				txtTopLogHomePort, txtTopTigHomePort, txtTopBillHomePort,
				// bank cluster
				txtBankPort, txtBank1Port, txtBank2Port, txtBankLogPort, 
				txtBankLogAccountPort, txtBankTigAccountPort, txtBankBillAccountPort,
				txtBankLogHashPort, txtBankTigHashPort, txtBankBillHashPort,
				txtBankLogGatePort, txtBankTigGatePort, txtBankBillGatePort, 
				txtBankLogEntrancePort, txtBankTigEntrancePort, txtBankBillEntrancePort,
				txtAccountPort, txtHashPort, txtGatePort, txtEntrancePort, 
				txtGateSuckerPort, txtGateDispatcherPort,
				txtEntranceSuckerPort, txtEntranceDispatcherPort,
				// home cluster
				txtHomePort, txtHome1Port, txtHome2Port, txtHomeLogPort, 
				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeBillDataPort, 
				txtHomeLogBuildPort, txtHomeTigBuildPort, txtHomeBillBuildPort,
				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeBillWorkPort,
				txtHomeLogCallPort, txtHomeTigCallPort, txtHomeBillCallPort,
				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort, txtCallPort,
				txtCallSuckerPort, txtCallDispatcherPort };
		for (int i = 0; i < ports.length; i++) {
			ports[i].setDocument(new DigitDocument(ports[i], 5));
			ports[i].setColumns(8);
			setToolTipText(ports[i], title);
		}

		Dimension d = new Dimension(96, 28);
		// IP 地址
		title = UIManager.getString("AddressScriptDialog.IPText");
		FlatTextField[] addresses = new FlatTextField[] { txtPrivateIP, txtGatewayIP,
				txtTopIP, txtTop1IP, txtTop2IP, txtBankIP, txtBank1IP,
				txtBank2IP, txtHomeIP, txtHome1IP, txtHome2IP };
		for (int i = 0; i < addresses.length; i++) {
			addresses[i].setDocument(new GraphDocument(addresses[i], 56));
			addresses[i].setColumns(10);
			setToolTipText(addresses[i], title);
			// 最优尺寸
			addresses[i].setPreferredSize(d);
		}
	}
	
	/**
	 * 建立事件监听
	 */
	private void initListeners() {
		// 给按纽设置事件
		FlatButton[] buttons = new FlatButton[] { cmdImport, cmdExport, cmdCreate, cmdReset,
				cmdExit };
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].addActionListener(this);
		}

		// 给选择按纽设置事件
		JCheckBox[] boxes = new JCheckBox[] { cmdAll, 
				// top cluster
				chkTop, chkTop1, chkTop2, chkTopLog, 
				// bank cluster
				chkBank, chkBank1, chkBank2, chkBankLog, 
				chkAccount, chkHash, chkGate, chkEntrance, 
				// home cluster
				chkHome, chkHome1, chkHome2, chkHomeLog, 
				chkDataMaster, chkDataSlave, chkBuild, chkWork, chkCall };
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].addActionListener(this);
			boxes[i].setIconTextGap(2);
		}
	}

	/**
	 * 生成一个保存文件的面板
	 * @return JPanel实例
	 */
	private JPanel createSaveToPanel() {
		JPanel left = new JPanel();
		left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		left.setLayout(new GridLayout(2, 1, 0, 4));
		left.add(cmdImport);
		left.add(cmdExport);

		JPanel rightBottom = new JPanel();
		rightBottom.setLayout(new BorderLayout(5, 0));
		rightBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		rightBottom.add(lblExport, BorderLayout.CENTER);
		rightBottom.add(cmdAll, BorderLayout.EAST);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1));
		rightBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		right.add(lblImport);
		right.add(rightBottom);

		// 分左右两端位置
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 生成网段面板：内网IP, 网关IP
	 * @return
	 */
	private JPanel createAreaPanel() {
		JLabel inner = new JLabel(UIManager.getString("AddressScriptDialog.PrivateIPText")); // findCaption("Dialog/address/label/private/title"));
		JLabel gateway = new JLabel(UIManager.getString("AddressScriptDialog.GatewayIPText")); // findCaption("Dialog/address/label/gateway/title"));

		JPanel p1 = new JPanel ();
		p1.setLayout(new BorderLayout(5, 1));
		p1.add(inner, BorderLayout.WEST);
		p1.add(txtPrivateIP, BorderLayout.CENTER);
		p1.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));

		JPanel p2 = new JPanel ();
		p2.setLayout(new BorderLayout(5, 1));
		p2.add(gateway, BorderLayout.WEST);
		p2.add(txtGatewayIP, BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		String title = UIManager.getString("AddressScriptDialog.NetworkText"); // findCaption("Dialog/address/panel/network/title");
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 10, 1));
		panel.add(p1);
		panel.add(p2);
		panel.setBorder(UITools.createTitledBorder(title, 4));

		return panel;
	}

	/**
	 * 生成管理节点域面板
	 * @param ip
	 * @param port
	 * @return
	 */
	private JPanel createManagerFieldPanel(FlatTextField ip, FlatTextField port) {
		JLabel sperator = new JLabel(":", SwingConstants.CENTER);
		Font font = sperator.getFont();
		Font subFont = new Font(font.getName(), Font.BOLD, font.getSize());
		sperator.setFont(subFont);

		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(5, 0));
		p1.add(ip, BorderLayout.CENTER);
		p1.add(sperator, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(p1, BorderLayout.CENTER);
		panel.add(port, BorderLayout.EAST);

		return panel;
	}

	private JPanel createTextFieldPanel(FlatTextField[] fields) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new GridLayout(1, fields.length, 10, 0));
		for (int i = 0; i < fields.length; i++) {
			panel.add(fields[i]);
		}
		return panel;
	}

	/**
	 * 生成TOP集群面板
	 * @return
	 */
	private JPanel createTopClusterPanel() {
		// 运行节点/备份节点
		JPanel master = createManagerFieldPanel(txtTopIP, txtTopPort);
		JPanel slave1 = createManagerFieldPanel(txtTop1IP, txtTop1Port);
		JPanel slave2 = createManagerFieldPanel(txtTop2IP, txtTop2Port);

		// 左侧标题
		JPanel left = createLeftTitlePanel(new JComponent[] { chkTop, chkTop1,
				chkTop2, chkTopLog, lblTopLogBank, lblTopLogHome });

		JPanel bankLogPanel = createTextFieldPanel(new FlatTextField[] {
				txtTopLogBankPort, txtTopTigBankPort, txtTopBillBankPort });
		JPanel homeLogPanel = createTextFieldPanel(new FlatTextField[] {
				txtTopLogHomePort, txtTopTigHomePort, txtTopBillHomePort });
		// 右侧文本框
		JPanel right = createRigthContentPanel(new JPanel[] { master, slave1,
				slave2 }, new JComponent[] { txtTopLogPort, bankLogPanel, homeLogPanel });

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		String title = UIManager.getString("AddressScriptDialog.TopClusterText");
		panel.setBorder(UITools.createTitledBorder(title, 4));

		return panel;
	}

	/**
	 * 生成BANK集群面板
	 * @return
	 */
	private JPanel createBankClusterPanel() {
		// 运行节点/备份节点
		JPanel master = createManagerFieldPanel(txtBankIP, txtBankPort);
		JPanel slave1 = createManagerFieldPanel(txtBank1IP, txtBank1Port);
		JPanel slave2 = createManagerFieldPanel(txtBank2IP, txtBank2Port);

		// 左侧标题
		JPanel left = createLeftTitlePanel(new JComponent[] { chkBank,
				chkBank1, chkBank2 , chkBankLog, 
				lblBankLogAccount, lblBankLogHash, lblBankLogGate, lblBankLogEntrance,
				chkAccount, chkHash, chkGate, chkEntrance });

		// 日志面板
		JPanel account = createTextFieldPanel(new FlatTextField[] {
				txtBankLogAccountPort, txtBankTigAccountPort, txtBankBillAccountPort });
		JPanel gate = createTextFieldPanel(new FlatTextField[] {
				txtBankLogGatePort, txtBankTigGatePort, txtBankBillGatePort });
		JPanel hash = createTextFieldPanel(new FlatTextField[] {
				txtBankLogHashPort, txtBankTigHashPort, txtBankBillHashPort });
		JPanel entrance = createTextFieldPanel(new FlatTextField[] {
				txtBankLogEntrancePort, txtBankTigEntrancePort, txtBankBillEntrancePort });
		
		// 生成GATE节点异步通信端口面板
		String[] xmlPaths = new String[] { "AddressScriptDialog.GateSuckerPortText", "AddressScriptDialog.GateDispatcherPortText" };		
		JPanel gatePortPanel = createReplyPanel(txtGatePort, xmlPaths, new FlatTextField[] { txtGateSuckerPort, txtGateDispatcherPort });

		// 生成ENTRANCE节点异步通信端口面板
		xmlPaths = new String[] {"AddressScriptDialog.EntranceSuckerPortText", "AddressScriptDialog.EntranceDispatcherPortText" };
		JPanel entrancePortPanel = createReplyPanel(txtEntrancePort, xmlPaths, new FlatTextField[] { txtEntranceSuckerPort, txtEntranceDispatcherPort });

		// 右侧文本框
		JPanel right = createRigthContentPanel(new JPanel[] { master, slave1,
				slave2 }, new JComponent[] { txtBankLogPort, account, hash,
				gate, entrance, txtAccountPort, txtHashPort, gatePortPanel,
				entrancePortPanel });

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		String title = UIManager.getString("AddressScriptDialog.BankClusterText");
		panel.setBorder(UITools.createTitledBorder(title, 4));

		return panel;
	}

	private JPanel createSubPortPanel(JComponent field) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(field, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * @param subs
	 * @return
	 */
	private JPanel createLeftTitlePanel(JComponent[] subs) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(subs.length, 1, 0, 4));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		for (int i = 0; i < subs.length; i++) {
			panel.add(subs[i]);
		}
		return panel;
	}

	private JPanel createRigthContentPanel(JPanel[] masters, JComponent[] subs) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(masters.length + subs.length, 1, 0, 4));
		panel.setBorder(BorderFactory.createEmptyBorder());
		for (int i = 0; i < masters.length; i++) {
			panel.add(masters[i]);
		}
		for (int i = 0; i < subs.length; i++) {
			panel.add(createSubPortPanel(subs[i]));
		}

		return panel;
	}

	/**
	 * 生成异步通信端口面板 
	 * @param left
	 * @param xmlPaths
	 * @param fields
	 * @return
	 */
	private JPanel createReplyPanel(FlatTextField left, String[] xmlPaths, FlatTextField[] fields) {		
		// 判断一致
		if (xmlPaths.length != fields.length) {
			throw new IllegalValueException("%d != %d", xmlPaths.length, fields.length);
		}

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, fields.length, 10, 0));
		right.setBorder(BorderFactory.createEmptyBorder());
		for (int i = 0; i < fields.length; i++) {
			// 生成子面板
			JLabel label = new JLabel(UIManager.getString(xmlPaths[i]));
			JPanel sub = new JPanel();
			sub.setBorder(BorderFactory.createEmptyBorder());
			sub.setLayout(new BorderLayout(5, 0));
			sub.add(label, BorderLayout.WEST);
			sub.add(fields[i], BorderLayout.CENTER);

			right.add(sub);
		}

		JPanel sub = new JPanel();
		sub.setBorder(BorderFactory.createEmptyBorder());
		sub.setLayout(new BorderLayout(10, 0));
		sub.add(left, BorderLayout.WEST);
		sub.add(right, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(sub, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 生成HOME集群面板
	 * @return
	 */
	private JPanel createHomeClusterPanel() {
		// 运行节点/备份节点
		JPanel master = createManagerFieldPanel(txtHomeIP, txtHomePort);
		JPanel slave1 = createManagerFieldPanel(txtHome1IP, txtHome1Port);
		JPanel slave2 = createManagerFieldPanel(txtHome2IP, txtHome2Port);

		// 左侧标题
		JPanel left = createLeftTitlePanel(new JComponent[] { chkHome,
				chkHome1, chkHome2 , chkHomeLog,
				lblHomeLogData, lblHomeLogBuild, lblHomeLogWork, lblHomeLogCall,
				chkDataMaster, chkDataSlave, chkBuild, chkWork, chkCall });

		// 日志
		JPanel data = createTextFieldPanel(new FlatTextField[] {
				txtHomeLogDataPort, txtHomeTigDataPort, txtHomeBillDataPort });
		JPanel work = createTextFieldPanel(new FlatTextField[] {
				txtHomeLogWorkPort, txtHomeTigWorkPort, txtHomeBillWorkPort });
		JPanel build = createTextFieldPanel(new FlatTextField[] {
				txtHomeLogBuildPort, txtHomeTigBuildPort, txtHomeBillBuildPort });
		JPanel call = createTextFieldPanel(new FlatTextField[] {
				txtHomeLogCallPort, txtHomeTigCallPort, txtHomeBillCallPort });

		// 生成异步通信端口面板		
		String[] xmlPaths = new String[] { "AddressScriptDialog.CallSuckerPortText", "AddressScriptDialog.CallDispatcherPortText" };
		JPanel callPortPanel = createReplyPanel(txtCallPort, xmlPaths,
				new FlatTextField[] { txtCallSuckerPort, txtCallDispatcherPort });

		// 右侧文本框
		JPanel right = createRigthContentPanel(new JPanel[] { master,
				slave1, slave2 }, new JComponent[] { txtHomeLogPort,
				data, build, work, call,
				txtDataMasterPort, txtDataSlavePort, txtBuildPort, txtWorkPort,
				callPortPanel });

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 0)); // 间隔10个像素
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		String title = UIManager.getString("AddressScriptDialog.HomeClusterText");
		panel.setBorder(UITools.createTitledBorder(title, 4));

		return panel;
	}

//	/**
//	 * 生成集群面板
//	 * @return
//	 */
//	private JPanel createClusterPanel() {
//		JPanel area = createAreaPanel();
//		JPanel top = createTopClusterPanel();
//		JPanel bank = createBankClusterPanel();
//		JPanel home = createHomeClusterPanel();
//		
//		// 提示信息
//		String title = findContent("Dialog/address/tooltip/remark");
//		if (title != null && title.trim().length() > 0) {
//			home.setToolTipText(title);
//			bank.setToolTipText(title);
//			area.setToolTipText(title);
//			top.setToolTipText(title);
//		}
//
//		JPanel p1 = new JPanel();
//		p1.setLayout(new BorderLayout(0, 5));
//		p1.add(area, BorderLayout.NORTH);
//		p1.add(top, BorderLayout.CENTER);
//
//		JPanel p2 = new JPanel();
//		p2.setLayout(new BorderLayout(0, 5));
//		p2.add(p1, BorderLayout.NORTH);
//		p2.add(bank, BorderLayout.CENTER);
//
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(0, 5));
//		panel.add(p2, BorderLayout.NORTH);
//		panel.add(home, BorderLayout.CENTER);
//
//		return panel;
//	}
	
	/**
	 * 生成集群面板
	 * @return
	 */
	private JPanel createClusterPanel() {
		JPanel area = createAreaPanel();
		JPanel top = createTopClusterPanel();
		JPanel bank = createBankClusterPanel();
		JPanel home = createHomeClusterPanel();
		
		// 提示信息
		String title = UIManager.getString("AddressScriptDialog.RemarkText");
		if (title != null && title.trim().length() > 0) {
			home.setToolTipText(title);
			bank.setToolTipText(title);
			area.setToolTipText(title);
			top.setToolTipText(title);
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(area);
		panel.add(top);
		panel.add(bank);
		panel.add(home);
		return panel;
	}

	/**
	 * 设置中心面板
	 * @return
	 */
	private JPanel createCenterPanel() {
		// 保存脚本的面板，位于底部
		JPanel bottom = createSaveToPanel();
		JPanel center = createClusterPanel();

		// 滚动栏
		JScrollPane scroll = new JScrollPane(center);
		scroll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		// 文件面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
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
		panel.setLayout(new BorderLayout(0, 5));
		panel.setBorder(new EmptyBorder(4,4,4,4));
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

//	/**
//	 * 返回范围
//	 * @return Rectangle实例
//	 */
//	private Rectangle getBound() {
//		// 面板范围
//		Object obj = UITools.getProperity(KEY_RECT);
//		if (obj != null && obj.getClass() == Rectangle.class) {
//			return (Rectangle) obj;
//		}
//
//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
//		int width = 550;
//		int height = 520;
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
//		String title = findCaption("Dialog/address/title");
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
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "AddressScriptDialog/Bound", rect);
	}

	/**
	 * 读范围
	 * @return
	 */
	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"AddressScriptDialog/Bound");
		if (bounds == null) {
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			int width = 550;
			int height = 520;
			int x = (size.width - width) / 2;
			int y = (size.height - height) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
			return new Rectangle(x, y, width, height);
		}
		return bounds;
	}
	
	private void initDialog() {
		setTitle(UIManager.getString("AddressScriptDialog.Title"));
		setFrameIcon(UIManager.getIcon("AddressScriptDialog.TitleIcon"));

		// 位置
		setMinimumSize(new Dimension(300, 150));

		JPanel panel = createRootPanel();
		// 全部无效
		reset();

		setContentPane(panel);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.gui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
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
		RayAddressScriptDialog.selfHandle = this;

		// 显示窗口
		if (modal) {
			return showModalDialog(bind);
		} else {
			return showNormalDialog(bind);
		}
	}

}