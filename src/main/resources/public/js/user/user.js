layui.use(['table','layer'],function(){
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        table = layui.table;
    /**
     * 用户列表展示
     */
    var  tableIns = table.render({
        elem: '#userList',
        url : ctx + '/user/list',
        cellMinWidth : 95,
        page : true,
        height : "full-125",
        limits : [10,15,20,25],
        limit : 10,
        toolbar: "#toolbarDemo",
        id : "userListTable",
        cols : [[
            {type: "checkbox", fixed:"left", width:50},
            {field: "id", title:'编号',fixed:"true", width:80},
            {field: 'userName', title: '用户名', minWidth:50, align:"center"},
            {field: 'email', title: '用户邮箱', minWidth:100, align:'center'},
            {field: 'phone', title: '用户电话', minWidth:100, align:'center'},
            {field: 'trueName', title: '真实姓名', align:'center'},
            {field: 'createDate', title: '创建时间', align:'center',minWidth:150},
            {field: 'updateDate', title: '更新时间', align:'center',minWidth:150},
            {title: '操作', minWidth:150, templet:'#userListBar',fixed:"right",align:"center"}
        ]]
    });

    /**
     * 绑定搜索按钮的点击事件
     */
    //数据表格重载
    $("#btnSearch").click(function (){
        tableIns.reload({
            where: { //设定异步数据接口的额外参数，任意设
                userName:$('[name="userName"]').val(),
                email:$('[name="email"]').val(),
                phone:$('[name="phone"]').val()
            }
            ,page: {
                curr: 1 //重新从第 1 页开始
            }
        });
    });

    /**
     * 绑定头部工具栏
     */
    table.on("toolbar(users)", function (data) {
        var checkStatus = table.checkStatus(data.config.id);
        console.log(checkStatus.data);
        switch(data.event){
            case 'add':
                openAddOrUpdateUserDialog();//打开添加修改的窗口页面
                break;
            case 'del':
                deleteUser(checkStatus.data);
                break;
        }
    });
    /**
     * 绑定行工具栏
     */
    table.on("tool(users)",function (data){
        if(data.event=="edit"){
            openAddOrUpdateUserDialog(data.data.id);//id与user中对应
        }else if(data.event=="del"){
            layer.confirm('确定删除当前用户？', {icon: 3, title: "用户管理"}, function
                (index) {
                $.post(ctx + "/user/delete",{ids:data.data.id},function (data) {
                    if(data.code==200){
                        layer.msg("操作成功！");
                        tableIns.reload();
                    }else{
                        layer.msg(data.msg, {icon: 5});
                    }
                });
            });
        }
    });


    function openAddOrUpdateUserDialog(userId){

        //userId为空是添加，不为空是修改

        var title = "用户管理-用户添加";
        var url = ctx + "/user/toAddOrUpdateUserPage";
        if(userId!=null){
            title = "用户管理-用户更新";
            url += "?userId="+userId;
        }
        //打开弹出层
        layui.layer.open({
           title:title,
           content:url,
            type:2,
            maxmin:true,
            area:["650px","400px"]
        });

    }

    /**
     *
     * @param data
     */
    function deleteUser(data) {
        //判断是否选中数据
        if (data.length == 0) {
            layer.msg("请至少选中一条数据");
            return;
        }
        //向用户确认删除行为
        layer.confirm("您确定要删除选中的记录吗？", {
            btn: ["确认", "取消"],
            icon:3,
            title:"用户管理"
        }, function (index) {
            //关闭弹出框
            layer.close(index);

            //拼接后台需要的id数组  ids=1&ids=2
            var str = "ids=";
            for (var i = 0; i < data.length; i++) {
                //判断是否是倒数第二个
                if (i < data.length - 1) {
                    str += data[i].id + "&ids=";
                } else {
                    str += data[i].id;
                }
            }
            console.log(str);

            $.ajax({
                type: "post",
                url: ctx + "/user/delete",
                data: str,
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        //刷新数据表格
                        tableIns.reload();
                    } else {
                        layer.msg(data.msg, {icon: 5})
                    }
                }
            });

        });
    }
});