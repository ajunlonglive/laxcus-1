/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.io.*;
import java.security.interfaces.*;
import java.util.*;

import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FIXP通信密钥 <br><br>
 * 
 * FIXP通信密钥由对称加密/解密算法标识和密码组成。<br>
 * 
 * 它在客户机/服务器握手时，由客户机随机生成，投递到服务器，被客户机/服务器共同保存。<br>
 * FIXP通信密钥本身被RSA加密/解密，双方共同使用它，对随后传输的数据进行加密/解密。<br>
 * 
 * 客户机需要定时与服务器保持通信，这个通信的同时也激活FIXP密钥。<br>
 * 服务器上的密钥超时不使用（没有激活），将被服务器删除，这时服务器不通知客户机。<br>
 * 
 * 在通信过程是否加密数据，这个权利由服务端掌握，在FIXP密钥令牌配置。具体见com.laxcus.fixp.secure包中的定义。<br><br>
 * 
 * FIXP密钥的时效性：<br>
 * 密钥在FIXP服务器的保存时间默认是3分钟，也可以通过“local.xml/local-site/cipher-timeout”自主定义，
 * 超时不使用，密钥将被FIXP服务器删除。选择3分钟的原因是：UDP HELLO包在20秒有效，失效100秒；
 * TCP/UDP的命令/数据传输，超时都在3分钟以下，所以3分钟是一个靠谱的时间。<br><br>
 * 
 * 修改FIXP服务器密钥超时时间，可以通过命令：SET CIPHER TIMEOUT 来实现。<br>
 * 这个命令由WATCH站点投递给TOP/HOME/BANK站点，再分发给它们的下属站点。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/7/2015
 * @since laxcus 1.0
 */
public final class Cipher implements Classable, Serializable, Cloneable, Comparable<Cipher> {

	private static final long serialVersionUID = 8642964471473160184L;

	// 初始种子值
	private static long initSeed = 0;
	// 生成种子值
	static {
		ClassCode code = ClassCodeCreator.create(Cipher.class);
		Cipher.initSeed = code.getHigh() ^ code.getLow();
		Cipher.initSeed = Cipher.initSeed & 0x1FFFFFFFFFFFFFFFL;
	}

	/** 密文在服务器的超时时间，超时没有使用（激活），服务器将清除 **/
	private static long timeout = 180000;

	/**
	 * 设置服务器密钥超时时间。单位：毫秒
	 * @param ms 长整型的超时时间
	 * @return 新的超时时间
	 */
	public static long setTimeout(long ms) {
		if (ms >= 60000) {
			Cipher.timeout = ms;
		}
		return Cipher.timeout;
	}

	/**
	 * 返回服务器密钥超时时间
	 * @return 长整型的超时时间
	 */
	public static long getTimeout() {
		return Cipher.timeout;
	}

	/** 加密/解密算法  */
	private int family;

	/** 密码 */
	private byte[] pwd;

	/** 最后一次使用时间 **/
	private long refreshTime;
	
	/** 启用时间，构造时设置，以后不改变。 **/
	private long launchTime;

	/**
	 * 将FIXP通信密钥写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 参数
		writer.writeInt(family);
		// 密码
		writer.writeByteArray(pwd);
		// 刷新时间
		writer.writeLong(refreshTime);
		// 返回写入长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中读取FIXP通信密钥
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 参数
		family = reader.readInt();
		// 密码
		pwd = reader.readByteArray();
		// 刷新时间
		refreshTime = reader.readLong();
		// 返回解析字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的参数，生成它的数据副本
	 * @param that Cipher实例
	 */
	private Cipher(Cipher that) {
		this();
		setFamily(that.family);
		setPassword(that.pwd);
		refreshTime = that.refreshTime;
	}

	/**
	 * 构造一个默认的FIXP通信密钥
	 */
	public Cipher() {
		super();
		family = 0;
		refresh();
		// 定义启用时间
		launchTime = System.currentTimeMillis();
	}

