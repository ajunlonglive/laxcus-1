/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ring;

import java.security.*;
import java.security.interfaces.*;

import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * RINT命令启动器
 * 
 * 启动配置
 * 1. 日志包
 * 2. FIXP包
 * 3. RING配置
 * 4. SOCKET地址
 * 5. ConfigParser解析
 * 6. RING命令和父类
 * 7. laxcus.util包
 * 
 * @author scott.liang
 * @version 1.0 9/30/2019
 * @since laxcus 1.0
 */
public class RingLauncher {

	/** 输入器 **/
	private RingInputter inputter;

	/**
	 * 构造实例
	 */
	public RingLauncher() {
		super();
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
	 * 生成默认的密钥令牌
	 */
	private boolean createDefaultSecureToken() {
		int keysize = 1024;

		// 生成密码
		byte[] pwd = createPassword();

		// 生成RSA密钥
		try {
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keysize, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			// 密钥令牌
			SecureToken token = new SecureToken("T0", SecureType.CIPHER, SecureMode.COMMON);
			// 服务器密钥
			ServerKey serverKey = new ServerKey();
			serverKey.setKey(privateKey);
			serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

			// 客户机密钥
			ClientKey clientKey = new ClientKey();
			clientKey.setKey(publicKey);
			clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

			// 设置RSA密钥
			token.setServerKey(serverKey);
			token.setClientKey(clientKey);

			// 保存密钥令牌
			SecureController.getInstance().add(token);
			return true;
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		}

		return false;
	}

	/**
	 * 从配置文件中找到本地站点地址，通知节点结束服务
	 * @param log 日志配置文件
	 * @return 成功返回真，否则假
	 */
	public boolean launch(String log) {
//		// 生成随机的非对称密钥
//		SecureTokenParser parser = new SecureTokenParser();
//		boolean success = parser.split(secure);
//		// 不成功，生成默认值
//		if (!success) {
//			success = createDefaultSecureToken();
//		}
		
		// 生成随机的非对称密钥
		boolean success = createDefaultSecureToken();
		if (!success) {
			System.out.println("cannot be build secure token!");
			return false;
		}

		// 加载日志配置
		success = Logger.loadXML(log);
		if (success) {
			success = Logger.loadService();
		}

		// 不成功，提示
		if (!success) {
			System.out.println("cannot be load log!");
		}

		// 判断，启动初始化
		if (success) {
			inputter = new RingInputter();
			// 初始化控制台界面
			success = inputter.initialize();
		}
		// 启动线程
		if (success) {
			success = inputter.start();
		}

		// 打印出错信息
		if (!success) {
			Logger.gushing();
		}
		return success;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		RingLauncher launcher = new RingLauncher();
		if (args.length == 2) {
//			String secure = args[0];
			String log = args[1];
			launcher.launch(log);
		}else if(args.length == 1) {
			String log = args[0];
			launcher.launch(log);
		} else {
			System.out.println("invalid!");
		}
	}
	
}