/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.slider;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.data.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列码位值统计池。<br>
 * 记录每一列的码位值，统计它们的发生次数。
 * 这些参数将用于平衡分布数据计算时使用。
 * 
 * @author scott.liang
 * @version 1.2 12/13/2015
 * @since laxcus 1.0
 */
public class DataSliderPool extends VirtualPool {

	private static DataSliderPool selfHandle = new DataSliderPool();

//	/** 码位计算器代理 **/
//	private ScalerTrustor trustor;
//
//	/**
//	 * 设置码位计算器代理。在站点启动时设置。
//	 * @param e ScaleTrustor实例
//	 */
//	public void setScaleTrustor(ScalerTrustor e) {
//		trustor = e;
//	}
//
//	/**
//	 * 返回码位计算器代理
//	 * @return ScaleTrustor实例
//	 */
//	public ScalerTrustor getScaleTrustor() {
//		return trustor;
//	}

	/** 码位统计文件名 **/
	private final String filename = "codepts.conf";
	
	/** 数据刷新标识 **/
	private boolean refresh;

	/** 列空间 -> 列值统计表 **/
	private Map<Dock, ScalerTable> tables = new TreeMap<Dock, ScalerTable>();

	/**
	 * 构造默认和私有的列码位值统计池
	 */
	private DataSliderPool() {
		super();
	}

	/**
	 * 返回列码位值统计池静态句柄
	 * @return ScaleCountPool实例
	 */
	public static DataSliderPool getInstance() {
		return DataSliderPool.selfHandle;
	}
	
