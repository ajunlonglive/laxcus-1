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
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 数据导入对话窗口，数据上传
 * 
 * @author scott.liang
 * @version 1.0 9/24/2019
 * @since laxcus 1.0
 */
public class TerminalImportEntityDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 5313651104510165780L;

	/** 文件列表框 **/
	private JList lstFile = new JList();

	/** 文件显示模型 **/
	private DefaultListModel mdlFile = new DefaultListModel();
	
	/** 数据表下拉框 **/
	private JComboBox boxTable = new JComboBox();

	/** 选择文件 **/
	private JButton cmdChoose = new JButton();
	
	/** 删除列表上的文件 **/
	private JButton cmdDelete = new JButton();
	
	/** 文件内容格式 **/
	private JComboBox boxType = new JComboBox();
	
	/** 文件内容编码 **/
	private JComboBox boxCharset = new JComboBox();
	
	/** 写入行数 **/
	private JTextField txtLine = new JTextField();
	
	/** 取消按纽 **/
	private JButton cmdCancel = new JButton();

	/** 确定按纽 **/
	private JButton cmdOK = new JButton();
	
	/** 文件记录 ！**/
	private ArrayList<String> array = new ArrayList<String>();
	
	/** 数据结果 **/
	private String result;

	/**
	 * 构造数据导入窗口
	 * @param parent 父窗口
	 * @param modal 模态
	 */
	public TerminalImportEntityDialog(Frame parent, boolean modal) {
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
	 * @param e
	 */
	private void touch(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			// 关闭
			boolean success = check();
			if (success) {
				fire();
				dispose();
			}
		} else if (e.getSource() == cmdCancel) {
			// 关闭
			boolean success = exit();
			if (success) {
				result = null;
				dispose();
			}
		} else if (e.getSource() == cmdChoose) {
			chooseFiles();
		} else if (e.getSource() == cmdDelete) {
			deleteFiles();
		}
	}
	
	/**
	 * 检查参数
	 * @return 成功返回真，否则假
	 */
	private boolean check() {
		// 数据表
		if (!hasTable()) {
			boxTable.requestFocus();
			warning();
			return false;
		}
		// 文件类型
		if (!hasType()) {
			boxType.requestFocus();
			warning();
			return false;
		}
		// 文件集
		if (array.isEmpty()) {
			cmdChoose.requestFocus();
			warning();
			return false;
		}

		return true;
	}
	
	/**
	 * 参数警告！
	 */
	private void warning(){
		// 提示错误
		String title = findCaption("Dialog/ImportEntity/warning/title");
		String content = findContent("Dialog/ImportEntity/warning");
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	
	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/ImportEntity/exit/message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}
	
	/**
	 * 返回表名
	 * @return 表名，或者空指针
	 */
	private String getTable() {
		Object e = boxTable.getSelectedItem();
		if (e == null || e.getClass() != String.class) {
			return null;
		}
		return (String) e;
	}

	/**
	 * 判断定义表名
	 * @return
	 */
	private boolean hasTable() {
		return getTable() != null;
	}
	
	/**
	 * 返回文件类型
	 * @return 文件类型字符串，没有定义返回空指针
	 */
	private String getTypeName() {
		// 文件类型
		String type = (String) boxType.getSelectedItem();
		if (EntityStyle.translate(type) != EntityStyle.NONE) {
			return type;
		}
		return null;
	}
	
	/**
	 * 判断定义了文件类型
	 * @return 真或者假
	 */
	private boolean hasType() {
		return getTypeName() != null;
	}
	
	/**
	 * 返回文件编码
	 * @return 文件编码字符串，没有定义返回空指针
	 */
	private String getCharset() {
		String charset = (String) boxCharset.getSelectedItem();
		if (CharsetType.translate(charset) != CharsetType.NONE) {
			return charset;
		}
		return null;
	}

	/**
	 * 整合生成命令输出
	 */
	private void fire() {
		String cmd = String.format("IMPORT ENTITY %s FROM ", getTable());
		int size = array.size();
		for (int index = 0; index < size; index++) {
			cmd += array.get(index);
			if (index + 1 < size) {
				cmd += " , ";
			}
		}

		// 文件类型
		cmd += String.format(" TYPE %s", getTypeName());
		// 文件编码
		String charset = getCharset();
		if(charset != null) {
			cmd += String.format(" CHARSET %s", charset);
		}

		// 单次写入行数
		String section = txtLine.getText();
		if (section.length() > 0) {
			cmd += String.format(" SECTION %s", section);
		}

		// 保存参数
		result = cmd;
	}
	
	/** 最后一次的目录 **/
	private File root;

	/** 选择的文件类型选项 **/
	private static String selectType;

	/**
	 * 打开窗口，选择文件
	 */
	private void chooseFiles() {
		String title = findCaption("Dialog/ImportEntity/file-chooser/title/title");
		String buttonText = findCaption("Dialog/ImportEntity/file-chooser/selected/title");
		
		// TEXT文件
		String ds_text = findCaption("Dialog/ImportEntity/file-chooser/text/description/title");
		String text = findCaption("Dialog/ImportEntity/file-chooser/text/extension/title");
		// CSV文件
		String ds_csv = findCaption("Dialog/ImportEntity/file-chooser/csv/description/title");
		String csv = findCaption("Dialog/ImportEntity/file-chooser/csv/extension/title");
		
		DiskFileFilter f1 = new DiskFileFilter(ds_text, text);
		DiskFileFilter f2 = new DiskFileFilter(ds_csv, csv);

		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(f1);
		chooser.addChoosableFileFilter(f2);
		// 找到选项
		chooseFileFilter(chooser, selectType);
		
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title); 
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText); 
		// 目录
		if (root != null) {
			chooser.setCurrentDirectory(root);
		}
		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		selectType = saveFileFileter(chooser);
		File[] files = chooser.getSelectedFiles();
		// 显示和保存文件
		int size = (files == null ? 0 : files.length);
		for (int index = 0; index < size; index++) {
			File file = files[index];
			// 文件不存在，忽略它
			boolean success = (file.exists() && file.isFile());
			if (!success) {
				continue;
			}
			
			String filename = file.toString();
			if (array.contains(filename)) {
				continue;
			}
			array.add(filename);
			// 显示在界面上
			mdlFile.addElement(filename);
			// 目录
			root = file.getParentFile();
		}
	}
	
	/**
	 * 删除已经选中的文件
	 */
	private void deleteFiles() {
		int[] list = lstFile.getSelectedIndices();
		if (list == null || list.length == 0) {
			return;
		}
		// 保存！
		ArrayList<String> a = new ArrayList<String>();
		for (int index : list) {
			Object e = mdlFile.elementAt(index);
			if (e.getClass() == String.class) {
				a.add((String) e);
			}
		}
		// 删除
		for (String e : a) {
			mdlFile.removeElement(e);
		}
		// 删除内存记录
		array.removeAll(a);
	}
	
