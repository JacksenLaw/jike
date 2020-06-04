package com.jackson.jike.model;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: BottomBar
 * Author: Luo
 * Date: 2020/3/12 20:45
 * Description: 底部导航json文件
 */
public class BottomBar {


    /**
     * activeColor : #333333
     * inActiveColor : #666666
     * selectTab : 0
     * tabs : [{"size":24,"enable":true,"index":0,"pageUrl":"main/tabs/home","title":"首页"},{"size":24,"enable":true,"index":1,"pageUrl":"main/tabs/sofa","title":"沙发"},{"size":40,"enable":true,"index":2,"tintColor":"#ff678f","pageUrl":"main/tabs/publish","title":""},{"size":24,"enable":true,"index":3,"pageUrl":"main/tabs/find","title":"发现"},{"size":24,"enable":true,"index":4,"pageUrl":"main/tabs/my","title":"我的"}]
     */

    public String activeColor;
    public String inActiveColor;
    public int selectTab;
    public List<TabsBean> tabs;

    public static class TabsBean {
        /**
         * size : 24 icon大小
         * enable : true
         * index : 0
         * pageUrl : main/tabs/home
         * title : 首页
         * tintColor : #ff678f
         */

        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
        public String tintColor;

        @Override
        public String toString() {
            return "TabsBean{" +
                    "size=" + size +
                    ", enable=" + enable +
                    ", index=" + index +
                    ", pageUrl='" + pageUrl + '\'' +
                    ", title='" + title + '\'' +
                    ", tintColor='" + tintColor + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BottomBar{" +
                "activeColor='" + activeColor + '\'' +
                ", inActiveColor='" + inActiveColor + '\'' +
                ", selectTab=" + selectTab +
                ", tabs=" + tabs +
                '}';
    }
}
