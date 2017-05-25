package com.callke8.common;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.callke8.utils.BlankUtils;
import com.jfinal.i18n.I18N;
import com.jfinal.kit.StringKit;

/**
 * 自定义国际化标签，主要是用于在前端显示国际化信息
 * 
 * @author <a href="mailto:120077407@qq.com">hwz</a>
 *
 */
public class I18nTag extends TagSupport {

	private static final long servialVersionUID = 8073376431317433802L;
	
	/**
	 * I18N中的key
	 */
	private String key;
	
	/**
	 * 当key值不存在时使用的默认值
	 */
	private String defaultValue;
	
	/**
	 * 地区属性,如 zh_CN,en_US
	 */
	private String locale;
	
	/**
	 * 作为value格式化值使用的参数，多个用逗号隔开，如果参数在 request中有对应的 attribute,则取 attribute值
	 */
	private String paras;
	
	public int doStartTag() throws JspException {
		
		//定义输出给页面的 text
		String text = null;
		
		try {
			System.out.println(this.defaultValue + "--------------0000000000000-----------");
			System.out.println(locale);
			
			String loc = pageContext.getRequest().getLocale().toString();
			System.out.println("传入的 locale 为:" + loc + ",Key:" + key);
			
			locale = loc;
			
			if(BlankUtils.isBlank(locale)) {
				//通过I18N接口拿到值
				text = I18N.getText(key,this.defaultValue);
			}else {
				text = I18N.getText(key, this.defaultValue,I18N.localeFromString(locale));
			}
			
		}catch(Exception e) {
			text = defaultValue;
		}
		
		if(!BlankUtils.isBlank(paras)) {
			
			//如果tag中指定了 paras,则对 paras解析为 array
			String[] attrs = paras.split(",");
			Object[] values = new Object[attrs.length];
			
			//循环将参数到 request 中取值,如果有值，则替换
			for(int i=0; i<attrs.length; i++) {
				String a = attrs[i];
				
				values[i] = pageContext.getRequest().getAttribute(a) == null?attrs[i]:pageContext.getRequest().getAttribute(a);
			}
			
			pageContext.getAttribute("");
			
			text = String.format(text,values);
		}
		
		try {
			//将结果输出到页面
			pageContext.getOut().write(text);
		}catch(IOException ioe) {
			return Tag.SKIP_BODY;
		}
		
		return Tag.EVAL_BODY_INCLUDE;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getParas() {
		return paras;
	}

	public void setParas(String paras) {
		this.paras = paras;
	}
	
}
