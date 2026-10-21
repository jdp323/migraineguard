package com.migraineguard.app.core.ai

import org.junit.Assert.*
import org.junit.Test
import okio.Buffer
import org.json.JSONObject

class ProviderSettingsTest {
    @Test fun ambiguousKeysAreNotAutoAssigned() { assertNull(AiCatalog.suggest("sk-generic-fixture")); assertEquals("claude",AiCatalog.suggest("sk-ant-fixture")); assertEquals("openrouter",AiCatalog.suggest("sk-or-v1-fixture")); assertEquals("openai",AiCatalog.suggest("sk-proj-fixture")); assertNull(AiCatalog.suggest("")) }
    @Test fun openAiRequestKeepsSecretInHeaderAndDisablesStorage() {
        val request=buildServiceRequest(AiCatalog.service("openai")!!,AiConnection("fixture-private-key","gpt-4.1-mini"),"fixture prompt","YWJj","image/jpeg")
        val buffer=Buffer();request.body!!.writeTo(buffer);val body=buffer.readUtf8();val json=JSONObject(body)
        assertEquals("api.openai.com",request.url.host);assertEquals("Bearer fixture-private-key",request.header("Authorization"));assertFalse(json.getBoolean("store"));assertFalse(body.contains("fixture-private-key"));assertTrue(body.contains("input_image"))
    }
    @Test fun claudeUsesItsOwnProtocol() {
        val r=buildServiceRequest(AiCatalog.service("claude")!!,AiConnection("fixture-anthropic","fixture-model"),"prompt","YWJj")
        assertEquals("api.anthropic.com",r.url.host);assertEquals("fixture-anthropic",r.header("x-api-key"));assertEquals("2023-06-01",r.header("anthropic-version"));assertNull(r.header("Authorization"))
    }
    @Test fun parseActualProtocolText() {
        assertEquals("open",parseServiceOutput(ProviderProtocol.RESPONSES,"{\"status\":\"completed\",\"output\":[{\"content\":[{\"type\":\"output_text\",\"text\":\"open\"}]}]}"))
        assertEquals("claude",parseServiceOutput(ProviderProtocol.CLAUDE,"{\"content\":[{\"type\":\"text\",\"text\":\"claude\"}]}"))
        assertEquals("chat",parseServiceOutput(ProviderProtocol.CHAT,"{\"choices\":[{\"message\":{\"content\":\"chat\"}}]}"))
        assertEquals("gemini",parseServiceOutput(ProviderProtocol.GEMINI,"{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"gemini\"}]}}]}"))
    }
    @Test(expected=IllegalArgumentException::class) fun incompleteResponsesDoNotPass() { parseServiceOutput(ProviderProtocol.RESPONSES,"{\"status\":\"incomplete\",\"output\":[]}") }
    @Test(expected=IllegalArgumentException::class) fun unsupportedImageProviderCannotBuildUpload() { buildServiceRequest(AiCatalog.service("deepseek")!!,AiConnection("fixture","model"),"prompt","YWJj") }
}
