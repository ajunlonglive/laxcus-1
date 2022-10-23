/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

/**
 * 自定义资源标签。<br><br>
 * 
 * 自定义资源配置规则：<br>
 * 1. 每个节点的“local.xml”文件的“custom”标签声明自定义资源配置。<br>
 * 2. 自定义资源目录通过“directory”标签指向。<br>
 * 3. 自定义命令/调用器对配置文件通过“statement”标签指向。<br><br>
 * 
 * 自定义命令/调用器对配置文件规则：<br>
 * 1. “command-cracker”指示自定义命令解码器。解码器用来判断自命令语句合法，和把自定义命令语句转成自定义命令类。<br>
 * 2. “custom-item”标签下面，“command”指示命令，“invoker”指示调用器，必须是全路径名。<br>
 * 
 * 
 * <pre>
 *  <!-- 自定义命令解码器类名，必须是全路径名。可选项，只在FRONT/WATCH节点使用 -->
 *  <command-cracker> </command-cracker>
 *  
 *  <!-- 命令标签在JAR文件包的路径 -->
 *  <command-tokens> </command-tokens>
 *  
 *  <!-- 命令/调用器对类名。在所有节点存在 -->
 *  <custom-item>
 *  	<command>  </command>
 *  	<invoker> </invoker>
 *  </custom-item>
 *  
 *  </pre>
 *  
 * @author scott.liang
 * @version 1.0 6/12/2017
 * @since laxcus 1.0
 */
public final class CustomMark {

	/** 以下是 local.xml 文件中的定义  **/
	
	/** 集群系统COMMAND/INVOKER开发根标签 **/
	public static final String CUSTOM = "custom";
	
	/** 自定义JAR包自动更新，即线程发现磁盘JAR包更新时，自动替换内存旧包 **/
	public static final String CUSTOM_AUTOUPDATE ="auto-update";

	/** 自定义JAR目录 **/
	public static final String CUSTOM_DIRECTORY = "directory";

	/** 自定义命令/调用器(COMMAND/INVOKER)集合文件名(基于INVOKE/PRODUCE架构的自定义扩展文件名。JAR包文件默认部署在“custom”目录 **/
	public static final String CUSTOM_STATEMENT = "statement";

	
	/** 以下是声明文件中的定义  **/
	
	/** 自定义命令解码器类。全路径文件名 **/
	public static final String COMMAND_CRACKER = "command-cracker";
	
	/** 自定义标签在JAR文件的路径 **/
	public static final String COMMAND_TOKENS = "command-tokens";

	/** 命令/调用器对 **/
	public static final String CUSTOM_ITEM = "custom-item";

	/** 自定义命令类。全路径文件名 **/
	public static final String COMMAND = "command";

	/** 自定义调用器类。全路径文件名 **/
	public static final String INVOKER = "invoker";
}
