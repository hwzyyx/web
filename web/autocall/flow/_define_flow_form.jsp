<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<style type="text/css">
		.one{
		    width: 450px;
		    height: 40px;
		    margin: 2px;
		}
		.boxList{
		    border: 1px solid #ff0033;
		    height: 550px;
		    /*margin: 30px;*/
		    position: relative;
		    width: 500px;
		}
		
		.tezml{
		    border: 3px #ff00ff dashed;
		}
		
		.div8{
		    float: left;
		}
	
</style>
<!-- 
<div class="box">
    
    <div class="example_one">
       
        <div class="example">
 -->
            <div class="boxList">
                <div class="one div8"><input type="button" value="第一个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第二个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第三个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第四个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第五个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第六个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第七个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第八个" style="width:450px;"/></div>
                <div class="one div8"><input type="button" value="第九个" style="width:450px;"/></div>
            </div>
<!-- 
        </div>
    </div>

</div>
 -->
  <script type="text/javascript">
    $(function(){
    	$(".div8").Tdrag({
    	    scope:".boxList",
    	    pos:true,
    	    dragChange:true
    	});

    })
    
    
    function addNode() {
    	$("#father").prepend("<div class='lanren' style='right:50px;'></div>");
    	$(".lanren").Tdrag();
    }
  </script>
  <!-- 代码部分end -->