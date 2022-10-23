/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.traffic.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.ray.pool.*;

/**
 * 多节点流量测试调用器
 * 
 * @author scott.liang
 * @version 1.0 8/20/2018
 * @since laxcus 1.0
 */
public class RayMultiGustInvoker extends RayInvoker {

	/**
	 * 构造多节点流量测试调用器，指定命令
	 * @param cmd 多节点流量测试命令
	 */
	public RayMultiGustInvoker(MultiGust cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MultiGust getCommand() {
		return (MultiGust) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MultiGust cmd = getCommand();

		ArrayList<TrafficProduct> array = new ArrayList<TrafficProduct>();
		
		// 提示执行命令
		messageX(MessageTip.COMMAND_EXECUTE);

		int faults = 0;
		for (Gust sub : cmd.list()) {
			GustHook hook = new GustHook();
			ShiftGust shift = new ShiftGust(sub, hook);
			boolean success = RayCommandPool.getInstance().press(shift);
			if (!success) {
				faults++;
				continue;
			}

			// 进入等待
			hook.await();
			// 返回结果
			TrafficProduct product = hook.getProduct();
			if (product != null && product.isSuccessful()) {
				array.add(product);
			} else {
				faults++;
			}
		}

		// 显示结果
		print(faults, array);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 生成标题单元
	 */
	protected void printTitle() {
		// 生成表格标题
		String[] cells = new String[] { "MULTI-TRAFFIC/ATTRIBUTE",
				"MULTI-TRAFFIC/VALUE" };
		createShowTitle(cells);
	}

	/**
	 * 显示一行参数
	 * @param xmlPath XML标签路径
	 * @param cell 数据单元
	 */
	private void show(String xmlPath, ShowItemCell cell ) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlPath);
		item.add(new ShowStringCell(0, name));
		cell.setIndex(1);
		item.add(cell);
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param array
	 */
	private void print(int faults, List<TrafficProduct> array) {
		printRuntime(array.size(), faults);

		// 显示空标题
		printTitle();

		// 序列号
		MultiGust cmd = getCommand();
		if (cmd.getSerial() > 0) {
			show("MULTI-TRAFFIC/SERIAL", new ShowIntegerCell(1, cmd.getSerial()));
		}
		
		// 失败
		show("MULTI-TRAFFIC/FAULTS", new ShowIntegerCell(1, faults));
		// 成功
		show("MULTI-TRAFFIC/CORRECTS", new ShowIntegerCell(1, array.size()));

		// 如果空，以下忽略！
		if (array.isEmpty()) {
			return;
		}
		
		long length = 0;
		long runtime = 0;
		long packets = 0;
		int retries = 0;
		int timeoutCount = 0;

		for (TrafficProduct e : array) {
			length += e.getSendSize();
			runtime += e.getRunTime();

			packets += e.getSendPacket();
			retries += e.getRetries();
			timeoutCount += e.getTimeoutCount();
		}

		// 发送数据长度
		show("MULTI-TRAFFIC/LENGTH", new ShowStringCell(1, ConfigParser.splitCapacity(length, 3)));

		// 耗时
		String value = doStyleTime(runtime);
		show("MULTI-TRAFFIC/RUNTIME", new ShowStringCell(1, value));

		// 速率
		long rate = (length / runtime) * 1000;
		show("MULTI-TRAFFIC/RATE", new ShowStringCell(1, ConfigParser.splitCapacity(rate, 3)));

		// 子包数目
		show("MULTI-TRAFFIC/PACKETS", new ShowLongCell(1, packets));

		// 重试次数
		show("MULTI-TRAFFIC/RETRIES", new ShowIntegerCell(1, retries));

		// 超时次数
		show("MULTI-TRAFFIC/TIMEOUTS", new ShowIntegerCell(1, timeoutCount));
		
		// 输出全部记录
		flushTable();
	}

}