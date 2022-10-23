/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 管理员账号文件生成窗口
 *
 * @author scott.liang
 * @version 1.0 9/29/2018
 * @since laxcus 1.0
 */
public class WatchDBADialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8052561827289011432L;

	/** 账号文本框 **/
	private JTextField txtName = new JTextField();
	private JPasswordField txtPwd = new JPasswordField();
	private JPasswordField txtAgain = new JPasswordField();
	private JTextField txtMembers = new JTextField();

	/** 签名按纽 **/
	private JButton cmdName = new JButton();

	private JButton cmdPwd = new JButton();

	/** 操作按纽 **/
	private JButton cmdBuild = new JButton();

	private JButton cmdReset = new JButton();

	private JButton cmdExit = new JButton();

	/** 选择的目录 **/
	private File currentDirectoty;

	/**
	 * 构造DBA窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public WatchDBADialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}
	
	/**
	 * 选择退出或者否
	 */
	private void exit() {
		String title = findCaption("Dialog/dba/close/title");
		String content = findContent("Dialog/dba/close");
		
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		if (who == JOptionPane.YES_OPTION) {
			dispose();
		}
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;
		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}
		public void process() {
			active(event);
		}
	}

	/**
	 * 激活
	 * @param e
	 */
	private void active(ActionEvent e) {
		// 文本框
		if (e.getSource() == txtName) {
			txtPwd.requestFocus();
		} else if (e.getSource() == txtPwd) {
			txtAgain.requestFocus();
		} else if (e.getSource() == txtAgain) {
			txtMembers.requestFocus();
		} else if(e.getSource() == txtMembers) {
			txtName.requestFocus();
		}
		// 签名
		else if (e.getSource() == cmdName) {
			doUsername();
		} else if (e.getSource() == cmdPwd) {
			doPassword();
		}
		// 操作
		else if (e.getSource() == cmdReset) {
			reset();
		} else if (e.getSource() == cmdBuild) {
			build();
		} else if (e.getSource() == cmdExit) {
			exit();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}
	
	/**
	 * 显示对话框
	 * @param title
	 * @param text
	 */
	private void showBox(String title, String text) {
		StringBuilder bf = new StringBuilder();
		int unit = 64;
		for (int seek = 0; seek < text.length(); seek += unit) {
			if (bf.length() > 0) {
				bf.append("<br>");
			}
			int len = (text.length() - seek > unit ? unit : text.length() - seek);
			String sub = text.substring(seek, seek + len);
			bf.append(sub);
		}

		String content = String.format("<html><body>%s</body></html>", bf.toString());
		MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 生成用户名
	 */
	private void doUsername() {
		String text = txtName.getText();
		if(text.trim().isEmpty()) {
			txtName.requestFocus();
			return;
		}

		text = doSHA256(text.trim());
		String title = findCaption("Dialog/dba/username/sign");
		showBox(title, text);
	}

	/**
	 * 生成密码
	 */
	private void doPassword() {
		char[] pwd = txtPwd.getPassword();
		if (pwd.length == 0) {
			txtPwd.requestFocus();
			return;
		}
		String text = doSHA512(new String(pwd));
		
		String title = findCaption("Dialog/dba/password/sign");
		showBox(title, text);
	}

	/**
	 * 重置文本框
	 */
	private void reset() {
		txtName.setText("");
		txtPwd.setText("");
		txtAgain.setText("");
		txtMembers.setText("");
	}
	
	/**
	 * 选择一个新的保存目录
	 * @param dir
	 * @return
	 */
	private File choose(File dir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (dir != null) {
			chooser.setCurrentDirectory(dir);
		}

		String title = findCaption("Dialog/dba/directory/title");
		String button = findCaption("Dialog/dba/directory/button");
		chooser.setMultiSelectionEnabled(false);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);

		chooser.setApproveButtonText(button); //"选择你要的目录");
		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		// 返回选择的文件
		return chooser.getSelectedFile();
	}

	/**
	 * 生成管理员账号XML文档
	 */
	private void build() {
		String username = txtName.getText();
		char[] password = txtPwd.getPassword();
		char[] password2 = txtAgain.getPassword();
		String members = txtMembers.getText();
		if(username.isEmpty()) {
			String title = findCaption("Dialog/dba/error/param-missing/title");
			String content = findContent("Dialog/dba/error/param-missing/username");
			
			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			
			txtName.requestFocus();
			return;
		}
		if (password.length == 0) {
			String title = findCaption("Dialog/dba/error/param-missing/title");
			String content = findContent("Dialog/dba/error/param-missing/password");

			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}
		if (password2.length == 0) {
			String title = findCaption("Dialog/dba/error/param-missing/title");
			String content = findContent("Dialog/dba/error/param-missing/password");

			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtAgain.requestFocus();
			return;
		}

		if (password.length != password2.length) {
			String title = findCaption("Dialog/dba/error/password/title");
			String content = findContent("Dialog/dba/error/password/title");
			
			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}
		// 判断是数字
		if (ConfigParser.splitInteger(members, -1) == -1) {
			String title = findCaption("Dialog/dba/error/param-missing/title");
			String content = findContent("Dialog/dba/error/param-missing/members");

			MessageDialog.showMessageBox(this, title,
					JOptionPane.WARNING_MESSAGE, content,
					JOptionPane.DEFAULT_OPTION);

			txtMembers.requestFocus();
			return;
		}

		boolean match = true;
		for (int i = 0; i < password.length; i++) {
			match = (password[i] == password2[i]);
			if (!match) break;
		}
		if (!match) {
			String title = findCaption("Dialog/dba/error/password/title");
			String content = findContent("Dialog/dba/error/password/title");
			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}

		// 用户名生成SHA256编码
		username = doSHA256(username.toLowerCase());
		// 密码生成SHA512编码
		String pwd = doSHA512(new String(password));

		// 选择一个你要的磁盘目录
		File dir = choose(currentDirectoty);
		if (dir == null) {
			return;
		}
		// 记录这个目录
		currentDirectoty = dir;

		// 本地文件
		File file = new File(currentDirectoty, "dba.xml");

		// 生成XML文档
		byte[] xml = buildXML(username, pwd, Integer.parseInt(members));

		boolean success = false;
		try {
			FileOutputStream writer = new FileOutputStream(file);
			writer.write(xml);
			writer.close();
			success = true;
		} catch (IOException ex) {

		}
		
		if (success) {
			String title = findCaption("Dialog/dba/result/okay/title");
			String content = findContent("Dialog/dba/result/okay");
			content = String.format(content, file.getAbsolutePath());

			MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		} else {
			String title = findCaption("Dialog/dba/result/failed/title");
			String content = findContent("Dialog/dba/result/failed");
			MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		}
	}

	/**
	 * 生成XML文档
	 * @param username
	 * @param password
	 * @return
	 */
	private byte[] buildXML(String username, String password, int members) {
		String rootword = "root";
		String key1 = "username";
		String key2 = "password";
		String key3 = "members";
		String s1 = String.format("<%s>%s</%s>", key1, username, key1);
		String s2 = String.format("<%s>%s</%s>", key2, password, key2);
		String s3 = String.format("<%s>%s</%s>", key3, members, key3);
		String code = String.format("<account>%s</account>", s1 + s2+s3);

		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String root = String.format("<%s>%s</%s>", rootword, code, rootword);

		return new UTF8().encode( head + root);
	}

	/**
	 * 生成用户签名（SHA256）
	 * @param text 文本
	 * @return SHA256签名
	 */
	private String doSHA256(String text) {
		return Laxkit.doSHA256Hash(text).toString();
	}

	/**
	 * 生成账号密码签名（SHA512）
	 * @param text 文本
	 * @return SHA512签名
	 */
	private String doSHA512(String text) {
		return Laxkit.doSHA512Hash(text).toString();
	}

	private JPanel initTextPanel() {
		// 参数名
		JLabel username = new JLabel();
		setLabelText(username, findCaption("Dialog/dba/username/title"));
		JLabel password = new JLabel();
		setLabelText(password, findCaption("Dialog/dba/password/title"));
		JLabel password2 = new JLabel();
		setLabelText(password2, findCaption("Dialog/dba/again/title"));
		JLabel members = new JLabel();
		setLabelText(members, findCaption("Dialog/dba/members/title"));

		// 签名
		setButtonText(cmdName, findCaption("Dialog/dba/name_sign/title")); 
		cmdName.setMnemonic('S');
		cmdName.addActionListener(this);
		
		setButtonText(cmdPwd, findCaption("Dialog/dba/pwd_sign/title"));
		cmdPwd.setMnemonic('P');
		cmdPwd.addActionListener(this);

		txtName.addActionListener(this);
		txtPwd.addActionListener(this);
		txtAgain.addActionListener(this);
		txtMembers.addActionListener(this);
		// 只限数字
		txtMembers.setDocument(new DigitDocument(txtMembers, 2));

		username.setDisplayedMnemonic('U');
		password.setDisplayedMnemonic('P');
		password2.setDisplayedMnemonic('A');
		members.setDisplayedMnemonic('M');
		username.setLabelFor(txtName);
		password.setLabelFor(txtPwd);
		password2.setLabelFor(txtAgain);
		members.setLabelFor(txtMembers);
		
		String title = findCaption("Dialog/dba/account/title");
//		Font font = createTitledBorderFont(title);
//		CompoundBorder com = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(10, 15, 10, 5));
//		TitledBorder border = new TitledBorder(com, title, 
//				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, font);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(4, 1, 0, 10));
		p1.add(username);
		p1.add(password);
		p1.add(password2);
		p1.add(members);

		JPanel p11 = new JPanel();
		p11.setLayout(new BorderLayout(5, 0));
		p11.add(txtName, BorderLayout.CENTER);
		p11.add(cmdName, BorderLayout.EAST);

		JPanel p12 = new JPanel();
		p12.setLayout(new BorderLayout(5, 0));
		p12.add(txtPwd, BorderLayout.CENTER);
		p12.add(cmdPwd, BorderLayout.EAST);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(4, 1, 0, 10));
		p2.add(p11);
		p2.add(p12);
		p2.add(txtAgain);
		p2.add(txtMembers);

		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(5, 0));
		p3.add(p1, BorderLayout.WEST);
		p3.add(p2, BorderLayout.CENTER);
