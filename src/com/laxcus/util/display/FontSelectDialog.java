/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.util.event.*;
import com.laxcus.util.local.*;
import com.laxcus.util.skin.*;

/**
 * 字体选择窗口。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/29/2009
 * @since laxcus 1.0
 */
public class FontSelectDialog extends CommonFontDialog implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -8311924281855037879L;

	/** 字体样式 -> 字符串描述 **/
	private Map<Integer, String> styles = new TreeMap<Integer, String>();

	private JTextField txtName = new JTextField();
	private JTextField txtStyle = new JTextField();
	private JTextField txtSize = new JTextField();

	private JList listName = new JList();
	private JList listStyle = new JList();
	private JList listSize = new JList();

	private JLabel demo = new JLabel();

	private JButton cmdOK = new JButton();
	private JButton cmdReset = new JButton();
	private JButton cmdCancel = new JButton();

	/** 默认字体 */
	private Font defaultFont;

	/** 返回字体 */
	private Font selectFont;

	/** 判断用户选择了新字体 */
	private boolean selected = false;

	/** 本地文本匹配器 **/
	private LocalMatcher localMatcher;

	/**
	 * 查询XML中的匹配文本
	 * @param xmlPath
	 * @return
	 */
	private String findCaption(String xmlPath) {
		return localMatcher.findCaption(xmlPath);
	}

	/**
	 * 构造基础窗口的字体对话框
	 * @param frame 窗口句柄
	 * @param modal 是否模态
	 * @param matcher 本地文本匹配器
	 * @param font 字体
	 */
	public FontSelectDialog(JFrame frame, boolean modal, LocalMatcher matcher, Font font) {
		super(frame, modal);

		if (font != null) {
			defaultFont = font;
		} else {
			defaultFont = getFont(); // 系统默认字体
		}
		// 匹配器
		localMatcher = matcher;

		initStyle();
		initDialog();
		reset(font);
		
		// 检查对话框中的字体
		checkDialogFonts();
	}

	/**
	 * 定义字体样式
	 */
	private void initStyle() {
		String plain = findCaption("Dialog/Font/Style/Plain/title");
		String bold = findCaption("Dialog/Font/Style/Bold/title");
		String italic = findCaption("Dialog/Font/Style/Italic/title");
		String boldItalic = findCaption("Dialog/Font/Style/BoldItalic/title");

		styles.put(Font.PLAIN, plain);
		styles.put(Font.BOLD, bold);
		styles.put(Font.ITALIC, italic);
		styles.put(Font.BOLD | Font.ITALIC, boldItalic);
	}

	/**
	 * 根据关键字，查找对应的字体样式 
	 * @param word
	 * @return
	 */
	private int findStyle(String word) {
		Iterator<Map.Entry<Integer, String>> iterator = styles.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, String> entry = iterator.next();
			if (entry.getValue().equals(word)) {
				return entry.getKey();
			}
		}
		return 0;
	}

	/**
	 * 根据字体样式编号，查找它对应的字符串
	 * @param style
	 * @return
	 */
	private String findStyleWord(int style) {
		Iterator<Map.Entry<Integer, String>> iterator = styles.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, String> entry = iterator.next();
			if (entry.getKey() == style) {
				return entry.getValue();
			}
		}
		return "";
	}

	/**
	 * 定义范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 530; // (int) (size.getWidth() * 0.4);
		int height = 380; // (int) (size.getHeight() * 0.5);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		// 标题
		String title = findCaption("Dialog/Font/Title/title");
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// 主面板
		JPanel panel = initPanel();
		Container canvas = getContentPane();
		canvas.setLayout(new BorderLayout(0, 0));
		canvas.add(panel, BorderLayout.CENTER);

		// 位置
		Rectangle rect = getBound();
		setBounds(rect);
		setMinimumSize(rect.getSize());

		listName.setSelectedValue(txtName.getText(), true);
	}

//	/**
//	 * 重置字体
//	 * @param font
//	 */
//	private void reset(Font font) {
//		if (font != null) {
//			defaultFont = font;
//		}
//
//		demo.setFont(defaultFont);
//		listName.setSelectedValue(defaultFont.getName(), true);
//		listStyle.setSelectedValue(findStyleWord(defaultFont.getStyle()), true);
//		listSize.setSelectedValue(new Integer(defaultFont.getSize()).toString(), true);
//	}
	
	/**
	 * 重置字体
	 * @param font
	 */
	private void reset(Font font) {
		if (font != null) {
			defaultFont = font;
		}
		
//		String family = font.getFamily();

		demo.setFont(defaultFont);
		listName.setSelectedValue(font.getName(), true);
		listStyle.setSelectedValue(findStyleWord(defaultFont.getStyle()), true);
		listSize.setSelectedValue(new Integer(defaultFont.getSize()).toString(), true);
	}
	
	/**
	 * 建立按钮面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		setButtonText(cmdOK, findCaption("Dialog/Font/Button/Okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdReset, findCaption("Dialog/Font/Button/Reset/title"));
		cmdReset.setMnemonic('R');
		cmdReset.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/Font/Button/Cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 3, 6, 0));
		right.add(cmdOK);
		right.add(cmdReset);
		right.add(cmdCancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);

		return panel;
	}
	
	/**
	 * 建立字体演示面板
	 * @return
	 */
	private JPanel createDemoPanel() {
		String welcome = findCaption("Dialog/Font/Welcome/title");
		setLabelText(demo, welcome);
		demo.setHorizontalAlignment(SwingConstants.CENTER);
		demo.setHorizontalTextPosition(SwingConstants.CENTER);

		JScrollPane jsp = new JScrollPane(demo);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(UITools.createTitledBorder(null, 5));
		panel.setPreferredSize(new Dimension(80, 100));
		panel.add(jsp, BorderLayout.CENTER);

		return panel;
	}

