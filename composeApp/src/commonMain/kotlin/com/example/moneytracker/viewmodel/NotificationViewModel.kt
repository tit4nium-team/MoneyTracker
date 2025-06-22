import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
        updateUnreadCount()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                val notifications = notificationRepository.getNotifications()
                _notifications.value = notifications
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun updateUnreadCount() {
        viewModelScope.launch {
            try {
                val count = notificationRepository.getUnreadCount()
                _unreadCount.value = count
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                loadNotifications()
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)
                loadNotifications()
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 