/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.index.section.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.util.range.*;

/**
 * 索引平衡分割器。<br><br>
 * 
 * 索引平衡分割器收集来自多个节点的索引值区（IndexZone），按照索引值区的权重，生成一个索引扇形结构（IndexSector）。
 * 索引扇形结构用于分布计算的数据平均分配，被放在FROM/TO的会话中，分发到DATA/WORK节点上。
 * DATA/WORK节点根据索引扇形结构，确定每个索引值所处的下标位置。再通过下标，分配到下一阶段的节点。
 * 这样处理的结果是：使用索引扇区，每个列值可以知道在整个集群的列范围中，自己所处的下标位置。<br><br>
 * 
 * 平衡算法分两步: <br>
 * <1> 调用"add"方法， 向内存中添加分片和统计值，转为IndexZone子类保存 <br>
 * <2> 调用"balance"方法，平均分布数据域，输出IndexSector的子集, IndexSector子集包含分片结果 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/10/2009
 * @since laxcus 1.0
 */
public interface IndexBalancer {

	/**
	 * 增加一个索引数据分片区域，分片区域包括数据范围和权重
	 * @param zone 分片区域
	 * @return 保存成功返回“真”，否则“假”。
	 */
	boolean add(IndexZone zone);

	/**
	 * 增加一个索引分片区域，分片区域包括数据范围和权重
	 * @param range 数据范围
	 * @param weight 权重
	 * @return 保存成功返回“真”，否则“假”。
	 */
	boolean add(Range range, int weight);

	/**
	 * 增加一个索引数据分片区域，分片区域包括数据范围和权重
	 * @param begin 索引开始位置
	 * @param end 索引结束位置
	 * @param weight 权重
	 * @return 保存成功返回“真”，否则“假”。
	 */
	boolean add(java.lang.Number begin, java.lang.Number end, int weight);

	/**
	 * 按照节点数目和对象定位器分割数据区域，返回一个分割后的数据区域对象
	 * @param sites 节点数目
	 * @param slider 对象定位器
	 * @return ColumnSector对象
	 */
	ColumnSector balance(int sites, Slider slider);
	
	/**
	 * 按照节点数平均分割数据区域，返回一个分割后的数据区域对象
	 * @param sites 节点数目
	 * @return ColumnSector对象。
	 */
	ColumnSector balance(int sites);
}