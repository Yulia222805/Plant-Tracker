// ui/screens/ReferenceSearchDialog.kt
package com.example.planttracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.ReferencePlant

@Composable
fun ReferenceSearchDialog(
    onDismiss: () -> Unit,
    onPlantSelected: (ReferencePlant) -> Unit // ← убрали Set<String>
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val refPlantDao = db.referencePlantDao()

    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ReferencePlant>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.length >= 2) {
            searchResults = refPlantDao.searchByName(query)
        } else {
            searchResults = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Поиск в справочнике") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Название растения") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn {
                    items(searchResults) { plant ->
                        ListItem(
                            headlineContent = { Text(plant.name) },
                            trailingContent = {
                                Button(onClick = {
                                    onPlantSelected(plant) // ← сразу передаём растение
                                    onDismiss()
                                }) {
                                    Text("Выбрать")
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Отмена") }
        }
    )
}