//		p3.setBorder(border);
		p3.setBorder(UITools.createTitledBorder(title));
		return p3;
	}

	/**
	 * 初始化按纽
	 * @return
	 */
	private JPanel initButtonPanel() {
		setButtonText(cmdReset,findCaption("Dialog/dba/buttons/reset/title"));
		cmdReset.setMnemonic('R');
		cmdReset.addActionListener(this);

		setButtonText(cmdBuild,findCaption("Dialog/dba/buttons/build/title"));
		cmdBuild.setMnemonic('B');
		cmdBuild.addActionListener(this);

		setButtonText(cmdExit, findCaption("Dialog/dba/buttons/exit/title"));
		cmdExit.setMnemonic('X');
		cmdExit.addActionListener(this);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 3, 8, 0));
		p1.add(cmdBuild);
		p1.add(cmdReset);
		p1.add(cmdExit);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(new JPanel(), BorderLayout.CENTER);
		p2.add(p1, BorderLayout.EAST);

		return p2;
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel initPane() {
		JPanel enrypt = initTextPanel();
		JPanel button = initButtonPanel();

		JPanel panel = new JPanel();
//		panel.setBorder(new EmptyBorder(6, 10, 6, 10));
		setRootBorder(panel);
		panel.setLayout(new BorderLayout(5, 10));
		panel.add(enrypt, BorderLayout.NORTH);
		panel.add(button, BorderLayout.SOUTH);
		
		return panel;
	}

	/**
	 * 生成范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

		int width = 550;
		int height = 330;

		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return 抽取的标签属性
//	 */
//	private String getCaption(String xmlPath) {
//		return WatchLauncher.getInstance().findCaption(xmlPath);
//	}
//
//	/**
//	 * 解析内容
//	 * @param xmlPath
//	 * @return 抽取的文本
//	 */
//	private String findContent(String xmlPath) {
//		return WatchLauncher.getInstance().findContent(xmlPath);
//	}

	/**
	 * 显示对话框
	 */
	public void showDialog() {
		setContentPane(initPane());
		
//		JPanel panel = initPane();
//		Container root = getContentPane();
//		root.setLayout(new BorderLayout());
//		root.add(panel, BorderLayout.CENTER);

		// 标题
		setTitle(findCaption("Dialog/dba/title"));
		// 按纽无效
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setBounds(getBound());
		setAlwaysOnTop(true);
		
		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}

}