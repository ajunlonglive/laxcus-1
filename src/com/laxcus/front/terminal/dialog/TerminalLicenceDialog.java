/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 显示用户许可协议对话框
 *
 * @author scott.liang
 * @version 1.0 4/3/2020
 * @since laxcus 1.0
 */
public class TerminalLicenceDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -9189343876362936960L;
	
	/** 显示的文本 **/
	private String licenceText;

	/** 显示的标题 **/
	private JTextArea licence = new JTextArea();

	/** 接受 **/
	private JButton cmdOK = new JButton();

	/** 拒绝 **/
	private JButton cmdCancel = new JButton();

	/** 确认接受 **/
	private boolean accepted;

	/**
	 * @param frame
	 * @param modal
	 */
	public TerminalLicenceDialog(Frame frame, boolean modal, String content) {
		super(frame, modal);
		accepted = false;
		// 保存文本！
		licenceText = content;
	}
	
	/**
	 * 判断接受！
	 * @return 返回真或者假
	 */
	public boolean isAccpeted() {
		return accepted;
	}

	/**
	 * 调用线程
	 *
	 * @author scott.liang
	 * @version 1.0 3/24/2020
	 * @since laxcus 1.0
	 */
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

	/**
	 * 事件点击
	 * @param e 事件
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			accepted = true;
			dispose();
		} else if (e.getSource() == cmdCancel) {
			accepted = false;
			dispose();
		}
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return TerminalLauncher.getInstance().findCaption(xmlPath);
//	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 580;
		int height = 360;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 初始化组件
	 */
	private void initControls() {	
		setButtonText(cmdOK, findCaption("Dialog/Licence/buttons/okay/title"));
		cmdOK.setMnemonic('A');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/Licence/buttons/cancel/title"));
		cmdCancel.setMnemonic('R');
		cmdCancel.addActionListener(this);
		cmdCancel.setDefaultCapable(true);
	}

	/**
	 * 根据类型生成不同的标题
	 * @return 返回字符串
	 */
	private String getBorderTitle() {
		return findCaption("Dialog/Licence/borderTitle");
	}

	/**
	 * 生成中央面板
	 * @return JPanel实例
	 */
	private JPanel createCenterPanel() {
		licenceText = Laxkit.trim(licenceText);
		licence.setEditable(false);
		licence.setLineWrap(true);
		licence.setText(licenceText);

		JScrollPane scroll = new JScrollPane(licence);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);

		// 文件面板
		String title = getBorderTitle();
		panel.setBorder(UITools.createTitledBorder(title, 6));

		return panel;
	}

	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 8, 0));
		right.add(cmdOK);
		right.add(cmdCancel);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(right, BorderLayout.EAST);

		return bottom;
	}

	/**
	 * 根据类型生成不同的标题
	 * @return 返回字符串
	 */
	private String getDialogTitle() {
		return findCaption("Dialog/Licence/dialogTitle");
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel createPanel() {
		// 初始化显示控件
		initControls();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 5));
		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 显示窗口
	 */
	public boolean showDialog() {
		setContentPane(createPanel());
		
		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = getDialogTitle();
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);

		// 返回结果
		return accepted;
	}

}