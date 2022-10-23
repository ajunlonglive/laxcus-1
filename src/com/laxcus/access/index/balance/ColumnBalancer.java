/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

/**
 * 列索引平衡分割器。<br><br>
 * 
 * 列索引平衡分割器收集来自多个节点的列索引值区（IndexZone），按照列索引值区的权重，生成一个列索引扇形结构（ColumnSector）。
 * 列索引扇形结构用于分布计算的数据平均分配，被放在FROM/TO的会话中，分发到DATA/WORK节点上。
 * DATA/WORK节点根据列索引扇形结构，确定每个索引值所处的下标位置。再通过下标，分配到下一阶段的节点。
 * 这样处理的结果是：使用列索引扇区，每个列值可以知道在整个集群的列范围中，自己所处的下标位置。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 8/4/2020
 * @since laxcus 1.0
 */
public abstract class ColumnBalancer implements IndexBalancer {

	/**
	 * 构造默认的列索引平衡分割器
	 */
	protected ColumnBalancer() {
		super();
	}

}
