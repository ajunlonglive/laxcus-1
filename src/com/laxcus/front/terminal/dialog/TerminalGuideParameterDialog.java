/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.access.util.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 分布应用启动参数输入窗口
 * 
 * @author scott.liang
 * @version 1.0 7/25/2020
 * @since laxcus 1.0
 */
public class TerminalGuideParameterDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;
	
	private final static String BOUND = TerminalGuideParameterDialog.class.getSimpleName() + "_BOUND";

	private JButton cmdCancel = new JButton();
	
	/** 按纽 **/
	private JButton cmdOK = new JButton();

	private ArrayList<JComponent> values = new ArrayList<JComponent>();
	
	/** 结果 **/
	private int result = -1;
	
	/**
	 * 构造版本窗口
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalGuideParameterDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
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

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			if (event.getSource() == cmdOK) {
				boolean success = check();
				if (success) {
					success = confirm(); // 确认执行
				}
				// 成功，关闭窗口
				if (success) {
					result = JOptionPane.YES_OPTION;
					saveBound();
					dispose();
				}
			} else if(event.getSource() == cmdCancel) {
				boolean success = cancel();
				if (success) {
					result = JOptionPane.NO_OPTION;
					// 提示确认取消
					dispose();
				}
			}
		}
	}
	
	/**
	 * 确认执行
	 * @return 真或者假
	 */
	private boolean confirm() {
		String title = findCaption("Dialog/GuideParameter/confirm/title");
		String content = findContent("Dialog/GuideParameter/confirm");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 确认取消
	 * @return 真或者假
	 */
	private boolean cancel() {
		String title = findCaption("Dialog/GuideParameter/cancel/title");
		String content = findContent("Dialog/GuideParameter/cancel");
		int who = MessageDialog.showMessageBox(this, title, 
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
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
	 * 显示不足
	 * @param text
	 */
	private void showMissing(InputParameter param) {
		String name = param.getNameText();
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/GuideParameter/missing/title");
		String content = findContent("Dialog/GuideParameter/missing"); 
		content = String.format(content, name);
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	
	/**
	 * 参数错误
	 * @param text
	 */
	private void showParamError(Object param) {
		String name = param.getClass().getName();
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/GuideParameter/param-error/title");
		String content = findContent("Dialog/GuideParameter/param-error"); 
		content = String.format(content, name);
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	
	/**
	 * 格式错误
	 * @param text
	 */
	private void showFormatError(InputParameter param) {
		String name = param.getNameText();
		String title = findCaption("Dialog/GuideParameter/format-error/title");
		String content = findContent("Dialog/GuideParameter/format-error"); 
		content = String.format(content, name);
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	
	/**
	 * 布尔变量
	 * @param cmd
	 * @param param
	 */
	private boolean setBoolean(JCheckBox cmd, InputBoolean param) {
		param.setValue(cmd.isSelected());
		return true;
	}

	/**
	 * 字符串
	 * @param field
	 * @param param
	 * @return
	 */
	private boolean setString(JTextField field, InputString param) {
		String text = field.getText();
		if (text.length() > 0) {
			param.setValue(text);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	private boolean setShort(JTextField field, InputShort param) {
		String text = field.getText();
		if (text.length() > 0) {
			short value = Short.parseShort(text);
			param.setValue(value);
		}else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	private boolean setInteger(JTextField field , InputInteger param) {
		String text = field.getText();
		if (text.length() > 0) {
			int value = Integer.parseInt(text);
			param.setValue(value);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	private boolean setLong(JTextField field , InputLong param) {
		String text = field.getText();
		if (text.length() > 0) {
			long value = Long.parseLong(text);
			param.setValue(value);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}
	
	private boolean setFloat(JTextField field , InputFloat param) {
		String text = field.getText();
		if (text.length() > 0) {
			float value = Float.parseFloat(text);
			param.setValue(value);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	private boolean setDouble(JTextField field , InputDouble param) {
		String text = field.getText();
		if (text.length() > 0) {
			double value = Double.parseDouble(text);
			param.setValue(value);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	private boolean setDate(JTextField field , InputDate param) {
		String text = field.getText();
		if (text.length() > 0) {
			try {
				int value = CalendarGenerator.splitDate(text);
				param.setValue(value);
			} catch (IllegalValueException e) {
				showFormatError(param);	// 弹出错误
				field.requestFocus(); // 获得焦点
				return false;
			}
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		
		return true;
	}

	private boolean setTime(JTextField field , InputTime param) {
		String text = field.getText();
		if (text.length() > 0) {
			try {
				int value = CalendarGenerator.splitTime(text);
				param.setValue(value);
			} catch (IllegalValueException e) {
				showFormatError(param);	// 弹出错误
				field.requestFocus(); // 获得焦点
				return false;
			}
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置参数
	 * @param field
	 * @param param
	 * @return
	 */
	private boolean setTimestamp(JTextField field , InputTimestamp param) {
		String text = field.getText();
		if (text.length() > 0) {
			try {
				long timestamp = CalendarGenerator.splitTimestamp(text);
				param.setValue(timestamp);
			} catch (IllegalValueException e) {
				showFormatError(param); // 弹出错误
				field.requestFocus(); // 获得焦点
				return false;
			}
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				field.requestFocus(); // 获得焦点
				return false;
			}
		}

		return true;
	}

	/**
	 * 检测参数
	 * @return
	 */
	private boolean check() {
		for (JComponent js : values) {
			String name = js.getName();
			Object e = js.getClientProperty(name);
			// 判断继承接口
			if (!Laxkit.isClassFrom(e, InputParameter.class)) {
				showParamError(e);
				return false;
			}
			
			InputParameter param = (InputParameter) e;

			boolean success = false;
			if (js.getClass() == JCheckBox.class && param.getClass() == InputBoolean.class) {
				success = setBoolean((JCheckBox) js, (InputBoolean) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputString.class) {
				success = setString((JTextField) js, (InputString) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputShort.class) {
				success = setShort((JTextField) js, (InputShort) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputInteger.class) {
				success = setInteger((JTextField) js, (InputInteger) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputLong.class) {
				success = setLong((JTextField) js, (InputLong) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputFloat.class) {
				success = setFloat((JTextField) js, (InputFloat) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputDouble.class) {
				success = setDouble((JTextField) js, (InputDouble) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputDate.class) {
				success = setDate((JTextField) js, (InputDate) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputTime.class) {
				success = setTime((JTextField) js, (InputTime) param);
			} else if (js.getClass() == JTextField.class && param.getClass() == InputTimestamp.class) {
				success = setTimestamp((JTextField) js, (InputTimestamp) param);
			} else {
				// 弹出对话框
				showParamError(param);
				js.requestFocus();
				return false;
			}
			
			// 以上已经弹出对话框，不处理返回
			if (!success) {
				js.requestFocus();
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 初始化控件
	 */
	private void initControls() {
		setButtonText(cmdOK, findCaption("Dialog/GuideParameter/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/GuideParameter/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
	}
	
	/**
	 * 初始化按钮
	 * @return
	 */
	private JPanel createButtons() {
		JPanel east = new JPanel();
		east.setLayout(new GridLayout(1, 2, 8, 0));
		east.add(cmdOK);
		east.add(cmdCancel);

		// 做出一个分割线
		JPanel js = new JPanel();
		js.setLayout(new BorderLayout(0, 6));
		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		js.add(new JPanel(), BorderLayout.CENTER);
		js.add(east, BorderLayout.EAST);
		return js;
	}
	
	private JComponent createBoolean(InputBoolean param) {
		JCheckBox cmd = new JCheckBox();
		if (param.isEnabled()) {
			cmd.setSelected(param.getValue());
		}
		if (param.getTooltip() != null) {
			cmd.setToolTipText(param.getTooltip());
		}
		return cmd;
	}
	
	private JComponent createString(InputString param) {
		JTextField field = new JTextField();
		if (param.isEnabled() && param.getValue() != null) {
			field.setText(param.getValue());
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}

	private JComponent createShort(InputShort param) {
		JTextField field = new JTextField();
		field.setDocument(new IntegralDocument(field, 6)); // 包括符号位，6个长度
		if (param.isEnabled()) {
			field.setText(String.valueOf(param.getValue()));
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}
	
	private JComponent createInteger(InputInteger param) {
		JTextField field = new JTextField();
		field.setDocument(new IntegralDocument(field, 11)); // 包括符号位，11个长度
		if (param.isEnabled()) {
			field.setText(String.valueOf(param.getValue()));
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}
	
	private JComponent createLong(InputLong param) {
		JTextField field = new JTextField();
		field.setDocument(new IntegralDocument(field, 20)); // 包括符号位，20个长度
		if (param.isEnabled()) {
			field.setText(String.valueOf(param.getValue()));
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}
	
	private JComponent createFloat(InputFloat param) {
		JTextField field = new JTextField();
		field.setDocument(new FloatDocument(field, 19));
		if (param.isEnabled()) {
			String value = String.format("%.5f", param.getValue());
			field.setText(value);
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}
	
	private JComponent createDouble(InputDouble param) {
		JTextField field = new JTextField();
		field.setDocument(new FloatDocument(field, 38));
		if (param.isEnabled()) {
			String value = String.format("%.5f", param.getValue());
			field.setText(value);
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}
	
	private JComponent createDate(InputDate param) {
		int limit = 10; 
		JTextField field = new JTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleDate.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = findContent("Dialog/GuideParameter/date");
		if (param.getTooltip() != null) {
			tooltip = String.format("<html><body>%s<br>%s</body></html>", tooltip, param.getTooltip());
			field.setToolTipText(tooltip);
		} else {
			field.setToolTipText(tooltip);
		}
		return field;
	}
	
	private JComponent createTime(InputTime param) {
		final int limit = 12;
		JTextField field = new JTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleTime.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("HH:mm:ss SSS", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = findContent("Dialog/GuideParameter/time");
		if (param.getTooltip() != null) {
			tooltip = String.format("<html><body>%s<br>%s</body></html>", tooltip, param.getTooltip());
			field.setToolTipText(tooltip);
		} else {
			field.setToolTipText(tooltip);
		}
		return field;
	}
	
	/**
	 * 建立时间戳
	 * @param param 参数
	 * @return
	 */
	private JComponent createTimestamp(InputTimestamp param) {
		final int limit = 23; //日期时间限制在23个字符
		JTextField field = new JTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleTimestamp.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = findContent("Dialog/GuideParameter/timestamp");
		if (param.getTooltip() != null) {
			tooltip = String.format("<html>%s<br>%s</html>", tooltip, param.getTooltip());
			field.setToolTipText(tooltip);
		} else {
			field.setToolTipText(tooltip);
		}
		return field;
	}
	
	/**
	 * 输入参数
	 * @param param
	 * @return
	 */
	private JComponent createInputField(InputParameter param) {
		if (param.isBoolean()) {
			return createBoolean((InputBoolean)param);
		} else if (param.isString()) {
			return createString((InputString)param);
		}
		// 数字
		else if (param.isShort()) {
			return createShort((InputShort) param);
		} else if (param.isInteger()) {
			return createInteger((InputInteger) param);
		} else if (param.isLong()) {
			return createLong((InputLong) param);
		} else if (param.isFloat()) {
			return createFloat((InputFloat) param);
		} else if (param.isDouble()) {
			return createDouble((InputDouble) param);
		}
		// 日期/时间
		else if (param.isDate()) {
			return createDate((InputDate) param);
		} else if (param.isTime()) {
			return createTime((InputTime) param);
		} else if (param.isTimestamp()) {
			return createTimestamp((InputTimestamp) param);
		}
		
		return null;

//		return new JTextField();
	}
	
	/**
	 * 生成一行参数
	 * @param param 输入参数
	 * @return 
	 */
	private JComponent[] createRow(InputParameter param) {
		// 参数
		JComponent component = createInputField(param);
		// 无效，退出
		if (component == null) {
			return null;
		}

		// 文本框或者其它
		int index = values.size() + 1;
		String name = String.format("PARAM-INDEX:%d", index);
		
		component.putClientProperty(name, param);
		component.setName(name);

		// 标签名称
		JLabel label = new JLabel(param.getNameText());
		label.setToolTipText(param.getNameText());
		setSubSize(label);
		
		return new JComponent[] { label, component };
	}
	
	/**
	 * 建立表
	 * @param table
	 * @return
	 */
	private JPanel createUnit(InputParameterUnit table) {
		int rows = table.size();

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(rows, 1, 0, 4));
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(rows, 1, 0, 4));

		for (InputParameter param : table.list()) {
			JComponent[] js = createRow(param);
			// 忽略！
			if (js == null) {
				continue;
			}
			
			left.add(js[0]);
			right.add(js[1]);
			// 保存参数值
			values.add(js[1]);
		}

		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(5, 0));
		sub.add(left, BorderLayout.WEST);
		sub.add(right, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(sub, BorderLayout.CENTER);
		return panel;
	}
	
	
//	/**
//	 * 生成中心面板
//	 * @param list 录入参数清单
//	 * @return 返回JPanel实例
//	 */
//	private JPanel createCenter(InputParameterList list) {
//		JPanel panel = new JPanel();
//		GridBagLayout gridbag = new GridBagLayout();
//        GridBagConstraints c = new GridBagConstraints();
//        panel.setLayout(gridbag);
//        c.fill = GridBagConstraints.BOTH;
//
//		for (InputParameterUnit unit : list.list()) {
//			JPanel sub = createUnit(unit);
//			String text = unit.getFamilyText();
//			sub.setBorder(UITools.createTitledBorder(text, 1)); // 边框
//			
//			c.weightx = 1.0;
//			c.gridwidth = GridBagConstraints.REMAINDER; //end row
//			gridbag.setConstraints(sub, c);
//			panel.add(sub);
//			
//			// reset to default
//			c.weightx = 0.0;			
//		}
//		
//		return panel;
//	}
	
//	/**
//	 * 生成中心面板
//	 * @param list 录入参数清单
//	 * @return 返回JPanel实例
//	 */
//	private JPanel createCenter(InputParameterList list) {
//		String stage = findContent("Dialog/GuideParameter/stage");
//		
//		JPanel panel = new JPanel();
//		GridBagLayout gridbag = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		panel.setLayout(gridbag);
////		c.fill = GridBagConstraints.BOTH;
//		
//		c.fill = GridBagConstraints.HORIZONTAL;
//		
//		int index = 0;
//		
//		for (InputParameterUnit unit : list.list()) {
//			JPanel sub = createUnit(unit);
//			String text = String.format(stage, unit.getFamilyText());
//			sub.setBorder(UITools.createTitledBorder(text, 2)); // 边框
//
//			if (index == 0) {
//				c.fill = GridBagConstraints.HORIZONTAL;
//				c.anchor = GridBagConstraints.NORTH;
//			} else {
//				c.fill = GridBagConstraints.HORIZONTAL;
//				c.anchor = GridBagConstraints.NORTH;
//			}
//			c.weightx = 1.0; // 为0不拉伸，否则自动水平拉伸
//			c.weighty = 1.0; // 为0不拉伸，否则自动垂直拉伸
////			c.ipady = 3;
//			c.gridwidth = GridBagConstraints.REMAINDER; // 0值，最后一个
//			gridbag.setConstraints(sub, c);
//			panel.add(sub);
//
//			index++;
//			
////			// reset to default
////			c.weightx = 0.0;			
//		}
//
//		return panel;
//	}
	
//	/**
//	 * 生成中心面板
//	 * @param list 录入参数清单
//	 * @return 返回JPanel实例
//	 */
//	private JPanel createCenter(InputParameterList list) {
//		String stage = findContent("Dialog/GuideParameter/stage");
//		
//		JPanel panel = new JPanel();
//		GridBagLayout gridbag = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		panel.setLayout(gridbag);
////		c.fill = GridBagConstraints.BOTH;
//		
//		c.fill = GridBagConstraints.HORIZONTAL;
//		
//		/**
//		 * anchor属性：将组件放在占单元格的某个固定方向上
//		 * 当fill在水平有效时，WEST/EAST对于anchor无效
//		 * 当fill在垂直有效时，NORTH/SOUTH对于anchor无效
//		 * 当weightx/weighty有分配值时，anchor才有效
//		 */
//		for (InputParameterUnit unit : list.list()) {
//			JPanel sub = createUnit(unit);
//			String text = String.format(stage, unit.getFamilyText());
//			sub.setBorder(UITools.createTitledBorder(text, 2)); // 边框
//
//			c.fill = GridBagConstraints.HORIZONTAL;
//			c.anchor = GridBagConstraints.NORTH;
//
//			c.gridx = GridBagConstraints.RELATIVE;
//			c.gridy = GridBagConstraints.RELATIVE;
//
//			c.weightx = 1.0; // 为0不拉伸，否则自动水平拉伸
//			c.weighty = 1.0; // 为0不拉伸，否则自动垂直拉伸
//			// c.ipady = 3;
//			c.gridheight = 1;// GridBagConstraints.REMAINDER; // 0值，最后一个
//			c.gridwidth = GridBagConstraints.REMAINDER; // 0值，最后一个
//			gridbag.setConstraints(sub, c);
//			panel.add(sub);
//
//			// // reset to default
//			// c.weightx = 0.0;
//		}
//
//		return panel;
//	}
	
	/**
	 * 生成中心面板
	 * @param list 录入参数清单
	 * @return 返回JPanel实例
	 */
	private JPanel createCenter(InputParameterList list) {
		String stage = findContent("Dialog/GuideParameter/stage");
		
//		JPanel panel = new JPanel();
//		GridBagLayout gridbag = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		panel.setLayout(gridbag);
////		c.fill = GridBagConstraints.BOTH;
//		
//		c.fill = GridBagConstraints.HORIZONTAL;
//		
//		/**
//		 * anchor属性：将组件放在占单元格的某个固定方向上
//		 * 当fill在水平有效时，WEST/EAST对于anchor无效
//		 * 当fill在垂直有效时，NORTH/SOUTH对于anchor无效
//		 * 当weightx/weighty有分配值时，anchor才有效
//		 */
//		for (InputParameterUnit unit : list.list()) {
//			JPanel sub = createUnit(unit);
//			String text = String.format(stage, unit.getFamilyText());
//			sub.setBorder(UITools.createTitledBorder(text, 2)); // 边框
//
//			c.fill = GridBagConstraints.HORIZONTAL;
//			c.anchor = GridBagConstraints.NORTH;
//
//			c.gridx = GridBagConstraints.RELATIVE;
//			c.gridy = GridBagConstraints.RELATIVE;
//
//			c.weightx = 1.0; // 为0不拉伸，否则自动水平拉伸
//			c.weighty = 1.0; // 为0不拉伸，否则自动垂直拉伸
//			// c.ipady = 3;
//			c.gridheight = 1;// GridBagConstraints.REMAINDER; // 0值，最后一个
//			c.gridwidth = GridBagConstraints.REMAINDER; // 0值，最后一个
//			gridbag.setConstraints(sub, c);
//			panel.add(sub);
//
//			// // reset to default
//			// c.weightx = 0.0;
//		}

		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		
		for (InputParameterUnit unit : list.list()) {
			JPanel sub = createUnit(unit);
			String text = String.format(stage, unit.getFamilyText());
			sub.setBorder(UITools.createTitledBorder(text, 2)); // 边框
			
			panel.add(sub);
		}
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(panel, BorderLayout.NORTH);
		p.add(new JPanel(), BorderLayout.CENTER);
		return p;
	}

	private void setSubSize(JLabel sub) {
		Dimension d = new Dimension(50, 28);
//		sub.setPreferredSize(d);
		sub.setMinimumSize(d);
//		sub.setMaximumSize(d);
	}
	
	/**
	 * 前界面
	 * @param caption 标题 
	 * @return 返回JPanel实例
	 */
	private JPanel createNorth(String caption) {
		if (caption == null) {
			caption = "";
		}
		JLabel label = new JLabel(caption);
		label.setToolTipText(caption);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		setSubSize(label);
		
		Font font = label.getFont();
		Font f = new Font(font.getName(), font.getStyle() | Font.BOLD, font.getSize());
		label.setFont(f);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(label, BorderLayout.CENTER);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * 构造布局
	 * @return
	 */
	private JPanel initPane(String caption, InputParameterList list) {
		initControls();

		JScrollPane scroll = new JScrollPane(createCenter(list));
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		setRootBorder(panel);
		panel.add(createNorth(caption), BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(createButtons(), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 系统中取出参数
		Object e = UITools.getProperity(BOUND);
		if (e != null && e.getClass() == Rectangle.class) {
			return (Rectangle) e;
		}
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (size.getWidth() * 0.26);
		int height = (int) (size.getHeight() * 0.55);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;

		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * 显示窗口
	 * @param caption
	 * @param table
	 * @return
	 */
	public int showDialog(String caption, InputParameterList table) {
		JPanel pane = initPane(caption, table);
		setContentPane(pane);

		setBounds(getBound());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 300));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/GuideParameter/title");
		setTitle(title);

		setVisible(true);
		
		// 返回结果
		return result;
	}

}