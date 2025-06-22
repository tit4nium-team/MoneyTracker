interface NotificationRepository {
    suspend fun getNotifications(): List<Notification>
    suspend fun markAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun createNotification(notification: Notification)
    suspend fun getUnreadCount(): Int
} 