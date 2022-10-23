/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 命令语法解析器。<br><br>
 * 
 * 解析的语句分为SQL和LAXCUS两大类。<br>
 * LAXCUS中的所有语句型的命令解析从这里派生。<br>
 * 
 * @author scott.liang
 * @version 1.35 7/10/2017
 * @since laxcus 1.0
 */
public class SyntaxParser { 
	
	/** 远程访问探测器 **/
	private static VisitRobot robot;

	/** 资源检索接口 **/
	private static ResourceChooser chooser;

	/** 提示打印接口 **/
	private static TipPrinter printer;
	
	/**
	 * 设置远程访问探测器
	 * @param e 实例
	 */
	public static void setVisitRobot(VisitRobot e) {
		SyntaxParser.robot = e;
	}
	
	/**
	 * 返回远程访问探测器
	 * @return 远程访问探测器实例
	 */
	public static VisitRobot getVisitRobot() {
		return SyntaxParser.robot;
	}

	/**
	 * 设置资源检索接口 <br>
	 * 资源检索接口在本地或者通过网络检索数据。
	 * @param e ResourceChooser实例
	 */
	public static void setResourceChooser(ResourceChooser e) {
		SyntaxParser.chooser = e;
	}

	/**
	 * 返回资源检索接口
	 * @return ResourceChooser实例
	 */
	public static ResourceChooser getResourceChooser() {
		return SyntaxParser.chooser;
	}

	/**
	 * 设置提示打印接口
	 * @param e TipPrinter实例
	 */
	public static void setTipPrinter(TipPrinter e) {
		SyntaxParser.printer = e;
	}

	/**
	 * 返回提示打印接口
	 * @return 返回TipPrinter实例
	 */
	public static TipPrinter getTipPrinter() {
		return SyntaxParser.printer;
	}

	/**
	 * 判断是绑定到用户应用、无操作界面的驱动程序站点
	 * @return 返回真或者假
	 */
	public boolean isDriver() {
		return getTipPrinter().isDriver();
	}

	/**
	 * 判断是字符界面的控制台站点
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		return getTipPrinter().isConsole();
	}

	/**
	 * 判断是图形界面的终端站点
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		return getTipPrinter().isTerminal();
	}

	/**
	 * 判断是用于边缘计算的服务端节点（在后台运行）
	 * @return 返回真或者假
	 */
	public boolean isEdge() {
		return getTipPrinter().isEdge();
	}

	/**
	 * 判断是WATCH节点
	 * @return 返回真或者假
	 */
	public boolean isWatch() {
		return getTipPrinter().isWatch();
	}

	/**
	 * 语法解析器
	 */
	protected SyntaxParser() {
		super();
	}

	/**
	 * 取文件的规范路径，否则是绝对路径
	 * @param file File实例
	 * @return 字符串文件
	 */
	protected String canonical(File file) {
		try {
			return file.getCanonicalPath();
		} catch(IOException e) {
			return file.getAbsolutePath();
		}
	}
	
	/**
	 * 根据提示编号，在窗口输出普通消息确认
	 * @param no 提示编号
	 * @return 接受返回真，否则假
	 */
	public boolean confirm(int no) {
		return getTipPrinter().confirm(no);
	}

	/**
	 * 根据提示编号，格式化普通消息并且输出窗口确认
	 * @param no 提示编号
	 * @param params 参与格式化的参数
	 * @return 接受返回真，否则假
	 */
	public boolean confirm(int no, Object... params) {
		return getTipPrinter().confirm(no, params);
	}

	/**
	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	 * @param no 提示编号
	 */
	public String message(int no) {
		return getTipPrinter().message(no);
	}

	/**
	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	public String message(int no, Object... params) {
		return getTipPrinter().message(no, params);
	}

	/**
	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	 * @param no 提示编号
	 */
	public String fault(int no) {
		return getTipPrinter().fault(no);
	}

	/**
	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	public String fault(int no, Object... params) {
		return getTipPrinter().fault(no, params);
	}

	/**
	 * 根据后缀填充信息，故障编号和当前语言配置，显示经过格式化处理的故障提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	public String fault(String paddingSuffix, int no, Object... params) {
		String prefix = getTipPrinter().fault(no, params);
		return prefix + " " + paddingSuffix;
	}

	/**
	 * 根据故障编号，弹出SyntaxException异常
	 * @param no 错误编号
	 * @throws SyntaxException
	 */
	protected void throwable(int no) throws SyntaxException {
		String content = fault(no);
		throw new SyntaxException(content);
	}

