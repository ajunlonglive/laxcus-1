/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.util.tip.*;

/**
 * <code>SQL SELECT</code>语句解析器<br><br>
 * 
 * 允许SELECT嵌套，和GROUP BY、ORDER BY语句。<br>
 * 
 * 语法格式:<br>
 * SELECT <br>
 * [TOP {digit}] [RANGE {digit, digit}] <br>
 * column_name, column_name AS alias, function_name, function AS alias, ... <br>
 * FROM SCHEMA.TABLE <br>
 * WHERE condition [AND|OR condition] <br>
 * GROUP BY column_name, column_name2, ... [HAVING aggregate_function] <br>
 * ORDER BY column_name [ASC|DESC]<br>
 *  ( SELECT ... FROM ... WHERE ... (SELECT ... FROM ... WHERE ))
 *  
 * @author scott.liang
 * @version 1.3 9/23/2013
 * @since laxcus 1.0
 */
public class SelectParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+)\\s*$";

	/** 过滤分隔逗号","*/
	private final static String SQL_FILTEREFIX = "^\\s*(?:\\,)\\s*([\\w\\W]+)\\s*$";

	/** SELECT格式(FROM左侧最小化匹配，WHERE右侧最大化匹配) **/
	private final static String SQL_SELECT_ALL = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s+((?i)(?:GROUP\\s+BY)\\s+[\\w\\W]+)\\s+((?i)(?:ORDER\\s+BY)\\s+[\\w\\W]+)\\s*$";
	private final static String SQL_SELECT_GROUPBY = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s+((?i)(?:GROUP\\s+BY)\\s+[\\w\\W]+)\\s*$";
	private final static String SQL_SELECT_ORDERBY = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s+((?i)(?:ORDER\\s+BY)\\s+[\\w\\W]+)\\s*$";
	private final static String SQL_SELECT_WHERE  = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s*$";

	/** 列数量 */
	private final static String SQL_SELECT_DISTINCT = "^\\s*(?i)DISTINCT\\s+(.*)$";
	private final static String SQL_SELECT_PREFIX_TOP = "^\\s*(?i)TOP\\s+(\\d+)(.*)$";
	private final static String SQL_SELECT_PREFIX_RANGE = "^\\s*(?i)RANGE\\s*\\(\\s*(\\d+)\\s*\\,\\s*(\\d+)\\s*\\)(.*)$";

	/** 显示列格式 **/
	private final static String SELECT_LIST_SUFFIX = "(\\s*\\,\\s*[\\w\\W]+|\\s*)$";
	private final static String LIST_ALL = "^\\s*(\\*)" + SELECT_LIST_SUFFIX;
	private final static String LIST_COLUMN_AS = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)AS\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)" + SELECT_LIST_SUFFIX; 
	private final static String LIST_COLUMN = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)" + SELECT_LIST_SUFFIX;
	private final static String LIST_FUNCTION_AS = "^([^\\)]+?\\))\\s+(?i)AS\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)" + SELECT_LIST_SUFFIX;
	private final static String LIST_FUNCTION = "^([^\\)]+?\\))" + SELECT_LIST_SUFFIX;

	/** GROUP BY语句，先解析GROUP BY，再解析HAVING */
	private final static String SQL_GROUPBY_HAVING = "^\\s*(?i)(?:GROUP\\s+BY)\\s+([\\w\\W]+?)\\s+(?i)(?:HAVING)\\s+([\\w\\W]+?)\\s*$";
	private final static String SQL_GROUPBY = "^\\s*(?i)(?:GROUP\\s+BY)\\s+([\\w\\W]+?)\\s*$";
	private final static String SQL_GROUPBY_ELEMENT = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)(\\s*\\,[\\w\\W]+|\\s*)$";

	/** ORDER BY语句 */
	private final static String SQL_ORDERBY = "^\\s*(?i)(?:ORDER\\s+BY)\\s+([\\w\\W]+?)\\s*$";
	private final static String JOIN_ORDERBY_ELEMENT = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)(?i)(\\s+ASC|\\s+DESC|\\s*)(\\s*\\,[\\w\\W]+|\\s*)$";

	/**
	 * 生成一个默认的SQL SELECT语句解析器
	 */
	public SelectParser() {
		super();
	}

	/**
	 * 检查传入的SQL语句是否匹配"SQL SELECT"语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SELECT", input);
		}
		Pattern pattern = Pattern.compile(SelectParser.SQL_SELECT_ALL);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_GROUPBY);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		if (!success) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_ORDERBY);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		if (!success) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_WHERE);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 解析"DISTINCT"关键字
	 * @param select
	 * @param input
	 * @return
	 */
	private String splitPrefixDistinct(Select select, String input) {
		Pattern pattern = Pattern.compile(SelectParser.SQL_SELECT_DISTINCT);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			return input;
		}

		select.setDistinct(true);
		return matcher.group(1);
	}

	/**
	 * 解析 TOP digit 
	 * @param table
	 * @param select
	 * @param input
	 * @return
	 */
	private String splitPrefixTop(Table table, Select select, String input) {
		Pattern pattern = Pattern.compile(SelectParser.SQL_SELECT_PREFIX_TOP);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			return input;
		}

		String top = matcher.group(1);
		String suffix = matcher.group(2);
		select.setRange(1, Integer.parseInt(top));
		return suffix;
	}

	/**
	 * 解析 RANGE begin, end
	 * @param table
	 * @param select
	 * @param input
	 * @return
	 */
	private String splitPrefixRange(Table table, Select select, String input) {
		Pattern pattern = Pattern.compile(SelectParser.SQL_SELECT_PREFIX_RANGE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return input;
		}

		String begin = matcher.group(1);
		String end = matcher.group(2);
		String suffix = matcher.group(3);

		int num1 = Integer.parseInt(begin);
		int num2 = Integer.parseInt(end);
		if (num1 > num2) {
			String s = String.format("%s - %s", begin, end);
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, s);
			// throw new SyntaxException("illegal range %s - %s", begin, end);
		}
		select.setRange(num1, num2);
		return suffix;
	}

	/**
	 * 解析显示成员，保存入ListSheet并且返回
	 * 
	 * @param table
	 * @param sql
	 * @return
	 */
	private ListSheet splitShowElement(Table table, String sql) {
		ListSheet sheet = new ListSheet();		
		// 函数标识号是在原有列标识之外建立
		short functionId = (short) (table.size() + 1);

		for (int index = 0; sql.trim().length() > 0; index++) {
			// 过滤前面的逗号分隔符(必须有)
			if (index > 0) {
				Pattern pattern = Pattern.compile(SelectParser.SQL_FILTEREFIX);
				Matcher matcher = pattern.matcher(sql);
				if (!matcher.matches()) {
					throwableNo(FaultTip.NOTRESOLVE_X, sql);
				}
				sql = matcher.group(1);
			}

			if (sql.trim().length() == 0) {
				// throw new SyntaxException("empty sql string!");
				throwable(FaultTip.INCORRECT_SYNTAX);
			}

			//1. 如果是"*"时，显示全部
			Pattern pattern = Pattern.compile(SelectParser.LIST_ALL);
			Matcher matcher = pattern.matcher(sql);
			boolean match = matcher.matches();
			if (match) {
				sql = matcher.group(2);
				// 保存全部显示列
				for (ColumnAttribute attribute : table.list()) {
					ColumnElement element = new ColumnElement(table.getSpace(), attribute.getTag());
					if (sheet.contains(table.getSpace(), element.getColumnId())) {
						throwableNo(FaultTip.SQL_OVERLAP_COLUMN_X, attribute.getNameText());
						// throw new SyntaxException("overlap column '%s'", attribute.getNameText());
					}
					sheet.add(element);
				}
				continue;
			}

			String input = null; 	//	列名/SQL函数原语
			String as = null;		//	别名
			//2. 检查列别名和标准列格式
			pattern = Pattern.compile(SelectParser.LIST_COLUMN_AS);
			matcher = pattern.matcher(sql);
			if(match = matcher.matches()) {
				input = matcher.group(1);		// 实际列名
				as = matcher.group(2);			// 列别名
				sql = matcher.group(3);			// 剩余SQL语句
			} else {
				pattern = Pattern.compile(SelectParser.LIST_COLUMN);
				matcher = pattern.matcher(sql);
				if(match = matcher.matches()) {
					input = matcher.group(1);	// 实际列名
					sql = matcher.group(2);		// 剩余SQL语句
				}
			}
			if (match) {
				ColumnAttribute attribute = table.find(input); // 列名
				if (attribute == null) {
					throwableNo(FaultTip.NOTFOUND_X, input);
				}
				// 如果有成员存在时是错误
				if (sheet.contains(table.getSpace(), attribute.getColumnId())) {
					//					throw new SyntaxException("overlap column '%s'", input);
					throwableNo(FaultTip.SQL_OVERLAP_COLUMN_X, input);
				}
				// 生成列成员
				ColumnElement element = new ColumnElement(table.getSpace(), attribute.getTag(), as);
				sheet.add(element);
				continue;
			}

			//3. SQL函数。首先匹配别名，再匹配非别名的情况
			pattern = Pattern.compile(SelectParser.LIST_FUNCTION_AS);
			matcher = pattern.matcher(sql);
			if (match = matcher.matches()) {
				input = matcher.group(1);
				as = matcher.group(2);
				sql = matcher.group(3); // 其它SQL语句
			} else {
				pattern = Pattern.compile(SelectParser.LIST_FUNCTION);
				matcher = pattern.matcher(sql);
				if (match = matcher.matches()) {
					input = matcher.group(1);
					sql = matcher.group(2); // 其它SQL语句
				}
			}
			if (match) {
				ColumnFunction function = ColumnFunctionCreator.create(table, input); // 生成函数
				if (function == null) {
					//					throw new SyntaxException("illegal function '%s'", input);
					throwableNo(FaultTip.SQL_ILLEGAL_FUNCTION_X, input);
				}
				// 生成函数成员
				FunctionElement element = new FunctionElement(table.getSpace(), functionId, function, as);
				sheet.add(element);
				functionId++; // 下一个显示列标识号
				continue;
			}

			// 全部解析不成功
			throwableNo(FaultTip.NOTRESOLVE_X, sql);
		}

		return sheet;
	}

	/**
	 * 分析 SELECT ... FROM 之间的数据
	 * 
	 * @param table
	 * @param select
	 * @param input
	 */
	private void splitSelectPrefix(Table table, Select select, String input) {
		// 解析TOP... | RANGE ... 
		do {
			String suffix = splitPrefixTop(table, select, input);
			if (!input.equals(suffix)) {
				input = suffix;
				continue;
			}
			suffix = splitPrefixRange(table, select, input);
			if (!input.equals(suffix)) {
				input = suffix;
				continue;
			}
			suffix = splitPrefixDistinct(select, input);
			if (!input.equals(suffix)) {
				input = suffix;
				continue;
			}
		} while (false);

		// 解析显示列集合
		ListSheet sheet = splitShowElement(table, input);
		select.setListSheet(sheet);
	}

	/**
	 * 解析"GROUP BY"，语法分为两部分:列分组和"HAVING"条件比较
	 * @param table
	 * @param input
	 */
	private GroupByAdapter splitGroupBy(Table table, String input) {
		Pattern pattern = Pattern.compile(SelectParser.SQL_GROUPBY_HAVING);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			pattern = Pattern.compile(SelectParser.SQL_GROUPBY);
			matcher = pattern.matcher(input);
		}
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		String align = matcher.group(1);
		String having = (matcher.groupCount() > 1 ? matcher.group(2) : null);
		GroupByAdapter adjuster = new GroupByAdapter(table.getSpace());

		// 1. 解析"GROUP BY" 分组列名
		for (int index = 0; align.trim().length() > 0; index++) {
			if (index > 0) {
				pattern = Pattern.compile(SelectParser.SQL_FILTEREFIX);
				matcher = pattern.matcher(align);
				if (!matcher.matches()) {
					throwableNo(FaultTip.NOTRESOLVE_X, align);
				}
				align = matcher.group(1);
			}

			pattern = Pattern.compile(SelectParser.SQL_GROUPBY_ELEMENT);
			matcher = pattern.matcher(align);
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, align);
			}
			String name = matcher.group(1);
			align = matcher.group(2);
			// 查找对应的列属性
			ColumnAttribute attribute = table.find(name);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, name);
			}
			adjuster.addColumnId(attribute.getColumnId());
		}

		// 2. 解析"HAVING"后续函数
		if (having != null) {
			HavingParser parser = new HavingParser();
			Situation situa = parser.split(table, having);
			adjuster.setSituation(situa);
		}

		return adjuster;
	}

	/**
	 * 解析"ORDER BY"语句
	 * @param table 数据表名
	 * @param input 输入语句
	 */
	private OrderByAdapter splitOrderBy(Table table, String input) {
		Pattern pattern = Pattern.compile(SelectParser.SQL_ORDERBY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		input = matcher.group(1);
		OrderByAdapter adjuster = null;

		for(int index = 0; input.trim().length() > 0; index++) {
			//1. 过滤","符号
			if (index > 0) {
				pattern = Pattern.compile(SelectParser.SQL_FILTEREFIX);
				matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					throwableNo(FaultTip.NOTRESOLVE_X, input);
				}
				input = matcher.group(1);
			}

			// 解析"ORDER BY"语句
			pattern = Pattern.compile(SelectParser.JOIN_ORDERBY_ELEMENT);
			matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, input);
			}
			String name = matcher.group(1);
			String sort = matcher.group(2);
			input = matcher.group(3);

			// 查找列属性
			ColumnAttribute attribute = table.find(name);
			if (attribute == null) {
				throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, name);
			}			
			// 默认是升序排列
			byte align = OrderByAdapter.ASC;
			if (sort.matches("^\\s*(?i)(DESC)\\s*$")) {
				align = OrderByAdapter.DESC;
			}

			// 建立"ORDER BY"实例
			if (adjuster == null) {
				adjuster = new OrderByAdapter(table.getSpace(), attribute.getColumnId(), align);
			} else {
				adjuster.setLast(new OrderByAdapter(table.getSpace(), attribute.getColumnId(), align));
			}
		}

		return adjuster;
	}

	/**
	 * 解析SQL SELECT语句，生成SELECT实例
	 * @param input SQL SELECT语句
	 * @param online 在线模式
	 * @return 返回Select命令
	 */
	public Select split(final String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Space space = null;
		String prefix = null;
		String where = null;
		String groupby = null;
		String orderby = null;

		Pattern pattern = Pattern.compile(SelectParser.SQL_SELECT_ALL);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			prefix = matcher.group(1);
			space = new Space(matcher.group(2), matcher.group(3));
			where = matcher.group(4);
			groupby = matcher.group(5);
			orderby = matcher.group(6);
		}
		if (!match) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_GROUPBY);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				prefix = matcher.group(1);
				space = new Space(matcher.group(2), matcher.group(3));
				where = matcher.group(4);
				groupby = matcher.group(5);
			}
		}
		if (!match) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_ORDERBY);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				prefix = matcher.group(1);
				space = new Space(matcher.group(2), matcher.group(3));
				where = matcher.group(4);
				orderby = matcher.group(5);
			}
		}
		if (!match) {
			pattern = Pattern.compile(SelectParser.SQL_SELECT_WHERE);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				prefix = matcher.group(1);
				space = new Space(matcher.group(2), matcher.group(3));
				where = matcher.group(4);
			}
		}
		// 全部条件不能匹配，"SELECT"语法错误
		if (!match) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

