/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.cross;

import java.util.*;

import com.laxcus.util.*;

/**
 * 共享资源操作符 <br>
 * 共享操作符有：“SELECT、INSERT、DELETE、UPDATE”四种，用户从FRONT站点输入。<br>
 * 
 * “INSERT、DELETE、UPDATE”三种写操作，隐含包括撤销操作：LEAVE。<br><br>
 * 
 * INSERT解释成0x1 <br>
 * DELETE解释成0x2 <br>
 * UPDATE解释成0x4<br>
 * SELECT解释成0x8 <br>
 * 
 * 多个操作符通过“与、或、异步”三种操作合并或者分解。
 * <br>
 * 
 * @author scott.liang
 * @version 8/16/2017
 * @since laxcus 1.0
 */
public class CrossOperator {

	/** 无定义 **/
	public final static int NONE = 0;

	/** 插入操作 **/
	public final static int INSERT = 1; 

	/** 删除操作 **/
	public final static int DELETE = 2; 

	/** 更新操作 **/
	public final static int UPDATE = 4; 

	/** 检索操作 **/
	public final static int SELECT = 8;

	/** 全部 **/
	public final static int ALL = (SELECT | INSERT | DELETE | UPDATE);

	/**
	 * 异或操作，相同值清零。 规则：参加运算的两个对应位，同号结果为假（0），异号结果为真（1）。
	 *  0^0=0, 0^1=1, 1^0=1, 1^1=0
	 * @param operator1 操作符1
	 * @param operator2 操作符2
	 * @return 返回异或结果
	 */
	public static int xor(int operator1, int operator2) {
		// 判断操作符合法
		if (!CrossOperator.isOperator(operator1)) {
			throw new IllegalValueException("illegal operator:%d", operator1);
		} else if (!CrossOperator.isOperator(operator2)) {
			throw new IllegalValueException("illegal operator:%d", operator2);
		}
		// 异步操作
		return  (operator1 ^ operator2);
	}

	/**
	 * 或操作： 0|0=0, 0|1=1, 1|0=1, 1|1=1, 
	 * @param operator1 操作符1
	 * @param operator2 操作符2
	 * @return 返回或结果
	 */
	public static int or(int operator1, int operator2) {
		// 判断操作符合法
		if (!CrossOperator.isOperator(operator1)) {
			throw new IllegalValueException("illegal operator:%d", operator1);
		} else if (!CrossOperator.isOperator(operator2)) {
			throw new IllegalValueException("illegal operator:%d", operator2);
		}
		// 与操作
		return  (operator1 | operator2);
	}

	/**
	 * 与操作。 两个相应位都是1，则该位为1，否则是0。 0&0=0, 0&1=0, 1&0=0, 1&1=1
	 * @param operator1 操作符1
	 * @param operator2 操作符2
	 * @return 返回“与”操作结果
	 */
	public static int and(int operator1, int operator2) {
		// 判断操作符合法
		if (!CrossOperator.isOperator(operator1)) {
			throw new IllegalValueException("illegal operator:%d", operator1);
		} else if (!CrossOperator.isOperator(operator2)) {
			throw new IllegalValueException("illegal operator:%d", operator2);
		}
		// 异步操作
		return  (operator1 & operator2);
	}
	
	/**
	 * 判断无定义
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isNone(int who) {
		return (who == CrossOperator.NONE);
	}

	/**
	 * 判断共享操作符包含SELECT操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isSelect(int who) {
		return (who & CrossOperator.SELECT) == CrossOperator.SELECT;
	}

	/**
	 * 判断共享操作符包含INSERT操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isInsert(int who) {
		return (who & CrossOperator.INSERT) == CrossOperator.INSERT;
	}

	/**
	 * 判断共享操作符包含DELETE操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isDelete(int who) {
		return (who & CrossOperator.DELETE) == CrossOperator.DELETE;
	}

	/**
	 * 判断共享操作符包含UPDATE操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isUpdate(int who) {
		return (who & CrossOperator.UPDATE) == CrossOperator.UPDATE;
	}

	/**
	 * “INSERT、DELETE、UPDATE”三种操作同时隐含“LEAVE”操作。
	 * 
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isLeave(int who) {
		return (isInsert(who) || isDelete(who) || isUpdate(who));
	}

	/**
	 * 判断是SELECT, INSERT, DELETE, UPDATE, LEAVE全部操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isAll(int who) {
		return (who == CrossOperator.ALL);
	}

	/**
	 * 判断共享操作符在规定范围内
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	private static boolean inside(int who) {
		// 判断在规定范围
		boolean success = (CrossOperator.NONE < who && who <= CrossOperator.ALL);
		// 判断是SELECT/INSERT/DELETE/UPDATE/LEAVE五种格式
		if (success) {
			success = (isSelect(who) || isDelete(who) || isInsert(who) || isUpdate(who));
		}
		return success;
	}

	/**
	 * 判断是合法的共享操作
	 * @param who 共享操作符
	 * @return 返回真或者假
	 */
	public static boolean isOperator(int who) {
		// 在规定范围内
		return inside(who);
	}

