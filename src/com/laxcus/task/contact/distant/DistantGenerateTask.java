/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;

import com.laxcus.distribute.contact.*;
import com.laxcus.task.*;

/**
 * DISTANT数据生成阶段组件
 * 这是每个DISTANT操作的第一步骤，产生原始数据，数据结果可以是元数据信息，或者最终的实体数据。
 * 
 * @author scott.liang
 * @version 1.0 5/10/2020
 * @since laxcus 1.0
 */
public abstract class DistantGenerateTask extends DistantTask {

	/**
	 * 构造DistantGenerateTask实例
	 */
	public DistantGenerateTask() {
		super(DistantMode.GENERATE);
	}

	/**
	 * 执行首次CONTACT计算。<br>
	 * 
	 * 注意，只在首次处理时调用！
	 * 
	 * @return 返回产生的字节数
	 * @throws TaskException 快捷组件异常
	 */
	public abstract long process() throws TaskException;
	
}
