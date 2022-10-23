/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.front;

/**
 * 用户级别。用来区别FRONT SITE的登录者的身份和权限。<br><br>
 * 
 * 登录账号按照级别分有两种：管理员/普通用户。<br>
 * 判断登录用户级别的唯一条件是“用户名和密码”。管理员的账号是由默认在系统中，单独保存，全局唯一。
 * 管理员账号由管理员账号生成器单独生成，这是一个辅件。
 * 普通用户的账号由管理员通过前端站点（FRONT SITE）生成，发送到TOP站点，集中保存在一个配置文件中。<br><br>
 * 
 * 管理员除了不具备“建立数据库和建表”两权权利外（这两项权利在TOP SITE上限制），是拥有全部操作权限，包括对普通用户账号的授权。
 * 普通用户要获得授权才可以进行操作。操作授权是由管理员或者等同管理员身份的用户操作。
 * 通过授权，普遍用户拥有各种操作权限，包括管理员权限（由管理员授权），但是身份仍然是普通用户。反之，也可以通过解除授权，限制用户的操作。<br>
 * 
 * @author scott.liang
 * @version 1.0 09/22/2009
 * @since laxcus 1.0
 */
public final class GradeTag {
	
	/** 离线未连接状态 **/
	public final static int OFFLINE = -1;

	/** 管理员 **/
	public final static int ADMINISTRATOR = 1;

	/** 普通用户 **/
	public final static int USER = 2;

	/**
	 * 构造级别
	 */
	public GradeTag() {
		super();
	}

	/**
	 * 判断是合法的级别标记
	 * @param grade 级别标记
	 * @return 返回真或者假
	 */
	public static boolean isGrade(int grade) {
		switch (grade) {
		case GradeTag.OFFLINE:
		case GradeTag.ADMINISTRATOR:
		case GradeTag.USER:
			return true;
		}
		return false;
	}
	
	/**
	 * 把级别标记的数字描述转为文本描述
	 * @param grade 级别标记
	 * @return 级别标记的文本描述
	 */
	public static String translate(int grade) {
		switch (grade) {
		case GradeTag.ADMINISTRATOR:
			return "ADMINISTRATOR";
		case GradeTag.USER:
			return "USER";
		case GradeTag.OFFLINE:
			return "OFFLINE";
		}
		return "INVALID!";
	}
	
	/**
	 * 判断是离线未使用的状态
	 * @return 返回真或者假
	 */
	public static boolean isOffline(int grade) {
		return grade == GradeTag.OFFLINE;
	}

	/**
	 * 判断是管理员级别
	 * @return 返回真或者假
	 */
	public static boolean isAdministrator(int grade) {
		return grade == GradeTag.ADMINISTRATOR;
	}

	/**
	 * 判断是普通用户级别
	 * @return 返回真或者假
	 */
	public static boolean isUser(int grade) {
		return grade == GradeTag.USER;
	}

}