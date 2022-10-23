/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.command.*;
import com.laxcus.ui.display.*;

/**
 * 命令分派器 <br>
 * 这是一个接口，由所在节点（DESKTOP/RAY）实现，开放给应用软件使用。应用软件开发者和用户通过这个接口向集群发送命令。
 * 
 * @author scott.liang
 * @version 1.0 6/19/2021
 * @since laxcus 1.0
 */
public interface CommandDispatcher extends PlatformListener {
	/**
	 * 输出语法字节流
	 * @return 返回XML的字节流
	 */
	byte[] getSyntaxStream();
	
	/**
	 * 判断命令匹配，只检查命令名称本身。
	 * 
	 * @param input 输入语法
	 * @param throwin 如果出错，弹出异常或者异常
	 * @param display 输出显示接口
	 * @return 成功返回真，否则假
	 */
	boolean match(String input, boolean throwin, MeetDisplay display);

	/**
	 * 检查命令，包括参数。
	 * 
	 * @param input 输入语法
	 * @param throwin 如果出错，弹出异常或者异常
	 * @param display 输出显示接口
	 * @return 成功返回真，否则假
	 */
	boolean check(String input, boolean throwin, MeetDisplay display);

	/**
	 * 执行字符串命令
	 * @param input 输入字符串
	 * @param auditor 命令核准器
	 * @param display 输出显示接口
	 * @return 返回CommandSubmit的结果
	 */
	int submit(String input, CommandAuditor auditor, MeetDisplay display);

	/**
	 * 执行类化命令处理 <br><br>
	 * 
	 * @param cmd 命令实例
	 * @param auto 自动处理，自动处理跳过确认环节，否则执行核准确认操作。通知应用软件或者后台进程调用时，auto是true，否则是false。
	 * @param auditor 命令核准器
	 * @param display 输出显示接口
	 * @return 返回CommandSubmit结果
	 */
	int submit(Command cmd, boolean auto, CommandAuditor auditor, MeetDisplay display);
}