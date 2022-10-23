/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.rabbet.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * GATE辅助连接器集合。<br><br>
 * 
 * GATE辅助连接器集合是保存一批GATE辅助连接器。集合支持同步/非同步两种模式。
 * add、remove、list、contains、clear、size、isEmpty是非同步方法，push、drop、show、exists是同步方法。
 * 非同步方法的名称继承自java.util.Collection命名规范，同步方法的名称是自定义。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class GateRabbetSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 5759691680429498837L;
	
	/** GATE辅助连接器集合 */
	private TreeSet<GateRabbet> array = new TreeSet<GateRabbet>();

	/**
	 * 将GATE辅助连接器集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (GateRabbet e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析GATE辅助连接器集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			GateRabbet e = new GateRabbet(reader);
			array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的GATE辅助连接器集合实例，生成它的副本
	 * @param that GateRabbetSet实例
	 */
	private GateRabbetSet(GateRabbetSet that) {
		this();
		addAll(that);
	}

	/**
	 * 构造一个默认的GATE辅助连接器集合
	 */
	public GateRabbetSet() {
		super();
	}

	/**
	 * 构造GATE辅助连接器集合，保存GATE辅助连接器数组
	 * @param a GATE辅助连接器数组
	 */
	public GateRabbetSet(GateRabbet[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造GATE辅助连接器集合
	 * @param a GATE辅助连接器集合
	 */
	public GateRabbetSet(Collection<GateRabbet> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析GATE辅助连接器集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public GateRabbetSet(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 刷新时间
	 */
	public void refreshTime() {
		for (GateRabbet e : array) {
			e.refreshTime();
		}
	}
	
	/**
	 * 根据授权人签名，判断授权人存在
	 * @param authorizer 授权人签名
	 * @return 返回真或者假
	 */
	public boolean hasAuthorizer(Siger authorizer) {
		for (GateRabbet e : array) {
			if (Laxkit.compareTo(e.getAuthorizer(), authorizer) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 保存一个GATE辅助连接器
	 * @param e GATE辅助连接器
	 * @return 返回真或者假
	 */
	public boolean add(GateRabbet e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个GATE辅助连接器
	 * @param e GATE辅助连接器
	 * @return 返回真或者假
	 */
	public boolean remove(GateRabbet e) {
		return array.remove(e);
	}

	/**
	 * 保存一批保存GATE辅助连接器
	 * @param that SportSet实例
	 * @return 返回新增成员数目
	 */
	public int addAll(GateRabbetSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批GATE辅助连接器
	 * @param a GATE辅助连接器数组
	 * @return 已经保存的成员数目
	 */
	public int addAll(GateRabbet[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批GATE辅助连接器
	 * @param that GATE辅助连接器集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<GateRabbet> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断GATE辅助连接器存在
	 * @param e GATE辅助连接器
	 * @return 返回真或者假
	 */
	public boolean contains(GateRabbet e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 以非锁定方式输出全部GATE辅助连接器
	 * @return GateRabbet列表
	 */
	public List<GateRabbet> list() {
		return new ArrayList<GateRabbet>(array);
	}

	/**
	 * 输出全部GATE辅助连接器
	 * @return GateRabbet集合
	 */
	public Set<GateRabbet> set() {
		return array;
	}
	
	/**
	 * 清除全部GATE辅助连接器
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 统计GATE辅助连接器的数目
	 * @return GATE辅助连接器数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 检测集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 以非锁定方式输出全部GATE辅助连接器数组
	 * @return GATE辅助连接器数组
	 */
	public GateRabbet[] toArray() {
		GateRabbet[] a = new GateRabbet[array.size()];
		return array.toArray(a);
	}

	/**
	 * 检查两个GATE辅助连接器集合完全一致
	 * @param that 另一个集合
	 * @return 一致返回真，否则假。
	 */
	public boolean alike(GateRabbetSet that) {
		// 1. 参数有效
		boolean success = (that != null);
		// 2. 数目一致
		if (success) {
			success = (size() == that.size());
		}
		// 3. 检查每个表都匹配
		if (success) {
			int count = 0;
			for (GateRabbet e : that.array) {
				if (contains(e)) {
					count++;
				}
			}
			success = (count == size());
		}
		// 返回匹配结果
		return success;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return GateRabbetSet实例
	 */
	public GateRabbetSet duplicate() {
		return new GateRabbetSet(this);
	}

	/**
	 * 克隆当前GATE辅助连接器集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 以锁定方式增加一个GATE辅助连接器。不允许空指针或者GATE辅助连接器重叠的现象存在。
	 * @param e GATE辅助连接器
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(GateRabbet e) {
		boolean success = (e != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.add(e);
			}
		} catch (Throwable b) {
			Logger.fatal(b);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 *以锁定方式删除一个GATE辅助连接器。
	 * @param e GATE辅助连接器
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(GateRabbet e) {
		boolean success = (e != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.remove(e);
			}
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 以锁定方式增加一组GATE辅助连接器。每个GATE辅助连接器都是唯一的，不允许重叠现象存在。
	 * @param a GATE辅助连接器数组
	 * @return 返回增加的成员数
	 */
	public int push(GateRabbet[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组GATE辅助连接器
	 * @param a 运行GATE辅助连接器集合
	 * @return 返回增加的成员数
	 */
	public int push(Collection<GateRabbet> a) {
		int size = array.size();
		for (GateRabbet e : a) {
			push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组GATE辅助连接器。
	 * @param a GATE辅助连接器集合
	 * @return 返回删除的成员数目
	 */
	public int drop(Collection<GateRabbet> a) {
		int size = array.size();
		for (GateRabbet e : a) {
			drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断GATE辅助连接器存在
	 * @param e GATE辅助连接器
	 * @return 存在返回真，否则假
	 */
	public boolean exists(GateRabbet e) {
		boolean success = (e != null);
		super.lockMulti();
		try {
			if (success) {
				success = array.contains(e);
			}
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 以锁定方式输出全部GATE辅助连接器
	 * @return GATE辅助连接器集合
	 */
	public List<GateRabbet> show() {
		super.lockMulti();
		try {
			return new ArrayList<GateRabbet>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部GATE辅助连接器
	 * @return GATE辅助连接器数组
	 */
	public GateRabbet[] array() {
		super.lockMulti();
		try {
			GateRabbet[] all = new GateRabbet[array.size()];
			return array.toArray(all);
		} finally {
			super.unlockMulti();
		}
	}
}