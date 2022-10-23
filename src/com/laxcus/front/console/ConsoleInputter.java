/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.command.*;
import com.laxcus.command.cloud.*;
import com.laxcus.front.*;
import com.laxcus.front.meet.*;
import com.laxcus.front.meet.invoker.*;
import com.laxcus.front.tub.*;
import com.laxcus.site.front.*;
import com.laxcus.thread.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.help.*;
import com.laxcus.util.net.*;

/**
 * FRONT站点控制台输入窗口 <br><br>
 * 
 * FRONT控制台登录语法：OPEN ENTRANCE服务器主机地址 端口 <br>
 * 
 * @author scott.liang
 * @version 1.05 10/6/2011
 * @since laxcus 1.0
 */
final class ConsoleInputter extends VirtualThread implements MeetDisplay, MeetCommandAuditor {

	/** 登录正则表达式，格式: OPEN ipv4|ipv6|dns port **/
	private final static String LOGIN_REGEX = "^\\s*(?i)(?:OPEN)\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}||\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}\\:\\p{XDigit}{1,4}|[a-zA-Z0-9]{1}[\\w\\.\\-]{1,})(?:\\s+|\\:)(\\d{1,5})\\s*$";	

	/** 帮助参数 **/
	private final String HELPREGEX = "^\\s*(?i)(?:HELP)\\s+(.+)\\s*$";

	/** 忽略通符符 **/
	private final String HELP_SUFFIX = "^\\s*(?:[\\\\*]*)([^\\\\*]+)\\s*([\\\\*]+)\\s*$";

	/** JAVA控制台 */
	private Console console;

	/** 命令分派器 **/
	private MeetCommandDispatcher dispatcher = new MeetCommandDispatcher();

	/** 异步读取拦截器 **/
	private ReadAttacher attacher = new ReadAttacher();

	/**
	 * 构造字符前端界面
	 */
	public ConsoleInputter() {
		super();
		// 给异步命令调用器分配显示接口
		MeetInvoker.setDefaultDisplay(this);
		TubServlet.setDisplay(new TubDisplayImpl(this));

		dispatcher.setCommandAuditor(this);
		dispatcher.setDisplay(this);
	}

	/**
	 * 返回控制台句柄
	 * @return
	 */
	public Console getConsole() {
		return console;
	}

	/**
	 * 初始化控制台
	 * @return 成功返回真，否则假
	 */
	public boolean initialize() {
		if (console == null) {
			console = System.console();
		}

		boolean success = (console != null);
		if (success) {
			// printXML("console/welcome");
			showWelcome();
		}
		return success;
	}

