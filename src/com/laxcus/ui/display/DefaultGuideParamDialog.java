/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.access.util.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 分布应用启动参数输入窗口
 * 
 * 以图形界面显示
 * 
 * @author scott.liang
 * @version 1.0 7/25/2020
 * @since laxcus 1.0
 */
public class DefaultGuideParamDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;

	/** 取消 **/
	private FlatButton cmdCancel = new FlatButton();
	
	/** 确定 **/
	private FlatButton cmdOK = new FlatButton();

	private ArrayList<JComponent> values = new ArrayList<JComponent>();
	
	/** 标题 **/
	private String caption;
	
	/** 表实例 **/
	private InputParameterList table;
	
	/**
	 * 构造版本窗口
	 */
	public DefaultGuideParamDialog(String s, InputParameterList e)  {
		super();
		caption = s;
		table = e;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 单击
	 * @param event
	 */
	private void click(ActionEvent event) {
		if (event.getSource() == cmdOK) {
			boolean success = check();
			if (success) {
				success = confirm(); // 确认执行
			}
			// 成功，关闭窗口
			if (success) {
				this.writeBound();
				this.setSelectedValue(new Boolean(true));
			}
		} else if (event.getSource() == cmdCancel) {
			boolean success = cancel();
			if (success) {
				this.writeBound();
				// 提示确认取消
				this.setSelectedValue(new Boolean(false));
			}
		}
	}
	
	/**
	 * 确认执行
	 * @return 真或者假
	 */
	private boolean confirm() {
		String title = UIManager.getString("DefaultGuideParamDialog.ConfirmTitle");
		String content = UIManager.getString("DefaultGuideParamDialog.ConfirmContent");
		return MessageBox.showYesNoDialog(this, title, content);
	}

	/**
	 * 确认取消
	 * @return 真或者假
	 */
	private boolean cancel() {
		String title = UIManager.getString("DefaultGuideParamDialog.CancelTitle");
		String content = UIManager.getString("DefaultGuideParamDialog.CancelContent");
		return MessageBox.showYesNoDialog(this, title, content);
	}
	
	/**
	 * 显示不足
	 * @param text
	 */
	private void showMissing(InputParameter param) {
		String name = param.getNameText();
		// xxx 是必选项，请输入！
		String title = UIManager.getString("DefaultGuideParamDialog.MissingTitle");
		String content = UIManager.getString("DefaultGuideParamDialog.MissingContent"); 
		content = String.format(content, name);
		MessageBox.showWarning(this, title, content);
	}
	
	/**
	 * 参数错误
	 * @param text
	 */
	private void showParamError(Object param) {
		String name = param.getClass().getName();
		// xxx 是必选项，请输入！
		String title = UIManager.getString("DefaultGuideParamDialog.ParamErrorTitle");
		String content = UIManager.getString("DefaultGuideParamDialog.ParamErrorContnet"); 
		content = String.format(content, name);
		MessageBox.showFault(this, title, content); 
	}
	
	/**
	 * 格式错误
	 * @param text
	 */
	private void showFormatError(InputParameter param) {
		String name = param.getNameText();
		String title = UIManager.getString("DefaultGuideParamDialog.FormatErrorTitle");
		String content = UIManager.getString("DefaultGuideParamDialog.FormatErrorContent"); 
		content = String.format(content, name);
		MessageBox.showFault(this, title, content);
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
	private boolean setString(FlatTextField field, InputString param) {
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

	private boolean setShort(FlatTextField field, InputShort param) {
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

	private boolean setInteger(FlatTextField field , InputInteger param) {
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

	private boolean setLong(FlatTextField field , InputLong param) {
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
	
	private boolean setFloat(FlatTextField field , InputFloat param) {
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

	private boolean setDouble(FlatTextField field , InputDouble param) {
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

	private boolean setDate(FlatTextField field , InputDate param) {
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

	private boolean setTime(FlatTextField field , InputTime param) {
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
	private boolean setTimestamp(FlatTextField field , InputTimestamp param) {
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
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputString.class) {
				success = setString((FlatTextField) js, (InputString) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputShort.class) {
				success = setShort((FlatTextField) js, (InputShort) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputInteger.class) {
				success = setInteger((FlatTextField) js, (InputInteger) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputLong.class) {
				success = setLong((FlatTextField) js, (InputLong) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputFloat.class) {
				success = setFloat((FlatTextField) js, (InputFloat) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputDouble.class) {
				success = setDouble((FlatTextField) js, (InputDouble) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputDate.class) {
				success = setDate((FlatTextField) js, (InputDate) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputTime.class) {
				success = setTime((FlatTextField) js, (InputTime) param);
			} else if (js.getClass() == FlatTextField.class && param.getClass() == InputTimestamp.class) {
				success = setTimestamp((FlatTextField) js, (InputTimestamp) param);
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
		cmdOK.setText(UIManager.getString("DefaultGuideParamDialog.ButtonOkayText"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		cmdCancel.setText(UIManager.getString("DefaultGuideParamDialog.ButtonCancelText"));
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
		FlatTextField field = new FlatTextField();
		if (param.isEnabled() && param.getValue() != null) {
			field.setText(param.getValue());
		}
		if (param.getTooltip() != null) {
			field.setToolTipText(param.getTooltip());
		}
		return field;
	}

	private JComponent createShort(InputShort param) {
		FlatTextField field = new FlatTextField();
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
		FlatTextField field = new FlatTextField();
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
		FlatTextField field = new FlatTextField();
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
		FlatTextField field = new FlatTextField();
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
		FlatTextField field = new FlatTextField();
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
		FlatTextField field = new FlatTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleDate.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = UIManager.getString("DefaultGuideParamDialog.DateContent");
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
		FlatTextField field = new FlatTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleTime.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("HH:mm:ss SSS", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = UIManager.getString("DefaultGuideParamDialog.TimeContent");
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
		FlatTextField field = new FlatTextField();
		field.setColumns(limit);
		field.setDocument(new DateTimeDocument(field, limit));
		
		if (param.isEnabled() && param.getValue() > 0) {
			Date date = com.laxcus.util.datetime.SimpleTimestamp.format(param.getValue());
			SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
			String text = style.format(date);
			field.setText(text);
		}
		
		String tooltip = UIManager.getString("DefaultGuideParamDialog.TimestampContent"); // findContent("Dialog/GuideParameter/timestamp");
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
	
	/**
	 * 生成中心面板
	 * @param list 录入参数清单
	 * @return 返回JPanel实例
	 */
	private JPanel createCenter(InputParameterList list) {
		String stage = UIManager.getString("DefaultGuideParamDialog.StateContent"); 

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
		sub.setMinimumSize(d);
	}
	
	/**
	 * 前界面
	 * @param caption 标题 
	 * @return 返回JPanel实例
	 */
	private JPanel createNorth() {
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
	private JPanel initPane(InputParameterList list) {
		initControls();

		JScrollPane jsp = new JScrollPane(createCenter(list));
		jsp.putClientProperty("NotBorder",  Boolean.TRUE);
		//	jsp.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		panel.add(createNorth(), BorderLayout.NORTH);
		panel.add(jsp, BorderLayout.CENTER);
		panel.add(createButtons(), BorderLayout.SOUTH);
		return panel;
	}

	
	private Rectangle readBound() {
		return RTKit.readBound(RTEnvironment.ENVIRONMENT_USER, "DefaultGuideParamDialog/Bound");
	}

	private void writeBound() {
		Rectangle rect = getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_USER, "DefaultGuideParamDialog/Bound", rect);
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = readBound(); 
		if (rect != null) {
			return rect;
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (size.getWidth() * 0.26);
		int height = (int) (size.getHeight() * 0.55);
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
		setDefaultBounds(dlg, parent);
	}
	
	/**
	 * 初始化窗口
	 */
	private void initDialog() {
		// 标题
		setTitle(UIManager.getString("DefaultGuideParamDialog.Title"));
		// 面板
		JPanel pane = initPane(table);
		setContentPane(pane);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.gui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 必须是模态窗口
		if (!modal) {
			throw new IllegalArgumentException("must be modal!");
		} 

		initDialog();

		// 范围
		setBounds(parent);

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		return showModalDialog(parent, cmdCancel);
	}

	/**
	 * 以模态方式显示窗口
	 * @param parent 父类窗口句柄
	 * @return 成功返回真，撤销返回假
	 */
	public boolean showDialog(Component parent) {
		Boolean value = (Boolean) showDialog(parent, true);
		if (value != null) {
			return value.booleanValue();
		}
		return false;
	}

}