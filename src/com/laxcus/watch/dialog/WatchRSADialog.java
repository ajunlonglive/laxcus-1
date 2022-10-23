/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * FIXP通信密钥生成对话窗口
 *
 * @author scott.liang
 * @version 1.0 9/29/2018
 * @since laxcus 1.0
 */
public class WatchRSADialog extends WatchCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 7229957947568650831L;

	private JComboBox cbxBit = new JComboBox();
	private JTextField txtPwd = new JTextField();

	private JTextArea txtPrivateModulus = new JTextArea();
	private JTextArea txtPrivateExponent = new JTextArea();

	private JTextArea txtPublicModulus = new JTextArea();
	private JTextArea txtPublicExponent = new JTextArea();

	private JButton cmdBuild = new JButton("Generate");
	private JButton cmdSave = new JButton("Save");
	private JButton cmdReset = new JButton("Reset");
	private JButton cmdExit = new JButton("Exit");

	private String privateModulus;
	private String privateExponent;
	private String publicModulus;
	private String publicExponent;

	/** 当前路径 **/
	private File currentDirectoty;

	/**
	 * 构造FIXP通信密钥生成对话窗口，指定参数
	 * @param parent 父窗口
	 * @param modal 模态或者否
	 * @throws HeadlessException
	 */
	public WatchRSADialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
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
	 * 执行关联操作
	 * @param event
	 */
	private void active(ActionEvent event) {
		if (event.getSource() == cmdBuild) {
			build();
		} else if (event.getSource() == cmdSave) {
			save();
		} else if (event.getSource() == cmdReset) {
			reset();
		} else if (event.getSource() == cmdExit) {
			exit();
		}
	}

	/**
	 * 退出
	 */
	private void exit() {
		String title = findCaption("Dialog/rsa/close/title");
		String content = findContent("Dialog/rsa/close");
		
		int who = MessageDialog.showMessageBox(this, title, JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		
		if (who == JOptionPane.YES_OPTION) {
			super.dispose();
		}
	}

	/**
	 * 重置参数
	 */
	private void reset() {
		txtPwd.setText("");
		txtPrivateModulus.setText("");
		txtPrivateExponent.setText("");
		txtPublicModulus.setText("");
		txtPublicExponent.setText("");

		privateModulus = null;
		privateExponent = null;
		publicModulus = null;
		publicExponent = null;

		cmdSave.setEnabled(false);
	}

	/**
	 * 在文本框中显示密钥
	 * @param area 文本框
	 * @param key 密钥文本
	 */
	private void showKey(JTextArea area, String key) {
		setFieldText(area, key);
	}

	/**
	 * 转成16进制值
	 * @param value
	 * @return
	 */
	private String toHex(BigInteger value) {
		return value.toString(16);
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

		String title = findCaption("Dialog/rsa/directory/title");
		String button = findCaption("Dialog/rsa/directory/button");
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
	 * 生成密钥
	 */
	private void build() {
		// 取密码
		String text = txtPwd.getText().trim();
		if (text.isEmpty()) {
			txtPwd.requestFocus(); // 获得焦点！
			return;
		}

		String item = (String) cbxBit.getSelectedItem();
		// 如果不是数字，忽略它
		if (!ConfigParser.isInteger(item)) {
			return;
		}
		int keysize = Integer.parseInt(item);
		// 如果小于512位，忽略它
		if (keysize < 512) {
			return;
		}

		// 所有语言的文本转成UTF8编码再输出
		byte[] pwd = new UTF8().encode(text);

		boolean success = false;

		// 生成RSA密钥
		try {
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keysize, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			// 转成16进制字符
			publicModulus = toHex(publicKey.getModulus());
			publicExponent = toHex(publicKey.getPublicExponent());
			privateModulus = toHex(privateKey.getModulus());
			privateExponent = toHex(privateKey.getPrivateExponent());

			// 显示文本
			showKey(txtPrivateModulus, privateModulus);
			showKey(txtPrivateExponent, privateExponent);
			showKey(txtPublicModulus, publicModulus);
			showKey(txtPublicExponent, publicExponent);

			cmdSave.setEnabled(success = true);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		} catch (java.security.InvalidParameterException e) {
			Logger.error(e);
		}

		if (success) {
			String title = findCaption("Dialog/rsa/build/okay/title");
			String content = findContent("Dialog/rsa/build/okay");			
			MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		} else {
			String title = findCaption("Dialog/rsa/build/failed/title");
			String content = findContent("Dialog/rsa/build/failed");
			MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		}
	}

	/**
	 * 生成XML文档
	 * @param rootword
	 * @param modulus
	 * @param exponent
	 * @return
	 */
	private byte[] buildXML(String rootword, String modulus, String exponent) {
		String key_modulus = "modulus";
		String key_exponent = "exponent";
		String m = String.format("<%s>%s</%s>", key_modulus, modulus, key_modulus);
		String e = String.format("<%s>%s</%s>", key_exponent, exponent, key_exponent);
		String code = String.format("<code>%s%s</code>", m , e);

		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		String root = String.format("<%s>%s</%s>", rootword, code, rootword);

		return new UTF8().encode(head + root);
	}

	/**
	 * 保存RSA到磁盘
	 */
	private void save() {
		// 选择存储目录
		File dir = choose(currentDirectoty);
		if (dir == null) {
			return;
		}
		currentDirectoty = dir;

		// 生成XML
		byte[] privateKey = buildXML("private-key", privateModulus, privateExponent);
		byte[] publicKey = buildXML("public-key", publicModulus, publicExponent);

		// 保存到磁盘
		boolean success = false;
		try {
			FileOutputStream writer = new FileOutputStream(new File(currentDirectoty, "rsakey.public"));
			writer.write(publicKey);
			writer.close();

			writer = new FileOutputStream(new File(currentDirectoty, "rsakey.private"));
			writer.write(privateKey);
			writer.close();

			success = true;
		} catch (IOException ex) {

		}

		if (success) {
			String title = findCaption("Dialog/rsa/save/okay/title");
			String content = findContent("Dialog/rsa/save/okay");
			content = String.format(content, currentDirectoty.getAbsolutePath());

//			JOptionPane.showMessageDialog(this, content, title, JOptionPane.INFORMATION_MESSAGE);
			MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		} else {
			String title = findCaption("Dialog/rsa/save/failed/title");
			String content = findContent("Dialog/rsa/save/failed");
//			JOptionPane.showMessageDialog(this, content, title, JOptionPane.ERROR_MESSAGE);
			MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
		}
	}
	
	

	/**
	 * 加密位
	 * @return
	 */
	private JPanel initEncrypt() {
		// 允许编辑
		cbxBit.setEditable(true);
		// 只输入数字
		Component editor = cbxBit.getEditor().getEditorComponent();
		if (Laxkit.isClassFrom(editor, JTextField.class)) {
			JTextField field = (JTextField) editor;
			field.setDocument(new DigitDocument(field, 5));
		}
		// 最小是512位
		for (int ks = 512; ks <= 5120; ks += 512) {
			String value = String.format("%d", ks);
			cbxBit.addItem(value);
		}
		// 显示第一个
		cbxBit.setSelectedIndex(0);
		

		JLabel one = new JLabel();
		setLabelText(one, findCaption("Dialog/rsa/encrypt/bits/title"));
		JLabel two = new JLabel();
		setLabelText(two, findCaption("Dialog/rsa/encrypt/password/title"));

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(5, 0));
		left.setBorder(new EmptyBorder(2, 5, 2, 2));
		left.add(one, BorderLayout.WEST);
		left.add(cbxBit, BorderLayout.CENTER);

		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(5, 0));
		right.setBorder(new EmptyBorder(2, 5, 2, 2));
		right.add(two, BorderLayout.WEST);
		right.add(txtPwd, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(15, 0));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		
		String title = findCaption("Dialog/rsa/encrypt/title");
		
		panel.setBorder(UITools.createTitledBorder(title));
		
		return panel;
	}

	/**
	 * 初始化私钥
	 * @return
	 */
	private JPanel initPrivateKey() {
		EmptyBorder empty = new EmptyBorder(2, 5, 2, 2);

		String title = findCaption("Dialog/rsa/private-key/title");
		
		JScrollPane sp1 = new JScrollPane(txtPrivateModulus);
		JScrollPane sp2 = new JScrollPane(txtPrivateExponent);
		
		JLabel one = new JLabel();
		setLabelText(one, findCaption("Dialog/rsa/private-key/modulus/title"));
		JLabel two = new JLabel();
		setLabelText(two, findCaption("Dialog/rsa/private-key/exponent/title"));

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 1, 3));
		left.setBorder(empty);
		left.add(one);
		left.add(two);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1, 1, 3));
		right.setBorder(empty);
		right.add(sp1);
		right.add(sp2);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 1));
