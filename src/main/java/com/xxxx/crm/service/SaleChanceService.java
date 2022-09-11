package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.query.SaleChanceQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.vo.SaleChance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleChanceService extends BaseService<SaleChance, Integer> {

    @Resource
    private SaleChanceMapper saleChanceMapper;
    /**
     * 多条件分页查询营销机会 (BaseService 中有对应的方法)
     * @param query
     * @return
     */
    public Map<String, Object> querySaleChanceByParams (SaleChanceQuery query) {
        Map<String, Object> map = new HashMap<>();
        PageHelper.startPage(query.getPage(), query.getLimit());
        PageInfo<SaleChance> pageInfo = new PageInfo<>(saleChanceMapper.selectByParams(query));
        map.put("code",0);
        map.put("msg", "success");
        map.put("count", pageInfo.getTotal());
        map.put("data", pageInfo.getList());
        return map;
    }

    /**
     * 添加数据
     *      1.校验参数
     *          customerName   客户名称 非空
     *          linkMan       联系人   非空
     *          linkPhone      手机号码 非空  手机号11位正则校验
     *      2.设置默认值
     *          is_valid     数据有效   0无效 1有效
     *          create_date  数据创建时间
     *          update_date  数据修改时间
     *          create_man   数据的创建人  当前登录用户（交给controller层从cookie获取）直接设置到 salechance对象中
     *
     *          判断用户是否设置了分配人
     *              如果分配了
     *                  assign_man   分配人
     *                  assign_time  分配时间
     *                  state        已分配 分配状态  0未分配 1已分配
     *                  dev_result   开发中 开发状态  0-未开发 1-开发中 2-开发成功 3-开发失败
     *              如果未分配
     *                  state        未分配 分配状态  0未分配 1已分配
     *                  dev_result   未开发 开发状态  0-未开发 1-开发中 2-开发成功 3-开发失败
     *       3.执行添加操作，判断是否添加成功
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addSlaChance(SaleChance saleChance){
        //校验参数
        checkParams(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        //设置默认值
        saleChance.setIsValid(1);
        saleChance.setUpdateDate(new Date());
        saleChance.setCreateDate(new Date());

        //判断用户是否设置了分配人
        if(StringUtils.isBlank(saleChance.getAssignMan())){
            //未分配状态
            saleChance.setState(0);
            saleChance.setDevResult(0);
        }else{
            //分配了人员
            saleChance.setAssignTime(new Date());
            saleChance.setState(1);
            saleChance.setDevResult(1);
        }

        //执行添加操作，判断是否添加成功
        AssertUtil.isTrue(saleChanceMapper.insertSelective(saleChance) < 1,"营销机会数据添加失败");
    }

    private void checkParams(String customerName, String linkMan, String linkPhone) {
        AssertUtil.isTrue(StringUtils.isBlank(customerName),"客户名称不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(linkMan),"联系人不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(linkPhone),"手机号码不能为空");
        //校验手机号是否符合规范
        AssertUtil.isTrue(!PhoneUtil.isMobile(linkPhone),"手机号不符合规范");
    }

    public void updateSaleChance(SaleChance saleChance){
        //校验id
        AssertUtil.isTrue(saleChance.getId()==null,"他奶奶滴，给空值是吧");

        checkParams(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        //通过ID查询营销机会对象
        SaleChance saleChanceObject = saleChanceMapper.selectByPrimaryKey(saleChance.getId());

        AssertUtil.isTrue(saleChanceObject==null,"我的世界没有你要查的的数据");

        //判断原有项目是否有分配人(数据库中的数据)
        if(StringUtils.isBlank(saleChanceObject.getAssignMan())){
            //现在要修改的数据
            if(!StringUtils.isBlank(saleChance.getAssignMan())){
                saleChance.setAssignTime(new Date());
                saleChance.setState(1);
                saleChance.setDevResult(1);
            }
        }else {
            //进入当前判断说明修改前有分配人

            //判断修改后是否有分配人
            if (StringUtils.isBlank(saleChance.getAssignMan())) {
                //修改后没有分配人
                saleChance.setAssignTime(null);
                saleChance.setState(0);
                saleChance.setDevResult(0);
            } else {
                //修改后有分配人
                //判断前后的分配人是否有变化
                if (!saleChanceObject.getAssignMan().equals(saleChance.getAssignMan())) {
                    //不是一个人，有变化
                    saleChance.setAssignTime(new Date());
                } else {
                    //修改前后是同一个人，则使用修改前的数据
                    saleChance.setAssignTime(saleChanceObject.getAssignTime());
                }
            }
        }
        AssertUtil.isTrue(saleChanceMapper.updateByPrimaryKeySelective(saleChance)<1,"好像并没有改变，看看出了什么问题吧");
    }

    /**
     * 查询所有的销售人员
     * @return
     */
    public List<Map<String, Object>> queryAllSales() {
        return  saleChanceMapper.queryAllSales();
    }


    /**
     * 营销机会数据删除
     * @param ids
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteSaleChance (Integer[] ids) {
            // 判断要删除的id是否为空
        AssertUtil.isTrue(null == ids || ids.length == 0, "请选择需要删除的数据！");
            // 删除数据
        AssertUtil.isTrue(saleChanceMapper.deleteBatch(ids) < 0, "营销机会数据删除失败！");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateSaleChanceDevResult(Integer id, Integer devResult) {
        AssertUtil.isTrue( null ==id,"待更新记录不存在!");
        SaleChance temp =selectByPrimaryKey(id);
        AssertUtil.isTrue( null ==temp,"待更新记录不存在!");
        temp.setDevResult(devResult);
        AssertUtil.isTrue(updateByPrimaryKeySelective(temp)<1,"机会数据更新失败!");
    }
}