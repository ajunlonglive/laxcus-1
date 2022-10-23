/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.awt.*;
import java.io.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 注册工具 <br>
 * 执行输入/输出 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/16/2021
 * @since laxcus 1.0
 */
public class RTKit {

	/**
	 * 删除子单元，包括目录和参量
	 * @param paths
	 * @return
	 */
	public static boolean remove(String paths, byte attribute) {
		String[] texts = paths.split("/");
		if (texts.length < 2) {
			return false;
		}

		// 根
		Naming root = new Naming(texts[0]);
		// 成员名称
		Naming elementName = new Naming(texts[1]);
		// 标签
		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}

		boolean success = false;
		RTEnvironment environment = RTEnvironment.getInstance();
		if (tokenNames.length == 0) {
			success = environment.removeElement(root, elementName);
		} else {
			// 找到成员，删除子类型
			RElement element = environment.findElement(root, elementName);
			if (element != null) {
				success = element.remove(tokenNames, attribute);
			}
		}
		
		return success;
	}

	/**
	 * 根据路径，取出字体
	 * @param root 根
	 * @param paths 子路径
	 * @return 返回字体实例，或者空指针
	 */
	public static boolean remove(Naming root, String paths, byte attribute) {
		paths = root.toString() + "/" + paths;
		return remove(paths, attribute);
	}

	/**
	 * 根据路径，取出字体
	 * @param root 根
	 * @param paths 子路径
	 * @return 返回字体实例，或者空指针
	 */
	public static boolean remove(Naming root, String[] paths, byte attribute) {
		StringBuilder buf = new StringBuilder();
		// 根目录
		buf.append(root.toString());
		// 子目录
		for (String path : paths) {
			if (buf.length() > 0) {
				buf.append("/"); // 分隔符
			}
			buf.append(path);
		}
		return remove(buf.toString(), attribute);
	}
	
	/**
	 * 找到目录
	 * @param paths
	 * @return
	 */
	public static RFolder findFolder(String paths) {
		String[] texts = paths.split("/");
		if (texts.length < 3) {
			return null;
		}

		// 根
		Naming root = new Naming(texts[0]);
		// element name
		Naming elementName = new Naming(texts[1]);
		// 标签
		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}

		RTEnvironment environment = RTEnvironment.getInstance();
		RElement element = environment.findElement(root, elementName);
		if (element == null) {
			return null;
		}
		
		// 找到目录
		return element.findFolder(tokenNames);
	}
	
	/**
	 * 找到一个目录
	 * @param root
	 * @param paths
	 * @return 返回实例，没有是空指针
	 */
	public static RFolder findFolder(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return findFolder(paths);
	}

	/**
	 * 生成一个目录
	 * @param paths
	 * @return
	 */
	public static RFolder buildFolder(String paths) {
		String[] texts = paths.split("/");
		if (texts.length < 3) {
			return null;
		}

		// 根
		Naming root = new Naming(texts[0]);
		// element name
		Naming elementName = new Naming(texts[1]);
		// 标签
		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}

		RTEnvironment environment = RTEnvironment.getInstance();
		RElement element = environment.findElement(root, elementName);
		if (element == null) {
			element = new RElement(elementName);
			environment.addElement(root, element);
		}
		
		return element.createFloder(tokenNames);
	}
	
	/**
	 * 生成一个目录
	 * @param root
	 * @param paths
	 * @return
	 */
	public static RFolder buildFolder(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return buildFolder(paths);
	}

	/**
	 * 读一个范围
	 * @param paths
	 * @return
	 */
	public static Rectangle readBound(String paths) {
		// 返回目录
		RFolder folder = RTKit.findFolder(paths);
		if (folder == null) {
			return null;
		}

		boolean success = folder.hasParameter(new String[] { "x", "y", "w", "h" }, 
				new Class<?>[] { RInteger.class, RInteger.class, RInteger.class, RInteger.class });

		if (success) {
			RInteger x = (RInteger) folder.findParameter("x");
			RInteger y = (RInteger) folder.findParameter("y");
			RInteger width = (RInteger) folder.findParameter("w");
			RInteger height = (RInteger) folder.findParameter("h");
			return new Rectangle(x.getValue(), y.getValue(), width.getValue(), height.getValue());
		} else {
			return null;
		}
	}

	public static Rectangle readBound(Naming root, String paths) {
		paths = root.toString() + "/"+paths;
		return readBound(paths);
	}

	/**
	 * 写入一个范围参数
	 * @param paths
	 * @param rect
	 * @return
	 */
	public static boolean writeBound(String paths, Rectangle rect) {
		RFolder folder = RTKit.buildFolder(paths);
		if (folder == null) {
			return false;
		}

		folder.add(new RInteger("x", rect.x));
		folder.add(new RInteger("y", rect.y));
		folder.add(new RInteger("w", rect.width));
		folder.add(new RInteger("h", rect.height));
		return true;
	}

	public static boolean writeBound(Naming root, String paths, Rectangle rect) {
		paths = root.toString() + "/" + paths;
		return writeBound(paths, rect);
	}
	
	/**
	 * 读一个区间
	 * @param paths
	 * @return
	 */
	public static Dimension readDimension(String paths) {
		// 返回目录
		RFolder folder = RTKit.findFolder(paths);
		if (folder == null) {
			return null;
		}

		boolean success = folder.hasParameter(new String[] {  "w", "h" }, 
				new Class<?>[] {  RInteger.class, RInteger.class });

		if (success) {
			RInteger width = (RInteger) folder.findParameter("w");
			RInteger height = (RInteger) folder.findParameter("h");
			return new Dimension( width.getValue(), height.getValue());
		} else {
			return null;
		}
	}

	/**
	 * 读一个区间
	 * @param root
	 * @param paths
	 * @return
	 */
	public static Dimension readDimension(Naming root, String paths) {
		paths = root.toString() + "/"+paths;
		return readDimension(paths);
	}
	
	/**
	 * 写入一个区间
	 * @param paths
	 * @param rect
	 * @return
	 */
	public static boolean writeDimension(String paths, Dimension dim) {
		RFolder folder = RTKit.buildFolder(paths);
		if (folder == null) {
			return false;
		}

		folder.add(new RInteger("w", dim.width));
		folder.add(new RInteger("h", dim.height));
		return true;
	}

	/**
	 * 写入一个区间
	 * @param root
	 * @param paths
	 * @param dim
	 * @return
	 */
	public static boolean writeDimension(Naming root, String paths, Dimension dim) {
		paths = root.toString() + "/" + paths;
		return writeDimension(paths, dim);
	}

	/**
	 * 读一个颜色
	 * @param paths
	 * @return
	 */
	public static Color readColor(String paths) {
		// 返回目录
		RFolder folder = RTKit.findFolder(paths);
		if (folder == null) {
			return null;
		}

		boolean success = folder.hasParameter(new String[] { "r", "g", "b" }, 
				new Class<?>[] { RInteger.class, RInteger.class, RInteger.class });

		if (success) {
			RInteger r = (RInteger) folder.findParameter("r");
			RInteger g = (RInteger) folder.findParameter("g");
			RInteger b = (RInteger) folder.findParameter("b");
			return new Color(r.getValue(), g.getValue(), b.getValue());
		} else {
			return null;
		}
	}

	/**
	 * 读一个颜色
	 * @param root
	 * @param paths
	 * @return
	 */
	public static Color readColor(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readColor(paths);
	}
	
	/**
	 * 写入一个颜色
	 * @param paths
	 * @param rect
	 * @return
	 */
	public static boolean writeColor(String paths, Color color) {
		RFolder folder = RTKit.buildFolder(paths);
		if (folder == null) {
			return false;
		}

		folder.add(new RInteger("r", color.getRed()));
		folder.add(new RInteger("g", color.getGreen()));
		folder.add(new RInteger("b", color.getBlue()));
		return true;
	}

	/**
	 * 写入一个颜色
	 * @param root
	 * @param paths
	 * @param color
	 * @return
	 */
	public static boolean writeColor(Naming root, String paths, Color color) {
		paths = root.toString() + "/" + paths;
		return writeColor(paths, color);
	}
	
