/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.terminal.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 数据导出对话窗口，下载数据
 * @author scott.liang
 * @version 1.0 9/24/2019
 * @since laxcus 1.0
 */
public class TerminalExportEntityDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8557676657995378141L;

	class EntityStub implements Comparable<EntityStub> {

		long stub;

		String filename;

		EntityStub(String name, long id) {
			filename = name;
			stub = id;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object that) {
			if (that == null || that.getClass() != getClass()) {
				return false;
			} else if (this == that) {
				return true;
			}
			return compareTo((EntityStub) that) == 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) ((stub >>> 32) ^ stub);
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(EntityStub that) {
			return Laxkit.compareTo(stub, that.stub);
		}
	}

	/** 数据表下拉框 **/
	private JComboBox boxTable = new JComboBox();

	/** 编码文本框 **/
	private JTextArea txtStubs = new JTextArea();

	/** 选择目录 **/
	private JButton cmdPath = new JButton();

	/** 显示目录 **/
	private JLabel lblPath = new JLabel();

	/** 文件列表框 **/
	private JList lstFile = new JList();

	/** 文件显示模型 **/
	private DefaultListModel mdlFile = new DefaultListModel();

	/** 建立文件 **/
	private JButton cmdCreate = new JButton();

	/** 删除文件 **/
	private JButton cmdDelete = new JButton();

	/** 文件内容格式 **/
	private JComboBox boxType = new JComboBox();

	/** 文件内容编码 **/
	private JComboBox boxCharset = new JComboBox();

	/** 取消按纽 **/
	private JButton cmdCancel = new JButton();

	/** 确定按纽 **/
	private JButton cmdOK = new JButton();

	/** 数据块集合 **/
	private ArrayList<EntityStub> array = new ArrayList<EntityStub>();

	/** 数据结果 **/
	private String result;

	/**
	 * 构造数据导出窗口
	 * @param parent 父窗口
	 * @param modal 模式
	 */
	public TerminalExportEntityDialog(Frame parent, boolean modal) {
		super(parent, modal);
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;

		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			touch(event);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}

	/**
	 * 返回结果
	 * @return
	 */
	public String getResult() {
		return result;
	}

	/**
	 * 触发操作
	 * 
	 * @param e 激活事件
	 */
	private void touch(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			boolean success = checkAll();
			if (success) {
				fire();
				dispose();
			}
		} else if (e.getSource() == cmdCancel) {
			boolean success = exit();
			if (success) {
				result = null;
				dispose(); // 关闭
			}
		} else if (e.getSource() == cmdPath) {
			// 选择目录
			choice();
		} else if (e.getSource() == cmdCreate) {
			// 生成文件
			doCreate();
		} else if (e.getSource() == cmdDelete) {
			// 删除文件
			doDelete();
		}
	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/ExportEntity/exit/message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 参数警告！
	 */
	private void warning(){
		// 提示错误
		String title = findCaption("Dialog/ExportEntity/warning/title");
		String content = findContent("Dialog/ExportEntity/warning");
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	private File getDirectory() {
		String text = lblPath.getText();
		boolean success = (text.trim().length() > 0);
		if (success) {
			File dir = new File(text.trim());
			success = (dir.exists() && dir.isDirectory());
			if(success) {
				return dir;
			}
		}
		return null;
	}

	/**
	 * 返回表名
	 * @return
	 */
	private String getTable() {
		Object e = boxTable.getSelectedItem();
		if (e == null || e.getClass() != String.class) {
			return null;
		}
		return (String)e;
	}

	/**
	 * 返回指定类型
	 * @return
	 */
	private String getTypeName() {
		Object e = boxType.getSelectedItem();
		if (e == null || e.getClass() != String.class) {
			return null;
		}
		String type = (String) e;
		if (EntityStyle.translate(type) != EntityStyle.NONE) {
			return type;
		}
		return null;
	}

	/**
	 * 返回指定字符集
	 * @return
	 */
	private String getCharset() {
		Object who = boxCharset.getSelectedItem();
		if (who == null || who.getClass() != String.class) {
			return null;
		}
		String charset = (String) who;
		if (CharsetType.translate(charset) != CharsetType.NONE) {
			return charset;
		}
		return null;
	}

	/**
	 * 切割数据块编号
	 * @return 成功返回数组，否则是空指针
	 */
	private long[] splitStubs() {
		String text = txtStubs.getText();
		if (text.trim().length() == 0) {
			return null;
		}

		// 切割字符串
		String[] elements = text.split("\\s+|\\,");
		ArrayList<String> a = new ArrayList<String>();
		for (String e : elements) {
			e = e.trim();
			if (e.length() > 0) {
				a.add(e);
			}
		}

		// 解析生成数据块
		int size = a.size();
		long[] stubs = new long[size];
		for(int index =0; index < size; index++) {
			String input = a.get(index);
			if (!ConfigParser.isLong(input)) {
				return null;
			}
			// 解析，如果是0，是错误
			long value = ConfigParser.splitLong(input, 0);
			if (value == 0) {
				return null;
			}
			stubs[index]= value;
		}
		return stubs;
	}

	/**
	 * 判断选择了表
	 * @return 返回真或者假
	 */
	private boolean hasTable() {
		return getTable() != null;
	}

	/**
	 * 判断定义目录
	 * @return 返回真或者假
	 */
	private boolean hasPath() {
		File dir = getDirectory();
		return dir != null;
	}

	/**
	 * 判断有数据块
	 * @return 返回真或者假
	 */
	private boolean hasStubs() {
		long[] stubs = splitStubs();
		return stubs != null;
	}

	/**
	 * 判断选择了数据类型
	 * @return 返回真或者假
	 */
	private boolean hasType() {
		return getTypeName() != null;
	}

	/**
	 * 判断字符集有效
	 * @return 返回真或者假
	 */
	private boolean hasCharset() {
		return getCharset() != null;
	}

	/**
	 * 根据目录生成文件
	 * @param stub
	 * @param dir
	 * @param type
	 * @return
	 */
	private File createFile(long stub, File dir, String type) {
		String who = String.format("%x.%s", stub, type);
		File file = new File(dir, who);
		if (!file.exists()) {
			return file;
		}
		for (int index = 1; true; index++) {
			who = String.format("%x_%d.%s", stub, index, type);
			file = new File(dir, who);
			if (!file.exists()) {
				return file;
			}
		}
	}

	/**
	 * 检查所有参数已经选择
	 * @return 返回真或者假
	 */
	private boolean checkPress() {
		if (!hasTable()) {
			warning();
			boxTable.requestFocus();
			return false;
		}
		if (!hasPath()) {
			warning();
			cmdPath.requestFocus();
			return false;
		}
		if (!hasStubs()) {
			warning();
			txtStubs.requestFocus();
			return false;
		}
		if (!hasType()) {
			warning();
			boxType.requestFocus();
			return false;
		}
		if (!hasCharset()) {
			warning();
			boxCharset.requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * 检查全部参数，按下确定按纽时调用
	 * @return 返回真或者假
	 */
	private boolean checkAll() {
		boolean success = checkPress();
		if (success) {
			success = (array.size() > 0);
			if (!success) {
				warning();
				cmdCreate.requestFocus();
			}
		}
		return success;
	}

	/**
	 * 生成文件
	 */
	private void doCreate() {
		// 存在错误，退出！
		if (!checkPress()) {
			return;
		}

		// 目录
		File dir = getDirectory();
		String type = getTypeName().toLowerCase();
		long[] stubs = splitStubs();

		for (long stub : stubs) {
			File file = createFile(stub, dir, type);
			String filename = file.toString();
			EntityStub e = new EntityStub(filename, stub);
			// 如果存在，忽略它
			if (array.contains(e)) {
				continue;
			}
			array.add(e);
			// 显示在界面上
			mdlFile.addElement(filename);
		}
	}

	/**
	 * 删除选中的文件
	 */
	private void doDelete() {
		int[] list = lstFile.getSelectedIndices();
		if (list == null || list.length == 0) {
			return;
		}
		// 查找和保存
		ArrayList<EntityStub> a = new ArrayList<EntityStub>();
		for (int index : list) {
			Object e = mdlFile.elementAt(index);
			if (e.getClass() != String.class) {
				continue;
			}
			// 找到匹配文件
			String filename = (String) e;
			for (EntityStub n : array) {
				if (Laxkit.compareTo(n.filename, filename) == 0) {
					a.add(n);
					break;
				}
			}
		}
		// 删除
		for (EntityStub e : a) {
			mdlFile.removeElement(e.filename);
		}
		// 删除内存记录
		array.removeAll(a);
	}

	/**
	 * 整合生成命令输出
	 */
	private void fire() {
		String cmd = String.format("EXPORT ENTITY %s ", getTable());

		String prefix = "";
		String suffix = " TO ";

		// 生成命令
		int size = array.size();
		for (int index = 0; index < size; index++) {
			EntityStub e = array.get(index);

			prefix += String.format("0x%x", e.stub);
			suffix += String.format("%s", e.filename);

			if (index + 1 < size) {
				prefix += ",";
				suffix += ",";
			}
		}

		// 文件类型
		String type = String.format(" TYPE %s", getTypeName());
		String charset = String.format(" CHARSET %s", getCharset());

		// 保存参数
		result = cmd + prefix + suffix + type + charset;
	}

	/** 之前选择的目录 **/
	private File root;

	/**
	 * 打开窗口，选择磁盘目录
	 */
	private void choice() {
		String title = findCaption("Dialog/ExportEntity/path-chooser/title/title");
		String buttonText = findCaption("Dialog/ExportEntity/path-chooser/selected/title");

		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // .FILES_ONLY);
		chooser.setDialogTitle(title); 
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		if (root != null) {
			chooser.setCurrentDirectory(root);
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}
		// 判断目录有效，显示它。否则清除
		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isDirectory());
		if (success) {
			root = file;
			FontKit.setLabelText(lblPath, file.toString());
		} else {
			FontKit.setLabelText(lblPath, "");
		}
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return TerminalLauncher.getInstance().findCaption(xmlPath);
//	}
//
//	/**
//	 * 解析内容
//	 * @param xmlPath
//	 * @return 抽取的文本
//	 */
//	private String findContent(String xmlPath) {
//		return TerminalLauncher.getInstance().findContent(xmlPath);
//	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 560;
		int height = 386;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 建立文件格式面板
	 * @return JPanel实例
	 */
	private JPanel createTypePanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ExportEntity/label/type/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(boxType);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 0));
		panel.add(label);
		panel.add(boxType);
		return panel;
	}

	/**
	 * 建立文件编码面板
	 * @return JPanel实例
	 */
	private JPanel createCharsetPanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ExportEntity/label/charset/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(boxCharset);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 0));
		panel.add(label);
		panel.add(boxCharset);
		return panel;
	}

	/**
	 * 文件格式、文件编码、输入行数。生成一个面板
	 * @return
	 */
	private JPanel createParamPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 10));
		sub.add(createTypePanel());
		sub.add(createCharsetPanel());

		// 分左右两端位置
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.NORTH);

		return panel;
	}

	/**
	 * 建立表面板
	 * @return
	 */
	private JPanel createTablePanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ExportEntity/label/table/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(boxTable);
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(5, 0));
		sub.add(label, BorderLayout.WEST);
		sub.add(boxTable, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.WEST);
		return panel;
	}

	/**
	 * 建立数据操作按纽面板
	 * @return
	 */
	private JPanel createCreateDeleteButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 5, 0));
		panel.add(cmdCreate);
		panel.add(cmdDelete);
		return panel;
	}

	/**
	 * 建立表/建立/删除按纽的面板
	 * @return JPanel
	 */
	private JPanel createTableCreatePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(createTablePanel(), BorderLayout.EAST);
		panel.add(createCreateDeleteButtonPanel(), BorderLayout.WEST);
		return panel;
	}

	/**
	 * 路径选择
	 * @return
	 */
	private JPanel createPathPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(cmdPath, BorderLayout.WEST);
		panel.add(lblPath, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 建立左侧面板的中间部分
	 * @return JPanel
	 */
	private JPanel createLeftMiddlePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 3));
		panel.add(createPathPanel());
		panel.add(createTableCreatePanel());
		return panel;
	}

	/**
	 * 左侧面板顶部部分
	 * @return
	 */
	private JPanel createLeftTopPanel() {
		// 显示数字文本框
		JScrollPane center = new JScrollPane(txtStubs);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(center, BorderLayout.CENTER);
		panel.add(createLeftMiddlePanel(), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 建立左侧面板
	 * @return
	 */
	private JPanel createLeftPanel() {
		JScrollPane center = new JScrollPane(lstFile);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(createLeftTopPanel(), BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 生成中央面板
	 * @return
	 */
	private JPanel createCenterPanel() {
		// 左侧面板
		JPanel center = createLeftPanel();
		// 右侧面板
		JPanel right = createParamPanel();
		
		// 面板边框
		String fileText = findCaption("Dialog/ExportEntity/panel/file/title"); // "数据文件";
		String paramText = findCaption("Dialog/ExportEntity/panel/param/title"); //"数据参数";
		
//		CompoundBorder fileCB = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(3, 4, 3, 4));
//		TitledBorder fileBorder = new TitledBorder(fileCB, fileText, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
//				createTitledBorderFont(fileText));
//		center.setBorder(fileBorder);
//
//		
//		CompoundBorder paramCB = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(3, 4, 3, 4));
//		TitledBorder paramBorder = new TitledBorder(paramCB, paramText, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
//				createTitledBorderFont(paramText));
//		right.setBorder(paramBorder);

		center.setBorder(UITools.createTitledBorder(fileText));
		right.setBorder(UITools.createTitledBorder(paramText));
		
		// 生成面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 0));
		panel.add(center, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);

		return panel;
	}

	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 5, 0));
		right.add(cmdOK);
		right.add(cmdCancel);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(right, BorderLayout.EAST);

		return bottom;
	}

	/**
	 * 生成组件
	 */
	private void initControls() {
		// 文件列表
		lstFile.setCellRenderer(new TerminalFileCellRenderer());
		lstFile.setModel(mdlFile);

		// 显示三行
		txtStubs.setRows(3);
		txtStubs.setLineWrap(true);

		// 数据表，判断自有/授权。如果是授权表，判断导出权限！
		TreeSet<Space> tables = new TreeSet<Space>();
		StaffOnFrontPool staff = TerminalLauncher.getInstance().getStaffPool();
		for (Space space : staff.getSpaces()) {
			if (staff.isPassiveTable(space)) {
				boolean allow = staff.canTable(space, ControlTag.EXPORT_ENTITY);
				if (allow) {
					tables.add(space);
				}
			} else {
				tables.add(space);
			}
		}
		for (Space space : tables) {
			boxTable.addItem(space.toString());
		}

		//		// 自有表
		//		java.util.List<Space> spaces = staff.getSpaces();
		//		for (Space e : spaces) {
		//			boxTable.addItem(e.toString());
		//		}
		//		// 被授权表
		//		spaces = staff.getPassiveSpaces();
		//		for (Space e : spaces) {
		//			boolean allow = staff.canTable(e, ControlTag.EXPORT_ENTITY);
		//			if (allow) {
		//				boxTable.addItem(e.toString());
		//			}
		//		}

		// 类型
		String none = EntityStyle.translate(EntityStyle.NONE);
		boxType.addItem(none);
		String[] symbols = EntityStyle.getStrings();
		for(String e : symbols) {
			boxType.addItem(e);
		}

		// 字符集
		none = CharsetType.translate(CharsetType.NONE);
		boxCharset.addItem(none);
		symbols = CharsetType.getStrings();
		for(String e : symbols) {
			boxCharset.addItem(e);
		}

		// 提示文本
		setToolTipText(txtStubs, findCaption("Dialog/ExportEntity/text-area/stubs/tooltip"));
		// 提示文本
		setToolTipText(lstFile, findCaption("Dialog/ExportEntity/list/file/tooltip"));


		// 以下是按纽
		setButtonText(cmdPath, findCaption("Dialog/ExportEntity/buttons/path/title"));
		cmdPath.addActionListener(this);

		setButtonText(cmdCreate, findCaption("Dialog/ExportEntity/buttons/create/title"));
		cmdCreate.addActionListener(this);

		setButtonText(cmdDelete, findCaption("Dialog/ExportEntity/buttons/delete/title"));
		cmdDelete.addActionListener(this);

		setButtonText(cmdOK, findCaption("Dialog/ExportEntity/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/ExportEntity/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel initPanel() {
		initControls();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 显示对话框
	 */
	public void showDialog() {
		setContentPane(initPanel());
		
		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(220, 180));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/ExportEntity/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);
	}

}