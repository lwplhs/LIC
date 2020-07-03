

function lic_add(title,url){
    layer.open({
        type: 2,
        /*shadeClose: true,*/
        fixed: false, //不固定
        maxmin: false, //开启最大化最小化按钮
        area: ['800px', '500px'],
        title: title,
        content: url,
        end: function () {
            loadData();
        }
    });
}



$(function () {
    //loadData();
    getPage();
});
var page = 1;
var limit = 10;
var total;
function loadData() {

    var logMin = $("#logMin").val();
    var logMax = $("#logMax").val();
    var unitName = $("#unitName").val();
    var code = $("#code").val();

    NProgress.start();
    var url = "/lic/list";
    $.ajax({
        url: url,
        type: 'POST',
        data:{
            "pageNum":page,
            "limit":limit,
            "logMin":logMin,
            "logMax":logMax,
            "unitName":unitName,
            "code":code,
        },
        success: function (data) {
            $(".lic_list").html(data);
            NProgress.done();
        }
    });
}
function getPage() {
    total=$("#total").val();
    console.log(total);
    layui.use('laypage', function(){
        var laypage = layui.laypage;
        //执行一个laypage实例
        laypage.render({
            elem: 'laypage' //注意，这里的 test1 是 ID，不用加 # 号
            ,count: total, //数据总数，从服务端得到
            limit:limit,   //每页条数设置
            jump: function(obj, first){
                //obj包含了当前分页的所有参数，比如：
                //console.log(obj.curr); //得到当前页，以便向服务端请求对应页的数据。
                //console.log(obj.limit); //得到每页显示的条数
                page=obj.curr;  //改变当前页码
                limit=obj.limit;
                total = $("#total").val();
                //首次不执行
                if(!first){
                    loadData()  //加载数据
                }
            }
        });
    });
}

function deleteLic(obj) {
    var id =  $(obj).parent().parent().find("[name='id']").val();
    lic_del(id);
}

function datadel() {
    var ids = "";
    $("input:checkbox[name='checkbox']:checked").each(function (i) {
        if(0 == i){
            ids = $(this).parent().parent().find("[name='id']").val();
        }else {
            ids +=(","+$(this).parent().parent().find("[name='id']").val());
        }
    });
    lic_del(ids);
}

/*-删除*/
function lic_del(ids){
    layer.confirm('确认要删除吗？',function(index){
        $.ajax({
            type: 'POST',
            url: '/lic/licDelete',
            data:{
                ids:ids,
            },
            dataType: 'json',
            success: function(data){
                layer.msg(data.msg);
                if(data && data.success){
                    setTimeout(function () {
                        loadData();
                    },100);
                }
            },
            error:function(data) {
                layer.alert(data.msg);
            },
        });
    });
}

function downloadLic(obj) {
    var id =  $(obj).parent().parent().find("[name='id']").val();
    window.open("/lic/downloadLicense?id="+id);
}

/**
 * 清除查询条件
 */
function clearLic(){
    $("#logMin").val("");
    $("#logMax").val("");
    $("#unitName").val("");
    $("#code").val("");
}

/**
 * 按照条件搜索
 */
function searchLic() {
    page = 1;
    loadData();
}