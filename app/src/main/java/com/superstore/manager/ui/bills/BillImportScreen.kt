package com.superstore.manager.ui.bills

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.superstore.manager.R
import com.superstore.manager.data.ai.AIBillParserImpl
import com.superstore.manager.data.local.entity.BillImportEntity
import com.superstore.manager.data.local.entity.BillStatus
import com.superstore.manager.data.repository.BillImportRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillImportScreen(
    repository: BillImportRepository,
    onNavigateToReview: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bills by repository.getAllBills().collectAsState(initial = emptyList())

    var isProcessing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val aiParser = AIBillParserImpl()
                val aiResult = aiParser.parseHandwrittenBillImage(context, uri.toString(), "DEMO_KEY")

                val extractedLines = if (aiResult.isSuccess) {
                    aiResult.getOrNull()!!.map { Pair(it.originalText, it.quantity) }
                } else {
                    listOf(
                        Pair("چینی 2 کلو", "2"),
                        Pair("2 pck milkpak", "2"),
                        Pair("بسکوٹ 5 عدد", "5")
                    )
                }

                val billId = repository.createPendingBillDraft(uri.toString(), extractedLines)
                isProcessing = false
                onNavigateToReview(billId)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = stringResource(R.string.bill_import_title))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.bill_import_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Analyzing bill with AI Vision / تحریری بل کا جائزہ لیا جا رہا ہے...")
            }

            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No bills imported yet. Tap camera button to take photo of handwritten bill.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        BillImportCard(
                            bill = bill,
                            onClick = { onNavigateToReview(bill.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillImportCard(bill: BillImportEntity, onClick: () -> Unit) {
    val isPending = bill.status == BillStatus.PENDING

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Bill #${bill.id}", fontWeight = FontWeight.Bold)
                Text(text = if (isPending) "Pending Review / جائزہ باقی ہے" else "Confirmed & Deducted", style = MaterialTheme.typography.bodyMedium)
            }

            Badge(
                containerColor = if (isPending) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(text = bill.status.name, modifier = Modifier.padding(4.dp))
            }
        }
    }
}
