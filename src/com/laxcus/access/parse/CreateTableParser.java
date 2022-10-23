/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.*;

/**
 * 建表命令解析器 <br>
 * 
 * <pre>
 * 建表格式：
 * 
 * CREATE TABLE
 * 		[SM|STORAGEMODEL=DSM|NSM|COLUMNS|ROWS]
 * 		[MULTIPLE={digit}]
 * 		[HOSTMODEL|HM=SHARE|EXCLUSIVE]
 * 		[CHUNKSIZE={digit}M]
 * 		[CHUNKCOPY={digit}]
 * 		[PRIMEHOSTS={digit}]
 * 		
 * DATABASE-NAME.TABLE-NAME
	(
		[COLUMN-NAME COLUMN-TYPE 
			[NOT NULL|NULL]
			[NOT CASE|CASE]
			[NOT LIKE|LIKE]
			[PRIME KEY|SLAVE KEY[(digit)]]
			[DEFAULT [{string}]|[{digit}]|[{function description}]]
			[PACKING [encrypt-name:'{password}'] AND [compress-name]]
			[CHECK ({description}]
		]

		[COLUMN-NAME COLUMN-DATATYPE ...]
	}

	PUBLISH TO [3] [HOME://12.9.23.23:2300_2300, HOME://123.23.23.88:900_2333]
	</pre>
 *
 *
 * @author scott.liang
 * @version 1.3 11/26/2013
 * @since laxcus 1.0
 */
