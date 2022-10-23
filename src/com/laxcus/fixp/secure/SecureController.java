/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.security.interfaces.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * RSA密钥令牌管理器 <br><br>
 * 
 * SecureController被FixpMonitor及子类、子类相关类调用。是在收到数据后，解密数据。
 * 
 * @author scott.liang
 * @version 1.0 11/19/2016
 * @since laxcus 1.0
 */
public class SecureController extends MutexHandler {

	/** 实例 **/
	private static SecureController selfHandle = new SecureController();

	/** 密钥令牌 **/
	private ArrayList<SecureToken> specialTokens = new ArrayList<SecureToken>();

	/** 公共密钥令牌。适用于指定地址之外的所有地址 **/
	private SecureToken defaultToken;

	/**
	 * 构造默认和私有的RSA密钥令牌管理器
	 */
	private SecureController() {
		super();
	}

	/**
	 * 返回RSA密钥令牌管理器实例
	 * 
	 * @return SecurityController实例
	 */
	public static SecureController getInstance() {
		return SecureController.selfHandle;
	}

	/**
	 * 返回参数
	 * @return
	 */
	public List<SecureToken> getSpecialTokens() {
		super.lockMulti();
		try {
			return new ArrayList<SecureToken>(specialTokens);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 返回默认值
	 * @return
	 */
	public SecureToken getDefaultToken() {
		return defaultToken;
	}

	/**
	 * 返回密钥令牌数目
	 * 
	 * @return 密钥令牌数目
	 */
	public int size() {
		super.lockMulti();
		try {
			return specialTokens.size() + (defaultToken != null ? 1 : 0);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 释放全部服务器密钥令牌
	 */
	public void clear() {
		super.lockSingle();
		try {
			specialTokens.clear();
			defaultToken = null;
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 输出全部密钥令牌
	 * @return SecureToken
	 */
	public List<SecureToken> list() {
		ArrayList<SecureToken> array = new ArrayList<SecureToken>();
		super.lockSingle();
		try {
			array.addAll(specialTokens);
			if (defaultToken != null) {
				array.add(defaultToken);
			}
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 根据命名，查找匹配的密钥令牌
	 * @param naming 命名
	 * @return 返回SecureToken对象，没有是空指针
	 */
	public SecureToken findToken(Naming naming) {
		super.lockMulti();
		try {
			// 查找指定的RSA密钥令牌
			for (SecureToken token : specialTokens) {
				if (Laxkit.compareTo(token.getName(), naming) == 0) {
					return token;
				}
			}
			// 查找公共密钥令牌
			if (defaultToken != null) {
				if (Laxkit.compareTo(defaultToken.getName(), naming) == 0) {
					return defaultToken;
				}
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据命名，查找匹配的密钥令牌
	 * @param name 名称
	 * @return 返回SecureToken对象，没有是空指针
	 */
	public SecureToken findToken(String name) {
		return findToken(new Naming(name));
	}

	/**
	 * 判断指定名称的密钥令牌存在
	 * @param naming 命名
	 * @return 真或者假
	 */
	public boolean hasToken(Naming naming) {
		return findToken(naming) != null;
	}

	/**
	 * 判断指定名称的密钥令牌存在
	 * @param name 名称
	 * @return 真或者假
	 */
	public boolean hasToken(String name) {
		return findToken(name) != null;
	}

	/**
	 * 根据命名，删除匹配的密钥令牌。
	 * 在节点中，密钥令牌必须保证最少一个；否则全部密钥令牌删除后，将无法实现安全通信！
	 * 
	 * @param naming 命名
	 * @return 返回真或者假
	 */
	public boolean removeToken(Naming naming) {
		super.lockSingle();
		try {
			// 查找指定的RSA密钥令牌，删除！
			for (SecureToken token : specialTokens) {
				if (Laxkit.compareTo(token.getName(), naming) == 0) {
					// 如果只有最后一个密钥令牌，不允许删除
					if (defaultToken == null && specialTokens.size() == 1) {
						return false;
					}
					// 可以删除
					specialTokens.remove(token);
					return true;
				}
			}
			// 如果是公共令牌，也要删除！
			if (defaultToken != null) {
				if (Laxkit.compareTo(defaultToken.getName(), naming) == 0) {
					// 如果除公共密钥令牌之外，没有其它密钥令牌，不允许删除！
					if (specialTokens.size() == 0) {
						return false;
					}
					// 清除公共密钥令牌
					defaultToken = null;
					return true;
				}
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 保存一个RSA密钥令牌
	 * @param token RSA密钥令牌
	 */
	public boolean add(SecureToken token) {
		// 不允许空指针
		Laxkit.nullabled(token);
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			if (token.isCommon()) {
				defaultToken = token;
			} else {
				// 先删除旧的，保存新的
				specialTokens.remove(token);
				specialTokens.add(token);
			}
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 查找客户机密钥
	 * @param client 客户机地址
	 * @return 返回PublicSecure实例，或者空指针
	 */
	public PublicSecure findPublicSecure(Address client) {
		super.lockMulti();
		try {
			// 返回指定的RSA密钥令牌
			for (SecureToken token : specialTokens) {
				if (token.contains(client)) {
					return new PublicSecure(token.getFamily(), token
							.getClientKey().getStripe());
				}
			}
			// 返回公共密钥令牌
			if (defaultToken != null) {
				return new PublicSecure(defaultToken.getFamily(), defaultToken
						.getClientKey().getStripe());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断允许某个IP地地址
	 * @param client 客户机主机
	 * @return 返回真或者假
	 */
	public boolean allow(Address client) {
		super.lockMulti();
		try {
			// 返回指定的RSA密钥令牌
			for (SecureToken token : specialTokens) {
				if (token.contains(client)) {
					return true;
				}
			}
			// 公共密钥令牌默认适配所有IP地址
			if (defaultToken != null) {
				return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	/**
	 * 根据客户机地址，找到它匹配的RSA密钥令牌
	 * @param client 客户机地址
	 * @return 服务器密钥令牌
	 */
	public SecureToken find(Address client) {
		super.lockMulti();
		try {
			// 返回指定的RSA密钥令牌
			for (SecureToken token : specialTokens) {
				if (token.contains(client)) {
					return token;
				}
			}
			// 公共密钥令牌有效，返回公共密钥令牌
			if (defaultToken != null) {
				return defaultToken;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据客户机地址，选择一个RSA私钥。
	 * RSA私钥用于解密
	 * 
	 * @param client 客户机地址
	 * @return 返回 RSAPrivateKey实例
	 */
	public RSAPrivateKey choice(Address client) {
		SecureToken key = find(client);
		if (key != null) {
			return key.getServerKey().getKey();
		}
		// 空值
		return null;
	}

	/**
	 * 根据客户机地址，判断这个地址无须校验
	 * @param client 客户机地址
	 * @return 返回真或者假
	 */
	public boolean isNone(Address client) {
		SecureToken token = find(client);
		return (token != null ? token.isNone() : false);
	}

	/**
	 * 根据客户机地址，判断这个地址采用地址校验
	 * @param client 客户机地址
	 * @return 返回真或者假
	 */
	public boolean isAddress(Address client) {
		SecureToken token = find(client);
		return (token != null ? token.isAddress() : false);
	}

	/**
	 * 根据客户机地址，判断这个地址采用密钥校验
	 * @param client 客户机地址
	 * @return 返回真或者假
	 */
	public boolean isCipher(Address client) {
		SecureToken token = find(client);
		return (token != null ? token.isCipher() : false);
	}

	/**
	 * 根据客户机地址，判断这个地址采用双重校验（地址校验和密钥校验）
	 * @param client 客户机地址
	 * @return 返回真或者假
	 */
	public boolean isDuplex(Address client) {
		SecureToken token = find(client);
		return (token != null ? token.isDuplex() : false);
	}

}