/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.uninstall;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.application.factory.*;
import com.laxcus.application.manage.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 删除软件对话框
 * 
 * @author scott.liang
 * @version 1.0 7/31/2021
 * @since laxcus 1.0
 */
public class RayUninstallDialog extends LightDialog implements ActionListener, ListSelectionListener {
	
	private static final long serialVersionUID = -6347255420484398487L;
	
	/** 删除按纽 **/
	private FlatButton cmdDelete;
	
	/** 退出按纽 **/
	private FlatButton cmdExit;
	
	/** 删除单元渲染器 **/
	private UninstallCellRenderer renderer;
	private DefaultListModel model = new DefaultListModel();
	private JList list = new JList();

	private UninstallFactory factory;
	
	/**
	 * 构造默认的删除软件对话框
	 */
	public RayUninstallDialog(UninstallFactory e) {
		super();
		factory = e;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
//		addThread(new ClickThread(e));
		click(event);
	}
	
//	public class ClickThread extends SwingEvent {
//		ActionEvent event;
//		ClickThread(ActionEvent e){
//			super();
//			event = e;
//		}
//		public void process() {
//			click(event);
//		}
//	}
	
	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdExit) {
			exit();
		} else if(source == cmdDelete){
			delete();
		}
	}
	
	private void exit() {
		String content = UIManager.getString("UninstallDialog.exitMessageText");
		boolean success = MessageBox.showYesNoDialog(this, getTitle(), content);
		if (!success) {
			return;
		}

		// 保存范围
		saveBounds();

		// 关闭窗口
		super.closeWindow();

		//		if (isModal()) {
		//			setSelectedValue(null);
		//		} else {
		//			setVisible(false);
		//			dispose();
		//		}
	}
	
	/**
	 * 删除文件
	 */
	private void delete() {
		int index = list.getSelectedIndex();
		if (index < 0) {
			return;
		}

		Object object = model.elementAt(index);
		if (object.getClass() != WRoot.class) {
			return;
		}

		WRoot root = (WRoot) object;
		WElement element = root.getElement();
		String title = UIManager.getString("UninstallDialog.deleteApplicationTitle"); // "删除应用软件";
		String content = UIManager.getString("UninstallDialog.queryDeleteApplicationText");
		content = String.format(content, element.getTitle());
		boolean yes = MessageBox.showYesNoDialog(this, title, content);
		if (!yes) {
			return;
		}

		// 删除软件
		boolean success = factory.deleteApplication(root);
		
		// 删除记录
		if (success) {
			model.remove(index);
		}
		
		if (success) {
			content = UIManager.getString("UninstallDialog.successDeleteApplicationText");
			content = String.format(content, element.getTitle());
			MessageBox.showInformation(this, title, content);
		} else {
			content = UIManager.getString("UninstallDialog.failedDeleteApplicationText");
			content = String.format(content, element.getTitle());
			MessageBox.showFault(this, title, content);
		}
	}

	private void saveBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "UninstallDialog/Bound", rect);
	}

	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"UninstallDialog/Bound");
		if (bounds == null) {
			int w = 386;
			int h = 488;

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (d.width - w) / 2;
			int y = (d.height - h) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
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
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新UI
		if (renderer != null) {
			renderer.updateUI();
		}

		// 注意，不要更新UI，否则在updateUI里会死循环!
		FontKit.updateDefaultFonts(this, false);
	}
	
	private FlatButton createButton(String text, char w) {
		FlatButton but = new FlatButton();
		FontKit.setButtonText(but, text);

		but.setIconTextGap(4);
		but.addActionListener(this);
		but.setMnemonic(w);

		return but;
	}
	
	private void addList() {
		List<WRoot> list = RTManager.getInstance().list();

		for (WRoot root : list) {
			if (root.isSystem()) {
				continue;
			}
			// 单元...
			model.addElement(root);
		}
	}
	
	
	private JComponent createCenter() {
		list.setModel(model);
		list.setCellRenderer(renderer = new UninstallCellRenderer());
		list.addListSelectionListener(this);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		list.setVisibleRowCount(6); // 6行
		String value = UIManager.getString("UninstallDialog.ListCellHeight");
		int height = ConfigParser.splitInteger(value, 50);
		list.setFixedCellHeight(height);
		list.setBorder(new EmptyBorder(1, 1, 1, 1));

		// 显示...
		addList();

		JScrollPane jsp = new JScrollPane(list);
		jsp.setBorder(new HighlightBorder(1));
//		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 滚动
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jsp, BorderLayout.CENTER);
		return panel;

		//		JPanel panel = new JPanel();
		//		return panel;
	}

	private JPanel createSouth() {
		
		cmdDelete = createButton(UIManager.getString("UninstallDialog.deleteButtonText"), 'd');
		
		cmdExit = createButton(UIManager.getString("UninstallDialog.exitButtonText"), 'X');
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 4));
		panel.add(cmdDelete, BorderLayout.WEST);
		panel.add(cmdExit, BorderLayout.EAST);
		return panel;
	}
	
	class ChangeThread extends SwingEvent {
		ListSelectionEvent event;

		ChangeThread(ListSelectionEvent e) {
			super();
			event = e;
		}

		public void process() {
			// exchange(event);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) {
			addThread(new ChangeThread(event));
		}
	}

	/**
	 * 初始化窗口
	 */
	private void initDialog() {
		setTitle(UIManager.getString("UninstallDialog.Title"));
		setFrameIcon(UIManager.getIcon("UninstallDialog.TitleIcon"));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(createCenter(), BorderLayout.CENTER);
		panel.add(createSouth(), BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(4, 6, 6, 6));

		setContentPane(panel);
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 初始化窗口
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		setBounds();

		if (modal) {
			return super.showModalDialog(parent);
		} else {
			return super.showNormalDialog(parent);
		}
	}
	
	
}