package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.Datadic;

public interface DatadicMapper extends BaseMapper<Datadic,Integer> {

    public String queryDcByName();

    Integer deleteDatadicById(Integer id);


}