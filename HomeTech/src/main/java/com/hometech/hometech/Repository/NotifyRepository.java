package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Notify;
import com.hometech.hometech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotifyRepository extends JpaRepository<Notify, Long> {
    
    // Find all notifications for a user, ordered by newest first
    List<Notify> findByUserOrderByCreatedAtDesc(User user);
    
    // Find all notifications for a user by userId
    List<Notify> findByUser_IdOrderByCreatedAtDesc(Long userId);
    
    // Find unread notifications for a user
    List<Notify> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Find unread notifications by userId
    List<Notify> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Count unread notifications for a user
    long countByUserAndIsReadFalse(User user);
    
    // Count unread notifications by userId
    long countByUser_IdAndIsReadFalse(Long userId);
    
    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notify n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(Long userId);
    
    // Delete old read notifications (older than X days)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notify n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    void deleteOldReadNotifications(java.time.LocalDateTime cutoffDate);
}
