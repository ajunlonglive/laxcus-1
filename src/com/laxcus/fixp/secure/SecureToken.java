/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * RSA密钥令牌。<br>
 * 
 * RSA密钥令牌保存在服务端，RSA私钥用于解密数据，RSA公钥分发给客户机，让客户机使用RSA公钥加密“对称密钥”，再由“对称密钥”加密数据，实现数据传输过程中的安全可靠。
 * RSA密钥令牌针对不同网段的IP地址，实现有区别的个性化的网络安全控制，杜绝不可靠安全通信。比如对来自公网IP的任务要求加密通信，对内网IP的任务不要求加密。
 * 
 * 
 * FIXP安全通信说明：<br>
 * 1. 在每个站点的“conf/local.xml”文件中，有一个“security-network”标签，这个标签指向一个安全管理文件，它决定一个站点的FIXP安全通信策略。<br>
 * 2. 每个站点连接不同的站点，和接收不同站点的连接时，会选择不同的安全通信策略，这些安全通信策略由RSA密钥令牌决定。<br>
 * 3. 客户机是否使用加密通信，由服务器决定 ；服务器是否要求客户机进行加密通信，由RSA密钥令牌决定。<br>
 * 4. RSA密钥令牌分为公钥令牌和私钥令牌两种。公钥令牌用于客户机，对发送的数据进行加密。私钥令牌用于服务器，对接收的数据进行解密。<br>
 * 5. 在一个安全策略文件中，公钥、私钥令牌可以有任意多个，具体数量不限。<br>
 * 
 * <br><br>
 * 
 * RSA密钥令牌说明：<br>
 * 1.“token”是密钥令牌标签，携带“check” 、“mode”两个属性参数。
 * 2.“check”说明服务器对客户机进行哪种安全检查。安全检查分为四种：“不检查、地址检查、密文检查（使用对称密钥），双重检查（地址/密文）”。<br>
 * 3. “mode”说明一个RSA密钥令牌的安全管理范围，分“special”和“common”两种。“special”要求指定一批IP地址，“common”适用于“special”之外的所有的IP地址（忽略“range”标签）。“special”令牌允许任意多个，“common”令牌最多只有一个（超过一个，只保留最后一个）。程序判断一个地址对应的令牌时，先检查“special”令牌，在没有情况下，输出“common”令牌。<br>
 * 4. “server”是私钥令牌标签，“client”是公钥令牌标签。<br>
 * 5. 私钥令牌的“private-key”标签，指向RSA私钥；公钥令牌的“public-key”标签，指向RSA公钥。<br>
 * 6. 私钥令牌的"range"标签存在于"server"标签下，任意多个，说明本段服务器私钥令牌可以受理的客户机IP地址。<br>
 * 
 * <br>
 * 
 * 补充说明：<br>
 * Laxcus集群分为内网和外网两个部分，由于内网由管理员管理，不对外公开，有比较可靠的安全保证。
 * 外网的FRONT节点由用户管理，不在管理员控制范围内。
 * 因此做为连接FRONT和内网的桥梁，网关节点ENTRANCE/GATE/CALL的密钥令牌设置就显得重要。
 * 建议ENTRANCE/GATE/CALL密钥令牌设置为：&lttoken check="cipher" mode="common"&gt，
 * 
 * @author scott.liang
 * @version 1.0 2/10/2020
 * @since laxcus 1.0
 */
public final class SecureToken implements Comparable<SecureToken> {

	/** 名称，具备唯一性 **/
	private Naming name;

	/** 安全检查类型 **/
	private int family;

	/** 安全管理模式，指定/公共中的任意一种 **/
	private int mode;

	/** 地址范围，当mode="common"，地址范围忽略。 **/
	private TreeSet<SecureRange> ranges = new TreeSet<SecureRange>();

	/** 服务器私钥，保存在服务器，解密客户机数据 **/
	private ServerKey serverKey;

	/** 客户机公钥，保存在服务器，分发给客户机 **/
	private ClientKey clientKey;

	/**
	 * 构造默认的RSA密钥令牌
	 */
	public SecureToken() {
		super();
	}

	/**
	 * 生成RSA密钥令牌
	 * @param naming 命名
	 * @param family 安全类型
	 * @param mode 安全管理模型
	 */
	public SecureToken(Naming naming, int family, int mode) {
		this();
		setName(naming);
		setFamily(family);
		setMode(mode);
	}

	/**
	 * 生成RSA密钥令牌
	 * @param naming 命名
	 * @param family 安全类型
	 * @param mode 安全管理模型
	 */
	public SecureToken(String name, int family, int mode) {
		this(new Naming(name), family, mode);
	}
	
	/**
	 * 设置名称
	 * @param e 命名对象
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 返回名称
	 * @return 命名对象
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 设置安全检查类型
	 * @param who 安全检查类型
	 */
	public void setFamily(int who) {
		if (!SecureType.isFamily(who)) {
			throw new IllegalValueException("illegal value:%d", who);
		}
		family = who;
	}

	/**
	 * 返回安全检查类型
	 * @return 安全检查类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断无校验
	 * @return 返回真或者假
	 */
	public boolean isNone() {
		return SecureType.isNone(family);
	}

	/**
	 * 判断是地址校验
	 * @return 返回真或者假
	 */
	public boolean isAddress() {
		return SecureType.isAddress(family);
	}

