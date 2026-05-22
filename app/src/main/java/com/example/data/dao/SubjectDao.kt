package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Subject
import kotlinx.coroutines.flow.Flow

data class SubjectWithPdfCount(
    val id: Int,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val pdfCount: Int
)

@Dao
interface SubjectDao {
    @Query("""
        SELECT s.id, s.name, s.iconName, s.colorHex, COUNT(p.id) as pdfCount 
        FROM subjects s 
        LEFT JOIN pdf_shelf_items p ON s.id = p.subjectId 
        GROUP BY s.id 
        ORDER BY s.name ASC
    """)
    fun getAllSubjectsWithPdfCount(): Flow<List<SubjectWithPdfCount>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Int)

    @Query("SELECT COUNT(*) FROM subjects")
    fun getSubjectsCountFlow(): Flow<Int>
}
