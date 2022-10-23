/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.lock;

/**
 * 互斥锁。<br><br>
 * 
 * 互斥锁的特点是一个时间内允许存在“多个读”操作，或者“一个写”操作的存在。<br>
 * 当执行多读(multi)操作时，单写(single)被排斥，但是不再允许多读进入，直到前面的多读操作全部完成，单写进入锁定。
 * 做单写(single)锁定操作时，多读(multi)被排斥，直到这个单写结束，多读进入状态。
 *
 * 这是一个非常重要的类，用于资源竞用环境!!!<br>
 *
 * @author scott.liang 
 * @version 1.0 8/17/2008
 * @since laxcus 1.0
 */
public final class MutexLock {

	/** 多读锁，允许同时有多个并发存在，同时与单写锁互斥 */
	private volatile int multiCount;
	/** is true， 多读锁进入等待状态 **/
	private volatile boolean multiInto;

	/** 单写锁，一个时间内只允许有一个单写锁存在，同时与多读锁互斥 */
	private volatile boolean single;
	/** is true，单写锁进入等待状态 **/
	private volatile boolean singleInto;

	/** 多读锁/单写锁在锁定不成功时的等待时间，单位:毫秒 */
	private long timeout;

	/**
	 * 建立默认的互斥锁
	 */
	public MutexLock() {
		super();
		multiCount = 0;
		single = false;
		multiInto = false;
		singleInto = false;
		setTimeout(5L);
	}

	/**
	 * 建立互斥锁，指定锁定延时时间
	 * @param timeout 毫秒
	 */
	public MutexLock(long timeout) {
		this();
		setTimeout(timeout);
	}

	/**
	 * 锁定等待延时，必须大于0。单位：毫秒
	 * @param ms 延时时间
	 */
	public void setTimeout(long ms) {
		if (ms > 0L) {
			timeout = ms;
		}
	}

	/**
	 * 锁定等待时间，单位：毫秒
	 * @return 为毫秒为单位的锁定等待时间
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 判断是否多读锁定
	 * @return 返回真或者假
	 */
	public boolean isLockMulti() {
		return multiCount > 0;
	}
	
	/**
	 * 返回多锁成员数目
	 * @return 整数值
	 */
	public int getLockMulti() {
		return multiCount;
	}

	/**
	 * 判断是否单写锁定
	 * @return 返回真或者假
	 */
	public boolean isLockSingle() {
		return single;
	}