	/**
	 * 切割成多个操作符
	 * @param who 操作符
	 * @return 返回切割后的单个操作符
	 */
	public static int[] split(int who) {
		ArrayList<java.lang.Integer> array = new ArrayList<java.lang.Integer>();
		if (isSelect(who)) {
			array.add(CrossOperator.SELECT);
		}
		if (isInsert(who)) {
			array.add(CrossOperator.INSERT);
		}
		if (isDelete(who)) {
			array.add(CrossOperator.DELETE);
		}
		if (isUpdate(who)) {
			array.add(CrossOperator.UPDATE);
		}
		// 全部不成立，返回错误信息
		if (array.isEmpty()) {
			throw new IllegalValueException("ILLEGAL CROSS OPERATOR");
		}
		int[] b = new int[array.size()];
		for (int i = 0; i < b.length; i++) {
			b[i] = array.get(i).intValue();
		}
		return b;
	}

	/**
	 * 将数字翻译为字符串描述
	 * @param who 操作符
	 * @return 返回字符串
	 */
	public static String translate(int who) {
		// 切割操作符
		int[] array = CrossOperator.split(who);
		StringBuilder bf = new StringBuilder();
		// 格式化操作符
		for (int operator : array) {
			if (bf.length() > 0) {
				bf.append(",");
			}
			switch (operator) {
			case CrossOperator.SELECT:
				bf.append("SELECT");
				break;
			case CrossOperator.INSERT:
				bf.append("INSERT");
				break;
			case CrossOperator.DELETE:
				bf.append("DELETE");
				break;
			case CrossOperator.UPDATE:
				bf.append("UPDATE");
				break;
			}
		}
		// 返回结果
		return bf.toString();
	}

	/**
	 * 将共享操作符文本翻译为数字描述<br>
	 * 解析在Laxcus.Front终端上的OPEN SHARE TABLE/CLOSE SHARE TABLE等命令输入
	 * 
	 * @param input 输入的字符串描述
	 * @return 共享操作符，返回"NONE"：0 是错误!
	 */
	public static int translate(String input) {
		int operator = CrossOperator.NONE;
		
		// 要求全部，否则逐一分割
		if (input.matches("^\\s*(?i)(ALL)\\s*$")) {
			operator = CrossOperator.ALL;
		} else {
			// 切割字符串
			String[] tokens = input.split("\\s*\\,\\s*");
			// 逐一判断
			for (String token : tokens) {
				if (token.matches("^\\s*(?i)(SELECT)\\s*$")) {
					operator |= CrossOperator.SELECT;
				} else if (token.matches("^\\s*(?i)(INSERT)\\s*$")) {
					operator |= CrossOperator.INSERT;
				} else if (token.matches("^\\s*(?i)(DELETE)\\s*$")) {
					operator |= CrossOperator.DELETE;
				} else if (token.matches("^\\s*(?i)(UPDATE)\\s*$")) {
					operator |= CrossOperator.UPDATE;
				} 
				// 以上不成立，返回 NONE
				else {
					return CrossOperator.NONE;
				}
			}
		}
		// 返回符号
		return operator;
	}

