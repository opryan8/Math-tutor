package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MathTutorViewModel(
    application: Application,
    private val repository: MathTutorRepository
) : AndroidViewModel(application) {

    private val geminiRepository = GeminiRepository()

    // Observables directly bound to Room Database (Reactive UI core)
    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val quizHistory: StateFlow<List<QuizHistory>> = repository.quizHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Active Practice State ---
    var quizCategorySelected by mutableStateOf("Linear Equations")
        private set

    var currentProblem by mutableStateOf<MathProblem?>(null)
        private set

    var shownExplanation by mutableStateOf(false)
        private set

    var answerChecked by mutableStateOf(false)
        private set

    var selectedOption by mutableStateOf<String?>(null)
        private set

    var isLastAnswerCorrect by mutableStateOf<Boolean?>(null)
        private set

    // --- AI Chat Tutor State ---
    var chatInput by mutableStateOf("")
    var isChatLoading by mutableStateOf(false)
        private set

    init {
        // Prepare initial practice question
        nextQuizQuestion(quizCategorySelected)
        // Ensure profile is initialized
        viewModelScope.launch {
            repository.userProgress.collect { progress ->
                if (progress == null) {
                    repository.saveUserProgress(UserProgress())
                }
            }
        }
    }

    // --- Practice Handlers ---

    fun changePracticeCategory(category: String) {
        quizCategorySelected = category
        nextQuizQuestion(category)
    }

    fun selectOption(option: String) {
        if (!answerChecked) {
            selectedOption = option
        }
    }

    fun checkAnswer() {
        val problem = currentProblem ?: return
        val currentOption = selectedOption ?: return
        
        answerChecked = true
        val correct = currentOption == problem.solution
        isLastAnswerCorrect = correct
        shownExplanation = true

        viewModelScope.launch {
            val progressList = repository.userProgress.stateIn(viewModelScope).value ?: UserProgress()
            
            // Calculate Gamification Gains
            val xpGained = if (correct) 15 else 5
            val newExp = progressList.exp + xpGained
            
            // 100 XP per level
            val newLevel = 1 + (newExp / 100)
            
            // Check Streaks
            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L
            val isConsecutive = (currentTime - progressList.lastActiveTimestamp) in (dayInMillis..(dayInMillis * 2))
            val isSameDay = (currentTime - progressList.lastActiveTimestamp) <= dayInMillis
            
            val newStreak = when {
                progressList.lastActiveTimestamp == 0L -> 1
                isConsecutive -> progressList.streak + 1
                isSameDay -> progressList.streak
                else -> 1 // Streak broken or reset
            }

            val updatedProgress = progressList.copy(
                level = newLevel,
                exp = newExp,
                streak = newStreak,
                lastActiveTimestamp = currentTime,
                problemsSolved = progressList.problemsSolved + 1,
                correctProblems = if (correct) progressList.correctProblems + 1 else progressList.correctProblems
            )
            repository.saveUserProgress(updatedProgress)

            // Save in Quiz History SQLite Database
            repository.insertQuizHistory(
                QuizHistory(
                    equation = problem.equation,
                    category = problem.category,
                    userAnswer = currentOption,
                    correctAnswer = problem.solution,
                    isCorrect = correct,
                    xpEarned = xpGained,
                    timestamp = currentTime
                )
            )
        }
    }

    fun nextQuizQuestion(category: String) {
        currentProblem = MathEngine.generateProblem(category)
        selectedOption = null
        answerChecked = false
        isLastAnswerCorrect = null
        shownExplanation = false
    }

    // --- Lesson Accomplishments ---

    fun completeLesson(lessonId: String, xpReward: Int) {
        viewModelScope.launch {
            val progressList = repository.userProgress.stateIn(viewModelScope).value ?: UserProgress()
            val currentLessons = progressList.completedLessonsCsv.split(",").filter { it.isNotEmpty() }.toMutableSet()
            
            if (!currentLessons.contains(lessonId)) {
                currentLessons.add(lessonId)
                val newExp = progressList.exp + xpReward
                val newLevel = 1 + (newExp / 100)
                
                val updatedProgress = progressList.copy(
                    level = newLevel,
                    exp = newExp,
                    lastActiveTimestamp = System.currentTimeMillis(),
                    completedLessonsCsv = currentLessons.joinToString(",")
                )
                repository.saveUserProgress(updatedProgress)
            }
        }
    }

    // --- AI Chat Tutor Interaction ---

    fun sendChatMessage() {
        val messageText = chatInput.trim()
        if (messageText.isEmpty()) return

        chatInput = ""
        isChatLoading = true

        viewModelScope.launch {
            // Save user message in Room
            val userMsg = ChatMessage(text = messageText, isUser = true)
            repository.insertChatMessage(userMsg)

            // Get recent context
            val recentHistory = repository.getRecentMessagesForContext()

            // Fetch Gemini tutor response
            val replyText = geminiRepository.getTutorResponse(messageText, recentHistory)

            // Save AI reply in Room
            val aiMsg = ChatMessage(text = replyText, isUser = false)
            repository.insertChatMessage(aiMsg)

            isChatLoading = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Add initial welcome instruction
            repository.insertChatMessage(
                ChatMessage(
                    text = "Hello! I am your AI Math Tutor. Ask me any algebraic questions, paste equations you want me to explain, or say 'give me a challenge' to start!",
                    isUser = false
                )
            )
        }
    }

    // Prepopulate welcome text if empty
    fun initializeChatWithWelcome() {
        viewModelScope.launch {
            val current = repository.getRecentMessagesForContext()
            if (current.isEmpty()) {
                repository.insertChatMessage(
                    ChatMessage(
                        text = "Hello! I am your AI Math Tutor. Ask me any algebra questions, like: \"Why do we flip the inequality sign when dividing by a negative number?\" or type any equation for a step-by-step tutorial!",
                        isUser = false
                    )
                )
            }
        }
    }
}

// --- Factory Generation for ViewModel Injection ---

class MathTutorViewModelFactory(
    private val application: Application,
    private val repository: MathTutorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MathTutorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MathTutorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
