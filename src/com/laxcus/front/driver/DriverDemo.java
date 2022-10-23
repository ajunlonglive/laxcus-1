/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver;

import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * @author scott.liang
 *
 */
public class DriverDemo implements LogPrinter {

	/**
	 * 
	 */
	public DriverDemo() {
		super();
	}

	public void testPrintLog() {
		DriverLauncher.getInstance().setLogPrinter(this);

		Logger.setLevel(LogLevel.DEBUG);

		//		System.out.printf("print log is %s\n", Logger.isConsolePrint() );
		//		System.out.printf("print log level is %d\n", Logger.getLevel());
		//		Logger.debug("test print log");
		//		Logger.debug("this is print log");
		//		Logger.debug("千里江山寒色远");
		//		Logger.info("Helo World");
		//		Logger.warning("warning message");
		//		Logger.error("for fox");
		//		Logger.fatal("芦花深处泊孤舟，笛在月明楼");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.log.client.LogPrinter#print(java.lang.String)
	 */
	@Override
	public void print(String e) {
		//		System.out.print("["+e+"]");
	}

	/**
	 * 线程延时。单位：毫秒。
	 * @param ms - 超时时间
	 */
	private synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				super.wait(ms);
			}
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	private String make() {
		if (DriverLauncher.getInstance().isLinux()) {
			String s1 = "driver-user=demo/demo;";
			String s2 = "log-file=/laxcus/driver/conf/log.xml;";
			String s3 = "config-file=/laxcus/driver/conf/local.xml;";
			return s1 + s2 + s3;
		} else {
			String s1 = "driver-user=demo/demo;";
			String s2 = "log-file=e:/parallel/driver/conf/log.xml;";
			String s3 = "config-file=e:/parallel/driver/conf/local.xml;";
			return s1 + s2 + s3;
		}
	}

	//	private String make() {
	//		String s1 = "hub-site=entrance://localhost:6666_6666;";
	//		String s2 = "local-node=front://localhost:8211_8211;";
	//		String s3 = "echo-directory=e:/laxcus/driver/echo/;";
	//		String s4 = "task-directory=e:/laxcus/driver/task;";
	//		String s5 = "log-file=e:/parallel/driver/conf/log.xml;";
	//		String s6 = "config-file=e:/parallel/driver/conf/local.xml;";
	//
	//		String s10 = "driver-user=demo/demo;";
	////		String s11 = "driver-print=YES";
	//		return s1 + s2 + s3 + s4 + s5+ s10;
	//	}

