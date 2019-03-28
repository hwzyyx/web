package com.callke8.cnn.cnnvoice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import com.callke8.autocall.voice.Voice;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import net.sf.json.JSONArray;

public class CnnVoiceController extends Controller implements IController  {

	//重命名的新文件名，主要是上传的语音名字可能是中文名，有可能有乱码的情况
	String renameFileName = null;
	//语音类型(定义一个空变量即可)
	String mimeType = null;
	
	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String voiceDesc = getPara("voiceDesc");
		String flag = getPara("flag");

		Map map = CnnVoice.dao.getCnnVoiceByPaginateToMap(pageNumber,pageSize,voiceDesc,flag);
		renderJson(map);
	}

	@Override
	public void add() {
		
		int fileSize = getRequest().getContentLength();   //得到上传的语音文件的大小,jfinal 最大的限制是10M,上传的语音文件不能超过限制
		if(fileSize > (10 * 1024 * 1024)) {
			render(RenderJson.error("添加语音失败,上传的语音文件过大,上传的语音不能超过10M!"));
			return;
		}
		
		this.renameFileName = String.valueOf(DateFormatUtils.getTimeMillis());     //定义一个文件名，不包括文件名后缀
		this.mimeType = "";                                                        //定义一个空变量，用上传文件操作赋值用
		
		//执行文件上传操作
		String fileUploadResult = fileUpload();              //执行上传操作
		System.out.println("上传的文件的 mimeType ：" + mimeType);
		if(!BlankUtils.isBlank(fileUploadResult)) {    //如果文件上传处理结果返回不为空时，表示遇到处理错误了，按错误直接返回
			render(RenderJson.error(fileUploadResult));
			return ;
		}
		
		
		CnnVoice formData = getModel(CnnVoice.class,"cnn_voice");
		
		//判断语音内容，是否重复
		String voiceDesc = formData.getStr("VOICE_DESC");
		if(!BlankUtils.isBlank(CnnVoice.dao.getCnnVoiceByVoiceDesc(voiceDesc))) {
			//错误时，要删除已经上传的文件
			//String autocallVoicePath = MemoryVariableUtil.voicePathMap.get("autocallVoicePath");
			String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");
			String uploadDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator;
			
			File file = new File(uploadDir + renameFileName + "." + mimeType);
			
			if(file.exists()) {
				file.delete();
			}
			
			render(RenderJson.error("新增语音失败,已经存在相同的语音内容!"));
			return;
		}
		
		//设置添加人代码
		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		//设置语音文件名
		formData.set("VOICE_NAME",renameFileName);
		
		//设置创建时间
		formData.set("CREATE_TIME", DateFormatUtils.getCurrentDate());

		boolean b = CnnVoice.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}
	
	public void addForTTS() {
		//执行上传语音保存
		fileUpload();        //由于在上传前已经将文件框的内容清空，所以不会造成什么影响，必须要做这一个操作，那些参数才能正常传过来。
		
		CnnVoice formData = getModel(CnnVoice.class,"cnn_voice");
		
		//判断语音名字是否重复
		String voiceDesc = formData.get("VOICE_DESC");
		if(!BlankUtils.isBlank(CnnVoice.dao.getCnnVoiceByVoiceDesc(voiceDesc))) {
			render(RenderJson.error("新增语音失败,已经存在相同的语音内容!"));
			return;
		}
		
		String ttsContent = null;
		try {   //要进行 TTS 的内容
			ttsContent = URLDecoder.decode(getPara("ttsContent").toString(),"utf-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {  //要进行 TTS 的内容
			ttsContent = URLEncoder.encode(ttsContent,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		};
		
		this.renameFileName = String.valueOf(DateFormatUtils.getTimeMillis());     //定义一个文件名，不包括文件名后缀
		this.mimeType = "wav";
		
		//先判断TTS内容是否为空
		if(BlankUtils.isBlank(ttsContent)) {
			render(RenderJson.error("TTS内容为空,添加语音失败!"));
			return;
		}
		
		String tok = CommonController.getTTSTok();
		
		//从内存中取出配置的自动外呼的语音路径配置，主要包括原始语音路径和转换成 wav(8000)即是single 存放的路径
		String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");
		String voicePathSingle = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle");
		
		//组织上传路径的绝对路径()
		String voicePathFullDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + renameFileName + "." + mimeType;
		String voicePathSingleFullDir = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + renameFileName + "." + mimeType;
		
		//执行TTS操作,并保存到 outputDir
		HttpRequestUtils.httpRequestForTTSToFile(tok, ttsContent, voicePathFullDir);
		
		//然后执行sox转换
		String chmodCmd = "chmod 777 " + voicePathFullDir;
		
		String cmd = ParamConfig.paramConfigMap.get("paramType_1_soxBinPath") + " " + voicePathFullDir + " -r 8000 -c 1 " + voicePathSingleFullDir;
		System.out.println("执行的 CMD  命令为:" + cmd);
		
		try {
			Process chmodP = Runtime.getRuntime().exec(chmodCmd);
			
			Process p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.out.println("执行语音格式转换失败!");
			e.printStackTrace(); 
		}
		
		//设置添加人代码
		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		//设置语音文件名
		formData.set("VOICE_NAME",renameFileName);
		
		//设置创建时间
		formData.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = CnnVoice.dao.add(formData);
		
		if(b) {
			render(RenderJson.success("插入语音成功!"));
		}else {
			render(RenderJson.error("插入语音失败!"));
		}
		
	}
	
	
	/**
	 * 语音文件处理，主要包括语音上传及语音格式转换
	 * 
	 * @return
	 * 				返回空时，表示语音文件处理没有问题；返回不为空时，表示上传过程中有错误
	 */
	public String fileUpload() {
		
		//从内存中取出配置的自动外呼的语音路径配置，主要包括原始语音路径和转换成 vox 存放的路径
		String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");
		String voicePathSingle = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle");
		
		//组织上传路径的绝对路径
		String uploadDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator;
		String voxVoiceDir = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator;
		
		//执行上传语音保存
		UploadFile uf = getFile("voiceFile",uploadDir);
		
		if(BlankUtils.isBlank(uf)) {    //如果上传文件操作得到的 UploadFile 为空时，表示没有选择上传文件
			return "fileEmpty";
		}
		
		//获取上传文件的类型，系统暂时只支持 wav、mp3格式语音文件
		mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//判断文件格式,系统仅支持 wav
		if(BlankUtils.isBlank(mimeType) || !mimeType.equalsIgnoreCase("wav")) {
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists()) {      //先删除语音文件再返回错误提示
				uf.getFile().delete();
			}
			
			return "新增语音文件失败,文件类型不正确,系统仅支持 wav格式语音文件!";
		}
		
		//重命名文件名
		File newFile = new File((uploadDir + renameFileName + "." + mimeType));
		
		uf.getFile().renameTo(newFile);
		
		//重命名文件名后，对语音文件进行语音格式转换，由 wav 转为 vox, wav 用于语音试听， vox 用于 asterisk 使用
		String chmodCmd = "chmod 777 " + uploadDir + renameFileName + "." + mimeType;
		
		String voxFile = voxVoiceDir + renameFileName + ".vox";
		String cmd = ParamConfig.paramConfigMap.get("paramType_1_soxBinPath") + " " + uploadDir + renameFileName + "." + mimeType + " -r 8000 -c 1 " + voxFile;
		System.out.println("执行的 CMD  命令为:" + cmd);
		
		try {
			//执行语音转换前，需要将原始文件的权限加到最大
			Process chmodP = Runtime.getRuntime().exec(chmodCmd);
			
			Process p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.out.println("执行语音格式转换失败!");
			e.printStackTrace(); 
		}
		
		return null;
	}

	@Override
	public void update() {      //通过文件上传
		
		String fType = getPara("fType");        //从上传的参数中确定，该上传类型为 fType 
		
		renameFileName = String.valueOf(DateFormatUtils.getTimeMillis());          //定义一个文件名，不包括文件名后缀
		mimeType = "";                                                             //定义一个空变量，用上传文件操作赋值用
		
		//如果有上传语音，就要做语音上传及语音转换
		int fileSize = getRequest().getContentLength();   //得到上传的语音文件的大小,jfinal 最大的限制是10M,上传的语音文件不能超过限制
		
		if(fileSize > (10 * 1024 * 1024)) {
			render(RenderJson.error("添加语音失败,上传的语音文件过大,上传的语音不能超过10M!"));
			return;
		}
		
		//执行文件上传操作
		String fileUploadResult = fileUpload();
		if(!BlankUtils.isBlank(fileUploadResult) && !fileUploadResult.equalsIgnoreCase("fileEmpty")) {    //如果文件上传处理结果返回不为空时，表示遇到处理错误了，按错误直接返回
			render(RenderJson.error(fileUploadResult));
			return ;
		}
		
		
		CnnVoice formData = getModel(CnnVoice.class,"cnn_voice");

		int id = formData.get("ID");
		String voiceDesc = formData.get("VOICE_DESC");
		int flag = Integer.valueOf(formData.get("FLAG").toString());
		
		//修改之前,先检查是否已经存在相同的语音内容
		Record checkCnnVoice = CnnVoice.dao.getCnnVoiceByVoiceDesc(voiceDesc);
		if(!BlankUtils.isBlank(checkCnnVoice)) {
			int vId = checkCnnVoice.getInt("ID");
			if(vId!=0 && vId!=id) {             //表示数据库中已经存在相同的内容了，该取消这次的修改操作
				//错误时，要删除已经上传的文件
				String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");
				String uploadDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator;
				
				File file = new File(uploadDir + renameFileName + "." + mimeType);
				
				if(file.exists()) {
					file.delete();
				}
				
				render(RenderJson.error("修改语音失败!已存在相同的任务名字!"));
				return;
			}
		}
		
		//如果 fType=1时，表示上传了新的语音文件,要先删除旧文件，然后替换成新的文件
		if(!BlankUtils.isBlank(fType) && fType.equalsIgnoreCase("1")) {
			//在修改数据库信息之前，先要取出原来的记录数据，用于删除旧的语音文件
			CnnVoice cnnVoice = CnnVoice.dao.getCnnVoiceById(id);
			
			//先删除旧的文件
			fileDelete(cnnVoice);
			
			formData.set("VOICE_NAME", renameFileName);
		}

		boolean b = CnnVoice.dao.update(voiceDesc,flag,formData.getStr("VOICE_NAME"),id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}
	
	public void updateForTTS() {
		
		//执行上传语音保存
		fileUpload();        //由于在上传前已经将文件框的内容清空，所以不会造成什么影响，必须要做这一个操作，那些参数才能正常传过来。
		
		CnnVoice formData = getModel(CnnVoice.class,"cnn_voice");
		
		int id = formData.get("ID");
		int flag = Integer.valueOf(formData.get("FLAG").toString());
		String voiceDesc = formData.getStr("VOICE_DESC");
		
		
		//修改之前,先检查是否已经存在相同的语音内容
		Record checkCnnVoice = CnnVoice.dao.getCnnVoiceByVoiceDesc(voiceDesc);
		if(!BlankUtils.isBlank(checkCnnVoice)) {
			int vId = checkCnnVoice.getInt("ID");
			if(vId!=0 && vId!=id) {             //表示数据库中已经存在相同的内容了，该取消这次的修改操作
				render(RenderJson.error("修改语音失败!已存在相同的任务内容!"));
				return;
			}
		}
		
		String ttsContent = null;
		
		try {   //要进行 TTS 的内容
			ttsContent = URLDecoder.decode(getPara("ttsContent").toString(),"utf-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {  //要进行 TTS 的内容
			ttsContent = URLEncoder.encode(ttsContent,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		};
		
		renameFileName = String.valueOf(DateFormatUtils.getTimeMillis());     //定义一个文件名，不包括文件名后缀
		mimeType = "wav";                                                        //定义一个空变量，用上传文件操作赋值用
		
		//如果TTS内容不为空时，就要做TTS转换
		if(!BlankUtils.isBlank(ttsContent)) {
			
			String tok = CommonController.getTTSTok();
			
			//从内存中取出配置的自动外呼的语音路径配置，主要包括原始语音路径和转换成 vox 存放的路径
			String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");            
			String voicePathSingle = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle"); 
			
			//组织上传路径的绝对路径
			String voicePathFullDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + renameFileName + "." + mimeType;
			String voicePathSingleFullDir = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + renameFileName + "." + mimeType;
			
			//执行TTS操作,并保存到 outputDir
			HttpRequestUtils.httpRequestForTTSToFile(tok, ttsContent, voicePathFullDir);
			
			//然后执行sox转换
			String chmodCmd = "chmod 777 " + voicePathFullDir;
			
			String cmd = ParamConfig.paramConfigMap.get("paramType_1_soxBinPath") + " " + voicePathFullDir + " -r 8000 -c 1 " + voicePathSingleFullDir;
			
			System.out.println("执行的 CMD  命令为:" + cmd);
			
			try {
				//执行转换之前，先将权限加大
				Process chmodP = Runtime.getRuntime().exec(chmodCmd);
				
				Process p = Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				System.out.println("执行语音格式转换失败!");
				e.printStackTrace(); 
			}
			
			//删除原来的语音文件
			//在修改数据库信息之前，先要取出原来的记录数据，用于删除旧的语音文件
			//在修改数据库信息之前，先要取出原来的记录数据，用于删除旧的语音文件
			CnnVoice cnnVoice = CnnVoice.dao.getCnnVoiceById(id);
			
			//先删除旧的文件
			fileDelete(cnnVoice);
			
			formData.set("VOICE_NAME", renameFileName);
			
		}
		
		boolean b = CnnVoice.dao.update(voiceDesc,flag,formData.getStr("VOICE_NAME"),id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
		
	}

	@Override
	public void delete() {
		String id = getPara("id");
		
		//从数据库中查出记录
		CnnVoice cnnVoice = CnnVoice.dao.getCnnVoiceById(Integer.valueOf(id));
		
		if(BlankUtils.isBlank(cnnVoice)) {
			render(RenderJson.error("删除操作失败,数据库中没有该记录,请查证该记录是否已被删除!"));
			return;
		}
		
		//在删除前先删除对应的语音文件
		fileDelete(cnnVoice);

		boolean b = CnnVoice.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * 传入语音文件记录，根据文件名和语音文件类型，删除语音文件（包括Wav 文件和 vox 文件）
	 * @param voice
	 */
	public void fileDelete(CnnVoice cnnVoice) {   
		
		//在删除之前，要先将语音文件一并删除，不然这些残留的语音文件会占用大量的内存
		//定义文件名、及生成文件
		String voiceFileName = cnnVoice.get("VOICE_NAME") + ".wav";   					//文件名=文件名 + 后缀
		String voiceVoxFileName = cnnVoice.get("VOICE_NAME") + ".vox";                  //再根据文件名，定义 vox 文件
		
		File voiceFile = new File(PathKit.getWebRootPath() + File.separator + ParamConfig.paramConfigMap.get("paramType_4_voicePath") + File.separator + voiceFileName);
		File voiceVoxFile = new File(PathKit.getWebRootPath() + File.separator + ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle") + File.separator + voiceVoxFileName);
		
		//删除过程
		if(voiceFile.exists()) {
			voiceFile.delete();
		}
		
		if(voiceVoxFile.exists()) {
			voiceVoxFile.delete();
		}
		
	}
	
}
