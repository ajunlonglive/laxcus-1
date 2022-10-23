/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.util.*;
import java.util.regex.*;

import com.laxcus.util.*;

/**
 * 分布描述语言的语法表述单元。<br>
 * 提供一行完整的分布描述语言中的正则表达式格式、关键字的定义。
 * 见FRONT/WATCH站点下的tokens.xml文件中的定义。
 * 
 * @author scott.liang
 * @version 1.1 07/02/2012
 * @since laxcus 1.0
 */
public class SyntaxItem implements Comparable<SyntaxItem> {

	/** 命令集合正则表达式（在一行中允许多个命令同时存在，如LOAD INDEX和LOAD CHUNK） **/
	private String COMMAND_REGEX;

	/** 关键字集合正则表达式（除命令之外需要高亮显示的字符） **/
	private String KEYWORD_REGEX;

	/** 数据类型集合正则表达式（表示参数单位的字符，如M、G、T） **/
	private String TYPE_REGEX;
	
	/** 单个类型的正则表达式定义 **/
	private ArrayList<String> typeRegexs = new ArrayList<String>();

	/** 以上全部参数集合的正则表达式，以“或（|）”的形式展现 **/
	private String WORD_REGEX;

	/** 命令名 **/
	private String commandText;
	
	/**
	 * 构造默认的分布描述语言的语法表述单元
	 */
	public SyntaxItem() {
		super();
	}

	/**
	 * 判断命令匹配
	 * @param input
	 * @return
	 */
	public boolean matchs(String input) {
		return input.matches(COMMAND_REGEX);
	}

	/**
	 * 判断匹配命令
	 * @param input 输入字
	 * @return 返回真或者假
	 */
	public boolean isCommand(String input) {
		return input.matches(COMMAND_REGEX);
	}

	/**
	 * 取匹配的命令字的长度
	 * @param input 输入文本
	 * @return 返回匹配字长度，不区配返回-1。
	 */
	public int getCommandSize(String input) {
		Pattern pattern = Pattern.compile(COMMAND_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String sub = matcher.group(1);
			return sub.length();
		}
		return -1;
	}

	/**
	 * 判断匹配关键字
	 * @param input 输入语字
	 * @return 返回真或者假
	 */
	public boolean isKeyword(String input) {
		return input.matches(KEYWORD_REGEX);
	}

	/**
	 * 取匹配的关键字的长度
	 * @param input 输入文本
	 * @return 返回字长度，不区配返回-1。
	 */
	public int getKeywordSize(String input) {
		Pattern pattern = Pattern.compile(KEYWORD_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String sub = matcher.group(1);
			return sub.length();
		}
		return -1;
	}

	/**
	 * 判断匹配类型
	 * @param input 输入字
	 * @return 返回真或者假
	 */
	public boolean isType(String input) {
		return input.matches(TYPE_REGEX);
	}

	/**
	 * 取匹配的类型字的长度
	 * @param input 输入文本
	 * @return 返回类型字长度，不区配返回-1。
	 */
	public int getTypeSize(String input) {
		Pattern pattern = Pattern.compile(TYPE_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String sub = matcher.group(1);
			return sub.length();
		}
		return -1;
	}

	//	/**
	//	 * 返回输入字符类型
	//	 * @param input
	//	 * @return
	//	 */
	//	public byte getFamily(String input) {
	//		if (isCommand(input)) {
	//			return SyntaxToken.COMMAND;
	//		} else if (isKeyword(input)) {
	//			return SyntaxToken.KEYWORD;
	//		} else if (isType(input)) {
	//			return SyntaxToken.TYPE;
	//		}
	//		return 0;
	//	}

	//	/**
	//	 * 返回输入字符类型
	//	 * @param input
	//	 * @return
	//	 */
	//	public byte getFamily(String input) {
	//		if (isCommand(input)) {
	//			return SyntaxToken.COMMAND;
	//		} else if (isKeyword(input) && isType(input)) {
	//			int keyLen = getKeywordSize(input);
	//			int typeLen = getTypeSize(input);
	//			return (keyLen > typeLen ? SyntaxToken.KEYWORD : SyntaxToken.TYPE);
	//		} else if (isKeyword(input)) {
	//			return SyntaxToken.KEYWORD;
	//		} else if (isType(input)) {
	//			return SyntaxToken.TYPE;
	//		}
	//		return 0;
	//	}

