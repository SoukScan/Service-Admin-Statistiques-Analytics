package com.soukscan.admin.controller;

import com.soukscan.admin.dto.AdminActionLogDTO;
import com.soukscan.admin.entity.AdminActionLog;
import com.soukscan.admin.service.AdminActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for admin action audit logs.
 * Provides endpoints to retrieve and filter admin actions performed on the system.
 *
 * All endpoints require JWT Bearer token authentication with ROLE_ADMIN or ROLE_MODERATOR.
 */
@RestController
@RequestMapping("/admin/logs")
@Tag(name = "Admin - Audit Logs", description = "Admin action audit log endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminActionLogController {

    private final AdminActionLogService logService;

    public AdminActionLogController(AdminActionLogService logService) {
        this.logService = logService;
    }

    /**
     * Convert AdminActionLog entity to DTO.
     * @param log Admin action log entity
     * @return DTO representation
     */
    private AdminActionLogDTO toDTO(AdminActionLog log) {
        AdminActionLogDTO dto = new AdminActionLogDTO();
        dto.setId(log.getId());
        dto.setAdminId(log.getAdminId());
        dto.setActionType(log.getActionType());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setComment(log.getComment());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    /**
     * Retrieve all admin action logs.
     * @return List of all admin actions
     */
    @GetMapping
    @Operation(summary = "Get all admin logs", description = "Retrieve all admin action logs")
    public List<AdminActionLogDTO> getAll() {
        return logService.getAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve admin action logs by specific admin.
     * @param adminId Admin user ID
     * @return List of actions performed by that admin
     */
    @GetMapping("/admin/{adminId}")
    @Operation(summary = "Get logs by admin", description = "Retrieve all actions performed by a specific admin")
    @Parameter(name = "adminId", description = "Admin ID", required = true)
    public List<AdminActionLogDTO> getByAdmin(@PathVariable Long adminId) {
        return logService.getByAdminId(adminId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve admin action logs by action type.
     * @param actionType Type of action (e.g., VENDOR_VERIFIED, USER_WARNED, PRODUCT_CREATED)
     * @return List of actions of that type
     */
    @GetMapping("/action/{actionType}")
    @Operation(summary = "Get logs by action type", description = "Retrieve all actions of a specific type")
    @Parameter(name = "actionType", description = "Action type (e.g., VENDOR_VERIFIED, USER_WARNED)", required = true)
    public List<AdminActionLogDTO> getByAction(@PathVariable String actionType) {
        return logService.getByActionType(actionType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve admin action logs by target type.
     * @param targetType Type of target entity (e.g., VENDOR, USER, PRODUCT)
     * @return List of actions targeting that type
     */
    @GetMapping("/target/{targetType}")
    @Operation(summary = "Get logs by target type", description = "Retrieve all actions targeting a specific entity type")
    @Parameter(name = "targetType", description = "Target type (e.g., VENDOR, USER, PRODUCT)", required = true)
    public List<AdminActionLogDTO> getByTargetType(@PathVariable String targetType) {
        return logService.getByTargetType(targetType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve admin action logs by target ID.
     * @param targetId ID of the target entity
     * @return List of actions targeting that entity
     */
    @GetMapping("/target-id/{targetId}")
    @Operation(summary = "Get logs by target ID", description = "Retrieve all actions targeting a specific entity")
    @Parameter(name = "targetId", description = "Target entity ID", required = true)
    public List<AdminActionLogDTO> getByTargetId(@PathVariable Long targetId) {
        return logService.getByTargetId(targetId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
