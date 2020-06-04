package com.jackson.nav_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface FragmentDestination {

    String pageUrl();

    /**
     * 标注页面是否需要登录
     * @return
     */
    boolean needLogin() default false;

    /**
     * 标注页面是否是默认页面
     * @return
     */
    boolean asStarter() default false;

}
