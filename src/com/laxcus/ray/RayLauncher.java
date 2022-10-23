/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.application.manage.*;
import com.laxcus.command.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.container.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.pool.*;
import com.laxcus.ray.dispatcher.*;
import com.laxcus.ray.pool.*;
import com.laxcus.ray.runtime.*;
import com.laxcus.ray.status.*;
import com.laxcus.register.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.watch.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
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
import com.laxcus.xml.*;

/**
 * WATCH站点启动器。<br>
 * WATCH站点做为集群的观察者，在图形界面上显示集群运行中的站点。当集群的站点发生退出或者撤销时，在图形窗口显示出来。
 * 
 * @author scott.liang
 * @version 1.3 12/21/2015
 * @since laxcus 1.0
 */
public class RayLauncher extends SlaveLauncher implements TipPrinter, VisitRobot, InvokerMessenger {  // , ClusterListener {

	/** WATCH站点静态句柄 **/
	private static RayLauncher selfHandle = new RayLauncher();

	/** 当前WATCH站点地址配置 */
	private WatchSite local = new WatchSite();

	/** 图形窗口 **/
	private RayWindow window = new RayWindow();

	/** 皮肤颜色加载器 **/
	private SkinTokenLoader skinLoader = new SkinTokenLoader();

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

//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#showRuntime(com.laxcus.command.site.watch.SiteRuntime)
//	 */
//	@Override
//	public void showRuntime(SiteRuntime cmd) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void modify(SiteRuntime runtime){
//		
//	}
//	
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#updateStatusMembers()
//	 */
//	@Override
//	public void updateStatusMembers() {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#pushOnlineMember(com.laxcus.util.Siger)
//	 */
//	@Override
//	public void pushOnlineMember(Siger siger) {
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#pushRegisterMember(com.laxcus.util.Siger)
//	 */
//	@Override
//	public void pushRegisterMember(Siger siger) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#dropRegisterMember(com.laxcus.util.Siger)
//	 */
//	@Override
//	public void dropRegisterMember(Siger siger) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#dropOnlineMember(com.laxcus.util.Siger)
//	 */
//	@Override
//	public void dropOnlineMember(Siger siger) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#updateOnlineMember(com.laxcus.util.Siger)
//	 */
//	@Override
//	public void updateOnlineMember(Siger siger) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#pushSite(com.laxcus.site.Node)
//	 */
//	@Override
//	public boolean pushSite(Node node) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#dropSite(com.laxcus.site.Node)
//	 */
//	@Override
//	public boolean dropSite(Node node) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ray.util.ClusterListener#destroySite(com.laxcus.site.Node)
//	 */
//	@Override
//	public boolean destroySite(Node node) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	/**
	 * 构造WATCH站点启动器
	 */
	private RayLauncher() {
		super(RayLogTrustor.getInstance());
		
		// 管理员
		local.setRank(RankTag.BENCH);
		
		// 退出JVM
		setExitVM(true);
		// 出错时打印日志
		setPrintFault(true);
		// WATCH站点监听
		setStreamInvoker(new RayStreamAdapter());
		setPacketInvoker(new RayPacketAdapter());

		// 初始化
		initial();
	}

