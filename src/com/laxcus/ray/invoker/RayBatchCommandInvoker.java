/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.*;

/**
 * 批处理命令调用器 <br>
 * 
 * 每个批量处理命令中包含多个子命令。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2012
 * @since laxcus 1.0
 */
public abstract class RayBatchCommandInvoker extends RayInvoker {

	/**
	 * 构造批处理命令调用器，指定批量处理命令
	 * @param batch 批量处理命令
	 */
	public RayBatchCommandInvoker(BatchCommand batch) {
		super(batch);
		// 设置本地基本参数
		for (Command cmd : batch.list()) {
			cmd.setMemory(isMemory());
			cmd.setTimeout(getCommandTimeout());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BatchCommand getCommand() {
		return (BatchCommand) super.getCommand();
	}


}
