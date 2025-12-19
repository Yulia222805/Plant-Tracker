// ui/screens/PlantDetailDialog.kt
package com.example.planttracker.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.planttracker.database.Plant
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import com.example.planttracker.ui.components.FullScreenImageDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//@Composable
//fun PlantDetailDialog(
//    plant: Plant,
//    onEditClick: () -> Unit,
//    onDeleteClick: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(plant.name) },
//        text = {
//            androidx.compose.foundation.layout.Column(
//                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
//            ) {
//                if (!plant.description.isNullOrEmpty()) {
//                    Text("Описание: ${plant.description}")
//                }
//                Text("Полив: раз в ${plant.wateringIntervalDays} дней")
//                Text("Удобрение: раз в ${plant.fertilizingIntervalDays} дней")
//                Text("Последний полив: ${formatDate(plant.lastWatered)}")
//                Text("Последнее удобрение: ${formatDate(plant.lastFertilized)}")
//            }
//        },
//        confirmButton = {
//            androidx.compose.foundation.layout.Row {
//                Button(onClick = onEditClick) {
//                    Text("Редактировать")
//                }
//                Button(
//                    onClick = onDeleteClick,
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                ) {
//                    Text("Удалить")
//                }
//            }
//        },
//        dismissButton = {
//            Button(onClick = onDismiss) {
//                Text("Закрыть")
//            }
//        }
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailDialog(
    plant: Plant,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    var showFullScreenImage by remember { mutableStateOf(false) } // ← новое состояние

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ФОТО — кликабельное!
                plant.imagePath?.let { path ->
                    val painter = rememberAsyncImagePainter(
                        model = File(path), // ✅ Используем File
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { showFullScreenImage = true } // ← клик по фото
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Фото ${plant.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Небольшая подсказка — можно добавить значок увеличения
                        if (showFullScreenImage) {
                            // Это не обязательно, просто для UX
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Полный экран",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Основные данные
                DetailRow(label = "Полив", value = "раз в ${plant.wateringIntervalDays} дн.")
                DetailRow(label = "Последний полив", value = sdf.format(Date(plant.lastWatered)))

                DetailRow(label = "Удобрение", value = "раз в ${plant.fertilizingIntervalDays} дн.")
                DetailRow(label = "Последнее удобрение", value = sdf.format(Date(plant.lastFertilized)))

                // Описание, если есть
                plant.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = "Описание:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Редактировать")
                }

                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Удалить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )

    // Полноэкранный просмотр — отдельный диалог
    if (showFullScreenImage && plant.imagePath != null) {
        FullScreenImageViewer(
            imageUrl = plant.imagePath,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

//private fun formatDate(timestamp: Long): String {
//    return java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
//        .format(java.util.Date(timestamp))
//}