/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.util.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.display.*;

/**
 * TERMINAL节点日志代理
 * 
 * @author scott.liang
 * @version 1.0 9/17/2019
 * @since laxcus 1.0
 */
public final class TerminalLogTrustor extends DisplayLogTrustor {
	
	/** TERMINAL节点日志代理实例 **/
	private static TerminalLogTrustor selfHandle = new TerminalLogTrustor();

	/**
	 * 构造默认的TERMINAL节点日志代理
	 */
	private TerminalLogTrustor() {
		super();
		setSleepTime(5);
	}

	/**
	 * 返回TERMINAL节点日志代理句柄实例
	 * @return TerminalLogTrustor句柄
	 */
	public static TerminalLogTrustor getInstance() {
		return TerminalLogTrustor.selfHandle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.DisplayLogTrustor#push()
	 */
	@Override
	protected boolean push() {
		TerminalWindow window = TerminalLauncher.getInstance().getWindow();
		if (!window.isVisible()) {
			return false;
		}

		// 输出日志
		List<LogItem> logs = flush();
		if (logs == null || logs.isEmpty()) {
			return false;
		}

		// 日志进入显示
		TerminalLogPanel panel = window.getLogPanel();
		panel.pushLogs(logs);
		return true;
	}
}

//public final class TerminalLogTrustor extends VirtualPool implements LogPrinter {
//
//	private final static String REGEX ="^\\s*([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+?)([DEBUG|INFO|WRANING|ERROR|FATAL]+\\:[\\d\\-\\:\\s]+[\\p{ASCII}\\W]+)$";
//	private final static String SUFFIX = "^\\s*([\\p{ASCII}\\W]+?)\\s*$";
//
//	/** TERMINAL节点日志代理实例 **/
//	private static TerminalLogTrustor selfHandle = new TerminalLogTrustor();
//
//	/** 保存日志文本 **/
//	private ArrayList<String> array = new ArrayList<String>(2000);
//
//	/**
//	 * 构造默认的TERMINAL节点日志代理
//	 */
//	private TerminalLogTrustor() {
//		super();
//		setSleepTime(5);
//	}
//
//	/**
//	 * 返回TERMINAL节点日志代理句柄实例
//	 * @return TerminalLogTrustor句柄
//	 */
//	public static TerminalLogTrustor getInstance() {
//		return TerminalLogTrustor.selfHandle;
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
//		while (!isInterrupted()) {
//			boolean success = (array.size() > 0);
//			if (success) {
//				success = subprocess();
//			}
//			if (!success) {
//				sleep();
//			}
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
//		super.lockSingle();
//		try {
//			array.add(log);
//		} catch(Throwable e) {
//
//		} finally {
//			super.unlockSingle();
//		}
//		// 唤醒线程
//		wakeup();
//	}
//
//	/**
//	 * 子级处理
//	 */
//	private boolean subprocess() {
//		TerminalWindow window = TerminalLauncher.getInstance().getWindow();
//		if (!window.isVisible()) {
//			return false;
//		}
//		
//		int size = array.size();
//		if (size == 0) {
//			return false;
//		}
//
//		ArrayList<LogItem> logs = new ArrayList<LogItem>(size);
//		for (int i = 0; i < size; i++) {
//			String text = pop();
//			if (text != null) {
//				List<LogItem> items = splitLog(text);
//				logs.addAll(items);
//			}
//		}
//		
//		// 日志进入显示
//		TerminalLogPanel panel = window.getLogPanel();
//		panel.pushLogs(logs);
//		return true;
//	}
//
//	/**
//	 * 弹出一行日志
//	 * @return 一行日志，或者空指针
//	 */
//	private String pop() {
//		super.lockSingle();
//		try {
//			if (array.size() > 0) {
//				return array.remove(0);
//			}
//		} catch (Throwable e) {
//
//		} finally {
//			super.unlockSingle();
//		}
//		return null;
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
//	 * @param input
//	 * @return
//	 */
//	private String[] split(String input) {
//		ArrayList<String> array = new ArrayList<String>();
//		while(true) {
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
//		ArrayList<LogItem> array = new ArrayList<LogItem>();
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
//			array.add(new LogItem(family, subs[0]));
//
//			for (int j = 1; j < subs.length; j++) {
//				if (family == LogItem.DEBUG) {
//					array.add(new LogItem(LogItem.SUBDEBUG, subs[j]));
//				} else if (family == LogItem.INFO) {
//					array.add(new LogItem(LogItem.SUBINFO, subs[j]));
//				} else if (family == LogItem.WARNING) {
//					array.add(new LogItem(LogItem.SUBWARNING, subs[j]));
//				} else if (family == LogItem.ERROR) {
//					array.add(new LogItem(LogItem.SUBERROR, subs[j]));
//				} else if (family == LogItem.FATAL) {
//					array.add(new LogItem(LogItem.SUBFATAL, subs[j]));
//				}
//			}
//		}
//
//		return array;
//	}
//}
