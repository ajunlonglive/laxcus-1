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

import com.laxcus.command.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.invoker.*;
import com.laxcus.watch.pool.*;

/**
 * 节点资源诊断间隔窗口
 * 
 * @author scott.liang
 * @version 1.0 10/2/2018
 * @since laxcus 1.0
 */
public class WatchOutlookIntervalDialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	/** 小时 **/
	private JComboBox boxHour = new JComboBox();

	/** 分钟 **/
	private JComboBox boxMinute = new JComboBox();

	/** 秒 **/
	private JComboBox boxSecond = new JComboBox();

	/** 取消按纽 **/
	private JButton cmdCancel = new JButton();
	
	/** 确定按纽 **/
	private JButton cmdOK = new JButton();
	
	/**
	 * 构造节点资源诊断间隔窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public WatchOutlookIntervalDialog(Frame parent, boolean modal) {
		super(parent, modal);
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
			long interval = evaluate();
			if (interval < 5000) {
				// 提示错误
				String title = findCaption("Dialog/OutlookInterval/warning/title");
				String content = findContent("Dialog/OutlookInterval/warning");
				
				MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
				return;
			}
			
			OutlookInterval cmd = new OutlookInterval(interval);
			WatchOutlookIntervalInvoker invoker = new WatchOutlookIntervalInvoker(cmd);
			WatchInvokerPool.getInstance().launch(invoker);

			// 关闭
			dispose();
		} else if (event.getSource() == cmdCancel) {
			dispose();
		}
	}
	
	/**
	 * 返回转换值
	 * @param box
	 * @return
	 */
	private long translate(JComboBox box) {
		String item = (String) box.getSelectedItem();
		return Integer.parseInt(item);
	}

	/**
	 * 计算时间，以毫秒为单位。
	 * @return 返回毫秒为单位的时间
	 */
	private long evaluate() {
		long hour = translate(boxHour);
		long minute = translate(boxMinute);
		long second = translate(boxSecond);

		long interval = hour * Laxkit.HOUR + minute * Laxkit.MINUTE + second * Laxkit.SECOND;

		return interval;
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return WatchLauncher.getInstance().findCaption(xmlPath);
//	}
//	
//	/**
//	 * 解析内容
//	 * @param xmlPath
//	 * @return 抽取的文本
//	 */
//	private String findContent(String xmlPath) {
//		return WatchLauncher.getInstance().findContent(xmlPath);
//	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new InvokeThread(event));
	}
	
	/**
	 * 初始化参数
	 */
	private void initControls() {
		for (int i = 0; i < 24; i++) {
			boxHour.addItem(String.format("%d", i));
		}
		for (int i = 0; i < 60; i++) {
			boxMinute.addItem(String.format("%d", i));
		}
		for (int i = 0; i < 60; i++) {
			boxSecond.addItem(String.format("%d", i));
		}
		
		// 间隔时间
		long interval = WatchTube.getTimeout();
		// 取出时间下标
		int hour = (int) (interval / Laxkit.HOUR);
		int minute = (int) ((interval - hour * Laxkit.HOUR) / Laxkit.MINUTE);
		int second = (int) ((interval - hour * Laxkit.HOUR - minute * Laxkit.MINUTE) / 1000);
		
		// 设置下标位置
		boxHour.setSelectedIndex(hour);
		boxMinute.setSelectedIndex(minute);
		boxSecond.setSelectedIndex(second);
	}
	
	/**
	 * 分割组件
	 * @param lable
	 * @param box
	 * @return
	 */
	private JPanel createSubPanel(JLabel lable, JComboBox box) {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 2));
		sub.add(lable);
		sub.add(box);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(sub, BorderLayout.WEST);
		return panel;
	}
	
	/**
	 * 多选框面板
	 * @return
	 */
	private JPanel createComboxPanel() {
		JLabel hour = new JLabel("", SwingConstants.LEFT);
		setLabelText(hour, findCaption("Dialog/OutlookInterval/time/hour/title"));
		hour.setDisplayedMnemonic('H');
		hour.setLabelFor(boxHour);

		JLabel minute = new JLabel("",SwingConstants.LEFT);
		setLabelText(minute, findCaption("Dialog/OutlookInterval/time/minute/title"));
		minute.setDisplayedMnemonic('M');
		minute.setLabelFor(boxMinute);

		JLabel second = new JLabel("",SwingConstants.LEFT);
		setLabelText(second, findCaption("Dialog/OutlookInterval/time/second/title"));
		second.setDisplayedMnemonic('S');
		second.setLabelFor(boxSecond);

		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1, 3, 1, 0));
		
		String title = findCaption("Dialog/OutlookInterval/time/title");
		
//		Font font = createTitledBorderFont(title);
//		CompoundBorder border = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(0, 15, 8, 0));
//		center.setBorder(new TitledBorder(border,  title, 
//				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, font));
		
		center.setBorder(UITools.createTitledBorder(title));
		
		center.add(createSubPanel(hour, boxHour));
		center.add(createSubPanel(minute, boxMinute));
		center.add(createSubPanel(second, boxSecond));

		return center;
	}
	
	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		setButtonText(cmdOK, findCaption("Dialog/OutlookInterval/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);
		
		setButtonText(cmdCancel, findCaption("Dialog/OutlookInterval/buttons/cancel/title"));
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
		initControls();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
//		panel.setBorder(new EmptyBorder(3, 10, 3, 10));
		setRootBorder(panel);
		panel.add(createComboxPanel(), BorderLayout.NORTH);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 358; 
		int height = 180; 
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		setContentPane(initPanel());
		
//		JPanel pane = initPanel();
//		Container canvas = getContentPane();
//		canvas.setLayout(new BorderLayout(0, 0));
//		canvas.add(pane, BorderLayout.CENTER);

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(rect.getSize()); // new Dimension(200, 180));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/OutlookInterval/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}

}