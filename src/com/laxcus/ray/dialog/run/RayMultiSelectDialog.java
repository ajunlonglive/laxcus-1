/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.run;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 多选择对话框
 * 
 * @author scott.liang
 * @version 1.0 8/13/2021
 * @since laxcus 1.0
 */
class RayMultiSelectDialog extends LightDialog implements ActionListener, ListSelectionListener {
	
	private static final long serialVersionUID = 2009792215490298529L;

	/** 数组 **/
	private ArrayList<MultiSelectCommand> array = new ArrayList<MultiSelectCommand>();

	/** 单元渲染器 **/
	private MultiSelectCellRenderer renderer;
	private DefaultListModel model = new DefaultListModel();
	private JList list = new JList();
	
	/** 确定/选择按纽 **/
	private JButton cmdSelect;
	private JButton cmdCancel;

	/**
	 * 构造多选择对话框，指定数组
	 * @param collect
	 */
	public RayMultiSelectDialog(Collection<MultiSelectCommand> collect) {
		super();
		array.addAll(collect);
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
		ClickThread(ActionEvent e){
			super();
			event = e;
		}
		public void process() {
			click(event);
		}
	}
	
	void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdSelect) {
			select();
		} else if (source == cmdCancel) {
			saveBound();
			setSelectedValue(null);
		}
	}
	
	private void select() {
		Object value = list.getSelectedValue();
		if (value != null) {
			MultiSelectCommand ms = (MultiSelectCommand) value;
			// 保存..
			saveBound();
			// 选择
			setSelectedValue(ms.getKey());
		}
	}
	
	/**
	 * 增加单元
	 */
	private void addElements() {
		for (MultiSelectCommand element : array) {
			model.addElement(element);
		}
	}
	
	private JComponent createCenter() {
		list.setModel(model);
		list.setCellRenderer(renderer = new MultiSelectCellRenderer());
		list.addListSelectionListener(this);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		String value = UIManager.getString("RunDialog.MultiSelect.ListCellHeight");
		int height = ConfigParser.splitInteger(value, 50);
		list.setFixedCellHeight(height);
		list.setBorder(new EmptyBorder(2,2,2,2));
		
		// 显示...
		addElements();

		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 滚动
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		panel.setBorder(new HighlightBorder(1));

		return panel;
	}

	private JButton createButton(String text, char w) {
		JButton but = new JButton();
		FontKit.setButtonText(but, text);

		but.setIconTextGap(4);
		but.addActionListener(this);
		but.setMnemonic(w);

		return but;
	}
	
	/**
	 * 初始化对话框
	 */
	private void initDialog(Component parent) {
		// 找到父窗口
		JInternalFrame frame = findInternalFrameForComponent(parent);
		if (frame != null) {
			setFrameIcon(frame.getFrameIcon());
		}
		
		setTitle(UIManager.getString("RunDialog.MultiSelect.Title"));

		cmdSelect = createButton(UIManager.getString("RunDialog.MultiSelect.selectButtonText"), 'S'); 
		cmdCancel = createButton(UIManager.getString("RunDialog.MultiSelect.cancelButtonText"), 'C'); 

		JPanel n = new JPanel();
		n.setLayout(new GridLayout(1, 2, 6, 0));
		n.add(cmdSelect); n.add(cmdCancel);
		JPanel w = new JPanel();
		w.setLayout(new BorderLayout());
		w.add(n, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(createCenter(), BorderLayout.CENTER);
		panel.add(w, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		// 设置面板
		setContentPane(panel);
	}
	
	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/MultiSelect/Bound", rect);
		}
	}
	
	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/MultiSelect/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 308;
		int height = 388;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * 设置范围
	 * @param parent
	 */
	private void setBounds(Component parent) {
		// 读取对话框范围
		Rectangle dlg = readBounds();
		// 找到父窗口
		JInternalFrame frame = findInternalFrameForComponent(parent);
		if (frame == null) {
			setBounds(dlg);
			return;
		}
		
		// 计算空间位置
		Rectangle frm = frame.getBounds();
		int gapx = (dlg.width < frm.width ? ( frm.width - dlg.width )/2 : 0);
		int gapy = (dlg.height < frm.height ? (frm.height - dlg.height)/2 : 0);
		int x = frm.x + gapx;
		int y = frm.y + gapy;
		
		// 超过显示范围时...
		Dimension dim = PlatformKit.getPlatformDesktop().getSize(); 
		if (x + dlg.width > dim.width) {
			x = dim.width - dlg.width;
		}
		if (y + dlg.height > dim.height) {
			y = dim.height - dlg.height;
		}
		
		// 设置显示范围
		Rectangle rect = new Rectangle(x, y, dlg.width, dlg.height);
		setBounds(rect);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 显示对话框
		initDialog(parent);

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		setBounds(parent);

		if (modal) {
			return super.showModalDialog(parent);
		} else {
			return super.showNormalDialog(parent);
		}
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

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新UI界面
		if (renderer != null) {
			renderer.updateUI();
		}

		// 更新字体，注意！不要更新UI，否则在updateUI方法里会形成死循环
		FontKit.updateDefaultFonts(this, false);
	}

}