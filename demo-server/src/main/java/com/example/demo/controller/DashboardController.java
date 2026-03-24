package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.pojo.entity.Cate_Ari_Total;
import com.example.demo.pojo.entity.Dashboard;
import com.example.demo.service.impl.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    @GetMapping("/alltotal")
    public Result allTotal() {

        Dashboard dashboard = dashboardService.getCount();
        return Result.success(dashboard);
    }
    @GetMapping("/category_article_total")
    public Result getCategory_article_total(@RequestParam Long categoryId) {
        List<Cate_Ari_Total>  list = dashboardService.getCategory_article_total(categoryId);
        return Result.success(list);
    }
}
