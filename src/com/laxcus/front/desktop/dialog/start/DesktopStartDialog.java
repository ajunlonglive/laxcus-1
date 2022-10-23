/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.start;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.factory.*;
import com.laxcus.application.manage.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.skin.*;

/**
 * 应用启动窗口
 * 
 * @author scott.liang
 * @version 1.0 1/28/2022-1-28
 * @since laxcus 1.0
 */
public class DesktopStartDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -1248933295789006634L;

	/** 窗口实例 **/
	private StartKey startKey;
	
	private String name;

	/** 文件列表框 **/
	private JList list = new JList();

	/** 文件显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 平面按纽 **/
	private FlatButton cmdSelect;

	private FlatButton cmdCancel;
	
	/** 总是用这个应用 **/
	private JCheckBox boxAlway = new JCheckBox();

	/**
	 * 构造启动窗口
	 */
	public DesktopStartDialog(StartKey e, String link) {
		super();
		startKey = e;
		// 转义目录
		pickup(link);
	}
	
	/**
	 * 取出文件名
	 * @param link
	 */
	private void pickup(String link) {
		// 如果是SRL
		if (SRL.validate(link)) {
			int last = link.lastIndexOf("/");
			if (last >= 0) {
				name = link.substring(last + 1);
			} else {
				name = "";
			}
		}
		// 如果是本地文件
		else {
			File file = new File(link);
			if (file.exists() && file.isFile()) {
				name = file.getName(); // 取出文件名
			} else {
				name = "";
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cmdSelect) {
			Object element = list.getSelectedValue();
			if (element != null && element.getClass() == StartToken.class) {
				writeBounds();
				StartToken token = (StartToken)element;
				
				// 保存KEY
				if(this.boxAlway.isSelected()) {
					int last = name.lastIndexOf(".");
					String type = (last >0 ? name.substring(last+1) : null);
					if(type !=null){
					this.writeWKey(type.trim().toLowerCase(), token.getKey());
					}
				}
				
				setSelectedValue(token);
				
			}
		} else if (event.getSource() == cmdCancel) {
			writeBounds();
			setSelectedValue(null);
		}
	}

	/**
	 * 写入WKey
	 * @param type
	 * @param key
	 */
	public void writeWKey(String type, WKey key) {
		String path = String.format("StartTypes/%s", type); 
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, path, key.toString());
	}

	/**
	 * 生成一个按纽
	 * @param icon
	 * @param text
	 * @return
	 */
	private FlatButton createButton(String textKey, char mword) {
		String text = UIManager.getString(textKey);
		FlatButton but = new FlatButton(text);
		but.setMnemonic(mword);
		but.addActionListener(this);
		return but;

		//		FontKit.setButtonText(but, text);
		//		FontKit.setToolTipText(but, title);
		//		
		//		but.setIcon(icon, 30); // 支持高亮
		////		but.setToolTipText(text);
		//		but.setBorder(new EmptyBorder(8,8,8,8));
		//		
		////		Dimension size = new Dimension(88, 88);
		//		Dimension size = new Dimension(78, 78);
		//		but.setSize(size);
		//		
		////		but.setToolTipText(title);
		//		
		//		but.setPreferredSize(size);
		//		but.setMinimumSize(size);
		//		but.setMaximumSize(size);
		//		
		//		but.setIconTextGap(4);
		//		
		//		but.addActionListener(this);
		//				
		//		// 设置组件字体
		//		setComponentFont(but, text);
		//		return but;
	}
	
	private JPanel createCenterPanel() {
		Icon icon = UIManager.getIcon("StartDialog.RemarkIcon");
		String text = UIManager.getString("StartDialog.RemakeText");
		// 如果定义名称
		if (name != null) {
			text = String.format("<html><body>%s<br>%s</body></html>", text, name);
		}
		JLabel remark = new JLabel(text, icon, SwingConstants.LEFT);
		remark.setIconTextGap(5);

		text = UIManager.getString("StartDialog.AlwayCheckboxText");
		boxAlway.setText(text);
		boxAlway.setSelected(false);

		// 文件列表
		list.setCellRenderer(new StartFileCellRenderer());
		list.setModel(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBorder(new EmptyBorder(1, 1, 1, 1));
		JScrollPane jsp = new JScrollPane(list);

		text = UIManager.getString("StartDialog.ListTitle");

		JPanel south = new JPanel();
		south.setLayout(new BorderLayout(0, 4));
		south.setBorder(UITools.createTitledBorder(text, 4));
		south.add(jsp, BorderLayout.CENTER);
		south.add(boxAlway, BorderLayout.SOUTH);
		
		// 加载TOKEN
		loadTokens();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(remark, BorderLayout.NORTH);
		panel.add(south, BorderLayout.CENTER);
		return panel;
	}
	
	private void loadTokens() {
		for (StartToken token : startKey.list()) {
			model.addElement(token);
		}
	}
	
	private JPanel createButtonPanel() {
		cmdSelect = createButton("StartDialog.SelectButtonText", 'S');
		cmdCancel = createButton("StartDialog.CancelButtonText", 'C');

		JPanel east = new JPanel();
		east.setLayout(new GridLayout(1, 2, 5, 0));
		east.add(cmdSelect);
		east.add(cmdCancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(east, BorderLayout.EAST);
		return panel;
	}
	
	private void initDialog() {
		setFrameIcon(UIManager.getIcon("StartDialog.LogoIcon")); // PlatfromUtilities.getPlatformIcon());
		setTitle(UIManager.getString("StartDialog.Title")); // "运行");

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		
		setContentPane(panel);
	}
	
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "StartDialog/Bound", rect);
	}

	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"StartDialog/Bound");
		if (bounds == null) {
			int w = 312;
			int h = 398;

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (d.width - w) / 2;
			int y = (d.height - h) / 2;
			bounds = new Rectangle(x, y, w, h);
		}
		return bounds;
	}
	
	/**
	 * 设置显示范围
	 * @param desktop
	 */
	private void setBounds() {
		setBounds(readBounds());
	}
	
	
