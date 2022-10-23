/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

/**
 * HSL/RGB转换器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2021
 * @since laxcus 1.0
 */
public class HSLConverter {
	
	/**
	 * 从RGB转换成HSL
	 * @param rgb
	 * @return
	 */
	public static HSL RGB2HSL(RGB rgb) {
		if (rgb == null) {
			return null;

		}

		float H, S, L, var_Min, var_Max, del_Max, del_R, del_G, del_B;

		H = 0;

		var_Min = Math.min(rgb.red, Math.min(rgb.blue, rgb.green));

		var_Max = Math.max(rgb.red, Math.max(rgb.blue, rgb.green));

		del_Max = var_Max - var_Min;

		L = (var_Max + var_Min) / 2;

		if (del_Max == 0) {

			H = 0;

			S = 0;

		} else {

			if (L < 128) {
				S = 256 * del_Max / (var_Max + var_Min);
			} else {
				S = 256 * del_Max / (512 - var_Max - var_Min);
			}

			del_R = ((360 * (var_Max - rgb.red) / 6) + (360 * del_Max / 2)) / del_Max;

			del_G = ((360 * (var_Max - rgb.green) / 6) + (360 * del_Max / 2)) / del_Max;

			del_B = ((360 * (var_Max - rgb.blue) / 6) + (360 * del_Max / 2)) / del_Max;

			if (rgb.red == var_Max) {
				H = del_B - del_G;
			} else if (rgb.green == var_Max) {
				H = 120 + del_R - del_B;
			} else if (rgb.blue == var_Max) {
				H = 240 + del_G - del_R;
			}

			if (H < 0) {
				H += 360;
			}

			if (H >= 360) {
				H -= 360;
			}

			if (L >= 256) {
				L = 255;
			}

			if (S >= 256) {
				S = 255;
			}
		}
		// 返回HSL颜色
		return new HSL(H, S, L);
	}

	/**
	 * HSL颜色转成RGB
	 * @param hsl
	 * @return
	 */
	public static RGB HSL2RGB(HSL hsl) {
		if (hsl == null) {
			return null;
		}

		float H = hsl.getH();

		float S = hsl.getS();

		float L = hsl.getL();

		float R, G, B, var_1, var_2;

		if (S == 0) {

			R = L;

			G = L;

			B = L;

		} else {

			if (L < 128) {

				var_2 = (L * (256 + S)) / 256;

			} else {

				var_2 = (L + S) - (S * L) / 256;

			}

			if (var_2 > 255) {

				var_2 = Math.round(var_2);

			}

			if (var_2 > 254) {

				var_2 = 255;

			}

			var_1 = 2 * L - var_2;

			R = RGBFromHue(var_1, var_2, H + 120);

			G = RGBFromHue(var_1, var_2, H);

			B = RGBFromHue(var_1, var_2, H - 120);

		}

		R = R < 0 ? 0 : R;

		R = R > 255 ? 255 : R;

		G = G < 0 ? 0 : G;

		G = G > 255 ? 255 : G;

		B = B < 0 ? 0 : B;

		B = B > 255 ? 255 : B;

		return new RGB(Math.round(R),  Math.round(G), Math.round(B));
	}

	/**
	 * RGB转换成HUE
	 * @param a
	 * @param b
	 * @param h
	 * @return
	 */
	public static float RGBFromHue(float a, float b, float h) {

		if (h < 0) {

			h += 360;

		}

		if (h >= 360) {

			h -= 360;

		}

		if (h < 60) {

			return a + ((b - a) * h) / 60;

		}

		if (h < 180) {

			return b;

		}

		if (h < 240) {

			return a + ((b - a) * (240 - h)) / 60;

		}

		return a;

	}

	/**

	 * @param args

	 */

	public static void main(String[] args) {

		HSL hsl = RGB2HSL(new RGB(58,110,165));

		System.out.println(hsl.toString());

		RGB rgb = HSL2RGB(new HSL(120.0f,255.0f,127.5f));

		System.out.println(rgb.toString());

	}

}