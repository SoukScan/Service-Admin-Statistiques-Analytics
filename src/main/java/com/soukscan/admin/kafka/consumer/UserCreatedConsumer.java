package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.entity.UserStats;
import com.soukscan.admin.kafka.dto.UserCreatedEvent;
import com.soukscan.admin.repository.UserStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for user-created events.
 * Initializes user statistics when a new user is created.
 */
@Service
public class UserCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserCreatedConsumer.class);

    private final UserStatsRepository userStatsRepository;

    @Autowired
    public UserCreatedConsumer(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    @KafkaListener(topics = "${kafka.topics.userCreated}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(UserCreatedEvent event) {
        logger.info("Received user created event: eventId={}, userId={}, username={}, userType={}",
                event.getEventId(), event.getUserId(), event.getUsername(), event.getUserType());
        try {
            // Initialize user stats for new user
            if (!userStatsRepository.existsByUserId(event.getUserId())) {
                UserStats userStats = new UserStats();
                userStats.setUserId(event.getUserId());
                userStats.setTotalReportsSubmitted(0);
                userStats.setTotalValidReports(0);
                userStats.setTotalRejectedReports(0);
                userStats.setWarningCount(0);
                userStatsRepository.save(userStats);
                logger.info("User stats initialized successfully: eventId={}, userId={}", 
                        event.getEventId(), event.getUserId());
            } else {
                logger.warn("User stats already exist for userId={}", event.getUserId());
            }
        } catch (Exception ex) {
            logger.error("Error processing user created event: eventId={}, userId={}, error={}",
                    event.getEventId(), event.getUserId(), ex.getMessage(), ex);
        }
    }
}
