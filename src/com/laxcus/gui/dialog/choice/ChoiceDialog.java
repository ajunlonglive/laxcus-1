/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.choice;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;

/**
 * 选择磁盘资源对话框 <br><br>
 * 
 * 注意！fileSelectionMode默认是DialogOption.DIRECTORIES_ONLY，这样参数acceptAll生成实例，保证mdType最少有一个选择项可用。<br>
 * 否则绘制时会发生空指针错误，错误源是：Caused by: java.lang.NullPointerException <br>
 * 	at javax.swing.plaf.metal.MetalComboBoxButton.paintComponent(MetalComboBoxButton.java:161) <br><br>
 * 
 * 注意啊，继承自ListCellRenderer，或者其它渲染器，返回结果必须是this，如是null，会出现空指针异常！
 * 
 * @author scott.liang
 * @version 1.0 8/28/2021
 * @since laxcus 1.0
 */
public class ChoiceDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = 1740073145166647035L;

	/** 匹配类型 **/
	private ArrayList<FileMatcher> matchers = new ArrayList<FileMatcher>();

	/** 选中的实例 **/
	private FileMatcher selectMatcher;

	/** 选中全部 **/
	private DiskFileMatcher acceptAll;

	/** 磁盘文件 -> 映射记录 **/
	private TreeMap<File, RootItem> history = new TreeMap<File, RootItem>();
	
	/** 记录HOME单元 **/
	private ArrayList<RootItem> home = new ArrayList<RootItem>();

	/** 自定义按纽文本 **/
	private String approveButtonText;
	private String approveButtonToolTipText;

	/** 文本显示：选择/确定 **/
	private FlatButton cmdChoice;

	/** 文本显示：取消 **/
	private FlatButton cmdCancel;

	/** 选择的目录 **/
	private File selectDir;

	/** 磁盘/目录下拉框 **/
	private DefaultComboBoxModel mdDisk = new DefaultComboBoxModel();
	private JComboBox boxDisk = new JComboBox();
	private RootItemRenderer rootRenderer;

	/** 显示文本 **/
	private FlatTextField txtFile = new FlatTextField();

	/** 类型 **/
	private DefaultComboBoxModel mdType = new DefaultComboBoxModel();
	private JComboBox boxType = new JComboBox();
	private FileFilterRenderer typeRenderer;

	/** 类型 **/
	private DefaultComboBoxModel mdEncode = new DefaultComboBoxModel();
	private JComboBox boxEncode = new JComboBox();
	private FileEncodeRenderer encodeRenderer;

	/** 显示字符编码 **/
	private boolean showCharsetEncode;
	/** 字符串编码 **/
	private String charsetEncode;

	/** 多选或者单选，默认是单选 **/
	private boolean multiSelectionEnabled = false;

	/** 默认目录，打开后，定位到这个目录 **/
	private File defaultDirectory;

	/** 列表 **/
	private ListItemRenderer listItemRenderer;
	private DefaultListModel mdList = new DefaultListModel();
	private JList list = new JList();
	/** 滚动面板 **/
	private JScrollPane spList;

	/** 表格 **/
	private TableItemRenderer tableItemRenderer;
	private TableItemModel mdTable = new TableItemModel();
	private JTable table = new JTable();
	private JScrollPane spTable;

	/** 中央面板 **/
	private JPanel center = new JPanel();

	// 保存的文件记录
	private ArrayList<File> saves = new ArrayList<File>();

	//	1. 上一个
	//	2. 新建文件夹
	//	3. 列表
	//	4. 详细信息
	private FlatButton cmdUp;
	private FlatButton cmdNewFolder;
	private FlatButton cmdDesktop;
	private FlatButton cmdList;
	private FlatButton cmdDetial;

	/** 文件模式 **/
	private int dialogType; // = DialogOption.OPEN_DIALOG;

	/** 默认是同时选择文件和目录 **/
	private int fileSelectionMode; // = DialogOption.DIRECTORIES_ONLY; // DialogOption.FILES_AND_DIRECTORIES;

	/**
	 * 构造默认的选择磁盘资源对话框
	 */
	public ChoiceDialog() {
		super();
		// 定义初始值
		setDialogType(DialogOption.OPEN_DIALOG);
		setFileSelectionMode(DialogOption.DIRECTORIES_ONLY);
		setMultiSelectionEnabled(false);
		setShowCharsetEncode(false);
		createTitle();
	}

	/**
	 * 生成标题
	 * @param title
	 */
	public ChoiceDialog(String title) {
		this();
		setTitle(title);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}
	
	/**
	 * 加一个文件过滤器
	 * @param e
	 */
	public void addFileMatcher(FileMatcher e) {
		Laxkit.nullabled(e);
		matchers.add(e);
	}

	public void setSelectFileMatcher(FileMatcher e) {
		selectMatcher = e;
	}

	public FileMatcher getSelectFileMatcher() {
		return selectMatcher;
	}

	/**
	 * 返回全部匹配...
	 * @return FileMatcher数组
	 */
	public FileMatcher[] getFileMatchers() {
		FileMatcher[] a = new FileMatcher[matchers.size()];
		return matchers.toArray(a);
	}

	/**
	 * 设置显示编码
	 * @param b
	 */
	public void setShowCharsetEncode(boolean b) {
		showCharsetEncode = b;
	}

	/**
	 * 显示编码
	 * @return 真或者假
	 */
	public boolean isShowCharsetEncode() {
		return showCharsetEncode;
	}

	/**
	 * 返回字符串编码
	 * @return 编码的字符串描述
	 */
	public String getCharsetEncode() {
		return charsetEncode;
	}

	public void setApproveButtonText(String s) {
		approveButtonText = s;
	}

	public void setApproveButtonToolTipText(String s) {
		approveButtonToolTipText = s;
	}

	/**
	 * 设置初始目录
	 * @param e 目录
	 */
	public void setCurrentDirectory(File e) {
		setDefaultDirectory(e);
	}
	
	/**
	 * 设置默认目录，兼容"setCurrentDirectory"方法
	 * @param e 目录
	 */
	public void setDefaultDirectory(File e) {
		if (e == null) {
			defaultDirectory = null;
		} else if (e.exists() && e.isDirectory()) {
			defaultDirectory = e;
		}
	}

	/**
	 * 设置单选/多选
	 * @param b 真或者假
	 */
	public void setMultiSelectionEnabled(boolean b) {
		multiSelectionEnabled = b;
	}

	/**
	 * 判断允许多选
	 * @return 真或者假
	 */
	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	//	/**
	//	 * 设置选择类型
	//	 * @param who
	//	 */
	//	public boolean setSelectionType(int who) {
	//		switch (who) {
	//		case DialogType.SINALE_SELECT_TYPE:
	//		case DialogType.MULTI_SELECT_TYPE:
	//			selectType = who;
	//			return true;
	//		default:
	//			return false;
	//		}
	//	}

	//	/**
	//	 * 判断是单选
	//	 * @return
	//	 */
	//	public boolean isSingleSelection() {
	//		/** 文件选择类型，多选/单选 **/
	//		return selectType ==  DialogType.SINALE_SELECT_TYPE;
	//	}
	//
	//	/**
	//	 * 判断是多选
	//	 * @return
	//	 */
	//	public boolean isMultiSelection() {
	//		/** 文件选择类型，多选/单选 **/
	//		return selectType ==  DialogType.MULTI_SELECT_TYPE;
	//	}

	//	/** 打开模式 **/
	//	public static final int FILE_OPEN_MODE = 1;
	//	
	//	public static final int FILE_SAVE_MODE = 2;

	/**
	 * 设置文件模式
	 * @param who
	 */
	public boolean setDialogType(int who) {
		switch (who) {
		case DialogOption.OPEN_DIALOG:
		case DialogOption.SAVE_DIALOG:
			dialogType = who;
			return true;
		default:
			throw new IllegalArgumentException("illegal value " + String.valueOf(who));
		}
	}

	/**
	 * 判断是打开模式
	 * @return 返回真或者假
	 */
	public boolean isOpenDialog() {
		return dialogType == DialogOption.OPEN_DIALOG;
	}

	/**
	 * 判断是保存模式
	 * @return 返回真或者假
	 */
	public boolean isSaveDialog() {
		return dialogType == DialogOption.SAVE_DIALOG;
	}

	//	/**
	//	 * 判断是自定义模式
	//	 * @return 返回真或者假
	//	 */
	//	public boolean isCustomDialog() {
	//		return dialogType == DialogType.CUSTOM_DIALOG;
	//	}

	/**
	 * 返回文件模式
	 * @return
	 */
	public int getDialogType() {
		return dialogType;
	}

	/**
	 * 设置文件选择模式
	 * @param who 模式
	 * @return 成功返回真，否则假
	 */
	public boolean setFileSelectionMode(int who) {
		switch(who) {
		case DialogOption.DIRECTORIES_ONLY:
			setAcceptAllFileFilterUsed(true);
			fileSelectionMode = who;
			return true;
		case DialogOption.FILES_ONLY:
		case DialogOption.FILES_AND_DIRECTORIES:
			fileSelectionMode = who;
			return true;
		default:
			throw new IllegalArgumentException("illegal value " + String.valueOf(who));
		}
	}

	/**
	 * 只要文件
	 * @return 真或者假
	 */
	public boolean isFilesOnly() {
		return fileSelectionMode == DialogOption.FILES_ONLY;
	}

	/**
	 * 只要目录
	 * @return 真或者假
	 */
	public boolean isDirectoriesOnly() {
		return fileSelectionMode == DialogOption.DIRECTORIES_ONLY;
	}

	/**
	 * 同时要文件和目录
	 * @return 真或者假
	 */
	public boolean isFileAndDirectories() {
		return fileSelectionMode == DialogOption.FILES_AND_DIRECTORIES;
	}


	/**
	 * 返回文件选择模式
	 * @return 整数
	 */
	public int setFileSelectionMode(){
		return fileSelectionMode;
	}

	//	class DesktopListSelectionListener implements ListSelectionListener {
	//		/* (non-Javadoc)
	//		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	//		 */
	//		@Override
	//		public void valueChanged(ListSelectionEvent e) {
	//			addThread(new ExchangeThread(e));
	//		}
	//	}
	//
	//	class ExchangeThread extends SwingEvent {
	//		ListSelectionEvent event;
	//		public ExchangeThread(ListSelectionEvent e){
	//			super();
	//			event = e;
	//		}
	//		public void process() {
	//			exchange(event);
	//		}
	//	}
	//	
	//	private void exchange(ListSelectionEvent e) {
	//		
	//	}



	//	class ClickThread extends SwingEvent {
	//		ActionEvent event;
	//
	//		public ClickThread(ActionEvent e) {
	//			super();
	//			event = e;
	//		}
	//
	//		public void process() {
	//			click(event);
	//		}
	//	}

	/**
	 * 单击事件 
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdChoice) {
			clickOkayButton();
		} else if (source == cmdCancel) {
			clickCancelButton();
		} else if (source == boxDisk) {
			doSwitchToList();
		} else if (source == boxType) {
			doFilterTypes();
		} else if(source == boxEncode) {
			doCharsetEncode();
		}
	}

	private void doCharsetEncode() {
		int index = boxEncode.getSelectedIndex();
		if (index >= 0) {
			EncodeType et = (EncodeType) mdEncode.getElementAt(index);
			if (et != null) {
				charsetEncode = et.getEncode();
			} else {
				charsetEncode = null;
			}
		}
	}
	
//	/**
//	 * 是标准的路径格式，以根目录为判断标准
//	 * @param filename 文件名
//	 * @return 返回真或者假
//	 */
//	private boolean isStandard(File file) {
//		String filename = Laxkit.canonical(file);
//		File[] roots = File.listRoots();
//		// 判断
//		if (roots != null && roots.length > 0) {
//			for (File root : roots) {
//				String prefix = Laxkit.canonical(root);
//				if (filename.startsWith(prefix)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * 是标准的路径格式，以根目录为判断标准
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	private boolean isStandard(String filename) {
		filename = filename.replace('\\', File.separatorChar);
		// 判断是WINDOWS
		boolean windows = isWindows();
		if (windows) {
			filename = filename.toLowerCase();
		}

		File[] roots = File.listRoots();
		// 判断
		if (roots != null && roots.length > 0) {
			for (File root : roots) {
				String prefix = Laxkit.canonical(root);
				// 如果是WINDOWS，转成小写格式
				if (windows) {
					prefix = prefix.toLowerCase();
				}
				// 判断一致
				if (filename.startsWith(prefix)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 处理文本
	 * @return
	 */
	private boolean doSaveTextField() {
		if (selectDir == null) {
			String title = UIManager.getString("ChoiceDialog.SelectDirEmptyTitle");
			String content = UIManager.getString("ChoiceDialog.SelectDirEmptyContent");
			MessageBox.showWarning(this, title, content);
			return false;
		}
		String text = txtFile.getText();
		text = text.trim();
		if (text.isEmpty()) {
			// 如果没有定义默认的选中目录时，弹出提示
			boolean success = (defaultDirectory != null && (isDirectoriesOnly() || isFileAndDirectories()));
			if (!success) {
				String title = UIManager.getString("ChoiceDialog.EnterFileEmptyTitle");
				String content = UIManager.getString("ChoiceDialog.EnterFileEmptyContent");
				MessageBox.showWarning(this, title, content);
			}
			return false;
		}

		String[] ss = text.split(File.pathSeparator);
		for (String filename : ss) {
			File file = new File(filename);
			if (file.exists()) { // 文件存在
				//				System.out.println("FUCK 1");
				saves.add(file);
			} else if (isStandard(filename)) { // 判断是标准的文件格式
				//				System.out.println("FUCK 2");
				saves.add(file);
			} else { // 其它情况，加上前缀路径
				//				System.out.println("FUCK 3");
				file = new File(selectDir, filename);
				file = checkSuffix(file);
				saves.add(file);
			}
			//			System.out.printf("%s save file %s -> %s\n", selectDir, filename, Laxkit.canonical(file));
		}
		return true;
	}

	/**
	 * 检测后缀，追加它
	 * @param file 文件
	 * @return 返回实例
	 */
	private File checkSuffix(File file) {
		if (selectMatcher == null) {
			return file;
		}
		// 判断符合名称要求
		if (!selectMatcher.accept(file)) {
			String filename = Laxkit.canonical(file);
			String[] exts = selectMatcher.getExtensions();
			filename = String.format("%s.%s", filename, exts[0]);
			file = new File(filename);
		}
		return file;
	}

	/**
	 * 选中类型
	 */
	private void saveSelectFileMatcher() {
		if (mdType.getSize() > 0) {
			int index = boxType.getSelectedIndex();
			if (index >= 0) {
				selectMatcher = (FileMatcher) mdType.getElementAt(index);
			}
		}
	}

	/**
	 * 单击保存按纽
	 */
	private void clickSaveButton() {
		// 重置参数
		saves.clear();
		// 保存显示的单元
		boolean success = doSaveTextField();
		// 不成功
		if (!success) {
			// 如果定义的选中的目录时
			if (defaultDirectory != null && isDirectoriesOnly() && defaultDirectory.exists()) {
				setSelectedValue(new File[] { defaultDirectory });
			}
			return;
		}

		// 保存
		writeBound();
		saveAligmentMode();
		saveSelectFileMatcher();
		
		// 判断有文件
		File[] files = new File[saves.size()];
		saves.toArray(files);
		setSelectedValue(files);
	}

	/**
	 * 单击打开按纽
	 */
	private void clickOpenButton() {
		// 弹出对话框
		if (saves.isEmpty()) {
			String title = UIManager.getString("ChoiceDialog.OpenTypeEmptyTitle");
			String content = UIManager.getString("ChoiceDialog.OpenTypeEmptyContent");
			MessageBox.showWarning(this, title, content);
			return;
		}

		// 保存
		writeBound();
		saveAligmentMode();
		saveSelectFileMatcher();
		
		// 判断有文件
		File[] files = new File[saves.size()];
		saves.toArray(files);
		setSelectedValue(files);
	}

	private void clickOkayButton() {
		if (isSaveDialog()) {
			clickSaveButton();
		} else if (isOpenDialog()) {
			clickOpenButton();
		}
	}

	//	private void clickOkayButton() {
	//		saveBound();
	//		saveAligmentMode();
	//		// 判断有文件
	//		if (saves.isEmpty()) {
	//			setSelectedValue(null);
	//		} else {
	//			File[] files = new File[saves.size()];
	//			saves.toArray(files);
	//			setSelectedValue(files);
	//		}
	//	}

	private void clickCancelButton() {
		writeBound();
		saveAligmentMode();
		setSelectedValue(null);
	}

	//	class CloseDialogThread extends SwingEvent {
	//		CloseDialogThread() {
	//			super();
	//		}
	//		public void process() {
	//			clickOkayButton();
	//		}
	//	}

	/**
	 * 切换
	 */
	private void doSwitchToList() {
		RootItem item = (RootItem) mdDisk.getSelectedItem();
		File file = item.getFile();
		// 是目录
		if (file.isDirectory()) {
			// 显示...
			showDirectory(file);
			// 目录写入环境变量
			writeHomeItem(file);
		}
	}
	
	/**
	 * 如果是在HOME记录范围内，写入环境变量
	 * @param file
	 */
	private void writeHomeItem(File file) {
		for (RootItem item : home) {
			if (file.compareTo(item.getFile()) == 0) {
				writeSelectDisk(file);
				break;
			}
		}
	}

	//	/**
	//	 * 过滤类型
	//	 */
	//	private void doFilterTypes() {
	//		int index = boxType.getSelectedIndex();
	//		if (index >= 0 && selectDir != null) {
	//			// 如果选择有效，记录它
	//			int selectIndex = boxType.getSelectedIndex();
	//			if (selectIndex >= 0) {
	//				selectMatcher = (FileMatcher) mdType.getElementAt(selectIndex);
	//			}
	//			showDirectory(selectDir);
	//		}
	//	}

	/**
	 * 过滤类型
	 */
	private void doFilterTypes() {
		if (mdType.getSize() > 0) {
			int selectIndex = boxType.getSelectedIndex();
			if (selectIndex >= 0 && selectDir != null) {
				// 记录这个选择
				selectMatcher = (FileMatcher) mdType.getElementAt(selectIndex);
				// 显示结果
				showDirectory(selectDir);
			}
		}
	}

	private boolean isParent(File root, File sub) {
		String r = Laxkit.canonical(root);
		String s = Laxkit.canonical(sub);
		int endIndex = r.length();
		String prefix = (s.length() > endIndex ? s.substring(0, endIndex) : "");
		// 区分操作系统
		if (isWindows()) {
			return prefix.equalsIgnoreCase(r);
		} else {
			return prefix.equals(r);
		}
	}

	private void splitPathLink(File dir, ArrayList<PathLink> array) {
		if (dir.isDirectory()) {
			PathLink e = new PathLink(dir, dir.getName());
			int size = array.size();
			if (size == 0) {
				array.add(e);
			} else {
				array.add(0, e);
			}
		}

		File parent = dir.getParentFile();
		if (parent != null) {
			splitPathLink(parent, array);
		}
	}

	/**
	 * 解析路径链
	 * @param dir
	 * @return
	 */
	private PathLink[] splitPathLinks(File dir) {
		ArrayList<PathLink> array = new ArrayList<PathLink>();
		splitPathLink(dir, array);

		PathLink[] a = new PathLink[array.size()];
		return array.toArray(a);
	}

	private RootItem addRooItem(RootItem parent, PathLink link) {
		RootItem sub = new RootItem(parent.getTab() + 1);
		sub.setFile(link.path);
		sub.setDisplayName(link.name);
		sub.setDescription(link.name);
		sub.setIcon(UIManager.getIcon("ChoiceDialog.DirectoryIcon"));

		//		FileSystemView fsv = FileSystemView.getFileSystemView();
		//		sub.setDescription(fsv.getSystemTypeDescription(link.path));
		//		sub.setIcon(fsv.getSystemIcon(link.path));

		// 如果已经存在...
		RootItem ri = history.get(sub.getFile());
		if (ri != null) {
			return ri;
		} else {
			int index = mdDisk.getIndexOf(parent);
			if (index >= 0) {
				mdDisk.insertElementAt(sub, index + 1); // 插入到指定位置
				history.put(sub.getFile(), sub); // 保存
				return sub;
			}
		}
		return null;
	}

	/**
	 * 设置已经在集合中的
	 * @param dir
	 */
	private void setComboxOfHistory(File dir) {
		RootItem ri = history.get(dir);
		if (ri != null) {
			//			System.out.printf("check2 %s is %s\n", dir , (ri !=null ? "valid" :"invalid") );
			mdDisk.setSelectedItem(ri);
		}
	}

	/**
	 * 添加到显示列表中
	 * @param dir
	 */
	private void appendToCombox(File dir) {
		File[] roots = File.listRoots();
		int size = (roots != null ? roots.length : 0);
		RootItem parent = null;

		for (int i = 0; i < size; i++) {
			File root = roots[i];
			// 确定这个RootItem是根目录
			RootItem ri = history.get(root);
			// 没有找到，忽略...
			if (ri == null) {
				continue;
			}
			// 当前目录是父目录
			if (isParent(ri.getFile(), dir)) {
				parent = ri;
				break;
			}
		}

		if (parent == null) {
			setComboxOfHistory(dir);
			return;
		}
		// 在这个目录下建立了单元
		PathLink[] paths = splitPathLinks(dir);
		for (PathLink e : paths) {
			parent = addRooItem(parent, e);
			if (parent == null) {
				break;
			}
		}

		// 设置到指定位置
		if (parent != null) {
			mdDisk.setSelectedItem(parent);
		} 
		// 另一种情况，在列表里
		else {
			setComboxOfHistory(dir);

			//			RootItem ri = history.get(dir);
			//			System.out.printf("check2 %s is %s\n", dir , (ri !=null ? "valid" :"invalid") );
			//			if (ri != null) {
			//				mdDisk.setSelectedItem(ri);
			//			}
		}

	}

	//	/**
	//	 * 判断是允许的..
	//	 * @param file
	//	 * @return 返回真或者假
	//	 */
	//	private boolean allow(File file) {
	//		if (file.isHidden()) {
	//			return false;
	//		} else if (file.isDirectory()) {
	//			return true;
	//		}
	//		for (FileMatcher f : filters) {
	//			if (f.accept(file)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	
//	private String formatting(File file) {
//		File[] files = File.listRoots();
//		int size = (files != null ? files.length : 0);
//		for (int i = 0; i < size; i++) {
//			File f = files[i];
//			if (f.compareTo(file) == 0) {
//				return Laxkit.canonical(f);
//			}
//		}
//
//		return file.getName();
//	}

	/**
	 * 显示目录信息
	 * @param dir
	 */
	private void showDirectory(File dir) {
		if (table.isVisible()) {
			showTableDirectory(dir);
		} else if (list.isVisible()) {
			showListDirectory(dir);
		}
		// 添加显示
		appendToCombox(dir);
		// 保存它
		selectDir = dir;
		
//		// 如果是保存对话框，清除之前的记录
//		if (isSaveDialog()) {
//			// 清除记录
//			saves.clear();
//			// 清空目录
//			txtFile.setText("");
//		}
		
//		System.out.printf("select directory [%s] -> [%s]\n", selectDir.toString(), dir.getName());
		
		// 如果选择目录，或者选择文件和文件时，记录它
		if (isDirectoriesOnly() || isFileAndDirectories()) {
			saves.clear();
			saves.add(dir);
//			String name = Laxkit.canonical(dir);
			txtFile.setText(Laxkit.canonical(dir));
		} else {
			saves.clear();
			txtFile.setText("");
		}
	}

	/**
	 * 判断是不是允许这个文件
	 * @param file
	 * @param matcher
	 * @return
	 */
	private boolean allow(File file, FileMatcher matcher) {
		// 如果是文件，当前只显示目录，不显示它
		if (file.isFile() && isDirectoriesOnly()) {
			return false;
		}
		
		// 隐藏
		if (file.isHidden()) {
			return false;
		} else if (file.isDirectory()) {
			return true;
		}
		
		// 接受这个文件
		if (matcher.accept(file)) {
			return true;
		}
		return false;
	}

	private String getSuffix(File file) {
		if (!file.isFile()) {
			return "";
		}
		String name = file.getName();
		int last = name.lastIndexOf(".");
		if (last > 0) {
			return name.substring(last + 1);
		}
		return "";
	}

	private FileItem createFileItem(File file) {
		FileSystemView fsv = FileSystemView.getFileSystemView();

		FileItem item = new FileItem(file);

		// 设置参数
		RootItem ri = history.get(file);
		if (ri != null) {
			item.setIcon(ri.getIcon());

			String text = ri.getDisplayName();
			if (text == null) {
				text = ri.getDescription();
			}
			item.name = text;
		} else {
			if (file.isFile()) {
				item.setIcon(fsv.getSystemIcon(file));
			}
		}

		// 0 长度
		if (file.isDirectory() && isLinux()) {
			item.setLength(0);
		}

		String des = fsv.getSystemTypeDescription(file);
		if (des == null || des.trim().isEmpty()) {
			des = getSuffix(file);
		}
		item.setTypeDescription(des);

		return item;
	}

	/**
	 * 过滤文件
	 * @param dir
	 * @return
	 */
	private java.util.List<FileItem> filteFiles(File dir) {
		ArrayList<FileItem> a = new ArrayList<FileItem>();
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return a;
		}

		//		int index = boxType.getSelectedIndex();
		//		if (index == -1) {
		//			return a;
		//		}

		FileMatcher matcher = (FileMatcher) mdType.getSelectedItem();
		if (matcher == null) {
			return a;
		}

		//		FileMatcher matcher = (FileMatcher) mdType.getElementAt(index);
		//		FileSystemView fsv = FileSystemView.getFileSystemView();

		//		boolean linux = isLinux();

		for (File file : files) {
			// 如果是隐藏文件或者不允许的，忽略它
			if (allow(file, matcher)) {
				//				FileItem item = new FileItem(file);
				//
				//				// 设置参数
				//				RootItem ri = history.get(file);
				//				if (ri != null) {
				//					item.setIcon(ri.getIcon());
				//
				//					String text = ri.getDisplayName();
				//					if (text == null) {
				//						text = ri.getDescription();
				//					}
				//					item.name = text;
				//				}
				//				
				//				// 0 长度
				//				if (linux && file.isDirectory()) {
				//					item.setLength(0);
				//				}
				//				
				//				String des = fsv.getSystemTypeDescription(file);
				//				if (des == null || des.trim().isEmpty()) {
				//					des = getSuffix(file);
				//				}
				//				item.setTypeDescription(des);

				FileItem item = createFileItem(file);
				if (item != null) {
					a.add(item);
				}
			}
		}
		// 排序
		Collections.sort(a);
		return a;
	}

	private ShowItem createTableShowItem(FileItem item) {
		ShowItem showItem = new ShowItem();
		// 名称
		ShowImageCell name = new ShowImageCell(TableIndex.NAME.index, item.icon);
		if (name.getIcon() == null) {
			Icon fileIcon = UIManager.getIcon("ChoiceDialog.FileIcon");
			Icon dirIcon = UIManager.getIcon("ChoiceDialog.DirectoryIcon");
			if (item.isFile()) {
				name.setIcon(fileIcon);
			} else if (item.isDirectory()) {
				name.setIcon(dirIcon);
			}
		}
		name.setText(item.name);
		name.setTooltip(item.path);
		showItem.add(name);
		// 修改时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
		String str = sdf.format(new java.util.Date(item.lastModified));
		ShowStringCell time = new ShowStringCell(TableIndex.LAST_MODIFIED.index, str);
		time.setSymbol(new java.lang.Long(item.lastModified));
		showItem.add(time);
		// 长度
		str = " ";
		if (item.length > 0) {
			str = ConfigParser.splitCapacity(item.length, 2);
		}
		ShowStringCell length = new ShowStringCell(TableIndex.LENGTH.index, str);
		length.setSymbol(new java.lang.Long(item.length));
		showItem.add(length);
		// 类型定义
		ShowStringCell type = new ShowStringCell(TableIndex.TYPE.index, item.getTypeDescription());
		showItem.add(type);
		return showItem;
	}

	private void showTableDirectory(File dir) {
		mdTable.clear();

		java.util.List<FileItem> a = filteFiles(dir);
		//		Icon fileIcon = UIManager.getIcon("ChoiceDialog.FileIcon");
		//		Icon dirIcon = UIManager.getIcon("ChoiceDialog.DirectoryIcon");
		//		
		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
		//		
		for (FileItem item : a) {
			//			ShowItem si = new ShowItem();
			//			// 名称
			//			ShowImageCell name = new ShowImageCell(TableIndex.NAME.index, item.icon);
			//			if (name.getIcon() == null) {
			//				if (item.isFile()) {
			//					name.setIcon(fileIcon);
			//				} else if (item.isDirectory()) {
			//					name.setIcon(dirIcon);
			//				}
			//			}
			//			name.setText(item.name);
			//			name.setTooltip(item.path);
			//			si.add(name);
			//			// 修改时间
			//			String str = sdf.format(new java.util.Date(item.lastModified));
			//			ShowStringCell time = new ShowStringCell(TableIndex.LAST_MODIFIED.index, str);
			//			si.add(time);
			//			// 长度
			//			str = " ";
			//			if (item.length > 0) {
			//				str = ConfigParser.splitCapacity(item.length, 2);
			//			}
			//			ShowStringCell length = new ShowStringCell(TableIndex.LENGTH.index, str);
			//			si.add(length);
			//			// 类型定义
			//			ShowStringCell type = new ShowStringCell(TableIndex.TYPE.index, item.getTypeDescription());
			//			si.add(type);

			ShowItem showItem = createTableShowItem(item);
			// 加一行
			if (showItem != null) {
				mdTable.addRow(showItem);
			}
		}
	}

	/**
	 * 显示目录
	 * @param dir
	 */
	private void showListDirectory(File dir) {
		mdList.clear();

		//		File[] files = dir.listFiles();
		//		if (files == null || files.length == 0) {
		//			return;
		//		}
		//		
		////		System.out.printf("filters size: %d\n", filters.size());
		//		
		//		FileSystemView fsv = FileSystemView.getFileSystemView();
		//		
		//		ArrayList<FileItem> a = new ArrayList<FileItem>();
		//		for (File file : files) {
		//			// 如果是隐藏文件或者不允许的，忽略它
		//			if (allow(file)) {
		//				FileItem item = new FileItem(file);
		//				
		//				// 设置参数
		//				RootItem ri = history.get(file);
		//				if (ri != null) {
		//					item.setIcon(ri.getIcon());
		//
		//					String text = ri.getDisplayName();
		//					if (text == null) {
		//						text = ri.getDescription();
		//					}
		//					item.name = text;
		//				}
		//				String des = fsv.getSystemTypeDescription( file );
		//				item.setTypeDescription(des);
		//				
		//				a.add(item);
		//			}
		//		}
		//		// 排序
		//		Collections.sort(a);

		// 过滤文件
		java.util.List<FileItem> a = filteFiles(dir);
		// 显示结果
		for (FileItem item : a) {
			mdList.addElement(item);
		}

		//		Object o = boxDisk.getPrototypeDisplayValue();
		//		System.out.printf("class %s\n", (o == null ? "nullable class" : o.getClass().getName()));

		//		Object o = boxDisk.getEditor().getEditorComponent();
		//		if (o.getClass() == JTextField.class) {
		//			JTextField field = (JTextField) o;
		//			String name = dir.getName();
		//			field.setText(name);
		//		}

		//		System.out.printf("class %s\n", (o == null ? "null class" : o.getClass().getName()));

		//		// 显示
		//		String name = dir.getName();
		//		boxDisk.getEditor().setItem(name);
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
	 * 生成按纽
	 * @param key
	 * @param w
	 * @return
	 */
	private JLabel createLabel(String key, char w, JComponent component, int alignment) {
		String text = UIManager.getString(key);
		JLabel label = new JLabel(text);
		label.setDisplayedMnemonic(w);
		if (component != null) {
			label.setLabelFor(component);
		}
		label.setHorizontalAlignment(alignment);
		return label;
	}

	/**
	 * 生成标题
	 */
	private void createTitle() {
		// 标题和图标
		setTitle(UIManager.getString("ChoiceDialog.Title"));
		setFrameIcon(UIManager.getIcon("ChoiceDialog.TitleIcon"));
	}

	/**
	 * 保存排列模式
	 */
	private void saveAligmentMode(){
		String title = (spTable.isVisible() ? "Table" : "List");
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/AligmentMode", title);
	}

	/**
	 * 判断是显示表格模式
	 * @return
	 */
	private boolean isShowTableAligment() {
		String str = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/AligmentMode");
		if (str == null) {
			return false;
		}
		return str.equals("Table");
	}

	/**
	 * 保存被中的磁盘或者目录
	 */
	private void writeSelectDisk(File file) {
		RTKit.writeFile(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/SelectDisk", file);
	}

	/**
	 * 从环境变量读取之前选中的磁盘或者目录
	 * @return File实例
	 */
	private File readSelectDisk() {
		return RTKit.readFile(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/SelectDisk");
	}
	
	/**
	 * 保存范围
	 */
	private void writeBound() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/Bound", rect);
		}
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "ChoiceDialog/Bound");
		if (rect != null) {
			return rect;
		}

		Dimension size = PlatformKit.getPlatformDesktop().getSize(); 

		int width = 512; 
		int height = 356; 

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

		//		// 找到父窗口
		//		JInternalFrame frame = findInternalFrameForComponent(parent);
		//		if (frame == null) {
		//			setBounds(dlg);
		//			return;
		//		}
		//
		//		// 计算空间位置
		//		Rectangle frm = frame.getBounds();
		////		int gapx = (dlg.width < frm.width ? (frm.width - dlg.width) / 2 : 0);
		////		int gapy = (dlg.height < frm.height ? (frm.height - dlg.height) / 2 : 0);
		//		
		//		int gapx = (dlg.width < frm.width ? (frm.width - dlg.width) / 2 : -((dlg.width - frm.width) / 2));
		//		int gapy = (dlg.height < frm.height ? (frm.height - dlg.height) / 2 : -((dlg.height - frm.height) / 2));
		//		int x = frm.x + gapx;
		//		int y = frm.y + gapy;
		//		
		//		// 最小是0
		//		if (x < 0) x = 0;
		//		if (y < 0) y = 0;
		//
		//		// 超过显示范围时...
		//		Dimension dim = PlatfromKit.getDesktopPane().getSize(); 
		//		if (x + dlg.width > dim.width) {
		//			x = dim.width - dlg.width;
		//		}
		//		if (y + dlg.height > dim.height) {
		//			y = dim.height - dlg.height;
		//		}
		//
		//		// 设置显示范围
		//		Rectangle rect = new Rectangle(x, y, dlg.width, dlg.height);
		//		setBounds(rect);
	}

	/**
	 * 判断是LINUX系统
	 * @return 返回真或者假
	 */
	private boolean isLinux() {
		String os = System.getProperty("os.name");
		if (os == null) {
			return false;
		}
		return os.matches("^(.*?)(?i)(LINUX)(.*)$");
	}

	/**
	 * 判断是WINDOWS系统
	 * @return 返回真或者假
	 */
	private boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			return false;
		}
		return os.matches("^(.*?)(?i)(WINDOWS)(.*)$");
	}

	//	private RootItem[] doDisks() {
	//		ArrayList<RootItem> array = new ArrayList<RootItem>();
	//		// 判断是LINUX系统
	//		if (isLinux()) {
	//			File[] files = File.listRoots();
	//			for (File file : files) {
	//				String root = Laxkit.canonical(file);
	//				File[] subs = file.listFiles();
	//				boolean sub = (subs != null && subs.length > 0);
	//				RootItem item = new RootItem(root, false, sub);
	//				array.add(item);
	//			}
	//		}
	//		// 判断是WINDOWS系统
	//		else if (isWindows()) {
	//			File[] files = File.listRoots();
	//			for (File file : files) {
	//				String root = Laxkit.canonical(file);
	//				File[] subs = file.listFiles();
	//				boolean sub = (subs != null && subs.length > 0);
	//				RootItem item = new RootItem(root, true, sub);
	//				array.add(item);
	//			}
	//		}
	//		RootItem[] a = new RootItem[array.size()];
	//		return array.toArray(a);
	//	}

	private boolean isWindowsSystemDriver(File root) {
		File[] files = File.listRoots();
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (root.compareTo(file) == 0) {
				return true;
			}
		}
		return false;
	}

	//	private Icon findIcon(FileSystemView fsv, File dir) {
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		if (!success) {
	//			return fsv.getSystemIcon(dir);
	//		}
	//
	//		String path = Laxkit.canonical(dir);
	//
	//		File[] roots = File.listRoots();
	//		if(roots != null){
	//			for(File root : roots) {
	//				String s = Laxkit.canonical(root);
	//				// 是个子目录时...
	//				if(path.startsWith(s)) {
	//					Icon icon = UIManager.getIcon("ChoiceDialog.DirectoryIcon");
	//					if(icon != null){
	//						return icon;
	//					}
	//				}
	//			}
	//		}
	//
	//		return fsv.getSystemIcon(dir);
	//	}

	private void showWindowsDrivers(FileSystemView fsv, int tab, File root, ArrayList<RootItem> array) {
		File[] files = fsv.getFiles(root, true);
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			
			//			// 不是磁盘驱动，忽略...
			//			if (isWindows()) {
			//				
			//			} else if (isLinux()) {
			//
			//			} else {
			//				continue;
			//			} findIcon(fsv, file)); 

			if (!isWindowsSystemDriver(file)) {
				continue;
			}

			RootItem item = new RootItem(tab);
			item.setFile(file);
			item.setIcon(fsv.getSystemIcon(file));
			item.setDisplayName(fsv.getSystemDisplayName(file));
			item.setDescription(fsv.getSystemTypeDescription(file));
			array.add(item);
		}
	}

	/**
	 * 返回显示的目录
	 * @return
	 */
	private RootItem[] getWindowsRoots() {
		ArrayList<RootItem> array = new ArrayList<RootItem>();

		FileSystemView fsv = FileSystemView.getFileSystemView();
		File home = fsv.getHomeDirectory();

		int tab = 0;

		RootItem item = new RootItem(tab);
		item.setFile(home);
		item.setIcon(fsv.getSystemIcon(home));
		item.setDisplayName(fsv.getSystemDisplayName(home));
		item.setDescription(fsv.getSystemTypeDescription(home));
		array.add(item);

		//		// 子目录
		//		File[] files = File.listRoots();
		//		for(File file : files) {
		//			item = new RootItem(false);
		//			
		//			item.setFile(file);
		//			item.setIcon(fsv.getSystemIcon(file));
		//			item.setDisplayName(fsv.getSystemDisplayName(file));
		//			item.setDescription(fsv.getSystemTypeDescription(file));
		//			array.add(item);
		//		}

		//		Icon icon = UIManager.getIcon("ChoiceDialog.DirectoryIcon");

		// 取出下属目标
		File[] files = fsv.getFiles(home, false);
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 不是目录，忽略
			boolean success = (file.exists() && file.isDirectory());
			if (!success) {
				continue;
			}

			// 保存
			item = new RootItem(tab + 1);
			item.setFile(file);
			// item.setIcon(findIcon(fsv, file));
			item.setIcon(fsv.getSystemIcon(file));
			item.setDisplayName(fsv.getSystemDisplayName(file));
			item.setDescription(fsv.getSystemTypeDescription(file));
			array.add(item);

			// 保存磁盘驱动器
			showWindowsDrivers(fsv, tab + 2, file, array);
		}
		RootItem[] a = new RootItem[array.size()];
		return array.toArray(a);
	}

	private void showLinuxFolder(FileSystemView fsv, int tab, File root, ArrayList<RootItem> array) {
		File[] files = fsv.getFiles(root, true);
		Icon folder = UIManager.getIcon("ChoiceDialog.DirectoryIcon");
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 只能是目录
			boolean success = (file.exists() && file.isDirectory());
			if (!success) {
				continue;
			}

			RootItem item = new RootItem(tab);
			item.setFile(file);
			item.setIcon(folder); // fsv.getSystemIcon(file));
			item.setDisplayName(fsv.getSystemDisplayName(file));
			item.setDescription(fsv.getSystemTypeDescription(file));
			array.add(item);
		}
	}

	private RootItem[] getLinuxRoots() {
		ArrayList<RootItem> array = new ArrayList<RootItem>();

		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] roots = fsv.getRoots();
		int size = (roots != null ? roots.length : 0);

		Icon folder = UIManager.getIcon("ChoiceDialog.DirectoryIcon");

		for (int i = 0; i < size; i++) {
			File home = roots[i];
			int tab = 0;

			RootItem item = new RootItem(tab);
			item.setFile(home);
			item.setIcon(folder); // fsv.getSystemIcon(home));
			item.setDisplayName(fsv.getSystemDisplayName(home));
			item.setDescription(fsv.getSystemTypeDescription(home));
			array.add(item);

			showLinuxFolder(fsv, tab + 1, home, array);
		}
		RootItem[] a = new RootItem[array.size()];
		return array.toArray(a);
	}


	class TitleButtonActionListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == cmdUp) {
				doUpDirectory();
			} else if (source == cmdNewFolder) {
				createNewFolder();
			} else if (source == cmdList) {
				exchangeToList();
			} else if (source == cmdDetial) {
				exchangeToTable();
			} else if(source == cmdDesktop) {
				exchangeToDesktop();
			}
		}
	}

	//	class FolderThread extends SwingEvent {
	//		FolderThread(){
	//			super();
	//		}
	//		public void process() {
	////			doTest();
	//			createNewFolder();
	//		}
	//	}

	//	private void doTest() {
	//		MessageBox.showInformation(this, "TITLE", "FUCK!FUCK!FUCK!TOOO......");
	//	}

	/**
	 * 显示桌面
	 */
	private void exchangeToDesktop() {
		int size = mdDisk.getSize();
		if (size > 0) {
			boxDisk.setSelectedIndex(0);
		}
	}

	private void createNewFolder() {
		String title = UIManager.getString("ChoiceDialog.CreateNewFolderTitle");
		InputDirectoryDialog dialog = new InputDirectoryDialog(title);

		dialog.setApproveText(UIManager.getString("ChoiceDialog.EnterNewFolderText"));
		dialog.setParentPath(selectDir);
		// 显示对话框
		String text = (String) dialog.showDialog(this, true);
		if (text == null) {
			return;
		}

		// 生成新目录
		File dir = new File(selectDir, text);
		boolean success = dir.mkdirs();
		if (!success) {
			title = UIManager.getString("ChoiceDialog.CreateFolderFailedTitle");
			String content = UIManager.getString("ChoiceDialog.CreateFolderFailedContent");
			MessageBox.showFault(this, title, content);
			return;
		}

		// 生成实例
		FileItem item = createFileItem(dir);
		// 添加到单元，并且是选中
		if (spList.isVisible()) {
			mdList.addElement(item);
			// 选中它
			list.setSelectedValue(item, true);
		} else if (spTable.isVisible()) {
			ShowItem showItem = createTableShowItem(item);
			// 加一行
			if (showItem != null) {
				mdTable.addRow(showItem);
				int size = mdTable.getRowCount();
				if (size > 0) {
					table.setRowSelectionInterval(size - 1, size - 1);
				}
			}
		}
	}

	private void doUpDirectory() {
		if (selectDir == null) {
			return;
		}

		File dir = selectDir.getParentFile();
		if (dir != null) {
			showDirectory(dir);
		}
	}

	//	private void exchangeToTable() {
	//		// 如果可视时，忽略
	//		if (spTable.isVisible()) {
	//			return;
	//		}
	//
	//		list.setVisible(false);
	//		spList.setVisible(false);
	//		center.remove(spList);
	//
	//		txtFile.setText("");
	//
	//		center.add(spTable, BorderLayout.CENTER);
	//		table.setVisible(true);
	//		table.validate();
	//		spTable.setVisible(true);
	//		spTable.validate();
	//		table.repaint();
	//		spTable.repaint();
	//
	//		// 可视
	//		validate();
	//		repaint();
	//
	//		if (selectDir != null) {
	//			showTableDirectory(selectDir);
	//		}
	//	}

	private void exchangeToTable() {
		// 如果可视时，忽略
		if (spTable.isVisible()) {
			return;
		}

		list.setVisible(false);
		spList.setVisible(false);
		center.remove(spList);

		txtFile.setText("");

		center.add(spTable, BorderLayout.CENTER);
		table.setVisible(true);
		spTable.setVisible(true);

		// 可视
		validate();
		repaint();

		if (selectDir != null) {
			showTableDirectory(selectDir);
		}
	}

	//	private void exchangeToList() {
	//		// 如果可视时...
	//		if (spList.isVisible()) {
	//			return;
	//		}
	//
	//		table.setVisible(false);
	//		spTable.setVisible(false);
	//		center.remove(spTable);
	//
	//		txtFile.setText("");
	//
	//		center.add(spList, BorderLayout.CENTER);
	//		list.setVisible(true);
	//		list.validate();
	//		spList.setVisible(true);
	//		spList.validate();
	//		list.repaint();
	//		spList.repaint();
	//		
	//		validate();
	//		repaint();
	//		
	//		if (selectDir != null) {
	//			showListDirectory(selectDir);
	//		}
	//	}

	private void exchangeToList() {
		// 如果可视时...
		if (spList.isVisible()) {
			return;
		}

		table.setVisible(false);
		spTable.setVisible(false);
		center.remove(spTable);

		txtFile.setText("");

		center.add(spList, BorderLayout.CENTER);
		list.setVisible(true);
		spList.setVisible(true);

		validate();
		repaint();

		if (selectDir != null) {
			showListDirectory(selectDir);
		}
	}

	/**
	 * 生成图标按纽
	 * @param key
	 * @param listener
	 * @return
	 */
	private FlatButton createTitleButton(String key, String tooltip, TitleButtonActionListener listener) {
		Icon icon = UIManager.getIcon(key);
		FlatButton but = new FlatButton(icon);
		but.setFlat(true); // 设置为平面状态
		but.setToolTipText(UIManager.getString(tooltip));
		but.addActionListener(listener);
		but.setPreferredSize(new Dimension(32, 24));
		but.setFocusPainted(false); // 不绘制焦点边框
		return but;
	}

	/**
	 * 生成标题面板
	 * @return
	 */
	private JPanel createTitleButtonsPanel() {
		TitleButtonActionListener listener = new TitleButtonActionListener();
		cmdUp = createTitleButton("ChoiceDialog.UpIcon", "ChoiceDialog.UpTooltip", listener);
		cmdNewFolder = createTitleButton("ChoiceDialog.NewFolderIcon", "ChoiceDialog.NewFolderTooltip", listener);
		cmdDesktop = createTitleButton("ChoiceDialog.DesktopIcon", "ChoiceDialog.DesktopTooltip", listener);
		cmdList = createTitleButton("ChoiceDialog.ListIcon", "ChoiceDialog.ListTooltip", listener);
		cmdDetial = createTitleButton("ChoiceDialog.TableIcon", "ChoiceDialog.TableTooltip", listener);

		// 如果是打开文件，不允许建立新文件夹
		if (isOpenDialog()) {
			cmdNewFolder.setEnabled(false);
		}
		// 不是WINDOWS，忽略它
		if (!isWindows()) {
			cmdDesktop.setEnabled(false);
		}
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 2, 0, 0));
		p1.setBorder(new EmptyBorder(0, 0, 0, 0));
		p1.add(cmdUp);
		p1.add(cmdNewFolder);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2, 0, 0));
		p2.setBorder(new EmptyBorder(0, 0, 0, 0));
		p2.add(cmdList);
		p2.add(cmdDetial);
		
		JPanel p12 = new JPanel();
		p12.setLayout(new BorderLayout(5, 0));
		p12.setBorder(new EmptyBorder(0, 0, 0, 0));
		p12.add(p1, BorderLayout.CENTER);
		p12.add(cmdDesktop, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(p12, BorderLayout.CENTER);
		panel.add(p2, BorderLayout.EAST);
		return panel;
	}
	