	/**
	 * 返回输入字符类型
	 * @param input
	 * @return
	 */
	public byte getFamily(String input) {
		if (isCommand(input)) {
			return SyntaxToken.COMMAND;
		}

		int key = getKeywordSize(input);
		int type = getTypeSize(input);
		// 判断字长度
		if (key > 0 && type > 0) {
			return (key > type ? SyntaxToken.KEYWORD : SyntaxToken.TYPE);
		} else if (key > 0) {
			return SyntaxToken.KEYWORD;
		} else if (type > 0) {
			return SyntaxToken.TYPE;
		}
		return 0;
	}

	/**
	 * 按照指定符号分割字符串。分割后的字符串，两侧空格过滤，中间的空格用正则空字符（\s+）代替。
	 * @param input 输入字符串
	 * @param regex 标记分割符号的正则表达式
	 * @return 输入分割后的字符串数组
	 */
	private ArrayList<String> split(String input, String regex) {
		ArrayList<String> a = new ArrayList<String>();
		String[] items = input.split(regex);
		for (int i = 0; i < items.length; i++) {
			String e = items[i].trim();
			if(e.isEmpty()) {
				continue;
			}
			e = e.replaceAll("\\s+", "\\\\s+"); // 空格用正则空格符号取代
			a.add(e);
		}
		return a;
	}

	/**
	 * 将多个字符串合并为一行，中间插入正则表达式符号，用于判断。
	 * @param array 字符串数组
	 * @param regex 正则表达式
	 * @return 返回新的正则字符串
	 */
	private String combin(List<String> array, String regex) {
		StringBuilder bf = new StringBuilder();
		for (int i = 0; i < array.size(); i++) {
			if (i > 0) {
				bf.append(regex);
			}
			bf.append(array.get(i));
		}
		return bf.toString();
	}

	/**
	 * 格式化XML配置文档中的三组字符串，转成正则表达式保留
	 * @param command 命令字符串
	 * @param keywords 关键字字符串
	 * @param types 数据类型字符串
	 */
	public void format(String command, String keywords, String types) {
		final String doute = "\\s*,\\s*"; // 逗号分割
		final String COMMAND_FORMAT = "^\\s*(?i)(%s)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
		final String WORD_FORMAT = "^(?:\\s*|[\\p{ASCII}\\W]*?[^a-zA-Z]{1}?)(?i)(%s)(\\s*|[^a-zA-Z]{1}[\\p{ASCII}\\W]{0,})$";

		ArrayList<String> a1 = split(command, doute);
		if (a1.size() > 0) {
			String text = combin(a1, "|"); // 合并，中间用“|”分开，这是正则表达式的“或”标记符
			COMMAND_REGEX = String.format(COMMAND_FORMAT, text);
		} else {
			COMMAND_REGEX = "";
		}

		ArrayList<String> a2 = split(keywords, doute);
		if (a2.size() > 0) {
			String text = combin(a2, "|");
			KEYWORD_REGEX = String.format(WORD_FORMAT, text);
		} else {
			KEYWORD_REGEX = "";
		}

		ArrayList<String> a3 = split(types, doute);
		
		if (a3.size() > 0) {
			String text = combin(a3, "|");
			TYPE_REGEX = String.format(WORD_FORMAT, text);
			typeRegexs.addAll(a3); //  保存单个类型单元
		} else {
			TYPE_REGEX = "";
		}

		// 合并全部参数
		a1.addAll(a2);
		a1.addAll(a3);
		String text = combin(a1, "|");
		WORD_REGEX = String.format(WORD_FORMAT, text);

		// System.out.printf("%s\n%s\n%s\n%s\n\n", commandRegex, keywordRegex,
		// typeRegex, wordRegex);
	}

