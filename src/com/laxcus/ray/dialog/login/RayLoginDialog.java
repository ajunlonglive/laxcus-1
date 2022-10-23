/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.login;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.laxcus.access.diagram.*;
import com.laxcus.platform.control.*;
import com.laxcus.ray.*;
import com.laxcus.log.client.*;
import com.laxcus.register.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.hash.*;
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
public class RayLoginDialog extends CommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 7962518727514615920L;
	
	/**
	 * 鼠标拖拽窗口
	 *
	 * @author scott.liang
	 * @version 1.0 3/5/2022
	 * @since laxcus 1.0
	 */
	class MouseDragAdapter extends MouseAdapter {

		/** 拖放 **/
		private boolean dragged;

		/** 坐标 **/
		private Point axis;
		
		public MouseDragAdapter(){
			super();
			dragged = false;
		}

		public void mousePressed(MouseEvent e) {
			dragged = true;
			axis = new Point(e.getX(), e.getY());
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}

		public void mouseReleased(MouseEvent e) {
			dragged = false;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseDragged(MouseEvent e) {
			if (dragged) {
				int x = e.getXOnScreen() - axis.x;
				int y = e.getYOnScreen() - axis.y;
				setLocation(x, y);
			}
		}
	}
	
	/** 鼠标事件 **/
	private MouseDragAdapter mouseListener = new MouseDragAdapter();

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
	private JCheckBox cmdReset = new JCheckBox();

	private JButton cmdOK = new JButton("Login");
	private JButton cmdCancel = new JButton("Cancel");

	private boolean canceled;

	private char echoChar = 0;

	private ResetHandler handler;
	
	/**
	 * 构造图形前端登录窗口
	 * @param frame 主窗口
	 * @throws HeadlessException
	 */
	public RayLoginDialog(Frame frame, ResetHandler rh) {
		super(frame);
		handler = rh;
		canceled = false;

		// 弹出窗口不装修边框
		MessageDialog.setUndressing(true);
		// 使用平面按纽
		MessageDialog.setFlatButton(true);

		setLabelText(lblUsername, UIManager.getString("LoginDialog.Account.UsernameText"));
		setLabelText(lblPassword, UIManager.getString("LoginDialog.Account.PasswordText"));
		setLabelText(lblHost, UIManager.getString("LoginDialog.Server.AddressText"));
		setLabelText(lblPort, UIManager.getString("LoginDialog.Server.PortText"));
		
		setButtonText(cmdLicence, UIManager.getString("LoginDialog.Button.LicenceText"));
		setButtonText(cmdReset, UIManager.getString("LoginDialog.Button.ResetText"));
		setToolTipText(cmdReset, UIManager.getString("LoginDialog.Button.ResetTooltip"));
		setButtonText(cmdOK, UIManager.getString("LoginDialog.Button.OkayText"));
		setButtonText(cmdCancel, UIManager.getString("LoginDialog.Button.CancelText"));
		
		// 回显字符，加密的！
		echoChar = txtLicence.getEchoChar();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
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
		if (words != null && words.length > 0) {
			String signature = new String(words);
			RayLauncher.getInstance().setSignature(signature);
			RayLauncher.getInstance().loadLicence(false);
		}
	}

	/**
	 * 登录注册
	 */
	private void login() {
		RayLoginThread thread = new RayLoginThread(this);
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
		// 重置
		else if (e.getSource() == cmdReset) {
			if (cmdReset.isSelected()) {
				reset();
			}
		}
	}

	private void reset() {
		String title = UIManager.getString("LoginDialog.Reset.Title");
		String content = UIManager.getString("LoginDialog.Reset.QueryText");
		Icon icon = UIManager.getIcon("MessageDialog.questionIcon");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content, JOptionPane.YES_NO_OPTION);
		if (who != JOptionPane.YES_OPTION) {
			return;
		}

		boolean success = handler.reset();
		if (success) {
			content = UIManager.getString("LoginDialog.Reset.SuccessfulText");
			icon = UIManager.getIcon("MessageDialog.informationIcon");
			MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
		} else {
			content = UIManager.getString("LoginDialog.Reset.FailedText");
			icon = UIManager.getIcon("MessageDialog.errorIcon");
			MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
		}
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
	private void showFailed( String title, String content) {
		Icon icon = UIManager.getIcon("LoginDialog.FailedIcon");
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 格式化和显示错误！
	 * @param titleKey
	 * @param contentKey
	 */
	private void showXMLFailed(String titleKey, String contentKey) {
		String title = UIManager.getString(titleKey);
		String content = UIManager.getString(contentKey);
		showFailed(title, content);
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarning(String title, String content) {
		Icon icon = UIManager.getIcon("LoginDialog.WarningIcon");
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 格式化和显示错误！
	 * @param xmlMessage
	 * @param xmlTitle
	 */
	private void showXMLWarning(String titleKey, String contentKey) {
		String title = UIManager.getString(titleKey);
		String content = UIManager.getString(contentKey);
		showWarning(title, content);
	}
	
	/**
	 * 显示错误
	 * @param host
	 * @param port
	 */
	void showLoginFailed(String host, String port, int who, int pitchId, SiteHost pitchHub) {
		String title = ""; 
		String content = "";
		
		if (who == RayEntryFlag.CHECK_FAULT) {
			title = UIManager.getString("LoginDialog.Error.CheckFailedTitle"); 
			content = UIManager.getString("LoginDialog.Error.CheckFailedContent"); 
			content = String.format(content, host, port);
		} else if (who == RayEntryFlag.CONNECT_FAULT) {
			title = UIManager.getString("LoginDialog.Error.ConnectFaultTitle"); 
			content = UIManager.getString("LoginDialog.Error.ConnectFaultContent"); 
			content = String.format(content, host, port);
		} else if (who == RayEntryFlag.VERSION_NOTMATCH) {
			title = UIManager.getString("LoginDialog.Error.VersionNotMatchTitle"); 
			content = UIManager.getString("LoginDialog.Error.VersionNotMatchContent"); 
		} else if (who == RayEntryFlag.LOGIN_FAULT) {
			title = UIManager.getString("LoginDialog.Error.LoginFaultTitle"); 
			content = UIManager.getString("LoginDialog.Error.LoginFaultContent"); 
			content = String.format(content, host, port);
		} else if (who == RayEntryFlag.REFLECT_FAULT) {
			showPitchFailed(pitchId, pitchHub);
			return;
		}

		showFailed(title, content);
	}

	/**
	 * 主机定位错误
	 * @param pitchId 定位错误码
	 */
	private void showPitchFailed(int pitchId, SiteHost pitchHub) {
		if (pitchId == RayPitch.NOT_FOUND) {
			String host = String.format("%s:%d", pitchHub.getAddress(), pitchHub.getUDPort());
			String title = UIManager.getString("LoginDialog.Error.Pitch.NotFoundTitle");
			String content = UIManager.getString("LoginDialog.Error.Pitch.NotFoundContent");
			content = String.format(content, host);
			showFailed(title, content);
		} else if (pitchId == RayPitch.NAT_ERROR) {
			String title = UIManager.getString("LoginDialog.Error.Pitch.NATErrorTitle");
			String content = UIManager.getString("LoginDialog.Error.Pitch.NATErrorContent");
			showFailed(title, content);
		} else if (pitchId == RayPitch.ADDRESS_NOTMATCH) {
			String title = UIManager.getString("LoginDialog.Error.Pitch.NotMatchTitle");
			String content = UIManager.getString("LoginDialog.Error.Pitch.NotMatchContent");
			showFailed(title, content);
		}
	}
	
	/**
	 * 注册到集群管理节点，包括TOP/HOME/BANK
	 * @return 成功返回“真”，失败“假”。
	 */
	private boolean check(RayLoginThread thread) {
		String username = txtUser.getText().trim();
		String password = new String(txtPwd.getPassword()).trim();
		String host = txtHost.getText().trim();
		String port = txtPort.getText().trim();

		Logger.debug(this, "login", "login to hub...");

		// 依次判断：主机、端口、用户名、密码
		if (host.length() == 0) {
			showXMLWarning("LoginDialog.Error.ServerTitle", "LoginDialog.Error.ServerText");
			txtHost.requestFocus();
			return false;
		}
		if (port.length() == 0) {
			showXMLWarning("LoginDialog.Error.PortTitle", "LoginDialog.Error.PortText");
			txtPort.requestFocus();
			return false;
		}
		if (username.length() == 0) {
			showXMLWarning("LoginDialog.Error.UsernameTitle", "LoginDialog.Error.UsernameText");
			txtUser.requestFocus();
			return false;
		}
		if (password.length() == 0) {
			showXMLWarning("LoginDialog.Error.PasswordTitle", "LoginDialog.Error.PasswordText");
			txtPwd.requestFocus();
			return false;
		}

		// 判断签名，必须一致，要么是SHA签名，要么是明文
		boolean sha256 = SHA256Hash.validate(username);
		boolean sha512 = SHA512Hash.validate(password);
		boolean success = ((sha256 && sha512) || (!sha256 && !sha512));
		if (!success) {
			showXMLWarning("LoginDialog.Error.AccountTitle", "LoginDialog.Error.AccountText"); 
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
			showXMLFailed("LoginDialog.Error.UnhostTitle", "LoginDialog.Error.UnhostText"); 

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

		setToolTipText(txtHost, UIManager.getString("LoginDialog.Server.AddressTooltip"));
		setToolTipText(txtPort, UIManager.getString("LoginDialog.Server.PortTooltip"));
		setToolTipText(txtUser, UIManager.getString("LoginDialog.Account.UsernameTooltip"));
		setToolTipText(txtPwd, UIManager.getString("LoginDialog.Account.PasswordTooltip"));
		setToolTipText(txtLicence, UIManager.getString("LoginDialog.Licence.SignTooltip"));

		// 限制字符
		txtHost.setDocument(new GraphDocument(txtHost, 128));

		// 数字限制，有效的端口范围在 1 - 0xFFFF之间。端口号是0是通配符
		DefaultStyledDocument def = new DefaultStyledDocument();
		def.setDocumentFilter(new PwdDocumentFilter(def, 1, 0xFFFF));
		txtPort.setDocument(def);

		// 匿名
		cmdLicence.addActionListener(this);
		cmdLicence.setMnemonic('N');
		cmdLicence.setSelected(true); // 默认不显示
		cmdLicence.setRolloverEnabled(true); // 动态反应

		cmdOK.addActionListener(this);
		cmdCancel.addActionListener(this);
		cmdOK.setMnemonic('L');
		cmdCancel.setMnemonic('C');
		cmdReset.addActionListener(this);
		cmdReset.setRolloverEnabled(true); // 动态反应
		
		// 取出系统中的签名，显示它!
		String signature = RayLauncher.getInstance().getSignature();
		if (signature != null) {
			setFieldText(txtLicence, signature);
		}
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
		// 显示释放资源按纽
		if (handler != null) {
			p.add(cmdReset, BorderLayout.WEST);
		}
		return p;
	}

	
	/**
	 * 固定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 设置对话窗口范围
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width = 430; 
		int height = 380; 
		
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 生成标题字体
	 * @param text 文本
	 * @return 返回Font实例
	 */
	private Font createTitleFont(String text) {
		// 桌面系统字体
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
		if (font != null) {
			font = new Font(font.getName(), Font.BOLD, 12);
		}
		// 没有定义，取边框字体
		if (font == null) {
			font = UITools.createTitledBorderFont(text);
		}
		// 无定义，使用当前字体
		if (font == null) {
			font = getFont();
			font = new Font(font.getName(), Font.BOLD, 12);
		}
		return font;
	}
	
	/**
	 * 生成标题面板
	 * @param xmlPath
	 * @return
	 */
	private JPanel createTitlePane(String xmlPath) {
		String text = UIManager.getString(xmlPath);

		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setFont(createTitleFont(text));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 2));
		panel.add(label, BorderLayout.CENTER);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * 图标面板
	 * @return
	 */
	private JPanel createIconPanel() {
		Icon icon = UIManager.getIcon("LoginDialog.AdministratorIcon");
		JLabel label = new JLabel(icon, SwingConstants.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 生成许可证签名面板
	 * @return JPanel实例
	 */
	private JPanel createLicencePanel() {
		switchPassword();

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(10, 0));
		bottom.add(txtLicence, BorderLayout.CENTER);
		bottom.add(cmdLicence, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(createTitlePane("LoginDialog.Licence.Title"), BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.CENTER);
		return panel;
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
		p1.setLayout(new BorderLayout(10, 0));
		p1.add(lblHost, BorderLayout.WEST);
		p1.add(txtHost, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(10, 0));
		p2.add(lblPort, BorderLayout.WEST);
		p2.add(txtPort, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(10, 0));
		bottom.add(p1, BorderLayout.CENTER);
		bottom.add(p2, BorderLayout.EAST);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(createTitlePane("LoginDialog.Server.Title"), BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 初始化账号输入面板
	 * @return
	 */
	private JPanel createAccountPanel() {
		lblUsername.setDisplayedMnemonic('U');
		lblPassword.setDisplayedMnemonic('P');
		lblUsername.setLabelFor(txtUser);
		lblPassword.setLabelFor(txtPwd);

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 0, 8));
		left.add(lblUsername);
		left.add(lblPassword);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1, 0, 8));
		right.add(txtUser);
		right.add(txtPwd);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(10, 0));
		bottom.add(left, BorderLayout.WEST);
		bottom.add(right, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(createTitlePane("LoginDialog.Account.Title"), BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createTwinsPanel(JPanel north, JPanel center, int gap) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, gap));
		panel.add(north, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 建立面板
	 * @return
	 */
	private JPanel createContentPanel() {
		JPanel panel = createTwinsPanel(createIconPanel(), createLicencePanel(), 8);
		panel = createTwinsPanel(panel, createServerPanel(), 20);
		return createTwinsPanel(panel, createAccountPanel(), 20);
	}
	
	/**
	 * 生成主面板
	 * @return
	 */
	private JPanel initPanel() {
		// 建立图形控件
		createControls();
		
		// 生成面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		
		HighlightBorder outside = new HighlightBorder(1);
		Border inside = new EmptyBorder(6, 10, 6, 10);
		panel.setBorder(new CompoundBorder(outside, inside));
		
		panel.add(createContentPanel(), BorderLayout.NORTH);
		panel.add(createButtons(), BorderLayout.SOUTH);
		
		// 鼠标事件
		panel.addMouseListener(mouseListener);
		panel.addMouseMotionListener(mouseListener);
		
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
		
		try {
			setUndecorated(true);
		} catch (IllegalComponentStateException e) {
			
		}
		setBounds(rect);
		setMinimumSize(new Dimension(300, 200));

		// 窗口参数
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		String title = UIManager.getString("LoginDialog.Title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();

		// 总在最前面
		setAlwaysOnTop(true);

		// 显示
		if (token != null && token.isShow()) {
			com.laxcus.site.Node hub = token.getHub();
			User user = token.getUser();

			setFieldText(txtHost, hub.getAddress().toString());
			setFieldText(txtPort, String.format("%d", hub.getUDPort()));
			setFieldText(txtUser, user.getUsername().toString());
			setFieldText(txtPwd, user.getPassword().toString());

			// 不再显示
			token.setShow(false);
		}	
		
		// 关闭启动屏幕窗口
		RayLauncher.getInstance().stopSplash();
		
		// 显示
		setVisible(true);
	}

}