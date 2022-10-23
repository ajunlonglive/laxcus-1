/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 获得数据表在集群的分布图谱命令。<br><br>
 * 
 * 命令格式：PRINT ENTITY STUBS DIAGRAM 数据库.表 <br><br>
 * 
 * 此操作由FRONT节点发出，通过CALL站点，分发到所有DATA站点，包括主节点和从节点。<br>
 * FRONT -> CALL -> DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class PrintStubsDiagram extends ProcessTable {

	private static final long serialVersionUID = -2759462687810010723L;

	/**
	 * 构造默认和私有获得数据表在集群的分布图谱命令
	 */
	private PrintStubsDiagram() {
		super();
	}

	/**
	 * 从传入的获得数据表在集群的分布图谱命令，生成它的数据副本
	 * @param that PrintStubsDiagram实例
	 */
	private PrintStubsDiagram(PrintStubsDiagram that) {
		super(that);
	}

	/**
	 * 构造获得数据表在集群的分布图谱命令，指定数据表名
	 * @param space 数据表名
	 */
	public PrintStubsDiagram(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析获得数据表在集群的分布图谱命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public PrintStubsDiagram(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintStubsDiagram duplicate() {
		return new PrintStubsDiagram(this);
	}

}