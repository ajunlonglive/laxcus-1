/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.input;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.display.*;

/**
 * 输入对话框
 * 
 * @author scott.liang
 * @version 1.0 9/8/2021
 * @since laxcus 1.0
 */
public abstract class InputDialog extends LightDialog implements ActionListener {
	
	private static final long serialVersionUID = 6341987361102241956L;
	
//	public class InputField extends JTextField {
//		
//		private static final int HEIGHT = 28;
//		
//		public InputField(){
//			super();
//		}
//		
//		public Dimension getMaximumSize() {
//			Dimension d = super.getMaximumSize();
//			d.height = InputField.HEIGHT;
//			return d;
//		}
//		
//		public Dimension getMinimumSize() {
//			Dimension d = super.getMinimumSize();
//			d.height = InputField.HEIGHT;
//			return d;
//		}
//		
//		public Dimension getPreferredSize() {
//			Dimension d = super.getPreferredSize();
//			d.height = InputField.HEIGHT;
//			return d;
//		}
//	}

	/** 左侧的标题文本 **/
	private String approveText;
	
	/** 初始化时的输入文本 **/
	private String initInputText;

	/** 文本框 **/
	private FlatTextField field = new FlatTextField();

	/** 按纽 **/
	private FlatButton cmdOkay;

	private FlatButton cmdCancel;
	
	/**
	 * 初始化对话框标题
	 */
	private void initTitle() {
		String title = UIManager.getString("InputDialog.Title");
		if (title != null) {
			setTitle(title);
		}
		Icon icon = UIManager.getIcon("InputDialog.TitleIcon");
		if (icon != null) {
			setFrameIcon(icon);
		}
	}

	/**
	 * 构造默认的输入对话框
	 */
	public InputDialog() {
		super();
		initTitle();
	}

	/**
	 * @param title
	 */
	public InputDialog(String title) {
		this();
		setTitle(title);
	}

	/**
	 * @param title
	 * @param resizable
	 */
	public InputDialog(String title, boolean resizable) {
		this(title);
		setResizable(resizable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public InputDialog(String title, boolean resizable, boolean closable) {
		this(title, resizable);
		setClosable(closable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public InputDialog(String title, boolean resizable, boolean closable,
			boolean maximizable) {
		this(title, resizable, closable);
		setMaximizable(maximizable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public InputDialog(String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		this(title, resizable, closable, maximizable);
		setIconifiable(iconifiable);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}
	
	/**
	 * 单点事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdOkay) {
			String text = field.getText();
			boolean success = confirm(text);
			if (success) {
				writeBounds();
				setSelectedValue(text);
			}
		} else if (source == cmdCancel) {
			writeBounds();
			setSelectedValue(null);
		}
	}
	
	public void setInitInputText(String s) {
		initInputText = s;
	}
	public String getInitInputText(){
		return this.initInputText;
	}
	
	/**
	 * 设置提示文本
	 * @param text
	 */
	public void setApproveText(String text) {
		approveText = text;
	}

	/**
	 * 返回提示文本
	 * @return
	 */
	public String getApproveText() {
		return approveText;
	}

	/**
	 * 窗口范围写入运行环境
	 * 可以让子类来覆盖
	 */
	protected void writeBounds() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "InputDialog/Bound", rect);
		}
	}

	/**
	 * 读窗口范围
	 * 允许子类来覆盖
	 * @return
	 */
	protected Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "InputDialog/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension dim = PlatformKit.getPlatformDesktop().getSize(); 
		
		int w = 438;
		int h = 168;

		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		return new Rectangle(x, y, w, h);
	}
	
	private void setBounds(Component parent) {
		// 读取对话框范围
		Rectangle dlg = readBounds();
		setDefaultBounds(dlg, parent);
	}
	
	private FlatButton createButton(String key, char w) {
		String text = UIManager.getString(key);
		FlatButton but = new FlatButton(text);
		but.setMnemonic(w);
		but.addActionListener(this);
		return but;
	}
	
//	/**
//	 * 生成输入面板
//	 * @return
//	 */
//	private JPanel createInputPanel() {
//		JLabel open = new JLabel();
//		// 设置文本
//		if (approveText != null) {
//			FontKit.setLabelText(open, approveText);
//		} else {
//			FontKit.setLabelText(open, UIManager.getString("InputDialog.InputLabelText"));
//		}
//		field.setPreferredSize(new Dimension(120, 32));
//
//		// 子面板
//		JPanel x = new JPanel();
//		x.setLayout(new BorderLayout(6, 0));
//		x.add(open, BorderLayout.WEST);
//		x.add(field, BorderLayout.CENTER);
//		
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout());
//		panel.add(x, BorderLayout.CENTER);
//		return panel;
//	}
	
	/**
	 * 生成输入面板
	 * @return
	 */
	private JPanel createInputPanel() {
		JLabel open = new JLabel();
		// 设置文本
		if (approveText != null) {
			FontKit.setLabelText(open, approveText);
		} else {
			FontKit.setLabelText(open, UIManager.getString("InputDialog.InputLabelText"));
		}
		field.setPreferredSize(new Dimension(120, 32));
		
		// 初始化的输入文本
		if (initInputText != null) {
			field.setText(initInputText);
		}

		// 子面板
		JPanel x = new JPanel();
		x.setLayout(new BorderLayout(6, 0));
		x.add(open, BorderLayout.WEST);
		x.add(field, BorderLayout.CENTER);
		
		JPanel x2 = new JPanel();
		x2.setLayout(new BorderLayout());
		x2.add(x, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(), BorderLayout.NORTH);
		panel.add(x2, BorderLayout.CENTER);
		panel.add(new JLabel(), BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * 生成按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		cmdOkay = createButton("InputDialog.OkayButtonText", 'O');
		cmdCancel = createButton("InputDialog.CancelButtonText", 'C');
		// 面板
		JPanel x = new JPanel();
		x.setLayout(new GridLayout(1, 2, 6, 0));
		x.add(cmdOkay);
		x.add(cmdCancel);

		//		JPanel panel = new JPanel();
		//		panel.setLayout(new BorderLayout());
		//		panel.add(x, BorderLayout.EAST);
		//		return panel;

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.add(new JLabel());
		panel.add(x);
		panel.add(new JLabel());
		return panel;
	}
	
	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		JPanel panel = new JPanel();
		// 设置面板
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(createInputPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 4, 8));
		// 面板
		setContentPane(panel);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 必须是模态窗口
		if (!modal) {
			throw new IllegalArgumentException("must be modal!");
		}

		// 初始化窗口
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		// 范围
		setBounds(parent);

		// 显示模态窗口
		return showModalDialog(parent, field);
	}

	/**
	 * 显示对话框
	 * @param parent 父类
	 * @return 返回字符串，或者空指针
	 */
	public String showDialog(Component parent) {
		return (String) showDialog(parent, true);
	}

	/**
	 * 核对字符串
	 * @param text
	 * @return 成功返回真，否则假
	 */
	public abstract boolean confirm(String text);

}