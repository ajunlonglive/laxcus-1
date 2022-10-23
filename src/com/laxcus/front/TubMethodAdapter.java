/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import com.laxcus.tub.command.*;
import com.laxcus.tub.invoke.*;
import com.laxcus.tub.method.*;
import com.laxcus.tub.product.*;

/**
 * 边缘容器方法适配器。
 * 
 * @author scott.liang
 * @version 1.0 10/20/2020
 * @since laxcus 1.0
 */
public class TubMethodAdapter implements TubMethodInvoker {

	/**
	 * 构造默认的边缘容器方法适配器
	 */
	public TubMethodAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.invoke.TubMethodInvoker#doTubRunService(com.laxcus.tub.command.TubRunService)
	 */
	@Override
	public TubProduct doTubRunService(TubRunService cmd) {
		return TubMethodTrustor.getInstance().doTubRunService(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.invoke.TubMethodInvoker#doTubStopService(com.laxcus.tub.command.TubStopService)
	 */
	@Override
	public TubProduct doTubStopService(TubStopService cmd) {
		return TubMethodTrustor.getInstance().doTubStopService(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.invoke.TubMethodInvoker#doTubPrintService(com.laxcus.tub.command.TubPrintService)
	 */
	@Override
	public TubProduct doTubPrintService(TubPrintService cmd) {
		return TubMethodTrustor.getInstance().doTubPrintService(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.invoke.TubMethodInvoker#doTubShowContainer(com.laxcus.tub.command.TubShowContainer)
	 */
	@Override
	public TubProduct doTubShowContainer(TubShowContainer cmd) {
		return TubMethodTrustor.getInstance().doTubShowContainer(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.invoke.TubMethodInvoker#doTubCheckListen(com.laxcus.tub.command.TubCheckListen)
	 */
	@Override
	public TubProduct doTubCheckListen(TubCheckListen cmd) {
		return TubMethodTrustor.getInstance().doCheckTubListen(cmd);
	}

}