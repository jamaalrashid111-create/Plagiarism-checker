package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plagiarism_checks")
data class PlagiarismCheckEntity(
    @PrimaryKey
    val id: String,
    val documentName: String,
    val inputText: String,
    val timestamp: Long,
    val wordCount: Int,
    val charCount: Int,
    val plagiarismPercentage: Int,
    val originalPercentage: Int,
    val matchedWordCount: Int,
    val sourcesJson: String,
    val matchesJson: String
)
