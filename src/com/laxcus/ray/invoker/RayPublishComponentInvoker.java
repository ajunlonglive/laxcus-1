/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.io.*;
import com.laxcus.command.*;

/**
 * 发布分布组件调用器。<br>
 * 这个工作只允许由管理员操作。
 * 
 * @author scott.liang
 * @version 1.0 3/12/2017
 * @since laxcus 1.0
 */
public abstract class RayPublishComponentInvoker extends RayInvoker {

	/**
	 * 发布单元
	 * 
	 * @author scott.liang
	 * @version 1.0 3/12/2017
	 * @since laxcus 1.0
	 */
	class PublishItem {
		/** 成功 **/
		private boolean successful;

		/** 本地文件 **/
		private File file;

		/**
		 * 构造发布单元
		 * @param successful
		 * @param file
		 */
		public PublishItem(boolean successful, File file) {
			super();
			setSuccessful(successful);
			setFile(file);
		}

		/**
		 * 设置成功
		 * @param b
		 */
		public void setSuccessful(boolean b) {
			successful = b;
		}

		/**
		 * 判断成功
		 * @return
		 */
		public boolean isSuccesful() {
			return successful;
		}

		/**
		 * 设置文件
		 * @param e
		 */
		public void setFile(File e) {
			file = e;
		}

		/**
		 * 返回文件
		 * @return
		 */
		public File getFile() {
			return file;
		}
	}

	/**
	 * 构造默认的发布分布组件调用器
	 * @param cmd
	 */
	protected RayPublishComponentInvoker(Command cmd) {
		super(cmd);
	}

}