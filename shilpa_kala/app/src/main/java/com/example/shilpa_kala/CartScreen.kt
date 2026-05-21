package com.example.shilpa_kala

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onBack: () -> Unit, onCheckout: (Double) -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val portfolioDao = database.portfolioDao()
    val cartItems by portfolioDao.getCartItems().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val totalPrice = cartItems.sumOf { it.price.toDoubleOrNull() ?: 0.0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MY CART", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3B2314)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF3B2314))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF8E1)
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 24.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("₹$totalPrice", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3B2314))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onCheckout(totalPrice) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B2314))
                        ) {
                            Text("PROCEED TO PAY", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFFFF8E1)
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is feeling light!", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    Text("Add some handcrafted magic to it.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                    ) { 
                        Text("Explore Marketplace", color = Color(0xFF3B2314), fontWeight = FontWeight.Bold) 
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onRemove = {
                            scope.launch {
                                portfolioDao.removeFromCart(item.cartId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUri),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color(0xFF3B2314),
                    maxLines = 1
                )
                Text(
                    text = "by ${item.artisanName}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹${item.price}", 
                    color = Color(0xFF3B2314), 
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}
