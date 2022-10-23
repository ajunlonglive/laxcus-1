/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * <code>SQL SELECT JOIN</code>语句解析器。<br><br>
 * 
 * 支持的语法格式：<br>
 * 
 * SELECT <br>
 * schema.table.column_name, alias2.column_name, ... <br>
 * FROM schema.table [AS alias1] <br>
 * JOIN|INNER JOIN|LEFT JOIN|RIGHT JOIN|FULL JOIN schema.table [AS alias2] <br>
 * ON schema.table.column_name = alias.column_name <br>
 * ORDER BY schema.table.column_name DESC|ASC, [alias1.column DESC|ASC] <br>
 * 
 * 或者: <br>
 * SELECT <br>
 * schema1.table1.column, alias2.column_name, ... <br>
 * FROM schema1.table1 [alias], schema2.table2 [alias2] <br>
 * WHERE alias1.column_name = alias2.column_name <br>
 * ORDER BY schema.table.column_name DESC|ASC, [alias1.column DESC|ASC] <br>
 * 
 * @author scott.liang
 * @version 1.1 7/6/2014
 * @since laxcus 1.0
 */
public final class JoinParser extends SyntaxParser {

	/**
	 * JOIN连接表，包括左表和右表
	 */
	final class JoinSpace implements Comparable<JoinSpace> {
		/** 表名 **/
		Space space;
		/** 表的别名 **/
		Naming alias;

