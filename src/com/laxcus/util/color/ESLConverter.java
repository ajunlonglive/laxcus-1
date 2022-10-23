/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

/**
 * ESL/RGB颜色转换器。<br><br>
 * 
 * 把ESL颜色转换成RGB颜色，或者把RGB颜色转换成ESL颜色。
 * 
 * ESL转RGB，L值是0值，返回RGB(0,0,0)，即全黑。
 * 
 * @author scott.liang
 * @version 1.0 3/27/2020
 * @since laxcus 1.0
 */
public class ESLConverter {

	/**
	 * 根据规则转换颜色
	 * @param v1
	 * @param v2
	 * @param vH
	 * @return
	 */
	private static double HUE2RGB(double v1, double v2, double vH) {
		if (vH < 0) vH += 1;
		if (vH > 1) vH -= 1;
		if (6.0 * vH < 1) return v1 + (v2 - v1) * 6.0 * vH;
		if (2.0 * vH < 1) return v2;
		if (3.0 * vH < 2) return v1 + (v2 - v1) * ((2.0 / 3.0) - vH) * 6.0;
		return (v1);
	}

	/**
	 * 转换颜色
	 * @param H
	 * @param S
	 * @param L
	 * @return
	 */
	private static RGB __convert(double H, double S, double L) {
		double R, G, B;
		double var_1, var_2;
		if (S == 0) {
			R = L * 255.0;
			G = L * 255.0;
			B = L * 255.0;
		} else {
			if (L < 0.5) var_2 = L * (1 + S);
			else var_2 = (L + S) - (S * L);

			var_1 = 2.0 * L - var_2;

			R = 255.0 * HUE2RGB(var_1, var_2, H + (1.0 / 3.0));
			G = 255.0 * HUE2RGB(var_1, var_2, H);
			B = 255.0 * HUE2RGB(var_1, var_2, H - (1.0 / 3.0));
		}

		// 转换成颜色值
		return new RGB((int) Math.round(R), (int) Math.round(G), (int) Math.round(B));
	}

	/**
	 * 转换成颜色值
	 * @param H
	 * @param S
	 * @param L
	 * @return RGB实例
	 */
	public static RGB convert(double H, double S, double L) {
		double h, s, l;
		h = H / 239.0;
		s = S / 240.0;
		l = L / 240.0;
		return __convert(h, s, l);
	}

	/**
	 * 转换成颜色值
	 * @param e ESL实例
	 * @return RGB实例
	 */
	public static RGB convert(ESL e) {
		return convert(e.getH(), e.getS(), e.getL());
	}

	/**
	 * 把颜色从RGB转成ESL
	 * @param rgb 颜色
	 * @return ESL实例
	 */
	private static ESL __convert(RGB rgb) {
		double R = rgb.getRed();
		double G = rgb.getGreen();
		double B = rgb.getBlue();
		R = R / 255.0F;
		G = G / 255.0F;
		B = B / 255.0F;

		double min = Math.min(R, Math.min(G, B));
		double max = Math.max(R, Math.max(G, B));
		double del_max = max - min;

		double S = 0, H =0;
		double L = (max + min) / 2.0;

		if (del_max == 0) // this is a gray
		{
			H = 0;
			S = 0;
		} else {
			if (L < 0.5) S = del_max / (max + min);
			else S = del_max / (2 - max - min);

			double del_R = (((max - R) / 6.0) + (del_max / 2.0)) / del_max;
			double del_G = (((max - G) / 6.0) + (del_max / 2.0)) / del_max;
			double del_B = (((max - B) / 6.0) + (del_max / 2.0)) / del_max;

			if (R == max) H = del_B - del_G;
			else if (G == max) H = (1.0 / 3.0) + del_R - del_B;
			else if (B == max) H = (2.0 / 3.0) + del_G - del_R;

			if (H < 0) H += 1;
			if (H > 1) H -= 1;
		}

		return new ESL(H, S, L);
	}

	/**
	 * 把颜色从RGB转成ESL
	 * @param rgb 颜色
	 * @return ESL实例
	 */
	public static ESL convert(RGB rgb) {
		ESL esl = __convert(rgb);
		// 取近似值
		double H = Math.round(esl.getH() * 240.0f);
		double S = Math.round(esl.getS() * 240.0f);
		double L = Math.round(esl.getL() * 240.0f);
		// 返回真实对象
		return new ESL(H, S, L);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ESL hsl = new ESL(139, 240, 0.8);
//		RGB rgb = convert(hsl);
//		System.out.printf("%s -> %s\n", hsl.toString() , rgb.toString());
//
//		hsl = convert(rgb);
//		System.out.printf("%s -> %s\n", rgb.toString(), hsl.toString());
		
		java.awt.Color c = new java.awt.Color(58, 110, 165);
		System.out.println(c.toString());
		RGB rgb = new RGB(c);
		System.out.println(rgb.toString());
		ESL esl = rgb.toESL();
		System.out.println(esl.toString());
		
		System.out.println("---------");
		rgb = esl.toRGB();
		System.out.println(rgb.toString());
		c = esl.toColor();
		System.out.println(c.toString());
		
//		System.out.println("---------");
//		esl = new ESL(141,115,104);
//		System.out.println(esl.toString());
//		rgb = esl.toRGB();
//		System.out.println(rgb.toString());
	}
}
