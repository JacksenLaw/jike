package com.jackson.jike.model;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: SofaTab
 * Author: Luo
 * Date: 2020/5/9 16:12
 * Description:
 */
public class SofaTab {

    public int activeSize;
    public int normalSize;
    public String activeColor;
    public String normalColor;
    public int select;
    public int tabGravity;
    public List<TabsBean> tabs;

    public static class TabsBean {
        public String title;
        public int index;
        public String tag;
        public boolean enable;
    }

}
