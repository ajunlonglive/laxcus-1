/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.access.diagram.*;
import com.laxcus.front.*;
import com.laxcus.front.terminal.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.login.*;
import com.laxcus.util.net.*;
import com.laxcus.util.skin.*;

/**
 * 图形前端登录窗口
 * 
 * @author scott.liang
 * @version 1.2 9/20/2012
 * @since laxcus 1.0
 */
public class TerminalLoginDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 7962518727514615920L;

	private JLabel lblUsername = new JLabel("Username", SwingConstants.RIGHT);
	private JLabel lblPassword = new JLabel("Password", SwingConstants.RIGHT);
	private JLabel lblHost = new JLabel("IP Address", SwingConstants.RIGHT);
	private JLabel lblPort = new JLabel("Port", SwingConstants.RIGHT);

	private JTextField txtUser = new JTextField();
	private JPasswordField txtPwd = new JPasswordField();

	private JTextField txtHost = new JTextField();
	private JTextField txtPort = new JTextField();
	
	private JPasswordField txtLicence = new JPasswordField();
	private JCheckBox cmdLicence = new JCheckBox();

	private JButton cmdOK = new JButton("Login");
	private JButton cmdCancel = new JButton("Cancel");

	private boolean canceled;

	private char echoChar = 0;

	/**
	 * 构造图形前端登录窗口
	 * @param frame 主窗口
	 * @throws HeadlessException
	 */
	public TerminalLoginDialog(Frame frame) {
		super(frame);
		canceled = false;

		setLabelText(lblUsername, findCaption("Dialog/Login/Account/Username/title"));
		setLabelText(lblPassword, findCaption("Dialog/Login/Account/Password/title"));
		setLabelText(lblHost, findCaption("Dialog/Login/Server/Address/title"));
		setLabelText(lblPort, findCaption("Dialog/Login/Server/Port/title"));
		
		setButtonText(cmdLicence, findCaption("Dialog/Login/Button/Licence/title"));
		setButtonText(cmdOK, findCaption("Dialog/Login/Button/Okay/title"));
		setButtonText(cmdCancel, findCaption("Dialog/Login/Button/Cancel/title"));
		
		// 回显字符，加密的！
		echoChar = txtLicence.getEchoChar();
	}

	/**
	 * 销毁窗口
	 */
	public void destroy() {
		dispose();
	}

	/**
	 * 关闭对话窗口
	 */
	private void cancel() {
		canceled = true;
		destroy();
	}

	/**
	 * 根据用户签名加载他的许可证
	 */
	private void loadLicence() {
		char[] words = txtLicence.getPassword();
		String signature = null;
		if (words != null && words.length > 0) {
			signature = new String(words);
		}
		TerminalLauncher.getInstance().setSignature(signature);
		TerminalLauncher.getInstance().loadLicence(false);
	}

	/**
	 * 登录注册
	 */
	private void login() {
		TerminalLoginThread thread = new TerminalLoginThread(this);
		boolean success = check(thread);
		if (success) {
			thread.start();
		}
	}
	
	/**
	 * 处理加载许可证和登录
	 */
	private void process() {
		// 加载许可证
		loadLicence();
		// 登录注册
		login();
	}
	
	/**
	 * 切换密码显示
	 */
	private void switchPassword() {
		boolean select = cmdLicence.isSelected();
		char word = (select ? echoChar : 0);
		txtLicence.setEchoChar(word);
		txtLicence.requestFocus();
	}

	/**
	 * 执行登录
	 * @param e
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == txtLicence) {
			txtHost.requestFocus();
		} else if (e.getSource() == txtHost) {
			txtPort.requestFocus();
		} else if (e.getSource() == txtPort) {
			txtUser.requestFocus();
		} else if (e.getSource() == txtUser) {
			txtPwd.requestFocus();
		} else if (e.getSource() == txtPwd) {
			process();
		}
		// 按纽
		else if(e.getSource() == cmdLicence) {
			switchPassword();
		}
		else if (e.getSource() == cmdOK) {
			process(); // 加载许可证，启动登录线程
		} else if (e.getSource() == cmdCancel) {
			cancel();
		}
	}

	class ActionThread extends SwingEvent {
		ActionEvent event;

		ActionThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ActionThread(e));
	}

	/**
	 * 判断已经取消
	 * @return
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showFailed(String content, String title) {
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/failed.png", 32, 32);

		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 格式化和显示错误！
	 * @param xmlMessage
	 * @param xmlTitle
	 */
	private void showXMLFailed(String xmlMessage, String xmlTitle) {
		String title = findCaption(xmlTitle);
		String content = findContent(xmlMessage);
		showFailed(content, title);
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarning(String content, String title) {
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/warning.png", 32, 32);

		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 格式化和显示错误！
	 * @param xmlMessage
	 * @param xmlTitle
	 */
	private void showXMLWarning(String xmlMessage, String xmlTitle) {
		String title = findCaption(xmlTitle);
		String content = findContent(xmlMessage);
		showWarning(content, title);
	}

	/**
	 * 显示错误
	 * @param host
	 * @param port
	 */
	void showLoginFailed(String host, String port, int who, int pitchId, SiteHost pitchHub) {
		String title = findCaption("Dialog/Login/error/connect-failed/title");
		String content = "";

		if (who == FrontEntryFlag.ENTRANCE_FAULT) {
			content = findContent("Dialog/Login/error/entrance-failed");
		} else if (who == FrontEntryFlag.CANNOT_REDIRECT) {
			content = findContent("Dialog/Login/error/redirect-failed");
		} else if (who == FrontEntryFlag.GATE_FAULT) {
			content = findContent("Dialog/Login/error/gate-failed");
		} else if(who == FrontEntryFlag.NAT_FAULT) {
			content = findContent("Dialog/Login/error/nat-failed");
		} else if(who == FrontEntryFlag.MAX_USER) {
			content = findContent("Dialog/Login/error/max-user");
		} else if(who == FrontEntryFlag.LOGIN_TIMEOUT) {
			content = findContent("Dialog/Login/error/login-timeout");
		} else if(who == FrontEntryFlag.SERVICE_MISSING) {
			content = findContent("Dialog/Login/error/service-missing");
		} else if(who == FrontEntryFlag.REFLECT_FAULT) {
			showPitchFailed(pitchId, pitchHub);
			return;
		} else if(who == FrontEntryFlag.MAX_RETRY) {
			content = findContent("Dialog/Login/error/max-retry");
		} else if(who == FrontEntryFlag.LICENCE_NAT_REFUSE) {
			content = findContent("Dialog/Login/error/licence-nat-refuse");
			showWarning(content, title);
			return;
		} else if (who == FrontEntryFlag.VERSION_NOTMATCH) {
			content = findContent("Dialog/Login/error/version-notmatch");
		}
		// 其它错误
		else {
			content = findContent("Dialog/Login/error/connect-failed");
			content = String.format(content, host, port);
		}

		showFailed(content, title);
	}

	/**
	 * 显示PITCH错误
	 * @param pitchId
	 * @param pitchHub
	 */
	private void showPitchFailed(int pitchId, SiteHost pitchHub) {
		if (pitchId == FrontPitch.NOT_FOUND) {
			String host = String.format("%s:%d", pitchHub.getAddress(), pitchHub.getUDPort());
			String title = findCaption("Dialog/Login/error/pitch/not-found/title");
			String content = findContent("Dialog/Login/error/pitch/not-found");
			content = String.format(content, host);
			showFailed(content, title);
		} else {
			String title = findCaption("Dialog/Login/error/connect-failed/title");
			String content = findContent("Dialog/Login/error/reflect-failed");
			showFailed(content, title);
		}
	}

	/**
	 * 注册到ENTRANCE/GATE节点
	 * @return 成功返回“真”，失败“假”。
	 */
	private boolean check(TerminalLoginThread thread) {
		String username = txtUser.getText().trim();
		String password = new String(txtPwd.getPassword()).trim();
		String host = txtHost.getText().trim();
		String port = txtPort.getText().trim();

		Logger.debug(this, "login", "login to hub...");

		// 依次判断：主机、端口、用户名、密码
		if (host.length() == 0) {
			showXMLWarning("Dialog/Login/error/server", "Dialog/Login/error/server/title");
			txtHost.requestFocus();
			return false;
		}
		if (port.length() == 0) {
			showXMLWarning("Dialog/Login/error/port", "Dialog/Login/error/port/title");
			txtPort.requestFocus();
			return false;
		}
		if (username.length() == 0) {
			showXMLWarning("Dialog/Login/error/username", "Dialog/Login/error/username/title");
			txtUser.requestFocus();
			return false;
		}
		if (password.length() == 0) {
			showXMLWarning("Dialog/Login/error/password", "Dialog/Login/error/password/title");
			txtPwd.requestFocus();
			return false;
		}

		// 判断签名，必须一致，要么是SHA签名，要么是明文
		boolean sha256 = SHA256Hash.validate(username);
		boolean sha512 = SHA512Hash.validate(password);
		boolean success = ((sha256 && sha512) || (!sha256 && !sha512));
		if (!success) {
			showXMLWarning("Dialog/Login/error/account", "Dialog/Login/error/account/title"); 
			txtUser.requestFocus();
			return false;
		}

		// 转换主机端口
		int iPort = Integer.parseInt(port);
		// TCP/UDP的端口相同
		try {
			Address address = new Address(host);
			new SiteHost(address, iPort, iPort);
		} catch (Exception e) {
			showXMLFailed("Dialog/Login/error/unhost", "Dialog/Login/error/unhost/title"); 

			// 检查参数
			if (iPort < 0 || iPort >= 0xFFFF) {
				txtPort.requestFocus();
			} else {
				txtHost.requestFocus();
			}
			return false;
		}

		// 保存这些参数
		thread.setUser(username, password);
		thread.setHub(host, port);

		return true;
	}

	/**
	 * 初始化图形控件
	 */
	private void createControls() {
		JTextField[] fields = { txtUser, txtPwd, txtHost, txtPort, txtLicence };
		for (int i = 0; i < fields.length; i++) {
			fields[i].setPreferredSize(new Dimension(120, 26));
			fields[i].addActionListener(this);
		}

		setToolTipText(txtHost, findCaption("Dialog/Login/Server/Address/tooltip"));
		setToolTipText(txtPort, findCaption("Dialog/Login/Server/Port/tooltip"));
		setToolTipText(txtUser, findCaption("Dialog/Login/Account/Username/tooltip"));
		setToolTipText(txtPwd, findCaption("Dialog/Login/Account/Password/tooltip"));
		setToolTipText(txtLicence, findCaption("Dialog/Login/Licence/Sign/tooltip"));
	
		txtHost.setDocument(new GraphDocument(txtHost, 128));
		txtPort.setDocument(new DigitDocument(txtPort, 5));

		// 匿名
		cmdLicence.addActionListener(this);
		cmdLicence.setMnemonic('N');
		cmdLicence.setSelected(true); // 默认不显示
		
		cmdOK.addActionListener(this);
		cmdCancel.addActionListener(this);
		cmdOK.setMnemonic('L');
		cmdCancel.setMnemonic('C');
		
		// 取出系统中的签名，显示它!
		String signature = TerminalLauncher.getInstance().getSignature();
		if (signature != null) {
			txtLicence.setText(signature);
		}
	}

	/**
	 * 初始化服务器参数输入面板
	 * @return
	 */
	private JPanel createServerPanel() {
		lblHost.setDisplayedMnemonic('a');
		lblHost.setLabelFor(txtHost);

		lblPort.setDisplayedMnemonic('o');
		lblPort.setLabelFor(txtPort);
		txtPort.setColumns(6);

		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(5, 0));
		p1.add(lblHost, BorderLayout.WEST);
		p1.add(txtHost, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(5, 0));
		p2.add(lblPort, BorderLayout.WEST);
		p2.add(txtPort, BorderLayout.CENTER);

		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(5, 0));
		p3.add(p1, BorderLayout.CENTER);
		p3.add(p2, BorderLayout.EAST);
		return p3;
	}

	/**
	 * 初始化账号输入面板
	 * @return
	 */
	private JPanel createUserPanel() {
		lblUsername.setDisplayedMnemonic('U');
		lblPassword.setDisplayedMnemonic('P');
		lblUsername.setLabelFor(txtUser);
		lblPassword.setLabelFor(txtPwd);

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 0, 8));
		left.add(lblUsername);
		left.add(lblPassword);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1 , 0, 8));
		right.add(txtUser);
		right.add(txtPwd);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(10, 0));
		p.setBorder(new TitledBorder(null, "", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(left, BorderLayout.WEST);
		p.add(right, BorderLayout.CENTER);
		return p;
	}

	/**
	 * 生成许可证签名面板
	 * @return JPanel实例
	 */
	private JPanel createLicencePanel() {
		switchPassword();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.add(txtLicence, BorderLayout.CENTER);
		panel.add(cmdLicence, BorderLayout.EAST);
		return panel;
	}
	
	/**
	 * 初始化按钮
	 * @return
	 */
	private JPanel createButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 6, 2));
		panel.add(cmdOK);
		panel.add(cmdCancel);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(panel, BorderLayout.EAST);
		return p;
	}

	/**
	 * 固定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 设置对话窗口范围
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 600; // (int) (size.getWidth() * 0.36);
		int height = 320; // (int) (size.getHeight() * 0.36);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel initPanel() {
		// 建立图形控件
		createControls();

		JPanel licence = createLicencePanel();
		JPanel address = createServerPanel();
		JPanel account = createUserPanel();
		JPanel buttons = createButtons();

		String accountText = findCaption("Dialog/Login/Account/title");
		String serverText = findCaption("Dialog/Login/Server/title");
		String licenceText = findCaption("Dialog/Login/Licence/title");

		account.setBorder(UITools.createTitledBorder(accountText, 4));
		address.setBorder(UITools.createTitledBorder(serverText, 4));
		licence.setBorder(UITools.createTitledBorder(licenceText, 4));

		JPanel north = new JPanel();
		BoxLayout layout = new BoxLayout(north, BoxLayout.Y_AXIS);
		north.setLayout(layout);
		north.add(licence);
		north.add(address);
		north.add(account);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.setBorder(new EmptyBorder(6, 8, 8, 8));
		panel.add(north, BorderLayout.NORTH);
		panel.add(buttons, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * 显示对话窗口
	 */
	public void showDialog(LoginToken token) {
		// 初始化面板
		setContentPane(initPanel());

		// 设置对话窗口范围
		Rectangle rect = getBound();
		setBounds(rect);
		setMinimumSize(new Dimension(300, 200));

		// 窗口参数
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		String title = TerminalLauncher.getInstance().findCaption("Dialog/Login/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();

		// 总在最前面
		setAlwaysOnTop(true);

		// 显示
		if (token != null && token.isShow()) {
			com.laxcus.site.Node hub = token.getHub();
			txtHost.setText(hub.getAddress().toString());
			txtPort.setText(String.format("%d", hub.getUDPort()));

			User user = token.getUser();
			txtUser.setText(user.getUsername().toString());
			txtPwd.setText(user.getPassword().toString());
			// 不再显示
			token.setShow(false);
		}	
		
		// 关闭启动屏幕窗口
		TerminalLauncher.getInstance().stopSplash();
		
		// 显示
		setVisible(true);
	}

}