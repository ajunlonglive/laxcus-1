/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.script;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.choice.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 管理员账号文件生成窗口
 *
 * @author scott.liang
 * @version 1.0 3/29/2021
 * @since laxcus 1.0
 */
public class RayDBAScriptDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = 8052561827289011432L;

	/** 句柄 **/
	static RayDBAScriptDialog selfHandle;

	/**
	 * 返回句柄
	 * @return 句柄
	 */
	public static RayDBAScriptDialog getInstance() {
		return RayDBAScriptDialog.selfHandle;
	}

	/** 账号文本框 **/
	private FlatTextField txtName = new FlatTextField();
	private FlatPasswordField txtPwd = new FlatPasswordField();
	private FlatPasswordField txtAgain = new FlatPasswordField();
	private FlatTextField txtMembers = new FlatTextField();

	/** 签名按纽 **/
	private FlatButton cmdName = new FlatButton();

	private FlatButton cmdPwd = new FlatButton();

	/** 操作按纽 **/
	private FlatButton cmdBuild = new FlatButton();

	private FlatButton cmdReset = new FlatButton();

	private FlatButton cmdExit = new FlatButton();

	/**
	 * 构造默认窗口
	 */
	public RayDBAScriptDialog()  {
		super();
	}
	
	/**
	 * 关闭窗口
	 */
	public void closeWindow() {
		super.closeWindow();
		RayDBAScriptDialog.selfHandle = null;
	}
	
	/**
	 * 选择退出或者否
	 */
	private void exit() {
		String title = UIManager.getString("DBADialog.CloseTitle");
		String content = UIManager.getString("DBADialog.CloseContent");

		// 显示对话框
		boolean success = MessageBox.showYesNoDialog(this, title, content);
		if (success) {
			writeBounds();
			closeWindow();
		}
	}
	
//	/**
//	 * 选择退出或者否
//	 */
//	private void exit() {
//		String title = findCaption("Dialog/dba/close/title");
//		String content = findContent("Dialog/dba/close");
//		
//		int who = MessageDialog.showMessageBox(this, title,
//				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
//		if (who == JOptionPane.YES_OPTION) {
//			dispose();
//		}
//	}