public class CreateTableParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:CREATE\\s+TABLE)\\s+([\\w\\W]+)\\s*$";

	/** 集群地址 **/
	private final static String PUSLISH_TO_NUMBER = "^\\s*(?i)(?:PUBLISH\\s+TO)\\s+([0-9]+)\\s+(?i)(?:GROUPS)\\s*$";
	private final static String PUBLISH_TO_SITES = "^\\s*(?i)(?:PUBLISH\\s+TO)\\s+([\\w\\W]+?)\\s*$";

	/** 建表格式语法：CREATE TABLE [SM=NSM,PRIMEHOSTS=3,HOSTMODE=SHARE,CHUNKCOPY=3,CHUNKSIZE=64M] database.table (id int, word char not null like not case default 'helo', ...) [PUBLISH TO {NUMBER}|HOME SITE, HOME SITE] **/
	private final static String CREATE_TABLE = "\\s*(?i)(?:CREATE\\s+TABLE)(\\s+|\\s+[\\w\\W]+?\\s+)([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\(([\\w\\W]+?)\\)(\\s*|\\s+PUBLISH\\s+TO\\s+[\\w\\W]+)$";
	
	/** 建表时的配置参数集  **/
	private final static String TABLE_PREFIX_STORAGEMODEL = "^\\s*(?i)(?:SM|STORAGEMODEL)\\s*=\\s*(?i)(DSM|COLUMNS|NSM|ROWS)(\\s*|\\s+[\\w\\W]+)$";
	private final static String TABLE_PREFIX_PRIMEHOSTS = "^\\s*(?i)PRIMEHOSTS\\s*=\\s*(\\d+)(\\s*|\\s+[\\w\\W]+)$";
	private final static String TABLE_PREFIX_HOSTMODE = "^\\s*(?i)(?:HM|HOSTMODE)\\s*=\\s*(?i)(SHARE|EXCLUSIVE)(\\s*|\\s+[\\w\\W]+)$";
	private final static String TABLE_PREFIX_CHUNKSIZE = "^\\s*(?i)CHUNKSIZE\\s*=\\s*(\\d+)(?i)M(\\s*|\\s+[\\w\\W]+)$";
	private final static String TABLE_PREFIX_CHUNKCOPY = "^\\s*(?i)CHUNKCOPY\\s*=\\s*(\\d+)(\\s*|\\s+[\\w\\W]+)$";
	private final static String TABLE_PREFIX_MULTIPLE = "^\\s*(?i)MULTIPLE\\s*=\\s*(\\d+)(\\s*|\\s+[\\w\\W]+)$";
	
	/** 列的基本属性， NULL|NOT NULL, CASE|NOT CASE, LIKE|NOT LIKE. 后两项只限可变长类型 **/
	private final static String TABLE_COLUMN_NULLORNOT = "^\\s*(?i)(NOT\\s+NULL|NULL)(\\s+[\\w\\W]+|\\s*)$";
	private final static String TABLE_COLUMN_CASEORNOT = "^\\s*(?i)(NOT\\s+CASE|CASE)(\\s+[\\w\\W]+|\\s*)$";
	private final static String TABLE_COLUMN_LIKEORNOT = "^\\s*(?i)(NOT\\s+LIKE|LIKE)(\\s+[\\w\\W]+|\\s*)$";

	/** PAKCING. 压缩和加密. 格式: packing des:'unix' | packing gzip | packing gzip and des:'unix' | packing des:'unix' and gzip **/
	private final static String ENCRYPT_COMPRESS = "^\\s*(?i)PACKING\\s+(\\w+)\\s*\\:\\s*\\'(\\p{Graph}+)\\'\\s+(?i)(?:AND)\\s+(\\w+)(\\s+[\\w\\W]+|\\s*)$"; // 加密和压缩
	private final static String COMPRESS_ENCRYPT = "^\\s*(?i)PACKING\\s+(\\w+)\\s+(?i)(?:AND)\\s+(\\w+)\\s*\\:\\s*\\'(\\p{Graph}+)\\'(\\s+[\\w\\W]+|\\s*)$"; // 压缩和加密
	private final static String ENCRYPT = "^\\s*(?i)PACKING\\s+(\\w+)\\s*\\:\\s*\\'(\\p{Graph}+)\\'(\\s+[\\w\\W]+|\\s*)$";  // 加密
	private final static String COMPRESS = "^\\s*(?i)PACKING\\s+(\\w+)(\\s+[\\w\\W]+|\\s*)$"; // 压缩

	/** 备注 **/
	private final static String COMMENT = "^\\s*(?i)COMMENT\\s+\\'([\\w\\W]+?)\\'(\\s+[\\w\\W]+|\\s*)$";
	
	/** 键判断. 格式: PRIME KEY|SLAVE KEY [(20)]. 可变长类型列需要指定长度，默认是16字节 **/
	private final static String INDEXKEY_LIMIT = "^\\s*(?i)(PRIME\\s+KEY|SLAVE\\s+KEY)\\s*\\(\\s*([0-9]+)\\s*\\)(\\s+[\\w\\W]+|\\s*)$";
	private final static String INDEXKEY = "^\\s*(?i)(PRIME\\s+KEY|SLAVE\\s+KEY)(\\s+[\\w\\W]+|\\s*)$";

	/** 默认值判断，包括字符串,整数值,浮点值,函数 **/
	private final static String DEFAULT_VARIABLE = "^\\s*(?i)DEFAULT\\s+\\'([\\w\\W]+?)\\'(\\s+[\\w\\W]+|\\s*)$";
	private final static String DEFAULT_DIGIT_FLOAT = "^\\s*(?i)DEFAULT\\s+([-+]?\\d+\\.\\d+)(\\s+[\\w\\W]+|\\s*)$";
	private final static String DEFAULT_DIGIT_CONST = "^\\s*(?i)DEFAULT\\s+([-+]?\\d+)(\\s+[\\w\\W]+|\\s*)$";
	private final static String DEFAULT_FUNCTION = "^\\s*(?i)DEFAULT\\s+(\\w+\\s*\\(\\s*.*\\s*\\))(\\s+[\\w\\W]+|\\s*)$";

	/** 列类型，名称在20个字符范围内，带精度/标度，格式如：id int(23), money double(12,5)... **/
	private final static String TABLE_COLUMN1 = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s+(?i)(RAW|BINARY|DOCUMENT|IMAGE|AUDIO|VIDEO|CHAR|WCHAR|HCHAR|SHORT|SMALLINT|INT|INTEGER|LONG|BIGINT|REAL|FLOAT|DOUBLE|TIMESTAMP|DATETIME|DATE|TIME)\\s*\\(([0-9\\,\\s]+)\\)\\s+(.*)\\s*$";
	
	/** RAW|BINARY|DOCUMENT|IMAGE|AUDIO|VIDEO|CHAR|WCHAR|HCHAR|SHORT|SMALLINT|INT|INTEGER|LONG|BIGINT|REAL|FLOAT|DOUBLE|TIMESTAMP|DATE|TIME **/
	/** 列类型格式。列名称限制为20个字符，没有精度和标度 **/
	private final static String TABLE_COLUMN2 = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s+(?i)(RAW|BINARY|DOCUMENT|IMAGE|AUDIO|VIDEO|CHAR|WCHAR|HCHAR|SHORT|SMALLINT|INT|INTEGER|LONG|BIGINT|REAL|FLOAT|DOUBLE|TIMESTAMP|DATETIME|DATE|TIME)\\s*(.*)\\s*$";

	/** 检查列标记. 后缀三种情况:空格|逗号和其它参数|空格和其它参数 **/
	private final static String COLUMN_CHECKTAG = "^\\,\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s+(?i)(RAW|BINARY|DOCUMENT|IMAGE|AUDIO|VIDEO|CHAR|WCHAR|HCHAR|SHORT|SMALLINT|INT|INTEGER|LONG|BIGINT|FLOAT|REAL|DOUBLE|TIMESTAMP|DATETIME|DATE|TIME)(\\s*|\\s*\\,[\\w\\W]+|\\s+[\\w\\W]+|\\s*\\(([0-9\\,\\s]+)\\)\\s+[\\w\\W]+)$";
	