	/**
	 * 设置更新标识。当这个标识为真，且定时超时后，发生写磁盘操作
	 * 
	 * @param b 更新标识
	 */
	private void setRefresh(boolean b) {
		super.lockSingle();
		try {
			refresh = b;
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 判断发生更新
	 * 
	 * @return 返回真或者假
	 */
	private boolean isRefresh() {
		return refresh;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return readScales();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		// 触发间隔
		final long interval = 600000;
		// 触发时间
		long touchTime = System.currentTimeMillis() + interval;
		// 循环检测
		while (!isInterrupted()) {
			// 当发生更新和超时后，写数据到磁盘
			boolean success = (isRefresh() && System.currentTimeMillis() >= touchTime);
			if (success) {
				setRefresh(false);
				touchTime = System.currentTimeMillis() + interval;
				// 写数据到磁盘
				writeScales(); 
			}
			super.sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		writeScales();
	}

	/**
	 * 根据列空间，查找分区
	 * @param dock 列空间
	 * @return 返回列索引值分区数组，没有找到或者出错是空指针
	 */
	public IndexZone[] findIndexZones(Dock dock) {
		super.lockMulti();
		try {
			ScalerTable that = tables.get(dock);
			if (that != null) {
				return that.put();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 建立码位统计表，并且保存在队列里.
	 * @param dock 列空间
	 * @param attribute 列属性
	 * @return 返回ScaleTable实例
	 */
	private ScalerTable createScaleTable(Dock dock, ColumnAttribute attribute) {
		ScalerTable table = null;
		super.lockSingle();
		try {
			// 取统计表
			table = tables.get(dock);
			// 存在，返回它
			if (table != null) {
				return table;
			}
			// 如果没有，根据列属性建立一个
			table = ScalerTableCreator.create(attribute.getType());
			if (table != null) {
				table.setDock(dock);
				tables.put(dock, table);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return table;
	}

//	/**
//	 * 取出INSERT命令中的列值，分析它的列码位值，保存到内存中。
//	 * @param insert SQL INSERT语句
//	 * @return 成功返回真，否则假
//	 */
//	public boolean insert(Insert insert) {
//		//1. 取出字符类的属性信息
//		Space space = insert.getSpace();
//		// 查找表
//		Table table = StaffOnDataPool.getInstance().findTable(space);
//		if(table == null) {
//			Logger.error(this, "insert", "cannot find %s", space);
//			return false;
//		}
//
//		// 1.保存索引编号
//		ArrayList<java.lang.Short> array = new ArrayList<java.lang.Short>();
//		for (ColumnAttribute attribute : table.list()) {
//			// 是索引即保留
//			if (attribute.isKey()) {
//				array.add(attribute.getColumnId());
//			}
//		}
//
//		Logger.debug(this, "insert", "%s key size is %d", space, array.size());
//
//		// 空集合退出
//		if (array.isEmpty()) {
//			return true;
//		}
//
//		// 2. 逐一提取列中的首字符代码位
//		for (short columnId : array) {
//			ColumnAttribute attribute = table.find(columnId);
//			Dock dock = new Dock(space, columnId);
//			ScalerTable element = createScaleTable(dock, attribute);
//			// 是错误
//			if (element == null) {
//				throw new IllegalValueException("cannot be create scale table");
//			}
//
//			// 建立码位计算器
//			ScalerPart part = null;
//			if (attribute.getScaler() != null) {
//				part = new ScalerPart(table.getIssuer(), attribute.getScaler());
//			}
//			// 码位计算器，首选用户定义，否则是系统定义
//			Slider scaler = createCodeScaler(part, attribute);
//
//			ArrayList<java.lang.Number> stack = new ArrayList<java.lang.Number>(insert.size());
//			for (Row row : insert.list()) {
//				// 列编号减1是索引号。因为顺序已经排定，用索引号取出列
//				Column column = row.get(columnId - 1); 
//				try {
//					java.lang.Number num = scaler.seek(column);
//					stack.add(num);
//				} catch (ScalerException e) {
//					Logger.error(e);
//					return false;
//				}
//			}
//
//			//3. 保存数据
//			super.lockSingle();
//			try {
//				for(java.lang.Number value : stack) {
//					element.add(value, 1);
//				}
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			} finally {
//				super.unlockSingle();
//			}
//		}
//		
//		setRefresh(true);
//
//		Logger.debug(this, "insert", "table size:%d", tables.size());
//
//		return true;
//	}

	/**
	 * 取出INSERT命令中的列值，分析它的列码位值，保存到内存中。
	 * @param insert SQL INSERT语句
	 * @return 成功返回真，否则假
	 */
	public boolean insert(Insert insert) {
		//1. 取出字符类的属性信息
		Space space = insert.getSpace();
		// 查找表
		Table table = StaffOnDataPool.getInstance().findTable(space);
		if(table == null) {
			Logger.error(this, "insert", "cannot find %s", space);
			return false;
		}

		// 1.保存索引编号
		ArrayList<java.lang.Short> array = new ArrayList<java.lang.Short>();
		for (ColumnAttribute attribute : table.list()) {
			// 是索引即保留
			if (attribute.isKey()) {
				array.add(attribute.getColumnId());
			}
		}

		Logger.debug(this, "insert", "%s key size is %d", space, array.size());

		// 空集合退出
		if (array.isEmpty()) {
			return true;
		}

		// 2. 逐一提取列中的首字符代码位
		for (short columnId : array) {
			ColumnAttribute attribute = table.find(columnId);
			Dock dock = new Dock(space, columnId);
			ScalerTable element = createScaleTable(dock, attribute);
			// 是错误
			if (element == null) {
				throw new IllegalValueException("cannot be create scale table");
			}

//			// 建立码位计算器
//			ScalerPart part = null;
//			if (attribute.getScaler() != null) {
//				part = new ScalerPart(table.getIssuer(), attribute.getScaler());
//			}
			
			// 系统中列对象定位器
			ColumnSlider scaler = createColumnSlider(attribute);

			ArrayList<java.lang.Number> stack = new ArrayList<java.lang.Number>(insert.size());
			for (Row row : insert.list()) {
				// 列编号减1是索引号。因为顺序已经排定，用索引号取出列
				Column column = row.get(columnId - 1); 
				try {
					java.lang.Number num = scaler.seek(column);
					stack.add(num);
				} catch (SliderException e) {
					Logger.error(e);
					return false;
				}
			}

			//3. 保存数据
			super.lockSingle();
			try {
				for(java.lang.Number value : stack) {
					element.add(value, 1);
				}
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}
		
		setRefresh(true);

		Logger.debug(this, "insert", "table size:%d", tables.size());

		return true;
	}
	
//	/**
//	 * 根据码位部件或者列属性，建立码位计算器。<br>
//	 * 首先检查码位计算器部件 ，在有效情况下建立用户自定义的码位计算器。否则，使用系统定义的码位计算器。
//	 * 
//	 * @param part 码位计算器部件
//	 * @param attribute 列属性
//	 * @return 返回码位计算器
//	 */
//	protected CodeScaler createCodeScaler(ScalerPart part, ColumnAttribute attribute) {
//		if (part != null) {
//			if (trustor == null) {
//				throw new NullPointerException();
//			}
//			CodeScaler scaler = trustor.createScaler(part);
//			if (scaler == null) {
//				throw new IllegalValueException("canno be find %s", part);
//			}
//			return scaler;
//		} 
//
//		// 建立一个码位计算器
//		ColumnScaler scaler = ColumnScalerCreator.create(attribute.getType());
//		if(scaler == null) {
//			throw new IllegalValueException("cannot be create %d", attribute.getType());
//		}
//
//		// 如果是可变长或者字符，定义它的参数
//		if (attribute.isWord()) {
//			((WordScaler) scaler).setSentient(((WordAttribute) attribute)
//					.isSentient());
//		}
//		if (attribute.isVariable()) {
//			((VariableScaler) scaler)
//					.setPacking(((VariableAttribute) attribute).getPacking());
//		}
//
//		return scaler;
//	}

//	/**
//	 * 根据码位部件或者列属性，建立码位计算器。<br>
//	 * 首先检查码位计算器部件 ，在有效情况下建立用户自定义的码位计算器。否则，使用系统定义的码位计算器。
//	 * 
//	 * @param part 码位计算器部件
//	 * @param attribute 列属性
//	 * @return 返回码位计算器
//	 */
//	protected Slider createCodeScaler(ScalerPart part, ColumnAttribute attribute) {
//		if (part != null) {
//			if (trustor == null) {
//				throw new NullPointerException();
//			}
//			CodeScaler scaler = trustor.createScaler(part);
//			if (scaler == null) {
//				throw new IllegalValueException("canno be find %s", part);
//			}
//			return scaler;
//		} 
//
//		// 建立一个码位计算器
//		CodeScaler scaler = ColumnScalerCreator.create(attribute.getType());
//		if(scaler == null) {
//			throw new IllegalValueException("cannot be create %d", attribute.getType());
//		}
//
//		// 如果是可变长或者字符，定义它的参数
//		if (attribute.isWord()) {
//			((WordScaler) scaler).setSentient(((WordAttribute) attribute)
//					.isSentient());
//		}
//		if (attribute.isVariable()) {
//			((VariableScaler) scaler)
//					.setPacking(((VariableAttribute) attribute).getPacking());
//		}
//
//		return scaler;
//	}
	
	/**
	 * 根据码位部件或者列属性，建立码位计算器。<br>
	 * 首先检查码位计算器部件 ，在有效情况下建立用户自定义的码位计算器。否则，使用系统定义的码位计算器。
	 * 
	 * @param part 码位计算器部件
	 * @param attribute 列属性
	 * @return 返回码位计算器
	 */
	protected ColumnSlider createColumnSlider(ColumnAttribute attribute) {
//		if (part != null) {
//			if (trustor == null) {
//				throw new NullPointerException();
//			}
//			CodeScaler scaler = trustor.createScaler(part);
//			if (scaler == null) {
//				throw new IllegalValueException("canno be find %s", part);
//			}
//			return scaler;
//		} 

		// 建立一个列对象定位器
		ColumnSlider slider = ColumnSliderCreator.create(attribute.getType());
		if (slider == null) {
			throw new IllegalValueException("cannot be create %d", attribute.getType());
		}

		// 如果是可变长或者字符，定义它的参数
		if (attribute.isWord()) {
			((WordSlider) slider).setSentient(((WordAttribute) attribute).isSentient());
		}
		if (attribute.isVariable()) {
			((VariableSlider) slider)
					.setPacking(((VariableAttribute) attribute).getPacking());
		}

		return slider;
	}
	
	/**
	 * 从磁盘中读取全部索引值范围
	 * @return 成功返回真，否则假
	 */
	private boolean readScales() {
		File file = DataLauncher.getInstance().createResourceFile(filename);
		if (!file.exists()) {
			return true;
		}

		byte[] b = DataLauncher.getInstance().readFile(file);
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			byte family = reader.current();
			ScalerTable that = null;
			switch (family) {
			case ScaleType.SHORT_SCALE:
				that = new ShortScalerTable();
				break;
			case ScaleType.INTEGER_SCALE:
				that = new IntegerScalerTable();
				break;
			case ScaleType.LONG_SCALE:
				that = new LongScalerTable();
				break;
			case ScaleType.FLOAT_SCALE:
				that = new FloatScalerTable();
				break;
			case ScaleType.DOUBLE_SCALE:
				that = new DoubleScalerTable();
				break;
			}
			if (that == null) {
				throw new IllegalValueException("cannot be create:%d", family);
			}
			that.resolve(reader);
			tables.put(that.getDock(), that);

			Logger.info(this, "read", "dock is %s, count is %d",
					that.getDock(), that.size());
		}
		return true;
	}

	/**
	 * 写全部索引值范围到磁盘
	 */
	private void writeScales() {
		// 数据写入可类化存储器
		ClassWriter writer = new ClassWriter();
		// 锁定写入
		super.lockSingle();
		try {
			writer.writeInt(tables.size());
			for (ScalerTable that : tables.values()) {
				writer.writeObject(that);

				Logger.info(this, "write", "dock is %s, count is %d",
						that.getDock(), that.size());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 数据写入磁盘
		byte[] b = writer.effuse();
		File file = DataLauncher.getInstance().createResourceFile(filename);
		DataLauncher.getInstance().flushFile(file, b);
	}

}