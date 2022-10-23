/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.color;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.color.*;

/**
 * 颜色对话框
 * 
 * @author scott.liang
 * @version 1.0 8/26/2021
 * @since laxcus 1.0
 */
public class ColorDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = 2819376533814230409L;

	/** 默认的颜色 **/
	private Color defaultColor;

	/** 选中的颜色 **/
	private Color selectColor;

	/** 自定义区域 **/
	private ColorField[] customFields;

	/** 调色板 **/
	private ColorPlate colorPlate;

	/** 滚动面板 **/
	private SildePlate sildePlate;
	/** 滑动条 **/
	private JSlider slider;

	/** 例子面板 **/
	private ExamplePlate examplePlate;

	/** 选中 **/
	private FlatButton cmdAppend;

	/** 确定按纽 **/
	private FlatButton cmdOkay;

	/** 取消按纽 **/
	private FlatButton cmdCancel;
	
	private ChangeListener sliderListener = new SliderAdapter();
	
	class CustomActionAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ColorField field = (ColorField) e.getSource();
			Color c = field.getSelectColor();
			if (c != null) {
//				addThread(new SelectColorThread(c));
				setSelectColor(c);
			}
		}
	}

	class BasicActionAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ColorField field = (ColorField) e.getSource();
			Color c = field.getSelectColor();
			if (c != null) {
//				addThread(new SelectColorThread(c));
				setSelectColor(c);
			}
		}
	}
	
//	class SelectColorThread extends SwingEvent {
//		Color color;
//		public SelectColorThread(Color c) {
//			super();
//			color = c;
//		}
//		public void process() {
//			setSelectColor(color);
//		}
//	}
	
	class SliderAdapter implements ChangeListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == slider) {
				// addThread(new SliderThread(slider.getValue()));
				doSlider(slider.getValue());
			}
		}
	}
	
	class ESLKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			doESLText();
		}
	}
	
	class RGBKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			doRGBText();
		}
	}
	
	/**
	 * ESL文本框键盘弹起后，处理颜色
	 */
	private void doESLText() {
		int H = getValue(txtH);
		int S = getValue(txtS);
		int L = getValue(txtL);
		ESL esl = new ESL((double) H, (double) S, (double) L);
		Color c = esl.toColor();
		
		selectColor = c;
		setExampleColor(c);
		setSlideColor(c);
		setRGBTextColorValue(c);
	}
	
	/**
	 * RGB文本框键盘弹起后，处理颜色
	 */
	private void doRGBText() {
		int r = getValue(txtR);
		int g = getValue(txtG);
		int b = getValue(txtB);
		RGB rgb = new RGB(r, g, b);
		Color c = rgb.toColor();
		
		selectColor = c;
		setExampleColor(c);
		setSlideColor(c);
		setESLTextColorValue(c);
	}
	
	
	private int getValue(JTextField field) {
		String s = field.getText();
		if (s.trim().length() == 0) {
			// field.setText("0");
			field.setText("");
			return 0;
		}
		
		return Integer.parseInt(s);
	}

//	class SliderThread extends SwingEvent {
//		int value;
//
//		public SliderThread(int v) {
//			super();
//			value = v;
//		}
//
//		public void process() {
//			// 调整亮度
//			int H = getValue(txtH);
//			int S = getValue(txtS);
//			ESL esl = new ESL((double) H, (double) S, (double) value);
//			Color c = esl.toColor();
//
//			// setCursorColor(c);
//
//			selectColor = c;
//			// 设置示例面板的颜色
//			setExampleColor(c);
//			// 修改ESL的L值
//			setESLValue(txtL, (double) value);
//			// 设置RGB颜色值
//			setRGBTextColorValue(c);
//		}
//	}
	
	private void doSlider(int value) {
		// 调整亮度
		int H = getValue(txtH);
		int S = getValue(txtS);
		ESL esl = new ESL((double) H, (double) S, (double) value);
		Color c = esl.toColor();

		// setCursorColor(c);

		selectColor = c;
		// 设置示例面板的颜色
		setExampleColor(c);
		// 修改ESL的L值
		setESLValue(txtL, (double) value);
		// 设置RGB颜色值
		setRGBTextColorValue(c);
	}
	