//		panel.setBorder(border);
		panel.setBorder(UITools.createTitledBorder(title));
		
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 初始化公钥
	 * @return
	 */
	private JPanel initPublicKey() {
		EmptyBorder empty = new EmptyBorder(2, 5, 2, 2);
		
		String title = findCaption("Dialog/rsa/public-key/title");
	
		JScrollPane sp1 = new JScrollPane(txtPublicModulus);
		JScrollPane sp2 = new JScrollPane(txtPublicExponent);
		
		JLabel one = new JLabel();
		setLabelText(one, findCaption("Dialog/rsa/public-key/modulus/title"));
		JLabel two = new JLabel();
		setLabelText(two, findCaption("Dialog/rsa/public-key/exponent/title"));
		
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 1, 5));
		left.setBorder(empty);
		left.add(one);
		left.add(two);

		JPanel rigth = new JPanel();
		rigth.setLayout(new GridLayout(2, 1, 1, 5));
		rigth.setBorder(empty);
		rigth.add(sp1);
		rigth.add(sp2);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 1));
//		panel.setBorder(border);
		panel.setBorder(UITools.createTitledBorder(title));
		panel.add(left, BorderLayout.WEST);
		panel.add(rigth, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * 初始化按纽
	 * @return
	 */
	private JPanel initButtons() {
		setButtonText(cmdBuild, findCaption("Dialog/rsa/button/build/title"));
		cmdBuild.setMnemonic('B');
		cmdBuild.addActionListener(this);

		setButtonText(cmdSave, findCaption("Dialog/rsa/button/save/title"));
		cmdSave.setMnemonic('S');
		cmdSave.addActionListener(this);
		cmdSave.setEnabled(false);

		setButtonText(cmdReset, findCaption("Dialog/rsa/button/reset/title"));
		cmdReset.setMnemonic('R');
		cmdReset.addActionListener(this);

		setButtonText(cmdExit, findCaption("Dialog/rsa/button/exit/title"));
		cmdExit.setMnemonic('X');
		cmdExit.addActionListener(this);

		JPanel east = new JPanel();
		east.setLayout(new GridLayout(1, 4, 8, 0));
		east.add(cmdBuild);
		east.add(cmdSave);
		east.add(cmdReset);
		east.add(cmdExit);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(east, BorderLayout.EAST);

		return panel;
	}

	/**
	 * 生成面板
	 * @return
	 */
	private JPanel initPane() {
		JTextArea[] areas = new JTextArea[] { txtPrivateModulus,
				txtPrivateExponent, txtPublicModulus, txtPublicExponent };
		for (int i = 0; i < areas.length; i++) {
			areas[i].setEditable(false);
			areas[i].setLineWrap(true);
		}

		JPanel enrypt = initEncrypt();
		JPanel rsaPrivate = initPrivateKey();
		JPanel rsaPublic = initPublicKey();
		JPanel button = initButtons();

		JPanel center = new JPanel();
		center.setLayout(new GridLayout(2, 1));
		center.add(rsaPrivate);
		center.add(rsaPublic);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 2));
		setRootBorder(panel);
		panel.add(enrypt, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		panel.add(button, BorderLayout.SOUTH);

		return panel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new InvokeThread(event));
	}

	/**
	 * 定义边框
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

		int width = 700;
		int height = 462;

		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		Rectangle rect = new Rectangle(x, y, width, height);
		return rect;
	}

	/**
	 * 显示对话框
	 */
	public void showDialog() {
		setContentPane(initPane());

		// 标题
		setTitle(findCaption("Dialog/rsa/title"));
		// 按纽无效
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setBounds(getBound());

		// 检查对话框字体
		checkDialogFonts();
		
		setAlwaysOnTop(true);
		
		setVisible(true);
	}

}