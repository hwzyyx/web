<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="easyui-panel" title="" style="height:90%;padding:10px;" data-options="fit:true">
	<div class="easyui-layout" style="width:450px;height:600px;" data-options="fit:true">
		<div data-options="region:'west',split:true" style="width:450px;height:600px;padding:10px">
			<table>
				<tr>
					<td>
						<div style="padding-top:10px;">
							插入固定文本
						</div>
						<div style="padding-top:10px;">
							<a href="#" class="easyui-linkbutton c8" onclick="insertText()" style="width:80px;">固定文本</a>
						</div>
						<div style="padding-top:10px;">
							插入参数：
						</div>
						<div style="padding-top:10px;">
							<a href="#" class="easyui-linkbutton c1" onclick="insertText()" style="width:80px;">文字参数</a>
							<a href="#" class="easyui-linkbutton c3" onclick="insertNumber()" style="width:80px;margin-left:10px;">数字参数</a>
							<a href="#" class="easyui-linkbutton c4" onclick="insertNumber()" style="width:80px;margin-left:10px;">金额参数</a>
							<a href="#" class="easyui-linkbutton c5" onclick="insertDate()" style="width:80px;margin-left:10px;">日期参数</a>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div style="margin:20px 0;"></div>
						<div style="border:1px solid #ccc;width:400px;height:400px;float:left;margin-top:20px;">
							<ul style="margin:0px;padding:0px;margin-left:0px;">
								<li class="drag-item">中华人民共和国中华人民共和国中华人民共和国中</li>
								<li class="drag-item">Drag 2</li>
								<li class="drag-item">Drag 3</li>
								<li class="drag-item">Drag 4</li>
								<li class="drag-item">Drag 5</li>
								<li class="drag-item">Drag 6</li>
							</ul>
						</div>
					</td>
				</tr>
			</table>
		</div>
		<div data-options="region:'center'" style="width:100px;padding:10px">
			Right Content
		</div>
	</div>
</div>
<style type="text/css">
	.drag-item{
		list-style-type:none;
		display:block;
		padding:5px;
		border:1px solid #ccc;
		margin:2px;
		width:385px;
		background:#fafafa;
		color:#444;
	}
	.indicator{
		position:absolute;
		font-size:9px;
		width:10px;
		height:10px;
		display:none;
		color:red;
	}
</style>
<script>
	$(function(){
		var indicator = $('<div class="indicator">>></div>').appendTo('body');
		$('.drag-item').draggable({
			revert:true,
			deltaX:0,
			deltaY:0
		}).droppable({
			onDragOver:function(e,source){
				indicator.css({
					display:'block',
					left:$(this).offset().left-10,
					top:$(this).offset().top+$(this).outerHeight()-5
				});
			},
			onDragLeave:function(e,source){
				//indicator.hide();
			},
			onDrop:function(e,source){
				$(source).insertAfter(this);
				indicator.hide();
			}
		});
	});
</script>
