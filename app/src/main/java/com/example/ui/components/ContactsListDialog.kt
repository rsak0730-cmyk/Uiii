package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ContactEntry
import com.example.model.NeonTheme
import com.example.model.UiDesignStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListDialog(
    contacts: List<ContactEntry>,
    theme: NeonTheme,
    uiStyle: UiDesignStyle,
    onCallNumber: (String, String) -> Unit,
    onSendSms: (String, String) -> Unit,
    onRecordVoiceNoteForContact: (ContactEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Family", "Friend", "Work")

    val filteredContacts = remember(searchQuery, selectedCategory, contacts) {
        contacts.filter { contact ->
            val matchesQuery = searchQuery.isBlank() || contact.name.contains(searchQuery.trim(), ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || contact.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .testTag("contacts_list_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F111A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Friends & Family",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Filter and connect with contacts instantly",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2230))
                            .testTag("contacts_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SEARCH BAR AT TOP OF CONTACTS LIST
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contacts_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search friends & family by name...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("contacts_search_clear")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF161926),
                        unfocusedContainerColor = Color(0xFF161926),
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = Color(0xFF282D3E)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CATEGORY FILTER CHIPS
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = if (category == "Family") "👨‍👩‍👧‍👦 Family"
                                    else if (category == "Friend") "🤝 Friends"
                                    else if (category == "Work") "💼 Work"
                                    else "🌐 All Contacts",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.25f),
                                selectedLabelColor = theme.primary,
                                containerColor = Color(0xFF181B26),
                                labelColor = Color.LightGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) theme.primary else Color(0xFF2C3246)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RESULTS COUNT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredContacts.size} contact${if (filteredContacts.size != 1) "s" else ""} found",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (searchQuery.isNotBlank() || selectedCategory != "All") {
                        Text(
                            text = "Reset Filter",
                            color = theme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CONTACTS LIST
                if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No friends or family matching \"$searchQuery\"",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Try adjusting your search query or clear category filters.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2232)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Clear Search & Show All", color = theme.primary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredContacts, key = { it.name }) { contact ->
                            ContactCardItem(
                                contact = contact,
                                theme = theme,
                                onCall = { number -> onCallNumber(contact.name, number) },
                                onSms = { number -> onSendSms(contact.name, number) },
                                onVoiceNote = { onRecordVoiceNoteForContact(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCardItem(
    contact: ContactEntry,
    theme: NeonTheme,
    onCall: (String) -> Unit,
    onSms: (String) -> Unit,
    onVoiceNote: () -> Unit
) {
    val categoryColor = when (contact.category.lowercase()) {
        "family" -> Color(0xFFE040FB)
        "work" -> Color(0xFFFFB74D)
        else -> Color(0xFF00E5FF)
    }

    Surface(
        color = Color(0xFF161924),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_item_${contact.name.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.initials,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (contact.isFavorite) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Category Pill
                        Surface(
                            color = categoryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = contact.category.uppercase(),
                                color = categoryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Quick Action Buttons
                val primaryNumber = contact.phoneNumbers.firstOrNull() ?: ""
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Button
                    IconButton(
                        onClick = { onCall(primaryNumber) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.primary.copy(alpha = 0.18f))
                            .testTag("contact_call_${contact.name.replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call ${contact.name}",
                            tint = theme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // SMS Button
                    IconButton(
                        onClick = { onSms(primaryNumber) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222738))
                            .testTag("contact_sms_${contact.name.replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "SMS ${contact.name}",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Voice Note Button
                    IconButton(
                        onClick = onVoiceNote,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222738))
                            .testTag("contact_voicenote_${contact.name.replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Send Voice Note to ${contact.name}",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Phone numbers list
            Spacer(modifier = Modifier.height(10.dp))
            contact.phoneNumbers.forEachIndexed { index, number ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F121C))
                        .clickable { onCall(number) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (contact.phoneNumbers.size > 1) "Line ${index + 1}:" else "Mobile:",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = number,
                            color = Color(0xFFE0E0E0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Tap to dial",
                        color = theme.primary.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
