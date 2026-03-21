package com.cardioo.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.presentation.chart.ChartScreen
import com.cardioo.presentation.readings.ReadingsScreen
import com.cardioo.presentation.statistics.StatisticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onOpenEntry: (measurementIdOrNull: Long?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenManageAccounts: () -> Unit,
    vm: MainViewModel = hiltViewModel(),
) {
    var tab by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    val accountState by vm.state.collectAsState()
    val currentAccount = accountState.accounts.firstOrNull { it.id == accountState.currentAccountId }
    val currentName = currentAccount?.name ?: "Account"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            0 -> "Readings"
                            1 -> "Statistics"
                            else -> "Chart"
                        },
                    )
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.Group, contentDescription = "Accounts")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Current: $currentName") },
                            onClick = { menuExpanded = false },
                            leadingIcon = {
                                AccountAvatar(
                                    name = currentName,
                                    background = avatarColor(currentName),
                                )
                            },
                        )
                        // Show switch targets only; current account is already shown above.
                        accountState.accounts
                            .filter { it.id != accountState.currentAccountId }
                            .forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.name) },
                                    onClick = {
                                        vm.switchAccount(account.id)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        AccountAvatar(
                                            name = account.name,
                                            background = avatarColor(account.name),
                                        )
                                    },
                                )
                            }
                        DropdownMenuItem(
                            text = { Text("Create account") },
                            onClick = {
                                menuExpanded = false
                                showCreateDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Manage accounts") },
                            onClick = {
                                menuExpanded = false
                                onOpenManageAccounts()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Edit current profile") },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    onClick = { onOpenEntry(null) },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.ListAlt, contentDescription = null) },
                    label = { Text("Readings") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Insights, contentDescription = null) },
                    label = { Text("Statistics") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
                    label = { Text("Chart") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> ReadingsScreen(contentPadding = padding, onEdit = { onOpenEntry(it) })
            1 -> StatisticsScreen(contentPadding = padding)
            else -> ChartScreen(contentPadding = padding)
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create account") },
            text = {
                OutlinedTextField(
                    value = createName,
                    onValueChange = { createName = it },
                    label = { Text("Account name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.createAccount(createName)
                        createName = ""
                        showCreateDialog = false
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AccountAvatar(
    name: String,
    background: Color,
) {
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val contentColor = if (background.luminance() > 0.6f) Color.Black else Color.White
    androidx.compose.material3.Surface(
        color = background,
        shape = androidx.compose.foundation.shape.CircleShape,
    ) {
        Text(
            text = initials.ifBlank { "A" },
            color = contentColor,
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun avatarColor(seed: String): Color {
    val palette = listOf(
        Color(0xFFFF6B8B),
        Color(0xFF7E57C2),
        Color(0xFF26A69A),
        Color(0xFF42A5F5),
        Color(0xFFFFA726),
        Color(0xFFEC407A),
    )
    val idx = kotlin.math.abs(seed.hashCode()) % palette.size
    return palette[idx]
}

