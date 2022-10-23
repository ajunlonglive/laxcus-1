/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.edge.mission.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 驱动程序半截符调用器
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class EdgeBuildHalfInvoker extends EdgeInvoker {

	/**
	 * 构造散列码调用器，指定任务实例
	 * @param mission 任务实例
	 */
	public EdgeBuildHalfInvoker(EdgeMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildHalf getCommand() {
		return (BuildHalf) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BuildHalf cmd = getCommand();

		try {
			String text = todo(cmd);
			// 设置返回结果
			if (text != null) {
				StringProduct product = new StringProduct(text);
				product.setPrimitive(cmd.getPrimitive());
				setProduct(product);
			} else {
				faultX(FaultTip.FAILED_X, getCommand());
			}
		} catch (Throwable e) {
			setFault(new MissionException(e));
		}

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

	/**
	 * 计算半截码值
	 * @param cmd
	 */
	private String todo(BuildHalf cmd) {
		String text = cmd.getText();
		// 忽略大小写，只在编码时有效
		if (cmd.isEncode()) {
			if (cmd.isIgnore()) {
				text = text.toLowerCase();
			}
			return Halffer.encode(text);
		} else {
			return Halffer.decode(text);
		}
	}

}