//	/**
//	 * 设置光标颜色
//	 * @param c
//	 */
//	private void setCursorColor(Color c) {
//		if (colorPlate != null) {
//			colorPlate.setFocusColor(c);
//			colorPlate.repaint();
//		}
//	}
	
	/**
	 * 设置滑动条颜色
	 * @param c
	 */
	private void setSlideColor(Color c) {
		// 亮度面板
		if (sildePlate != null) {
			sildePlate.setSelectColor(c);
			sildePlate.repaint();
		}
		// 滑块
		if (slider != null) {			
			// 删除事件句柄后，不产生事件
			slider.removeChangeListener(sliderListener);
			
			// 调整值
			ESL e = new RGB(c).toESL();
			int l = (int) Math.round(e.getL());
			slider.setValue(l);
			
			// 恢复事件句柄
			slider.addChangeListener(sliderListener);
		}
	}
	
	/**
	 * 设置示例面板颜色
	 * @param c
	 */
	private void setExampleColor(Color c) {
		// 设置颜色
		if (examplePlate != null) {
			//	System.out.printf("example color %s\n", c);
			examplePlate.setSelectColor(c);
			examplePlate.repaint();
		}
	}

	/**
	 * 设置选择的颜色
	 * @param c
	 */
	private void setSelectColor(Color c) {
		selectColor = c;
		// 示例面板、滑块、ESL值、RGB值
		setExampleColor(c);
		setSlideColor(c);
		setESLTextColorValue(c);
		setRGBTextColorValue(c);
	}
	
	private void setESLValue(JTextField field, double value) {
		String s = String.format("%d", Math.round(value));
		field.setText(s);
	}
	
	private void setRGBValue(JTextField field, int value) {
		String s = String.format("%d", value);
		field.setText(s);
	}
	
	private void setESLTextColorValue(Color c) {
		ESL esl = new RGB(c).toESL();
		setESLValue(txtH, esl.getH());
		setESLValue(txtS, esl.getS());
		setESLValue(txtL, esl.getL());
	}

	private void setRGBTextColorValue(Color c) {
		setRGBValue(txtR, c.getRed());
		setRGBValue(txtG, c.getGreen());
		setRGBValue(txtB, c.getBlue());
	}

	/**
	 * 设置颜色对话框
	 * @param c 颜色对话框
	 */
	public ColorDialog(Color c) {
		super();
		setDefaultColor(c);
		setDefaultTitle();
	}

	/**
	 * 设置默认的颜色对话框
	 */
	public ColorDialog() {
		this(null);
	}

	/**
	 * 设置默认的颜色
	 * @param c
	 */
	public void setDefaultColor(Color c) {
		defaultColor = c;
	}

	/**
	 * 返回默认的颜色
	 * @return
	 */
	public Color getDefaultColor() {
		return defaultColor;
	}

	/**
	 * 单击
	 * @param e
	 */
	private void click(ActionEvent e) {
		Object source = e.getSource();
		// 按纽
		if (source == cmdOkay) {
			writeCustomColors();
			saveBound();
			setSelectedValue(selectColor);
		} else if (source == cmdCancel) {
			writeCustomColors();
			saveBound();
			setSelectedValue(null);
		} else if(source == cmdAppend) {
			doAppendColor();
		} else if(source == colorPlate) {
			choicePaletteColor();
		}
	}