//	/**
//	 * 读一个文件
//	 * @param paths
//	 * @return
//	 */
//	public static File readFile(String paths) {
//		// 返回目录
//		RFolder folder = RTKit.findFolder(paths);
//		if (folder == null) {
//			return null;
//		}
//
//		boolean success = folder.hasParameter(new String[] { "File" }, 
//				new Class<?>[] { RString.class });
//
//		if (success) {
//			RString r = (RString) folder.findParameter("File");
//			return new File(r.getValue());
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * 读一个文件
//	 * @param root
//	 * @param paths
//	 * @return
//	 */
//	public static File readFile(Naming root, String paths) {
//		paths = root.toString() + "/" + paths;
//		return readFile(paths);
//	}
//	
//	/**
//	 * 写入一个文件
//	 * @param paths
//	 * @param rect
//	 * @return
//	 */
//	public static boolean writeFile(String paths, File file) {
//		RFolder folder = RTKit.buildFolder(paths);
//		if (folder == null) {
//			return false;
//		}
//
//		String filename = Laxkit.canonical(file);
//		folder.add(new RString("File", filename));
//		return true;
//	}
//
//	/**
//	 * 写入一个文件
//	 * @param root
//	 * @param paths
//	 * @param file
//	 * @return
//	 */
//	public static boolean writeFile(Naming root, String paths, File file) {
//		paths = root.toString() + "/" + paths;
//		return writeFile(paths, file);
//	}
	
	/**
	 * 根据路径，取出字体
	 * @param paths 路径
	 * @return 返回字体实例，或者空指针
	 */
	public static Font readFont(String paths) {
		// 返回目录
		RFolder folder = RTKit.findFolder(paths);
		if (folder == null) {
			return null;
		}

		// 判断参数有效
		boolean success = folder.hasParameter(new String[] { "name", "style", "size" }, 
				new Class<?>[] { RString.class, RInteger.class, RInteger.class });

		if (success) {
			RString name = (RString) folder.findParameter("name");
			RInteger style = (RInteger) folder.findParameter("style");
			RInteger size = (RInteger) folder.findParameter("size");
			return new Font(name.getValue(), style.getValue(), size.getValue());
		} else {
			return null;
		}
	}

	/**
	 * 根据路径，取出字体
	 * @param root 根
	 * @param paths 子路径
	 * @return 返回字体实例，或者空指针
	 */
	public static Font readFont(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readFont(paths);
	}

	/**
	 * 向指定位置，写入字体
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeFont(String paths, Font font) {
		RFolder folder = RTKit.buildFolder(paths);
		if (folder == null) {
			return false;
		}

		folder.add(new RString("name", font.getName()));
		folder.add(new RInteger("style", font.getStyle()));
		folder.add(new RInteger("size", font.getSize()));
		return true;
	}

	/**
	 * 向指定位置，写入字体
	 * @param root
	 * @param paths
	 * @param font
	 * @return
	 */
	public static boolean writeFont(Naming root, String paths, Font font) {
		paths = root.toString() + "/" + paths;
		return writeFont(paths, font);
	}

	/**
	 * 找参数
	 * @param paths
	 * @param type
	 * @return
	 */
	public static RParameter findParameter(String paths, byte type) {
		String[] texts = paths.split("/");
		if (texts.length < 3) {
			return null;
		}

		// 根
		Naming root = new Naming(texts[0]);
		// element name
		Naming elementName = new Naming(texts[1]);
		// 标签
		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}

		RTEnvironment environment = RTEnvironment.getInstance();
		RElement element = environment.findElement(root, elementName);
		if (element == null) {
			return null;
		}
		// 找到参数
		return element.findParameter(tokenNames, type);
	}
	
	/**
	 * 构造一个参数
	 * @param paths
	 * @return
	 */
	public static RParameter buildParameter(String paths, final byte type) {
		String[] texts = paths.split("/");
		if (texts.length < 3) {
			return null;
		}

		// 根目录
		Naming root = new Naming(texts[0]);
		// 成员变量
		Naming elementName = new Naming(texts[1]);
		// 标签
		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}

		RTEnvironment environment = RTEnvironment.getInstance();
		RElement element = environment.findElement(root, elementName);
		if (element == null) {
			element = new RElement(elementName);
			environment.addElement(root, element);
		}
		
		// 生成参数
		return element.createParameter(tokenNames, type);
	}
	
	/**
	 * 向指定位置，写入布尔参数
	 * @param paths 路径
	 * @param value 布尔值
	 * @return 成功返回真，否则假
	 */
	public static boolean writeBoolean(String paths, boolean value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.BOOLEAN);
		if (param == null) {
			return false;
		}
		((RBoolean) param).setValue(value);
		return true;
	}

	/**
	 * 写入布尔值
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeBoolean(Naming root, String paths, boolean value) {
		paths = root.toString() + "/" + paths;
		return writeBoolean(paths, value);
	}

	/**
	 * 判断布尔值
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasBoolean(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.BOOLEAN);
		return (param != null) ;
	}

	/**
	 * 判断布尔值
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasBoolean(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasBoolean(paths);
	}
	
	/**
	 * 读布尔值
	 * @param paths
	 * @param defaultValue
	 * @return
	 */
	public static boolean readBoolean(String paths, boolean defaultValue) {
		RParameter param = RTKit.findParameter(paths, RParameterType.BOOLEAN);
		if (param == null) {
			return defaultValue;
		}
		return ((RBoolean) param).getValue();
	}
	
	/**
	 * 读布尔值
	 * @param root
	 * @param paths
	 * @return
	 */
	public static boolean readBoolean(Naming root, String paths, boolean defaultValue) {
		paths = root.toString() + "/" + paths;
		return readBoolean(paths, defaultValue);
	}

	/**
	 * 读布尔值
	 * @param paths
	 * @return
	 */
	public static boolean readBoolean(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.BOOLEAN);
		if (param == null) {
			return false;
		}
		return ((RBoolean) param).getValue();
	}

	/**
	 * 读布尔值
	 * @param root
	 * @param paths
	 * @return
	 */
	public static boolean readBoolean(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readBoolean(paths);
	}

	/**
	 * 向指定位置，写入字节数组
	 * @param paths 路径
	 * @param value 字节数组
	 * @return 成功返回真，否则假
	 */
	public static boolean writeRaw(String paths, byte[] value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.RAW);
		if (param == null) {
			return false;
		}

		((RRaw) param).setValue(value);
		return true;
	}

	/**
	 * 写入字节数组
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeRaw(Naming root, String paths, byte[] value) {
		paths = root.toString() + "/" + paths;
		return writeRaw(paths, value);
	}

	/**
	 * 判断字节数组
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasRaw(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.RAW);
		return (param != null);
	}

	/**
	 * 判断字节数组
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasRaw(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasRaw(paths);
	}

	/**
	 * 读字节数组
	 * @param paths
	 * @return
	 */
	public static byte[] readRaw(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.RAW);
		if (param == null) {
			return null;
		}
		return ((RRaw) param).getValue();
	}

	/**
	 * 读字节数组
	 * @param root
	 * @param paths
	 * @return
	 */
	public static byte[] readRaw(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readRaw(paths);
	}

	/**
	 * 写入字符串
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeString(String paths, String value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.STRING);
		if (param == null) {
			return false;
		}

		((RString) param).setValue(value);
		return true;
	}

	/**
	 * 写入字符串
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeString(Naming root, String paths, String value) {
		paths = root.toString() + "/" + paths;
		return writeString(paths, value);
	}

	/**
	 * 写入字符串
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeString(Naming root, String[] paths, String value) {
		// 格式化路径
		StringBuilder buf = new StringBuilder();
		buf.append(root.toString());
		for (String path : paths) {
			if (buf.length() > 0) {
				buf.append("/");
			}
			buf.append(path);
		}
		return writeString(buf.toString(), value);
	}
	
	/**
	 * 判断字符串
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasString(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.STRING);
		return (param != null);
	}

	/**
	 * 判断字符串存在
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasString(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasString(paths);
	}

	/**
	 * 判断字符串存在
	 * @param root
	 * @param paths
	 * @return
	 */
	public static boolean hasString(Naming root, String[] paths) {
		// 格式化路径
		StringBuilder buf = new StringBuilder();
		// 根目录
		buf.append(root.toString());
		// 子目录
		for (String path : paths) {
			if (buf.length() > 0) {
				buf.append("/");
			}
			buf.append(path);
		}
		// 判断字符串存在
		return RTKit.hasString(buf.toString());
	}
	
	/**
	 * 读字符串
	 * @param paths
	 * @return
	 */
	public static String readString(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.STRING);
		if (param == null) {
			return null;
		}
		return ((RString) param).getValue();
	}

	/**
	 * 读字符串
	 * @param root
	 * @param paths
	 * @return
	 */
	public static String readString(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readString(paths);
	}
	
	/**
	 * 读字符串
	 * @param root
	 * @param paths
	 * @return
	 */
	public static String readString(Naming root, String[] paths) {
		// 格式化路径
		StringBuilder buf = new StringBuilder();
		buf.append(root.toString());
		for (String path : paths) {
			if (buf.length() > 0) {
				buf.append("/");
			}
			buf.append(path);
		}
		// 读取字符串
		return readString(buf.toString());
	}
	
	/**
	 * 删除字符串
	 * @param root
	 * @param paths
	 * @return
	 */
	public static boolean removeString(Naming root, String paths) {
		return RTKit.remove(root, paths, RTokenAttribute.PARAMETER);
	}
	
	/**
	 * 删除字符串
	 * @param root 根目录
	 * @param paths 没有分隔符的子目录
	 * @return 删除成功返回真，否则假
	 */
	public static boolean removeString(Naming root, String[] paths) {
		return RTKit.remove(root, paths, RTokenAttribute.PARAMETER);
	}

	/**
	 * 写短整型数值
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeShort(String paths, short value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.SHORT);
		if (param == null) {
			return false;
		}
		((RShort) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个短整型参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeShort(Naming root, String paths, short value) {
		paths = root.toString() + "/" + paths;
		return writeShort(paths, value);
	}

	/**
	 * 判断短整形
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasShort(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.SHORT);
		return (param != null);
	}

	/**
	 * 判断短整形
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasShort(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasShort(paths);
	}

	/**
	 * 读短整形
	 * @param paths
	 * @return
	 */
	public static short readShort(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.SHORT);
		if (param == null) {
			return 0;
		}
		return ((RShort) param).getValue();
	}

	/**
	 * 读短整形
	 * @param root
	 * @param paths
	 * @return
	 */
	public static short readShort(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readShort(paths);
	}

	/**
	 * 写入整形值
	 * 
	 * @param paths 路径
	 * @param value 整形实例
	 * @return
	 */
	public static boolean writeInteger(String paths, int value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.INTEGER);
		if (param == null) {
			return false;
		}
		((RInteger) param).setValue(value);
		return true;
	}

	/**
	 * 写入一个整数参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeInteger(Naming root, String paths, int value) {
		paths = root.toString() + "/" + paths;
		return writeInteger(paths, value);
	}

	/**
	 * 判断整形
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasInteger(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.INTEGER);
		return (param != null);
	}

	/**
	 * 判断整形
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasInteger(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasInteger(paths);
	}

	/**
	 * 读整数
	 * @param paths
	 * @param defaultValue
	 * @return
	 */
	public static int readInteger(String paths, int defaultValue) {
		boolean exists = RTKit.hasInteger(paths);
		if (!exists) {
			return defaultValue;
		}

		RParameter param = RTKit.findParameter(paths, RParameterType.INTEGER);
		if (param == null) {
			return 0;
		}
		return ((RInteger) param).getValue();
	}
	
	/**
	 * 读整数
	 * @param paths
	 * @return
	 */
	public static int readInteger(Naming root, String paths, int defaultValue) {
		paths = root.toString() + "/" + paths;
		return RTKit.readInteger(paths, defaultValue);
	}
	
	/**
	 * 读整数
	 * @param paths
	 * @return
	 */
	public static int readInteger(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.INTEGER);
		if (param == null) {
			return 0;
		}

		return ((RInteger) param).getValue();
	}

	/**
	 * 读取整数参数
	 * @param root
	 * @param paths
	 * @return
	 */
	public static int readInteger(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readInteger(paths);
	}

	/**
	 * 写整型数值
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeLong(String paths, long value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.LONG);
		if (param == null) {
			return false;
		}
		((RLong) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个整型参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeLong(Naming root, String paths, long value) {
		paths = root.toString() + "/" + paths;
		return writeLong(paths, value);
	}

	/**
	 * 判断长整形
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasLong(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.LONG);
		return (param != null);
	}

	/**
	 * 判断长整形
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasLong(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasLong(paths);
	}

	/**
	 * 读长整形
	 * @param paths
	 * @return
	 */
	public static long readLong(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.LONG);
		if (param == null) {
			return 0;
		}
		return ((RLong) param).getValue();
	}

	/**
	 * 读长整形
	 * @param root
	 * @param paths
	 * @return
	 */
	public static long readLong(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readLong(paths);
	}

	/**
	 * 写单浮点数值
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeFloat(String paths, float value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.FLOAT);
		if (param == null) {
			return false;
		}
		((RFloat) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个单浮点参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeFloat(Naming root, String paths, float value) {
		paths = root.toString() + "/" + paths;
		return writeFloat(paths, value);
	}

	/**
	 * 判断单浮点
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasFloat(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.FLOAT);
		return (param != null);
	}

	/**
	 * 判断单浮点
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasFloat(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasFloat(paths);
	}

	/**
	 * 读单浮点
	 * @param paths
	 * @return
	 */
	public static float readFloat(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.FLOAT);
		if (param == null) {
			return 0.0f;
		}
		return ((RFloat) param).getValue();
	}

	/**
	 * 读单浮点
	 * @param root
	 * @param paths
	 * @return
	 */
	public static float readFloat(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readFloat(paths);
	}

	/**
	 * 写双浮点数值
	 * @param paths 路径
	 * @param value 数值
	 * @return
	 */
	public static boolean writeDouble(String paths, double value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.DOUBLE);
		if (param == null) {
			return false;
		}
		((RDouble) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个双浮点参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeDouble(Naming root, String paths, double value) {
		paths = root.toString() + "/" + paths;
		return writeDouble(paths, value);
	}

	/**
	 * 判断双浮点
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasDouble(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.DOUBLE);
		return (param != null);
	}

	/**
	 * 判断双浮点
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasDouble(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasDouble(paths);
	}

	/**
	 * 读双浮点
	 * @param paths
	 * @return
	 */
	public static double readDouble(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.DOUBLE);
		if (param == null) {
			return 0.0d;
		}
		return ((RDouble) param).getValue();
	}

	/**
	 * 读双浮点
	 * @param root
	 * @param paths
	 * @return
	 */
	public static double readDouble(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readDouble(paths);
	}

	/**
	 * 写日期数值
	 * @param paths 路径
	 * @param value 数值
	 * @return
	 */
	public static boolean writeDate(String paths, int value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.DATE);
		if (param == null) {
			return false;
		}
		((RDate) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个日期参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeDate(Naming root, String paths, int value) {
		paths = root.toString() + "/" + paths;
		return writeDate(paths, value);
	}

	/**
	 * 判断日期
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasDate(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.DATE);
		return (param != null);
	}

	/**
	 * 判断日期
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasDate(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasDate(paths);
	}

	/**
	 * 读日期
	 * @param paths
	 * @return
	 */
	public static int readDate(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.DATE);
		if (param == null) {
			return 0;
		}
		return ((RDate) param).getValue();
	}

	/**
	 * 读日期
	 * @param root
	 * @param paths
	 * @return
	 */
	public static int readDate(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readDate(paths);
	}

	/**
	 * 写时间数值
	 * @param paths 路径
	 * @param value 数值
	 * @return
	 */
	public static boolean writeTime(String paths, int value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.TIME);
		if (param == null) {
			return false;
		}
		((RTime) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个时间参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeTime(Naming root, String paths, int value) {
		paths = root.toString() + "/" + paths;
		return writeTime(paths, value);
	}

	/**
	 * 判断时间
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasTime(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.TIME);
		return (param != null);
	}

	/**
	 * 判断时间
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasTime(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasTime(paths);
	}

	/**
	 * 读时间
	 * @param paths
	 * @return
	 */
	public static int readTime(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.TIME);
		if (param == null) {
			return 0;
		}
		return ((RTime) param).getValue();
	}

	/**
	 * 读时间
	 * @param root
	 * @param paths
	 * @return
	 */
	public static int readTime(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readTime(paths);
	}

	/**
	 * 写时间戳数值
	 * @param paths 路径
	 * @param value 数值
	 * @return
	 */
	public static boolean writeTimestamp(String paths, long value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.TIMESTAMP);
		if (param == null) {
			return false;
		}
		((RTimestamp) param).setValue(value);
		return true;
	}	

	/**
	 * 写入一个时间戳参数
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeTimestamp(Naming root, String paths, long value) {
		paths = root.toString() + "/" + paths;
		return writeTimestamp(paths, value);
	}

	/**
	 * 判断时间戳
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasTimestamp(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.TIMESTAMP);
		return (param != null);
	}

	/**
	 * 判断时间戳
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasTimestamp(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasTimestamp(paths);
	}

	/**
	 * 读时间戳
	 * @param paths
	 * @return
	 */
	public static long readTimestamp(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.TIMESTAMP);
		if (param == null) {
			return 0L;
		}
		return ((RTimestamp) param).getValue();
	}

	/**
	 * 读时间戳
	 * @param root
	 * @param paths
	 * @return
	 */
	public static long readTimestamp(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readTimestamp(paths);
	}


	/**
	 * 向指定位置，写入命令
	 * @param paths 路径
	 * @param value 命令
	 * @return 成功返回真，否则假
	 */
	public static boolean writeCommand(String paths, Command value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.COMMAND);
		if (param == null) {
			return false;
		}
		((RCommand) param).setValue(value);
		return true;
	}

	/**
	 * 写入命令
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeCommand(Naming root, String paths, Command value) {
		paths = root.toString() + "/" + paths;
		return writeCommand(paths, value);
	}

	/**
	 * 判断命令
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasCommand(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.COMMAND);
		return (param != null);
	}

	/**
	 * 判断命令
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasCommand(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasCommand(paths);
	}

	/**
	 * 读命令
	 * @param paths
	 * @return
	 */
	public static Command readCommand(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.COMMAND);
		if (param == null) {
			return null;
		}
		return ((RCommand) param).getValue();
	}

	/**
	 * 读命令
	 * @param root
	 * @param paths
	 * @return
	 */
	public static Command readCommand(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readCommand(paths);
	}


	/**
	 * 向指定位置，写入可类化对象
	 * @param paths 路径
	 * @param value 可类化对象
	 * @return 成功返回真，否则假
	 */
	public static boolean writeClassable(String paths, Classable value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.CLASSABLE);
		if (param == null) {
			return false;
		}
		((RClassable) param).setValue(value);
		return true;
	}

	/**
	 * 写入可类化对象
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeClassable(Naming root, String paths, Classable value) {
		paths = root.toString() + "/" + paths;
		return writeClassable(paths, value);
	}

	/**
	 * 判断可类化对象
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasClassable(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.CLASSABLE);
		return (param != null);
	}

	/**
	 * 判断可类化对象
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasClassable(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasClassable(paths);
	}

	/**
	 * 读可类化对象
	 * @param paths
	 * @return
	 */
	public static Classable readClassable(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.CLASSABLE);
		if (param == null) {
			return null;
		}
		return ((RClassable) param).getValue();
	}

	/**
	 * 读可类化对象
	 * @param root
	 * @param paths
	 * @return
	 */
	public static Classable readClassable(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readClassable(paths);
	}

	/**
	 * 向指定位置，写入串行化对象
	 * @param paths 路径
	 * @param value 串行化对象
	 * @return 成功返回真，否则假
	 */
	public static boolean writeSerializable(String paths, Serializable value) {
		RParameter param = RTKit.buildParameter(paths, RParameterType.SERIALABLE);
		if (param == null) {
			return false;
		}
		((RSerializable) param).setValue(value);
		return true;
	}

	/**
	 * 写入串行化对象
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeSerializable(Naming root, String paths, Serializable value) {
		paths = root.toString() + "/" + paths;
		return writeSerializable(paths, value);
	}

	/**
	 * 判断串行化对象
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasSerializable(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.SERIALABLE);
		return (param != null);
	}

	/**
	 * 判断串行化对象
	 * @param root
	 * @param paths
	 * @return 返回真或者假
	 */
	public static boolean hasSerializable(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return hasSerializable(paths);
	}

	/**
	 * 读串行化对象
	 * @param paths
	 * @return
	 */
	public static Serializable readSerializable(String paths) {
		RParameter param = RTKit.findParameter(paths, RParameterType.SERIALABLE);
		if (param == null) {
			return null;
		}
		return ((RSerializable) param).getValue();
	}

	/**
	 * 读串行化对象
	 * @param root
	 * @param paths
	 * @return
	 */
	public static Serializable readSerializable(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readSerializable(paths);
	}
	
	/**
	 * 写入文件
	 * @param paths 路径
	 * @param font 字体实例
	 * @return
	 */
	public static boolean writeFile(String paths, File value) {
		String filename = Laxkit.canonical(value);
		return RTKit.writeString(paths, filename);
	}

	/**
	 * 写入文件
	 * @param root
	 * @param paths
	 * @param value
	 * @return
	 */
	public static boolean writeFile(Naming root, String paths, File value) {
		paths = root.toString() + "/" + paths;
		return RTKit.writeFile(paths, value);
	}

	/**
	 * 读文件
	 * @param paths
	 * @return 文件实例，或者空指针
	 */
	public static File readFile(String paths) {
		String filename = RTKit.readString(paths);
		if (filename == null) {
			return null;
		}
		return new File(filename);
	}

	/**
	 * 读文件
	 * @param root
	 * @param paths
	 * @return
	 */
	public static File readFile(Naming root, String paths) {
		paths = root.toString() + "/" + paths;
		return readFile(paths);
	}
	
	public static void test2() {
		RTEnvironment.getInstance().createDefault();
		
		String paths = "PeropertiesDialog/Background/ImageFile/123";
		
		File file = new File("c:/abc.png");
		
		boolean b = RTKit.writeFile(RTEnvironment.ENVIRONMENT_USER, paths, file);
		System.out.printf("write %s %s\n", file, b);
		
		File f = RTKit.readFile(RTEnvironment.ENVIRONMENT_USER, paths);
		System.out.printf("read %s\n", f);
	}
	
	public static void test() {
		RTEnvironment.getInstance().createDefault();

		String filename = "unix.cpp";
		String paths = "PeropertiesDialog/Background/ImageFile/123";

		boolean b = RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, paths, filename);
		System.out.printf("write %s %s | %s\n", paths, filename, (b ? "Yes":"No"));
		b = RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, paths, filename);
		System.out.printf("write %s %s | %s\n", paths, filename, (b ? "Yes":"No"));

		System.out.println();
		paths = "PeropertiesDialog/ChoiceFile/Bound";
		Rectangle rect = new Rectangle(12,12,900,390);
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, paths, rect);
		
		RElement element = RTEnvironment.getInstance().findElement(RTEnvironment.ENVIRONMENT_SYSTEM, "PeropertiesDialog");
		element.print();

		System.out.println();
		paths = "PeropertiesDialog/Background/ImageFile/123";
		b = RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, paths, RTokenAttribute.PARAMETER);
		System.out.printf("remove %s | %s\n", paths, (b ? "Successfuly" :"Failed"));
		
		System.out.println();
		element.print();
	}
		
		public static void testRElement() {
			RElement element = new RElement("Element");
			element.add( new RFolder("Folder"));
			element.add(new RString("Hi", "String"));
			
			ClassWriter writer = new ClassWriter();
			element.build(writer);
			
			byte[] b = writer.effuse();
			System.out.printf("build len %d\n", b.length);
			
			ClassReader reader = new ClassReader(b);
			RElement e = new RElement(reader);
			System.out.printf("This Is %s\n", e.getName());
		}
		
		public static void testREString() {
			RTEnvironment.getInstance().createDefault();
			
			RTKit.writeString(RTEnvironment.ENVIRONMENT_USER, "MonitorFrame/Register", "YES");
			String s = RTKit.readString(RTEnvironment.ENVIRONMENT_USER, "MonitorFrame/Register");
			
			System.out.println(s);
			
			boolean b = RTKit.hasString(RTEnvironment.ENVIRONMENT_USER, "MonitorFrame/Register");
			System.out.printf("exists %s\n", b);
			
			b = RTKit.removeString(RTEnvironment.ENVIRONMENT_USER, "MonitorFrame/Register");
			System.out.printf("remove is %s\n", b);
			
			 s = RTKit.readString(RTEnvironment.ENVIRONMENT_USER, "MonitorFrame/Register");
			System.out.println(s);
		}
	
		public static void main(String[] args) {
//			RTKit.test2();
//			UIKit.testRElement();
			
			RTKit.testREString();
		}

}