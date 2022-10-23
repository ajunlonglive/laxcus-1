/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.util.*;
import java.util.regex.*;

import com.laxcus.thread.*;
import com.laxcus.log.client.*;

/**
 * 图形界面节点日志代理。<br>
 * 
 * 针对WATCH/TERMINAL的图形界面，提供日志处理代理服务。
 * 
 * @author scott.liang
 * @version 1.0 9/17/2019
 * @since laxcus 1.0
 */
public abstract class DisplayLogTrustor extends MutexThread implements LogPrinter {

	/** 日志格式正则表达式 **/
	private final static String REGEX ="^\\s*([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+?)([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+)$";

	/** 后缀 **/
	private final static String SUFFIX = "^\\s*([\\p{ASCII}\\W]+?)\\s*$";
	
	/** 允许的日志容量,1万行！ **/
	private static final int maxCapcity = 10000;
	
	/** 最大/最小时延 **/
	private static final long maxSlientTime = 5000L;
	private static final long minSlientTime = 1000L;
	
	/** 最大/最小日志发送数量 **/
	private static final int maxLogNumber = 200;
	private static final int minLogNumber = 20;

	/** 保存日志文本 **/
	private ArrayList<String> array;
	
	/** 延时时间 **/
	private volatile long slientTime;
	
	/** 日志的单次发送量限制 **/
	private volatile int pushLimit;

	/**
	 * 构造默认的图形界面节点日志代理
	 */
	protected DisplayLogTrustor() {
		super();
		array = new ArrayList<String>(DisplayLogTrustor.maxCapcity);
		// 默认为最低值
		low();

		// // 延时5秒
		// setSlientTime(maxSlientTime);
		// // 单次限制值
		// setLimit(50);
	}

	/**
	 * 设置延时间隔
	 * @param value
	 */
	private void setSlientTime(long value) {
		if (value < minSlientTime) {
			slientTime = minSlientTime;
		} else if (value > maxSlientTime) {
			slientTime = maxSlientTime;
		} else {
			slientTime = value;
		}
	}
	
	/**
	 * 日志单次发送限制值
	 * 
	 * @param value 限制值
	 */
	private void setLimit(int value) {
		if (value < minLogNumber) {
			value = minLogNumber;
		} else if (value > maxLogNumber) {
			pushLimit = maxLogNumber;
		} else {
			pushLimit = value;
		}
	}

	/**
	 * 返回日志单次发送限制值
	 * 
	 * @return 限制值
	 */
	public int getLimit() {
		return pushLimit;
	}
	
	/**
	 * 降低到最低值。发送间隔最大，发送日志数量最少
	 */
	public void low() {
		slientTime = maxSlientTime;
		pushLimit = minLogNumber;
	}

	/**
	 * 压力增大，降低日志处理，处理办法：<br>
	 * 1. 提高延时时间，延时时间不得超过10秒。<br>
	 * 2, 减少日志发送数量，日志发送数量不超过100。<br><br>
	 * 
	 * 这个方法被WatchLauncher.defaultSubProcess / TerminalLauncher.defaultSubProcess 调用。
	 */
	public void descent() {
		// // 增加延时
		// if (slientTime < 5000L) {
		// slientTime += 250L;
		// }
		// // 减少发送数量，5是下限
		// if (pushLimit > 50) {
		// pushLimit -= 5;
		// }

		// 增加时延
		setSlientTime(slientTime + 250);
		// 减少日志发送数量
		setLimit(pushLimit - 10);
	}