		/**
		 * @param s1
		 * @param s2
		 */
		public JoinSpace(Space s1, String s2) {
			this.space = (Space) s1.clone();
			if (s2 != null) {
				this.alias = new Naming(s2);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object that) {
			if (that == null || that.getClass() != JoinSpace.class) {
				return false;
			} else if (that == this) {
				return true;
			}
			return this.compareTo((JoinSpace) that) == 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.space.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(JoinSpace that) {
			return space.compareTo(that.space) ;
		}
	}

	/**
	 * JOIN表和别名的集合
	 */
	class JoinSet {
		Map<Naming, Space> relates = new TreeMap<Naming, Space>();

		List<Space> spaces = new ArrayList<Space>();

		public JoinSet() {
			super();
		}

		public void add(Naming alias, Space space) {
			if (alias != null) {
				relates.put(alias, space);
			}
			spaces.add(space);
		}

		public Space get(Naming alias) {
			return relates.get(alias);
		}

		public boolean inside(Space space) {
			return spaces.contains(space);
		}

		public List<Space> list() {
			return this.spaces;
		}
	}

	/** 过滤分隔逗号","*/
	private final static String SQL_FILTEREFIX = "^\\s*(?:\\,)\\s*([\\w\\W]+)\\s*$";

	/** JOIN连接类型 **/
	private final static String INNER_JOIN = "(?i)JOIN|INNER\\s+JOIN";
	private final static String LEFT_JOIN = "(?i)LEFT\\s+JOIN|LEFT\\s+OUTER\\s+JOIN";
	private final static String RIGHT_JOIN = "(?i)LEFT\\s+JOIN|RIGHT\\s+OUTER\\s+JOIN";
	private final static String FULL_JOIN = "(?i)FULL\\s+JOIN|FULL\\s+OUTER\\s+JOIN";

	/** JOIN连接类型集合 **/
	private final static String JOIN_STYLE = String.format("(%s|%s|%s|%s)", INNER_JOIN, LEFT_JOIN, RIGHT_JOIN, FULL_JOIN);

	/** JOIN 表名格式 **/
	private final static String JOIN_SPACE_ALIAS = "^\\s*(\\w+)\\.(\\w+)(?i)(?:\\s+AS\\s+|\\s+)(\\w+)\\s*$";
	private final static String JOIN_SPACE_PURE = "^\\s*(\\w+)\\.(\\w+)\\s*$";

	/** JOIN列格式，分为全名和别名表示两种 **/
	private final static String JOIN_COLUMN_REAL = "^\\s*(\\w+)\\.(\\w+)\\.(\\w+)\\s*$";
	private final static String JOIN_COLUMN_ALIAS = "^\\s*(\\w+)\\.(\\w+)\\s*$";

	/** JOIN ON语句语法 **/
	private final static String JOIN_ON = "^\\s*([\\w\\.]+)\\s*(=|<>|!=|>|>=|<|<=)\\s*([\\w\\.]+)\\s*$";

	/** JOIN "ORDER BY"语句 **/
	private final static String JOIN_ORDERBY = "^\\s*(?i)(?:ORDER\\s+BY)\\s+([\\w\\W]+)\\s*$";
	private final static String JOIN_ORDERBY_ELEMENT = "^\\s*([\\w\\.]+)(?i)(\\s+ASC|\\s+DESC|\\s*)(\\s*\\,[\\w\\W]+|\\s*)$";

	/** JOIN显示单元格式 **/
	private final static String JOIN_LIST_SUFFIX = "(\\s*\\,[\\w\\W]+|\\s*)$";
	private final static String JOIN_LIST_SPACE_ALL = "^\\s*(\\w+)\\.(\\w+)\\.(?:\\*)" + JOIN_LIST_SUFFIX;
	private final static String JOIN_LIST_ALIAS_ALL = "^\\s*(\\w+)\\.(?:\\*)" + JOIN_LIST_SUFFIX;
	private final static String JOIN_LIST_FUNCTION_AS = "^\\s*(\\w+\\s*\\(\\s*.*?\\s*\\))\\s+(?i)AS\\s+(\\w+)" + JOIN_LIST_SUFFIX;
	private final static String JOIN_LIST_FUNCTION_REAL = "^\\s*(\\w+\\s*\\(\\s*.*?\\s*\\))" + JOIN_LIST_SUFFIX;
	private final static String JOIN_LIST_ELEMENT_AS = "^\\s*(\\w+\\.\\w+\\.\\w+|\\w+\\.\\w+)\\s+(?i)AS\\s+(\\w+)" + JOIN_LIST_SUFFIX;
	private final static String JOIN_LIST_ELEMENT_REAL = "^\\s*(\\w+\\.\\w+\\.\\w+|\\w+\\.\\w+)" + JOIN_LIST_SUFFIX;

	/** 在JOIN使用的SQL函数格式，使用实际表名+列名的情况: function ( schema.table.column ... ) */
	private final static String JOIN_FUNCTION_FULLREAL =  "^\\s*(\\w+\\s*\\(.*?)(\\w+)\\.(\\w+)\\.(\\w+)(.*\\))\\s*$";
	/** 在JOIN中使用的SQL函数，使用表别名+列名的情况 : function ( alias.column ...)**/
	private final static String JOIN_FUNCTION_ALIAS = "^\\s*(\\w+\\s*\\(.*?)(\\w+)\\.(\\w+)(.*\\))\\s*$";

	/** 标准JOIN检索语句，使用ON子句，区别与WHERE子句 **/
	private final static String SQL_JOIN = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+?)\\s+"
		+ JOIN_STYLE + "\\s+([\\w\\W]+?)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)(\\s*|\\s+(?i)(?:ORDER\\s+BY)\\s+[\\w\\W]+)\\s*$";
	/** 使用WHERE子句的JOIN语句 **/
	private final static String SQL_SELECT = "^\\s*(?i)(?:SELECT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\s\\.]+?)\\,([\\w\\s\\.]+?)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+?)(\\s*|\\s+(?i)(?:ORDER\\s+BY)\\s+[\\w\\W]+?)$";

	// select schema.table1.word from schema.table1 LEFT join schema.table2 on table1.id=table2.id order by table2.id

	/**
	 * 构造一个默认的JOIN解析器
	 */
	public JoinParser() {
		super();
	}

