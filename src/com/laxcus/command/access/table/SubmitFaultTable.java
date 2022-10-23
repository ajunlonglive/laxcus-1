/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 提交故障表命令。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 06/26/2019
 * @since laxcus 1.0
 */
public class SubmitFaultTable extends Command {
	
	private static final long serialVersionUID = -7875742262322642113L;
	
	/** 本地节点 **/
	private Node site;

	/** 故障表集合 **/
	private TreeSet<FaultTable> array = new TreeSet<FaultTable>();

	/**
	 * 构造默认的提交故障表命令。
	 */
	public SubmitFaultTable() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析提交故障表
	 * @param reader 可类化数据读取器
	 */
	public SubmitFaultTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的提交故障表命令，生成它的数据副本
	 * @param that SubmitFaultTable实例
	 */
	private SubmitFaultTable(SubmitFaultTable that) {
		super(that);
		this.site = that.site;
		array.addAll(that.array);
	}
	
	/**
	 * 设置站点地址
	 * @param e 站点实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 保存一个故障表
	 * @param e FaultTable实例
	 * @return 返回真或者假
	 */
	public boolean add(FaultTable e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}

	/**
	 * 保存一批数据表
	 * @param a 数据表集合
	 * @return 返回新增数目
	 */
	public int addAll(Collection<FaultTable> a) {
		int size = array.size();
		for (FaultTable e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回故障表列表
	 * @return FaultTable列表
	 */
	public List<FaultTable> list() {
		return new ArrayList<FaultTable>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回表名数目
	 * @return 表名数
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SubmitFaultTable duplicate() {
		return new SubmitFaultTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		writer.writeInt(array.size());
		for (FaultTable e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FaultTable e = new FaultTable(reader);
			array.add(e);
		}
	}

}
