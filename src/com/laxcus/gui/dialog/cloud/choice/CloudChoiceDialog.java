/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

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

import com.laxcus.command.cloud.store.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;
import com.laxcus.register.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;

/**
 * 选择磁盘资源对话框
 * 
 * @author scott.liang
 * @version 1.0 8/28/2021
 * @since laxcus 1.0
 */
public class CloudChoiceDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = 1740073145166647035L;

	/** 任务分派器 **/
	protected CommandDispatcher dispatcher;

	/** 匹配类型 **/
	private ArrayList<FileMatcher> matchers = new ArrayList<FileMatcher>();

	/** 选中的实例 **/
	private FileMatcher selectMatcher;

	/** 选中全部 **/
	private DiskFileMatcher acceptAll;

	/** 自定义按纽文本 **/
	private String approveButtonText;
	private String approveButtonToolTipText;

	private JLabel lblText;

	/** 文本显示：选择/确定 **/
	private FlatButton cmdOkay;

	/** 文本显示：取消 **/
	private FlatButton cmdCancel;

	/** 磁盘/目录下拉框 **/
	private DefaultComboBoxModel mdSRL = new DefaultComboBoxModel();
	private JComboBox boxSRL = new JComboBox();
	private SRLRootRenderer rootRenderer;

	/** 显示文本 **/
	private FlatTextField txtFile = new FlatTextField();

	/** 类型 **/
	private DefaultComboBoxModel mdType = new DefaultComboBoxModel();
	private JComboBox boxType = new JComboBox();
	private FileFilterRenderer typeRenderer;

	/** 编码 **/
	private DefaultComboBoxModel mdEncode = new DefaultComboBoxModel();
	private JComboBox boxEncode = new JComboBox();
	private FileEncodeRenderer encodeRenderer;

	/** 显示字符编码 **/
	private boolean showCharsetEncode;
	/** 字符串编码 **/
	private String charsetEncode;

	/** 文件模式 **/
	private int dialogType = DialogOption.OPEN_DIALOG;

	/** 默认是同时选择文件和目录 **/
	private int fileSelectionMode = DialogOption.FILES_AND_DIRECTORIES;

	/** 多选或者单选，默认是单选 **/
	private boolean multiSelectionEnabled = false;

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
	private ArrayList<SRL> saves = new ArrayList<SRL>();

	//	1. 上一个
	//	2. 新建文件夹
	//	3. 列表
	//	4. 详细信息

	private FlatButton cmdUp;
	private FlatButton cmdHome;
	private FlatButton cmdNewFolder;
	private FlatButton cmdList;
	private FlatButton cmdDetial;

	/**
	 * 构造默认的选择磁盘资源对话框
	 */
	public CloudChoiceDialog() {
		super();
		// 任务分配器
		dispatcher = PlatformKit.findListener(CommandDispatcher.class); // PlatformKit.getCommandDispatcher();
		// 显示字符编码
		showCharsetEncode = false;
		initTitle();
	}

	/**
	 * @param title
	 */
	public CloudChoiceDialog(String title) {
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
	 * 返回任务分配器
	 * @return CommandDispatcher实例
	 */
	public CommandDispatcher getCommandDispatcher(){
		return dispatcher;
	}

	/**
	 * 设置文本描述
	 * @param s
	 */
	public void setDescription(String s) {
		if (s != null) {
			lblText.setText(s);
		} else {
			lblText.setText("");
		}
	}

	/**
	 * 加一个文件过滤器
	 * @param e
	 */
	public void addFileMatcher(FileMatcher e) {
		Laxkit.nullabled(e);
		matchers.add(e);
	}

	/**
	 * 设置默认选择的过滤器
	 * @param e
	 */
	public void setSelectFileMatcher(FileMatcher e) {
		selectMatcher = e;
	}

	/**
	 * 返回被选中的过滤器
	 * @return
	 */
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
	 * @return
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
	 * 设置单选/多选
	 * @param b
	 */
	public void setMultiSelectionEnabled(boolean b) {
		multiSelectionEnabled = b;
	}

	/**
	 * 判断允许多选
	 * @return
	 */
	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

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
			return false;
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

	/**
	 * 返回文件模式
	 * @return
	 */
	public int getDialogType() {
		return dialogType;
	}

	/**
	 * 设置文件选择模式
	 * @param who
	 * @return
	 */
	public boolean setFileSelectionMode(int who) {
		switch(who) {
		case DialogOption.DIRECTORIES_ONLY:
			fileSelectionMode = who;
			setAcceptAllFileFilterUsed(true);
			return true;
		case DialogOption.FILES_ONLY:
		case DialogOption.FILES_AND_DIRECTORIES:
			fileSelectionMode = who;
			return true;
		default:
			return false;
		}
	}

	/**
	 * 只要文件
	 * @return
	 */
	public boolean isFilesOnly() {
		return fileSelectionMode == DialogOption.FILES_ONLY;
	}

	/**
	 * 只要目录
	 * @return
	 */
	public boolean isDirectoriesOnly() {
		return fileSelectionMode == DialogOption.DIRECTORIES_ONLY;
	}

	/**
	 * 同时要文件和目录
	 * @return
	 */
	public boolean isFileAndDirectories() {
		return fileSelectionMode == DialogOption.FILES_AND_DIRECTORIES;
	}


	/**
	 * 返回文件选择模式
	 * @return
	 */
	public int setFileSelectionMode(){
		return fileSelectionMode;
	}


	/**
	 * 单击事件 
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdOkay) {
			clickOkayButton();
		} else if (source == cmdCancel) {
			clickCancelButton();
		} else if (source == boxSRL) {
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

	/**
	 * 检测后缀，追加后缀
	 * @param file 文件
	 * @return 返回原来或者新的名称
	 */
	private String checkSuffix(String name) {
		if (selectMatcher == null) {
			return name;
		}
		// 判断符合名称要求
		if (!selectMatcher.accept(name)) {
			String[] exts = selectMatcher.getExtensions();
			name = String.format("%s.%s", name, exts[0]);
		}
		return name;
	}

	/**
	 * 处理文本
	 * @return
	 */
	private boolean doSaveTextField() {
		if (selectPath == null) {
			String title = UIManager.getString("CloudChoiceDialog.SelectDirEmptyTitle");
			String content = UIManager.getString("CloudChoiceDialog.SelectDirEmptyContent");
			MessageBox.showWarning(this, title, content);
			return false;
		}
		String text = txtFile.getText();
		text = text.trim();
		if (text.isEmpty()) {
			// 如果是目录，但是没有定义子目录时
			if (isDirectoriesOnly() || isFileAndDirectories()) {
				SRL srl = new SRL(selectSite, selectPath.getPath());
				saves.add(srl);
				return true;
			} else {
				String title = UIManager.getString("CloudChoiceDialog.EnterFileEmptyTitle");
				String content = UIManager.getString("CloudChoiceDialog.EnterFileEmptyContent");
				MessageBox.showWarning(this, title, content);
				return false;
			}
		}

		String path = selectPath.getPath();
		// 保存信息
		String[] names = text.split(";");
		for (String name : names) {
			name = checkSuffix(name); // 判断和追加后缀

			SRL srl = new SRL(selectSite);
			if (path.equals("/")) {
				srl.setPath("/" + name);
			} else {
				srl.setPath(path + "/" + name);
			}
			saves.add(srl);
		}
		return true;
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
		if (!doSaveTextField()) {
			return;
		}

		// 保存
		saveBound();
		saveAligmentMode();
		saveSelectFileMatcher();
		
		// 判断SRL
		SRL[] elements = new SRL[saves.size()];
		saves.toArray(elements);
		setSelectedValue(elements);
	}

	/**
	 * 单击打开按纽
	 */
	private void clickOpenButton() {
		// 弹出对话框
		if (saves.isEmpty()) {
			String title = UIManager.getString("CloudChoiceDialog.OpenTypeEmptyTitle");
			String content = UIManager.getString("CloudChoiceDialog.OpenTypeEmptyContent");
			MessageBox.showWarning(this, title, content);
			return;
		}

		// 保存
		saveBound();
		saveAligmentMode();
		saveSelectFileMatcher();
		
		// 保存SRL
		SRL[] elements = new SRL[saves.size()];
		//		for (int i = 0; i < elements.length; i++) {
		//			SRLItem item = saves.get(i);
		//			elements[i] = item.getSRL();
		//		}

		saves.toArray(elements);
		setSelectedValue(elements);
	}

	private void clickOkayButton() {
		if (isSaveDialog()) {
			clickSaveButton();
		} else if (isOpenDialog()) {
			clickOpenButton();
		}
	}

	private void clickCancelButton() {
		saveBound();
		saveAligmentMode();
		setSelectedValue(null);
	}

	/**
	 * 切换到另一个主机服务器目录
	 */
	private void doSwitchToList() {
		SRLRoot item = (SRLRoot) mdSRL.getSelectedItem();
		SRL srl = item.getSRL();
		showSite(srl);
	}

	/**
	 * 过滤类型
	 */
	private void doFilterTypes() {
		int selectIndex = boxType.getSelectedIndex();

		if (selectIndex >= 0 && selectPath != null) {
			// 记录这个选择
			selectMatcher = (FileMatcher) mdType.getElementAt(selectIndex);
			// 显示结果
			showDirectory(selectPath);
		}
	}

	private boolean allow(String path) {
		File file = new File(path);
		if (selectMatcher != null) {
			if (selectMatcher.accept(file)) {
				return true;
			}
		}
		// 选中
		FileMatcher matcher = (FileMatcher) boxType.getSelectedItem();
		if (matcher != null) {
			if (matcher.accept(file)) {
				return true;
			}
		}

		return false;
	}

	class ShowVPath extends SwingEvent {
		Node site;

		VPath path;

		public ShowVPath(Node n, VPath v) {
			super(true);
			site = n;
			path = v;
		}

		public void process() {
			showDisk(site, path);
		}
	}
	
	/**
	 * 判断来源地址与本地匹配
	 * @param hub 服务器
	 * @return 匹配返回真，否则假
	 */
	private boolean hasMatch(Node hub) {
		int size = mdSRL.getSize();
		for (int i = 0; i < size; i++) {
			SRLRoot st = (SRLRoot) mdSRL.getElementAt(i);
			if (Laxkit.compareTo(st.getNode(), hub) == 0) {
				return true;
			}
		}
		return false;
	}

	class ScanCloudDirectoryProductListener implements ProductListener {

		@Override
		public void push(Object e) {
			boolean success = (e != null && Laxkit.isClassFrom(e, ScanCloudDirectoryProduct.class));
			if (success) {
				ScanCloudDirectoryProduct sl = (ScanCloudDirectoryProduct) e;
				if (hasMatch(sl.getSite())) {
					addThread(new ShowVPath(sl.getSite(), sl.getVPath()));
				} else if (hasMatch(sl.getGateway())) {
					addThread(new ShowVPath(sl.getGateway(), sl.getVPath()));
				}
			} else {
				String text = UIManager.getString("CloudChoiceDialog.NotScanSiteResource");
				setDescription(text);
			}
		}
	}

	class CloudMeetDisplay extends MeetDisplayAdapter {
		public CloudMeetDisplay(ProductListener e) {
			super(e);
		}
	}

	class CloudCommandAuditor extends CommandAuditorAdapter {

	}

	/**
	 * 显示主机地址下面的全部目录
	 */
	private void showSite(SRL srl) {
		ScanCloudDirectory cmd = new ScanCloudDirectory(srl);
		cmd.setSound(false);
		
		// 把结果放入对应的监听器中
		ScanCloudDirectoryProductListener listener = new ScanCloudDirectoryProductListener();
		int who = dispatcher.submit(cmd, true, new CloudCommandAuditor(), new CloudMeetDisplay(listener));
		// 如果是取消时，弹出通知
		if (CommandSubmit.isAccpeted(who)) {
			String text = UIManager.getString("CloudChoiceDialog.ScanSiteText");
			text = String.format(text, srl);
			setDescription(text);
		} else {
			String text = UIManager.getString("CloudChoiceDialog.RefuseSubmitText");
			text = String.format(text, srl.getNode());
			setDescription(text);
		}
	}

	private ShowItem createTableShowItem(SRLItem item) {
		ShowItem showItem = new ShowItem();
		// 名称
		ShowImageCell name = new ShowImageCell(TableIndex.NAME.index, item.icon);
		if (name.getIcon() == null) {
			Icon fileIcon = UIManager.getIcon("CloudChoiceDialog.FileIcon");
			Icon dirIcon = UIManager.getIcon("CloudChoiceDialog.DirectoryIcon");
			if (item.isFile()) {
				name.setIcon(fileIcon);
			} else if (item.isDirectory()) {
				name.setIcon(dirIcon);
			}
		}
		name.setText(item.name);
		name.setTooltip(item.path.toString());
		showItem.add(name);
		// 标记
		name.setSymbol(item);

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

		// 返回结果
		return showItem;
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

	private void initTitle() {
		// 标题
		String title = UIManager.getString("CloudChoiceDialog.Title");
		setTitle(title);
		// 图标
		setFrameIcon(UIManager.getIcon("CloudChoiceDialog.TitleIcon"));
	}

	/**
	 * 保存排列模式
	 */
	private void saveAligmentMode(){
		String title = (spTable.isVisible() ? "Table" : "List");
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "CloudChoiceDialog/AligmentMode", title);
	}

	/**
	 * 判断是显示表格模式
	 * @return
	 */
	private boolean isShowTableAligment() {
		String str = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "CloudChoiceDialog/AligmentMode");
		if (str == null) {
			return false;
		}
		return str.equals("Table");
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle rect = super.getBounds();
		if (rect != null) {
			RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "CloudChoiceDialog/Bound", rect);
		}
	}

	/**
	 * 从环境变量读取范围或者定义范围
	 * @return Rectangle实例
	 */
	private Rectangle readBounds() {
		// 从环境中取参数
		Rectangle rect = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "CloudChoiceDialog/Bound");
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
	}


	private long translate(long date) {
		Date d = SimpleTimestamp.format(date);
		return d.getTime();
	}

	private static Map<String, Icon> images = new TreeMap<String, Icon>();

	private static Map<String, String> descriptions = new TreeMap<String, String>();

	private Node selectSite;

	private VPath selectRoot;

	private VPath selectPath;

	/**
	 * 取得图标和描述字
	 * @param path
	 * @return
	 */
	private Object[] doIconString(String path) {
		// 找到最后
		int last = path.lastIndexOf('.');
		if (last < 0) {
			return null;
		}

		// 取得结果
		String suffix = path.substring(last);
		Icon icon = images.get(suffix);
		String des = descriptions.get(suffix);
		if (icon != null && des != null) {
			return new Object[] { icon, des };
		}

		// 从系统中取得 
		FileSystemView fsv = FileSystemView.getFileSystemView();
		String tmp = System.getProperty("java.io.tmpdir");
		if (tmp == null) {
			return null;
		}
		// 取得系统中的定义
		try {
			File file = new File(tmp, path);
			FileOutputStream os = new FileOutputStream(file);
			os.write(12);
			os.close();
			// 取出图标
			icon = fsv.getSystemIcon(file);
			des = fsv.getSystemTypeDescription(file);
			file.delete();
		} catch (IOException e) {

		}

		// 保存它
		if (icon != null && des != null) {
			images.put(suffix, icon);
			descriptions.put(suffix, des);
			return new Object[] { icon, des };
		}
		return null;
	}

	/**
	 * 显示根目录下面文件和目录
	 * @param v
	 * @param disk
	 */
	private void showDisk(Node v, VPath disk) {
		selectSite = v;
		selectRoot = disk;
		selectPath = disk;

		// 向上的按纽失效
		cmdUp.setEnabled(false);
		cmdHome.setEnabled(false);

		// 生成对象
		ArrayList<SRLItem> a = new ArrayList<SRLItem>();
		for (VPath sub : selectRoot.list()) {
			SRL srl = new SRL(selectSite);
			// 根目录和子目录的组合
			srl.setPath(sub.getPath());

			SRLItem item = new SRLItem(sub);
			item.setParent(selectRoot);
			item.setSRL(srl);
			item.setName(sub.getName());
			item.setLength(sub.getLength());
			item.setLastModified(translate(sub.getLastModified()));

			// 区分目录和文件
			if (sub.isDirectory()) {
				item.setDirectory(true);
			} else if (sub.isFile()) {
				String name = sub.getName();
				// 如果不是允许的文件名时，忽略它
				if (!allow(name)) {
					continue;
				}

				item.setDirectory(false);
				Object[] objs = doIconString(name);
				if (objs != null) {
					item.setIcon((Icon) objs[0]);
					item.setTypeDescription((String) objs[1]);
				}
				item.setLength(sub.getLength());
			}
			a.add(item);
		}

		Collections.sort(a);
		// 有区别的保存单元
		if (list.isVisible()) {
			mdList.clear();
			for (SRLItem item : a) {
				mdList.addElement(item);
			}
		} else if (table.isVisible()) {
			mdTable.clear();
			for (SRLItem item : a) {
				ShowItem showItem = createTableShowItem(item);
				if (showItem != null) {
					mdTable.addRow(showItem);
				}
			}
		}

		// 显示当前是的目录
		SRL srl = new SRL(selectSite);
		srl.setPath(selectPath.getPath());
		setDescription(srl.toString());
	}

	private void showDirectory(VPath dir) {
		if (dir == null) {
			return;
		}
		// 记录父目录
		selectPath = dir;

		// 如果当前是目录时，向上的按纽失效
		if (dir.isDisk()) {
			cmdUp.setEnabled(false);
			cmdHome.setEnabled(false);
		} else {
			cmdUp.setEnabled(true);
			cmdHome.setEnabled(true);
		}

		// 显示...
		ArrayList<SRLItem> a = new ArrayList<SRLItem>();
		for (VPath sub : dir.list()) {
			SRL srl = new SRL(selectSite);
			srl.setPath(sub.getPath());

			SRLItem item = new SRLItem(sub);
			item.setParent(selectPath);
			item.setSRL(srl);
			item.setName(sub.getName());
			item.setLength(sub.getLength());
			item.setLastModified(translate(sub.getLastModified()));

			// 区分目录和文件
			if (sub.isDirectory()) {
				item.setDirectory(true);
			} else if (sub.isFile()) {
				String name = sub.getName();
				// 如果不是允许的文件名时，忽略它
				if (!allow(name)) {
					continue;
				}

				item.setDirectory(false);
				Object[] objs = doIconString(name);
				if (objs != null) {
					item.setIcon((Icon) objs[0]);
					item.setTypeDescription((String) objs[1]);
				}
				item.setLength(sub.getLength());
			}
			a.add(item);
		}

		Collections.sort(a);
		// 显示...
		if (list.isVisible()) {
			mdList.clear();
			for (SRLItem item : a) {
				mdList.addElement(item);
			}
		} else if (table.isVisible()) {
			mdTable.clear();
			for (SRLItem item : a) {
				ShowItem showItem = createTableShowItem(item);
				// 加一行
				if (showItem != null) {
					mdTable.addRow(showItem);
				}
			}
		}

		// 显示当前是的目录
		SRL srl = new SRL(selectSite);
		srl.setPath(selectPath.getPath());
		setDescription(srl.toString());
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
			}  else if(source == cmdHome) {
				exchangeToHome();
			}
			// 建立新目录
			else if (source == cmdNewFolder) {
				createNewFolder();
			} 
			// 列表
			else if (source == cmdList) {
				exchangeToList();
			} else if (source == cmdDetial) {
				exchangeToTable();
			}
		}
	}

	/**
	 * 显示桌面
	 */
	private void exchangeToHome() {		
		if (selectRoot != null) {
			showDirectory(selectRoot);
		}
	}

	class CreateVPath extends SwingEvent {
		CreateCloudDirectoryProduct product;

		CreateVPath(CreateCloudDirectoryProduct e) {
			super(true);
			product = e;
		}

		public void process() {
			setDescription("");
			if (product != null) {
				processCreateDirectory(product);
			}
		}
	}

	private void processCreateDirectory(CreateCloudDirectoryProduct product) {
		// 结果...
		int state = product.getState();

		// 取出目录，更新界面。注意，这是不更新情况下
		if (StoreState.isSuccessful(state)) {
			// 判断路径一致时
			boolean match = (focusSelectPath != null && selectPath != null && Laxkit
					.compareTo(focusSelectPath, selectPath) == 0);
			focusSelectPath = null;
			if (!match) {
				return;
			}
			// 显示新目录
			VPath path = product.getPath();
			selectPath.add(path);
			showDirectory(selectPath);
		} else {
			String title = UIManager.getString("CloudChoiceDialog.CreateFolderFailedTitle");
			String content = UIManager.getString("CloudChoiceDialog.CreateFolderFailedContent");
			MessageBox.showWarning(this, title, content);
		}
	}

	class CreateDirectoryProductListener implements ProductListener {

		@Override
		public void push(Object e) {
			if (e != null && Laxkit.isClassFrom(e, CreateCloudDirectoryProduct.class)) {
				CreateCloudDirectoryProduct sd = (CreateCloudDirectoryProduct) e;
				addThread(new CreateVPath(sd));
			}
		}
	}

	private VPath focusSelectPath = null;

	private void createNewFolder() {
		// 判断已经有效
		boolean allow = (selectSite != null && selectPath != null);
		if (!allow) {
			String title = UIManager.getString("CloudChoiceDialog.CreateNewFolder.NotSiteTitle");
			String content = UIManager.getString("CloudChoiceDialog.CreateNewFolder.NotSiteContent");
			MessageBox.showWarning(this, title, content);
			return;
		}

		String title = UIManager.getString("CloudChoiceDialog.CreateNewFolderTitle");
		InputCloudDirectoryDialog dialog = new InputCloudDirectoryDialog(title);

		dialog.setApproveText(UIManager.getString("CloudChoiceDialog.EnterNewFolderText"));
		dialog.setParentPath(selectPath);

		// 显示对话框
		String path = dialog.showDialog(this);
		if (path == null) {
			return;
		}

		SRL srl = new SRL(selectSite);
		String s = selectPath.getPath();
		if (s.equals("/")) {
			srl.setPath("/" + path);
		} else {
			srl.setPath(s + "/" + path);
		}

		// 生成命令
		CreateCloudDirectory cmd = new CreateCloudDirectory(srl);
		CreateDirectoryProductListener listener = new CreateDirectoryProductListener();
		int who = dispatcher.submit(cmd, true, new CloudCommandAuditor(), new CloudMeetDisplay(listener));
		// 如果是取消时，弹出通知
		if (CommandSubmit.isAccpeted(who)) {
			focusSelectPath = selectPath;
			String text = UIManager.getString("CloudChoiceDialog.CreateDirectoryText");
			text = String.format(text, srl);
			setDescription(text);
		} else {
			// cancel();
		}
	}

	private VPath findParentPath(VPath parent, VPath path) {
		if (parent == null || path == null) {
			return null;
		}
		for (VPath sub : parent.list()) {
			if (Laxkit.compareTo(sub, path) == 0) {
				return parent;
			}
			// 如果是目录，进去找到它父目录
			if (sub.isDirectory()) {
				VPath temp = findParentPath(sub, path);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * 上一级目录
	 */
	private void doUpDirectory() {
		// 当前目录没有定义时...
		if (selectPath == null || selectPath.isDisk()) {
			return;
		}

		// selectPath的上级目录
		VPath parent = findParentPath(selectRoot, selectPath);
		if (parent != null) {
			showDirectory(parent);
		} else {
			if (list.isVisible()) {
				mdList.clear();
			} else if (table.isVisible()) {
				mdTable.clear();
			}
		}
	}

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

		// 显示结果
		if (selectPath != null) {
			showDirectory(selectPath);
		}
	}

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

		// 显示结果
		if (selectPath != null) {
			showDirectory(selectPath);
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

		cmdUp = createTitleButton("CloudChoiceDialog.UpIcon", "CloudChoiceDialog.UpTooltip", listener);
		cmdHome = createTitleButton("CloudChoiceDialog.DesktopIcon", "CloudChoiceDialog.DesktopTooltip", listener);
		cmdNewFolder = createTitleButton("CloudChoiceDialog.NewFolderIcon", "CloudChoiceDialog.NewFolderTooltip", listener);
		cmdList = createTitleButton("CloudChoiceDialog.ListIcon", "CloudChoiceDialog.ListTooltip", listener);
		cmdDetial = createTitleButton("CloudChoiceDialog.TableIcon", "CloudChoiceDialog.TableTooltip", listener);
		
		cmdUp.setEnabled(false);
		cmdHome.setEnabled(false);

		// 如果是打开文件，或者当前不是用户状态，不允许建立新文件夹
		if (isOpenDialog() || !isUser()) {
			cmdNewFolder.setEnabled(false);
		}

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 2, 0, 0));
		p1.setBorder(new EmptyBorder(0, 0, 0, 0));
		p1.add(cmdUp);
		p1.add(cmdHome);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2, 0, 0));
		p2.setBorder(new EmptyBorder(0, 0, 0, 0));
		p2.add(cmdList);
		p2.add(cmdDetial);

		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(5, 0));
		p3.setBorder(new EmptyBorder(0, 0, 0, 0));
		p3.add(p1, BorderLayout.CENTER);
		p3.add(cmdNewFolder, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(p3, BorderLayout.CENTER);
		panel.add(p2, BorderLayout.EAST);

		return panel;
	}

	/**
	 * 返回用户资源适配器
	 * @return 返回FrontListener实例，或者空指针
	 */
	private FrontListener getFrontListener() {
		return PlatformKit.findListener(FrontListener.class);
	}

	/**
	 * 返回级别适配器
	 * @return 返回GradeListener实例，或者空指针
	 */
	private GradeListener getGradeListener() {
		return PlatformKit.findListener(GradeListener.class);
	}

	/**
	 * 判断是用户状态
	 * @return 返回真或者假
	 */
	private boolean isUser() {
		GradeListener as = getGradeListener();
		return as != null && as.isUser();
	}

	/**
	 * 获得云端节点，显示到界面上
	 */
	private void pickupCloudSites() {
		// 如果不是注册用户，忽略它
		if (!isUser()) {
			return;
		}

		// 用户资源适配器
		FrontListener as = getFrontListener();
		if (as == null) {
			return;
		}

		// 取出CALL节点
		Node[] sites = as.getCloudSites(); // as.getCallSites();
		int size = (sites == null ? 0 : sites.length);
		// 保存SRL实例
		for (int i = 0; i < size; i++) {
			SRL srl = new SRL(sites[i]);
			SRLRoot root = new SRLRoot(srl);
			mdSRL.addElement(root);
		}
	}

	/**
	 * 建立标题面板
	 * @return 返回面板
	 */
	private JPanel createTitlePanel() {
		boxSRL.setModel(mdSRL);
		boxSRL.setRenderer(rootRenderer = new SRLRootRenderer());
		boxSRL.setLightWeightPopupEnabled(false); // 重量级组件
		boxSRL.setPreferredSize(new Dimension(160, 32));
		
		// 获得云端主机地址
		pickupCloudSites();

		// 如果有时，选择第一个
		if (boxSRL.getItemCount() > 0) {
			boxSRL.setSelectedIndex(0);
		}
		// 事件
		boxSRL.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		JLabel label = createLabel("CloudChoiceDialog.LookLabelText", 'V', boxSRL, SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		panel.add(boxSRL, BorderLayout.CENTER);
		panel.add(createTitleButtonsPanel(), BorderLayout.EAST);

		return panel;
	}

	public void setAcceptAllFileFilterUsed(boolean b) {
		if (b) {
			if (acceptAll == null) {
				String des = UIManager.getString("CloudChoiceDialog.AllFileDescriptionText");
				String ext = UIManager.getString("CloudChoiceDialog.AllFileExtensionText");
				acceptAll = new DiskFileMatcher(des, ext, true);
			}
		} else {
			if (acceptAll != null) {
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

	private EncodeType createET(String key, String encode) {
		String value = UIManager.getString(key);
		return new EncodeType(value, encode);
	}

	private void initEncodeCombox() {
		boxEncode.setModel(mdEncode);
		boxEncode.setRenderer(encodeRenderer = new FileEncodeRenderer());
		boxEncode.setPreferredSize(new Dimension(10, 32));
		boxEncode.setLightWeightPopupEnabled(false); // 重量级组件
		
		// 默认编码
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeDefaultText", null));
		// UTF系列编辑
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF8Text", CharsetType.translate(CharsetType.UTF8))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF16Text", CharsetType.translate(CharsetType.UTF16))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF16BEText", CharsetType.translate(CharsetType.UTF16_BE))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF16LEText", CharsetType.translate(CharsetType.UTF16_LE))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF32Text", CharsetType.translate(CharsetType.UTF32)));
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF32BEText", CharsetType.translate(CharsetType.UTF32_BE)));
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeUTF32LEText", CharsetType.translate(CharsetType.UTF32_LE)));
		// 中文编码
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeGBKText", CharsetType.translate(CharsetType.GBK))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeGB2312Text", CharsetType.translate(CharsetType.GB2312))); 
		mdEncode.addElement(createET("CloudChoiceDialog.EncodeTypeGB18030Text", CharsetType.translate(CharsetType.GB18030))); 

		// 选择第一个
		boxEncode.setSelectedIndex(0); // 默认是0
		boxEncode.addActionListener(this);
	}

	/**
	 * 初始化类型
	 */
	private void initTypeCombox() {
		// 过滤单元
		int selectIndex = -1;
		int size = matchers.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				FileMatcher ff = matchers.get(i);
				mdType.addElement(ff);
				if (selectMatcher != null) {
					String s1 = selectMatcher.getDescription();
					String s2 = ff.getDescription();
					if (s1.compareToIgnoreCase(s2) == 0) {
						selectIndex = i;
					}
				}
			}
		}
		// 选择全部
		if (acceptAll != null) {
			mdType.addElement(acceptAll);
		}

		boxType.setPreferredSize(new Dimension(10, 32));
		boxType.setLightWeightPopupEnabled(false); // 重量级组件
		boxType.setRenderer(typeRenderer = new FileFilterRenderer());
		boxType.setModel(mdType);
		if (selectIndex >= 0) {
			boxType.setSelectedIndex(selectIndex);
		} else if (mdType.getSize() > 0) {
			boxType.setSelectedIndex(0);
			selectMatcher = (FileMatcher) mdType.getElementAt(0);
		}
		boxType.addActionListener(this);
	}

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
				return "CloudChoiceDialog.TableHeaderNameWidth";
			case 1:
				return "CloudChoiceDialog.TableHeaderTimeWidth";
			case 2:
				return "CloudChoiceDialog.TableHeaderLengthWidth";
			case 3:
				return "CloudChoiceDialog.TableHeaderTypeWidth";
			}
			return "";
		}

		public String getNameKey() {
			switch (index) {
			case 0:
				return "CloudChoiceDialog.TableHeaderName";
			case 1:
				return "CloudChoiceDialog.TableHeaderTime";
			case 2:
				return "CloudChoiceDialog.TableHeaderLength";
			case 3:
				return "CloudChoiceDialog.TableHeaderType";
			}
			return "";
		}
	}

	private void clickTableItem() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			row = table.convertRowIndexToModel(row);
			ShowImageCell cell = (ShowImageCell) mdTable.getCellAt(row, 0);
			SRLItem item = (SRLItem) cell.getSymbol();
			// 显示这个目录下面的目录和文件
			if (item.isDirectory()) {
				VPath path = item.getPath();
				showDirectory(path);
			}
		}
	}

	class TableLKeyAdapter extends KeyAdapter {
//		public void keyPressed(KeyEvent e) {
//			int code = e.getKeyCode();
//			if (code == KeyEvent.VK_ENTER) {
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
			} else if(count == 1) {
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
				//					// 结果...
				//					SRLItem item = (SRLItem) cell.getSymbol();
				//					saves.add(item.getSRL());
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
				bf.append(";");
			}
			bf.append(cell.getText());
			// 结果...
			SRLItem item = (SRLItem) cell.getSymbol();
			saves.add(item.getSRL());
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
		int rowHeight = ConfigParser.splitInteger(UIManager.getString("CloudChoiceDialog.TableRowHeight"), 30);
		int headerHeight = ConfigParser.splitInteger(UIManager.getString("CloudChoiceDialog.TableHeaderHeight"), 28);

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

		// 不要空格
		table.setIntercellSpacing(new Dimension(0,0));

		table.addMouseListener(new TableMouseAdapter());
		table.addKeyListener(new TableLKeyAdapter());
		// 初始化排序
		initSorts();

		spTable = new JScrollPane(table);
		spTable.setColumnHeader(new HeightViewport(headerHeight));
		spTable.setBorder(new HighlightBorder(1));
	}

	class SRLListSelectionListener implements ListSelectionListener {

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
					SRLItem item = (SRLItem) mdList.getElementAt(index);
					// 进入子目录
					if (item != null && item.isDirectory()) {
						VPath path = item.getPath();
						showDirectory(path);
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
					SRLItem item = (SRLItem) mdList.getElementAt(index);
					if (item != null && item.isDirectory()) {
						VPath path = item.getPath();
						showDirectory(path);
					}
				}
			}
			else if(count == 1) {
				showListSelectItem();
			}
		}
	}

	private void showListSelectItem() {
		// 清除已经保存的
		saves.clear();

		StringBuilder buf = new StringBuilder();
		Object[] objs = list.getSelectedValues();
		for (int i = 0; i < objs.length; i++) {
			SRLItem item = (SRLItem) objs[i];
			if (buf.length() > 0) {
				buf.append(';');
			}
			buf.append(item.getName());
			// 保存目录
			saves.add(item.getSRL());
		}

		txtFile.setText(buf.toString());
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
		int cellHeight = ConfigParser.splitInteger(UIManager.getString("CloudChoiceDialog.ListCellHeight"), 28);
		int cellWidth = ConfigParser.splitInteger(UIManager.getString("CloudChoiceDialog.ListCellWidth"), 180);
		list.setFixedCellHeight(cellHeight);
		list.setFixedCellWidth(cellWidth);

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
		list.addComponentListener(new ListComponentAdatpter());
		list.addListSelectionListener(new SRLListSelectionListener());

		// 边框
		list.setBorder(new EmptyBorder(1,1,1,1));

		spList = new JScrollPane(list);
		spList.setBorder(new HighlightBorder(1));
	}

	/**
	 * 执行回车
	 */
	private void doTextFieldEnter() {
		String text = txtFile.getText();
		text = text.trim();
		if (text.isEmpty()) {
			String title = UIManager.getString("CloudChoiceDialog.EnterFileEmptyTitle");
			String content = UIManager.getString("CloudChoiceDialog.EnterFileEmptyContent");
			MessageBox.showWarning(this, title, content);
			return ;
		}

		// 单击执行
		clickOkayButton();

		//		// 保存信息
		//		saves.clear();
		//		
		//		String[] names = text.split(";");
		//		for (String name : names) {
		//			SRL srl = new SRL(selectSite);
		//			srl.setPath(selectPath.getPath() + "/" + name);
		//			saves.add(srl);
		//		}
	}

	class TextFieldLKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER) {
				doTextFieldEnter();
			}
		}
	}

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
		// 如果是打开对话框，输入文本框不可编辑
		if (isOpenDialog()) {
			txtFile.setEditable(false);
		}

		JPanel s1 = new JPanel();
		s1.setLayout(new GridLayout(rows, 1, 0, 6));
		s1.add(createLabel("CloudChoiceDialog.FileLabelText", 'F', txtFile, SwingConstants.LEFT));
		s1.add(createLabel("CloudChoiceDialog.TypeLabelText", 'T', boxType, SwingConstants.LEFT));
		if (isShowCharsetEncode()) {
			s1.add(createLabel("CloudChoiceDialog.EncodeLabelText", 'E', boxEncode, SwingConstants.LEFT));
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
		cmdCancel = createButton("CloudChoiceDialog.CancelButtonText", 'C');

		//  打开/保存/其它...
		if (isOpenDialog()) {
			cmdOkay = createButton("CloudChoiceDialog.OpenButtonText", 'O');
		} else if (isSaveDialog()) {
			cmdOkay = createButton("CloudChoiceDialog.SaveButtonText", 'S');
		} else {
			cmdOkay = new FlatButton("Okay");
			cmdOkay.addActionListener(this);
		}

		// 自定义文本
		if (approveButtonText != null) {
			cmdOkay.setText(approveButtonText);
		}
		if (approveButtonToolTipText != null) {
			cmdOkay.setToolTipText(approveButtonToolTipText);
		}

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 2, 4, 0));
		sub.add(cmdOkay);
		sub.add(cmdCancel);

		// 标签
		lblText = new JLabel();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(lblText, BorderLayout.CENTER);
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

	/**
	 * 初始化基本参数
	 */
	private void initDialog() {
		// 设置面板
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createPanel(), BorderLayout.CENTER);
	}

	/**
	 * 加载被选中的节点资源
	 */
	private void loadSiteResource() {
		int size = mdSRL.getSize();
		if (size > 0) {
			SRLRoot root = (SRLRoot) mdSRL.getSelectedItem();
			if (root != null) {
				SRL srl = root.getSRL();
				showSite(srl);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#doDefaultOnShow()
	 */
	@Override
	protected void doDefaultOnShow() {
		// 加载选中的节点资源
		loadSiteResource();
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

		// 返回结果
		return showModalDialog(parent, cmdCancel);
	}

	/**
	 * 以模态方式打开窗口
	 * @param parent
	 * @return 返回SRL对象对象实例，SRL[]数组或者单个SRL对象
	 */
	public SRL[] showDialog(Component parent) {
		return (SRL[]) showDialog(parent, true);
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
	}

}