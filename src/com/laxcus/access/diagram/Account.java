/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户账号<br>
 * 用户账号保存一个注册用户的所有私有数据，包括用户登录账号、数据库、表、三级操纵权限许可，以及数据库配置。<br><br>
 * 
 * 每个账号下拥有任意多个数据库，一个数据库下拥有任意多个表。<br>
 * 用户操作权限有三级：用户级、数据库级、数据表级(见Permit定义)。<br>
 * 
 * @author scott.liang
 * @version 1.3 4/13/2015
 * @since laxcus 1.0
 */
public final class Account extends Audit {

	private static final long serialVersionUID = -5837702920115645197L;

	/** 数据库名称 -> 数据库 **/
	private TreeMap<Fame, Schema> schemas = new TreeMap<Fame, Schema>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 全部数据库
		writer.writeInt(schemas.size());
		for (Schema e : schemas.values()) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 全部数据库
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Schema e = new Schema(reader);
			schemas.put(e.getFame(), e);
		}
	}

	/**
	 * 根据传入的账号实例，生成它的副本
	 * @param that  Account实例
	 */
	private Account(Account that) {
		super(that);
		schemas.putAll(that.schemas);
	}

	/**
	 * 构造一个默认的账号
	 */
	public Account() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析账号参数。首先构造默认参数，再进行解析。
	 * @param reader 可类化数据读取器
	 * @since 1.3
	 */
	public Account(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出账号参数
	 * @param reader 标记化读取器
	 */
	public Account(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 构造一个账号，同时指定它的注册用户名称
	 * @param user 用户账号
	 */
	public Account(User user) {
		this();
		setUser(user);
	}

	/**
	 * 返回全部数据库名称
	 * @return 数据库名称列表
	 */
	public List<Fame> getFames() {
		return new ArrayList<Fame>(schemas.keySet());
	}

	/**
	 * 返回全部数据表名记录
	 * @return 数据表名称列表
	 */
	public List<Space> getSpaces() {
		ArrayList<Space> array = new ArrayList<Space>();
		for (Schema e : schemas.values()) {
			array.addAll(e.getSpaces());
		}
		return array;
	}

	/**
	 * 返回数据库
	 * @return 数据库集合
	 */
	public List<Schema> getSchemas() {
		return new ArrayList<Schema>(schemas.values());
	}

	/**
	 * 返回全部数据表记录
	 * @return 数据表集合
	 */
	public List<Table> getTables() {
		ArrayList<Table> array = new ArrayList<Table>();
		for (Schema e : schemas.values()) {
			array.addAll(e.list());
		}
		return array;
	}

	/**
	 * 查找某个数据库下的全部数据库表
	 * @param fame 数据库命名
	 * @return 表名列表
	 */
	public List<Space> findSpaces(Fame fame) {
		Schema schema = schemas.get(fame);
		if(schema != null) {
			return schema.getSpaces();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#duplicate()
	 */
	@Override
	public Account duplicate() {
		return new Account(this);
	}

	/**
	 * 判断数据库存在
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean hasSchema(Fame fame) {
		return schemas.get(fame) != null;
	}

	/**
	 * 判断数据表存在（本处为专属表，由用户建立和删除，区别与共享表）
	 * @param space 表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space space) {
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			success = schema.contains(space);
		}
		return success;
	}

	/**
	 * 查找数据库
	 * @param fame 数据库名
	 * @return 返回数据库实例，或者空指针
	 */
	public Schema findSchema(Fame fame) {
		return schemas.get(fame);
	}

	/**
	 * 查找数据表
	 * @param space 表名
	 * @return 返回表实例，或者空指针
	 */
	public Table findTable(Space space) {
		Schema schema = schemas.get(space.getSchema());
		if (schema != null) {
			return schema.find(space);
		}
		return null;
	}

	/**
	 * 查找数据块尺寸
	 * @param space 表名
	 * @return 以字节为单位的数据块尺寸
	 */
	public int findChunkSize(Space space) {
		int size = -1;
		Schema schema = schemas.get(space.getSchema());
		if (schema != null) {
			size = schema.findChunkSize(space);
		}
		return size;
	}

	/**
	 * 设置数据块尺寸
	 * @param space 表名
	 * @param size 数据块尺寸
	 * @return 成功返回真，否则假
	 */
	public boolean setChunkSize(Space space, int size) {
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			success = schema.setChunkSize(space, size);
		}
		return success;
	}

	/**
	 * 建立数据表优化器
	 * @param time 定时触发时间
	 * @return 成功返回真，否则假。
	 */
	public boolean createSwitchTime(SwitchTime time) {
		Space space = time.getSpace();
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			success = schema.createSwitchTime(time);
		}
		return success;
	}

	/**
	 * 返回数据表优化触发器
	 * @param space 数据表名
	 * @return 返回数据表优化触发器实例
	 */
	public SwitchTime findSwitchTime(Space space) {
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			return schema.findSwitchTime(space);
		}
		return null;
	}

	/**
	 * 撤销数据优化器
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	public boolean dropSwitchTime(Space space) {
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			success = schema.dropSwitchTime(space);
		}
		return success;
	}

	/**
	 * 输出一个账号全部时间触发器
	 * @return 时间触发器列表
	 */
	public List<SwitchTime> getSwitchTimes() {
		ArrayList<SwitchTime> array = new ArrayList<SwitchTime>();
		Iterator<Map.Entry<Fame, Schema>> iterator = schemas.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Fame, Schema> entry = iterator.next();
			Schema schema = entry.getValue();
			array.addAll(schema.getSwitchTimes());
		}
		return array;
	}
	
	/**
	 * 统计时间触发器数目
	 * @return 时间触发器数目
	 */
	public int countSwitchTimes() {
		return getSwitchTimes().size();
	}

	/**
	 * 建立一个数据库配置，不允许同名数据库存在
	 * @param schema 数据库
	 * @return 成功返回真，否则假
	 */
	public boolean createSchema(Schema schema) {
		Fame fame = schema.getFame();
		boolean success = (schemas.get(fame) == null);
		if (success) {
			success = (schemas.put(fame, schema) == null);
		}
		return success;
	}

	/**
	 * 删除一个数据库及其下属于的全部表
	 * @param fame 数据库名
	 * @return 返回被删除的数据库
	 */
	public Schema dropSchema(Fame fame) {
		return schemas.remove(fame);
	}

	/**
	 * 保存一个数据表。前提是数据库必须存在
	 * @param table 数据表实例
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean createTable(Table table) {
		Space space = table.getSpace();
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			success = schema.add(table);
		}
		return success;
	}

	/**
	 * 根据数据表名，删除一个数据表
	 * @param space 数据表名
	 * @return 返回表配置实例，或者空指针。
	 */
	public Table dropTable(Space space) {
		Schema schema = schemas.get(space.getSchema());
		boolean success = (schema != null);
		if (success) {
			return schema.remove(space);
		}
		return null;
	}


	//	public static void main(String[] a) {
	//		Account account = new Account();
	//		ArrayList<PassiveItem> all = new ArrayList<PassiveItem>();
	//		PassiveItem e = new PassiveItem( SHAUser.doUsername("PENTIUM"), new Space("Media", "Music") );
	//		all.add(e);
	//		int count = account.addPassiveItems(all);
	//		System.out.printf("add count:%d", count);
	//	}


	//	public static void main(String[] s) {
	//		Account a = new Account();
	//		Control c = new Control();
	//		c.add(ControlOption.CREATE_SCHEMA);
	//		c.add(ControlOption.CREATE_TABLE);
	//		c.add(ControlOption.CREATE_USER);
	////		c.add(Control.ALL);
	//		UserPermit permit = new UserPermit();
	//		permit.add(c);
	//		a.grant(permit);
	//
	//		System.out.printf("create schema is %s\n", a.canCreateSchema());
	//	}
}