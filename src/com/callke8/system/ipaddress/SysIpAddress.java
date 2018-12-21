package com.callke8.system.ipaddress;

import java.util.*;

import com.callke8.system.callerid.SysCallerIdConfig;
import com.callke8.system.operator.Operator;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class SysIpAddress extends Model<SysIpAddress>  {

	private static final long serialVersionUID = 1L;
	public static SysIpAddress dao = new SysIpAddress();

	public Page getSysIpAddressByPaginate(int pageNumber,int pageSize,String ipAddress,String memo) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from sys_ip_address where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(ipAddress)) {
			sb.append(" and IP_ADDRESS like ?");
			pars[index] = "%" + ipAddress + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(memo)) {
			sb.append(" and MEMO like ?");
			pars[index] = "%" + memo + "%";
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getSysIpAddressByPaginateToMap(int pageNumber,int pageSize,String ipAddress,String memo) {

		Page<Record> p =  getSysIpAddressByPaginate(pageNumber,pageSize,ipAddress,memo);

		int total = p.getTotalRow();     //取出总数量
		ArrayList<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			//设置操作员名字（工号）
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			newList.add(r);
		}

		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);

		return map;
	}
	public boolean add(SysIpAddress formData) {

		Record r = new Record();
		r.set("IP_ADDRESS", formData.get("IP_ADDRESS"));
		r.set("MEMO", formData.get("MEMO"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());

		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("sys_ip_address", "ID", record);
		if(b) {
			loadSysIpAddressToMemory();
		}
		return b;

	}

	public boolean update(String ipAddress,String memo,int id) {

		boolean b = false;
		String sql = "update sys_ip_address set IP_ADDRESS=?,MEMO=? where ID=?";

		int count = Db.update(sql,ipAddress,memo,id);
		if(count > 0) {
			b = true;
			loadSysIpAddressToMemory();
		}
		return b;

	}

	public SysIpAddress getSysIpAddressById(int id){

		String sql = "select * from sys_ip_address where ID=?";
		SysIpAddress entity = findFirst(sql, id);
		return entity;

	}
	
	//取出所有主叫号码的列表
	public List<Record> getAllSysIpAddress() {
		
		String sql = "select * from sys_ip_address order by ID asc";
		
		List<Record> list  = Db.find(sql);
		
		return list;
	}
	
	public SysIpAddress getSysIpAddressByIpAddress(String ipAddress) {
		
		String sql = "select * from sys_ip_address where IP_ADDRESS=?";
		
		SysIpAddress entity = findFirst(sql,ipAddress);
		
		return entity;
		
	}
	
	public SysIpAddress getSysIpAddressByMemo(String memo) {
		
		String sql = "select * from sys_ip_address where MEMO=?";
		
		SysIpAddress entity = findFirst(sql,memo);
		
		return entity;
		
	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from sys_ip_address where ID=?",id);
		if(count > 0) {
			b = true;
			loadSysIpAddressToMemory();
		}
		return b;

	}
	
	/**
	 * 加载 IP地址到内存
	 */
	public void loadSysIpAddressToMemory() {
		
		List<Record> sysIpAddressList = getAllSysIpAddress();
		
		if(BlankUtils.isBlank(sysIpAddressList)) {
			System.out.println("警告：=======-加载IP地址数据到内存失败,sys_ip_address 表数据为空,请添加数据后,再重新启动进行加载!");
			return;
		}
		
		/**
		 * 初始化原来的配置
		 */
		SysIpAddressConfig.ipAddressList = new ArrayList<String>();
		for(Record sysIpAddress:sysIpAddressList) {
			SysIpAddressConfig.ipAddressList.add(sysIpAddress.getStr("IP_ADDRESS"));
		}
		
		int i = 1;
		System.out.println("系统加载 IP地址到内存，配置如下：");
		for(String ipAddress:SysIpAddressConfig.ipAddressList) {
			System.out.println("IP地址" + i + ":" + ipAddress);
			i++;
		}
		
	}
}
