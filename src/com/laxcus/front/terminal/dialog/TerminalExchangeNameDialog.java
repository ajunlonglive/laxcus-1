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
import javax.swing.border.*;

import com.laxcus.util.event.*;

/**
 * 修改软件名称窗口
 * 
 * @author scott.liang
 * @version 1.0 7/28/2020
 * @since laxcus 1.0
 */
public class TerminalExchangeNameDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	private JTextField field = new JTextField();

	private JButton cmdCancel = new JButton();

	private JButton cmdOK = new JButton();
	
	/** 新的软件名称 **/
	private String newName;

	/**
	 * 构造修改软件名称窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalExchangeNameDialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}
	
	/**
	 * 设置新名称
	 * @param s
	 */
	public void setNewName(String s) {
		newName = s;
	}
	
	/**
	 * 返回新的名称
	 * @return
	 */
	public String getNewName() {
		return newName;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ClickThread(e));
	}

	class ClickThread extends SwingEvent {
		ActionEvent event;

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			if (event.getSource() == cmdCancel) {
				newName = null;
				dispose();
			} else if(event.getSource() == cmdOK) {
				boolean success = saveName();
				if(success) {
					dispose();
				}
			}
		}
	}
	
	/**
	 * 保存名称
	 * @return 成功返回真，否则假
	 */
	private boolean saveName() {
		String text = field.getText();
		if(text.trim().isEmpty()) {
			return false;
		}
		newName = text.trim();
		return true;
	}
	
//	private void setSubSize(JComponent sub) {
//		Dimension d = new Dimension(50, 28);
//		sub.setPreferredSize(d);
//		sub.setMinimumSize(d);
//		sub.setMaximumSize(d);
//	}

	/**
	 * 构造布局
	 * @return
	 */
	private JPanel initPane() {
		// 显示文本
		if (newName != null) {
			field.setText(newName);
		}
		
		String text = findCaption("Dialog/ExchangeName/label/name/title");
		JLabel caption = new JLabel(text);
//		setSubSize(caption);

		text = findCaption("Dialog/ExchangeName/buttons/okay/title");
		cmdOK.setText(text);
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		text = findCaption("Dialog/ExchangeName/buttons/cancel/title");
		cmdCancel.setText(text);
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(6, 0));
		north.setBorder(new EmptyBorder(6, 8, 0, 8));
		north.add(caption, BorderLayout.WEST);
		north.add(field, BorderLayout.CENTER);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 5, 0));
		right.add(cmdOK);
		right.add(cmdCancel);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 6));
		bottom.add(new JSeparator(), BorderLayout.NORTH);
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(right, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		setRootBorder(panel);
		panel.add(north, BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 窗口尺寸！
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 420; // (int) (size.getWidth() * 0.35);
		int height = 118; // (int) (size.getHeight() * 0.35);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		setContentPane(initPane());

		// 窗口位置
		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(320, 180));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/ExchangeName/title");
		setTitle(title);

		// 显示！
		setVisible(true);
	}

}