/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

import java.io.*;

import com.laxcus.access.util.*;
import com.laxcus.command.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.establish.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.meet.*;
import com.laxcus.front.meet.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 构造运行分布软件处理器
 * 
 * @author scott.liang
 * @version 1.0 8/29/2020
 * @since laxcus 1.0
 */
public class RunTaskConsoleProcesser extends ShiftConsoleCommandProcesser {

	/** 命令实例 **/
	private RunTask cmd;

	/**
	 * 构造构造运行分布软件处理器，指定命令
	 * 
	 * @param console 控制台
	 * @param auditor 核准器
	 * @param cmd 命令实例
	 */
	public RunTaskConsoleProcesser(Console console, MeetCommandAuditor auditor, RunTask cmd) {
		super(console, auditor);
		setCommand(cmd);
	}

	/**
	 * 设置命令
	 * @param e
	 */
	public void setCommand(RunTask e) {
		Laxkit.nullabled(e);
		cmd = e;
	}

	/**
	 * 返回命令
	 * @return
	 */
	public RunTask getCommand() {
		return cmd;
	}

	/**
	 * 显示信息
	 * @param xmlPath
	 */
	private String findContent(String xmlPath) {
		return ConsoleLauncher.getInstance().findContent(xmlPath);
	}

	/**
	 * 显示信息
	 * @param xmlPath
	 */
	private void print(String xmlPath) {
		String text = findContent(xmlPath);
		System.out.println(text);
	}

	/**
	 * 显示参数不足
	 * @param param
	 */
	private void showMissing(InputParameter param) {
		String name = param.getNameText();
		String content = findContent("console/guide/missing");
		content = String.format(content, name);
		System.out.println(content);
	}

	/**
	 * 格式错误
	 * @param param
	 */
	private void showFormatError(InputParameter param) {
		String name = param.getNameText();
		String content = findContent("console/guide/format-error");
		content = String.format(content, name);
		System.out.println(content);
	}

	/**
	 * 参数错误
	 * @param param
	 */
	private void showParamError(InputParameter param) {
		String name = param.getClass().getName();
		// xxx 是必选项，请输入！
		String content = findContent("console/guide/param-error"); 
		content = String.format(content, name);
		System.out.println(content);
	}

	/**
	 * 弹出错误
	 * @param string
	 */
	private void showError(String string) {
		String content = findContent("console/guide/error");
		content = String.format(content, string);
		System.out.println(content);
	}

	/**
	 * 没有找到实例
	 * @param sock
	 */
	private void showTaskNotFound(Sock sock) {
		String content = findContent("console/guide/notfound-task");
		content = String.format(content, sock.toString());
		System.out.println(content);
	}

	/**
	 * 弹出错误
	 */
	private void showThrowable() {
		String content = findContent("console/guide/throwable");
		System.out.println(content);
	}

	/**
	 * 启动失败！
	 * @param sock
	 */
	private void showStartupFailed(Sock sock) {
		String content = findContent("console/guide/startup-failed");
		content = String.format(content, sock.toString());
		System.out.println(content);
	}

	/**
	 * 取消操作
	 */
	private void cancel() {
		print("console/cancel");
	}

	/**
	 * 布尔变量
	 * @param input
	 * @param param
	 */
	private boolean setBoolean(String input, InputBoolean param) {
		boolean yes = ConfigParser.splitBoolean(input, false);
		param.setValue(yes);
		return true;
	}

