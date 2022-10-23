/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import com.laxcus.access.column.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 字符串列分区。子类包括：CharSector、WCharSector、HCharSector
 * 
 * @author scott.liang
 * @version 1.0 08/05/2009
 * @since laxcus 1.0
 */
public abstract class WordSector extends VariableSector {  

	private static final long serialVersionUID = -6439005358526818543L;

	/** 判断大小写敏感，默认是TRUE(敏感) **/
	private boolean sentient;

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.access.index.section.VariableSector#createCodeScaler()
//	 */
//	@Override
//	protected WordScaler createCodeScaler() {
//		WordScaler scaler = (WordScaler) super.createCodeScaler();
//		if (scaler != null) {
//			scaler.setSentient(sentient);
//			return scaler;
//		}
//		return null;
//	}
	

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.VariableSector#getSlider()
	 */
	@Override
	protected WordSlider getSlider() {
		Slider slider = super.getSlider();
		if(slider != null && Laxkit.isClassFrom(slider, WordSlider.class)) {
			((WordSlider)slider).setSentient(sentient);
			return (WordSlider)slider;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.VariableSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀信息
		super.buildSuffix(writer);
		// 敏感
		writer.writeBoolean(sentient);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.VariableSector#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader){
		// 解析前缀信息
		super.resolveSuffix(reader);
		// 大小写敏感
		sentient = reader.readBoolean();
	}

	/**
	 * 构造一个默认的字符分区
	 */
	protected WordSector() {
		super();
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that
	 */
	protected WordSector(WordSector that) {
		super(that);
		sentient = that.sentient;
	}

	/**
	 * 设置大小写敏感。IS TRUE，大小写敏感，否则为NO
	 * @param b
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 判断大小写敏感
	 * @return
	 */
	public boolean isSentient() {
		return sentient;
	}

	//	/**
	//	 * 从字符中中取出首字符的代码位，根据代码位，选择文字对应的存放位置。
	//	 * 
	//	 * 执行顺序是:
	//	 * <1> 取出二进制字节数组格式的字符串，可能是已经压缩或者加密的。
	//	 * <2> 解包(解密，解压)
	//	 * <3> 根据字符集编码(UTF8、UTF16、UTF32)解码，生成String
	//	 * <4> 如果是忽略大小写的，改成小写字符
	//	 * <5> 从String中取出首字符代码位(兼容UTF-16，UTC-2，包括基本多语言平面BMP和辅助平面)
	//	 * <6> 根据首字代码位，取得对应下标位置
	//	 * 
	//	 * @param that
	//	 * @return
	//	 */
	//	private int seekWord1(Word that) {
	////		// 编码类型必须匹配
	////		if (this.charset.getClass() != that.getCharset().getClass()) {
	////			throw new ClassCastException("not match charset");
	////		}
	//		// 如果是null/empty状态时，返回0下标
	//		if (that.isNull() || that.isEmpty()) {
	//			return 0;
	//		}
	//
	//		byte[] value = that.getValue();
	//
	//		// 解包
	//		Packing packing = super.getPacking();
	//		if (packing != null && packing.isEnabled()) {
	//			try {
	//				value = VariableGenerator.depacking(packing, value, 0, value.length);
	//			} catch (IOException e) {
	//				Logger.error(e);
	//				return 0;
	//			}
	//		}
	//
	//		String text = charset.decode(value, 0, value.length);
	//		// 如果大小写不敏感，转成小写字符
	//		if (!this.isSentient()) {
	//			text = text.toLowerCase();
	//		}
	//
	//		// 取首字符代码位
	//		int codePoint = charset.codePointAt(0, text);
	//		return this.seekCodePoint(codePoint);
	//	}

	/**
	 * 执行顺序：<br>
	 * 1. 确认码位计算器
	 * 2. 若没有，使用系统自定义的。
	 * 3. 通过码位计算器，找到字符列在分区中的位置
	 * @param column - 字节列
	 * @return - 返回字符列对应的下标位置
	 */
	private int seekWord(Word column) {
		// 如果是null/empty状态时，返回0下标
		if (column.isNull() || column.isEmpty()) {
			return 0;
		}

		// 先找系统中的码位计算器，如果没有，根据数据类型使用关联的码位计算器
//		WordScaler scaler = createCodeScaler();
		
		WordSlider scaler = getSlider();
		// 没有，建立本地定义
		if (scaler == null) {
			if (column.isChar()) {
				scaler = new CharSlider();
			} else if (column.isWChar()) {
				scaler = new WCharSlider();
			} else if (column.isHChar()) {
				scaler = new HCharSlider();
			}
			// 设置封包和大小写敏感
			scaler.setPacking(getPacking());
			scaler.setSentient(sentient);
		}

		// 使用码位计算器判断字符在分区中的位置
		try {
			// 确定码值
			java.lang.Long seek = (java.lang.Long) scaler.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.longValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 根据传入的“字符串/字符/字符数组”三种情况，取出首字符代码位，选择它在集合的下标位置。
	 * @param that
	 * @return
	 */
	private int seekString(Object that) {
		String text = null;
		if (that.getClass() == String.class) {
			text = (String) that;
		} else if (that.getClass() == java.lang.Character.class) {
			text = Character.toString(((Character) that).charValue());
		} else if (that.getClass() == new char[0].getClass()) {
			char[] s = (char[]) that;
			if (s != null && s.length > 0) {
				text = new String(s, 0, s.length);
			}
		} else {
			throw new ClassCastException("only string or character or char array");
		}

		// 空指针或者没有字符时，排在最前面
		if (text == null || text.length() == 0) {
			return 0;
		}

		// 如果大小写不敏感，转成小写字符
		if (!isSentient()) {
			text = text.toLowerCase();
		}

		// 取首字符代码位
		int codePoint = text.codePointAt(0);
		// 定位
		return seekIndex(codePoint);
	}

	/**
	 * 根据传入的对象实例，判断它在分区数组的下标位置。<br>
	 * 允许传入的对象包括：<br>
	 * (1) com.laxcus.access.column.Word的子类 <br>
	 * (2) java.lang.String <br>
	 * (3) java.lang.Character <br>
	 * (4) char数组 <br>
	 * 
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 检查空错误
		super.check();
		// 空指针，排在最前面
		if (that == null) {
			return 0;
		}

		// 选择按照WORD列，或者字符串/字符/字符数组处理
		if (that instanceof Word) {
			return seekWord((Word) that);
		} else {
			return seekString(that);
		}
	}

}