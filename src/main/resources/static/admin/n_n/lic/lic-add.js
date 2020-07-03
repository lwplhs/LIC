<!--carousel-add页面业务相关的自定义脚本-->
function lic_save_submit(){
    var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
    var jsondata = $("#form-lic-add").serialize();
    //console.log(jsondata);
    $.ajax({
        type:"POST",
        data:jsondata,
        url:"/lic/saveLic",
        success: function(data){
            console.log(data.code)
            if(data && data.success){
                layer.msg(data.msg);
                setTimeout(function (){
                    parent.layer.close(index);
                },500);
            }else {
                layer.alert(data.msg || "请求失败，请刷新后重试");
            }
        }

    });
}

/**
 * 取消
 */
function quxiao(){
    var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
    parent.layer.close(index);
}

$(function(){
    //下拉框初始化
    if($("#isMac option:selected").val()=='0'){
        $("#customize").hide();
    }else {
        $("#customize").show();
    }
    /**
     * 下拉框选择
     *
     * */
    $("#isMac").change(function () {
        if($("#isMac option:selected").val()=='0'){
            $("#customize").hide();
        }else {
            $("#customize").show();
        }
    });

    $('.skin-minimal input').iCheck({
        checkboxClass: 'icheckbox-blue',
        radioClass: 'iradio-blue',
        increaseArea: '20%'
    });

});

