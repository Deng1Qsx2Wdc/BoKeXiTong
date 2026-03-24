package com.example.demo.pojo.entity;

/**
 * 参数校验分组接口
 * 用于在不同场景下应用不同的校验规则
 */
public interface ValidationGroups {

    /**
     * 注册场景
     */
    interface Register {}

    /**
     * 登录场景
     */
    interface Login {}

    /**
     * 更新场景
     */
    interface Update {}

    /**
     * 删除场景
     */
    interface Delete {}
}
