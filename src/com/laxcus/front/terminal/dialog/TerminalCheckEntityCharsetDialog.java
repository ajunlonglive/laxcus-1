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

import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 文件字符集对话窗口，数据上传
 * 
 * @author scott.liang
 * @version 1.0 9/29/2019
 * @since laxcus 1.0
 */
public class TerminalCheckEntityCharsetDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = -6804810792137080741L;

	/** 文件列表框 **/
	private JList details = new JList();

	/** 文件显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 选择文件 **/
	private JButton cmdChoose = new JButton();

	/** 删除列表上的文件 **/
	private JButton cmdDelete = new JButton();

	/** 取消按纽 **/
	private JButton cmdCancel = new JButton();

	/** 确定按纽 **/
	private JButton cmdOK = new JButton();

	/** 文件记录 ！**/
	private ArrayList<String> array = new ArrayList<String>();

	/** 数据结果 **/
	private String result;

	/**
	 * 构造文件字符集窗口
	 * @param parent 父窗口
	 * @param modal 模态
	 */
	public TerminalCheckEntityCharsetDialog(Frame parent, boolean modal) {
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
		String title = findCaption("Dialog/CheckEntityCharset/warning/title");
		String content = findContent("Dialog/CheckEntityCharset/warning");
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/CheckEntityCharset/exit/message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 整合生成命令输出
	 */
	private void fire() {
		String cmd = "CHECK ENTITY CHARSET ";
		int size = array.size();
		for (int index = 0; index < size; index++) {
			cmd += array.get(index);
			if (index + 1 < size) {
				cmd += ",";
			}
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
		String title = findCaption("Dialog/CheckEntityCharset/file-chooser/title/title");
		String buttonText = findCaption("Dialog/CheckEntityCharset/file-chooser/selected/title");
		
		// TEXT文件
		String ds_text = findCaption("Dialog/CheckEntityCharset/file-chooser/text/description/title");
		String text = findCaption("Dialog/CheckEntityCharset/file-chooser/text/extension/title");
		// CSV文件
		String ds_csv = findCaption("Dialog/CheckEntityCharset/file-chooser/csv/description/title");
		String csv = findCaption("Dialog/CheckEntityCharset/file-chooser/csv/extension/title");
		
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
			model.addElement(filename);
			// 目录
			root = file.getParentFile();
		}
	}

	/**
	 * 删除已经选中的文件
	 */
	private void deleteFiles() {
		int[] indices = details.getSelectedIndices();
		if (indices == null || indices.length == 0) {
			return;
		}
		// 保存！
		ArrayList<String> a = new ArrayList<String>();
		for (int index : indices) {
			Object e = model.elementAt(index);
			if (e.getClass() == String.class) {
				a.add((String) e);
			}
		}
		// 删除
		for (String e : a) {
			model.removeElement(e);
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
		details.setCellRenderer(new TerminalFileCellRenderer());
		details.setModel(model);

		// 提示文本
		setToolTipText(details, findCaption("Dialog/CheckEntityCharset/list/file/tooltip"));

		setButtonText(cmdChoose, findCaption("Dialog/CheckEntityCharset/buttons/choose/title"));
		cmdChoose.addActionListener(this);

		setButtonText(cmdDelete, findCaption("Dialog/CheckEntityCharset/buttons/delete/title"));
		cmdDelete.addActionListener(this);

		setButtonText(cmdOK, findCaption("Dialog/CheckEntityCharset/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/CheckEntityCharset/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
	}

	/**
	 * 建立数据文件面板
	 * @return JPanel
	 */
	private JPanel createFilePanel() {
		// 上部面板
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout(0, 0));
		south.add(createFileButtonPanel(), BorderLayout.WEST);

		// 整个面板
		JScrollPane bottom = new JScrollPane(details);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(south, BorderLayout.SOUTH);
		panel.add(bottom, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 建立数据操作按纽面板
	 * @return
	 */
	private JPanel createFileButtonPanel() {
		JPanel north = new JPanel();
		north.setLayout(new GridLayout(1, 2, 8, 0));
		north.add(cmdChoose);
		north.add(cmdDelete);
		return north;
	}

	/**
	 * 生成中央面板
	 * @return JPanel实例
	 */
	private JPanel createCenterPanel() {
		// 文件面板，中心位置
		JPanel center = createFilePanel();

		// 文件面板
		String fileText = findCaption("Dialog/CheckEntityCharset/panel/file/title");
		
//		CompoundBorder fileCB = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(3, 4, 3, 4));
//		TitledBorder fileBorder = new TitledBorder(fileCB, fileText, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
//				createTitledBorderFont(fileText));
//		center.setBorder(fileBorder);
		
		center.setBorder(UITools.createTitledBorder(fileText));

		// 生成面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 0));
		panel.add(center, BorderLayout.CENTER);

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
		String title = findCaption("Dialog/CheckEntityCharset/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);
	}
}