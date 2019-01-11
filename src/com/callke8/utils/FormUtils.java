package com.callke8.utils;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

/**
 * 表单的工具类
 * 
 * 通过该工具类，可以收取上传文件格式表单
 * 
 * @author hasee
 *
 */
public class FormUtils {
	
	/**
	 * 获取通过表单上传的文件
	 * 
	 * @param request
	 * 			请求句柄
	 * @param controller
	 * 			控制类对象
	 * @param fileColumnName
	 * 			表单文件字段名
	 * @param mimeTypeRequired
	 * 			文件格式要求，以|分隔，如: txt|xls|xlsx 或   vox|wav 
	 * @return
	 * 			（1）如果获取文件成功：返回的 Record 包括三个信息：
	 * 				statusCode: success
	 * 				file:  重命名后的文件对象
	 * 				mimeType: 文件格式类型
	 * 			（2）如果失败：返回的 Record 包括两个信息：
	 * 				statusCode:error
	 * 				message: 错误的提示信息
	 */
	public static Record getUploadFile(HttpServletRequest request,Controller controller,String fileColumnName,String mimeTypeRequired) {
		
		
		Record rsRecord = new Record();    //三个参数：  statusCode:error/success;  message,失败时提示信息, file: file对象
		
		//一、先判断文件大小限制
		int fileSize = request.getContentLength();     //得到上传文件的大小，由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			rsRecord.set("statusCode","error");
			rsRecord.set("message","上传失败：上传文件过大，已经超过 10M");
			return rsRecord;
		}
		
		//二、获取上传的文件
		//（1）:定义上传路径，临时文件将上传到 upload 文件夹
		String uploadDir = PathKit.getWebRootPath() + File.separator + "upload" + File.separator;
		//（2）执行上传操作
		UploadFile uf = controller.getFile(fileColumnName,uploadDir);
		
		//三、获取上传文件的类型
		String mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//四、判断文件类型，是否为 txt、xls 或是 xlsx
		if(BlankUtils.isBlank(mimeType) || !mimeTypeRequired.contains(mimeType)) {
			//提示错误之前，先将上传的文件删除
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists())  {
				uf.getFile().delete();
			}
			
			rsRecord.set("statusCode","error");
			rsRecord.set("message","上传失败,上传的号码文件类型不正确,只支持:" + mimeTypeRequired);
			return rsRecord;
		}
		
		//五、上传的文件名可能是中文名，不利于读取，需先重命名为数字格式文件名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File(uploadDir + renameFileName);
		
		uf.getFile().renameTo(newFile);
		
		rsRecord.set("statusCode", "success");
		rsRecord.set("file",newFile);
		rsRecord.set("mimeType",mimeType);
		
		return rsRecord;
	}
	
}
