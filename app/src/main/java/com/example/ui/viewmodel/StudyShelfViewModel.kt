package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dao.PdfWithSubjectName
import com.example.data.dao.SubjectWithPdfCount
import com.example.data.database.StudyShelfDatabase
import com.example.data.entity.PdfShelfItem
import com.example.data.entity.Subject
import com.example.data.repository.StudyShelfRepository
import com.example.ui.theme.ThemeOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ShelfStats(
    val totalSubjects: Int = 0,
    val totalPdfs: Int = 0,
    val totalImportant: Int = 0,
    val totalPyqs: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudyShelfViewModel(private val repository: StudyShelfRepository) : ViewModel() {

    // Theme Management
    private val _themeOption = MutableStateFlow(ThemeOption.SYSTEM)
    val themeOption: StateFlow<ThemeOption> = _themeOption.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSettingFlow("theme").collect { setting ->
                _themeOption.value = when (setting?.value) {
                    "light" -> ThemeOption.LIGHT
                    "dark" -> ThemeOption.DARK
                    else -> ThemeOption.SYSTEM
                }
            }
        }
    }

    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            val value = when (option) {
                ThemeOption.LIGHT -> "light"
                ThemeOption.DARK -> "dark"
                ThemeOption.SYSTEM -> "system"
            }
            repository.saveSetting("theme", value)
        }
    }

    // Active Student User Profile name setting (Greeting Section: e.g. "Good Morning, Jainam")
    private val _studentName = MutableStateFlow("Student")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSettingFlow("student_name").collect { setting ->
                _studentName.value = setting?.value ?: "Student"
            }
        }
    }

    fun updateStudentName(name: String) {
        viewModelScope.launch {
            repository.saveSetting("student_name", name.ifBlank { "Student" })
        }
    }

    // Subjects and PDFs reactive data triggers
    val subjects: StateFlow<List<SubjectWithPdfCount>> = repository.allSubjectsWithPdfCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantPdfs: StateFlow<List<PdfWithSubjectName>> = repository.getImportantPdfs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pyqPdfs: StateFlow<List<PdfWithSubjectName>> = repository.getPyqPdfs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyOpenedPdfs: StateFlow<List<PdfWithSubjectName>> = repository.getRecentlyOpenedPdfs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall Shelf Stats Counters
    val stats: StateFlow<ShelfStats> = combine(
        repository.subjectsCount,
        repository.pdfCount,
        repository.importantPdfCount,
        repository.pyqPdfCount
    ) { sub, pdf, imp, pyq ->
        ShelfStats(sub, pdf, imp, pyq)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShelfStats())

    // Search Query state
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<PdfWithSubjectName>> = searchQuery
        .debounce(100)
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.searchPdfs(query.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fetch PDFs list of a specific subject
    fun getPdfsForSubject(subjectId: Int): Flow<List<PdfWithSubjectName>> {
        return repository.getPdfsForSubject(subjectId)
    }

    // Fetch PDFs of a specific subject and unit (1-5)
    fun getPdfsForSubjectAndUnit(subjectId: Int, unitNumber: Int): Flow<List<PdfWithSubjectName>> {
        return repository.getPdfsForSubjectAndUnit(subjectId, unitNumber)
    }

    // Operations
    suspend fun getSubjectById(id: Int): Subject? = repository.getSubjectById(id)
    suspend fun getPdfById(id: Int): PdfShelfItem? = repository.getPdfById(id)

    fun addSubject(name: String, iconName: String = "Book", colorHex: String = "#1A1A1A") {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name.trim(), iconName = iconName, colorHex = colorHex))
        }
    }

    fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            repository.deleteSubjectById(subjectId)
        }
    }

    fun addPdf(
        subjectId: Int,
        unitNumber: Int,
        displayName: String,
        filePath: String,
        fileSizeString: String,
        isImportant: Boolean = false,
        isPyq: Boolean = false
    ) {
        viewModelScope.launch {
            val item = PdfShelfItem(
                subjectId = subjectId,
                unitNumber = unitNumber.coerceIn(1, 5),
                customDisplayName = displayName.trim(),
                filePath = filePath,
                fileSizeString = fileSizeString,
                isImportant = isImportant,
                isPyq = isPyq
            )
            repository.insertPdf(item)
        }
    }

    fun renamePdf(pdfId: Int, newName: String) {
        viewModelScope.launch {
            val pdf = repository.getPdfById(pdfId)
            if (pdf != null) {
                repository.updatePdf(pdf.copy(customDisplayName = newName.trim()))
            }
        }
    }

    fun togglePdfImportant(pdfId: Int) {
        viewModelScope.launch {
            val pdf = repository.getPdfById(pdfId)
            if (pdf != null) {
                repository.updatePdf(pdf.copy(isImportant = !pdf.isImportant))
            }
        }
    }

    fun togglePdfPyq(pdfId: Int) {
        viewModelScope.launch {
            val pdf = repository.getPdfById(pdfId)
            if (pdf != null) {
                repository.updatePdf(pdf.copy(isPyq = !pdf.isPyq))
            }
        }
    }

    fun movePdfSubject(pdfId: Int, targetSubjectId: Int, targetUnitNumber: Int) {
        viewModelScope.launch {
            val pdf = repository.getPdfById(pdfId)
            if (pdf != null) {
                repository.updatePdf(
                    pdf.copy(
                        subjectId = targetSubjectId,
                        unitNumber = targetUnitNumber.coerceIn(1, 5)
                    )
                )
            }
        }
    }

    fun recordPdfOpened(pdfId: Int) {
        viewModelScope.launch {
            val pdf = repository.getPdfById(pdfId)
            if (pdf != null) {
                repository.updatePdf(pdf.copy(lastOpened = System.currentTimeMillis()))
            }
        }
    }

    fun removePdf(pdfId: Int) {
        viewModelScope.launch {
            repository.deletePdfById(pdfId)
        }
    }

    // Offline Premium Backup & Restore (JSON-Based Serialization)
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Fetch direct elements from database flow single emitters
                val subjectsList = repository.allSubjectsWithPdfCount.first()
                val pdfsList = repository.allPdfsWithSubject.first()

                val backupObj = JSONObject()
                backupObj.put("backupVersion", 1)
                backupObj.put("appName", "StudyShelf")
                backupObj.put("timestamp", System.currentTimeMillis())

                val subjectsArray = JSONArray()
                subjectsList.forEach { s ->
                    val sObj = JSONObject()
                    sObj.put("id", s.id)
                    sObj.put("name", s.name)
                    sObj.put("iconName", s.iconName)
                    sObj.put("colorHex", s.colorHex)
                    subjectsArray.put(sObj)
                }
                backupObj.put("subjects", subjectsArray)

                val pdfsArray = JSONArray()
                pdfsList.forEach { p ->
                    val pObj = JSONObject()
                    pObj.put("subjectId", p.subjectId)
                    pObj.put("unitNumber", p.unitNumber)
                    pObj.put("customDisplayName", p.customDisplayName)
                    pObj.put("filePath", p.filePath)
                    pObj.put("fileSizeString", p.fileSizeString)
                    pObj.put("isImportant", p.isImportant)
                    pObj.put("isPyq", p.isPyq)
                    pObj.put("dateAdded", p.dateAdded)
                    pObj.put("lastOpened", p.lastOpened)
                    pdfsArray.put(pObj)
                }
                backupObj.put("pdfs", pdfsArray)

                onResult(backupObj.toString(4))
            } catch (e: Exception) {
                onResult("")
            }
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupObj = JSONObject(jsonString)
                if (!backupObj.has("subjects")) {
                    onResult(false, "Invalid Backup: Missing Subject items")
                    return@launch
                }

                val subjectsArray = backupObj.getJSONArray("subjects")
                val pdfsArray = backupObj.optJSONArray("pdfs") ?: JSONArray()

                // Insert elements
                for (i in 0 until subjectsArray.length()) {
                    val sObj = subjectsArray.getJSONObject(i)
                    val sName = sObj.getString("name")
                    val sIcon = sObj.optString("iconName", "Book")
                    val sColor = sObj.optString("colorHex", "#1A1A1A")

                    // Create subject and get database assigned ID
                    val newSubId = repository.insertSubject(Subject(name = sName, iconName = sIcon, colorHex = sColor)).toInt()

                    // Match matching PDFs in backup associated with the old ID and insert them with the new ID mapping
                    val oldSubId = sObj.optInt("id", -1)
                    if (oldSubId != -1) {
                        for (j in 0 until pdfsArray.length()) {
                            val pObj = pdfsArray.getJSONObject(j)
                            val pSubId = pObj.getInt("subjectId")
                            if (pSubId == oldSubId) {
                                val item = PdfShelfItem(
                                    subjectId = newSubId,
                                    unitNumber = pObj.optInt("unitNumber", 1).coerceIn(1, 5),
                                    customDisplayName = pObj.getString("customDisplayName"),
                                    filePath = pObj.getString("filePath"),
                                    fileSizeString = pObj.optString("fileSizeString", "Unknown"),
                                    isImportant = pObj.optBoolean("isImportant", false),
                                    isPyq = pObj.optBoolean("isPyq", false),
                                    dateAdded = pObj.optLong("dateAdded", System.currentTimeMillis()),
                                    lastOpened = pObj.optLong("lastOpened", 0L)
                                )
                                repository.insertPdf(item)
                            }
                        }
                    }
                }
                onResult(true, "Backup imported successfully! Loaded ${subjectsArray.length()} subjects.")
            } catch (e: Exception) {
                onResult(false, "Restoration failure: ${e.localizedMessage ?: "Unknown syntax error"}")
            }
        }
    }

    // Simple Factory for simple ViewModelProvider instantiations
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudyShelfViewModel::class.java)) {
                val db = StudyShelfDatabase.getDatabase(context)
                val repository = StudyShelfRepository(db.subjectDao(), db.pdfShelfItemDao(), db.appSettingDao())
                @Suppress("UNCHECKED_CAST")
                return StudyShelfViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
