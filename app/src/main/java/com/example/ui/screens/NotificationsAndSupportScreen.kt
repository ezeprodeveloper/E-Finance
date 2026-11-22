package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RwandaReferenceData
import com.example.data.local.NotificationEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.security.SecurityUtils
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Emerald600
import com.example.ui.theme.RoyalBlue600

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onMarkAllRead: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ModuleTopBar("Notification Center", "${notifications.count { !it.isRead }} unread alerts", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notifications.isNotEmpty()) {
                item {
                    OutlinedButton(onClick = onMarkAllRead, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mark All Notifications Read")
                    }
                }
            }
            if (notifications.isEmpty()) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = RoyalBlue600)
                            Text("No notifications yet.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { n ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusBadge(if (n.isRead) "READ" else "NEW")
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(n.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("${n.category} • ${SecurityUtils.formatKigaliDateTime(n.createdAt)}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportCenterScreen(
    tickets: List<SupportTicketEntity>,
    onStoreScreenshot: (Uri) -> String?,
    onCreateTicket: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var category by remember { mutableStateOf(RwandaReferenceData.supportCategories.first()) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var screenshotPath by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreScreenshot(it)?.let { p -> screenshotPath = p } }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ModuleTopBar("Customer Support Center", "Kigali Member Assistance", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = RoyalBlue600)
                        Text("Submit Support Request", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    DropdownSelectorField("Support Category", category, RwandaReferenceData.supportCategories, { category = it })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Explain Your Issue *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (screenshotPath.isBlank()) "Attach Supporting Screenshot (Optional)" else "Screenshot Attached ✓")
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onCreateTicket(category, subject, description, screenshotPath)
                            subject = ""
                            description = ""
                            screenshotPath = ""
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_support_ticket_btn")
                    ) {
                        Text("Submit Support Ticket")
                    }
                }
            }

            item { Text("Your Support Tickets (${tickets.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(tickets, key = { it.id }) { t ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("[${t.category}] ${t.subject}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Assigned: ${t.assignedAgent}", style = MaterialTheme.typography.labelMedium)
                        }
                        StatusBadge(t.status)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(t.description, style = MaterialTheme.typography.bodyMedium)
                    if (t.adminReply.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Admin Response: ${t.adminReply}", style = MaterialTheme.typography.bodyMedium, color = Emerald600, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
