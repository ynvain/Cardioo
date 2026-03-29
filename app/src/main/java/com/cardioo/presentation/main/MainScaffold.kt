package com.cardioo.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.presentation.chart.ChartScreen
import com.cardioo.presentation.readings.ReadingsScreen
import com.cardioo.presentation.readings.ReadingsViewModel
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
    val readingsVm: ReadingsViewModel = hiltViewModel()
    val readingsState by readingsVm.state.collectAsState()
    val currentAccount = accountState.accounts.firstOrNull { it.id == accountState.currentAccountId }
    val defaultAccountName = stringResource(R.string.account_default_name)
    val currentName = currentAccount?.name ?: defaultAccountName

    LaunchedEffect(tab) {
        if (tab != 0) readingsVm.clearSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when (tab) {
                                0 -> R.string.title_readings
                                1 -> R.string.title_statistics
                                else -> R.string.title_chart
                            },
                        ),
                    )
                },
                actions = {
                    if (tab == 0 && readingsState.selectedIds.isNotEmpty()) {
                        IconButton(onClick = readingsVm::clearSelection) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_readings_cancel_selection),
                            )
                        }
                        IconButton(onClick = readingsVm::deleteSelected) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.cd_readings_delete_selected),
                            )
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        AccountAvatar(
                            name = currentName,
                            background = avatarColor(currentName),
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.account_current_format, currentName)) },
                            onClick = { menuExpanded = false },
                            leadingIcon = {
                                AccountAvatar(
                                    name = currentName,
                                    background = avatarColor(currentName),
                                )
                            },
                        )
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
                            text = { Text(stringResource(R.string.menu_create_account)) },
                            onClick = {
                                menuExpanded = false
                                showCreateDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_manage_accounts)) },
                            onClick = {
                                menuExpanded = false
                                onOpenManageAccounts()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_edit_current_profile)) },
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
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.ListAlt, contentDescription = null) },
                    label = { Text(stringResource(R.string.cd_nav_readings)) },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Insights, contentDescription = null) },
                    label = { Text(stringResource(R.string.cd_nav_statistics)) },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
                    label = { Text(stringResource(R.string.cd_nav_chart)) },
                )
            }
        },
    ) { padding ->
        when (tab) {
            0 -> ReadingsScreen(
                contentPadding = padding,
                onEdit = { onOpenEntry(it) },
                vm = readingsVm,
            )
            1 -> StatisticsScreen(contentPadding = padding)
            else -> ChartScreen(contentPadding = padding)
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.title_create_account)) },
            text = {
                OutlinedTextField(
                    value = createName,
                    onValueChange = { createName = it },
                    label = { Text(stringResource(R.string.label_account_name)) },
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
                ) { Text(stringResource(R.string.action_create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun AccountAvatar(
    name: String,
    background: Color,
) {
    val initialsFallback = stringResource(R.string.avatar_initials_fallback)
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val contentColor = if (background.luminance() > 0.6f) Color.Black else Color.White
    androidx.compose.material3.Surface(
        color = background,
        shape = androidx.compose.foundation.shape.CircleShape,
    ) {
        Text(
            text = initials.ifBlank { initialsFallback },
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
