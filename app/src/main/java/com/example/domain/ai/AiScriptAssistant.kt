package com.example.domain.ai

enum class ScriptFormat(val displayName: String) {
    YOUTUBE_LONG("YouTube Video (8-12 min)"),
    SHORT_REEL("Short / Reel / TikTok (60s)"),
    PRODUCT_PITCH("Product Demo / Pitch"),
    NEWS_PRESENTATION("News / Corporate Presentation"),
    PODCAST_MONOLOGUE("Podcast Monologue")
}

enum class RewriteStyle(val displayName: String) {
    CONVERSATIONAL("Conversational & Warm"),
    ENERGETIC("High Energy & Punchy"),
    PROFESSIONAL("Authoritative & Professional"),
    CONCISE("Concise & Fast Paced")
}

interface AiScriptAssistant {
    suspend fun generateScript(topic: String, format: ScriptFormat): Result<String>
    suspend fun rewrite(content: String, style: RewriteStyle): Result<String>
    suspend fun shorten(content: String): Result<String>
    suspend fun expand(content: String): Result<String>
}

/**
 * Built-in local offline creator engine that generates structured speaking scripts
 * immediately without requiring external AI services, and is architected for seamless
 * plug-in of Gemini API.
 */
class LocalAiScriptAssistant : AiScriptAssistant {

    override suspend fun generateScript(topic: String, format: ScriptFormat): Result<String> {
        val cleanTopic = topic.trim().ifEmpty { "Creating engaging content" }
        val script = when (format) {
            ScriptFormat.SHORT_REEL -> """
                Stop scrolling! If you want to know about $cleanTopic, listen up.

                Here is the single biggest mistake people make: They overcomplicate the basics.

                Instead, focus on this one actionable tip today: Start small, stay consistent, and test what works.

                Drop a comment below with your experience, and hit follow for more quick breakdowns!
            """.trimIndent()

            ScriptFormat.YOUTUBE_LONG -> """
                Welcome back, everyone! In today's video, we are tackling a question so many of you have asked about: $cleanTopic.

                [HOOK]
                If you have been struggling to get real results, this 10-minute guide will give you the exact framework you need.

                [POINT 1: THE FOUNDATION]
                First, let's establish why $cleanTopic matters right now. The landscape has changed, and old approaches no longer deliver.

                [POINT 2: STEP-BY-STEP ACTION]
                Next, take immediate action on your workflow. Break it down into daily 15-minute milestones.

                [POINT 3: COMMON PITFALLS TO AVOID]
                Watch out for analysis paralysis. Done is better than perfect.

                [OUTRO]
                Let me know in the comments: What is your #1 takeaway from today? Subscribe for our weekly breakdowns, and see you in the next one!
            """.trimIndent()

            ScriptFormat.PRODUCT_PITCH -> """
                Good morning, everyone.

                Today I am thrilled to introduce an innovative approach to $cleanTopic.

                We designed this specifically for busy creators and professionals who need clarity, speed, and reliability.

                Here is how it works: Intuitive setup, distraction-free execution, and seamless workflow.

                Thank you for your time, and let's open the floor for questions!
            """.trimIndent()

            ScriptFormat.NEWS_PRESENTATION -> """
                Good evening. Tonight's top focus centers on: $cleanTopic.

                Key developments continue to emerge as industry leaders assess the broader implications.

                Our analysis highlights three immediate impacts to watch over the coming weeks: operational shifts, audience engagement trends, and strategic adoption.

                We will continue monitoring this story as updates become available.
            """.trimIndent()

            ScriptFormat.PODCAST_MONOLOGUE -> """
                Hey friends, welcome to today's episode.

                I was thinking this morning about $cleanTopic, and a fascinating insight struck me.

                Most of us believe success is about raw effort. But in reality, it is about intentionality and clarity of speech.

                Grab your favorite beverage, get comfortable, and let's unpack this together.
            """.trimIndent()
        }
        return Result.success(script)
    }

    override suspend fun rewrite(content: String, style: RewriteStyle): Result<String> {
        val lines = content.lines()
        val rewritten = when (style) {
            RewriteStyle.CONVERSATIONAL -> {
                lines.joinToString("\n") { line ->
                    if (line.isNotBlank()) "You know, ${line.replaceFirstChar { it.lowercase() }}" else ""
                }
            }
            RewriteStyle.ENERGETIC -> {
                lines.joinToString("\n") { line ->
                    if (line.isNotBlank()) "${line.trimEnd('.', '!')}!" else ""
                }
            }
            RewriteStyle.CONCISE -> {
                lines.filter { it.isNotBlank() }.take(5).joinToString("\n\n")
            }
            RewriteStyle.PROFESSIONAL -> content
        }
        return Result.success(rewritten)
    }

    override suspend fun shorten(content: String): Result<String> {
        val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
        val shortened = if (paragraphs.size > 2) {
            paragraphs.take(paragraphs.size / 2 + 1).joinToString("\n\n")
        } else {
            content.lines().take(4).joinToString("\n")
        }
        return Result.success(shortened)
    }

    override suspend fun expand(content: String): Result<String> {
        val expanded = buildString {
            append(content)
            append("\n\n")
            append("Let me emphasize why this is so critical. When you articulate this with confidence, your audience immediately connects with your message.")
        }
        return Result.success(expanded)
    }
}
