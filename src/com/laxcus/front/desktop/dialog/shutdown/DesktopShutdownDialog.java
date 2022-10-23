/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.shutdown;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;

import com.laxcus.front.desktop.dialog.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 系统关闭对话框
 * 
 * 只有两个按纽，提示文本
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public class DesktopShutdownDialog extends DesktopLightDialog implements ActionListener {
	
	private static final long serialVersionUID = -3128548567773425108L;
	
	class MouseDragAdapter extends MouseAdapter {

		/** 拖放 **/
		private boolean dragged;

		/** 坐标 **/
		private Point axis;
		
		public MouseDragAdapter(){
			super();
			dragged = false;
		}

		public void mousePressed(MouseEvent e) {
			dragged = true;
			axis = new Point(e.getX(), e.getY());
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}

		public void mouseReleased(MouseEvent e) {
			dragged = false;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseDragged(MouseEvent e) {
			if (dragged) {
				int x = e.getXOnScreen() - axis.x;
				int y = e.getYOnScreen() - axis.y;
				setLocation(x, y);
			}
		}
	}

	/** 鼠标事件 **/
	private MouseDragAdapter listener = new MouseDragAdapter();
	
	/** 关闭按纽 **/
	private ShutdownButton cmdShutdown;
	
	/** 取消关闭 **/
	private ShutdownButton cmdCancel;

	/**
	 * 构造默认的系统关闭对话框
	 */
	public DesktopShutdownDialog() {
		super();
		
		// 不要刷新UI
		setRefreshUI(false);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 单击事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		if (event.getSource() == cmdShutdown) {
			setSelectedValue(new Boolean(true));
		} else if (event.getSource() == cmdCancel) {
			setSelectedValue(new Boolean(false));
		}
	}
	
	/**
	 * 生成一个按纽
	 * @param icon
	 * @param text
	 * @param title
	 * @return
	 */
	private ShutdownButton createButton(ImageIcon icon, String text, String title) {
		ShutdownButton but = new ShutdownButton();
		FontKit.setButtonText(but, text);
		FontKit.setToolTipText(but, title);
		
		but.setIcon(icon, 30); // 支持高亮
		but.setBorder(new EmptyBorder(8, 8, 8, 8));

		Dimension size = new Dimension(78, 78);
		but.setSize(size);

		but.setPreferredSize(size);
		but.setMinimumSize(size);
		but.setMaximumSize(size);
		
		but.setIconTextGap(4);
		
		but.addActionListener(this);

		// 设置组件字体
		setComponentFont(but, text);
		
		return but;
	}
	
	/**
	 * 显示边框
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	
		int width = ConfigParser.splitInteger(UIManager.getString("ShutdownDialog.width"), 410);
		int height = ConfigParser.splitInteger(UIManager.getString("ShutdownDialog.height"), 230);

		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2 - 68;
		if (y < 0) y = 0;
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * 返回首选的字体名称
	 * @return 返回名称，没有是空指针
	 */
	private Font getPreferredFont(String text) {
		// 找到系统的默认字体
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
		if (font != null) {
			String name = font.getName();
			boolean success = FontKit.hasFontName(name);
			if (success) {
				font = new Font(name, Font.BOLD, 12);
				// 判断可以正常显示
				success = FontKit.canDisplay(font, text);
				if (success) {
					return font;
				}
			}
		}
		
		// 其它字体
		String[] fontNames = { "微软雅黑", "新宋体", "宋体", "DialogInput", "SansSerif",
				"Dialog", "DialogInput", "Monospaced" };
		for (int i = 0; i < fontNames.length; i++) {
			boolean success = FontKit.hasFontName(fontNames[i]);
			if (success) {
				// 判断字体可以正常显示
				font = new Font(fontNames[i], Font.BOLD, 12);
				success = FontKit.canDisplay(font, text);
				if (success) {
					return font;
				}
			}
		}
		return null;
	}
	
	/**
	 * 设置组件的最佳字体
	 * @param j
	 * @param text
	 */
	private void setComponentFont(JComponent j, String text) {
		Font font = FontKit.findFont(j, text);
		if (font == null) {
			font = getPreferredFont(text);
		}
		if (font != null) {
			int size = font.getSize();
			// 如果是高分辨率屏幕显示
			if (GUIKit.isHighScreen()) {
				size = 18;
			} else {
				size = (size <= 12 ? 16 : size + 4);
			}
			j.setFont(new Font(font.getName(), Font.BOLD, size));
		}
	}
	
	/**
	 * 设置背景色
	 * @param self
	 * @param c
	 */
	private void setSubBackground(java.awt.Container self, Color c) {
		Component[] elements = self.getComponents();
		for (int i = 0; elements != null && i < elements.length; i++) {
			Component element = elements[i];
			if (Laxkit.isClassFrom(element, java.awt.Container.class)) {
				setSubBackground((java.awt.Container) element, c);
			} else {
				element.setBackground(c);
			}
		}
		self.setBackground(c);
	}
	
	/**
	 * 初始化
	 * @param text
	 * @param shutdown
	 * @param shutdownText
	 * @param cancel
	 * @param cancelText
	 */
	private void initDialog() {
		String text = UIManager.getString("ShutdownDialog.showContentText");

		String shutdownText = UIManager.getString("ShutdownDialog.shutdownButtonText");
		String shutdownTitle = UIManager.getString("ShutdownDialog.shutdownButtonTitle");
		Icon shutdown = UIManager.getIcon("ShutdownDialog.shutdownButtonIcon");
		String cancelText = UIManager.getString("ShutdownDialog.cancelButtonText");
		String cancelTitle = UIManager.getString("ShutdownDialog.cancelButtonTitle");
		Icon cancel = UIManager.getIcon("ShutdownDialog.cancelButtonIcon");

		String html = String.format("<html>%s</html>", text);
		JLabel label = new JLabel(html, SwingConstants.CENTER); // 居中显示
		label.setVerticalAlignment(SwingConstants.CENTER);
		setComponentFont(label, text);

		// 建立按纽
		cmdShutdown = createButton((ImageIcon) shutdown, shutdownText, shutdownTitle);
		cmdCancel = createButton((ImageIcon) cancel, cancelText, cancelTitle);

		// 如果是灰色
		if (Skins.isGraySkin()) {
			ESL esl = new ESL(160, 0, 233);
			Color color = esl.toColor();
			cmdShutdown.setForeground(color);
			cmdCancel.setForeground(color);
			label.setForeground(color);
		}

		// 显示
		JPanel twins = new JPanel();
		twins.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		twins.add(cmdShutdown);
		twins.add(cmdCancel);

		JPanel bo = new JPanel();
		bo.setLayout(new BorderLayout(10, 0));
		bo.add(new JPanel(), BorderLayout.NORTH);
		bo.add(twins, BorderLayout.CENTER);
		bo.add(new JPanel(), BorderLayout.WEST);
		bo.add(new JPanel(), BorderLayout.EAST);
		bo.add(new JPanel(), BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(label, BorderLayout.CENTER);
		panel.add(bo, BorderLayout.SOUTH);
		
		// 直角效果
		panel.setBorder(new CompoundBorder(new HighlightBorder(1, false), new EmptyBorder(0, 0, 20, 0)));
		
		// 鼠标事件
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);

		// 主面板
		Container canvas = getContentPane();
		canvas.setLayout(new BorderLayout(0, 0));
		canvas.add(panel, BorderLayout.CENTER);

		if (isMetalUI()) {
			Color color = (Skins.isGraySkin() ? UIManager.getColor("ShutdownDialog.mealGrayBackgroundColor")
					: MetalLookAndFeel.getWindowTitleBackground());
			// 没有，使用默认值
			if (color != null) {
				setSubBackground(canvas, color);
			}
		}

		// 位置
		Rectangle rect = getBound();
		setBounds(rect);
		setMinimumSize(new Dimension(300, 250));
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.util.desktop.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 非模态，弹出错误
		if (!modal) {
			throw new RuntimeException("must be modal!");
		}
		
		// 初始化窗口
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);
		
		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);
		
		// 窗体外沿去掉边框
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// 隐藏标题栏
		hideTitlePane();

		// 显示窗口
		return showModalDialog(parent, cmdCancel);
	}
	
	/**
	 * 生成对话框
	 * @param parent 父类
	 * @return 返回结果
	 */
	public Object showDialog(Component parent) {
		return showDialog(parent, true);
	}

}


