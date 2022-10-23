/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.naming;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 阶段命名。<br><br>
 * 
 * 阶段命名是LAXCUS分布计算体系中，对一个分布任务组件的描述，具有唯一性。
 * 通过阶段命名，可以快速定位LAXCUS集群中的分布对象和调用它。应用在CONDUCT/ESTABLISH/CONTACT的命令里。<br><br>
 * 
 * 阶段命名参数由4部分组成：<br>
 * 1. 执行阶段，是一个整型值，必选项。<br>
 * 2. 根命名，或者称“基础命名”，必选项，包括软件名称和任务组件命名。<br>
 * 3. 子命名，根命名之下的命名，允许任意多个。子命名以链接和迭代形式存在，是可选项。CONDUCT.TO/CONTACT.DISTANT阶段已经使用。<br>
 * 4. 用户签名签名，SHA256散列码，可选项。用于检查操作人的合法性。<br><br>
 * 
 * 执行阶段定义见PhaseTag中的定义。<br><br>
 * 
 * 用户签名说明：<br>
 * 1. 分布任务组件分为系统级和用户级两种。<br>
 * 2. 判断组件属性（系统级或者用户级）由任务管理池（TaskPool）加载时判断。<br>
 * 如果没有定义用户签名，且Sock.ware=“STSTEM”时，属于系统级。<br>
 * 如果定义用户签名，且Sock.ware!="SYSTEM"时，属于用户级。其它情况都是错误。<br>
 * 
 * 对所有操作人公开，但是这种调用是隐性的，用户无法通过CONDUCT/ESTABLIS/CONTACTH命令获得，例如系统中的SQL嵌套查询。<br>
 * 
 * 阶段命名中的用户签名为生成任务实例提供依据，用在“TaskPool.createTask”方法中。
 * 当项目的用户签名和阶段命名的用户签名不一致时，TaskPool将拒绝提供组件类实例。<br><br>
 * 
 * 系统级分布任务组件由LAXCUS设计和部署。用户级分布任务组件由注册用户设计和编写代码，发布到LAXCUS集群中使用。<br><br>
 * 
 * <br>
 * 
 * 在引入第三方应用后，判断系统组件的两个条件：<br>
 * 1. Sock.ware 是"SYSTEM"。<br>
 * 2. issuer 是空指针。<br><br>
 * 
 * 判断用户组件的两个条件：<br>
 * 1. Sock.ware不是"SYSTEM"。<br>
 * 2. issuer不是空指针。<br><br>
 * 
 * 另外情况：如果是Sock.isSystemLevel，且issuer有签名，此时issuer不是组件拥有者，而是操作者！
 * 
 * TaskPart.issuer 是空指针时，表示是系统调用。<br>
 * 
 * @author scott.liang
 * @version 1.2 05/03/2015
 * @since laxcus 1.0
 */
public final class Phase implements Classable, Markable, Serializable, Cloneable, Comparable<Phase> {

	private static final long serialVersionUID = -6653411802822467078L;

	/** 阶段命名标准的正则表达格式 **/
	private final static String STANDARD_FULL = "^\\s*(\\w+)\\:\\{(?i)([0-9a-fA-F]{64})\\}\\/([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*";
	private final static String STANDARD_ROOT = "^\\s*(\\w+)\\:\\{(?i)([0-9a-fA-F]{64})\\}\\/([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*";

	/** 阶段命名简化的正则表达格式 **/
	private final static String SIMPLE_FULL = "^\\s*(\\w+)\\:([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*";
	private final static String SIMPLE_ROOT = "^\\s*(\\w+)\\:([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*";

	/** 命名格式 **/
	private final static String NAMING = "^\\s*(?i)([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+)\\s*$";
	
	/** 执行阶段，必选项 **/
	private int family;

	/** 根命名，必选项 **/
	private Sock sock;

	/** 子命名，可选项。用于迭代环境 **/
	private Naming sub;

	/** 操作者签名。当Sock不是系统组件时，比较这个签名 **/
	private Siger issuer;

	/**
	 * 构造一个私有和默认的阶段命名
	 */
	private Phase() {
		super();
		family = 0;
	}

	/**
	 * 使用传入的阶段命名，生成一个新的数据副本
	 * @param that 阶段命名对象
	 */
	private Phase(Phase that) {
		this();
		family = that.family;
		// 本地参数副本
		if (that.issuer != null) {
			issuer = that.issuer.duplicate();
		}
		if (that.sock != null) {
			sock = that.sock.duplicate();
		}
		if (that.sub != null) {
			sub = that.sub.duplicate();
		}
	}

