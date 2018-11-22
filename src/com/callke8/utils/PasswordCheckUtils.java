package com.callke8.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.callke8.system.param.ParamConfig;

/**
 * 密码检查工具 
 * 
 * @author 黄文周
 */
public class PasswordCheckUtils {
	
	//用于匹配是否包含数字
	private final static String REGEX_NUMBER = ".*[0-9]+.*";
	
	//用于匹配是否包含字母
	private final static String REGEX_LETTER = ".*[a-zA-Z]+.*";
	
	//用于匹配是否包含特殊字符,
    private final static String REGEX_SPECIAL_CHAR = ".*[" + "`~!@#\\$%\\^&\\*\\(\\)\\-_\\+=<,>\\.\\?\\/\\[\\{\\}\\]" + "]+.*";
   
    
    private static Pattern PATTERN_NUMBER;
    private static Pattern PATTERN_LETTER;
    private static Pattern PATTERN_SPECIAL_CHAR;
    
    static{
    	PATTERN_NUMBER = Pattern.compile(REGEX_NUMBER);
    	PATTERN_LETTER = Pattern.compile(REGEX_LETTER);
    	PATTERN_SPECIAL_CHAR = Pattern.compile(REGEX_SPECIAL_CHAR);
    }
    
    /**
     * 检查密码的长度，如果密码长度小于XX位(根据系统参数配置)，返回false
     * 
     * @param password
     * @return
     */
    public static boolean checkLength(String password){
    	
    	if(BlankUtils.isBlank(password)) {
    		return false;
    	}
    	
    	//系统配置的密码的长度要求
    	int passwordLength = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_1_passwordLength"));
    	if(password.length()<passwordLength) {
    		return false;
    	}else {
    		return true;
    	}
    	
    }
    
    /**
     * 检查密码是否包含数字
     * 
     * @param password
     * @return
     */
    public static boolean containNumber(String password) {
    	if(!checkLength(password)) {
    		return false;
    	}
    	Matcher match = PATTERN_NUMBER.matcher(password);
    	return match.matches();
    }
    
    /**
     * 检查密码是否包含字母
     * 
     * @param password
     * @return
     */
    public static boolean containLetter(String password) {
    	if(!checkLength(password)) {
    		return false;
    	}
    	Matcher match = PATTERN_LETTER.matcher(password);
    	return match.matches();
    }
    
    /**
     * 检查密码是否包含特殊字符
     * 
     * @param password
     * @return
     */
    public static boolean containSpecialChar(String password) {
    	if(!checkLength(password)) {
    		return false;
    	}
    	Matcher match = PATTERN_SPECIAL_CHAR.matcher(password);
    	return match.matches();
    }
    
    /**
     * 检查密码是否包括数字和字母
     * 
     * @param password
     * @return
     */
    public static boolean containNumberAndLetter(String password) {
    	if(containNumber(password) && containLetter(password)) {     //两种判断都为 true 时，返回 true
    		return true;
    	}else {
    		return false;
    	}
    }
    
    /**
     * 检查是否包括数字、字母和特殊字符 三种密码组合方式
     * 
     * @param password
     * @return
     */
    public static boolean containNumberAndLetterAndSpecialChar(String password) {
    	
    	if(containNumber(password) && containLetter(password) && containSpecialChar(password)) {
    		return true;
    	}else {
    		return false;
    	}
    	
    }
	
}
