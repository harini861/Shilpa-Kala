package com.example.shilpa_kala

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shilpa_kala.ui.theme.Shilpa_kalaTheme
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Color as ComposeColor

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            Shilpa_kalaTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ComposeColor(0xFFFFF8E1)
                ) {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(onLoginSuccess = { 
                                navController.navigate("welcome") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("welcome") {
                            WelcomeScreen(
                                onArtisanMode = { navController.navigate("camera") },
                                onBuyerMode = { navController.navigate("marketplace") }
                            )
                        }
                        composable("camera") {
                            ShilpaKalaApp(
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            HistoryScreen(onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                        composable("marketplace") {
                            MarketplaceScreen(
                                onBack = { navController.popBackStack() },
                                onCartClick = { navController.navigate("cart") },
                                onProductClick = { product ->
                                    navController.navigate("product_details/${product.id}")
                                }
                            )
                        }
                        composable("cart") {
                            CartScreen(
                                onBack = { navController.popBackStack() },
                                onCheckout = { total -> 
                                    navController.navigate("payment/${total.toFloat()}")
                                }
                            )
                        }
                        composable(
                            route = "product_details/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                            ProductDetailsScreen(
                                productId = productId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "payment/{amount}",
                            arguments = listOf(navArgument("amount") { type = NavType.FloatType })
                        ) { backStackEntry ->
                            val amount = backStackEntry.arguments?.getFloat("amount") ?: 0f
                            PaymentScreen(
                                totalAmount = amount.toDouble(),
                                onPaymentSuccess = {
                                    navController.navigate("marketplace") {
                                        popUpTo("marketplace") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShilpaKalaApp(onNavigateToHistory: () -> Unit, onNavigateToProfile: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Studio", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ComposeColor(0xFF3B2314),
                    titleContentColor = ComposeColor.White,
                    navigationIconContentColor = ComposeColor.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                CameraScreen(onNavigateToHistory, onNavigateToProfile)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen(onNavigateToHistory: () -> Unit, onNavigateToProfile: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val database = remember { AppDatabase.getDatabase(context) }
    val profile by database.portfolioDao().getProfile().collectAsState(initial = null)

    val imageCapture = remember { 
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build() 
    }
    val previewView = remember { PreviewView(context) }

    var productName by remember { mutableStateOf("") }
    var artisanName by remember { mutableStateOf("") }
    var woodType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(profile) {
        profile?.let {
            artisanName = it.name
            woodType = it.craftType
        }
    }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("ShilpaKala", "Use case binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Subtle Camera Frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val color = ComposeColor(0xFFD4AF37)
            val margin = 40.dp.toPx()
            val len = 30.dp.toPx()
            
            // Corners
            // Top Left
            drawLine(color, androidx.compose.ui.geometry.Offset(margin, margin + 140.dp.toPx()), androidx.compose.ui.geometry.Offset(margin + len, margin + 140.dp.toPx()), strokeWidth)
            drawLine(color, androidx.compose.ui.geometry.Offset(margin, margin + 140.dp.toPx()), androidx.compose.ui.geometry.Offset(margin, margin + 140.dp.toPx() + len), strokeWidth)
            
            // Bottom Right
            drawLine(color, androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin - 220.dp.toPx()), androidx.compose.ui.geometry.Offset(size.width - margin - len, size.height - margin - 220.dp.toPx()), strokeWidth)
            drawLine(color, androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin - 220.dp.toPx()), androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin - 220.dp.toPx() - len), strokeWidth)
        }

        // Top Navigation Buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = onNavigateToProfile,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ComposeColor(0xFF3B2314).copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = ComposeColor.White)
            }
            FilledIconButton(
                onClick = onNavigateToHistory,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ComposeColor(0xFF3B2314).copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.History, contentDescription = "History", tint = ComposeColor.White)
            }
        }

        // Details Input Card
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ComposeColor.White.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CRAFT BRANDING", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = ComposeColor(0xFFD4AF37))
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = productName,
                    onValueChange = { productName = it },
                    placeholder = { Text("What are you making?") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = ComposeColor.Transparent, unfocusedContainerColor = ComposeColor.Transparent),
                    singleLine = true
                )
                TextField(
                    value = artisanName,
                    onValueChange = { artisanName = it },
                    placeholder = { Text("Artisan Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = ComposeColor.Transparent, unfocusedContainerColor = ComposeColor.Transparent),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = woodType,
                        onValueChange = { woodType = it },
                        placeholder = { Text("Material") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(focusedContainerColor = ComposeColor.Transparent, unfocusedContainerColor = ComposeColor.Transparent),
                        singleLine = true
                    )
                    TextField(
                        value = price,
                        onValueChange = { price = it },
                        placeholder = { Text("Price ₹") },
                        modifier = Modifier.weight(0.7f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = ComposeColor.Transparent, unfocusedContainerColor = ComposeColor.Transparent),
                        singleLine = true
                    )
                }
            }
        }

        // Capture Button
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)) {
            Button(
                onClick = {
                    captureAndProcessImage(context, imageCapture, productName, artisanName, woodType, price) { uri, bitmap ->
                        lastSavedUri = uri
                        capturedBitmap = bitmap
                        // Save to database
                        scope.launch {
                            val db = AppDatabase.getDatabase(context)
                            db.portfolioDao().insert(
                                PortfolioItem(
                                    productName = productName.ifBlank { "Traditional Craft" },
                                    artisanName = artisanName.ifBlank { "Master Artisan" },
                                    material = woodType.ifBlank { "Handcrafted" },
                                    price = price.ifBlank { "0" },
                                    imageUri = uri.toString()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.size(85.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFD4AF37)),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(40.dp), tint = ComposeColor(0xFF3B2314))
            }
        }

        // --- PREVIEW DIALOG ---
        if (capturedBitmap != null) {
            Dialog(
                onDismissRequest = { capturedBitmap = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(ComposeColor.Black.copy(alpha = 0.95f))) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .align(Alignment.Center)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                    ) {
                        Column {
                            // Header Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ComposeColor(0xFF3B2314))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { capturedBitmap = null }) {
                                        Icon(Icons.Default.Close, "Close", tint = ComposeColor.White)
                                    }
                                    
                                    Button(
                                        onClick = { lastSavedUri?.let { shareImage(context, it) } },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFD4AF37))
                                    ) {
                                        Icon(Icons.Default.Share, "Share", tint = ComposeColor(0xFF3B2314))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Share Portfolio", color = ComposeColor(0xFF3B2314), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Branded Image Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .aspectRatio(capturedBitmap!!.width.toFloat() / capturedBitmap!!.height.toFloat())
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Portfolio",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            // Success Text
                            Text(
                                "✨ Heritage Branding Complete!",
                                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = ComposeColor(0xFF3B2314),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun captureAndProcessImage(
    context: Context,
    imageCapture: ImageCapture,
    pName: String,
    aName: String,
    wood: String,
    price: String,
    onSaved: (Uri, Bitmap) -> Unit
) {
    val productName = pName.ifBlank { "Traditional Craft" }
    val artisan = aName.ifBlank { "Master Artisan" }
    val material = wood.ifBlank { "Handcrafted" }
    val photoFile = File(context.cacheDir, "temp_capture.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    val brandedBitmap = addProfessionalBranding(bitmap, productName, artisan, material, price)
                    val savedUri = saveBrandedImage(context, brandedBitmap)
                    if (savedUri != null) {
                        onSaved(savedUri, brandedBitmap)
                    }
                }
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("ShilpaKala", "Capture failed: ${exception.message}")
            }
        }
    )
}

private fun addProfessionalBranding(original: Bitmap, productName: String, artisanName: String, wood: String, price: String): Bitmap {
    val width = original.width
    val height = original.height
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    
    // 1. ELITE COLOR GRADING
    val enhancePaint = Paint().apply {
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(1.4f) 
        val contrast = 1.25f
        val brightness = 15f
        val cmContrast = android.graphics.ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(cmContrast)
        colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        isFilterBitmap = true 
        isAntiAlias = true
    }
    
    canvas.drawBitmap(original, 0f, 0f, enhancePaint)
    
    val base = Math.min(width, height).toFloat()
    
    // 2. Cinematic Soft Vignette
    val vignette = Paint().apply {
        shader = android.graphics.RadialGradient(
            width / 2f, height / 2f, Math.max(width, height) * 0.9f,
            intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.argb(140, 0, 0, 0)),
            null, Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignette)
    
    // 3. Heritage Badge
    val badgeH = height * 0.085f
    val badgeW = width * 0.45f
    val margin = width * 0.06f 
    val rect = RectF(width - badgeW - margin, margin, width - margin, margin + badgeH)
    
    val badgePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#3B2314")
        isAntiAlias = true
    }
    canvas.drawRoundRect(rect, 12f, 12f, badgePaint)
    
    val goldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        style = Paint.Style.STROKE
        strokeWidth = base * 0.006f
        isAntiAlias = true
    }
    canvas.drawRoundRect(rect, 12f, 12f, goldPaint)
    
    val knPaint = Paint().apply {
        textSize = badgeH * 0.35f
        color = android.graphics.Color.parseColor("#D4AF37")
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText("ಕರ್ನಾಟಕದ ಕರಕುಶಲ", rect.centerX(), rect.top + (badgeH * 0.45f), knPaint)
    
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = badgeH * 0.22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    canvas.drawText("HANDMADE IN KARNATAKA", rect.centerX(), rect.bottom - (badgeH * 0.15f), textPaint)

    // 4. Designer Footer Section
    val footerH = height * 0.22f
    val footerPaint = Paint().apply {
        shader = LinearGradient(0f, height - footerH, 0f, height.toFloat(),
            intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.argb(220, 0, 0, 0)), null, Shader.TileMode.CLAMP)
    }
    canvas.drawRect(0f, height - footerH, width.toFloat(), height.toFloat(), footerPaint)
    
    val prodP = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = height * 0.055f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(15f, 0f, 5f, android.graphics.Color.BLACK)
    }
    canvas.drawText(productName.uppercase(), margin, height - (footerH * 0.65f), prodP)
    
    val nameP = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        textSize = height * 0.035f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        isAntiAlias = true
    }
    canvas.drawText("by $artisanName", margin, height - (footerH * 0.48f), nameP)
    
    val detailP = Paint().apply {
        color = android.graphics.Color.parseColor("#BBBBBB")
        textSize = height * 0.024f
        isAntiAlias = true
        typeface = Typeface.SANS_SERIF
    }
    canvas.drawText("Material: $wood | Genuine Craftsmanship", margin, height - (footerH * 0.35f), detailP)
    
    val priceP = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = height * 0.09f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
        setShadowLayer(20f, 0f, 10f, android.graphics.Color.BLACK)
    }
    canvas.drawText("₹$price", width - margin, height - (footerH * 0.45f), priceP)
    
    return result
}
