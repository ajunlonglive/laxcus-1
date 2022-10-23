/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

/**
 * 可类化接口。<br><br>
 * 可类化是LAXCUS提供的一项基础功能，允许将一个类生成为字节数组输出，或者将字节数组解析为一个类。
 * 此功能与JAVA提供的串行化类似。不同之处在于，可类化的数据允许用户自由定义，包括数据内容、数据结构等。
 * 可类化提供了更强的灵活性，任意的数据处理自由度，但是不便之处是用户需要自己编写代码做具体的实现。
 * 可类化产生的数据量比串行化少，这可以减少网络传输中的数据量，提高处理效率很有帮助。<br><br>
 * 
 * 可类化是LAXCUS API的可选操作，如果需要更简短精确的数据处理，请使用此接口。如果不是，请选择JAVA的串行化。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public interface Classable {

	/**
	 * 将实现Classable接口的类数据写入可类化存储器，返回写入数据的字节长度。<br>
	 * 如果分配内存不足时，会由JVM弹出内存溢出异常。<br>
	 * 
	 * @param writer 可类化存储器
	 * @return 返回写入的字节长度
	 * 
	 * @throws OutOfMemoryError
	 */
	int build(ClassWriter writer);

	/**
	 * 从可类化读取器中读取数据，解析到对应的类中。<br>
	 * 如果成功，返回读取(解析)的数据字节长度(正整数，大于0)；<br>
	 * 如果失败，返回0或者负数(返回值由用户去自行解释)。<br>
	 * 
	 * @param reader 可类化读取器
	 * @return 返回解析的字节长度
	 * 
	 * @throws ClassableException, IndexOutOfBoundsException, ArithmeticException, IllegalArgumentException
	 */
	int resolve(ClassReader reader);
}
