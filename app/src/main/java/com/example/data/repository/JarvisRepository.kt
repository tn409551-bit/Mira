package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val chatDao: ChatDao,
    private val geminiApiClient: GeminiApiClient = GeminiApiClient()
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String = "ନୂତନ ବାର୍ତ୍ତାଳାପ", language: String = "or"): Long {
        val session = ChatSessionEntity(
            title = title,
            language = language,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return chatDao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Long, newTitle: String) {
        val existing = chatDao.getSessionById(sessionId) ?: return
        chatDao.updateSession(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllHistory() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun saveMessage(
        sessionId: Long,
        sender: String,
        text: String,
        language: String = "or",
        isError: Boolean = false
    ): Long {
        val msg = ChatMessageEntity(
            sessionId = sessionId,
            sender = sender,
            text = text,
            language = language,
            isError = isError,
            timestamp = System.currentTimeMillis()
        )
        val id = chatDao.insertMessage(msg)
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val title = if (session.title == "ନୂତନ ବାର୍ତ୍ତାଳାପ" && sender == "user") {
                if (text.length > 25) text.take(25) + "..." else text
            } else {
                session.title
            }
            chatDao.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
        return id
    }

    suspend fun generateAiResponse(
        apiKey: String,
        model: String,
        sessionId: Long,
        userPrompt: String,
        primaryLanguage: String = "or"
    ): Result<String> {
        val recentMessages = chatDao.getRecentMessages(sessionId, limit = 8).reversed()

        val contents = mutableListOf<GeminiContent>()
        for (m in recentMessages) {
            val role = if (m.sender == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = m.text))
                )
            )
        }

        // Add current user prompt
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userPrompt))
            )
        )

        val systemPrompt = buildSystemPrompt(primaryLanguage)

        return geminiApiClient.generateContent(
            apiKey = apiKey,
            model = model,
            history = contents,
            systemInstructionText = systemPrompt
        )
    }

    suspend fun testApiConnection(apiKey: String, model: String): Result<String> {
        val testPrompt = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = "Respond in 3 words: System online."))
            )
        )
        return geminiApiClient.generateContent(
            apiKey = apiKey,
            model = model,
            history = testPrompt,
            systemInstructionText = "You are ODIA JARVIS AI. Confirm system status briefly."
        )
    }

    private fun buildSystemPrompt(language: String): String {
        return """
            You are ODIA JARVIS AI, an advanced, futuristic personal AI assistant inspired by Jarvis, created by developer Tikeswar.
            Current preferred user language setting: $language (Odia='or', Hindi='hi', English='en').
            
            Key Directives:
            1. If preferred language is Odia ('or') or the user asks in Odia, respond primarily in fluent, natural, respectful Odia (ଓଡ଼ିଆ).
            2. If the user writes in Hindi or English, seamlessly respond in that language.
            3. Capabilities:
               - General knowledge and deep insights on Odisha culture, Jagannath temple, history, geography, festivals, and literature.
               - Educational tutoring in Mathematics, Science, and Coding with step-by-step clarity.
               - Creative writing: Odia stories (ଗଳ୍ପ), poetry (କବିତା), and traditional riddles (ଢଗଢମାଳି).
               - Precise translation between Odia, Hindi, and English.
               - Daily planning, productivity, and motivation.
            4. Tone: High-tech, intelligent, respectful, polite, and enthusiastic like Jarvis.
            5. Always provide direct, helpful answers without unnecessary generic filler.
        """.trimIndent()
    }
}