	/**
	 * 弹出错误
	 * @param input
	 * @throws SyntaxException
	 */
	protected void throwable(String input) throws SyntaxException {
		throw new SyntaxException(input);
	}

	/**
	 * 弹出错误
	 * @param input
	 * @param index
	 * @throws SyntaxException
	 */
	protected void throwable(String input, int index) throws SyntaxException {
		StringBuilder b = new StringBuilder();
		if (index > 0) {
			char[] c = new char[index - 1];
			for (int i = 0; i < c.length; i++) {
				c[i] = 0x20;
			}
			b.append(c);
		}
		b.append(" ^ ");
		
		// 错误语法
		String suffix = fault(FaultTip.INCORRECT_SYNTAX);
		if (suffix == null) {
			suffix = "Syntax error";
		}
		
		String s = String.format("%s\n%s\n %s", input, b.toString(), suffix);
		throw new SyntaxException(s);
	}

//	/**
//	 * 弹出错误
//	 * @param format
//	 * @param args
//	 * @throws SyntaxException
//	 */
//	protected void throwable(String format, Object... args) throws SyntaxException {
//		throwable(String.format(format, args));	
//	}
//
//	/**
//	 * 根据故障编号和当前语言的定义，弹出SyntaxException异常
//	 * @param paddingSuffix 填充语句
//	 * @param no 错误编号
//	 * @param params 被格式字符串引用的参数
//	 * @throws SyntaxException
//	 */
//	protected void throwable(String paddingSuffix, int no, Object... params) throws SyntaxException {
//		String content = fault(paddingSuffix, no, params);
//		throw new SyntaxException(content);
//	}

//	/**
//	 * 根据故障编号和当前语言的定义，弹出SyntaxException异常
//	 * @param no 错误编号
//	 * @param params 被格式字符串引用的参数
//	 * @throws SyntaxException
//	 */
//	protected void throwable(int no, Object... params) throws SyntaxException {
//		String content = fault(no, params);
//		throw new SyntaxException(content);
//	}

	/**
	 * 弹出错误
	 * @param format
	 * @param args
	 * @throws SyntaxException
	 */
	protected void throwableFormat(String format, Object... args) throws SyntaxException {
		throwable(String.format(format, args));	
	}

	/**
	 * 根据故障编号和当前语言的定义，弹出SyntaxException异常
	 * @param no 错误编号
	 * @param params 被格式字符串引用的参数
	 * @throws SyntaxException
	 */
	protected void throwableNo(int no, Object... params) throws SyntaxException {
		String content = fault(no, params);
		throw new SyntaxException(content);
	}
	
	/**
	 * 根据故障编号和当前语言的定义，弹出SyntaxException异常
	 * @param paddingSuffix 填充语句
	 * @param no 错误编号
	 * @param params 被格式字符串引用的参数
	 * @throws SyntaxException
	 */
	protected void throwablePrefixFormat(String paddingSuffix, int no, Object... params) throws SyntaxException {
		String content = fault(paddingSuffix, no, params);
		throw new SyntaxException(content);
	}
	
	/**
	 * 根据列编号，生成对应的LIKE关键字列编号(0x8xxx)
	 * @param columnId 列编号
	 * @return LIKE编号
	 */
	protected final short buildLikeId(short columnId) {
		return (short) (columnId | 0x8000);
	}

	/**
	 * 根据列编号，返回它的标准列编号(1-0x7fff)
	 * @param columnId 列编号
	 * @return 标准列编号
	 */
	protected final short buildNormalId(short columnId) {
		return (short) (columnId & 0x7FFF);
	}

	/**
	 * 以逗号为分割符，对字符串进行切割
	 * @param input 输入字符
	 * @return 切割后的字节串数组
	 */
	protected String[] splitCommaSymbol(String input) {
		return input.split("\\s*\\,\\s*");
	}