	/**
	 * 构造一个阶段命名，同时指定它的参数
	 * @param family 执行阶段
	 * @param sock 根命名
	 */
	public Phase(int family, Sock sock) {
		this();
		setFamily(family);
		setSock(sock);
	}

	/**
	 * 构造一个阶段命名，同时指定它的参数
	 * @param family 执行阶段
	 * @param sock 根命名
	 * @param sub 子命名
	 */
	public Phase(int family, Sock sock, String sub) {
		this(family, sock);
		setSub(sub);
	}

	/**
	 * 构造一个阶段命名，同时指定它的参数
	 * @param family 执行阶段
	 * @param sock 根命名
	 * @param sub 子命名
	 */
	public Phase(int family, Sock sock, Naming sub) {
		this(family, sock);
		setSub(sub);
	}
	
	/**
	 * 构造阶段命名，指定用户名、执行阶段、根命名
	 * @param issuer 用户名称
	 * @param family 执行阶段
	 * @param sock 根命名
	 */
	public Phase(Siger issuer, int family, Sock sock) {
		this(family, sock);
		setIssuer(issuer);
	}

	/**
	 * 构造阶段命名，指定用户名、执行阶段、根命名、子命名
	 * @param issuer 用户名称
	 * @param family 执行阶段
	 * @param sock 根命名
	 * @param sub 子命名
	 */
	public Phase(Siger issuer, int family, Sock sock, Naming sub) {
		this(family, sock, sub);
		setIssuer(issuer);
	}

	/**
	 * 构造阶段命名，指定用户名、执行阶段、根命名、子命名
	 * @param issuer 用户名称
	 * @param family 执行阶段
	 * @param sock 根命名
	 * @param sub 子命名
	 */
	public Phase(Siger issuer, int family, Sock sock, String sub) {
		this(family, sock, sub);
		setIssuer(issuer);
	}

	/**
	 * 使用传入的可类化读取器解析阶段命名
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public Phase(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出阶段命名参数
	 * @param reader 标记化读取器
	 */
	public Phase(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 返回任务根命名
	 * @return Sock实例
	 */
	public Sock getSock() {
		return sock;
	}
	
	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Naming getWare() {
		return sock.getWare();
	}

	/**
	 * 返回任务根命名的文本描述
	 * @return 字符串
	 */
	public String getSockText() {
		return sock.toString();
	}

	/**
	 * 设置根命名，根命名不可以是空指针
	 * @param e 命名
	 */
	public void setSock(Sock e) {
		Laxkit.nullabled(e);

		sock = e;
	}

	/**
	 * 返回任务子命名(子命名非必要存在)
	 * @return Naming实例
	 */
	public Naming getSub() {
		return sub;
	}

	/**
	 * 返回任务子命名的文本描述
	 * @return 字符串
	 */
	public String getSubText() {
		if (sub == null) {
			return null;
		}
		return sub.toString();
	}

	/**
	 * 设置子命名，子命名允许空指针
	 * @param e 命名实例
	 */
	public void setSub(Naming e) {
		if (e == null) {
			sub = null;
		} else {
			sub = e;
		}
	}

	/**
	 * 设置子命名
	 * @param text 字符串
	 */
	public void setSub(String text) {
		if(text == null || text.trim().isEmpty()) {
			sub = null;
		} else {
			sub = new Naming(text);
		}
	}

	/**
	 * 设置执行阶段
	 * @param who 执行阶段符号
	 */
	public void setFamily(int who) {
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalPhaseException("illegal phase: %d", who);
		}
		family = who;
	}

	/**
	 * 返回执行阶段
	 * @return 整型值执行阶段
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 设置执行阶段
	 * @param family 执行阶段的字符串描述
	 */
	public void setFamily(String family) {
		int who = PhaseTag.translate(family);
		if (who < 0) {
			throw new IllegalPhaseException("illegal phase:%s", family);
		}
		setFamily(who);
	}

	/**
	 * 返回执行阶段的字符描述
	 * @return 字符串
	 */
	public String getFamilyText() {
		return PhaseTag.translate(family);
	}

