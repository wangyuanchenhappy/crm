package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.query.UserModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.User;
import com.xxxx.crm.vo.UserRole;
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
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    /** 用户登录
             2.校验参数是否为空
                如果为空，抛异常
             3.调用dao层查询通过用户名查询数据库数据
                如果未查到，抛异常(用户不存在)
             4.校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
                如果不一致，抛异常(密码错误)
             5.封装ResultInfo对象给前台（根据前台需求：usermodel对象封装后传到前台使用）
     */
    public ResultInfo loginCheck(String userName ,String userPwd){
        //校验参数是否为空
        checkLoginData(userName,userPwd);
        //调用dao层查询通过用户名查询数据库数据，判断账号是否存在
        User user = userMapper.queryUserByName(userName);
        AssertUtil.isTrue(user == null,"账号不存在");

        //校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
        checkLoginPwd(user.getUserPwd(),userPwd);

        //封装ResultInfo对象给前台（根据前台需求：usermodel对象封装后传到前台使用）
        ResultInfo resultInfo = buildResultInfo(user);

        return resultInfo;
    }


    /** 修改密码
     */
    public void userUpdate(Integer userId,String oldPassword,String newPassword,String confirmPassword){
        //确保用户是否是登录状态获取cookie中的id 非空 查询数据库
        AssertUtil.isTrue(userId == null,"用户未登录");
        User user = (User) userMapper.selectByPrimaryKey(userId);
        AssertUtil.isTrue(user == null,"用户状态异常");

        //校验密码数据
        checkUpdateData(oldPassword,newPassword,confirmPassword,user.getUserPwd());
        // 执行修改操作，返回ResultInfo
        user.setUserPwd(Md5Util.encode(newPassword));
        user.setUpdateDate(new Date());
        //判断是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1,"密码修改失败");
    }



    /**密码校验
     *          1.确保用户是否是登录状态获取cookie中的id 非空 查询数据库
     *          2.校验老密码 非空  老密码必须要跟数据库中密码一致
     *          3.新密码    非空  新密码不能和原密码一致
     *          4.确认密码  非空  确认必须和新密码一致
     *          5.执行修改操作，返回ResultInfo
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param
     */
    private void checkUpdateData(String oldPassword, String newPassword, String confirmPassword, String dbPassword) {
        //校验老密码  非空  老密码必须要跟数据库中密码一致
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"原始密码不存在");
        AssertUtil.isTrue(!dbPassword.equals(Md5Util.encode(oldPassword)),"原始密码错误");

        //新密码    非空  新密码不能和原密码一致
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        AssertUtil.isTrue(oldPassword.equals(newPassword),"新密码不能和原密码一致");

        //确认密码  非空  确认必须和新密码一致
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"确认密码不能为空");
        AssertUtil.isTrue(!confirmPassword.equals(newPassword),"确认密码必须和新密码一致");

    }


    /**
     * 准备前台cookie需要的数  usermodel
     * @param user
     */
    private ResultInfo buildResultInfo(User user) {
        ResultInfo resultInfo = new ResultInfo();

        //封装userMdel  cookie需要的数据
        UserModel userModel = new UserModel();
        //将userid加密
        String id = UserIDBase64.encoderUserID(user.getId());
        userModel.setUserId(id);
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());

        resultInfo.setResult(userModel);
        return resultInfo;
    }


    /**
     * 校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
     */
    private void checkLoginPwd(String dbPwd, String userPwd) {
        //将传来的密码加密再校验
        String encodePwd = Md5Util.encode(userPwd);
        //校验
        AssertUtil.isTrue(!encodePwd.equals(dbPwd),"用户密码错误");
    }

    /**
     * 用户登录参数非空校验
     * @param userName
     * @param userPwd
     */
    private void checkLoginData(String userName, String userPwd) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空");
    }

    /**
     * 添加用户
     * (一)、参数校验
     *  1.userName    非空且唯一
     *  2.trueName   非空
     *  3.email  非空
     *  4.phone   非空且格式正确
     *
     * (二)、设置默认值
     *  1.isValid   1
     *  2.createDate    系统当前时间
     *  3.updateDate    系统当前时间
     *  4.userPwd       123456(默认密码，需要加密)
     *
     *  (三)、执行添加操作，判断受影响行数
     * @Transactional(propagation=Propagation.REQUIRED) ：如果有事务, 那么加入事务, 没有的话新建一个(默认情况下) spring默认传播行为
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){
        //1
        checkAddUpdateParams(user);
        //2
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setUserPwd(Md5Util.encode("123456"));

        //AssertUtil.isTrue(userMapper.insertSelective(user)<1,"用户添加失败");

        //执行添加操作，返回主键,主键会被设置到User对象的id属性中
        Integer key = userMapper.insertHasKey(user);

        AssertUtil.isTrue(key<1,"用户添加失败");
        //绑定角色给用户
        relationUserRole(user.getId(),user.getRoleIds());
    }

    private void relationUserRole(Integer userId, String roleIds) {

        //判断用户是否有角色关联用户
        Integer count = userRoleMapper.countRoleMapperByUserId(userId);

        //如果用户记录存在，则执行删除操作
        if(count>0){
            AssertUtil.isTrue(userRoleMapper.deleteRoleMapperByUserId(userId)!=count,"用户角色分配失败");
        }
        if (StringUtils.isNotBlank(roleIds)) {
            //重新添加新的角色
            List<UserRole> userRoles = new ArrayList<UserRole>();
            for (String s : roleIds.split(",")) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(Integer.parseInt(s));
                userRole.setCreateDate(new Date());
                userRole.setUpdateDate(new Date());
                userRoles.add(userRole);
            }
            AssertUtil.isTrue(userRoleMapper.insertBatch(userRoles) < userRoles.size(), "用户角色分配失败!");
        }
    }

    /**
     * 验证添加或修改用户的参数
     * @param user
     */
    private void checkAddUpdateParams(User user) {
        AssertUtil.isTrue(StringUtils.isBlank(user.getUserName()),"用户名不能为空");
        //添加操作，只需要判断数据库中是否存在相同的用户名，存在即不可用
        //接口中有对应的查询方法
        User temp = userMapper.queryUserByName(user.getUserName());

        if(user.getId()==null){
            //判断是否存在
            AssertUtil.isTrue(temp!=null,"用户名已被使用，请重新输入");
        }else{
            //更新操作(数据存在，且id不是当前数据本身)
            AssertUtil.isTrue(temp !=null && !temp.getId().equals(user.getId()),"用户名已被使用，请重新输入");
        }


        AssertUtil.isTrue(StringUtils.isBlank(user.getTrueName()), "请输入真实姓名！");
        AssertUtil.isTrue(StringUtils.isBlank(user.getEmail()), "请输入邮箱地址！");
        AssertUtil.isTrue(StringUtils.isBlank(user.getPhone()),"手机号码不能为空");
        AssertUtil.isTrue(!PhoneUtil.isMobile(user.getPhone()), "手机号码格式不正确！");
    }


    /**
     * 更新用户
     * (校验：加一个id)
     * id   非空
     *
     * (设置默认值)
     * updateDate
     *
     * 判断有无更新
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(User user){
        AssertUtil.isTrue(user.getId()==null,"请给到我一个id,蟹蟹");
        checkAddUpdateParams(user);
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"用户更新失败!!!");

        relationUserRole(user.getId(),user.getRoleIds());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(Integer[] ids){
        AssertUtil.isTrue(ids==null||ids.length<1,"待删除记录不存在");
        //删除
        AssertUtil.isTrue(userMapper.deleteBatch(ids)!=ids.length,"用户删除失败!");
    }

}
