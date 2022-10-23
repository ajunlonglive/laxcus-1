/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 可变长数组分区。子类有：RawSector、WordSector、DocumentSector、ImageSector、AudioSector、VideoSector
 * 
 * @author scott.liang
 * @version 1.0 08/05/2009
 * @since laxcus 1.0
 */
public abstract class VariableSector extends Bit64Sector {

	private static final long serialVersionUID = -8780656102620794897L;

	/** 数据封装(列的压缩和加密参数)，见com.laxcus.access.column.attribute.Packing **/
	private Packing packing;

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.access.index.section.IndexSector#createCodeScaler()
//	 */
//	@Override
//	protected VariableScaler createCodeScaler() {
//		VariableScaler scaler = (VariableScaler) super.createCodeScaler();
//		if (scaler != null) {
//			scaler.setPacking(packing);
//			return scaler;
//		}
//		return null;
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#getSlider()
	 */
	@Override
	protected VariableSlider getSlider() {
		Slider slider = super.getSlider();
		if (slider != null && Laxkit.isClassFrom(slider, VariableSlider.class)) {
			((VariableSlider) slider).setPacking(packing);
			return (VariableSlider)slider;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.Bit32Sector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据封装配置
		writer.writeInstance(packing);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.Bit32Sector#resolvePrefix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader){
		// 解析前缀信息
		super.resolveSuffix(reader);
		// 解析数据封装配置
		packing = reader.readInstance(Packing.class);
	}

	/**
	 * 构造默认的可变长数据分割器
	 */
	protected VariableSector() {
		super();
	}

	/**
	 * 根据传入的可变长数据分割器实例，生成它的副本
	 * @param that VariableSector实例
	 */
	protected VariableSector(VariableSector that) {
		super(that);
		setPacking(that.packing);
	}

	/**
	 * 设置数据封装实例
	 * @param e 数据封装实例
	 */
	public void setPacking(Packing e) {
		packing = e;
	}

	/**
	 * 返回数据封装实例
	 * @return 数据封装实例
	 */
	public Packing getPacking() {
		return packing;
	}

}