//private void setSubForeground(JComponent[] c) {
//	ESL esl = new ESL(160, 0, 220);
//	for (int i = 0; i < c.length; i++) {
//		c[i].setForeground(esl.toColor());
//	}
//}

///**
//* 设置组件的字体，最佳字体
//* @param j 组件
//*/
//private void setComponentFont2(JComponent j, String text) {
//	Font font = getPreferredFont(text);
//	if (font != null) {
//		j.setFont(new Font(font.getName(), Font.BOLD, 14));
//		return;
//	}
//
//	// 找到匹配的字体
//	font = FontKit.findFont(j, text);
//	if (font != null) {
//		j.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
//	}
//}

///**
//* 设置组件的最佳字体
//* @param j
//* @param text
//*/
//private void setComponentFont(JComponent j, String text) {
////	Font font = FontKit.findFont(j, text);
////	if (font != null) {
////		j.setFont(new Font(font.getName(), Font.BOLD, 14));
////		return;
////	}
//	
//	
//	
//	// 设置默认的字体
//	boolean success = FontKit.setDefaultFont(j);
//	if (success) {
//		Font font = j.getFont();
//		j.setFont(new Font(font.getName(), Font.BOLD, 14));
//		return;
//	}
//
//	// 找到默认的...
//	Font font = getPreferredFont(text);
//	if (font != null) {
//		j.setFont(new Font(font.getName(), Font.BOLD, 14));
//		return;
//	}
//}


