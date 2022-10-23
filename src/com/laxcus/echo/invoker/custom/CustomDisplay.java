/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import com.laxcus.util.display.show.*;

/**
 * 自定义显示器。
 * 
 * @author scott.liang
 * @version 1.0 11/1/2017
 * @since laxcus 1.0
 */
public interface CustomDisplay {

	/**
	 * 设置二维表格标题。在此之前，旧标题和全部行将被删除。<br>
	 * 这个方法只有FRONT.TERMINAL/FRONT.CONSOLE/WATCH站点有效，其它站点忽略。
	 * 
	 * @param e ShowTitle实例
	 */
	void setShowTitle(ShowTitle e);

	/**
	 * 增加二维表格记录。在增加表记录前，必须调用“setShowTitle”方法。<br>
	 * 这个方法只有FRONT.TERMINAL/FRONT.CONSOLE/WATCH站点有效，其它站点忽略。
	 * 
	 * @param e ShowItem实例
	 */
	void addShowItem(ShowItem e);

	/**
	 * 在窗口显示普通信息。<br>
	 * 这个方法只有FRONT.TERMINAL/FRONT.CONSOLE/WATCH站点有效，其它站点忽略。
	 * 
	 * @param text 消息文本
	 */
	void message(String text);

	/**
	 * 在窗口显示警告信息。<br>
	 * 这个方法只有FRONT.TERMINAL/FRONT.CONSOLE/WATCH站点有效，其它站点忽略。
	 * 
	 * @param text 警告文本
	 */
	void warn(String text) ;

	/**
	 * 在窗口显示错误信息。<br>
	 * 这个方法只有FRONT.TERMINAL/FRONT.CONSOLE/WATCH站点有效，其它站点忽略。
	 * 
	 * @param text 错误文本
	 */
	void fault(String text);
}
