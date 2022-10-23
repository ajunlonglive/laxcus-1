/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.net.*;

/**
 * LAXCUS集群节点。<br>
 * 节点是站点（Site）的静态定义，包括：站点类型、站点级别（目前只限DATA站点），站点主机地址三个部分。
 * 
 * @author scott.liang
 * @version 1.1 3/22/2015
 * @since laxcus 1.0
 */
public final class Node implements Classable, Markable, Cloneable, Serializable, Comparable<Node> {

	private static final long serialVersionUID = 4194609727368518662L;

	/** 标准站点格式 **/
	private final static String REGEX = "^\\s*(?i)(TOP|BANK|ACCOUNT|HASH|GATE|ENTRANCE|HOME|DATA|WORK|BUILD|CALL|WATCH|LOG|FRONT)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";

	/** DATA站点格式 **/
	private final static String DATA_REGEX = "^\\s*(?i)(DATA)\\((?i)(MASTER|SLAVE|PRIMARY|SECONDARY|MAIN|SUB)\\)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";

	/** 交换中心站点格式**/
	private final static String HUB_REGEX = "^\\s*(?i)(TOP|BANK|HOME)\\((?i)(MANAGER|MONITOR)\\)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";
	
	/** FRONT站点格式 **/
	private final static String FRONT_REGEX = "^\\s*(?i)(FRONT)\\((?i)(DRIVER|EDGE|CONSOLE|TERMINAL|DESKTOP|APPLICATION)\\)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";

	/** WATCH站点格式 **/
	private final static String WATCH_REGEX = "^\\s*(?i)(WATCH)\\((?i)(ADMINISTRATOR|BENCH)\\)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";
	
	/** 节点类型，见SiteTag中的定义。 */
	private byte family;

	/** 所属站点级别，见RankTag定义。*/
	private byte rank;

	/** 通信主机地址。FIXP站点的服务器地址，包括TCP/UDP两种。如果有NAT设备，表示NAT入口地地址。**/
	private SiteHost host;
	
	/** 节点别名 **/
	private String alias;

	/**
	 * 将节点参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 节点类型
		writer.write(family);
		// 站点级别
		writer.write(rank);
		// 绑定地址
		writer.writeInstance(host);
		// 别名
		writer.writeString(alias);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析节点参数
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 节点类型
		family = reader.read();
		// 站点级别
		rank = reader.read();
		// 绑定地址
		host = reader.readInstance(SiteHost.class);
		// 别名
		alias = reader.readString();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的节点实例，生成它的数据副本
	 * @param that Node实例
	 */
	private Node(Node that) {
		super();
		family = that.family;
		rank = that.rank;
		alias = that.alias;
		host = that.host.duplicate();
	}

	/**
	 * 构造一个未定义的节点
	 */
	public Node() {
		super();
		family = SiteTag.NONE;
		rank = RankTag.NONE;
	}

	/**
	 * 构造节点，指定它的类型
	 * @param family 节点类型
	 */
	public Node(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造节点，指定一个节点全部参数
	 * @param family 节点类型
	 * @param addr 绑定地址
	 * @param tcport TCP监听端口
	 * @param udport UDP监听端口
	 */
	public Node(byte family, Address addr, int tcport, int udport) {
		this(family);
		setHost(addr, tcport, udport);
	}

	/**
	 * 构造节点，指定类型和主机地址
	 * @param family 节点类型
	 * @param host 主机地址
	 */
	public Node(byte family, SiteHost host) {
		this(family);
		setHost(host);
	}

	/**
	 * 从可类化数据读取器中解析节点参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Node(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出节点参数
	 * @param reader 标记化读取器
	 */
	public Node(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 用正则表达式解析节点参数
	 * @param input 输入语句
	 * @throws UnknownHostException
	 */
	public Node(String input) throws UnknownHostException {
		this();
		resolve(input);
	}

	/**
	 * 执行安全权限检查
	 * @param suffix 操作名称
	 */
	private void check(String suffix) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", suffix);
			sm.checkPermission(new NodePermission(name));
		}
	}

