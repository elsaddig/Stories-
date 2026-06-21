package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.SavedWord
import com.example.data.db.UserStats
import com.example.data.model.QuizQuestion
import com.example.data.model.Story
import com.example.data.model.StoryData
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    Dashboard,
    Stories,
    Dictionary,
    Flashcards
}

enum class ReadingMode {
    Normal,
    Cloze
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.savedWordDao(), database.userStatsDao())

    // App core navigation
    private val _currentScreen = MutableStateFlow(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Database Flows
    val userStats: StateFlow<UserStats> = repository.userStats
        .map { it ?: UserStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val savedWords: StateFlow<List<SavedWord>> = repository.allSavedWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedCount: StateFlow<Int> = repository.learnedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSavedCount: StateFlow<Int> = repository.totalCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Stories List
    val availableStories = StoryData.stories

    // Selected Story View State
    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    private val _selectedWordForLookup = MutableStateFlow<String?>(null)
    val selectedWordForLookup: StateFlow<String?> = _selectedWordForLookup.asStateFlow()

    private val _isAdvancedLevel = MutableStateFlow(false)
    val isAdvancedLevel: StateFlow<Boolean> = _isAdvancedLevel.asStateFlow()

    private val _readingMode = MutableStateFlow(ReadingMode.Normal)
    val readingMode: StateFlow<ReadingMode> = _readingMode.asStateFlow()

    // Cloze Test Interactive States
    // Map of specific word index/identifer to guessed word.
    private val _clozeGuesses = MutableStateFlow<Map<String, String>>(emptyMap())
    val clozeGuesses: StateFlow<Map<String, String>> = _clozeGuesses.asStateFlow()

    private val _revealedClozeWords = MutableStateFlow<Set<String>>(emptySet())
    val revealedClozeWords: StateFlow<Set<String>> = _revealedClozeWords.asStateFlow()

    // Quiz Navigation States
    private val _activeQuizIndex = MutableStateFlow(-1) // -1 means quiz is inactive, 0..N means active question, -2 means finished review
    val activeQuizIndex: StateFlow<Int> = _activeQuizIndex.asStateFlow()

    private val _quizSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // QuestID -> selectedOptionIndex
    val quizSelectedAnswers: StateFlow<Map<Int, Int>> = _quizSelectedAnswers.asStateFlow()

    private val _quizSumbitted = MutableStateFlow(false)
    val quizSubmitted: StateFlow<Boolean> = _quizSumbitted.asStateFlow()

    // Shadowing Dialog States
    private val _activeShadowSentence = MutableStateFlow<String?>(null)
    val activeShadowSentence: StateFlow<String?> = _activeShadowSentence.asStateFlow()

    private val _isRecordingShadow = MutableStateFlow(false)
    val isRecordingShadow: StateFlow<Boolean> = _isRecordingShadow.asStateFlow()

    private val _shadowScore = MutableStateFlow<Int?>(null)
    val shadowScore: StateFlow<Int?> = _shadowScore.asStateFlow()

    private val _shadowWaveform = MutableStateFlow<List<Float>>(emptyList())
    val shadowWaveform: StateFlow<List<Float>> = _shadowWaveform.asStateFlow()

    // Dictionary states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lookupResult = MutableStateFlow<SavedWord?>(null)
    val lookupResult: StateFlow<SavedWord?> = _lookupResult.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Flashcards state
    private val _activeCardIndex = MutableStateFlow(0)
    val activeCardIndex: StateFlow<Int> = _activeCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // Background Shadowing Simulation Jobs
    private var recordingJob: Job? = null

    init {
        // Initial streak integrity check
        viewModelScope.launch {
            repository.updateStreak()
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        // If changing screens, close story and stop search
        if (screen != AppScreen.Stories) {
            _activeStory.value = null
        }
    }

    // Story Management
    fun selectStory(story: Story) {
        _activeStory.value = story
        _isAdvancedLevel.value = false
        _readingMode.value = ReadingMode.Normal
        _clozeGuesses.value = emptyMap()
        _revealedClozeWords.value = emptySet()
        _activeQuizIndex.value = -1
        _quizSelectedAnswers.value = emptyMap()
        _quizSumbitted.value = false
        _activeShadowSentence.value = null
        _shadowScore.value = null
        _isRecordingShadow.value = false
        _currentScreen.value = AppScreen.Stories
    }

    fun closeStory() {
        _activeStory.value = null
        _activeQuizIndex.value = -1
        _quizSelectedAnswers.value = emptyMap()
        _quizSumbitted.value = false
        _activeShadowSentence.value = null
        _shadowScore.value = null
    }

    fun toggleReadingLevel(advanced: Boolean) {
        _isAdvancedLevel.value = advanced
        // Reset Cloze when switching level
        _clozeGuesses.value = emptyMap()
        _revealedClozeWords.value = emptySet()
        _quizSelectedAnswers.value = emptyMap()
        _quizSumbitted.value = false
        _activeQuizIndex.value = -1
    }

    fun setReadingMode(mode: ReadingMode) {
        _readingMode.value = mode
        if (mode == ReadingMode.Cloze) {
            _clozeGuesses.value = emptyMap()
            _revealedClozeWords.value = emptySet()
        }
    }

    // Cloze Test Interactions
    fun guessClozeWord(wordKey: String, guessedWord: String, isCorrect: Boolean) {
        val current = _clozeGuesses.value.toMutableMap()
        current[wordKey] = guessedWord
        _clozeGuesses.value = current

        if (isCorrect) {
            val correctSet = _revealedClozeWords.value.toMutableSet()
            correctSet.add(wordKey)
            _revealedClozeWords.value = correctSet
        }
    }

    // Quiz functions
    fun startQuiz() {
        _activeQuizIndex.value = 0
        _quizSelectedAnswers.value = emptyMap()
        _quizSumbitted.value = false
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        if (_quizSumbitted.value) return
        val current = _quizSelectedAnswers.value.toMutableMap()
        current[questionIndex] = optionIndex
        _quizSelectedAnswers.value = current
    }

    fun nextQuizQuestion(totalQuestions: Int) {
        val current = _activeQuizIndex.value
        if (current < totalQuestions - 1) {
            _activeQuizIndex.value = current + 1
        } else {
            // Reached end, submit and mark story read!
            _quizSumbitted.value = true
            _activeQuizIndex.value = -2 // Finished Screen

            // Trigger Story Complete Persistence & Streak Increment
            viewModelScope.launch {
                _activeStory.value?.let { story ->
                    repository.markStoryAsRead(story.id)
                }
            }
        }
    }

    fun resetQuiz() {
        _activeQuizIndex.value = -1
        _quizSelectedAnswers.value = emptyMap()
        _quizSumbitted.value = false
    }

    // Shadowing Simulator
    fun selectSentenceForShadowing(sentence: String) {
        _activeShadowSentence.value = sentence
        _shadowScore.value = null
        _isRecordingShadow.value = false
        _shadowWaveform.value = emptyList()
    }

    fun closeShadowing() {
        _activeShadowSentence.value = null
        recordingJob?.cancel()
        _isRecordingShadow.value = false
    }

    fun startRecordingShadow() {
        _isRecordingShadow.value = true
        _shadowScore.value = null
        
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            // Generate visual waveform dynamically to resemble real mic detection
            val list = mutableListOf<Float>()
            for (i in 0..40) {
                list.add(Random.nextFloat() * 0.1f)
            }
            _shadowWaveform.value = list

            // Simulate amplitude changes
            for (tick in 1..25) {
                delay(120)
                val updatedWave = _shadowWaveform.value.map {
                    // Random burst corresponding to speech peaks
                    if (Random.nextFloat() > 0.4f) Random.nextFloat() * 0.8f else Random.nextFloat() * 0.2f
                }
                _shadowWaveform.value = updatedWave
            }

            stopRecordingShadow()
        }
    }

    fun stopRecordingShadow() {
        recordingJob?.cancel()
        if (_isRecordingShadow.value) {
            _isRecordingShadow.value = false
            // Calculate a nice, encouraging high-fidelity shadowing score based on speech rhythms
            val simulatedScore = Random.nextInt(78, 99)
            _shadowScore.value = simulatedScore
            
            // Increment streak because they practiced audio shadowing!
            viewModelScope.launch {
                repository.updateStreak()
            }
        }
    }

    // Smart Dictionary Searches
    fun setQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _lookupResult.value = null
            _searchError.value = null
        }
    }

    fun lookupWordInput(word: String, contextSentence: String = "") {
        if (word.isBlank()) return
        
        _isSearchLoading.value = true
        _searchError.value = null
        
        viewModelScope.launch {
            try {
                val result = repository.lookupWord(word.trim(), contextSentence)
                _lookupResult.value = result
            } catch (e: Exception) {
                _searchError.value = "Unable to process definition. Please check connectivity."
                Log.e("MainViewModel", "Search Error: ${e.message}")
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    // Saved Deck Interactions
    fun saveSearchedWord() {
        _lookupResult.value?.let { word ->
            viewModelScope.launch {
                repository.saveWord(word)
                // Instantly update current lookup with visual sign that is saved in DB
                _lookupResult.value = word
            }
        }
    }

    fun deleteWordFromDeck(wordText: String) {
        viewModelScope.launch {
            repository.deleteWord(wordText)
        }
    }

    fun toggleCardLearned(wordText: String, isLearned: Boolean) {
        viewModelScope.launch {
            repository.toggleLearned(wordText, isLearned)
        }
    }

    // Flashcards reviews
    fun setCardIndex(index: Int) {
        _activeCardIndex.value = index
        _isCardFlipped.value = false
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }
}
