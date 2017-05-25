package com.callke8.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
	
	/**
	 * Md5 加密
	 * 
	 * @param str
	 * @return
	 */
	public static String Md5(String str) {
		
		String md5Result = null;
		
		if(BlankUtils.isBlank(str)) {
			return null;
		}
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            md5Result = buf.toString();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return md5Result;
		
	}
}