	/**
	 * 检查参数是否匹配
	 * @param sql
	 * @return
	 */
	public boolean matches(String sql) {
		Pattern pattern = Pattern.compile(JoinParser.SQL_JOIN);
		Matcher matcher = pattern.matcher(sql);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(JoinParser.SQL_SELECT);
			matcher = pattern.matcher(sql);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析并且保存一个表的全部列属性
	 * @param sql
	 * @param jset
	 * @param chooser
	 * @return
	 */
	private void splitShowSpace(Space space, ResourceChooser chooser, ListSheet sheet) {
		// 如果没有这个表配置
		Table table = chooser.findTable(space);
		if (table == null) {
			// this.throwable("cannot find %s", space);
			throwableNo(FaultTip.NOTFOUND_X, space);
		}

		// 按照顺序逐一保存
		for (ColumnAttribute attribute : table.list()) {
			ColumnElement element = new ColumnElement(space, attribute.getTag());
			element.setAlias(String.format("%s.%s", space, attribute.getNameText()));
			if (sheet.contains(space, element.getIdentity())) {
//				this.throwable("overlap %s %s", space, attribute.getName());
			}
			sheet.add(element);
		}
	}

	/**
	 * 解析并且保存一个函数列。在JOIN中的函数必须指定所属的表
	 * @param columnFunction
	 * @param as
	 * @param jset
	 * @param chooser
	 * @param sheet
	 */
	private void splitShowFunction(String sql, String as, short functionId, JoinSet jset, ListSheet sheet) {
		//1. 从函数中取出列名
		Space space = null;
		Pattern pattern = Pattern.compile(JoinParser.JOIN_FUNCTION_FULLREAL);
		Matcher matcher = pattern.matcher(sql);

		// 第一种是函数使用表名全称的情况
		boolean match = matcher.matches();
		if (match) {
			String prefix = matcher.group(1);
			space = new Space(matcher.group(2), matcher.group(3));
			String column = matcher.group(4);
			String suffix = matcher.group(5);
			// 转成标准SQL函数格式
			sql = prefix + column + suffix;
		}
		// 第二种是函数中使用表别名的情况
		if (!match) {
			pattern = Pattern.compile(JOIN_FUNCTION_ALIAS);
			matcher = pattern.matcher(sql);
			match = matcher.matches();
			if (match) {
				String prefix = matcher.group(1);
				String alias = matcher.group(2);
				String column = matcher.group(3);
				String suffix = matcher.group(4);
				// 取出表名
				space = jset.get(new Naming(alias));
				if (space == null) {
					this.throwableFormat("illegal %s", alias);
				}
				// 转成标准SQL函数格式
				sql = prefix + column + suffix;
			}
		}

		// JOIN中必须有表名，即使名默认无需表名的情况，如 Now(schema.table)
		if (space == null) {
			this.throwableFormat("illegal %s", sql);
		}

		// 检查表名存在
		if (!jset.inside(space)) {
			this.throwableFormat("illegal %s", space);
		}
		// 检查是否有这个表
		 ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			this.throwableFormat("cannot find %s", space);
		}

		// 生成函数
		ColumnFunction function = ColumnFunctionCreator.create(table, sql);
		if (function == null) {
			throw new SyntaxException("illegal function:%s", sql);
		}

		// 生成函数成员
		FunctionElement element = new FunctionElement(space, functionId, function, as);
		sheet.add(element);
	}

	/**
	 * 分析、检查、生成一个显示列成员，并且保存
	 * @param space
	 * @param column
	 * @param as
	 * @param sheet
	 */
	private void splitShowColumn(Space space, String column, String as, ListSheet sheet) {
		// 查找表
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			this.throwableFormat("cannot find '%s'", space);
		}
		// 查找列属性
		ColumnAttribute attribute = table.find(column);
		if(attribute == null) {
			this.throwableFormat("cannot find %s %s", space, column);
		}

		ColumnElement element = new ColumnElement(space, attribute.getTag());
		// 不允许重复
		if (sheet.contains(space, element.getIdentity())) {
			this.throwableFormat("overlap %s %s", space, column);
		}
		// 写入别名
		if (as != null) {
			element.setAlias(as);
		}
		// 保存
		sheet.add(element);
	}