	//	/**
	//	 * 解析字符串，返回键值集合
	//	 * @param input
	//	 * @return
	//	 */
	//	public SyntaxToken[] resolve_old(String input) {
	//		ArrayList<SyntaxToken> array = new ArrayList<SyntaxToken>();
	//
	//		/**
	//		 *  InputPanel.DefaultStyleDocument.setCharacterAttributes 定位时，
	//		 *  对“回车换行”符，忽略“回车”符号，只保留“换行”符号。这些做对应的处理。
	//		 */
	//		input = input.replaceAll("\r\n", "\n");
	//
	//		Pattern pattern = Pattern.compile(WORD_REGEX);
	//		Matcher matcher = pattern.matcher(input);
	//		int index = 0;
	//		do {
	//			if (!matcher.matches()) {
	//				break;
	//			}
	//			// 取出字符
	//			String word = matcher.group(1);
	//			// 下标位置
	//			int start = matcher.start(1);
	//
	//			// 判断属性
	//			byte family = getFamily(word);
	//			// 保存检索字符
	//			SyntaxToken token = new SyntaxToken(word, index + start, word.length(), family);
	//			array.add(token);
	//
	//			// 下一次检索位置
	//			index = index + start + word.length();
	//			// 新字符串重置
	//			String sub = input.substring(index);
	//			matcher = matcher.reset(sub);
	//		} while (true);
	//
	//		SyntaxToken[] a = new SyntaxToken[array.size()];
	//		return array.toArray(a);
	//	}
	
	/**
	 * 取出经过正则表达式框定的字符串，所需要的类型在最后面
	 * @return 返回过滤后的字符串，不匹配是空指针
	 */
	private String getPatternType(String input) {
		for (String regex : typeRegexs) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(input);
			boolean success = matcher.matches();
			if (success) {
				int count = matcher.groupCount();
				String sub = matcher.group(count);

				// // 取出实际参数下标位置
				// int off = input.length() - sub.length();
				//
				// System.out.printf("%s match %s, count:%d, off is: %d\n",
				// input, sub, matcher.groupCount(), off);

				return sub;
			}
		}

		return null;
	}

