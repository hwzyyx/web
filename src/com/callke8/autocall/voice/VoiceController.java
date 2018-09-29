package com.callke8.autocall.voice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistory;
import com.callke8.autocall.questionnaire.Question;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.HttpRequestUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;

/**
 * 语音管理
 * 
 * @author hwz
 */
public class VoiceController extends Controller implements IController {

	//重命名的新文件名，主要是上传的语音名字可能是中文名，有可能有乱码的情况
	String renameFileName = null;
	//语音类型(定义一个空变量即可)
	String mimeType = null;
	
	@Override
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		//语音类型combobox数据返回,有两一个，一个是带请选择，一个不带选择
		setAttr("voiceTypeComboboxDataFor0", CommonController.getComboboxToString("VOICE_TYPE","0"));
		setAttr("voiceTypeComboboxDataFor1", CommonController.getComboboxToString("VOICE_TYPE","1"));
		
		render("list.jsp");
	}
	
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
		
		
		Voice voice = getModel(Voice.class, "voice");
		
		//判断语音名字是否重复
		String voiceDesc = voice.get("VOICE_DESC");
		if(!BlankUtils.isBlank(Voice.dao.getVoiceByVoiceDesc(voiceDesc))) {
			
			//错误时，要删除已经上传的文件
			//String autocallVoicePath = MemoryVariableUtil.voicePathMap.get("autocallVoicePath");
			String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");
			String uploadDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator;
			
			File file = new File(uploadDir + renameFileName + "." + mimeType);
			
			if(file.exists()) {
				file.delete();
			}
			
			render(RenderJson.error("新增语音失败,已经存在相同的语音名称"));
			return;
		}
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String voiceId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		voice.set("VOICE_ID", voiceId);
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		voice.set("CREATE_USERCODE", operId);
		//设置操作工号所在的组织编码
		voice.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置语音文件
		voice.set("FILE_NAME", renameFileName);
		
		//设置语音类型
		voice.set("MIME_TYPE",mimeType);
		
		//设置创建时间
		voice.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = Voice.dao.add(voice);
		
		if(b) {
			render(RenderJson.success("插入语音成功!",voiceId));
		}else {
			render(RenderJson.error("插入语音失败!"));
		}

	}
	
	
	public void addForTTS() {
		
		//执行上传语音保存
		fileUpload();        //由于在上传前已经将文件框的内容清空，所以不会造成什么影响，必须要做这一个操作，那些参数才能正常传过来。
		
		Voice voice = getModel(Voice.class,"voice");
		
		//判断语音名字是否重复
		String voiceDesc = voice.get("VOICE_DESC");
		if(!BlankUtils.isBlank(Voice.dao.getVoiceByVoiceDesc(voiceDesc))) {
			render(RenderJson.error("新增语音失败,已经存在相同的语音名称"));
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
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String voiceId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		voice.set("VOICE_ID", voiceId);
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		voice.set("CREATE_USERCODE", operId);
		//设置操作工号所在的组织编码
		voice.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置语音文件
		voice.set("FILE_NAME", renameFileName);
		
		//设置语音类型
		voice.set("MIME_TYPE",mimeType);
		
		//设置创建时间
		voice.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = Voice.dao.add(voice);
		
		if(b) {
			render(RenderJson.success("插入语音成功!",voiceId));
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
	
	/**
	 * 传入语音文件记录，根据文件名和语音文件类型，删除语音文件（包括Wav 文件和 vox 文件）
	 * @param voice
	 */
	public void fileDelete(Voice voice) {   
		
		//在删除之前，要先将语音文件一并删除，不然这些残留的语音文件会占用大量的内存
		//定义文件名、及生成文件
		String voiceFileName = voice.get("FILE_NAME") + "." + voice.getStr("MIME_TYPE");   //文件名=文件名 + 后缀
		String voiceVoxFileName = voice.get("FILE_NAME") + ".vox";                        //再根据文件名，定义 vox 文件
		
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

	@Override
	public void datagrid() {
		System.out.println("取voice datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String voiceDesc = getPara("voiceDesc");
		String orgCode = getPara("orgCode");
		String voiceType = getPara("voiceType");
		String startTime = getPara("startTime");   //这是一个dateBox,需要加入时间
		String endTime = getPara("endTime");       //这是一个dateBox,需要加入时间
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		
		Map map = Voice.dao.getVoiceByPaginateToMap(pageNumber,pageSize,voiceDesc,voiceType,orgCode,startTime,endTime);
		
		System.out.println("取voice datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}

	@Override
	public void delete() {
		
		String voiceId = getPara("voiceId");
		
		Voice voice = Voice.dao.getVoiceByVoiceId(voiceId);
		
		if(BlankUtils.isBlank(voice)) {   //如果没有该记录时返回错误
			render(RenderJson.error("删除失败,要删除的记录不存在!"));
			return;
		}
		
		//删除之前,先判断该语音是否已经被问卷问题选择
		boolean isUsed = Question.dao.checkVoiceIsUsedByVoiceId(voiceId);
		//System.out.println("isUsed:" + isUsed);
		if(isUsed) {
			render(RenderJson.error("删除失败,要删除的记录已经被问卷管理->问题语音引用,不允许删除!"));
			return;
		}
		
		//删除之前，先判断该语音是否已经被外呼任务选择
		boolean voiceBeUsed = AutoCallTask.dao.checkVoiceBeUsed(voiceId);
		if(voiceBeUsed) {
			render(RenderJson.error("删除失败,要删除的记录已经被自动外呼任务引用,不允许删除!"));
			return;
		}
		
		//删除之前，先判断该语音是否已经被历史外呼任务选择
		voiceBeUsed = AutoCallTaskHistory.dao.checkVoiceBeUsed(voiceId);
		if(voiceBeUsed) {
			render(RenderJson.error("删除失败,要删除的记录已经被历史外呼任务引用,不允许删除!"));
			return;
		}
		
		//执行文件删除操作
		fileDelete(voice);
		boolean b = Voice.dao.deleteByVoiceId(voiceId);
		
		if(b) {
			render(RenderJson.success("删除语音成功!"));
		}else {
			render(RenderJson.error("删除语音失败!"));
		}
		
	}
	
	public void download() {
		
		String path = getPara("path");
		
		File file = new File(PathKit.getWebRootPath() + File.separator + path);
		
		if(file.exists()) {
			renderFile(file);
		}
	}
	
	public void update() {
		
		String flag = getPara("flag");   //从上传的参数中更
		
		renameFileName = String.valueOf(DateFormatUtils.getTimeMillis());     //定义一个文件名，不包括文件名后缀
		mimeType = "";                                                        //定义一个空变量，用上传文件操作赋值用
		
			
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
			
		
		Voice voice = getModel(Voice.class, "voice");   //得上传来的提交信息
		
		String voiceId = voice.get("VOICE_ID");
		String voiceDesc = voice.get("VOICE_DESC");
		
		//修改之前，先检查是否已经存在相同的语音名称
		Voice checkVoice = Voice.dao.getVoiceByVoiceDesc(voiceDesc);
		if(!BlankUtils.isBlank(checkVoice)) {
			String vId = checkVoice.get("VOICE_ID");
			if(!BlankUtils.isBlank(vId) && !vId.equalsIgnoreCase(voiceId)) {
				
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
		
		//如果 flag 为1时，表示上传了新的语音文件，要先删除旧文件，然后替换成新的文件
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			//在修改数据库信息之前，先要取出原来的记录数据，用于删除旧的语音文件
			Voice oldVoiceInfo = Voice.dao.getVoiceByVoiceId(voice.get("VOICE_ID").toString());
			
			//先删除旧的文件
			fileDelete(oldVoiceInfo);
			
			voice.set("FILE_NAME",renameFileName);
			voice.set("MIME_TYPE",mimeType);
		}
		
		boolean b = Voice.dao.update(voice.getStr("VOICE_DESC"),voice.getStr("VOICE_TYPE"),voice.getStr("FILE_NAME"), voice.getStr("MIME_TYPE"), voice.getStr("VOICE_ID"));
		
		if(b) {
			render(RenderJson.success("修改语音成功!"));
		}else {
			render(RenderJson.error("修改语音失败!"));
		}
	}
	
	
	public void updateForTTS() {
		
		//执行上传语音保存
		fileUpload();        //由于在上传前已经将文件框的内容清空，所以不会造成什么影响，必须要做这一个操作，那些参数才能正常传过来。
		
		Voice voice = getModel(Voice.class, "voice");   //得上传来的提交信息
		
		String voiceId = voice.get("VOICE_ID");
		String voiceDesc = voice.get("VOICE_DESC");
		
		//修改之前，先检查是否已经存在相同的语音名称
		Voice checkVoice = Voice.dao.getVoiceByVoiceDesc(voiceDesc);
		if(!BlankUtils.isBlank(checkVoice)) {
			String vId = checkVoice.get("VOICE_ID");
			if(!BlankUtils.isBlank(vId) && !vId.equalsIgnoreCase(voiceId)) {
				render(RenderJson.error("修改语音失败!已存在相同的任务名字!"));
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
			Voice oldVoiceInfo = Voice.dao.getVoiceByVoiceId(voice.get("VOICE_ID").toString());
			
			//先删除旧的文件
			fileDelete(oldVoiceInfo);
			
			voice.set("FILE_NAME",renameFileName);
			voice.set("MIME_TYPE",mimeType);
			
		}
		
		boolean b = Voice.dao.update(voice.getStr("VOICE_DESC"),voice.getStr("VOICE_TYPE"),voice.getStr("FILE_NAME"), voice.getStr("MIME_TYPE"), voice.getStr("VOICE_ID"));
		
		if(b) {
			render(RenderJson.success("修改语音成功!"));
		}else {
			render(RenderJson.error("修改语音失败!"));
		}
	}

}