	/**
	 * 解析显示成员
	 * @param sql
	 * @param aliases
	 * @param chooser
	 * @return
	 */
	private ListSheet splitListSheet(String sql, JoinSet jset) {
		// 检查并且统计函数成员开始数
		int functionId = 0;
		for (Space space : jset.list()) {
			ResourceChooser chooser = SyntaxParser.getResourceChooser();
			Table table = chooser.findTable(space);
			if (table == null) {
				this.throwableFormat("cannot find %s", space);
			}
			functionId += table.size();
		}
		functionId++;

		// 显示成员集合
		ListSheet sheet = new ListSheet();

		// 显示全部成员
		if(sql.matches("^\\s*(?:\\*)\\s*$")) {
			for(Space space : jset.list()) {
				ResourceChooser chooser = SyntaxParser.getResourceChooser();
				Table table = chooser.findTable(space);
				if (table == null) {
					this.throwableFormat("cannot find '%s'", space);
				}
				for (ColumnAttribute attribute : table.list()) {
					ColumnElement element = new ColumnElement(space, attribute.getTag());
					element.setAlias(String.format("%s.%s", space, attribute.getNameText()));
					sheet.add(element);
				}
			}
			return sheet;
		}

		// 分段解析，显示每一列成员
		for(int index = 0; sql.trim().length() > 0; index++) {
			//1. 过滤前面的逗号
			if (index > 0) {
				Pattern pattern = Pattern.compile(JoinParser.SQL_FILTEREFIX);
				Matcher matcher = pattern.matcher(sql);
				if (!matcher.matches()) {
					this.throwableFormat("illegal syntax:%s", sql);
				}
				sql = matcher.group(1);
			}

			//2. 显示全部表成员: "schema.table.*"
			Pattern pattern = Pattern.compile(JoinParser.JOIN_LIST_SPACE_ALL);
			Matcher matcher = pattern.matcher(sql);
			if (matcher.matches()) {
				Space space = new Space(matcher.group(1), matcher.group(2));
				sql = matcher.group(3);
				// 表名不存在
				if(!jset.inside(space)) {
					this.throwableFormat("illegal %s", space);
				}
				// 解析并且保存某个表的全部列属性
				ResourceChooser chooser = SyntaxParser.getResourceChooser();
				this.splitShowSpace(space, chooser, sheet);
				continue;
			}

			//3. 显示成员别名成员: "alias.*"
			pattern = Pattern.compile(JoinParser.JOIN_LIST_ALIAS_ALL);
			matcher = pattern.matcher(sql);
			if (matcher.matches()) {
				Naming naming = new Naming(matcher.group(1));
				sql = matcher.group(2);
				Space space = jset.get(naming);
				if (space == null) {
					this.throwableFormat("illegal %s", naming);
				}
				// 解析并且保存某个表的全部列属性
				ResourceChooser chooser = SyntaxParser.getResourceChooser();
				this.splitShowSpace(space, chooser, sheet);
				continue;
			}

			//4. 显示函数成员带别名的情况: function(schema.table.column|alias.column) as function_alias
			pattern = Pattern.compile(JoinParser.JOIN_LIST_FUNCTION_AS);
			matcher = pattern.matcher(sql);
			if(matcher.matches()) {
				String function = matcher.group(1);
				String as = matcher.group(2);
				sql = matcher.group(3);
				this.splitShowFunction(function, as, (short) functionId, jset, sheet);
				functionId++; // 函数成员标识号自增1
				continue;
			}
			//5. 显示函数成员不带别名的情况
			pattern = Pattern.compile(JoinParser.JOIN_LIST_FUNCTION_REAL);
			matcher = pattern.matcher(sql);
			if(matcher.matches()) {
				String function = matcher.group(1);
				sql = matcher.group(2);
				this.splitShowFunction(function, null, (short) functionId, jset, sheet);
				functionId++; // 函数成员标识号自增1
				continue;
			}

			//6.分析列全名/表别名列带别名的情况: schema.table.column|alias.column as column_alias [,....]
			pattern = Pattern.compile(JoinParser.JOIN_LIST_ELEMENT_AS);
			matcher = pattern.matcher(sql);
			if (matcher.matches()) {
				String s1 = matcher.group(1);
				String as = matcher.group(2);
				sql = matcher.group(3);
				Label lable = this.splitLabel(s1, jset);
				this.splitShowColumn(lable.getSpace(), lable.getName(), as, sheet);
				continue;
			}

			//7.分析列全名/表别名列不带别名的情况: schema.table.column|alias.column [,...]
			pattern = Pattern.compile(JoinParser.JOIN_LIST_ELEMENT_REAL);
			matcher = pattern.matcher(sql);
			if(matcher.matches()) {
				String s1 = matcher.group(1);
				sql = matcher.group(2);
				Label lable = this.splitLabel(s1, jset);
				this.splitShowColumn(lable.getSpace(), lable.getName(), null, sheet);
				continue;
			}

			this.throwableFormat("cannot resolve %s", sql);
		}

		return sheet;
	}

