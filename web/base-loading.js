function closes() {
    $("#Loading").fadeOut("normal", function () {

        $(this).remove();
        alert("数据加载完成");
    });
}
var pc;
$.parser.onComplete = function () {
    if (pc) {
        clearTimeout(pc);
    }
    pc = setTimeout(closes, 1000);

}











