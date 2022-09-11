package com.xxxx.crm.controller;

import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.service.DatadicService;
import com.xxxx.crm.vo.Datadic;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("dc")
public class DatadicController extends BaseController {

    @Resource
    private DatadicService datadicService;

    @PostMapping("add")
    @ResponseBody
    public ResultInfo addDatadic(Datadic datadic){

        datadicService.addDatadic(datadic);
        return success();
    }

    @RequestMapping("update")
    @ResponseBody
    public ResultInfo updateDatadic(Datadic datadic){
        datadicService.updateDatadic(datadic);
        return success();
    }

    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo deleteDatadic(Integer id){
        datadicService.deleteDatadic(id);
        return success();
    }
}
