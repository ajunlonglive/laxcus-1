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

import javax.swing.*;
import javax.swing.filechooser.*;

import com.laxcus.command.cloud.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 生成云计算应用包对话框
 *
 * @author scott.liang
 * @version 1.0 3/24/2020
 * @since laxcus 1.0
 */
public class TerminalBuildCloudPackageDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	/** 属性值，写入UIMamanager **/
	private final static String KEY_READER = "TERMINAL-BUILD-CLOUD-READ-PATH";
	
	private final static String KEY_WRITER = "TERMINAL-BUILD-CLOUD-WRITE-PATH";
	
	private final static String KEY_BOUND = TerminalBuildCloudPackageDialog.class.getSimpleName() + "_BOUND";

	/** 类型定义 **/
	private int family;

	/** 显示的标题 **/
	private JLabel lblChoice = new JLabel();

	private JLabel lblSave = new JLabel();

	/** 按纽 **/
	private JButton cmdChoice = new JButton();

	private JButton cmdSave = new JButton();

	private JButton cmdOK = new JButton();

	private JButton cmdCancel = new JButton();

	/** 读取的源文件 **/
	private String reader;

	/** 写入的文件 **/
	private String writer;

	/** 确认接受 **/
	private boolean accepted;
	
	/**
	 * @param frame
	 * @param modal
	 * @param taskType CONDUCT/ESTABLISH/CONTACT之一
	 */
	public TerminalBuildCloudPackageDialog(Frame frame, boolean modal, int taskType) {
		super(frame, modal);
		family = taskType;
		accepted = false;
	}

	/**
	 * 返回“IMPORT BY”关键字对话的磁盘文件
	 * @return
	 */
	public String getReader() {
		return reader;
	}

	/**
	 * 返回“BUILD XXX PACKAGE”关键字对应的磁盘文件
	 * @return
	 */
	public String getWriter() {
		return writer;
	}

	/**
	 * 调用线程
	 *
	 * @author scott.liang
	 * @version 1.0 3/24/2020
	 * @since laxcus 1.0
	 */
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

	/**
	 * 事件点击
	 * @param e 事件
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			boolean b = check();
			if (b) {
				saveBound();
				save();
				accepted = true;
				dispose();
			}
		} else if (e.getSource() == cmdCancel) {
			saveBound();
			reader = null;
			writer = null;
			accepted = false;
			dispose();
		} else if(e.getSource() == cmdChoice) {
			doReader();
		} else if(e.getSource() == cmdSave) {
			doWriter();
		}
	}
	
	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle e = super.getBounds();
		if (e != null) {
			UITools.putProperity(KEY_BOUND, e);
		}
	}
	
	/**
	 * 保存默认的目录
	 */
	private void save() {
		File file = new File(reader);
		File readPath = file.getParentFile();
		UITools.putProperity(KEY_READER, readPath.toString());

		file = new File(writer);
		File writePath = file.getParentFile();
		UITools.putProperity(KEY_WRITER, writePath.toString());
	}

	/**
	 * 检查参数
	 * @return
	 */
	private boolean check() {
		return reader != null && writer != null;
	}

	/** 选择的文件类型选项 **/
	private static String selectRead;

	/**
	 * 选择读的文件
	 */
	private void doReader() {
		// 对话窗口标题和确认按纽
		String title = findCaption("Dialog/BuildCloudPackage/read-chooser/title/title");
		String buttonText = findCaption("Dialog/BuildCloudPackage/read-chooser/selected/title");
		
		String ds_all = findCaption("Dialog/BuildCloudPackage/read-chooser/description_all/title");
		String all = findCaption("Dialog/BuildCloudPackage/read-chooser/extension_all/title");

		// 文本文件
		String ds_txt = findCaption("Dialog/BuildCloudPackage/read-chooser/description_text/title");
		String txt = findCaption("Dialog/BuildCloudPackage/read-chooser/extension_text/title");
		
		// 文本文件
		String ds_script = findCaption("Dialog/BuildCloudPackage/read-chooser/description_script/title");
		String script = findCaption("Dialog/BuildCloudPackage/read-chooser/extension_script/title");
		
		DiskFileFilter f0 = new DiskFileFilter(ds_all, all, true);
		DiskFileFilter f1 = new DiskFileFilter(ds_txt, txt);
		DiskFileFilter f2 = new DiskFileFilter(ds_script, script);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		
		chooser.addChoosableFileFilter(f0);
		chooser.addChoosableFileFilter(f1);
		chooser.addChoosableFileFilter(f2);
		
		// 找到选项
		chooseFileFilter(chooser, selectRead);

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title); 
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText); 

		// 目录
		if (reader != null) {
			File root = new File(reader);
			File parent = root.getParentFile();
			if (parent != null) {
				chooser.setCurrentDirectory(parent);
			}
		} else {
			Object memory = UITools.getProperity(KEY_READER);
			if (memory != null && memory.getClass() == String.class) {
				File path = new File((String) memory);
				chooser.setCurrentDirectory(path);
			}
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		selectRead = saveFileFileter(chooser);
		
		// 提取文件
		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			String path = Laxkit.canonical(file);
			reader = path;
			FontKit.setLabelText(lblChoice, path);
		} else {
			reader = null;
			FontKit.setLabelText(lblChoice, "");
		}
	}

	private String getWriterDialogTitle() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/BuildCloudPackage/Conduct/write-chooser/title/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/BuildCloudPackage/Establish/write-chooser/title/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/BuildCloudPackage/Contact/write-chooser/title/title");
		}
		return "";
	}

	private String getWriterDialogDescription() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/BuildCloudPackage/Conduct/write-chooser/description/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/BuildCloudPackage/Establish/write-chooser/description/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/BuildCloudPackage/Contact/write-chooser/description/title");
		}
		return "";
	}

	private String getWriterDialogExtension() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/BuildCloudPackage/Conduct/write-chooser/extension/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/BuildCloudPackage/Establish/write-chooser/extension/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/BuildCloudPackage/Contact/write-chooser/extension/title");
		}
		return "";
	}

	/**
	 * 选择写入的目录和文件
	 */
	private void doWriter() {
		String title = getWriterDialogTitle();

		String ds_dll = getWriterDialogDescription();
		String extension = getWriterDialogExtension();

		FileNameExtensionFilter filter = new FileNameExtensionFilter(ds_dll, extension);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title); 
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);

		// 目录
		if (writer != null) {
			File root = new File(writer);
			File parent = root.getParentFile();
			if (parent != null) {
				chooser.setCurrentDirectory(parent);
			}
		} else if (reader != null) {
			// 目录
			File root = new File(reader);
			File parent = root.getParentFile();
			if (parent != null) {
				chooser.setCurrentDirectory(parent);
			}
		} else {
			Object memory = UITools.getProperity(KEY_WRITER);
			if (memory != null && memory.getClass() == String.class) {
				File path = new File((String) memory);
				if (path.exists() && path.isDirectory()) {
					chooser.setCurrentDirectory(path);
				}
			}
		}
		
		// 保存文件
		int val = chooser.showSaveDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// 提取文件
		File file = chooser.getSelectedFile();
		String path = Laxkit.canonical(file);
		if (!path.toLowerCase().endsWith(extension.toLowerCase())) {
			path = String.format("%s.%s", path, extension);
		}
		writer = path;
		FontKit.setLabelText(lblSave, path);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 系统中取出参数
		Object e = UITools.getProperity(KEY_BOUND);
		if (e != null && e.getClass() == Rectangle.class) {
			return (Rectangle) e;
		}
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 530;
		int height = 230;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 初始化组件
	 */
	private void initControls() {		
		setButtonText(cmdChoice, findCaption("Dialog/BuildCloudPackage/buttons/choice/title"));
		cmdChoice.addActionListener(this);
		cmdChoice.setHorizontalAlignment(SwingConstants.LEFT);

		setButtonText(cmdSave, findCaption("Dialog/BuildCloudPackage/buttons/save/title"));
		cmdSave.addActionListener(this);
		cmdSave.setHorizontalAlignment(SwingConstants.LEFT);

		setButtonText(cmdOK, findCaption("Dialog/BuildCloudPackage/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/BuildCloudPackage/buttons/cancel/title"));
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
		cmdCancel.setDefaultCapable(true);
	}

	/**
	 * 根据类型生成不同的标题
	 * @return 返回字符串
	 */
	private String getBorderTitle() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/BuildCloudPackage/Conduct/borderTitle");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/BuildCloudPackage/Establish/borderTitle");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/BuildCloudPackage/Contact/borderTitle");
		}
		return "";
	}

	/**
	 * 生成中央面板
	 * @return JPanel实例
	 */
	private JPanel createCenterPanel() {
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 1, 8));
		left.add(cmdChoice);
		left.add(cmdSave);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1, 1, 8));
		right.add(lblChoice);
		right.add(lblSave);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(8, 1));
		panel.add(BorderLayout.WEST, left);
		panel.add(BorderLayout.CENTER, right);

		// 文件面板
		String title = getBorderTitle();
		panel.setBorder(UITools.createTitledBorder(title));

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
	 * 根据类型生成不同的标题
	 * @return 返回字符串
	 */
	private String getDialogTitle() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/BuildCloudPackage/Conduct/dialogTitle");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/BuildCloudPackage/Establish/dialogTitle");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/BuildCloudPackage/Contact/dialogTitle");
		}
		return "";
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
		panel.add(createCenterPanel(), BorderLayout.NORTH);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 显示窗口
	 */
	public boolean showDialog() {;
		setContentPane(initPanel());

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 200));
		setAlwaysOnTop(true);

		// 标题
		String title = getDialogTitle();
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);

		// 返回结果
		return accepted;
	}

}