//	/**
//	 * 建立字体选择面板
//	 * @return
//	 */
//	private JPanel createFontPanel() {
//		// 全部字体
//		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//		listName.setListData(names);
//
//		// 字体样式
//		String[] styleWords = new String[styles.size()];
//		styles.values().toArray(styleWords);
//		
//		// 找到匹配的字体
//		Font sub = FontKit.findFont(listStyle.getFont(), styleWords[0]);
//		if (sub != null && !sub.equals(listStyle.getFont())) {
//			listStyle.setFont(sub);
//		}
//
//		// 设置字体样式
//		listStyle.setListData(styleWords);
//		
//		// 字体尺寸
//		int begin = 6, end = 90;
//		String[] sizes = new String[end - begin + 1];
//		for (int i = 0; i < sizes.length; i++) {
//			sizes[i] = String.format("%d", begin++);
//		}
//		listSize.setListData(sizes);
//
//		listName.addListSelectionListener(this);
//		listStyle.addListSelectionListener(this);
//		listSize.addListSelectionListener(this);
//
//		txtName.setEditable(false);
//		txtStyle.setEditable(false);
//		txtSize.setEditable(false);
//
//		setFieldText(txtName, defaultFont.getName());
//		txtSize.setColumns(4);
//		
//		JLabel name = new JLabel();
//		setLabelText(name, findCaption("Dialog/Font/List/Name/title"));
//		name.setBorder(new EmptyBorder(0, 6, 0, 0));
//
//		JLabel style = new JLabel();
//		setLabelText(style, findCaption("Dialog/Font/List/Style/title"));
//		style.setBorder(new EmptyBorder(0, 6, 0, 0));
//
//		JLabel size = new JLabel();
//		setLabelText(size, findCaption("Dialog/Font/List/Size/title"));
//		size.setBorder(new EmptyBorder(0, 6, 0, 0));
//
//		JPanel p1 = new JPanel();
//		p1.setLayout(new GridLayout(2, 1, 0, 0));
//		p1.add(name);
//		p1.add(txtName);
//
//		JPanel p11 = new JPanel();
//		p11.setLayout(new BorderLayout(0, 5));
//		p11.add(p1, BorderLayout.NORTH);
//		p11.add(new JScrollPane(listName), BorderLayout.CENTER);
//
//		JPanel p2 = new JPanel();
//		p2.setLayout(new GridLayout(2, 1, 0, 0));
//		p2.add(style);
//		p2.add(txtStyle);
//
//		JPanel p22 = new JPanel();
//		p22.setLayout(new BorderLayout(0, 5));
//		p22.add(p2, BorderLayout.NORTH);
//		p22.add(new JScrollPane(listStyle), BorderLayout.CENTER);
//
//		JPanel p3 = new JPanel();
//		p3.setLayout(new java.awt.GridLayout(2, 1, 0, 0));
//		p3.add(size);
//		p3.add(txtSize);
//
//		JPanel p33 = new JPanel();
//		p33.setLayout(new BorderLayout(0, 5));
//		p33.add(p3, BorderLayout.NORTH);
//		p33.add(new JScrollPane(listSize), BorderLayout.CENTER);
//
//		JPanel root = new JPanel();
//		root.setLayout(new GridLayout(1, 3, 8, 0));
//		root.add(p11);
//		root.add(p22);
//		root.add(p33);
//
//		return root;
//	}

