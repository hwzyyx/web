package com.callke8.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.jfinal.plugin.activerecord.Record;

public class XLSUtils {
	
	public static List<Record> readXls(File file) {
	
		List<Record> list = new ArrayList<Record>();   //定义一个 list
	
		try {
			
			Workbook wb = WorkbookFactory.create(new FileInputStream(file));
			
			//HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));    //读取文件流，并创建workbook
			
			int sheetCount = wb.getNumberOfSheets();                          //得到工作表的数量，暂时没有用上
			
			for(int i=0; i<sheetCount; i++) {                                 //循环 sheet 
				if(i>0) {                                  //只读取第一个工作表
					break;  
				}
				Sheet sheet = wb.getSheetAt(i);       //得到第一张工作表
				
				int rowCount = sheet.getLastRowNum();     //得到当前工作表的总行数
				
				for(int j=0;j<=rowCount;j++) {                                 //循环行 row
					
					Row row = sheet.getRow(j);
					if(BlankUtils.isBlank(row)) {                      //如果当前行为空时，跳过循环
						continue;
					}
					
					Record record = new Record();     //定义一个 record
					for(int k=0; k<=row.getLastCellNum();k++) {                //循环 Cell
						Cell cell = row.getCell(k);
						if(BlankUtils.isBlank(cell)) {     //如果为空时
							record.set(String.valueOf(k),"");
							continue;
						}
						String value = getValue(cell);
						record.set(String.valueOf(k), value);
					}
					list.add(record);
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
	public static String getValue(Cell cell) {
		if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {     //如果数据类型为 0,表示这个数据是  numeric 类型
			String rs = String.valueOf(cell.getNumericCellValue());    //返回的，有可能是科学记数法，需要将转换成正常的字符串
			BigDecimal bd = new BigDecimal(rs);
			
			String value = bd.toPlainString();     //得到结果，但是可能结果会以 XXX.0 结尾
			
			if(StringUtil.containsAny(value, ".0") && value.endsWith(".0")) {       //如果结果包括 .0 ,且是以 .0 结尾时
				value = value.replace(".0", "");         //将 .0 替换为空即可
			}
			return value;
		}else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {   
			return String.valueOf(cell.getBooleanCellValue());
		}else {
			return String.valueOf(cell.getStringCellValue());
		}
	}
	
	
}