//	class ClickThread extends SwingEvent {
//		ActionEvent event;
//
//		ClickThread(ActionEvent e) {
//			super();
//			event = e;
//		}
//
//		public void process() {
//			click(event);
//		}
//	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
//		addThread(new ClickThread(e));
		click(e);
	}

	private void choicePaletteColor() {
		Color color = colorPlate.getSelectColor();
		setSelectColor(color);
	}

	/** 选择下标 **/
	private int selectIndex = 0;

	/**
	 * 添加基础颜色到自定义颜色区域
	 */
	private void doAppendColor() {
		// 如果没有选中的颜色...
		if (selectColor == null) {
			return;
		}

		// 如果颜色已经定义，忽略它
		for (int i = 0; i < customFields.length; i++) {
			Color s = customFields[i].getSelectColor();
			if (s != null && s.getRGB() == selectColor.getRGB()) {
				String title = UIManager.getString("ColorDialog.DuplicateColorTitle");
				String content = UIManager.getString("ColorDialog.DuplicateColorContent");
				MessageBox.showWarning(this, title, content);
				return; // 忽略
			}
		}

		// 找到一个空的，写入颜色
		for (int i = 0; i < customFields.length; i++) {
			if (customFields[i].getSelectColor() == null) {
				customFields[i].setSelectColor(selectColor);
				customFields[i].repaint();
				return;
			}
		}

		// 循环
		int index = (selectIndex >= customFields.length ? 0 : selectIndex);
		// 重新绘制颜色
		customFields[index].setSelectColor(selectColor);
		customFields[index].repaint();
		// 指向下一个
		selectIndex = index + 1;
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "ColorDialog/Bound", rect);
		}
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "ColorDialog/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension size = PlatformKit.getPlatformDesktop().getSize(); // Toolkit.getDefaultToolkit().getScreenSize();

		int width = 558; 
		int height = 410;

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
	 * 生成按纽
	 * @param key
	 * @param w
	 * @return
	 */
	private FlatButton createButton(String key, char w) {
		String text = UIManager.getString(key);
		FlatButton but = new FlatButton(text);
		but.setMnemonic(w);
		but.addActionListener(this);
		return but;
	}

	/**
	 * 生成最底层的按纽
	 * @return
	 */
	private JPanel createBottomPanel() {
		cmdAppend = createButton("ColorDialog.AppendButtonText", 'A');
		cmdOkay = createButton("ColorDialog.OkayButtonText", 'O');
		cmdCancel = createButton("ColorDialog.CancelButtonText", 'C');

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 4, 0));
		p.add(cmdOkay);
		p.add(cmdCancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(cmdAppend, BorderLayout.WEST);
		panel.add(p, BorderLayout.EAST);
		return panel;
	}

	private final String REGEX_COLOR = "^\\s*([0-9]{1,3})\\s*\\,\\s*\\s*([0-9]{1,3})\\s*\\,\\s*\\s*([0-9]{1,3})\\s*$";

	private final String REGEX_XCOLOR = "^\\s*(?:#)(?i)([0-9a-f]{1,6})\\s*";

	/**
	 * 解析一个颜色值
	 * @param input 输入
	 * @return 返回颜色值，或者空指针
	 */
	private Color splitSingleColor(String input) {
		// 1. 三个整数
		Pattern pattern = Pattern.compile(REGEX_COLOR);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			int r = java.lang.Integer.parseInt(matcher.group(1));
			int g = java.lang.Integer.parseInt(matcher.group(2));
			int b = java.lang.Integer.parseInt(matcher.group(3));
			// 在规定值内
			if (r <= 255 && g <= 255 && b <= 255) {
				return new Color(r, g, b);
			}
		}

		// 2. 一个16位数字
		pattern = Pattern.compile(REGEX_XCOLOR);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			int value = java.lang.Integer.parseInt(matcher.group(1), 16);
			return new Color(value);
		}
		return null;
	}

	/**
	 * 解析颜色
	 * @param input
	 * @return
	 */
	private Color[] splitColor(String input) {
		String[] items = input.split(";");
		ArrayList<Color> array = new ArrayList<Color>();
		for (String s : items) {
			Color color = splitSingleColor(s);
			if (color != null && !array.contains(color)) {
				array.add(color);
			}
		}
		Color[] colors = new Color[array.size()];
		return array.toArray(colors);
	}

	/**
	 * 基础颜色面板
	 * @return JPanel实例
	 */
	private JPanel createBasicPanel() {
		JLabel title = new JLabel(UIManager.getString("ColorDialog.BasicLabelText"));

		// 自定义颜色
		String input = UIManager.getString("ColorDialog.BasicColor");
		Color[] colors = splitColor(input);

		int rows = 11;
		int columns = 8;
		ColorField[] fields = createFields(rows, columns, new BasicActionAdapter(), null);
		// 定义颜色
		for (int i = 0; i < colors.length; i++) {
			if (i >= fields.length) {
				break;
			}
			fields[i].setSelectColor(colors[i]);
		}

		// 显示颜色
		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(rows, columns, 4, 4));
		for (int i = 0; i < fields.length; i++) {
			grid.add(fields[i]);
		}
