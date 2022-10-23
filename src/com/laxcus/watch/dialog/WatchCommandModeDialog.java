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

import com.laxcus.command.mix.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;
import com.laxcus.watch.invoker.*;
import com.laxcus.watch.pool.*;

/**
 * 命令处理模式窗口
 * 
 * @author scott.liang
 * @version 1.0 10/2/2018
 * @since laxcus 1.0
 */
public class WatchCommandModeDialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	/** 内存按纽 **/
	private JRadioButton butMemory = new JRadioButton();
	
	/** 硬盘按纽 **/
	private JRadioButton butDisk = new JRadioButton();
	
	/** 取消按纽 **/
	private JButton cmdCancel = new JButton();
	
	/** 确定按纽 **/
	private JButton cmdOK = new JButton();
	
	/**
	 * 构造命令处理模式窗口，指定上级窗口和模式
	 * @param frame 上级窗口
	 * @param modal 模态或者否
	 */
	public WatchCommandModeDialog(Frame frame, boolean modal) {
		super(frame, modal);
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;

		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			active(event);
		}
	}

	/**
	 * 执行关联操作
	 * @param event
	 */
	private void active(ActionEvent event) {
		if (event.getSource() == cmdOK) {
			// 判断是内存存取
			boolean memory = butMemory.isSelected();
			CommandMode cmd = new CommandMode(memory);
			// 交给调用器管理池
			WatchCommandModeInvoker invoker = new WatchCommandModeInvoker(cmd);
			WatchInvokerPool.getInstance().launch(invoker);

			// 关闭
			dispose();
		} else if (event.getSource() == cmdCancel) {
			dispose();
		}
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return WatchLauncher.getInstance().findCaption(xmlPath);
//	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new InvokeThread(event));
	}
	
	/**
	 * 单选按纽面板
	 * @return
	 */
	private JPanel createRadioPanel() {
		setButtonText(butMemory, findCaption("Dialog/CommandMode/mode/memory/title"));
		setButtonText(butDisk,findCaption("Dialog/CommandMode/mode/disk/title"));
		
		ButtonGroup group = new ButtonGroup();
		group.add(butDisk);
		group.add(butMemory);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 6));

		String title = findCaption("Dialog/CommandMode/mode/title");
		
//		Font font = createTitledBorderFont(title);
//		CompoundBorder border = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(1, 15, 8, 0));
//		panel.setBorder(new TitledBorder(border, title, 
//				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, font));
		
		panel.setBorder(UITools.createTitledBorder(title));
		
		panel.add(butMemory);
		panel.add(butDisk);
		
		// 选择其中之一
		boolean memory = WatchLauncher.getInstance().isMemory();
		if(memory) {
			butMemory.setSelected(true);
		} else {
			butDisk.setSelected(true);
		}
		
		return panel;
	}
	
	/**
	 * 触发按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		setButtonText(cmdOK,findCaption("Dialog/CommandMode/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);
		
		setButtonText(cmdCancel,findCaption("Dialog/CommandMode/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
		
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 8, 0));
		right.add(cmdOK);
		right.add(cmdCancel);
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(right, BorderLayout.EAST);
		
		return bottom;
	}
	
	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 8));
		panel.setBorder(new EmptyBorder(8, 10, 8, 10));
		panel.add(createRadioPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 320; 
		int height = 180; 
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		JPanel pane = initPanel();
		
		Container canvas = getContentPane();
		canvas.setLayout(new BorderLayout(0, 0));
		canvas.add(pane, BorderLayout.CENTER);

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(rect.getSize());
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/CommandMode/title");
		setTitle(title);
		
		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}

}