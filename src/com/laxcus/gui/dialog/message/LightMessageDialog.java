/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.message;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.display.*;

/**
 * 消息对话框
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public class LightMessageDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -1010127688051731004L;

	//	private final static String BOUND = LightMessageDialog.class.getSimpleName() + "_BOUND";

	/** 图标 **/
	private JLabel lblImage = new JLabel("", SwingConstants.CENTER);

	/** 文本 **/
	private JLabel lblContent = new JLabel("", SwingConstants.LEFT);

	/** YES按纽 **/
	private JButton cmdYes = new JButton("Yes");

	/** NO按纽 **/
	private JButton cmdNo = new JButton("No");

	/** 取消按纽 **/
	private JButton cmdCancel = new JButton("Cancel");
	
//	/** YES按纽 **/
//	private FlatButton cmdYes = new FlatButton("Yes");
//
//	/** NO按纽 **/
//	private FlatButton cmdNo = new FlatButton("No");
//
//	/** 取消按纽 **/
//	private FlatButton cmdCancel = new FlatButton("Cancel");

	/** 返回值 **/
	private int select;

	/**
	 * 构造默认的消息对话框
	 */
	public LightMessageDialog() {
		super();
	}

	/**
	 * 构造消息对话框，指定标题
	 * @param title 标题
	 */
	public LightMessageDialog(String title) {
		super(title);
	}

	/**
	 * 构造消息对话框，指定参数
	 * @param title
	 * @param resizable
	 */
	public LightMessageDialog(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造消息对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public LightMessageDialog(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * 构造消息对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public LightMessageDialog(String title, boolean resizable, boolean closable, boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * 构造消息对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public LightMessageDialog(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}

	/*
	 * 如果是模态，赋值后，让上级来释放。非模态，关闭窗口，释放资源。
	 * 
	 * @see com.laxcus.ui.LightForm#closeWindow()
	 */
	@Override
	public void closeWindow() {
		if (isModal()) {
			setSelectedValue(null);
		} else {
			setVisible(false);
			dispose();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 保存范围
	 */
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "MessageDialog/Bound", rect);
		}
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "MessageDialog/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension size = PlatformKit.getPlatformDesktop().getSize(); // Toolkit.getDefaultToolkit().getScreenSize();

		// 四分之一
		int width = 428; 
		int height = 192; 

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
//			// 返回系统可以正确显示的字体
//			Font font = FontKit.findFont(cmd, text);
//			// 字体不匹配时，选择返回的字体
//			if (font != null && !font.equals(cmd.getFont())) {
//				cmd.setFont(font);
//			}
//			// 设置显示文本
//			cmd.setText(text);
			
			// 设置字体
			FontKit.setButtonText(cmd, text);
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
		if(who == DialogOption.YES_NO_CANCEL_OPTION) {
			setButton(cmdYes, "MessageDialog.yesButtonText", 'Y');
			setButton(cmdNo, "MessageDialog.noButtonText", 'N');
			setButton(cmdCancel, "MessageDialog.cancelButtonText", 'C');

//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdNo);
//			sub.add(cmdCancel);

			return createButtonPanel(new JButton[] { cmdYes, cmdNo, cmdCancel });
		} else if (who == DialogOption.YES_NO_OPTION) {
			setButton(cmdYes, "MessageDialog.yesButtonText", 'Y');
			setButton(cmdNo, "MessageDialog.noButtonText", 'N');

//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdNo);

			return createButtonPanel(new JButton[] { cmdYes, cmdNo });
		} else if(who == DialogOption.OK_CANCEL_OPTION) {
			setButton(cmdYes, "MessageDialog.yesButtonText", 'Y');
			setButton(cmdCancel, "MessageDialog.cancelButtonText", 'C');

//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 2, 6, 0));
//			sub.add(cmdYes);
//			sub.add(cmdCancel);

			return createButtonPanel(new JButton[] { cmdYes, cmdCancel });
		} else if(who == DialogOption.DEFAULT_OPTION) {
			setButton(cmdYes, "MessageDialog.okayButtonText", 'O');

//			JPanel sub = new JPanel();
//			sub.setLayout(new GridLayout(1, 1, 6, 0));
//			sub.add(cmdYes);

			return createButtonPanel(new JButton[] { cmdYes });
		} else {
			setButton(cmdYes, "MessageDialog.okayButtonText", 'O');
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
//		// 显示字体
//		Font font = FontKit.findFont(lblContent, content);
//		if (font != null && !font.equals(lblContent.getFont())) {
//			lblContent.setFont(font);
//		}

		// 如果图标是空值，选择系统中的图标
		if (icon == null) {
			if (iconId == DialogOption.WARNING_MESSAGE) {
				icon = UIManager.getIcon("MessageDialog.warningIcon");
			} else if (iconId == DialogOption.ERROR_MESSAGE) {
				icon = UIManager.getIcon("MessageDialog.errorIcon");
			} else if (iconId == DialogOption.QUESTION_MESSAGE) {
				icon = UIManager.getIcon("MessageDialog.questionIcon");
			} else {
				icon = UIManager.getIcon("MessageDialog.informationIcon");
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
		// panel.add(BorderLayout.WEST, lblImage);
		// panel.add(BorderLayout.CENTER, lblContent);
		panel.add(lblImage, BorderLayout.WEST);
		panel.add(lblContent, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 初始化基本参数
	 * @param title
	 * @param iconId
	 * @param content
	 * @param buttonId
	 */
	public void initDialog(String title, Icon titleIcon, int iconId, Icon icon, String content, int buttonId) {
		// 标题栏
		setTitle(title);
		// 标题图标
		if (titleIcon != null) {
			setFrameIcon(titleIcon);
		} else {
			setFrameIcon(PlatformKit.getPlatformIcon());
		}

		JPanel center = createCenter(iconId, icon, content);
		JPanel bottom = createButtons(buttonId);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(center, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);

//		panel.add(BorderLayout.CENTER, center);
//		panel.add(BorderLayout.SOUTH, bottom);

		// 边框
		panel.setBorder(new EmptyBorder(8, 16, 6, 16));

		//		// 主面板
		//		Container canvas = getContentPane();
		//		canvas.setLayout(new BorderLayout(0, 0));
		//		canvas.add(panel, BorderLayout.CENTER);

		// 主面板
		setContentPane(panel);

		setMinimumSize(new Dimension(300, 150));

		//		// 位置
		//		Rectangle rect = getBound();
		//		setBounds(rect);
	}

	/**
	 * 点击处理
	 * @param event
	 */
	private void click(ActionEvent event) {
		if (event.getSource() == cmdYes) {
			writeBounds();
			setSelectedValue(DialogOption.YES_OPTION);
		} else if (event.getSource() == cmdNo) {
			writeBounds();
			setSelectedValue(DialogOption.NO_OPTION);
		} else if (event.getSource() == cmdCancel) {
			writeBounds();
			setSelectedValue(DialogOption.CANCEL_OPTION);
		}
	}

	/**
	 * 返回值
	 * @return
	 */
	public int getSelectValue() {
		return select;
	}
	
	/**
	 * 设置范围
	 * @param parent
	 */
	private void setBounds(Component parent) {
		// 读取对话框范围
		Rectangle dlg = readBounds();
		super.setDefaultBounds(dlg, parent);
		
//		// 找到父窗口
//		JInternalFrame frame = findInternalFrameForComponent(parent);
//		if (frame == null) {
//			setBounds(dlg);
//			return;
//		}
//		
//		// 计算空间位置
//		Rectangle frm = frame.getBounds();
//		int gapx = (dlg.width < frm.width ? ( frm.width - dlg.width )/2 : 0);
//		int gapy = (dlg.height < frm.height ? (frm.height - dlg.height)/2 : 0);
//		int x = frm.x + gapx;
//		int y = frm.y + gapy;
//		
//		// 超过显示范围时...
//		Dimension dim = PlatfromKit.getDesktopPane().getSize();
//		if (x + dlg.width > dim.width) {
//			x = dim.width - dlg.width;
//		}
//		if (y + dlg.height > dim.height) {
//			y = dim.height - dlg.height;
//		}
//
//		// 设置显示范围
//		Rectangle rect = new Rectangle(x, y, dlg.width, dlg.height);
//		setBounds(rect);
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

		// 设置对话框显示位置
		setBounds(parent);

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		//		// METAL界面，去掉边框
		//		if (isMetalUI()) {
		//			setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
		//
		//			//			InternalFrameUI ui = getUI();
		//			//			if (ui.getClass() == BasicInternalFrameUI.class) {
		//			//				BasicInternalFrameUI bui = (BasicInternalFrameUI) ui;
		//			//				JComponent north = bui.getNorthPane();
		//			//				if (north != null) {
		//			//					north.setMinimumSize(new Dimension(20, 38));
		//			//				}
		//			//			}
		//
		//		}

		//		// 清除标题栏
		//		javax.swing.plaf.InternalFrameUI ui = getUI();
		//		if (ui.getClass() == javax.swing.plaf.basic.BasicInternalFrameUI.class) {
		//			((javax.swing.plaf.basic.BasicInternalFrameUI) ui).setNorthPane(null);
		//		}

		//		// 清除标题栏
		//		javax.swing.plaf.basic.BasicInternalFrameUI ui = (javax.swing.plaf.basic.BasicInternalFrameUI)getUI();
		//		ui.setNorthPane(null);

		//		// 边框
		//		if (Skins.isMetal()) {
		//			setBorder(new EmptyBorder(0, 0, 4, 0));
		//		}

		// 显示窗口
		return showModalDialog(parent);

		//		if (modal) {
		//			return showModalDialog(parent);
		//		} else {
		//			return this.showNormalDialog(parent);
		//		}
	}

}