///**
// * 初始化
// * @param text
// * @param shutdown
// * @param shutdownText
// * @param cancel
// * @param cancelText
// */
//public void initDialog(String text, ImageIcon shutdown, String shutdownText, ImageIcon cancel, String cancelText) {	
//	String html = String.format("<html>%s</html>", text);
//	JLabel label = new JLabel(html, SwingConstants.CENTER); // 居中显示
//	label.setVerticalAlignment(SwingConstants.CENTER);
//	setComponentFont(label, text);
//	
//	// 建立按纽
//	cmdShutdown = createButton(shutdown, shutdownText,"");
//	cmdCancel = createButton(cancel, cancelText,"");
//	
//	// 阳刻
////	cmdShutdown.setBorder(new ShadowBorder(false));
////	cmdCancel.setBorder(new ShadowBorder(true));
////	label.setBorder(new ShadowBorder(false));
//	
//	// 显示
//	JPanel twins = new JPanel();
//	twins.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
//	twins.add(cmdShutdown);
//	twins.add(cmdCancel);
//	
//	JPanel bo = new JPanel();
//	bo.setLayout(new BorderLayout(10, 0));
//	bo.add(new JPanel(), BorderLayout.NORTH);
//	bo.add(twins, BorderLayout.CENTER);
//	bo.add(new JPanel(), BorderLayout.WEST);
//	bo.add(new JPanel(), BorderLayout.EAST);
//	
//	JPanel panel = new JPanel();
//	panel.setLayout(new BorderLayout(0, 8));
//	panel.add(label, BorderLayout.CENTER);
//	panel.add(bo, BorderLayout.SOUTH);
//	
//	// 边框
//	panel.setBorder(new EmptyBorder(10, 12, 20, 12));
//	
//	// 面板边框
////	CompoundBorder cb = new CompoundBorder(new ShadowBorder(true, Color.DARK_GRAY, new ESL(160, 0, 120).toColor(), true),
////	CompoundBorder cb = new CompoundBorder(new ShadowBorder(true, Color.GRAY, new ESL(160, 0, 180).toColor(), true),
////	CompoundBorder cb = new CompoundBorder(new ShadowBorder(true, new ESL(160, 0, 60).toColor(), new ESL(160, 0, 170).toColor(), true),
//	
//	ESL e = new RGB(Color.GRAY).toESL();
//	CompoundBorder cb = new CompoundBorder(new EtchBorder(true, e.toDraker(50).toColor(), e.toBrighter(40).toColor(), true),
//			new EmptyBorder(10, 12, 20, 12));
//	panel.setBorder(cb);
//
//	// 主面板
//	Container canvas = getContentPane();
//	canvas.setLayout(new BorderLayout(0, 0));
//	canvas.add(panel, BorderLayout.CENTER);
//	
//	// 设置前景色
//	setSubForeground(new JComponent[]{label, cmdShutdown, cmdCancel});
//	
//	// 更新背景色
//	if (Skins.isNimbus()) {
//		
//		Color color = UIManager.getColor("ShutdownDialog.nimbusBackgroundColor");
//		// 没有，使用默认值
//		if(color == null){
//			 color = new ESL(141, 115, 115).toColor();
//		}
//		setSubBackground(canvas, color);
//		
//////		ESL esl = new ESL(141, 115, 125);
////		ESL esl = new ESL(141, 115, 115);
////
////		//			ESL esl = new ESL(140, 240, 150);
////		//			canvas.setBackground(esl.toColor());
////		//			ESL esl = new ESL(134, 126, 88);
////
////		setSubBackground(canvas, esl.toColor());
//	}
//	
////	// 阳刻浮雕
////	panel.setBorder(new ShadowBorder(false));
//	
//	// 位置
//	Rectangle rect = getBound();
//	setBounds(rect);
//	setMinimumSize(new Dimension(300, 250));
//}




