/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

/**
 * 标记化接口。<br><br>
 * 
 * 标记化是LAXCUS大数据管理系统体系里，除JAVA串联化和可类化之外，提供的另一种组织、生成、解析类参数的处理接口。<br><br>
 * 
 * 标记化和可类化特点对比：<br>
 * 1. 标记化在每个参数前有一个标记符号，这个标记符号标记参数基础信息。每个参数的格式是“标记符号+数据内容”的组合；
 * 可类化则没有，只记录数据内容本身。<br>
 * 2. 标记化数据适合持久保存（硬盘保存），更新类里被删除或者新增加变量，只要变量名称不变，可以实现自动忽略。
 * 可类化取消了“标记符号”，适合做为网络/内存的临时数据和实时交互传递。<br><br>
 * 
 * 标记化数据长度特点：标记化数据 > 可类化数据 && 标记化数据 < 串行化数据。<br><br>
 * 
 * 标记头格式：参数类型（1个字节）+ 类名称（2个字节）+ 参数名称（2个字节）<br><br>
 * 
 * 实现标记化接口的类必须声明“Markable”。定义构造方法时，必须保证有一个空的公有构造方法，或者有一个声明为 xxx(MarkReader)的公有构造方法。<br>
 * 
 * 标记化保证类参数动态可扩展。就是当数据保存的硬盘后，而类升级或者变化，这里读取硬盘的数据，仍然是有效和可持续使用的。<br>
 * 
 * 实现了“Markable”接口类中的参数名称和类型不可修改，否则数据保存到硬盘，再根据类读取时，可能会出现找不到的现象。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2017
 * @since laxcus 1.0
 */
public interface Markable {

}