//		grid.setBorder(new EmptyBorder(4, 4, 4, 4));
		grid.setBorder(new EmptyBorder(2,2,2,2));

		// 基本值
		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(0, 4));
		north.add(title, BorderLayout.NORTH);
		north.add(grid, BorderLayout.CENTER);

		JPanel panel =new JPanel();
		panel.setLayout(new BorderLayout());
		//		panel.setBorder(new EmptyBorder(6, 0, 0, 0));
		panel.add(north, BorderLayout.NORTH);
		return panel;
	}


	private ColorField[] createFields(int rows, int columns, ActionListener listener, Color color) {
		Dimension d = new Dimension(21, 17);
		int size = rows * columns;
		ColorField[] fields = new ColorField[size];
		for (int i = 0; i < size; i++) {
			fields[i] = new ColorField();

			fields[i].setContentAreaFilled(false); // 平面
			fields[i].setBorderPainted(false); // 不绘制边框

			fields[i].setBorder(new EmptyBorder(0, 0, 0, 0));
			fields[i].setPreferredSize(d);
			fields[i].setMinimumSize(d);
			fields[i].setMaximumSize(d);
			//			fields[i].setEditable(true);

			fields[i].setFocusable(true);
			fields[i].setRequestFocusEnabled(true);

			if (color != null) {
				fields[i].setSelectColor(color);
			}

			//			if (adapter != null) {
			//				fields[i].addFocusListener(adapter);
			////				fields[i].addActionListener(adapter);
			//			}

			if (listener != null) {
				fields[i].addActionListener(listener);
			}
		}
		return fields;
	}

	/**
	 * 写入自定义颜色
	 */
	private void writeCustomColors() {
		ArrayList<Color> array = new ArrayList<Color>();
		for (ColorField field : customFields) {
			if (field.getSelectColor() != null) {
				array.add(field.getSelectColor());
			}
		}

		for (int i = 0; i < array.size(); i++) {
			Color color = array.get(i);
			String path = String.format("ColorDialog/CustomColor%d", i + 1);
			RTKit.writeColor(RTEnvironment.ENVIRONMENT_SYSTEM, path, color);
		}
	}

	/**
	 * 读取自定义颜色
	 * @return 返回数组
	 */
	private Color[] readCustomColors() {
		ArrayList<Color> array = new ArrayList<Color>();
		for(int i =0; i < 100; i++) {
			String path = String.format("ColorDialog/CustomColor%d", i + 1);
			Color color = RTKit.readColor(RTEnvironment.ENVIRONMENT_SYSTEM, path);
			if(color == null){
				break;
			}
			array.add(color);
		}

		Color[] colors = new Color[array.size()];
		return array.toArray(colors);
	}

	/**
	 * 生成自定义颜色边框
	 * @return
	 */
	private JPanel createCustomPanel() {
		JLabel title = new JLabel(UIManager.getString("ColorDialog.CustomLabelText"));

		int rows = 2;
		int columns = 8;

		customFields = createFields(rows, columns, new CustomActionAdapter(), null);

		// 读取自定义颜色
		Color[] colors = readCustomColors();
		for (int i = 0; i < colors.length; i++) {
			if (i >= customFields.length) {
				break;
			}
			// 设置自定义颜色
			customFields[i].setSelectColor(colors[i]);
		}

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(rows, columns, 4, 4));
		for (int i = 0; i < customFields.length; i++) {
			grid.add(customFields[i]);
		}
