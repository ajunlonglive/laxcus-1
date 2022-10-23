/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import java.lang.reflect.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.custom.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;

/**
 * 自定义资源生成器。<br><br>
 * 
 * 工作内容：<br>
 * 1. 从配置中解析自定义命令解码器、自定义命令、自定义调用器三类参数。其中自定义命令解析器是可选项，一般只交互模式的站点，如FRONT/WATCH站点。<br>
 * 2. 根据自定义器命令，查找配置表，生成自定义调用器。注意：自定义命令和自定义调用器是一对多关系，一个命令在不同的站点，会有多个自定义调用器。<br><br><br>
 * 
 *  <!-- 自定义命令检查器的类名。可选项，只在FRONT/WATCH节点使用 -->
 *  <command-parser-class> </command-parser-class>
 *  
 *  <!-- 自定义命令 -> 自定义调用器对类名。在所有节点存在 -->
 *  <custom-class>
 *  	<command>  </command>
 *  	<invoker> </invoker>
 *  </custom-class>
 * 
 * @author scott.liang
 * @version 1.0 6/12/2017
 * @since laxcus 1.0
 */
public final class CustomCreator {

	/** 自定义类加载器 **/
	private static HotClassLoader loader;

	/** 自定义命令解码器 **/
	private static CustomCommandCracker cracker;

	/** 命令类名 -> 调用器类名称 **/
	private static Map<String, String> invokers = new TreeMap<String, String>();

	/**
	 * 设置类加载器
	 * @param e
	 */
	public static void setClassLoader(HotClassLoader e) {
		CustomCreator.loader = e;
	}

	/**
	 * 返回类加载器
	 * @return
	 */
	public static HotClassLoader getClassLoader() {
		return CustomCreator.loader;
	}

	/**
	 * 设置自定义命令解码器
	 * @param e CustomCommandCracker子类实例
	 */
	public static void setCracker(CustomCommandCracker e) {
		CustomCreator.cracker = e;
	}

	/**
	 * 设置自定义命令解码器
	 * @param clazzName 类名
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void setCracker(String clazzName)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// 转成类
		Class<?> clazz = null;
		if (CustomCreator.loader != null) {
			clazz = Class.forName(clazzName, true, CustomCreator.loader);
		} else {
			clazz = Class.forName(clazzName);
		}
		// 生成实例
		Object e = clazz.newInstance();
		// 设置实例
		CustomCreator.setCracker((CustomCommandCracker) e);
	}

	/**
	 * 返回自定义命令解码器
	 * @return 返回CustomCommandCracker子类实例
	 */
	public static CustomCommandCracker getCracker() {
		return CustomCreator.cracker;
	}

	/**
	 * 根据输入语句判断是自定义命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isCommand(String input) {
		// 当命令解码器有效时， 判断是自定义命令
		if (CustomCreator.cracker != null) {
			return CustomCreator.cracker.isCommand(input);
		}
		return false;
	}

	/**
	 * 把字符串语句生成命令
	 * 
	 * @param input 输入语句
	 * @return 返回CustomCommand命令子类实例，或者空指针
	 */
	public static CustomCommand split(String input) {
		// 当命令解码器有效时，解析自定义命令
		if (CustomCreator.cracker != null) {
			return CustomCreator.cracker.split(input);
		}
		return null;
	}

	/**
	 * 保存参数
	 * @param command 命令类名
	 * @param invoker 调用器类名
	 */
	public static void add(String command, String invoker) {
		CustomCreator.invokers.put(command, invoker);
	}

	/**
	 * 删除参数
	 * @param command 命令类名
	 * @return 调用器类名
	 */
	public static String remove(String command) {
		return CustomCreator.invokers.remove(command);
	}

	/**
	 * 清除COMMAND/INVOKER对
	 */
	public static void clear() {
		CustomCreator.invokers.clear();
	}

	/**
	 * 根据字符串命令生成异步调用器
	 * @param input 输入语句
	 * @return EchoInvoker子类实例，或者空指针
	 */
	public static EchoInvoker createInvoker(String input) {
		CustomCommand cmd = CustomCreator.split(input);
		if (cmd == null) {
			return null;
		}
		return CustomCreator.createInvoker(cmd);
	}

	/**
	 * 根据命令生成异步调用器
	 * @param cmd CustomCommand子类
	 * @return EchoInvoker子类实例，或者空指针
	 * @throws InvokerException
	 */
	public static EchoInvoker createInvoker(Command cmd) {
		// 如果不是自定义命令，返回空指针
		if (!Laxkit.isClassFrom(cmd, CustomCommand.class)) {
			return null;
		}

		// 生成命令名
		String classCommand = cmd.getClass().getName();
		// 根据命令名，找到对应的类名
		String classInvoker = CustomCreator.invokers.get(classCommand);
		// 没有找到，返回空指针
		if (classInvoker == null) {
			return null;
		}

		try {
			Class<?> clazz = null;
			// 如果指定了类加载器，将首先通过类加载器来找到调用器类
			if (CustomCreator.loader != null) {
				clazz = Class.forName(classInvoker, true, CustomCreator.loader);
			} else {
				clazz = Class.forName(classInvoker);
			}

			// 枚举所有公共构造方法
			Constructor<?>[] heads = clazz.getConstructors();
			// 首先检查带命令的自定义调用器
			for (int i = 0; i < heads.length; i++) {
				// 提取构造方法中的参数
				Class<?>[] ts = heads[i].getParameterTypes();
				// 判断构造参数是CustomCommand命令的子类
				if (ts.length == 1 && Laxkit.isClassFrom(ts[0], CustomCommand.class)) {
					Object obj = heads[i].newInstance(new Object[] { cmd });
					EchoInvoker invoker = (EchoInvoker) obj;
					return invoker;
				}
			}
			// 构造空参数的自定义调用器
			for (int i = 0; i < heads.length; i++) {
				// 提取构造方法中的参数
				Class<?>[] ts = heads[i].getParameterTypes();
				if (ts.length == 0) {
					// 调用空构造方法
					Object obj = heads[i].newInstance((Object[]) null);
					EchoInvoker invoker = (EchoInvoker) obj;
					invoker.setCommand(cmd);
					return invoker;
				}
			}
		} catch (IllegalArgumentException e) {
			throw new InvokerException(Laxkit.printThrowable(e));
		} catch (ClassNotFoundException e) {
			throw new InvokerException(Laxkit.printThrowable(e));
		} catch (InstantiationException e) {
			throw new InvokerException(Laxkit.printThrowable(e));
		} catch (IllegalAccessException e) {
			throw new InvokerException(Laxkit.printThrowable(e));
		} catch (InvocationTargetException e) {
			throw new InvokerException(Laxkit.printThrowable(e));
		}

		// 以上不成功，返回空指针
		return null;
	}


}