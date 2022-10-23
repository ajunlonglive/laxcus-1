/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.field.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.site.Node;

/**
 * 查找WORK站点元数据调用器。<br>
 * 
 * 接受CALL站点的WORK资源检查。WORK站点包括CONDUCT.TO/CONDUCT.SUBTO/CONTACT.DISTANT。返回它们全部阶段命名
 * 
 * @author scott.liang
 * @version 1.0 2/23/2013
 * @since laxcus 1.0
 */
public class HomeFindWorkFieldInvoker extends HomeInvoker {

	/**
	 * 构造查找WORK站点元数据调用器，指定命令
	 * @param cmd
	 */
	public HomeFindWorkFieldInvoker(FindWorkField cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindWorkField getCommand() {
		return (FindWorkField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindWorkField cmd = this.getCommand();
		Node endpoint = cmd.getSource().getNode();

		Map<Node, PushWorkField> fields = new TreeMap<Node, PushWorkField>();

		for (Siger username : cmd.getUsers()) {
			List<PushWorkField> list = WorkOnHomePool.getInstance().validate(username);
			for(PushWorkField field : list) {
				PushWorkField that = fields.get(field.getNode());
				if (that == null) {
					fields.put(field.getNode(), field);
				} else {
					that.addPhases(field.getPhases());
				}
			}
		}

		// 发送到目标站点
		Iterator<Map.Entry<Node, PushWorkField>> iterator = fields.entrySet().iterator();
		int count = 0;
		while (iterator.hasNext()) {
			Map.Entry<Node, PushWorkField> entry = iterator.next();
			boolean success = super.submit(endpoint, entry.getValue());
			if (success) {
				count++;
			}
		}

		boolean success = (count > 0);
		Logger.debug(this, "launch", "send to:%s, size is:%d", endpoint, fields.size());

		return useful(success);
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
