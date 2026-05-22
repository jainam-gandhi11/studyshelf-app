package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pdf_shelf_items",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class PdfShelfItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val unitNumber: Int, // 1 to 5
    val customDisplayName: String,
    val filePath: String, // Android Content URI or file path string
    val fileSizeString: String = "Unknown Size",
    val isImportant: Boolean = false,
    val isPyq: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastOpened: Long = 0L
)
