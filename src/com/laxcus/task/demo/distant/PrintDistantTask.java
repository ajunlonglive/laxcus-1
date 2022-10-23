/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.distant;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.task.demo.distant.random.*;
import com.laxcus.util.classable.*;

/**
 * CONTACT.DISTANT阶段组件，打印显示信息
 * 
 * @author scott.liang
 * @version 1.0 5/5/2020
 * @since laxcus 1.0
 */
public class PrintDistantTask extends DistantGenerateTask {

	/** 快捷组件打印堆栈 **/
	private SwiftPrintStack stack = new SwiftPrintStack();
	
	/**
	 * 构造默认的DISTANT打印组件
	 */
	public PrintDistantTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantGenerateTask#process()
	 */
	@Override
	public long process() throws TaskException {
		long invokerId = getInvokerId();
		
		long rnd = new DistantRandom().random();

		String issuer = (getIssuer() != null ? getIssuer().toString()
				: "Null Issuer");

		stack.printf("本次调用器编号是\"%d\", 签名是\"%s\" \n", invokerId, issuer);
		stack.printf("相位随机聚合数是：%d\n", rnd);
		
//		stack.println();
//		stack.println("莫听穿林打叶声，何妨吟啸且徐行。");
//		stack.println("行杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。");
//		stack.println("料峭春风吹酒醒，微冷。山头斜照却相迎。");
//		stack.println("回首向来萧瑟处，归去，也无风雨也无晴！");
//		
//		stack.println();
//		stack.println("白马非马，吾与子适矣！");
//
//		stack.println();
//		stack.println("大漠孤烟直，长河落日圆。");
//		stack.println("行到水穷处，坐看云起时。");
//		stack.println("望门投止思张俭，忍死须臾待杜根。我自横刀向天笑，去留肝胆两昆仑。");
//		stack.println("凉风起天末，君子意如何？鸿雁几时到，江湖秋水多。");
//		stack.println("待到秋来九月八，我花开后百花杀。冲天香阵透长安，满城尽带黄金甲。");
//		stack.println("瑟瑟西风满院栽，蕊寒香冷蝶难来。他年我若为青帝，报与桃花一处开。");
//		stack.println("马宿江头苜蓿香，半云半雨渡潇湘。东风吹醒英雄梦，不是咸阳是洛阳！");
//		
////		stack.printf("存储命令原文：%s\n", getCommand().getPrimitive());

		// 取当前节点地址
		Node node = getDistantTrustor().getLocal(invokerId);
//		stack.println();
		stack.printf("当前节点地址是 \"%s\"\n", node);

//		Space space = new Space("快捷组件池", "大数据资源");
		stack.println("检测调用者\"%d\"的结果是\"%s\"", invokerId, (getDistantTrustor().allow(invokerId) ? "存在" : "不存在"));
//		stack.println("检索：%s 的结果是：%s", space, (trustor.allow(invokerId, space) ? "表存在" : "表不存在"));

		byte[] b = effuse();
		return b.length;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		ClassWriter writer = new ClassWriter();
		writer.writeInt(stack.size());
		for(SwiftPrintLine line : stack.flush()) {
			writer.writeObject(line);
		}
		return writer.effuse();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		// 转为字节数组
		byte[] b = effuse();
		// 输出到指定的文件
		return	writeTo(file, false, b, 0, b.length);
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.task.contact.distant.DistantTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, byte[], int, int)
//	 */
//	@Override
//	public boolean evaluate(FluxField field, byte[] b, int off, int len)
//			throws TaskException {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.task.contact.distant.DistantTask#evaluate(com.laxcus.distribute.calculate.mid.FluxField, java.io.File)
//	 */
//	@Override
//	public boolean evaluate(FluxField field, File file) throws TaskException {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.task.contact.distant.DistantTask#assemble()
//	 */
//	@Override
//	public long assemble() throws TaskException {
//		// TODO Auto-generated method stub
//		return 0;
//	}

}
