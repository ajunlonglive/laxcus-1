/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.util.naming.*;

/**
 * 分布任务组件资源读取接口。<br>
 * 这个接口是系统向操作者提供读取发布的DTC包和JAR附件包中资源的通道，读取的内容只限操作者自己DTC/JAR资源，越界操作将发生异常。<br>
 * <br>
 * 
 * @author scott.liang
 * @version 1.0 11/8/2017
 * @since laxcus 1.0
 */
public interface TaskReader {

	/**
	 * 通过调用器编号，判断调用有效。帮助操作者从DTC/JAR包中找到所需要资源。
	 * @param invokerId 调用器编号
	 * @param ware 软件包名称
	 * @param name 资源名称。以“/”号为分隔符的路径名，保存在操作者自己的分布任务组件文件里。
	 * @return 返回读取的字节数组，没有返回空指针。
	 * @throws TaskException - 如果异步调用器不存在，或者越界调用（操作者读取其它DTC/JAR包资源）时，将发生异常。
	 */
	byte[] readResource(long invokerId, Naming ware, String name) throws TaskException;
}