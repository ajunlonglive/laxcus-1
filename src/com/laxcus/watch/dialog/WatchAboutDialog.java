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

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.watch.*;

/**
 * WATCH产品介绍窗口
 * 
 * @author scott.liang
 * @version 1.0 4/1/2013
 * @since laxcus 1.0
 */
public class WatchAboutDialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	/** 确定按纽 **/
	private JButton cmdOK = new JButton();

	/**
	 * 构造WATCH产品介绍窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public WatchAboutDialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ActionThread(e));
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

		String buttonTitle = findCaption("Dialog/about/okay/title");

		LocalSelector selector = new LocalSelector("conf/watch/about/config.xml");		
		String path = selector.findPath("resource");

		// 设置图片
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/window/about/logo.png");
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
		
		Font font = WatchProperties.readSystemFont();
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
	 * 窗口尺寸！
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (size.getWidth() * 0.36);
		int height = (int) (size.getHeight() * 0.37);
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

		setMinimumSize(new Dimension(386, 252));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/about/title");
		setTitle(title);

		// 显示！
		setVisible(true);
	}

}