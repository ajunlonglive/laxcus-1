/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;
import com.laxcus.xml.*;

/**
 * RSA密钥令牌解析器 <br><br>
 * 
 * 分别解析服务器的私钥令牌和客户机的公钥令牌。
 * 解析的数据分别保存到SecurityController配置文档中。
 * 
 * @author scott.liang
 * @version 1.0 11/19/2016
 * @since laxcus 1.0
 */
public class SecureTokenParser {

	/**
	 * 构造默认的密钥令牌解析器
	 */
	public SecureTokenParser() {
		super();
	}

	/**
	 * 解析地址范围。在两个IP地址之间，用“-”或者“,”符号分隔。
	 * @param input 地址范围
	 * @return 返回SecureRange实例
	 * @throws UnknownHostException
	 */
	private SecureRange splitRange(String input) throws UnknownHostException {
		final String regex = "^\\s*([\\p{XDigit}\\.\\:]+)\\s*(?:[\\,\\-]{1})\\s*([\\p{XDigit}\\.\\:]+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throw new IllegalValueException("cannot be resolve %s" , input);
		}
		Address begin = new Address(matcher.group(1));
		Address end = new Address(matcher.group(2));
		return new SecureRange(begin, end);
	}

	/**
	 * 使用模值和私用指数，生成RSA私钥
	 * @param modulus 模
	 * @param exponent 私用指数
	 * @return RSAPrivateKey实例
	 * @throws SecureException
	 */
	private RSAPrivateKey createPrivateKey(String modulus, String exponent)
	throws SecureException {
		return SecureGenerator.buildRSAPrivateKey(modulus, exponent);
	}

	/**
	 * 使用模值和公用指数，生成RSA公钥
	 * @param modulus 模
	 * @param exponent 公用指数
	 * @return RSAPublicKey实例
	 * @throws SecureException
	 */
	private RSAPublicKey createPublicKey(String modulus, String exponent)
	throws SecureException {
		return SecureGenerator.buildRSAPublicKey(modulus, exponent);
	}

	/**
	 * 从ELEMENT中解析RSA参数
	 * @param element XML成员
	 * @return RSA参数数组
	 */
	private String[] splitKeyFromElement(Element element) {
		String modulus = XMLocal.getValue(element, SecureMark.MODULUS);
		String exponent = XMLocal.getValue(element, SecureMark.EXPONENT);
		return new String[] { modulus, exponent };
	}

	/**
	 * 从文件中解析RSA私钥
	 * @param filename
	 * @return RSAPrivateKey实例
	 * @throws SecureException
	 */
	private RSAPrivateKey splitPrivateKeyFromFile(String filename) throws SecureException {
		Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return null;
		}
		NodeList nodes = document.getElementsByTagName(SecureMark.CODE);
		if (nodes == null || nodes.getLength() == 0) {
			return null;
		}

		Element element = (Element) nodes.item(0);
		String[] params = splitKeyFromElement(element);
		return createPrivateKey(params[0], params[1]);
	}

	/**
	 * 从文件中解析RSA公钥
	 * @param filename
	 * @return RSAPublicKey
	 * @throws SecureException
	 */
	private RSAPublicKey splitPublicKeyFromFile(String filename) throws SecureException {
		Document document = XMLocal.loadXMLSource(filename);
		if(document == null) return null;
		NodeList nodes = document.getElementsByTagName(SecureMark.CODE);
		if (nodes == null || nodes.getLength() == 0) {
			return null;
		}

		Element element = (Element) nodes.item(0);
		String[] params = splitKeyFromElement(element);
		return createPublicKey(params[0], params[1]);
	}

	/**
	 * 解析地址范围
	 * @param element 成员
	 * @return 返回范围
	 */
	private List<SecureRange> splitRanges(Element element) {
		ArrayList<SecureRange> ranges = new ArrayList<SecureRange>();

		// 解析服务器受理的地址范围
		NodeList nodes = element.getElementsByTagName(SecureMark.RANGE);
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element sub = (Element) nodes.item(i);
			String content = sub.getTextContent();
			try {
				SecureRange range = splitRange(content);
				ranges.add(range);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		return ranges;
	}

	/**
	 * 生成随机密码
	 * @return 返回字节数组
	 */
	private byte[] createPassword() {
		ClassWriter writer = new ClassWriter();

		// 依赖类对象码，生成实例，做为密码处理
		ClassCode code = ClassCodeCreator.create(this, System.currentTimeMillis());
		writer.write(code.toBytes());
		// 生成随机数
		writer.writeLong(System.nanoTime());
		writer.writeLong(Runtime.getRuntime().maxMemory());
		writer.writeLong(Runtime.getRuntime().freeMemory());
		writer.writeLong(Runtime.getRuntime().totalMemory());
		writer.writeLong(Runtime.getRuntime().availableProcessors());

		return writer.effuse();
	}

	/**
	 * 自动生成RSA密钥，密码随机产生。
	 * @param token 
	 * @param element
	 * @return 成功返回真，否则假
	 */
	private boolean splitAutoKey(SecureToken token, Element element) {
		String keysize = XMLocal.getValue(element, SecureMark.KEYSIZE);
		if (keysize == null || keysize.trim().length() == 0) {
			Logger.error(this, "splitAutoKey", "cannot be find RSA KEYSIZE");
			return false;
		}
		// 如果不是数字，忽略它
		if (!ConfigParser.isInteger(keysize)) {
			Logger.error(this, "splitAutoKey", "invalid RSA KEYSIZE! %s", keysize);
			return false;
		}

		// 解析数位，保证最小512位
		int ks = ConfigParser.splitInteger(keysize, 512);
		if (ks < 512) {
			ks = 512;
		}

		// 生成RSA密钥
		boolean success = false;
		try {
			// 生成密码
			byte[] pwd = createPassword();
			// 设置值
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(ks, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			ServerKey serverKey = new ServerKey();
			serverKey.setKey(privateKey);
			serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

			ClientKey clientKey = new ClientKey();
			clientKey.setKey(publicKey);
			clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

			// 设置RSA密钥
			token.setServerKey(serverKey);
			token.setClientKey(clientKey);
			// 成功
			success = true;
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		return success;
	}

	/**
	 * 解析明文RSA密钥
	 * @param token
	 * @param element
	 * @return
	 * @throws SecureException
	 */
	private boolean splitTextKey(SecureToken token, Element element) {
		String password = XMLocal.getValue(element, SecureMark.PASSWORD);

		if (password == null || password.trim().length() == 0) {
			Logger.error(this, "splitTextKey", "cannot be find RSA PASSWORD");
			return false;
		}
		String keysize = XMLocal.getValue(element, SecureMark.KEYSIZE);
		if (keysize == null || keysize.trim().length() == 0) {
			Logger.error(this, "splitTextKey", "cannot be find RSA KEYSIZE");
			return false;
		}
		// 如果不是数字，忽略它
		if (!ConfigParser.isInteger(keysize)) {
			Logger.error(this, "splitTextKey", "invalid RSA KEYSIZE! %s", keysize);
			return false;
		}

		// 解析数位，保证最小512位
		int ks = ConfigParser.splitInteger(keysize, 512);
		if (ks < 512) {
			ks = 512;
		}

		// 生成RSA密钥
		boolean success = false;
		try {
			byte[] pwd = new UTF8().encode(password);
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(ks, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			ServerKey serverKey = new ServerKey();
			serverKey.setKey(privateKey);
			serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

			ClientKey clientKey = new ClientKey();
			clientKey.setKey(publicKey);
			clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

			// 设置RSA密钥
			token.setServerKey(serverKey);
			token.setClientKey(clientKey);
			// 成功
			success = true;
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		//		Logger.debug(this, "splitTextKey", "password: %s, public key:%s, private key:%s",
		//		password, publicKey.getModulus().toString(), privateKey.getModulus().toString());		

		//		if (success) {
		//			Logger.debug(this, "splitTextKey", "password: %s", password);
		//			Logger.debug(this, "splitTextKey", "Server Key: %s", token.getServerKey());
		//			Logger.debug(this, "splitTextKey", "Client Key: %s", token.getClientKey());
		//		}

		return success;
	}

	/**
	 * 解析代码段RSA密钥
	 * @param token
	 * @param ranges
	 * @param element
	 * @return
	 * @throws SecureException
	 */
	private boolean splitCodeKey(SecureToken token, Element element) throws SecureException {
		// RSA私钥
		NodeList nodes = element.getElementsByTagName(SecureMark.PRIVATE_KEY);
		if (nodes == null || nodes.getLength() != 1) {
			Logger.error(this, "splitCodeKey", "cannot be find RSA PRIVATE KEY");
			return false;
		}
		Element sub = (Element)nodes.item(0);
		String[] params = splitKeyFromElement(sub);
		RSAPrivateKey privateKey = createPrivateKey(params[0], params[1]);
		if (privateKey == null) {
			return false;
		}
		ServerKey serverKey = new ServerKey();
		serverKey.setKey(privateKey);
		serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

		// RSA公钥
		nodes = element.getElementsByTagName(SecureMark.PUBLIC_KEY);
		if (nodes == null || nodes.getLength() != 1) {
			Logger.error(this, "splitCodeKey", "cannot be find RSA PUBLIC KEY");
			return false;
		}
		sub = (Element)nodes.item(0);
		params = splitKeyFromElement(sub);
		RSAPublicKey publicKey = createPublicKey(params[0], params[1]);
		if (publicKey == null) {
			return false;
		}
		ClientKey clientKey = new ClientKey();
		clientKey.setKey(publicKey);
		clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

		// 设置RSA密钥
		token.setServerKey(serverKey);
		token.setClientKey(clientKey);

		return true;
	}

	/**
	 * 解析文件中的RSA密钥
	 * @param token
	 * @param ranges
	 * @param element
	 * @return
	 * @throws SecureException
	 */
	private boolean splitFileKey(SecureToken token, Element element) throws SecureException {
		// RSA私钥，用于服务器
		String filename = XMLocal.getValue(element, SecureMark.PRIVATE_KEY);
		boolean success = (filename != null && filename.length() > 0);
		if (!success) {
			return false;
		}
		filename = ConfigParser.splitPath(filename);
		RSAPrivateKey privateKey = splitPrivateKeyFromFile(filename);
		if (privateKey == null) {
			return false;
		}
		ServerKey serverKey = new ServerKey();
		serverKey.setKey(privateKey);
		serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

		// RSA公钥，保存在服务器，分发到客户机
		filename = XMLocal.getValue(element, SecureMark.PUBLIC_KEY);
		success = (filename != null && filename.length() > 0);
		if (!success) {
			return false;
		}
		filename = ConfigParser.splitPath(filename);
		RSAPublicKey publicKey = splitPublicKeyFromFile(filename);
		ClientKey clientKey = new ClientKey();
		clientKey.setKey(publicKey);
		clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

		// 设置RSA密钥
		token.setServerKey(serverKey);
		token.setClientKey(clientKey);

		return true;
	}

	/**
	 * 生成令牌名称
	 * @return 新的名称，且不存在
	 */
	private String createTokenName() {
		int index = 1;
		while (true) {
			String name = String.format("Token%d", index++);
			if (!SecureController.getInstance().hasToken(name)) {
				return name;
			}
		}
	}

	/**
	 * 解析密钥令牌
	 * @param element
	 * @return 返回SecureToken，失败是空指针
	 * @throws SecureException 
	 */
	private SecureToken splitToken(Element element) throws SecureException {
		SecureToken token = new SecureToken();

		// "name"属性，没有生成一个
		String name = element.getAttribute(SecureMark.NAME_ATTRIBUTE);
		if (name == null || name.trim().isEmpty()) {
			name = createTokenName();
		}
		token.setName(new Naming(name));

		// “check”和“mode”属性
		String check = element.getAttribute(SecureMark.CHECK_ATTRIBUTE);
		token.setFamily(SecureType.translate(check));
		String mode = element.getAttribute(SecureMark.MODE_ATTRIBUTE);
		token.setMode(SecureMode.translate(mode));

		// 地址范围
		List<SecureRange> ranges = splitRanges(element);
		// 如果没有地址范围，并且不是“common”，忽略它
		if (ranges.isEmpty() && !token.isCommon()) {
			return null;
		}
		token.addAll(ranges);

		// RSA KEY
		NodeList nodes = element.getElementsByTagName(SecureMark.KEY);
		if (nodes == null || nodes.getLength() != 1) {
			return null;
		}
		Element sub = (Element) nodes.item(0);

		// 文件
		nodes = sub.getElementsByTagName(SecureMark.FILE);
		boolean success = (nodes != null && nodes.getLength() == 1);
		if (success) {
			sub = (Element) nodes.item(0);
			success = splitFileKey(token, sub);
			return (success ? token : null);
		}

		// 代码段
		nodes = sub.getElementsByTagName(SecureMark.CODE);
		success = (nodes != null && nodes.getLength() == 1);
		if (success) {
			sub = (Element) nodes.item(0);
			success = splitCodeKey(token, sub);
			return (success ? token : null);
		}

		// 明文段
		nodes = sub.getElementsByTagName(SecureMark.TEXT);
		success = (nodes != null && nodes.getLength() == 1);
		if (success) {
			sub = (Element) nodes.item(0);
			success = splitTextKey(token, sub);
			return (success ? token : null);
		}

		// 自动生成RSA密钥
		nodes = sub.getElementsByTagName(SecureMark.AUTO);
		success = (nodes != null && nodes.getLength() == 1);
		if (success) {
			sub = (Element) nodes.item(0);
			success = splitAutoKey(token, sub);
			return (success ? token : null);
		}

		// 经典密钥机

		// 量子密钥机

		return null;
	}

	/**
	 * 从XML文档中解析密钥令牌
	 * @param document XML文档
	 * @return 解析成功返回真，否则假
	 */
	private boolean split(Document document) {
		// 解析服务器令牌
		NodeList nodes = document.getElementsByTagName(SecureMark.TOKEN);
		int size = nodes.getLength();
		for (int index = 0; index < size; index++) {
			Element element = (Element) nodes.item(index);
			// 解析密钥令牌
			try {
				SecureToken token = splitToken(element);
				if (token == null) {
					return false;
				}
				SecureController.getInstance().add(token);
			} catch (IOException e) {
				Logger.error(e);
				return false;
			}
		}

		// 判断加载成功
		return (SecureController.getInstance().size() > 0);
	}

	/**
	 * 从传入的XML内容中解析密钥令牌
	 * @param content XML内容
	 * @return 成功返回真，否则假
	 */
	public boolean split(byte[] content) {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return false;
		}
		return split(document);
	}

	/**
	 * 解析一个网络通讯安全配置文件
	 * @param file FIXP安全配置文件
	 * @return 解析成功返回真，否则假
	 */
	public boolean split(File file) {
		Document document = XMLocal.loadXMLSource(file);
		if (document == null) {
			return false;
		}
		return split(document);
	}

	/**
	 * 解析一个网络通讯安全配置文件
	 * @param filename FIXP安全配置文件
	 * @return 解析成功返回真，否则假
	 */
	public boolean split(String filename) {
		filename = ConfigParser.splitPath(filename);
		return split(new File(filename));
	}

	public static void main(String[] args) {
		String filename = "e:/parallel/tokens.xml";
		filename = "c:/s3.xml";
		filename = "e:/parallel/safe.xml";
		filename = "j:/security.xml";
		SecureTokenParser parser = new SecureTokenParser();
		boolean s = parser.split(filename);
		System.out.println("result is " + s);

		System.out.printf("tokens size:%d\n", SecureController.getInstance().size());

		SecureTokenBuilder st = new SecureTokenBuilder();
		byte[] b = st.buildTokens();
		System.out.println(new String(b));
	}

}