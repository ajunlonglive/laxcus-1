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

import com.laxcus.tub.servlet.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 边缘应用启动参数输入窗口
 * 
 * @author scott.liang
 * @version 1.0 9/27/2020
 * @since laxcus 1.0
 */
public class TerminalRunTubDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;

	private final static String BOUND = TerminalRunTubDialog.class.getSimpleName() + "_BOUND";

	/** 取消 **/
	private JButton cmdCancel = new JButton();

	/** 按纽 **/
	private JButton cmdOK = new JButton();

	/** 参数输入窗口 **/
	private JTextArea area = new JTextArea();

	/** 标记 **/
	private TubTag tag;

	/** 结果 **/
	private int result = -1;

	/** 初始化参数 **/
	private String params;

	/**
	 * 构造版本窗口
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalRunTubDialog(Frame owner, boolean modal) throws HeadlessException {
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
				boolean success = confirm(); // 确认执行
				// 成功，关闭窗口
				if (success) {
					params = area.getText();
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
			}
		}
	}

	/** 
	 * 输出参数
	 * @return
	 */
	public String getParams() {
		return params;
	}

	/**
	 * 确认执行
	 * @return 真或者假
	 */
	private boolean confirm() {
		String title = findCaption("Dialog/RunTub/confirm/title");
		String content = findContent("Dialog/RunTub/confirm");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 确认取消
	 * @return 真或者假
	 */
	private boolean cancel() {
		String title = findCaption("Dialog/RunTub/cancel/title");
		String content = findContent("Dialog/RunTub/cancel");
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
		setButtonText(cmdOK, findCaption("Dialog/RunTub/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/RunTub/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);

		// 启动参数提示
		String s = tag.getStartArgumentTooltip();
		if (s != null && s.trim().length() > 0) {
			area.setToolTipText(s);
		}
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

	private void setSubSize(JLabel sub) {
		Dimension d = new Dimension(50, 28);
		//		sub.setPreferredSize(d);
		sub.setMinimumSize(d);
		//		sub.setMaximumSize(d);
	}

	private void setSubSize(JLabel[] subs) {
		for(JLabel e : subs) {
			setSubSize(e);
		}
	}

	private JPanel createCenter() {
		JLabel caption = new JLabel(findContent("Dialog/RunTub/naming"));
		JLabel param = new JLabel(findContent("Dialog/RunTub/params"));
		setSubSize(new JLabel[] {caption, param});

		JTextField field = new JTextField(tag.getCaption());		
		field.setEditable(false);
		field.setToolTipText(tag.getCaption());

		JScrollPane scroll = new JScrollPane(area,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(area.getBackground());

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(6, 0));
		north.add(caption, BorderLayout.WEST);
		north.add(field, BorderLayout.CENTER);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(6, 0));
		center.add(param, BorderLayout.WEST);
		center.add(scroll, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(north, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		panel.setBorder(UITools.createTitledBorder("", 4)); // 边框

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
		int width = 520; // (int) (size.getWidth() * 0.26);
		int height = 210; // (int) (size.getHeight() * 0.55);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;

		return new Rectangle(x, y, width, height);
	}

	private void setTag(TubTag e) {
		tag = e;
	}

	/**
	 * 显示窗口
	 * @param caption
	 * @param table
	 * @return
	 */
	public int showDialog(TubTag tag) {
		setTag(tag);
		setContentPane(initPane());

		setBounds(getBound());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(200, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/RunTub/title");
		setTitle(title);

		setVisible(true);

		// 返回结果
		return result;
	}

}