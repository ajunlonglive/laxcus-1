/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 隐性网络IP地址。<br><br>
 * 
 * 隐性网络IP地址避开“沙箱”的安全检查，做为转换成“Address”之前的过渡存在。兼容IPv4和IPv6格式。<br>
 * 
 * @author scott.liang
 * @version 1.0 01/13/2016
 * @since laxcus 1.0
 */
public final class ShadowAddress implements Classable, Markable, Serializable, Cloneable, Comparable<ShadowAddress> {

	private static final long serialVersionUID = -2943698218266996852L;

	/** TCP/IP v6 网络地址(128位)，兼容IPv4地址 **/
	protected long high, low;

	/** 网络地址散列码，隐藏指示出IPv4/IPv6 */
	protected int hash;

	/**
	 * 根据传入的隐性网络IP地址实例，生成它的数据副本
	 * @param that
	 */
	private ShadowAddress(ShadowAddress that) {
		this();
		high = that.high;
		low = that.low;
		hash = that.hash;
	}

	/**
	 * 构造一个默认的隐性网络IP地址。默认为IPv4通配符地址
	 */
	public ShadowAddress() {
		super();
		high = low = 0L;
		hash = 0;
	}

	/**
	 * 构造一个TCP/IP网络地址，指定地址
	 * @param inet InetAddress实例
	 */
	public ShadowAddress(InetAddress inet) {
		this();
		setInetAddress(inet);
	}

	/**
	 * 通过原始IP地址字节数组构造一个新对象
	 * @param addr InetAddress字节数组
	 * @throws UnknownHostException
	 */
	public ShadowAddress(byte[] addr) throws UnknownHostException {
		this();
		setAddress(addr);
	}

	/**
	 * 通过字符串描述的网络地址构造一个新对象
	 * @param input 字符串描述的IP地址或者域名主机地址
	 * @throws UnknownHostException
	 */
	public ShadowAddress(String input) throws UnknownHostException {
		this();
		setAddress(input);
	}