	/**
	 * 从控制台上接受命令
	 * @return 返回命令，或者空字符串
	 */
	private String input() {
		// 从控制台读取
		String cmd = console.readLine();
		// 如果是空指针，返回一个空字符串，在外部判断退出。
		if (cmd == null) {
			return "";
		}
		return cmd.trim();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#confirm()
	 */
	@Override
	public boolean confirm() {
		String content = findContent("console/doit");
		while (true) {
			String cmd = console.readLine("%s", content);
			if (cmd == null) {
				continue;
			}
			cmd = cmd.trim();
			if(cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$")) {
				return true;
			} else if(cmd.matches("^\\s*(?i)(?:NO|N)\\s*$")) {
				break;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String content) {
		while (true) {
			String cmd = console.readLine("%s", content);
			if (cmd == null) {
				continue;
			}
			cmd = cmd.trim();
			if (cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$")) {
				return true;
			} else if (cmd.matches("^\\s*(?i)(?:NO|N)\\s*$")) {
				break;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#confirm(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean confirm(String title, String content) {
		while (true) {
			String cmd = console.readLine("%s", content);
			if (cmd == null) {
				continue;
			}
			cmd = cmd.trim();
			if (cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$")) {
				return true;
			} else if (cmd.matches("^\\s*(?i)(?:NO|N)\\s*$")) {
				break;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetCommandAuditor#shift(java.lang.String)
	 */
	@Override
	public void shift(Command cmd) {
		if (cmd.getClass() == RunTask.class) {
			RunTask sub = (RunTask) cmd;
			// 交给专用对象处理
			RunTaskConsoleProcesser processer = new RunTaskConsoleProcesser(console, this, sub);
			processer.process();
		}
	}

//	/**
//	 * 显示许可信息
//	 * @param content 文本内容
//	 * @return 接受返回真，否则假
//	 */
//	public boolean showLicence(String content) {
//		String tip = findContent("console/licence");
//
//		// 过滤空格！
//		content = Laxkit.trim(content);
//		System.out.println(content);
//		// 显示和确定！
//		while (true) {
//			System.out.print(tip);
//			// 进入截获读状态
//			String cmd = attacher.read();
//			if (cmd == null) {
//				continue;
//			}
//			cmd = cmd.trim();
//			if (cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$")) {
//				return true;
//			} else if (cmd.matches("^\\s*(?i)(?:NO|N)\\s*$")) {
//				break;
//			}
//		}
//		return false;
//	}

	/**
	 * 判断是帮助命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isHelp(String input) {
		return input.matches("^\\s*(?i)(HELP)\\s*$");
	}

	/**
	 * 判断是版本命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isVersion(String input) {
		return input.matches("^\\s*(?i)(VERSION)\\s*$");
	}

	/**
	 * 判断包含帮助命令子句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isHelpCommand(String input) {
		return input.matches(HELPREGEX);
	}

	/**
	 * 判断是退出命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isExit(String input) {
		return input.matches("^\\s*(?i)(EXIT|QUIT)\\s*$");
	}

	/**
	 * 核准退出
	 * @return 退出返回真，否则假
	 */
	private boolean affirm() {
		String suffix = findContent("console/affirm");
		String cmd = console.readLine("%s", suffix);
		// 如果是空指针，返回假
		if(cmd == null) {
			return false;
		}
		return cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$");
	}
	
	/**
	 * 查找信息
	 * @param xmlPath XML路径
	 * @return 返回对应的字符串
	 */
	private String findContent(String xmlPath) {
		return ConsoleLauncher.getInstance().findContent(xmlPath);
	}

	/**
	 * 打印文本
	 * @param text
	 */
	private void println(String text) {
		System.out.println(text);
	}

	/**
	 * 显示信息
	 * @param xmlPath
	 */
	protected void printXML(String xmlPath) {
		String text = findContent(xmlPath);
		println(text);
	}
	
	/**
	 * 接受输入命令并且执行操作。<br>
	 * 如果退出返回真，执行命令返回假。<br>
	 * 
	 * @return 真或者假
	 */
	private boolean todo() {
		String cmd = input();

		// 判断要求截获时...，把结果赋给它！
		if (attacher.isAttached()) {
			attacher.done(cmd);
			return false;
		}

		// 无效命令，返回假
		if (cmd.isEmpty()) {
			printXML("console/illegal-command");
			return false;
		}

		// 判断是空符串或者退出命令
		if (isExit(cmd)) {
			// 确认是退出
			boolean exit = affirm();
			// 在控制台打印退出信息
			if (exit) {
				printXML("console/exit");
			} else {
				printXML("console/cancel");
			}
			return exit; // 退出命令
		} else if (isHelp(cmd)) {
			showHelp(); // 显示全部帮助
		} else if (isHelpCommand(cmd)) {
			showHelpComment(cmd); // 帮助参数
		} else if (isVersion(cmd)) {
			showVersion();
		} else {
			//			// 交命令翻译器处理
			//			int who = dispatcher.submit(cmd); 
			//			// 如果被取消，显示提示信息
			//			if (MeetSubmit.isCanceled(who)) {
			//				printXML("console/cancel");
			//			}
			
			// 语法检测
			// boolean success = dispatcher.check(cmd, false);

			// 只检查命令，不检查参数
			boolean success = dispatcher.match(cmd, false);
			// 不成立，判断是本地命令
			if (!success) {
				try {
					doLocal(cmd);
				} catch (Exception e) {
					printXML("console/illegal-command");
				}
				return false;
			}
			// 交命令翻译器处理
			int who = dispatcher.submit(cmd); 
			// 如果被取消，显示提示信息
			if (MeetSubmit.isCanceled(who)) {
				printXML("console/cancel");
			}
		}
		return false;
	}
	
	/**
	 * 当前操作系统的默认编码
	 * 
	 * @return 返回一个字符串，默认是GBK
	 */
	private String getOSEncoding() {
		String value = System.getProperty("sun.jnu.encoding");
		if (value == null) {
			if (Laxkit.isWindows()) {
				value = "GBK";
			} else if (Laxkit.isLinux()) {
				value = "UTF-8";
			} else {
				value = "UTF-8";
			}
		}
		return value;
	}

	/**
	 * 做为本地命令来执行
	 * @param cmd
	 * @throws InterruptedException 
	 */
	private void doLocal(String cmd) throws IOException, InterruptedException {
		ProcessBuilder builder = null;
		String charset = getOSEncoding(); // 返回操作系统编码
		if (Laxkit.isWindows()) {
			// 如果是清除屏幕，忽略
			if (cmd.equalsIgnoreCase("CLS")) {
				return;
			}
			builder = new ProcessBuilder("cmd", "/c", cmd);
		} else if (Laxkit.isLinux()) {
			// 清除屏幕，忽略！
			if (cmd.equals("clear")) {
				return;
			}
			builder = new ProcessBuilder("sh", "-c", cmd);
		} else {
			return;
		}

		// 重定向错误日志信息到InputStream，这个很重要！不然错误信息无法捕捉
		builder.redirectErrorStream(true);

		Process process = builder.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
		do {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			// 输出
			println(line);
		} while (true);
		
		// 等待直到结果
		int code = process.waitFor();
		if (code == 0) {
			// 通常情况，0表示命令或者脚本正常退出。但是如果脚本自己有返回状态，这里需要根据自己的状态判断
		}
	}

	/**
	 * 连接TOP节点
	 * @param remote 目标地址
	 * @return 成功返回真，否则假
	 */
	private boolean connect(SiteHost remote) {
		String s1 = findContent("console/username");
		String s2 = findContent("console/password");
		// 命令名
		String username = console.readLine("%s", s1);
		if (username == null) {
			return false;
		}
		// 密码
		char[] pwd = console.readPassword("%s", s2);
		if (pwd == null) {
			return false;
		}
		String password = new String(pwd);

		FrontSite site = ConsoleLauncher.getInstance().getSite();
		site.setUser(username, password);

		// 登录到TOP节点
		ConsoleTracker tracker = new ConsoleTracker();
		long time = System.currentTimeMillis();
		int who = ConsoleLauncher.getInstance().login(remote, false, tracker);
		boolean success = FrontEntryFlag.isSuccessful(who);
		if (success) {
			String prefix = findContent("console/login-successful");
			double usedTime = (double) (System.currentTimeMillis() - time) / 1000.0f;
			System.out.printf(prefix + "\n", usedTime);
		} else {
			if (who == FrontEntryFlag.ENTRANCE_FAULT) {
				printXML("console/entrance-failed");
			} else if(who == FrontEntryFlag.CANNOT_REDIRECT) {
				printXML("console/redirect-failed");
			} else if(who == FrontEntryFlag.GATE_FAULT) {
				printXML("console/gate-failed");
			} else if(who == FrontEntryFlag.NAT_FAULT) {
				printXML("console/nat-failed");
			} else if(who == FrontEntryFlag.MAX_USER) {
				printXML("console/max-user");
			} else if(who == FrontEntryFlag.LOGIN_TIMEOUT){
				printXML("console/login-timeout");
			} else if(who == FrontEntryFlag.SERVICE_MISSING) {
				printXML("console/service-missing");
			} else if (who == FrontEntryFlag.REFLECT_FAULT) {
				if (tracker.getPitchId() != FrontPitch.SUCCESSFUL) {
					SiteHost pitchHub = tracker.getPitchHub();
					String host = String.format("%s:%d", pitchHub.getAddress(), pitchHub.getUDPort());
					String content = findContent("console/pitch-not-found");
					content = String.format(content, host);
					println(content);
				} else {
					printXML("console/reflect-failed");
				}
			} else if(who == FrontEntryFlag.MAX_RETRY) {
				printXML("console/max-retry");
			} else if(who == FrontEntryFlag.LICENCE_NAT_REFUSE) {
				printXML("console/licence-nat-refuse");
			} else if(who == FrontEntryFlag.VERSION_NOTMATCH) {
				printXML("console/version-notmatch");
			} else {
				printXML("console/connect-failed");
			}
		}

		return success;
	}

	/**
	 * 输入GATE站点参数，登录到服务器。
	 * @return 登录成功返回“真”，取消登录返回“假”
	 */
	protected boolean login() {
		boolean success = false;
		while (true) {
			String cmd = input();

			// 判断是空字符，确认是否退出
			if (cmd.isEmpty()) {
				boolean exit = affirm();
				if (exit) {
					printXML("console/exit");
					break;
				} else {
					continue;
				}
			}

			if (isHelp(cmd)) {
				showHelp();
			} else if (isHelpCommand(cmd)) {
				showHelpComment(cmd);
			} else if (isVersion(cmd)) {
				showVersion();
			} else if (isExit(cmd)) {
				printXML("console/exit");
				break;
			} else {
				Pattern pattern = Pattern.compile(ConsoleInputter.LOGIN_REGEX);
				Matcher matcher = pattern.matcher(cmd);
				if (!matcher.matches()) {
					printXML("console/syntax-error");
					continue;
				}

				// 判断地址在规定范围内
				int port = Integer.parseInt(matcher.group(2));
				if (!(port > 0 && port <= 0xFFFF)) {
					printXML("console/illegal-port");
					continue;
				}
				// IP地址或者域名
				Address address = null;
				try {
					address = new Address(matcher.group(1));
				} catch (IOException e) {
					printXML("console/address-error");
					continue;
				}

				// 在控制台窗口显示提示信息
				String text = findContent("console/loginto");
				System.out.printf(text + "\n", matcher.group(1), matcher.group(2));

				// ENTRANCE站点的TCP/UDP端口一致。
				SiteHost server = new SiteHost(address, port, port); 
				// 连接ENTRANCE服务器，再定向到GATE站点，进一步找到和定位CALL站点。
				success = connect(server);

				if(success) break;
			}
		}
		return success;
	}
	
	/**
	 * 分割多行文本
	 * @param text 文本
	 * @return 返回分割后的文本
	 */
	private String splitMultiText(String text) {
		return text.replaceAll("<br>", "\n");
	}
	
	/**
	 * 显示欢迎信息
	 */
	private void showWelcome() {
		String text = findContent("console/welcome");
		text = splitMultiText(text);
		System.out.println(text);
	}

	/**
	 * 显示控制台版本
	 */
	private void showVersion() {
		String text = findContent("console/version");
		text = splitMultiText(text);
		System.out.println(text);
	}

	/**
	 * 显示帮助命令
	 */
	private void showHelp() {
		// 命令注释
		CommentContext context = ConsoleLauncher.getInstance().getCommentContext();

		// 保存参数
		StringBuilder buf = new StringBuilder();
		for (CommentGroup group : context.list()) {
			if (buf.length() > 0) {
				buf.append("\r\n");
			}
			// 命令类型
			buf.append(group.getTitle());
			buf.append("\r\n");
			// 单个命令
			for (CommentElement element : group.list()) {
				buf.append(element.getCommand());
				buf.append("\r\n");
			}
		}
		// 打印参数
		System.out.println(buf.toString());
	}

	/**
	 * 显示某些指定的命令
	 */
	private void showHelpComment(String input) {
		Pattern pattern = Pattern.compile(HELPREGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return;
		}
		// 16进制字符串
		String suffix = matcher.group(1);

		// 判断和过滤掉最后的“*”符号
		boolean really = true;
		pattern = Pattern.compile(HELP_SUFFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			really = false;
			suffix = matcher.group(1);
		}

		// 上下文
		CommentContext context = ConsoleLauncher.getInstance().getCommentContext();

		// 单个命令
		if (really) {
			CommentElement element = context.findComment(suffix);
			if (element != null) {
				String str = context.formatConsole(element);
				System.out.print(str);
				return;
			}
		}

		// 部分匹配的命令
		List<CommentElement> elements = context.findAllComments(suffix);
		if (elements.size() > 0) {
			StringBuffer bf = new StringBuffer();
			for (CommentElement e : elements) {
				bf.append(e.getCommand());
				bf.append("\r\n");
			}
			System.out.print(bf.toString());
			return;
		}

		// 提示没有找到的命令
		printXML("console/not-found-command");
	}

	//	/**
	//	 * 显示某些指定的命令
	//	 */
	//	private void showHelpComment(String input) {
	//		Pattern pattern = Pattern.compile(HELPREGEX);
	//		Matcher matcher = pattern.matcher(input);
	//		if (!matcher.matches()) {
	//			return;
	//		}
	//		// 16进制字符串
	//		String suffix = matcher.group(1);
	//
	//		// 上下文
	//		CommentContext context = ConsoleLauncher.getInstance().getCommentContext();
	//
	//		// 单个命令
	//		CommentElement element = context.findComment(suffix);
	//		if (element != null) {
	//			String str = context.formatConsole(element);
	//			System.out.print(str);
	//			return;
	//		}
	//		
	//		// 部分匹配的命令
	//		List<CommentElement> elements = context.findAllComments(suffix);
	//		if (elements.size() > 0) {
	//			StringBuffer bf = new StringBuffer();
	//			for (CommentElement e : elements) {
	//				bf.append(e.getCommand());
	//				bf.append("\r\n");
	//			}
	//			System.out.print(bf.toString());
	//			return;
	//		}
	//		
	//		// 提示没有找到的命令
	//		suffix = findContent("console/not-found-command");
	//		System.out.println(suffix);
	//	}

	//	/**
	//	 * 判断是清屏幕
	//	 * @param input 输入语句
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isClear(String input) {
	//		return input.matches("^\\s*(?i)(CLEAR)\\s*$");
	//	}

	//	/**
	//	 * 清除屏幕字符
	//	 */
	//	private void clear() {
	//		System.out.println("CLEAR SCREEN!");
	//		try {
	//			if (ConsoleLauncher.getInstance().isLinux()) {
	//				Runtime.getRuntime().exec("clear");
	//			} else if (ConsoleLauncher.getInstance().isWindows()) {
	//				// ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
	//				// pb.start();
	//				// pb.waitFor();
	//			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!isInterrupted()) {
			boolean exit = todo();
			// 通知启动器退出
			if (exit) {
				setInterrupted(true);
				ConsoleLauncher.getInstance().stop();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub		
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.display.Display#isDriver()
//	 */
//	@Override
//	public boolean isDriver() {
//		return false;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.display.Display#isConsole()
//	 */
//	@Override
//	public boolean isConsole() {
//		return true;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.display.Display#isWindow()
//	 */
//	@Override
//	public boolean isWindow() {
//		return false;
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.Display#erase()
	 */
	@Override
	public void clearPrompt() {
		// TODO Auto-generated method stub

	}

	/** 标题默认是0 **/
	private int tableTitleSize = 0;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getTitleCellCount()
	 */
	@Override
	public int getTitleCellCount(){
		return tableTitleSize;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle title) {
		StringBuilder bf = new StringBuilder();
		List<ShowTitleCell> cells = title.list();
		tableTitleSize = cells.size();
		for (int i = 0; i < tableTitleSize; i++) {
			if (bf.length() > 0) bf.append("  ");
			bf.append(cells.get(i).toString());
		}
		System.out.println(bf.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem item) {
		StringBuilder buf = new StringBuilder();
		java.util.List<ShowItemCell> cells = item.list();
		int size = cells.size();
		for (int i = 0; i < size; i++) {
			if (buf.length() > 0) buf.append("  ");
			Object obj = cells.get(i).visible();
			if (obj.getClass() == String.class) {
				buf.append((String) obj);
			} else {
				buf.append(" -- null --");
			}
		}
		System.out.println(buf.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#showTable(com.laxcus.util.display.show.ShowTitle, java.util.Collection)
	 */
	@Override
	public synchronized void showTable(ShowTitle title, Collection<ShowItem> items) {
		setShowTitle(title);
		if (items != null) {
			for (ShowItem e : items) {
				addShowItem(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setStatusText(java.lang.String)
	 */
	@Override
	public void setStatusText(String text){
		System.out.println(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearShowItems()
	 */
	@Override
	public void clearShowItems() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#isUsabled()
	 */
	@Override
	public boolean isUsabled() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#approveLicence(java.lang.String)
	 */
	@Override
	public boolean approveLicence(String content) {
		String tip = findContent("console/licence");

		// 过滤空格！
		content = Laxkit.trim(content);
		System.out.println(content);

		// 显示和确定！
		while (true) {
			System.out.print(tip);
			// 进入截获读状态
			String cmd = attacher.read();
			if (cmd == null) {
				continue;
			}
			cmd = cmd.trim();
			if (cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$")) {
				return true;
			} else if (cmd.matches("^\\s*(?i)(?:NO|N)\\s*$")) {
				break;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String, boolean)
	 */
	@Override
	public void message(String text, boolean foucs) {
		System.out.println(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.Display#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
		System.out.println(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.Display#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
		System.out.println(text);
	}


	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String, boolean)
	 */
	@Override
	public void fault(String text, boolean foucs) {
		System.out.println(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.Display#warn(java.lang.String)
	 */
	@Override
	public void warning(String text) {
		System.out.println(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String, boolean)
	 */
	@Override
	public void warning(String text, boolean focus) {
		System.out.println(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.Display#flash(com.laxcus.task.display.GraphItem)
	 */
	@Override
	public void flash(GraphItem item) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.Display#clearGraph()
	 */
	@Override
	public void clearGraph() {

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getProductListener()
	 */
	@Override
	public ProductListener getProductListener() {
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.ui.display.MeetDisplay#ratify(java.lang.String)
//	 */
//	@Override
//	public boolean ratify(String content) {
//		return confirm(content);
//	}
}