	/**
	 * 依据源操作符，判断被比较操作符符合操作要求
	 * @param src 源操作符
	 * @param dest 被比较操作符
	 * @return 允许要求返回真，否则假
	 */
	public static boolean allow(int src, int dest) {
		// 都必须在规定范围内，任意不符合返回假
		if (!inside(src) || !inside(dest)) {
			return false;
		}

		/** 在被比较操作符包含情况下，判断源操作符也包含，即是条件成立；否则不成立 **/

		// 判断SELECT条件成立
		if (isSelect(dest)) {
			if (!isSelect(src)) {
				return false;
			}
		}
		// 判断INSERT条件成立
		if (isInsert(dest)) {
			if (!isInsert(src)) {
				return false;
			}
		}
		// 判断DELETE条件成立
		if (isDelete(dest)) {
			if (!isDelete(src)) {
				return false;
			}
		}
		// 判断UPDATE条件成立
		if (isUpdate(dest)) {
			if (!isUpdate(src)) {
				return false;
			}
		}
		// 通过以上判断，条件成立
		return true;
	}
	
	public static void main(String[] args) {
		//		int who = (CrossOperator.INSERT|CrossOperator.UPDATE|CrossOperator.DELETE|CrossOperator.LEAVE);
		final int all = CrossOperator.ALL; // (CrossOperator.INSERT|CrossOperator.UPDATE|CrossOperator.DELETE|CrossOperator.SELECT);
//		String input = CrossOperator.translate(who);
		System.out.printf("%d - %s\n", all,  CrossOperator.translate(all));
		
		int who = CrossOperator.xor(all, (CrossOperator.SELECT | CrossOperator.INSERT)); //  ( who ^ .UPDATE);
		System.out.printf("xor！ %d - %s\n", who, CrossOperator.translate(who));
		
		who = CrossOperator.and(all, (CrossOperator.SELECT | CrossOperator.INSERT)); 
		System.out.printf("and！ %d - %s\n", who, CrossOperator.translate(who));
		
		who = CrossOperator.or(all, (CrossOperator.SELECT | CrossOperator.INSERT)); 
		System.out.printf("or！ %d - %s\n", who, CrossOperator.translate(who));
		
		who = CrossOperator.and((CrossOperator.SELECT | CrossOperator.INSERT)  , (CrossOperator.DELETE | CrossOperator.UPDATE));
		System.out.printf("and！ %d - %s\n", who, "fuck"); // CrossOperator.translate(who));
		
		who = CrossOperator.xor(all, all);
		System.out.printf("xor！ %d - %s\n", who, "fuck"); // CrossOperator.translate(who));
		
		
//		System.out.printf("xor who is %d\n", who);
//		input = CrossOperator.translate(who);
		
//
//		int next = CrossOperator.and(who, who);
//		System.out.printf("and who is %d\n", next);
//		next = CrossOperator.xor(who, who);
//		System.out.printf("xor who is %d\n", next);
//		
//		who = CrossOperator.translate("all");
//		System.out.printf("%s\n",  CrossOperator.translate(who));


		//		int src =  (CrossOperator.SELECT | CrossOperator.INSERT);
		//		int dest =  (CrossOperator.DELETE | CrossOperator.INSERT);
		//		boolean success = CrossOperator.allow(src, dest);
		//		System.out.printf("allow is : %s\n", success);
	}

//	public static void main(String[] args) {
//		//		int who = (CrossOperator.INSERT|CrossOperator.UPDATE|CrossOperator.DELETE|CrossOperator.LEAVE);
//		int who = (CrossOperator.INSERT|CrossOperator.UPDATE|CrossOperator.DELETE|CrossOperator.SELECT);
//		String input = CrossOperator.translate(who);
//		System.out.printf("%d - %s - %d\n", who, input, CrossOperator.translate(input));
//
//		who = CrossOperator.xor(who, CrossOperator.UPDATE); //  ( who ^ .UPDATE);
//		System.out.printf("xor who is %d\n", who);
//		input = CrossOperator.translate(who);
//		System.out.printf("%d - %s - %d\n", who, input, CrossOperator.translate(input));
//
//		int next = CrossOperator.and(who, who);
//		System.out.printf("and who is %d\n", next);
//		next = CrossOperator.xor(who, who);
//		System.out.printf("xor who is %d\n", next);
//		
//		who = CrossOperator.translate("all");
//		System.out.printf("%s\n",  CrossOperator.translate(who));
//
//
//		//		int src =  (CrossOperator.SELECT | CrossOperator.INSERT);
//		//		int dest =  (CrossOperator.DELETE | CrossOperator.INSERT);
//		//		boolean success = CrossOperator.allow(src, dest);
//		//		System.out.printf("allow is : %s\n", success);
//	}

}