	/**
	 * 从可类化数据读取器中解析网络地址
	 * @param reader 可类化数据读取器
	 */
	public ShadowAddress(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出网络地址
	 * @param reader 标记化读取器
	 */
	public ShadowAddress(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 返回当前Address对象的深层副本
	 * @return Address实例
	 */
	public ShadowAddress duplicate() {
		return new ShadowAddress(this);
	}

	/**
	 * 128位的高位地址序列
	 * @return 长整型值
	 */
	protected long high() {
		return high;
	}

	/**
	 * 128位的低位地址序列
	 * 
	 * @return 长整型值
	 */
	protected long low() {
		return low;
	}

	/**
	 * 设置隐性网络IP地址的字节序列
	 * @param b 字节数组
	 */
	public void setAddress(byte[] b) throws UnknownHostException {
		// 设置参数
		int index = 0;
		long high64 = 0L, low64 = 0L;
		// 字节排序是高位在前低位在后
		if (b.length == 4) {
			for (int seek = 24; seek >= 0; seek -= 8) {
				long value = b[index++] & 0xFF;
				low64 |= (value << seek);
			}
			// 设置哈希码，最高位是0代表是IPV4地址
			hash = (int) (low64 & 0x7FFFFFFFL);
		} else if (b.length == 16) {
			for (int seek = 56; seek >= 0; seek -= 8) {
				long value = b[index++] & 0xFF;
				high64 |= (value << seek);
			}
			for (int seek = 56; seek >= 0; seek -= 8) {
				long value = b[index++] & 0xFF;
				low64 |= (value << seek);
			}
			// 设置哈希码，最高位是1代表IPV6地址
			hash = 1;
			hash <<= 31;
			hash |= (int) ((high64 ^ low64) & 0x7FFFFFFFL);
		} else {
			throw new UnknownHostException("illegal address");
		}

		// 赋值
		high = high64;
		low = low64;
	}

	/**
	 * 设置隐性网络IP地址
	 * @param e 隐性网络IP地址
	 */
	public void setInetAddress(InetAddress e) {
		// 设置地址
		try {
			setAddress(e.getAddress());
		} catch (UnknownHostException u) {
			Logger.error(u);
		}
	}

	/**
	 * 根据主机域名或者主机IP地址，设置隐性网络IP地址
	 * 
	 * @param input 主机域名或者IP地址
	 * @throws UnknownHostException
	 */
	public void setAddress(String input) throws UnknownHostException {
		// 设置地址
		if (input.matches(Address.REGEX_IPV4) || input.matches(Address.REGEX_IPV6)) {
			resolve(input);
		} else {
			setInetAddress(InetAddress.getByName(input));
		}
	}

	/**
	 * 返回隐性网络IP地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		byte[] b = bits();
		try {
			return InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {

		}
		return null;
	}
	
	/**
	 * 生成安全网络IP地址
	 * @return Address地址
	 */
	public Address getAddress() {
		return new Address(this);
	}

	/**
	 * 返回隐性网络IP地址的字节序列。
	 * IPv4返回4字节，IPv6返回16字节。采用BigEndian字序。
	 * @return 字节数组。
	 */
	public byte[] bits() {
		byte[] b = new byte[isIPv4() ? 4 : 16];
		int index = 0;
		if (b.length == 4) {
			for (int seek = 24; seek >= 0; seek -= 8) {
				b[index++] = (byte) ((low >>> seek) & 0xFF);
			}
		} else {
			for (int seek = 56; seek >= 0; seek -= 8) {
				b[index++] = (byte) ((high >>> seek) & 0xFF);
			}
			for (int seek = 56; seek >= 0; seek -= 8) {
				b[index++] = (byte) ((low >>> seek) & 0xFF);
			}
		}
		return b;
	}

	/**
	 * 哈希码最高位是0代表IPv4地址
	 * 
	 * @return 返回真或者假
	 */
	public boolean isIPv4() {
		return (hash >>> 31) == 0;
	}

	/**
	 * 哈希码最高位是1代表IPv6地址
	 * 
	 * @return 返回真或者假
	 */
	public boolean isIPv6() {
		return (hash >>> 31) == 1;
	}

	/**
	 * 判断是通配符地址(全0)
	 * 
	 * @return 返回真或者假
	 */
	public boolean isAnyLocalAddress() {
		return getInetAddress().isAnyLocalAddress();
	}

	/**
	 * 判断是自回路地址(127.0.0.1)
	 * 
	 * @return 返回真或者假
	 */
	public boolean isLoopbackAddress() {
		return getInetAddress().isLoopbackAddress();
	}

	/**
	 * 判断是设备自动配置地址(169.254.xxx.xxx)
	 * 
	 * @return 返回真或者假
	 */
	public boolean isLinkLocalAddress() {
		return getInetAddress().isLinkLocalAddress();
	}

	/**
	 * 判断是广播地址(224.xxx.xxx.xxx)
	 * 
	 * @return 返回真或者假
	 */
	public boolean isMulticastAddress() {
		return getInetAddress().isMulticastAddress();
	}

	/**
	 * 判断是内网地址
	 * 
	 * @return 返回真或者假
	 */
	public boolean isSiteLocalAddress() {
		return getInetAddress().isSiteLocalAddress();
	}

	/**
	 * 判断是公网地址
	 * 
	 * @return 返回真或者假
	 */
	public boolean isWideAddress() {
		InetAddress inet = getInetAddress();
		boolean b = inet.isAnyLocalAddress(); // 通配符地址
		if (!b) b = inet.isLoopbackAddress(); // 自回路地址
		if (!b) b = inet.isLinkLocalAddress(); // 机器自分配地址
		if (!b) b = inet.isSiteLocalAddress(); // 内网地址
		// 广播地址
		if (!b) b = inet.isMulticastAddress();
		if (!b) b = inet.isMCGlobal();
		if (!b) b = inet.isMCLinkLocal();
		if (!b) b = inet.isMCNodeLocal();
		if (!b) b = inet.isMCOrgLocal();
		if (!b) b = inet.isMCSiteLocal();
		// 以上不成立，是公网地址
		return !b;
	}

	/**
	 * 比较在输入的地址数组里，是否有匹配的存在
	 * 
	 * @param inputs 输入地址数组
	 * @return 返回真或者假
	 */
	public boolean matchsIn(ShadowAddress[] inputs) {
		for(int i = 0; inputs != null && i < inputs.length; i++) {
			if (compareTo(inputs[i]) == 0) return true;
		}
		return false;
	}

	/**
	 * 判断两个隐性网络IP地址一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ShadowAddress.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ShadowAddress) that) == 0;
	}

	/**
	 * 返回隐性网络IP地址的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/**
	 * 返回隐性网络IP地址的字符串格式
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		if (isIPv4()) {
			for (int seek = 24; seek >= 0; seek -= 8) {
				if (buff.length() > 0) buff.append(".");
				buff.append(String.format("%d", ((low >> seek) & 0xFFL)));
			}
		} else {
			for (int seek = 48; seek >= 0; seek -= 16) {
				if (buff.length() > 0) buff.append(":");
				buff.append(String.format("%X", ((high >> seek) & 0xFFFFL)));
			}
			for (int seek = 48; seek >= 0; seek -= 16) {
				if (buff.length() > 0) buff.append(":");
				buff.append(String.format("%X", ((low >> seek) & 0xFFFFL)));
			}
		}
		return buff.toString();
	}

	/**
	 * 返回当前Address对象的深层副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 无符号比较值大小
	 * @param n1
	 * @param n2
	 * @return
	 */
	private final int compareTo(long n1, long n2) {
		int ret = 0;
		if (n1 != n2) {
			for (int shift = 32; shift >= 0; shift -= 32) {
				long v1 = ((n1 >>> shift) & 0xFFFFFFFFL);
				long v2 = ((n2 >>> shift) & 0xFFFFFFFFL);
				ret = Laxkit.compareTo(v1, v2);
				if (ret != 0) break;
			}
		}
		return ret;
	}

	/**
	 * 比较两个隐性网络IP地址的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShadowAddress that) {
		// 空对象排在前面，当前非空对象排在后面
		if (that == null) {
			return 1;
		}
		// 比较高位
		int ret = compareTo(high, that.high);
		// 比较低位
		if (ret == 0) {
			ret = compareTo(low, that.low);
		}
		return ret;
	}

	/**
	 * 将隐性网络IP地址参数写入可类化数据存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeLong(high);
		writer.writeLong(low);
		writer.writeInt(hash);
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析隐性网络IP地址参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		high = reader.readLong();
		low = reader.readLong();
		hash = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 将隐性网络IP地址输出为字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从字节数组里解析隐性网络IP地址，返回解析的长度
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 解析隐性网络IP地址
	 * @param input 地址字符串
	 * @throws UnknownHostException
	 */
	public void resolve(String input) throws UnknownHostException {
		// IPv4地址格式
		Pattern pattern = Pattern.compile(Address.REGEX_IPV4);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			byte[] b = new byte[4];
			for (int i = 0; i < b.length; i++) {
				String s = matcher.group(i + 1);
				int value = Integer.parseInt(s);
				if (value > 0xFF) {
					throw new UnknownHostException("ip address out!");
				}
				b[i] = (byte) (value & 0xFF);
			}
			setAddress(b);
			return;
		}
		// IPv6地址格式
		pattern = Pattern.compile(Address.REGEX_IPV6);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			byte[] b = new byte[16];
			int seek = 0;
			for (int index = 1; index <= 8; index++) {
				String s = matcher.group(index);
				int value = Integer.parseInt(s, 16) & 0xFFFF;
				b[seek++] = (byte) ((value >>> 8) & 0xFF);
				b[seek++] = (byte) (value & 0xFF);
			}
			setAddress(b);
			return;
		}
		// 错误!
		throw new UnknownHostException("cannot resolve! "+input);
	}