//		grid.setBorder(new EmptyBorder(4, 4, 4, 4));
		grid.setBorder(new EmptyBorder(2,2,2,2));

		// 上方
		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(0, 4));
		north.add(title, BorderLayout.NORTH);
		north.add(grid, BorderLayout.CENTER);

		// 面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(north, BorderLayout.NORTH);
		return panel;
	}

	private JPanel createLeftColorPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 4));
		p.add(createBasicPanel(), BorderLayout.CENTER);
		p.add(createCustomPanel(), BorderLayout.SOUTH);
		return p;
	}

	private JPanel createRightNorthColorPanel() {
		//		JLabel title = new JLabel(UIManager.getString("ColorDialog.PlateLabelText"));
		colorPlate = new ColorPlate();
		colorPlate.addActionListener(this);

		sildePlate = new SildePlate();
		slider = new JSlider(SwingConstants.VERTICAL, 0, 240, 0);
		
//		slider.setInverted(true);
//		slider.setSnapToTicks(true);
		
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(60);
		slider.setMinorTickSpacing(5);
		slider.addChangeListener(sliderListener);
		slider.setValueIsAdjusting(false);
	 	
		// 左侧面板
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(0, 0));
		// left.add(title, BorderLayout.NORTH);
		left.add(colorPlate, BorderLayout.CENTER);
		left.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 右侧面板
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(4, 0));
		right.add(sildePlate, BorderLayout.CENTER);
		right.add(slider, BorderLayout.WEST);
		right.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 0));
		panel.add(left, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		return panel;
	}
	
	private JLabel createLabelField(String key, char w, JTextField field, int begin, int end, KeyListener listener) {
		JLabel title = new JLabel(UIManager.getString(key));
		title.setLabelFor(field);
		title.setDisplayedMnemonic(w);
		title.setHorizontalAlignment(SwingConstants.RIGHT); // 从右向左
		
		DefaultStyledDocument def = new DefaultStyledDocument();
		def.setDocumentFilter(new ColorDocumentFilter(def, begin, end));

//		def.setDocumentFilter(new ColorDocumentFilter(def, this, field, begin, end));

//		field.setDocument(new ColorDigitDocument(field, begin, end));
		field.setDocument(def);
		if (isNimbusUI()) {
			field.setColumns(3);
		} else {
			field.setColumns(5);
		}
		field.setPreferredSize(new Dimension(20, 23));
		field.addKeyListener(listener);
//		field.addActionListener(listener);
		
		return title;
		
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(4, 0));
//		panel.add(title, BorderLayout.WEST);
//		panel.add(field, BorderLayout.CENTER);
//		return panel;
	}
	
	/** 三基色 **/
	private FlatTextField txtH = new FlatTextField();
	private FlatTextField txtS = new FlatTextField();
	private FlatTextField txtL = new FlatTextField();

	private JPanel createESLPanel() {
		ESLKeyAdapter adapter = new ESLKeyAdapter();
		
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(3, 1, 0, 4));
		left.add(createLabelField("ColorDialog.ELabelText", 'E', txtH, 0, 239, adapter));
		left.add(createLabelField("ColorDialog.SLabelText", 'S', txtS, 0, 240, adapter));
		left.add(createLabelField("ColorDialog.LLabelText", 'L', txtL, 0, 240, adapter));
		
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(3, 1, 0, 4));
		right.add(txtH);
		right.add(txtS);
		right.add(txtL);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}
	
	/** 三原色 **/
	private FlatTextField txtR = new FlatTextField();
	private FlatTextField txtG = new FlatTextField();
	private FlatTextField txtB = new FlatTextField();

	private JPanel createRGBPanel() {
		RGBKeyAdapter adapter = new RGBKeyAdapter();
		
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(3, 1, 0, 4));
		left.add(createLabelField("ColorDialog.RLabelText", 'R', txtR, 0, 255, adapter));
		left.add(createLabelField("ColorDialog.GLabelText", 'G', txtG, 0, 255, adapter));
		left.add(createLabelField("ColorDialog.BLabelText", 'B', txtB, 0, 255, adapter));

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(3, 1, 0, 4));
		right.add(txtR);
		right.add(txtG);
		right.add(txtB);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createRightFieldColorPanel() {
		JPanel panel = new JPanel();
		
		if (isNimbusUI()) {
			panel.setLayout(new BorderLayout(2, 0));
		} else {
			panel.setLayout(new BorderLayout(4, 0));
		}
		panel.add(createESLPanel(), BorderLayout.WEST);
		panel.add(createRGBPanel(), BorderLayout.EAST);
		return panel;
	}

	/**
	 * 右侧的下方面板
	 * @return
	 */
	private JPanel createRightSouthColorPanel() {
		examplePlate = new ExamplePlate();
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(4, 0));
		p.add(examplePlate, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(p, BorderLayout.WEST);
		panel.add(createRightFieldColorPanel(), BorderLayout.EAST);
		return panel;
	}

	/**
	 * 构建右侧面板
	 * @return JPanel实例
	 */
	private JPanel createRightColorPanel() {
		// 面板
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(0, 0));
		sub.add(createRightNorthColorPanel(), BorderLayout.NORTH);
		
		JPanel sub2 = new JPanel();
		sub2.setLayout(new BorderLayout(0, 0));
		sub2.add(createRightSouthColorPanel(), BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 4));
		panel.add(sub, BorderLayout.NORTH);
		panel.add(sub2 , BorderLayout.SOUTH);
		
		return panel;
	}

	private JPanel createColorPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(10, 0));
		p.setBorder(new EmptyBorder(0, 0, 0, 0));
		p.add(createLeftColorPanel(), BorderLayout.WEST);
		p.add(createRightColorPanel(), BorderLayout.EAST);
		return p;
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(createColorPanel(), BorderLayout.NORTH);
		panel.add(createBottomPanel(), BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(6, 6,6,6));
		return panel;
	}
	
	/**
	 * 设置默认的标题
	 */
	private void setDefaultTitle(){
		String title = UIManager.getString("ColorDialog.Title");
		setTitle(title);
	}

	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		// 图标
		setFrameIcon(UIManager.getIcon("ColorDialog.TitleIcon"));

		// 设置面板
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createPanel(), BorderLayout.CENTER);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#doDefaultOnShow()
	 */
	@Override
	protected void doDefaultOnShow() {
		// 设置默认值
		if (defaultColor != null) {
			setRGBTextColorValue(defaultColor);
			setESLTextColorValue(defaultColor);
			setExampleColor(defaultColor);
			setSlideColor(defaultColor);
		}
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
	 * 以模态显示窗口，返回选择颜色
	 * @param parent 父类对象
	 * @return 颜色对象，或者是空指针
	 */
	public Color showDialog(Component parent) {
		return (Color) showDialog(parent, true);
	}

}