	/**
	 * 判断是密文校验
	 * @return 返回真或者假
	 */
	public boolean isCipher() {
		return SecureType.isCipher(family);
	}

	/**
	 * 判断是地址/密文双重校验
	 * @return 返回真或者假
	 */
	public boolean isDuplex() {
		return SecureType.isDuplex(family);
	}

	/**
	 * 设置安全管理模式
	 * @param who 安全管理模式
	 */
	public void setMode(int who) {
		if (!SecureMode.isMode(who)) {
			throw new IllegalValueException("illegal secure mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回安全管理模式
	 * @return 安全管理模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断是公用安全管理模式
	 * @return 返回真或者假
	 */
	public boolean isCommon() {
		return SecureMode.isCommon(mode);
	}

	/**
	 * 判断是特用安全管理模式
	 * @return 返回真或者假
	 */
	public boolean isSpecial() {
		return SecureMode.isSpecial(mode);
	}

	/**
	 * 设置服务器私钥，不允许空指针
	 * @param e ServerKey实例
	 */
	public void setServerKey(ServerKey e) {
		Laxkit.nullabled(e);
		serverKey = e;
	}

	/**
	 * 返回服务器私钥
	 * @return ServerKey实例
	 */
	public ServerKey getServerKey() {
		return serverKey;
	}

	/**
	 * 设置客户机公钥，不允许空指针
	 * @param e ClientKey
	 */
	public void setClientKey(ClientKey e) {
		Laxkit.nullabled(e);
		clientKey = e;
	}

	/**
	 * 返回客户机公钥
	 * @return
	 */
	public ClientKey getClientKey() {
		return clientKey;
	}

	/**
	 * 返回网段数目
	 * @return 整型值的网段数目
	 */
	public int size() {
		return ranges.size();
	}

	/**
	 * 保存一个地址范围
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public boolean add(Address begin, Address end) {
		if (begin.compareTo(end) > 0) {
			throw new IllegalValueException("%s > %s", begin, end);
		}
		return add(new SecureRange(begin, end));
	}

	/**
	 * 保存一个地址范围
	 * @param e 地址范围实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(SecureRange e) {
		Laxkit.nullabled(e);
		// 保存
		return ranges.add(e);
	}

	/**
	 * 保存一组地址
	 * @param a 地址数组
	 * @return 新增成员数目
	 */
	public int addAll(Collection<SecureRange> a) {
		int size = ranges.size();
		for (SecureRange e : a) {
			add(e);
		}
		return ranges.size() - size;
	}

	/**
	 * 返回地址范围
	 * @return SecureRange列表
	 */
	public List<SecureRange> list() {
		return new ArrayList<SecureRange>(ranges);
	}
	
	/**
	 * 判断一个地址在指定范围中
	 * @param address IP地址
	 * @return 返回真或者假
	 */
	public boolean contains(Address address) {
		// 支持所有地址
		if (isCommon()) {
			return true;
		}
		// 检查在配置中指定的地址范围，判断这个IP地址在它们之中
		for (SecureRange range : ranges) {
			if (range.contains(address)) {
				return true;
			}
		}
		// 以上不成立，返回假
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SecureToken) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SecureToken that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(name, that.name);
	}

}


//	/**
//	 * 返回网段数目
//	 * @return 整型值的网段数目
//	 */
//	public int size() {
//		super.lockMulti();
//		try {
//			return serverKey.size();
//		} finally {
//			super.unlockMulti();
//		}
//	}
//
//	/**
//	 * 保存一个地址范围
//	 * @param begin 开始位置
//	 * @param end 结束位置
//	 */
//	public boolean add(Address begin, Address end) {
//		if (begin.compareTo(end) > 0) {
//			throw new IllegalValueException("%s > %s", begin, end);
//		}
//		return add(new SecureRange(begin, end));
//	}
//
//	/**
//	 * 保存一个地址范围
//	 * @param e 地址范围实例
//	 * @return 成功返回真，否则假
//	 */
//	public boolean add(SecureRange e) {
//		Laxkit.nullabled(e);
//
//		// 锁定保存
//		super.lockSingle();
//		try {
//			return serverKey.add(e);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//
//	/**
//	 * 返回地址范围
//	 * @return SecureRange列表
//	 */
//	public List<SecureRange> list() {
//		super.lockMulti();
//		try {
//			return new ArrayList<SecureRange>(serverKey.list());
//		} finally {
//			super.unlockMulti();
//		}
//	}
//
//	/**
//	 * 判断一个地址在指定范围中
//	 * @param address IP地址
//	 * @return 返回真或者假
//	 */
//	public boolean contains(Address address) {
//		super.lockMulti();
//		try {
//			// 如果是公用类型，适用所有地址，返回真
//			if (isCommon()) {
//				return true;
//			}
//			// 检查在配置中指定的地址范围，判断这个IP地址在它们之中
//			return serverKey.contains(address);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//		// 以上不成立，返回假
//		return false;
//	}

///**
// * 判断一个地址在指定范围中
// * @param address IP地址
// * @return 返回真或者假
// */
//public boolean contains(Address address) {
//	// 如果是公用类型，适用所有地址，返回真
//	if (isCommon()) {
//		return true;
//	}
//	// 检查在配置中指定的地址范围，判断这个IP地址在它们之中
//	return ranges.contains(address);
//}