	/**
	 * 字符串
	 * @param field
	 * @param param
	 * @return
	 */
	private boolean setString(String input, InputString param) {
		if (input.length() > 0) {
			param.setValue(input);
		} else {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 短整数
	 * @param input 字符
	 * @param param 参数
	 * @return 成功返回真，否则假
	 */
	private boolean setShort(String input, InputShort param) {
		boolean success = false;
		try {
			short value = Short.parseShort(input);
			param.setValue(value);
			success = true;
		} catch (Throwable e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 整数
	 * @param input 字符
	 * @param param 参数
	 * @return 成功返回真，否则假
	 */
	private boolean setInteger(String input, InputInteger param) {
		boolean success = false;
		try {
			int value = Integer.parseInt(input);
			param.setValue(value);
			success = true;
		} catch (Throwable e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 长整数
	 * @param input 字符
	 * @param param 参数
	 * @return 成功返回真，否则假
	 */
	private boolean setLong(String input, InputLong param) {
		boolean success = false;
		try {
			long value = Long.parseLong(input);
			param.setValue(value);
			success = true;
		} catch (Throwable e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		// 不成功
		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 单浮点
	 * @param input 字符
	 * @param param 参数
	 * @return 成功返回真，否则假
	 */
	private boolean setFloat(String input, InputFloat param) {
		boolean success = false;
		try {
			float value = Float.parseFloat(input);
			param.setValue(value);
			success = true;
		} catch (Throwable e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 双浮点
	 * @param input 字符
	 * @param param 参数
	 * @return 成功返回真，否则假
	 */
	private boolean setDouble(String input, InputDouble param) {
		boolean success = false;
		try {
			double value = Double.parseDouble(input);
			param.setValue(value);
			success = true;
		} catch (Throwable e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		// 以上不成功
		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置日期参数
	 * @param input
	 * @param param
	 * @return
	 */
	private boolean setDate(String input, InputDate param) {
		boolean success = false;
		try {
			int value = CalendarGenerator.splitDate(input);
			param.setValue(value);
			success = true;
		} catch (IllegalValueException e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置参数
	 * @param input
	 * @param param
	 * @return
	 */
	private boolean setTime(String input, InputTime param) {
		boolean success = false;
		try {
			int value = CalendarGenerator.splitTime(input);
			param.setValue(value);
			success = true;
		} catch (IllegalValueException e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置参数
	 * @param field
	 * @param param
	 * @return
	 */
	private boolean setTimestamp(String input, InputTimestamp param) {
		boolean success = false;
		try {
			long timestamp = CalendarGenerator.splitTimestamp(input);
			param.setValue(timestamp);
			success = true;
		} catch (IllegalValueException e) {
			showFormatError(param); // 弹出错误
			return false;
		}

		// 不成功..
		if (!success) {
			if (param.isSelect()) {
				showMissing(param); // 弹出对话框，输入参数
				return false;
			}
		}

		return true;
	}

	/**
	 * 处理一行参数
	 * @param param 参数
	 */
	private void process(InputParameter param) {
		String family = InputParameterType.translate(param.getFamily());
		String input = String.format("%s(%s):", param.getNameText(), family);
		// 循环处理
		do {			
			String result = readLine(input);
			if (result == null || result.isEmpty()) {
				continue;
			}

			boolean success = false;
			// 布尔
			if (param.getClass() == InputBoolean.class) {
				success = setBoolean(result, (InputBoolean) param);
			}
			// 字符串
			else if (param.getClass() == InputString.class) {
				success = setString(result, (InputString) param);
			}
			// 数字
			else if (param.getClass() == InputShort.class) {
				success = setShort(result, (InputShort) param);
			} else if (param.isInteger()) {
				success = setInteger(result, (InputInteger) param);
			} else if (param.isLong()) {
				success = setLong(result, (InputLong) param);
			} else if(param.isFloat()) {
				success = setFloat(result, (InputFloat)param);
			} else if(param.isDouble()) {
				success = setDouble(result, (InputDouble)param);
			} 
			// 日期/时间
			else if(param.isDate()) {
				success = setDate(result, (InputDate)param);
			} else if(param.isTime()) {
				success = setTime(result, (InputTime)param);
			} else if(param.isTimestamp()) {
				success = setTimestamp(result, (InputTimestamp)param);
			}
			// 类型不匹配，弹出错误
			else {
				// 弹出错误
				showParamError(param);
				continue;
			}

			// 以上检测出错，要求重新输入
			if (!success) {
				continue;
			}

			// 退出
			break;
		} while (true);
	}

	/**
	 * 显示阶段
	 * @param family
	 */
	private void showStage(String family) {
		String text = findContent("console/guide/stage");
		String content = String.format(text, family); 
		System.out.println(content);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.console.ShiftCommandProcesser#process()
	 */
	@Override
	public void process() {
		Sock sock = cmd.getSock();
		
		// 产生任务实例
		GuideTask task = GuideTaskPool.getInstance().createTask(sock);
		if (task == null) {
			showTaskNotFound(sock);
			cancel();
			return;
		}

		// 取出参数集
		InputParameterList list = null;
		try {
			list = task.markup(sock);
		} catch (GuideTaskException e) {
			Logger.error(e);
			showError(e.getMessage()); // 弹出错误
			cancel();
			return;
		} catch (Throwable e) {
			Logger.fatal(e);
			showThrowable(); // 弹出错误!
			cancel();
			return;
		}

		// 判断有效，逐行显示参数和修改它
		if (list != null) {
			for (InputParameterUnit unit : list.list()) {
				showStage(unit.getFamilyText());
				// 参数逐个输入
				for (InputParameter param : unit.list()) {
					process(param);
				}
			}
		}

		// 产生分布错误
		DistributedCommand command = null;
		try {
			command = task.create(sock, list);
		} catch (GuideTaskException e) {
			Logger.error(e);
			showError(e.getMessage()); // 弹出错误
			cancel();
			return;
		} catch (Throwable e) {
			Logger.fatal(e);
			showThrowable(); // 弹出错误
			cancel();
			return;
		}

		// 判断命令实例，执行它
		EchoInvoker invoker = null;
		if (command != null) {
			if (command.getClass() == Conduct.class) {
				Conduct conduct = (Conduct) command;
				invoker = new MeetConductInvoker(conduct);
			} else if (command.getClass() == Contact.class) {
				Contact contact = (Contact) command;
				invoker = new MeetContactInvoker(contact);
			} else if (command.getClass() == Establish.class) {
				Establish establish = (Establish) command;
				invoker = new MeetEstablishInvoker(establish);
			}
		}

		// 成功，去处理不成功，弹出错误
		if (invoker != null) {
			// 确认命令
			boolean success = getCommandAuditor().confirm();
			if (success) {
				launch(invoker);
			} else {
				cancel();
			}
		} else {
			showStartupFailed(sock);
		}
	}

}