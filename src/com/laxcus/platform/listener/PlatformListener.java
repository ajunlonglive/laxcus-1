/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 平台监听器 <br>
 * 通过它们实现系统和用户的交互 <br><br>
 * 
 * 平台监听器分为服务器端和客户端，服务器端由系统环境（DESKTOP/BENCH）在启动时注册，有且只有一个。客户端由应用软件在启动时注册到系统环境，允许任意多个，在结束时释放（删除）。
 * 
 * @author scott.liang
 * @version 1.0 3/5/2022
 * @since laxcus 1.0
 */
public interface PlatformListener {

}
