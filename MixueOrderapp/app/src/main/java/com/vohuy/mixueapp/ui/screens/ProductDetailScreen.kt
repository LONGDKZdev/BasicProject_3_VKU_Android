package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vohuy.mixueapp.data.model.ProductReview
import com.vohuy.mixueapp.ui.components.ToastMessageHandler
import com.vohuy.mixueapp.ui.navigation.Routes
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.ProductReviewViewModel
import com.vohuy.mixueapp.ui.viewmodel.ProductViewModel
import com.vohuy.mixueapp.utils.formatPrice
import com.vohuy.mixueapp.utils.sdp
import java.util.Locale

private data class OptionPrice(val label: String, val extraPrice: Double = 0.0)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel? = null,
    cartViewModel: CartViewModel? = null,
) {
    val productVm = productViewModel ?: viewModel<ProductViewModel>()
    val cartVm = cartViewModel ?: viewModel<CartViewModel>()
    val authVm: AuthViewModel = viewModel()
    val reviewVm: ProductReviewViewModel = viewModel()

    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            productVm.loadProductById(productId)
            reviewVm.listenReviews(productId)
            authVm.fetchCurrentUser()
        }
    }

    val product by productVm.selectedProduct.observeAsState(null)
    val currentUser by authVm.currentUser.observeAsState()
    val reviews by reviewVm.reviews.observeAsState(emptyList())
    val currentUid = authVm.getCurrentUserId()
    val ownReview = reviews.firstOrNull { it.userId == currentUid }

    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf(OptionPrice("M")) }
    var selectedSugar by remember { mutableStateOf("70%") }
    var selectedIce by remember { mutableStateOf("Vừa") }
    var selectedToppings by remember { mutableStateOf(setOf<OptionPrice>()) }
    var note by remember { mutableStateOf("") }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    LaunchedEffect(ownReview?.id, ownReview?.updatedAt) {
        if (ownReview != null) {
            reviewRating = ownReview.rating.coerceIn(1, 5)
            reviewComment = ownReview.comment
        }
    }

    ToastMessageHandler(productVm, reviewVm, authVm, cartVm)

    val sizeOptions = remember {
        listOf(OptionPrice("M"), OptionPrice("L", 5000.0), OptionPrice("XL", 10000.0))
    }
    val sugarOptions = remember { listOf("30%", "50%", "70%", "100%") }
    val iceOptions = remember { listOf("Ít", "Vừa", "Nhiều", "Không đá") }
    val toppingOptions = remember {
        listOf(
            OptionPrice("Trân châu", 5000.0),
            OptionPrice("Thạch dừa", 5000.0),
            OptionPrice("Kem cheese", 8000.0)
        )
    }

    val toppingExtra = selectedToppings.sumOf { it.extraPrice }
    val extraPrice = selectedSize.extraPrice + toppingExtra
    val unitPrice = (product?.price ?: 0.0) + extraPrice
    val itemTotal = unitPrice * quantity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Sản Phẩm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 6.sdp, shadowElevation = 10.sdp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.sdp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantityButton(text = "-", enabled = quantity > 1) { quantity-- }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.sdp)
                        )
                        QuantityButton(text = "+") { quantity++ }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = itemTotal.formatPrice(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(12.sdp))

                    Button(
                        onClick = {
                            val p = product ?: return@Button
                            cartVm.addItem(
                                product = p,
                                quantity = quantity,
                                size = selectedSize.label,
                                sugarLevel = selectedSugar,
                                iceLevel = selectedIce,
                                toppings = selectedToppings.map { it.label }.sorted(),
                                note = note,
                                extraPrice = extraPrice
                            )
                            navController.navigate(Routes.CART)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.sdp),
                        shape = RoundedCornerShape(16.sdp),
                        enabled = product != null
                    ) {
                        Text(
                            "Thêm Vào Giỏ Hàng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = product?.imageUrl.orEmpty(),
                contentDescription = product?.name ?: "Ảnh sản phẩm",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.sdp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.sdp),
                verticalArrangement = Arrangement.spacedBy(16.sdp)
            ) {
                if (product == null) {
                    Text(
                        text = "Đang tải sản phẩm...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = product?.name.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (product?.price ?: 0.0).formatPrice(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RatingSummary(
                        average = product?.ratingAverage ?: 0.0,
                        count = product?.ratingCount ?: 0
                    )
                }

                Text(
                    text = product?.description?.takeIf { it.isNotBlank() }
                        ?: "Chưa có mô tả cho sản phẩm này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                OptionSection("Size") {
                    OptionChips(
                        options = sizeOptions,
                        selected = selectedSize,
                        label = { option ->
                            if (option.extraPrice > 0) "${option.label} +${option.extraPrice.formatPrice()}" else option.label
                        },
                        onSelect = { selectedSize = it }
                    )
                }

                OptionSection("Mức đường") {
                    TextChips(sugarOptions, selectedSugar) { selectedSugar = it }
                }

                OptionSection("Mức đá") {
                    TextChips(iceOptions, selectedIce) { selectedIce = it }
                }

                OptionSection("Topping") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                        toppingOptions.forEach { topping ->
                            val selected = selectedToppings.contains(topping)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedToppings = if (selected) {
                                        selectedToppings - topping
                                    } else {
                                        selectedToppings + topping
                                    }
                                },
                                label = { Text("${topping.label} +${topping.extraPrice.formatPrice()}") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ghi chú cho món") },
                    maxLines = 2
                )

                ReviewComposer(
                    isLoggedIn = !currentUid.isNullOrBlank(),
                    rating = reviewRating,
                    comment = reviewComment,
                    ownReview = ownReview,
                    onRatingChange = { reviewRating = it },
                    onCommentChange = { reviewComment = it },
                    onSubmit = {
                        reviewVm.submitReview(
                            productId = productId,
                            userId = currentUid,
                            userName = currentUser?.fullName.orEmpty(),
                            rating = reviewRating,
                            comment = reviewComment
                        )
                    },
                    onDelete = {
                        reviewVm.deleteOwnReview(productId, currentUid)
                        reviewComment = ""
                        reviewRating = 5
                    }
                )

                ReviewsList(reviews = reviews)

                Spacer(modifier = Modifier.height(120.sdp))
            }
        }
    }
}

