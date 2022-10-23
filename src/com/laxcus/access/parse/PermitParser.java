/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.access.diagram.*;
import com.laxcus.util.tip.*;

/**
 * 操作权限解析器
 * 
 * @author scott.liang
 * @version 1.2 8/11/2017
 * @since laxcus 1.0
 */
class PermitParser extends SyntaxParser {

	/**
	 * 构造操作权限解析器
	 */
	protected PermitParser() {
		super();
	}

	/**
	 * 将文本描述的操作控制选项翻译成数字
	 * @param items 选择数组
	 * @return 操作控制选项数字数组
	 */
	protected short[] splitControlTags(String[] items) {
		short[] array = new short[items.length];

		for (int i = 0; i < items.length; i++) {
			short option = ControlTag.translate(items[i]);
			if (option < 1) {
				// throw new SyntaxException("illegal item %s", items[i]);
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, items[i]);
			}
			array[i] = option;
		}

		return array;
	}

	//	/** 数据操作权限 **/
	//	private final static String SELECT = "^\\s*(?i)SELECT\\s*$";
	//	private final static String INSERT = "^\\s*(?i)INSERT\\s*$";
	//	private final static String DELETE = "^\\s*(?i)DELETE\\s*$";
	//	private final static String UPDATE = "^\\s*(?i)UPDATE\\s*$";
	//	private final static String CONDUCT = "^\\s*(?i)CONDUCT\\s*$";
	//	private final static String ESTABLISH = "^\\s*(?i)ESTABLISH\\s*$";
	//
	//	/** 管理权限 **/
	//	private final static String GRANT = "^\\s*(?i)GRANT\\s*$";
	//	private final static String REVOKE = "^\\s*(?i)REVOKE\\s*$";
	//	private final static String CREATE_USER = "^\\s*(?i)(?:CREATE\\s+USER)\\s*$";
	//	private final static String DROP_USER = "^\\s*(?i)(?:DROP\\s+USER)\\s*$";
	//	private final static String ALTER_USER = "^\\s*(?i)(?:ALTER\\s+USER)\\s*$";
	//	private final static String CREATE_DATABASE = "^\\s*(?i)(?:CREATE\\s+DATABASE)\\s*$";
	//	private final static String DROP_DATABASE = "^\\s*(?i)(?:DROP\\s+DATABASE)\\s*$";
	//	private final static String CREATE_TABLE = "^\\s*(?i)(?:CREATE\\s+TABLE)\\s*$";
	//	private final static String DROP_TABLE = "^\\s*(?i)(?:DROP\\s+TABLE)\\s*$";
	//	private final static String OPEN_RESOURCE = "^\\s*(?i)(?:OPEN\\s+RESOURCE)\\s*$"; // 开放共享资源（共享数据库、共享数据表）
	//	private final static String CLOSE_RESOURCE = "^\\s*(?i)(?:CLOSE\\s+RESOURCE)\\s*$"; // 关闭共享资源
	//
	//	/** 除DBA之外的所有权限 **/
	//	private final static String ALL = "^\\s*(?i)ALL\\s*$";
	//
	//	/** DBA权限 **/
	//	private final static String DBA = "^\\s*(?i)DBA\\s*$";
	//
	//	/**
	//	 * 解析各选项名称与指定值是否匹配，并返回选项的数字集合
	//	 * @param items
	//	 * @return
	//	 */
	//	protected short[] splitControls1(String[] items) {
	//		ArrayList<java.lang.Short> array = new ArrayList<java.lang.Short>();
	//		// 逐一分析
	//		for (String item : items) {
	//			if (item.matches(PermitParser.SELECT)) {
	//				array.add(ControlTag.SELECT);
	//			} else if (item.matches(PermitParser.INSERT)) {
	//				array.add(ControlTag.INSERT);
	//			} else if (item.matches(PermitParser.DELETE)) {
	//				array.add(ControlTag.DELETE);
	//			} else if (item.matches(PermitParser.UPDATE)) {
	//				array.add(ControlTag.UPDATE);
	//			} else if (item.matches(PermitParser.CONDUCT)) {
	//				array.add(ControlTag.CONDUCT);
	//			} else if (item.matches(PermitParser.ESTABLISH)) {
	//				array.add(ControlTag.ESTABLISH);
	//			} else if (item.matches(PermitParser.ALL)) {
	//				array.add(ControlTag.ALL);
	//			} else if (item.matches(PermitParser.DBA)) {
	//				array.add(ControlTag.DBA);
	//			} else if (item.matches(PermitParser.GRANT)) {
	//				array.add(ControlTag.GRANT);
	//			} else if (item.matches(PermitParser.REVOKE)) {
	//				array.add(ControlTag.REVOKE);
	//			} else if (item.matches(PermitParser.CREATE_USER)) {
	//				array.add(ControlTag.CREATE_USER);
	//			} else if (item.matches(PermitParser.DROP_USER)) {
	//				array.add(ControlTag.DROP_USER);
	//			} else if (item.matches(PermitParser.ALTER_USER)) {
	//				array.add(ControlTag.ALTER_USER);
	//			} else if (item.matches(PermitParser.CREATE_DATABASE)) {
	//				array.add(ControlTag.CREATE_SCHEMA);
	//			} else if (item.matches(PermitParser.DROP_DATABASE)) {
	//				array.add(ControlTag.DROP_SCHEMA);
	//			} else if (item.matches(PermitParser.CREATE_TABLE)) {
	//				array.add(ControlTag.CREATE_TABLE);
	//			} else if (item.matches(PermitParser.DROP_TABLE)) {
	//				array.add(ControlTag.DROP_TABLE);
	//			} else if (item.matches(PermitParser.OPEN_RESOURCE)) {
	//				array.add(ControlTag.OPEN_RESOURCE);
	//			} else if (item.matches(PermitParser.CLOSE_RESOURCE)) {
	//				array.add(ControlTag.CLOSE_RESOURCE);
	//			} else {
	//				throw new SyntaxException("illegal item %s", item);
	//			}
	//		}
	//
	//		short[] a = new short[array.size()];
	//		for (int i = 0; i < a.length; i++) {
	//			a[i] = array.get(i).shortValue();
	//		}
	//		return a;
	//	}

	//		public static void main(String[] args) {
	//			PermitParser p = new PermitParser();
	//			String input = "ALL";// "  CREATE DATABASE , CREATE TABLE , select , delete, DBA,ALL, create fault, drop fault,  create limit , drop limit , OPEN RESOURCE , Close RESOURCE ";
	//			String[] items = p.splitComma(input);
	//			System.out.println(input);
	//			for(int i =0; i < items.length; i++) {
	//				System.out.printf("[%s]\n", items[i]);
	//			}
	//	
	//			short[] array = p.splitControlTags(items);
	//			System.out.printf("size is %d\n", array.length);
	//			for(int i =0; i < array.length; i++){
	//				System.out.printf("%d  ", array[i]);
	//			}
	//			
	//			System.out.println("-----------");
	//			
	//			Control control = new Control();
	//			boolean success = control.setOption(PermitTag.USER_PERMIT, array);
	//			for(short id : control.toArray()) {
	//				System.out.printf("%d - %s\n" , id, ControlTag.translate(id) );
	//			}
	//		}

}