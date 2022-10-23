/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.from;

import java.io.*;
import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 整数排序FROM(DIFFUSE)阶段任务。<br>
 * 在这里，产生用于供后续排序的初始数据。<br><br>
 * 
 * @author scott.liang
 * @version 1.2 05/09/2015
 * @since laxcus 1.0
 */
public class SortFromTask extends FromTask {

	/**
	 * 构造整数排序FROM阶段任务实例
	 */
	public SortFromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#divide()
	 */
	@Override
	public long divide() throws TaskException {
		FromStep cmd = getCommand();
		FromSession session = cmd.getSession();
		ColumnSector sector = session.getIndexSector();

		if (sector.getClass() != IntegerSector.class) {
			throw new FromTaskException("illegal integer sector");
		}

		Collection<IntegerRange> ranges = ((IntegerSector) sector).list();
		Iterator<IntegerRange> iterator = ranges.iterator();

		/** 整数分区下标（模值） -> 整数集合 **/
		TreeMap<Integer, ClassWriter> buffs = new TreeMap<Integer, ClassWriter>();

		// 找到最大范围值
		int begin = 0, end = 0;
		for (int index = 0; iterator.hasNext(); index++) {
			IntegerRange e = iterator.next();
			if (index == 0) {
				begin = e.begin();
				end = e.end();
			}
			if (begin > e.begin()) {
				begin = e.begin();
			}
			if (end < e.end()) {
				end = e.end();
			}
		}
		IntegerRange range = new IntegerRange(begin, end);

		int capacity = findInteger(session, "size");
		if (capacity < 1) {
			throw new FromTaskException("size error");
		}
		int blocks = capacity / ranges.size();
		if (capacity % ranges.size() != 0) {
			blocks++;
		}

		Random random = new Random(System.currentTimeMillis());

		// 循环产生一组随机数字
		int count = 0;
		while (count < capacity) {
			int value = random.nextInt();
			// 必须在指定的范围内
			if (!range.inside(value)) {
				continue;
			}

			// 根据数字确定它在分片中的下标位置。这个下标也是模值
			int index = sector.indexOf(new java.lang.Integer(value));

			// 找到缓存
			ClassWriter writer = buffs.get(index);
			if (writer == null) {
				writer = new ClassWriter(blocks * 4);
				buffs.put(index, writer);
			}
			// 保存整型值
			writer.writeInt(value);
			count++;
		}

		Logger.debug(getIssuer(), this, "divide", "memory:%s, mod size:%d", cmd.isMemory(), buffs.size());

		// 建立数据写入器，存取模式根据命令的“内存/磁盘”选定
		FluxWriter writer = fetchWriter();

		// 值模 -> 存储类实例
		Iterator<Map.Entry<Integer, ClassWriter>> link = buffs.entrySet().iterator();
		
		while (link.hasNext()) {
			Map.Entry<Integer, ClassWriter> entry = link.next();

			// 模值和实例数据
			long mod = entry.getKey().intValue();
			byte[] b = entry.getValue().effuse();

			// 成员数目
			int elements = b.length / 4;

			// 在添加方式写
			FluxField field = writer.append(mod, elements, b, 0, b.length);
			// 如果为null，即写入失败
			if (field == null) {
				throw new FromTaskException("write failed");
			}
			Logger.debug(getIssuer(), this, "divide", "field is '%s'", field);
		}

		// 返回元数组字节长度
		return assemble();
	}

	/**
	 * 计算元数据字节数组长度
	 * @return 长整数
	 * @throws TaskException
	 */
	private long assemble() throws TaskException {
		// 生成元数据字节数组，返回字节长度
		byte[] b = effuse();
		return b.length;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#complete()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		FluxArea area = createFluxArea();
		return area.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return writeTo(file, false, b, 0, b.length);
	}
}