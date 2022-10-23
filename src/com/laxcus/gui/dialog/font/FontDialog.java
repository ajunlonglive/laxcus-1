/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.font;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.gui.component.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 字体选择对话框，依赖JDesktopPane或者JInternalFrame显示
 * 
 * @author scott.liang
 * @version 1.0 6/13/2021
 * @since laxcus 1.0
 */
public class FontDialog extends LightFontDialog implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -583072256802024003L;

	private FlatTextField txtName = new FlatTextField();
	private FlatTextField txtStyle = new FlatTextField();
	private FlatTextField txtSize = new FlatTextField();

	private NameCellRenderer nameRef;
	private JList listName; 
	private DefaultListModel mdName = new DefaultListModel();

	private StyleCellRenderer styleRef;
	private JList listStyle;
	private DefaultListModel mdStyle = new DefaultListModel();

	private SizeCellRenderer sizeRef;
	private JList listSize;
	private DefaultListModel mdSize = new DefaultListModel();

	private JLabel demo = new JLabel();

	private JButton cmdOK = new JButton();
	private JButton cmdReset = new JButton();
	private JButton cmdCancel = new JButton();

	/** 默认字体 */
	private Font defaultFont;

	/** 返回字体 */
	private Font selectFont;

	/**
	 * 构造基础窗口的字体对话框
	 * @param font 字体
	 */
	public FontDialog(Font font) {
		super();
		setDefaultFont(font);
	}

	/**
	 * 构造默认的桌面字体对话框
	 */
	public FontDialog() {
		this(null);
	}

	/**
	 * 设置默认的字体
	 * @param font
	 */
	public void setDefaultFont(Font font) {
		if (font != null) {
			defaultFont = font;
		} else {
			defaultFont = getFont(); // 系统默认字体
		}
	}

	private String[] createFamilies() {
		String text = UIManager.getString("FontDialog.Hello");
		ArrayList<String> array = new ArrayList<String>();

		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
		.getAvailableFontFamilyNames();
		for (String name : names) {
			Font font = new Font(name, Font.PLAIN, 12);
			boolean success = FontKit.canDisplay(font, text);
			if (success) {
				array.add(name);
			}
		}

		Collections.sort(array);
		String[] families = new String[array.size()];
		array.toArray(families);

		String[] a = new String[array.size()];
		return array.toArray(a);
	}

	/**
	 * 定义字体样式
	 */
	private FontStyle[] createStyles() {
		String plain = UIManager.getString("FontDialog.Plain");
		String bold = UIManager.getString("FontDialog.Bold");
		String italic = UIManager.getString("FontDialog.Italic");
		String boldItalic = UIManager.getString("FontDialog.BoldItalic");

		ArrayList<FontStyle> array = new ArrayList<FontStyle>();

		array.add(new FontStyle(Font.PLAIN, plain));
		array.add(new FontStyle(Font.BOLD, bold));
		array.add(new FontStyle(Font.ITALIC, italic));
		array.add(new FontStyle(Font.BOLD | Font.ITALIC, boldItalic));

		FontStyle[] a = new FontStyle[array.size()];
		return array.toArray(a);
	}

	/**
	 * 保存范围
	 */
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "FontDialog/Bound", rect);
		}
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "FontDialog/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension size = PlatformKit.getPlatformDesktop().getSize(); // Toolkit.getDefaultToolkit().getScreenSize();

		int width = 530; 
		int height = 380; 

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
		super.setDefaultBounds(dlg, parent);
	}

	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		// 标题
		String title = UIManager.getString("FontDialog.Title");
		setTitle(title);
		// 图标
		setFrameIcon(UIManager.getIcon("FontDialog.TitleIcon"));

		// 设置面板
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(initPanel(), BorderLayout.CENTER);
	}

	private void redefine() {
		demo.setFont(defaultFont);

		// 找到匹配字体
		String name = defaultFont.getName();
		int size = mdName.size();
		for (int i = 0; i < size; i++) {
			String str = (String) mdName.getElementAt(i);
			if (name.equals(str)) {
				listName.setSelectedValue(str, true);
				break;
			}
		}

		// 找到样式
		int style = defaultFont.getStyle();
		size = mdStyle.size();
		for (int i = 0; i < size; i++) {
			FontStyle fs = (FontStyle) mdStyle.getElementAt(i);
			if (fs.getStyle() == style) {
				listStyle.setSelectedValue(fs, true);
				break;
			}
		}

		// 字体值
		int bold = defaultFont.getSize();
		size = mdSize.size();
		for (int i = 0; i < size; i++) {
			Integer rt = (Integer) mdSize.getElementAt(i);
			if (rt.intValue() == bold) {
				listSize.setSelectedValue(rt, true);
				break;
			}
		}
	}

	/**
	 * 重置字体
	 * @param font
	 */
	private void reset(Font font) {
		if (font != null) {
			defaultFont = font;
			redefine();
		} ;
	}

	/**
	 * 按纽触发的单击方法
	 */
	private void resetX() {
		if (defaultFont != null) {
			redefine();
		}
	}

	/**
	 * 建立按钮面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		setButtonText(cmdOK, UIManager.getString("FontDialog.OkayButtonText"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdReset, UIManager.getString("FontDialog.ResetButtonText"));
		cmdReset.setMnemonic('R');
		cmdReset.addActionListener(this);

		setButtonText(cmdCancel, UIManager.getString("FontDialog.CancelButtonText"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 6, 0));
		right.add(cmdOK);
		right.add(cmdCancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(cmdReset, BorderLayout.WEST);
		panel.add(right, BorderLayout.EAST);

		return panel;
	}

	/**
	 * 建立字体演示面板
	 * @return
	 */
	private JPanel createDemoPanel() {
		String welcome = UIManager.getString("FontDialog.Hello");
		setLabelText(demo, welcome);
		demo.setHorizontalAlignment(SwingConstants.CENTER);
		demo.setHorizontalTextPosition(SwingConstants.CENTER);

		JScrollPane jsp = new JScrollPane(demo);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
		// 重新定义背景
		Color c = UIManager.getColor("Panel.background");
		c = new Color(c.getRGB());
		jsp.getViewport().setBackground(c);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(UITools.createTitledBorder(null, 5));
		panel.setPreferredSize(new Dimension(100, 82));
		panel.add(jsp, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 建立字体选择面板
	 * @return
	 */
	private JPanel createFontPanel() {
		String str = UIManager.getString("FontDialog.CellHeight");
		int cellHeight = ConfigParser.splitInteger(str, 28);

		// 加入
		String[] families = createFamilies();
		for (String name : families) {
			mdName.addElement(name);
		}
		listName = new JList(mdName); //.setModel(mdName);
		listName.setCellRenderer(nameRef = new NameCellRenderer());
		listName.setFixedCellHeight(cellHeight);
		listName.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listName.setBorder(new EmptyBorder(1, 1, 1, 1));
		
		// 字体样式
		FontStyle[] fss = createStyles();
		for (FontStyle fs : fss) {
			mdStyle.addElement(fs);
		}
		listStyle = new JList(mdStyle); // .setModel(mdStyle);
		listStyle.setCellRenderer(styleRef = new StyleCellRenderer());
		listStyle.setFixedCellHeight(cellHeight);
		listStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listStyle.setBorder(new EmptyBorder(1, 1, 1, 1));
		
		// 字体尺寸
		int begin = 6, end = 90;
		// String[] sizes = new String[end - begin + 1];
		for (int i = begin; i <= end; i++) {
			mdSize.addElement(new Integer(i));
		}
		listSize = new JList(mdSize); //.setModel(mdSize);
		listSize.setCellRenderer(sizeRef = new SizeCellRenderer());
		listSize.setFixedCellHeight(cellHeight);
		listSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSize.setBorder(new EmptyBorder(1, 1, 1, 1));

		listName.addListSelectionListener(this);
		listStyle.addListSelectionListener(this);
		listSize.addListSelectionListener(this);

		txtName.setEditable(false);
		txtStyle.setEditable(false);
		txtSize.setEditable(false);
		txtName.setPreferredSize(new Dimension(20, 30));
		txtStyle.setPreferredSize(new Dimension(20, 30));
		txtSize.setPreferredSize(new Dimension(20, 30));

		JLabel name = new JLabel();
		setLabelText(name, UIManager.getString("FontDialog.Name"));
		name.setPreferredSize(new Dimension(10, 26));
		name.setBorder(new EmptyBorder(0, 6, 0, 0));

		JLabel style = new JLabel();
		setLabelText(style, UIManager.getString("FontDialog.Style"));
		style.setPreferredSize(new Dimension(10, 26));
		style.setBorder(new EmptyBorder(0, 6, 0, 0));

		JLabel size = new JLabel();
		setLabelText(size, UIManager.getString("FontDialog.Size"));
		size.setPreferredSize(new Dimension(10, 26));
		size.setBorder(new EmptyBorder(0, 6, 0, 0));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(2, 1, 0, 0));
		p1.add(name);
		p1.add(txtName);

		JPanel p11 = new JPanel();
		p11.setLayout(new BorderLayout(0, 5));
		p11.add(p1, BorderLayout.NORTH);
		JScrollPane jsp = new JScrollPane(listName);
		jsp.setBorder(new HighlightBorder(1));
		p11.add(jsp, BorderLayout.CENTER);
		
//		p11.add(new JScrollPane(listName), BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 1, 0, 0));
		p2.add(style);
		p2.add(txtStyle);

		JPanel p22 = new JPanel();
		p22.setLayout(new BorderLayout(0, 5));
		p22.add(p2, BorderLayout.NORTH);
		jsp = new JScrollPane(listStyle);
		jsp.setBorder(new HighlightBorder(1));
		p22.add(jsp, BorderLayout.CENTER);
		
//		p22.add(new JScrollPane(listStyle), BorderLayout.CENTER);

		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(2, 1, 0, 0));
		p3.add(size);
		p3.add(txtSize);

		JPanel p33 = new JPanel();
		p33.setLayout(new BorderLayout(0, 5));
		p33.add(p3, BorderLayout.NORTH);
		jsp = new JScrollPane(listSize);
		jsp.setBorder(new HighlightBorder(1));
		p33.add(jsp, BorderLayout.CENTER);
		
//		p33.add(new JScrollPane(listSize), BorderLayout.CENTER);

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

	/**
	 * 激活操作
	 * @param e
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			if (selectFont != null) {
				writeBounds();
				// 设置选中的字体。注意！关闭窗口和解除模态的工作，让父类去执行
				setSelectedValue(selectFont);
			}
		} else if (e.getSource() == cmdReset) {
			resetX(); // 重置字体
		} else if (e.getSource() == cmdCancel) {
			writeBounds();
			setSelectedValue(null);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		click(e);
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
			int index = listName.getSelectedIndex();
			if (index >= 0) {
				String name = (String) mdName.getElementAt(index);
				setFieldText(txtName, name);
				select();
			}
		} else if (source == listStyle) {
			int index = listStyle.getSelectedIndex();
			if (index >= 0) {
				FontStyle fs = (FontStyle) mdStyle.getElementAt(index);
				if (fs != null) {
					setFieldText(txtStyle, fs.getName());
					select();
				}
			}
		} else if (source == listSize) {
			int index = listSize.getSelectedIndex();
			if (index >= 0) {
				Integer v = (Integer) mdSize.getElementAt(index);
				if (v != null) {
					setFieldText(txtSize, String.format("%d", v.intValue()));
					select();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		exchange(e);
	}

	/**
	 * 选择新字体
	 */
	private void select() {
		String name = null;
		Integer style = null;
		Integer size = null;

		// 字体名称
		int index = listName.getSelectedIndex();
		if (index >= 0) {
			name = (String) mdName.getElementAt(index);
		}
		// 字体样式
		index = listStyle.getSelectedIndex();
		if (index >= 0) {
			FontStyle fs = (FontStyle) mdStyle.getElementAt(index);
			if (fs != null) {
				style = new Integer(fs.style);
			}
		}
		// 字体尺寸
		index = listSize.getSelectedIndex();
		if (index >= 0) {
			size = (Integer) mdSize.getElementAt(index);
		}

		// 任意一个无效都忽略！
		boolean success = (name != null && style != null && size != null);
		if (success) {
			selectFont = new Font(name, style.intValue(), size.intValue());
			demo.setFont(selectFont);
		}

	}

	/**
	 * 返回选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return  selectFont;
	}

	/**
	 * @param parent 上级组件对象，可以是JInternalFrame或者JDesktopPane
	 * @param modal 模态或者否
	 * @return 返回输出结果，具体由子类去解释
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		initDialog();
		reset(defaultFont);

		// 检查对话框中的字体
		checkDialogFonts();

		// 范围
		setBounds(parent);

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		// 非模态，或者模态
		if (!modal) {
			// 非模态显示的窗口
			return showNormalDialog(parent);
		} else {
			return showModalDialog(parent);
		}
	}

	/**
	 * 生成对话框
	 * @param parent
	 * @return
	 */
	public Font showDialog(Component parent) {
		return (Font) showDialog(parent, true);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新UI
		if (nameRef != null) {
			nameRef.updateUI();
		}
		if (styleRef != null) {
			styleRef.updateUI();
		}
		if (sizeRef != null) {
			sizeRef.updateUI();
		}
	}

}