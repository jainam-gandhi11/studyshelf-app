package com.example.data.repository

import com.example.data.dao.AppSettingDao
import com.example.data.dao.PdfShelfItemDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.SubjectWithPdfCount
import com.example.data.dao.PdfWithSubjectName
import com.example.data.entity.AppSetting
import com.example.data.entity.PdfShelfItem
import com.example.data.entity.Subject
import kotlinx.coroutines.flow.Flow

class StudyShelfRepository(
    private val subjectDao: SubjectDao,
    private val pdfShelfItemDao: PdfShelfItemDao,
    private val appSettingDao: AppSettingDao
) {
    // Subjects
    val allSubjectsWithPdfCount: Flow<List<SubjectWithPdfCount>> = subjectDao.getAllSubjectsWithPdfCount()
    val subjectsCount: Flow<Int> = subjectDao.getSubjectsCountFlow()

    suspend fun getSubjectById(id: Int): Subject? = subjectDao.getSubjectById(id)
    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubjectById(id: Int) = subjectDao.deleteSubjectById(id)

    // PDFs
    val allPdfsWithSubject: Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.getAllPdfsWithSubject()
    val pdfCount: Flow<Int> = pdfShelfItemDao.getPdfCountFlow()
    val importantPdfCount: Flow<Int> = pdfShelfItemDao.getImportantPdfCountFlow()
    val pyqPdfCount: Flow<Int> = pdfShelfItemDao.getPyqPdfCountFlow()

    fun getPdfsForSubject(subjectId: Int): Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.getPdfsForSubject(subjectId)
    fun getPdfsForSubjectAndUnit(subjectId: Int, unitNumber: Int): Flow<List<PdfWithSubjectName>> = 
        pdfShelfItemDao.getPdfsForSubjectAndUnit(subjectId, unitNumber)
    fun getImportantPdfs(): Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.getImportantPdfs()
    fun getPyqPdfs(): Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.getPyqPdfs()
    fun getRecentlyOpenedPdfs(): Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.getRecentlyOpenedPdfs()
    fun searchPdfs(query: String): Flow<List<PdfWithSubjectName>> = pdfShelfItemDao.searchPdfs(query)

    suspend fun getPdfById(id: Int): PdfShelfItem? = pdfShelfItemDao.getPdfById(id)
    suspend fun insertPdf(pdf: PdfShelfItem): Long = pdfShelfItemDao.insertPdf(pdf)
    suspend fun updatePdf(pdf: PdfShelfItem) = pdfShelfItemDao.updatePdf(pdf)
    suspend fun deletePdfById(id: Int) = pdfShelfItemDao.deletePdfById(id)

    // Settings
    fun getSettingFlow(key: String): Flow<AppSetting?> = appSettingDao.getSettingFlow(key)
    suspend fun getSetting(key: String): AppSetting? = appSettingDao.getSetting(key)
    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.saveSetting(AppSetting(key, value))
    }
}
