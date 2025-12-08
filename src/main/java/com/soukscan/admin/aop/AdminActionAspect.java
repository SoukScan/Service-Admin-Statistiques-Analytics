package com.soukscan.admin.aop;

import com.soukscan.admin.service.AdminActionLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminActionAspect {

    private final AdminActionLogService logService;

    public AdminActionAspect(AdminActionLogService logService) {
        this.logService = logService;
    }

    @AfterReturning("@annotation(logAdminAction)")
    public void logAction(JoinPoint joinPoint, LogAdminAction logAdminAction) {

        Long adminId = findArgument(joinPoint, "adminId");
        Long targetId = findArgument(joinPoint, "targetId");

        String comment = "Action automatique enregistrée";

        logService.logAction(
                adminId,
                logAdminAction.action(),
                logAdminAction.target(),
                targetId,
                comment
        );
    }

    private Long findArgument(JoinPoint joinPoint, String name) {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }
}