	//	/*
	//	 * 
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public <T> T getProduct(DriverResult result, Class<T> clazz) {
	//		Laxkit.nullabled(result);
	//
	//		if (result.getClass() != ProductResult.class) {
	//			return null;
	//		}
	//
	//		ProductResult rs = (ProductResult) result;
	//		return rs.getObject(clazz);
	//	}

	void testHash() {
		String input = "build sha256 not case DEMO";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();// 1000, false);

			if(result != null) {
				System.out.printf("result class name is %s\n", result.getClass().getName());
				StringProduct product = result.getObject(StringProduct.class);
				System.out.printf("product class is %s\n", (product == null ? "NUll" : product.getClass().getName()) );
				System.out.printf("product class is: %s\n", result.getThumb().getName());
				if (product != null) {
					System.out.println(product.getValue());
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void testHalf() {
		String input = "encode half not case LAXCUS大数据操作系统。";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();

			if(result != null){
				System.out.printf("result class name is %s\n", result.getClass().getName());
				StringProduct product = result.getObject(StringProduct.class);
				System.out.printf("product class is %s\n", (product == null ? "NUll" : product.getClass().getName()) );
				System.out.printf("product class is:%s\n", result.getThumb().getName());
				if (product != null) {
					System.out.println(product.getValue());
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void textCommandMode(){
		String input = "Set Command Mode Memory";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			if (result != null) {
				System.out.printf("result class name is %s\n", result.getClass().getName());
			}

			//			System.out.printf("On DriverDemo, %s class name is %s, thumb:%s\n",
			//					input, result.getClass().getName(), result.getThumb().getName());

			//			StringProduct product = result.getObject(StringProduct.class);
			//			System.out.printf("product class is %s\n", (product == null ? "NUll" : product.getClass().getName()) );
			//			if (product != null) {
			//				System.out.println(product.getValue());
			//			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void testSwarm() {
		String input = "swarm 1m 256k 32k 0ms to hub";
		System.out.printf("\ntest:%s\n", input);
		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s finished! %s\n", input, result != null);
			if(result != null) {
				System.out.printf("thumb class is %s\n", result.getThumb().getName());
				TrafficProduct product = (TrafficProduct)( ((MissionProductResult)result).getProduct());
				System.out.printf("from node: %s\n",	product.getFrom());
				System.out.printf("to node: %s\n", product.getTo());
				System.out.printf("send size: %s\n", ConfigParser.splitCapacity(product.getSendSize()));
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void testMiner() {
		String input = "CONDUCT MINING FROM SITES:2; PREFIX(STRING)='PentiumIX'; BEGIN(LONG)=0;END(LONG)=500000; ZEROS(INT)=2; GPU(BOOL)=YES; TO SITES:2; PUT NODE(CHAR)='挖矿节点'; TEXT(CHAR)='明文'; SHA256(CHAR)='矿码';";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s finished! %s\n", input, result != null);
			if (result != null) {
				if(result.getClass() == MissionFileResult.class) {
					MissionFileResult rs = (MissionFileResult)result;
					System.out.printf("file is: %s\n", rs.getFile());
				} else if(result.getClass() == MissionBufferResult.class) {
					MissionBufferResult rs = (MissionBufferResult)result;
					byte[] b = rs.getBuffer();
					System.out.printf("buff bytes:%d\n", (b != null ? b.length: -1));

					// 读字节内容，显示参数
					ClassReader reader = new ClassReader(b, 0, b.length);
					while (reader.hasLeft()) {
						Node site = new Node(reader);
						SHA256Hash hash = new SHA256Hash(reader);
						String plain = reader.readString();

						System.out.printf("%s - %s -%s\n", site, hash, plain);
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void testSort() {
		String input = "conduct demo_sort from sites:6;begin(int)=-1000;end(int)=99999;total(int)=200; to sites:3;orderby(char)='desc'; ";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s finished! %s\n", input, result != null);
			if (result != null) {
				if(result.getClass() == MissionFileResult.class) {
					MissionFileResult rs = (MissionFileResult)result;
					System.out.printf("file is: %s\n", rs.getFile());
				} else if(result.getClass() == MissionBufferResult.class) {
					MissionBufferResult rs = (MissionBufferResult)result;
					byte[] b = rs.getBuffer();
					System.out.printf("buff bytes:%d\n", (b != null ? b.length: -1));
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	// 查询
	void testInsert() {
		String input = "INJECT INTO 媒体素材库.音频 (词条, 编号, memo) values ('凉风起天末', 233, 'laxcus bigdata system'), ('麦金突时机器', 899, 'linux system'), ('Solaris', 877, 'SOLARIS UNIX SYSTEM'), ('MacintoshMister', 823, '江海客愁人，匆匆疏离影。'), ('windows', 723, 'windows system'), ('pentium', 322, '木落雁南度，北风江上寒。我家湘水趣，摇隔楚云端'), ('core', 923, 'core cpu'), ('atom', 9233, 'atom cpu'), ('arm', 922, '凉风起天末，君子意如何？鸿雁几时到，江湖秋水多。文章憎命达，鬼魅喜人过。应共冤魂语，投诗赠汨泺'), ('江湖秋水多', 923, 'KEYWORD SECURITY')";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s finished! %s\n", input, result != null);
			if (result != null) {
				if(result.getClass() == MissionFileResult.class) {
					MissionFileResult rs = (MissionFileResult)result;
					System.out.printf("file is: %s\n", rs.getFile());
				} else if(result.getClass() == MissionBufferResult.class) {
					MissionBufferResult rs = (MissionBufferResult)result;
					byte[] b = rs.getBuffer();
					System.out.printf("buff bytes:%d\n", (b != null ? b.length: -1));
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	// 查询
	void testSelect() {
		String input = "SELECT * FROM 媒体素材库.音频 WHERE 编号 > 0 ORDER BY BIRDAY DESC";
		System.out.printf("\ntest:%s\n", input);

		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s finished! %s\n", input, result != null);
			if (result != null) {
				if(result.getClass() == MissionFileResult.class) {
					MissionFileResult rs = (MissionFileResult)result;
					System.out.printf("file is: %s\n", rs.getFile());
				} else if(result.getClass() == MissionBufferResult.class) {
					MissionBufferResult rs = (MissionBufferResult)result;
					byte[] b = rs.getBuffer();
					System.out.printf("buff bytes:%d\n", (b != null ? b.length: -1));
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	void testCreateUser() {
		String input = "CREATE USER unix password 'unix'";
		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit(0, false);
			System.out.printf("\nclass name is %s\n", result.getClass().getName());

			CreateUserProduct product = result.getObject( CreateUserProduct.class);
			if(product != null){
				System.out.printf("aid site:%s - %s\n", product.getEntranceInner(), product.getEntranceOuter());
			}

		} catch (Exception e) {
			//			e.printStackTrace();
			Logger.error(e);
		}
	}

	void testCreateSchema() {
		String input = "CREATE SCHEMA Technology";
		try {
			DriverMission mission = DriverLauncher.getInstance().create(input);
			MissionResult result = mission.commit();
			System.out.printf("%s class name is %s\n", input, result.getClass().getName());

			CreateSchemaProduct product = result.getObject( CreateSchemaProduct.class);
			if(product != null){
				System.out.printf("schema is:%s\n", product.getFame());
			}

		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public void test() {
		DriverLauncher.getInstance().setLogPrinter(this);

		String params = make();
		System.out.println(params);

		boolean success = DriverLauncher.getInstance().launch(params);

		System.out.printf("Start Driver %s!!!\n", success);

		delay(5000);
		if (success) {
			testHash();
			testHalf();
			textCommandMode();
			// 流量测试
			testSwarm();

						// 挖矿
						testMiner();

			//			// 排序
			//			testSort();
			//			// 插入
			//			testInsert();
			//			// 查询
			//			testSelect();

			// this.testCreateUser();
			// this.testCreateSchema();
		}

		delay(5000);

		System.out.println("\nStop Driver, One!");

		DriverLauncher.getInstance().stop();
		System.out.println("Stop Driver, Two!");
		delay(2000);
		Logger.gushing();

		// 释放
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DriverDemo demo = new DriverDemo();
		demo.test();

		//		demo.testPrintLog();
	}

	// hub-site=entrance://localhost:7500_7500;local-node=front://localhost:8211_8211;echo-directory=/laxcus/driver/echo/;task-directory=/laxcus/driver/task;driver-user=demo/demo;driver-print=YES

}