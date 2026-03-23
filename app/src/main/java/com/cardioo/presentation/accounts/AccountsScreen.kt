package com.cardioo.presentation.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.presentation.util.heightUnitString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onEditCurrent: () -> Unit,
    vm: AccountsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var accountPendingDeleteId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_manage_accounts)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.accounts, key = { it.id }) { account ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (account.id == state.currentId) {
                                account.name + stringResource(R.string.account_current_suffix)
                            } else {
                                account.name
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        AccountAvatar(
                            name = account.name,
                            background = avatarColor(account.name),
                        )
                        Text(
                            stringResource(
                                R.string.account_height_format,
                                account.height.toString(),
                                heightUnitString(account.heightUnit),
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { vm.switchTo(account.id) }) {
                                Text(stringResource(R.string.action_switch))
                            }
                            OutlinedButton(
                                onClick = {
                                    vm.switchTo(account.id)
                                    onEditCurrent()
                                },
                            ) { Text(stringResource(R.string.action_edit_profile)) }
                            OutlinedButton(
                                onClick = { accountPendingDeleteId = account.id },
                                enabled = state.accounts.size > 1,
                            ) { Text(stringResource(R.string.action_delete)) }
                        }
                    }
                }
            }
        }
    }

    val accountToDelete = state.accounts.firstOrNull { it.id == accountPendingDeleteId }
    if (accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountPendingDeleteId = null },
            title = { Text(stringResource(R.string.title_delete_account)) },
            text = {
                Text(stringResource(R.string.delete_account_message, accountToDelete.name))
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.delete(accountToDelete.id)
                        accountPendingDeleteId = null
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { accountPendingDeleteId = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
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
