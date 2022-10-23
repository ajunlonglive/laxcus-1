/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 建立用户账号解析器。<br><br>
 * 
 * 账号参数准则：<br>
 * 1. 用户名和密码是除了ASCII的控制字符和空格字符之外，任何语言/语种文字的组合。<br>
 * 2. 账号附属参数：空间尺寸、登录用户数、最大表数目、部署集群数目、发布到Gate的主机地址。<br><br>
 * 
 * 语法格式：<br>
 * 1. CREATE USER username password 'XXX' <br>
 * 2. CREATER USER username identified by 'XXX'<br> 
 * 3. CREATE USER username password='XXX'<br><br>
 * 
 * 参数：<br>
 * [MAXSIZE={digit}[M|G|T|P]] [MEMBERS={digit}] [JOBS={digit}] [CHUNKSIZE={digit}[M]] [MEMEBERS={digit}] [MAXTABLES={digit}]
 * [GATEWAYS={digit}] [WORKERS={digit}] [BUILDERS={digit}] [CLUSTERS={digit}] [DEPLOY TO ip, ip2...]<br>
 * 
 * @author scott.liang
 * @version 1.0 5/6/2009
 * @since laxcus 1.0
 */
public class CreateUserParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:CREATE\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/** 
	 * 建立用户账号(包括用户名、密码、最大空间)。<br>
	 * CREATE USER username password 'XXX' | CREATER USER username identified by 'XXX' | CREATE USER username password='XXX'  [MAXSIZE={digit}[M|G|T|P]] 
	 */
	private final static String CREATE_USER = "^\\s*(?i)(?:CREATE\\s+USER)\\s+([^\\p{Cntrl}^\\p{Space}]{1,128})\\s+(?i)(?:IDENTIFIED\\s+BY\\s+|PASSWORD\\s*=\\s*|PASSWORD\\s+)\\'([^\\p{Cntrl}^\\p{Space}]+)\\'(\\s+[\\w\\W]+|\\s*)$";
	//"^\\s*(?i)(?:CREATE\\s+USER)\\s+([^\\p{Cntrl}^\\p{Space}]+)\\s+(?i)(?:IDENTIFIED\\s+BY\\s+|PASSWORD\\s*=\\s*|PASSWORD\\s+)\\'([^\\p{Cntrl}^\\p{Space}]+)\\'(\\s+[\\w\\W]+|\\s*)$";
	