//	/**
//	 * 解析字符串，返回键值集合
//	 * @param input 输入文本
//	 * @return 返回切割后的标记符数组
//	 */
//	public SyntaxToken[] resolve(String input) {
//		ArrayList<SyntaxToken> array = new ArrayList<SyntaxToken>();
//
//		/**
//		 *  InputPanel.DefaultStyleDocument.setCharacterAttributes 定位时，
//		 *  对“回车换行”符，忽略“回车”符号，只保留“换行”符号。这些做对应的处理。
//		 */
//		input = input.replaceAll("\r\n", "\n");
//		
//
//		Pattern pattern = Pattern.compile(WORD_REGEX);
//		Matcher matcher = pattern.matcher(input);
//		int index = 0;
//		do {
//			if (!matcher.matches()) {
//				break;
//			}
//			// 取出字符
//			String word = matcher.group(1);
//			// 下标位置
//			int start = matcher.start(1);
//
//			// 判断属性
//			byte family = getFamily(word);
//			
//			// 如果是类型，判断它符合正则表达式特别类型的定义
//			if (family == SyntaxToken.TYPE) {
//				String sub  = getPatternType(word);
//				if(sub != null) {
//					System.out.printf("%s 实际值是：%s\n",word, sub);
////					// 保存检索字符
////					SyntaxToken token = new SyntaxToken(word, index + start, word.length(), family);
////					array.add(token);
//				}
//			}
//			
//			// 保存检索字符
//			SyntaxToken token = new SyntaxToken(word, index + start, word.length(), family);
//			array.add(token);
//
//			// 下一次检索位置
//			index = index + start + word.length();
//			// 新字符串重置
//			String sub = input.substring(index);
//			matcher = matcher.reset(sub);
//		} while (true);
//
//		SyntaxToken[] a = new SyntaxToken[array.size()];
//		return array.toArray(a);
//	}
	
	/**
	 * 解析字符串，返回键值集合
	 * @param input 输入文本
	 * @return 返回切割后的标记符数组
	 */
	public SyntaxToken[] resolve(String input) {
		ArrayList<SyntaxToken> array = new ArrayList<SyntaxToken>();

		/**
		 *  InputPanel.DefaultStyleDocument.setCharacterAttributes 定位时，
		 *  对“回车换行”符，忽略“回车”符号，只保留“换行”符号。这些做对应的处理。
		 */
		input = input.replaceAll("\r\n", "\n");

		Pattern pattern = Pattern.compile(WORD_REGEX);
		Matcher matcher = pattern.matcher(input);
		int index = 0;
		do {
			if (!matcher.matches()) {
				break;
			}
			// 取出字符
			String word = matcher.group(1);
			// 下标位置
			int start = matcher.start(1);

			// 判断属性
			byte family = getFamily(word);
			
			// 判断是子类型
			boolean isSubType = false;
			
			// 如果是类型，判断它符合正则表达式特别类型的定义
			if (family == SyntaxToken.TYPE) {
				String sub = getPatternType(word);
				isSubType = (sub != null);
				if (isSubType) {
//					System.out.printf("%s 实际值是：%s\n", word, sub);
					int left = word.length() - sub.length(); // 溢出的尺寸
					// 保存检索字符
					SyntaxToken token = new SyntaxToken(sub, index + start + left, sub.length(), family);
					array.add(token);
				}
			}
			
			// 没有子类型，原样保存检索字符
			if (!isSubType) {
				SyntaxToken token = new SyntaxToken(word, index + start, word.length(), family);
				array.add(token);
			}

			// 下一次检索位置
			index = index + start + word.length();
			// 新字符串重置
			String sub = input.substring(index);
			matcher = matcher.reset(sub);
		} while (true);

		SyntaxToken[] a = new SyntaxToken[array.size()];
		return array.toArray(a);
	}
	
	/**
	 * 设置命令文本，过滤多个空格！
	 * @param input 输入语句
	 */
	public void setCommandText(String input) {
		Laxkit.nullabled(input);
		commandText = input.replaceAll("\\s+", " ");
	}
	
	/**
	 * 返回命令文本
	 * @return 字符串
	 */
	public String getCommandText() {
		return commandText;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SyntaxItem that) {
		if (that == null) {
			return 0;
		}
		// 1. 比较长度
		int len1 = commandText.length();
		int len2 = that.commandText.length();
		int ret = (len1 < len2 ? 1 : (len1 > len2 ? -1 : 0));
		// 2. 比较字符串，升序排序
		if(ret == 0) {
			ret = commandText.compareToIgnoreCase(that.commandText);
		}
		return ret;
	}

	//	/**
	//	 * 按照指定的符号进行分割
	//	 * @param input
	//	 * @param tag
	//	 * @return
	//	 */
	//	private String[] split(String input, String tag) {
	//		return input.split(tag);
	//	}

	//	/**
	//	 * 按照指定符号进行组合
	//	 * @param a
	//	 * @param tag
	//	 * @return
	//	 */
	//	private String combin(String[] a, String tag) {
	//		StringBuilder bf = new StringBuilder();
	//		for (int i = 0; a != null && i < a.length; i++) {
	//			if (i > 0) {
	//				bf.append(tag);
	//			}
	//			bf.append(a[i].trim());
	//		}
	//		return bf.toString();
	//	}

	//	/**
	//	 * 按照指定符号进行组合
	//	 * @param a1
	//	 * @param a2
	//	 * @param tag
	//	 * @return
	//	 */
	//	private String combin(String[] a1, String[] a2, String tag) {
	//		String s1 = combin(a1, tag);
	//		String s2 = combin(a2, tag);
	//		if (s2.length() > 0) {
	//			return s1 + tag + s2;
	//		}
	//		return s1;
	//	}


	//	/**
	//	 * 格式式原始字符串，转成正则表达式保留
	//	 * @param command
	//	 * @param keywords
	//	 * @param types
	//	 */
	//	public void format1(String command, String keywords, String types) {
	//		final String COMMAND_REGEX = "^\\s*(?i)(%s)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	//		final String WORD_REGEX = "^(?:\\s*|[\\p{ASCII}\\W]*?[^a-zA-Z]{1}?)(?i)(%s)(\\s*|[^a-zA-Z]{1}[\\p{ASCII}\\W]{0,})$";
	//		final String doute = "\\s*,\\s*"; // 逗号分割
	//
	//		// 命令前缀
	//		String[] items = split(command, " ");
	//		String res = combin(items, "\\s+");
	//		COMMAND_REGEX = String.format(COMMAND_REGEX, res);		
	//
	//		// 命令关键字
	//		String[] kws = split(keywords, doute);
	//		res = combin(kws, "|");
	//		KEYWORD_REGEX = String.format(WORD_REGEX, res);
	//
	//		// 数据类型
	//		String[] tps = split(types, doute); 
	//		res = combin(tps, "|");
	//		TYPE_REGEX = String.format(WORD_REGEX, res);
	//
	//		// 全部键值
	//		res = combin(kws, tps, "|");
	//		WORD_REGEX = String.format(WORD_REGEX, res);
	//
	////		System.out.printf("%s\n%s\n%s\n%s\n\n", commandRegex, keywordRegex, typeRegex, wordRegex);
	//	}	

	//	public static void main(String[] args) {
	//		InputItem item = new InputItem();
	//		item.wordRegex = "^(?:\\s*|[\\p{ASCII}\\W^a-zA-Z]+?)(?i)(CONDUCT|INIT|FROM|TO|BALANCE|PUT|WRITETO|SITES|DIFFUSE|SUBTO|NEXTO|TASK|GENERATE|COMPUTE|QUERY|RAW|BINARY|BOOLEAN|BOOL|CHAR|STRING|DATE|TIME|DATETIME|TIMESTAMP|SMALLINT|SHORT|INT|LONG|BIGINT|FLOAT|REAL|DOUBLE)(\\s*|[^a-zA-Z]{1}[\\p{ASCII}\\W]{0,})$";
	//		String input = "CONDUCT SYSTEM_SORT FROM SITES:6,BEGIN(INT)=1,END(INT)=10000,TOTAL(INTINT)=100 TO SITES:9,ORDERBY(STRING)='DESC',TITLE(CHAR)='大江东去，浪淘沙，忆秦娥' PUT WRITETO:\"/notes/randoms.bin\"";
	//		Token[] ts = item.resolve(input);
	//		System.out.printf("item size %d\n", ts.length);
	//	}

	//	public static void main(String[] args) {
	//		InputItem item = new InputItem();
	////		final String doute = "\\s*,\\s*"; // 逗号分割
	//		String command = " STOP INDEX,UNLOAD INDEX";
	//		String keywords = "STOP,UNLOAD,INDEX,FROM";
	//		String types = "";
	//		item.format(command, keywords, types);
	//		String input = " stop index System.pentium from  localhost  ";
	//		Token[] tokens = item.resolve(input);
	//		for(int i =0; i < tokens.length; i++) {
	//			System.out.printf("%s\n", tokens[i].getWord());
	//		}
	//	}

	public static void main(String[] args) {
		SyntaxItem item = new SyntaxItem();
		String command = " PUBLISH ASSIST TASK";
		String keywords = "PUBLISH, ASSIST, TASK, TO, From";
//		String types = "CONDUCT.TO, CONDUCT.FROM, ESTABLISH.ISSUE, ESTABLISH.RISE, ESTABLISH.SCAN , M";
		String types = "CONDUCT.TO, CONDUCT.FROM, ESTABLISH.ISSUE, ESTABLISH.RISE, ESTABLISH.SCAN , [0-9\\s]+(?i)(M|MB|G|GB|T|TB|P|PB)";
		
		item.format(command, keywords, types);

		System.out.println(item.COMMAND_REGEX);
		System.out.println(item.KEYWORD_REGEX);
		System.out.println(item.TYPE_REGEX);

		String input = "  PUBLISH ASSIST TASK From  e:/tasks/aixbit_ext.jar, e:/tasks/vockx.jar  To  conduct.from , conduct.to, ESTABLISH.SCAN, ESTABLISH.ISSUE, ESTABLISH,RISE  123mb ";

		System.out.println(input);
		
		SyntaxToken[] tokens = item.resolve(input);
		
		// 打印它！
		for(int i =0; i < tokens.length; i++) {
			SyntaxToken token = tokens[i];
			if (token.isKeyword()) {
				System.out.printf("关键字 | %s | %d - %d\n", token.getWord(),  token.getIndex(), token.getSize());
			} else if (token.isType()) {
				System.out.printf("类型 | %s | %d - %d\n", token.getWord(), token.getIndex(), token.getSize());
			} else if (token.isCommand()) {
				System.out.printf("命令 | %s | %d - %d\n", token.getWord(),  token.getIndex(), token.getSize());
			}

			//			System.out.printf("%s | type:%d | %d - %d\n", token.getWord(), token.getFamily(), token.getIndex(), token.getSize());
		}
	}



}