package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.dao.RoleMapper;
import com.xxxx.crm.model.TreeModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Module;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleService extends BaseService<Module,Integer> {

    @Resource
    private ModuleMapper moduleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    public List<TreeModel> queryAllModules(Integer roleId){

        AssertUtil.isTrue(roleId==null||roleMapper.selectByPrimaryKey(roleId)==null,"角色不存在");

        List<Integer>  mIds = permissionMapper.selectPermissionByRoleId(roleId);

        List<TreeModel> treeModels = moduleMapper.queryAllModules();

        for(TreeModel treeModel:treeModels){
           Integer id = treeModel.getpId();
           if(mIds.contains(id)){
               treeModel.setChecked(true);
               treeModel.setOpen(true);
           }
        }

        return treeModels;
    }

    public Map<String,Object> queryModules(){
        Map<String,Object> result = new HashMap<String,Object>();
        List<Module> modules = moduleMapper.queryModules();

        AssertUtil.isTrue(modules==null||modules.size()<1,"资源加载异常");
        result.put("count",modules.size());
        result.put("data",modules);
        result.put("code",0);
        result.put("msg","");
        return result;
    }

    public void addModule(Module module) {
        AssertUtil.isTrue(module.getGrade() == null,"层级不能为空");
        AssertUtil.isTrue(!(module.getGrade() == 0 || module.getGrade() == 1 || module.getGrade() == 2),"层级有误");
        //模块名称 非空  同级唯一
        AssertUtil.isTrue(StringUtils.isBlank(module.getModuleName()),"模块名称不能为空");
        Module dbModule = moduleMapper.queryModuleByGradeAName(module.getGrade(),module.getModuleName());
        AssertUtil.isTrue(dbModule != null,"模块名称已存在");

        // 二级菜单URL：非空，同级唯一
        if(module.getGrade() == 1){
            AssertUtil.isTrue(StringUtils.isBlank(module.getUrl()),"模块地址不能为空");
            dbModule = moduleMapper.queryModuleByGradeAUrl(module.getGrade(),module.getUrl());
            AssertUtil.isTrue(dbModule != null,"地址已存在，请重新输入");
        }

        //父级菜单  二级|三级：非空 | 必须存在
        if(module.getGrade() == 1 || module.getGrade() == 2){
            AssertUtil.isTrue(module.getParentId() == null,"父ID不能为空");
            dbModule = moduleMapper.queryModuleById(module.getParentId());
            AssertUtil.isTrue(dbModule == null,"父ID不存在");
        }

        //权限码  非空  唯一
        AssertUtil.isTrue(StringUtils.isBlank(module.getOptValue()),"权限码不能为空");
        dbModule = moduleMapper.queryModuleByOptValue(module.getOptValue());
        AssertUtil.isTrue(dbModule != null,"权限码已存在");

        //默认值
        module.setIsValid((byte) 1);
        module.setCreateDate(new Date());
        module.setUpdateDate(new Date());

        //执行添加操作  判断受影响行数
        AssertUtil.isTrue(moduleMapper.insertSelective(module) < 1,"模块添加失败");

    }

    public void updateModule(Module module) {
        AssertUtil.isTrue(module.getId()==null,"你输入了空为更新的资源");
        Module dbModule = moduleMapper.selectByPrimaryKey(module.getId());
        AssertUtil.isTrue(dbModule==null,"你要找的资源数据库中没有");

        AssertUtil.isTrue(module.getGrade() == null,"层级不能为空");
        AssertUtil.isTrue(!(module.getGrade() == 0 || module.getGrade() == 1 || module.getGrade() == 2),"层级有误");

        AssertUtil.isTrue(StringUtils.isBlank(module.getModuleName()),"模块名称不能为空");
        dbModule = moduleMapper.queryModuleByGradeAName(module.getGrade(),module.getModuleName());
        AssertUtil.isTrue(dbModule != null && !(module.getId().equals(dbModule.getId())),"模块名称已存在");

        // 二级菜单URL：非空，同级唯一
        if(module.getGrade() == 1){
            AssertUtil.isTrue(StringUtils.isBlank(module.getUrl()),"模块地址不能为空");
            dbModule = moduleMapper.queryModuleByGradeAUrl(module.getGrade(),module.getUrl());
            AssertUtil.isTrue(dbModule != null && !(module.getId().equals(dbModule.getId())),"地址已存在，请重新输入");
        }

        //父级菜单  二级|三级：非空 | 必须存在
        if(module.getGrade() == 1 || module.getGrade() == 2){
            AssertUtil.isTrue(module.getParentId() == null,"父ID不能为空");
            dbModule = moduleMapper.queryModuleById(module.getParentId());
            AssertUtil.isTrue(dbModule == null && !(module.getId().equals(dbModule.getId())),"父ID不存在");
        }

        //权限码  非空  唯一
        AssertUtil.isTrue(StringUtils.isBlank(module.getOptValue()),"权限码不能为空");
        dbModule = moduleMapper.queryModuleByOptValue(module.getOptValue());
        AssertUtil.isTrue(dbModule != null && !(module.getId().equals(dbModule.getId())),"权限码已存在");

        //默认值
        module.setUpdateDate(new Date());

        //执行修改操作
        AssertUtil.isTrue(moduleMapper.updateByPrimaryKeySelective(module) < 1,"资源修改失败");
    }

    public void deleteModule(Integer mId) {

        AssertUtil.isTrue(mId==null,"嘿，你删了个空值");
        AssertUtil.isTrue(selectByPrimaryKey(mId).getId()==null,"你要删除的资源数据库没有");
        //查询是否有子模块，有就不能删
        Integer count = moduleMapper.queryCountModelByParentId(mId);
        AssertUtil.isTrue(count>0,"它有孕在身，不能删");
        //查询权限表中(角色和资源)是否包含当前模块的数据，有则删除
        count = permissionMapper.queryCountByModuleId(mId);
        if(count>0){
            AssertUtil.isTrue(permissionMapper.deletePermissionByModuleId(mId)!=count,"权限删除失败");
        }
        AssertUtil.isTrue(moduleMapper.deleteModuleByMid(mId)<1,"资源删除失败");
    }
}
