/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * SELECT命令调用器。<BR><BR>
 * 
 * 调用器接受的SELECT分成三种：<BR>
 * 1. 标准格式："SELECT * FROM schema.table WHERE ..."。将直接转到CALL站点执行。<BR>
 * 2. 在“WHERE”后面带“ORDER BY、GROUP BY“语句，这将生成ConductInvoker，转交执行，自己退出。<BR>
 * 3. WHERE是嵌套查询（SUB SELECT），与2情况一样处理。<BR>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopSelectInvoker extends DesktopQueryInvoker {

	/**
	 * 构造SELECT命令调用器，指定SELECT命令
	 * @param cmd SELECT命令
	 */
	public DesktopSelectInvoker(Select cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Select getCommand() {
		return (Select) super.getCommand();
	}

	/**
	 * 启动SQL.SELECT异步检索。<br><br>
	 * 
	 * SELECT检索有三种可能：<br>
	 * 1. 是嵌套检索（SUB SELECT），转给CONDUCT命令执行。<br>
	 * 2. 带“ORDER BY/GROUP BY/DISTINCT”关键字，转给CONDUCT命令执行。<br>
	 * 3. 是“SELECT * FROM schema.table WHERE ... AND|OR ...” 语句，直接执行。<br>
	 * 
	 * @see com.laxcus.front.meet.invoker.DesktopInvoker#launch()
	 * @return 成功返回“真”，否则“假”。
	 */
	@Override
	public boolean launch() {
		Select cmd = getCommand();
		boolean success = false;
		if (cmd.hasNested()) {
			success = createSubSelect();
		} else if (cmd.isDistinct() || cmd.getGroup() != null || cmd.getOrder() != null) {
			success = createStandardSelect();
		} else {
			success = createDirectSelect();
		}

		// 如果失败，弹出错误提示
		if (!success) {
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 根据INIT阶段命名，生成CONDUCT异步调用器，转发执行。
	 * @param root 根命名
	 * @return 成功受理返回“真”，否则“假”。
	 */
	private boolean shift(Sock root) {
		Select cmd = getCommand();
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("SELECT_OBJECT", cmd); // SELECT命令保存到自定义参数

		// SELECT共享读方式，生成和保存规则
		initObject.addRules(cmd.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 提交给命令管理池处理
		return getCommandPool().press(conduct, getDisplay());
	}

	/**
	 * 建立一个嵌套查询的CONDUCT调用器。当前调用器退出。
	 * @return 成功返回真，否则假。
	 */
	private boolean createSubSelect() {
		// SUBSELECT是系统嵌套检索的根命名
		Sock root = Sock.doSystemSock("SUBSELECT");
		return shift(root);
	}

	/**
	 * 建立一个标准SQL.SELECT检索。这个接口定义“CONDUCT.INIT”阶段，分配SELECT句，其它操作到CALL.INIT中去执行。
	 * @return 接受返回“真”，否则“假”。
	 */
	private boolean createStandardSelect() {
		// "SELECT"命名在tasks.xml和SelectTaskKit中定义。
		Sock root = Sock.doSystemSock("SELECT");
		return shift(root);
	}

	/**
	 * 建立一个简单的SELECT检索，交给调用器处理
	 * @return 成功返回真，否则假
	 */
	private boolean createDirectSelect() {
		Select cmd = getCommand();
		DesktopDirectSelectInvoker invoker = new DesktopDirectSelectInvoker(cmd);
		return getInvokerPool().launch(invoker, getDisplay());
	}

}