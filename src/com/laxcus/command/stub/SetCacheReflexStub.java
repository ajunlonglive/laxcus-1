/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 设置缓存映像块编号。<br><br>
 * 
 * 这个命令由DATA站点，通过HOME站点转发给CALL站点。CALL站点保存缓存映像块编号，为分布检索做准备。
 * 
 * @author scott.liang
 * @version 1.1 10/13/2015
 * @since laxcus 1.0
 */
public final class SetCacheReflexStub extends Command {

	private static final long serialVersionUID = -5291252510234873520L;
	
	/** 数据块 **/
	private TreeSet<CacheReflexStub> array = new TreeSet<CacheReflexStub>();

	/**
	 * 根据传入的设置缓存映像块编号，生成它的浅层数据副本
	 * @param that SetCacheReflexStub实例
	 */
	private SetCacheReflexStub(SetCacheReflexStub that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的设置缓存映像块编号
	 */
	public SetCacheReflexStub() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析设置缓存映像块编号命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SetCacheReflexStub(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存一组缓存映像块记录
	 * @param e 映像数据块
	 * @return 保存成功返回真，否则假。
	 */
	public boolean add(CacheReflexStub e) {
		if (e != null && !e.isEmpty()) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 保存一批缓存映像块记录
	 * @param a CacheReflexStub数组
	 * @return 返回保存的成员数
	 */
	public int addAll(Collection<CacheReflexStub> a) {
		int size = array.size();
		for (CacheReflexStub e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 返回全部缓存映像块记录
	 * @return CacheReflexStub列表
	 */
	public List<CacheReflexStub> list() {
		return new ArrayList<CacheReflexStub>(array);
	}

	/**
	 * 返回源站点地址
	 * @return Node地址
	 */
	public Node getFrom() {
		return this.getSource().getNode();
	}

	/**
	 * 输出数据块编号集合
	 * @return 数据块编号集合
	 */
	public List<Long> getStubs() {
		ArrayList<Long> a = new ArrayList<Long>();
		for (CacheReflexStub e : this.array) {
			a.addAll(e.getStubs());
		}
		return a;
	}

	/**
	 * 判断一个数据表名和数据块编号存在
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 存在返回真，否则假
	 */
	public boolean contains(Space space, long stub) {
		for (CacheReflexStub reflex : array) {
			if (reflex.contains(space, stub)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetCacheReflexStub duplicate() {
		return new SetCacheReflexStub(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(CacheReflexStub e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CacheReflexStub e = new CacheReflexStub(reader);
			array.add(e);
		}
	}

//		public static void main(String[] args) {
//			SetReflexCacheStub cmd = new SetReflexCacheStub();
//			ReflexCacheStub item = new ReflexCacheStub(new Space("media", "music"));
//			item.add(Long.MAX_VALUE);
//			item.add(Long.MIN_VALUE);
//			item.add(100L);
//			
////			cmd.add(item);
//			
//			byte[] b = cmd.build();
//			System.out.printf("size is %d\n", b.length);
//			
//			SetReflexCacheStub cmd2 = new SetReflexCacheStub(new ClassReader(b));
//			
//			System.out.println("okay!");
//		}

}