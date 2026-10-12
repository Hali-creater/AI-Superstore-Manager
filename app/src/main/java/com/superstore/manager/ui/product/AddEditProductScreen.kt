package com.superstore.manager.ui.product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.UnitType
import com.superstore.manager.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: AddEditProductViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isSaved by viewModel.isSaved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val nameEn by viewModel.nameEn.collectAsState()
    val nameUr by viewModel.nameUr.collectAsState()
    val brand by viewModel.brand.collectAsState()
    val category by viewModel.category.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val openingStock by viewModel.openingStock.collectAsState()
    val costPrice by viewModel.costPrice.collectAsState()
    val sellingPrice by viewModel.sellingPrice.collectAsState()
    val minThreshold by viewModel.minThreshold.collectAsState()
    val barcode by viewModel.barcode.collectAsState()
    val customAliases by viewModel.customAliases.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.imageUri.value = uri.toString()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_add)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(36.dp))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(stringResource(R.string.action_pick_photo))
                    }
                    if (imageUri != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.identifyProductWithAi(context, "") }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.action_ai_identify))
                            }
                            OutlinedButton(onClick = { viewModel.imageUri.value = null }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                value = nameEn,
                onValueChange = { viewModel.nameEn.value = it },
                label = { Text(stringResource(R.string.product_name_en)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = nameUr,
                onValueChange = { viewModel.nameUr.value = it },
                label = { Text(stringResource(R.string.product_name_ur) + " (مثلاً: چینی / آٹا)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { viewModel.brand.value = it },
                    label = { Text(stringResource(R.string.product_brand)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { viewModel.category.value = it },
                    label = { Text(stringResource(R.string.product_category)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text(
                text = stringResource(R.string.product_unit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitType.entries.forEach { unit ->
                    FilterChip(
                        selected = selectedUnit == unit,
                        onClick = { viewModel.selectedUnit.value = unit },
                        label = { Text(unit.name) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = openingStock,
                    onValueChange = { viewModel.openingStock.value = it },
                    label = { Text(stringResource(R.string.product_opening_stock)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minThreshold,
                    onValueChange = { viewModel.minThreshold.value = it },
                    label = { Text(stringResource(R.string.product_min_threshold)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { viewModel.costPrice.value = it },
                    label = { Text(stringResource(R.string.product_cost_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { viewModel.sellingPrice.value = it },
                    label = { Text(stringResource(R.string.product_selling_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            val qty = UnitConverter.parseBigDecimal(openingStock)
            val cost = UnitConverter.parseBigDecimal(costPrice)
            val sell = UnitConverter.parseBigDecimal(sellingPrice)
            val initialCostVal = UnitConverter.calculateTotalCost(qty, cost)
            val potentialSalesVal = UnitConverter.calculateSalesValue(qty, sell)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Valuation Summary / ابتدائی مالیت:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "• Initial Stock Cost = Rs. $initialCostVal")
                    Text(text = "• Potential Sales Value = Rs. $potentialSalesVal")
                }
            }

            OutlinedTextField(
                value = customAliases,
                onValueChange = { viewModel.customAliases.value = it },
                label = { Text("Local Aliases / Nicknames (Comma separated: چینی, sugar)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = barcode,
                onValueChange = { viewModel.barcode.value = it },
                label = { Text(stringResource(R.string.product_barcode)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveProduct() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_save), fontSize = 16.sp)
            }
        }
    }
}
