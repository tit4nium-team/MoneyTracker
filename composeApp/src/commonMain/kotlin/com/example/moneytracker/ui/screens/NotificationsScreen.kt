import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import moneytracker.composeapp.generated.resources.Res
import moneytracker.composeapp.generated.resources.ic_arrow_back
import moneytracker.composeapp.generated.resources.ic_assessment
import moneytracker.composeapp.generated.resources.ic_attach_money
import moneytracker.composeapp.generated.resources.ic_emoji_events
import moneytracker.composeapp.generated.resources.ic_error
import moneytracker.composeapp.generated.resources.ic_event
import moneytracker.composeapp.generated.resources.ic_lightbulb
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma notificação",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onDismiss = { viewModel.deleteNotification(notification.id) },
                        onClick = {
                            notification.actionRoute?.let { route ->
                                onNavigateToRoute(route)
                            }
                            viewModel.markAsRead(notification.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: Notification,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(when (notification.type) {
                            NotificationType.BUDGET_ALERT -> Res.drawable.ic_error
                            NotificationType.SPENDING_INSIGHT -> Res.drawable.ic_assessment
                            NotificationType.SAVING_GOAL -> Res.drawable.ic_attach_money
                            NotificationType.BILL_REMINDER -> Res.drawable.ic_event
                            NotificationType.ACHIEVEMENT -> Res.drawable.ic_emoji_events
                            NotificationType.TIP -> Res.drawable.ic_lightbulb
                            // else -> Res.drawable.ic_info // Default case for exhaustiveness
                        }),
                        contentDescription = null,
                        tint = when (notification.type) {
                            NotificationType.BUDGET_ALERT -> MaterialTheme.colorScheme.error
                            NotificationType.ACHIEVEMENT -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface // Default tint
                        }
                    )
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!notification.isRead) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (notification.actionRoute != null) {
                TextButton(
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Ver Mais")
                }
            }
        }
    }
} 