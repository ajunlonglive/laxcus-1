/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.markable.*;

/**
 * DATA主节点主块重构时间触发器。<br>
 * 当发生删除或者更新时，磁盘上会产生数据碎片和冗余数据，<br>
 * DATA节点为提高检索效率，需要定时清理它们，重构数据块。<br>
 * 此类是触发时间定义。<br>
 * 
 * <br>
 * 监控在TOP节点进行，到达指定时间后，通知HOME节点，再由HOME节点通知各DATA主节点重构数据。<br>
 * DATA节点操作过程：删除磁盘上的过期记录，重新排列有效记录，达到节省空间和提高检索效率的目标的<br>
 *
 * @author scott.liang 
 * @version 1.2 3/8/2016
 * @since laxcus 1.0
 */
public final class SwitchTime implements Serializable, Cloneable, Classable, Markable, Comparable<SwitchTime> {

	private static final long serialVersionUID = -8734509269171482089L;

	/** 数据表名 **/
	private Space space;

	/** 重构列标识号，默认是0，即主键(prime key)为索引 */
	private short columnId;

	/** 触发类型，见解发时间定义 */
	private byte family;

	/** 触发间隔 */
	private long interval;

	/** 触发时间(毫秒)。是JVM系统时间。*/
	private long touchTime;

	/**
	 * 根据传入的时间触发器，生成它的数据副本
	 * @param that SwitchTime实例
	 */
	private SwitchTime(SwitchTime that) {
		this();
		space = that.space.duplicate();
		columnId = that.columnId;
		family = that.family;
		interval = that.interval;
		touchTime = that.touchTime;
	}

	/**
	 * 构造一个空的时间触发器
	 */
	public SwitchTime() {
		super();
		columnId = 0;
		family = 0;
		interval = 0L;
		touchTime = 0L;
	}