	/**
	 * 读写互斥锁定操作。允许多读锁同时有多个锁定，或者单写锁有一个锁定。<br>
	 * @param multi 如果是多向锁为true，否则是false
	 * @param singleKeep 如果前面有写操作锁定，这个写操作是否等待。这个参数只限于写操作状态。
	 * @return 锁定成功返回TRUE，失败返回FALSE
	 */
	private synchronized boolean lock(boolean multi, boolean singleKeep) {
		if (multi) {
			// 多读锁进入状态
			multiInto = true;
			// 如果单向锁处于等待状态，延时等待，直到单向锁解除锁定
			while (single) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
				}
			}
			multiCount++;
			multiInto = false;		//多读锁退出状态
		} else {
			// 单写锁进入状态
			singleInto = true;
			// 如果单写锁已经锁定，进入以下判断
			if (single) {
				// 单写锁定但是不需要持续等待时，返回FALSE
				if (!singleKeep) {
					singleInto = false; // 解除状态
					return false;
				}
				// 延时等待直到解除锁定
				while (single) {
					try {
						wait(timeout);
					} catch (InterruptedException e) {
					}
				}
			}
			// 等待多读锁全部解锁
			while (multiCount > 0) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
				}
			}
			single = true;
			singleInto = false;//单写锁退出状态
		}
		return true;
	}

	/**
	 * 解除单写/多读锁定
	 * @param multi 如果多向锁是true，否则是false
	 * @return 解锁成功返回真，否则假
	 */
	private synchronized boolean unlock(boolean multi) {
		if (multi) {
			// 小于1，多锁未锁定，不接受
			if (multiCount < 1) {
				return false;
			}
			multiCount--;
			// 单写锁处于进入等待状态，多读锁全部结束，这时候唤醒它
			if (singleInto && multiCount == 0) {
				try {
					notify();
				} catch (IllegalMonitorStateException e) {
				}
			}
		} else {
			// 单写锁未锁定，不接受解锁
			if (!single) {
				return false;
			}
			single = false;
			// 任何一个锁处于等待状态，唤醒它
			if (multiInto || singleInto) {
				try {
					notify();
				} catch (IllegalMonitorStateException e) {
				}
			}
		}
		return true;
	}

	/**
	 * 多读锁定，直到操作被接受。如果单写锁已经锁定，必须等待单写锁解锁
	 * @return 总是返回真
	 */
	public boolean lockMulti() {
		return lock(true, false);
	}

	/**
	 * 多读解锁
	 * @return 返回真或者假
	 */
	public boolean unlockMulti() {
		return unlock(true);
	}

	/**
	 * 单写锁定，直到否则才返回
	 * @return 返回真或者假
	 */
	public boolean lockSingle() {
		return lock(false, true);
	}

	/**
	 * 单写锁定，如果不能立即锁定时，选择是否等待
	 * @param keep true，等待直到锁定，否则是false
	 * @return 返回真或者假
	 */
	public boolean lockSingle(boolean keep) {
		return lock(false, keep);
	}

	/**
	 * 单写解锁
	 * @return 返回真或者假
	 */
	public boolean unlockSingle() {
		return unlock(false);
	}


	//	/** 多读锁，允许同时有多个并发存在，同时与单写锁互斥 */
	//	private int multiCount;
	//	/** is true， 多读锁进入等待状态 **/
	//	private boolean multiInto;
	//
	//	/** 单写锁，一个时间内只允许有一个单写锁存在，同时与多读锁互斥 */
	//	private boolean single;
	//	/** is true，单写锁进入等待状态 **/
	//	private boolean singleInto;
	//	
	//	/**
	//	 * 读写互斥锁定操作。允许多读锁同时有多个锁定，或者单写锁有一个锁定。<br>
	//	 * @param multi 如果是多向锁为true，否则是false
	//	 * @param singleKeep 如果前面有写操作锁定，这个写操作是否等待。这个参数只限于写操作状态。
	//	 * @return 锁定成功返回TRUE，失败返回FALSE
	//	 */
	//	private boolean __lock(boolean multi, boolean singleKeep) {
	//		if (multi) {
	//			
	//			// 多读锁进入状态
	//			multiInto = true;
	//			// 如果单向锁处于等待状态，延时等待，直到单向锁解除锁定
	//			while (single) {
	//				try {
	//					wait(timeout);
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//			System.out.printf("multi into! %s\n", single);
	//			multiCount++;
	//			multiInto = false;		//多读锁退出状态
	//		} else {
	//			
	//			// 单写锁进入状态
	//			singleInto = true;
	//			// 如果单写锁已经锁定，进入以下判断
	//			if (single) {
	//				// 单写锁定但是不需要持续等待时，返回FALSE
	//				if (!singleKeep) {
	//					singleInto = false; // 解除状态
	//					return false;
	//				}
	//				// 延时等待直到解除锁定
	//				while (single) {
	//					try {
	//						wait(timeout);
	//					} catch (InterruptedException e) {
	//						e.printStackTrace();
	//					}
	//				}
	//			}
	//			// 等待多读锁全部解锁
	//			while (multiCount > 0) { 		
	//				try {
	//					wait(timeout);
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//			System.out.printf("single into! %s\n", multiCount > 0);
	//			single = true;
	//			singleInto = false;//单写锁退出状态
	//		}
	//		return true;
	//	}
	//
	//	/**
	//	 * 解除单写/多读锁定
	//	 * @param multi 如果多向锁是true，否则是false
	//	 * @return 解锁成功返回真，否则假
	//	 */
	//	private boolean __unlock(boolean multi) {
	//		if (multi) {
	//			System.out.println("multi exit!");
	//			// 小于1，多锁未锁定，不接受
	//			if (multiCount < 1) {
	//				return false;
	//			}
	//			multiCount--;
	//			// 单写锁处于进入等待状态，多读锁全部结束，这时候唤醒它
	//			if (singleInto && multiCount == 0) {
	//				try {
	//					notify();
	//				} catch (IllegalMonitorStateException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		} else {
	//			System.out.println("single exit!");
	//			// 单写锁未锁定，不接受解锁
	//			if (!single) {
	//				return false;
	//			}
	//			single = false;
	//			// 任何一个锁处于等待状态，唤醒它
	//			if (multiInto || singleInto) {
	//				try {
	//					notify();
	//				} catch (IllegalMonitorStateException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//		return true;
	//	}
	//
	//	/**
	//	 * 这里做为唯一的同步入口，进行加锁和解锁的操作
	//	 * @param lock 锁定模式或者否
	//	 * @param multi 多向锁
	//	 * @param singleKeep 如果是单向锁，保持执行锁定
	//	 * @return
	//	 */
	//	private synchronized boolean switchTo(boolean lock, boolean multi, boolean singleKeep) {
	//		if (lock) {
	//			return __lock(multi, singleKeep);
	//		} else {
	//			return __unlock(multi);
	//		}
	//	}
	//
	//	/**
	//	 * 多读锁定，直到操作被接受。如果单写锁已经锁定，必须等待单写锁解锁
	//	 * @return 总是返回真
	//	 */
	//	public boolean lockMulti() {
	//		return switchTo(true, true, false);
	//	}
	//
	//	/**
	//	 * 多读解锁
	//	 * @return 返回真或者假
	//	 */
	//	public boolean unlockMulti() {
	//		return switchTo(false, true, false);
	//	}
	//
	//	/**
	//	 * 单写锁定，直到否则才返回
	//	 * @return 返回真或者假
	//	 */
	//	public boolean lockSingle() {
	//		return switchTo(true, false, true);
	//	}
	//
	//	/**
	//	 * 单写锁定，如果不能立即锁定时，选择是否等待
	//	 * @param keep true，等待直到锁定，否则是false
	//	 * @return 返回真或者假
	//	 */
	//	public boolean lockSingle(boolean keep) {
	//		return switchTo(true, false, keep);
	//	}
	//
	//	/**
	//	 * 单写解锁
	//	 * @return 返回真或者假
	//	 */
	//	public boolean unlockSingle() {
	//		return switchTo(false, false, false);
	//	}
	

	//	/**
	//	 * 读写互斥锁定操作。允许多读锁同时有多个锁定，或者单写锁有一个锁定。<br>
	//	 * @param multi 如果是多向锁为true，否则是false
	//	 * @param singleKeep 如果前面有写操作锁定，这个写操作是否等待。这个参数只限于写操作状态。
	//	 * @return 锁定成功返回TRUE，失败返回FALSE
	//	 */
	//	private synchronized boolean lock(boolean multi, boolean singleKeep) {
	//		if (multi) {
	//			// 多读锁进入状态
	//			multiInto = true;
	//			// 如果单向锁处于等待状态，延时等待，直到单向锁解除锁定
	//			while (single) {
	//				try {
	//					wait(timeout);
	//				} catch (InterruptedException e) {
	//				}
	//			}
	//			multiCount++;
	//			multiInto = false;		//多读锁退出状态
	//		} else {
	//			// 如果单写锁已经锁定，进入以下判断
	//			if (single) {
	//				// 单写锁定但是不需要持续等待时，返回FALSE
	//				if (!singleKeep) {
	//					return false;
	//				}
	//				// 如果需要持续等待进入延时
	//				while (!single) {
	//					try {
	//						wait(timeout);
	//					} catch (InterruptedException e) {
	//					}
	//				}
	//			}
	//			// 单写锁进入状态
	//			singleInto = true;
	//			// 等待多读锁全部解锁
	//			while (multiCount > 0) { 		
	//				try {
	//					wait(timeout);
	//				} catch (InterruptedException e) {
	//				}
	//			}
	//			single = true;
	//			singleInto = false;//单写锁退出状态
	//		}
	//		return true;
	//	}

	//	/**
	//	 * 解除单写/多读锁定
	//	 * @param multi 如果多向锁是true，否则是false
	//	 * @return 解锁成功返回真，否则假
	//	 */
	//	private synchronized boolean unlock(boolean multi) {
	//		if (multi) {
	//			// 小于1，多锁未锁定，不接受
	//			if (multiCount < 1) {
	//				return false;
	//			}
	//			multiCount--;
	//			// 单写锁处于进入等待状态，多读锁全部结束，这时候唤醒它
	//			if (singleInto && multiCount == 0) {
	//				try {
	//					notify();
	//				} catch (IllegalMonitorStateException e) {
	//				}
	//			}
	//		} else {
	//			// 单写锁未锁定，不接受解锁
	//			if (!single) {
	//				return false;
	//			}
	//			single = false;
	//			// 多读锁处于进入等待状态，唤醒它
	//			if (multiInto) {
	//				try {
	//					notify();
	//				} catch (IllegalMonitorStateException e) {
	//				}
	//			}
	//		}
	//		return true;
	//	}

}