//	private final static String COLUMN_CHECKTAG = "^\\,\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s+(?i)(RAW|BINARY|DOCUMENT|IMAGE|AUDIO|VIDEO|CHAR|WCHAR|HCHAR|SHORT|SMALLINT|INT|INTEGER|LONG|BIGINT|FLOAT|REAL|DOUBLE|TIMESTAMP|DATETIME|DATE|TIME)(\\s*|\\s*\\,[\\w\\W]+|\\s+[\\w\\W]+)$";

	/** 精度和标度判断  **/
	private final static String COLUMN_MD1 = "^\\s*([0-9]+)\\s*\\,\\s*([0-9]+)\\s*$";
	
	/** 精度判断  **/
	private final static String COLUMN_MD2 = "^\\s*([0-9]+)\\s*$";
	
	
	/**
	 * 构造数据表解析器
	 */
	public CreateTableParser() {
		super();
	}

	/**
	 * 判断匹配建表语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CREATE TABLE", input);
		}
		Pattern pattern = Pattern.compile(CreateTableParser.CREATE_TABLE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析 "SM|STORAGEMODEL=DSM|NSM|ROWS|COLUMNS"
	 * 数据在主机上的物理存储模式，分为行存储(NSM)和列存储(DSM)
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	private String splitStorageModel(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_STORAGEMODEL);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input; // 不匹配,原样返回
		}

		String model = matcher.group(1);
		input = matcher.group(2);

		// 选择存储模型
		if(StorageModel.isNSM(model)) {
			table.setStorage(StorageModel.NSM);
		} else if(StorageModel.isDSM(model)) {
			table.setStorage(StorageModel.DSM);
		}

		//		if (model.matches("^\\s*(?i)(NSM|ROWS)\\s*$")) {
		//			table.setStorage(StorageModel.NSM);
		//		} else if (model.matches("^\\s*(?i)(DSM|COLUMNS)\\s*$")) {
		//			table.setStorage(StorageModel.DSM);
		//		}

		//		if ("NSM".equalsIgnoreCase(model) || "ROWS".equalsIgnoreCase(model)) {
		//			table.setStorage(StorageModel.NSM);
		//		} else if ("DSM".equalsIgnoreCase(model) || "COLUMNS".equalsIgnoreCase(model)) {
		//			table.setStorage(StorageModel.DSM);
		//		}

		// 返回剩余字符串
		return matcher.group(2);
	}

	/**
	 * 解析 "hostmodel=share|exclusive"
	 * 被分配表在主机上的存在状态: 共亨或者独占
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	private String splitHostModel(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_HOSTMODE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input; // 不匹配,原样返回
		}

		// 取出模式
		String text = matcher.group(1);
		int mode = TableMode.translate(text);
		table.setSiteMode(mode);
		// 返回剩余字符串
		return matcher.group(2);
	}

	/**
	 * 解析 "chunksize={digit}M"
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	private String splitChunkSize(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_CHUNKSIZE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input; // 不匹配,原样返回
		}

		// 匹配,计算数据块尺寸
		String size = matcher.group(1);
		int value = java.lang.Integer.parseInt(size);
		table.setChunkSize(value * Laxkit.mb);
		// 返回剩余字符串
		return matcher.group(2);
	}

	/**
	 * 解析 "chunkcopy=[digit]"
	 * @param table
	 * @param input
	 * @return
	 */
	private String splitChunkCopy(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_CHUNKCOPY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input; // 不匹配,原值返回
		}

		// 匹配,数据的备份数(一个主块,N个从块)
		String num = matcher.group(1);
		table.setChunkCopy(java.lang.Integer.parseInt(num));
		// 返回剩余字节
		return matcher.group(2);
	}

	/**
	 * 解析 "multiple=[digit]"，DSM表压缩倍数
	 * @param table 表实例
	 * @param input 输入语句
	 * @return 返回剩余字节
	 */
	private String splitMultiple(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_MULTIPLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input; // 不匹配,原值返回
		}

		// 匹配,数据的备份数(一个主块,N个从块)
		String num = matcher.group(1);
		int multiple = java.lang.Integer.parseInt(num);
		if (multiple < 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, num);
		}
		table.setMultiple(multiple);
		// 返回剩余字节
		return matcher.group(2);
	}
	
	/**
	 * 解析 "primehosts={digit}"
	 * @param table
	 * @param input
	 * @return
	 */
	private String splitPrimeHosts(Table table, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_PREFIX_PRIMEHOSTS);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input;
		}

		String hosts = matcher.group(1);
		table.setPrimeSites(java.lang.Integer.parseInt(hosts));
		return matcher.group(2);
	}

	/**
	 * 解析发布地址
	 * @param input
	 * @return
	 */
	private Domain splitPublishTo(String input) {
		Domain domain = new Domain();

		Pattern pattern = Pattern.compile(CreateTableParser.PUSLISH_TO_NUMBER);
		Matcher matcher = pattern.matcher(input);

		// 解析主机数量
		if (matcher.matches()) {
			int sites = java.lang.Integer.parseInt(matcher.group(1));
			domain.setSites(sites);
			return domain;
		}

		pattern = Pattern.compile(CreateTableParser.PUBLISH_TO_SITES);
		matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			super.throwable(input);
		}

		String suffix = matcher.group(1);
		String[] items = suffix.split(",");
		for (int i = 0; i < items.length; i++) {
			try {
				com.laxcus.site.Node site = new com.laxcus.site.Node(items[i]);
				// 只允许HOME主机
				if (!site.isHome()) {
//					throwable("illegal site:%s", items[i]);
					throwableNo(FaultTip.ILLEGAL_SITE_X, items[i]);
				}
				domain.add(site);
			} catch (UnknownHostException e) {
				super.throwable(items[i]);
			}
		}

		return domain;
	}

	/**
	 * 分析在"CREATE TABLE" 和 "SCHEMA-NAME.TABLE-NAME"之间的数据
	 * @param table
	 * @param prefix
	 */
	private void splitTablePrefix(Table table, String prefix) {
		while (prefix.trim().length() > 0) {
			// 1. 数据存储模式
			String result = splitStorageModel(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 2. DSM表压缩倍数
			result = splitMultiple(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 2. 表在主机的存在模式(共亨物理空间还是独占)
			result = splitHostModel(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 4. 数据块尺寸
			result = splitChunkSize(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 5. 数据块备份数(一个主块,N个从块)
			result = splitChunkCopy(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 6. 表分配到主节点(DATA NODE)数量
			result = splitPrimeHosts(table, prefix);
			if (!prefix.equals(result)) {
				prefix = result;
				continue;
			}
			// 8. 出错
			//			throw new SyntaxException("syntax error:%s", prefix);

			throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
		}
	}


	/**
	 * 解析 列属性中的 "NOT NULL|NULL"
	 * @param attribute
	 * @param input
	 * @return
	 */
	private String splitNull(ColumnAttribute attribute, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN_NULLORNOT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String s = matcher.group(1);
			attribute.setNull("NULL".equalsIgnoreCase(s)); 	//允许空 或者不允许
			return matcher.group(2); 	//返回剩下的字符串
		}
		// 不匹配,完全返回
		return input;
	}

	/**
	 * 解析列属性中的 "NOT CASE|CASE"
	 * @param attribute
	 * @param input
	 * @return
	 */
	private String splitCase(ColumnAttribute attribute, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN_CASEORNOT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			if (!attribute.isWord()) {
//				throw new SyntaxException("%s cannot support 'CASE or NOT CASE'", attribute.getNameText());
				
				throwablePrefixFormat("\'CASE or NOT CASE\'", FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
			}

			WordAttribute instan = (WordAttribute)attribute;
			String s = matcher.group(1);
			instan.setSentient("CASE".equalsIgnoreCase(s)); //CASE，大小写敏感。NOT CASE，大小写不敏感

			return matcher.group(2); 	// 返回剩下的字符串
		}
		// 不匹配,完全返回
		return input;
	}

	/**
	 * 解析列属性中的 "NOT LIKE|LIKE"
	 * @param attribute
	 * @param input
	 * @return
	 */
	private String splitLike(ColumnAttribute attribute, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN_LIKEORNOT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			if (!attribute.isWord()) {
//				throw new SyntaxException("%s cannot support 'LIKE or NOT LIKE'", attribute.getNameText());
				throwablePrefixFormat("\'LIKE or NOT LIKE\'", FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
			}

			WordAttribute instan = (WordAttribute) attribute;
			String s = matcher.group(1);
			instan.setLike("LIKE".equalsIgnoreCase(s));

			return matcher.group(2); // 返回剩下的字符串
		}
		// 不匹配,完全返回
		return input;
	}

	/**
	 * 解析 "DEFAULT [...]" 语句
	 * @param attribute
	 * @param input
	 * @return
	 */
	private String splitDefault(ColumnAttribute attribute, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.DEFAULT_VARIABLE);
		Matcher matcher = pattern.matcher(input);

		//1. 字符串格式判断。包括字符和日期
		if(matcher.matches()) {
			String s = matcher.group(1);
			if (attribute.isWord()) {
				// 字符需要编码保存
				WordAttribute instan = (WordAttribute) attribute;
				Charset charset = instan.getCharset();
				instan.setValue( charset.encode(s) );
			} else if (attribute.isDate()) {
				// 转换为日期格式
				int date = CalendarGenerator.splitDate(s);
				((DateAttribute) attribute).setValue(date);
			} else if (attribute.isTime()) {
				// 转换为时间格式
				int time = CalendarGenerator.splitTime(s);
				((TimeAttribute) attribute).setValue(time);
			} else if (attribute.isTimestamp()) {
				// 转换为时间戳格式
				long timestamp = CalendarGenerator.splitTimestamp(s);
				((TimestampAttribute) attribute).setValue(timestamp);
			} else {
//				throw new SyntaxException("%s cannot support %s", attribute.getNameText(), input);
				throwablePrefixFormat(input, FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
			}
			return matcher.group(2); // 返回剩下的字符串
		}

		//2. 浮点数格式判断
		pattern = Pattern.compile(CreateTableParser.DEFAULT_DIGIT_FLOAT);
		matcher = pattern.matcher(input);
		if(matcher.matches()) {
			String s = matcher.group(1);
			if(attribute.isFloat()) {
				float value = java.lang.Float.parseFloat(s);
				((FloatAttribute) attribute).setValue(value);
			} else if(attribute.isDouble()) {
				double value = java.lang.Double.parseDouble(s);
				((DoubleAttribute) attribute).setValue(value);
			} else {
//				throw new SyntaxException("%s is invalid!", input);
				throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
			}
			return matcher.group(2);
		}

		//3. 整数格式判断
		pattern = Pattern.compile(CreateTableParser.DEFAULT_DIGIT_CONST);
		matcher = pattern.matcher(input);
		if ( matcher.matches()) {
			String s = matcher.group(1);
			if (attribute.isShort()) {
				short value = java.lang.Short.parseShort(s);
				((ShortAttribute) attribute).setValue(value);
			} else if(attribute.isInteger()) {
				int value = java.lang.Integer.parseInt(s);
				((IntegerAttribute) attribute).setValue(value);
			} else if(attribute.isLong()) {
				long value = java.lang.Long.parseLong(s);
				((LongAttribute) attribute).setValue(value);
			} else {
				throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
				//	throw new SyntaxException("%s is invalid!", input);
			}
			return matcher.group(2);
		}

		//4. 函数格式判断
		pattern = Pattern.compile(CreateTableParser.DEFAULT_FUNCTION);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String description = matcher.group(1);
			ColumnFunction function = ColumnFunctionCreator.create(null, description);
			if(function == null) {
				throwableNo(FaultTip.NOTFOUND_X, input);
			}
			// 检查是否支持默认参数
			if (!function.isSupportDefault()) {
				throwableNo(FaultTip.NOTSUPPORT_X, input);
			} else if (function.getResultFamily() != attribute.getType()) {
				throwableNo(FaultTip.NOTMATCH_X,
						ColumnType.translate(attribute.getType()),
						ColumnType.translate(function.getResultFamily()));
			}
			attribute.setFunction(function);

			return matcher.group(2);
		}

		// 不匹配,完全返回
		return input;
	}
	
	/**
	 * 解析备注，COMMENT ... 子句
	 * @param attribute 列属性
	 * @param input 输入语句
	 * @return 返回剩余字符串
	 */
	private String splitComment(ColumnAttribute attribute, String input) {
		Pattern pattern = Pattern.compile(CreateTableParser.COMMENT);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		// 不匹配，忽略，原文返回
		if (!match) {
			return input;
		}

		String comment = matcher.group(1);
		String suffix = matcher.group(2);
		
		// 限制在1024字符以内
		if (comment.length() > 1024) {
			throwableNo(FaultTip.FAILED_X, comment);
		}

		attribute.setComment(comment);
		// 返回剩余字符串
		return suffix;
	}

	/**
	 * 解析"PACKING [...]"子句
	 * @param attribute
	 * @param input
	 */
	private String splitPacking(ColumnAttribute attribute, String input) {
		String encrypt = null;
		byte[] password = null;
		String compress = null;
		String suffix = null;

		Pattern pattern = Pattern.compile(CreateTableParser.ENCRYPT_COMPRESS);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if(match) {
			encrypt = matcher.group(1);
			password = matcher.group(2).getBytes();
			compress = matcher.group(3);
			suffix = matcher.group(4);
		}
		if(!match) {
			pattern = Pattern.compile(CreateTableParser.COMPRESS_ENCRYPT);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				compress = matcher.group(1);
				encrypt = matcher.group(2);
				password = matcher.group(3).getBytes();
				suffix = matcher.group(4);
			}
		}
		if(!match) {
			pattern = Pattern.compile(CreateTableParser.ENCRYPT);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				encrypt = matcher.group(1);
				password = matcher.group(2).getBytes();
				suffix = matcher.group(3);
			}
		}
		if(!match) {
			pattern = Pattern.compile(CreateTableParser.COMPRESS);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				compress = matcher.group(1);
				suffix = matcher.group(2);
			}
		}

		// 如果最后不匹配,原值返回
		if(!match) return input; 

		// PACKING 属性只限可变长类型
		if (!attribute.isVariable()) {
//			throw new SyntaxException("%s is not variable", attribute.getNameText());
			throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
		}

		VariableAttribute variable = (VariableAttribute)attribute;

		int encryptId = 0, compressId = 0;

		//		// 压缩算法名称
		//		if ("GZIP".equalsIgnoreCase(compress)) {
		//			compressId = PackingTag.GZIP;
		//		} else if ("ZIP".equalsIgnoreCase(compress)) {
		//			compressId = PackingTag.ZIP;
		//		}
		//		// 加密算法名称
		//		if ("DES".equalsIgnoreCase(encrypt)) {
		//			encryptId = PackingTag.DES;
		//		} else if ("DES3".equalsIgnoreCase(encrypt) || "3DES".equalsIgnoreCase(encrypt)) {
		//			encryptId = PackingTag.DES3;
		//		} else if ("AES".equalsIgnoreCase(encrypt)) {
		//			encryptId = PackingTag.AES;
		//		} else if ("blowfish".equalsIgnoreCase(encrypt)) {
		//			encryptId = PackingTag.BLOWFISH;
		//		}

		// 压缩算法
		if (PackingTag.isGZIP(compress)) {
			compressId = PackingTag.GZIP;
		} else if (PackingTag.isZIP(compress)) {
			compressId = PackingTag.ZIP;
		}
		// 加密算法
		if (PackingTag.isDES(encrypt)) {
			encryptId = PackingTag.DES;
		} else if (PackingTag.is3DES(encrypt)) {
			encryptId = PackingTag.DES3;
		} else if (PackingTag.isAES(encrypt)) {
			encryptId = PackingTag.AES;
		} else if (PackingTag.isBlowfish(encrypt)) {
			encryptId = PackingTag.BLOWFISH;
		}

		variable.setPacking(compressId, encryptId, password);

		return suffix;
	}

	/**
	 * 解析主键和从键.可变长类型可指定字符长度(注意，是字符，不是字节)
	 * 格式: "PRIME KEY|SLAVE KEY [(key size)]"
	 * @param attribute
	 * @param input
	 * @return
	 */
	private String splitKey(ColumnAttribute attribute, String input) {
		String key = null;
		int indexLimit = 0;
		String suffix = null;

		Pattern pattern = Pattern.compile(CreateTableParser.INDEXKEY_LIMIT);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			key = matcher.group(1);
			indexLimit = java.lang.Integer.parseInt(matcher.group(2));
			suffix = matcher.group(3);
		}
		if (!match) {
			pattern = Pattern.compile(CreateTableParser.INDEXKEY);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				key = matcher.group(1);
				suffix = matcher.group(2);
			}
		}

		// 不匹配，原值返回
		if (!match) {
			return input;
		}

		// 判断是主键还是从键
		byte rank = (key.matches("^(?i)(PRIME\\s+KEY)$") ? KeyType.PRIME_KEY : KeyType.SLAVE_KEY);
		attribute.setKey(rank);

		// 如果定义截取字节长度，必须是可变长类型
		if (indexLimit > 0) {
			if (!attribute.isVariable()) { // 必须有可变长类型
//				throw new SyntaxException("'%s' is not vairable!", attribute.getNameText());
				throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
			}
			((VariableAttribute) attribute).setIndexSize(indexLimit);
		}

		// 返回剩余字符
		return suffix;
	}
	
	/**
	 * 解析列参数的精度和标度。精度必须是正整数，标度可以是0或者正整数
	 * @param attribute 属性
	 * @param input 输入语句
	 */
	private void splitAttributeMD(ColumnAttribute attribute, String input) {
		// 默认的精度和标度
		int m = 0, d = 0;

		// 解析
		Pattern pattern = Pattern.compile(CreateTableParser.COLUMN_MD1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			m = java.lang.Integer.parseInt(matcher.group(1));
			d = java.lang.Integer.parseInt(matcher.group(2));
		}
		if (!success) {
			pattern = Pattern.compile(CreateTableParser.COLUMN_MD2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				m = java.lang.Integer.parseInt(matcher.group(1));
			}
		}
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 判断数字溢出！
		if (m > 255 || d > 255) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 设置精度和标度
		attribute.getTag().setWidth(m, d);
	}

	/**
	 * 解析列和它的相关属性
	 * @param input
	 * @return
	 */
	private ColumnAttribute splitColumnAttribute(String input) {
		String title = null;;
		String family = null;
		String md = null; // 精度标度值
		String suffix = null;
		
		// 带精度/标度的类型
		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			title = matcher.group(1);
			family = matcher.group(2);
			md = matcher.group(3);
			suffix = matcher.group(4);
		}
		if (!success) {
			pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				title = matcher.group(1);
				family = matcher.group(2);
				suffix = matcher.group(3);
			}
		}
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
//		Pattern pattern = Pattern.compile(CreateTableParser.TABLE_COLUMN2);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
////			throw new SyntaxException("invalid syntax: %s", input);
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//
//		String title = matcher.group(1);
//		String family = matcher.group(2);
//		String suffix = matcher.group(3);

		ColumnAttribute attribute = ColumnAttributeCreator.create(family);
		if(attribute == null) {
//			throw new SyntaxException("illegal attribute: %s", family);
			throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, family);
		}
		attribute.setName(title);

		// 解析精度/标度
		if (md != null) {
			splitAttributeMD(attribute, md);
		}

		// 解析列类型之外的其它属性
		while(suffix.trim().length() > 0) {
			//1. 空值判断
			String result = splitNull(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			//2. 大小写开关判断
			result = splitCase(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			//3. 模糊检索判断
			result = splitLike(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			//4. 索引键判断
			result = splitKey(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			//5. 默认值判断
			result = splitDefault(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			//6. 打包判断
			result = splitPacking(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}
			// 7. 备注
			result = splitComment(attribute, suffix);
			if (!suffix.equals(result)) {
				suffix = result; continue;
			}

			// 全部判断结束，仍未找到匹配的结果，是错误
//			throw new SyntaxException("cannot resolve '%s'", suffix);
			
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
		}
		
		return attribute;
	}

	/**
	 * 区分列属性之间关系的标记符是逗号，以逗号加下一列名和列类型，可以实现分割列属性
	 * @param input
	 * @return
	 */
	private String[] splitTableColumns(String input) {
		int index = 0, seek = 0;
		ArrayList<String> array = new ArrayList<String>();
		while (seek < input.length()) {
			char w = input.charAt(seek++);
			if (w != ',') {
				continue;
			}
			String suffix = input.substring(seek - 1);
			if (!suffix.matches(CreateTableParser.COLUMN_CHECKTAG)){
				continue;
			}
			String prefix = input.substring(index, seek - 1);
			if (prefix.trim().length() > 0) {
				array.add(prefix);
				index = seek; // 跨过逗号
			}
		}

		if (index < input.length()) {
			String suffix = input.substring(index);
			if (suffix.trim().length() > 0) array.add(suffix);
		}

		String[] s = new String[array.size()];
		return array.toArray(s);
	}

	/**
	 * 根据表中参数定义，生成Table实例
	 * @param input 输入语句
	 * @return 返回CreateTable实例
	 */
	private CreateTable splitCreateTable(String input) {
		// 拆分"Create Table"参数
		Pattern pattern = Pattern.compile(CreateTableParser.CREATE_TABLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		String schemaName = matcher.group(2);
		String tableName = matcher.group(3);
		String content = matcher.group(4);
		String publish = matcher.group(5);

		// 解析集群参数
		Domain domain = null;
		if (publish.trim().length() > 0) {
			domain = splitPublishTo(publish);
		}

		// 数据库和表的长度判断，不能超过20字节
		if (!Space.isSchemaSize(schemaName.length())) {
//			throw new SyntaxException("database sizeout! >20!");
			throwableNo(FaultTip.SQL_DATABASE_NAME_SIZEOUT_X, schemaName);
		} else if (!Space.isTableSize(tableName.length())) {
//			throw new SyntaxException("table sizeout! >20!");
			throwableNo(FaultTip.SQL_TABLE_NAME_SIZEOUT_X, tableName);
		}

		// 建表
		Space space = new Space(schemaName, tableName);
		Table table = new Table(space);
		// 解析针对表的系统参数
		splitTablePrefix(table, prefix);
		// 将表的列属性集合分成多个独立单元
		String[] items = splitTableColumns(content);
		if (items == null || items.length == 0) {
//			throw new SyntaxException("invalid sql table: %s!", input);
			throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, space.toString());
		}

		// 分析各列的参数定义，列ID从1开始
		short columnId = 1;
		for (String item : items) {
			ColumnAttribute attribute = splitColumnAttribute(item);
			if (attribute == null) {
//				throw new SyntaxException("illegal column attribute: %s", item);
				throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, item);
			}
			if (table.find(attribute.getNameText()) != null) {
//				throw new SyntaxException("duplicate column: %s", attribute.getNameText());
				throwableNo(FaultTip.SQL_OVERLAP_COLUMN_X, attribute.getNameText());
			}
			attribute.setColumnId(columnId++);
			table.add(attribute);
		}

		// 检查主键和从键的数量(主键只能有一个，从键允许任意多个)
		int prime = 0, slave = 0;
		for (ColumnAttribute attribute : table.list()) {
			if (attribute.isPrimeKey()) prime++;
			else if (attribute.isSlaveKey()) slave++;
		}
		if (prime != 1) {
//			throw new SyntaxException("prime key size is %d", prime);
			throwableNo(FaultTip.SQL_PRIMEKEY_MEMBEROUT_X, space.toString());
		}
		if (prime == 0 && slave == 0) {
//			throw new SyntaxException("cannot set key");
			throwableNo(FaultTip.SQL_KEY_MISSING_X, space.toString());
		}

		return new CreateTable(table, domain);
	}

	/**
	 * 解析"CREATE TABLE..."语句，生成Table对象
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回数据表对象
	 */
	public CreateTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		// 解析表中所有参数
		CreateTable cmd = splitCreateTable(input);
		cmd.setPrimitive(input);

		Table table = cmd.getTable();
		Space space = table.getSpace();

		// 检查数据库
		if (online) {
			if (!hasSchema(space.getSchema())) {
				throwableNo(FaultTip.NOTFOUND_X, space.getSchema());
			}
		}
		// 检查数据库表
		if (online) {
			if (hasTable(space)) {
				throwableNo(FaultTip.EXISTED_X, space);
			}
		}
		// 如果是列存储，所有列都是索引
		if (table.isDSM()) {
			for (ColumnAttribute attribute : table.list()) {
				if (attribute.isNoneKey()) attribute.setKey(KeyType.SLAVE_KEY);
			}
		}

		// 重新定义可变长类型的值和索引
		for (ColumnAttribute attribute : table.list()) {
			// 必须是可变长，且是索引类型时，才允许继续			
			if (!(attribute.isVariable() && attribute.isKey())) continue;

			VariableAttribute variable = (VariableAttribute) attribute;
			// 取出数据，如果是字符类型，已经编码，使用前需要解码
			byte[] origin = variable.getValue();
			if (origin == null) continue;

			// 根据打包算法进行重新编码
			try {
				if (attribute.isWord()) {
					WordAttribute word = (WordAttribute) attribute;
					Charset charset = word.getCharset();
					// 字符串解码
					String text = charset.decode(origin, 0, origin.length);

					// 设置经过编码和打包(压缩，加密)的字符值勤
					byte[] value = VariableGenerator.encode(word, text);
					word.setValue(value);
					// 设置索引(可能为NULL)
					byte[] index = VariableGenerator.toIndex(table.isDSM(), word, text);
					word.setIndex(index);				
					// 设置模糊检索关键字集合
					java.util.List<RWord> set = VariableGenerator.createRWords(word, text);
					if (set != null) word.addRWords(set);
				} else {
					byte[] result = VariableGenerator.encode(variable, origin, 0, origin.length);
					variable.setValue(result);

					byte[] index = VariableGenerator.toIndex(table.isDSM(), variable, origin);
					variable.setIndex(index);
				}
			} catch (IOException e) {
				throw new SyntaxException(e);
			}
		}

		return cmd;
	}

}