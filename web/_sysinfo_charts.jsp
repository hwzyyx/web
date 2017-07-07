<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<style type="text/css">
	
	#P-Panel .portal {
		padding: 0;
		margin: 0;
		border: 1px solid #99BBE8;
		overflow: auto
	}
	
	#P-Panel .portal-noborder {
		border: 0;
	}
	
	#P-Panel .portal-panel {
		margin-bottom: 10px;
	}
	
	#P-Panel .portal-column-td {
		vertical-align: top;
	}
	
	#P-Panel .portal-column {
		padding: 10px 0 10px 10px;
		overflow: hidden;
	}
	
	#P-Panel .portal-column-left {
		padding-left: 10px;
	}
	
	#P-Panel .portal-column-right {
		padding-right: 10px;
	}
	
	#P-Panel .portal-proxy {
		background-color: #0e2d5f;
		opacity: 0.6;
		filter: alpha(opacity = 60);
	}
	
	#P-Panel .portal-spacer {
		border: 3px dashed #eee;
		margin-bottom: 10px;
	}

</style>


<script type="text/javascript">

function systemResourceRefresh() {
	
	initSystemResourceChart();
	
}


function initSystemResourceChart() {

	cpuResourceChart = echarts.init(document.getElementById('cpuResourceChartDiv'),'macarons');
	
	var cpuOption = {
		tooltip : {
			formatter: "{a} <br/>{b} : {c}%"
		},
		toolbox: {
			show : true,
			feature : {
				mark : {show: true}
				//restore : {show: true},
				//saveAsImage : {show: true}
			}
		},
		series : [
			{
				name:'CPU使用情况',
				type:'gauge',
				detail : {
					formatter:'{value}%',
					textStyle:{fontSize:18},
					offsetCenter:[0,'70%']
				},
				//data:[{value: 2, name: ''}]
				data:[]
			}
		]
	};
	
	cpuResourceChart.setOption(cpuOption);

	ramResourceChart = echarts.init(document.getElementById('ramResourceChartDiv'),'macarons');
	
	var ramOption = {
		tooltip : {
			formatter: "{a} <br/>{b} : {c}%"
		},
		toolbox: {
			show : true,
			feature : {
				mark : {show: true}
				//restore : {show: true},
				//saveAsImage : {show: true}
			}
		},
		series : [
			{
				name:'内存使用情况',
				type:'gauge',
				detail : {
					formatter:'{value}%',
					textStyle:{fontSize:18},
					offsetCenter:[0,'70%']
				},
				//data:[{value: 20, name: ''}]
				data:[]
			}
		]
	};
	
	ramResourceChart.setOption(ramOption);

}

setInterval(function(){


	//定义了图表之后，对系统资源进行加载数据	
	/*$.messager.progress({
		msg:'系统正在处理，请稍候...',
		interval:3000
	});*/

	$.ajax({

		url:'systemResource/getCpuRamResourceData',
		method:'post',
		dataType:'json',
		success:function(rs) {
			//$.messager.progress('close');
			var cpuValue = rs.cpuValue;
			var ramValue = rs.ramValue;
			var cpuInfo = rs.cpuInfo;
			var upTime = rs.upTime;
			var cpuSpeed = rs.cpuSpeed;
			var memoryUsage = rs.memoryUsage;

			//(1) cpu数据赋值
			var cpuResourceData=[];

			var cpuMap = {};
			cpuMap.name = '';
			cpuMap.value = cpuValue;
			cpuResourceData[0] = cpuMap;

			cpuResourceChart.setOption({
				series:[{
					data:cpuResourceData	
				}]
			});
			//(2) ram 数据赋值
			var ramResourceData=[];

			var ramMap = {};
			ramMap.name = '';
			ramMap.value = ramValue;
			ramResourceData[0] = ramMap;

			//(3) 系统文字信息输出
			$("#cpuInfo").html("CPU Info: " + cpuInfo);
			$("#upTime").html("Uptime: " + upTime);
			$("#cpuSpeed").html("CPU Speed: " + cpuSpeed);
			$("#memoryUsage").html("Memory Usage: " + memoryUsage);
			

			ramResourceChart.setOption({
				series:[{
					data:ramResourceData
				}]
			});
			
		}
		
	});

	
},3000);