	/**
	 * 设置阶段命名用户签名，允许空值.
	 * @param e 数字签名人
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 返回阶段命名用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}
	
	/**
	 * 判断是匹配用户签名
	 * @param that 用户签名
	 * @return 返回真或者假
	 */
	public boolean isIssuer(Siger that) {
		return (issuer != null && Laxkit.compareTo(issuer, that) == 0);
	}

//	/**
//	 * 判断是系统级阶段命名
//	 * @return 判断成立返回“真”，否则“假”。
//	 */
//	public boolean isSystemLevel() {
//		return issuer == null;
//	}
//
//	/**
//	 * 判断是用户级阶段命名
//	 * @return 判断成立返回“真”，否则“假”。
//	 */
//	public boolean isUserLevel() {
//		return issuer != null;
//	}

	/**
	 * 判断是系统级阶段命名
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isSystemLevel() {
		return sock.isSystemLevel();
	}

	/**
	 * 判断是用户级阶段命名
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isUserLevel() {
		return sock.isUserLevel();
	}
	
	/**
	 * 根据当前阶段命名对象，生成一个新的深层数据副本。
	 * @return Phase 当前的数据副本
	 */
	public Phase duplicate() {
		return new Phase(this);
	}

	/**
	 * 返回阶段命名的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// 非简化模式输出
		return toString(false);
	}

	/**
	 * 以字符串格式输出
	 * @param simple 简化格式，忽略用户签名，如果阶段命名属于用户级别的话。
	 * @return 返回字符串描述
	 */
	public String toString(boolean simple) {
		String text = null;
		if (simple) {
			text = String.format("%s:%s", getFamilyText(), sock);
		} else {
			// 只当是用户组件时，定义签名
			if (isUserLevel() && issuer != null) {
				text = String.format("%s:{%s}/%s", getFamilyText(), issuer, sock);
			} else {
				text = String.format("%s:%s", getFamilyText(), sock);
			}
		}

		if (sub != null) {
			text = String.format("%s.%s", text, sub);
		}
		return text;
	}

	/**
	 * 比较两个阶段命名是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Phase.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((Phase) that) == 0;
	}

	/**
	 * 返回当前阶段命名的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int ret = family ^ sock.hashCode();
		if (sub != null) {
			ret ^= sub.hashCode();
		}
		return ret;
	}

	/*
	 * 返回当前对象的深层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

//	/**
//	 * 比较阶段对象的排序位置。用户签名不参与比较
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(Phase that) {
//		// 空对象排在前面，当前对象排在后面
//		if (that == null) {
//			return 1;
//		}
//		// 1. 阶段类型
//		int ret = Laxkit.compareTo(family, that.family);
//		// 2. 用户签名
//		if (ret == 0) {
//			ret = Laxkit.compareTo(issuer, that.issuer);
//		}
//		// 3. 根命名
//		if (ret == 0) {
//			ret = root.compareTo(that.root);
//		}
//		// 4. 子命名
//		if (ret == 0) {
//			ret = Laxkit.compareTo(sub, that.sub);
//		}
//		return ret;
//	}

	/**
	 * 比较阶段对象的排序位置。
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Phase that) {
		// 空对象排在前面，当前对象排在后面
		if (that == null) {
			return 1;
		}
		// 1. 阶段类型
		int ret = Laxkit.compareTo(family, that.family);
		// 2. 根命名
		if (ret == 0) {
			ret = sock.compareTo(that.sock);
		}
		// 3. 子命名
		if (ret == 0) {
			ret = Laxkit.compareTo(sub, that.sub);
		}
		// 4. 比较用户签名
		if (ret == 0) {
			// 任何一方是系统组件，都允许对方使用，条件成立。否则比较双方的签名。
			if (sock.isSystemLevel() || that.sock.isSystemLevel()) {

			} else {
				ret = Laxkit.compareTo(issuer, that.issuer);
			}
		}
		return ret;
	}
	
	/**
	 * 将命名任务阶段参数写入可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节数据长度
	 * @since 1.2
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 1. 执行阶段
		writer.writeInt(family);
		// 2. 根命名，这个必须有
		writer.writeObject(sock);
		// 3. 子命名，这个选择性存在
		writer.writeInstance(sub);
		// 4.阶段命名用户签名
		writer.writeInstance(issuer);
		// 返回写入字节数
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析命名任务阶段参数
	 * @param reader 可类化读取器
	 * @return 返回解析的字节数据长度
	 * @since 1.2
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 1.执行阶段，同时进行检查
		setFamily(reader.readInt());
		// 2. 根命名，这个必须有
		sock = new Sock(reader);
		// 3.子命名，这个选择性存在
		sub = reader.readInstance(Naming.class);
		// 4.阶段命名用户签名。选择性存在
		issuer = reader.readInstance(Siger.class);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 分解标准完整格式
	 * @param input 输入语句
	 * @return 匹配解析成功返回真，否则假
	 */
	private boolean splitStandardFull(String input) {
		// 1.全路径格式（阶段名称，根路径，子路径）
		Pattern pattern = Pattern.compile(Phase.STANDARD_FULL);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		if (!matcher.matches()) {
			return false;
		}

		setFamily(matcher.group(1));
		String user = matcher.group(2);
		if (user.matches("^\\s*(?i)(SYSTEM)\\s*$")) {
			setIssuer(null);
		} else {
			setIssuer(new Siger(user));
		}
		setSock(new Sock(matcher.group(3), matcher.group(4)));
		setSub(matcher.group(5));
		return true;
	}

