<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<div style="vertical-align: bottom;position:absolute;right:200px;bottom:3px;">
	<img src="themes/ctiicons/dialout.png" style="width:30px;height:30px" onclick="doCti(1)" /><span style="color:#62C7FB">外呼</span>
	<img src="themes/ctiicons/holdon.png" style="width:30px;height:30px" onclick="doCti(2)" /><span style="color:#62C7FB">保持</span>
	<img src="themes/ctiicons/transfer.png" style="width:30px;height:30px" onclick="doCti(3)" /><span style="color:#62C7FB">转移</span>
	<img src="themes/ctiicons/busy.png" style="width:30px;height:30px" onclick="doCti(4)" /><span style="color:#62C7FB">示忙</span>
	<img src="themes/ctiicons/hangup.png" style="width:30px;height:30px" onclick="doCti(5)" class="cti_hangup" onmouseover="this.src='themes/ctiicons/hangup_red.png'" onmouseout="this.src='themes/ctiicons/hangup.png'" /><span style="color:#62C7FB">挂机</span>
</div>