//	/**
//	 * 生成标题面板
//	 * @return
//	 */
//	private JPanel createTitleButtonsPanelX() {
//		TitleButtonActionListener listener = new TitleButtonActionListener();
//		JPanel panel = new JPanel();
//		panel.setLayout(new GridLayout(1, 5, 2, 0));
//		panel.add(cmdUp = createTitleButton("ChoiceDialog.UpIcon", "ChoiceDialog.UpTooltip", listener));
//		panel.add(cmdNewFolder = createTitleButton("ChoiceDialog.NewFolderIcon", "ChoiceDialog.NewFolderTooltip", listener));
//		panel.add(cmdDesktop = createTitleButton("ChoiceDialog.DesktopIcon", "ChoiceDialog.DesktopTooltip", listener));
//		panel.add(cmdList = createTitleButton("ChoiceDialog.ListIcon", "ChoiceDialog.ListTooltip", listener));
//		panel.add(cmdDetial = createTitleButton("ChoiceDialog.TableIcon", "ChoiceDialog.TableTooltip", listener));
//
//		// 如果是打开文件，不允许建立新文件夹
//		if (isOpenDialog()) {
//			cmdNewFolder.setEnabled(false);
//		}
//		// 不是WINDOWS，忽略它
//		if (!isWindows()) {
//			cmdDesktop.setEnabled(false);
//		}
//		return panel;
//	}
	
	/**
	 * 输出和选择
	 */
	private void selectDiskCombox() {
		// 记录
		int selectIndex = -1;
		File file = readSelectDisk();
		
		int size = home.size();
		for (int i = 0; i < size; i++) {
			RootItem value = home.get(i);
			history.put(value.getFile(), value);
			mdDisk.addElement(value);
			
			// 判断
			if (file != null && selectIndex == -1) {
				if (Laxkit.compareTo(file, value.getFile()) == 0) {
					selectIndex = i;
				}
			}
		}
		
		// 没有定义初始目录时，指定显示目录
		if (defaultDirectory == null && size > 0) {
			if (selectIndex >= 0) {
				boxDisk.setSelectedIndex(selectIndex);
			} else {
				boxDisk.setSelectedIndex(0);
			}
		}
	}

	/**
	 * 建立标题面板
	 * @return 返回面板
	 */
	private JPanel createTitlePanel() {
		history.clear();

		// 取出根目录或者磁盘
		RootItem[] roots = null;
		if (isWindows()) {
			roots = getWindowsRoots();
		} else if (isLinux()) {
			roots = getLinuxRoots();
		} else {
			roots = new RootItem[0];
		}
		// 先保存起来，最后初始化
		int size = (roots != null ? roots.length : 0);
		for (int i = 0; i < size; i++) {
			home.add(roots[i]);
		}
		
//		for (int i = 0; i < roots.length; i++) {
//			mdDisk.addElement(roots[i]);
//			// 保存...
//			history.put(roots[i].getFile(), roots[i]);
//		}

		//		if(File.separatorChar == '\\') {
		//		    if(windowsFileSystemView == null) {
		//			windowsFileSystemView = new WindowsFileSystemView();
		//		    }
		//		    return windowsFileSystemView;
		//		}
		//
		//		if(File.separatorChar == '/') {
		//		    if(unixFileSystemView == null) {
		//			unixFileSystemView = new UnixFileSystemView();
		//		    }
		//		    return unixFileSystemView;
		//		}

		// UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");

		//		mdDisk.addElement(new BackgroundDiskItem(UIManager.getString("PropertiesDialog.backgroundFullText"), PlatfromBackground.FULL));
		//		mdDisk.addElement(new BackgroundDiskItem(UIManager.getString("PropertiesDialog.backgroundCenterText"), PlatfromBackground.CENTER));
		//		mdDisk.addElement(new BackgroundDiskItem(UIManager.getString("PropertiesDialog.backgroundMultiText"), PlatfromBackground.MULTI));

		boxDisk.setModel(mdDisk);
		boxDisk.setRenderer(rootRenderer = new RootItemRenderer());
		boxDisk.setPreferredSize(new Dimension(160, 32));
		boxDisk.setLightWeightPopupEnabled(false); // 重量级组件
//		boxDisk.setSelectedIndex(0);
		boxDisk.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		JLabel label = createLabel("ChoiceDialog.LookLabelText", 'V', boxDisk, SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		panel.add(boxDisk, BorderLayout.CENTER);
		panel.add(createTitleButtonsPanel(), BorderLayout.EAST);

		return panel;

		////		JPanel suffix = new JPanel();
		//
		//		JPanel panel = new JPanel();
		//		panel.setLayout(new BorderLayout(8, 0));
		//		panel.add(sub, BorderLayout.CENTER);
		////		panel.add(suffix, BorderLayout.EAST);
		//		return panel;
	}

	/**
	 * 接受全部或者否
	 * @param b
	 */
	public void setAcceptAllFileFilterUsed(boolean b) {
		if (b) {
			if (acceptAll == null) {
				String des = UIManager.getString("ChoiceDialog.AllFileDescriptionText");
				String ext = UIManager.getString("ChoiceDialog.AllFileExtensionText");
				acceptAll = new DiskFileMatcher(des, ext, true);
			}
		} else {
			if (acceptAll != null) {
//				mdType.removeElement(acceptAll);
				acceptAll = null;
			}
		}
	}

	public boolean isAcceptAllFileFilterUsed() {
		if (acceptAll == null) {
			return false;
		}

		int index = mdType.getIndexOf(acceptAll);
		return index != -1;
	}

	//	ChoiceDialog.EncodeTypeDefaultText 默认
	//	ChoiceDialog.EncodeTypeGBKText GBK
	//	ChoiceDialog.EncodeTypeGB2312Text GB2312
	//	ChoiceDialog.EncodeTypeASCIIText ANSI/ASCII
	//	ChoiceDialog.EncodeTypeUTF8Text UTF-8
	//	ChoiceDialog.EncodeTypeUTF16BEText UTF-16 BE
	//	ChoiceDialog.EncodeTypeUTF16LEText UTF-16 LE
	//	ChoiceDialog.EncodeTypeUTF32Text UTF-32


	private EncodeType createET(String key, String encode) {
		String value = UIManager.getString(key);
		return new EncodeType(value, encode);
	}

	private void initEncodeCombox() {
		boxEncode.setModel(mdEncode);
		boxEncode.setRenderer(encodeRenderer = new FileEncodeRenderer());
		boxEncode.setPreferredSize(new Dimension(10, 32));
		boxEncode.setLightWeightPopupEnabled(false); // 重量级组件
		boxEncode.addActionListener(this);
		//	boxEncode.setSelectedIndex(0); // 默认是0

		// 默认编码
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeDefaultText", null));
		// UTF编码
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF8Text", CharsetType.translate(CharsetType.UTF8))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF16Text", CharsetType.translate(CharsetType.UTF16))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF16BEText", CharsetType.translate(CharsetType.UTF16_BE))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF16LEText", CharsetType.translate(CharsetType.UTF16_LE))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF32Text", CharsetType.translate(CharsetType.UTF32)));
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF32BEText", CharsetType.translate(CharsetType.UTF32_BE)));
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF32LEText", CharsetType.translate(CharsetType.UTF32_LE)));
		// 中文编码
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeGBKText", CharsetType.translate(CharsetType.GBK))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeGB2312Text", CharsetType.translate(CharsetType.GB2312))); 
		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeGB18030Text", CharsetType.translate(CharsetType.GB18030)));
	}
	
	//	private void initEncodeCombox() {
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeDefaultText", null));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeGBKText","GBK"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeGB2312Text","GB2312"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeASCIIText","ASCII"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF8Text","UTF-8"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF16BEText","UTF-16 BE"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF16LEText","UTF-16 LE"));
	//		mdEncode.addElement(createET("ChoiceDialog.EncodeTypeUTF32Text","UTF-32"));
	//
	//		boxEncode.setModel(mdEncode);
	//		boxEncode.setPreferredSize(new Dimension(10, 32));
	//		boxEncode.setLightWeightPopupEnabled(false); // 重量级组件
	//		boxEncode.setRenderer(encodeRenderer = new FileEncodeRenderer());
	//		boxEncode.setSelectedIndex(0); // 默认是0
	//		boxEncode.addActionListener(this);
	//	}

