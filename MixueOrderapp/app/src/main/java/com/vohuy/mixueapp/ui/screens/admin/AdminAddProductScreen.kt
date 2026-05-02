package com.vohuy.mixueapp.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.data.model.Product
import com.vohuy.mixueapp.ui.viewmodel.ProductViewModel
import com.vohuy.mixueapp.utils.readBytes

/**
 * Phase 2 (Option A): Admin screen to create a product and upload its main image to Supabase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel? = null,
) {
    val vm = viewModel ?: viewModel<ProductViewModel>()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val isLoading by vm.isLoading.observeAsState(false)
    val errorMessage by vm.errorMessage.observeAsState()
    val successMessage by vm.successMessage.observeAsState()
    val createdProductId by vm.createdProductId.observeAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri
            selectedImageBytes = try {
                uri?.readBytes(context)
            } catch (_: Exception) {
                null
            }
        }
    )

    LaunchedEffect(errorMessage) {
        val msg = errorMessage
        if (!msg.isNullOrBlank()) snackbarHostState.showSnackbar(msg)
    }

    LaunchedEffect(successMessage, createdProductId) {
        val msg = successMessage
        if (!msg.isNullOrBlank()) snackbarHostState.showSnackbar(msg)

        // After successful creation, you can go back or keep editing.
        // For now, navigate back if productId exists.
        if (!createdProductId.isNullOrBlank()) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin - Thêm sản phẩm") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tạo sản phẩm mới (ảnh lưu Supabase)",
                style = MaterialTheme.typography.titleMedium
            )

            MixueTextField(
                value = name,
                onValueChange = { name = it },
                label = "Tên sản phẩm",
            )

            MixueTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả",
                singleLine = false,
            )

            MixueTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = "Giá (VD: 35000)",
            )

            MixueTextField(
                value = category,
                onValueChange = { category = it },
                label = "Danh mục (Kem/Trà Sữa/Nước)",
            )

            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (selectedImageUri == null) "Chọn ảnh" else "Đổi ảnh")
            }

            Text(
                text = if (selectedImageBytes != null) "✅ Đã chọn ảnh" else "❗ Chưa chọn ảnh",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val bytes = selectedImageBytes
                    if (bytes == null) {
                        // show snackbar via vm error channel
                        vm.setError("Vui lòng chọn ảnh trước")
                        return@Button
                    }
                    if (name.isBlank()) {
                        vm.setError("Tên sản phẩm không được để trống")
                        return@Button
                    }
                    if (category.isBlank()) {
                        vm.setError("Danh mục không được để trống")
                        return@Button
                    }

                    vm.createProductWithImage(
                        product = Product(
                            name = name.trim(),
                            description = description.trim(),
                            price = price,
                            category = category.trim(),
                            available = available,
                        ),
                        imageBytes = bytes,
                        contentType = "image/jpeg",
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Đang tạo..." else "Tạo sản phẩm")
            }
        }
    }
}

@Composable
private fun MixueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
    )
}

