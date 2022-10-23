/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.plaf.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.watch.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.splash.*;
import com.laxcus.util.event.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.help.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.util.login.*;
import com.laxcus.util.net.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.visit.impl.watch.*;
import com.laxcus.watch.component.*;
import com.laxcus.watch.pool.*;
import com.laxcus.watch.window.*;
import com.laxcus.xml.*;

/**
 * WATCH站点启动器。<br>
 * WATCH站点做为集群的观察者，在图形界面上显示集群运行中的站点。当集群的站点发生退出或者撤销时，在图形窗口显示出来。
 * 
 * @author scott.liang
 * @version 1.3 12/21/2015
 * @since laxcus 1.0
 */
public class WatchLauncher extends SlaveLauncher implements TipPrinter, LocalMatcher, VisitRobot, InvokerMessenger {

	/** WATCH站点静态句柄 **/
	private static WatchLauncher selfHandle = new WatchLauncher();

	/** 当前WATCH站点地址配置 */
	private WatchSite local = new WatchSite();

	/** 图形窗口 **/
	private WatchWindow window = new WatchWindow();

	/** 皮肤颜色加载器 **/
	private SkinTokenLoader skinLoader = new SkinTokenLoader();

	/** 窗口界面资源解析器 **/
	private SurfaceLoader surfaceLoader = new SurfaceLoader();

	/** 数据处理模式。默认采用磁盘为做中间存取，内存模式是假。 **/
	private boolean memory;

	/** 命令超时时间 **/
	private long commandTimeout;

	/** 命令优先级 **/
	private byte commandPriority;

	/** 消息提示池 **/
	private TipLoader messages = new TipLoader();

	/** 故障提示池 **/
	private TipLoader faults = new TipLoader();

	/** 警告提示池 **/
	private TipLoader warnings = new TipLoader();

	/** 回显码 **/
	private TipLoader echos = new TipLoader();

	/** 命令解释语境 **/
	private CommentContext context = new CommentContext();

	/** 警告拒止 **/
	private NoticeMuffler warningMuffler = new NoticeMuffler();

	/** 错误拒止 **/
	private NoticeMuffler faultMuffler = new NoticeMuffler();

	/** 启动屏窗口 **/
	private SplashWindow splash = new SplashWindow();

	/**
	 * 构造WATCH站点启动器
	 */
	private WatchLauncher() {
		super(WatchLogTrustor.getInstance());
		
		// 管理员
		local.setRank(RankTag.ADMINISTRATOR);
		
		// 退出JVM
		setExitVM(true);
		// 出错时打印日志
		setPrintFault(true);
		// WATCH站点监听
		setStreamInvoker(new WatchStreamAdapter());
		setPacketInvoker(new WatchPacketAdapter());

		// 远程访问探测接口
		SyntaxParser.setVisitRobot(this);
		// 提示打印接口
		SyntaxParser.setTipPrinter(this);
		
		// 设置INVOKER句柄
		InvokerTrustor.setInvokerMessenger(this);

		// 默认是磁盘处理模式
		setMemory(false);
		// 默认无限制超时
		setCommandTimeout(-1L);
		// 命令优先级
		setCommandPriority(CommandPriority.NONE);

		// 加载日志
		loadCommentContext();
	}

