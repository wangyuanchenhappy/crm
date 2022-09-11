layui.use(['form', 'layer','formSelects'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        formSelects = layui.formSelects;

    /**
     * 表单提交
     */
    form.on("submit(addOrUpdateUser)",function (data){
        // 提交数据时的加载层 （https://layer.layui.com/）
        var index = layer.msg("数据提交中,请稍后...",{
            icon:16, // 图标
            time:false, // 不关闭
            shade:0.8 // 设置遮罩的透明度
        });

        var url = ctx+"/user/add";
        var userId = $('[name="id"]').val();
        //如果进入判断则是修改
        if(userId != null && userId !=''){
            url = ctx+"/user/updateUser";
        }

        //发送请求
       $.ajax({
           type:"post",
           url:url,
           data:data.field,
           success:function(data){
               if(data.code==200){
                   //关闭加载层
                   layer.close(index);
                   //提示用户
                   layer.msg("操作成功！",{icon:6})
                   layer.closeAll("iframe");
                   //刷新
                   parent.location.reload();

               }else{
                   layer.msg(data.msg,{icon:5})
               }
           }
       });

        return false;

    });
    /**
     * 加载下拉框
     */
    formSelects.config('selectId',{
        type:"post",
        searchUrl:ctx + "/role/queryAllRoles?id="+$('[name="id"]').val(),
        //自定义返回数据中name的key, 默认 name
        keyName: 'rname',
        //自定义返回数据中value的key, 默认 value
        keyVal: 'id'
    },true);
});