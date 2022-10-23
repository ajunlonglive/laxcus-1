/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.event;

import java.text.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.log.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * SWING事件线程分派器
 * 
 * @author scott.liang
 * @version 1.0 1/3/2020
 * @since laxcus 1.0
 */
public class SwingDispatcher extends VirtualThread {
	
	class EventObject implements Comparable<EventObject> {
		long startTime;

		long lastTime;

		int count;

		String className;

		boolean synch;
		
		public EventObject(String s, boolean synch) {
			super();
			count = 0;
			startTime = lastTime = System.currentTimeMillis();
			setClassName(s);
			setSynch(synch);
		}

		public long getStartTime() {
			return startTime;
		}
		
		public long getLastTime(){
			return lastTime;
		}
		
		public void setSynch(boolean b){
			synch = b;
		}
		
		public boolean isSynch(){
			return synch;
		}
		
		public void setClassName(String s){
			className = s;
		}
		
		public String getClassName(){
			return className;
		}
		
		public void add() {
			count++;
			lastTime = System.currentTimeMillis();
		}

		public int getCount() {
			return count;
		}
		

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(EventObject that) {
			int ret = Laxkit.compareTo(count, that.count);
			return (ret > 0 ? -1 : (ret < 0 ? 1 : 0));
		}
		
	}
	
	private Map<String, EventObject> objects = new TreeMap<String,EventObject>();

	/** 实例 **/
	private static SwingDispatcher selfHandle = new SwingDispatcher();
	
	/** 互斥锁(多读/单写模式) */
	private SingleLock lock = new SingleLock();

	/** 线程队列  **/
	private ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

//	/** 线程睡眠延时间隔时间，默认是5秒 **/
//	private volatile long sleepInterval = 5000L;
//
//	/** 线程睡眠最低限制阀值是200毫秒 **/
//	private final long mixInterval = 200L;

	/** 最后一个事件对象 **/
	private SwingEvent lastElement;

	/** 拒绝接受事件，默认是假 **/
	private volatile boolean refuseEvent;

	/** 让线程暂时工作和暂时工作时间 **/
	private volatile boolean slack;
	private volatile long slackTime;
	
	/**
	 * 构造默认的SWING事件线程分派器
	 */
	private SwingDispatcher() {
		super();

		// 拒绝接受事件
		refuseEvent = false;

		// 暂停是假
		setSlack(0);
	}

	/**
	 * 返回SWING事件线程分派器
	 * @return 
	 */
	public static SwingDispatcher getInstance() {
		return SwingDispatcher.selfHandle;
	}
	
	/**
	 * 设置一次让线程强制暂停的时间
	 * @param ms 毫秒
	 */
	public void setSlack(long ms) {
		if (ms > 0) {
			slackTime = ms;
			slack = true;
		} else {
			slackTime = 0;
			slack = false;
		}
	}

	/**
	 * 拒绝新的事件或者否
	 * @param b 真或者假
	 */
	public void setRefuseEvent(boolean b){
		refuseEvent = b;
	}

	/**
	 * 判断拒绝接受新的事件
	 * @return 返回真或者假
	 */
	public boolean isRefuseEvent(){
		return refuseEvent;
	}

