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
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.skin.*;
import com.laxcus.xml.*;

/**
 * 计算程序配置窗口
 * 
 * @author scott.liang
 * @version 1.0 8/10/2020
 * @since laxcus 1.0
 */
public class TerminalCreateTaskDialog extends TerminalCommonFontDialog implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -8345570047914638477L;

	/** 窗口范围 **/
	private final static String BOUND = TerminalCreateTaskDialog.class.getSimpleName() + "_BOUND";

	private final static String OPEN_KEY = TerminalCreateTaskDialog.class.getSimpleName() + "_OPEN";

	private final static String SAVE_KEY = TerminalCreateTaskDialog.class.getSimpleName() + "_SAVE";

	/** SIGN 区域 **/
	private JTextField txtSignName = new JTextField();

	private JComboBox boxSignType = new JComboBox(); 

	/** WARE 区域 **/
	private JTextField txtWareNaming = new JTextField();

	private JTextField txtWareVersion = new JTextField();

	private JTextField txtWareProductName = new JTextField();

	private JTextField txtWareProductDate = new JTextField();

	private JTextField txtWareMaker = new JTextField();

	private JTextArea txtWareComment = new JTextArea();

//	private JCheckBox cmdWareSelfly = new JCheckBox();

	/** TASK 区域 **/
	private JTextField txtTaskNaming = new JTextField();

	private JTextField txtTaskBootClass = new JTextField();

	private JTextField txtTaskProject = new JTextField();

	private JTextArea txtTaskResource = new JTextArea();

	/** 列表 **/
	private JList list = new JList();

	/** 模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 新增/导入单元/导出单元 **/
	private JButton cmdClear = new JButton();

	private JButton cmdRead = new JButton();

	private JButton cmdWrite = new JButton();

	private JButton cmdDelete = new JButton();

	/** 导入脚本/导出脚本 **/
	private JButton cmdImport = new JButton();

	private JButton cmdExport = new JButton();

	private JButton cmdExit = new JButton();

	/** 引导集合 **/
	private ArrayList<TaskToken> elements = new ArrayList<TaskToken>();

	/**
	 * @param owner
	 */
	public TerminalCreateTaskDialog(Frame owner, boolean model) {
		super(owner, model);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		addThread(new ListThread(e));
	}

	class ListThread extends SwingEvent {
		ListSelectionEvent event;

		ListThread(ListSelectionEvent e) {
			super();
			event = e;
		}

		public void process() {
			TaskToken token = getSelectToken();
			cmdRead.setEnabled(token != null);
		}
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;

		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}


	class ReadThread extends SwingEvent {
		File file;

		ReadThread(File e) {
			super();
			file = e;
		}

		public void process() {
			readTask(file);
		}
	}
	

	class WriteThread extends SwingEvent {
		File file;

		WriteThread(File e) {
			super();
			file = e;
		}

		public void process() {
			writeXML(file);
		}
	}

	
	/**
	 * 触发操作
	 * 
	 * @param e 激活事件
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdExit) {
			boolean success = exit();
			if (success) {
				saveBound();
				dispose();
			}
		} else if (e.getSource() == cmdImport) {
			// 导入脚本文件
			chooseFile();
		} else if (e.getSource() == cmdExport) {
			// 导出脚本文件
			saveFile();
		}
		// 单元
		else if (e.getSource() == cmdClear) {
			clear();
		} else if (e.getSource() == cmdRead) {
			read();
		} else if (e.getSource() == cmdWrite) {
			write();
		} else if(e.getSource() == cmdDelete) {
			delete();
		}
	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/CreateTask/exit/message/title");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, null, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle e = super.getBounds();
		if (e != null) {
			UITools.putProperity(TerminalCreateTaskDialog.BOUND, e);
		}
	}

	private TaskToken getSelectToken() {
		// 定位下标
		int index = list.getSelectedIndex();
		// 小于0或者大于成员数目时，忽略
		if (index < 0 || index >= model.size()) {
			return null;
		}
		// 找到指定位置的成员，处理它！
		Object value = model.elementAt(index);
		if (value.getClass() != TaskToken.class) {
			return null;
		}
		return (TaskToken) value;
	}

	/**
	 * 读取
	 */
	private void clear() {
		txtTaskNaming.setText("");
		txtTaskBootClass.setText("");
		txtTaskResource.setText("");
		txtTaskProject.setText("");
	}

	/**
	 * 切换显示
	 */
	private void read() {
		// 切换显示!
		TaskToken token = getSelectToken();
		if (token != null) {
			showTask(token);
		}
	}

	/**
	 * 写入参数
	 */
	private void write() {
		TaskToken token = new TaskToken();
		token.setNaming(txtTaskNaming.getText());
		token.setBootClass(txtTaskBootClass.getText());
		token.setProjectClass(txtTaskProject.getText());
		token.setResource(txtTaskResource.getText());

		// 判断参数有效
		boolean success = token.isEnabled();
		if (!success) {
			// 弹出错误，参数不足
			showMissing(txtTaskNaming);
			return;
		}

		// 判断参数重复
		success = elements.contains(token);
		if (success) {
			String title = findCaption("Dialog/CreateTask/duplicate/title");
			String content = findContent("Dialog/CreateTask/duplicate");
			content = String.format(content, token.getNaming());
			showWarming(content, title);
			return;
		}

		elements.add(token);
		// 保存记录
		model.addElement(token);
	}

	/**
	 * 删除选中的单元
	 */
	private void delete() {
		TaskToken token = getSelectToken();
		if (token != null) {
			elements.remove(token);
			model.removeElement(token);
		}
	}

	/** 选择的文件 **/
	private File selectFile;

	/**
	 * 选择图像文件
	 */
	private void chooseFile() {
		String title = findCaption("Dialog/CreateTask/open-chooser/title/title");
		String buttonText = findCaption("Dialog/CreateTask/open-chooser/choose/title");

		// XML文件
		String ds_xml = findCaption("Dialog/CreateTask/open-chooser/xml/description/title");
		String xml = findCaption("Dialog/CreateTask/open-chooser/xml/extension/title");

		DiskFileFilter f1 = new DiskFileFilter(ds_xml, xml);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f1);

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);

		// 文件！
		if (selectFile != null) {
			chooser.setCurrentDirectory(selectFile.getParentFile());
		}
		// 没有定义，从系统中取
		else {
			Object memory = UITools.getProperity(OPEN_KEY);
			if (memory != null && memory.getClass() == String.class) {
				File file = new File((String) memory);
				if (file.exists() && file.isFile()) {
					chooser.setCurrentDirectory(file.getParentFile());
				}
			}
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			addThread(new ReadThread(file));
		}
	}

	private byte[] readFile(File file) {
		try {
			byte[] b = new byte[(int)file.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}
	
	/**
	 * 读取签名
	 * @param file
	 */
	private void showSign(File file) {
		byte[] taskText = readFile(file);
		if (taskText == null) {
			return;
		}

		org.w3c.dom.Document document = XMLocal.loadXMLSource(taskText);
		if (document == null) {
			return;
		}

		// 用户签名
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(TaskMark.SIGN);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
			txtSignName.setText(element.getTextContent());
		} else {
			txtSignName.setText("");
		}
		
		// 阶段类型
		nodes = document.getElementsByTagName(TaskMark.PHASE);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
			String phase = element.getTextContent();
			phase = PhaseTag.translate(PhaseTag.translate(phase));
			boxSignType.setSelectedItem(phase);
		} else {
			boxSignType.setSelectedIndex(0); // 无效！
		}
	}

	/**
	 * 读取和显示配置
	 * @param file
	 */
	private void readTask(File file) {
		WareToken ware = null;
		java.util.List<TaskToken> subs = null;
		try {
			TaskConfigReader reader = new TaskConfigReader(file);
			ware = reader.readWareToken();
			subs = reader.readTaskTokens();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		boolean success = (ware != null && subs != null && subs.size() > 0);
		// 不成立，弹出对话框退出
		if (!success) {
			showParamError(txtWareNaming);
			return;
		}

		// 保存文件名
		selectFile = file;
		String filename = file.toString();
		UITools.putProperity(OPEN_KEY, filename);
		
		// 取出签名
		showSign(file);
		// 显示软件参数
		showWare(ware);

		// 清除旧的单元
		model.clear();
		// 显示新的单元
		for (TaskToken token : subs) {
			model.addElement(token);
		}
		// 保存全部
		elements.clear();
		elements.addAll(subs);
		// 显示第一个单元
		showTask(subs.get(0));
		list.setSelectedIndex(0);
	}

	private WareToken readWareToken() {
		WareToken token = new WareToken();
		token.setNaming(txtWareNaming.getText());
		token.setVersion(txtWareVersion.getText());
		token.setProductName(txtWareProductName.getText());
		token.setProductDate(txtWareProductDate.getText());
		token.setMaker(txtWareMaker.getText());
		token.setComment(txtWareComment.getText());
//		token.setSelfly(cmdWareSelfly.isSelected());
		return token;
	}

	/** 保存的文件 **/
	private File saveFile;

	/**
	 * 显示不足
	 * @param text
	 */
	private void showMissing(JComponent param) {
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/CreateTask/missing/title");
		String content = findContent("Dialog/CreateTask/missing"); 
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);

		// 获得焦点
		param.requestFocus();
	}

	/**
	 * 参数错误
	 * @param text
	 */
	private void showParamError(JComponent param) {
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/CreateTask/param-error/title");
		String content = findContent("Dialog/CreateTask/param-error"); 
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);

		// 获得焦点
		param.requestFocus();
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarming(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 显示正确消息
	 * @param content
	 * @param title
	 */
	private void showMessage(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 保存参数
	 */
	private void saveFile() {
		// 判断签名
		String name = txtSignName.getText();
		if (name.trim().isEmpty()) {
			showMissing(txtSignName);
			return;
		}
		// 判断类型
		String type = (String) boxSignType.getSelectedItem();
		if (!PhaseTag.isPhase(type)) {
			showMissing(boxSignType);
			return;
		}

		// 软件令牌
		WareToken token = readWareToken();
		boolean success = token.isEnabled();
		if (!success) {
			// 弹出错误，参数不足
			showMissing(txtWareNaming);
			return;
		}

		if (elements.isEmpty()) {
			// 弹出错误，参数不足
			showMissing(txtTaskNaming);
			return;
		}

		String title = findCaption("Dialog/CreateTask/save-chooser/title/title");
		String buttonText = findCaption("Dialog/CreateTask/save-chooser/save/title");

		// XML文件
		String ds_xml = findCaption("Dialog/CreateTask/save-chooser/xml/description/title");
		String xml = findCaption("Dialog/CreateTask/save-chooser/xml/extension/title");
		DiskFileFilter f1 = new DiskFileFilter(ds_xml, xml);
		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f1);

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);

		// 文件！
		if (saveFile != null) {
			chooser.setCurrentDirectory(saveFile.getParentFile());
		}
		// 没有定义，从系统中取
		else {
			Object memory = UITools.getProperity(SAVE_KEY);
			if (memory != null && memory.getClass() == String.class) {
				File file = new File((String) memory);
				if (file.exists() && file.isFile()) {
					chooser.setCurrentDirectory(file.getParentFile());
				}
			}
		}

		int val = chooser.showSaveDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		DiskFileFilter filter = (DiskFileFilter) chooser.getFileFilter();
		File file = chooser.getSelectedFile();
		// 判断符合名称要求
		if (!filter.accept(file)) {
			String filename = Laxkit.canonical(file);
			String[] exts = filter.getExtensions();
			filename = String.format("%s.%s", filename, exts[0]);
			file = new File(filename);
		}

		// 判断文件存在，是否覆盖
		if (file.exists() && file.isFile()) {
			success = override(file);
			if (!success) return;
		}
		
		// 写入磁盘
		addThread(new WriteThread(file));
	}

	/**
	 * 询问覆盖...
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	private boolean override(File file) {
		// 提示错误
		String title = findCaption("Dialog/CreateTask/override/title");
		String content = findContent("Dialog/CreateTask/override");
		String format = String.format(content, file.toString());
		// 选择...
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, null, format,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}
	
	/**
	 * 图像写入磁盘文件
	 * @param file
	 * @return 返回真或者假
	 */
	private boolean writeXML(File file) {
		// 签名域
		String name = txtSignName.getText();
		String type = (String) boxSignType.getSelectedItem();
		name = CloudToken.formatXML_CDATA(TaskMark.SIGN, name); // "sign"
		type = CloudToken.formatXML_CDATA(TaskMark.PHASE, type); //  "phase"

		// 软件域
		WareToken token = readWareToken();
		String wareText = token.buildXML();

		// 分布任务域
		StringBuilder suffix = new StringBuilder();
		for (TaskToken sub : elements) {
			suffix.append(sub.buildXML());
		}

		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		String body = CloudToken.formatXML("root", name + type + wareText + suffix.toString());

		boolean success = false;
		try {
			byte[] b = new UTF8().encode(head + body);
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}

		// 保存
		if (success) {
			saveFile = file;
			String filename = Laxkit.canonical(saveFile);
			UITools.putProperity(SAVE_KEY, filename);

			String title = findCaption("Dialog/CreateTask/write/success/title");
			String content = findContent("Dialog/CreateTask/write/success");
			content = String.format(content, filename);
			showMessage(content, title);
		} else {
			String text = findCaption("Dialog/CreateTask/write/failed/title");
			showWarming(text, getTitle());
		}

		return success;
	}

	/**
	 * 设置显示文本
	 * @param field
	 * @param text
	 */
	private void setText(JTextComponent field, String text) {
		if (text != null) {
			field.setText(text);
		} else {
			field.setText("");
		}
	}

	/**
	 * 显示引导单元
	 * @param token
	 */
	private void showTask(TaskToken token) {
		setText(txtTaskNaming, token.getNaming());
		setText(txtTaskBootClass, token.getBootClass());
		setText(txtTaskResource, token.getResource());
		setText(txtTaskProject,token.getProjectClass());
	}

	/**
	 * 显示软件
	 * @param token
	 */
	private void showWare(WareToken token) {
		setText(txtWareNaming, token.getNaming());
		setText(txtWareVersion, token.getVersion());
		setText(txtWareProductName, token.getProductName());
		setText(txtWareProductDate, token.getProductDate());
		setText(txtWareMaker, token.getMaker());
		setText(txtWareComment, token.getComment());
//		// 自用或者否
//		cmdWareSelfly.setSelected(token.isSelfly());
	}

	/**
	 * 返回范围
	 * @return Rectangle实例
	 */
	private Rectangle getBound() {
		// 面板范围
		Object obj = UITools.getProperity(TerminalCreateTaskDialog.BOUND);
		if (obj != null && obj.getClass() == Rectangle.class) {
			return (Rectangle) obj;
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 650;
		int height = 630;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}
	
	private void initControlsTooltip() {
		setToolTipText(list, findCaption("Dialog/CreateTask/list/tooltip"));

		setToolTipText(txtSignName, findCaption("Dialog/CreateTask/label/sign/name/tooltip"));
		setToolTipText(boxSignType, findCaption("Dialog/CreateTask/label/sign/phase/tooltip"));
		
		setToolTipText(txtWareNaming, findCaption("Dialog/CreateTask/label/ware/naming/tooltip"));
		setToolTipText(txtWareVersion, findCaption("Dialog/CreateTask/label/ware/version/tooltip"));
		setToolTipText(txtWareProductName, findCaption("Dialog/CreateTask/label/ware/product-name/tooltip"));
		setToolTipText(txtWareProductDate, findCaption("Dialog/CreateTask/label/ware/product-date/tooltip"));
		setToolTipText(txtWareMaker, findCaption("Dialog/CreateTask/label/ware/maker/tooltip"));
		setToolTipText(txtWareComment, findCaption("Dialog/CreateTask/label/ware/comment/tooltip"));

		setToolTipText(txtTaskNaming, findCaption("Dialog/CreateTask/label/task/naming/tooltip"));
		setToolTipText(txtTaskBootClass, findCaption("Dialog/CreateTask/label/task/boot-class/tooltip"));
		setToolTipText(txtTaskProject, findCaption("Dialog/CreateTask/label/task/project/tooltip"));
		setToolTipText(txtTaskResource, findCaption("Dialog/CreateTask/label/task/resource/tooltip"));
	}

	/**
	 * 初始化控制界面
	 */
	private void initControls() {
//		setButtonText(cmdWareSelfly, findCaption("Dialog/CreateTask/buttons/selfly/title"));
		setButtonText(cmdImport, findCaption("Dialog/CreateTask/buttons/import-script/title"));
		setButtonText(cmdExport, findCaption("Dialog/CreateTask/buttons/export-script/title"));
		cmdImport.setMnemonic('I');
		cmdExport.setMnemonic('E');

		setButtonText(cmdClear, findCaption("Dialog/CreateTask/buttons/append/title"));
		setButtonText(cmdRead, findCaption("Dialog/CreateTask/buttons/import-unit/title"));
		setButtonText(cmdWrite, findCaption("Dialog/CreateTask/buttons/export-unit/title"));
		setButtonText(cmdDelete, findCaption("Dialog/CreateTask/buttons/delete/title"));
		cmdClear.setMnemonic('C');
		cmdRead.setMnemonic('R');
		cmdWrite.setMnemonic('W');
		cmdDelete.setMnemonic('D');

		setButtonText(cmdExit, findCaption("Dialog/CreateTask/buttons/exit/title"));
		cmdExit.setMnemonic('X');		

		// 文档限制
		txtWareProductDate.setDocument(new DateTimeDocument(txtWareProductDate, 10));
		txtWareVersion.setDocument(new FloatDocument(txtWareVersion, 10));

		// 显示单元
		list.setCellRenderer(new TerminalWareTaskRenderer()); 
		list.setModel(model);

		// 行高度
		String value = findCaption("Window/CreateTask/list/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);

		list.setFixedCellHeight(rowHeight);
		list.setBorder(new EmptyBorder(3, 2, 2, 2)); // top, left, bottom, right
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选
		list.addListSelectionListener(this);

		// 显示单元
		boxSignType.addItem("NONE");
		int[] types = PhaseTag.enumlate();
		for (int type : types) {
			String who = PhaseTag.translate(type);
			boxSignType.addItem(who);
		}
		boxSignType.setSelectedIndex(0);
		
		// 设置工具提示
		initControlsTooltip();
	}

	/**
	 * 设置监听接口
	 */
	private void initListeners() {
		// 显示单元
		cmdClear.addActionListener(this);
		cmdRead.addActionListener(this);
		cmdWrite.addActionListener(this);
		cmdDelete.addActionListener(this);
		// 导入脚本/导出脚本
		cmdImport.addActionListener(this);
		cmdExport.addActionListener(this);
		// 退出
		cmdExit.addActionListener(this);
	}

	/**
	 * 生成一个标签
	 * @param xmlPath
	 * @return
	 */
	private JLabel createLabel(String xmlPath) {
		String caption = findCaption(xmlPath);
		JLabel label = new JLabel(caption);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		return label;
	}

	/**
	 * 生成“Ware”左侧面板
	 * @param keys
	 * @return
	 */
	private JPanel createWareLeftPanel(String[] keys) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(keys.length, 1, 0, 5));
		for (String key : keys) {
			String xmlPath = String.format("Dialog/CreateTask/label/ware/%s/title", key);
			panel.add(createLabel(xmlPath));
		}
		return panel;
	}

	private JPanel createWareAbove() {
		String[] keys = new String[] { "naming", "version", "product-name", "product-date", "maker" };
		JPanel leftTop = createWareLeftPanel(keys);

		JPanel rightTop = new JPanel();
		rightTop.setLayout(new GridLayout(keys.length, 1, 0, 5));
		rightTop.add(txtWareNaming);
		rightTop.add(txtWareVersion);
		rightTop.add(txtWareProductName);
		rightTop.add(txtWareProductDate);
		rightTop.add(txtWareMaker);

		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(8, 0));
		sub.add(leftTop, BorderLayout.WEST);
		sub.add( rightTop, BorderLayout.CENTER);
		return sub;
	}

	private JPanel createWareBottom() {
		// 标签
		JLabel label = createLabel("Dialog/CreateTask/label/ware/comment/title");
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBorder(new EmptyBorder(2, 2, 0, 22));
		// 注释，显示3行
		txtWareComment.setRows(3);
		txtWareComment.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(txtWareComment,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(txtWareComment.getBackground());
		scroll.setMinimumSize(new Dimension(50, 60));

		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(8, 0));
		sub.add(label, BorderLayout.WEST);
		sub.add(scroll, BorderLayout.CENTER);
		return sub;
	}

	private JPanel createWarePanel() {
		// 整合后输出!
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(createWareAbove(), BorderLayout.NORTH);
		panel.add(createWareBottom(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 生成“Ware”左侧面板
	 * @param keys
	 * @return
	 */
	private JPanel createTaskLeftPanel(String[] keys) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(keys.length, 1, 0, 5));
		for (String key : keys) {
			String xmlPath = String.format("Dialog/CreateTask/label/task/%s/title", key);
			panel.add(createLabel(xmlPath));
		}
		return panel;
	}

	private JPanel createTaskAbove() {
		String[] keys = new String[] { "naming", "boot-class", "project"};
		JPanel left = createTaskLeftPanel(keys);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(keys.length, 1, 0, 5));
		right.add(txtTaskNaming);
		right.add(txtTaskBootClass);
		right.add(txtTaskProject);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTaskBottom() {
		// 标签
		JLabel label = createLabel("Dialog/CreateTask/label/task/resource/title");
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBorder(new EmptyBorder(2, 2, 0, 20));
		// 注释，显示3行
		txtTaskResource.setRows(3);
		txtTaskResource.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(txtTaskResource,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(txtTaskResource.getBackground());
		scroll.setMinimumSize(new Dimension(50, 60));

		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(8, 0));
		sub.add(label, BorderLayout.WEST);
		sub.add(scroll, BorderLayout.CENTER);
		return sub;
	}

	private JPanel createUnitButtonPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 4, 6, 0));
		sub.add(cmdClear);
		sub.add(cmdDelete);
		sub.add(cmdRead);
		sub.add(cmdWrite);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(sub, BorderLayout.WEST);
		return panel;
	}
	
	/**
	 * 输出签名面板
	 * @return
	 */
	private JPanel createSign() {
		JLabel name = createLabel("Dialog/CreateTask/label/sign/name/title");
		name.setVerticalAlignment(SwingConstants.CENTER);
		name.setBorder(new EmptyBorder(2, 2, 2, 2));

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(5, 0));
		left.add(name, BorderLayout.WEST);
		left.add(txtSignName, BorderLayout.CENTER);

		JLabel type = createLabel("Dialog/CreateTask/label/sign/phase/title");
		type.setVerticalAlignment(SwingConstants.CENTER);
		type.setBorder(new EmptyBorder(2, 2, 2, 2));
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(5, 0));
		right.add(type, BorderLayout.WEST);
		right.add(boxSignType, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		panel.add(left, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		return panel;
	}

	/**
	 * 任务面板
	 * @return
	 */
	private JPanel createTaskPanel() {
		// 整合后输出!
		JPanel above = new JPanel();
		above.setLayout(new BorderLayout(0, 5));
		above.add(createTaskAbove(), BorderLayout.NORTH);
		above.add(createTaskBottom(), BorderLayout.CENTER);
		
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(0, 5));
		left.add(above, BorderLayout.CENTER);
		left.add(createUnitButtonPanel(), BorderLayout.SOUTH);

		JScrollPane scroll = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(150, 10));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		panel.add(left, BorderLayout.CENTER);
		panel.add(scroll, BorderLayout.EAST);
		return panel;
	}

	private JPanel createCenterPanel() {
		JPanel sign = createSign();
		JPanel ware = createWarePanel() ;
		JPanel task = createTaskPanel();

		String caption = findCaption("Dialog/CreateTask/panel/ware/title");
		ware.setBorder(UITools.createTitledBorder(caption, 3)); // 边框
		
		caption = findCaption("Dialog/CreateTask/panel/task/title");
		task.setBorder(UITools.createTitledBorder(caption, 3)); // 边框
		
		caption = findCaption("Dialog/CreateTask/panel/sign/title");
		sign.setBorder(UITools.createTitledBorder(caption, 3)); // 边框

		JPanel sub = new JPanel();
		BoxLayout layout = new BoxLayout(sub, BoxLayout.Y_AXIS);
		sub.setLayout(layout);
		sub.add(sign);
		sub.add(ware);
		sub.add(task);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.NORTH);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 生成按纽面板
	 * @return 返回JPanel实例
	 */
	private JPanel createButtonPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 2, 6, 0));
		sub.add(cmdImport);
		sub.add(cmdExport);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		panel.add(sub, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(cmdExit, BorderLayout.EAST);
		return panel;
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private Container createRootPanel() {
		// 初始化显示控件
		initControls();
		// 建立事件监听
		initListeners();

		// 滚动面板
		JScrollPane scroll = new JScrollPane(createCenterPanel());
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 5));
		setRootBorder(panel);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		// 设置面板
		setContentPane(createRootPanel());

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(200, 320));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/CreateTask/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}

}