package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.dao.RoleMapper;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Permission;
import com.xxxx.crm.vo.Role;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class RoleService extends BaseService<Role,Integer> {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private ModuleMapper moduleMapper;

    public List<Map<String,Object>> queryAllRoles(Integer id){
        return roleMapper.queryAllRoles(id);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addRole(Role role){
        //判断传入的姓名是否为空
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"用户名不能为空");

        //查询数据库中是否存在相似的id
        Role temp = roleMapper.queryRoleByRoleName(role.getRoleName());
        AssertUtil.isTrue(null !=temp,"该角色已存在!");

        //设置参数
        role.setIsValid(1);
        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());

        //判断是否添加成功
        AssertUtil.isTrue(insertSelective(role)<1,"角色记录添加失败!");

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRole(Role role){
        AssertUtil.isTrue(null==role.getId()||null==selectByPrimaryKey(role.getId()),"待修改的ID不存在");
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名!");
        Role temp = roleMapper.queryRoleByRoleName(role.getRoleName());
        AssertUtil.isTrue(null !=temp && !(temp.getId().equals(role.getId())),"该角色 已存在!");
        role.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"角色记录更新失败!");

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRole(Integer roleId){
        Role temp =selectByPrimaryKey(roleId);
        AssertUtil.isTrue(null==roleId||null==temp,"待删除的记录不存在!");
        temp.setIsValid(0);
        AssertUtil.isTrue(updateByPrimaryKeySelective(temp)<1,"角色记录删除失败!");
    }

    @Transactional
    public  void addGrant(Integer roleId, Integer[] mIds){
        Role role = roleMapper.selectByPrimaryKey(roleId);
        AssertUtil.isTrue(role == null,"角色不存在");
        //查询当前角色是否有权限/资源  拿到个数
        Integer count = permissionMapper.countPermission(roleId);
        if(count > 0){
            AssertUtil.isTrue(permissionMapper.deletePermissionByRoleId(roleId) != count,"角色删除失败");
        }

        //判断是否有需要添加的权限/资源
        if(mIds != null && mIds.length > 0){
            List<Permission> permissions = new ArrayList<>();
            //遍历获取新数据
            for(Integer mid:mIds){
                Permission permission = new Permission();
                permission.setRoleId(roleId);
                permission.setModuleId(mid);
                permission.setCreateDate(new Date());
                permission.setUpdateDate(new Date());
                //设置权限码
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mid).getOptValue());

                //将对象添加到集合中准备批添加
                permissions.add(permission);
            }

            permissionMapper.insertBatch(permissions);
        }

    }

}