	/**
	 * 分解标准缩略格式
	 * @param input 输入语句
	 * @return 匹配解析成功返回真，否则假
	 */
	private boolean splitStandardRoot(String input) {
		// 2.根路径格式
		Pattern pattern = Pattern.compile(Phase.STANDARD_ROOT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}
		// 提取参数
		setFamily(matcher.group(1));
		String user = matcher.group(2);
		if (user.matches("^\\s*(?i)(SYSTEM)\\s*$")) {
			setIssuer(null);
		} else {
			setIssuer(new Siger(user));
		}
		setSock(new Sock(matcher.group(3), matcher.group(4)));
		sub = null;
		return true;
	}

	/**
	 * 分解简化完整格式
	 * @param input 输入语句
	 * @return 匹配解析成功返回真，否则假
	 */
	private boolean splitSimpleFull(String input) {
		Pattern pattern = Pattern.compile(Phase.SIMPLE_FULL);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			setFamily(matcher.group(1));
			setIssuer(null);
			setSock(new Sock(matcher.group(2), matcher.group(3)));
			setSub(matcher.group(4));
		}
		return match;
	}

	/**
	 * 分解简化缩略格式
	 * @param input 输入语句
	 * @return 匹配解析成功返回真，否则假
	 */
	private boolean splitSimpleRoot(String input) {
		Pattern pattern = Pattern.compile(Phase.SIMPLE_ROOT);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			setFamily(matcher.group(1));
			setIssuer(null);
			setSock(new Sock(matcher.group(2), matcher.group(3)));
			sub = null;
		}
		return match;
	}

	/**
	 * 从输入的阶段命名中解析它的参数。如果参数错误，弹出异常。
	 * @param input 阶段命名的文本描述
	 * @throws IllegalPhaseException
	 */
	public void resolve(String input) {
		boolean success = splitStandardFull(input);
		if (!success) {
			success = splitStandardRoot(input);
		}
		if (!success) {
			success = splitSimpleFull(input);
		}
		if (!success) {
			success = splitSimpleRoot(input);
		}
		if (!success) {
			throw new IllegalPhaseException("illegal phase '%s'", input);
		}
	}
	
	/**
	 * 判断阶段命名中的命名符合标准格式
	 * @param input 输入文本
	 * @return 返回真或者假
	 */
	public static boolean isNaming(String input) {
		Pattern pattern = Pattern.compile(Phase.NAMING);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	//	public void resolve1(String input) {
	//		// 1.全路径格式（阶段名称，根路径，子路径）
	//		Pattern pattern = Pattern.compile(Phase.STANDARD_FULL);
	//		Matcher matcher = pattern.matcher(input);
	//		if (matcher.matches()) {
	//			setFamily(matcher.group(1));
	//			String user = matcher.group(2);
	//			if(user.matches("^\\s*(?i)(SYSTEM)\\s*$")) {
	//				setIssuer(null);
	//			} else {
	//				setIssuer(new Siger(user));
	//			}
	//			setRoot(matcher.group(3));
	//			setSub(matcher.group(4));
	//			return;
	//		}
	//
	//		// 2.根路径格式
	//		pattern = Pattern.compile(Phase.STANDARD_ROOT);
	//		matcher = pattern.matcher(input);
	//		if (matcher.matches()) {
	//			setFamily(matcher.group(1));
	//			String user = matcher.group(2);
	//			if(user.matches("^\\s*(?i)(SYSTEM)\\s*$")) {
	//				setIssuer(null);
	//			} else {
	//				setIssuer(new Siger(user));
	//			}
	//			setRoot(matcher.group(3));
	//			sub = null;
	//			return;
	//		}
	//
	//		throw new IllegalPhaseException("illegal phase '%s'", input);
	//	}

//	public static void main(String[] args) {
//		//			Siger user = new Siger("89E495E7941CF9E40E6980D14A16BF023CCD4C91");
//
//		Siger user = SHAUser.doUsername("AIXBIT");
//		String root = "侏罗纪公园III";
//
////		Phase phase = new Phase(user, PhaseTag.FROM, new Naming(root), new Naming("恐龙归来"));
//		Phase phase = new Phase(user, PhaseTag.FROM,
//				new Sock("大漠风尘",root), new Naming("恐龙归来"));
//		String s = phase.toString();
//		System.out.println(s);
//
//		Phase two = new Phase(s);
//		System.out.println(two.toString());
//
//		Phase three = new Phase(two.toString());
//		System.out.println(three.toString());
//
//		String input = "FROM:SYSTEM_PENTIUM奔腾.MEMBER成员";
//		input = "FROM:流浪地圩.PENTIUM.奔腾成员";
//		Phase four = new Phase(input);
//		System.out.println(four.toString());
//
//		System.out.println("==================");
//		com.laxcus.access.schema.Space space = new com.laxcus.access.schema.Space(" 数据库DB ", "   数据表TB");
//		s = space.toString();
//		System.out.println(s);
//		space = new com.laxcus.access.schema.Space(s);
//		System.out.println(space);
//
//		byte[] b = space.build();
//		StringBuilder sb = new StringBuilder();
//		for(int i =0; i < b.length; i++) {
//			s = String.format("%d ", b[i]);
//			sb.append(s);
//		}
//		System.out.printf("%d - %s\n", b.length, sb.toString());
//
//		com.laxcus.access.schema.Fame fame = new com.laxcus.access.schema.Fame("凉风起天末，君子意如何");
//		System.out.println(fame);
//
//	}

	//	/**
	//	 * 从输入的阶段命名中解析它的参数。如果参数错误，弹出异常。
	//	 * @param input 阶段命名的文本描述
	//	 * @throws IllegalPhaseException
	//	 */
	//	public void resolve(String input) {
	//		// 1.全路径格式（阶段名称，根路径，子路径）
	//		Pattern pattern = Pattern.compile(Phase.PHASE_FULL);
	//		Matcher matcher = pattern.matcher(input);
	//		if (matcher.matches()) {
	//			setFamily(matcher.group(1));
	//			setRoot(matcher.group(2));
	//			setSub(matcher.group(3));
	//			return;
	//		}
	//
	//		// 2.根路径格式
	//		pattern = Pattern.compile(Phase.PHASE_ROOT);
	//		matcher = pattern.matcher(input);
	//		if (matcher.matches()) {
	//			setFamily(matcher.group(1));
	//			setRoot(matcher.group(2));
	//			sub = null;
	//			return;
	//		}
	//
	//		throw new IllegalPhaseException("illegal phase '%s'", input);
	//	}

//	/**
//	 * 从阶段命名的字符串描述中解析它的参数
//	 * @param input 阶段命名的字符串描述
//	 * @throws IllegalPhaseException
//	 */
//	public Phase(String input) {
//		this();
//		resolve(input);
//	}
//
//	public static void main(String[] args) {
//		String[] s = new String[] {
//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/NIMBUS.SORT_BENCHMARK",
//				"TO:SYSTEM.SYSTEM_SUBSELECT.NOT_IN",
//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/Ticker.MINING",
//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/System.DEMO_SORT",
//				"TO:SYSTEMS.SYSTEM_SELECT.GROUPBY",
//				"DISTANT:SYSTEM.PRINT"};
//		for (String input : s) {
//			Phase e = new Phase(input);
//			System.out.println(e.toString());
//			System.out.printf("system is %s\n", e.isSystemLevel());
//			System.out.printf("issuer is %s\n", e.getIssuer());
//			System.out.printf("sock is \"%s\"\n", e.getSock());
//			System.out.println();
//		}
//	}
	
//	public static void main(String[] args) {
//		Siger sign = Laxkit.doSiger("demo");
//		Phase phase = new Phase(sign, PhaseTag.TO,
//		new Sock("Benchmark", "Sort"), new Naming("Geon"));
//		
//		System.out.println(phase.toString());
//	}

}