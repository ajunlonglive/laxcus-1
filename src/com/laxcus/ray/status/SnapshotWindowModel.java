/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.status;

import com.laxcus.register.*;

/**
 * 悬浮窗口类型
 * 
 * @author scott.liang
 * @version 1.0 1/4/2022
 * @since laxcus 1.0
 */
class SnapshotWindowModel {

	/** 等比例压缩 **/
	public final static int ZOOM = 1;

	/** 充满整个空间 **/
	public final static int FILL = 2;

	/** 隐藏，不显示 **/
	public final static int HIDE = 3;

//	/** 默认定义 **/
//	public static int defaultModel = ZOOM;
	
	/**
	 * 判断是等比例缩放
	 * @param who
	 * @return
	 */
	public static boolean isZoom(int who) {
		return who == SnapshotWindowModel.ZOOM;
	}
	
	/**
	 * 填充
	 * @param who
	 * @return
	 */
	public static boolean isFill(int who) {
		return who == SnapshotWindowModel.FILL;
	}
	
	/**
	 * 隐藏
	 * @param who
	 * @return
	 */
	public static boolean isHide(int who) {
		return who == SnapshotWindowModel.HIDE;
	}
	
	/**
	 * 判断是有效的模式
	 * @param who
	 * @return
	 */
	public static boolean isModel(int who) {
		switch (who) {
		case SnapshotWindowModel.ZOOM:
		case SnapshotWindowModel.FILL:
		case SnapshotWindowModel.HIDE:
			return true;
		default:
			return false;
		}
	}
	
//	/**
//	 * 设置环境默认模式
//	 * @param who
//	 * @return
//	 */
//	public static boolean setDefaultModel(int who) {
//		// 模式
//		if (SnapshotWindowModel.isModel(who)) {
//			SnapshotWindowModel.defaultModel = who;
//			return true;
//		}
//		return false;
//	}
//	
//	public static int getDefaultMode() {
//		return SnapshotWindowModel.defaultModel;
//	}
	
	/**
	 * 读取环境中的定义
	 * @return
	 */
	public static int readEnvironmentModel() {
		return RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM,
				"StatusBar/FrameBar/FrameButton/SnapshotWindow/Model",
				SnapshotWindowModel.ZOOM);
	}

	/**
	 * 写入环境中的定义
	 * @param who
	 * @return
	 */
	public static boolean writeEnvironmentModel(int who) {
		if (!SnapshotWindowModel.isModel(who)) {
			return false;
		}
		return RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM,
				"StatusBar/FrameBar/FrameButton/SnapshotWindow/Model", who);
	}
}