	/**
	 * 返回线程成员数目
	 * @return 整数
	 */
	public int elements() {
		// 锁定输出
		lock.lock();
		try {
			return array.size();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 返回事件类
	 * @return
	 */
	public String[] getEventClasses() {
		ArrayList<String> a = new ArrayList<String>();

		// 锁定输出
		lock.lock();
		try {
			for (SwingEvent e : array) {
				a.add(e.getClass().getName());
			}
		} catch(Throwable e) {

		} finally {
			lock.unlock();
		}

		// 输出
		String[] strs = new String[a.size()];
		return a.toArray(strs);
	}

	/**
	 * 判断处于空闲状态
	 * @return 返回真或者假
	 */
	public boolean isIdle() {
		boolean success = false;
		// 锁定判断
		lock.lock();
		try {
			success = (array.size() == 0);
			if (success) {
				// 如果最后一个对象有效，判断它已经退出
				if (lastElement != null) {
					success = lastElement.isExit();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlock();
		}
		return success;
	}

	/**
	 * 判断处于繁忙状态
	 * @return 返回真或者假
	 */
	public boolean isBusy() {
		return !isIdle();
	}

	/**
	 * 把一个线程放入队列
	 * @param e 线程实例
	 * @return 成功返回真，否则假
	 */
	public static boolean invokeThread(SwingEvent e) {
		return SwingDispatcher.getInstance().add(e);
	}

	/**
	 * 把一批线程放入队列
	 * @param a 线程实例数组
	 * @return 返回增加成员数目
	 */
	public static int invokeThreads(Collection<SwingEvent> a) {
		return SwingDispatcher.getInstance().addAll(a);
	}

//	/**
//	 * 保存实例
//	 * @param event
//	 * @return 成功返回真，否则假
//	 */
//	private boolean add(SwingEvent event) {
//		// 判断空指针!
//		Laxkit.nullabled(event);
//		// 如果拒绝接受事件
//		if (refuseEvent) {
//			return false;
//		}
//
//		boolean empty = false;
//		boolean success = false;
//		// 锁定
//		lock.lock();
//		try {
//			empty = array.isEmpty();
//			event.doReady(); // 进入就绪状态
//			success = array.add(event);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			lock.unlock();
//		}
////		// 唤醒线程
////		if (empty && success) {
////			wakeupAll();
////		}
//		
//		// 唤醒线程
//		if (success) {
//			wakeup();
//		}
//
//		return success;
//	}
	
	/**
	 * 保存实例
	 * @param event
	 * @return 成功返回真，否则假
	 */
	private boolean add(SwingEvent event) {
		// 判断空指针!
		Laxkit.nullabled(event);
		// 如果拒绝接受事件
		if (refuseEvent) {
			return false;
		}

		boolean success = false;
		// 锁定
		lock.lock();
		try {
			event.doReady(); // 进入就绪状态
			success = array.add(event);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlock();
		}
		
		// 唤醒线程
		if (success) {
			wakeup();
		}

		return success;
	}
	
//	/**
//	 * 保存一批实例
//	 * @param event
//	 * @return 成功返回真，否则假
//	 */
//	private int addAll(Collection<SwingEvent> events) {
//		// 判断空指针!
//		Laxkit.nullabled(events);
//		// 如果拒绝接受事件
//		if (refuseEvent) {
//			return 0;
//		}
//		
//		boolean empty = false;
//		int count = 0;
//		// 锁定
//		lock.lock();
//		try {
//			int size = array.size();
//			empty = (size == 0);
//			// 进入到准备就绪状态
//			for (SwingEvent e : events) {
//				e.doReady();
//			}
//			// 保存
//			array.addAll(events);
//			// 统计增加成员数目
//			count = array.size() - size;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			lock.unlock();
//		}
//	
////		// 唤醒线程
////		if (empty && count > 0) {
////			wakeupAll();
////		}
//		
//		// 唤醒线程
//		if (count > 0) {
//			wakeup();
//		}
//
//		return count;
//	}

	/**
	 * 保存一批实例
	 * @param event
	 * @return 成功返回真，否则假
	 */
	private int addAll(Collection<SwingEvent> events) {
		// 判断空指针!
		Laxkit.nullabled(events);
		// 如果拒绝接受事件
		if (refuseEvent) {
			return 0;
		}
		
		int count = 0;
		// 锁定
		lock.lock();
		try {
			int size = array.size();
			// 进入到准备就绪状态
			for (SwingEvent e : events) {
				e.doReady();
			}
			// 保存
			array.addAll(events);
			// 统计增加成员数目
			count = array.size() - size;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlock();
		}
		// 唤醒线程
		if (count > 0) {
			wakeup();
		}

		return count;
	}
	
	/**
	 * 弹出一个事件
	 * @return 返回实例或者空指针
	 */
	private SwingEvent popup() {
		// 锁定
		lock.lock();
		try {
			int size = array.size();
			for (int index = 0; index < size; index++) {
				// 取一个事件
				SwingEvent event = array.get(index);
				// 判断达到触发时间
				if (event.isTouched()) {
					// 删除这个事件
					array.remove(index);
					return event;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlock();
		}
		// 不成立，返回空指针
		return null;
	}
	
	/**
	 * 判断保存有成员
	 * @return
	 */
	public int size() {
		lock.lock();
		try {
			return array.size();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlock();
		}
		return 0;
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
		Logger.info(this, "process", "into...");

		final long minTime = 1000;
		final long maxTime = 10000;
		final long gapTime = 500;
		long sleepTime = 1000;

		while (!isInterrupted()) {
			// 判断被强制暂时
			if (slack) {
				delay(slackTime);
				setSlack(0); // 恢复成初始值
			}
			
			// 取出实例
			lastElement = popup();
			// 没有，采用规定时间延时
			if (lastElement == null) {
				// 如果有记录时，但是没有达到触发时间，用最小延时
				if (size() > 0) {
					delay(sleepTime = minTime);
				} else {
					// 延时...
					delay(sleepTime);
					// 增加延时时间
					sleepTime += gapTime;
					if (sleepTime > maxTime) {
						sleepTime = maxTime;
					}
				}
				// 返回
				continue;
			}
			
			// 记录参数
			addObject(lastElement);

			// 判断是等待
			boolean waiting = lastElement.isWaiting();

			//			System.out.printf("%s %s\n", (waiting ? "等待" : "立即"), event
			//					.getClass().getName());
			
			// 特别注意! GUI界面卡死，和Swing线程调用频繁强相关，所以无论哪种线程，在处理完都做延时处理。
			// 这个时间，是给Swing线程处理GUI留出时间

			// 同步放入队列
			if (waiting) {
				try {
					SwingUtilities.invokeAndWait(lastElement);
					// 凡是同步，完成后做20毫秒的延时，看是不是能够避免UI界面卡死的现象...
					delay(20L);
					// 等待结束...
					waitForSynchronousEvent(lastElement);
				} catch (Exception e) {
					String clsName = lastElement.getClass().getName();
					Logger.error(clsName, e);
				}
			} else {
				// 异步放入队列
				SwingUtilities.invokeLater(lastElement);
				// 异步，延时10秒，能避免UI界面卡死
				delay(10L);
				// 等待结束
				waitForAsynchronousEvent(lastElement);
			}
			
			// 减少延时时间
			sleepTime -= gapTime;
			if (sleepTime < minTime) {
				sleepTime = minTime;
			}
		}

		// 打印事件
		printObjects();

		Logger.info(this, "process", "exit...");
	}
	
	/**
	 * 等待同步事件结束
	 * @param event
	 */
	private void waitForSynchronousEvent(SwingEvent event) {
		long maxTime = 20000L;
		long startTime = System.currentTimeMillis();
		while (!event.isExit()) {
			// 延时100毫秒
			event.sleep(100L);
			// 线程中断，退出！
			if (isInterrupted()) {
				break;
			}
			// 20秒钟仍然没有反应，是达到最大间隔时间，退出
			if (System.currentTimeMillis() - startTime >= maxTime) {
				Logger.error(this, "waitForEvent", "SwingUtilities blocked!");
				break;
			}
		}
	}
	
	/**
	 * 等待异步事件结束
	 * @param event
	 */
	private void waitForAsynchronousEvent(SwingEvent event) {
		int count = 0;
		while (!event.isExit()) {
			// 延时100毫秒
			event.sleep(100L);
			// 线程中断，退出
			if (isInterrupted()) {
				break;
			}
			// 最大2秒，超过就退出...
			count++;
			if (count >= 20) {
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
	}

	/**
	 * 记录对象
	 * @param e
	 */
	private void addObject(SwingEvent e) {
		String className = e.getClass().getName();
		boolean synch = e.isWaiting();

		// 锁定，记录
		lock.lock();
		try {
			EventObject obj = objects.get(className);
			if (obj == null) {
				obj = new EventObject(className, synch);
				objects.put(obj.getClassName(), obj);
			}
			obj.add();
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 格式化时间
	 * @param gap
	 * @return
	 */
	private String getTime(long gap) {
		long hour = 60 * 60 * 1000;
		long minute = 60 * 1000;
		long second = 1000;
		// 小时
		long h = gap / hour;
		gap -= (h * hour);
		// 分钟
		long m = gap / minute;
		gap -= (m * minute);
		// 秒
		long s = gap / second;
		gap -= (s * second);
		// 毫秒
		long ms = gap;
		// 结果
		return String.format("%d:%d:%d.%d", h, m, s, ms);
	}
	
	/**
	 * 在结束时打印结果
	 */
	private void printObjects() {
		ArrayList<EventObject> a = new ArrayList<EventObject>();
		a.addAll(objects.values());

		java.util.Collections.sort(a);

		SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

		for (EventObject o : a) {
			long st = o.getStartTime();
			long lt = o.getLastTime();
			String startTime = style.format(new java.util.Date(st));
			String lastTime = style.format(new java.util.Date(lt));
			String gap = getTime(lt - st);

			String text = String.format("%s, %s %d, %s %s %s", o.getClassName(),
					(o.isSynch() ? "Synchronous" : "None synchronous"), o.getCount(),
					startTime, lastTime, gap);
			Logger.info(this, "printObjects", "%s", text);
		}
	}

}

///* (non-Javadoc)
// * @see com.laxcus.thread.VirtualThread#process()
// */
//@Override
//public void process() {
//	Logger.info(this, "process", "into...");
//
//	while (!isInterrupted()) {
//		SwingEvent event = popup();
//		// 没有，延时
//		if (event == null) {
//			delay(5000);
//			continue;
//		}
//
//		// 设置句柄
//		event.setDispatcher(this);
//
//		// 事件中，延时
//		waitLaster();
//
//		// 放入队列
//		SwingUtilities.invokeLater(event);
//
//		// 判断进入线程
//		//			while (true) {
//		//				delay(20L);
//		//				if (event.isLaunched()) break;
//		//			}
//
//		//			while (!event.isLaunched()) {
//		//				delay(100L);
//		//			}
//
//		// 判断要求等待直到线程完成工作
//		if (event.isWaiting()) {
//			while (true) {
//				delay(100L);
//				// 停止的条件
//				if (event.isExit() || isInterrupted()) {
//					break;
//				}
//			}
//		}
//
//		// 事件中，延时
//		waitLaster();
//	}
//
//	Logger.info(this, "process", "exit...");
//}


///* (non-Javadoc)
// * @see com.laxcus.thread.VirtualThread#process()
// */
//@Override
//public void process() {
//	Logger.info(this, "process", "into...");
//
//	while (!isInterrupted()) {
//		SwingEvent event = popup();
//		// 没有，采用规定时间延时
//		if (event == null) {
//			delay(sleepInterval);
//			continue;
//		}
//
//		boolean waiting = event.isWaiting();
//
//		// // 设置句柄
//		// event.setDispatcher(this);
//
//		// 事件中，延时
//		if (waiting) {
//			waitLaster();
//		}
//
//		// 放入队列
//		SwingUtilities.invokeLater(event);
//
//		// 判断要求等待直到线程完成工作
//		if (waiting) {
//			while (true) {
//				delay(100L);
//				// 停止的条件
//				if (event.isExit() || isInterrupted()) {
//					break;
//				}
//			}
//		}
//
//		// 事件中，延时
//		if (waiting) {
//			waitLaster();
//		}
//	}
//
//	Logger.info(this, "process", "exit...");
//}

///* (non-Javadoc)
// * @see com.laxcus.thread.VirtualThread#process()
// */
//@Override
//public void process() {
//	Logger.info(this, "process", "into...");
//
//	while (!isInterrupted()) {
//		SwingEvent event = popup();
//		// 没有，采用规定时间延时
//		if (event == null) {
//			delay(sleepInterval);
//			continue;
//		}
//
//		boolean waiting = event.isWaiting();
//
//		// 事件中，延时
//		if (waiting) {
//			waitLaster();
//		}
//		
////		System.out.printf("%s\n", event.getClass().getName());
//
////		// 放入队列
////		SwingUtilities.invokeLater(event);
//		
//		// 同步放入队列
//		try {
//			SwingUtilities.invokeAndWait(event);
//		} catch (Exception  e) {
//			Logger.error(e);
//		}
//
//		// 判断要求等待直到线程完成工作
//		if (waiting) {
//			while (true) {
//				delay(100L);
//				// 停止的条件
//				if (event.isExit() || isInterrupted()) {
//					break;
//				}
//			}
//		}
//
//		// 事件中，延时
//		if (waiting) {
//			waitLaster();
//		}
//	}
//
//	Logger.info(this, "process", "exit...");
//}

///**
// * 等待到最后
// */
//private void waitLaster() {
//	final long interval = 20000L;
//	final long startTime = System.currentTimeMillis();
//	// 事件中，延时
//	while (SwingUtilities.isEventDispatchThread()) {
//		// 延时20毫秒
//		delay(20L);
//		// 线程中断，退出！
//		if (isInterrupted()) {
//			break;
//		}
//		// 20秒钟仍然没有反应，是达到最大间隔时间，退出
//		if (System.currentTimeMillis() - startTime >= interval) {
//			Logger.error(this, "waitLaster", "SwingUtilities blocked!");
//			break;
//		}
//	}
//}


///**
// * 弹出一个事件
// * @return 返回实例或者空指针
// */
//private SwingEvent popup2() {
//	// 锁定
//	lock.lock();
//	try {
//		if (array.size() > 0) {
//			return array.remove(0);
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		lock.unlock();
//	}
//	return null;
//}

//private void waitLaster(SwingEvent event) {
//final long interval = 20000L;
//final long startTime = System.currentTimeMillis();
//// 事件中，延时
//while (SwingUtilities.isEventDispatchThread()) {
//	// 延时20毫秒
//	delay(20L);
//	// 线程中断，退出！
//	if (isInterrupted()) {
//		break;
//	}
//	// 20秒钟仍然没有反应，是达到最大间隔时间，退出
//	if (System.currentTimeMillis() - startTime >= interval) {
//		Logger.error(this, "waitLaster", "SwingUtilities blocked!");
//		break;
//	}
//}
//}


///* (non-Javadoc)
// * @see com.laxcus.thread.VirtualThread#process()
// */
//@Override
//public void process() {
//	Logger.info(this, "process", "into...");
//
//	while (!isInterrupted()) {
//		SwingEvent event = popup();
//		// 没有，采用规定时间延时
//		if (event == null) {
//			delay(sleepInterval);
//			continue;
//		}
//
//		boolean waiting = event.isWaiting();
//
////		System.out.printf("%s %s\n", (waiting ? "等待" : "立即"), event
////				.getClass().getName());
//
//		// 同步放入队列
//		if (waiting) {
//			try {
//				SwingUtilities.invokeAndWait(event);
//				waitForEvent(event); // 等待结束...
//			} catch (Exception e) {
//				Logger.error(e);
//			}
//		} else {
//			// 异步放入队列
//			SwingUtilities.invokeLater(event);
//		}
//	}
//
//	Logger.info(this, "process", "exit...");
//}


///**
// * 保存一批实例
// * @param event
// * @return 成功返回真，否则假
// */
//private int addAll(Collection<SwingEvent> events) {
//	// 判断空指针!
//	Laxkit.nullabled(events);
//
//	int count = 0;
//	// 锁定
//	lock.lock();
//	try {
//		int size = array.size();
//		array.addAll(events);
//		// 统计增加成员数目
//		count = array.size() - size;
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		lock.unlock();
//	}
//
//	// 唤醒线程
//	if (count > 0) {
//		wakeup();
//	}
//
//	return count;
//}


///**
// * 判断处于空闲状态
// * @return 返回真或者假
// */
//public boolean isIdle() {
//	return (!SwingUtilities.isEventDispatchThread() && array.size() == 0);
//}


///**
// * 通知解决锁定
// */
//public void done() {
//	//		System.out.printf("drop %s\n",e.getClass().getSimpleName());
//	wakeup();
//}
