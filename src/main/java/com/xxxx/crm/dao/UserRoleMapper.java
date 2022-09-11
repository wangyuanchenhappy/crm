package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole,Integer> {

    Integer countRoleMapperByUserId(Integer userId);

    Integer deleteRoleMapperByUserId(Integer userId);
}