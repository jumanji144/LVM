package me.darknet.lua.vm.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringUtil {

	private final static NumberFormat nf = new DecimalFormat("#.##########");

	public static String asString(double d) {
		return nf.format(d);
	}

}
