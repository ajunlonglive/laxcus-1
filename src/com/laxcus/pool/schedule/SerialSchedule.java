/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.schedule;

/**
 * 串行工作<br><br>
 * 
 * 当多个线程竞争一个资源时，为避免造成资源写冲突，每次只允许一个线程使用这个资源。<br>
 * 
 * SerialSchedule接口可以放到其它类（如异步调用器）去实现，或者直接调用DefaultSerialSchedule类。<br><br>
 * 
 * SerialSchedule接口一个主要应用是共享写（PARALLEL WRITE）的后续操作。当一个INSERT任务在AID站点实现申请了共享写成功之后，
 * 命令分发到DATA站点，DATA站点为避免资源竞用造成的数据不一致现象，将通过SerialSchedule接口来锁定表资源，然后将数据写入磁盘和分发到从站点。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public interface SerialSchedule {

	/**
	 * 串行工作管理池发送已经锁定通知 <br><br>
	 * 
	 * 说明：<br>
	 * 如果SerialSchedule向SerialSchedulePool申请资源，SerialSchedulePool不能立即批准，SerialSchedule将进入等待状态
	 * SerialSchedulePool会把这个SerialSchedule句柄保存，等待其它SerialSchedule处理完毕后，调用“attach”方法通知它申请的资源已经锁定。<br><br>
	 * 
	 * “attach”方法只能被SerialSchedulePool调用。
	 */
	void attach();

	/**
	 * 判断已经锁定资源
	 * @return - 返回真或者假
	 */
	boolean isAttached();
}