/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.method;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;

/**
 * 构造方法代理实例
 * 
 * @author scott.liang
 * @version 1.0 10/20/2020
 * @since laxcus 1.0
 */
public class TubMethodTrustor {

	/** 代理实例 **/
	private static TubMethodTrustor selfHandle = new TubMethodTrustor();
	
	/**
	 * 构造实例
	 */
	public TubMethodTrustor() {
		super();
	}

	/**
	 * 返回结果
	 * @return
	 */
	public static TubMethodTrustor getInstance() {
		return TubMethodTrustor.selfHandle;
	}
	
	/**
	 * 
	 * @param cmd
	 * @return
	 */
	public TubProduct doTubRunService(TubRunService cmd) {
		TubRunServiceRunner invoker = new TubRunServiceRunner(cmd);
		return invoker.launch();
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 */
	public TubProduct doTubStopService(TubStopService cmd) {
		TubStopServiceRunner invoker = new TubStopServiceRunner(cmd);
		return invoker.launch();
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 */
	public TubProduct doTubPrintService(TubPrintService cmd) {
		TubPrintServiceRunner invoker = new TubPrintServiceRunner(cmd);
		return invoker.launch();
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 */
	public TubProduct doTubShowContainer(TubShowContainer cmd) {
		TubShowContainerRunner invoker = new TubShowContainerRunner(cmd);
		return invoker.launch();
	}

	/**
	 * 执行处理结果
	 * @param cmd
	 * @return
	 */
	public TubProduct doCheckTubListen(TubCheckListen cmd) {
		TubCheckListenRunner invoker = new TubCheckListenRunner(cmd);
		return invoker.launch();
	}
}
