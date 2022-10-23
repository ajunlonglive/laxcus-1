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
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.task.archive.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.event.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.skin.*;

/**
 * 显示属性信息
 * 
 * @author scott.liang
 * @version 1.0 7/29/2020
 * @since laxcus 1.0
 */
public class TerminalWareTagDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	/** 按纽 **/
	private JButton cmdOK = new JButton();
	
	/** 参数 **/
	private Sock sock;
	
	/** 新的软件名称 **/
	private WareTag tag;

	/**
	 * 构造修改软件名称窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalWareTagDialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}
	
	/**
	 * 设置新名称
	 * @param s
	 */
	public void setWare(WareTag e, Sock s) {
		tag = e;
		sock = s;
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
			if (event.getSource() == cmdOK) {
				dispose();
			} 
		}
	}
	
	/**
	 * 生成标签
	 * @param xmlPath 标签XML路径
	 * @param text 显示文本
	 * @return JLabel实例
	 */
	private JLabel createLeftCaption(String xmlPath) {
		String text = findCaption(xmlPath);
		text = String.format("<html><body>%s</body></html>", text);
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		return label;
	}

	/**
	 * 生成标签
	 * @param xmlPath 标签XML路径
	 * @param text 显示文本
	 * @return JLabel实例
	 */
	private JLabel createRightCaption(String text) {
		StringBuilder buf = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(text));
			do {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (buf.length() > 0) {
					buf.append("<br>");
				}
				buf.append(line);
			} while (true);
		} catch (IOException e) {

		}
		
		text = String.format("<html><body>%s</body></html>", buf.toString());
		
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		return label;
	}
	
	/**
	 * 查找图标
	 * @return
	 */
	private ImageIcon findIcon() {
		byte[] stream = GuideTaskPool.getInstance().readIcon(sock);
		if(stream != null) {
			ImageIcon icon = new ImageIcon(stream);
			Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			return new ImageIcon(image);
		}
		return null;
	}
	
	private JPanel createRightCenter() {
		// 日期
		String productDate = "";
		if (tag.getProductDate() > 0) {
			java.util.Date date = com.laxcus.util.datetime.SimpleDate.format(tag.getProductDate());
			SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			productDate = style.format(date);
		}
		
		
		int rows = 4;
		JPanel leftTop = new JPanel();
		leftTop.setLayout(new GridLayout(rows, 1, 0, 8));
		leftTop.add(createLeftCaption("Dialog/WareTag/software/product-name/title"));
		leftTop.add(createLeftCaption("Dialog/WareTag/software/version/title"));
		leftTop.add(createLeftCaption("Dialog/WareTag/software/product-date/title"));
		leftTop.add(createLeftCaption("Dialog/WareTag/software/maker/title"));

		JPanel rightTop = new JPanel();
		rightTop.setLayout(new GridLayout(rows, 1, 0, 8));
		rightTop.add(createRightCaption(tag.getProductName()));
		rightTop.add(createRightCaption(tag.getVersion().toString()));
		rightTop.add(createRightCaption(productDate));
		rightTop.add(createRightCaption(tag.getMaker()));
		
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(0, 8));
		left.add(leftTop, BorderLayout.NORTH);
		JLabel label = createLeftCaption("Dialog/WareTag/software/comment/title");
		label.setVerticalAlignment(SwingConstants.TOP);
		left.add(label, BorderLayout.CENTER);
		
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(0, 8));
		right.add(rightTop, BorderLayout.NORTH);
		
		label = createRightCaption(tag.getComment());
		label.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(label, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		scroll.getViewport().setBackground(label.getBackground());
		right.add(scroll, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 上侧面板
	 * @return
	 */
	private JPanel createAbove() {
		JLabel image = new JLabel();
		image.setVerticalAlignment(SwingConstants.CENTER);
		image.setHorizontalAlignment(SwingConstants.CENTER);
		Dimension d = new Dimension(48, 48);
		image.setMinimumSize(d);
		image.setMaximumSize(d);
		image.setPreferredSize(d);
		ImageIcon icon = findIcon();
		if (icon != null) {
			image.setIcon(icon);
		}
		image.setBorder(UITools.createTitledBorder());
		
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout());
		sub.add(image, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 0));
		panel.add(sub, BorderLayout.WEST);
		panel.add(createRightCenter(), BorderLayout.CENTER);
		return panel;
	}

	
	/**
	 * 构造布局
	 * @return
	 */
	private JPanel initPane() {
		cmdOK.setText(findCaption("Dialog/WareTag/buttons/okay/title"));
		cmdOK.addActionListener(this);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 6));
		bottom.add(new JSeparator(), BorderLayout.NORTH);
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(cmdOK, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		setRootBorder(panel);
		panel.add(createAbove(), BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 窗口尺寸！
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width =  (int) (size.getWidth() * 0.36);
		int height =  (int) (size.getHeight() * 0.48);

		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		// 面板
		JPanel pane = initPane();
		setContentPane(pane);

		// 窗口位置
		Rectangle rect =getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/WareTag/title");
		setTitle(title);

		// 显示！
		setVisible(true);
	}

}