//public void paintComponent(Graphics g) {
//	if(Laxkit.isClassFrom(g, Graphics2D.class)) {
//		System.out.printf("%s, darw image\n",g.getClass().getName());
//		
//		int width = getWidth();
//		int height = getHeight();
//		
//		RoundRectangle2D.Float rf = new RoundRectangle2D.Float(0, 0, width -1, height-1, 10, 10);
//		Graphics2D g2 = (Graphics2D)g;
//		g2.draw(rf);
//	} else {
//		super.paintComponent(g);
//	}
//}


///**
// * 隐藏标题栏
// */
//public void hideTitlePane() {
//	// 清除标题栏
//	InternalFrameUI ui = getUI();
//	if (ui!= null && Laxkit.isClassFrom(ui, BasicInternalFrameUI.class)) {
//		((BasicInternalFrameUI) ui).setNorthPane(null);
//		putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
//	}
//}

///*
// * (non-Javadoc)
// * @see com.laxcus.gui.dialog.LightDialog#updateUI()
// */
//@Override
//public void updateUI() {
//	super.updateUI();
//	hideTitlePane();
//}


///*
// * (non-Javadoc)
// * @see com.laxcus.gui.dialog.LightDialog#updateUI()
// */
//@Override
//public void updateUI() {
//	super.updateUI();
//	
//	// 窗体外沿去掉边框
//	setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
//}


//if(isMetalUI() && Skins.isGraySkin()) {
//	Color color = MetalLookAndFeel.getWindowTitleBackground();
////	Color color = UIManager.getColor("ShutdownDialog.mealGrayBackgroundColor");
//	// 没有，使用默认值
//	if (color == null) {
//		color = new ESL(141, 115, 115).toColor();
//	}
//	setSubBackground(canvas, color);
//}
