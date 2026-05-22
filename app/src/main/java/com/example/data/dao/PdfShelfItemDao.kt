package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PdfShelfItem
import kotlinx.coroutines.flow.Flow

data class PdfWithSubjectName(
    val id: Int,
    val subjectId: Int,
    val subjectName: String,
    val unitNumber: Int,
    val customDisplayName: String,
    val filePath: String,
    val fileSizeString: String,
    val isImportant: Boolean,
    val isPyq: Boolean,
    val dateAdded: Long,
    val lastOpened: Long
)

@Dao
interface PdfShelfItemDao {
    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        ORDER BY p.dateAdded DESC
    """)
    fun getAllPdfsWithSubject(): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.subjectId = :subjectId
        ORDER BY p.unitNumber ASC, p.dateAdded DESC
    """)
    fun getPdfsForSubject(subjectId: Int): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.subjectId = :subjectId AND p.unitNumber = :unitNumber
        ORDER BY p.dateAdded DESC
    """)
    fun getPdfsForSubjectAndUnit(subjectId: Int, unitNumber: Int): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.isImportant = 1
        ORDER BY p.dateAdded DESC
    """)
    fun getImportantPdfs(): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName  
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.isPyq = 1
        ORDER BY p.dateAdded DESC
    """)
    fun getPyqPdfs(): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.lastOpened > 0
        ORDER BY p.lastOpened DESC
        LIMIT 10
    """)
    fun getRecentlyOpenedPdfs(): Flow<List<PdfWithSubjectName>>

    @Query("""
        SELECT p.*, s.name as subjectName 
        FROM pdf_shelf_items p
        JOIN subjects s ON p.subjectId = s.id
        WHERE p.customDisplayName LIKE '%' || :query || '%' 
           OR s.name LIKE '%' || :query || '%'
        ORDER BY p.dateAdded DESC
    """)
    fun searchPdfs(query: String): Flow<List<PdfWithSubjectName>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdfShelfItem: PdfShelfItem): Long

    @Update
    suspend fun updatePdf(pdfShelfItem: PdfShelfItem)

    @Query("DELETE FROM pdf_shelf_items WHERE id = :id")
    suspend fun deletePdfById(id: Int)

    @Query("SELECT COUNT(*) FROM pdf_shelf_items")
    fun getPdfCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pdf_shelf_items WHERE isImportant = 1")
    fun getImportantPdfCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pdf_shelf_items WHERE isPyq = 1")
    fun getPyqPdfCountFlow(): Flow<Int>

    @Query("SELECT * FROM pdf_shelf_items WHERE id = :id")
    suspend fun getPdfById(id: Int): PdfShelfItem?
}
