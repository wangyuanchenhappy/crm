package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.model.TreeModel;
import com.xxxx.crm.vo.Module;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ModuleMapper extends BaseMapper<Module,Integer> {
    public List<TreeModel> queryAllModules();

    public List<Module> queryModules();

    Module queryModuleByGradeAName(@Param("grade") Integer grade,@Param("moduleName") String moduleName);
    Module queryModuleByGradeAUrl(@Param("grade") Integer grade, @Param("url") String url);

    Module queryModuleById(Integer parentId);

    Module queryModuleByOptValue(String optValue);

    Integer queryCountModelByParentId(Integer mId);

    Integer deleteModuleByMid(Integer mId);

}