//	private void initDialogX() {
//		// setFrameIcon(findImage("conf/desktop/image/window/system/screen.png"));
//
//		setFrameIcon(UIManager.getIcon("RunDialog.LogoIcon")); // PlatfromUtilities.getPlatformIcon());
//		setTitle(UIManager.getString("RunDialog.Title")); // "运行");
//
//		String text = UIManager.getString("RunDialog.contentText"); // "显示您的文档";
//		String html = String.format("<html>%s</html>", text);
//
//		JLabel label = new JLabel(); // html, SwingConstants.LEFT); // 居中显示
//		label.setIcon(UIManager.getIcon("RunDialog.contentIcon"));
//		label.setIconTextGap(6);
//		label.setHorizontalAlignment(SwingConstants.LEFT);
//		label.setVerticalAlignment(SwingConstants.CENTER);
//		FontKit.setLabelText(label, html);
//
//		cmdOkay = createButton("RunDialog.runButtonText", 'O'); // "运行");
//		cmdCancel = createButton("RunDialog.cancelButtonText", 'C'); // "取消");
//
//		renderer = new RunCommandCellRenderer();
//		field.setRenderer(renderer);
//		field.setModel(model);
//		field.setEditable(true);
//		field.setLightWeightPopupEnabled(false); // 重量级组件
//		// 最小尺寸
//		field.setMinimumSize(new Dimension(10, 32));
//		field.setPreferredSize(new Dimension(10, 32));
//		// 给输入组件加上回车键!
//		Component component = field.getEditor().getEditorComponent();
//		component.addKeyListener(new EnterAdapter());
//
////		field.getEditor().g.addActionListener(this);
//
//		// 加载单元，这些单元从内存或者其它地方取得
//		loadElement();
//
//		JLabel open = new JLabel();
//		FontKit.setLabelText(open, UIManager.getString("RunDialog.openLabelText"));
//		JPanel x = new JPanel();
//		x.setLayout(new BorderLayout(6, 0));
//		x.add(open, BorderLayout.WEST);
//		x.add(field, BorderLayout.CENTER);
//
//		JPanel j = new JPanel();
//		j.setLayout(new BorderLayout());
//		j.add(label, BorderLayout.CENTER);
//		j.add(x, BorderLayout.SOUTH);
//
//		JPanel n = new JPanel();
//		n.setLayout(new GridLayout(1, 2, 6, 0));
//		n.add(running); 
//		n.add(cancel);
//		JPanel w = new JPanel();
//		w.setLayout(new BorderLayout());
//		w.add(n, BorderLayout.EAST);
//
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(0, 10));
//		panel.add(j, BorderLayout.CENTER);
//		panel.add(w, BorderLayout.SOUTH);
//		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
//		// 设置面板
//		setContentPane(panel);
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.gui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
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
		
//		// 窗体外沿去掉边框
//		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// 边框
		setBounds();

		// 显示窗口
		return showModalDialog(parent);
	}
	
	public StartToken showDialog(Component parent) {
		return (StartToken)showDialog(parent, true);
	}

//	/**
//	 * 隐藏标题栏
//	 */
//	private void hideTitlePane() {
//		// 清除标题栏
//		InternalFrameUI ui = getUI();
//		if (Laxkit.isClassFrom(ui, BasicInternalFrameUI.class)) {
//			((BasicInternalFrameUI) ui).setNorthPane(null);
//			putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
//		}
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.gui.dialog.LightDialog#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		super.updateUI();
//		hideTitlePane();
//	}
	
}
