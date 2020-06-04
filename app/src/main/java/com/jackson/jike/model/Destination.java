package com.jackson.jike.model;

/**
 * Copyright (C), 2015-2020
 * FileName: Destination
 * Author: Luo
 * Date: 2020/3/12 20:08
 * Description: 页面节点
 */
public class Destination {


    /**
     * isFragment : true
     * asStarter : false
     * needLogin : false
     * pageUrl : main/tabs/dash
     * className : com.jackson.ppjoke.ui.dashboard.SofaFragment
     * id : 1649486359
     */

    public boolean isFragment;
    public boolean asStarter;
    public boolean needLogin;
    public String pageUrl;
    public String className;
    public int id;

    @Override
    public String toString() {
        return "Destination{" +
                "isFragment=" + isFragment +
                ", asStarter=" + asStarter +
                ", needLogin=" + needLogin +
                ", pageUrl='" + pageUrl + '\'' +
                ", className='" + className + '\'' +
                ", id=" + id +
                '}';
    }
}