//		System.out.println(input);
//		System.out.printf("SPACE:[%s] \n", space);
//		System.out.printf("PREFIX:[%s] \n", prefix);
//		System.out.printf("WHERE:[%s] \n", where);
//		System.out.printf("GROUP BY:[%s] \n", groupby);
//		System.out.printf("ORDER BY:[%s] \n", orderby);

		// 查找匹配的数据表
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}

		// 如果是在线模式，判断用户有没有操作这个表的权限
		if (online) {
			boolean allow = chooser.canSelect(space);
			if (!allow) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		Select select = new Select(space);
		// 解析 "select * from" 之间的显示成员和其它
		splitSelectPrefix(table, select, prefix);

		// 解析 WHERE语句
		WhereParser whereParser = new WhereParser();
		Where condi = whereParser.split(table, where, online);
		if (condi == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, where);
		}
		select.setWhere(condi);

		// 解析 "GROUP BY"语句
		if (groupby != null) {
			GroupByAdapter result = splitGroupBy(table, groupby);

			/*
			 * 按照"GROUP BY"规则，显示成员，如果属于"列"，必须在GROUP BY后面出现。
			 * 如果是函数，不操作列的函数被允许，操作列的必须是聚合函数。
			 * 如果一个列有多种显示，其中必须有聚合函数，或者出现在GROUP BY后面
			 */
			ListSheet sheet = select.getListSheet();
			for (ListElement element : sheet.list()) {
				short columnId = element.getColumnId();
				if (element.isColumn()) {
					if (result.inside(columnId)) {
						continue; // 列成员必须在GROUP BY里面
					}
					//	throwable("illegal column: %s", table.find(columnId).getNameText());
					//	throwable(FaultTip.SQL_ILLEGAL_COLUMN_X, table.find(columnId).getNameText());

					throwableNo(FaultTip.SQL_GROUPBY_ELEMENT_MISSING_X, table.find(columnId).getNameText());
				} else if (element.isFunction()) {
					/*
					 * <1> 没有设置列的函数
					 * <2> 列标识号已经在"GROUP BY"中定义
					 * <3> 一列分别用被多个函数使用，其中一个必须是聚合函数
					 * 以上3种情况，允许通过
					 */
					if (columnId == 0 || result.inside(columnId)
							|| sheet.findAggregateFunction(element.getSpace(), columnId) != null) {
						continue;
					}
					// 出错
					ColumnFunction function = ((FunctionElement) element).getFunction();
					//	throwable("illegal function: '%s'", function.getPrimitive());
					throwableNo(FaultTip.SQL_GROUPBY_ELEMENT_MISSING_X, function.getPrimitive());
				}
				// 错误
				//	throwable("failed: '%s'", groupby);
				throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, groupby);
			}

			// 设置GROUP BY
			select.setGroup(result);
		}
		// 没有情况下，检查函数必须是非聚合函数
		else {
			// 如果没有"GROUP BY"语句，要求显示列中的函数成员必须是"非聚合函数"，否则即是错误
			for (ListElement element : select.getListSheet().list()) {
				if (!element.isFunction()) continue;

				ColumnFunction function = ((FunctionElement) element).getFunction();
				if (function instanceof ColumnAggregateFunction) {
					//					throwable("illegal function '%s'", function.getPrimitive());
					throwableNo(FaultTip.SQL_ILLEGAL_FUNCTION_X, function.getPrimitive());
				}
			}
		}

		// 解析 "ORDER BY"语句
		if (orderby != null) {
			OrderByAdapter result = splitOrderBy(table, orderby);

			OrderByAdapter that = result;
			ListSheet sheet = select.getListSheet();
			do {
				// 操作"ORDER BY"的列必须是"列成员"，不能是"函数成员"，否则即错误
				if(!sheet.contains(that.getSpace(), that.getColumnId())) {
					//					throwable("failed: '%s'", orderby);
					throwableNo(FaultTip.SQL_ORDERBY_INCORRECT_SYNTAX_X, orderby);
				}
				that = that.getNext();
			} while (that != null);

			// 保存ORDER BY
			select.setOrder(result);
		}

		// 检查WHERE中的检索列，它们必须是键(主键/从键)，非键列不允许检索
		do {
			// 主检索条件
			WhereIndex index = condi.getIndex(); 
			short columnId = index.getColumnId();

			if (columnId != 0) { // 防止嵌套检索的 WHERE EXISTS|NOT EXISTS现象，这种情况没有列标识
				columnId = buildNormalId(columnId);
				ColumnAttribute attribute = table.find(columnId);
				if (!attribute.isKey()) {
					//					throw new SyntaxException("invalid key: %s", attribute.getNameText()); 
					throwableNo(FaultTip.SQL_ILLEGAL_KEY_X, attribute.getNameText()); 
				}
			}
			// 同级关联检索条件
			for(Where partner : condi.getPartners()) {
				index = partner.getIndex();
				columnId = buildNormalId(index.getColumnId());
				ColumnAttribute attribute = table.find(columnId);
				if (!attribute.isKey()) {
					//					throw new SyntaxException("invalid key: %s", attribute.getNameText()); // partner.getName());
					throwableNo(FaultTip.SQL_ILLEGAL_KEY_X, attribute.getNameText()); // partner.getName());
				}
			}
			// 子查询条件
			condi = condi.next();
		} while (condi != null);

		// 保存本次SELECT原语
		select.setPrimitive(input);
		
//		// 检索...
//		check(select);

		return select;
	}
	
//	private void checkWhere(Where where) {
//		if (where == null) {
//			return;
//		}
//		System.out.printf("%d - %s\n", where.getColumnId(), 
//				where.getIndex().getClass().getName());
//		for (Where sub : where.getPartners()) {
//			checkWhere(sub);
//		}
//		checkWhere(where.next());
//	}
//
//	private void check(Select select) {
//		Where where = select.getWhere();
//		System.out.printf("原语：%s\n", select.getPrimitive());
//		checkWhere(where);
//	}

//	public static void main(String[] args) {
//		String input = "SELECT * FROM MEDIA.MUSIC WHERE ID NOT IN (select * from media.video where word<>'Unix') Group by WORD ORDER BY WORD DESC ";
//		SelectParser parser = new SelectParser();
//		Select cmd = parser.split(input, false);
//		System.out.println("okay!");
//	}

}