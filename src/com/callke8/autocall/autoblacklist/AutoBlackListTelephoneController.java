package com.callke8.autocall.autoblacklist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TxtUtils;
import com.callke8.utils.XLSUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class AutoBlackListTelephoneController extends Controller implements IController {

	@Override
	public void datagrid() {
		System.out.println("取AutoBlackListTelephoneController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String blackListId = getPara("blackListId");
		String telephone = getPara("telephone");
		String clientName = getPara("clientName");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoBlackListTelephone.dao.getAutoBlackListTelephoneByPaginateToMap(pageNumber, pageSize, blackListId, telephone, clientName);
		System.out.println("取AutoBlackListTelephoneController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}
	
	@Override
	public void add() {
		String blackListId = getPara("blackListId");
		AutoBlackListTelephone ablt = getModel(AutoBlackListTelephone.class,"autoBlackListTelephone");
		
		Record autoBlackListTelephone = new Record();
		
		autoBlackListTelephone.set("BLACKLIST_ID", blackListId);
		autoBlackListTelephone.set("TELEPHONE", ablt.get("TELEPHONE"));
		autoBlackListTelephone.set("CLIENT_NAME",ablt.get("CLIENT_NAME"));
		
		boolean b = AutoBlackListTelephone.dao.add(autoBlackListTelephone);
		
		if(b) {
			render(RenderJson.success("新增号码成功!"));
		}else {
			render(RenderJson.error("新增号码失败!"));
		}
	}


	@Override
	public void delete() {
		
		String ids = getPara("ids");      //要删除的号码的ID
		
		int count = AutoBlackListTelephone.dao.batchDelete(ids);
		
		render(RenderJson.success("成功删除数据量为：" + count));
	}

	@Override
	public void index() {
		
	}

	@Override
	public void update() {
		
		AutoBlackListTelephone ablt = getModel(AutoBlackListTelephone.class,"autoBlackListTelephone");
		
		if(BlankUtils.isBlank(ablt)) {
			render(RenderJson.error("要修改的号码信息为空,修改失败!"));
		}
		
		String telephone = ablt.get("TELEPHONE");
		String clientName = ablt.get("CLIENT_NAME");
		int telId = Integer.valueOf(ablt.get("TEL_ID").toString());
		
		
		boolean b = AutoBlackListTelephone.dao.update(telephone, clientName, telId);
		
		if(b) {
			render(RenderJson.success("修改号码成功!"));
		}else {
			render(RenderJson.error("修改号码失败!"));
		}
		
	}
	
	/**
	 * 上传号码文件添加号码
	 */
	public void uploadFile() {
		
		//先判断文件大小限制
		int fileSize = getRequest().getContentLength();   //得到上传文件的大小, 由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			render(RenderJson.error("上传失败：上传文件过大，已经超过 10M"));
			return;
		}
		
		//获取上传的文件
		//(1)定义上传路径,这种上传号码,都上传到 upload 这个文件夹中
		String uploadDir = PathKit.getWebRootPath() + File.separator + "upload" + File.separator;
		//(2)执行上传操作
		UploadFile uf = getFile("telephoneFile",uploadDir);
		
		//判断上传的文件的类型
		String mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//判断文件的类型,是否为 txt、xls 或是 xlsx
		if(BlankUtils.isBlank(mimeType) || (!mimeType.equalsIgnoreCase("xls") && !mimeType.equalsIgnoreCase("xlsx") && !mimeType.equalsIgnoreCase("txt"))) {
			//提示错误之前，先将上传的文件删除
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists())  {
				uf.getFile().delete();
			}
			
			render(RenderJson.error("上传失败,上传的号码文件类型不正确,只支持 txt、xls 或是 xlsx"));
			return;
		}
		
		//上传的文件名有可能是中文名,不利于读取,需先重命名为数字名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File(uploadDir + renameFileName);
		
		uf.getFile().renameTo(newFile);
		
		String blackListId = getPara("blackListId");    //获取参数黑名单ID
		
		int count = insertTelephoneFormFile(newFile, mimeType, blackListId);   //成功插入的数量
		
		render(RenderJson.success("成功插入 " + count + " 个黑名单号码!"));
		
	}
	
	/**
	 * 从XLS读取号，并插入号码到数据库
	 * 
	 * @param file
	 * 			号码文件
	 * @return
	 */
	public int insertTelephoneFormFile(File file,String mimeType,String blackListId) {
	
		//把文件中的信息取出并放置到一个List中，Record以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {  //如果文件类型为txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);    
		}
		
		//新建一个 list，用于格式化成可以存储到数据库的 Record
		ArrayList<Record> listResult = new ArrayList<Record>(); 
		
		for(int i=0;i<list.size();i++) {    //由于第0行为表头,固以1为开始
			
			Record r = list.get(i);
			
			String telephone = r.get("0");     //得到号码
			String clientName = r.get("1");    //得到客户姓名
			
			//取出两列，然后判断第一列是否是号码,且号码的长度是否大于等于7位数，否则将不储存
			if(!BlankUtils.isBlank(telephone) && StringUtil.isNumber(telephone) && telephone.length() >= 7) {
				
				if(BlankUtils.isBlank(clientName)) {   //如果客户姓名为空时，则号码即为客户号码
					clientName = telephone;
				}
				
				Record blackListTelephone = new Record();
				blackListTelephone.set("BLACKLIST_ID", blackListId);
				blackListTelephone.set("TELEPHONE", telephone);
				blackListTelephone.set("CLIENT_NAME",clientName);
				
				listResult.add(blackListTelephone);
			}else {   //否则，跳过
				continue;
			}		
		}
		
		int count = AutoBlackListTelephone.dao.batchSave(listResult);
		
		return count;
	}
	
	                                   

}
