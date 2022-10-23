/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.util.*;

/**
 * 显示数据重组时间调用器。
 * 
 * 这个调用器在本地处理。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class DriverPrintRegulateTimeInvoker extends DriverInvoker {

	/**
	 * 构造显示数据重组时间调用器
	 * @param mission 驱动任务
	 */
	public DriverPrintRegulateTimeInvoker(DriverMission mission) {
		super(mission);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintRegulateTime getCommand() {
		return (PrintRegulateTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintRegulateTime cmd = getCommand();
		List<SwitchTime> times = getStaffPool().getSwitchTimes();

		SwitchTimeProduct product = new SwitchTimeProduct();
		// 全部或者选择的...
		if (cmd.isAll()) {
			product.addAll(times);
		} else {
			for (Space space : cmd.list()) {
				for (SwitchTime e : times) {
					if (Laxkit.compareTo(space, e.getSpace()) == 0) {
						product.add(e);
						break;
					}
				}
			}
		}
		// 输出结果
		setProduct(product);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}