@Composable
private fun QuantityButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(38.sdp)
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            imageVector = if (text == "+") Icons.Default.Add else Icons.Default.Remove,
            contentDescription = text
        )
    }
}

@Composable
private fun RatingSummary(average: Double, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.sdp)
        )
        Text(
            text = if (count > 0) {
                String.format(Locale("vi", "VN"), "%.1f (%d)", average, count)
            } else {
                "Chưa có"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OptionSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.sdp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionChips(
    options: List<OptionPrice>,
    selected: OptionPrice,
    label: (OptionPrice) -> String,
    onSelect: (OptionPrice) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TextChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
private fun ReviewComposer(
    isLoggedIn: Boolean,
    rating: Int,
    comment: String,
    ownReview: ProductReview?,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.sdp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.sdp),
            verticalArrangement = Arrangement.spacedBy(10.sdp)
        ) {
            Text(
                if (ownReview == null) "Đánh giá sản phẩm" else "Cập nhật đánh giá của bạn",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (!isLoggedIn) {
                Text(
                    "Bạn cần đăng nhập để bình luận và chấm sao.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { value ->
                    Icon(
                        imageVector = if (value <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$value sao",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.sdp)
                            .clickable { onRatingChange(value) }
                            .padding(2.sdp)
                    )
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bình luận") },
                minLines = 2,
                maxLines = 4
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                Button(onClick = onSubmit) {
                    Text(if (ownReview == null) "Gửi đánh giá" else "Lưu thay đổi")
                }
                if (ownReview != null) {
                    TextButton(onClick = onDelete) {
                        Text("Xóa")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsList(reviews: List<ProductReview>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.sdp)) {
        Text("Bình luận gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (reviews.isEmpty()) {
            Text(
                "Chưa có bình luận nào cho sản phẩm này.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        reviews.forEach { review ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.sdp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.sdp)
            ) {
                Column(modifier = Modifier.padding(12.sdp), verticalArrangement = Arrangement.spacedBy(6.sdp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            review.userName.ifBlank { "Khách hàng" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.sdp)
                                )
                            }
                        }
                    }
                    Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