//	/** 字体族 -> 名称 **/
//	private Map<String, String> fontFamilies = new TreeMap<String, String>();

	/**
	 * 建立字体选择面板
	 * @return
	 */
	private JPanel createFontPanel() {
		// 查找可显示的字体
		String text = findCaption("Window/Frame/title");
		ArrayList<String> a = new ArrayList<String>();
		
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(String name : names) {
			Font font = new Font(name, Font.PLAIN, 12);
			boolean success = FontKit.canDisplay(font, text);
			if (!success) {
				continue;
			}
			// 保存这个字体
			a.add(name);
		}

//		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//		for (Font font : fonts) {
//			boolean success = FontKit.canDisplay(font, text);
//			if (!success) {
//				continue;
//			}
//			String family = font.getFamily();
//			String name = font.getName();
//			System.out.printf("字族：%s 名称：%s\n", family, name);
//			
//			// 不存在保存
//			if (!fontFamilies.containsKey(family)) {
//				fontFamilies.put(family, name);
//				a.add(family);
//			}
//		}
		
		Collections.sort(a);
		String[] families = new String[a.size()];
		a.toArray(families);
		listName.setListData(families);

		// 字体样式
		String[] styleWords = new String[styles.size()];
		styles.values().toArray(styleWords);

		// 找到匹配的字体
		Font sub = FontKit.findFont(listStyle.getFont(), styleWords[0]);
		if (sub != null && !sub.equals(listStyle.getFont())) {
			listStyle.setFont(sub);
		}

		// 设置字体样式
		listStyle.setListData(styleWords);

		// 字体尺寸
		int begin = 6, end = 90;
		String[] sizes = new String[end - begin + 1];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = String.format("%d", begin++);
		}
		listSize.setListData(sizes);

		listName.addListSelectionListener(this);
		listStyle.addListSelectionListener(this);
		listSize.addListSelectionListener(this);

		txtName.setEditable(false);
		txtStyle.setEditable(false);
		txtSize.setEditable(false);

		setFieldText(txtName, defaultFont.getName());
		txtSize.setColumns(4);

		JLabel name = new JLabel();
		setLabelText(name, findCaption("FontDialog.Name"));
		name.setBorder(new EmptyBorder(0, 6, 0, 0));

		JLabel style = new JLabel();
		setLabelText(style, findCaption("FontDialog.Style"));
		style.setBorder(new EmptyBorder(0, 6, 0, 0));

		JLabel size = new JLabel();
		setLabelText(size, findCaption("FontDialog.Size"));
		size.setBorder(new EmptyBorder(0, 6, 0, 0));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(2, 1, 0, 0));
		p1.add(name);
		p1.add(txtName);

		JPanel p11 = new JPanel();
		p11.setLayout(new BorderLayout(0, 5));
		p11.add(p1, BorderLayout.NORTH);
		p11.add(new JScrollPane(listName), BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 1, 0, 0));
		p2.add(style);
		p2.add(txtStyle);

		JPanel p22 = new JPanel();
		p22.setLayout(new BorderLayout(0, 5));
		p22.add(p2, BorderLayout.NORTH);
		p22.add(new JScrollPane(listStyle), BorderLayout.CENTER);

		JPanel p3 = new JPanel();
		p3.setLayout(new java.awt.GridLayout(2, 1, 0, 0));
		p3.add(size);
		p3.add(txtSize);

		JPanel p33 = new JPanel();
		p33.setLayout(new BorderLayout(0, 5));
		p33.add(p3, BorderLayout.NORTH);
		p33.add(new JScrollPane(listSize), BorderLayout.CENTER);

		JPanel root = new JPanel();
		root.setLayout(new GridLayout(1, 3, 8, 0));
		root.add(p11);
		root.add(p22);
		root.add(p33);

		return root;
	}
	
	/**
	 * 建立面板
	 * @return
	 */
	private JPanel initPanel() {
		// 底部面板，包括显示字体和按纽
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 8));
		bottom.add(createDemoPanel(), BorderLayout.CENTER);
		bottom.add(createButtonPanel(), BorderLayout.SOUTH);

		// 主面板
		JPanel root = new JPanel();
		root.setLayout(new BorderLayout(0, 8));
		root.setBorder(new EmptyBorder(5, 5, 5, 5));
		root.add(createFontPanel(), BorderLayout.CENTER);
		root.add(bottom, BorderLayout.SOUTH);
		return root;
	}

	class ClickThread extends SwingEvent {
		ActionEvent event;

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	/**
	 * 激活操作
	 * @param e
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			selected = true;
			closeDialog(); // 隐藏窗口
		} else if (e.getSource() == cmdReset) {
			reset(null); // 重置字体
		} else if (e.getSource() == cmdCancel) {
			closeDialog(); // 隐藏窗口
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ClickThread(e));
	}

	class ChangeThread extends SwingEvent {
		ListSelectionEvent event;

		ChangeThread(ListSelectionEvent e) {
			super();
			event = e;
		}

		public void process() {
			exchange(event);
		}
	}

	/**
	 * 调整字体
	 * @param e
	 */
	private void exchange(ListSelectionEvent e) {
		Object source = e.getSource();
		if (source == null) {
			return;
		}
		
		if (source == listName) {
			String name = (String) listName.getSelectedValue();
			setFieldText(txtName, name);
			select();
		} else if (source == listStyle) {
			String style = (String) listStyle.getSelectedValue();
			setFieldText(txtStyle, style);
			select();
		} else if (source == listSize) {
			String size = (String) listSize.getSelectedValue();
			setFieldText(txtSize, size);
			select();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		addThread(new ChangeThread(e));
	}

	/**
	 * 选择新字体
	 */
	private void select() {
		// 提取参数
		String name = (String) listName.getSelectedValue();
		if (name == null || name.isEmpty()) {
			name = txtName.getText();
		}
		
//		// 提取名称
//		String familyText = (String) listName.getSelectedValue();
//		if (familyText == null || familyText.isEmpty()) {
//			familyText = txtName.getText();
//		}
//		String name = fontFamilies.get(familyText);
		
		String styleText = (String) listStyle.getSelectedValue();
		if (styleText == null || styleText.isEmpty()) {
			styleText = txtStyle.getText();
		}
		String sizeText = (String) listSize.getSelectedValue();
		if (sizeText == null || sizeText.isEmpty()) {
			sizeText = txtSize.getText();
		}

		// 任意一个无效都忽略！
		if (name == null || name.isEmpty() || styleText == null || styleText.isEmpty()
				|| sizeText == null || sizeText.isEmpty()) {
			return;
		}

		int style = findStyle(styleText);
		selectFont = new Font(name, style, Integer.parseInt(sizeText));
		demo.setFont(selectFont);
		
//		System.out.printf("选中字体：%s\n", selectFont);
	}

	/**
	 * 隐藏窗口。销毁窗口的工作在外面执行。
	 */
	private void closeDialog() {
		setVisible(false);
	}

	/**
	 * 判断是确认
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * 返回选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return (selected ? selectFont : null);
	}

}