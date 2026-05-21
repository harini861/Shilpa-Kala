package com.example.shilpa_kala

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val portfolioDao = database.portfolioDao()
    val profile by portfolioDao.getProfile().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var craftType by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            region = it.region
            craftType = it.craftType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            portfolioDao.saveProfile(
                                ArtisanProfile(
                                    name = name,
                                    region = region,
                                    craftType = craftType
                                )
                            )
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Artisan Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Region (e.g., Channapatna)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = craftType,
                onValueChange = { craftType = it },
                label = { Text("Craft Type") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    scope.launch {
                        portfolioDao.saveProfile(
                            ArtisanProfile(
                                name = name,
                                region = region,
                                craftType = craftType
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
