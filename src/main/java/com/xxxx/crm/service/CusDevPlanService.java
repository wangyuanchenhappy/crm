package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.CusDevPlanMapper;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.CusDevPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class CusDevPlanService extends BaseService<CusDevPlan,Integer> {

    @Resource
    private CusDevPlanMapper cusDevPlanMapper;


    @Transactional(propagation = Propagation.REQUIRED)
    public void saveCusDevPlan(CusDevPlan cusDevPlan) {
        // 1. 参数校验
        checkParams(cusDevPlan.getSaleChanceId(),
                cusDevPlan.getPlanItem(), cusDevPlan.getPlanDate());
        // 2. 设置参数默认值
        cusDevPlan.setIsValid(1);
        cusDevPlan.setCreateDate(new Date());
        cusDevPlan.setUpdateDate(new Date());
        // 3. 执行添加，判断结果
        AssertUtil.isTrue(insertSelective(cusDevPlan) < 1, "计划项记录添加失败！");
    }

    private void checkParams(Integer saleChanceId, String planItem, Date planDate) {
        AssertUtil.isTrue(null == saleChanceId || cusDevPlanMapper.selectByPrimaryKey(saleChanceId) == null, "请设置营销机会ID！");
        AssertUtil.isTrue(StringUtils.isBlank(planItem), "请输入计划项内容！");
        AssertUtil.isTrue(null == planDate, "请指定计划项日期！");
    }

    public void deleteCusDevPlan(Integer id){
        AssertUtil.isTrue(id==null,"你确定你进行了正确的操作吗");
        CusDevPlan cusDevPlan = cusDevPlanMapper.selectByPrimaryKey(id);
        AssertUtil.isTrue(null==cusDevPlan,"删除的数据不存在");
        cusDevPlan.setUpdateDate(new Date());
        cusDevPlan.setIsValid(0);//介个就是删除
        AssertUtil.isTrue(cusDevPlanMapper.updateByPrimaryKeySelective(cusDevPlan)<1,"数据删除失败");
    }
}