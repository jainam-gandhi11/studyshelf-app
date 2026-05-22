package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.dao.PdfWithSubjectName
import com.example.data.dao.SubjectWithPdfCount
import com.example.data.entity.Subject
import com.example.ui.theme.ThemeOption
import com.example.ui.viewmodel.ShelfStats
import com.example.ui.viewmodel.StudyShelfViewModel
import kotlinx.coroutines.delay

// Beautiful color constant for important stars (Metallic Gold)
val StarGold = Color(0xFFD4AF37)

// Premium custom flat monochrome border utility
fun Modifier.monochromeBorder(color: Color = Color.LightGray) = this.border(0.5.dp, color, RoundedCornerShape(12.dp))

// ==========================================
// CORE SHELF TOP BAR COMPOSABLE (COMPILER IMMUNE)
// ==========================================
@Composable
fun StudyShelfTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (actions != null) {
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

@Composable
fun StudyShelfApp(viewModel: StudyShelfViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("home") {
            MainAppContainer(navController, viewModel)
        }
        composable(
            route = "subject/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            SubjectScreen(navController, viewModel, subjectId)
        }
        composable(
            route = "unit_pdfs/{subjectId}/{unitNumber}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.IntType },
                navArgument("unitNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            val unitNumber = backStackEntry.arguments?.getInt("unitNumber") ?: 1
            UnitPdfsScreen(navController, viewModel, subjectId, unitNumber)
        }
        composable(
            route = "pdf_preview/{pdfId}",
            arguments = listOf(navArgument("pdfId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pdfId = backStackEntry.arguments?.getInt("pdfId") ?: 0
            PdfPreviewScreen(navController, viewModel, pdfId)
        }
    }
}

// ==========================================
// SPLASH SCREEN VIEW
// ==========================================
@Composable
fun SplashScreen(navController: NavHostController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Splash Alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Bookshelf vector Logo",
                    tint = Color.Black,
                    modifier = Modifier.size(52.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "StudyShelf",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your digital bookshelf",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(180.dp))
            
            Text(
                text = "Created by Jainam",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// MAIN APPLICATION CONTAINER (TABS NAVIGATION)
// ==========================================
@Composable
fun MainAppContainer(
    parentNavController: NavHostController,
    viewModel: StudyShelfViewModel
) {
    var selectedTab by remember { mutableStateOf("home") }
    
    // Dialog triggers
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddPdfDialog by remember { mutableStateOf(false) }
    var showAddOptionsSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(if (selectedTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = selectedTab == "search",
                    onClick = { selectedTab = "search" },
                    icon = { Icon(if (selectedTab == "search") Icons.Filled.Search else Icons.Outlined.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    modifier = Modifier.testTag("nav_search")
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    icon = { Icon(if (selectedTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == "home") {
                FloatingActionButton(
                    onClick = { showAddOptionsSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_item_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> HomeView(parentNavController, viewModel)
                "search" -> SearchView(parentNavController, viewModel)
                "settings" -> SettingsView(viewModel)
            }
        }
    }

    // Add Options Custom Alert Box (Simulating quick sheet)
    if (showAddOptionsSheet) {
        Dialog(onDismissRequest = { showAddOptionsSheet = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add to Shelf",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddOptionsSheet = false
                                showAddSubjectDialog = true
                            }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Create Subject", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Define a new academic bookshelf category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.tertiary, thickness = 0.5.dp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddOptionsSheet = false
                                showAddPdfDialog = true
                            }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Add PDF Reference", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Register storage PDF references into shelves", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddOptionsSheet = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary, 
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var subjectName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#1A1A1A") }
        val colorsPalette = listOf("#1A1A1A", "#3F3F46", "#71717A", "#27272A", "#09090B")
        
        Dialog(onDismissRequest = { showAddSubjectDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("New Subject Shelf", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Data Mining") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_input")
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Atmospheric Shade (Monochrome Theme)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorsPalette.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (selectedColor == hex) 3.dp else 1.dp,
                                        color = if (selectedColor == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showAddSubjectDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (subjectName.isNotBlank()) {
                                    viewModel.addSubject(subjectName, colorHex = selectedColor)
                                    showAddSubjectDialog = false
                                }
                            },
                            enabled = subjectName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    // Add PDF Reference Dialog (Supports real picker + sandbox demonstrative links)
    if (showAddPdfDialog) {
        val subjectsList by viewModel.subjects.collectAsState(emptyList())
        val context = LocalContext.current
        
        var selectedSubjectIndex by remember { mutableStateOf(0) }
        var selectedUnit by remember { mutableStateOf(1) }
        var customDisplayName by remember { mutableStateOf("") }
        var isImportant by remember { mutableStateOf(false) }
        var isPyq by remember { mutableStateOf(false) }
        
        // Browsed file details
        var browsedFileName by remember { mutableStateOf("") }
        var browsedFileUri by remember { mutableStateOf("") }
        var browsedFileSize by remember { mutableStateOf("") }

        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Safe fallbacks
                }
                
                browsedFileUri = uri.toString()
                
                var name = "Imported_" + System.currentTimeMillis() + ".pdf"
                var sizeStr = "1.2 MB"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) {
                            val cursorName = cursor.getString(nameIdx)
                            if (cursorName != null) name = cursorName
                        }
                        if (sizeIdx != -1) {
                            val bytes = cursor.getLong(sizeIdx)
                            sizeStr = if (bytes >= 1024 * 1024) {
                                String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
                            } else {
                                String.format("%d KB", bytes / 1024)
                            }
                        }
                    }
                }
                browsedFileName = name
                browsedFileSize = sizeStr
                
                if (customDisplayName.isBlank()) {
                    customDisplayName = name.substringBeforeLast(".pdf")
                }
            }
        }

        Dialog(onDismissRequest = { showAddPdfDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("Add Study PDF File", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (subjectsList.isEmpty()) {
                        Text(
                            "⚠️ You must create at least one Subject first before adding PDFs!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddPdfDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Okay")
                        }
                    } else {
                        Text("Associate Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        var expandedSubDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedSubDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val activeSubjectName = if (selectedSubjectIndex < subjectsList.size) subjectsList[selectedSubjectIndex].name else "Select..."
                                Text(activeSubjectName)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedSubDropdown,
                                onDismissRequest = { expandedSubDropdown = false }
                            ) {
                                subjectsList.forEachIndexed { index, sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            selectedSubjectIndex = index
                                            expandedSubDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Academic Unit Structure", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (1..5).forEach { uNum ->
                                val isSelected = selectedUnit == uNum
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.tertiary
                                        )
                                        .clickable { selectedUnit = uNum },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$uNum",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                                .border(0.5.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Choose PDF Source Label:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Browse UI", fontSize = 11.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Button(
                                        onClick = {
                                            browsedFileUri = "demo_mode://studyshelf/mock_lesson_" + System.currentTimeMillis() + ".pdf"
                                            val demoLessons = listOf(
                                                "Unit ${selectedUnit} Key Concepts notes.pdf",
                                                "Exam Pyr-series questions.pdf",
                                                "Academic syllabus guide.pdf",
                                                "Detailed Laboratory guide.pdf"
                                            )
                                            browsedFileName = demoLessons.random()
                                            browsedFileSize = "${(2..9).random()}.${(1..9).random()} MB"
                                            customDisplayName = browsedFileName.substringBeforeLast(".pdf")
                                        },
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Demo Link", fontSize = 11.sp)
                                    }
                                }
                                
                                if (browsedFileName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, "Connected", tint = Color.Green, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$browsedFileName ($browsedFileSize)",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customDisplayName,
                            onValueChange = { customDisplayName = it },
                            label = { Text("Custom Display Name") },
                            placeholder = { Text("e.g. Unit 1 Introduction Lecture Notes") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pdf_display_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isImportant, onCheckedChange = { isImportant = it })
                                Text("Mark Important", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isPyq, onCheckedChange = { isPyq = it })
                                Text("PYQ Paper", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showAddPdfDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (customDisplayName.isNotBlank() && browsedFileUri.isNotEmpty()) {
                                        val subjectIdValue = subjectsList[selectedSubjectIndex].id
                                        viewModel.addPdf(
                                            subjectId = subjectIdValue,
                                            unitNumber = selectedUnit,
                                            displayName = customDisplayName,
                                            filePath = browsedFileUri,
                                            fileSizeString = browsedFileSize.ifBlank { "1.2 MB" },
                                            isImportant = isImportant,
                                            isPyq = isPyq
                                        )
                                        Toast.makeText(context, "Added study reference!", Toast.LENGTH_SHORT).show()
                                        showAddPdfDialog = false
                                    } else {
                                        Toast.makeText(context, "Please browse/link a target PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = customDisplayName.isNotBlank() && browsedFileUri.isNotEmpty()
                            ) {
                                Text("Add Reference")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HOME VIEW DASHBOARD
// ==========================================
@Composable
fun HomeView(
    parentNavController: NavHostController,
    viewModel: StudyShelfViewModel
) {
    val studentName by viewModel.studentName.collectAsState("Student")
    val subjectsList by viewModel.subjects.collectAsState(emptyList())
    val stats by viewModel.stats.collectAsState(ShelfStats())
    val themeOption by viewModel.themeOption.collectAsState(ThemeOption.SYSTEM)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Styled Header matching Bold Typography exactly
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "GOOD EVENING",
                        style = MaterialTheme.typography.labelSmall, // Tracking-widest label
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "StudyShelf",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimal capsule for theme toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(2.dp)
                    ) {
                        val lightSelected = themeOption == ThemeOption.LIGHT
                        IconButton(
                            onClick = { viewModel.setTheme(ThemeOption.LIGHT) },
                            modifier = Modifier
                                .size(24.dp)
                                .background(if (lightSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LightMode, 
                                contentDescription = "Light Mode", 
                                tint = if (lightSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary, 
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        val darkSelected = themeOption == ThemeOption.DARK
                        IconButton(
                            onClick = { viewModel.setTheme(ThemeOption.DARK) },
                            modifier = Modifier
                                .size(24.dp)
                                .background(if (darkSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DarkMode, 
                                contentDescription = "Dark Mode", 
                                tint = if (darkSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary, 
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // White Square Profile Avatar J
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            val initialChar = if (studentName.isNotBlank()) studentName.first().uppercase() else "J"
                            Text(
                                text = initialChar,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Custom Search Input Box matching the text field bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Triggers transition to Search tab directly
                        Toast.makeText(parentNavController.context, "Ready to search StudyShelf below!", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon label",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Search your shelf...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Statistics Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(count = "${stats.totalSubjects}", label = "Subjects", modifier = Modifier.weight(1f))
                StatItem(count = "${stats.totalPdfs}", label = "Total PDFs", modifier = Modifier.weight(1f))
                StatItem(count = "${stats.totalImportant}", label = "Important", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Subject Header Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR SUBJECTS",
                    style = MaterialTheme.typography.labelSmall, // Extra Bold uppercase spacer tracking-widest
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (subjectsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty Shelf indicator icon",
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No academic subject folders created.", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use '+' below to create subjects", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 1600.dp)
                ) {
                    items(subjectsList) { subject ->
                        SubjectCardItem(subject) {
                            parentNavController.navigate("subject/${subject.id}")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBJECT SCREEN (UNIT PILLS, TABS, SUB-PDFS)
// ==========================================
@Composable
fun SubjectScreen(
    navController: NavHostController,
    viewModel: StudyShelfViewModel,
    subjectId: Int
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Units") }
    var subjectObj by remember { mutableStateOf<Subject?>(null) }
    
    LaunchedEffect(subjectId) {
        subjectObj = viewModel.getSubjectById(subjectId)
    }

    val pdfsList by viewModel.getPdfsForSubject(subjectId).collectAsState(emptyList())
    
    val importantList = pdfsList.filter { it.isImportant }
    val pyqList = pdfsList.filter { it.isPyq }
    val recentList = pdfsList.sortedByDescending { if (it.lastOpened > 0) it.lastOpened else it.dateAdded }.take(5)

    Scaffold(
        topBar = {
            StudyShelfTopBar(
                title = subjectObj?.name ?: "Subject Folders",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteSubject(subjectId)
                            Toast.makeText(context, "Subject folder deleted", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, "Delete Subject Category", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniHeadStat(label = "PDFs", value = "${pdfsList.size}")
                    MiniHeadStat(label = "Units", value = "5")
                    MiniHeadStat(label = "PYQs", value = "${pyqList.size}")
                    MiniHeadStat(label = "Important", value = "${importantList.size}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Bookshelf Categories Menu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Horizon tab selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("Units", "Important", "Recent", "PYQ")
                tabs.forEach { t ->
                    val isSelected = activeTab == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.tertiary
                            )
                            .clickable { activeTab = t }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (t) {
                                    "Important" -> Icons.Default.Star
                                    "Recent" -> Icons.Default.History
                                    "PYQ" -> Icons.Default.Grading
                                    else -> Icons.Default.Layers
                                },
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = t,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                "Units" -> {
                    Text("Academics Unit Shelves", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items((1..5).toList()) { unitNo ->
                            val count = pdfsList.filter { it.unitNumber == unitNo }.size
                            UnitFolderItem(unitNo = unitNo, count = count) {
                                navController.navigate("unit_pdfs/$subjectId/$unitNo")
                            }
                        }
                    }
                }
                
                "Important" -> {
                    Text("Super Important Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    pdfsListBody(navController, viewModel, list = importantList, subjectId)
                }
                
                "Recent" -> {
                    Text("Recently Opened References", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    pdfsListBody(navController, viewModel, list = recentList, subjectId)
                }
                
                "PYQ" -> {
                    Text("Previous Year Exam Papers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    pdfsListBody(navController, viewModel, list = pyqList, subjectId)
                }
            }
        }
    }
}

// ==========================================
// UNIT PDFS SCREEN DISPLAY
// ==========================================
@Composable
fun UnitPdfsScreen(
    navController: NavHostController,
    viewModel: StudyShelfViewModel,
    subjectId: Int,
    unitNumber: Int
) {
    val itemsFlowList by viewModel.getPdfsForSubjectAndUnit(subjectId, unitNumber).collectAsState(emptyList())

    Scaffold(
        topBar = {
            StudyShelfTopBar(
                title = "Unit $unitNumber Syllabus Papers",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (itemsFlowList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No referenced files added to Unit $unitNumber yet.", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                    Text("Add references to this Unit using '+'", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(itemsFlowList) { pdf ->
                        PdfRowItemCom(pdf, viewModel, navController)
                    }
                }
            }
        }
    }
}

// ==========================================
// CORE ROW ITEM COMPOSABLE
// ==========================================
@Composable
fun PdfRowItemCom(
    pdf: PdfWithSubjectName,
    viewModel: StudyShelfViewModel,
    navController: NavHostController
) {
    var showMenuDropdown by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .monochromeBorder(MaterialTheme.colorScheme.tertiary)
            .clickable {
                viewModel.recordPdfOpened(pdf.id)
                navController.navigate("pdf_preview/${pdf.id}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Red.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF File",
                    tint = Color.Red,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pdf.customDisplayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (pdf.isImportant) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Star, "Important Priority", tint = StarGold, modifier = Modifier.size(14.dp))
                    }
                    if (pdf.isPyq) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.DarkGray)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("PYQ", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pdf.fileSizeString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(3.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unit ${pdf.unitNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenuDropdown = true }) {
                    Icon(Icons.Default.MoreVert, "More Options")
                }
                DropdownMenu(
                    expanded = showMenuDropdown,
                    onDismissRequest = { showMenuDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename Display Name") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            showMenuDropdown = false
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (pdf.isImportant) "Remove Important priority" else "Mark as Important") },
                        leadingIcon = { Icon(Icons.Default.Star, null) },
                        onClick = {
                            showMenuDropdown = false
                            viewModel.togglePdfImportant(pdf.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (pdf.isPyq) "Untag PYQ Paper" else "Tag as PYQ Paper") },
                        leadingIcon = { Icon(Icons.Default.LocalLibrary, null) },
                        onClick = {
                            showMenuDropdown = false
                            viewModel.togglePdfPyq(pdf.id)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move Reference Subject Folder") },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, null) },
                        onClick = {
                            showMenuDropdown = false
                            showMoveDialog = true
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Remove from Shelf Folder", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                        onClick = {
                            showMenuDropdown = false
                            viewModel.removePdf(pdf.id)
                            Toast.makeText(context, "Removed from shelf", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Rename display notes Dialog
    if (showRenameDialog) {
        var editName by remember { mutableStateOf(pdf.customDisplayName) }
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Rename display label", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    viewModel.renamePdf(pdf.id, editName)
                                    showRenameDialog = false
                                }
                            }
                        ) { Text("Save") }
                    }
                }
            }
        }
    }

    // Move Subject Dialog
    if (showMoveDialog) {
        val subjectsList by viewModel.subjects.collectAsState(emptyList())
        var selectedSubjectIdx by remember { mutableStateOf(0) }
        var selectedUnitMove by remember { mutableStateOf(pdf.unitNumber) }
        
        Dialog(onDismissRequest = { showMoveDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Move PDF Category Folder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Select Target Subject", style = MaterialTheme.typography.labelSmall)
                    var extDrop by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { extDrop = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeName = if (subjectsList.isNotEmpty() && selectedSubjectIdx < subjectsList.size) 
                                                  subjectsList[selectedSubjectIdx].name else "Select..."
                            Text(activeName)
                        }
                        DropdownMenu(expanded = extDrop, onDismissRequest = { extDrop = false }) {
                            subjectsList.forEachIndexed { i, s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectIdx = i
                                        extDrop = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Target Unit No.", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { uNum ->
                            val isSel = selectedUnitMove == uNum
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.tertiary
                                    )
                                    .clickable { selectedUnitMove = uNum },
                                contentAlignment = Alignment.Center
                            ) {
                                  Text(
                                      text = "$uNum",
                                      color = if (isSel) MaterialTheme.colorScheme.onPrimary 
                                              else MaterialTheme.colorScheme.onSurface,
                                      fontWeight = FontWeight.Bold
                                  )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showMoveDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (subjectsList.isNotEmpty()) {
                                    val newSubId = subjectsList[selectedSubjectIdx].id
                                    viewModel.movePdfSubject(pdf.id, newSubId, selectedUnitMove)
                                    Toast.makeText(context, "Move folder registration complete!", Toast.LENGTH_SHORT).show()
                                    showMoveDialog = false
                                }
                            }
                        ) { Text("Move") }
                    }
                }
            }
        }
    }
}

// ==========================================
// SEARCH SCREEN VIEW
// ==========================================
@Composable
fun SearchView(
    parentNavController: NavHostController,
    viewModel: StudyShelfViewModel
) {
    val query by viewModel.searchQuery.collectAsState("")
    val results by viewModel.searchResults.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Quick Search Engine", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Scan all subjects, units, or exam papers in seconds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search subjects, unit or PDF display label...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_engine")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ContentPasteSearch, null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Type above to start searching!", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    Text("Type DBMS, notes or Unit 1 to scan references", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No matching shelf items found.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Search Results (${results.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(results) { pdf ->
                    PdfRowItemCom(pdf, viewModel, parentNavController)
                }
            }
        }
    }
}

// ==========================================
// SETTINGS SCREEN VIEW
// ==========================================
@Composable
fun SettingsView(viewModel: StudyShelfViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val currentTheme by viewModel.themeOption.collectAsState(ThemeOption.SYSTEM)
    val studentName by viewModel.studentName.collectAsState("Student")

    var editingName by remember { mutableStateOf(studentName) }
    var isEditingNameMode by remember { mutableStateOf(false) }
    
    var showBackupRestoreSection by remember { mutableStateOf(false) }
    var pasteBackupString by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings Panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Manage display options, profile mappings & backup maps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Student Identity (Greetings customizer)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isEditingNameMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editingName,
                                onValueChange = { editingName = it },
                                singleLine = true,
                                label = { Text("Display Name") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    viewModel.updateStudentName(editingName)
                                    isEditingNameMode = false
                                }
                            ) {
                                Icon(Icons.Default.Check, "Save Profile Name", tint = Color.Green)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(studentName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Displayed inside study morning greetings", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { isEditingNameMode = true }) {
                                Icon(Icons.Default.Edit, "Edit Name")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Display Theme Options", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            ThemeOption.LIGHT to "Light Mode",
                            ThemeOption.DARK to "Dark mode",
                            ThemeOption.SYSTEM to "System"
                        ).forEach { (opt, label) ->
                            val activeSelected = currentTheme == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (activeSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.background
                                    )
                                    .clickable { viewModel.setTheme(opt) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeSelected) MaterialTheme.colorScheme.onPrimary 
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Durable Backup & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Synchronise shelf folders reference mappings", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { showBackupRestoreSection = !showBackupRestoreSection }) {
                            Icon(
                                if (showBackupRestoreSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                "Toggle Section"
                            )
                        }
                    }

                    if (showBackupRestoreSection) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    viewModel.exportBackup { backupString ->
                                        if (backupString.isNotEmpty()) {
                                            clipboardManager.setText(AnnotatedString(backupString))
                                            Toast.makeText(context, "Durable Map copied! Paste backup data safely.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Unable to generate backup payload.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export payload")
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Button(
                                onClick = {
                                    if (pasteBackupString.isNotBlank()) {
                                        viewModel.importBackup(pasteBackupString.trim()) { success, outcome ->
                                            if (success) {
                                                pasteBackupString = ""
                                                Toast.makeText(context, outcome, Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, outcome, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Paste an export string first inside the input box below!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import map")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pasteBackupString,
                            onValueChange = { pasteBackupString = it },
                            placeholder = { Text("Paste clipboard backup JSON data here...") },
                            maxLines = 5,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("StudyShelf Academic Locker", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Release Version 1.4.2 (Stable)", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Fully Offline • Energy Saver Configs", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Designed and Handcrafted with sheer passion,\nCreated by Jainam Gandhi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// IMMERSIVE PDF PREVIEWER & ACADEMIC READING DESK
// ==========================================
@Composable
fun PdfPreviewScreen(
    navController: NavHostController,
    viewModel: StudyShelfViewModel,
    pdfId: Int
) {
    val context = LocalContext.current
    var pdfItem by remember { mutableStateOf<com.example.data.entity.PdfShelfItem?>(null) }
    
    LaunchedEffect(pdfId) {
        pdfItem = viewModel.getPdfById(pdfId)
    }

    Scaffold(
        topBar = {
            StudyShelfTopBar(
                title = pdfItem?.customDisplayName ?: "Academic study desk",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(
                        onClick = {
                            if (pdfItem != null) {
                                viewModel.togglePdfImportant(pdfItem!!.id)
                                pdfItem = pdfItem!!.copy(isImportant = !pdfItem!!.isImportant)
                            }
                        }
                    ) {
                        val isImportant = pdfItem?.isImportant == true
                        Icon(
                            imageVector = if (isImportant) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Starred highlight",
                            tint = if (isImportant) StarGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            if (pdfItem != null) {
                                try {
                                    val uriStr = pdfItem!!.filePath
                                    if (uriStr.startsWith("demo_mode://")) {
                                        Toast.makeText(context, "Immersive virtual file mode actively running!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(uriStr)
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No standard PDF app configured. Running sandboxed reader!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, "Launch Native desk")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE5E5E9))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNIT ${pdfItem?.unitNumber ?: 1} ACADEMICS DESK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "STABLE OFFLINE REFERENCE",
                            fontSize = 8.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = pdfItem?.customDisplayName ?: "Academics Desk Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Reference path: ${pdfItem?.filePath}\nMemory reference allocations: ${pdfItem?.fileSizeString ?: "1.2 MB"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "1. Fundamentals Outline Overview\nThis syllabus maps relevant study metrics instantly during engineering exam preparation. Your original storage documents remain completely untouched in their parent storage pathways and locations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "2. Quick Revision tips\n- Organize files under specific Unit tags (Units 1-5 support)\n- Bookmark important exam questions with the Top Star Icon\n- Keep reference names short and meaningful.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Page 1 of 32", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { Toast.makeText(context, "Maximise scaling preview simulator lock", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.ZoomIn, "Zoom In")
                    }
                    IconButton(onClick = { Toast.makeText(context, "Minimise scaling preview simulator lock", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.ZoomOut, "Zoom Out")
                    }
                    IconButton(onClick = { Toast.makeText(context, "Simulated Bookmark added!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Bookmark, "Bookmark")
                    }
                    Button(
                        onClick = {
                            if (pdfItem != null) {
                                try {
                                    val uriStr = pdfItem!!.filePath
                                    if (uriStr.startsWith("demo_mode://")) {
                                        Toast.makeText(context, "Launching simulator academic reading deck file!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(uriStr)
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No standard local system viewer detected.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Open Document")
                    }
                }
            }
        }
    }
}

// Helper components & layout indicators
@Composable
fun StatItem(count: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start // Left-aligned like HTML
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall, // Uppercase tracked typography
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            val isImportantVal = label.lowercase().contains("important")
            Text(
                text = count.padStart(2, '0'), // Prepend zero matching "08" design HTML!
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (isImportantVal && count != "0" && count != "00") Color(0xFFD97706) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubjectCardItem(
    subject: SubjectWithPdfCount,
    onClick: () -> Unit
) {
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Generate 1-2 letter abbreviation badge
    val words = subject.name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    val abbreviation = when {
        words.isEmpty() -> "S"
        words.size == 1 -> if (words[0].length >= 2) words[0].take(2).uppercase() else words[0].uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }

    Surface(
        shape = RoundedCornerShape(24.dp), // rounded-3xl in HTML
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("subject_card_${subject.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // High contrast circular letter-badge inside card
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(subjectColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = abbreviation,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black, // Super heavy typography
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${subject.pdfCount} PDFs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MiniHeadStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun UnitFolderItem(
    unitNo: Int,
    count: Int,
    onClick: () -> Unit
) {
    val title = when (unitNo) {
        1 -> "Unit 1 - Fundamentals & Overview"
        2 -> "Unit 2 - Core Architectures & Data"
        3 -> "Unit 3 - Advanced Implementations"
        4 -> "Unit 4 - Systematic Analysis"
        else -> "Unit 5 - Live Applications Review"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("$count PDFs indexed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun pdfsListBody(
    navController: NavHostController,
    viewModel: StudyShelfViewModel,
    list: List<PdfWithSubjectName>,
    subjectId: Int
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Category shelf empty", color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 800.dp)
        ) {
            items(list) { pdf ->
                PdfRowItemCom(pdf, viewModel, navController)
            }
        }
    }
}
