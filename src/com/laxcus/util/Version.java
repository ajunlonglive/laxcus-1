/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * LAXCUS分布式操作系统版本
 * 
 * 版本包括：
 * 
 * 1. 内测版本：Alpha
 * 2. 正测版本：Beta，比Alpha高的一个版本
 * 3. 候选版本: Release Candidate，在Beta之后
 * 4. 体验版本/评估版本：Demo/Eval/Trial。功能上和正式版本没有区别，但是存在时间和空间上的限制
 * 
 * 16. 正式/释放版本：Release/Final，软件的正式版本，修正了Alpha版本和Beta版本的错误
 * 17. 免费版本：Free/Share
 * 18. 标准/完全版本：Standard/Full
 * 19. OEM版本，给计算机厂商随计算机贩卖的版本，也就是随机版，随机器出货，不能零售。
 * 20. PRO版本，专业版本，需要注册才能解除限制，否则为评估版本。
 * 21. ENTERPRISE版本，企业版本，需要注册才能解除限制，否则为评估版本。
 * 
 * 整数编码规则：
 * 0 - 7: 序列数 (8)
 * 8 - 15: 状态（8）
 * 16 - 23: 子编号 (8)
 * 24 - 31: 主编号 (8)
 * 
 * @author scott.liang
 * @version 1.0 1/18/2021
 * @since laxcus 1.0
 */
public final class Version implements Classable, Markable, Cloneable, Serializable, Comparable<Version> {

	private static final long serialVersionUID = -5373478980609886584L;

	/** 测试版本 **/
	private static final int ALPHA = 1;
	private static final int BETA = 2;
	private static final int RC = 3;
	private static final int DEMO = 4; // DEMO / TRIAL / EVAL
	
	/** 正则表达式 **/
	private static final String REGEX_ALPHA = "^\\s*(?i)(?:ALPHA)\\s*([0-9]+)\\s*$";
	private static final String REGEX_BETA = "^\\s*(?i)(?:BETA)\\s*([0-9]+)\\s*$";
	private static final String REGEX_RC = "^\\s*(?i)(?:RC)\\s*([0-9]+)\\s*$";
	private static final String REGEX_DEMO = "^\\s*(?i)(?:EVAL|DEMO|TRIAL)\\s*([0-9]+)\\s*$";
	
	/** 正式版本 **/
	private static final int RELEASE = 16;
	private static final int SHARE = 17; // SHAREWARE
	private static final int STANDARD = 18;
	private static final int OEM = 19;
	private static final int PROFESSIONAL = 20;
	private static final int ENTERPRISE = 21;

	private static final String REGEX_RELEASE = "^\\s*(?i)(?:RELEASE|FINAL)\\s*([0-9]+)\\s*$";
	private static final String REGEX_SHARE = "^\\s*(?i)(?:FREE|SHARE|SHAREWARE)\\s*([0-9]+)\\s*$";
	private static final String REGEX_STANDARD = "^\\s*(?i)(?:STANDARD|FULL)\\s*([0-9]+)\\s*$";
	private static final String REGEX_OEM = "^\\s*(?i)(?:OEM)\\s*([0-9]+)\\s*$";
	private static final String REGEX_PRO = "^\\s*(?i)(?:PROFESSIONAL|PRO)\\s*([0-9]+)\\s*$";
	private static final String REGEX_ENTERPRISE = "^\\s*(?i)(?:ENTERPRISE)\\s*([0-9]+)\\s*$";

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*([0-9]+)\\.([0-9]+)\\s+([\\w\\W]+)\\s*$";

	/** LAXCUS 5.2 BETA1 版本 **/
	public static final Version V_5_2_BETA1 = new Version(5, 2, "BETA 1");

	/** LAXCUS 5.2 RC1版本 **/
	public static final Version V_5_2_RC1 = new Version("5.2 RC 1");

	/** LAXCUS 5.2 FINAL 1版本，正式版本！ **/
	public static final Version V_5_2_STANDARD_1 = new Version("5.2 STANDARD 1");
	
	/** LAXCUS 6.0 标准版本，第1版 **/
	public static final Version V_6_0_STANDARD_1 = new Version("6.0 STANDARD 1");
	
	/** LAXCUS 6.0 标准版本，第2版 **/
	public static final Version V_6_0_STANDARD_2 = new Version("6.0 STANDARD 2");

	/** 当前LAXCUS分布式操作系统依赖版本，总是最新的 **/
	public static final Version current = Version.V_6_0_STANDARD_2;

	/** 版本号 **/
	private int version;

	/**
	 * 构造默认的LAXCUS分布式操作系统版本
	 */
	private Version() {
		super();
		version = 0;
	}

	/**
	 * 解析参数
	 * @param input 输入语句
	 */
	public Version(String input) {
		this();
		version = translate(input);
	}

	/**
	 * 生成版本实例
	 * @param major 主版本号
	 * @param minor 次版本号
	 * @param suffix 发行状态号
	 */
	public Version(int major, int minor, String suffix) {
		this();
		version = translate(major, minor, suffix);
	}

	/**
	 * 生成LAXCUS分布式操作系统版本副本
	 * @param that LAXCUS分布式操作系统版本
	 */
	private Version(Version that) {
		this();
		version = that.version;
	}

