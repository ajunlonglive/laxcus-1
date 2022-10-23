/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

import java.lang.reflect.*;
import java.io.*;

import com.laxcus.application.manage.*;
import com.laxcus.log.client.*;

/**
 * 应用程序启动器
 * 系统级应用和用户级应用，都从这里开始！
 * 
 * @author scott.liang
 * @version 1.0 7/5/2021
 * @since laxcus 1.0
 */
public class ApplicationStarter {
	
	/**
	 * 根据KEY加载应用。成功返回0，失败是负数其它数字的错误码
	 * 
	 * @param key WKey实例
	 * @param args 启动参数
	 * @return 返回整数，等于0表示成功，负数是错误，没有大于0的正整数
	 * 
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static int start(WKey key, String[] args) throws IOException,
			InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {

		WProgram program = RTManager.getInstance().findProgram(key);
		if (program == null) {
			throw new ApplicationNotFoundException("not found application");
		}
		WRoot root = RTManager.getInstance().findRoot(key.getHash());
		if (root == null) {
			throw new ApplicationNotFoundException("not found boot");
		}

		String className = key.getClassName();
		String command = program.getCommand();

		File tmp = (root.isSystem() ? ApplicationPool.getInstance()
				.getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
		// 生成一个进程号
		long pid = ApplicationPool.getInstance().nextPID();

		// 加载类窗口
		Container container = new Container();
		if (root.hasPath()) {
			container.load(tmp, command, pid, root.getPath());
		} else if (root.hasContent()) {
			container.load(tmp, command, pid, root.getContent());
		} else {
			throw new ApplicationNotFoundException("cannot be find source!");
		}

		// 放入类实例
		Class<?> clazz = container.createClass(className);
		// 找到静态“main”方法
		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
		Object res = null;
		try {
			res = method.invoke(null, new Object[] { args });
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断是整数
		boolean success = (res != null && res.getClass() == Integer.class);
		if (!success) {
			// 非数字返回码
			return ApplicationStartCode.ERROR_RETURN_CODE;
		}

		// 返回码
		int code = ((Integer) res).intValue();
		if (code == ApplicationStartCode.SUCCESSFUL) {
			// 放入管理池中
			ApplicationPool.getInstance().add(container);
			return 0;
		} else if (code == ApplicationStartCode.EXISTS_EXIT) {
			// 前一个同类型实例已经存在，本次拒绝执行，这个也是正常，不放入管理池
			// 找到这个实例的同名实例，显示在前端
			return 0;
		} else {
			return code;
		}
	}
	
	/**
	 * 根据KEY加载应用
	 * 
	 * @param key 应用键
	 * @return 返回整数，等于0表示成功，负数是错误，没有大于0的正整数
	 * 
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static int start(WKey key) throws IOException,
			InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		return ApplicationStarter.start(key, new String[0]);
	}

}

//	/**
//	 * 根据KEY加载应用
//	 * 
//	 * @param key
//	 * @throws IOException
//	 * @throws InstantiationException
//	 * @throws IllegalAccessException
//	 * @throws SecurityException
//	 * @throws NoSuchMethodException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 */
//	public static void load(WKey key) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//		
//		WProgram program = RTManager.getInstance().findProgram(key);
//		if (program == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		WRoot root = RTManager.getInstance().findRoot(key.getHash());
//		if (root == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		
//		String className = key.getClassName();
//		String command = program.getCommand();
//		
//		File tmp = (root.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//		
//		// 加载类窗口
//		Container container = new Container();
//		if (root.hasPath()) {
//			container.load(tmp, command, pid, root.getPath());
//		} else if (root.hasContent()) {
//			container.load(tmp, command, pid, root.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		String[] args = new String[0];
//		method.invoke(null, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//	}
	

//	private static String[] splitText(String text) {
//		return text.split(" ");
//	}
//
//	public static boolean load(String input) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//
//		// 分割
//		String[] units = splitText(input);
//		// 参数
//		String[] args = new String[0];
//		if (units.length > 1) {
//			args = new String[units.length - 1];
//			for (int i = 1; i < units.length; i++) {
//				args[i - 1] = units[i];
//			}
//		}
//
//		// 通过命令找到对应的单元
//		WKey key = RTManager.getInstance().findFromCommand(units[0]);
//		if (key == null) {
//			throw new ApplicationNotFoundException("not found application!");
//		}	
//
//		WProgram program = RTManager.getInstance().findProgram(key);
//		if(program == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		WRoot root = RTManager.getInstance().findRoot(key.getHash());
//		if(root == null){
//			throw new ApplicationNotFoundException("not found application");
//		}
//
//		File tmp = (root.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//
//		String command = program.getCommand();
//		String className = key.getClassName();
//
//		// 加载软件包
//		Container container = new Container();
//
//		if (root.hasPath()) {
//			container.load(tmp, command, pid, root.getPath());
//		} else if (root.hasContent()) {
//			container.load(tmp, command, pid, root.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//		
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		Object ret = method.invoke(null, new Object[] { args });
//		
//		boolean success = (ret != null && ret.getClass() == Integer.class);
//		if (success) {
//			int value = ((Integer) ret).intValue();
//			if (value != 0) {
//				return false;
//			}
//		}
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//		return true;
//	}
	
//	public static void load(BootItem boot) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//
//		File file = boot.getFile();
//		BootApplicationItem item =	boot.getApplication();
//		String command = item.getCommand();
//		String className =	item.getBootClass();
//
//		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot()); //  new File("d:/laxtmp");
//
////		System.out.printf("启动应用：｛%s｝\n", item.toString());
//
//		long pid = ApplicationPool.getInstance().nextPID();
//		// 加载类窗口
//		Container basket = new Container();
//		basket.load(tmp, command, pid, file);
//
//		// 放入类实例
//		Class<?> clazz = basket.createClass(className);
//		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		System.out.println("找到main方法");
//		//			Object[] args = new String[]{"",""};
////		String[] args = new String[] { "-f", "c:/laxbut.pdf" };
//		String[] args = new String[0];
//		method.invoke(bootstrap, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(basket);
//	}

	
//	public static void load(BootItem boot) throws IOException,
//	InstantiationException, IllegalAccessException, SecurityException,
//	NoSuchMethodException, IllegalArgumentException,
//	InvocationTargetException {
//
////		File file = boot.getFile();
//		BootApplicationItem item =	boot.getApplication();
//		String command = item.getCommand();
////		String className =	item.getBootClass();
//
////		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot()); //  new File("d:/laxtmp");
//
//		//System.out.printf("启动应用：｛%s｝\n", item.toString());
//		
////		String command = units[0];
//		
//		System.out.printf("命令是：%s\n", command);
//		
//		RTManager.getInstance().printFault();
//		
//		// 通过命令找到对应的单元
//		RTVector vector = RTManager.getInstance().findFromCommand(command);
//		if (vector == null) {
//			throw new ApplicationNotFoundException("not found boot item!");
//		}	
//		
//		// 输出全部单元
//		java.util.List<RTElement> elements = vector.list();
//		// 超过一个，弹出窗口，让用户选择
//		if (elements.size() > 1) {
//
//		}
//		
//		RTElement element = elements.get(0);
////		String command = element.getCommand().toString();
//		String className = element.getBootstrap();
//		
//		File tmp = (element.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//		
//		// 加载类窗口
//		Container container = new Container();
//		if (element.hasFile()) {
//			container.load(tmp, command, pid, element.getFile());
//		} else if (element.hasContent()) {
//			container.load(tmp, command, pid, element.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//		
//
//		System.out.printf("来自 %s 加载, command:%s, class name:%s 系统：%s\n",
//				tmp, command, className, (element.isSystem() ? "System" : "User"));
//
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
//		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		//		System.out.println("找到main方法");
//		String[] args = new String[0];
//		method.invoke(bootstrap, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//		
//////		basket.load(tmp, command, pid, file);
////
////		// 放入类实例
////		Class<?> clazz = basket.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//System.out.println("找到main方法");
////		//			Object[] args = new String[]{"",""};
////		//String[] args = new String[] { "-f", "c:/laxbut.pdf" };
////		String[] args = new String[0];
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(basket);
//	}
	
//	public static void load(String input) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//
//		// 分割
//		String[] units = splitText(input);
//		// 参数
//		String[] args = new String[0];
//		if (units.length > 1) {
//			args = new String[units.length - 1];
//			for (int i = 1; i < units.length; i++) {
//				args[i - 1] = units[i];
//			}
//		}
//
//		//		String command = units[0];
//
//		BootItem boot = RTManager.getInstance().findItem(units[0]);
//		if (boot == null) {
//			throw new ApplicationNotFoundException("not found boot item!");
//		}
//
////		File tmp = new File("d:/laxtmp");
//		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//
//		File source = boot.getFile();
//		String command = boot.getApplication().getCommand();
//		String className = boot.getApplication().getBootClass();
//
//		long pid = ApplicationPool.getInstance().nextPID();
//		// 加载软件包
//		Container container = new Container();
//		container.load(tmp, command, pid, source);
//
//		System.out.printf("加载 %s, command:%s, class name:%s 系统：%s\n",
//				tmp, command, className, (boot.isSystem() ? "System" : "User"));
//
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
//		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		//		System.out.println("找到main方法");
//		method.invoke(bootstrap, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//	}

//	public static void load(String input) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//
//		// 分割
//		String[] units = splitText(input);
//		// 参数
//		String[] args = new String[0];
//		if (units.length > 1) {
//			args = new String[units.length - 1];
//			for (int i = 1; i < units.length; i++) {
//				args[i - 1] = units[i];
//			}
//		}
//
//		//		String command = units[0];
//		
//		// 通过命令找到对应的单元
//		RTVector vector = RTManager.getInstance().findFromCommand(units[0]);
//		if (vector == null) {
//			throw new ApplicationNotFoundException("not found boot item!");
//		}	
//		
//		// 输出全部单元
//		java.util.List<RTElement> elements = vector.list();
//		// 超过一个，弹出窗口，让用户选择
//		if (elements.size() > 1) {
//
//		}
//
////		BootItem boot = RTManager.getInstance().findItem(units[0]);
////		if (boot == null) {
////			throw new ApplicationNotFoundException("not found boot item!");
////		}
//		
//		RTElement element = elements.get(0);
//
//		//File tmp = new File("d:/laxtmp");
//		File tmp = (element.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//		
//		String command = element.getCommand().toString();
//		String className = element.getBootstrap();
//		
////		File source = boot.getFile();
////		String command = boot.getApplication().getCommand();
////		String className = boot.getApplication().getBootClass();
//
//		
//		// 加载软件包
//		Container container = new Container();
//		
//		if (element.hasFile()) {
//			container.load(tmp, command, pid, element.getFile());
//		} else if (element.hasContent()) {
//			container.load(tmp, command, pid, element.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//		
//		System.out.printf("从 %s 加载, command:%s, class name:%s 系统：%s\n",
//				tmp, command, className, (element.isSystem() ? "System" : "User"));
//
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
//		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		//		System.out.println("找到main方法");
//		method.invoke(bootstrap, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//	}

//public class ApplicationStarter {
//	
////	private static void loadFromConstructor(Container container, String className,
////			Object[] args) throws ContainerException, InstantiationException,
////			IllegalAccessException, SecurityException, NoSuchMethodException,
////			IllegalArgumentException, InvocationTargetException, ApplicationNotFoundException {
////
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Constructor<?> c = null;
////		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
////		for(int i =0; i < constructors.length; i++) {
////			Class<?>[] types =	constructors[i].getParameterTypes();
////			if(types.length == 0) {
////				c = constructors[i];
////			}
////		}
////		
////		if (c == null) {
////			throw new ApplicationNotFoundException("illegal constructor function!");
////		}
////		
////		// 生成实例对象
////		Object bootstrap = c.newInstance((Object[]) null);
////		
//////		Object bootstrap = clazz.newInstance();
////		
////		// 找到"main"静态方法
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		// 调用"main"方法
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
////	}
//	
////	/**
////	 * 调用静态方法
////	 * @param container
////	 * @param className
////	 * @param args
////	 * @throws ContainerException
////	 * @throws SecurityException
////	 * @throws NoSuchMethodException
////	 * @throws IllegalArgumentException
////	 * @throws IllegalAccessException
////	 * @throws InvocationTargetException
////	 */
////	private static void callStaticMethod(Container container, String className,
////			Object[] args) throws ContainerException, SecurityException,
////			NoSuchMethodException, IllegalArgumentException,
////			IllegalAccessException, InvocationTargetException {
////
////		// 生成类实例
////		Class<?> clazz = container.createClass(className);
////		
////		// 找到"main"静态方法
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		// object忽略，为空值，调用"main"方法, 因为"main"方法是静态的
////		method.invoke(null, new Object[] { args });
////		
////		System.out.printf("in callStaticMethod, 生成静态方案：%s\n", className);
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
////	}
//
//	/**
//	 * 根据KEY加载应用
//	 * 
//	 * @param key
//	 * @throws IOException
//	 * @throws InstantiationException
//	 * @throws IllegalAccessException
//	 * @throws SecurityException
//	 * @throws NoSuchMethodException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 */
//	public static void load(WKey key) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//		
//		WProgram program = RTManager.getInstance().findProgram(key);
//		if (program == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		WRoot root = RTManager.getInstance().findRoot(key.getHash());
//		if (root == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		
//		String className = key.getClassName();
//		String command = program.getCommand();
//		
//		File tmp = (root.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//		
//		// 加载类窗口
//		Container container = new Container();
//		if (root.hasPath()) {
//			container.load(tmp, command, pid, root.getPath());
//		} else if (root.hasContent()) {
//			container.load(tmp, command, pid, root.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//		
////		// 调用静态方法
////		callStaticMethod(container, className, new String[0]);
//		
//
////		System.out.printf("来自 %s 加载, command:%s, class name:%s 系统：%s\n",
////				tmp, command, className, (root.isSystem() ? "System" : "User"));
//
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//		System.out.println("找到main方法");
////		String[] args = new String[0];
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
//		
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		//		System.out.println("找到main方法");
//		String[] args = new String[0];
//		method.invoke(null, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//	}
//	
//
//	private static String[] splitText(String text) {
//		return text.split(" ");
//	}
//
//	public static void load(String input) throws IOException,
//			InstantiationException, IllegalAccessException, SecurityException,
//			NoSuchMethodException, IllegalArgumentException,
//			InvocationTargetException {
//
//		// 分割
//		String[] units = splitText(input);
//		// 参数
//		String[] args = new String[0];
//		if (units.length > 1) {
//			args = new String[units.length - 1];
//			for (int i = 1; i < units.length; i++) {
//				args[i - 1] = units[i];
//			}
//		}
//
//		//		String command = units[0];
//
//		// 通过命令找到对应的单元
//		WKey key = RTManager.getInstance().findFromCommand(units[0]);
//		if (key == null) {
//			throw new ApplicationNotFoundException("not found application!");
//		}	
//
//		WProgram program = RTManager.getInstance().findProgram(key);
//		if(program == null) {
//			throw new ApplicationNotFoundException("not found application");
//		}
//		WRoot root = RTManager.getInstance().findRoot(key.getHash());
//		if(root == null){
//			throw new ApplicationNotFoundException("not found application");
//		}
//
//		//		// 输出全部单元
//		//		java.util.List<RTElement> elements = vector.list();
//		//		// 超过一个，弹出窗口，让用户选择
//		//		if (elements.size() > 1) {
//		//
//		//		}
//		//
//		//		//BootItem boot = RTManager.getInstance().findItem(units[0]);
//		//		//if (boot == null) {
//		//		//	throw new ApplicationNotFoundException("not found boot item!");
//		//		//}
//		//
//		//		RTElement element = elements.get(0);
//
//		//File tmp = new File("d:/laxtmp");
//		File tmp = (root.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
//		long pid = ApplicationPool.getInstance().nextPID();
//
//		String command = program.getCommand();
//		String className = key.getClassName();
//
//		//File source = boot.getFile();
//		//String command = boot.getApplication().getCommand();
//		//String className = boot.getApplication().getBootClass();
//
//
//		// 加载软件包
//		Container container = new Container();
//
//		if (root.hasPath()) {
//			container.load(tmp, command, pid, root.getPath());
//		} else if (root.hasContent()) {
//			container.load(tmp, command, pid, root.getContent());
//		} else {
//			throw new ApplicationNotFoundException("cannot be find source!");
//		}
//		
////		// 调用静态方法
////		callStaticMethod(container, className, new Object[] { args });
//		
//
//		//		System.out.printf("从 %s 加载, command:%s, class name:%s 系统：%s\n",
//		//				tmp, command, className, (root.isSystem() ? "System" : "User"));
//
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//	System.out.println("找到main方法");
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
//		
//		// 放入类实例
//		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
//		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//		//	System.out.println("找到main方法");
//		method.invoke(null, new Object[] { args });
//
//		// 放入类实例
//		ApplicationPool.getInstance().add(container);
//	}
//	
////	public static void load(BootItem boot) throws IOException,
////			InstantiationException, IllegalAccessException, SecurityException,
////			NoSuchMethodException, IllegalArgumentException,
////			InvocationTargetException {
////
////		File file = boot.getFile();
////		BootApplicationItem item =	boot.getApplication();
////		String command = item.getCommand();
////		String className =	item.getBootClass();
////
////		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot()); //  new File("d:/laxtmp");
////
//////		System.out.printf("启动应用：｛%s｝\n", item.toString());
////
////		long pid = ApplicationPool.getInstance().nextPID();
////		// 加载类窗口
////		Container basket = new Container();
////		basket.load(tmp, command, pid, file);
////
////		// 放入类实例
////		Class<?> clazz = basket.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//////		System.out.println("找到main方法");
////		//			Object[] args = new String[]{"",""};
//////		String[] args = new String[] { "-f", "c:/laxbut.pdf" };
////		String[] args = new String[0];
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(basket);
////	}
//
//	
////	public static void load(BootItem boot) throws IOException,
////	InstantiationException, IllegalAccessException, SecurityException,
////	NoSuchMethodException, IllegalArgumentException,
////	InvocationTargetException {
////
//////		File file = boot.getFile();
////		BootApplicationItem item =	boot.getApplication();
////		String command = item.getCommand();
//////		String className =	item.getBootClass();
////
//////		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot()); //  new File("d:/laxtmp");
////
////		//System.out.printf("启动应用：｛%s｝\n", item.toString());
////		
//////		String command = units[0];
////		
////		System.out.printf("命令是：%s\n", command);
////		
////		RTManager.getInstance().printFault();
////		
////		// 通过命令找到对应的单元
////		RTVector vector = RTManager.getInstance().findFromCommand(command);
////		if (vector == null) {
////			throw new ApplicationNotFoundException("not found boot item!");
////		}	
////		
////		// 输出全部单元
////		java.util.List<RTElement> elements = vector.list();
////		// 超过一个，弹出窗口，让用户选择
////		if (elements.size() > 1) {
////
////		}
////		
////		RTElement element = elements.get(0);
//////		String command = element.getCommand().toString();
////		String className = element.getBootstrap();
////		
////		File tmp = (element.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
////		long pid = ApplicationPool.getInstance().nextPID();
////		
////		// 加载类窗口
////		Container container = new Container();
////		if (element.hasFile()) {
////			container.load(tmp, command, pid, element.getFile());
////		} else if (element.hasContent()) {
////			container.load(tmp, command, pid, element.getContent());
////		} else {
////			throw new ApplicationNotFoundException("cannot be find source!");
////		}
////		
////
////		System.out.printf("来自 %s 加载, command:%s, class name:%s 系统：%s\n",
////				tmp, command, className, (element.isSystem() ? "System" : "User"));
////
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//		System.out.println("找到main方法");
////		String[] args = new String[0];
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
////		
////////		basket.load(tmp, command, pid, file);
//////
//////		// 放入类实例
//////		Class<?> clazz = basket.createClass(className);
//////		Object bootstrap = clazz.newInstance();
//////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
//////		//System.out.println("找到main方法");
//////		//			Object[] args = new String[]{"",""};
//////		//String[] args = new String[] { "-f", "c:/laxbut.pdf" };
//////		String[] args = new String[0];
//////		method.invoke(bootstrap, new Object[] { args });
//////
//////		// 放入类实例
//////		ApplicationPool.getInstance().add(basket);
////	}
//	
////	public static void load(String input) throws IOException,
////			InstantiationException, IllegalAccessException, SecurityException,
////			NoSuchMethodException, IllegalArgumentException,
////			InvocationTargetException {
////
////		// 分割
////		String[] units = splitText(input);
////		// 参数
////		String[] args = new String[0];
////		if (units.length > 1) {
////			args = new String[units.length - 1];
////			for (int i = 1; i < units.length; i++) {
////				args[i - 1] = units[i];
////			}
////		}
////
////		//		String command = units[0];
////
////		BootItem boot = RTManager.getInstance().findItem(units[0]);
////		if (boot == null) {
////			throw new ApplicationNotFoundException("not found boot item!");
////		}
////
//////		File tmp = new File("d:/laxtmp");
////		File tmp = (boot.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
////
////		File source = boot.getFile();
////		String command = boot.getApplication().getCommand();
////		String className = boot.getApplication().getBootClass();
////
////		long pid = ApplicationPool.getInstance().nextPID();
////		// 加载软件包
////		Container container = new Container();
////		container.load(tmp, command, pid, source);
////
////		System.out.printf("加载 %s, command:%s, class name:%s 系统：%s\n",
////				tmp, command, className, (boot.isSystem() ? "System" : "User"));
////
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//		System.out.println("找到main方法");
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
////	}
//
////	public static void load(String input) throws IOException,
////			InstantiationException, IllegalAccessException, SecurityException,
////			NoSuchMethodException, IllegalArgumentException,
////			InvocationTargetException {
////
////		// 分割
////		String[] units = splitText(input);
////		// 参数
////		String[] args = new String[0];
////		if (units.length > 1) {
////			args = new String[units.length - 1];
////			for (int i = 1; i < units.length; i++) {
////				args[i - 1] = units[i];
////			}
////		}
////
////		//		String command = units[0];
////		
////		// 通过命令找到对应的单元
////		RTVector vector = RTManager.getInstance().findFromCommand(units[0]);
////		if (vector == null) {
////			throw new ApplicationNotFoundException("not found boot item!");
////		}	
////		
////		// 输出全部单元
////		java.util.List<RTElement> elements = vector.list();
////		// 超过一个，弹出窗口，让用户选择
////		if (elements.size() > 1) {
////
////		}
////
//////		BootItem boot = RTManager.getInstance().findItem(units[0]);
//////		if (boot == null) {
//////			throw new ApplicationNotFoundException("not found boot item!");
//////		}
////		
////		RTElement element = elements.get(0);
////
////		//File tmp = new File("d:/laxtmp");
////		File tmp = (element.isSystem() ? ApplicationPool.getInstance().getSystemRoot() : ApplicationPool.getInstance().getUserRoot());
////		long pid = ApplicationPool.getInstance().nextPID();
////		
////		String command = element.getCommand().toString();
////		String className = element.getBootstrap();
////		
//////		File source = boot.getFile();
//////		String command = boot.getApplication().getCommand();
//////		String className = boot.getApplication().getBootClass();
////
////		
////		// 加载软件包
////		Container container = new Container();
////		
////		if (element.hasFile()) {
////			container.load(tmp, command, pid, element.getFile());
////		} else if (element.hasContent()) {
////			container.load(tmp, command, pid, element.getContent());
////		} else {
////			throw new ApplicationNotFoundException("cannot be find source!");
////		}
////		
////		System.out.printf("从 %s 加载, command:%s, class name:%s 系统：%s\n",
////				tmp, command, className, (element.isSystem() ? "System" : "User"));
////
////		// 放入类实例
////		Class<?> clazz = container.createClass(className);
////		Object bootstrap = clazz.newInstance();
////		Method method = clazz.getDeclaredMethod("main", new Class<?>[] { String[].class });
////		//		System.out.println("找到main方法");
////		method.invoke(bootstrap, new Object[] { args });
////
////		// 放入类实例
////		ApplicationPool.getInstance().add(container);
////	}
//}