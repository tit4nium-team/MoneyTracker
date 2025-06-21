import androidx.compose.runtime.remember
import com.example.moneytracker.data.AuthRepository
import com.example.moneytracker.data.BudgetRepository
import com.example.moneytracker.data.TransactionRepository
import com.example.moneytracker.model.Notification
import com.example.moneytracker.model.NotificationType
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.toCurrencyString
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend fun checkAndGenerateNotifications() {
        checkBudgetAlerts()
        checkSpendingInsights()
        generateFinancialTips()
        checkAchievements()
    }

    private suspend fun checkBudgetAlerts() {
        val userId = authRepository.getCurrentUserId().orEmpty()
        val currentDate =
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.month.name} ${it.year}" }.toLocalDateTime()

        val budgets = budgetRepository.getBudgets(
            userId = userId.orEmpty(),
            month = currentDate.monthNumber,
            year = currentDate.year
        ).first()

        budgets.forEach { budget ->
            val transactions =
                transactionRepository
                    .getTransactionsByCategory(userId,budget.category)
                    .onSuccess {
                        val spent = it.sumOf { it.amount }
                        val percentageUsed = (spent / budget.amount) * 100

                        when {
                            percentageUsed >= 90 -> {
                                createNotification(
                                    title = "Alerta de Orçamento!",
                                    message = "Você já usou 90% do orçamento de ${budget.category.name}",
                                    type = NotificationType.BUDGET_ALERT,
                                    actionRoute = "budget"
                                )
                            }

                            percentageUsed >= 80 -> {
                                createNotification(
                                    title = "Atenção ao Orçamento",
                                    message = "Você já usou 80% do orçamento de ${budget.category.name}",
                                    type = NotificationType.BUDGET_ALERT,
                                    actionRoute = "budget"
                                )
                            }
                        }
                    }

        }
    }

    private suspend fun checkSpendingInsights() {
        val userId = authRepository.getCurrentUserId().orEmpty()

        val transactions = transactionRepository.getTransactionsFlow(userId).first()
        val currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

        // Filtrar transações do mês atual
        val monthlyTransactions = transactions.filter { transaction ->
            val transactionDate =
                transaction.date.toLocalDateTime()
            transactionDate.month == currentMonth && transactionDate.year == currentYear
        }

        // Calcular gastos por categoria
        val expensesByCategory = monthlyTransactions
            .filter { transaction -> transaction.type == TransactionType.EXPENSE }
            .groupBy { transaction -> transaction.category }
            .mapValues { (_, transactions) -> transactions.sumOf { transaction -> transaction.amount } }

        // Identificar categoria com maior gasto
        expensesByCategory.maxByOrNull { (_, amount) -> amount }?.let { (category, amount) ->
            createNotification(
                title = "Análise de Gastos",
                message = "Sua maior despesa este mês foi com ${category.name}: R$ ${amount.toCurrencyString()}",
                type = NotificationType.SPENDING_INSIGHT
            )
        }
    }

    private suspend fun generateFinancialTips() {
        val tips = listOf(
            "Que tal começar a poupar? Separe 10% da sua renda todo mês.",
            "Revise suas assinaturas mensais. Você realmente usa todas?",
            "Compare preços antes de comprar. Pode economizar bastante!",
            "Estabeleça metas financeiras claras para o mês.",
            "Mantenha um fundo de emergência com 6 meses de despesas.",
            "Evite compras por impulso. Espere 24h antes de decidir.",
            "Priorize pagar dívidas com juros altos.",
            "Anote todos os seus gastos, até os pequenos.",
            "Pesquise alternativas mais econômicas para seu lazer.",
            "Faça um planejamento mensal de gastos."
        )

        // Seleciona uma dica aleatória
        val randomTip = tips.random()

        createNotification(
            title = "Dica Financeira",
            message = randomTip,
            type = NotificationType.TIP
        )
    }

    private suspend fun checkAchievements() {
        val userId = authRepository.getCurrentUserId().orEmpty()
        val transactions = transactionRepository.getTransactionsFlow(userId).first()

        // Verifica se o usuário economizou este mês
        val currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

        val monthlyTransactions = transactions.filter { transaction ->
            val transactionDate =
                transaction.date.toLocalDateTime()
            transactionDate.month == currentMonth && transactionDate.year == currentYear
        }

        val income = monthlyTransactions
            .filter { transaction -> transaction.type == TransactionType.INCOME }
            .sumOf { transaction -> transaction.amount }

        val expenses = monthlyTransactions
            .filter { transaction -> transaction.type == TransactionType.EXPENSE }
            .sumOf { transaction -> transaction.amount }

        val savings = income - expenses

        if (savings > 0) {
            createNotification(
                title = "Parabéns! 🎉",
                message = "Você economizou R$ ${savings.toCurrencyString()} este mês!",
                type = NotificationType.ACHIEVEMENT
            )
        }
    }

    private suspend fun createNotification(
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String? = null
    ) {
        val notification = Notification(
            title = title,
            message = message,
            type = type,
            actionRoute = actionRoute
        )
        notificationRepository.createNotification(notification)
    }
} 