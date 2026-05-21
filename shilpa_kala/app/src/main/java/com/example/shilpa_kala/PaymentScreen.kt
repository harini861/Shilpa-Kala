package com.example.shilpa_kala

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PaymentScreen(totalAmount: Double, onPaymentSuccess: () -> Unit) {
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500) // Simulate processing
        isProcessing = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B2314), Color(0xFF8D6E63))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color(0xFFD4AF37), 
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "PROCESSING PAYMENT", 
                    color = Color.White, 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    "Securely connecting to bank...", 
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = Color(0xFF4CAF50)
                        ) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.padding(20.dp).size(48.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "PAYMENT RECEIVED", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Color(0xFF2E7D32)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ID", color = Color.Gray, fontSize = 14.sp)
                            Text("#SK${System.currentTimeMillis().toString().takeLast(8)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Paid", color = Color.Gray, fontSize = 14.sp)
                            Text("₹$totalAmount", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3B2314), fontSize = 18.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF8E1)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "A confirmation email has been sent to your registered ID.",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF3B2314)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onPaymentSuccess,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B2314))
                        ) {
                            Text("CONTINUE SHOPPING", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}
