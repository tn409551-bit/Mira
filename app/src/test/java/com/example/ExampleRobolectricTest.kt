package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Odia Jarvis app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ODIA JARVIS AI", appName)
    }

    @Test
    fun `chat entities instantiate properly`() {
        val session = ChatSessionEntity(id = 1, title = "ଓଡ଼ିଆ କଥାବାର୍ତ୍ତା", language = "or")
        assertEquals("ଓଡ଼ିଆ କଥାବାର୍ତ୍ତା", session.title)
        assertEquals("or", session.language)

        val message = ChatMessageEntity(
            id = 10,
            sessionId = 1,
            sender = "assistant",
            text = "ନମସ୍କାର! ମୁଁ ଆପଣଙ୍କ ଓଡ଼ିଆ ଜାର୍ଭିସ୍ ଏଆଇ ସହାୟକ।"
        )
        assertEquals("assistant", message.sender)
        assertNotNull(message.text)
    }
}
