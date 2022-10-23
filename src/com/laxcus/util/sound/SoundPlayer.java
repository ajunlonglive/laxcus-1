/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.sound;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.thread.*;

/**
 * 音频播放器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2019
 * @since laxcus 1.0
 */
public class SoundPlayer extends MutexThread {

	/** 音频播放器实例 **/
	private static SoundPlayer selfHandle = new SoundPlayer();
	
	/** 播放声音或者否 **/
	private volatile boolean play;

	/** 单元编号 -> 音频单元 **/
	private TreeMap<Integer, SoundItem> sounds = new TreeMap<Integer, SoundItem>();

	/** 声音单元编号池 **/
	private ArrayList<Integer> array = new ArrayList<Integer>();

	/**
	 * 构造默认的音频播放器
	 */
	private SoundPlayer() {
		super();
		// 默认播放声音
		setPlay(true);
	}

	/**
	 * 返回音频播放器静态实例
	 * 
	 * @return SoundPlayer实例
	 */
	public static SoundPlayer getInstance() {
		return SoundPlayer.selfHandle;
	}

	/**
	 * 设置播放声音
	 * @param b 是或者否
	 */
	public void setPlay(boolean b) {
		play = b;
	}

	/**
	 * 判断播放声音
	 * @return 是或者否
	 */
	public boolean isPlay() {
		return play;
	}
	
	/**
	 * 保存音频单元
	 * @param item 音频单元
	 * 
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SoundItem item) {
		boolean success = false;
		super.lockSingle();
		try {
			int who = item.getID();
			success = (who >= 0);
			// 保存单元
			if (success) {
				sounds.put(who, item);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return success;
	}

	/**
	 * 返回音频单元
	 * @param who 编号
	 * @return 返回音频单元编号，或者空指针
	 */
	private SoundItem find(int who) {
		super.lockMulti();
		try {
			return sounds.get(who);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 有触发单元
	 * @return 返回真或者假
	 */
	private boolean hasTouch() {
		return array.size() > 0;
	}
	
	/**
	 * 依据编号播放声音
	 * @param soundId 音频单元编号
	 */
	public void play(int soundId) {
		// 不允许播放声音时，忽略它
		if (!isPlay()) {
			return;
		}

		boolean empty = false;
		// 锁定
		super.lockSingle();
		try {
			// 判断是空
			empty = array.isEmpty();
			// 保存编号
			array.add(soundId);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 保存成功，唤醒线程
		if (empty) {
			wakeup();
		}
	}
	
	/**
	 * 弹出一个单元编号
	 * @return 成功是大于等于0的数字，否则是-1。
	 */
	private int popup() {
		int soundId = -1;
		super.lockSingle();
		try {
			if (array.size() > 0) {
				soundId = array.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return soundId;
	}
	
	/**
	 * 发出声音
	 */
	private void sound() {
		// 弹出音频单元编号
		int soundId = popup();
		if (soundId == -1) {
			return;
		}
		// 找到音频单元
		SoundItem item = find(soundId);
		if (item != null && isPlay()) {
			item.play();
			// 延时50毫秒
			delay(50);
		}
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
		Logger.info(this, "process", "sound count: %d", sounds.size());

		while (!isInterrupted()) {
			// 判断有触发通知
			if (hasTouch()) {
				sound();
				continue;
			}
			sleep();
		}

		Logger.info(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
		sounds.clear();
	}

}