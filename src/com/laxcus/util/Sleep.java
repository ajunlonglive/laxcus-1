/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

/**
 * 线程延时器
 * 
 * @author scott.liang
 *
 */
public class Sleep implements Runnable {
	private Thread thread;
	private int timeout;

	public Sleep() {
		super();
		setTime(10);
	}

	public void setTime(int second) {
		if (second < 1) return;
		this.timeout = second;
	}

	protected synchronized void delay(long time) {
		try {
			this.wait(time);
		} catch (InterruptedException exp) {
			exp.printStackTrace();
		}
	}

	private synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException exp) {
			exp.printStackTrace();
		}
	}

	public void sleep() {
		if (thread != null) return;

		thread = new Thread(this);
		thread.start();

		System.out.println("sleep " + timeout + " second, start...");
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException exp) {
				exp.printStackTrace();
			}
		}
		System.out.println("sleep " + timeout + " second, finished!");
	}

	public void run() {
		for (int index = 0; index < timeout; index++) {
			this.delay(1000);
		}
		this.wakeup();
		thread = null;
	}

	public static void main(String[] args) {
		Sleep sleep = new Sleep();
		if (args.length > 0) {
			int second = Integer.parseInt(args[0].trim());
			sleep.setTime(second);
		}
		sleep.sleep();
	}

}
