/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.tip.*;

/**
 * 驱动程序散列码调用器
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class DriverBuildHashInvoker extends DriverInvoker {

	/**
	 * 构造散列码调用器，指定任务实例
	 * @param mission 任务实例
	 */
	public DriverBuildHashInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildHash getCommand() {
		return (BuildHash) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BuildHash cmd = getCommand();

		String text = cmd.getPlant();
		// 如果忽略大小写，转成小写字符串
		if (cmd.isIgnore()) {
			text = text.toLowerCase();
		}

		try {
			// 统一转为UTF8编码
			byte[] b = new UTF8().encode(text);

			String code = null;
			if (cmd.isSHA1()) {
				SHA1Hash hash = Laxkit.doSHA1Hash(b);
				code = hash.toString();
			} else if (cmd.isSHA256()) {
				SHA256Hash hash = Laxkit.doSHA256Hash(b);
				code = hash.toString();
			} else if (cmd.isSHA512()) {
				SHA512Hash hash = Laxkit.doSHA512Hash(b);
				code = hash.toString();
			} else if (cmd.isMD5()) {
				MD5Hash hash = Laxkit.doMD5Hash(b);
				code = hash.toString();
			} else {
				setFault(new MissionException("cannot be support!"));
			}

			// 设置返回结果
			if (code != null) {
				StringProduct product = new StringProduct(code);
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

}