//	/**
//	 * 初始化类型
//	 */
//	private void initTypeCombox() {
//		// 过滤单元
//		int selectIndex = -1;
//		int size = matchers.size();
//		if (size > 0) {
//			for (int i = 0; i < size; i++) {
//				FileMatcher ff = matchers.get(i);
//				mdType.addElement(ff);
//				if (selectMatcher != null) {
//					String s1 = selectMatcher.getDescription();
//					String s2 = ff.getDescription();
//					if (s1.compareToIgnoreCase(s2) == 0) {
//						selectIndex = i;
//					}
//				}
//			}
//		}
//		// 选择全部
//		if (acceptAll != null) {
//			mdType.addElement(acceptAll);
//		}
//		//		else {
//		//			String des = UIManager.getString("ChoiceDialog.AllFileDescriptionText");
//		//			String ext = UIManager.getString("ChoiceDialog.AllFileExtensionText");
//		//			DiskFileMatcher ff = new DiskFileMatcher(des, ext, true);
//		//			mdType.addElement(ff);
//		//		}
//
//		boxType.setPreferredSize(new Dimension(10, 32));
//		boxType.setLightWeightPopupEnabled(false); // 重量级组件
//		boxType.setRenderer(typeRenderer = new FileFilterRenderer());
//		boxType.setModel(mdType);
//		
//		
//		if (selectIndex >= 0) {
//			boxType.setSelectedIndex(selectIndex); 
//		} else if(mdType.getSize()>0) {
//			boxType.setSelectedIndex(0);
//		}
//		boxType.addActionListener(this);
//	}

