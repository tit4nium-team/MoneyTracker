data class Notification(
    val id: String = "",
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionRoute: String? = null
)

enum class NotificationType {
    BUDGET_ALERT,      // Alertas de orçamento próximo do limite
    SPENDING_INSIGHT,  // Insights sobre gastos
    SAVING_GOAL,      // Metas de economia
    BILL_REMINDER,    // Lembretes de contas
    ACHIEVEMENT,      // Conquistas e marcos
    TIP              // Dicas financeiras
} 