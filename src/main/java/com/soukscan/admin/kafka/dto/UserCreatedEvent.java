package com.soukscan.admin.kafka.dto;

/**
 * Event triggered when a user is created.
 */
public class UserCreatedEvent extends KafkaEvent {
    private Long userId;
    private String username;
    private String email;
    private String userType; // CUSTOMER, VENDOR, ADMIN

    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(Long userId, String username, String email, String userType) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.userType = userType;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
