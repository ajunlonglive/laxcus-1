/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.command.rebuild.*;
import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列存储表最大压缩倍数处理报告。<br>
 * 这个命令只能由管理员或者等同管理员身份的用户，通过FRONT节点发起操作。
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class SetMaxDSMReduceProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 2306595194741454574L;

	/** 执行单元 **/
	private TreeSet<TissItem> array = new TreeSet<TissItem>();

	/**
	 * 根据传入的列存储表最大压缩倍数处理报告实例，生成它的数据副本
	 * @param that SetMaxDSMReduceProduct实例
	 */
	private SetMaxDSMReduceProduct(SetMaxDSMReduceProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的列存储表最大压缩倍数处理报告
	 */
	public SetMaxDSMReduceProduct() {
		super();
	}

	/**
	 * 构造默认的列存储表最大压缩倍数处理报告
	 */
	public SetMaxDSMReduceProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析列存储表最大压缩倍数处理报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SetMaxDSMReduceProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个单元
	 * @param e SetDSMReduceItem实例
	 * @return 返回真或者假
	 */
	public boolean add(TissItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 构造列存储表最大压缩倍数处理报告，指定参数
	 * @param site 节点地址
	 * @param state 状态
	 */
	public void add(Node site, int state) {
		add(new TissItem(site, state));
	}
	
	/**
	 * 保存一个列存储表最大压缩倍数处理报告
	 * @param e SetMaxDSMReduceProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<TissItem> e) {
		int size = array.size();
		array.addAll(e);
		return array.size() - size;
	}

	/**
	 * 输出全部处理单元 
	 * @return 返回SetDSMReduceItem列表
	 */
	public List<TissItem> list() {
		return new ArrayList<TissItem>(array);
	}

	/**
	 * 统计单元数目
	 * @return 单元数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SetMaxDSMReduceProduct duplicate() {
		return new SetMaxDSMReduceProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(array.size());
		for (TissItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			TissItem item = new TissItem(reader);
			array.add(item);
		}
	}

}