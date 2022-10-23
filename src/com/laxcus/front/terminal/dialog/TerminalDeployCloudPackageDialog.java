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

import com.laxcus.command.cloud.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 发布云计算应用包对话框
 *
 * @author scott.liang
 * @version 1.0 3/25/2020
 * @since laxcus 1.0
 */
public class TerminalDeployCloudPackageDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 3977634690903786782L;

	/** 系统属性值 **/
	private final static String KEY_DEPLOY = "TERMINAL-DEPLOY-CLOUD-PACKAGE-PATH";
	
	private final static String KEY_BOUND = TerminalDeployCloudPackageDialog.class.getSimpleName() + "_BOUND";
	
	/** 类型定义 **/
	private int family;

	/** 显示的标题 **/
	private JLabel lblChoice = new JLabel();
	
	/** 在本地部署 **/
	private JCheckBox chkLocal = new JCheckBox();

	/** 按纽 **/
	private JButton cmdChoice = new JButton();

	private JButton cmdOK = new JButton();

	private JButton cmdCancel = new JButton();

	/** 读的文件 **/
	private String reader;
	
	/** 在本地发布 **/
	private boolean local;

	/** 确认接受 **/
	private boolean accepted;

	/**
	 * @param frame
	 * @param modal
	 * @param taskType CONDUCT/ESTABLISH/CONTACT三者之一
	 */
	public TerminalDeployCloudPackageDialog(Frame frame, boolean modal, int taskType) {
		super(frame, modal);
		family = taskType;
		accepted = false;
		local = false;
	}
	
	/**
	 * 在本地部署
	 * 
	 * @return 返回真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 返回“BUILD XXX PACKAGE”关键字对应的磁盘文件
	 * @return
	 */
	public String getReader() {
		return reader;
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
			boolean b = (reader != null);
			if (b) {
				saveBound();
				save();
				accepted = true;
				dispose();
			}
		} else if (e.getSource() == cmdCancel) {
			saveBound();
			reader = null;
			accepted = false;
			dispose();
		} else if(e.getSource() == cmdChoice) {
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
	 * 保存组件目录，以备下次再用时...
	 */
	private void save() {
		local = chkLocal.isSelected();
		
		File file = new File(reader);
		File path = file.getParentFile();
		UITools.putProperity(KEY_DEPLOY, path.toString());
	}

	/**
	 * 
	 * @return
	 */
	private String getWriterDialogTitle() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/DeployCloudPackage/Conduct/write-chooser/title/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/write-chooser/title/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/write-chooser/title/title");
		}
		return "";
	}

	private String getWriterDialogButtonText() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/DeployCloudPackage/Conduct/write-chooser/selected/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/write-chooser/selected/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/write-chooser/selected/title");
		}
		return "";
	}

	private String getWriterDialogDescription() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/DeployCloudPackage/Conduct/write-chooser/description/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/write-chooser/description/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/write-chooser/description/title");
		}
		return "";
	}

	private String getWriterDialogExtension() {
		switch (family) {
		case BuildCloudPackageTag.CONDUCT:
			return findCaption("Dialog/DeployCloudPackage/Conduct/write-chooser/extension/title");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/write-chooser/extension/title");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/write-chooser/extension/title");
		}
		return "";
	}

	/**
	 * 选择写入的目录和文件
	 */
	private void doWriter() {
		String title = getWriterDialogTitle();
		String buttonText = getWriterDialogButtonText();

		String ds = getWriterDialogDescription();
		String extension = getWriterDialogExtension();
		
		DiskFileFilter filter = new DiskFileFilter(ds, extension);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(filter);
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
			Object memoty = UITools.getProperity(KEY_DEPLOY);
			if (memoty != null && memoty.getClass() == String.class) {
				File path = new File((String) memoty);
				if (path.exists() && path.isDirectory()) {
					chooser.setCurrentDirectory(path);
				}
			}
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// 提取文件
		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			reader = Laxkit.canonical(file);
			FontKit.setLabelText(lblChoice, reader);
		} else {
			reader = null;
			FontKit.setLabelText(lblChoice, "");
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
		setButtonText(chkLocal, findCaption("Dialog/DeployCloudPackage/buttons/local/title"));
		
		setButtonText(cmdChoice, findCaption("Dialog/DeployCloudPackage/buttons/choice/title"));
		cmdChoice.addActionListener(this);

		setButtonText(cmdOK, findCaption("Dialog/DeployCloudPackage/buttons/okay/title"));
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		setButtonText(cmdCancel, findCaption("Dialog/DeployCloudPackage/buttons/cancel/title"));
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
			return findCaption("Dialog/DeployCloudPackage/Conduct/borderTitle");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/borderTitle");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/borderTitle");
		}
		return "";
	}

	/**
	 * 生成中央面板
	 * @return JPanel实例
	 */
	private JPanel createCenterPanel() {
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout(8, 1));
		top.add(BorderLayout.WEST, cmdChoice);
		top.add(BorderLayout.CENTER, lblChoice);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(BorderLayout.WEST, chkLocal);
		bottom.add(BorderLayout.CENTER, new JPanel());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(top, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);

		// 文件面板
		String title = getBorderTitle();
		panel.setBorder(UITools.createTitledBorder(title, 5));
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
			return findCaption("Dialog/DeployCloudPackage/Conduct/dialogTitle");
		case BuildCloudPackageTag.ESTABLISH:
			return findCaption("Dialog/DeployCloudPackage/Establish/dialogTitle");
		case BuildCloudPackageTag.CONTACT:
			return findCaption("Dialog/DeployCloudPackage/Contact/dialogTitle");
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
	public boolean showDialog() {
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