//	class InvokeThread extends SwingEvent {
//		ActionEvent event;
//		InvokeThread(ActionEvent e) {
//			super();
//			event = e;
//		}
//		public void process() {
//			active(event);
//		}
//	}

	/**
	 * 激活
	 * @param e
	 */
	private void click(ActionEvent e) {
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
		// addThread(new InvokeThread(e));
		click(e);
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
		MessageBox.showInformation(this, title, content);
//		MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 生成用户名
	 */
	private void doUsername() {
		String text = txtName.getText();
		if (text.trim().isEmpty()) {
			txtName.requestFocus();
			return;
		}

		text = doSHA256(text.trim());
		String title = UIManager.getString("DBADialog.UsernameSignText");
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

		String title = UIManager.getString("DBADialog.PasswordSignText");
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
	
//	/**
//	 * 选择一个新的保存目录
//	 * @param dir
//	 * @return
//	 */
//	private File choose(File dir) {
//		JFileChooser chooser = new JFileChooser();
//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		if (dir != null) {
//			chooser.setCurrentDirectory(dir);
//		}
//
//		String title = findCaption("Dialog/dba/directory/title");
//		String button = findCaption("Dialog/dba/directory/button");
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setDialogTitle(title);
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//
//		chooser.setApproveButtonText(button); //"选择你要的目录");
//		int val = chooser.showOpenDialog(this);
//		// 显示窗口
//		if (val != JFileChooser.APPROVE_OPTION) {
//			return null;
//		}
//		// 返回选择的文件
//		return chooser.getSelectedFile();
//	}

	/**
	 * 选择一个新的保存目录
	 * @param dir
	 * @return
	 */
	private File choose() {
		String title = UIManager.getString("DBADialog.DirectoryTitle");  
		String buttonText = UIManager.getString("DBADialog.DirectoryButtonText"); 
		
		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setShowCharsetEncode(false);
		dialog.setMultiSelectionEnabled(false); // 只能选择一个
		dialog.setDialogType(DialogOption.SAVE_DIALOG);
		dialog.setFileSelectionMode(DialogOption.DIRECTORIES_ONLY);	
//		dialog.setAcceptAllFileFilterUsed(false);
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);
		
		// 设置目录
		File dir = readPath();
		if (dir != null) {
			File parent = dir.getParentFile();
			if (parent != null) {
				dialog.setCurrentDirectory(parent);
			} else {
				dialog.setCurrentDirectory(dir);
			}
		}

		File[] files = dialog.showDialog(this);
		int size = (files != null ? files.length : 0);
		if (size > 0) {
			writePath(files[0]);
			return files[0];
		}
		return null;
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
			String title = UIManager.getString("DBADialog.ParamMissing.Title"); // findCaption("Dialog/dba/error/param-missing/title");
			String content = UIManager.getString("DBADialog.ParamMissing.UseranemText"); // findContent("Dialog/dba/error/param-missing/username");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			
			txtName.requestFocus();
			return;
		}
		if (password.length == 0) {
			String title = UIManager.getString("DBADialog.ParamMissing.Title"); // findCaption("Dialog/dba/error/param-missing/title");
			String content = UIManager.getString("DBADialog.ParamMissing.PasswordText"); // findContent("Dialog/dba/error/param-missing/password");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}
		if (password2.length == 0) {
			String title = UIManager.getString("DBADialog.ParamMissing.Title"); // findCaption("Dialog/dba/error/param-missing/title");
			String content = UIManager.getString("DBADialog.ParamMissing.PasswordText"); // findContent("Dialog/dba/error/param-missing/password");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtAgain.requestFocus();
			return;
		}

		if (password.length != password2.length) {
			String title = UIManager.getString("DBADialog.ParamMissing.PasswordTitle"); // findCaption("Dialog/dba/error/password/title");
			String content = UIManager.getString("DBADialog.ParamMissing.PasswordContent"); // findContent("Dialog/dba/error/password/title");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}
		// 判断是数字
		if (ConfigParser.splitInteger(members, -1) == -1) {
			String title = UIManager.getString("DBADialog.ParamMissing.Title"); // findCaption("Dialog/dba/error/param-missing/title");
			String content = UIManager.getString("DBADialog.ParamMissing.MembersText"); // findContent("Dialog/dba/error/param-missing/members");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title,
//					JOptionPane.WARNING_MESSAGE, content,
//					JOptionPane.DEFAULT_OPTION);

			txtMembers.requestFocus();
			return;
		}

		boolean match = true;
		for (int i = 0; i < password.length; i++) {
			match = (password[i] == password2[i]);
			if (!match) break;
		}
		if (!match) {
			String title = UIManager.getString("DBADialog.ParamMissing.PasswordTitle"); // findCaption("Dialog/dba/error/password/title");
			String content = UIManager.getString("DBADialog.ParamMissing.PasswordContent"); // findContent("Dialog/dba/error/password/title");
			MessageBox.showWarning(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
			txtPwd.requestFocus();
			return;
		}

		// 用户名生成SHA256编码
		username = doSHA256(username.toLowerCase());
		// 密码生成SHA512编码
		String pwd = doSHA512(new String(password));

		// 选择一个你要的磁盘目录
		File dir = choose();
		if (dir == null) {
			return;
		}
		
		// 本地文件
		File file = new File(dir, "dba.xml");
		
//		System.out.printf("this is [%s]\n", Laxkit.canonical(file));

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
			String title = UIManager.getString("DBADialog.Result.OkayTitle"); // findCaption("Dialog/dba/result/okay/title");
			String content = UIManager.getString("DBADialog.Result.OkayContent"); // findContent("Dialog/dba/result/okay");
			content = String.format(content, file.getAbsolutePath());
			MessageBox.showInformation(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		} else {
			String title = UIManager.getString("DBADialog.Result.FailedTitle"); // findCaption("Dialog/dba/result/failed/title");
			String content = UIManager.getString("DBADialog.Result.FailedContent"); // findContent("Dialog/dba/result/failed");
			MessageBox.showFault(this, title, content);
			
//			MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
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
		username.setText(UIManager.getString("DBADialog.UsernameText"));
//		setLabelText(username, findCaption("Dialog/dba/username/title"));
		JLabel password = new JLabel();
		password.setText(UIManager.getString("DBADialog.PasswordText"));
//		setLabelText(password, findCaption("Dialog/dba/password/title"));
		JLabel password2 = new JLabel();
		password2.setText(UIManager.getString("DBADialog.AgainText"));
//		setLabelText(password2, findCaption("Dialog/dba/again/title"));
		JLabel members = new JLabel();
		members.setText(UIManager.getString("DBADialog.MembersText"));
//		setLabelText(members, findCaption("Dialog/dba/members/title"));

		// 签名
//		setButtonText(cmdName, findCaption("Dialog/dba/name_sign/title")); 
		cmdName.setText(UIManager.getString("DBADialog.NameSignText"));
		cmdName.setMnemonic('S');
		cmdName.addActionListener(this);
		
//		setButtonText(cmdPwd, findCaption("Dialog/dba/pwd_sign/title"));
		cmdPwd.setText(UIManager.getString("DBADialog.PwdSignText"));
		cmdPwd.setMnemonic('P');
		cmdPwd.addActionListener(this);

		txtName.addActionListener(this);
		txtPwd.addActionListener(this);
		txtAgain.addActionListener(this);
		txtMembers.addActionListener(this);
		// 只限数字
		txtMembers.setDocument(new DigitDocument(txtMembers, 3));

		username.setDisplayedMnemonic('U');
		password.setDisplayedMnemonic('P');
		password2.setDisplayedMnemonic('A');
		members.setDisplayedMnemonic('M');
		username.setLabelFor(txtName);
		password.setLabelFor(txtPwd);
		password2.setLabelFor(txtAgain);
		members.setLabelFor(txtMembers);
		
		String title = UIManager.getString("DBADialog.AccountText"); // findCaption("Dialog/dba/account/title");
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
		//		setButtonText(cmdReset,findCaption("Dialog/dba/buttons/reset/title"));
		cmdReset.setText(UIManager.getString("DBADialog.ButtonResetText"));
		cmdReset.setMnemonic('R');
		cmdReset.addActionListener(this);

		//		setButtonText(cmdBuild,findCaption("Dialog/dba/buttons/build/title"));
		cmdBuild.setText(UIManager.getString("DBADialog.ButtonBuildText"));
		cmdBuild.setMnemonic('B');
		cmdBuild.addActionListener(this);

		//		setButtonText(cmdExit, findCaption("Dialog/dba/buttons/exit/title"));
		cmdExit.setText(UIManager.getString("DBADialog.ButtonExitText"));
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
		// setRootBorder(panel);
		panel.setLayout(new BorderLayout(5, 10));
		panel.add(enrypt, BorderLayout.NORTH);
		panel.add(button, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));

		return panel;
	}

//	/**
//	 * 生成范围
//	 * @return
//	 */
//	private Rectangle getBound() {
//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
//
//		int width = 550;
//		int height = 330;
//
//		int x = (size.width - width) / 2;
//		int y = (size.height - height) / 2;
//		return new Rectangle(x, y, width, height);
//	}
	
	private void writePath(File file) {
		RTKit.writeFile(RTEnvironment.ENVIRONMENT_SYSTEM, "AdminDialog/Directory", file);
	}

	private File readPath() {
		return RTKit.readFile(RTEnvironment.ENVIRONMENT_SYSTEM,"AdminDialog/Directory");
	}

	private void writeBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "AdminDialog/Bound", rect);
	}

	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"AdminDialog/Bound");
		if (bounds == null) {
			int w = 550;
			int h = 330;

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (d.width - w) / 2;
			int y = (d.height - h) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
			bounds = new Rectangle(x, y, w, h);
		}
		return bounds;
	}

	/**
	 * 设置显示范围
	 * @param desktop
	 */
	private void setBounds() {
		setBounds(readBounds());
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

//	/**
//	 * 显示对话框
//	 */
//	public void showDialog() {
//		setContentPane(initPane());
//		
////		JPanel panel = initPane();
////		Container root = getContentPane();
////		root.setLayout(new BorderLayout());
////		root.add(panel, BorderLayout.CENTER);
//
//		// 标题
//		setTitle(findCaption("Dialog/dba/title"));
//		// 按纽无效
//		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//
//		setBounds(getBound());
//		setAlwaysOnTop(true);
//		
//		// 检查对话框字体
//		checkDialogFonts();
//
//		setVisible(true);
//	}

	/**
	 * 初始化对话框
	 */
	private void initDialog() {
		setTitle(UIManager.getString("DBADialog.Title"));
		setFrameIcon(UIManager.getIcon("DBADialog.TitleIcon"));
		setContentPane(initPane());

		//		setFrameIcon(UIManager.getIcon("BuildDialog.TitleIcon"));
		//
		//		JPanel panel = new JPanel();
		//		panel.setLayout(new BorderLayout(0, 4));
		//		panel.add(createCenter(), BorderLayout.NORTH);
		//		panel.add(createSouth(), BorderLayout.SOUTH);
		//		panel.setBorder(new EmptyBorder(4, 6, 6, 6));
		//
		//		setContentPane(panel);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.gui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		setBounds();
		
		RayDBAScriptDialog.selfHandle = this;

		if (modal) {
			return super.showModalDialog(parent);
		} else {
			return super.showNormalDialog(parent);
		}
	}

}