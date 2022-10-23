/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.security.*;
import java.security.interfaces.*;
import java.util.regex.*;
import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;

/**
 * 建立密钥令牌解析器 <BR><BR>
 * 
 * 格式: CREATE SECURE TOKEN [参数] TO [ 节点地址 ]
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class CreateSecureTokenParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CREATE\\s+SECURE\\s+TOKEN)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s([\\w\\W]+?)\\s*$";

	/** -SECURE 安全加密通信 **/
	private final static String NAME = "^\\s*(?i)(?:-NAME|-N)\\s+(?i)([\\w\\W]+?)(\\s*|\\s+.+)$";

	/** -SECURE 安全加密通信 **/
	private final static String CHECK = "^\\s*(?i)(?:-CHECK|-C)\\s+(?i)(NONE|ADDRESS|CIPHER|DUPLEX)(\\s*|\\s+.+)$";

	/** -SECURE 安全加密通信 **/
	private final static String MODE = "^\\s*(?i)(?:-MODE|-M)\\s+(?i)(COMMON|SPECIAL)(\\s*|\\s+.+)$";

	/** -SECURE 安全加密通信 **/
//	private final static String PASSWORD = "^\\s*(?i)(?:-PASSWORD|-PWD|-P)\\s+(?i)([\\w\\W]+?)(\\s*|\\s+.+)$";
	
	/** 密码 **/
	private final static String PASSWORD = "^\\s*(?i)(?:-PASSWORD|-PWD|-P)\\s+\\'(?i)([\\w\\W]+?)\\'(\\s*|\\s+.+)$";

	/** -SECURE 安全加密通信 **/
	private final static String KEYSIZE = "^\\s*(?i)(?:-KEYSIZE|-KS)\\s+(?i)([1-9][0-9]*)(\\s*|\\s+.+)$";

	/** -ADDRESS 地址配置 **/
	private final static String ADDRESS = "^\\s*(?i)(?:-ADDRESS|-A)\\s+(?i)([\\w\\W]+?)(?i)(\\s*|\\s+(?:-NAME|-N|-CHECK|-C|-MODE|-M|-PASSWORD|-PWD|-P|-KEYSIZE|-KS)[\\w\\W]+)$";


	/**
	 * 构造默认的建立密钥令牌解析器
	 */
	public CreateSecureTokenParser() {
		super();
	}

	/**
	 * 判断匹配“CREATE SECURE TOKEN”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("CREATE SECURE TOKEN", input);
		}
		Pattern pattern = Pattern.compile(CreateSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析地址
	 * @param input
	 * @return
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
	 * 解析地址范围
	 * @param input 输入语句
	 * @return 
	 * @throws UnknownHostException
	 */
	private List<SecureRange> splitRanges(String input) throws UnknownHostException {
		ArrayList<SecureRange> array = new ArrayList<SecureRange>();
		String[] items = input.split("\\s*\\;\\s*");
		for (String item : items) {
			SecureRange e = splitRange(item);
			array.add(e);
		}
		return array;
	}

	/**
	 * 解析参数
	 * @param cmd 命令
	 * @param input 输入语句
	 */
	private SecureTokenSlice splitParameters(String input) {
		SecureTokenSlice slice = new SecureTokenSlice();
		String password = null;
		int keysize = -1;
		String address = null;

		while (input.trim().length() > 0) {
			// -NAME参数
			Pattern pattern = Pattern.compile(CreateSecureTokenParser.NAME);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String name = matcher.group(1);
				slice.setName(name);
				input = matcher.group(2);
				continue;
			}

			// -CHECK参数
			pattern = Pattern.compile(CreateSecureTokenParser.CHECK);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				slice.setFamily(SecureType.translate(text));
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// -MODE参数
			pattern = Pattern.compile(CreateSecureTokenParser.MODE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				slice.setMode(SecureMode.translate(text));
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// -PASSWORD参数
			pattern = Pattern.compile(CreateSecureTokenParser.PASSWORD);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				password = matcher.group(1);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// -KEYSIZE参数
			pattern = Pattern.compile(CreateSecureTokenParser.KEYSIZE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				keysize = Integer.parseInt( matcher.group(1));
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// -ADDRESS参数
			pattern = Pattern.compile(CreateSecureTokenParser.ADDRESS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				address = matcher.group(1);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// 错误
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// 参数不足
		if (slice.getName() == null) {
//			throwable(FaultTip.PARAM_MISSING_X, backup);
			throwableNo(FaultTip.PARAM_MISSING_X, "-NAME");
		}
		// 参数不足
		if (!SecureType.isFamily(slice.getFamily())) {
			throwableNo(FaultTip.PARAM_MISSING_X, "-CHECK");
		}
		// 参数不足
		if (!SecureMode.isMode(slice.getMode())) {
			throwableNo(FaultTip.PARAM_MISSING_X, "-MODE");
		}
		// 参数不足
		if (password == null) {
			throwableNo(FaultTip.PARAM_MISSING_X, "-PASSWORD");
		}
		if (keysize < 0) {
			throwableNo(FaultTip.PARAM_MISSING_X, "-KEYSIZE");
		}
		// 如果地址是空值，并且不是公共类型，那么是参数不足
		if (address == null) {
			if (!slice.isCommon()) {
				throwableNo(FaultTip.PARAM_MISSING_X, "-ADDRESS");
			}
		} else {
			try {
				List<SecureRange> ranges = splitRanges(address);
				slice.addRanges(ranges);
			} catch (Throwable e) {
				throwableNo(FaultTip.PARAM_MISSING_X, address);
			}
		}

		// 不能低于512位
		if (keysize < 512) { // || keysize % 8 !=0
			throwableNo(FaultTip.ILLEGAL_VALUE_X, address);
		}

		// 检查参数有效
		byte[] pwd = new UTF8().encode(password);

		// 生成RSA密钥
		try {
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keysize, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			// 密钥令牌
			slice.setPrivateStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));
			slice.setPublicStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));
		} catch (NoSuchAlgorithmException e) {
			throwableNo(FaultTip.FAILED_X, password);
		} catch (Throwable e) {
			throwableNo(FaultTip.FAILED_X, password);
		}

		return slice;
	}


	/**
	 * 解析“CREATE SECURE TOKEN”语句
	 * @param input 输入语句
	 * @return 返回CreateSecureToken命令
	 */
	public CreateSecureToken split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(CreateSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CreateSecureToken cmd = new CreateSecureToken();

		// 解析参数
		String prefix = matcher.group(1);
		String suffix = matcher.group(2);

		// 解析参数
		SecureTokenSlice slice = splitParameters( prefix);
		cmd.setSlice(slice);

		// 解析节点地址
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}