//	/**
//	 * 初始化类型
//	 */
//	private void initTypeCombox() {
//		boxType.setModel(mdType);
//		boxType.setRenderer(typeRenderer = new FileFilterRenderer());
//		boxType.setLightWeightPopupEnabled(false); // 重量级组件
//		boxType.setPreferredSize(new Dimension(10, 32));
//		boxType.addActionListener(this);
//		
//		// 过滤单元
//		int selectIndex = -1;
//		int size = matchers.size();
//		if (size > 0) {
//			for (int i = 0; i < size; i++) {
//				FileMatcher ff = matchers.get(i);
//				mdType.addElement(ff);
//				if (selectMatcher != null) {
//					String s1 = selectMatcher.getDescription();
//					String s2 = ff.getDescription();
//					if (s1.compareToIgnoreCase(s2) == 0) {
//						selectIndex = i;
//					}
//				}
//			}
//		}
//		// 选择全部
//		if (acceptAll != null) {
//			mdType.addElement(acceptAll);
//		}
//		//		else {
//		//			String des = UIManager.getString("ChoiceDialog.AllFileDescriptionText");
//		//			String ext = UIManager.getString("ChoiceDialog.AllFileExtensionText");
//		//			DiskFileMatcher ff = new DiskFileMatcher(des, ext, true);
//		//			mdType.addElement(ff);
//		//		}
//
////		if (selectIndex >= 0) {
////			boxType.setSelectedIndex(selectIndex);
////		} else if (mdType.getSize() > 0) {
////			boxType.setSelectedIndex(0);
////		}
//	}
	
	private void selectTypeCombox() {
		int selectIndex = -1;

		int size = mdType.getSize();
		for (int i = 0; i < size; i++) {
			FileMatcher ff = (FileMatcher) mdType.getElementAt(i);
			if (selectMatcher != null) {
				String s1 = selectMatcher.getDescription();
				String s2 = ff.getDescription();
				if (s1.compareToIgnoreCase(s2) == 0) {
					selectIndex = i;
					break;
				}
			}
		}
		// 定义了选项时
		if (selectIndex >= 0) {
			boxType.setSelectedIndex(selectIndex);
		} 
//		else if (size > 0) {
//			boxType.setSelectedIndex(0);
//		}
	}
	
	/**
	 * 选择编码
	 */
	private void selectEncodeCombox() {
//		if (mdEncode.getSize() > 0) {
//			boxEncode.setSelectedIndex(0); // 默认是0
//		}
	}
	
	/**
	 * 初始化类型
	 */
	private void initTypeCombox() {
		boxType.setModel(mdType);
		boxType.setRenderer(typeRenderer = new FileFilterRenderer());
		boxType.setLightWeightPopupEnabled(false); // 重量级组件
		boxType.setPreferredSize(new Dimension(10, 32));
		boxType.addActionListener(this);
		
//		// 过滤单元
//		int selectIndex = -1;
//		int size = matchers.size();
//		if (size > 0) {
//			for (int i = 0; i < size; i++) {
//				FileMatcher ff = matchers.get(i);
//				mdType.addElement(ff);
//				if (selectMatcher != null) {
//					String s1 = selectMatcher.getDescription();
//					String s2 = ff.getDescription();
//					if (s1.compareToIgnoreCase(s2) == 0) {
//						selectIndex = i;
//					}
//				}
//			}
//		}
		
		int size = matchers.size();
		for (int i = 0; i < size; i++) {
			FileMatcher ff = matchers.get(i);
			mdType.addElement(ff);
			// if (selectMatcher != null) {
			// String s1 = selectMatcher.getDescription();
			// String s2 = ff.getDescription();
			// if (s1.compareToIgnoreCase(s2) == 0) {
			// selectIndex = i;
			// }
			// }
		}
		
		// 选择全部
		if (acceptAll != null) {
			mdType.addElement(acceptAll);
		}

		//		else {
		//			String des = UIManager.getString("ChoiceDialog.AllFileDescriptionText");
		//			String ext = UIManager.getString("ChoiceDialog.AllFileExtensionText");
		//			DiskFileMatcher ff = new DiskFileMatcher(des, ext, true);
		//			mdType.addElement(ff);
		//		}

//		if (selectIndex >= 0) {
//			boxType.setSelectedIndex(selectIndex);
//		} else if (mdType.getSize() > 0) {
//			boxType.setSelectedIndex(0);
//		}
	}
	
	//	private String[] tableTitles = null;

	enum TableIndex {
		NAME(0), LAST_MODIFIED(1), LENGTH(2), TYPE(3);

		public static TableIndex[] effuse() {
			return new TableIndex[] { NAME, LAST_MODIFIED, LENGTH, TYPE };
		}

		int index = 0;

		private TableIndex(int i) {
			index = i;
		}

		/**
		 * 返回索引
		 * @return
		 */
		public int getIndex(){
			return index;
		}

		/**
		 * 判断匹配
		 * @param who 标识符
		 * @return 返回真或者假
		 */
		public boolean isIndex(int who) {
			return index == who;
		}

		public String getWidthKey() {
			switch (index) {
			case 0:
				return "ChoiceDialog.TableHeaderNameWidth";
			case 1:
				return "ChoiceDialog.TableHeaderTimeWidth";
			case 2:
				return "ChoiceDialog.TableHeaderLengthWidth";
			case 3:
				return "ChoiceDialog.TableHeaderTypeWidth";
			}
			return "";
		}

		public String getNameKey() {
			switch (index) {
			case 0:
				return "ChoiceDialog.TableHeaderName";
			case 1:
				return "ChoiceDialog.TableHeaderTime";
			case 2:
				return "ChoiceDialog.TableHeaderLength";
			case 3:
				return "ChoiceDialog.TableHeaderType";
			}
			return "";
		}
	}

	//	class XTableModelListener implements TableModelListener {
	//		
	//		public XTableModelListener(){
	//			super();
	//		}
	//		
	//		public void tableChanged(TableModelEvent e) {
	//			int[] rows = table.getSelectedRows();
	//			for(int row : rows) {
	//				ShowItemCell cell = mdTable.getCellAt(row, 0);
	//				System.out.printf("cell is %s\n", (cell == null ? "NULL" : cell.getClass().getName()));
	//			}
	//		}
	//	}

	private void clickTableItem() {
		int row = table.getSelectedRow();
		row = table.convertRowIndexToModel(row);
		ShowImageCell cell = (ShowImageCell) mdTable.getCellAt(row, 0);
		File file = new File(cell.getTooltip());

		//		System.out.printf("clickTableItem, dir is %s\n", cell.getTooltip());

		// 打开目录
		if (file.isDirectory()) {
			showDirectory(file);
		}
	}

	class TableLKeyAdapter extends KeyAdapter {
		//		public void keyPressed(KeyEvent e) {
		//			int code = e.getKeyCode();
		//			if (e.isControlDown() && code == KeyEvent.VK_A) {
		//				showSelectTableItems();
		//			} else if (code == KeyEvent.VK_ENTER) {
		//				clickTableItem();
		//			}
		//		}

		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (e.isControlDown() && code == KeyEvent.VK_A) {
				showSelectedTableItems();
			} else if (code == KeyEvent.VK_ENTER) {
				clickTableItem();
			}
		}
	}

	class TableMouseAdapter extends MouseAdapter {

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			int count = e.getClickCount();
			if (count > 1) {
				// 显示表格单元
				clickTableItem();
			} else if (count == 1) {
				showSelectedTableItems();

				//				saves.clear();
				//				StringBuilder bf = new StringBuilder();
				//
				//				int[] rows = table.getSelectedRows();
				//				for (int row : rows) {
				//					row = table.convertRowIndexToModel(row);
				//					ShowImageCell cell = (ShowImageCell) mdTable.getCellAt(row, 0);
				//					if (bf.length() > 0) {
				//						bf.append(";");
				//					}
				//					bf.append(cell.getText());
				//					// 文件
				//					File file = new File(cell.getTooltip());
				//
				//					// 保存文件
				//					if (file.isFile() && isFilesOnly()) {
				//						saves.add(file);
				//					} else if (file.isDirectory() && isDirectoriesOnly()) {
				//						saves.add(file);
				//					} else if (isFileAndDirectories()) {
				//						saves.add(file);
				//					}
				//				}
				//				// 输出
				//				txtFile.setText(bf.toString());
			}
		}
	}
	
	private void showSelectedTableItems() {
		saves.clear();
		StringBuilder bf = new StringBuilder();

		int[] rows = table.getSelectedRows();
		for (int row : rows) {
			row = table.convertRowIndexToModel(row);
			ShowImageCell cell = (ShowImageCell) mdTable.getCellAt(row, 0);
			if (bf.length() > 0) {
//				bf.append(";");
				bf.append(File.pathSeparatorChar);
			}
//			bf.append(cell.getText());
			// 文件
			File file = new File(cell.getTooltip());
			bf.append(Laxkit.canonical(file));

			// 保存文件
			if (file.isFile() && isFilesOnly()) {
				saves.add(file);
			} else if (file.isDirectory() && isDirectoriesOnly()) {
				saves.add(file);
			} else if (isFileAndDirectories()) {
				saves.add(file);
			}
		}
		// 输出
		txtFile.setText(bf.toString());
	}

	private void initSorts() {
		// 初始化排序
		TableRowSorter<TableItemModel> sorter = new TableRowSorter<TableItemModel>(mdTable);
		table.setRowSorter(sorter);

		sorter.setComparator(TableIndex.NAME.getIndex(), new NameComparator());
		sorter.setComparator(TableIndex.LAST_MODIFIED.getIndex(), new TimestampComparator());
		sorter.setComparator(TableIndex.LENGTH.getIndex(), new LengthComparator());
		sorter.setComparator(TableIndex.TYPE.getIndex(), new StringComparator());
	}

	class NameComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			ShowImageCell c1 = (ShowImageCell) e1;
			ShowImageCell c2 = (ShowImageCell) e2;
			String s1 = c1.getText();
			String s2 = c2.getText();
			return s1.compareTo(s2);
		}
	}
	
	class LengthComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Long r1 = (Long)e1.getSymbol();
			Long r2 = (Long)e2.getSymbol();
			return r1.compareTo(r2);
		}
	}
	
	class TimestampComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Long r1 = (Long)e1.getSymbol();
			Long r2 = (Long)e2.getSymbol();
			return r1.compareTo(r2);
		}
	}

	class StringComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 比较字符串
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				ret = Laxkit.compareTo((String)str1, (String)str2);
			}
			return ret;
		}
	}

	/**
	 * 设置表对
	 */
	private void initTableHeader() {
		ShowTitle showTitle = new ShowTitle();

		TableIndex[] indexes = TableIndex.effuse();
		for (TableIndex ti : indexes) {
			// 从配置文件中取出参数
			String title = UIManager.getString(ti.getNameKey());
			int width = ConfigParser.splitInteger(UIManager.getString(ti.getWidthKey()), 99);
			ShowTitleCell cell = new ShowTitleCell(ti.index, title, width); //ti.getWidth());
			showTitle.add(cell);

			TableColumn column = new TableColumn(cell.getIndex(), cell.getWidth());
			column.setHeaderValue(cell.getName());

			// 显示站点参数的标题
			//			mdTable.addColumn(cell);
			table.addColumn(column);
		}
		mdTable.setTitle(showTitle);
	}

	private void initDirectoryTable() {
		// 命令处理的显示表格
		table = new JTable(mdTable);
		// 构造渲染器
		tableItemRenderer = new TableItemRenderer();
		table.setDefaultRenderer(ShowItemCell.class, tableItemRenderer);
		// 初始化标题
		initTableHeader();

		// 行高度和表头高度
		int rowHeight = ConfigParser.splitInteger(UIManager.getString("ChoiceDialog.TableRowHeight"), 30);
		int headerHeight = ConfigParser.splitInteger(UIManager.getString("ChoiceDialog.TableHeaderHeight"), 28);

		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(false);
		table.getTableHeader().setReorderingAllowed(true);

		if (isMultiSelectionEnabled()) {
			table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			table.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		}
		table.setColumnSelectionAllowed(false);
		table.setSurrendersFocusOnKeystroke(true);
		table.setBorder(new EmptyBorder(2, 1, 1, 1));

		
		table.setIntercellSpacing(new Dimension(0, 0)); // 不要空格
		
		table.addMouseListener(new TableMouseAdapter());
		table.addKeyListener(new TableLKeyAdapter());
		// 初始化排序
		initSorts();

		spTable = new JScrollPane(table);
		spTable.setColumnHeader(new HeightViewport(headerHeight));
		spTable.setBorder(new HighlightBorder(1));
	}

	class FileListSelectionListener implements ListSelectionListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// 调整中不处理，调整结束后处理
			if (!e.getValueIsAdjusting()) {
				showListSelectItem();
			}
		}
	}

	class ListLKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER) {
				int index = list.getSelectedIndex();
				if (index != -1) {
					FileItem item = (FileItem) mdList.getElementAt(index);
					if (item != null && item.isDirectory()) {
						File dir = new File(item.getPath());
						//						addThread(new OpenDirectoryThread(dir));
						showDirectory(dir);
					}
				}
			}
		}
	}

	class ListMouseAdapter extends MouseAdapter {

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			int count = e.getClickCount();
			if (count > 1) {
				int index = list.locationToIndex(e.getPoint());
				if (index != -1) {
					FileItem item = (FileItem) mdList.getElementAt(index);
					if (item != null && item.isDirectory()) {
						//						System.out.printf("ListMouseAdapter.mouseClick, dir is %s\n", item.getPath() );
						File dir = new File(item.getPath());
						//						addThread(new OpenDirectoryThread(dir)); 
						showDirectory(dir);
					}
				}
			}
			//			else if(count == 1) {
			//				showListSelectItem();
			//			}
		}
	}

	//	private void showListSelectItem() {
	//		StringBuilder buf = new StringBuilder();
	//		int[] all = list.getSelectedIndices();
	//		int size = (all == null ? 0 : all.length);
	//		for (int i = 0; i < size; i++) {
	//			FileItem item = (FileItem) mdList.get(i);
	//			if (buf.length() > 0) {
	//				buf.append(';');
	//			}
	//			buf.append(item.getName());
	//			System.out.println(item.getPath());
	//		}
	//		if (buf.length() > 0) {
	//			txtFile.setText(buf.toString());
	//		}
	//	}

	//	private void showListSelectItem(int firstIndex, int lastIndex) {
	//		StringBuilder buf = new StringBuilder();
	//		//		int[] all = list.getSelectedIndices();
	//		//		int size = (all == null ? 0 : all.length);
	//		for (int i = firstIndex; i <= lastIndex; i++) {
	//			FileItem item = (FileItem) mdList.get(i);
	//			if (buf.length() > 0) {
	//				buf.append(';');
	//			}
	//			buf.append(item.getName());
	//			System.out.println(item.getPath());
	//		}
	//		if (buf.length() > 0) {
	//			txtFile.setText(buf.toString());
	//		}
	//	}

	private void showListSelectItem() {
		// 清除已经保存的
		saves.clear();

		StringBuilder buf = new StringBuilder();
		Object[] objs = list.getSelectedValues();
		for (int i = 0; i < objs.length; i++) {
			FileItem item = (FileItem) objs[i];
			if (buf.length() > 0) {
				// buf.append(';');
				buf.append(File.pathSeparatorChar);
			}
//			buf.append(item.getName());

			File file = new File(item.path);
			buf.append(Laxkit.canonical(file));
			
			// 保存文件
			if (file.isFile() && isFilesOnly()) {
				saves.add(file);
			} else if (file.isDirectory() && isDirectoriesOnly()) {
				saves.add(file);
			} else if (isFileAndDirectories()) {
				saves.add(file);
			}
		}

		txtFile.setText(buf.toString());

		//		if (buf.length() > 0) {
		//			txtFile.setText(buf.toString());
		//		} else {
		////			txtFile.setText("");
		//		}
	}
	
	class ListComponentAdatpter extends ComponentAdapter {

		public void componentResized(ComponentEvent e) {
			// 动态调整垂直列的显示列数
			if (e.getSource() == list) {
				Rectangle r = list.getVisibleRect();
				int fixedHeight = list.getFixedCellHeight();
				int rows = r.height / fixedHeight;
				if (rows > 0) {
					list.setVisibleRowCount(rows);
				}
			}
		}
	}

	/**
	 * 初始化列表
	 */
	private void initDirectoryList() {
		list.setCellRenderer(listItemRenderer = new ListItemRenderer());
		list.setModel(mdList);
		list.setLayoutOrientation(JList.VERTICAL_WRAP); // JList.HORIZONTAL_WRAP);
		// 显示单元范围随显示文本需要变化
		// 高度和宽度
		int cellHeight = ConfigParser.splitInteger(UIManager.getString("ChoiceDialog.ListCellHeight"), 28);
		int cellWidth = ConfigParser.splitInteger(UIManager.getString("ChoiceDialog.ListCellWidth"), 180);
		list.setFixedCellHeight(cellHeight);
		list.setFixedCellWidth(cellWidth);
		
//		list.setVisibleRowCount(5); // 设置为5个不滚动

		// 多选/单选
		if (isMultiSelectionEnabled()) {
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}

		//		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// 事件 ...
		list.addMouseListener(new ListMouseAdapter());
		list.addKeyListener(new ListLKeyAdapter());
		list.addListSelectionListener(new FileListSelectionListener());
		list.addComponentListener(new ListComponentAdatpter());

		// 边框
		list.setBorder(new EmptyBorder(1, 1, 1, 1));

		spList = new JScrollPane(list);
		spList.setBorder(new HighlightBorder(1));
	}

	/**
	 * 执行回车
	 */
	private void doTextFieldEnter() {
		String filename = txtFile.getText();
		filename = filename.trim();
		// 更新路径
		filename = filename.replace('\\', File.separatorChar);

		File file = new File(filename);
		// 如果是目录，打开它，如果是文件，保存它
		if (file.exists()) {
			// 以下三种情况，
			if (file.isDirectory()) {
				showDirectory(file);
			} else if (isFilesOnly() && file.isFile()) {
				saves.clear();
				saves.add(file);
				clickOkayButton();
			} else if(isFileAndDirectories()){
				saves.clear();
				saves.add(file);
				clickOkayButton();
			}
		}
		// 如果是标准格式，直接保存了
		else if (isStandard(filename)) {
			saves.clear();
			saves.add(file);
			clickOkayButton();
		}
		// 有定义目录时
		else if (selectDir != null) {
			file = new File(selectDir, filename);
			if (!file.exists()) {
				return;
			}
			// 以下三种情况，保存它
			if (file.isDirectory()) {
				//				addThread(new OpenDirectoryThread(file));
				showDirectory(file);
			} else if (isFilesOnly() && file.isFile()) {
				saves.clear();
				saves.add(file);
				//				addThread(new CloseDialogThread());
				clickOkayButton();
			} else if(isFileAndDirectories()){
				saves.clear();
				saves.add(file);
				//				addThread(new CloseDialogThread());
				clickOkayButton();
			}
		}
	}

	class TextFieldLKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER) {
				doTextFieldEnter();
			}
		}
	}

	//	class OpenDirectoryThread extends SwingEvent {
	//		File dir;
	//		public OpenDirectoryThread(File d){
	//			super();
	//			dir = d;
	//		}
	//		public void process() {
	//			showDirectory(dir);
	//		}
	//	}

	/**
	 * 生成目录面板
	 * @return 返回面板实例
	 */
	private JPanel createDirectoryPanel() {
		int rows = (isShowCharsetEncode() ? 3 : 2);
		// 初始化字符编码
		if (isShowCharsetEncode()) {
			initEncodeCombox();
		}

		// 初始化
		initTypeCombox();
		initDirectoryTable();
		initDirectoryList();

		center.setLayout(new BorderLayout());

		// 显示表格，或者是列表
		if (isShowTableAligment()) {
			list.setVisible(false);
			spList.setVisible(false);
			center.add(spTable, BorderLayout.CENTER);
		} else {
			table.setVisible(false);
			spTable.setVisible(false);
			center.add(spList, BorderLayout.CENTER);
		}

		txtFile.setPreferredSize(new Dimension(10, 32));
		txtFile.addKeyListener(new TextFieldLKeyAdapter());

		JPanel s1 = new JPanel();
		s1.setLayout(new GridLayout(rows, 1, 0, 6));
		s1.add(createLabel("ChoiceDialog.FileLabelText", 'F', txtFile, SwingConstants.LEFT));
		s1.add(createLabel("ChoiceDialog.TypeLabelText", 'T', boxType, SwingConstants.LEFT));
		if (isShowCharsetEncode()) {
			s1.add(createLabel("ChoiceDialog.EncodeLabelText", 'E', boxEncode, SwingConstants.LEFT));
		}

		JPanel s2 = new JPanel();
		s2.setLayout(new GridLayout(rows, 1, 0, 6));
		s2.add(txtFile);
		s2.add(boxType);
		if (isShowCharsetEncode()) {
			s2.add(boxEncode);
		}

		JPanel s = new JPanel();
		s.setLayout(new BorderLayout(10, 0));
		s.add(s1, BorderLayout.WEST);
		s.add(s2, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(center, BorderLayout.CENTER);
		panel.add(s, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 生成按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		// 取消按纽
		cmdCancel = createButton("ChoiceDialog.CancelButtonText", 'C');

		//  打开/保存/其它...
		if (isOpenDialog()) {
			cmdChoice = createButton("ChoiceDialog.OpenButtonText", 'O');
		} else if (isSaveDialog()) {
			cmdChoice = createButton("ChoiceDialog.SaveButtonText", 'S');
		} else {
			cmdChoice = new FlatButton("Okay");
			cmdChoice.addActionListener(this);
		}

		// 自定义文本
		if (approveButtonText != null) {
			cmdChoice.setText(approveButtonText);
		}
		if (approveButtonToolTipText != null) {
			cmdChoice.setToolTipText(approveButtonToolTipText);
		}

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 2, 4, 0));
		sub.add(cmdChoice);
		sub.add(cmdCancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.EAST);
		return panel;
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.setBorder(new EmptyBorder(10, 8, 6, 8));

		panel.add(createTitlePanel(), BorderLayout.NORTH);
		panel.add(createDirectoryPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}
	
	class ShowComponentThread extends SwingEvent {
		
		public ShowComponentThread() {
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.event.SwingEvent#process()
		 */
		@Override
		public void process() {
			// 初始化HOME对话框上的记录
			selectDiskCombox();
			selectTypeCombox();
			selectEncodeCombox();
			// 显示目录
			if (defaultDirectory != null) {
				showDirectory(defaultDirectory);
			}
		}
	}

	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		// 设置面板
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createPanel(), BorderLayout.CENTER);
		
//		// 初始化HOME对话框上的记录
//		selectDiskCombox();
//		selectTypeCombox();
//		selectEncodeCombox();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#doDefaultOnShow()
	 */
	@Override
	protected void doDefaultOnShow() {
//		// 显示目录
//		if (defaultDirectory != null) {
//			showDirectory(defaultDirectory);
//		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 * 
	 * 返回File[]数组，或者空指针
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 必须是模态窗口
		if (!modal) {
			throw new IllegalArgumentException("must be modal!");
		}

		// 初始化窗口
		initDialog();
		// 范围
		setBounds(parent);

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);
		
		addThread(new ShowComponentThread());

		// 返回结果
		return showModalDialog(parent, cmdCancel);
	}

	/**
	 * 以模态方式打开窗口
	 * @param parent
	 * @return 返回File对象实例，File[]数组或者单个File对象
	 */
	public File[] showDialog(Component parent) {
		return (File[]) showDialog(parent, true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新UI
		if (rootRenderer != null) {
			rootRenderer.updateUI();
		}
		if (typeRenderer != null) {
			typeRenderer.updateUI();
		}
		if (encodeRenderer != null) {
			encodeRenderer.updateUI();
		}
		if (listItemRenderer != null) {
			listItemRenderer.updateUI();
		}
		
		
		
//		if(spTable!=null) {
//			spTable.getViewport().setBackground(Color.white);
//		}
	}

}