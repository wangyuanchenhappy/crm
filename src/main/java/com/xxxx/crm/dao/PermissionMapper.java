package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer> {


    Integer countPermission(Integer roleId);

    Integer deletePermissionByRoleId(Integer roleId);

    List<Integer> selectPermissionByRoleId(Integer roleId);


    Integer queryCountByModuleId(Integer mId);
    //删除某个模块关联的所有权限数据
    Integer deletePermissionByModuleId(Integer mId);

    List<Integer> selectAclvalueByUserId(Integer id);

    List<String> queryUserHasRolesHasPermissions(Integer userId);

}