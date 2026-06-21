package com.example.data.model

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class Story(
    val id: String,
    val title: String,
    val imageUrl: String, // Decorative background image generated dynamically or cached
    val summary: String,
    val b1Text: String,   // Simple level
    val b2Text: String,   // Advanced level
    val b1MaskedWords: List<String>, // Words hidden in Cloze Test for B1
    val b2MaskedWords: List<String>, // Words hidden in Cloze Test for B2
    val b1Quiz: List<QuizQuestion>,
    val b2Quiz: List<QuizQuestion>
)