	/**
	 * 构造FIXP通信密钥，指定它的加密/解密算法类型
	 * @param family 算法类型
	 */
	public Cipher(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 从可类化读取器中解析FIXP通信密钥
	 * @param reader  可类化读取器
	 * @since 1.1
	 */
	public Cipher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造FIXP通信密钥和设置它的参数
	 * @param family 算法类型
	 * @param pwd 密码字节数组
	 * @param off 密码字节数组的开始下标
	 * @param len 密码字节数组的有效长度
	 */
	public Cipher(int family, byte[] pwd, int off, int len) {
		this(family);
		setPassword(pwd, off, len);
	}

	/**
	 * 构造FIXP通信密钥和设置它的参数
	 * @param family 算法类型
	 * @param pwd 密码字节数组
	 */
	public Cipher(int family, byte[] pwd) {
		this(family);
		setPassword(pwd);
	}

	/**
	 * 构造FIXP通信密钥和设置它的参数
	 * @param family 算法的文本描述
	 * @param pwd 密码字节数组
	 * @param off 密码字节数组的开始下标
	 * @param len 密码字节数组的有效长度
	 */
	public Cipher(String family, byte[] pwd, int off, int len) {
		this(CipherTag.translate(family));
		setPassword(pwd, off, len);
	}

	/**
	 * 判断服务器端的密钥调用超时。<br>
	 * 当前时间减去最后一次调用时间，超过密钥最大等待时间，就是超时。
	 * 
	 * @return 返回真或者假
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis() - refreshTime >= Cipher.timeout;
	}

	/**
	 * 返回最后调用时间
	 * @return 长整型的系统时间
	 */
	public long getRefreshTime() {
		return refreshTime;
	}

	/**
	 * 更新使用时间
	 */
	public void refresh() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 返回密钥启用时间
	 * @return 以毫秒为单位的启用时间
	 */
	public long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 设置密钥算法类型
	 * @param who 算法类型
	 */
	public void setFamily(int who) {
		if (!CipherTag.isCipher(who)) {
			throw new IllegalValueException("illegal algorithm: %d", who);
		}
		family = who;
	}

	/**
	 * 返回密钥算法，见CliperTag定义
	 * @return 返回密钥算法类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 返回算法算法的文本描述
	 * @return 密钥算法文本描述
	 */
	public String getFamilyText() {
		return CipherTag.translate(family);
	}

	/**
	 * 设置密码
	 * @param b 密码字节数组
	 * @param off 密码字节数组的开始下标
	 * @param len 密码字节数组的有效长度
	 */
	public void setPassword(byte[] b, int off, int len) {
		pwd = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 设置密码
	 * @param b 密码字节数组
	 */
	public void setPassword(byte[] b) {
		setPassword(b, 0, b.length);
	}

	/**
	 * 返回密码
	 * @return 密码字节数组
	 */
	public byte[] getPassword() {
		return pwd;
	}

	/**
	 * 对传入的数据进行加密
	 * @param b  未加密的原始字节数组
	 * @param off  字节数组下标
	 * @param len  数据长度
	 * @return  返回加密后的字节数组
	 */
	public byte[] encrypt(byte[] b, int off, int len) throws SecureException {
		byte[] raws = null;

		if (CipherTag.isAES(family)) {
			raws = SecureEncryptor.aes(pwd, b, off, len);
		} else if (CipherTag.isDES(family)) {
			raws = SecureEncryptor.des(pwd, b, off, len);
		} else if (CipherTag.isDES3(family)) {
			raws = SecureEncryptor.des3(pwd, b, off, len);
		} else if (CipherTag.isBlowfish(family)) {
			raws = SecureEncryptor.blowfish(pwd, b, off, len);
		} else {
			throw new SecureException("illegal cipher: %d", family);
		}

		return raws;
	}

	/**
	 * 对传入的数据进行加密
	 * @param b  未加密的原始字节数组
	 * @return  返回加密后的字节数组
	 */
	public byte[] encrypt(byte[] b) throws SecureException {
		return encrypt(b, 0, b.length);
	}

	/**
	 * 对传入的数据进行解密
	 * @param b  已经加密的字节数组
	 * @param off  下标
	 * @param len  数据长度
	 * @return  返回解密后的字节数组
	 */
	public byte[] decrypt(byte[] b, int off, int len) throws SecureException {
		byte[] raws = null;

		if (CipherTag.isAES(family)) {
			raws = SecureDecryptor.aes(pwd, b, off, len);
		} else if (CipherTag.isDES(family)) {
			raws = SecureDecryptor.des(pwd, b, off, len);
		} else if (CipherTag.isDES3(family)) {
			raws = SecureDecryptor.des3(pwd, b, off, len);
		} else if (CipherTag.isBlowfish(family)) {
			raws = SecureDecryptor.blowfish(pwd, b, off, len);
		} else {
			throw new SecureException("illegal cipher %d", family);
		}

		return raws;
	}
	
	/**
	 * 对传入的数据进行解密
	 * @param b 已经加密的字节数组
	 * @return 返回解密后的字节数组
	 */
	public byte[] decrypt(byte[] b) throws SecureException {
		return decrypt(b, 0, b.length);
	}

	/**
	 * 生成当前FIXP密钥的数据副本
	 * @return Cipher实例
	 */
	public Cipher duplicate() {
		return new Cipher(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (pwd != null) {
			return String.format("%s#%x", getFamilyText(), Arrays.hashCode(pwd));
		} else {
			return getFamilyText();
		}
	}

	/**
	 * 判断是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Cipher) that) == 0;
	}

	/**
	 * 返回哈希码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family;
	}

	/**
	 * 根据当前实例，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较参数排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cipher that) {
		// 排序时，空值排在前面
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(pwd, that.pwd);
		}
		return ret;
	}

	/**
	 * 用RSA公钥对密钥本身进行加密
	 * @param key RSA公钥
	 * @return 经过RSA公钥加密后的字节数组
	 * @throws SecureException
	 */
	public byte[] encase(RSAPublicKey key) throws SecureException {
		ClassWriter writer = new ClassWriter();

		// 密钥算法类型
		writer.writeInt(family);
		// 写入密码
		writer.writeByteArray(pwd);

		// 输出原始字节
		byte[] primitive = writer.effuse();
		// 用RSA公钥加密(公钥加密通常比私解解密少时间)
		return SecureEncryptor.rsa(key, primitive, 0, primitive.length);
	}

	/**
	 * 用RSA私钥对密钥本身进行解密。 <br>
	 * RSA私钥解密过程会消耗大量CPU资源，对于网络安全有保障的计算机集群内网来说，可选择地址安全方案，判断数据来源，避免密钥方案产生的加密/解密过程，减少计算消耗。
	 * 
	 * @param key RSA私钥
	 * @param encrypt 公钥加密后的密钥
	 * @param off 密钥开始下标
	 * @param len 密钥有效长度
	 * @return 用RSA私钥解密后的字节数组
	 * @throws SecureException
	 */
	public int decase(RSAPrivateKey key, byte[] encrypt, int off, int len) throws SecureException {
		/**
		 * 根据私钥对数据进行解密，还原为原始数据。 解密过程可能会非常耗时(RSA解密通常很消耗CPU)
		 **/
		byte[] primitive = SecureDecryptor.rsa(key, encrypt, off, len);

		ClassReader reader = new ClassReader(primitive);

		// 密钥算法类型
		family = reader.readInt();
		// 读对称密码
		pwd = reader.readByteArray();

		// 返回解密的长度
		return len;
	}
	
	/** 客户端密文长度，以字节计数 **/
	private volatile static int clientWidth = 16;

	/** 服务端密文长度，以字节计数 **/
	private volatile static int serverWidth = 32;

	/**
	 * 设置客户端以字节计数的密文长度
	 * 
	 * @param keysize 字节长度，必须大于0，小于等于128。
	 * @return 返回新定义的以字节计数的密文长度，设置失败返回-1。
	 */
	public static int setClientWidth(int keysize) {
		if (keysize > 0 && keysize <= 128) {
			return Cipher.clientWidth = keysize;
		}
		return -1;
	}

	/**
	 * 设置客户端以数位计数的密文长度，8位等于1个字节
	 * @param bits 数位长度
	 * @return 返回新定义的以数位计算的密文长度，设置返回返回-1。
	 */
	public static int setClientWidthWithBits(int bits) {
		if (bits > 8) {
			int who = bits / 8;
			if (bits % 8 != 0) {
				who++;
			}
			return Cipher.setClientWidth(who);
		}
		return -1;
	}
	
	/**
	 * 返回客户端密文长度
	 * @return 以字节计的密文长度
	 */
	public static int getClientWidth() {
		return Cipher.clientWidth;
	}

	/**
	 * 返回客户端以数位计数的密文长度，8位是1个字节
	 * @return 返回以数位计算的密文长度
	 */
	public static int getClientWidthWithBits() {
		return Cipher.clientWidth * 8;
	}

	/**
	 * 设置服务端以字节计数的密文长度
	 * 
	 * @param size 字节长度，必须大于0，小于等于128。
	 * @return 返回新定义的以字节计数的密文长度，设置失败返回-1。
	 */
	public static int setServerWidth(int size) {
		if (size > 0 && size <= 128) {
			return Cipher.serverWidth = size;
		}
		return -1;
	}

	/**
	 * 设置服务端以数位计数的密文长度，8位等于1个字节
	 * @param bits 数位长度
	 * @return 返回新定义的以数位计算的密文长度，设置返回返回-1。
	 */
	public static int setServerWidthWithBits(int bits) {
		if (bits > 8) {
			int who = bits / 8;
			if (bits % 8 != 0) {
				who++;
			}
			return Cipher.setServerWidth(who);
		}
		return -1;
	}
	
	/**
	 * 返回服务端密文长度
	 * @return 以字节计的密文长度
	 */
	public static int getServerWidth() {
		return Cipher.serverWidth;
	}

	/**
	 * 返回服务端以数位计数的密文长度，8位是1个字节
	 * @return 返回以数位计算的密文长度
	 */
	public static int getServerWidthWithBits() {
		return Cipher.serverWidth * 8;
	}

	/**
	 * 生成一个密钥，密钥算法从列表中随机选择。如果是客户端的调用，使用16字节，否则使用32字节
	 * @param client 来自客户端调用
	 * @return 返回密钥实例
	 */
	public static Cipher create(boolean client) {
		// 用种子值随机选择一个类型
		long seed = Cipher.initSeed + System.currentTimeMillis();
		Random rnd = new Random(seed);
		int who = CipherTag.random(rnd);
		// 生成密码
		byte[] pwd = new byte[client ? Cipher.clientWidth : Cipher.serverWidth];
		rnd.nextBytes(pwd);
		// 输出FIXP密钥
		return new Cipher(who, pwd);
	}

}