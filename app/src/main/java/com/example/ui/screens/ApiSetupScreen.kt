package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentActionController
import com.example.model.ApiConfig
import com.example.model.ApiProvider
import com.example.model.NeonTheme
import com.example.model.UiDesignStyle
import com.example.ui.components.UiStyleCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSetupScreen(
    currentConfig: ApiConfig,
    onSaveConfig: (ApiConfig) -> Unit,
    theme: NeonTheme,
    uiStyle: UiDesignStyle,
    actionController: AgentActionController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var profileName by remember(currentConfig) { mutableStateOf(currentConfig.profileName) }
    var selectedProvider by remember(currentConfig) { mutableStateOf(currentConfig.provider) }
    var apiKey by remember(currentConfig) { mutableStateOf(currentConfig.apiKey) }
    var selectedModel by remember(currentConfig) { mutableStateOf(currentConfig.selectedModel) }
    var baseUrl by remember(currentConfig) { mutableStateOf(currentConfig.baseUrl) }

    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }
    var showSaveToast by remember { mutableStateOf(false) }

    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090A10),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "API Brain Setup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090A10))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            UiStyleCard(
                style = uiStyle,
                theme = theme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect AI Studio or Multi-Model Hub",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste your Google AI Studio Gemini API key or OpenRouter / OpenAI credentials below to power the agent's real-time reasoning engine.",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // 1st UI: Profile Name & Provider
            Text(
                text = "1. AI Brain Name & Provider",
                color = theme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = profileName,
                onValueChange = { profileName = it },
                label = { Text("Profile Name") },
                placeholder = { Text("e.g. Gemini AI Studio Brain") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = theme.primary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = Color(0xFF2A2E3D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_profile_name_input")
            )

            // Provider Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = providerDropdownExpanded,
                onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProvider.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select API Provider") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = Color(0xFF2A2E3D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("api_provider_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = providerDropdownExpanded,
                    onDismissRequest = { providerDropdownExpanded = false },
                    modifier = Modifier.background(Color(0xFF141722))
                ) {
                    for (provider in ApiProvider.entries) {
                        DropdownMenuItem(
                            text = { Text(provider.displayName, color = Color.White) },
                            onClick = {
                                selectedProvider = provider
                                baseUrl = provider.defaultBaseUrl
                                selectedModel = provider.models.first()
                                providerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 2nd UI: Paste API Key
            Text(
                text = "2. Paste API Key",
                color = theme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("${selectedProvider.name} API Key") },
                placeholder = { Text("AIzaSy... or sk-...") },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = theme.primary)
                },
                trailingIcon = {
                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = Color(0xFF2A2E3D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input")
            )

            // 3rd UI: Select Model
            Text(
                text = "3. Select Model (Latest Usable Models)",
                color = theme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            ExposedDropdownMenuBox(
                expanded = modelDropdownExpanded,
                onExpandedChange = { modelDropdownExpanded = !modelDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = { selectedModel = it },
                    label = { Text("Model Identifier") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = Color(0xFF2A2E3D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                        .testTag("api_model_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false },
                    modifier = Modifier.background(Color(0xFF141722))
                ) {
                    selectedProvider.models.forEach { modelName ->
                        DropdownMenuItem(
                            text = { Text(modelName, color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                selectedModel = modelName
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 4th UI: Base URL
            Text(
                text = "4. Base URL",
                color = theme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Service Endpoint Base URL") },
                leadingIcon = {
                    Icon(Icons.Default.Language, contentDescription = null, tint = theme.primary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = Color(0xFF2A2E3D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_base_url_input")
            )

            // Test Result Banner
            if (testResult != null) {
                Surface(
                    color = if (isTestSuccess) Color(0x3300E676) else Color(0x33FF1744),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTestSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isTestSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = testResult ?: "",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Action Buttons (Test & Save)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isTestingConnection = true
                            testResult = null
                            val tempConfig = ApiConfig(
                                profileName = profileName,
                                provider = selectedProvider,
                                apiKey = apiKey,
                                selectedModel = selectedModel,
                                baseUrl = baseUrl
                            )
                            val response = actionController.queryAiBrain("Ping test: confirm connection status in 5 words.", tempConfig)
                            isTestingConnection = false
                            if (response.contains("Error", ignoreCase = true) || response.contains("failed", ignoreCase = true)) {
                                isTestSuccess = false
                                testResult = response
                            } else {
                                isTestSuccess = true
                                testResult = "Connection Verified! Response: $response"
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_api_connection_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(
                            color = theme.primary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Test Connection", color = theme.primary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val newConfig = ApiConfig(
                            profileName = profileName,
                            provider = selectedProvider,
                            apiKey = apiKey,
                            selectedModel = selectedModel,
                            baseUrl = baseUrl
                        )
                        onSaveConfig(newConfig)
                        showSaveToast = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_api_config_button")
                ) {
                    Text("Save Brain", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            if (showSaveToast) {
                Text(
                    text = "Configuration saved successfully!",
                    color = Color(0xFF00E676),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
