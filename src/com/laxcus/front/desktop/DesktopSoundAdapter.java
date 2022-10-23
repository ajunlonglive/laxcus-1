/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.platform.listener.*;
import com.laxcus.util.sound.*;

/**
 * 桌面系统声音适配器
 * 
 * @author scott.liang
 * @version 1.0 3/8/2022
 * @since laxcus 1.0
 */
class DesktopSoundAdapter implements SoundListener {

	/**
	 * 构造默认的桌面系统声音适配器
	 */
	public DesktopSoundAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.SoundListener#playMessage()
	 */
	@Override
	public void playMessage() {
		SoundKit.playMessage();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.SoundListener#playWarning()
	 */
	@Override
	public void playWarning() {
		SoundKit.playWarning();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.SoundListener#playError()
	 */
	@Override
	public void playError() {
		SoundKit.playError();
	}

}