	/**
	 * 从可类化读取器解析LAXCUS分布式操作系统版本
	 * @param reader 可类化数据读取器
	 */
	public Version(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回数字值
	 * @return 整数
	 */
	public int getVersion() {
		return version;
	}
	
	/**
	 * 返回主版本号
	 * @return 整数
	 */
	public int getMajor() {
		return (version >>> 24) & 0xFF;
	}
	
	/**
	 * 返回次版本号
	 * @return 整数
	 */
	public int getMinor() {
		return (version >>> 16) & 0xFF;
	}
	
	/**
	 * 返回状态位
	 * @return 整数
	 */
	public int getStatus() {
		return (version >>> 8) & 0xFF;
	}

	/**
	 * 返回序列号 
	 * @return 整数
	 */
	public int getSerial() {
		return version & 0xFF;
	}

	/**
	 * 解析参数
	 * @param regex 正则表达式
	 * @param key 对应值
	 * @param input 输入值
	 * @return 整数
	 */
	private int doSuffix(String regex, int key, String input) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			int sub = Integer.parseInt(matcher.group(1));
			return (((key & 0xFF) << 8) | (sub & 0xFF));
		}
		return 0;
	}

	/*
	 * 	
	private static final int ALPHA = 1;
	private static final int BETA = 2;
	private static final int RC = 3;
	private static final int DEMO = 4; // DEMO / TRIAL / EVAL
	
	private static final int RELEASE = 16;
	private static final int SHARE = 17; // SHAREWARE
	private static final int STANDARD = 18;
	private static final int OEM = 19;
	private static final int PROFESSIONAL = 20;
	private static final int ENTERPRISE = 21;
	 */
	 
	/**
	 * 转义成字符串
	 * @param sub
	 * @return
	 */
	private String doSuffix(int sub) {
		int prefix = (sub >>> 8) & 0xFF;
		int suffix = (sub & 0xFF);

		switch (prefix) {
		// 测试状态版本
		case Version.ALPHA:
			return String.format("Alpha %d", suffix); //1. 测试版本
		case Version.BETA:
			return String.format("Beta %d", suffix); //2. BETA测试
		case Version.RC:
			return String.format("RC %d", suffix); //3. 释放版本
		case Version.DEMO:
			return String.format("Demo %d", suffix); //4. 体验/演示版本
		// 正式版本
		case Version.RELEASE:
			return String.format("Release %d", suffix); //16. 释放/固定版本
		case Version.SHARE:
			return String.format("Free %d", suffix); //17. 自由/共享版本
		case Version.STANDARD:
			return String.format("Standard %d", suffix); //18. 标准版本
		case Version.OEM:
			return String.format("OEM %d", suffix); //19. OEM版本
		case Version.PROFESSIONAL:
			return String.format("Professional %d", suffix); //20. 专业版本
		case Version.ENTERPRISE:
			return String.format("Enterprise %d", suffix); //21. 完全版本
		}
		return String.format("NONE %d", suffix);
	}

	/**
	 * 解析参数
	 * @param input
	 * @return
	 */
	private int doSuffix(String input) {
		// 1. ALPHA
		int sub = doSuffix(Version.REGEX_ALPHA, Version.ALPHA, input);
		// 2. BEATA
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_BETA, Version.BETA, input);
		}
		// 3. 内测版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_RC, Version.RC, input);
		}
		// 4. 演示版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_DEMO, Version.DEMO, input);
		}
		
		/*
	private static final int RELEASE = 16;
	private static final int SHARE = 17; // SHAREWARE
	private static final int STANDARD = 18;
	private static final int OEM = 19;
	private static final int PROFESSIONAL = 20;
	private static final int ENTERPRISE = 21;
		 */
		
		// 16. 体验版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_RELEASE, Version.RELEASE, input);
		}
		// 17. 免费版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_SHARE, Version.SHARE, input);
		}
		// 18. 正式版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_STANDARD, Version.STANDARD, input);
		}
		// 19. OEM版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_OEM, Version.OEM, input);
		}
		// 20. 专业版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_PRO, Version.PROFESSIONAL, input);
		}
		// 21. 企业版本
		if (sub == 0) {
			sub = doSuffix(Version.REGEX_ENTERPRISE, Version.ENTERPRISE, input);
		}
		return sub;
	}

	/**
	 * 转义成数字表述
	 * @param major 主版本号
	 * @param minor 次版本号
	 * @param suffix 发行版本
	 */
	private int translate(int major, int minor, String suffix) {
		major = (major & 0xFF) << 24;
		minor = (minor & 0xFF) << 16;
		int value = doSuffix(suffix);
		return (major | minor | value);
	}

	/**
	 * 翻译成字符串表达！
	 * @return 字节串
	 */
	public String translate() {
		int major = (version >>> 24) & 0xFF;
		int minor = ((version >>> 16)) & 0xFF;
		String suffix = doSuffix(version & 0xFFFF);
		return String.format("%d.%d %s", major, minor, suffix);
	}

	/**
	 * 解析值
	 * @param input 输入值
	 * @return 整数
	 */
	public int translate(String input) {
		Pattern pattern = Pattern.compile(Version.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(2));
			String suffix = matcher.group(3);
			return translate(major, minor, suffix);
		}
		return 0;
	}

	/**
	 * 生成副本
	 * @return 版本的副本
	 */
	public Version duplicate() {
		return new Version(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return translate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Version that) {
		if(that == null){
			return 1;
		}
		return Laxkit.compareTo(version, that.version);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(version);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		version = reader.readInt();
		return reader.getSeek() - seek;
	}

	public static void main(String[] args) {
		System.out.println(Version.current.toString());
		System.out.println(Version.V_5_2_BETA1);
		System.out.println(Version.V_5_2_RC1);
		System.out.println(Version.V_5_2_STANDARD_1.toString());
		System.out.println(Version.V_6_0_STANDARD_1.toString());
	}
}