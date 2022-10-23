/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 多用户签名命令。<br>
 * 
 * 保存用户签名的SHA256码和对应的明文。
 * 明文不做可类化处理，只在本地保存。
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public abstract class MultiUser extends Command {

	private static final long serialVersionUID = 4274567979778813797L;

	/** 注册用户账号集合 **/
	private ArrayList<Siger> users = new ArrayList<Siger>();

	/** 用户明文。不做可类化处理，不在网络间传输 **/
	private TreeSet<Naming> texts = new TreeSet<Naming>();

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 注册成员
		writer.writeInt(users.size());
		for (Siger e : users) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 注册成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			users.add(e);
		}
	}

	/**
	 * 构造多用户签名命令
	 */
	protected MultiUser() {
		super();
	}

	/**
	 * 根据传入的多用户签名命令，生成它的数据副本
	 * @param that MultiUser实例
	 */
	protected MultiUser(MultiUser that) {
		super(that);
		users.addAll(that.users);
		texts.addAll(that.texts);
	}

	/**
	 * 增加一个被授权或者撤消的用户名
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addUser(Siger e) {
		Laxkit.nullabled(e);
		// 保存签名
		boolean success = (!users.contains(e));
		if (success) {
			success = users.add(e);
		}
		return success;
	}

	/**
	 * 增加一个被授权或者撤消的用户名(用户名是明文，这里不需要密码)
	 * @param name 用户明文
	 * @return 保存成功返回真，否则假
	 */
	public boolean addUser(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		// 保存明文
		addPlainText(name);
		// 保存签名
		return addUser(SHAUser.doUsername(name));
	}

	/**
	 * 设置一组用户账号
	 * @param names 用户明文
	 * @return 返回增加成员数目
	 */
	public int setUsers(String[] names) {
		int size = users.size();
		for (int i = 0; names != null && i < names.length; i++) {
			addUser(names[i]);
		}
		return users.size() - size;
	}

	/**
	 * 输出全部被授权或者撤消授权的账号名称
	 * 
	 * @return 返回Siger列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(users);
	}

	/**
	 * 返回账号数目
	 * @return 账号数目
	 */
	public int getUserSize() {
		return users.size();
	}
	
	/**
	 * 判断包含指定的用户签名
	 * @param e 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger e) {
		return e != null && users.contains(e);
	}

	/**
	 * 保存一个用户明文
	 * @param e 字符串
	 * @return 保存成功返回真，否则假
	 */
	public boolean addPlainText(String e) {
		Laxkit.nullabled(e);
		
		return texts.add(new Naming(e));
	}

	/**
	 * 根据SHA256签名，查找匹配的明文
	 * @param hash 用户签名
	 * @return 返回用户明文，没有返回SHA256签名
	 */
	public String findPlainText(Siger hash) {
		for (Naming e : texts) {
			String name = e.toString();
			// 判断是SHA256码，或者明文
			if (Siger.validate(name)) {
				if (Laxkit.compareTo(name, hash.getHex(), false) == 0) {
					return name;
				}
			} else {
				Siger that = SHAUser.doUsername(name);
				if (that.compareTo(hash) == 0) {
					return name;
				}
			}
		}
		return hash.toString();
	}

	/**
	 * 判断有明文
	 * @param hash SHA256散列码
	 * @return 返回真或者假
	 */
	public boolean hasPlainText(Siger hash) {
		// 取出已经有的明文，逐个判断
		for (Naming e : texts) {
			String name = e.toString();
			// 如果是SHA256码，判断16进制符一致。如果是明文，转成签名判断一致！
			if (Siger.validate(name)) {
				if (Laxkit.compareTo(name, hash.getHex(), false) == 0) {
					return false;
				}
			} else {
				Siger that = SHAUser.doUsername(name);
				if (that.compareTo(hash) == 0) {
					return true;
				}
			}
		}
		// 以上不成立，返回假!
		return false;
	}
}