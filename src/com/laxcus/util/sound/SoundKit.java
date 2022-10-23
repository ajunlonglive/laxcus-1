/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.sound;

/**
 * 播放声音
 * 
 * @author scott.liang
 * @version 1.0 8/16/2021
 * @since laxcus 1.0
 */
public class SoundKit {

	public static void playMessage() {
		SoundPlayer.getInstance().play(SoundTag.MESSAGE);
	}

	public static void playWarning() {
		SoundPlayer.getInstance().play(SoundTag.WARNING);
	}

	public static void playError() {
		SoundPlayer.getInstance().play(SoundTag.ERROR);
	}

	public static void play(int who) {
		SoundPlayer.getInstance().play(who);
	}
}
