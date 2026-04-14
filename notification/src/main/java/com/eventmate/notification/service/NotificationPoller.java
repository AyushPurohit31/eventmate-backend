package com.eventmate.notification.service;

import com.eventmate.notification.model.Notification;
import com.eventmate.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationPoller {

    private static final Logger log = LoggerFactory.getLogger(NotificationPoller.class);

    private final NotificationRepository repository;
    private final NotificationService emailService;

    private final int batchSize = 50;
    private final int maxRetries = 5;
    private final int baseDelaySeconds = 30;

    public NotificationPoller(NotificationRepository repository,
                              NotificationService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollAndProcess() {
        List<Notification> batch = fetchAndMarkProcessing();
        if (batch.isEmpty()) return;
        log.info("Processing {} notifications", batch.size());
        for (Notification n : batch) {
            processSingle(n);
        }
    }

    @Transactional
    public List<Notification> fetchAndMarkProcessing() {
        List<Notification> list = repository.fetchBatchForUpdate(batchSize);
        if (list.isEmpty()) return list;
        List<UUID> ids = list.stream().map(Notification::getId).toList();
        repository.markProcessing(ids);
        return list;
    }

    private void processSingle(Notification n) {
        try {
            emailService.sendEmail(n.getUserEmail(), n.getSubject(), n.getBody());
            repository.markSent(n.getId());
        } catch (Exception ex) {
            int nextRetry = n.getRetryCount() + 1;
            if (nextRetry >= maxRetries) {
                repository.markFailed(n.getId(), ex.getMessage());
                return;
            }
            LocalDateTime nextAttempt = calculateBackoff(nextRetry);
            repository.markForRetry(n.getId(), nextRetry, nextAttempt, ex.getMessage());
        }
    }

    private LocalDateTime calculateBackoff(int retryCount) {
        long delay = (long) Math.pow(2, retryCount) * baseDelaySeconds;
        return LocalDateTime.now().plusSeconds(delay);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckJobs() {
        int count = repository.resetStuckProcessing();
        if (count > 0) {
            log.warn("Recovered {} stuck notifications", count);
        }
    }
}