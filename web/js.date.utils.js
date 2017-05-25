
var weekArr = new Array();
weekArr[0] = "星期一";
weekArr[1] = "星期二";
weekArr[2] = "星期三";
weekArr[3] = "星期四";
weekArr[4] = "星期五";
weekArr[5] = "星期六";
weekArr[6] = "星期日";


//得到当前日期
function getCurrDate() {      
	var dateString = "";
	var myDate = new Date();
	dateString += myDate.getFullYear() + "-";
	dateString += (myDate.getMonth()+1) + "-";
	dateString += myDate.getDate();
	return dateString;			
}

//得到当月第1天的日期
function getCurrMonthDay1() {   
	var dateString = "";
	var myDate = new Date();
	dateString += myDate.getFullYear() + "-";
	dateString += (myDate.getMonth()+1) + "-";
	dateString += "01";
	return dateString;
}

//得到N天后的日期
function getDateAfter(n) {
	var dateString = "";
	
	var nowDate = new Date();   //得到当前日期对象
	
	var newDate = new Date(nowDate.getTime() + n * 24 * 3600 * 1000);
	
	dateString += newDate.getFullYear() + "-";
	dateString += (newDate.getMonth()+1) + "-";
	dateString += newDate.getDate();
	
	return dateString;
}


//得到N天前的日期
function getDateBefore(n) {
	
	var dateString = "";
	
	var nowDate = new Date();   //得到当前日期对象
	
	var newDate = new Date(nowDate.getTime() - 7 * 24 * 3600 * 1000);
	
	dateString += newDate.getFullYear() + "-";
	dateString += (newDate.getMonth()+1) + "-";
	dateString += newDate.getDate();
	
	return dateString;
}

/**
 * 根据传入的数字，输出星期的日期
 * 0：星期一
 * 1：星期二
 * 。。。
 * 如此类推
 * 
 * @param index
 * @return
 */
function getWeekDay(index) {
	
	return weekArr[index];
	
}














