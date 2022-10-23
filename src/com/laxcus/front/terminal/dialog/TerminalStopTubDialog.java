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
 * 边缘应用停止参数输入窗口
 * 
 * @author scott.liang
 * @version 1.0 9/27/2020
 * @since laxcus 1.0
 */
public class TerminalStopTubDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;
	
	private final static String BOUND = TerminalStopTubDialog.class.getSimpleName() + "_BOUND";

	/** 取消 **/
	private JButton cmdCancel = new JButton();
	
	/** 按纽 **/
	private JButton cmdOK = new JButton();
	
	/** 数字 **/
	private JTextField digit = new JTextField();
	
	/** 参数输入窗口 **/
	private JTextArea area = new JTextArea();

	/** 标记 **/
	private TubTag tag;

	/** 结果 **/
	private int result = -1;
	
	/** 编号 **/
	private long pid = -1;
	
	/** 初始化参数 **/
	private String params;
	
	/**
	 * 构造版本窗口
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalStopTubDialog(Frame owner, boolean modal) throws HeadlessException {
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
	 * 返回进程号
	 * @return
	 */
	public long getPID() {
		return pid;
	}
	
	/** 
	 * 输出参数
	 * @return
	 */
	public String getParams() {
		return params;
	}
	
	/**
	 * 参数不足
	 * @param name
	 */
	private void showParamMissing() {
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/StopTub/missing/title");
		String content = findContent("Dialog/StopTub/missing"); 
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	
	/**
	 * 检测参数
	 * @return
	 */
	private boolean check() {
		String text = digit.getText();
		if (text.trim().isEmpty()) {
			showParamMissing();
			digit.requestFocus();
			return false;
		}
		// 生成进程号
		pid = Long.parseLong(text.trim());
		return true;
	}
	
	/**
	 * 确认执行
	 * @return 真或者假
	 */
	private boolean confirm() {
		String title = findCaption("Dialog/StopTub/confirm/title");
		String content = findContent("Dialog/StopTub/confirm");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 确认取消
	 * @return 真或者假
	 */
	private boolean cancel() {
		String title = findCaption("Dialog/StopTub/cancel/title");
		String content = findContent("Dialog/StopTub/cancel");
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
		setButtonText(cmdOK, findCaption("Dialog/StopTub/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/StopTub/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
		
		// 提示信息
		String s = tag.getStopArgumentTooltip();
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
		sub.setToolTipText(sub.getText());
		Dimension d = new Dimension(60, 28);
//		sub.setPreferredSize(d);
		sub.setMinimumSize(d);
//		sub.setMaximumSize(d);
	}
	
	/**
	 * 主界面
	 * @return
	 */
	private JPanel createCenter() {
		JLabel caption = new JLabel(findContent("Dialog/StopTub/naming"));
		JLabel process = new JLabel(findContent("Dialog/StopTub/pid"));
		JLabel param = new JLabel(findContent("Dialog/StopTub/params"));
		
		setSubSize(caption);
		setSubSize(process);
		setSubSize(param);

		JTextField field = new JTextField(tag.getNameText());
		field.setEditable(false);
		
		digit.setDocument(new DigitDocument(digit, 10));
		
//		field.setBorder(UITools.createTitledBorder("", 1)); // 边框
//		digit.setBorder(UITools.createTitledBorder("", 1)); // 边框
//		area.setBorder(UITools.createTitledBorder("", 1)); // 边框

		field.setToolTipText(tag.getCaption());
		digit.setToolTipText(process.getText());
		area.setToolTipText(param.getText());
		
		JScrollPane scroll = new JScrollPane(area,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(area.getBackground());

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(6, 0));
		north.add(caption, BorderLayout.WEST);
		north.add(field, BorderLayout.CENTER);
		
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout(6, 0));
		south.add(process, BorderLayout.WEST);
		south.add(digit, BorderLayout.CENTER);
		
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 8));
		sub.add(north);
		sub.add(south);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(6, 0));
		center.add(param, BorderLayout.WEST);
		center.add(scroll, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(sub, BorderLayout.NORTH);
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
		int width = 530; // (int) (size.getWidth() * 0.26);
		int height = 240; // (int) (size.getHeight() * 0.55);
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
		String title = findCaption("Dialog/StopTub/title");
		setTitle(title);

		setVisible(true);
		
		// 返回结果
		return result;
	}

}