	/**
	 * 统计当前主机绑定的所有隐性网络IP地址，返回这些地址集合
	 * 
	 * @return Address数组
	 */
	public static Address[] locales() {
		ArrayList<Address> a = new ArrayList<Address>();

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress inet = addresses.nextElement();
					a.add(new Address(inet));
				}
			}
		} catch (SocketException e) {
			Logger.error(e);
		}

		Address[] s = new Address[a.size()];
		return a.toArray(s);
	}

	/**
	 * 枚举当前计算机的全部网络地址
	 * @return 返回当前主机网络地址列表
	 */
	public static List<InetAddress> list() {
		ArrayList<InetAddress> a = new ArrayList<InetAddress>();
		try {
			Enumeration<NetworkInterface> frames = NetworkInterface.getNetworkInterfaces();
			while (frames.hasMoreElements()) {
				Enumeration<InetAddress> inets = frames.nextElement().getInetAddresses();
				while (inets.hasMoreElements()) {
					a.add(inets.nextElement());
				}
			}
		} catch (SocketException e) {
			Logger.error(e);
		}

		return a;
	}

	/**
	 * 判断传入的IP地址在本地计算机的IP地址集合中
	 * @param address 传入的地址
	 * @return 存在返回“真”，否则“假”。
	 */
	public static boolean contains(InetAddress address) {
		byte[] b1 = address.getAddress();
		List<InetAddress> list = Address.list();
		for (InetAddress e : list) {
			byte[] b2 = e.getAddress();
			if(Laxkit.compareTo(b1, b2) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断IP地址在当前集合中
	 * @param address Address实例
	 * @return 返回真或者假
	 */
	public static boolean contains(Address address) {
		return Address.contains(address.getInetAddress());
	}

	/**
	 * 返回通配符地址。优先选择IPV4格式地址
	 * @return 成功返回通配符地址，否则空值
	 */
	public static InetAddress getAnyLocalAddress() {
		return Address.getAnyLocalAddress(true);
	}

	/**
	 * 返回自回路地址。优先返回IPV4格式的自回路地址
	 * @return 成功返回自回路地址，否则空值
	 */
	public static InetAddress getLoopbackAddress() {
		return Address.getLoopbackAddress(true);
	}

	/**
	 * 返回机器分配地址。优先选择IPV4格式地址
	 * @return 成功返回机器分配地址，否则空值
	 */
	public static InetAddress getLinkLocalAddress() {
		return Address.getLinkLocalAddress(true);
	}

	/**
	 * 返回内网地址。总是返回列表中最开始的内网地址
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回地址，否则返回空指针。
	 */
	public static InetAddress getSiteLocalAddress(boolean ipv4) {
		List<InetAddress> a = Address.list();
		InetAddress scale = null;
		for (InetAddress inet : a) {
			if (inet.isSiteLocalAddress()) {
				if (inet.getClass() == Inet4Address.class && ipv4) {
					return inet;
				} else if (scale == null) {
					scale = inet;
				}
			}
		}
		return scale;
	}

	/**
	 * 返回公网地址。总是返回列表中排序最前面的公网地址
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回公网地址，否则返回空值
	 */
	public static InetAddress getSiteWideAddress(boolean ipv4) {
		List<InetAddress> a = Address.list();
		InetAddress scale = null;
		for (InetAddress inet : a) {
			if (inet.isAnyLocalAddress()) { // 通配符地址
			} else if (inet.isLoopbackAddress()) { // 自回路地址
			} else if (inet.isLinkLocalAddress()) { // 机器自分配地址
			} else if (inet.isMulticastAddress() || inet.isMCGlobal()
					|| inet.isMCLinkLocal() || inet.isMCNodeLocal()
					|| inet.isMCOrgLocal() || inet.isMCSiteLocal()) {
				// 广播地址
			} else if (inet.isSiteLocalAddress()) { // 内网地址
			} else { // 剩下是公网地址
				if (inet.getClass() == Inet4Address.class && ipv4) {
					return inet;
				} else if (scale == null) {
					scale = inet;
				}
			}
		}
		return scale;
	}

	/**
	 * 返回机器分配地址
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回机器分配地址，否则返回空值
	 */
	public static InetAddress getLinkLocalAddress(boolean ipv4) {
		List<InetAddress> a = Address.list();
		InetAddress scale = null;
		for (InetAddress inet : a) {
			if (inet.isLinkLocalAddress()) {
				if (inet.getClass() == Inet4Address.class && ipv4) {
					return inet;
				} else if (scale == null) {
					scale = inet;
				}
			}
		}
		return scale;
	}

	/**
	 * 返回自回路地址
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回自回路地址，否则返回空值
	 */
	public static InetAddress getLoopbackAddress(boolean ipv4) {
		List<InetAddress> a = Address.list();
		InetAddress scale = null;
		for (InetAddress inet : a) {
			if (inet.isLoopbackAddress()) {
				if (inet.getClass() == Inet4Address.class && ipv4) {
					return inet;
				} else if (scale == null) {
					scale = inet;
				}
			}
		}
		return scale;
	}

	/**
	 * 返回通配符地址
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回通朽符地址，否则返回空值
	 */
	public static InetAddress getAnyLocalAddress(boolean ipv4) {
		List<InetAddress> a = Address.list();
		InetAddress scale = null;
		for (InetAddress inet : a) {
			if (inet.isAnyLocalAddress()) {
				if (inet.getClass() == Inet4Address.class && ipv4) {
					return inet;
				} else if (scale == null) {
					scale = inet;
				}
			}
		}
		return scale;
	}

	/**
	 * 返回内网地址。优先选择IPV4格式地址
	 * @return 成功返回内网地址，否则空值
	 */
	public static InetAddress getSiteLocalAddress() {
		return Address.getSiteLocalAddress(true);
	}

	/**
	 * 返回公网地址。优先选择IPV4格式地址
	 * @return 成功返回公网地址，否则空值
	 */
	public static InetAddress getSiteWideAddress() {
		return Address.getSiteWideAddress(true);
	}

	/**
	 * 枚举当前计算机环境中的网络地址，返回一个最合适的网络地址。<br>
	 * 通常在运行环境中没有定义具体的本地网络地址，或者网络地址是自回路地址时使用。<br>
	 * 选择顺序：1.内网地址. 2.外网地址. 3. 机器自分配地址. 4. 自回路地址. 5. 通配符地址。<br>
	 * @param ipv4 优先选择IPV4格式地址
	 * @return 成功返回要求的地址，否则返回空值
	 */
	public static InetAddress select(boolean ipv4) {
		InetAddress e = Address.getSiteLocalAddress(ipv4);
		if (e == null) {
			e = Address.getSiteWideAddress(ipv4);
		}
		if (e == null) {
			e = Address.getLinkLocalAddress(ipv4);
		}
		if (e == null) {
			e = Address.getLoopbackAddress(ipv4);
		}
		if (e == null) {
			e = Address.getAnyLocalAddress(ipv4);
		}
		return e;
	}

	/**
	 * 枚举当前运行环境中的网络地址，返回其中一个最合适的。<br>
	 * 此方法通常在没有指定具体的本机地址时，或者网络地址是自回路地址(localhost/127.0.0.1)时使用。<br>
	 * 选择顺序：1.内网地址. 2.外网地址. 3. 机器分配地址. 4. 自回路地址. 5. 通配符地址。<br>
	 * 每一次的选择优先考虑IPV4格式地址。<br>
	 * @return 成功返回要求的地址，否则是空值。
	 */
	public static InetAddress select() {
		return Address.select(true);
	}
}