	/**
	 * 返回WATCH站点的静态句柄
	 * @return 返回WatchLauncher句柄
	 */
	public static WatchLauncher getInstance() {
		return WatchLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.parse.VisitRobot#isOnline()
	 */
	@Override
	public boolean isOnline() {
		return isLogined();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public Node getPublicListener() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#loadLicence(boolean)
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		// 不加载许可证
		int who = checkLicence();
		return (who == Licence.LICENCE_IGNORE || who == Licence.LICENCE_ALLOW);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#startInvoker(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public void startInvoker(EchoInvoker invoker) {
		if (!invoker.isDistributed()) {
			return;
		}
		// 发出
		window.rolling(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#stopInvoker(com.laxcus.echo.invoke.EchoInvoker, boolean)
	 */
	@Override
	public void stopInvoker(EchoInvoker invoker, boolean success) {
		if (!invoker.isDistributed()) {
			return;
		}
		// 不成功或者退出时，记录流量
		if (!success || invoker.isQuit()) {
			long rs = invoker.getReceiveFlowSize();
			long ss = invoker.getSendFlowSize();
			window.addFlows(rs, ss);
		}
		window.rolling(false);
	}
	
	/**
	 * 返回警告拒绝
	 * @return RadioMuffler实例
	 */
	public NoticeMuffler getWarningMuffler() {
		return warningMuffler;
	}

	/**
	 * 返回故障拒绝器
	 * @return RadioMuffler实例
	 */
	public NoticeMuffler getFaultMuffler() {
		return faultMuffler;
	}

	/**
	 * 处理平台字体
	 */
	public void doPlatformFont() {
		// 检查平台字体
		checkPlatformFont();
		// 消息对话框文本
		checkMessageDialog();
		checkFileChooser();
	}

	/**
	 * 消息对话框按纽文本
	 */
	private void checkMessageDialog() {
		String text = findContent("MessageDialog/Button/Okay");
		UIManager.put("OptionPane.okButtonText", text);
		text = findContent("MessageDialog/Button/Yes");
		UIManager.put("OptionPane.yesButtonText", text);
		text = findContent("MessageDialog/Button/No");
		UIManager.put("OptionPane.noButtonText", text);
		text = findContent("MessageDialog/Button/Cancel");
		UIManager.put("OptionPane.cancelButtonText", text);

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
		UIManager.put("OptionPane.questionIcon", icon);
		icon = loader.findImage("conf/watch/image/message/info.png", 32, 32);
		UIManager.put("OptionPane.informationIcon", icon);
		icon = loader.findImage("conf/watch/image/message/error.png", 32, 32);
		UIManager.put("OptionPane.errorIcon", icon);
		icon = loader.findImage("conf/watch/image/message/warning.png", 32, 32);
		UIManager.put("OptionPane.warningIcon", icon);
	}

	/**
	 * 文件选择窗口
	 */
	private void checkFileChooser() {
		String text = findContent("FileChooser/Button/Save");
		UIManager.put("FileChooser.saveButtonText", text);

		text = findContent("FileChooser/Button/Open");
		UIManager.put("FileChooser.openButtonText", text);

		text = findContent("FileChooser/Button/Cancel");
		UIManager.put("FileChooser.cancelButtonText", text);

		text = findContent("FileChooser/Button/Help");
		UIManager.put("FileChooser.helpButtonText", text);

		text = findContent("FileChooser/Button/Update");
		UIManager.put("FileChooser.updateButtonText", text);
	}

	/**
	 * 找到首个匹配的字体
	 * @param fonts
	 * @return 首选字体
	 */
	private Font choiceFirst(Font[] defaultFonts, Font[] fonts) {
		for (Font hot : defaultFonts) {
			String hotFamily = hot.getFamily();
			for (Font font : fonts) {
				String family = font.getFamily();
				if (family.indexOf(hotFamily) > -1) {
					return hot;
				}
			}
		}

		return fonts[0];
	}

	/**
	 * 解析一行字体
	 * @param input 输入参数
	 * @return 返回字体或者空指针
	 */
	private Font readFont(String input) {
		final String regex = "^\\s*(.+?)\\s*\\,\\s*(?i)([PLAIN|BOLD|ITALIC]+)\\s*\\,\\s*([\\d]+)\\s*$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String family = matcher.group(1);
		String styleName = matcher.group(2);
		int size = Integer.parseInt(matcher.group(3));

		// 字体样式
		int style = Font.PLAIN;
		if (styleName.matches("^\\s*(?i)(PLAIN)\\s*$")) {
			style = Font.PLAIN;
		} else if (styleName.matches("^\\s*(?i)(BOLD)\\s*$")) {
			style = Font.BOLD;
		} else if (styleName.matches("^\\s*(?i)(ITALIC)\\s*$")) {
			style = Font.ITALIC;
		}
		// 生成字体
		return new Font(family, style, size);
	}

	/**
	 * 读平台定义字体
	 * @return
	 */
	public Font[] readPlatformFont() {
		File dir = createDirectory();
		if (dir == null) {
			return null;
		}
		// 配置目录下的字体文件
		File file = new File(dir, "fonts.conf");
		// 没有这个文件，忽略它
		if(!(file.exists() && file.isFile())) {
			return null;
		}

		// 从配置文件中读取全部配置
		ArrayList<Font> array = new ArrayList<Font>();
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader is = new InputStreamReader(in, "UTF-8");
			BufferedReader bf = new BufferedReader(is);
			do {
				String line = bf.readLine();
				if (line == null) {
					break;
				}
				Font font = readFont(line);
				if (font != null) {
					array.add(font);
				}
			} while (true);
			bf.close();
			is.close();
			in.close();
		} catch (IOException e) {

		}

		if(array.isEmpty()) {
			return null;
		}
		// 输出全部字体
		Font[] fonts = new Font[array.size()];
		return array.toArray(fonts);
	}

	/**
	 * 检查平台字体
	 */
	private void checkPlatformFont() {
		// 读平台上定义的字体
		Font[] defaultFonts = readPlatformFont();
		if (defaultFonts == null) {
			return;
		}
		// 取出例子文本
		String text = findCaption("Window/Frame/title");
		if (text == null) {
			return;
		}
		// 找到合适的字体
		Font[] fonts = FontKit.findFonts(text);
		if (fonts == null) {
			return;
		}

		// 首选字体
		Font font = choiceFirst(defaultFonts, fonts);
		FontUIResource res = new FontUIResource(font);

		// 设置匹配的字体
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);

			if (value instanceof FontUIResource) {
				//	System.out.println(key.toString() + " | class is: " +value.getClass().getName()+" |" +value.toString() + " | "+font.toString());
				UIManager.put(key, res);

				//	Object f = UIManager.get(key);
				//	System.out.println(key.toString() + " | " +f.toString());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return WatchCustomTrustor.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findCaption(java.lang.String)
	 */
	@Override
	public String findCaption(String xmlPath) {
		if (!surfaceLoader.isLoaded()) {
			surfaceLoader.load(getSurfacePath());
		}
		return surfaceLoader.getAttribute(xmlPath);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findContent(java.lang.String)
	 */
	@Override
	public String findContent(String xmlPath) {
		if (!surfaceLoader.isLoaded()) {
			surfaceLoader.load(getSurfacePath());
		}
		return surfaceLoader.getContent(xmlPath);
	}

	/**
	 * 弹出确认窗口
	 * @param content 显示文本
	 * @return 接受返回真，否则假
	 */
	private boolean showConfirmDialog(String content) {
		String title = window.getTitle();
		int who = MessageDialog.showMessageBox(window, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}

	/**
	 * 判断是绑定到用户应用、无操作界面的驱动程序站点
	 * @return 返回真或者假
	 */
	public boolean isDriver(){ return false;}

	/**
	 * 判断是字符界面的控制台站点
	 * @return 返回真或者假
	 */
	public boolean isConsole(){ return false;}

	/**
	 * 判断是图形界面的终端站点
	 * @return 返回真或者假
	 */
	public boolean isTerminal(){ return false;}

	/**
	 * 判断是用于边缘计算的服务端节点（在后台运行）
	 * @return 返回真或者假
	 */
	public boolean isEdge(){ return false;} 

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#isWatch()
	 */
	@Override
	public boolean isWatch() {
		return true;
	}

	/**
	 * 弹出对话框提示信息
	 */
	public boolean confirm(int no) {
		String content = messages.format(no);
		return showConfirmDialog(content);
	}

	/**
	 * 弹出对话窗口
	 */
	public boolean confirm(int no, Object... params) {
		String content = messages.format(no, params);
		return showConfirmDialog(content);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String message(int no) {
		return messages.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String message(int no, Object... params) {
		return messages.format(no, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String warning(int no) {
		return warnings.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String warning(int no, Object... params) {
		return warnings.format(no, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String fault(int no) {
		return faults.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String fault(int no, Object... params) {
		return faults.format(no, params);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#echo(int)
	 */
	@Override
	public String echo(int minor) {
		return echos.format(minor);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#echo(int, java.lang.Object[])
	 */
	@Override
	public String echo(int minor, Object... params) {
		return echos.format(minor, params);
	}

	/**
	 * 根据编号，播放声音
	 * @param who 声音编号
	 */
	public void playSound(int who) {
		SoundPlayer.getInstance().play(who);
	}

	/**
	 * 返回当前账号签名
	 * @return 账号签名
	 */
	public Siger getUsername() {
		WatchUser user = local.getUser();
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	/**
	 * 设置内存处理模式
	 * @param b
	 */
	public void setMemory(boolean b) {
		memory = b;
	}

	/**
	 * 设置内存处理模式
	 * @param input 输入语句
	 */
	public void setMemory(String input) {
		boolean b = (input != null && input.matches("^\\s*(?i)(?:MEMORY)\\s*$"));
		setMemory(b);
	}

	/**
	 * 判断是内存处理模式
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return memory;
	}

	/**
	 * 判断是磁盘处理模式
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return !memory;
	}

	/**
	 * 设置命令超时，单位：毫秒
	 * 
	 * @param ms 毫秒
	 */
	public void setCommandTimeout(long ms) {
		commandTimeout = ms;
	}

	/**
	 * 设置命令超时时间，单位：毫秒。
	 * @param input 输入语句
	 */
	public void setCommandTimeout(String input) {
		long ms = ConfigParser.splitTime(input, -1);
		if (ms > 0) {
			setCommandTimeout(ms);
		}
	}

	/**
	 * 返回命令超时
	 * 
	 * @return 返回long类型的超时时间
	 */
	public long getCommandTimeout() {
		return commandTimeout;
	}

	/**
	 * 设置命令优先级
	 * 
	 * @param no 优先级编号
	 */
	public void setCommandPriority(byte no) {
		if (CommandPriority.isPriority(no)) {
			commandPriority = no;
		}
	}

	/**
	 * 设置命令优先级时间，单位：毫秒。
	 * @param input 输入语句
	 */
	public void setCommandPriority(String input) {
		byte no = CommandPriority.translate(input);
		if (no > 0) {
			setCommandPriority(no);
		}
	}

	/**
	 * 返回命令优先级
	 * 
	 * @return 返回命令优先级时间
	 */
	public byte getCommandPriority() {
		return commandPriority;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public WatchCommandPool getCommandPool() {
		return WatchCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public WatchInvokerPool getInvokerPool() {
		return WatchInvokerPool.getInstance();
	}

	/**
	 * 返回WATCH站点窗口
	 * 
	 * @return 返回WatchWindow实例
	 */
	public WatchWindow getWindow() {
		return window;
	}

	/**
	 * 从总配置文件中选出所关联语言的界面配置文件路径
	 * @return JAR文件中的资源文件路径
	 */
	public String getSurfacePath() {
		LocalSelector selector = new LocalSelector("conf/watch/resource/config.xml");
		return selector.findPath("resource");
	}

	/**
	 * 客户端发起RPC连接通知服务器（管理节点），删除服务器上的客户机记录，退出登录状态
	 * @param local 本地节点地址
	 * @param hub 服务器节点地址（管理节点）
	 * @return 成功返回真，否则假
	 */
	private boolean disconnect(Node hub) {
		HubClient client = fetchHubClient(hub);
		if (client == null) {
			Logger.error(this, "disconnect", "cannot be git %s", hub);
			return false;
		}

		Node local = getListener();
		boolean success = false;
		try {
			success = client.logout(local);
			client.close(); // 雅致关闭
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 强制销毁。如果已经关闭，这里不起作用。
		client.destroy(); 
		// 成功，删除本地保存的服务器密钥
		if (success) {
			removeCipher(hub);
		}
		// 返回结果
		return success;
	}

	/**
	 * 注销连接
	 * @param hub
	 * @param auto
	 */
	private void __logout(Node hub, boolean auto) {
		// 取消登录
		setLogined(false);
		// 如果是自动，不要求SiteLauncher.silent强制自循环；如果不是自动即手动，必须强制自循环。
		if (auto) {
			setRoundSuspend(false);
		} else {
			setRoundSuspend(true);
		}
		// 取消自动注册
		setKiss(false);

		// 退出登录状态
		boolean success = disconnect(hub);
		Logger.note(this, "__logout", success, "drop from %s", hub);

		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
		getPacketHelper().reset();
		getReplyHelper().reset();

//		// 撤销登录状态
//		setLogined(false);
	}

	/*
	 * 这个方法被WatchWindow窗口手动调用。
	 * @see com.laxcus.launch.SiteLauncher#logout()
	 */
	@Override
	public boolean logout() {
		Node hub = getHub();
		// 清除密文记录
		if (hub == null) {
			return false;
		}

		// 注销，不是自动是手动
		__logout(hub, false);

		//		// 设置为手动停止(登录取消，强制自循环)，SiteLauncher.silent进入循环
		//		setLogined(false);
		//		setRoundSuspend(true);
		//		// 取消自动注册
		//		setKiss(false);
		//
		//		// 从服务器节点注销
		//		boolean success = disconnect(hub);
		//		
		//		Logger.note(this, "logout", success, "from %s", hub);
		//		
		//		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
		//		getPacketHelper().reset();
		//		getReplyHelper().reset();

		return true;
	}

	/**
	 * 这个方法是被自动登录调用，手动登录不是这里。
	 * 本方法是为了兼容SiteLauncher.defaultProcess线程的自动登录。
	 * 流程：
	 * 1. 注销登录
	 * 2. 清队内存记录
	 * 3. 清除图形UI
	 * 4. 启动登录（调用WatchLauncher.login）
	 * 
	 * @see com.laxcus.launch.SiteLauncher#login()
	 */
	@Override
	public boolean login() {
		// 取出已经定义的服务器主机地址
		Node hub = getHub();
		if (hub == null) {
			Logger.error(this, "login", "hub site is null!");
			return false;
		}
		
		// 自动注销
		__logout(hub, true);

		// 清除用户
		RegisterMemberBasket.getInstance().clear();
		FrontMemberBasket.getInstance().clear();

		// 清除节点运行时
		SiteRuntimeBasket.getInstance().clear();
		// 清除节点
		SiteOnWatchPool.getInstance().clear();

		// 播放错误声音
		playSound(SoundTag.ERROR);
		// 启动自动登录
		return window.__auto_login(hub);
	}

	//	/*
	//	 * 这个方法被WatchWindow调用
	//	 * @see com.laxcus.launch.SiteLauncher#logout()
	//	 */
	//	@Override
	//	public boolean logout() {
	//		Node hub = getHub();
	//		// 清除密文记录
	//		if (hub == null) {
	//			return false;
	//		}
	//		
	//		// 设置为手动停止(登录取消，强制自循环)，SiteLauncher.silent进入循环
	//		setLogined(false);
	//		setRoundSuspend(true);
	//		// 取消自动注册
	//		setKiss(false);
	//
	//		// 从服务器节点注销
	//		boolean success = disconnect(hub);
	//		
	//		Logger.note(this, "logout", success, "from %s", hub);
	//		
	//		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
	//		getPacketHelper().reset();
	//		getReplyHelper().reset();
	//
	//		return true;
	//	}



	//	/**
	//	 * 这个方法是被自动登录调用，手动登录不是这里。
	//	 * 本方法是为了兼容SiteLauncher.defaultProcess线程的自动登录。
	//	 * 流程：
	//	 * 1. 注销登录
	//	 * 2. 清队内存记录
	//	 * 3. 清除图形UI
	//	 * 4. 启动登录（调用WatchLauncher.login）
	//	 * 
	//	 * @see com.laxcus.launch.SiteLauncher#login()
	//	 */
	//	@Override
	//	public boolean login() {
	//		// 取出已经定义的服务器主机地址
	//		Node hub = getHub();
	//		if (hub == null) {
	//			Logger.error(this, "login", "hub site is null!");
	//			return false;
	//		}
	//
	//		// 手动关闭
	//		__logout(hub, true);
	//
	//		// 注册!
	//		WatchAutoLoginHandler e = new WatchAutoLoginHandler();
	//		return e.__auto_login(window, hub);
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		// 判断HUB有效，再登录
		if (hasHub()) {
			WatchSite site = local.duplicate();
			register(site);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 加载声音参数
		loadSound();

		// 1. 启动FIXP监听
		boolean success = loadListen();
		Logger.note(this, "init", "load listen", success);
		// 2. 加载管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 3. 启动图形界面和注册
		if (success) {
			success = loadWindow();
		}
		Logger.note(this, "init", "launch", success);

		// 成功，在最后启动日志代理。不成功释放资源
		if (success) {
			WatchLogTrustor.getInstance().start();
		} else {
			stopPool();
			stopListen();
			destroyWindow();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		defaultProcess();
	}

	/**
	 * 继承自SiteLauncher.defaultSubProcess，定时检测CPU压力，控制日志更新频率
	 * @see com.laxcus.launch.SiteLauncher#defaultSubProcess()
	 */
	@Override
	protected void defaultSubProcess() {
		double rate = 100.0f;
		if (isLinux()) {
			rate = LinuxEffector.getInstance().getRate();
		} else if (isWindows()) {
			rate = WindowsEffector.getInstance().getRate();
		}

		// 修改日志刷新频率
		// 1. 达到60%比率，日志发送调整到最低值
		// 2. 超过30%比率，降低日志发送
		// 3. 低于15%比率，提高日志发送
		if (rate >= 60.0f) {
			WatchLogTrustor.getInstance().low();
		} else if (rate >= 30.0f) {
			WatchLogTrustor.getInstance().descent();
		} else if (rate < 15.0f) {
			WatchLogTrustor.getInstance().rise();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 从上级站点注销
		logout();
		// 停止资源配置池
		stopPool();
		// 关闭监听服务
		stopListen();		
		// 停止日志服务
		stopLog();
		// 销毁图形资源
		destroyWindow();

		// 参数写入配置
		writeConfigure();
	}

	/**
	 * 设置最大日志数目
	 * @param n 日志数目
	 * @return 返回修改的日志数目
	 */
	public int setMaxLogs(int n) {
		int logs = window.setMaxLogs(n);
		// 写入日志数目
		WatchProperties.writeLogElements(logs);
		// 返回结果
		return logs;
	}

	/**
	 * 加载声音参数
	 */
	private void loadSound() {
		ResourceLoader loader = new ResourceLoader("conf/watch/sound");
		// 警告声音
		byte[] stream = loader.findStream("warning.wav");
		SoundItem item = SoundItem.create(SoundTag.WARNING, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 错误声音
		stream = loader.findStream("error.wav");
		item = SoundItem.create(SoundTag.ERROR, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 消息提示声音
		stream = loader.findStream("message.wav");
		item = SoundItem.create(SoundTag.MESSAGE, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
	}

	/**
	 * 从总配置文件中选出所关联语言的命令帮助文件路径，加载上下文
	 */
	protected void loadCommentContext() {
		LocalSelector selector = new LocalSelector("conf/watch/help/config.xml");		
		String path = selector.findPath("resource");
		context.load(path, GUIKit.isHighScreen());
	}

	/**
	 * 返回命令解释语义
	 * @return 命令语义
	 */
	public CommentContext getCommentContext() {
		return context;
	}

	/**
	 * 启动FIXP监听
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean loadListen() {
		// 设置命令管理池
		CommandVisitOnWatch.setCommandPool(getCommandPool());
		// 启动监听
		Class<?>[] clazzs = { CommandVisitOnWatch.class };
		return loadSingleListen(clazzs, local.getNode());
	}

	/**
	 * 加载管理池
	 * @return 成功返回真，否则假
	 */
	private boolean loadPool() {
		// 启动线程
		VirtualThread[] threads = new VirtualThread[] {
				SwingDispatcher.getInstance(), SoundPlayer.getInstance() };
		startThreads(threads);

		// 启动业务管理池
		VirtualPool[] pools = new VirtualPool[] {
				WatchInvokerPool.getInstance(), WatchCommandPool.getInstance(),
				SiteOnWatchPool.getInstance() };
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		// 停止业务管理池
		VirtualPool[] pools = new VirtualPool[] {
				WatchInvokerPool.getInstance(), WatchCommandPool.getInstance(),
				SiteOnWatchPool.getInstance()};

		// 线程
		VirtualThread[] threads = new VirtualThread[] {
				SoundPlayer.getInstance(), SwingDispatcher.getInstance(),
				WatchLogTrustor.getInstance() };

		// 先停止线程，再停止管理池
		stopThreads(threads);
		stopAllPools(pools);
	}

	/**
	 * 启动图形界面
	 * @return 成功返回真，否则假
	 */
	private boolean loadWindow() {
		boolean success = window.showWindow();

		if (success) {
			window.setHub(getHub());
		}

		// 加载配置
		if (success && CustomConfig.isValidate()) {
			// 加载类定义
			CustomClassLoader loader = new CustomClassLoader();
			loader.load();
			// 加载自定义命令关键字
			String path = CustomConfig.getTokenPath();
			window.addCommandTokens(path);
		}

		return success;
	}

	/**
	 * 销毁图形资源
	 */
	private void destroyWindow() {
		window.destroy();
	}

	/**
	 * 判断是支持的节点类型，包括TOP/HOME/BANK三种。
	 * @param siteFamily 节点类型
	 * @return 返回真或者假
	 */
	private boolean isHub(byte siteFamily) {
		switch (siteFamily) {
		case SiteTag.TOP_SITE:
		case SiteTag.HOME_SITE:
		case SiteTag.BANK_SITE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 定位WATCH站点主机地址
	 * @param hub 注册服务器地址
	 * @return 成功返回真，否则假
	 */
	private int pitch(SocketHost hub) {
		// 确定WATCH主机地址
		SocketHost reflect = reflect(hub);
		if (reflect == null) {
			return WatchPitch.NOT_FOUND; // 不能定位主机
		}

		Logger.info(this, "pitch", "to hub %s, local is %s", hub, reflect);

		Address address = reflect.getAddress();
		// 本地不包含这个地址，那一定是NAT地址，返回错误码
		boolean exists = Address.contains(address);
		if (!exists) {
			return WatchPitch.NAT_ERROR;
		}

		// 如果本地是通配符地址，向服务器检测一个实际地址
		SocketHost host = packetMonitor.getBindHost();
		// 如果是通配符地址
		if (host.getAddress().isAnyLocalAddress()) {
			// 定义监听地址
			packetMonitor.setDefineHost(reflect);
			streamMonitor.getDefineHost().setAddress(address);
			// 私有主机地址
			replySucker.setDefinePrivateIP(address);
			replyDispatcher.setDefinePrivateIP(address);
			// TCP实际监听端口
			int tcport = streamMonitor.getBindPort();
			// 更新WATCH站点地址
			local.setHost(address, tcport, reflect.getPort());

			Logger.info(this, "pitch", "pitch to %s, local %s", hub, local);
		}
		// 如果地址不匹配
		else if (Laxkit.compareTo(host.getAddress(), address) != 0) {
			Logger.error(this, "pitch", "%s != %s", host.getAddress(), address);
			return WatchPitch.ADDRESS_NOTMATCH;
		}

		// 成功
		return WatchPitch.SUCCESSFUL;
	}

	/**
	 * 启动追踪器线程
	 * @param tracker 追踪器
	 */
	private void startTracker(WatchLoginTracker tracker) {
		if (tracker != null) {
			tracker.start();
		}
	}

	/**
	 * 关闭追踪器线程
	 * @param tracker 追踪器
	 */
	private void stopTracker(WatchLoginTracker tracker) {
		if (tracker != null) {
			tracker.stop();
		}
	}

	/**
	 * 采用TCP通信，检测服务器。主要是确认服务器TCP存在且有效
	 * @param remote 目标地址
	 * @param timeout 超时时间
	 * @return 成功返回真，否则假
	 */
	private boolean checkStreamHub(SocketHost remote, int timeout) {
		SocketHost host = null;
		FixpStreamClient client = new FixpStreamClient();
		try {
			client.setConnectTimeout(timeout);
			client.setReceiveTimeout(timeout);
			// 连接
			client.connect(remote);
			// 发送数据包
			host = client.test();
			// 关闭socket
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch(Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();

		// 判断成功!
		boolean success = (host != null);

		Logger.note(this, "checkStreamHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);

		return success;
	}

	/**
	 * 采用UDP通信，检测服务器。主要是确认服务存在且有效
	 * @param remote 目标地址
	 * @param timeout 超时时间，单位：毫秒
	 * @return 成功返回真，否则假
	 */
	private boolean checkPacketHub(SocketHost remote, int timeout) {
		FixpPacketClient client = new FixpPacketClient();
		client.setReceiveTimeout(timeout);
		client.setConnectTimeout(timeout);

		SocketHost host = null;
		try {
			// 1. 以通配符绑定本地任意端口
			boolean success = client.bind();
			if (success) {
				// 2. 询问服务器安全状态
				int type = client.askSecure(remote);
				// 3. 判断是否加密
				boolean secure = SecureType.isCipher(type);
				// 4. 以服务器规定方式，选择加密/非加密，检测服务器
				host = client.test(remote, secure);
			}
			// 关闭SOCKET
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();

		// 判断成功!
		boolean success = (host != null);

		Logger.note(this, "checkPacketHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);

		return success;
	}

	/**
	 * 检测服务器
	 * @param hub 服务器主机
	 * @param count 检测次数
	 * @return 成功返回真，否则假
	 */
	private boolean checkHub(SiteHost hub, int count) {
		if (count < 1) {
			count = 1;
		}
		// 信道超时
		int timeout = SocketTransfer.getDefaultChannelTimeout();

		// 1. 首先检测TCP模式
		boolean check = checkStreamHub(hub.getStreamHost(), timeout);
		if (check) {
			return true;
		}
		// 2. 不成功，检测UDP模式
		// 发包分时
		int subTimeout = timeout / count;
		if (subTimeout < 20000) {
			subTimeout = 20000; // 最少20秒
		}
		// 连续检测，直到最后
		for (int i = 0; i < count; i++) {
			boolean success = checkPacketHub(hub.getPacketHost(), subTimeout);
			if (success) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 注册到目标站点
	 * @param hub TOP/HOME/BANK站点
	 * @param auto 自动登录
	 * @param tracker 追踪器
	 * @return 返回登录结果状态码
	 */
	public int login(SiteHost hub, boolean auto, WatchLoginTracker tracker) {
		Logger.debug(this, "login", "connect to %s", hub);

		// 如果是重新登录，清除UDP服务器保存的过期数据
		getPacketHelper().reset();
		getReplyHelper().reset();

		// 启动追踪器线程
		startTracker(tracker);

		// 检测服务器，判断有效
		// 1. 借用JVM启动TCP/IP堆栈，启动时间超长！FixpStreamClient/FixpPacketClient可以长时间等待
		if (!checkHub(hub, 1)) {
			stopTracker(tracker); // 停止追踪
			return WatchEntryFlag.CHECK_FAULT;
		}

		// 定位WATCH主机地址
		int pitchId = pitch(hub.getPacketHost());
		// 设置结果码
		if (tracker != null) {
			tracker.setPitchId(pitchId);
			tracker.setPitchHub(hub);
		}

		// 非成功码
		if (pitchId != WatchPitch.SUCCESSFUL) {
			// 删除服务器密钥和本地密文
			dropSecure(hub.getPacketHost());
			removeCipher(hub.getPacketHost());

			// 清除UDP服务器保存的过期数据
			getPacketHelper().reset();
			getReplyHelper().reset();
			// 关闭追踪器线程
			stopTracker(tracker);

			// 错误退出
			return WatchEntryFlag.REFLECT_FAULT;
		}

		// 启动UDP通信连接
		HubClient client = fetchHubClient(hub, false);
		// 不成功
		if (client == null) {
			Logger.error(this, "login", "not found %s", hub);

			// 撤销通信密钥和本地密文
			dropSecure(hub.getPacketHost());
			removeCipher(hub.getPacketHost());

			// 关闭追踪器线程
			stopTracker(tracker);
			// 退出!
			return WatchEntryFlag.CONNECT_FAULT;
		}

		int who = WatchEntryFlag.LOGIN_FAULT;
		// 注册
		boolean success = false;
		try {
			// 1. 获得服务器返回的节点地址，必须是TOP/HOME/BANK的其中一种。这是安全操作，防止用户登录其他无关节点！
			Node realHub = client.getHub();
			success = (realHub != null && isHub(realHub.getFamily()));

			// 1. 检查版本
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(getVersion(), other) == 0);
				Logger.note(this, "login", success, "check version  \"%s\" -> \"%s\"", getVersion(), other);
				// 版本不致！
				if (!success) {
					who = WatchEntryFlag.VERSION_NOTMATCH;
				}
			}

			// 2. 获得激活时间
			if (success) {
				long ms = client.getSiteTimeout(local.getFamily());
				success = (ms > 0);
				if (success) {
					setSiteTimeoutMillis(ms);
					Logger.info(this, "login", "site timeout %d", ms);
				}
				// 注册延时
				long interval = client.getHubRegisterInterval();
				interval = registerTimer.setInterval(interval);
				Logger.info(this, "login", "register interval: %d", interval);

				// 最大延时注册时间
				interval = client.getHubMaxRegisterInterval();
				interval = registerTimer.setMaxInterval(interval);
				Logger.info(this, "login", "max register interval: %d", interval);
			}
			// 3. 注册
			if (success) {
				success = false;
				success = client.login(local);
				Logger.note(this, "login", success, "to %s", hub);
			}

			// 4. 注册成功，删除旧密钥，设置服务器站点地址
			if (success) {
				// 对应SiteLauncher.login的处理，删除本地保存的服务器密钥，重新开始！
				removeCipher(hub.getPacketHost());
				// 设置服务器站点地址
				setHub(realHub.duplicate());

				// 刷新最后时间
				refreshEndTime();
				// 发送HELP握手数据包
				hello();

				// 成功！
				who = WatchEntryFlag.SUCCESSFUL;
			}
			// 关闭socket
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
			e.printStackTrace();
		} catch (Throwable e) {
			Logger.fatal(e);
			e.printStackTrace();
		}
		// 销毁
		client.destroy();

		// 登录成功或者否
		setLogined(success);

		// 自动状态，不设置强制循环
		if (auto) {
			setRoundSuspend(false);
		} 
		// 手动状态，如果登录成功，强制循环退出，否则仍然是强制循环
		else {
			// switchStatus(success);

			if (success) {
				setRoundSuspend(false);
			} else {
				setRoundSuspend(true);
			}
		}

		// 成功，强制注销为假；否则，撤销管理站点上保存的私钥
		if (success) {
			// 重置警告信息
			warningMuffler.reset();
			faultMuffler.reset();
			// 向TOP/HOME/BANK询问本集群的注册站点
			getCommandPool().admit(new AskSite());
			// 向TOP/HOME/BANK发送查询用户节点命令
			getCommandPool().admit(new AskClusterMember());
		} else {
			// 撤销服务端密钥和本地密文
			dropSecure(hub.getPacketHost());
			removeCipher(hub.getPacketHost());
		}

		// 状态栏图标
		window.setOnlineIcon(success);

		// 关闭追踪器线程
		stopTracker(tracker);

		Logger.note(this, "login", success, "login to %s", hub);

		return who;
	}

	/**
	 * 推送一个新的登录站点
	 * @param node 站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean pushSite(Node node) {
		// 更新状态栏的节点数目显示
		window.updateStatusSites();
		// 加入
		return window.pushSite(node);
	}

	/**
	 * 正常退出一个登录站点
	 * @param node 站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropSite(Node node) {
		// 删除显示站点
		WatchMixedPanel panel = window.getSkeletonPanel().getMixPanel();
		panel.dropRuntime(node);
		// 更新状态栏的节点数目显示
		window.updateStatusSites();
		// 退出
		return window.dropSite(node);
	}

	/**
	 * 以故障状态销毁一个登录站点
	 * @param node 站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean destroySite(Node node) {
		// 从RuntimePanel界面中删除
		WatchMixedPanel panel = window.getSkeletonPanel().getMixPanel();
		panel.dropRuntime(node);

		// 更新状态栏的节点数目显示
		window.updateStatusSites();
		// 注销
		return window.destroySite(node);
	}

	/**
	 * 保存注册成员，来自CALL/ACCOUNT节点
	 * @param siger 用户签名
	 */
	public void pushRegisterMember(Siger siger) {
		window.pushRegisterMember(siger);
	}

	/**
	 * 删除注册成员，来自ACCOUNT/CALL节点
	 * @param siger 用户签名
	 */
	public void dropRegisterMember(Siger siger) {
		window.dropRegisterMember(siger);
	}

	/**
	 * 保存在线成员，来自GATE/CALL节点
	 * @param siger 用户签名
	 */
	public void pushOnlineMember(Siger siger) {
		window.pushOnlineMember(siger);
	}

	/**
	 * 删除在线成员，来自GATE/CALL节点
	 * @param siger 用户签名
	 */
	public void dropOnlineMember(Siger siger) {
		window.dropOnlineMember(siger);
	}

	/**
	 * 更新在线成员的参数，来自GATE/CALL节点
	 * @param siger 用户签名
	 */
	public void updateOnlineMember(Siger siger) {
		window.updateOnlineMember(siger);
	}

	/**
	 * 显示更新后的运行时参数
	 * @param runtime 站点运行时
	 */
	public void modify(SiteRuntime runtime) {
		window.modify(runtime);
	}

	/**
	 * 更新状态栏的集群成员数目
	 */
	public void updateStatusMembers() {
		window.updateStatusMembers();
	}

	/**
	 * 闪烁状态栏图标
	 */
	public void flash() {
		window.flash();
	}

	/**
	 * 覆盖和取代SiteLauncher.switchHub方法
	 * @see com.laxcus.launch.SiteLauncher#switchHub(com.laxcus.site.Node)
	 */
	@Override
	public boolean switchHub(Node hub) {
		SwitchHubThread e = new SwitchHubThread(this, hub);
		e.start();
		return true;
	}

	/**
	 * 退出登录。发生在窗口点击“注销”的情况下
	 * @return 成功返回真，否则假
	 */
	public boolean logoutWhenWindow() {
		// 判断是登录状态，注销！
		boolean success = isLogined();
		if (success) {
			success = logout();
		}
		return success;
	}

	/**
	 * 清除注销登录残留记录
	 * 包括本地保存的密文，注销状态
	 * @return 成功返回真，否则假
	 */
	protected boolean logoutWhenSwitchHub() {
		// 判断是登录状态，注销！
		boolean success = isLogined();
		if (success) {
			success = logout();
			// 清除HUB站点
			if (success) setHub(null);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		Logger.error(this, "disableProcess", "network interrupted!");
		// 设置图标断开连接
		window.setOnlineIcon(false);
	}

	/**
	 * 加载多语言的提示文本
	 */
	private boolean loadTips() {
		TipSelector selector = new TipSelector("conf/watch/tip/config.xml");
		try {
			String jpath = selector.getMessagePath(); // 返回JAR文件路径
			messages.loadXMLFromJar(jpath);

			jpath = selector.getFaultPath();
			faults.loadXMLFromJar(jpath);

			jpath = selector.getWarningPath();
			warnings.loadXMLFromJar(jpath);

			jpath = selector.getEchoPath();
			echos.loadXMLFromJar(jpath);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error(e);
		}
		return false;
	}

	/**
	 * 解析许可证签名
	 * @param element
	 */
	private void splitLicenceSignature(org.w3c.dom.Element element) {
		// 设置签名
		String signature = XMLocal.getValue(element, LoginMark.SIGNATURE);
		if (signature != null && signature.trim().length() > 0) {
			setSignature(signature.trim());
		}
	}

	/**
	 * 解析登录服务器地址
	 * @param element 
	 * @return 返回服务器节点
	 */
	private Node splitHubSite(org.w3c.dom.Element element) {
		String input = XMLocal.getValue(element, LoginMark.HUB_SITE);
		try {
			if (input != null) {
				return new Node(input);
			}
		} catch (java.net.UnknownHostException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 解析账号
	 * @param root
	 * @return 成功返回账号，否则是空指针
	 */
	private User splitUser(org.w3c.dom.Element root) {
		org.w3c.dom.NodeList nodes = root.getElementsByTagName(LoginMark.MARK_ACCOUNT);
		if (nodes == null || nodes.getLength() != 1) {
			return null;
		}

		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
		String username = XMLocal.getValue(element, LoginMark.USERNAME);
		String password = XMLocal.getValue(element, LoginMark.PASSWORD);

		// 判断是SHA数字，或者是明文
		if (Siger.validate(username) && SHA512Hash.validate(password)) {
			// 生成签名
			return new User(new Siger(username), new SHA512Hash(password));
		} else {
			return new User(username, password);
		}
	}

	/**
	 * 解析本地私有参数
	 * @param document XML文档
	 */
	private void splitPrivate(org.w3c.dom.Document document) {
		// 命令模式 / 命令超时
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
		setMemory(input);
		input = XMLocal.getValue(element, SiteMark.COMMAND_TIMEOUT);
		setCommandTimeout(input);
		// 监视站点定时刷新间隔时间
		input = XMLocal.getValue(element, SiteMark.OUTLOOK_INTERVAL);
		WatchTube.setTimeout(ConfigParser.splitTime(input, WatchTube.defaultTimeout));
		// 最小时间
		input = XMLocal.getAttribute(element, SiteMark.OUTLOOK_INTERVAL, "min");
		WatchTube.setMinTimeout(ConfigParser.splitTime(input, WatchTube.getMinTimeout()));


		// 登录参数
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
		if (nodes.getLength() == 1) {
			element = (org.w3c.dom.Element) nodes.item(0);
			String yes = element.getAttribute(LoginMark.AUTO);
			boolean auto = yes.matches("^\\s*(?i)(?:YES|TRUE)\\s*$");

			// 在YES情况下，解析许可证签名
			if (auto) {
				splitLicenceSignature(element);
			}
			Node hub = splitHubSite(element);
			User user = splitUser(element);

			boolean success = (hub != null && user != null);
			if (success) {
				success = SiteTag.isHub(hub.getFamily());
			}
			if (success) {
				LoginToken token = new LoginToken(auto, hub, user);
				window.setLoginToken(token);
			}
		}
	}

	/**
	 * 加载本地配置
	 * @param filename 配置文件名
	 * @return 成功返回真，否则假
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}

		// 本地私有参数
		splitPrivate(document);

		// 解析站点配置
		boolean success = splitSingleSite(local, document);
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 生成RSA密钥令牌
		if (success) {
			success = createDefaultSecureToken(document);
		}
		// 解析自定义资源
		if (success) {
			success = loadCustom(document);
		}
		// 加载多语言提示文本
		if (success) {
			success = loadTips();
		}

		// 加载日志并且启动
		if (success) {
			success = loadLogResourceWithLocal(filename);
		}

		return success;
	}

	/**
	 * 加载颜色参数
	 */
	private void loadColors() {
		String xmlPath = "conf/watch/color/color.txt";
		ColorTemplate.load(xmlPath);
	}

//	/**
//	 * 加载皮肤配置，同是更新外观界面
//	 * @return 成功返回真，否则假
//	 */
//	private boolean loadSkins() {
//		// 1. 从配置文件中加载皮肤参数，不成功退出
//		boolean success = skinLoader.load("conf/watch/skin/config.xml");
//		if (!success) {
//			return false;
//		}
//		// 2. 读取"watch.conf"配置文件，把"skin.name"参数读取出来
//		String name = readSkinName();
//		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
//		if (name == null) {
//			name = "normal"; // normal关键字在config.xml配置文件里定义
//		}
//
//		// 假设不成功
//		success = false;
//		// 3. 找到匹配的皮肤方案
//		SkinToken token = skinLoader.findSkinTokenByName(name);
//		if (token != null) {
//			// 3. 切换到主题界面
//			success = token.updateTheme(false);
//			if (success) {
//				// 选中它
//				skinLoader.exchangeCheckedSkinToken(token.getName());
//				// 定义外观
//				Skins.setLookAndFeel(token.getLookAndFeel());
//				Skins.setSkinName(token.getName());
//			}
//		} else {
//			// 启动“Nimbus”外观，“Metal”为暗黑！
//			success = UITools.updateLookAndFeel("Nimbus", null, null);
//			if (success) {
//				int count = SkinToken.loadSkins("conf/watch/skin/nimbus_normal.txt");
//				success = (count > 0);
//			}
//			// 选中Nimbus和确定外观
//			if (success) {
//				skinLoader.exchangeCheckedSkinToken("Nimbus");
//				Skins.setLookAndFeel(Skins.Nimbus);
//				Skins.setSkinName("normal"); // normal是脚本中的定义
//			}
//		}
//
//		return success;
//	}

	/**
	 * 加载皮肤配置，同是更新外观界面
	 * @return 成功返回真，否则假
	 */
	private boolean loadSkins() {
		// 1. 从配置文件中加载皮肤参数，不成功退出
		boolean success = skinLoader.load("conf/watch/skin/config.xml");
		if (!success) {
			return false;
		}
		// 2. 读取"watch.conf"配置文件，把"skin.name"参数读取出来
		String name = readSkinName();
		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
		if (name == null) {
			name = "gray"; // normal关键字在config.xml配置文件里定义
		}

		// 假设不成功
		success = false;
		// 3. 找到匹配的皮肤方案
		SkinToken token = skinLoader.findSkinTokenByName(name);
		if (token != null) {
			// 3. 切换到主题界面
			success = token.updateTheme(true);
			if (success) {
				// 选中它
				skinLoader.exchangeCheckedSkinToken(token.getName());
				// 定义外观
				Skins.setLookAndFeel(token.getLookAndFeel());
				Skins.setSkinName(token.getName());
			}
		} else {
//			// 启动“Nimbus”外观，“Metal”为暗黑！
//			success = UITools.updateLookAndFeel("Nimbus", null, null);
//			if (success) {
//				int count = SkinToken.loadSkins("conf/watch/skin/nimbus_normal.txt");
//				success = (count > 0);
//			}
//			// 选中Nimbus和确定外观
//			if (success) {
//				skinLoader.exchangeCheckedSkinToken("Nimbus");
//				Skins.setLookAndFeel(Skins.Nimbus);
//				Skins.setSkinName("normal"); // normal是脚本中的定义
//			}
			
			// 设置为默认的“Metal”外观，白色
			String clazz = "com.laxcus.util.skin.GrayMetalTheme";
			SkinSheet sheet = new FlatSkinSheet();
			success = UITools.updateLookAndFeel("Metal", clazz, sheet);
			if (success) {
				int count = SkinToken.loadSkins("conf/watch/skin/metal_gray.txt");
				success = (count > 0);
			}
			if (success) {
				skinLoader.exchangeCheckedSkinToken("gray");
				Skins.setLookAndFeel(Skins.Metal);
				Skins.setSkinName("gray"); // "gray"是脚本中的定义
			}
		}

		return success;
	}
	
	/**
	 * 从本地配置中读取皮肤配置名称
	 * @return 返回字符串或者空指针
	 */
	private String readSkinName() {
		// 建立配置目录
		File dir = createDirectory();

		File file = new File(dir, "watch.conf");
		// 读磁盘文件
		UITacker tracker = new UITacker();
		int who = tracker.read(file);
		if (who < 1) {
			return null;
		}
		// 找到"skin.name"对应值
		return tracker.getString(WatchProperties.skinName); // "skin.name");
	}

	/**
	 * 返回皮肤加载器
	 * @return SkinTokenLoader实例句柄
	 */
	public SkinTokenLoader getSkinLoader() {
		return skinLoader;
	}

	/**
	 * 建立目录
	 * @return
	 */
	private File createDirectory() {
		String bin = System.getProperty("user.dir");
		bin += "/../conf";
		File file = new File(bin);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		return (success ? file : null);
	}

	/**
	 * 读WATCH站点参数配置
	 */
	private void writeConfigure() {
		UITacker tracker = new UITacker();
		// 窗口范围
		tracker.put(WatchProperties.boundFrame, WatchProperties.readWindowBound());

		// 中央面板分割线位置
		tracker.put(WatchProperties.dividerCenterPane, WatchProperties.readCenterPaneDeviderLocation());
		// 浏览窗口分割线位置
		tracker.put(WatchProperties.dividerBrowserPane, WatchProperties.readBrowserPaneDeviderLocation());
		// 站点浏览面板分割线
		tracker.put(WatchProperties.dividerSiteBrowserPane, WatchProperties.readSiteBrowserPaneDeviderLocation());
		// 成员浏览面板分割线
		tracker.put(WatchProperties.dividerMemberBrowserPane, WatchProperties.readMemberBrowserPaneDeviderLocation());

		// 字体实例
		tracker.put(WatchProperties.fontSystem, WatchProperties.readSystemFont()); 
		tracker.put(WatchProperties.fontBrowserSite, WatchProperties.readBrowserSiteFont()); 
		tracker.put(WatchProperties.fontBrowserMember, WatchProperties.readBrowserMemberFont()); 
		tracker.put(WatchProperties.fontCommand, WatchProperties.readCommandPaneFont()); 
		tracker.put(WatchProperties.fontTabbed, WatchProperties.readTabbedFont());
		tracker.put(WatchProperties.fontMessage, WatchProperties.readTabbedMessageFont()); 
		tracker.put(WatchProperties.fontTable, WatchProperties.readTabbedTableFont()); 
		tracker.put(WatchProperties.fontSiteStatus, WatchProperties.readTabbedRuntimeFont()); 
		tracker.put(WatchProperties.fontLog, WatchProperties.readTabbedLogFont());
		// 主菜单字体
		tracker.put(WatchProperties.fontMenu, WatchProperties.readMainMenuFont());

		// 帮助字体类型
		String helpFamily = context.getTemplateFontName();
		tracker.put(WatchProperties.fontHelp, helpFamily);
		// 播放声音
		boolean play = SoundPlayer.getInstance().isPlay();
		tracker.put(WatchProperties.soundPlay, play);
		// 显示的日志数目
		tracker.put(WatchProperties.logElements, WatchProperties.readLogElements());
		// 拒绝显示日志
		tracker.put(WatchProperties.logForbid, WatchProperties.readLogForbid());

		// 界面皮肤颜色名称
		SkinToken token = skinLoader.findCheckedSkinToken();
		if (token != null) {
			tracker.put(WatchProperties.skinName, token.getName());
		}

		// 配置参数写入指定的目录
		File dir = createDirectory();
		if (dir != null) {
			File file = new File(dir, "watch.conf");
			// 写入磁盘
			tracker.write(file);
		}
	}

	/**
	 * 读WATCH站点配置并且设置参数
	 * @return 成功返回真，否则假
	 */
	private boolean readConfigure() {
		// 选择默认字体做为显示字体
		Font[] defaultFonts = readPlatformFont();
		if (defaultFonts != null) {
			context.setTemplateFontName(defaultFonts[0].getFamily());
		}

		// 读本地配置文件
		File dir = createDirectory();
		if (dir == null) {
			return false;
		}
		File file = new File(dir, "watch.conf");

		// 读磁盘文件
		UITacker tracker = new UITacker();
		int who = tracker.read(file);
		if (who < 1) {
			return false;
		}

		// 窗口范围
		Rectangle rect = tracker.getRectangle(WatchProperties.boundFrame); 
		if (rect != null) {
			WatchProperties.writeWindowBound(rect);
		}

		// 中央面板分割线位置
		Integer pixel = tracker.getInteger(WatchProperties.dividerCenterPane);
		if (pixel != null) {
			WatchProperties.writeCenterPaneDeviderLocation(pixel.intValue());
		}
		// 浏览面板分割线位置
		pixel = tracker.getInteger(WatchProperties.dividerBrowserPane);
		if(pixel !=null){
			WatchProperties.writeBrowserPaneDeviderLocation(pixel.intValue());
		}
		// 站点浏览面板分割线
		pixel = tracker.getInteger(WatchProperties.dividerSiteBrowserPane);
		if (pixel != null) {
			WatchProperties.writeSiteBrowserPaneDeviderLocation(pixel.intValue());
		}
		// 成员浏览面板分割线
		pixel = tracker.getInteger(WatchProperties.dividerMemberBrowserPane);
		if (pixel != null) {
			WatchProperties.writeMemberBrowserPaneDeviderLocation(pixel.intValue());
		}

		// 系统环境字体
		Font font = tracker.getFont(WatchProperties.fontSystem);
		if (font != null) {
			// 更新系统环境字体
			UITools.updateSystemFonts(font);
			WatchProperties.writeSystemFont(font);
		}
		// 站点浏览窗口字体
		font = tracker.getFont(WatchProperties.fontBrowserSite); 
		if (font != null) {
			WatchProperties.writeBrowserSiteFont(font);
		}
		// 站点浏览窗口字体
		font = tracker.getFont(WatchProperties.fontBrowserMember); 
		if (font != null) {
			WatchProperties.writeBrowserMemberFont(font);
		}
		// 命令字体
		font = tracker.getFont(WatchProperties.fontCommand); 
		if (font != null) {
			WatchProperties.writeCommandPaneFont(font);
		}
		// 选项卡字体
		font = tracker.getFont(WatchProperties.fontTabbed); 
		if (font != null) {
			WatchProperties.writeTabbedFont(font);
		}
		// 消息字体
		font = tracker.getFont(WatchProperties.fontMessage); 
		if (font != null) {
			WatchProperties.writeTabbedMessageFont(font);
		}
		// 表格字体
		font = tracker.getFont(WatchProperties.fontTable); 
		if (font != null) {
			WatchProperties.writeTabbedTableFont(font);
		}
		// 节点状态字体
		font = tracker.getFont(WatchProperties.fontSiteStatus); 
		if (font != null) {
			WatchProperties.writeTabbedRuntimeFont(font);
		}
		// 日志字体
		font = tracker.getFont(WatchProperties.fontLog); 
		if (font != null) {
			WatchProperties.writeTabbedLogFont(font);
		}
		// 主菜单字体
		font = tracker.getFont(WatchProperties.fontMenu); 
		if (font != null) {
			WatchProperties.writeMainMenuFont(font);
		}
		// 帮助字体类型
		String helpFamily = tracker.getString(WatchProperties.fontHelp); 
		if (helpFamily != null) {
			WatchProperties.writeHelpMenuFontFamily(helpFamily);
			WatchLauncher.getInstance().getCommentContext().setTemplateFontName(helpFamily);
		}
		// 声音
		Boolean play = tracker.getBoolean(WatchProperties.soundPlay); 
		if (play != null) {
			boolean yes = play.booleanValue(); 
			WatchProperties.writeSoundPlay(yes);
			SoundPlayer.getInstance().setPlay(yes);
		}
		// 日志显示数目
		Integer logs = tracker.getInteger(WatchProperties.logElements);
		if (logs != null) {
			WatchProperties.writeLogElements(logs.intValue());
		}
		// 拒绝显示日志
		Boolean forbid = tracker.getBoolean(WatchProperties.logForbid);
		if (forbid != null) {
			boolean yes = forbid.booleanValue();
			WatchProperties.writeLogForbid(yes);
		}

		// 返回成功
		return true;
	}


	/**
	 * 启动闪屏
	 */
	public void startSplash() {
		ResourceLoader loader = new ResourceLoader("conf/watch/image/splash/");
		ImageIcon icon = null;
		// 判断中文或者其它，显示！
		if (Laxkit.isSimplfiedChinese()) {
			icon = loader.findImage("zh_CN/splash.jpg");
		} else {
			icon = loader.findImage("en_US/splash.jpg");
		}

		// 设置界面
		splash.createWindow(icon);
		// 启动界面
		splash.start();
	}

	/**
	 * 启动闪屏
	 */
	public void stopSplash() {
		splash.stop();
	}

	/**
	 * WATCH启动入口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 句柄
		WatchLauncher launcher = WatchLauncher.getInstance();
		// 启动闪屏窗口
		launcher.startSplash();

		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 将颜色加载到内存集合中
		launcher.loadColors();

		// 加载外观和皮肤
		boolean loaded = launcher.loadSkins();
		if (!loaded) {
			Logger.error("cannot be load skin theme!");
			launcher.stopSplash();
			Logger.gushing();
			System.exit(0);
			return;
		}

		// 读取本地配置
		launcher.readConfigure();

		String filename = args[0];
		boolean success = launcher.loadLocal(filename);
		Logger.note("WatchLauncher.main, load local", success);

		// 启动线程
		if (success) {
			success = launcher.start();
			Logger.note("WatchLauncher.main, start service", success);
		}

		// 启动资源检测
		if (success) {
			launcher.loadTimerTasks(filename);
		} else {
			launcher.stopSplash();
			Logger.gushing();
			launcher.stopLog();
			System.exit(0);
		}
	}

}


///**
// * 注册到目标站点
// * @param hub TOP/HOME/BANK站点
// * @param tracker 追踪器
// * @return 注册成功返回真，否则假
// */
//public boolean login(SiteHost hub, WatchLoginTracker tracker) {
//	Logger.debug(this, "login", "connect to %s", hub);
//
//	// 如果是重新登录，清除UDP服务器保存的过期数据
//	getPacketHelper().reset();
//	getReplyHelper().reset();
//
//	// 启动追踪器线程
//	startTracker(tracker);
//
//	// 检测服务器，判断有效
//	// 1. 借用JVM启动TCP/IP堆栈，启动时间超长！FixpStreamClient/FixpPacketClient可以长时间等待
//	if (!checkHub(hub, 3)) {
//		stopTracker(tracker); // 停止追踪
//		return false;
//	}
//	
//	// 定位WATCH主机地址
//	int pitchId = pitch(hub.getPacketHost());
//	// 设置结果码
//	if (tracker != null) {
//		tracker.setPitchId(pitchId);
//		tracker.setPitchHub(hub);
//	}
//
//	// 非成功码
//	if (pitchId != WatchPitch.SUCCESSFUL) {
//		// 删除服务器密钥和本地密文
//		dropSecure(hub.getPacketHost());
//		removeCipher(hub.getPacketHost());
//
//		// 清除UDP服务器保存的过期数据
//		getPacketHelper().reset();
//		getReplyHelper().reset();
//		// 关闭追踪器线程
//		stopTracker(tracker);
//
//		// 错误退出
//		return false;
//	}
//
//	// 启动UDP通信连接
//	HubClient client = fetchHubClient(hub, false);
//	// 不成功
//	if (client == null) {
//		Logger.error(this, "login", "not found %s", hub);
//
//		// 撤销通信密钥和本地密文
//		dropSecure(hub.getPacketHost());
//		removeCipher(hub.getPacketHost());
//
//		// dropSecure(hub.getPacketHost(), 5000, 1);
//
//		// 关闭追踪器线程
//		stopTracker(tracker);
//		// 退出!
//		return false;
//	}
//
//	// 注册
//	boolean success = false;
//	try {
//		// 1. 获得服务器返回的节点地址，必须是TOP/HOME/BANK的其中一种。这是安全操作，防止用户登录其他无关节点！
//		Node realHub = client.getHub();
//		success = (realHub != null && isHub(realHub.getFamily()));
//		
//		// 1. 检查版本
//		if (success) {
//			Version other = client.getVersion();
//			success = (Laxkit.compareTo(getVersion(), other) == 0);
//			Logger.note(this, "login", success, "check version  \"%s\" -> \"%s\"", getVersion(), other);
//		}
//
//		// 2. 获得激活时间
//		if (success) {
//			long ms = client.getSiteTimeout(local.getFamily());
//			success = (ms > 0);
//			if (success) {
//				setSiteTimeoutMillis(ms);
//				Logger.info(this, "login", "site timeout %d", ms);
//			}
//			// 注册延时
//			long interval = client.getHubRegisterInterval();
//			interval = toucher.setInterval(interval);
//			Logger.info(this, "login", "register interval: %d", interval);
//
//			// 最大延时注册时间
//			interval = client.getHubMaxRegisterInterval();
//			interval = toucher.setMaxInterval(interval);
//			Logger.info(this, "login", "max register interval: %d", interval);
//		}
//		// 3. 注册
//		if (success) {
//			success = false;
//			success = client.login(local);
//		}
//
//		// 4. 注册成功，删除旧密钥，设置服务器站点地址
//		if (success) {
//			// 对应SiteLauncher.login的处理，删除本地保存的服务器密钥，重新开始！
//			removeCipher(hub.getPacketHost());
//			// 设置服务器站点地址
//			setHub(realHub.duplicate());
//		}
//		// 关闭socket
//		client.close();
//	} catch (VisitException e) {
//		Logger.error(e);
//		e.printStackTrace();
//	} catch (Throwable e) {
//		Logger.fatal(e);
//		e.printStackTrace();
//	}
//	// 销毁
//	client.destroy();
//	
//	// 更新登录状态
//	switchStatus(success);
//
//	// 成功，强制注销为假；否则，撤销管理站点上保存的私钥
//	if (success) {
//		// 重置警告信息
//		warningMuffler.reset();
//		faultMuffler.reset();
//		// 向TOP/HOME/BANK询问本集群的注册站点
//		getCommandPool().admit(new AskSite());
//		// 向TOP/HOME/BANK发送查询用户节点命令
//		getCommandPool().admit(new AskClusterMember());
//	} else {
//		// 撤销服务端密钥和本地密文
//		dropSecure(hub.getPacketHost());
//		removeCipher(hub.getPacketHost());
//	}
//
//	// 状态栏图标
//	window.setOnlineIcon(success);
//
//	// 关闭追踪器线程
//	stopTracker(tracker);
//
//	Logger.note(this, "login", success, "login to %s", hub);
//
//	return success;
//}

///**
// * 注册到指定地址
// * @param hub BANK/HOME/TOP站点地址
// * @return 注册成功返回真，否则假
// */
//public boolean login(com.laxcus.site.Node hub, WatchLoginTracker tracker) {
//	return login(hub.getHost(), tracker);
//}


//	/* (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#login()
//	 */
//	@Override
//	public boolean login() {
//		return login(local);
//	}

///*
// * 这个方法被WatchWindow调用
// * @see com.laxcus.launch.SiteLauncher#logout()
// */
//@Override
//public boolean logout() {
//	Node hub = getHub();
//	// 清除密文记录
//	if (hub == null) {
//		return false;
//	}
//
//	//  首先设置为失效，进入自循环状态
//	switchStatus(false);
//	// 取消自动注册
//	setKiss(false);
//
//	// 退出登录状态
//	boolean success = disconnect(hub);
//	Logger.note(this, "logout", success, "logout from %s", hub);
//	// 撤销密文
//	success = dropSecure(hub.getPacketHost(), SocketTransfer.getDefaultChannelTimeout(), 1);
//	Logger.note(this, "logout", success, "drop secure from %s", hub);
//
//	// 不成功，采用“NOTIFY.EXIT”方式结束UDP通信，释放双方保存的密文。 // 以上无论是否成功，都进行撤销操作，通知服务器，释放UDP信道连接和密文
//	if (!success) {
//		success = cancel(hub);
//		// 删除密文（冗余操作）
//		removeCipher(hub);
//		Logger.note(this, "logout", success, "cancel from %s", hub);
//	}
//
//	// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
//	getPacketHelper().reset();
//	getReplyHelper().reset();
//
//	return true;
//}


//	/* (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#login()
//	 */
//	@Override
//	public boolean login() {
//		// 取出已经定义的服务器主机地址
//		Node hub = getHub();
//		if (hub == null) {
//			Logger.error(this, "login", "hub site is null!");
//			return false;
//		}
//		// 先关闭
//		logout();
//		
//		window.setStatusText("尝试重新登录到" + hub.toString());
//
//		// 再登录
//		int who = login(hub.getHost(), null);
//		// 判断成功
//		boolean success = WatchEntryFlag.isSuccessful(who);
//
//		// 判断成功或者失败，演示登录结果
//		if (success) {
//			window.setStatusText("登录成功");
//		} else {
//			window.setStatusText("登录失败");
//		}
//		
//		return success;
//	}


///**
// * 切换状态
// * @param logined 注册成功
// */
//private void switchStatus(boolean logined) {
//	// 注册成功，修改状态
//	setLogined(logined);
//	// 撤销强制注销
//	setRoundSuspend(!logined);
//}
//
///**
// * 设置手动停止
// * @param yes
// */
//private void setHandStop(boolean yes) {
//	if (yes) {
//		setLogined(false);
//		setRoundSuspend(true);
//	} else {
//		setLogined(true);
//		setRoundSuspend(false);
//	}
//}