	/**
	 * 解析"ORDER BY"语句
	 * @param sql
	 * @param jset
	 * @param chooser
	 * @return
	 */
	private OrderByAdapter splitOrderBy(String sql, JoinSet jset) {
		Pattern pattern = Pattern.compile(JoinParser.JOIN_ORDERBY);
		Matcher matcher = pattern.matcher(sql);
		if (!matcher.matches()) {
			throw new SyntaxException("invalid syntax:%s", sql);
		}

		sql = matcher.group(1);
		OrderByAdapter instance = null;

		for(int index = 0; sql.trim().length() > 0; index++) {
			//1. 过滤","符号
			if (index > 0) {
				pattern = Pattern.compile(JoinParser.SQL_FILTEREFIX);
				matcher = pattern.matcher(sql);
				if (!matcher.matches()) {
					throw new SyntaxException("cannot resolve:%s", sql);
				}
				sql = matcher.group(1);
			}

			// 解析ORDER BY指定的列名称，分为全名称和别名两种
			pattern = Pattern.compile(JoinParser.JOIN_ORDERBY_ELEMENT);
			matcher = pattern.matcher(sql);
			if (!matcher.matches()) {
				throw new SyntaxException("cannot resolve:%s", sql);
			}

			String name = matcher.group(1);
			String sort = matcher.group(2);
			sql = matcher.group(3);

			// 解析列成员
			Label label = splitLabel(name, jset);
			// 排列顺序，默认是升序(ASC)
			byte align = OrderByAdapter.ASC;
			if (sort.matches("^\\s*(?i)(DESC)\\s*$")) {
				align = OrderByAdapter.DESC;
			}

			// 定义参数
			if (instance == null) {
				instance = new OrderByAdapter(label.getSpace(), label.getColumnId(), align);
			} else {
				instance.setLast(new OrderByAdapter(label.getSpace(), label.getColumnId(), align));
			}
		}

		return instance;
	}

	/**
	 * 解析列成员，分别全名称和别名两种，即:"schema.table.column_name", "alias.column_name"
	 * @param sql
	 * @param jset
	 * @param chooser
	 * @return
	 */
	private Label splitLabel(String sql, JoinSet jset) {
		Space space = null;
		String name = null;
		//1. JOIN列名标准全称
		Pattern pattern = Pattern.compile(JoinParser.JOIN_COLUMN_REAL);
		Matcher matcher = pattern.matcher(sql);
		boolean match = matcher.matches();
		if(match) {
			space = new Space(matcher.group(1), matcher.group(2));
			name = matcher.group(3);
		}
		//2. JOIN列名使用表别名
		if (!match) {
			pattern = Pattern.compile(JoinParser.JOIN_COLUMN_ALIAS);
			matcher = pattern.matcher(sql);
			if(match = matcher.matches()) {
				String alias = matcher.group(1);
				name = matcher.group(2);
				space = jset.get(new Naming(alias));
				if(space == null) {
					this.throwableFormat("illegal alias %s", alias);
				}
			}
		}
		if (!match) {
			this.throwableFormat("cannot resolve %s", sql);
		}

		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		// 如果没有找到表
		if (table == null) {
			this.throwableFormat("cannot find table: %s", space);
		}
		ColumnAttribute attribute = table.find(name);
		// 如果没有找到列属性定义时
		if (attribute == null) {
			this.throwableFormat("cannot find %s %s", space, name);
		}

		Label label = new Label(space, attribute.getTag());
		if (name != null) {
			label.getTag().setName(name);
		}
		return label;
	}

	/**
	 * 解析"ON schema1.table1.id = schema1.table2.id"语句
	 * @param sql
	 * @param jset
	 * @param chooser
	 * @return
	 */
	private OnIndex splitOn(String sql, JoinSet jset){
		Pattern pattern = Pattern.compile(JoinParser.JOIN_ON);
		Matcher matcher = pattern.matcher(sql);
		if (!matcher.matches()) {
			this.throwableFormat("cannot resolve %s", sql);
		}

		// 比较符
		byte comparison = CompareOperator.translate(matcher.group(2));
//		Gradation.translateCompare(matcher.group(2));
		// 左侧列
		Label left = this.splitLabel(matcher.group(1), jset);
		// 右侧列
		Label right = this.splitLabel(matcher.group(3), jset);

		if (left.getFamily() != right.getFamily()) {
			this.throwableFormat("cannot match %s %s",
					ColumnType.translate(left.getFamily()),
					ColumnType.translate(right.getFamily()));
		}

		// 返回比较索引
		return new OnIndex(new Dock(left.getSpace(), left.getColumnId()),
				comparison, new Dock(right.getSpace(), right.getColumnId()));
	}