//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return
//	 */
//	private String getCaption(String xmlPath) {
//		return TerminalLauncher.getInstance().findCaption(xmlPath);
//	}
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
	 * 初始化组件
	 */
	private void initControls() {
		// 文件列表
		lstFile.setCellRenderer(new TerminalFileCellRenderer());
		lstFile.setModel(mdlFile);
		
//		boxTable.addItem("SHAREDB.DSM");
		
		TreeSet<Space> tables = new TreeSet<Space>();
		
		// 判断是自有表/授权表，如果是授权表判断权限！
		StaffOnFrontPool staff = TerminalLauncher.getInstance().getStaffPool();
		for (Space space : staff.getSpaces()) {
			if (staff.isPassiveTable(space)) {
				boolean allow = staff.canTable(space, ControlTag.IMPORT_ENTITY);
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
		//			boolean allow = staff.canTable(e, ControlTag.IMPORT_ENTITY);
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
		
		// 默认行数，1000
		txtLine.setDocument(new DigitDocument(txtLine, 6));
		txtLine.setText("1000");
		
		// 提示文本
		setToolTipText(lstFile, findCaption("Dialog/ImportEntity/list/file/tooltip"));
		
		
		setButtonText(cmdChoose, findCaption("Dialog/ImportEntity/buttons/choose/title"));
		cmdChoose.addActionListener(this);
		
		setButtonText(cmdDelete, findCaption("Dialog/ImportEntity/buttons/delete/title"));
		cmdDelete.addActionListener(this);
		
		setButtonText(cmdOK, findCaption("Dialog/ImportEntity/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/ImportEntity/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
	}
	
	/**
	 * 建立数据文件面板
	 * @return JPanel
	 */
	private JPanel createFilePanel() {
		// 上部面板
		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(2, 0));
		north.add(createTablePanel(), BorderLayout.WEST);
		north.add(createFileButtonPanel(), BorderLayout.EAST);

		// 整个面板
		JScrollPane bottom = new JScrollPane(lstFile);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(north, BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 建立数据操作按纽面板
	 * @return
	 */
	private JPanel createFileButtonPanel() {
		JPanel north = new JPanel();
		north.setLayout(new GridLayout(1, 2, 10, 0));
		north.add(cmdChoose);
		north.add(cmdDelete);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(north, BorderLayout.EAST);
		return panel;
	}
	
	/**
	 * 建立表面板
	 * @return
	 */
	private JPanel createTablePanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ImportEntity/label/table/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(boxTable);
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(8, 0));
		sub.add(label, BorderLayout.WEST);
		sub.add(boxTable, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.WEST);
		return panel;
	}

	/**
	 * 建立文件格式面板
	 * @return JPanel实例
	 */
	private JPanel createTypePanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ImportEntity/label/type/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		//	label.setDisplayedMnemonic('S');

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
		setLabelText(label, findCaption("Dialog/ImportEntity/label/charset/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(boxCharset);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 0));
		panel.add(label);
		panel.add(boxCharset);
		return panel;
	}
	
	/**
	 * 建立文件行数选择面板
	 * @return JPanel实例
	 */
	private JPanel createLinePanel() {
		JLabel label = new JLabel();
		setLabelText(label, findCaption("Dialog/ImportEntity/label/line/title"));
		label.setHorizontalAlignment(SwingConstants.LEFT);

		label.setLabelFor(txtLine);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 0));
		panel.add(label);
		panel.add(txtLine);
		return panel;
	}
	
	/**
	 * 文件格式、文件编码、输入行数。生成一个面板
	 * @return
	 */
	private JPanel createParamPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(3, 1, 0, 12));
		sub.add(createTypePanel());
		sub.add(createCharsetPanel());
		sub.add(createLinePanel());
		
		// 分左右两端位置
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.NORTH);
		
		return panel;
	}
	
	/**
	 * 生成中央面板
	 * @return JPanel实例
	 */
	private JPanel createCenterPanel() {
		// 文件面板，中心位置
		JPanel center = createFilePanel();
		// 参数面板，右侧
		JPanel right = createParamPanel();

		// 文件面板
		String fileText = findCaption("Dialog/ImportEntity/panel/file/title"); // "数据文件";
		String paramText = findCaption("Dialog/ImportEntity/panel/param/title"); //"数据参数";
		
//		CompoundBorder fileCB = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(3, 4, 3, 4));
//		TitledBorder fileBorder = new TitledBorder(fileCB, fileText, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
//				createTitledBorderFont(fileText));
//		center.setBorder(fileBorder);
//
//		CompoundBorder paramCB = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(3, 4, 3, 4));
//		TitledBorder paramBorder = new TitledBorder(paramCB, paramText, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
//				createTitledBorderFont(paramText));
//		right.setBorder(paramBorder);
		
		// 边框
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
		// 初始化显示控件
		initControls();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 5));
		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}
	
	/**
	 * 显示窗口
	 */
	public void showDialog() {
		setContentPane(initPanel());

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/ImportEntity/title");
		setTitle(title);
		
		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);
	}
}