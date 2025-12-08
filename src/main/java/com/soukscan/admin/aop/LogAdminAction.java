package com.soukscan.admin.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAdminAction {

    String action();      // ex: "verify_vendor"
    String target();      // ex: "vendor"
}