//	/** 子参数 **/
//	private final static String MAXSIZE = "^\\s*(?i)MAXSIZE\\s*=\\s*([0-9]{1,})(?i)(M|G|T|P)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String MEMBERS = "^\\s*(?i)(?:MEMBERS|LOGINS)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String JOBS = "^\\s*(?i)(?:JOBS)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//
//	private final static String MAXTABLES = "^\\s*(?i)MAXTABLES\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String INDEXES = "^\\s*(?i)(?:INDEXES)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String CLUSTERS = "^\\s*(?i)CLUSTERS\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String CHUNKSIZE = "^\\s*(?i)CHUNKSIZE\\s*=\\s*([0-9]{1,})(?i)(?:M)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//
//	private final static String GATEWAYS = "^\\s*(?i)(?:GATEWAYS)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String WORKERS =  "^\\s*(?i)(?:WORKERS)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String BUILDERS = "^\\s*(?i)(?:BUILDERS)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String BASES = "^\\s*(?i)(?:BASES)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	private final static String SUBBASES = "^\\s*(?i)(?:SUBBASES)\\s*=\\s*([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
//	
//	// 超期时间
//	private final static String EXPIRE_TIME = "^\\s*(?i)(?:EXPIRE TIME)\\s*=\\s*\\'([\\w\\W]+)\\'(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** 子参数 **/
	private final static String MAXSIZE = "^\\s*(?i)MAXSIZE\\s+([0-9]{1,})(?i)(M|MB|G|GB|T|TB|P|PB)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String MEMBERS = "^\\s*(?i)(?:MEMBERS|LOGINS)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String JOBS = "^\\s*(?i)(?:JOBS)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	private final static String MAXTABLES = "^\\s*(?i)MAXTABLES\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String INDEXES = "^\\s*(?i)(?:INDEXES)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String CLUSTERS = "^\\s*(?i)CLUSTERS\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String CHUNKSIZE = "^\\s*(?i)CHUNKSIZE\\s+([0-9]{1,})(?i)(?:M)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	private final static String GATEWAYS = "^\\s*(?i)(?:GATEWAYS)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String WORKERS =  "^\\s*(?i)(?:WORKERS)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String BUILDERS = "^\\s*(?i)(?:BUILDERS)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String BASES = "^\\s*(?i)(?:BASES)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBBASES = "^\\s*(?i)(?:SUBBASES)\\s+([1-9][0-9]{0,})(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	
	// 超期时间
	private final static String EXPIRE_TIME = "^\\s*(?i)(?:EXPIRE TIME)\\s+\\'([\\w\\W]+)\\'(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/**
	 * 构造建立用户账号解析器
	 */
	public CreateUserParser() {
		super();
	}

	/**
	 * 检查与“建立账号”的语句匹配
	 * @param simple 简单格式
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CREATE USER", input);
		}
		Pattern pattern = Pattern.compile(CreateUserParser.CREATE_USER);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析建立用户账号<br>
	 * @param input 格式:CREATE USER username [PASSWORD 'xxx'|IDENTIFIED BY 'xxx'|PASSWORD='xx'] [MAXSIZE={digit}M|G|T|P]
	 * @param online 在线检查
	 * @return 返回CreateUser命令
	 */
	public CreateUser split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		// 解析用户账号
		Pattern pattern = Pattern.compile(CreateUserParser.CREATE_USER);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String username = matcher.group(1);
		String password = matcher.group(2);
		String suffix = matcher.group(3);
		User user = new User(username, password);
		
		// 检查用户账号是否存在
		if (online) {
			if (hasUser(username)) {
				throwableNo(FaultTip.EXISTED_X, username);
			}
		}

		// 数字签名的明文
		user.setPlainText(username);

		CreateUser cmd = new CreateUser(user);
		cmd.setPlainText(username);
		// 解析后缀参数
		splitSuffix(suffix, cmd);

		return cmd;
	}

	//	/**
	//	 * 解析节点
	//	 * @param input
	//	 * @return
	//	 */
	//	private Node splitNode(String input) {
	//		Node node = null;
	//		try {
	//			node = new Node(input);
	//		} catch (UnknownHostException e) {
	//			throwable(input, 0);
	//		}
	//		if (!node.isGate()) {
	//			throwable("must be gate site", 0);
	//		}
	//		return node;
	//	}

	/**
	 * 解析账号的其它参数
	 * @param input
	 * @param user
	 */
	private void splitSuffix(String input, CreateUser cmd) {
		User user = cmd.getUser();

		while (input.trim().length() > 0) {
			// 账号的最大空间尺寸
			Pattern pattern = Pattern.compile(CreateUserParser.MAXSIZE);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				long maxsize = Long.parseLong(matcher.group(1));
				String unit = matcher.group(2);
				input = matcher.group(3);
				if ("M".equalsIgnoreCase(unit)) {
					user.setMaxSize(maxsize * Laxkit.MB);
				} else if ("G".equalsIgnoreCase(unit)) {
					user.setMaxSize(maxsize * Laxkit.GB);
				} else if ("T".equalsIgnoreCase(unit)) {
					user.setMaxSize(maxsize * Laxkit.TB);
				} else if ("P".equalsIgnoreCase(unit)) {
					user.setMaxSize(maxsize * Laxkit.PB);
				}
				continue;
			}

			// FRONT站点最大登录数目（有多少个用户可以同时在线使用这个账户下的资源）
			pattern = Pattern.compile(CreateUserParser.MEMBERS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setMembers(num);
				continue;
			}

			// 最大并行任务数目
			pattern = Pattern.compile(CreateUserParser.JOBS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setJobs(num);
				continue;
			}

			// 网关数目（可分配的CALL站点数目）
			pattern = Pattern.compile(CreateUserParser.GATEWAYS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setGateways(num);
				continue;
			}

			// WORK节点数目
			pattern = Pattern.compile(CreateUserParser.WORKERS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setWorkers(num);
				continue;
			}
			// BUILD节点数目
			pattern = Pattern.compile(CreateUserParser.BUILDERS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setBuilders(num);
				continue;
			}
			// DATA主节点数目
			pattern = Pattern.compile(CreateUserParser.BASES);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setBases(num);
				continue;
			}
			// DATA从节点数目
			pattern = Pattern.compile(CreateUserParser.SUBBASES);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setSubBases(num);
				continue;
			}

			// 可分配到多少个HOME集群
			pattern = Pattern.compile(CreateUserParser.CLUSTERS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int num = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setGroups(num);
				continue;
			}

			// 用户可建立的最大表数目
			pattern = Pattern.compile(CreateUserParser.MAXTABLES);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int tables = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setTables(tables);
				continue;
			}

			//每个表的最大索引数目
			pattern = Pattern.compile(CreateUserParser.INDEXES);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int indexes = Integer.parseInt(matcher.group(1));
				input = matcher.group(2);
				user.setIndexes(indexes);
				continue;
			}

			// 解析数据块编号
			pattern = Pattern.compile(CreateUserParser.CHUNKSIZE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String num = matcher.group(1);
				input = matcher.group(2);
				// 设置数据块尺寸，在32M-256M之间
				int chunksize = Integer.parseInt(num);
				if (chunksize < 32 || chunksize > 256) {
					// throwable("This is illegal size: %s", num);
					throwableNo(FaultTip.ILLEGAL_VALUE_X, num);
				}
				user.setChunkSize(chunksize * 1024 * 1024);
				continue;
			}

			// 到期时间
			pattern = Pattern.compile(CreateUserParser.EXPIRE_TIME);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String time = matcher.group(1);
				input = matcher.group(2);
				// 转换为时间戳格式
				long timestamp = 0;
				// 不是无限制时间
				if (!time.matches("^\\s*(?i)(UNLIMIT)\\s*$")) {
					timestamp = CalendarGenerator.splitTimestamp(time);
				}
				user.setExpireTime(timestamp);
				continue;
			}

			// 错误
			throwable(input, 0);
		}
	}

}