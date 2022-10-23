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

import com.laxcus.front.terminal.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;

/**
 * 版本窗口
 * 
 * @author scott.liang
 * @version 1.0 7/1/2009
 * @since laxcus 1.0
 */
public class TerminalAboutDialog extends CommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;

	/** 按纽 **/
	private JButton cmdOK = new JButton();

	/**
	 * 构造版本窗口
	 */
	public TerminalAboutDialog() {
		super();
	}

	/**
	 * 构造版本窗口
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalAboutDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
	}

	/**
	 * 解析标签
	 * @param xmlPath
	 * @return
	 */
	private String getCaption(String xmlPath) {
		return TerminalLauncher.getInstance().findCaption(xmlPath);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new ActionThread(event));
	}

	class ActionThread extends SwingEvent {
		ActionEvent event;

		ActionThread(ActionEvent e) {
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
	 * 构造布局
	 * @return
	 */
	private JPanel initPane() {
		JLabel image = new JLabel();

		JLabel html = new JLabel();
		html.setHorizontalAlignment(SwingConstants.LEFT);
		html.setVerticalAlignment(SwingConstants.TOP);

		String buttonTitle = getCaption("Dialog/about/okay/title");

		LocalSelector selector = new LocalSelector("conf/front/terminal/about/config.xml");	
		String path = selector.findPath("resource");

		// 设置图片
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/about/logo.png");
		if (icon != null) {
			image.setIcon(icon);
		}

		// 生成文本，显示它！
		try {
			byte[] b = loader.findAbsoluteStream(path);
			String content = new UTF8().decode(b);
			html.setText(content);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		Font font = TerminalProperties.readSystemFont();
		if (font != null) {
			html.setFont(font);
		}

		FontKit.setButtonText(cmdOK, buttonTitle);
		cmdOK.addActionListener(this);
		cmdOK.setMnemonic('O');

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());
		left.add(image, BorderLayout.NORTH);
		left.add(new JPanel(), BorderLayout.CENTER);

		// 同一个背景色
		html.setBackground(left.getBackground());
		JScrollPane scroll = new JScrollPane(html);
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		scroll.setBackground(left.getBackground());
		scroll.getViewport().setBackground(left.getBackground());

		// 主面板
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(10, 0));
		center.add(left, BorderLayout.WEST);
		center.add(scroll, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 0));
		bottom.add(cmdOK, BorderLayout.EAST);
		
		// 做出一个分割线
		JPanel js = new JPanel();
		js.setLayout(new BorderLayout(0, 8));
		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		js.add(bottom, BorderLayout.CENTER);

		JPanel root = new JPanel();
		root.setBorder(new EmptyBorder(10, 8, 10, 8));
		root.setLayout(new BorderLayout(0, 0));
		root.add(center, BorderLayout.CENTER);
		root.add(js, BorderLayout.SOUTH);
		return root;
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		JPanel pane = initPane();
		setContentPane(pane);

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)(size.getWidth() * 0.36);
		int height = (int)(size.getHeight() * 0.37);
		int x = (size.width - width)/2;
		int y = (size.height - height)/2;
		setBounds(new Rectangle(x, y, width, height));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(386, 252));
		setAlwaysOnTop(true);

		// 标题
		String title = getCaption("Dialog/about/title");
		setTitle(title);

		setVisible(true);
	}

}