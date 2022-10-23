/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.stub.reflex.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.visit.*;

/**
 * 转发映像数据命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class DataShiftSetReflexDataInvoker extends DataInvoker {

	/**
	 * 构造转发映像数据命令调用器，指定命令
	 * @param shift 转发映像数据命令
	 */
	public DataShiftSetReflexDataInvoker(ShiftSetReflexData shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSetReflexData getCommand() {
		return (ShiftSetReflexData) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSetReflexData shift = getCommand();

		SetReflexData cmd = shift.getCommand();
		Node slave = shift.getSite();

		boolean success = completeTo(slave, cmd);
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "%s to %s", cmd.getClass().getSimpleName(), slave);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SetReflexDataProduct product = null;
		try {
			if (this.isSuccessObjectable(0)) {
				product = this.getObject(SetReflexDataProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftSetReflexData shift = getCommand();
		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		Logger.debug(this, "ending", success, "%s from %s", shift.getCommand()
				.getClass().getSimpleName(), shift.getSite());

		return useful(success);
	}

}