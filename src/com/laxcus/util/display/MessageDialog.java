/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 消息对话框
 * 
 * @author scott.liang
 * @version 1.0 10/30/2018
 * @since laxcus 1.0
 */
public class MessageDialog extends CommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5824974195862208955L;
	
	private final static String BOUND = MessageDialog.class.getSimpleName() + "_BOUND";
	
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
	private MouseDragAdapter mouseListener = new MouseDragAdapter();
	
	/** 不装修边框 **/
	private static volatile boolean undressing = false;
	
	/** 使用平面按纽 **/
	private static volatile boolean usingFlatButton = false;
	
	/**
	 * 设置不装修边框
	 * @param b 真或者假
	 */
	public static void setUndressing(boolean b) {
		MessageDialog.undressing = b;
	}
	
	/**
	 * 判断不装修边框
	 * @return 返回真或者假
	 */
	public static boolean isUndressing() {
		return MessageDialog.undressing;
	}
	
	/**
	 * 设置使用平面按纽
	 * @param b 真或者假
	 */
	public static void setFlatButton(boolean b) {
		MessageDialog.usingFlatButton = b;
	}
	
	/**
	 * 判断使用平面按纽
	 * @return 返回真或者假
	 */
	public static boolean isFlatButton() {
		return MessageDialog.usingFlatButton;
	}
	
	/** 图标 **/
	private JLabel lblImage = new JLabel("", SwingConstants.CENTER);

	/** 文本 **/
	private JLabel lblContent = new JLabel("", SwingConstants.LEFT);

	/** YES按纽 **/
	private JButton cmdYes; // = new JButton("Yes");

	/** NO按纽 **/
	private JButton cmdNo; // = new JButton("No");

	/** 取消按纽 **/
	private JButton cmdCancel;// = new JButton("Cancel");

	/** 返回值 **/
	private int select;
	
	/**
	 * 生成按纽
	 */
	private void createDefaultButtons() {
		if (MessageDialog.isFlatButton() && GUIKit.isMetalUI()) {
			cmdYes = new FlatButton("Yes");
			cmdNo = new FlatButton("No");
			cmdCancel = new FlatButton("Cancel");
		} else {
			cmdYes = new JButton("Yes");
			cmdNo = new JButton("No");
			cmdCancel = new JButton("Cancel");
		}
	}

	/**
	 * 构造消息对话框，指定参数
	 * @param parent
	 * @param model
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 */
	public MessageDialog(Frame parent, boolean model, String title, int iconId, Icon icon, String content, int buttonId) {
		super(parent, model);
		// 初始化窗口
		initDialog(title, iconId, icon, content, buttonId);
		// 范围
		setDefaultBounds(this);
	}
	
	/**
	 * 构造消息对话框，指定参数
	 * @param parent
	 * @param modal
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 */
	public MessageDialog(Dialog parent, boolean modal, String title, int iconId, Icon icon, String content,  int buttonId) {
		super(parent, modal);
		// 初始化窗口
		initDialog(title, iconId, icon, content, buttonId);
		// 范围
		setDefaultBounds(this);
	}
	
	/**
	 * 定义范围
	 * @return
	 */
	private Rectangle getBound() {
		// 系统中取出参数
		Object e = UITools.getProperity(BOUND);
		if (e != null && e.getClass() == Rectangle.class) {
			return (Rectangle) e;
		}
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 398;
		int height = 168;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * 设置按纽参数
	 * @param cmd
	 * @param uikey
	 * @param mnemonic
	 */
	private void setButton(JButton cmd, String uikey, char mnemonic) {
		String text = UIManager.getString(uikey);
		if (text != null) {
			// 返回系统可以正确显示的字体
			Font font = FontKit.findFont(cmd, text);
			// 字体不匹配时，选择返回的字体
			if (font != null && !font.equals(cmd.getFont())) {
				cmd.setFont(font);
			}
			// 设置显示文本
			cmd.setText(text);
		}
		cmd.setMnemonic(mnemonic);
		cmd.addActionListener(this);
	}
	
	/**
	 * 生成按纽面板
	 * @param cmds
	 * @return
	 */
	private JPanel createButtonPanel(JButton[] cmds) {
		JPanel east = new JPanel();
		east.setLayout(new GridLayout(1, cmds.length, 6, 0));
		for (int i = 0; i < cmds.length; i++) {
			east.add(cmds[i]);
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.add(new JLabel());
		panel.add(east);
		panel.add(new JLabel());
		return panel;
	}
	
	/**
	 * 建立按纽
	 * @param who
	 * @return
	 */
	private JPanel createButtons(int who) {
		if(who == JOptionPane.YES_NO_CANCEL_OPTION) {
			setButton(cmdYes, "OptionPane.yesButtonText", 'Y');
			setButton(cmdNo, "OptionPane.noButtonText", 'N');
			setButton(cmdCancel, "OptionPane.cancelButtonText", 'C');
			
//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdNo);
//			sub.add(cmdCancel);
			
			return createButtonPanel(new JButton[] { cmdYes, cmdNo, cmdCancel });
		} else if (who == JOptionPane.YES_NO_OPTION) {
			setButton(cmdYes, "OptionPane.yesButtonText", 'Y');
			setButton(cmdNo, "OptionPane.noButtonText", 'N');

//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdNo);
			
			return createButtonPanel(new JButton[] { cmdYes, cmdNo });
		} else if(who == JOptionPane.OK_CANCEL_OPTION) {
			setButton(cmdYes, "OptionPane.yesButtonText", 'Y');
			setButton(cmdCancel, "OptionPane.cancelButtonText", 'C');
			
//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdCancel);
			
			return createButtonPanel(new JButton[] { cmdYes, cmdCancel });
		} else if(who == JOptionPane.DEFAULT_OPTION) {
			setButton(cmdYes, "OptionPane.okButtonText", 'O');
			
//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 1, 6, 0));
//			sub.add(cmdYes);
			
			return createButtonPanel(new JButton[] { cmdYes });
		} else {
			setButton(cmdYes, "OptionPane.okButtonText", 'O');
			return createButtonPanel(new JButton[] { cmdYes });
		}
	}
	
	/**
	 * 构造中心面板
	 * @param iconId
	 * @param content
	 * @return
	 */
	private JPanel createCenter(int iconId, Icon icon, String content) {
		// 显示字体
		Font font = FontKit.findFont(lblContent, content);
		if (font != null && !font.equals(lblContent.getFont())) {
			lblContent.setFont(font);
		}
		
		// 如果图标是空值，选择系统中的图标
		if (icon == null) {
			if (iconId == JOptionPane.WARNING_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.warningIcon");
			} else if (iconId == JOptionPane.ERROR_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.errorIcon");
			} else if (iconId == JOptionPane.QUESTION_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.questionIcon");
			} else {
				icon = UIManager.getIcon("OptionPane.informationIcon");
			}
		}

		// 设置图标
		if (icon != null) {
			lblImage.setIcon(icon);
		}
		
		// 面板内容
		content = String.format("<html><body>%s</body></html>", content);
		FontKit.setLabelText(lblContent, content);
		
		// 面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(12, 1));
		panel.add(BorderLayout.WEST, lblImage);
		panel.add(BorderLayout.CENTER, lblContent);
		
		return panel;
	}
	
	/**
	 * 初始化基本参数
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 */
	private void initDialog(String title, int iconId, Icon icon, String content,  int buttonId) {
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// 生成按纽实例
		createDefaultButtons();
		
		JPanel center = createCenter(iconId, icon, content);
		JPanel bottom = createButtons(buttonId);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.CENTER, center);
		panel.add(BorderLayout.SOUTH, bottom);
		
		// 边框
		if (MessageDialog.isUndressing()) {
//			Border outside = new EtchBorder(true);
			HighlightBorder outside = new HighlightBorder(1);
			Border inside = new EmptyBorder(8, 16, 6, 12);
			panel.setBorder(new CompoundBorder(outside, inside));
			
			// 在无标题栏时，设置鼠标事件...
			panel.addMouseListener(mouseListener);
			panel.addMouseMotionListener(mouseListener);
		} else {
			panel.setBorder(new EmptyBorder(8, 16, 6, 12));
		}

		// 主面板
		Container canvas = getContentPane();
		canvas.setLayout(new BorderLayout(0, 0));
		canvas.add(panel, BorderLayout.CENTER);

		// 位置
		Rectangle rect = getBound();
		setBounds(rect);
		setMinimumSize(new Dimension(300, 150));
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
			click(event);
		}
	}
	
	/**
	 * 点击处理
	 * @param event
	 */
	private void click(ActionEvent event) {
		if (event.getSource() == cmdYes) {
			select = JOptionPane.YES_OPTION;
			closeDialog();
		} else if (event.getSource() == cmdNo) {
			select = JOptionPane.NO_OPTION;
			closeDialog();
		} else if(event.getSource() == cmdCancel) {
			select = JOptionPane.CANCEL_OPTION;
			closeDialog();
		}
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
	 * 隐藏窗口。销毁窗口的工作在外面执行。
	 */
	private void closeDialog() {
		saveBound();
		setVisible(false);
	}
	
	/**
	 * 返回值
	 * @return
	 */
	public int getSelectValue() {
		return select;
	}
	
	/**
	 * 显示消息窗口
	 * @param parent
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showFrameMessageBox(Frame parent, Image titleIcon, String title, int iconId, Icon icon, String content, int buttonId) {
		// 构造字体窗口
		MessageDialog dialog = new MessageDialog(parent, true, title, iconId, icon, content, buttonId);
		if (titleIcon != null) {
			dialog.setIconImage(titleIcon);
		}
		
		// 无边框时
		if (MessageDialog.isUndressing()) {
			try {
				dialog.setUndecorated(true);
			} catch (IllegalComponentStateException e) {

			}
		}
		
		// 显示窗口
		dialog.setVisible(true);
		// 选择新字体，也可能是空指针
		int select = dialog.getSelectValue();
		// 关闭窗口
		dialog.dispose();
		// 返回结果
		return select;
	}

	/**
	 * 显示消息窗口
	 * @param parent
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showFrameMessageBox(Frame parent, Image titleIcon, String title, int iconId, String content, int buttonId) {
		return MessageDialog.showFrameMessageBox(parent, titleIcon, title, iconId, null, content, buttonId);
	}
	
	/**
	 * 设置相对于父窗口的显示位置
	 * @param owner 父窗口
	 * @param dialog 消息对话框
	 */
	private void setDefaultBounds(MessageDialog dialog) {
		int width = dialog.getWidth();
		int height = dialog.getHeight();
		Window owner = dialog.getOwner();
		Rectangle frm = owner.getBounds();
		
		// 计算相对于父窗口的X/Y间隔值
		int gapx = (width < frm.width ? (frm.width - width) / 2 : -((width - frm.width) / 2));
		int gapy = (height < frm.height ? (frm.height - height) / 2 : -((height - frm.height) / 2));
		int x = frm.x + gapx;
		int y = frm.y + gapy;

		// 最小是0
		if (x < 0) x = 0;
		if (y < 0) y = 0;

		// 超过显示范围时...
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if (x + width > dim.width) {
			x = dim.width - width;
		}
		if (y + height > dim.height) {
			y = dim.height - height;
		}
		
		dialog.setBounds(new Rectangle(x, y, width, height));
	}

	/**
	 * 显示消息窗口
	 * @param parent
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showDialogMessageBox(Dialog parent, Image titleIcon, String title, int iconId, Icon icon, String content, int buttonId) {
		// 构造字体窗口
		MessageDialog dialog = new MessageDialog(parent, true, title, iconId, icon, content, buttonId);
		if (titleIcon != null) {
			dialog.setIconImage(titleIcon);
		}

		// 无边框时
		if (MessageDialog.isUndressing()) {
			try {
				dialog.setUndecorated(true);
			} catch (IllegalComponentStateException e) {

			}
		}
		
		// 显示窗口
		dialog.setVisible(true);
		// 选择新字体，也可能是空指针
		int select = dialog.getSelectValue();
		// 关闭窗口
		dialog.dispose();
		// 返回结果
		return select;
	}
	

	/**
	 * 显示消息窗口
	 * @param parent
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showDialogMessageBox(Dialog parent, Image titleIcon, String title, int iconId, String content, int buttonId) {
		return MessageDialog.showDialogMessageBox(parent, titleIcon, title, iconId, null, content, buttonId);
	}
	
	/**
	 * 返回根组件
	 * @param component
	 * @return
	 */
	private static Component getRoot(Component component) {
		if (Laxkit.isClassFrom(component, Frame.class)) {
			return component;
		} else if (Laxkit.isClassFrom(component, Dialog.class)) {
			return component;
		} else {
			Container container = component.getParent();
			if (container == null) {
				return component;
			}
			return getRoot(container);
		}
	}
	
	/**
	 * 显示对话框
	 * @param source 来源组件
	 * @param titleIcon 标题栏图标，可选项
	 * @param title 标题
	 * @param iconId 内容侧图标编号，如果它有效，内容图标被忽略。
	 * 	参数分别是：-1、JOptionPane.WARNING_MESSAGE、JOptionPane.ERROR_MESSAGE、JOptionPane.QUESTION_MESSAGE
	 * @param icon 内容图标
	 * @param content 显示的内容
	 * @param buttonId 按纽ID
	 * @return 返回整数值
	 */
	public static int showMessageBox(Component source, Image titleIcon, String title, int iconId, Icon icon, String content, int buttonId) {
		Component parent = getRoot(source);
		if (parent != null) {
			if (Laxkit.isClassFrom(parent, Frame.class)) {
				Frame frame = (Frame) parent;
				return showFrameMessageBox(frame, titleIcon, title, iconId, icon, content, buttonId);
			} else if (Laxkit.isClassFrom(parent, Dialog.class)) {
				Dialog dialog = (Dialog) parent;
				return showDialogMessageBox(dialog, titleIcon, title, iconId, icon, content, buttonId);
			}
		}

		return -1;
	}
	
	/**
	 * 
	 * @param source
	 * @param title
	 * @param iconId
	 * @param icon
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showMessageBox(Component source, String title,
			int iconId, Icon icon, String content, int buttonId) {
		return MessageDialog.showMessageBox(source, null, title, iconId, icon,
				content, buttonId);
	}
	
	/**
	 * 显示对话框
	 * @param source
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 * @return
	 */
	public static int showMessageBox(Component source, String title,
			int iconId, String content, int buttonId) {
		return MessageDialog.showMessageBox(source, null, title, iconId, null,
				content, buttonId);
	}

}