/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.util.*;

import com.laxcus.access.stub.index.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH.SCAN阶段的数据扫描区域。<br>
 * 在DATA.SCAN生成，返回给CALL.ASSIGN阶段处理。每个站点生成一个，包含多个表的扫描结果。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public final class ScanArea extends EstablishArea {

	private static final long serialVersionUID = 4690756494924276896L;

	/** 数据块分布区域。下属主机地址必须是与资源地址一致。  **/
	private Map<EstablishFlag, ScanField> fields = new TreeMap<EstablishFlag, ScanField>();

	/**
	 * 构造一个默认的数据扫描区域。
	 */
	private ScanArea() {
		super();
	}

	/**
	 * 根据传入的数据扫描区域，生成它的数据副本
	 * @param that ScanArea实例
	 */
	private ScanArea(ScanArea that) {
		super(that);
		fields.putAll(that.fields);
	}

	/**
	 * 构造数据扫描区域，设置它的源主机地址。
	 * @param source 源主机地址(一定是数据节点地址)
	 */
	public ScanArea(Node source) {
		this();
		setSource(source);
	}

	/**
	 * 从可类化读取器中解析数据扫描区域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ScanArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个表下的数据块
	 * @param flag 扫描标识
	 * @param item 数据块属性
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(EstablishFlag flag, StubItem item) {
		ScanField field = fields.get(flag);
		if (field == null) {
			field = new ScanField(flag);
			fields.put(field.getFlag(), field);
		}
		return field.addStubItem(item);
	}
	
	/**
	 * 保存一个表下的全部数据扫描域。如果已经存在，旧的将被清除。
	 * @param field 扫描区域
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ScanField field) {
		EstablishFlag flag = field.getFlag();
		return fields.put(flag, field) == null;
	}

	/**
	 * 输出全部
	 * @return ScanField列表
	 */
	public List<ScanField> list() {
		return new ArrayList<ScanField>(fields.values());
	}

	/**
	 * 统计表空间扫描集合长度
	 * @return 扫描集合长度
	 */
	public int size() {
		return fields.size();
	}

	/**
	 * 判断是否空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.util.EstabArea#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据成员数目
		writer.writeInt(fields.size());
		// 写入数据成员
		for (ScanField e : fields.values()) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.util.EstabArea#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 成员数目
		int size = reader.readInt();
		// 每一个成员
		for (int i = 0; i < size; i++) {
			ScanField e = new ScanField(reader);
			fields.put(e.getFlag(), e);
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public ScanArea duplicate() {
		return new ScanArea(this);
	}

//	public static void main(String[] args) {
//		com.laxcus.util.net.SiteHost host = new com.laxcus.util.net.SiteHost(
//				com.laxcus.util.net.Address.select(), 200, 9000);
//		com.laxcus.site.Node node = new com.laxcus.site.Node(
//				SiteTag.FRONT_SITE, host);
//
//		EstablishScanArea area = new EstablishScanArea(node);
//		byte[] b = area.build();
//
//		System.out.printf("length is %d, [%s]\n",b.length, new String(b));
//		
//		ClassReader reader = new ClassReader(b, 0, b.length);
//		
//		EstablishScanArea two = new EstablishScanArea(reader);
//		
//		System.out.printf("read seek:%d\n", reader.getSeek());
//	}
}