	/**
	 * 初始化基础参数
	 */
	private void initial() {
		// 给命令转发器设置FRONT站点句柄
		RayCommandDispatcher.setRayLauncher(this);
		// 托盘管理器
		PlatformKit.setTrayManager(new RayTrayManager());

		//		// 命令分派器
		//		PlatformKit.setCommandDispatcher(new RayCommandDispatcher());
		//		// 资源适配器
		//		RayResourceAssistor.setRayLauncher(this);
		//		PlatformKit.setResourceAssistor(new RayResourceAssistor());

		// 向自定义资源接口设置FRONT交互站点启动器句柄
		RayCustomTrustor.getInstance().setRayLauncher(this);

		// 将颜色加载到内存集合中
		loadColors();
		// 加载帮助上下文
		loadCommentContext();
		// 加载显示在UI界面上的文字和它的配置
		loadUIText();

		// 设置INVOKER句柄
		InvokerTrustor.setInvokerMessenger(this);
		
		// 远程访问探测接口
		SyntaxParser.setVisitRobot(this);
		// 提示打印接口
		SyntaxParser.setTipPrinter(this);
		
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
	 * @return 返回RayLauncher句柄
	 */
	public static RayLauncher getInstance() {
		return RayLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.parse.VisitRobot#isOnline()
	 */
	@Override
	public boolean isOnline() {
		return isLogined();
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
	
	/**
	 * 退出登录。发生在窗口点击“注销”的情况下
	 * @return 成功返回真，否则假
	 */
	public boolean logoutWhenWindow() {
		Node hub = getHub();
		if (hub == null) {
			Logger.error(this, "login", "hub site is null!");
			return false;
		}
		// 来自手动操作
		__logout(hub, false);
		return true;
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

	
	/* (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#isWatch()
	 */
	@Override
	public boolean isWatch() {
		return true;
	}

	/**
	 * 闪烁状态栏图标
	 */
	public void flash() {
		window.flash();
	}

//	/**
//	 * 弹出对话框提示信息
//	 */
//	public boolean confirm(int no) {
//		String content = messages.format(no);
//		return showConfirmDialog(content);
//	}
//
//	/**
//	 * 弹出对话窗口
//	 */
//	public boolean confirm(int no, Object... params) {
//		String content = messages.format(no, params);
//		return showConfirmDialog(content);
//	}

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

//	/**
//	 * 根据编号，播放声音
//	 * @param who 声音编号
//	 */
//	public void playSound(int who) {
//		SoundPlayer.getInstance().play(who);
//	}

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
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
//	 */
//	@Override
//	public CommandPool getCommandPool() {
//		return RayCommandPool.getInstance();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
//	 */
//	@Override
//	public InvokerPool getInvokerPool() {
//		return RayInvokerPool.getInstance();
//	}

	/**
	 * 返回WATCH站点窗口
	 * 
	 * @return 返回RayWindow实例
	 */
	public RayWindow getWindow() {
		return window;
	}

	///////////////// 其它的 ////////////////////
	
	
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

//	/**
//	 * 处理平台字体
//	 */
//	public void doPlatformFont() {
//		// 检查平台字体
//		checkPlatformFont();
//		// 消息对话框文本
//		checkMessageDialog();
//		checkFileChooser();
//	}

//	/**
//	 * 消息对话框按纽文本
//	 */
//	private void checkMessageDialog() {
//		String text = findContent("MessageDialog/Button/Okay");
//		UIManager.put("OptionPane.okButtonText", text);
//		text = findContent("MessageDialog/Button/Yes");
//		UIManager.put("OptionPane.yesButtonText", text);
//		text = findContent("MessageDialog/Button/No");
//		UIManager.put("OptionPane.noButtonText", text);
//		text = findContent("MessageDialog/Button/Cancel");
//		UIManager.put("OptionPane.cancelButtonText", text);
//
//		ResourceLoader loader = new ResourceLoader();
//		ImageIcon icon = loader.findImage("conf/watch/image/message/question.png", 32, 32);
//		UIManager.put("OptionPane.questionIcon", icon);
//		icon = loader.findImage("conf/watch/image/message/info.png", 32, 32);
//		UIManager.put("OptionPane.informationIcon", icon);
//		icon = loader.findImage("conf/watch/image/message/error.png", 32, 32);
//		UIManager.put("OptionPane.errorIcon", icon);
//		icon = loader.findImage("conf/watch/image/message/warning.png", 32, 32);
//		UIManager.put("OptionPane.warningIcon", icon);
//	}

//	/**
//	 * 文件选择窗口
//	 */
//	private void checkFileChooser() {
//		String text = findContent("FileChooser/Button/Save");
//		UIManager.put("FileChooser.saveButtonText", text);
//
//		text = findContent("FileChooser/Button/Open");
//		UIManager.put("FileChooser.openButtonText", text);
//
//		text = findContent("FileChooser/Button/Cancel");
//		UIManager.put("FileChooser.cancelButtonText", text);
//
//		text = findContent("FileChooser/Button/Help");
//		UIManager.put("FileChooser.helpButtonText", text);
//
//		text = findContent("FileChooser/Button/Update");
//		UIManager.put("FileChooser.updateButtonText", text);
//	}

//	/**
//	 * 找到首个匹配的字体
//	 * @param fonts
//	 * @return 首选字体
//	 */
//	private Font choiceFirst(Font[] defaultFonts, Font[] fonts) {
//		for (Font hot : defaultFonts) {
//			String hotFamily = hot.getFamily();
//			for (Font font : fonts) {
//				String family = font.getFamily();
//				if (family.indexOf(hotFamily) > -1) {
//					return hot;
//				}
//			}
//		}
//
//		return fonts[0];
//	}

//	/**
//	 * 解析一行字体
//	 * @param input 输入参数
//	 * @return 返回字体或者空指针
//	 */
//	private Font readFont(String input) {
//		final String regex = "^\\s*(.+?)\\s*\\,\\s*(?i)([PLAIN|BOLD|ITALIC]+)\\s*\\,\\s*([\\d]+)\\s*$";
//
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			return null;
//		}
//
//		String family = matcher.group(1);
//		String styleName = matcher.group(2);
//		int size = Integer.parseInt(matcher.group(3));
//
//		// 字体样式
//		int style = Font.PLAIN;
//		if (styleName.matches("^\\s*(?i)(PLAIN)\\s*$")) {
//			style = Font.PLAIN;
//		} else if (styleName.matches("^\\s*(?i)(BOLD)\\s*$")) {
//			style = Font.BOLD;
//		} else if (styleName.matches("^\\s*(?i)(ITALIC)\\s*$")) {
//			style = Font.ITALIC;
//		}
//		// 生成字体
//		return new Font(family, style, size);
//	}

//	/**
//	 * 读平台定义字体
//	 * @return
//	 */
//	public Font[] readPlatformFont() {
//		File dir = createDirectory();
//		if (dir == null) {
//			return null;
//		}
//		// 配置目录下的字体文件
//		File file = new File(dir, "fonts.conf");
//		// 没有这个文件，忽略它
//		if(!(file.exists() && file.isFile())) {
//			return null;
//		}
//
//		// 从配置文件中读取全部配置
//		ArrayList<Font> array = new ArrayList<Font>();
//		try {
//			FileInputStream in = new FileInputStream(file);
//			InputStreamReader is = new InputStreamReader(in, "UTF-8");
//			BufferedReader bf = new BufferedReader(is);
//			do {
//				String line = bf.readLine();
//				if (line == null) {
//					break;
//				}
//				Font font = readFont(line);
//				if (font != null) {
//					array.add(font);
//				}
//			} while (true);
//			bf.close();
//			is.close();
//			in.close();
//		} catch (IOException e) {
//
//		}
//
//		if(array.isEmpty()) {
//			return null;
//		}
//		// 输出全部字体
//		Font[] fonts = new Font[array.size()];
//		return array.toArray(fonts);
//	}

	
//	/**
//	 * 检查平台字体
//	 */
//	private void checkPlatformFont() {
//		// 读平台上定义的字体
//		Font[] defaultFonts = readPlatformFont();
//		if (defaultFonts == null) {
//			return;
//		}
//		// 取出例子文本
//		String text = findCaption("Window/Frame/title");
//		if (text == null) {
//			return;
//		}
//		// 找到合适的字体
//		Font[] fonts = FontKit.findFonts(text);
//		if (fonts == null) {
//			return;
//		}
//
//		// 首选字体
//		Font font = choiceFirst(defaultFonts, fonts);
//		FontUIResource res = new FontUIResource(font);
//
//		// 设置匹配的字体
//		Enumeration<Object> keys = UIManager.getDefaults().keys();
//		while (keys.hasMoreElements()) {
//			Object key = keys.nextElement();
//			Object value = UIManager.get(key);
//
//			if (value instanceof FontUIResource) {
//				//	System.out.println(key.toString() + " | class is: " +value.getClass().getName()+" |" +value.toString() + " | "+font.toString());
//				UIManager.put(key, res);
//
//				//	Object f = UIManager.get(key);
//				//	System.out.println(key.toString() + " | " +f.toString());
//			}
//		}
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
//	 */
//	@Override
//	public CustomTrustor getCustomTrustor() {
//		return RayCustomTrustor.getInstance();
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.util.local.LocalMatcher#findCaption(java.lang.String)
//	 */
//	@Override
//	public String findCaption(String xmlPath) {
//		if (!surfaceLoader.isLoaded()) {
//			surfaceLoader.load(getSurfacePath());
//		}
//		return surfaceLoader.getAttribute(xmlPath);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.util.local.LocalMatcher#findContent(java.lang.String)
//	 */
//	@Override
//	public String findContent(String xmlPath) {
//		if (!surfaceLoader.isLoaded()) {
//			surfaceLoader.load(getSurfacePath());
//		}
//		return surfaceLoader.getContent(xmlPath);
//	}
//
//	/**
//	 * 弹出确认窗口
//	 * @param content 显示文本
//	 * @return 接受返回真，否则假
//	 */
//	private boolean showConfirmDialog(String content) {
//		String title = window.getTitle();
//		int who = MessageDialog.showMessageBox(window, title,
//				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
//		return (who == JOptionPane.YES_OPTION);
//	}
//
//
//
//
//	/**
//	 * 从总配置文件中选出所关联语言的界面配置文件路径
//	 * @return JAR文件中的资源文件路径
//	 */
//	public String getSurfacePath() {
//		LocalSelector selector = new LocalSelector("conf/watch/resource/config.xml");
//		return selector.findPath("resource");
//	}
//
//	/**
//	 * 客户端发起RPC连接通知服务器（管理节点），删除服务器上的客户机记录，退出登录状态
//	 * @param local 本地节点地址
//	 * @param hub 服务器节点地址（管理节点）
//	 * @return 成功返回真，否则假
//	 */
//	private boolean disconnect(Node hub) {
//		HubClient client = fetchHubClient(hub);
//		if (client == null) {
//			Logger.error(this, "disconnect", "cannot be git %s", hub);
//			return false;
//		}
//
//		Node local = getListener();
//		boolean success = false;
//		try {
//			success = client.logout(local);
//			client.close(); // 雅致关闭
//		} catch (VisitException e) {
//			Logger.error(e);
//		}
//		// 强制销毁。如果已经关闭，这里不起作用。
//		client.destroy(); 
//		// 成功，删除本地保存的服务器密钥
//		if (success) {
//			removeCipher(hub);
//		}
//		// 返回结果
//		return success;
//	}
//
//	/**
//	 * 注销连接
//	 * @param hub
//	 * @param auto
//	 */
//	private void __logout(Node hub, boolean auto) {
//		// 取消登录
//		setLogined(false);
//		// 如果是自动，不要求SiteLauncher.silent强制自循环；如果不是自动即手动，必须强制自循环。
//		if (auto) {
//			setRoundSuspend(false);
//		} else {
//			setRoundSuspend(true);
//		}
//		// 取消自动注册
//		setKiss(false);
//
//		// 退出登录状态
//		boolean success = disconnect(hub);
//		Logger.note(this, "__logout", success, "drop from %s", hub);
//
//		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
//		getPacketHelper().reset();
//		getReplyHelper().reset();
//
////		// 撤销登录状态
////		setLogined(false);
//	}
//
//	/*
//	 * 这个方法被RayWindow窗口手动调用。
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
//		// 注销，不是自动是手动
//		__logout(hub, false);
//
//		//		// 设置为手动停止(登录取消，强制自循环)，SiteLauncher.silent进入循环
//		//		setLogined(false);
//		//		setRoundSuspend(true);
//		//		// 取消自动注册
//		//		setKiss(false);
//		//
//		//		// 从服务器节点注销
//		//		boolean success = disconnect(hub);
//		//		
//		//		Logger.note(this, "logout", success, "from %s", hub);
//		//		
//		//		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
//		//		getPacketHelper().reset();
//		//		getReplyHelper().reset();
//
//		return true;
//	}
//
//	/**
//	 * 这个方法是被自动登录调用，手动登录不是这里。
//	 * 本方法是为了兼容SiteLauncher.defaultProcess线程的自动登录。
//	 * 流程：
//	 * 1. 注销登录
//	 * 2. 清队内存记录
//	 * 3. 清除图形UI
//	 * 4. 启动登录（调用RayLauncher.login）
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
//		// 自动注销
//		__logout(hub, true);
//
//		// 清除用户
//		RegisterMemberBasket.getInstance().clear();
//		FrontMemberBasket.getInstance().clear();
//
//		// 清除节点运行时
//		SiteRuntimeBasket.getInstance().clear();
//		// 清除节点
//		SiteOnRayPool.getInstance().clear();
//
//		// 播放错误声音
//		playSound(SoundTag.ERROR);
//		// 启动自动登录
//		return window.__auto_login(hub);
//	}
//
//	//	/*
//	//	 * 这个方法被RayWindow调用
//	//	 * @see com.laxcus.launch.SiteLauncher#logout()
//	//	 */
//	//	@Override
//	//	public boolean logout() {
//	//		Node hub = getHub();
//	//		// 清除密文记录
//	//		if (hub == null) {
//	//			return false;
//	//		}
//	//		
//	//		// 设置为手动停止(登录取消，强制自循环)，SiteLauncher.silent进入循环
//	//		setLogined(false);
//	//		setRoundSuspend(true);
//	//		// 取消自动注册
//	//		setKiss(false);
//	//
//	//		// 从服务器节点注销
//	//		boolean success = disconnect(hub);
//	//		
//	//		Logger.note(this, "logout", success, "from %s", hub);
//	//		
//	//		// 清除UDP服务器保存的过期数据，包括处于等待的任务钩子
//	//		getPacketHelper().reset();
//	//		getReplyHelper().reset();
//	//
//	//		return true;
//	//	}
//
//
//
//	//	/**
//	//	 * 这个方法是被自动登录调用，手动登录不是这里。
//	//	 * 本方法是为了兼容SiteLauncher.defaultProcess线程的自动登录。
//	//	 * 流程：
//	//	 * 1. 注销登录
//	//	 * 2. 清队内存记录
//	//	 * 3. 清除图形UI
//	//	 * 4. 启动登录（调用RayLauncher.login）
//	//	 * 
//	//	 * @see com.laxcus.launch.SiteLauncher#login()
//	//	 */
//	//	@Override
//	//	public boolean login() {
//	//		// 取出已经定义的服务器主机地址
//	//		Node hub = getHub();
//	//		if (hub == null) {
//	//			Logger.error(this, "login", "hub site is null!");
//	//			return false;
//	//		}
//	//
//	//		// 手动关闭
//	//		__logout(hub, true);
//	//
//	//		// 注册!
//	//		RayAutoLoginHandler e = new RayAutoLoginHandler();
//	//		return e.__auto_login(window, hub);
//	//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#register()
//	 */
//	@Override
//	protected void register() {
//		// 判断HUB有效，再登录
//		if (hasHub()) {
//			RaySite site = local.duplicate();
//			register(site);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#getSite()
//	 */
//	@Override
//	public Site getSite() {
//		return local;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#init()
//	 */
//	@Override
//	public boolean init() {
//		// 加载声音参数
//		loadSound();
//
//		// 1. 启动FIXP监听
//		boolean success = loadListen();
//		Logger.note(this, "init", "load listen", success);
//		// 2. 加载管理池
//		if (success) {
//			success = loadPool();
//		}
//		Logger.note(this, "init", "load pool", success);
//		// 3. 启动图形界面和注册
//		if (success) {
//			success = loadWindow();
//		}
//		Logger.note(this, "init", "launch", success);
//
//		// 成功，在最后启动日志代理。不成功释放资源
//		if (success) {
//			RayLogTrustor.getInstance().start();
//		} else {
//			stopPool();
//			stopListen();
//			destroyWindow();
//		}
//
//		return success;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#process()
//	 */
//	@Override
//	public void process() {
//		defaultProcess();
//	}
//
//	/**
//	 * 继承自SiteLauncher.defaultSubProcess，定时检测CPU压力，控制日志更新频率
//	 * @see com.laxcus.launch.SiteLauncher#defaultSubProcess()
//	 */
//	@Override
//	protected void defaultSubProcess() {
//		double rate = 100.0f;
//		if (isLinux()) {
//			rate = LinuxEffector.getInstance().getRate();
//		} else if (isWindows()) {
//			rate = WindowsEffector.getInstance().getRate();
//		}
//
//		// 修改日志刷新频率
//		// 1. 达到60%比率，日志发送调整到最低值
//		// 2. 超过30%比率，降低日志发送
//		// 3. 低于15%比率，提高日志发送
//		if (rate >= 60.0f) {
//			RayLogTrustor.getInstance().low();
//		} else if (rate >= 30.0f) {
//			RayLogTrustor.getInstance().descent();
//		} else if (rate < 15.0f) {
//			RayLogTrustor.getInstance().rise();
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#finish()
//	 */
//	@Override
//	public void finish() {
//		// 从上级站点注销
//		logout();
//		// 停止资源配置池
//		stopPool();
//		// 关闭监听服务
//		stopListen();		
//		// 停止日志服务
//		stopLog();
//		// 销毁图形资源
//		destroyWindow();
//
//		// 参数写入配置
//		writeConfigure();
//	}
//
//	/**
//	 * 设置最大日志数目
//	 * @param n 日志数目
//	 * @return 返回修改的日志数目
//	 */
//	public int setMaxLogs(int n) {
//		int logs = window.setMaxLogs(n);
//		// 写入日志数目
//		RayProperties.writeLogElements(logs);
//		// 返回结果
//		return logs;
//	}
//
//	/**
//	 * 加载声音参数
//	 */
//	private void loadSound() {
//		ResourceLoader loader = new ResourceLoader("conf/watch/sound");
//		// 警告声音
//		byte[] stream = loader.findStream("warning.wav");
//		SoundItem item = SoundItem.create(SoundTag.WARNING, stream);
//		if (item != null) {
//			SoundPlayer.getInstance().add(item);
//		}
//		// 错误声音
//		stream = loader.findStream("error.wav");
//		item = SoundItem.create(SoundTag.ERROR, stream);
//		if (item != null) {
//			SoundPlayer.getInstance().add(item);
//		}
//		// 消息提示声音
//		stream = loader.findStream("message.wav");
//		item = SoundItem.create(SoundTag.MESSAGE, stream);
//		if (item != null) {
//			SoundPlayer.getInstance().add(item);
//		}
//	}
//
//	/**
//	 * 从总配置文件中选出所关联语言的命令帮助文件路径，加载上下文
//	 */
//	protected void loadCommentContext() {
//		LocalSelector selector = new LocalSelector("conf/watch/help/config.xml");		
//		String path = selector.findPath("resource");
//		context.load(path);
//	}
//
//	/**
//	 * 返回命令解释语义
//	 * @return 命令语义
//	 */
//	public CommentContext getCommentContext() {
//		return context;
//	}
//
//	/**
//	 * 启动FIXP监听
//	 * @return 成功返回“真”，否则“假”。
//	 */
//	private boolean loadListen() {
//		Class<?>[] clazzs = { CommandVisitOnRay.class };
//		return loadSingleListen(clazzs, local.getNode());
//	}
//
//	/**
//	 * 加载管理池
//	 * @return 成功返回真，否则假
//	 */
//	private boolean loadPool() {
//		// 启动线程
//		VirtualThread[] threads = new VirtualThread[] {
//				SwingDispatcher.getInstance(), SoundPlayer.getInstance() };
//		startThreads(threads);
//
//		// 启动业务管理池
//		VirtualPool[] pools = new VirtualPool[] {
//				RayInvokerPool.getInstance(), RayCommandPool.getInstance(),
//				SiteOnRayPool.getInstance() };
//		return startAllPools(pools);
//	}
//
//	/**
//	 * 停止管理池
//	 */
//	private void stopPool() {
//		// 停止业务管理池
//		VirtualPool[] pools = new VirtualPool[] {
//				RayInvokerPool.getInstance(), RayCommandPool.getInstance(),
//				SiteOnRayPool.getInstance()};
//
//		// 线程
//		VirtualThread[] threads = new VirtualThread[] {
//				SoundPlayer.getInstance(), SwingDispatcher.getInstance(),
//				RayLogTrustor.getInstance() };
//
//		// 先停止线程，再停止管理池
//		stopThreads(threads);
//		stopAllPools(pools);
//	}
//
//	/**
//	 * 启动图形界面
//	 * @return 成功返回真，否则假
//	 */
//	private boolean loadWindow() {
//		boolean success = window.showWindow();
//
//		if (success) {
//			window.setHub(getHub());
//		}
//
//		// 加载配置
//		if (success && CustomConfig.isValidate()) {
//			// 加载类定义
//			CustomClassLoader loader = new CustomClassLoader();
//			loader.load();
//			// 加载自定义命令关键字
//			String path = CustomConfig.getTokenPath();
//			window.addCommandTokens(path);
//		}
//
//		return success;
//	}
//
//	/**
//	 * 销毁图形资源
//	 */
//	private void destroyWindow() {
//		window.destroy();
//	}
//
//	/**
//	 * 判断是支持的节点类型，包括TOP/HOME/BANK三种。
//	 * @param siteFamily 节点类型
//	 * @return 返回真或者假
//	 */
//	private boolean isHub(byte siteFamily) {
//		switch (siteFamily) {
//		case SiteTag.TOP_SITE:
//		case SiteTag.HOME_SITE:
//		case SiteTag.BANK_SITE:
//			return true;
//		default:
//			return false;
//		}
//	}
//
//	/**
//	 * 定位WATCH站点主机地址
//	 * @param hub 注册服务器地址
//	 * @return 成功返回真，否则假
//	 */
//	private int pitch(SocketHost hub) {
//		// 确定WATCH主机地址
//		SocketHost reflect = reflect(hub);
//		if (reflect == null) {
//			return RayPitch.NOT_FOUND; // 不能定位主机
//		}
//
//		Logger.info(this, "pitch", "to hub %s, local is %s", hub, reflect);
//
//		Address address = reflect.getAddress();
//		// 本地不包含这个地址，那一定是NAT地址，返回错误码
//		boolean exists = Address.contains(address);
//		if (!exists) {
//			return RayPitch.NAT_ERROR;
//		}
//
//		// 如果本地是通配符地址，向服务器检测一个实际地址
//		SocketHost host = packetMonitor.getBindHost();
//		// 如果是通配符地址
//		if (host.getAddress().isAnyLocalAddress()) {
//			// 定义监听地址
//			packetMonitor.setDefineHost(reflect);
//			streamMonitor.getDefineHost().setAddress(address);
//			// 私有主机地址
//			replySucker.setDefinePrivateIP(address);
//			replyDispatcher.setDefinePrivateIP(address);
//			// TCP实际监听端口
//			int tcport = streamMonitor.getBindPort();
//			// 更新WATCH站点地址
//			local.setHost(address, tcport, reflect.getPort());
//
//			Logger.info(this, "pitch", "pitch to %s, local %s", hub, local);
//		}
//		// 如果地址不匹配
//		else if (Laxkit.compareTo(host.getAddress(), address) != 0) {
//			Logger.error(this, "pitch", "%s != %s", host.getAddress(), address);
//			return RayPitch.ADDRESS_NOTMATCH;
//		}
//
//		// 成功
//		return RayPitch.SUCCESSFUL;
//	}
//
//	/**
//	 * 启动追踪器线程
//	 * @param tracker 追踪器
//	 */
//	private void startTracker(RayLoginTracker tracker) {
//		if (tracker != null) {
//			tracker.start();
//		}
//	}
//
//	/**
//	 * 关闭追踪器线程
//	 * @param tracker 追踪器
//	 */
//	private void stopTracker(RayLoginTracker tracker) {
//		if (tracker != null) {
//			tracker.stop();
//		}
//	}
//
//	/**
//	 * 采用TCP通信，检测服务器。主要是确认服务器TCP存在且有效
//	 * @param remote 目标地址
//	 * @param timeout 超时时间
//	 * @return 成功返回真，否则假
//	 */
//	private boolean checkStreamHub(SocketHost remote, int timeout) {
//		SocketHost host = null;
//		FixpStreamClient client = new FixpStreamClient();
//		try {
//			client.setConnectTimeout(timeout);
//			client.setReceiveTimeout(timeout);
//			// 连接
//			client.connect(remote);
//			// 发送数据包
//			host = client.test();
//			// 关闭socket
//			client.close();
//		} catch (IOException e) {
//			Logger.error(e);
//		} catch(Throwable e) {
//			Logger.fatal(e);
//		}
//		// 销毁
//		client.destroy();
//
//		// 判断成功!
//		boolean success = (host != null);
//
//		Logger.note(this, "checkStreamHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);
//
//		return success;
//	}
//
//	/**
//	 * 采用UDP通信，检测服务器。主要是确认服务存在且有效
//	 * @param remote 目标地址
//	 * @param timeout 超时时间，单位：毫秒
//	 * @return 成功返回真，否则假
//	 */
//	private boolean checkPacketHub(SocketHost remote, int timeout) {
//		FixpPacketClient client = new FixpPacketClient();
//		client.setReceiveTimeout(timeout);
//		client.setConnectTimeout(timeout);
//
//		SocketHost host = null;
//		try {
//			// 1. 以通配符绑定本地任意端口
//			boolean success = client.bind();
//			if (success) {
//				// 2. 询问服务器安全状态
//				int type = client.askSecure(remote);
//				// 3. 判断是否加密
//				boolean secure = SecureType.isCipher(type);
//				// 4. 以服务器规定方式，选择加密/非加密，检测服务器
//				host = client.test(remote, secure);
//			}
//			// 关闭SOCKET
//			client.close();
//		} catch (IOException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		// 销毁
//		client.destroy();
//
//		// 判断成功!
//		boolean success = (host != null);
//
//		Logger.note(this, "checkPacketHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);
//
//		return success;
//	}
//
//	/**
//	 * 检测服务器
//	 * @param hub 服务器主机
//	 * @param count 检测次数
//	 * @return 成功返回真，否则假
//	 */
//	private boolean checkHub(SiteHost hub, int count) {
//		if (count < 1) {
//			count = 1;
//		}
//		// 信道超时
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//
//		// 1. 首先检测TCP模式
//		boolean check = checkStreamHub(hub.getStreamHost(), timeout);
//		if (check) {
//			return true;
//		}
//		// 2. 不成功，检测UDP模式
//		// 发包分时
//		int subTimeout = timeout / count;
//		if (subTimeout < 20000) {
//			subTimeout = 20000; // 最少20秒
//		}
//		// 连续检测，直到最后
//		for (int i = 0; i < count; i++) {
//			boolean success = checkPacketHub(hub.getPacketHost(), subTimeout);
//			if (success) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * 注册到目标站点
//	 * @param hub TOP/HOME/BANK站点
//	 * @param auto 自动登录
//	 * @param tracker 追踪器
//	 * @return 返回登录结果状态码
//	 */
//	public int login(SiteHost hub, boolean auto, RayLoginTracker tracker) {
//		Logger.debug(this, "login", "connect to %s", hub);
//
//		// 如果是重新登录，清除UDP服务器保存的过期数据
//		getPacketHelper().reset();
//		getReplyHelper().reset();
//
//		// 启动追踪器线程
//		startTracker(tracker);
//
//		// 检测服务器，判断有效
//		// 1. 借用JVM启动TCP/IP堆栈，启动时间超长！FixpStreamClient/FixpPacketClient可以长时间等待
//		if (!checkHub(hub, 1)) {
//			stopTracker(tracker); // 停止追踪
//			return RayEntryFlag.CHECK_FAULT;
//		}
//
//		// 定位WATCH主机地址
//		int pitchId = pitch(hub.getPacketHost());
//		// 设置结果码
//		if (tracker != null) {
//			tracker.setPitchId(pitchId);
//			tracker.setPitchHub(hub);
//		}
//
//		// 非成功码
//		if (pitchId != RayPitch.SUCCESSFUL) {
//			// 删除服务器密钥和本地密文
//			dropSecure(hub.getPacketHost());
//			removeCipher(hub.getPacketHost());
//
//			// 清除UDP服务器保存的过期数据
//			getPacketHelper().reset();
//			getReplyHelper().reset();
//			// 关闭追踪器线程
//			stopTracker(tracker);
//
//			// 错误退出
//			return RayEntryFlag.REFLECT_FAULT;
//		}
//
//		// 启动UDP通信连接
//		HubClient client = fetchHubClient(hub, false);
//		// 不成功
//		if (client == null) {
//			Logger.error(this, "login", "not found %s", hub);
//
//			// 撤销通信密钥和本地密文
//			dropSecure(hub.getPacketHost());
//			removeCipher(hub.getPacketHost());
//
//			// 关闭追踪器线程
//			stopTracker(tracker);
//			// 退出!
//			return RayEntryFlag.CONNECT_FAULT;
//		}
//
//		int who = RayEntryFlag.LOGIN_FAULT;
//		// 注册
//		boolean success = false;
//		try {
//			// 1. 获得服务器返回的节点地址，必须是TOP/HOME/BANK的其中一种。这是安全操作，防止用户登录其他无关节点！
//			Node realHub = client.getHub();
//			success = (realHub != null && isHub(realHub.getFamily()));
//
//			// 1. 检查版本
//			if (success) {
//				Version other = client.getVersion();
//				success = (Laxkit.compareTo(getVersion(), other) == 0);
//				Logger.note(this, "login", success, "check version  \"%s\" -> \"%s\"", getVersion(), other);
//				// 版本不致！
//				if (!success) {
//					who = RayEntryFlag.VERSION_NOTMATCH;
//				}
//			}
//
//			// 2. 获得激活时间
//			if (success) {
//				long ms = client.getSiteTimeout(local.getFamily());
//				success = (ms > 0);
//				if (success) {
//					setSiteTimeoutMillis(ms);
//					Logger.info(this, "login", "site timeout %d", ms);
//				}
//				// 注册延时
//				long interval = client.getHubRegisterInterval();
//				interval = registerTimer.setInterval(interval);
//				Logger.info(this, "login", "register interval: %d", interval);
//
//				// 最大延时注册时间
//				interval = client.getHubMaxRegisterInterval();
//				interval = registerTimer.setMaxInterval(interval);
//				Logger.info(this, "login", "max register interval: %d", interval);
//			}
//			// 3. 注册
//			if (success) {
//				success = false;
//				success = client.login(local);
//				Logger.note(this, "login", success, "to %s", hub);
//			}
//
//			// 4. 注册成功，删除旧密钥，设置服务器站点地址
//			if (success) {
//				// 对应SiteLauncher.login的处理，删除本地保存的服务器密钥，重新开始！
//				removeCipher(hub.getPacketHost());
//				// 设置服务器站点地址
//				setHub(realHub.duplicate());
//
//				// 刷新最后时间
//				refreshEndTime();
//				// 发送HELP握手数据包
//				hello();
//
//				// 成功！
//				who = RayEntryFlag.SUCCESSFUL;
//			}
//			// 关闭socket
//			client.close();
//		} catch (VisitException e) {
//			Logger.error(e);
//			e.printStackTrace();
//		} catch (Throwable e) {
//			Logger.fatal(e);
//			e.printStackTrace();
//		}
//		// 销毁
//		client.destroy();
//
//		// 登录成功或者否
//		setLogined(success);
//
//		// 自动状态，不设置强制循环
//		if (auto) {
//			setRoundSuspend(false);
//		} 
//		// 手动状态，如果登录成功，强制循环退出，否则仍然是强制循环
//		else {
//			// switchStatus(success);
//
//			if (success) {
//				setRoundSuspend(false);
//			} else {
//				setRoundSuspend(true);
//			}
//		}
//
//		// 成功，强制注销为假；否则，撤销管理站点上保存的私钥
//		if (success) {
//			// 重置警告信息
//			warningMuffler.reset();
//			faultMuffler.reset();
//			// 向TOP/HOME/BANK询问本集群的注册站点
//			getCommandPool().admit(new AskSite());
//			// 向TOP/HOME/BANK发送查询用户节点命令
//			getCommandPool().admit(new AskClusterMember());
//		} else {
//			// 撤销服务端密钥和本地密文
//			dropSecure(hub.getPacketHost());
//			removeCipher(hub.getPacketHost());
//		}
//
//		// 状态栏图标
//		window.setOnlineIcon(success);
//
//		// 关闭追踪器线程
//		stopTracker(tracker);
//
//		Logger.note(this, "login", success, "login to %s", hub);
//
//		return who;
//	}
//
//	/**
//	 * 推送一个新的登录站点
//	 * @param node 站点地址
//	 * @return 成功返回“真”，否则“假”。
//	 */
//	public boolean pushSite(Node node) {
//		// 更新状态栏的节点数目显示
//		window.updateStatusSites();
//		// 加入
//		return window.pushSite(node);
//	}
//
//	/**
//	 * 正常退出一个登录站点
//	 * @param node 站点地址
//	 * @return 成功返回“真”，否则“假”。
//	 */
//	public boolean dropSite(Node node) {
//		// 删除显示站点
//		RayMixedPanel panel = window.getSkeletonPanel().getMixPanel();
//		panel.dropRuntime(node);
//		// 更新状态栏的节点数目显示
//		window.updateStatusSites();
//		// 退出
//		return window.dropSite(node);
//	}
//
//	/**
//	 * 以故障状态销毁一个登录站点
//	 * @param node 站点地址
//	 * @return 成功返回“真”，否则“假”。
//	 */
//	public boolean destroySite(Node node) {
//		// 从RuntimePanel界面中删除
//		RayMixedPanel panel = window.getSkeletonPanel().getMixPanel();
//		panel.dropRuntime(node);
//
//		// 更新状态栏的节点数目显示
//		window.updateStatusSites();
//		// 注销
//		return window.destroySite(node);
//	}
//
//	/**
//	 * 保存注册成员，来自CALL/ACCOUNT节点
//	 * @param siger 用户签名
//	 */
//	public void pushRegisterMember(Siger siger) {
//		window.pushRegisterMember(siger);
//	}
//
//	/**
//	 * 删除注册成员，来自ACCOUNT/CALL节点
//	 * @param siger 用户签名
//	 */
//	public void dropRegisterMember(Siger siger) {
//		window.dropRegisterMember(siger);
//	}
//
//	/**
//	 * 保存在线成员，来自GATE/CALL节点
//	 * @param siger 用户签名
//	 */
//	public void pushOnlineMember(Siger siger) {
//		window.pushOnlineMember(siger);
//	}
//
//	/**
//	 * 删除在线成员，来自GATE/CALL节点
//	 * @param siger 用户签名
//	 */
//	public void dropOnlineMember(Siger siger) {
//		window.dropOnlineMember(siger);
//	}
//
//	/**
//	 * 更新在线成员的参数，来自GATE/CALL节点
//	 * @param siger 用户签名
//	 */
//	public void updateOnlineMember(Siger siger) {
//		window.updateOnlineMember(siger);
//	}
//
//	/**
//	 * 显示更新后的运行时参数
//	 * @param runtime 站点运行时
//	 */
//	public void modify(SiteRuntime runtime) {
//		window.modify(runtime);
//	}
//
//	/**
//	 * 更新状态栏的集群成员数目
//	 */
//	public void updateStatusMembers() {
//		window.updateStatusMembers();
//	}

//	/**
//	 * 覆盖和取代SiteLauncher.switchHub方法
//	 * @see com.laxcus.launch.SiteLauncher#switchHub(com.laxcus.site.Node)
//	 */
//	@Override
//	public boolean switchHub(Node hub) {
//		SwitchHubThread e = new SwitchHubThread(this, hub);
//		e.start();
//		return true;
//	}
//
//	/**
//	 * 退出登录。发生在窗口点击“注销”的情况下
//	 * @return 成功返回真，否则假
//	 */
//	public boolean logoutWhenWindow() {
//		// 判断是登录状态，注销！
//		boolean success = isLogined();
//		if (success) {
//			success = logout();
//		}
//		return success;
//	}
//
//	/**
//	 * 清除注销登录残留记录
//	 * 包括本地保存的密文，注销状态
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean logoutWhenSwitchHub() {
//		// 判断是登录状态，注销！
//		boolean success = isLogined();
//		if (success) {
//			success = logout();
//			// 清除HUB站点
//			if (success) setHub(null);
//		}
//		return success;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
//	 */
//	@Override
//	protected void disableProcess() {
//		Logger.error(this, "disableProcess", "network interrupted!");
//		// 设置图标断开连接
//		window.setOnlineIcon(false);
//	}

	/**
	 * 加载多语言的提示文本
	 */
	private boolean loadTips() {
		TipSelector selector = new TipSelector("conf/ray/tip/config.xml");
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

//
//	/**
//	 * 解析许可证签名
//	 * @param element
//	 */
//	private void splitLicenceSignature(org.w3c.dom.Element element) {
//		// 设置签名
//		String signature = XMLocal.getValue(element, LoginMark.SIGNATURE);
//		if (signature != null && signature.trim().length() > 0) {
//			setSignature(signature.trim());
//		}
//	}
//
//	/**
//	 * 解析登录服务器地址
//	 * @param element 
//	 * @return 返回服务器节点
//	 */
//	private Node splitHubSite(org.w3c.dom.Element element) {
//		String input = XMLocal.getValue(element, LoginMark.HUB_SITE);
//		try {
//			if (input != null) {
//				return new Node(input);
//			}
//		} catch (java.net.UnknownHostException e) {
//			Logger.error(e);
//		}
//		return null;
//	}
//
//	/**
//	 * 解析账号
//	 * @param root
//	 * @return 成功返回账号，否则是空指针
//	 */
//	private User splitUser(org.w3c.dom.Element root) {
//		org.w3c.dom.NodeList nodes = root.getElementsByTagName(LoginMark.MARK_ACCOUNT);
//		if (nodes == null || nodes.getLength() != 1) {
//			return null;
//		}
//
//		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
//		String username = XMLocal.getValue(element, LoginMark.USERNAME);
//		String password = XMLocal.getValue(element, LoginMark.PASSWORD);
//
//		// 判断是SHA数字，或者是明文
//		if (Siger.validate(username) && SHA512Hash.validate(password)) {
//			// 生成签名
//			return new User(new Siger(username), new SHA512Hash(password));
//		} else {
//			return new User(username, password);
//		}
//	}
//
//	/**
//	 * 解析本地私有参数
//	 * @param document XML文档
//	 */
//	private void splitPrivate(org.w3c.dom.Document document) {
//		// 命令模式 / 命令超时
//		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
//		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
//		setMemory(input);
//		input = XMLocal.getValue(element, SiteMark.COMMAND_TIMEOUT);
//		setCommandTimeout(input);
//		// 监视站点定时刷新间隔时间
//		input = XMLocal.getValue(element, SiteMark.OUTLOOK_INTERVAL);
//		RayTube.setTimeout(ConfigParser.splitTime(input, RayTube.defaultTimeout));
//		// 最小时间
//		input = XMLocal.getAttribute(element, SiteMark.OUTLOOK_INTERVAL, "min");
//		RayTube.setMinTimeout(ConfigParser.splitTime(input, RayTube.getMinTimeout()));
//
//
//		// 登录参数
//		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
//		if (nodes.getLength() == 1) {
//			element = (org.w3c.dom.Element) nodes.item(0);
//			String yes = element.getAttribute(LoginMark.AUTO);
//			boolean auto = yes.matches("^\\s*(?i)(?:YES|TRUE)\\s*$");
//
//			// 在YES情况下，解析许可证签名
//			if (auto) {
//				splitLicenceSignature(element);
//			}
//			Node hub = splitHubSite(element);
//			User user = splitUser(element);
//
//			boolean success = (hub != null && user != null);
//			if (success) {
//				success = SiteTag.isHub(hub.getFamily());
//			}
//			if (success) {
//				LoginToken token = new LoginToken(auto, hub, user);
//				window.setLoginToken(token);
//			}
//		}
//	}
//
//	/**
//	 * 加载本地配置
//	 * @param filename 配置文件名
//	 * @return 成功返回真，否则假
//	 */
//	private boolean loadLocal(String filename) {
//		filename = ConfigParser.splitPath(filename);
//		if (!Laxkit.hasFile(filename)) {
//			Logger.error(this, "localLocal", "not found %s", filename);
//			return false;
//		}
//		
//		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
//		if (document == null) {
//			return false;
//		}
//
//		// 本地私有参数
//		splitPrivate(document);
//
//		// 解析站点配置
//		boolean success = splitSingleSite(local, document);
//		// 解析和设置回显配置
//		if (success) {
//			success = splitEcho(document);
//		}
//		// 生成RSA密钥令牌
//		if (success) {
//			success = createDefaultSecureToken(document);
//		}
//		// 解析自定义资源
//		if (success) {
//			success = loadCustom(document);
//		}
//		// 加载多语言提示文本
//		if (success) {
//			success = loadTips();
//		}
//
//		// 加载日志并且启动
//		if (success) {
//			success = loadLogResourceWithLocal(filename);
//		}
//
//		return success;
//	}
//
//	/**
//	 * 加载颜色参数
//	 */
//	private void loadColors() {
//		String xmlPath = "conf/watch/color/color.txt";
//		ColorTemplate.load(xmlPath);
//	}
//	
//	/**
//	 * 从指定的文本中加载那些公共显示命令
//	 * 适用于所有图形界面的UI
//	 */
//	private void loadUIText() {
//		String xmlPath = "conf/ray/ui/config.xml";
//		LocalSelector selector = new LocalSelector(xmlPath);		
//		String path = selector.findPath("resource");
//
//		// 解析参数
//		try {
//			UISplitter splitter = new UISplitter();
//			splitter.load(path);
//			java.util.List<String> keys = splitter.getKeys();
//			for(String key : keys) {
//				Object value = splitter.find(key);
//				UIManager.put(key, value);
//			}
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//	}
//
////	/**
////	 * 加载皮肤配置，同是更新外观界面
////	 * @return 成功返回真，否则假
////	 */
////	private boolean loadSkins() {
////		// 1. 从配置文件中加载皮肤参数，不成功退出
////		boolean success = skinLoader.load("conf/watch/skin/config.xml");
////		if (!success) {
////			return false;
////		}
////		// 2. 读取"watch.conf"配置文件，把"skin.name"参数读取出来
////		String name = readSkinName();
////		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
////		if (name == null) {
////			name = "normal"; // normal关键字在config.xml配置文件里定义
////		}
////
////		// 假设不成功
////		success = false;
////		// 3. 找到匹配的皮肤方案
////		SkinToken token = skinLoader.findSkinTokenByName(name);
////		if (token != null) {
////			// 3. 切换到主题界面
////			success = token.updateTheme(false);
////			if (success) {
////				// 选中它
////				skinLoader.exchangeCheckedSkinToken(token.getName());
////				// 定义外观
////				Skins.setLookAndFeel(token.getLookAndFeel());
////				Skins.setSkinName(token.getName());
////			}
////		} else {
////			// 启动“Nimbus”外观，“Metal”为暗黑！
////			success = UITools.updateLookAndFeel("Nimbus", null, null);
////			if (success) {
////				int count = SkinToken.loadSkins("conf/watch/skin/nimbus_normal.txt");
////				success = (count > 0);
////			}
////			// 选中Nimbus和确定外观
////			if (success) {
////				skinLoader.exchangeCheckedSkinToken("Nimbus");
////				Skins.setLookAndFeel(Skins.Nimbus);
////				Skins.setSkinName("normal"); // normal是脚本中的定义
////			}
////		}
////
////		return success;
////	}
//
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
//			name = "gray"; // normal关键字在config.xml配置文件里定义
//		}
//
//		// 假设不成功
//		success = false;
//		// 3. 找到匹配的皮肤方案
//		SkinToken token = skinLoader.findSkinTokenByName(name);
//		if (token != null) {
//			// 3. 切换到主题界面
//			success = token.updateTheme(true);
//			if (success) {
//				// 选中它
//				skinLoader.exchangeCheckedSkinToken(token.getName());
//				// 定义外观
//				Skins.setLookAndFeel(token.getLookAndFeel());
//				Skins.setSkinName(token.getName());
//			}
//		} else {
////			// 启动“Nimbus”外观，“Metal”为暗黑！
////			success = UITools.updateLookAndFeel("Nimbus", null, null);
////			if (success) {
////				int count = SkinToken.loadSkins("conf/watch/skin/nimbus_normal.txt");
////				success = (count > 0);
////			}
////			// 选中Nimbus和确定外观
////			if (success) {
////				skinLoader.exchangeCheckedSkinToken("Nimbus");
////				Skins.setLookAndFeel(Skins.Nimbus);
////				Skins.setSkinName("normal"); // normal是脚本中的定义
////			}
//			
//			// 设置为默认的“Metal”外观，白色
//			String clazz = "com.laxcus.util.skin.GrayMetalTheme";
//			SkinSheet sheet = new FlatSkinSheet();
//			success = UITools.updateLookAndFeel("Metal", clazz, sheet);
//			if (success) {
//				int count = SkinToken.loadSkins("conf/watch/skin/metal_gray.txt");
//				success = (count > 0);
//			}
//			if (success) {
//				skinLoader.exchangeCheckedSkinToken("gray");
//				Skins.setLookAndFeel(Skins.Metal);
//				Skins.setSkinName("gray"); // "gray"是脚本中的定义
//			}
//		}
//
//		return success;
//	}
//	
//	/**
//	 * 从本地配置中读取皮肤配置名称
//	 * @return 返回字符串或者空指针
//	 */
//	private String readSkinName() {
//		// 建立配置目录
//		File dir = createDirectory();
//
//		File file = new File(dir, "watch.conf");
//		// 读磁盘文件
//		UITacker tracker = new UITacker();
//		int who = tracker.read(file);
//		if (who < 1) {
//			return null;
//		}
//		// 找到"skin.name"对应值
//		return tracker.getString(RayProperties.skinName); // "skin.name");
//	}
//
//	/**
//	 * 返回皮肤加载器
//	 * @return SkinTokenLoader实例句柄
//	 */
//	public SkinTokenLoader getSkinLoader() {
//		return skinLoader;
//	}
//
//	/**
//	 * 建立目录
//	 * @return
//	 */
//	private File createDirectory() {
//		String bin = System.getProperty("user.dir");
//		bin += "/../conf";
//		File file = new File(bin);
//		boolean success = (file.exists() && file.isDirectory());
//		if (!success) {
//			success = file.mkdirs();
//		}
//		return (success ? file : null);
//	}
//
//	/**
//	 * 读WATCH站点参数配置
//	 */
//	private void writeConfigure() {
//		UITacker tracker = new UITacker();
//		// 窗口范围
//		tracker.put(RayProperties.boundFrame, RayProperties.readWindowBound());
//
//		// 中央面板分割线位置
//		tracker.put(RayProperties.dividerCenterPane, RayProperties.readCenterPaneDeviderLocation());
//		// 浏览窗口分割线位置
//		tracker.put(RayProperties.dividerBrowserPane, RayProperties.readBrowserPaneDeviderLocation());
//		// 站点浏览面板分割线
//		tracker.put(RayProperties.dividerSiteBrowserPane, RayProperties.readSiteBrowserPaneDeviderLocation());
//		// 成员浏览面板分割线
//		tracker.put(RayProperties.dividerMemberBrowserPane, RayProperties.readMemberBrowserPaneDeviderLocation());
//
//		// 字体实例
//		tracker.put(RayProperties.fontSystem, RayProperties.readSystemFont()); 
//		tracker.put(RayProperties.fontBrowserSite, RayProperties.readBrowserSiteFont()); 
//		tracker.put(RayProperties.fontBrowserMember, RayProperties.readBrowserMemberFont()); 
//		tracker.put(RayProperties.fontCommand, RayProperties.readCommandPaneFont()); 
//		tracker.put(RayProperties.fontTabbed, RayProperties.readTabbedFont());
//		tracker.put(RayProperties.fontMessage, RayProperties.readTabbedMessageFont()); 
//		tracker.put(RayProperties.fontTable, RayProperties.readTabbedTableFont()); 
//		tracker.put(RayProperties.fontSiteStatus, RayProperties.readTabbedRuntimeFont()); 
//		tracker.put(RayProperties.fontLog, RayProperties.readTabbedLogFont());
//		// 主菜单字体
//		tracker.put(RayProperties.fontMenu, RayProperties.readMainMenuFont());
//
//		// 帮助字体类型
//		String helpFamily = context.getTemplateFontName();
//		tracker.put(RayProperties.fontHelp, helpFamily);
//		// 播放声音
//		boolean play = SoundPlayer.getInstance().isPlay();
//		tracker.put(RayProperties.soundPlay, play);
//		// 显示的日志数目
//		tracker.put(RayProperties.logElements, RayProperties.readLogElements());
//		// 拒绝显示日志
//		tracker.put(RayProperties.logForbid, RayProperties.readLogForbid());
//
//		// 界面皮肤颜色名称
//		SkinToken token = skinLoader.findCheckedSkinToken();
//		if (token != null) {
//			tracker.put(RayProperties.skinName, token.getName());
//		}
//
//		// 配置参数写入指定的目录
//		File dir = createDirectory();
//		if (dir != null) {
//			File file = new File(dir, "watch.conf");
//			// 写入磁盘
//			tracker.write(file);
//		}
//	}
//
//	/**
//	 * 读WATCH站点配置并且设置参数
//	 * @return 成功返回真，否则假
//	 */
//	private boolean readConfigure() {
//		// 选择默认字体做为显示字体
//		Font[] defaultFonts = readPlatformFont();
//		if (defaultFonts != null) {
//			context.setTemplateFontName(defaultFonts[0].getFamily());
//		}
//
//		// 读本地配置文件
//		File dir = createDirectory();
//		if (dir == null) {
//			return false;
//		}
//		File file = new File(dir, "watch.conf");
//
//		// 读磁盘文件
//		UITacker tracker = new UITacker();
//		int who = tracker.read(file);
//		if (who < 1) {
//			return false;
//		}
//
//		// 窗口范围
//		Rectangle rect = tracker.getRectangle(RayProperties.boundFrame); 
//		if (rect != null) {
//			RayProperties.writeWindowBound(rect);
//		}
//
//		// 中央面板分割线位置
//		Integer pixel = tracker.getInteger(RayProperties.dividerCenterPane);
//		if (pixel != null) {
//			RayProperties.writeCenterPaneDeviderLocation(pixel.intValue());
//		}
//		// 浏览面板分割线位置
//		pixel = tracker.getInteger(RayProperties.dividerBrowserPane);
//		if(pixel !=null){
//			RayProperties.writeBrowserPaneDeviderLocation(pixel.intValue());
//		}
//		// 站点浏览面板分割线
//		pixel = tracker.getInteger(RayProperties.dividerSiteBrowserPane);
//		if (pixel != null) {
//			RayProperties.writeSiteBrowserPaneDeviderLocation(pixel.intValue());
//		}
//		// 成员浏览面板分割线
//		pixel = tracker.getInteger(RayProperties.dividerMemberBrowserPane);
//		if (pixel != null) {
//			RayProperties.writeMemberBrowserPaneDeviderLocation(pixel.intValue());
//		}
//
//		// 系统环境字体
//		Font font = tracker.getFont(RayProperties.fontSystem);
//		if (font != null) {
//			// 更新系统环境字体
//			UITools.updateSystemFonts(font);
//			RayProperties.writeSystemFont(font);
//		}
//		// 站点浏览窗口字体
//		font = tracker.getFont(RayProperties.fontBrowserSite); 
//		if (font != null) {
//			RayProperties.writeBrowserSiteFont(font);
//		}
//		// 站点浏览窗口字体
//		font = tracker.getFont(RayProperties.fontBrowserMember); 
//		if (font != null) {
//			RayProperties.writeBrowserMemberFont(font);
//		}
//		// 命令字体
//		font = tracker.getFont(RayProperties.fontCommand); 
//		if (font != null) {
//			RayProperties.writeCommandPaneFont(font);
//		}
//		// 选项卡字体
//		font = tracker.getFont(RayProperties.fontTabbed); 
//		if (font != null) {
//			RayProperties.writeTabbedFont(font);
//		}
//		// 消息字体
//		font = tracker.getFont(RayProperties.fontMessage); 
//		if (font != null) {
//			RayProperties.writeTabbedMessageFont(font);
//		}
//		// 表格字体
//		font = tracker.getFont(RayProperties.fontTable); 
//		if (font != null) {
//			RayProperties.writeTabbedTableFont(font);
//		}
//		// 节点状态字体
//		font = tracker.getFont(RayProperties.fontSiteStatus); 
//		if (font != null) {
//			RayProperties.writeTabbedRuntimeFont(font);
//		}
//		// 日志字体
//		font = tracker.getFont(RayProperties.fontLog); 
//		if (font != null) {
//			RayProperties.writeTabbedLogFont(font);
//		}
//		// 主菜单字体
//		font = tracker.getFont(RayProperties.fontMenu); 
//		if (font != null) {
//			RayProperties.writeMainMenuFont(font);
//		}
//		// 帮助字体类型
//		String helpFamily = tracker.getString(RayProperties.fontHelp); 
//		if (helpFamily != null) {
//			RayProperties.writeHelpMenuFontFamily(helpFamily);
//			RayLauncher.getInstance().getCommentContext().setTemplateFontName(helpFamily);
//		}
//		// 声音
//		Boolean play = tracker.getBoolean(RayProperties.soundPlay); 
//		if (play != null) {
//			boolean yes = play.booleanValue(); 
//			RayProperties.writeSoundPlay(yes);
//			SoundPlayer.getInstance().setPlay(yes);
//		}
//		// 日志显示数目
//		Integer logs = tracker.getInteger(RayProperties.logElements);
//		if (logs != null) {
//			RayProperties.writeLogElements(logs.intValue());
//		}
//		// 拒绝显示日志
//		Boolean forbid = tracker.getBoolean(RayProperties.logForbid);
//		if (forbid != null) {
//			boolean yes = forbid.booleanValue();
//			RayProperties.writeLogForbid(yes);
//		}
//
//		// 返回成功
//		return true;
//	}
//
//
//	/**
//	 * 启动闪屏
//	 */
//	public void startSplash() {
//		ResourceLoader loader = new ResourceLoader("conf/watch/image/splash/");
//		ImageIcon icon = null;
//		// 判断中文或者其它，显示！
//		if (Laxkit.isSimplfiedChinese()) {
//			icon = loader.findImage("zh_CN/splash.jpg");
//		} else {
//			icon = loader.findImage("en_US/splash.jpg");
//		}
//
//		// 设置界面
//		splash.createWindow(icon);
//		// 启动界面
//		splash.start();
//	}
//
//	/**
//	 * 启动闪屏
//	 */
//	public void stopSplash() {
//		splash.stop();
//	}
//
//	/**
//	 * WATCH启动入口
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		if (args == null || args.length < 1) {
//			Logger.error("parameters missing!");
//			Logger.gushing();
//			return;
//		}
//
//		// 句柄
//		RayLauncher launcher = RayLauncher.getInstance();
//		// 启动闪屏窗口
//		launcher.startSplash();
//
//		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
//		JNILoader.init();
//
//		// 将颜色加载到内存集合中
//		launcher.loadColors();
//
//		// 加载外观和皮肤
//		boolean loaded = launcher.loadSkins();
//		if (!loaded) {
//			Logger.error("cannot be load skin theme!");
//			launcher.stopSplash();
//			Logger.gushing();
//			System.exit(0);
//			return;
//		}
//
//		// 读取本地配置
//		launcher.readConfigure();
//
//		String filename = args[0];
//		boolean success = launcher.loadLocal(filename);
//		Logger.note("RayLauncher.main, load local", success);
//
//		// 启动线程
//		if (success) {
//			success = launcher.start();
//			Logger.note("RayLauncher.main, start service", success);
//		}
//
//		// 启动资源检测
//		if (success) {
//			launcher.loadTimerTasks(filename);
//		} else {
//			launcher.stopSplash();
//			Logger.gushing();
//			launcher.stopLog();
//			System.exit(0);
//		}
//	}


	/**
	 * 从总配置文件中选出所关联语言的命令帮助文件路径，加载上下文
	 */
	protected void loadCommentContext() {
		LocalSelector selector = new LocalSelector("conf/ray/help/config.xml");		
		String path = selector.findPath("resource");
		context.load(path, GUIKit.isHighScreen());
	}

	/**
	 * 从指定的文本中加载那些公共显示命令
	 * 适用于所有图形界面的UI
	 */
	private void loadUIText() {
		String xmlPath = "conf/ray/ui/config.xml";
		LocalSelector selector = new LocalSelector(xmlPath);		
		String path = selector.findPath("resource");

		// 解析参数
		try {
			UISplitter splitter = new UISplitter();
			splitter.load(path);
			java.util.List<String> keys = splitter.getKeys();
			for(String key : keys) {
				Object value = splitter.find(key);
				UIManager.put(key, value);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * 返回命令解释语境
	 * @return 命令语境
	 */
	public CommentContext getCommentContext() {
		return context;
	}

	/**
	 * 检查平台字体
	 */
	private boolean checkPlatformFont() {
		FontRector rector = new FontRector();
		return rector.checkPlatformFont();
	}

	/**
	 * 退出登录
	 */
	public void disconnect() {
		// 退出登录
		logout();
	}

	/**
	 * 播放声音
	 * @param who
	 */
	public void playSound(int who){
		SoundKit.play(who);
	}

	/**
	 * 加载声音参数
	 */
	private void loadSound() {
		ResourceLoader loader = new ResourceLoader("conf/ray/sound");
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
			return RayPitch.NOT_FOUND; // 不能定位主机
		}

		Logger.info(this, "pitch", "to hub %s, local is %s", hub, reflect);

		Address address = reflect.getAddress();
		// 本地不包含这个地址，那一定是NAT地址，返回错误码
		boolean exists = Address.contains(address);
		if (!exists) {
			return RayPitch.NAT_ERROR;
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
			return RayPitch.ADDRESS_NOTMATCH;
		}

		// 成功
		return RayPitch.SUCCESSFUL;
	}

	
	/**
	 * 启动追踪器线程
	 * @param tracker 追踪器
	 */
	private void startTracker(RayLoginTracker tracker) {
		if (tracker != null) {
			tracker.start();
		}
	}

	/**
	 * 关闭追踪器线程
	 * @param tracker 追踪器
	 */
	private void stopTracker(RayLoginTracker tracker) {
		if (tracker != null) {
			tracker.stop();
		}
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
	public int login(SiteHost hub, boolean auto, RayLoginTracker tracker) {
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
			return RayEntryFlag.CHECK_FAULT;
		}

		// 定位WATCH主机地址
		int pitchId = pitch(hub.getPacketHost());
		// 设置结果码
		if (tracker != null) {
			tracker.setPitchId(pitchId);
			tracker.setPitchHub(hub);
		}

		// 非成功码
		if (pitchId != RayPitch.SUCCESSFUL) {
			// 删除服务器密钥和本地密文
			dropSecure(hub.getPacketHost());
			removeCipher(hub.getPacketHost());

			// 清除UDP服务器保存的过期数据
			getPacketHelper().reset();
			getReplyHelper().reset();
			// 关闭追踪器线程
			stopTracker(tracker);

			// 错误退出
			return RayEntryFlag.REFLECT_FAULT;
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
			return RayEntryFlag.CONNECT_FAULT;
		}

		int who = RayEntryFlag.LOGIN_FAULT;
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
					who = RayEntryFlag.VERSION_NOTMATCH;
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
				who = RayEntryFlag.SUCCESSFUL;
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
			getCommandPool().admit(new AskSite(false));
			// 向TOP/HOME/BANK发送查询用户节点命令
			getCommandPool().admit(new AskClusterMember(false));
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

//	/**
//	 * 自动登录，覆盖FrontLauncher.login方法
//	 * 
//	 * @see com.laxcus.front.FrontLauncher#login()
//	 */
//	@Override
//	public boolean login() {
//		// 如果没有达到GATE节点要求的重新注册间隔时间，忽略！
//		if (!canAutoReloginInterval()) {
//			return false;
//		}
//
//		// 确定初始HUB站点地址（ENTRANCE站点）
//		Node entrance = getInitHub();
//		if (entrance == null) {
//			Logger.error(this, "login", "entrance site is null!");
//			return false;
//		}
//
//		// 自动注销，SiteLauncher.defaultProcess方法不进入强制自循环！
//		__logout(true);
//
//		// 在重新注册前，刷新最后调用的时间
//		refreshEndTime();
//
//		//		// 故障声音
//		//		SoundKit.playError();
//
//		// 清除窗口，自动登录，返回结果
//		return window.__auto_login(entrance);
//	}
	
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
		RayRegisterMemberBasket.getInstance().clear();
		RayFrontMemberBasket.getInstance().clear();

		// 清除节点运行时
		RaySiteRuntimeBasket.getInstance().clear();
		// 清除节点
		SiteOnRayPool.getInstance().clear();

		// 播放错误声音
		playSound(SoundTag.ERROR);
		// 启动自动登录
		return window.__auto_login(hub);
	}
	
	//	/**
	//	 * 弹出确认窗口
	//	 * @param content 显示文本
	//	 * @return 接受返回真，否则假
	//	 */
	//	private boolean showConfirmDialog(String content) {		
	//		String title = window.getTitle();
	////		public static boolean showYesNoDialog(Component frame, String title, String content) {
	//		
	//		int who = MessageBox.showYesNoDialog(window, title, content);
	//		
	////		int who = MessageDialog.showMessageBox(window, title,
	////				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
	//		
	//		return (who == JOptionPane.YES_OPTION);
	//	}

	/**
	 * 弹出确认窗口
	 * @param content 显示文本
	 * @return 接受返回真，否则假
	 */
	private boolean showConfirmDialog(String content) {		
		String title = window.getTitle();
		return MessageBox.showYesNoDialog(window, title, content);
	}

	/**
	 * 弹出对话框提示信息
	 */
	public boolean confirm(int no) {
		String content = message(no);
		return showConfirmDialog(content);
	}

	/**
	 * 弹出对话窗口
	 */
	public boolean confirm(int no, Object... params) {
		String content = message(no, params);
		return showConfirmDialog(content);
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#ticking()
//	 */
//	@Override
//	public void ticking() {
//		// 触发动画显示
//		window.flash();
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.InvokerMessenger#startInvoker(com.laxcus.echo.invoke.EchoInvoker)
//	 */
//	@Override
//	public void startInvoker(EchoInvoker invoker) {
//		// 如果不是远程执行的，忽略它
//		if (!invoker.isDistributed()) {
//			return;
//		}
//		// 发出
//		window.rolling(true);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.InvokerMessenger#stopInvoker(com.laxcus.echo.invoke.EchoInvoker, boolean)
//	 */
//	@Override
//	public void stopInvoker(EchoInvoker invoker, boolean success) {
//		// 如果不是远程执行的，忽略它
//		if (!invoker.isDistributed()) {
//			return;
//		}
//
//		// 不成功或者退出时，记录流量
//		if (!success || invoker.isQuit()) {
//			long rs = invoker.getReceiveFlowSize();
//			long ss = invoker.getSendFlowSize();
//			window.addFlows(rs, ss);
//		}
//		window.rolling(false);
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isDriver()
//	 */
//	@Override
//	public boolean isDriver() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isConsole()
//	 */
//	@Override
//	public boolean isConsole() {
//		return false;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isTerminal()
//	 */
//	@Override
//	public boolean isTerminal() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isEdge()
//	 */
//	@Override
//	public boolean isEdge() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isRay()
//	 */
//	@Override
//	public boolean isRay() {
//		return true;
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#isApplication()
//	 */
//	@Override
//	public boolean isApplication() {
//		return false;
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
//	 */
//	@Override
//	public void showStatusText(String text) {
//		window.setStatusText(text);
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
//	 */
//	@Override
//	public void showGrade(int grade) {
//		if (GradeTag.isAdministrator(grade)) {
//			String e = UIManager.getString("grade.administrator"); 
//			showStatusText(e);
//			window.setAdministratorIcon();
//		} else if (GradeTag.isUser(grade)) {
//			String e = UIManager.getString("grade.user"); 
//			showStatusText(e);
//			window.setUserIcon();
//		} else {
//			String e = UIManager.getString("grade.undefined");
//			showStatusText(e);
//			window.setNobodyIcon();
//		}
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
//	 */
//	@Override
//	public StaffOnRayPool getStaffPool() {
//		return StaffOnRayPool.getInstance();
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.front.FrontLauncher#forsake()
//	 */
//	@Override
//	public void forsake() {
//		RayForsakeThread e = new RayForsakeThread(this);
//		e.start();
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#setMaxLogs(int)
	 */
	public int setMaxLogs(int n) {
		int logs = window.setMaxLogs(n);

		// 返回结果!
		return logs;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public RayCommandPool getCommandPool() {
		return RayCommandPool.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public RayInvokerPool getInvokerPool() {
		return RayInvokerPool.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		// 设置图标为中断
		window.setOnlineIcon(false);
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

		//		// 修改日志刷新频率
		//		// 1. 达到60%比率，日志发送调整到最低值
		//		// 2. 超过30%比率，降低日志发送
		//		// 3. 低于15%比率，提高日志发送
		//		if (rate >= 60.0f) {
		//			RayLogTrustor.getInstance().low();
		//		} else if (rate >= 30.0f) {
		//			RayLogTrustor.getInstance().descent();
		//		} else if (rate < 15.0f) {
		//			RayLogTrustor.getInstance().rise();
		//		}

		// 修改日志刷新频率
		// 1. 达到90%比率，日志发送调整到最低值
		// 2. 超过50%比率，降低日志发送
		// 3. 低于30%比率，提高日志发送
		if (rate >= 90.0f) {
			RayLogTrustor.getInstance().low();
		} else if (rate > 50.0f) {
			RayLogTrustor.getInstance().descent();
		} else if (rate < 30.0f) {
			RayLogTrustor.getInstance().rise();
		}

		// 写入单元
		writeApplications(false);
		writeDesktopButtons(false);
		// 写入环境变量
		writeEnvironement(false);

		//		testFire();
	}

	//	long nowTime = System.currentTimeMillis();
	//	private void testFire() {
	//		if(System.currentTimeMillis() - nowTime <= 60000) {
	//			return;
	//		}
	//		nowTime = System.currentTimeMillis();
	//		// 测试，显示窗口
	//		MemoryMissing mm = new MemoryMissing(getListener());
	//		this.getCommandPool().admit(mm);
	//		this.getCommandPool().admit(new VMMemoryMissing(this.getListener()));
	//		this.getCommandPool().admit(new DiskMissing(this.getListener()));
	//	}

	/**
	 * 写单元配置
	 */
	private void writeApplications(boolean force) {
		// 不可用时，忽略它
		if (!RTManager.getInstance().isUsabled()) {
			return;
		}
		// 判断更新...
		if (!force) {
			if (!RTManager.getInstance().isUpdated()) {
				return;
			}
		}

		// 导入资源
		File dir = RaySystem.createRuntimeDirectory();
		File file = new File(dir, "applications.conf");
		// 不写系统应用
		int elements = RTManager.getInstance().writeRoots(file, true);

		RTManager.getInstance().resetUpdate();

		Logger.info(this, "writeApplications", "write applications %d", elements);
	}

	/**
	 * 写入按纽参数
	 * @param force
	 */
	private void writeDesktopButtons(boolean force) {
		// 不可用，忽略
		if (!RayController.getInstance().isUsabled()) {
			return;
		}
		// 判断是强制
		if (!force) {
			if (!RayController.getInstance().isUpdated()) {
				return;
			}
		}

		RayController.getInstance().writeButtons();
		RayController.getInstance().resetUpdate();
	}

	/**
	 * 启动FIXP监听
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean loadListen() {
		// 设置命令管理池
		CommandVisitOnWatch.setCommandPool(getCommandPool());
		// 绑定监听
		Class<?>[] clazzs = { CommandVisitOnWatch.class };
		return loadSingleListen(clazzs, local.getNode());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#init()
	 */
	@Override
	public boolean init() {
		Logger.debug(this, "init", "local is %s", local);

		// 1. 预初始化
		boolean success = preinit();
		Logger.note(this, "init", "preinit", success);
		// 2. 启动FIXP服务器
		if(success) {
			success = loadListen();
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 4. 调用子类"launch"方法，启动图形/字符中的一种服务，注册ENTRANCE站点，重定向到GATE站点。
		if (success) {
			success = launch();
		}
		Logger.note(this, "init", "launch", success);
		
		Logger.note(this, "init", "load task pool", success);

		// 不成功，关闭退出
		if (!success) {
			stopPool();
			stopListen();
			// 销毁
			destroy();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncherThread#process()
	 */
	@Override
	public void process() {
		defaultProcess();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		if (isLogined()) {
			logout();
		}
		// 关闭管理池
		stopPool();
		// 停止FIXP服务器
		stopListen();
		// 销毁子类资源
		destroy();
		// 销毁日志
		stopLog();
	}

	/**
	 * 预加载
	 * @return
	 */
	protected boolean preinit() {
		// 1. 加载声音
		loadSound();

		// 2. 冗余加速资源处理
		RayReduceSpeeder rs = new RayReduceSpeeder();
		rs.start();

		// 返回成功
		return true;
	}

	/**
	 * 启动
	 * @return 返回真或者假
	 */
	protected boolean launch() {
		// 启动窗口，注册到GATE节点
		boolean success = window.showWindow();

		// 设置显示前端。不要设置显示界面，由各个运行任务来设置。
		if (success) {
			//			PutTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			//			EndTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			//			NearTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());

//			// 边缘容器管理池启动（必须保证在图形界面显示成功后才能启动，否则在没有启动图形界面前，TubPool调用图形界面会出错）
//			TubPool.getInstance().start();

			// 启动日志代理（保证在窗口可视情况下输出日志，否则会有UI卡死现象出现）
			RayLogTrustor.getInstance().start();
		}

		// 加载配置
		if (success && CustomConfig.isValidate()) {
			// 加载类定义
			CustomClassLoader loader = new CustomClassLoader();
			loader.load();
			// 加载自定义命令关键字
			//			String path = CustomConfig.getTokenPath();
			//			window.addCommandTokens(path);
		}

		return success;
	}	

	/**
	 * 销毁
	 */
	protected void destroy() {
		//		// 写入配置
		//		writeConfigure();

		// 写入环境变量
		writeEnvironement(true);

		// 强制写入应用参数
		writeApplications(true);
		// 强制写入桌面按纽
		writeDesktopButtons(true);

		// 销毁窗口
		window.dispose();

		//		try {
		//			printFromClass();
		//		} catch (Throwable e) {
		//			e.printStackTrace();
		//		}
	}

//	/**
//	 * 加载服务池
//	 * @return
//	 */
//	protected boolean loadPool() {
//		// 委托线程
//		VirtualThread[] threads = new VirtualThread[] {
//				SwingDispatcher.getInstance(), SoundPlayer.getInstance() };
//		startThreads(threads);
//
//		// 业务管理池
//		VirtualPool[] pools = new VirtualPool[] {
//				ApplicationPool.getInstance(),
//				RayInvokerPool.getInstance(), RayCommandPool.getInstance(),
//				StaffOnRayPool.getInstance(),
//				AuthroizerGateOnFrontPool.getInstance(),
//				CallOnFrontPool.getInstance() , GuideTaskPool.getInstance()};
//
//		// 注意！GuideTaskPool在这里启动
//
//		// 启动全部管理池
//		return startAllPools(pools);
//	}
//
//	/**
//	 * 加载任务池
//	 * @return
//	 */
//	protected boolean loadTaskPool() {
//		// 设置事件监听器
//		PutTaskPool.getInstance().setTaskListener(this);
//		EndTaskPool.getInstance().setTaskListener(this);
//		NearTaskPool.getInstance().setTaskListener(this);
//
//		// CONDUCT.PUT阶段资源代理
//		PutTaskPool.getInstance().setPutTrustor(getStaffPool());
//		// ESTABLISH.END阶段资源代理
//		EndTaskPool.getInstance().setEndTrustor(getStaffPool());
//		// CONTACT.NEAR阶段资源代理
//		NearTaskPool.getInstance().setNearTrustor(getStaffPool());
//
//		// 业务管理池
//		VirtualPool[] pools = new VirtualPool[] { 
//				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
//				NearTaskPool.getInstance() };
//
//		// 启动全部管理池
//		return startAllPools(pools);
//	}
//
//	/**
//	 * 停止任务池
//	 */
//	protected void stopPool() {
//		// 管理池
//		VirtualPool[] pools = new VirtualPool[] {
//				// 应用容器池
//				ApplicationPool.getInstance(),
//				// DESKTOP命令管理池
//				RayCommandPool.getInstance(),
//				// DESKTOP调用器管理池
//				RayInvokerPool.getInstance(), CallOnFrontPool.getInstance(),
//				AuthroizerGateOnFrontPool.getInstance(),
//				StaffOnRayPool.getInstance(), TubPool.getInstance(),
//				PutTaskPool.getInstance(), EndTaskPool.getInstance(), 
//				NearTaskPool.getInstance(), GuideTaskPool.getInstance()};
//
//		// 线程
//		VirtualThread[] threads = new VirtualThread[] {
//				SoundPlayer.getInstance(),
//				SwingDispatcher.getInstance(),
//				RayLogTrustor.getInstance() };
//
//		// 先关闭线程，再关闭管理池
//		stopThreads(threads);
//		// 关闭全部管理池
//		stopAllPools(pools);
//	}

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
				RayInvokerPool.getInstance(), RayCommandPool.getInstance(),
				SiteOnRayPool.getInstance() };
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		// 停止业务管理池
		VirtualPool[] pools = new VirtualPool[] {
				RayInvokerPool.getInstance(), RayCommandPool.getInstance(),
				SiteOnRayPool.getInstance()};

		// 线程
		VirtualThread[] threads = new VirtualThread[] {
				SoundPlayer.getInstance(), SwingDispatcher.getInstance(),
				RayLogTrustor.getInstance() };

		// 先停止线程，再停止管理池
		stopThreads(threads);
		stopAllPools(pools);
	}

	
	/**
	 * 返回皮肤加载器
	 * @return SkinTokenLoader实例句柄
	 */
	public SkinTokenLoader getSkinLoader() {
		return skinLoader;
	}

	/**
	 * 加载颜色参数
	 */
	private void loadColors() {
		String xmlPath = "conf/ray/color/color.txt";
		ColorTemplate.load(xmlPath);
	}

//	/**
//	 * 建立目录
//	 * @return
//	 */
//	private File createDirectoryX() {
//		String bin = System.getProperty("user.dir");
//		bin += "/../conf";
//		File file = new File(bin);
//		boolean success = (file.exists() && file.isDirectory());
//		if (!success) {
//			success = file.mkdirs();
//		}
//		return (success ? file : null);
//	}

	/**
	 * 读环境变量
	 * @return
	 */
	private boolean readEnvironement() {
		File dir = RaySystem.createRuntimeDirectory();
		File file = new File(dir, "environment.conf");
		RTEnvironment evnironment = PlatformKit.getRTEnvironment();
		boolean success = false;
		if (file.exists() && file.isFile()) {
			try {
				evnironment.infuse(file);
				success = true;
			} catch (IOException e) {
				Logger.error(e);
			}
		} else {
			evnironment.createDefault();// 生成默认值
			success = true;
		}

		if (success) {
			// 声音
			String paths = "Sound/Play";
			if (RTKit.hasBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, paths)) {
				boolean play = RTKit.readBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, paths);
				SoundPlayer.getInstance().setPlay(play);
			}
			// 系统字体
			Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
			if (font != null) {
				UITools.updateSystemFonts(font);
			}
		}
		return success;
	}

	/**
	 * 写环境变量
	 * @return
	 */
	private void writeEnvironement(boolean force) {
		RTEnvironment evnironment = PlatformKit.getRTEnvironment();
		// 不可用时，忽略它
		if (!evnironment.isUsabled()) {
			return;
		}
		// 非强制状态，且没有更新时，退出
		if (!force) {
			if (!evnironment.isUpdated()) {
				return;
			}
		}

		// 界面皮肤颜色名称
		SkinToken token = skinLoader.findCheckedSkinToken();
		if (token != null) {
			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "Skin/Name", token.getName());
		}

		// 声音
		boolean play = SoundPlayer.getInstance().isPlay();
		RTKit.writeBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, "Sound/Play", play);

		File dir = RaySystem.createRuntimeDirectory();
		File file = new File(dir, "environment.conf");

		// 写入磁盘
		try {
			evnironment.effuse(file);
			evnironment.setUpdated(false); // 恢复状态
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * 从环境变量读取皮肤配置名称
	 * @return 返回字符串或者空指针
	 */
	private String readSkinName() {
		return RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "Skin/Name");
	}

	//	private void testJSliderIcon() {
	//		Icon icon = UIManager.getIcon( "Slider.horizontalThumbIcon" );
	//		if(icon == null) {
	//			System.out.println("null icon");
	//		}
	//		System.out.printf("icon is w:%d h:%d\n", icon.getIconWidth(), icon.getIconHeight());
	//		System.out.printf("icon class is %s\n", icon.getClass().getName());
	//	}

	//	/**
	//	 * 加载皮肤配置，同是更新外观界面
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean loadSkins() {
	//		// 1. 从配置文件中加载皮肤参数，不成功退出
	//		boolean success = skinLoader.load("conf/ray/skin/config.xml");
	//		if (!success) {
	//			return false;
	//		}
	//		// 2. 读取"环境变量"配置文件，把"skin.name"参数读取出来
	//		String name = readSkinName();
	//		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
	//		if (name == null) {
	//			name = "normal"; // normal关键字在config.xml配置文件里定义
	//		}
	//
	//		//		name = "dark";
	//		//		name = "cyano";
	//		//		name = "bronze";
	//
	//		// 3. 找到匹配的皮肤方案
	//		success = false;
	//		SkinToken token = skinLoader.findSkinTokenByName(name);
	//		// 存在，加载皮肤界面
	//		if (token != null) {
	//			// 3. 切换到主题界面，同时更新组件UI
	//			success = token.updateTheme(true);
	//			if (success) {
	////				testJSliderIcon();
	//				// 选择中
	//				skinLoader.exchangeCheckedSkinToken(token.getName());
	//				// 定义外观
	//				Skins.setLookAndFeel(token.getLookAndFeel());
	//				Skins.setSkinName(token.getName());
	//			}
	//		} else {
	//			// 设置为默认的“Nimbus”外观，“Metal”为暗黑！
	//			success = UITools.updateLookAndFeel("Nimbus", null, null);
	//			if (success) {
	//				int count = SkinToken.loadSkins("conf/ray/skin/nimbus_normal.txt");
	//				success = (count > 0);
	//			}
	//			if (success) {
	//				skinLoader.exchangeCheckedSkinToken("Nimbus");
	//				Skins.setLookAndFeel(Skins.Nimbus);
	//				Skins.setSkinName("normal"); // "normal"是脚本中的定义
	//			}
	//		}
	//
	//		//		UIDefaults defs = UIManager.getDefaults();
	//		//		defs.put("TextPane.background", new ColorUIResource(Color.BLACK));
	//		//		defs.put("TextPane.inactiveBackground", new ColorUIResource(Color.BLACK));
	//
	//		return success;
	//	}

	/**
	 * 加载皮肤配置，同是更新外观界面
	 * @return 成功返回真，否则假
	 */
	private boolean loadSkins() {
		// 1. 从配置文件中加载皮肤参数，不成功退出
		boolean success = skinLoader.load("conf/ray/skin/config.xml");
		if (!success) {
			return false;
		}
		// 2. 读取"环境变量"配置文件，把"skin.name"参数读取出来
		String name = readSkinName();
		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
		if (name == null) {
			name = "gray"; // gray关键字在config.xml配置文件里定义
		}

		//		name = "dark";
		//		name = "cyano";
		//		name = "bronze";

		// 3. 找到匹配的皮肤方案
		success = false;
		SkinToken token = skinLoader.findSkinTokenByName(name);
		// 存在，加载皮肤界面
		if (token != null) {
			// 3. 切换到主题界面，同时更新组件UI
			success = token.updateTheme(true);
			if (success) {
				//				testJSliderIcon();
				// 选择中
				skinLoader.exchangeCheckedSkinToken(token.getName());
				// 定义外观
				Skins.setLookAndFeel(token.getLookAndFeel());
				Skins.setSkinName(token.getName());
			}
		} else {
			// 设置为默认的“Metal”外观，白色
			String clazz = "com.laxcus.util.skin.GrayMetalTheme";
			SkinSheet sheet = new FlatSkinSheet();
			success = UITools.updateLookAndFeel("Metal", clazz, sheet);
			if (success) {
				int count = SkinToken.loadSkins("conf/ray/skin/metal_gray.txt");
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
	 * 解析许可证签名
	 * @param element
	 */
	private void splitLicenceSignature(org.w3c.dom.Element element) {
		// 如果已经定义，local.xml脚本中的忽略
		String str = getSignature();
		if (str != null) {
			return;
		}
		// 2. 不成立，从配置脚本"local.xml"中取得许可证签名
		str = XMLocal.getValue(element, LoginMark.SIGNATURE);
		if (str != null && str.trim().length() > 0) {
			setSignature(str.trim());
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
	 * 解析自动注册登录参数
	 * @param filename
	 */
	private void splitLogin(String filename) {
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return ;
		}

		// 登录参数
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
			String yes = element.getAttribute(LoginMark.AUTO);
			boolean auto = yes.matches("^\\s*(?i)(?:YES|TRUE)\\s*$");

			// 在“YES”情况下，解析许可证
			if (auto) {
				splitLicenceSignature(element); // 解析许可证签名
			}
			Node hub = splitHubSite(element);
			User user = splitUser(element);

			// 判断有效且是ENTRANCE节点
			boolean success = (hub != null && user != null);
			if (success) {
				success = hub.isEntrance();
			}
			if (success) {
				LoginToken token = new LoginToken(auto, hub, user);
				window.setLoginToken(token);
			}
		}
	}

	/**
	 * 读取环境中的签名
	 */
	private void splitSignature() {
		String str = System.getProperty("laxcus.signature");
		if (str != null) {
			str = str.trim();
		}
		// 设置签名
		if (str != null && str.length() > 0) {
			super.setSignature(str);
		}
	}

//	/**
//	 * 解析私有参数
//	 * @param document
//	 */
//	private void splitPrivate(org.w3c.dom.Document document){
//		// CONSOLE/TERMINAL的命令模式
//		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
//		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
//		setMemory(input);
//		// CONSOLE/TERMINAL的命令超时
//		input = XMLocal.getValue(element, SiteMark.COMMAND_TIMEOUT);
//		setCommandTimeout(input);
//
//		// 内网节点检测NAT设备地址的间隔时间
//		input = XMLocal.getValue(element, SiteMark.POCK_INTERVAL);
//		setPockTimeout(input);
//
//		// CALL节点检查间隔时间
//		input = XMLocal.getValue(element, FrontMark.CALLSITE_CHECK_INTERVAL);
//		long interval = ConfigParser.splitTime(input, getStaffPool().getCheckInterval());
//		getStaffPool().setCheckInterval(interval);
//	}

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
		RayTube.setTimeout(ConfigParser.splitTime(input, RayTube.defaultTimeout));
		// 最小时间
		input = XMLocal.getAttribute(element, SiteMark.OUTLOOK_INTERVAL, "min");
		RayTube.setMinTimeout(ConfigParser.splitTime(input, RayTube.getMinTimeout()));


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
	
//	/**
//	 * 设置PUT/END任务组件共同的根目录
//	 * @param document XML文档
//	 * @param pool PUT/END组件管理池
//	 * @param subpath 子目录
//	 * @return 成功返回真，否则假
//	 */
//	private boolean setTaskPool(org.w3c.dom.Document document, DiskPool pool, String subpath) {
//		org.w3c.dom.NodeList list = document.getElementsByTagName(OtherMark.TASK_DIRECTORY);
//		if (list.getLength() != 1) {
//			Logger.error(this, "setTaskPool", "not found %s", OtherMark.TASK_DIRECTORY);
//			return false;
//		}
//		// 超时时间
//		org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
//		String input = element.getAttribute(OtherMark.ATTRIBUTE_TASK_SCANTIMEOUT);
//		long timeout = ConfigParser.splitTime(input, 120000); // 默认2分钟检查一次
//		pool.setSleepTimeMillis(timeout);
//		// 目录
//		String path = element.getTextContent();
//		if (path == null || path.trim().isEmpty()) {
//			Logger.error(this, "setTaskPool", "%s is null!", OtherMark.TASK_DIRECTORY);
//			return false;
//		}
//
//		// 如果在这个目录下指定子目录时
//		return pool.setRoot(path, subpath);
//	}

	/**
	 * 选择目录
	 * @return
	 */
	private String choiceDirectory() {
		String dir = System.getProperty("java.io.tmpdir");
		if (dir == null) {
			dir = System.getProperty("user.dir");
		}
		return dir;
	}

	/**
	 * 加载应用目录
	 * @return 
	 */
	private boolean loadApplicationDirectory() {
		// 系统应用运行目录
		String root = System.getProperty("laxcus.run.system");
		if (root != null) {
			boolean success = ApplicationPool.getInstance().setSystemRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "run" + File.separator + "system";
			boolean success = ApplicationPool.getInstance().setSystemRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 用户应用运行目录
		root = System.getProperty("laxcus.run.user");
		if (root != null) {
			boolean success = ApplicationPool.getInstance().setUserRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "run" + File.separator + "user";
			boolean success = ApplicationPool.getInstance().setUserRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 系统应用存储目录
		root = System.getProperty("laxcus.store.system");
		if (root != null) {
			boolean success = PlatformKit.setSystemStoreRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "system";
			boolean success = PlatformKit.setSystemStoreRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 用户应用存储目录
		root = System.getProperty("laxcus.store.user");
		if (root != null) {
			boolean success = PlatformKit.setUserStoreRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "user";
			boolean success = PlatformKit.setUserStoreRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 系统临时存储目录
		root = System.getProperty("laxcus.store.temp");
		if (root != null) {
			boolean success = PlatformKit.setSystemTemporaryRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "temp";
			boolean success = PlatformKit.setSystemTemporaryRoot(root, path);
			if (!success) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 从输入文件中获取配置参数
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	protected boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}

		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}

		// 私有参数
		splitPrivate(document);

//		// 解析边缘容器监听
//		splitTubListen(document);

		// 应用目录
		boolean success = loadApplicationDirectory();
		// 解析站点配置
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 生成RSA密钥令牌
		if (success) {
			success = createDefaultSecureToken(document);
		}
		// 加载自定义配置
		if (success) {
			success = loadCustom(document);
		}
//		// 设置PUT/END/NEAR/GUIDE发布目录，目录在local.xml文件中设置
//		if (success) {
//			success = setTaskPool(document, PutTaskPool.getInstance(), "put");
//		}
//		if (success) {
//			success = setTaskPool(document, EndTaskPool.getInstance(), "end");
//		}
//		if (success) {
//			success = setTaskPool(document, NearTaskPool.getInstance(), "near");
//		}
//		if (success) {
//			success = setTaskPool(document, GuideTaskPool.getInstance(), "guide");
//		}
		// 加载多文本提示
		if (success) {
//			success = loadTips("conf/ray/tip/config.xml");
			success = loadTips();
		}
//		// 设置边缘容器管理池的目录和更新检查参数
//		if (success) {
//			splitTubPool(document);
//		}

		// 加载日志并且启动
		if (success) {
			success = loadLogResourceWithLocal(filename);
		}

		// 只能是CONSOLE节点，才加载许可证，TERMINAL/DESKTOP在登录前进行
		if (success && isConsole()) {
			loadLicence(false);
		}

		return success;
	}

	/**
	 * 启动闪屏
	 */
	public void startSplash() {
		ResourceLoader loader = new ResourceLoader("conf/ray/image/splash/");
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
	public WatchSite getSite() {
		return local;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 如果有两个以上时. 判断有没有reset参数，有就清除旧记录
		if (args.length >= 2) {
			// 从第一个开始
			for (int i = 1; i < args.length; i++) {
				String param = args[i];
				if (param.matches("^\\s*(?i)(?:-RESET|RESET)\\s*$")) {
					ResourceReleaser rs = new ResourceReleaser();
					rs.deleteResource();
				}
			}
		}

		// 删除过期的垃圾文件
		RubbishReleaser rs = new RubbishReleaser();
		rs.deleteRubbishs();

		// 取句柄
		RayLauncher launcher = RayLauncher.getInstance();

		// 启动闪屏
		launcher.startSplash();

		// 加载“bin”目录下面和“laxcus.library”目录下面的库文件
		JNILoader.init();

		// 读取本地的环境变量
		if (!launcher.readEnvironement()) {
			launcher.stopSplash();
			Logger.error("environement parameter error!");
			Logger.gushing();
			System.exit(0);
			return;
		}

		// 加载外观和皮肤
		if (!launcher.loadSkins()) {
			launcher.stopSplash();
			Logger.error("cannot be load skin theme!");
			Logger.gushing();
			System.exit(0);
			return;
		}

		// 初始化界面UI字体
		launcher.checkPlatformFont();

		// 解析登录参数，只在Ray有用
		String filename = args[0];
		// 解析环境中的签名，再解析"local.xml"中的登录参数
		launcher.splitSignature();
		launcher.splitLogin(filename);
		// 解析
		boolean success = launcher.loadLocal(filename);
		Logger.note("RayLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = launcher.start();
			Logger.note("RayLauncher.main, start service", success);
		}

		// 启动限制参数
		if (success) {
			launcher.loadTimerTasks(filename);
		} else {
			launcher.stopSplash();
			Logger.gushing();
			// 关闭日志
			launcher.stopLog();
			System.exit(0);
		}
	}

}