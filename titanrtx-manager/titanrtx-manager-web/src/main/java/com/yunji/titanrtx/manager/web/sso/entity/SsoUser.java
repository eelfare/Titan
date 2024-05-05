package com.yunji.titanrtx.manager.web.sso.entity;

import java.io.Serializable;

/**
 * 用户实体 杭州sso返回的实体 这个可以放在api中公用的 md 哪个狗比写一堆同名的类 把它jj剁了喂猫
 */
public class SsoUser implements Serializable {

    /**
     *操作人工号
     */
    private String empId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * oa登录名
     */
    private String loginIdForOA;

    /**
     * 部门
     */
    private String department;

    /**
     * 邮箱
     */
    private String email;

    private Integer id;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginIdForOA() {
        return loginIdForOA;
    }

    public void setLoginIdForOA(String loginIdForOA) {
        this.loginIdForOA = loginIdForOA;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
