/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SQL检索命令，它的子类包括：SELECT、DELETE、UPDATE。<br><br>
 * 
 * Query包含SQL WHERE语句的类实现。
 * 
 * @author scott.liang
 * @version 1.1 5/11/2015
 * @since laxcus 1.0
 */
public abstract class Query extends Manipulate {

	private static final long serialVersionUID = -2933635563338931986L;

	/** WHERE 检索条件 **/
	private Where where;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.SQLCommand#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 构造上级信息
		super.buildSuffix(writer);
		// 检索条件
		writer.writeInstance(where);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.SQLCommand#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析上级信息
		super.resolveSuffix(reader);
		// 检索条件
		where = reader.readInstance(Where.class);
	}

	/**
	 * 构造SQL检索命令，并且指定的SQL操作命令标识
	 * @param family SQL操作命令标识
	 */
	protected Query(byte family) {
		super(family);
	}

	/**
	 * 根据传入的SQL检索命令，生成它的副本
	 * @param that Query实例
	 */
	protected Query(Query that) {
		super(that);
		if (that.where != null) {
			where = that.where.duplicate();
		}
	}

	/**
	 * 设置SQL WHERE检索条件。<br>
	 * 每次设置的WHERE实例放到最后，形成单向链表。
	 * 
	 * @param e Where实例
	 */
	public void setWhere(Where e) {
		if (where == null) {
			where = e;
		} else {
			where.attach(e); // 绑定到最后
		}
	}
	
	/**
	 * 重新设置SQL WHERE检索条件。<br>
	 * 注意：这个操作是替换SQL WHERE检索条件！
	 * @param e Where实例
	 */
	public void resetWhere(Where e) {
		where = e;
	}

	/**
	 * 返回当前SQL WHERE检索条件
	 * @return Where实例
	 */
	public Where getWhere() {
		return where;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger issuer) {
		super.setIssuer(issuer);
		// 设置检索条件的用户签名，把签名传导给内部包含的命令
		if (where != null) {
			where.setIssuer(issuer);
		}
	}

	/**
	 * 判断有嵌套
	 * @return 返回真或者假
	 */
	public boolean hasNested() {
		return where != null && where.isNested();
	}

	/**
	 * 将WHERE检索条件生成为字节数组并且输出。
	 * 这里的处理要兼容C接口。
	 * 
	 * @param condi WHERE条件
	 * @return 返回字节数组
	 */
	private byte[] buildCondition(Where condi) {
		ClassWriter writer = new ClassWriter();
		// 1. 外部连接关系 (AND|OR)
		writer.write(condi.getOuterRelation());
		// 2. 同级前面条件连接关系(AND|OR)
		writer.write(condi.getRelation());
		// 3. 值比较关系
		writer.write(condi.getCompare());
		// 4. 生成WhereIndex数据流，写入数据流长度和数据
		byte[] data = condi.getIndex().build();
		// WhereIndex数据流长度
		writer.writeInt(data.length);
		// 数据
		writer.write(data);

		// 同级关联检索条件
		ClassWriter partners = new ClassWriter();
		for (Where partner : condi.getPartners()) {
			data = buildCondition(partner);
			partners.write(data);
		}
		// 同级关系检索数据，保存!
		data = partners.effuse();
		// 同级数据流
		int len = (data == null ? 0 : data.length);
		writer.writeInt(len);
		if (len > 0) {
			writer.write(data);
		}
		// 输出数据流
		return writer.effuse();
	}

	/**
	 * 生成WHERE比较关系数据流。这里的处理兼容C接口。
	 * 
	 * @return 返回字节数组
	 */
	protected byte[] buildCondition() {
		ClassWriter writer = new ClassWriter(1024);

		Where condi = where;
		while (condi != null) {
			byte[] b = buildCondition(condi);
			writer.write(b, 0, b.length);
			// 下一个条件
			condi = condi.next();
		}
		return buildField(FieldTag.CONDITION, writer.effuse());
	}

	/**
	 * 从可类读取器中解析检索条件
	 * @param reader 被检索的数据流
	 * @param partner 是否属于同级关系
	 * @return 返回解析长度
	 */
	private int resolveCondition(ClassReader reader, boolean partner) {
		final int scale = reader.getSeek();

		Where condi = new Where();
		// 外部关联关系
		condi.setOuterRelation(reader.read());
		// 同级前面的关联关系
		condi.setRelation(reader.read());
		// 当前值的比较关系
		condi.setCompare(reader.read());
		// WhereIndex数据
		// 跨过4字节(WhereIndex数据流长度)
		reader.skip(4);
		// 判断WhereIndex子类
		WhereIndex index = null;
		byte family = reader.current();
		switch (family) {
		case IndexType.SHORT_INDEX:
			index = new com.laxcus.access.index.ShortIndex(); break;
		case IndexType.INTEGER_INDEX:
			index = new com.laxcus.access.index.IntegerIndex(); break;
		case IndexType.LONG_INDEX:
			index = new com.laxcus.access.index.LongIndex(); break;
		case IndexType.FLOAT_INDEX:
			index = new com.laxcus.access.index.FloatIndex(); break;
		case IndexType.DOUBLE_INDEX:
			index = new com.laxcus.access.index.DoubleIndex(); break;
		case IndexType.NESTED_INDEX:
			index = new com.laxcus.access.index.NestedIndex(); break;
		default:
			throw new ColumnException("cannot support index:%d", family);
		}
		// 解析WhereIndex和保存
		index.resolve(reader);
		condi.setIndex(index);

		// 如果是同级连接关系
		if (partner) {
			// 找到最后一个有效的检索条件，把新的检索条件加到它的队列中
			Where last = getWhere().lastEnabled();
			last.addPartner(condi);
		} else {
			// 不是同级关系时，设置这个检索条件
			setWhere(condi);
		}

		// 解析同级连接条件数据流长度
		int partnerSize = reader.readInt();
		for (int count = 0; count < partnerSize;) {
			int len = resolveCondition(reader, true);
			count += len;
		}

		return reader.getSeek() - scale;
	}

	/**
	 * 从字节数组中解析检索条件
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析长度
	 */
	protected int resolveCondition(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		while (reader.getSeek() < reader.getEnd()) {
			resolveCondition(reader, false);
		}
		return reader.getLength();
	}

	/**
	 * 从检索条件中收集表名
	 * @param condi 检索条件
	 * @return Space列表
	 */
	private List<Space> collect(Where condi) {
		TreeSet<Space> array = new TreeSet<Space>();
		WhereIndex index = condi.getIndex();
		if (index.getClass() == NestedIndex.class) {
			NestedIndex sub = (NestedIndex) index;
			array.add(sub.getSelect().getSpace());
		} else if (index.getClass() == OnIndex.class) {
			OnIndex on = (OnIndex) index;
			array.add(on.getLeft().getSpace());
			array.add(on.getRight().getSpace());
		}
		return new ArrayList<Space>(array);
	}

	/**
	 * 生成数据表规则集合
	 * @param operator 操作符
	 * @return 数据表规则集合
	 */
	protected List<RuleItem> createTableRules(byte operator) {
		TreeSet<RuleItem> array = new TreeSet<RuleItem>();

		// 共享读
		TableRuleItem root = new TableRuleItem(operator, getSpace());
		array.add(root);

		// 收集WHERE语句嵌套的表名
		List<Space> spaces = collect(where);
		// 共享读集合
		for (Space space : spaces) {
			TableRuleItem sub = new TableRuleItem(operator, space);
			array.add(sub);
		}
		return new ArrayList<RuleItem>( array);
	}	
}