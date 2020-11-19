$(function () {
    getPage();
});
var page = 1;
var limit = 10;
var total;
var searchKey;

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
            setTimeout(function () {
                //重新定义 页数 到首页
                page = 1;
                getPage();
            },200);
        }
    });
}



function loadData() {

    var searchKey = $("#search").val();

    NProgress.start();
    var url = "/lic/list";
    $.ajax({
        url: url,
        type: 'POST',
        data:{
            "pageNum":page,
            "limit":limit,
            "searchKey":searchKey
        },
        success: function (data) {
            $(".lic_list").html(data);
            NProgress.done();
            setTimeout(function () {
                /*
                                getMenu();
                */
                var total = $("#total").val();
                if(total == 0){
                    $("#laypage").attr("hidden","hidden");
                    $("#dataNull").removeAttr("hidden");
                }else {
                    $("#dataNull").attr("hidden","hidden");
                    $("#laypage").removeAttr("hidden");
                }
            },100);
        }
    });
}

function chooseAll(){
    var temp = $("#check1").is(':checked');
    $('input[name="checkbox"]').each(function () {
        if(temp){
            $(this).prop("checked","checked");
        }else {
            $(this).removeAttr("checked");
        }

    });
}
function getPage() {
    loadData();
    setTimeout(function () {
        total=$("#total").val();
        layui.use('laypage', function(){
            var laypage = layui.laypage;
            //执行一个laypage实例
            laypage.render({
                elem: 'laypage' //注意，这里的 test1 是 ID，不用加 # 号
                ,count: total, //数据总数，从服务端得到
                limit:limit,   //每页条数设置
                layout: ['count', 'prev', 'page', 'next', 'limit', 'skip'],
                jump: function(obj, first){
                    //首次不执行
                    if(!first){
                        page=obj.curr;  //改变当前页码
                        limit=obj.limit;
                        total = $("#total").val();
                        loadData();  //加载数据  //加载数据
                    }
                }
            });
        });
    },300);

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
                        //重新定义 页数
                        //获取当前页数
                        getTotal();
                        setTimeout(function () {
                            total = $("#total").val();
                            //判断当 前页面 是否存在
                            if(Math.ceil(total/limit) < page){
                                page = Math.ceil(total/limit);
                            }
                            getPage();
                        },100);
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
    getTotal();
    //loadData();
    setTimeout(function () {
        getPage();
    },200);
}

//获取总数
function getTotal() {
    var searchKey = $("#search").val();

    var url = "/lic/getTotal";
    $.ajax({
        url: url,
        type: 'POST',
        data:{
            "searchKey":searchKey
        },
        success: function (data) {
            $("#total").val(data);
        }
    });
}