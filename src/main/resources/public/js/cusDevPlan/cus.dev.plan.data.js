layui.use(['table','layer'],function(){
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        table = layui.table;

    /**
     * 计划项数据展示
     */
    var  tableIns = table.render({
        elem: '#cusDevPlanList',
        url : ctx+'/cus_dev_plan/list?sId='+$('[name="id"]').val(),
        cellMinWidth : 95,
        page : true,
        height : "full-125",
        limits : [10,15,20,25],
        limit : 10,
        toolbar: "#toolbarDemo",
        id : "cusDevPlanListTable",
        cols : [[
            {type: "checkbox", fixed:"center"},
            {field: "id", title:'编号',fixed:"true"},
            {field: 'planItem', title: '计划项',align:"center"},
            {field: 'exeAffect', title: '执行效果',align:"center"},
            {field: 'planDate', title: '执行时间',align:"center"},
            {field: 'createDate', title: '创建时间',align:"center"},
            {field: 'updateDate', title: '更新时间',align:"center"},
            {title: '操作',fixed:"right",align:"center", minWidth:150,templet:"#cusDevPlanListBar"}
        ]]
    });


    /**
     * 监听头部工具栏
     */
    table.on('toolbar(cusDevPlans)', function(obj){
        switch(obj.event){
            case "add":
                addOrUpdateCusDevPlanDialog();
                break;
            case "success":
                updateSaleChanceDevResult(2);
                break;
            case "failed":
                updateSaleChanceDevResult(3);
                break;
        };
    });




        //更新营销机会的开发状态
    function updateSaleChanceDevResult(devResult) {
        // 获取当前营销机会的ID（隐藏域中获取）
        var sid = $("input[name='id']").val();
        // 弹出提示框询问用户
        layer.confirm("确认执行当前操作？", {icon:3, title:"计划项维护"}, function
            (index) {
            $.post(ctx + "/sale_chance/updateSaleChanceDevResult", {id:sid,
                devResult:devResult}, function (data) {
                if (data.code == 200) {
                    layer.msg("操作成功！");
                    // 关闭弹出层
                    layer.closeAll("iframe");
                    // 刷新父页面
                    parent.location.reload();
                } else {
                    layer.msg(data.msg, {icon:5});
                }
            });
        });

    }


    /**
     * 监听行工具栏
     */
    table.on('tool(cusDevPlans)', function(obj){
        var layEvent = obj.event;
            // 监听编辑事件
        if(layEvent === "edit") {
            addOrUpdateCusDevPlanDialog(obj.data.id);
        } else if (layEvent == "del") {
                // 询问用户是否确认删除
            layer.confirm("确定删除当前数据？", {icon:3, title:"开发计划管理"}, function
                (index) {
                // 发送ajax请求
                $.post(ctx + "/cus_dev_plan/delete", {id:obj.data.id}, function
                    (data) {
                    if (data.code == 200) {
                        layer.msg("操作成功！");
                    // 重新加载表格
                        tableIns.reload();
                    } else {
                        layer.msg(data.msg, {icon:5});
                    }
                });
            });
        }
    });

    //打开计划项修改添加窗口
    function addOrUpdateCusDevPlanDialog(id) {
        var url = ctx + "/cus_dev_plan/addOrUpdateCusDevPlanPage?sId = "+$("input[name = 'id']").val();
        var title = "计划项管理-添加计划项";
        if (id) {
            url = url + "&id=" + id;
            title = "计划项管理-更新计划项";
        }
        layui.layer.open({
            title: title,
            type: 2,
            area: ["500px", "300px"],
            maxmin: true,
            content: url
        });
    }
});