	/**
	 * CPU压力减少，提高日志处理率，处理办法：<br>
	 * 1. 降低日志发送的延时时间，延时时间不得低于1秒。<br>
	 * 2. 增加日志发送数量，日志数量不低于10个。<br><br>
	 * 
	 * 这个方法被WatchLauncher.defaultSubProcess / TerminalLauncher.defaultSubProcess 调用。
	 */
	public void rise() {
		// // 减少延时
		// if (slientTime > 1000L) {
		// slientTime -= 250L;
		// }
		// // 增加发送数量，200是上限
		// if (pushLimit < 200) {
		// pushLimit += 5;
		// }

		// 减少时延
		setSlientTime(slientTime - 250L);
		// 增加日志发送数量
		setLimit(pushLimit + 10);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		// 延时...
		delay(5000);
		// 降到最低值
		low();
		
		// 循环处理
		while (!isInterrupted()) {
			// 判断有日志，显示它们
			boolean success = (array.size() > 0);
			if (success) {
				success = push();
			}
			
			// 延时，空出CPU给其他GUI线程
			// 原因：频率太快，会造成SWING线程队列压力过大；日志太多，导致图形界面资源用于日志显示，造成GUI刷新似乎停止，形成死锁假象！！！
			// 优化：由WatchLauncher.defaultSubProcess/TerminalLauncher.defaultSubProcess方法判断CPU压力，然后自动调整延时时间和日志输出数量，最低1秒，最高10秒
			delay(slientTime);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.log.client.LogPrinter#print(java.lang.String)
	 */
	@Override
	public void print(String log) {
		// 超过规定容量，忽略它！
		if (array.size() >= DisplayLogTrustor.maxCapcity) {
			return;
		}
		// 锁定，保存！
		super.lockSingle();
		try {
			array.add(log);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 输出经过处理的一组日志
	 * @return LogItem数组，没有是空指针
	 */
	protected List<LogItem> flush() {
		ArrayList<String> subs = new ArrayList<String>();
		// 锁定
		super.lockSingle();
		try {
			// 确定输出上限，取最小的
			int limit = (array.size() > pushLimit ? pushLimit : array.size());
			// 逐个提取，保存
			for (int i = 0; i < limit; i++) {
				String log = array.remove(0);
				subs.add(log);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			super.unlockSingle();
		}

		// 空记录，忽略！
		int size = subs.size();
		if (size == 0) {
			return null;
		}
	
		// 逐一解析，然后输出！
		ArrayList<LogItem> a = new ArrayList<LogItem>(size);
		for (String text : subs) {
			List<LogItem> items = splitLog(text);
			a.addAll(items);
		}
		return a;
	}

	/**
	 * 过滤最后的控制符号
	 * @param input
	 * @return
	 */
	private String suffix(String input) {
		Pattern pattern = Pattern.compile(SUFFIX);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return input;
	}

	/**
	 * 判断它前面的标记符，将一行记录拆分成多行日志。
	 * @param input 一行日志
	 * @return 切割后的多行日志。
	 */
	private String[] split(String input) {
		ArrayList<String> array = new ArrayList<String>();
		while (true) {
			Pattern pattern = Pattern.compile(REGEX);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String first = matcher.group(1);
				String second = matcher.group(2);

				// 判断解析出错
				int size = array.size();
				if (size > 0) {
					if (array.get(size - 1).compareTo(second) == 0) {
						break;
					}
				}

				array.add(suffix(first));
				input = second;
			} else {
				// 不匹配是完整一行，退出
				array.add(suffix(input));
				break;
			}
		}
		String[] a = new String[array.size()];
		return array.toArray(a);
	}

//	/**
//	 * 生成日志单元
//	 * @param input
//	 * @return
//	 */
//	private List<LogItem> splitLog(String input) {
//		String[] lines = split(input);
//		ArrayList<LogItem> logs = new ArrayList<LogItem>();
//		for(int i = 0; i < lines.length; i++) {
//			// 找到换行符，分解成多行
//			String[] subs = lines[i].split("([\\r\\n]+)");
//
//			byte family = 0; // 日志类型
//			if (subs[0].matches("^\\s*(?:DEBUG)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.DEBUG;
//			} else if (subs[0].matches("^\\s*(?:INFO)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.INFO;
//			} else if (subs[0].matches("^\\s*(?:WARNING)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.WARNING;
//			} else if (subs[0].matches("^\\s*(?:ERROR)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.ERROR;
//			} else if (subs[0].matches("^\\s*(?:FATAL)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.FATAL;
//			}
//			logs.add(new LogItem(family, subs[0]));
//
//			for (int j = 1; j < subs.length; j++) {
//				if (family == LogItem.DEBUG) {
//					logs.add(new LogItem(LogItem.SUBDEBUG, subs[j]));
//				} else if (family == LogItem.INFO) {
//					logs.add(new LogItem(LogItem.SUBINFO, subs[j]));
//				} else if (family == LogItem.WARNING) {
//					logs.add(new LogItem(LogItem.SUBWARNING, subs[j]));
//				} else if (family == LogItem.ERROR) {
//					logs.add(new LogItem(LogItem.SUBERROR, subs[j]));
//				} else if (family == LogItem.FATAL) {
//					logs.add(new LogItem(LogItem.SUBFATAL, subs[j]));
//				}
//			}
//		}
//
//		return logs;
//	}

	/**
	 * 生成日志单元
	 * @param input
	 * @return
	 */
	private List<LogItem> splitLog(String input) {
		String[] lines = split(input);
		ArrayList<LogItem> logs = new ArrayList<LogItem>();
		for(int i = 0; i < lines.length; i++) {
			// 找到换行符，分解成多行
			String[] subs = lines[i].split("([\\r\\n]+)");

			byte family = 0; // 日志类型
			String first = subs[0];
			if (first.matches("^\\s*(?:DEBUG)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
				family = LogItem.DEBUG;
			} else if (first.matches("^\\s*(?:INFO)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
				family = LogItem.INFO;
			} else if (first.matches("^\\s*(?:WARNING)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
				family = LogItem.WARNING;
			} else if (first.matches("^\\s*(?:ERROR)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
				family = LogItem.ERROR;
			} else if (first.matches("^\\s*(?:FATAL)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
				family = LogItem.FATAL;
			}
			logs.add(new LogItem(family, first));

			for (int j = 1; j < subs.length; j++) {
				String next = subs[j];
				if (family == LogItem.DEBUG) {
					logs.add(new LogItem(LogItem.SUBDEBUG, next));
				} else if (family == LogItem.INFO) {
					logs.add(new LogItem(LogItem.SUBINFO, next));
				} else if (family == LogItem.WARNING) {
					logs.add(new LogItem(LogItem.SUBWARNING, next));
				} else if (family == LogItem.ERROR) {
					logs.add(new LogItem(LogItem.SUBERROR, next));
				} else if (family == LogItem.FATAL) {
					logs.add(new LogItem(LogItem.SUBFATAL, next));
				}
			}
		}

		return logs;
	}
	
	/**
	 * 处理和推送日志到图形界面
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean push();
}

///**
// * 图形界面节点日志代理。<br>
// * 
// * 针对WATCH/TERMINAL的图形界面，提供日志处理代理服务。
// * 
// * @author scott.liang
// * @version 1.0 9/17/2019
// * @since laxcus 1.0
// */
//public abstract class DisplayLogTrustor extends MutexThread implements LogPrinter {
//
//	/** 日志格式正则表达式 **/
//	private final static String REGEX ="^\\s*([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+?)([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+)$";
//
//	/** 后缀 **/
//	private final static String SUFFIX = "^\\s*([\\p{ASCII}\\W]+?)\\s*$";
//	
//	/** 允许的日志容量,1万行！ **/
//	private static final int MAX_CAPACITY = 10000;
//
//	/** 保存日志文本 **/
//	private ArrayList<String> array;
//	
//	/** 延时时间 **/
//	private volatile long slientTime;
//	
//	/** 日志的单次发送量限制 **/
//	private volatile int pushLimit;
//
//	/**
//	 * 构造默认的图形界面节点日志代理
//	 */
//	protected DisplayLogTrustor() {
//		super();
//		array = new ArrayList<String>(DisplayLogTrustor.MAX_CAPACITY);
//		// 延时20秒
//		setSlientTime(20000L);
//		// 单次限制值
//		setLimit(5);
//	}
//	
//	/**
//	 * 设置延时间隔
//	 * @param value
//	 */
//	private void setSlientTime(long value) {
//		if (value < 1000L) {
//			slientTime = 1000L;
//		} else if (value >= 10000L) {
//			slientTime = 10000L;
//		} else {
//			slientTime = value;
//		}
//	}
//	
//	/**
//	 * 日志单次发送限制值
//	 * 
//	 * @param value 限制值
//	 */
//	private void setLimit(int value) {
//		if (value >= 5) {
//			pushLimit = value;
//		} else {
//			pushLimit = 5;
//		}
//	}
//
//	/**
//	 * 返回日志单次发送限制值
//	 * 
//	 * @return 限制值
//	 */
//	public int getLimit() {
//		return pushLimit;
//	}
//	
//	/**
//	 * 降低到最低值。
//	 * 下限：延时10秒，单次5个日志。
//	 */
//	public void low() {
//		slientTime = 10000L;
//		pushLimit = 5;
//	}
//
//	/**
//	 * 压力增大，降低日志处理，处理办法：<br>
//	 * 1. 提高延时时间，延时时间不得超过10秒。<br>
//	 * 2, 减少日志发送数量，日志发送数量不超过100。<br><br>
//	 * 
//	 * 这个方法被WatchLauncher.defaultSubProcess / TerminalLauncher.defaultSubProcess 调用。
//	 */
//	public void descent() {
//		// 增加延时
//		if (slientTime < 10000L) {
//			slientTime += 250L;
//		}
//		// 减少发送数量，5是下限
//		if (pushLimit > 5) {
//			pushLimit -= 5;
//		}
//	}
//
//	/**
//	 * CPU压力减少，提高日志处理率，处理办法：<br>
//	 * 1. 降低日志发送的延时时间，延时时间不得低于1秒。<br>
//	 * 2. 增加日志发送数量，日志数量不低于10个。<br><br>
//	 * 
//	 * 这个方法被WatchLauncher.defaultSubProcess / TerminalLauncher.defaultSubProcess 调用。
//	 */
//	public void rise() {
//		// 减少延时
//		if (slientTime > 1000L) {
//			slientTime -= 250L;
//		}
//		// 增加发送数量，50是上限
//		if (pushLimit < 50) {
//			pushLimit += 5;
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#init()
//	 */
//	@Override
//	public boolean init() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#process()
//	 */
//	@Override
//	public void process() {
//		// 延时...
//		delay(5000);
//		// 降到最低值
//		low();
//		
//		// 循环处理
//		while (!isInterrupted()) {
//			// 判断有日志，显示它们
//			boolean success = (array.size() > 0);
//			if (success) {
//				success = push();
//			}
//			
//			// 延时，空出CPU给其他GUI线程
//			// 原因：频率太快，会造成SWING线程队列压力过大；日志太多，导致图形界面资源用于日志显示，造成GUI刷新似乎停止，形成死锁假象！！！
//			// 优化：由WatchLauncher.defaultSubProcess/TerminalLauncher.defaultSubProcess方法判断CPU压力，然后自动调整延时时间和日志输出数量，最低1秒，最高10秒
//			delay(slientTime);
//		}
//	}
//	
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#finish()
//	 */
//	@Override
//	public void finish() {
//		array.clear();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.log.client.LogPrinter#print(java.lang.String)
//	 */
//	@Override
//	public void print(String log) {
//		// 超过规定容量，忽略它！
//		if (array.size() >= DisplayLogTrustor.MAX_CAPACITY) {
//			return;
//		}
//		// 锁定，保存！
//		super.lockSingle();
//		try {
//			array.add(log);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		} finally {
//			super.unlockSingle();
//		}
//	}
//
//	/**
//	 * 输出经过处理的一组日志
//	 * @return LogItem数组，没有是空指针
//	 */
//	protected List<LogItem> flush() {
//		ArrayList<String> subs = new ArrayList<String>();
//		// 锁定
//		super.lockSingle();
//		try {
//			// 确定输出上限，取最小的
//			int limit = (array.size() > pushLimit ? pushLimit : array.size());
//			// 逐个提取，保存
//			for (int i = 0; i < limit; i++) {
//				String log = array.remove(0);
//				subs.add(log);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		} finally {
//			super.unlockSingle();
//		}
//
//		// 空记录，忽略！
//		int size = subs.size();
//		if (size == 0) {
//			return null;
//		}
//		
////		System.out.printf("%d | %d - %d - %d\r\n", slientTime, array.size(), pushLimit, size);
//
//		// 逐一解析，然后输出！
//		ArrayList<LogItem> a = new ArrayList<LogItem>(size);
//		for (String text : subs) {
//			List<LogItem> items = splitLog(text);
//			a.addAll(items);
//		}
//		return a;
//	}
//
//	/**
//	 * 过滤最后的控制符号
//	 * @param input
//	 * @return
//	 */
//	private String suffix(String input) {
//		Pattern pattern = Pattern.compile(SUFFIX);
//		Matcher matcher = pattern.matcher(input);
//		if (matcher.matches()) {
//			return matcher.group(1);
//		}
//		return input;
//	}
//
//	/**
//	 * 判断它前面的标记符，将一行记录拆分成多行日志。
//	 * @param input 一行日志
//	 * @return 切割后的多行日志。
//	 */
//	private String[] split(String input) {
//		ArrayList<String> array = new ArrayList<String>();
//		while (true) {
//			Pattern pattern = Pattern.compile(REGEX);
//			Matcher matcher = pattern.matcher(input);
//			if (matcher.matches()) {
//				String first = matcher.group(1);
//				String second = matcher.group(2);
//
//				// 判断解析出错
//				int size = array.size();
//				if (size > 0) {
//					if (array.get(size - 1).compareTo(second) == 0) {
//						break;
//					}
//				}
//
//				array.add(suffix(first));
//				input = second;
//			} else {
//				// 不匹配是完整一行，退出
//				array.add(suffix(input));
//				break;
//			}
//		}
//		String[] a = new String[array.size()];
//		return array.toArray(a);
//	}
//
//	/**
//	 * 生成日志单元
//	 * @param input
//	 * @return
//	 */
//	private List<LogItem> splitLog(String input) {
//		String[] lines = split(input);
//		ArrayList<LogItem> logs = new ArrayList<LogItem>();
//		for(int i = 0; i < lines.length; i++) {
//			// 找到换行符，分解成多行
//			String[] subs = lines[i].split("([\\r\\n]+)");
//
//			byte family = 0; // 日志类型
//			if (subs[0].matches("^\\s*(?:DEBUG)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.DEBUG;
//			} else if (subs[0].matches("^\\s*(?:INFO)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.INFO;
//			} else if (subs[0].matches("^\\s*(?:WARNING)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.WARNING;
//			} else if (subs[0].matches("^\\s*(?:ERROR)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.ERROR;
//			} else if (subs[0].matches("^\\s*(?:FATAL)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$")) {
//				family = LogItem.FATAL;
//			}
//			logs.add(new LogItem(family, subs[0]));
//
//			for (int j = 1; j < subs.length; j++) {
//				if (family == LogItem.DEBUG) {
//					logs.add(new LogItem(LogItem.SUBDEBUG, subs[j]));
//				} else if (family == LogItem.INFO) {
//					logs.add(new LogItem(LogItem.SUBINFO, subs[j]));
//				} else if (family == LogItem.WARNING) {
//					logs.add(new LogItem(LogItem.SUBWARNING, subs[j]));
//				} else if (family == LogItem.ERROR) {
//					logs.add(new LogItem(LogItem.SUBERROR, subs[j]));
//				} else if (family == LogItem.FATAL) {
//					logs.add(new LogItem(LogItem.SUBFATAL, subs[j]));
//				}
//			}
//		}
//
//		return logs;
//	}
//
//	/**
//	 * 处理和推送日志到图形界面
//	 * @return 成功返回真，否则假
//	 */
//	protected abstract boolean push();
//}