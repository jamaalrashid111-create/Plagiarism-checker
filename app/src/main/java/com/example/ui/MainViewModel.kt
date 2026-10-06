package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsPreferences
import com.example.data.model.CheckingStep
import com.example.data.model.DocumentExtractionResult
import com.example.data.model.DocumentStats
import com.example.data.model.PlagiarismCheckEntity
import com.example.data.model.PlagiarismMatch
import com.example.data.model.PlagiarismResponse
import com.example.data.model.PlagiarismSource
import com.example.data.model.PlanType
import com.example.data.model.UserProfile
import com.example.data.remote.ExternalPlagiarismProvider
import com.example.data.remote.PlagiarismService
import com.example.domain.DocumentProcessor
import com.example.domain.PdfReportGenerator
import com.example.domain.SubscriptionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class NavigationTab {
    HOME,
    HISTORY,
    REPORTS,
    SETTINGS
}

data class MainUiState(
    val currentTab: NavigationTab = NavigationTab.HOME,
    val homeInputMode: Int = 0, // 0 = Paste Text, 1 = Upload Document
    val inputText: String = "",
    val uploadedFileName: String? = null,
    val documentStats: DocumentStats = DocumentStats(0, 0, 0, 0),
    val isChecking: Boolean = false,
    val checkingStep: CheckingStep = CheckingStep.ANALYZING,
    val currentResult: PlagiarismResponse? = null,
    val currentDocumentName: String = "Untitled Document",
    val activeJobId: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val history: List<PlagiarismCheckEntity> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val isApiConfigured: Boolean = false,
    val apiStatusMessage: String = "",
    val generatedPdfFile: File? = null,
    val generatedPdfUri: Uri? = null,
    val showSourcesScreen: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = SettingsPreferences(application)
    private val database = AppDatabase.getDatabase(application)
    private val plagiarismDao = database.plagiarismDao()
    private val provider = ExternalPlagiarismProvider(preferences)
    private val plagiarismService = PlagiarismService(application, provider, preferences)
    private val subscriptionManager = SubscriptionManager(preferences)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val sourcesListType = Types.newParameterizedType(List::class.java, PlagiarismSource::class.java)
    private val matchesListType = Types.newParameterizedType(List::class.java, PlagiarismMatch::class.java)
    private val sourcesAdapter = moshi.adapter<List<PlagiarismSource>>(sourcesListType)
    private val matchesAdapter = moshi.adapter<List<PlagiarismMatch>>(matchesListType)

    private val _uiState = MutableStateFlow(
        MainUiState(
            isApiConfigured = plagiarismService.isConfigured(),
            apiStatusMessage = plagiarismService.getConfigurationStatus(),
            userProfile = subscriptionManager.loadCurrentProfile()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var checkJob: Job? = null

    init {
        // Collect history
        viewModelScope.launch {
            plagiarismDao.getAllChecks().collectLatest { list ->
                _uiState.update { it.copy(history = list) }
            }
        }

        // Collect subscription state
        viewModelScope.launch {
            subscriptionManager.userProfile.collectLatest { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab, showSourcesScreen = false) }
    }

    fun setHomeInputMode(mode: Int) {
        _uiState.update { it.copy(homeInputMode = mode) }
    }

    fun updateInputText(text: String) {
        val stats = DocumentProcessor.calculateStats(text)
        _uiState.update {
            it.copy(
                inputText = text,
                documentStats = stats,
                errorMessage = null
            )
        }
    }

    fun clearInputText() {
        _uiState.update {
            it.copy(
                inputText = "",
                uploadedFileName = null,
                documentStats = DocumentStats(0, 0, 0, 0),
                errorMessage = null
            )
        }
    }

    fun handleDocumentSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            when (val result = DocumentProcessor.processUri(context, uri)) {
                is DocumentExtractionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            inputText = result.text,
                            uploadedFileName = result.fileName,
                            currentDocumentName = result.fileName,
                            documentStats = result.stats,
                            errorMessage = null,
                            infoMessage = "Document '${result.fileName}' loaded (${result.stats.wordCount} words)."
                        )
                    }
                }
                is DocumentExtractionResult.EmptyText -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Document '${result.fileName}' is empty.",
                            uploadedFileName = result.fileName
                        )
                    }
                }
                is DocumentExtractionResult.ScannedPdfNeedsOcr -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }
                is DocumentExtractionResult.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun runPlagiarismCheck() {
        val currentState = _uiState.value
        val text = currentState.inputText.trim()

        if (text.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No text entered.") }
            return
        }

        val (canCheck, limitError) = subscriptionManager.canPerformCheck(currentState.documentStats.wordCount)
        if (!canCheck && limitError != null) {
            _uiState.update { it.copy(errorMessage = limitError) }
            return
        }

        checkJob?.cancel()
        checkJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecking = true,
                    checkingStep = CheckingStep.ANALYZING,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            // Visual progressive status sequence
            delay(400)
            _uiState.update { it.copy(checkingStep = CheckingStep.SEARCHING) }
            delay(400)
            _uiState.update { it.copy(checkingStep = CheckingStep.COMPARING) }

            val docName = currentState.uploadedFileName ?: "Text Scan - ${System.currentTimeMillis() % 10000}"

            val result = plagiarismService.checkText(text)

            result.fold(
                onSuccess = { response ->
                    val checkId = UUID.randomUUID().toString()
                    val sourcesJson = sourcesAdapter.toJson(response.sources)
                    val matchesJson = matchesAdapter.toJson(response.matches)

                    val entity = PlagiarismCheckEntity(
                        id = checkId,
                        documentName = docName,
                        inputText = text,
                        timestamp = System.currentTimeMillis(),
                        wordCount = response.wordCount.takeIf { it > 0 } ?: currentState.documentStats.wordCount,
                        charCount = currentState.documentStats.charCount,
                        plagiarismPercentage = response.plagiarismPercentage,
                        originalPercentage = response.originalPercentage,
                        matchedWordCount = response.matchedWordCount,
                        sourcesJson = sourcesJson,
                        matchesJson = matchesJson
                    )

                    plagiarismDao.insertCheck(entity)
                    subscriptionManager.refreshProfile()

                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            currentResult = response,
                            currentDocumentName = docName,
                            currentTab = NavigationTab.REPORTS,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            errorMessage = error.localizedMessage ?: "Failed to perform plagiarism check."
                        )
                    }
                }
            )
        }
    }

    fun cancelCheck() {
        checkJob?.cancel()
        val jobId = _uiState.value.activeJobId
        if (jobId != null) {
            viewModelScope.launch {
                plagiarismService.cancelCheck(jobId)
            }
        }
        _uiState.update {
            it.copy(
                isChecking = false,
                errorMessage = "Plagiarism scan was cancelled."
            )
        }
    }

    fun openHistoricalReport(check: PlagiarismCheckEntity) {
        val sources = try {
            sourcesAdapter.fromJson(check.sourcesJson) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val matches = try {
            matchesAdapter.fromJson(check.matchesJson) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val response = PlagiarismResponse(
            jobId = check.id,
            status = "completed",
            plagiarismPercentage = check.plagiarismPercentage,
            originalPercentage = check.originalPercentage,
            wordCount = check.wordCount,
            matchedWordCount = check.matchedWordCount,
            sources = sources,
            matches = matches
        )

        _uiState.update {
            it.copy(
                currentResult = response,
                currentDocumentName = check.documentName,
                currentTab = NavigationTab.REPORTS,
                showSourcesScreen = false,
                errorMessage = null
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            plagiarismDao.deleteCheck(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            plagiarismDao.clearAllChecks()
            _uiState.update { it.copy(infoMessage = "All scan history cleared.") }
        }
    }

    fun toggleSourcesScreen(show: Boolean) {
        _uiState.update { it.copy(showSourcesScreen = show) }
    }

    fun exportPdfReport(context: Context, onComplete: (File, Uri) -> Unit) {
        val state = _uiState.value
        val result = state.currentResult ?: return

        viewModelScope.launch {
            try {
                val file = PdfReportGenerator.generateReportPdf(
                    context = context,
                    documentName = state.currentDocumentName,
                    wordCount = result.wordCount.takeIf { it > 0 } ?: state.documentStats.wordCount,
                    plagiarismPercentage = result.plagiarismPercentage,
                    originalPercentage = result.originalPercentage,
                    matchedWords = result.matchedWordCount,
                    sources = result.sources,
                    matches = result.matches
                )
                val uri = PdfReportGenerator.getShareableUri(context, file)
                _uiState.update {
                    it.copy(
                        generatedPdfFile = file,
                        generatedPdfUri = uri,
                        infoMessage = "PDF Report generated successfully."
                    )
                }
                onComplete(file, uri)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not generate PDF report: ${e.localizedMessage}")
                }
            }
        }
    }

    fun saveGeminiApiKey(key: String) {
        preferences.setCustomGeminiKey(key)
        _uiState.update {
            it.copy(
                isApiConfigured = plagiarismService.isConfigured(),
                apiStatusMessage = plagiarismService.getConfigurationStatus(),
                infoMessage = "Gemini AI Engine key configured."
            )
        }
    }

    fun saveCustomApiConfig(url: String, key: String) {
        preferences.setCustomApiConfig(url, key)
        _uiState.update {
            it.copy(
                isApiConfigured = plagiarismService.isConfigured(),
                apiStatusMessage = plagiarismService.getConfigurationStatus(),
                infoMessage = "API settings updated."
            )
        }
    }

    fun clearCustomApiConfig() {
        preferences.clearCustomApiConfig()
        _uiState.update {
            it.copy(
                isApiConfigured = plagiarismService.isConfigured(),
                apiStatusMessage = plagiarismService.getConfigurationStatus(),
                infoMessage = "Custom API settings reset."
            )
        }
    }

    fun loginUser(email: String, name: String) {
        subscriptionManager.login(email, name)
        _uiState.update { it.copy(infoMessage = "Logged in as $name.") }
    }

    fun logoutUser() {
        subscriptionManager.logout()
        _uiState.update { it.copy(infoMessage = "Logged out.") }
    }

    fun upgradePlan(plan: PlanType) {
        subscriptionManager.setPlan(plan)
        _uiState.update { it.copy(infoMessage = "Plan switched to ${plan.displayName}.") }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}