(function ($) {
	
	function init(target) {
		$(target).addClass('portal');
		var table = $('<table border="0" cellspacing="0" cellpadding="0"><tr></tr></table>').appendTo(target);
		var tr = table.find('tr');
	
		var columnWidths = [];
		var totalWidth = 0;
		$(target).children('div:first').addClass('portal-column-left');
		$(target).children('div:last').addClass('portal-column-right');
		$(target).find('>div').each(function () {	// each column panel
		var column = $(this);
		totalWidth += column.outerWidth();
		columnWidths.push(column.outerWidth());
	
		var td = $('<td class="portal-column-td"></td>').appendTo(tr)
		column.addClass('portal-column').appendTo(td);
		column.find('>div').each(function () {	// each portal panel
		var p = $(this).addClass('portal-p').panel({
		doSize: false,
		cls: 'portal-panel'//,
		//onClose: function () { $(this).panel("destroy"); }
		});
		var opts = p.panel("options"), onClose = opts.onClose;
		opts.onClose = function () {
		if ($.isFunction(onClose)) { onClose.apply(this, arguments); }
		$(this).panel("destroy");
		};
		makeDraggable(target, p);
		});
		});
		for (var i = 0; i < columnWidths.length; i++) {
		columnWidths[i] /= totalWidth;
		}
	
		$(target).bind('_resize', function () {
		var opts = $.data(target, 'portal').options;
		if (opts.fit == true) {
		setSize(target);
		}
		return false;
		});
	
		return columnWidths;
	}

	function setSize(target) {
		var t = $(target);
		var opts = $.data(target, 'portal').options;
		if (opts.fit) {
			var p = t.parent();
			opts.width = p.width();
			opts.height = p.height();
		}
		if (!isNaN(opts.width)) {
			t._outerWidth(opts.width);
		} else {
			t.width('auto');
		}
		if (!isNaN(opts.height)) {
			t._outerHeight(opts.height);
		} else {
			t.height('auto');
		}
	
		var hasScroll = t.find('>table').outerHeight() > t.height();
		var width = t.width();
		var columnWidths = $.data(target, 'portal').columnWidths;
		var leftWidth = 0;
	
		// calculate and set every column size
		for (var i = 0; i < columnWidths.length; i++) {
			var p = t.find('div.portal-column:eq(' + i + ')');
			var w = Math.floor(width * columnWidths[i]);
			if (i == columnWidths.length - 1) {
				//w = width - leftWidth - (hasScroll == true ? 28 : 10);
				w = width - leftWidth - (hasScroll == true ? 18 : 0);
			}
			p._outerWidth(w);
			leftWidth += p.outerWidth();
		
			// resize every panel of the column
			p.find('div.portal-p').panel('resize', { width: p.width() });
		}
		opts.onResize.call(target, opts.width, opts.height);
	}

	function makeDraggable(target, panel) {
		var spacer;
		panel.panel('panel').draggable({
			handle: '>div.panel-header>div.panel-title',
			proxy: function (source) {
				var p = $('<div class="portal-proxy">proxy</div>').insertAfter(source);
				p.width($(source).width());
				p.height($(source).height());
				p.html($(source).html());
				p.find('div.portal-p').removeClass('portal-p').hide();
				return p;
			},
			onBeforeDrag: function (e) {
				e.data.startTop = $(this).position().top + $(target).scrollTop();
			},
			onStartDrag: function (e) {
				$(this).hide();
				spacer = $('<div class="portal-spacer"></div>').insertAfter(this);
				setSpacerSize($(this).outerWidth(), $(this).outerHeight());
			},
			onDrag: function (e) {
				var p = findPanel(e, this);
				if (p) {
					if (p.pos == 'up') {
						spacer.insertBefore(p.target);
					} else {
						spacer.insertAfter(p.target);
					}
					setSpacerSize($(p.target).outerWidth());
				} else {
					var c = findColumn(e);
					if (c) {
						if (c.find('div.portal-spacer').length == 0) {
							spacer.appendTo(c);
							setSize(target);
							setSpacerSize(c.width());
						}
					}
				}
			},
			onStopDrag: function (e) {
				$(this).css('position', 'static');
				$(this).show();
				spacer.hide();
				$(this).insertAfter(spacer);
				spacer.remove();
				setSize(target);
				panel.panel('move');
			
				var opts = $.data(target, 'portal').options;
				opts.onStateChange.call(target);
			}
		});
	
		/**
		* find which panel the cursor is over
		*/
		function findPanel(e, source) {
			var result = null;
			$(target).find('div.portal-p').each(function () {
				var pal = $(this).panel('panel');
				if (pal[0] != source) {
					var pos = pal.offset();
					if (e.pageX > pos.left && e.pageX < pos.left + pal.outerWidth()
					&& e.pageY > pos.top && e.pageY < pos.top + pal.outerHeight()) {
						if (e.pageY > pos.top + pal.outerHeight() / 2) {
							result = {
								target: pal,
								pos: 'down'
							};
						} else {
							result = {
								target: pal,
								pos: 'up'
							}
						}
					}
				}
			});
			return result;
		}
	
		/**
		* find which portal column the cursor is over
		*/
		function findColumn(e) {
			var result = null;
			$(target).find('div.portal-column').each(function () {
				var pal = $(this);
				var pos = pal.offset();
				if (e.pageX > pos.left && e.pageX < pos.left + pal.outerWidth()) {
					result = pal;
				}
			});
			return result;
		}
	
		/**
		* set the spacer size
		*/
		function setSpacerSize(width, height) {
			spacer._outerWidth(width);
			if (height) {
				spacer._outerHeight(height);
			}
		}
	}


	$.fn.portal = function (options, param) {
		if (typeof options == 'string') {
			return $.fn.portal.methods[options](this, param);
		}
		options = options || {};
		return this.each(function () {
			var state = $.data(this, 'portal');
			if (state) {
				$.extend(state.options, options);
			} else {
				state = $.data(this, 'portal', {
					options: $.extend({}, $.fn.portal.defaults, $.fn.portal.parseOptions(this), options),
					columnWidths: init(this)
				});
			}
			if (state.options.border) {
				$(this).removeClass('portal-noborder');
			} else {
				$(this).addClass('portal-noborder');
			}
			setSize(this);
		});
	};

	$.fn.portal.methods = {
		options: function (jq) {
			return $.data(jq[0], 'portal').options;
		},
		resize: function (jq, param) {
			return jq.each(function () {
				if (param) {
					var opts = $.data(this, 'portal').options;
					if (param.width) opts.width = param.width;
					if (param.height) opts.height = param.height;
				}
				setSize(this);
			});
		},
		getPanels: function (jq, columnIndex) {
			var c = jq; // the panel container
			if (columnIndex >= 0) {
				c = jq.find('div.portal-column:eq(' + columnIndex + ')');
			}
			var panels = [];
			c.find('div.portal-p').each(function () {
				panels.push($(this));
			});
			return panels;
		},
		add: function (jq, param) {	// param: {panel,columnIndex}
			return jq.each(function () {
				if (!param || !$.isNumeric(param.columnIndex) || !param.panel) { return; }
				var portal = $(this), opts = portal.portal("options");
				if (opts.onBeforeAdd.call(this, param.columnIndex, param.panel) == false) { return; }
				var c = portal.find('div.portal-column:eq(' + param.columnIndex + ')');
				var p = param.panel.addClass('portal-p');
				p.panel('panel').addClass('portal-panel').appendTo(c);
				makeDraggable(this, p);
				p.panel('resize', { width: c.width() });
				var panelOpts = p.panel("options"), onClose = panelOpts.onClose;
				panelOpts.onClose = function () {
				if ($.isFunction(onClose)) { onClose.apply(this, arguments); }
				if (!p.length) { return; }
				var body = $("body");
				if ($.contains(body[0], p[0])) { p.panel("destroy"); }
				}
				opts.onAdd.call(this, param.columnIndex, param.panel);
			});
		},
		remove: function (jq, panel) {
			return jq.each(function () {
				var p = $(this), opts = p.portal("options");
				if (opts.onBeforeRemove.call(this, panel) == false) { return; }
				var panels = p.portal('getPanels'), panelOpts = panel.panel("options");
				for (var i = 0; i < panels.length; i++) {
					var p = panels[i];
					if (p[0] == $(panel)[0]) {
						p.panel('destroy');
					}
				}
				opts.onRemove.call(this, panelOpts);
			});
		},
		addColumn: function (jq) {
			return jq.each(function () {
				var state = $.data(this, "portal"), opts = state.options, columnWidths = state.columnWidths,
				portal = $(this), totalWidth = portal.outerWidth(),
				tr = portal.find(">table tr"), td = $("<td></td>").addClass("portal-column-td").appendTo(tr),
				column = $("<div></div>").addClass("portal-column").appendTo(td),
				width = parseFloat(1) / (columnWidths.length + 1);
				if (opts.onBeforeAddColumn.call(this, columnWidths.length) == false) { return; }
				for (var i = 0; i < columnWidths.length; i++) { columnWidths[i] = width; }
				columnWidths.push(width);
				tr.find(">td>div.portal-column").removeClass("portal-column-left portal-column-right");
				tr.find(">td>div.portal-column:first").addClass("portal-column-left");
				tr.find(">td>div.portal-column:last").addClass("portal-column-right");
				setSize(this);
				opts.onAddColumn.call(this, columnWidths.length - 1);
			});
		},
		removeColumn: function (jq, index) {
			return jq.each(function () {
				var state = $.data(this, "portal"), opts = state.options, columnWidths = state.columnWidths;
				if (!$.isNumeric(index) || !columnWidths || !columnWidths.length || index > columnWidths.length - 1) { return; }
				if (opts.onBeforeRemoveColumn.call(this, index) == false) { return; }
				var tr = $(this).find(">table tr"), width = parseFloat(1) / (columnWidths.length - 1);
				columnWidths.pop();
				for (var i = 0; i < columnWidths.length; i++) { columnWidths[i] = width; }
				tr.find(">td:eq(" + index + ")").remove();
				tr.find(">td>div.portal-column").removeClass("portal-column-left portal-column-right");
				tr.find(">td>div.portal-column:first").addClass("portal-column-left");
				tr.find(">td>div.portal-column:last").addClass("portal-column-right");
				setSize(this);
				opts.onRemoveColumn.call(this, index);
			});
		},
		columns: function (jq) {
			return $.data(jq[0], "portal").columnWidths.length;
		},
		disableDragging: function (jq, panel) {
			panel.panel('panel').draggable('disable');
			return jq;
		},
		enableDragging: function (jq, panel) {
			panel.panel('panel').draggable('enable');
			return jq;
		}
	};

	$.fn.portal.parseOptions = function (target) {
		return $.extend({}, $.parser.parseOptions(target, ["width", "height", { border: "boolean", fit: "boolean" }]));
	};

	$.fn.portal.defaults = {
		width: 'auto',
		height: 'auto',
		border: true,
		fit: false,
		onResize: function (width, height) { },
		onStateChange: function () { },
	
		onBeforeAdd: function (columnIndex, panel) { },
		onAdd: function (columnIndex, panel) { },
	
		onBeforeAddColumn: function (columnIndex) { },
		onAddColumn: function (columnIndex) { },
	
		onBeforeRemove: function (panel) { },
		onRemove: function (panelOption) { },
	
		onBeforeRemoveColumn: function (columnIndex) { },
		onRemoveColumn: function (columnIndex) { }
	};

	$.parser.plugins.push("portal");

	})(jQuery);