	/**
	 * 构造时间触发器，同时指定它的数据表名
	 * @param space 数据表名
	 */
	public SwitchTime(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 使用传入的可类化读取器解析参数
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public SwitchTime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出参数
	 * @param reader 标记化读取器
	 */
	public SwitchTime(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 取数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置重构列编号
	 * @param id 重构列编号
	 */
	public void setColumnId(short id) {
		columnId = id;
	}

	/**
	 * 返回重构列编号
	 * @return 重构列编号
	 */
	public short getColumnId() {
		return columnId;
	}

	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return new Dock(space, columnId);
	}

	/**
	 * 定义触发类型
	 * 
	 * @param who 触发类型
	 */
	private void setFamily(byte who) {
		// 不正确，弹出异常
		if(!SwitchTimeTag.isFamily(who)) {
			throw new IllegalValueException("illegal type %d", who);
		}
		family = who;
	}

	/**
	 * 取触发类型
	 * 
	 * @return 触发类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置触发时间间隔
	 * 
	 * @param i 时间间隔
	 */
	public void setInterval(long i) {
		interval = i;
	}

	/**
	 * 返回触发时间间隔
	 * @return 时间间隔
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * 返回时间描述
	 * @return 文本信息
	 */
	public String getIntervalText() {
		Calendar rd = Calendar.getInstance();
		rd.setTime(SimpleTimestamp.format(interval));

		if (SwitchTimeTag.isHourly(family)) {
			return String.format("%d:%d", rd.get(Calendar.MINUTE), 
					rd.get(Calendar.SECOND));
		} else if (SwitchTimeTag.isDaily(family)) {
			return String.format("%d:%d:%d", rd.get(Calendar.HOUR),
					rd.get(Calendar.MINUTE), rd.get(Calendar.SECOND));
		} else if (SwitchTimeTag.isWeekly(family)) {
			return String.format("%d %d:%d:%d", rd.get(Calendar.DAY_OF_MONTH),
					rd.get(Calendar.HOUR), rd.get(Calendar.MINUTE),
					rd.get(Calendar.SECOND));
		} else if (SwitchTimeTag.isMonthly(family)) {
			return String.format("%d %d:%d:%d", rd.get(Calendar.DAY_OF_MONTH),
					rd.get(Calendar.HOUR), rd.get(Calendar.MINUTE),
					rd.get(Calendar.SECOND));
		}
		return "";
	}

	/**
	 * 设置小时间隔
	 * @param minute 分
	 * @param second 秒
	 */
	public void setHourlyInterval(int minute, int second) {
		interval = SimpleTimestamp.format(0, 0, 0, 0, minute, second, 0);
		setFamily(SwitchTimeTag.HOURLY);
	}

	/**
	 * 返回下一次按小时触发时间
	 * @return 系统时间
	 */
	public long nextHourlyTime() {
		Calendar touch = Calendar.getInstance();
		touch.set(Calendar.MILLISECOND, 0);

		Calendar rd = Calendar.getInstance();
		rd.setTime(SimpleTimestamp.format(interval));

		// 调整到指定的分/秒
		touch.set(Calendar.MINUTE, rd.get(Calendar.MINUTE));
		touch.set(Calendar.SECOND, rd.get(Calendar.SECOND));

		// 当前时间超过指定时间，小时数加1，移到下一次发生的时间
		if (System.currentTimeMillis() > touch.getTimeInMillis()) {
			touch.add(Calendar.HOUR, 1);
		}
		// 返回下一次触发时间
		return touch.getTimeInMillis();
	}

	/**
	 * 设置天间隔
	 * @param hour 小时
	 * @param minute 分
	 * @param second 秒
	 */
	public void setDailyInterval(int hour, int minute, int second) {
		interval = SimpleTimestamp.format(0, 0, 0, hour, minute, second, 0);
		setFamily(SwitchTimeTag.DAILY);
	}

	/**
	 * 返回下一次按天触发时间
	 * @return 系统时间
	 */
	public long nextDailyTime() {
		Calendar touch = Calendar.getInstance();
		touch.set(Calendar.MILLISECOND, 0);

		Calendar rd = Calendar.getInstance();
		rd.setTime(SimpleTimestamp.format(interval));

		touch.set(Calendar.HOUR_OF_DAY, rd.get(Calendar.HOUR_OF_DAY));
		touch.set(Calendar.MINUTE, rd.get(Calendar.MINUTE));
		touch.set(Calendar.SECOND, rd.get(Calendar.SECOND));
		// 当前时间超过指定时间，天数加1，移到下一次发生的时间
		if (System.currentTimeMillis() > touch.getTimeInMillis()) {
			touch.add(Calendar.DAY_OF_MONTH, 1);
		}
		return touch.getTimeInMillis();
	}

	/**
	 * 设置星期间隔
	 * @param dayOfWeek 周/日
	 * @param hour 小时
	 * @param minute 分
	 * @param second 秒
	 */
	public void setWeeklyInterval(int dayOfWeek, int hour, int minute, int second) {
		interval = SimpleTimestamp.format(0, 0, dayOfWeek, hour, minute,second, 0);
		setFamily(SwitchTimeTag.WEEKLY);
	}

	/**
	 * 返回下一次按星期触发时间
	 * @return 系统时间
	 */
	public long nextWeeklyTime() {
		Calendar touch = Calendar.getInstance();
		touch.set(Calendar.MILLISECOND, 0);
		Calendar rd = Calendar.getInstance();
		rd.setTime(SimpleTimestamp.format(interval));

		touch.set(Calendar.DAY_OF_WEEK, rd.get(Calendar.DAY_OF_MONTH));
		touch.set(Calendar.HOUR_OF_DAY, rd.get(Calendar.HOUR_OF_DAY));
		touch.set(Calendar.MINUTE, rd.get(Calendar.MINUTE));
		touch.set(Calendar.SECOND, rd.get(Calendar.SECOND));
		// 当前时间达到指定时间，星期数加1，调整到下一次的时间
		if (System.currentTimeMillis() > touch.getTimeInMillis()) {
			touch.add(Calendar.WEEK_OF_MONTH, 1);
		}
		return touch.getTimeInMillis();
	}

	/**
	 * 设置月间隔
	 * @param dayOfMonth 月/日
	 * @param hour 小时
	 * @param minute 分钟
	 * @param second 秒
	 */
	public void setMonthlyInterval(int dayOfMonth, int hour, int minute, int second) {
		interval = SimpleTimestamp.format(0, 0, dayOfMonth, hour, minute, second, 0);
		setFamily(SwitchTimeTag.MONTHLY);
	}

	/**
	 * 返回下一次按月触发间隔时间
	 * @return 系统时间
	 */
	public long nextMonthlyTime() {
		Calendar touch = Calendar.getInstance();
		touch.set(Calendar.MILLISECOND, 0);
		Calendar rd = Calendar.getInstance();
		rd.setTime(SimpleTimestamp.format(interval));

		touch.set(Calendar.DAY_OF_MONTH, rd.get(Calendar.DAY_OF_MONTH));
		touch.set(Calendar.HOUR_OF_DAY, rd.get(Calendar.HOUR_OF_DAY));
		touch.set(Calendar.MINUTE, rd.get(Calendar.MINUTE));
		touch.set(Calendar.SECOND, rd.get(Calendar.SECOND));
		// 超过触发时间，月数加1，调整到下个月
		if (System.currentTimeMillis() > touch.getTimeInMillis()) {
			touch.add(Calendar.MONTH, 1);
		}
		// 返回触发时间
		return touch.getTimeInMillis();
	}

	/**
	 * 根据时间类型计算触发时间。<br><br>
	 * 
	 * 例如：<br>
	 * CREATE REGULATE TIME 数据库.表 HOURLY 12:12 ORDER BY 列名 <br>
	 * CREATE REGULATE TIME 数据库.表 DAILY 0:23:12		 <br>
	 * CREATE REGULATE TIME 数据库.表 WEEKLY 1 0:12:12  	(1-7) <br>
	 * CREATE REGULATE TIME 数据库.表 MONTHLY 31 0:12:23	(1-31) <br>
	 */
	public void nextTouchTime() {
		if (SwitchTimeTag.isHourly(family)) {
			touchTime = nextHourlyTime();
		} else if (SwitchTimeTag.isDaily(family)) {
			touchTime = nextDailyTime();
		} else if (SwitchTimeTag.isWeekly(family)) {
			touchTime = nextWeeklyTime();
		} else if (SwitchTimeTag.isMonthly(family)) {
			touchTime = nextMonthlyTime();
		}
	}

	//	public void nextTouch() {
	//		Calendar endTime = Calendar.getInstance();
	//		endTime.set(Calendar.MILLISECOND, 0);
	//		Calendar date = Calendar.getInstance();
	//		date.setTime(SimpleTimestamp.format(interval));
	//
	//		System.out.printf("INTERVAL:%d - TIME:%s\n", interval, date.getTime());
	//
	//		switch (family) {
	//		case SwitchTimeTag.HOURLY: // 每时触发时间
	//			endTime.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
	//			endTime.set(Calendar.SECOND, date.get(Calendar.SECOND));
	//
	//			System.out.println(endTime.getTime().toString());
	//			System.out.printf("CUR:%d - END:%d\n", System.currentTimeMillis(), endTime.getTimeInMillis());
	//			// 如果当前时间超过指定时间，移到下一次发生的时间
	//
	//			if (System.currentTimeMillis() > endTime.getTimeInMillis()) {
	//				endTime.add(Calendar.HOUR, 1);
	//			}
	//			System.out.printf("%d - %d\n", System.currentTimeMillis(), endTime.getTimeInMillis());
	//			touchTime = endTime.getTimeInMillis();
	//			break;
	//		case SwitchTimeTag.DAILY: // 每天触发时间
	//			endTime.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
	//			endTime.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
	//			endTime.set(Calendar.SECOND, date.get(Calendar.SECOND));
	//			if (System.currentTimeMillis() > endTime.getTimeInMillis()) {
	//				endTime.add(Calendar.DAY_OF_MONTH, 1);
	//			}
	//			touchTime = endTime.getTimeInMillis();
	//			break;
	//		case SwitchTimeTag.WEEKLY: // 每周触发时间
	//			endTime.set(Calendar.DAY_OF_WEEK, date.get(Calendar.DAY_OF_MONTH));
	//			endTime.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
	//			endTime.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
	//			endTime.set(Calendar.SECOND, date.get(Calendar.SECOND));
	//			if (System.currentTimeMillis() > endTime.getTimeInMillis()) {
	//				endTime.add(Calendar.WEEK_OF_MONTH, 1);
	//			}
	//			touchTime = endTime.getTimeInMillis();
	//			break;
	//		case SwitchTimeTag.MONTHLY: // 每月触发时间
	//			endTime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
	//			endTime.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
	//			endTime.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
	//			endTime.set(Calendar.SECOND, date.get(Calendar.SECOND));
	//			if (System.currentTimeMillis() > endTime.getTimeInMillis()) {
	//				endTime.add(Calendar.MONTH, 1);
	//			}
	//			touchTime = endTime.getTimeInMillis();
	//			break;
	//		}
	//	}

	/**
	 * 判断达到触发时间
	 * @return 返回真或者假
	 */
	public boolean isTouched() {
		if (touchTime == 0L) {
			nextTouchTime();
		}
		return System.currentTimeMillis() >= touchTime;
	}

	/**
	 * 生成数据副本
	 * @return SwitchTime实例
	 */
	public SwitchTime duplicate() {
		return new SwitchTime(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SwitchTime.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SwitchTime) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%s", new Dock(space, columnId), SwitchTimeTag.translate(family));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SwitchTime that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(space, that.space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeShort(columnId);
		writer.write(family);
		writer.writeLong(interval);
		writer.writeLong(touchTime);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		space = new Space(reader);
		columnId = reader.readShort();
		family = reader.read();
		interval = reader.readLong();
		touchTime = reader.readLong();
		return reader.getSeek() - seek;
	}

//		public static void main(String[] args) {
//			SwitchTime e = new SwitchTime();
//	
//			e.setHourlyInterval(33, 21);
//	
//			System.out.printf("touch is %s\n", e.getIntervalText());
//	
//			java.text.SimpleDateFormat style = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//			long dt = e.nextHourlyTime();
//			System.out.printf("%s\n", style.format(new Date(dt)));
//			dt = e.nextHourlyTime();
//			System.out.printf("%s\n", style.format(new Date(dt)));
//		}

}