	//	private OnIndex splitOn1(String sql, JoinSet jset, SQLChooser chooser) {
	//		Pattern pattern = Pattern.compile(JoinParser.JOIN_ON_TYPE1);
	//		Matcher matcher = pattern.matcher(sql);
	//		boolean match = matcher.matches();
	//		if (!match) {
	//			pattern = Pattern.compile(JoinParser.JOIN_ON_TYPE2);
	//			matcher = pattern.matcher(sql);
	//			match = matcher.matches();
	//		}
	//		if (!match) {
	//			this.throwable("cannot resolve %s", sql);
	//		}
	//		
	//		Space leftSpace = null;
	//		Space rightSpace = null;
	//		String leftName = null;
	//		String rightName = null;
	//		// 比较运算符
	//		byte comparison = 0;
	//
	//		if (matcher.groupCount() == 7) {
	//			leftSpace = new Space(matcher.group(1), matcher.group(2));
	//			leftName = matcher.group(3);
	//			comparison = Gradation.translateCompare(matcher.group(4));
	//			rightSpace = new Space(matcher.group(5), matcher.group(6));
	//			rightName = matcher.group(7);
	//		} else if (matcher.groupCount() == 5) {
	//			String leftAlias = matcher.group(1);
	//			leftName = matcher.group(2);
	//			comparison = Gradation.translateCompare(matcher.group(3));
	//			String rightAlias = matcher.group(4);
	//			rightName = matcher.group(5);
	//
	//			leftSpace = jset.get(new Naming(leftAlias));
	//			rightSpace = jset.get(new Naming(rightAlias));
	//			if (leftSpace == null) {
	//				this.throwable("illegal %s", leftAlias);
	//			} else if (rightSpace == null) {
	//				this.throwable("illegal %s", rightAlias);
	//			}
	//		}
	//
	//		Table leftTable = chooser.findTable(leftSpace);
	//		Table rightTable = chooser.findTable(rightSpace);
	//		// 如果没有找到表
	//		if (leftTable == null) {
	//			this.throwable("cannot find table: %s", leftSpace);
	//		} else if (rightTable == null) {
	//			this.throwable("cannot find table: %s", rightSpace);
	//		}
	//
	//		ColumnAttribute left = leftTable.find(leftName);
	//		ColumnAttribute right = rightTable.find(rightName);
	//		// 如果没有找到列属性定义时
	//		if(left == null) {
	//			this.throwable("cannot find %s %s", leftSpace, leftName);
	//		} else if(right == null) {
	//			this.throwable("cannot find %s %s", rightSpace, rightName);
	//		}
	//
	//		// 数据类型必须匹配
	//		if (left.getFamily() != right.getFamily()) {
	//			this.throwable("cannot match %s %s",
	//					SQLKinds.translateColumnFamily(left.getFamily()),
	//					SQLKinds.translateColumnFamily(right.getFamily()));
	//		}
	//
	//		// 返回比较索引
	//		return new OnIndex(new Dock(leftSpace, left.getColumnId()),
	//				comparison, new Dock(rightSpace, right.getColumnId()));
	//	}

	/**
	 * 解析表名，包括表的别名
	 * @param sql
	 * @param chooser
	 * @return
	 */
	private JoinSpace splitSpaces(String sql) {
		// 1. 首先匹配表名带别名的格式
		Pattern pattern = Pattern.compile(JoinParser.JOIN_SPACE_ALIAS);
		Matcher matcher = pattern.matcher(sql);
		boolean match = matcher.matches();
		//2. 条件不成功，匹配表名无别名的格式
		if (!match) {
			pattern = Pattern.compile(JoinParser.JOIN_SPACE_PURE);
			matcher = pattern.matcher(sql);
			match = matcher.matches();
		}
		if (!match) {
			this.throwableFormat("cannot resolve %s", sql);
		}

		String schema = matcher.group(1);
		String table = matcher.group(2);
		String alias = null;
		if (matcher.groupCount() == 3) {
			alias = matcher.group(3);
		}
		// 返回结果
		return new JoinSpace(new Space(schema, table), alias);
	}