	/**
	 * 设置节点类型
	 * @param who 节点类型
	 */
	public void setFamily(byte who) {
		// 安全检查
		check("Family");
		// 判断有效
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site %d", who);
		}
		family = who;
	}

	/**
	 * 返回节点类型
	 * @return 节点类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 返回节点的别名
	 * @return 节点域名地址描述
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * 判断是TOP节点
	 * @return 返回真或者假
	 */
	public boolean isTop() {
		return SiteTag.isTop(family);
	}

	/**
	 * 判断是ACCOUNT节点
	 * @return 返回真或者假
	 */
	public boolean isAccount() {
		return SiteTag.isAccount(family);
	}

	/**
	 * 判断是HASH节点
	 * @return 返回真或者假
	 */
	public boolean isHash() {
		return SiteTag.isHash(family);
	}

	/**
	 * 判断是GATE节点
	 * @return 返回真或者假
	 */
	public boolean isGate() {
		return SiteTag.isGate(family);
	}

	/**
	 * 判断是ENTRANCE节点
	 * @return 返回真或者假
	 */
	public boolean isEntrance() {
		return SiteTag.isEntrance(family);
	}

	/**
	 * 判断是BANK节点
	 * @return 返回真或者假
	 */
	public boolean isBank(){
		return SiteTag.isBank(family);
	}

	/**
	 * 判断是WATCH节点
	 * @return 返回真或者假
	 */
	public boolean isWatch() {
		return SiteTag.isWatch(family);
	}

	/**
	 * 判断是HOME节点
	 * @return 返回真或者假
	 */
	public boolean isHome() {
		return SiteTag.isHome(family);
	}

	/**
	 * 判断是LOG节点
	 * @return 返回真或者假
	 */
	public boolean isLog() {
		return SiteTag.isLog(family);
	}

	/**
	 * 判断是FRONT节点(图形窗口/字符控制台/驱动程序)
	 * @return 返回真或者假
	 */
	public boolean isFront() {
		return SiteTag.isFront(family);
	}

	/**
	 * 判断是CALL节点
	 * @return 返回真或者假
	 */
	public boolean isCall() {
		return SiteTag.isCall(family);
	}

	/**
	 * 判断是DATA节点
	 * @return 返回真或者假
	 */
	public boolean isData() {
		return SiteTag.isData(family);
	}
	
	/**
	 * 判断是DATA主节点
	 * @return 返回真或者假
	 */
	public boolean isMasterData() {
		return isData() && isMaster();
	}

	/**
	 * 判断是DATA从节点
	 * @return 返回真或者假
	 */
	public boolean isSlaveData() {
		return isData() && isSlave();
	}

	/**
	 * 判断是WORK节点
	 * @return 返回真或者假
	 */
	public boolean isWork() {
		return SiteTag.isWork(family);
	}

	/**
	 * 判断是BUILD节点
	 * @return 返回真或者假
	 */
	public boolean isBuild() {
		return SiteTag.isBuild(family);
	}

	/**
	 * 设置站点级别，包括DATA、TOP/HOME/BANK、FRONT
	 * @param who 站点级别
	 */
	public void setRank(byte who) {
		// 判断有效才设置
		if (RankTag.isRank(who)) {
			rank = who;
		}
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 判断无站点级别
	 * @return 返回真或者假
	 */
	public boolean isNoneRank() {
		return RankTag.isNone(rank);
	}

	/**
	 * 判断是DATA主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return SiteTag.isData(family) && RankTag.isMaster(rank);
	}

	/**
	 * 判断是DATA从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return SiteTag.isData(family) && RankTag.isSlave(rank);
	}

	/**
	 * 判断是交换中心管理节点
	 * @return 返回真或者假
	 */
	public boolean isManager() {
		return SiteTag.isHub(family) && RankTag.isManager(rank);
	}
	
	/**
	 * 判断是交换中心监视器节点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		return SiteTag.isHub(family) && RankTag.isMonitor(rank);
	}
	
	/**
	 * 判断是FRONT驱动
	 * @return 返回真或者假
	 */
	public boolean isDriver() {
		return SiteTag.isFront(family) && RankTag.isDriver(rank);
	}

	/**
	 * 判断是FRONT边缘节点
	 * @return 返回真或者假
	 */
	public boolean isEdge() {
		return SiteTag.isFront(family) && RankTag.isEdge(rank);
	}

	/**
	 * 判断是FRONT字符控制台
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		return SiteTag.isFront(family) && RankTag.isConsole(rank);
	}

	/**
	 * 判断是FRONT图形终端
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		return SiteTag.isFront(family) && RankTag.isTerminal(rank);
	}

	/**
	 * 判断是FRONT虚拟桌面 
	 * @return 返回真或者假
	 */
	public boolean isDesktop() {
		return SiteTag.isFront(family) && RankTag.isDesktop(rank);
	}

	/**
	 * 判断是FRONT客户端应用软件
	 * @return 返回真或者假
	 */
	public boolean isApplication() {
		return SiteTag.isFront(family) && RankTag.isApplication(rank);
	}
	
	/**
	 * 返回网络地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		return host.getInetAddress();
	}

	/**
	 * 设置网络地址
	 * @param e InetAddress实例
	 */
	public void setInetAddress(InetAddress e) {
		// 安全检查
		check("InetAddress");
		// 设置主机地址
		host.setInetAddress(e);
	}

	/**
	 * 返回TCP端口号
	 * @return 端口号
	 */
	public int getTCPort() {
		return host.getTCPort();
	}

	/**
	 * 返回UDP端口号
	 * @return 端口号
	 */
	public int getUDPort() {
		return host.getUDPort();
	}

	/**
	 * 设置节点主机地址
	 * @param addr IP地址
	 * @param tcport TCP端口号
	 * @param udport UDP端口号
	 */
	public void setHost(InetAddress addr, int tcport, int udport) {
		// 安全检查
		check("Host");
		// 设置主机地址
		host = new SiteHost(addr, tcport, udport);
	}

	/**
	 * 设置节点主机地址
	 * @param addr IP地址
	 * @param tcport TCP端口号
	 * @param udport UDP端口号
	 */
	public void setHost(Address addr, int tcport, int udport) {
		// 安全检查
		check("Host");
		// 设置主机地址
		host = new SiteHost(addr, tcport, udport);
	}

	/**
	 * 设置节点主机地址
	 * @param e SiteHost实例
	 */
	public void setHost(SiteHost e) {
		// 安全检查
		check("Host");
		// 设置主机地址
		host = e;
	}

	/**
	 * 根据流标识，选择一个SOCKET连接地址
	 * @param stream 数据流模式
	 * @return SocketHost实例
	 */
	public SocketHost choice(boolean stream) {
		if (stream) {
			return getStreamHost();
		} else {
			return getPacketHost();
		}
	}

	/**
	 * 返回节点主机
	 * @return SiteHost实例
	 */
	public SiteHost getHost() {
		return host;
	}

	/**
	 * 返回流模式套接字地址
	 * @return SocketHost实例
	 */
	public SocketHost getStreamHost() {
		return host.getStreamHost();
	}

	/**
	 * 返回包模式套接字地址
	 * @return SocketHost实例
	 */
	public SocketHost getPacketHost() {
		return host.getPacketHost();
	}

	/**
	 * 返回节点地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return host.getAddress();
	}

	/**
	 * 设置节点地址
	 * @param e Address实例
	 */
	public void setAddress(Address e) {
		// 安全检查
		check("Address");
		// 设置主机地址
		host.setAddress(e);
	}

	/**
	 * 返回当前对象的完整数据副本
	 * @return Node实例
	 */
	public Node duplicate(){
		return new Node(this);
	}

	/**
	 * 判断节点是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Node.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Node) that) == 0;
	}

	/**
	 * 节点散列码是站点类型
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family;
	}

	/**
	 * 返回Node对象的的浅层副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回节点的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * 返回字符串
	 * @param ip IP格式
	 * @return 返回字符串
	 */
	public String toString(boolean ip) {
		if (family == 0 || host == null) {
			return "none node";
		}

		// 在非IP格式的情况，首先选择别名，且不是"MANAGERMONITOR/MASTER/SLAVE"类型
		if (!ip) {
			if (alias != null && isNoneRank()) {
				return alias;
			}
		}

		// 根据级别，选择合适的输出格式
		if (isNoneRank()) {
			return String.format("%s://%s:%d_%d", SiteTag.translate(family),
					host.getAddress(), host.getTCPort(), host.getUDPort());
		} else {
			return String.format("%s(%s)://%s:%d_%d",
					SiteTag.translate(family), RankTag.translate(rank),
					host.getAddress(), host.getTCPort(), host.getUDPort());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Node that) {
		// 排序时空对象在前面
		if (that == null) {
			return 1;
		}

		// 比较节点“类型、节点”地址两个个参数，忽略“级别”！
		// 注意，因为TOP/BANK/HOME节点的“级别”是会随切换改变的，子节点不一定保存这个值，所以不要判断“级别”！
		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(host, that.host);
		}
		return ret;
	}

	/**
	 * 生成节点的数字签名
	 * @return 输出SHA256编码
	 */
	public SHA256Hash sign() {
		byte[] b = build();
		return Laxkit.doSHA256Hash(b);
	}

	/**
	 * 将节点参数转为字节数组输出
	 * @return 字节数组
	 * @since 1.1
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从字节数组中解析节点参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的长度
	 * @since 1.1
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 用正则表达式解析节点地址
	 * @param input 输入语句
	 * @throws UnknownHostException
	 */
	public void resolve(String input) throws UnknownHostException {
		// 1. 首先对节点地址进行转义处理
		input = ConfigParser.splitSite(input);
		// 2. 分析两种格式
		boolean match = splitFew(input);
		if (!match) {
			match = splitFull(input);
		}
		if (!match) {
			throw new UnknownHostException("illegal node " + input);
		}
	}
	
	/**
	 * 判断是"localhost"
	 * @param who 字符串
	 * @return 返回真或者假
	 */
	private boolean isLocalhost(String who) {
		return who.matches("^\\s*(?i)(LOCALHOST)\\s*$");
	}

	/**
	 * 解析通用格式
	 * @param input 输入语句
	 * @return 成功返回真否则假
	 * @throws UnknownHostException
	 */
	private boolean splitFew(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(Node.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配返回假
		if (!matcher.matches()) {
			return false;
		}

		family = SiteTag.translate(matcher.group(1));
		// 解析IP地址
		String name = matcher.group(2);
		Address address = new Address(name);
		// TCP/UDP端口号
		int tcport = Integer.parseInt(matcher.group(3));
		int udport = Integer.parseInt(matcher.group(4));

		// 设置参数
		host = new SiteHost(address, tcport, udport);

		// 不是IP地址，且不是“localhost”，做为别名保存
		if (!Address.isIPStyle(name) && !isLocalhost(name)) {
			alias = input.trim();
		}

		return true;
	}

	/**
	 * 解析DATA格式，带"MARSTER|SLAVE"标记。
	 * @param input 输入语句
	 * @return 成功返回真否则假
	 * @throws UnknownHostException
	 */
	private boolean splitFull(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(Node.DATA_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(Node.HUB_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(Node.FRONT_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(Node.WATCH_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		// 不匹配返回假
		if (!match) {
			return false;
		}
		
		family = SiteTag.translate(matcher.group(1));
		rank = RankTag.translate(matcher.group(2));
		// 解析IP地址
		String name = matcher.group(3);
		Address address = new Address(name);
		// TCP/UDP端口号
		int tcport = Integer.parseInt(matcher.group(4));
		int udport = Integer.parseInt(matcher.group(5));

		// 设置参数
		host = new SiteHost(address, tcport, udport);

		// 不是IP地址，且不是"localhost"字符串，做为别名保存
		if (!Address.isIPStyle(name) && !isLocalhost(name)) {
			alias = input.trim();
		}
		return true;
	}

	/**
	 * 判断是有效的节点地址格式
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean validate(String input) {
		// 不是有效格式，返回假
		if (input == null || input.isEmpty()) {
			return false;
		}

		// 正则表达式（不带级别）
		Pattern pattern = Pattern.compile(Node.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		// 正则表达式（带级别）
		if (!match) {
			pattern = Pattern.compile(Node.DATA_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		// 判断是交换中心（带级别）
		if (!match) {
			pattern = Pattern.compile(Node.HUB_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		// 判断是FRONT节点
		if (!match) {
			pattern = Pattern.compile(Node.FRONT_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		// 判断是FRONT节点
		if (!match) {
			pattern = Pattern.compile(Node.WATCH_REGEX);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		// 返回结果
		return match;
	}

}