</script>

<div id="P-Panel" class="easyui-layout" data-options="fit: true">
	<div data-options="region: 'center', border: false,fit: true"
		style="overflow: hidden;">
		<div id="portal" class="easyui-portal"
			data-options="border: false">
			<div>
				<div id="systemResourcePanel"
					data-options="title: '系统资源',height:350,collapsible: false, closable: false, tools:'#systemResourceTool'">
					
					<table border="1" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff"
						  style="border-collapse: collapse;height:300px;">
						  
						  <tr style="height: 240px;">
						  	 <td style="width:260px;">
								<div id="cpuResourceChartDiv" style="width:230px;height:230px;border"></div>
						  	 </td>
						  	 <td style="width:260px;">
								<div id="ramResourceChartDiv" style="width:230px;height:230px;border"></div>
						  	 </td>
						  	 <td>
						  	 	<div id="cpuInfo"></div>
						  	 	<div id="upTime"></div>
						  	 	<div id="cpuSpeed"></div>
						  	 	<div id="memoryUsage"></div>
						  	 </td>
						  </tr>
						  <tr>
						  	 <td align="center">
						  	 	<span style="font-weight:bolder;">CPU</span>
						  	 </td>
						  	 <td align="center">
						  	 	<span style="font-weight:bolder;">RAM</span>
						  	 </td>
						  	 <td>
						  	 </td>
						  </tr>
						  
					</table>
					
				</div>
				<div
					data-options="title: '功能简介',height:350,collapsible: false, closable: false, tools: [{ iconCls: 'icon-reload' }]">
				</div>
			</div>
			
			
			<div>
				<div
					data-options="title: '排队机信息',height:350,collapsible: false, closable: false, tools: [{ iconCls: 'icon-reload' }]">
					
					<div id="queueChartDiv" style="width:600px;height:600px;"></div>
					
				</div>
				<div
					data-options="title: '操作日志',height:350,collapsible: false, closable: false, tools: [{ iconCls: 'icon-reload' }]">
				</div>
			</div>
			
		</div>
	</div>
</div>


<div id="systemResourceTool" style="padding:5px;">
	<a href="javascript:void(0)" class="icon-reload" onclick="javascript:systemResourceRefresh()"></a>
</div>





























