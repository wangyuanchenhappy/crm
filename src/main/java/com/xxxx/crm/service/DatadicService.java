package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.DatadicMapper;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Datadic;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;


@Service
public class DatadicService extends BaseService<Datadic,Integer> {
    @Resource
    private DatadicMapper datadicMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void addDatadic(Datadic datadic){

        checkDatadicParams(datadic.getDataDicName(),datadic.getDataDicValue());

        datadic.setIsValid((byte) 1);
        datadic.setCreateDate(new Date());
        datadic.setUpdateDate(new Date());

        AssertUtil.isTrue(datadicMapper.insertSelective(datadic)<1,"数据添加失败");
    }


    private void checkDatadicParams(String name,String value){
        AssertUtil.isTrue(StringUtils.isBlank(name),"表名不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(value),"表值不能为空");

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDatadic(Datadic datadic){
        AssertUtil.isTrue(datadic.getId()==null,"你输入的id不能为空");
        Datadic in = datadicMapper.selectByPrimaryKey(datadic.getId());
        AssertUtil.isTrue(in.getId()==null,"你所查询的id数据库中不存在");
        AssertUtil.isTrue(!(datadic.getId().equals(in.getId())),"数据不匹配");
        datadic.setUpdateDate(new Date());
        AssertUtil.isTrue(datadicMapper.updateByPrimaryKeySelective(datadic)<1,"数据修改失败");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteDatadic(Integer id){

        AssertUtil.isTrue(id==null,"id值不能为空");
        Datadic dc = selectByPrimaryKey(id);
        AssertUtil.isTrue(dc.getId()==null,"数据库中未查到此数据");

        AssertUtil.isTrue(datadicMapper.deleteDatadicById(id)<1,"删除失败");

    }

}
