/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 边缘应用停止参数输入窗口
 * 
 * @author scott.liang
 * @version 1.0 9/27/2020
 * @since laxcus 1.0
 */
public class TerminalPrintTubServiceDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;

	private final static String BOUND = TerminalPrintTubServiceDialog.class.getSimpleName() + "_BOUND";

	/** 取消 **/
	private JButton cmdCancel = new JButton();

	/** 按纽 **/
	private JButton cmdOK = new JButton();

	/** 边缘应用名称 **/
	private JTextField txtName = new JTextField();

	/** 选择全部 **/
	private JCheckBox cmdAll = new JCheckBox();

	/** 结果 **/
	private int result = -1;

	/** 边缘应用名称 **/
	private String naming;

	/**
	 * 构造版本窗口
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalPrintTubServiceDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new ClickThread(event));
	}

	class ClickThread extends SwingEvent {
		ActionEvent event;

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			if (event.getSource() == cmdOK) {
				boolean success = check(); // 检查
				if (success) {
					success = confirm(); // 确认执行
				}
				// 成功，关闭窗口
				if (success) {
					result = JOptionPane.YES_OPTION;
					saveBound();
					dispose();
				}
			} else if (event.getSource() == cmdCancel) {
				boolean success = cancel();
				if (success) {
					result = JOptionPane.NO_OPTION;
					// 提示确认取消
					dispose();
				}
			} else if(event.getSource() == cmdAll) {
				modify();
			}
		}
	}

	/**
	 * 调整
	 */
	private void modify() {
		boolean select = cmdAll.isSelected();
		txtName.setEnabled(!select);
	}

	/** 
	 * 输出参数
	 * @return
	 */
	public String getNaming() {
		return naming;
	}

	/**
	 * 参数不足
	 * @param name
	 */
	private void showParamMissing() {
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/PrintTub/missing/title");
		String content = findContent("Dialog/PrintTub/missing"); 
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 检测参数
	 * @return
	 */
	private boolean check() {
		boolean select = cmdAll.isSelected();
		if(select) {
			naming = null;
			return true;
		}

		String text = txtName.getText();
		if (text.trim().isEmpty()) {
			showParamMissing();
			txtName.requestFocus();
			return false;
		}
		naming = text.trim();
		return true;
	}

	/**
	 * 确认执行
	 * @return 真或者假
	 */
	private boolean confirm() {
		String title = findCaption("Dialog/PrintTub/confirm/title");
		String content = findContent("Dialog/PrintTub/confirm");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 确认取消
	 * @return 真或者假
	 */
	private boolean cancel() {
		String title = findCaption("Dialog/PrintTub/cancel/title");
		String content = findContent("Dialog/PrintTub/cancel");
		int who = MessageDialog.showMessageBox(this, title, 
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle e = super.getBounds();
		if (e != null) {
			UITools.putProperity(BOUND, e);
		}
	}

	/**
	 * 初始化控件
	 */
	private void initControls() {
		setButtonText(cmdOK, findCaption("Dialog/PrintTub/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/PrintTub/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);

		setButtonText(cmdAll, findCaption("Dialog/PrintTub/buttons/all/title"));
		cmdAll.setMnemonic('A');
		cmdAll.addActionListener(this);
	}

	/**
	 * 初始化按钮
	 * @return
	 */
	private JPanel createButtons() {
		JPanel east = new JPanel();
		east.setLayout(new GridLayout(1, 2, 8, 0));
		east.add(cmdOK);
		east.add(cmdCancel);

		// 做出一个分割线
		JPanel js = new JPanel();
		js.setLayout(new BorderLayout(0, 6));
//		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		js.add(new JPanel(), BorderLayout.CENTER);
		js.add(east, BorderLayout.EAST);
		return js;
	}

//	private void setSubSize(JComponent sub) {
//		Dimension d = new Dimension(50, 18);
//		sub.setPreferredSize(d);
//		sub.setMinimumSize(d);
//		sub.setMaximumSize(d);
//	}

	private JPanel createCenter() {
		JLabel caption = new JLabel(findContent("Dialog/PrintTub/naming"));
//		setSubSize(caption);

//		txtName.setBorder(UITools.createTitledBorder("", 1)); // 边框

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(6, 0));
		north.add(caption, BorderLayout.WEST);
		north.add(txtName, BorderLayout.CENTER);

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 8));
		sub.add(north);
		sub.add(cmdAll);
		sub.setBorder(UITools.createTitledBorder("", 4)); // 边框

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(sub, BorderLayout.NORTH);
		panel.add(new JPanel(), BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 构造布局
	 * @return
	 */
	private JPanel initPane() {
		initControls();

		JScrollPane scroll = new JScrollPane(createCenter());
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		setRootBorder(panel);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(createButtons(), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 系统中取出参数
		Object e = UITools.getProperity(BOUND);
		if (e != null && e.getClass() == Rectangle.class) {
			return (Rectangle) e;
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 480;
		int height = 180;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 * @param caption
	 * @param table
	 * @return
	 */
	public int showDialog() {
		setContentPane(initPane());

		setBounds(getBound());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(200, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/PrintTub/title");
		setTitle(title);

		setVisible(true);

		// 返回结果
		return result;
	}

}