	/**
	 * 解析SELECT格式
	 * @param sql
	 * @param chooser
	 * @return
	 */
	private Join splitSelect(String sql) {
		Pattern pattern = Pattern.compile(JoinParser.SQL_SELECT);
		Matcher matcher = pattern.matcher(sql);
		if (!matcher.matches()) {
			return null;
		}

		// SELECT 单元
		String prefix = matcher.group(1);
		String leftSpace = matcher.group(2);
		String rightSpace = matcher.group(3);
		String where = matcher.group(4);
		String orderby = matcher.group(5);

		// 这里固定为内联
		Join result = new Join(Join.INNER_JOIN);

		//2. 解析FROM子句(左表)，处理别名现象
		JoinSpace left = this.splitSpaces(leftSpace);
		result.setLeftSpace(left.space);
		//3. 解析JOIN子句(右表)，处理别名现象
		JoinSpace right = this.splitSpaces(rightSpace);
		result.setRightSpace(right.space);

		// JOIN表
		JoinSet jset = new JoinSet();
		jset.add(left.alias, left.space);
		jset.add(right.alias, right.space);

		//4. 解析ON子句，对比关联
		OnIndex index = splitOn(where, jset);
		result.setIndex(index);
		//5. 解析显示单元语句
		ListSheet sheet = this.splitListSheet(prefix, jset);
		result.setListSheet(sheet);
		//5. 解析ORDER BY语句
		if(!orderby.matches("\\s*")) {
			OrderByAdapter by = this.splitOrderBy(orderby, jset);
			result.setOrderBy(by);
		}
		// 返回结果
		return result;
	}	

	/**
	 * 解析SQL JOIN语句，生成JOIN实例
	 * @param sql SQL JOIN语句
	 * @return 返回Join命令
	 */
	public Join split(final String sql) {
		String prefix = null;
		String from = null;
		String join_family = null;
		String to = null;
		String on = null;
		String order = null;

		// 第一种情况检查
		Pattern pattern = Pattern.compile(JoinParser.SQL_JOIN);
		Matcher matcher = pattern.matcher(sql);
		boolean match = matcher.matches();
		if (match) {
			prefix = matcher.group(1);
			from = matcher.group(2);
			join_family = matcher.group(3);
			to = matcher.group(4);
			on = matcher.group(5);
			order = matcher.group(6);
		}
		// 第二种情况检查
		if (!match) {
			Join join = this.splitSelect(sql);
			if (join != null) {
				join.setPrimitive(sql);
				return join;
			}
		}
		if (!match) {
			throwable("syntax error or missing");
		}

		//1. 检查JOIN类型
		byte kind = 0;
		if(join_family.matches(JoinParser.INNER_JOIN)) {
			kind = Join.INNER_JOIN;
		} else if(join_family.matches(JoinParser.LEFT_JOIN)) {
			kind = Join.LEFT_JOIN;
		} else if(join_family.matches(JoinParser.RIGHT_JOIN)) {
			kind = Join.RIGHT_JOIN;
		} else if(join_family.matches(JoinParser.FULL_JOIN)) {
			kind = Join.FULL_JOIN;
		}

		Join result = new Join(kind);
		//2. 解析FROM子句(左表)，处理别名现象
		JoinSpace left = this.splitSpaces(from);
		result.setLeftSpace(left.space);
		//3. 解析JOIN子句(右表)，处理别名现象
		JoinSpace right = this.splitSpaces(to);
		result.setRightSpace(right.space);

		// JOIN表
		JoinSet jset = new JoinSet();
		jset.add(left.alias, left.space);
		jset.add(right.alias, right.space);

		//4. 解析ON子句，对比关联
		OnIndex index = splitOn(on, jset);
		result.setIndex(index);
		//5. 解析显示单元语句
		ListSheet sheet = this.splitListSheet(prefix, jset);
		result.setListSheet(sheet);
		//5. 解析ORDER BY语句
		if(!order.matches("\\s*")) {
			OrderByAdapter by = this.splitOrderBy(order, jset);
			result.setOrderBy(by);
		}

		// 保存原语
		result.setPrimitive(sql);

		return result;
	}
}