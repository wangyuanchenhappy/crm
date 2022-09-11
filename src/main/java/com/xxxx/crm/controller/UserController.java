package com.xxxx.crm.controller;

import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.service.UserService;
import com.xxxx.crm.utils.LoginUserUtil;
import com.xxxx.crm.vo.SaleChance;
import com.xxxx.crm.vo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {

    @Resource
    private UserService userService;


    /**
     *  用户登录
     * @param userName
     * @param userPwd
     */
    @PostMapping("login")
    @ResponseBody
    public ResultInfo login(String userName,String userPwd){
        return userService.loginCheck(userName, userPwd);
    }



    /**
     *  修改密码

     */
    @PostMapping("update")
    @ResponseBody
    public ResultInfo update(HttpServletRequest request, String oldPassword, String newPassword, String confirmPassword){
        //获取登录用户的id
        int id = LoginUserUtil.releaseUserIdFromCookie(request);
            userService.userUpdate(id,oldPassword,newPassword,confirmPassword);
        return success();
    }


    //打开修改密码页面
    @RequestMapping("toPasswordPage")
    public String toPasswordPage(){
        return "user/password";
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> selectUserList(UserQuery userQuery){
        return userService.queryByParamsForTable(userQuery);
    }

    /**
     * 进入用户管理页面
     * @return
     */
    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    /**
     * 进入添加或修改用户页面
     * @return
     */
    @RequestMapping("toAddOrUpdateUserPage")
    public String toAddOrUpdateUserPage(HttpServletRequest request,Integer userId){
        //判断用户id是否为空，不为空查询用户对象
        if(userId != null){
            User user = userService.selectByPrimaryKey(userId);
            request.setAttribute("user",user);
        }
        return "user/add_update";
    }

    /**
     * 添加
     * @param user
     * @return
     */
    @PostMapping("add")
    @ResponseBody
    public ResultInfo addUser(User user){
        userService.addUser(user);
        return success();
    }

    /**
     * 修改
     * @param user
     * @return
     */
    @PostMapping("updateUser")
    @ResponseBody
    public ResultInfo updateUser(User user){
        userService.updateUser(user);
        return success();
    }

    @PostMapping("delete")
    @ResponseBody
    public ResultInfo deleteUser(Integer[] ids){
        userService.deleteUser(ids);
        return success();
    }
}