	/**
	 * 以空格为分割符，对字符串进行切割
	 * @param input 输入字符
	 * @return 切割后的字节串数组
	 */
	protected String[] splitSpaceSymbol(String input) {
		ArrayList<String> a = new ArrayList<String>();
		String[] subs = input.split("\\s+");
		for (int i = 0; i < subs.length; i++) {
			String s = subs[i].trim();
			if (s.length() > 0) a.add(s);
		}
		subs = new String[a.size()];
		return a.toArray(subs);
	}
	
	/**
	 * 判断是“ALL”关键字
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	protected boolean isAllKeyword(String input) {
		return input.matches("^\\s*(?i)(?:ALL)\\s*$");
	}

	/**
	 * 解析站点地址，可以指定站点类型。
	 * @param input 输入语句
	 * @param family 站点类型
	 * @return 返回站点地址列表
	 */
	protected Node splitSite(String input, byte family) {
		// 判断地址格式正确
		if (!Node.validate(input)) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, input);
		}
		// 转成类格式
		try {
			Node node = new Node(input);
			// 判断匹配
			if (family != SiteTag.NONE) {
				if (node.getFamily() != family) {
					throwableNo(FaultTip.ILLEGAL_SITE_X, input);
				}
			}
			return node;
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, input);
		}
		// 输出
		return null;
	}

	/**
	 * 解析站点地址，可以指定站点类型。如果有多个，以逗号分隔。
	 * @param input 输入语句
	 * @param family 站点类型
	 * @return 返回站点地址列表
	 */
	protected List<Node> splitSites(String input, byte family) {
		ArrayList<Node> array = new ArrayList<Node>();
		// 逗号分割它
		String[] items = splitCommaSymbol(input);
		// 逐一解析
		for (String item : items) {
			try {
				Node node = new Node(item);
				// 判断匹配
				if (family != SiteTag.NONE) {
					if (node.getFamily() != family) {
						throwableNo(FaultTip.ILLEGAL_SITE_X, item);
					}
				}
				// 保存地址
				array.add(node);
			} catch (UnknownHostException e) {
				throwableNo(FaultTip.ILLEGAL_SITE_X, item);
			}
		}
		// 输出
		return array;
	}
	
	/**
	 * 解析站点地址，可以指定站点类型。如果有多个，以逗号分隔。
	 * @param input 输入语句
	 * @param types 站点类型
	 * @return 返回站点地址列表
	 */
	protected List<Node> splitSites(String input, byte[] types) {
		ArrayList<Node> array = new ArrayList<Node>();
		// 逗号分割它
		String[] items = splitCommaSymbol(input);
		// 逐一解析
		for (String item : items) {
			try {
				Node node = new Node(item);
				// 判断匹配
				boolean match = false;
				for (byte family : types) {
					match = (node.getFamily() == family);
					if (match) {
						break;
					}
				}
				// 不匹配，弹出异常
				if(!match) {
					throwableNo(FaultTip.ILLEGAL_SITE_X, item);
				}
				// 保存地址
				array.add(node);
			} catch (UnknownHostException e) {
				throwableNo(FaultTip.ILLEGAL_SITE_X, item);
			}
		}
		// 输出
		return array;
	}

	/**
	 * 解析站点地址
	 * @param input 输入语句
	 * @return 返回站点地址列表
	 */
	protected List<Node> splitSites(String input) {
		return splitSites(input, SiteTag.NONE);
	}

	/**
	 * 解析一批数据表名
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回数据表名列表
	 */
	protected List<Space> splitSpaces(String input, boolean online) {
		ArrayList<Space> array = new ArrayList<Space>();

		// 分割逗号
		String[] items = splitCommaSymbol(input);
		// 逐一处理
		for (String item : items) {
			// 判断不匹配
			if (!Space.validate(item)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			// 转成数据表名保存
			Space space = new Space(item);
			// 如果是在线模式，查找表存在
			if (online) {
				if (!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, item);
				}
			}
			// 保存数据表名
			array.add(space);
		}
		// 返回结果
		return array;
	}
	
	/**
	 * 是管理员
	 * @return
	 */
	protected boolean isAdministrator() {
		return SyntaxParser.chooser.isAdministrator();
	}
	
	/**
	 * 拥有等同于管理员的用户
	 * @return
	 */
	protected boolean isSameAdministrator() {
		return SyntaxParser.chooser.isAdministrator();
	}
	
	/**
	 * 查找数据表
	 * @param space 表名 
	 * @return 返回数据表实例，或者空指针
	 */
	protected Table findTable(Space space) {
		return SyntaxParser.chooser.findTable(space);
	}
	
	/**
	 * 返回账号
	 * @param siger
	 * @return
	 */
	protected Account findAccount(Siger siger) {
		try {
			return SyntaxParser.chooser.findAccount(siger);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	/**
	 * 判断数据表存在。先进行本地检查，没有通过网络查询集群。
	 * @param space 数据表名
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasTable(Space space) {
		try {
			return SyntaxParser.chooser.hasTable(space);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	/**
	 * 判断数据库存在。先进行本地检查，没有通过网络查询集群。
	 * @param fame 数据库名称
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasSchema(Fame fame) {
		try {
			return SyntaxParser.chooser.hasSchema(fame);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	/**
	 * 在线判断用户名存在。只有管理员或者等同管理员身份的用户才可以查询。
	 * @param username 用户用户明文
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasUser(String username) {
		try {
			return SyntaxParser.chooser.hasUser(username);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	/**
	 * 在线判断用户名存在。只有管理员或者等同管理员身份的用户才可以查询。
	 * @param siger 用户签名
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasUser(Siger siger) {
		try {
			return SyntaxParser.chooser.hasUser(siger);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	/**
	 * 判断分布应用在本地存在
	 * @param phase 阶段命名
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasLocalTask(Phase phase) {
		try {
			return SyntaxParser.chooser.hasLocalTask(phase);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}
	
	/**
	 * 判断边缘容器存在。先进行本地检查，没有通过网络查询集群。
	 * @param naming 边缘容器命名
	 * @return 存在返回“真”，否则“假”。
	 */
	protected boolean hasTubTag(Naming naming) {
		try {
			return SyntaxParser.chooser.hasTubTag(naming);
		} catch (ResourceException e) {
			throw new SyntaxException(e.getMessage());
		}
	}

	
	/**
	 * 判断是账号持有人自己
	 * @param username 用户签名
	 * @return 返回真或者假
	 */
	protected boolean isPrivate(Siger username) {
		return getResourceChooser().isPrivate(username);
	}

	/**
	 * 判断数据库为当前用户私有
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	protected boolean isPrivate(Fame fame) {
		return getResourceChooser().isPrivate(fame);
	}

	/**
	 * 判断数据表为当前用户私有
	 * @param space 表名
	 * @return 返回真或者假
	 */
	protected boolean isPrivate(Space space) {
		return getResourceChooser().isPrivate(space);
	}

	/**
	 * 判断是授权表
	 * @param space 表名
	 * @return 返回真或者假
	 */
	protected boolean isPassiveTable(Space space) {
		return getResourceChooser().isPassiveTable(space);
	}

	/**
	 * 判断支持某个用户级操作
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	protected boolean canUser(short operator) {
		return getResourceChooser().canUser(operator);
	}

	/**
	 * 判断支持某个数据库级操作
	 * @param fame 数据库名
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	protected boolean canSchema(Fame fame, short operator) {
		return getResourceChooser().canSchema(fame, operator);
	}

	/**
	 * 判断支持某个数据表操作
	 * @param space 数据表名
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	protected boolean canTable(Space space, short operator) {
		return getResourceChooser().canTable(space, operator);
	}
	
	/**
	 * 判断能够执行分布计算
	 * @return 返回真或者假
	 */
	protected boolean canConduct() {
		return getResourceChooser().canConduct();
	}

	/**
	 * 判断能够执行快速计算
	 * @return 返回真或者假
	 */
	protected boolean canContact() {
		return getResourceChooser().canContact();
	}

	/**
	 * 判断能够执行分布数据构建
	 * @return 返回真或者假
	 */
	protected boolean canEstablish() {
		return getResourceChooser().canEstablish();
	}

	/**
	 * 判断能够执行分布上数据构建
	 * @param space 表名
	 * @return 返回真或者假
	 */
	protected boolean canEstablish(Space space) {
		return getResourceChooser().canEstablish(space);
	}
	
	/**
	 * 检查处于在线状态
	 */
	protected void checkOnline() {
		if (SyntaxParser.robot != null) {
			boolean success = SyntaxParser.robot.isOnline();
			// 非连线状态，弹出异常!
			if (!success) {
				throwable(FaultTip.OFFLINE_REFUSE);
			}
		}
	}

	/**
	 * 把字符串的转义字符还原。被转义的字符特点是：左右两侧有空格，且有一个前缀符号：“\”。转义时忽略它的大小写。
	 * 
	 * @param input 输入语句
	 * @return 转义后的字符串
	 */
	public String replace(String input, String keyword) {
		return replace(input, keyword, true);
	}

	/**
	 * 把字符串的转义字符还原。被转义的字符特点是：左右两侧有空格，且有一个前缀符号：“\”。转义时可以选择是否忽略大小写
	 * @param input 输入语句
	 * @param keyword 需要黑底的字符
	 * @param ignoreCase 是否忽略小大写
	 * @return 返回转义后的新字符串
	 */
	public String replace(String input, String keyword, boolean ignoreCase) {
		String regex = "^([\\p{ASCII}\\W]*?)(\\s+)(?i)(\\\\" + keyword + ")(\\s*|\\s+[\\p{ASCII}\\W]*)$";
		if (!ignoreCase) {
			regex = "^([\\p{ASCII}\\W]*?)(\\s+)(\\\\" + keyword + ")(\\s*|\\s+[\\p{ASCII}\\W]*)$";
		}

		Pattern pattern = Pattern.compile(regex);
		do {
			Matcher matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				break;
			}
			String symbol = matcher.group(3);
			input = matcher.group(1) + matcher.group(2) + symbol.substring(1)  + matcher.group(4);
		} while (true);

		return input;
	}

	/** 用户SHA256签名，只检查SIGN关键字，后面参数忽略它 **/
	protected static String SIGER_SHA256_ASSERT = "^\\s*(?i)(?:SIGN)\\s+([\\w\\W]+?)\\s*$";

	/** 用户SHA256签名标准格式 **/
	protected static String SIGER_SHA256 = "^\\s*(?i)(?:SIGN)\\s+([0-9a-fA-F]{64})\\s*$";
	
	/** 限制符号 **/
	protected static String USERNAME_LIMIT = "^\\s*(?i)([^\\p{Cntrl}^\\p{Space}]+)\\s*$";

	/**
	 * 解析单个用户签名。<br>
	 * 用户签名分为两种，一种以“SIGN”为前缀的64个数字，另一种是普通文本。
	 * 
	 * @param input 输入语句
	 * @return 返回用户签名
	 */
	protected Siger splitSiger(String input) {
		// 判断是标准的SHA256码
		if (input.matches(SIGER_SHA256_ASSERT)) {
			Pattern pattern = Pattern.compile(SIGER_SHA256);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			// 16进制字符串
			String hex = matcher.group(1);
			return new Siger(hex);
		} else {
			// 如果文本含有空格和控制字符，是错误，不允许有这样的名称
			if (!input.matches(SyntaxParser.USERNAME_LIMIT)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			
			// 否则生成用户数字签名
			return SHAUser.doUsername(input);
		}
	}
	
	/**
	 * 判断是空字符
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	protected boolean isEmpty(String input) {
		return input != null && input.matches("^(\\s*)$");
	}

	/**
	 * 判断是命令前缀
	 * @param prefix 命令关键字
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	protected boolean isCommand(String prefix, String input) {
		if (prefix == null || prefix.trim().isEmpty()) {
			return false;
		}
		// 格式		
		String formatter = "^\\s*(?i)(?:%s)(\\s*|\\s+[\\w\\W]*)$";
		prefix = prefix.replaceAll("\\s+", "\\\\s+");
		String regex = String.format(formatter, prefix);

		// 语法检查
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();		
	}
	
	
	public static void main(String[] args) {
		SyntaxParser sp = new SyntaxParser();
//		boolean b = sp.isCommand("PRINT  USER  DIAGRAM", "print user diagram");
		boolean b = sp.isCommand("ring", "RING -SECURE");
		System.out.printf("match is %s\n", b);
	}
}