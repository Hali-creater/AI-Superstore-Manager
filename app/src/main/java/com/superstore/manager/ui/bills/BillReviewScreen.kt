package com.superstore.manager.ui.bills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.BillImportEntity
import com.superstore.manager.data.local.entity.BillImportLineEntity
import com.superstore.manager.data.local.entity.BillStatus
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.BillImportRepository
import com.superstore.manager.data.repository.ProductRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReviewScreen(
    billId: Long,
    billRepository: BillImportRepository,
    productRepository: ProductRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var bill by remember { mutableStateOf<BillImportEntity?>(null) }
    var lines by remember { mutableStateOf<List<BillImportLineEntity>>(emptyList()) }
    val allProducts by productRepository.getAllActiveProducts().collectAsState(initial = emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(billId) {
        bill = billRepository.getBillById(billId)
        lines = billRepository.getBillLines(billId)
    }

    val currentBill = bill

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bill_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentBill == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val isConfirmed = currentBill.status == BillStatus.CONFIRMED

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(text = errorMessage!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                if (successMessage != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text(text = successMessage!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Bill #${currentBill.id} Status: ${currentBill.status.name}", fontWeight = FontWeight.Bold)
                        Text(text = "Review extracted lines below before confirming stock deduction.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lines, key = { it.id }) { line ->
                        BillLineRow(
                            line = line,
                            allProducts = allProducts,
                            isEditable = !isConfirmed,
                            onProductSelected = { pId ->
                                coroutineScope.launch {
                                    billRepository.updateBillLineMatch(line, pId)
                                    lines = billRepository.getBillLines(billId)
                                }
                            }
                        )
                    }
                }

                if (!isConfirmed) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = billRepository.confirmAndDeductBillAtomically(billId)
                                result.onSuccess {
                                    successMessage = "Bill confirmed! Stock deducted and sale recorded."
                                    bill = billRepository.getBillById(billId)
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to confirm bill"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.bill_confirm_all), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillLineRow(
    line: BillImportLineEntity,
    allProducts: List<ProductEntity>,
    isEditable: Boolean,
    onProductSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val matched = allProducts.firstOrNull { it.id == line.matchedProductId }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (line.isResolved) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Original: ${line.originalText}", fontWeight = FontWeight.Bold)
                    Text(text = "Extracted Qty: ${line.quantity} ${line.unit.name.lowercase()}")
                }

                if (!line.isResolved) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditable) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = matched?.let { "${it.nameEn} (${it.nameUr})" } ?: "Select Matched Product",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allProducts.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nameEn} (${p.nameUr})") },
                                onClick = {
                                    onProductSelected(p.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Matched: ${matched?.nameEn ?: "Unresolved"}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
