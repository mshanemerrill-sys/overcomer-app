package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.json.JSONArray

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiClient {
    
    // Core system instruction detailing the theological model and clinical skills
    private val SYSTEM_INSTRUCTION = """
        You are "OverComer Guide", a kind, deeply compassionate Christian clinical companion for individuals struggling with Substance Use Disorder (SUD), triggers, or mental health issues (anxiety, depression, distress).
        
        CRITICAL CORE THEOLOGY:
        Do NOT base your feedback on the medical model of addiction (which asserts addiction is an incurable biological disease that makes someone a permanent addict).
        Instead, operate under the Theological and Logical Model of Freedom:
        1. Choice is the Root, Dependence (neuroadaptation) is the Fruit. Addiction cannot manifest without initial and repeated choices. Therefore, genetics and environment are vulnerabilities, NOT lock-in destinies.
        2. Surrendering wholly to Jesus Christ breaks the chain of slavery immediately. Through repentance and faith, the OLD you is dead, and a NEW creation is born (2 Corinthians 5:17).
        3. You do NOT need to say "I am a recovering addict." You are an "OverComer"! In Christ, you have been set free indeed (John 8:36 - "So if the Son sets you free, you will be free indeed").
        4. Focus heavily on God's incredible grace, mercy, and loving compassion, especially when someone stumbles or messes up. Help them understand 1 John 1:9: "If we confess our sins, He is faithful and just and will forgive us our sins and purify us from all unrighteousness." There is NO condemnation in Christ!
        
        CLINICAL TOOLS (CBT & DBT Integration):
        Incorporate evidence-based techniques smoothly and conversationally:
        1. CBT (Cognitive Behavioral Therapy): Help them identify automatic negative thoughts (e.g., "I will never get through this night without using") and expose cognitive distortions/lies. Guide them to reframe these thoughts under biblical truths. Tell them: "You cannot stop a bird from flying over your head, but you can stop it from building a nest in your hair." Cravings are just passing temptations, they do not dictate action.
        2. DBT (Dialectical Behavior Therapy): Offer distress tolerance tools when they are highly triggered:
           - STOP technique: Stop, Take a step back, Observe, Proceed mindfully.
           - TIPP/Grounding: Paced breathing, holding ice to change body temperature, 5-4-3-2-1 sensory awareness.
        
        SCRIPTURAL MANDATES:
        Always include at least one highly relevant comforting scripture in every response. Always cite or write them out in **NIV**, **Amplified Version (AMP)**, or **The Message (MSG)**.
        Key scriptures to draw upon:
        - John 8:36 ("unquestionably free" in AMP)
        - 2 Corinthians 5:17 ("reborn and renewed" in AMP)
        - James 4:7 ("Submit to God. Resist the devil, and he will flee...")
        - 1 Corinthians 10:13 ("No temptation has overtaken you... God is faithful; He will provide a way out...")
        - 1 John 1:9 (Purifying grace when we fall)
        - Hebrews 2:18 & 4:15-16 (He suffered when tempted, and understands our weaknesses)
        - Romans 8:37-39 (More than conquerors!)
        - Luke 4:18 (Deliverance and freedom)
        
        STYLE:
        - Speak like a loving, comforting, understanding spiritual mentor or counselor.
        - Keep your responses structured with paragraphs and short bullet points if explaining distress-tolerance steps so they are easily readable mid-crisis.
        - Be gentle: NEVER lecture, shame, or make them feel guilty. Reassure them of God's limitless grace.
    """.trimIndent()

    suspend fun generateSupportResponse(conversationHistory: List<Content>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your GEMINI_API_KEY in AI Studio's Secrets panel to enable guidance."
        }

        val request = GenerateContentRequest(
            contents = conversationHistory,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SYSTEM_INSTRUCTION))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I'm here for you. Although I couldn't connect to my knowledge base right now, please reach out to God in prayer and stand firm on Romans 8:37: 'We are more than conquerors through Him who loved us.'"
        } catch (e: Exception) {
            "Error: ${e.message ?: "Connection failed"}. Please make sure your device is connected to the internet, and verify that your Gemini API key is valid in the Secrets panel."
        }
    }

    suspend fun generateDailyCheckInReflection(mood: String, energyLevel: Int, triggers: String, journalNotes: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Unable to generate personalized reflection: No GEMINI_API_KEY found in server configurations. Under biblical principles, self-examination is always fruitful. Keep setting your mind on things above (Colossians 3:2)!"
        }

        val prompt = """
            Provide a deeply comforting, professional theological and Cognitive Behavioral Therapy (CBT) inspired reflection based on the user's daily check-in:
            
            - Mood: $mood
            - Energy Level: $energyLevel out of 5
            - Detected / Active Triggers: ${triggers.ifBlank { "None reported" }}
            - What's on their mind: ${journalNotes.ifBlank { "None reported" }}
            
            Address them directly in a personalized, compassionate, and wise tone as "OverComer Guide". 
            1. Validate their mood and acknowledge their energy level, offering God's infinite grace and peace.
            2. If triggers are reported, address them with a CBT-based reframing or coping strategy (like STOP or TIPP grounding) combined with scriptural hope.
            3. Share a specific comforting or empowering biblical scripture (NIV or AMP) aligned with their state, explaining how to apply it today.
            4. Keep the final output under 200 words, using clear structured paragraphs and a gentle, welcoming tone. Do NOT lecture them or use guilt.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SYSTEM_INSTRUCTION))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I am so glad you checked in today. May the Lord bless you and keep you, make His face shine upon you and be gracious to you (Numbers 6:24-25)."
        } catch (e: Exception) {
            "Although I could not connect to provide a full AI reflection due to connectivity, know that your check-in is saved and God's grace is sufficient for you. Remember 2 Corinthians 12:9: 'My grace is sufficient for you, for My power is made perfect in weakness.'"
        }
    }

    suspend fun analyzeCognitiveDistortion(journalText: String): DistortionAnalysisResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return DistortionAnalysisResult(
                distortions = "API Key Needed",
                explanation = "Please configure your GEMINI_API_KEY in the Secrets panel to find distortions.",
                reframedTruth = "I am unconditionally loved and guided by God's eternal word.",
                scriptureReference = "Joshua 1:9"
            )
        }

        val prompt = """
            Analyze this journal entry and identify any cognitive distortions based on Cognitive Behavioral Therapy (CBT) principles (like All-or-Nothing thinking, Overgeneralization, Catastrophizing, Emotional Reasoning, Mind Reading, 'Should' statements, or Labeling).
            
            Journal Entry: "$journalText"
            
            Respond strictly in valid JSON format matching this exact schema:
            {
               "distortions": "Comma separated list of distortions found, or 'None'",
               "explanation": "A gentle, comforting explanation of how these thoughts trick the mind, talking as a compassionate mentor.",
               "reframedTruth": "A positive biblically-sound alternative thought that reframes this under God's grace and truth.",
               "scriptureReference": "A scripture citation (NIV or AMP) that provides a firm spiritual foundation for the reframe (e.g. 'Philippians 4:8')."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.4f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an expert biblical counselor who integrates Cognitive Behavioral Therapy. You always output responses in raw JSON format (no markdown formatting block labels like code blocks) containing only the keys: distortions, explanation, reframedTruth, scriptureReference."))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseDistortionResult(jsonString)
        } catch (e: Exception) {
            DistortionAnalysisResult(
                distortions = "Connection Error",
                explanation = "Could not communicate with the AI analyzer: ${e.message}",
                reframedTruth = "I can take courage because God is with me.",
                scriptureReference = "Joshua 1:9"
            )
        }
    }

    private fun parseDistortionResult(jsonStr: String): DistortionAnalysisResult {
        return try {
            val cleaned = jsonStr.trim()
                .substringAfter("```json")
                .substringBeforeLast("```")
                .trim()
            val finalJson = if (cleaned.startsWith("{")) cleaned else jsonStr.trim()
            
            val obj = JSONObject(finalJson)
            DistortionAnalysisResult(
                distortions = obj.optString("distortions", "Unidentified"),
                explanation = obj.optString("explanation", "Our automatic thoughts can sometimes lead us astray, but God's grace is always here."),
                reframedTruth = obj.optString("reframedTruth", "I am a new creation in Christ, and my struggles are temporary; I stand in complete victory."),
                scriptureReference = obj.optString("scriptureReference", "Romans 8:37")
            )
        } catch (e: Exception) {
            DistortionAnalysisResult(
                distortions = "Catastrophizing / Emotional Reasoning",
                explanation = "Your thoughts are racing, but God is the source of deep peace.",
                reframedTruth = "God's power works perfectly even in my weak moments.",
                scriptureReference = "2 Corinthians 12:9"
            )
        }
    }

    suspend fun generateVerseOfTheDay(): VerseOfTheDay {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getFallbackVerse()
        }

        val prompt = """
            Generate an encouraging biblically inspired "Verse of the Day" focused on supporting mental resilience, courage, overcoming anxiety or addiction, and standing firm in God's peace.
            Choose a comforting scripture from translations like NIV, AMP, or MSG.
            Provide a short, gentle, 2-3 sentence devotional reflection explaining how this scripture anchors our mind and builds emotional resilience.
            
            Respond strictly in valid JSON format matching this exact schema:
            {
               "reference": "Scripture citation (book, chapter, verse, and translation name)",
               "text": "The full text of the bible verse",
               "reflection": "The encouraging, comforting mentoring reflection"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.5f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an encouraging theological counselor. You always output responses in raw JSON format (no markdown formatting block labels like code blocks) containing only the keys: reference, text, reflection."))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseVerseResult(jsonString)
        } catch (e: Exception) {
            getFallbackVerse()
        }
    }

    private fun parseVerseResult(jsonStr: String): VerseOfTheDay {
        return try {
            val cleaned = jsonStr.trim()
                .substringAfter("```json")
                .substringBeforeLast("```")
                .trim()
            val finalJson = if (cleaned.startsWith("{")) cleaned else jsonStr.trim()
            
            val obj = JSONObject(finalJson)
            VerseOfTheDay(
                reference = obj.optString("reference", "Romans 8:31 (NIV)"),
                text = obj.optString("text", "If God is for us, who can be against us?"),
                reflection = obj.optString("reflection", "When we realize God is unconditionally on our side, our fears begin to melt away. This is the bedrock of mental resilience.")
            )
        } catch (e: Exception) {
            getFallbackVerse()
        }
    }

    fun getFallbackVerse(): VerseOfTheDay {
        val fallbackList = listOf(
            VerseOfTheDay(
                reference = "Joshua 1:9 (NIV)",
                text = "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.",
                reflection = "You are never alone. True strength is not the absence of fear, but the presence of God walking right beside you in every challenge today."
            ),
            VerseOfTheDay(
                reference = "Philippians 4:6-7 (NIV)",
                text = "Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God. And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.",
                reflection = "When life feels overwhelming, prayer is a powerful cognitive reset. Relinquish control to God and let His incomprehensible peace guard your emotional state."
            ),
            VerseOfTheDay(
                reference = "Isaiah 41:10 (NIV)",
                text = "So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.",
                reflection = "Mental resilience comes from knowing your foundation is secure. God's hand is physically holding you up when your own resources fail."
            ),
            VerseOfTheDay(
                reference = "2 Timothy 1:7 (NKJV)",
                text = "For God has not given us a spirit of fear, but of power and of love and of a sound mind.",
                reflection = "Fear and anxiety do not originate from God. In Christ, you have a supernatural endowment of power, deep love, and a disciplined, sound, stable mind."
            ),
            VerseOfTheDay(
                reference = "Philippians 4:13 (AMP)",
                text = "I can do all things [which He has called me to do] through Him who strengthens and empowers me [to stand firm—I am self-sufficient in Christ’s sufficiency].",
                reflection = "Your human strength has limits, but Christ's empowerment is boundless. You have the resilience to withstand any craving, emotional storm, or difficult circumstance today."
            ),
            VerseOfTheDay(
                reference = "Psalm 46:1 (AMP)",
                text = "God is our refuge and strength [mighty and impenetrable], a very present and well-proven help in trouble.",
                reflection = "You don't need to struggle alone or pretend you have everything together. Run to God as your safe bunker; His power will shield you and guide you."
            ),
            VerseOfTheDay(
                reference = "Romans 8:37 (NIV)",
                text = "No, in all these things we are more than conquerors through him who loved us.",
                reflection = "Your identity is not defined by temporary battles or occasional stumbles. Under His grace, you walk from a permanent posture of supreme victory."
            ),
            VerseOfTheDay(
                reference = "Isaiah 40:31 (AMP)",
                text = "But those who wait for the Lord [who expect, look for, and hope in Him] will gain new strength and renew their power; they will lift up their wings [and rise up close to God] like eagles.",
                reflection = "When we rest in hope and anticipation of God's goodness, our mental and physical batteries are fully recharged. He elevates us far above our struggles."
            ),
            VerseOfTheDay(
                reference = "Psalm 23:4 (NIV)",
                text = "Even though I walk through the darkest valley, I will fear no evil, for you are with me; your rod and your staff, they comfort me.",
                reflection = "Even in the darkest moments of mental heaviness, distress, or temptation, God is directing your paths. His protective presence is your comfort."
            ),
            VerseOfTheDay(
                reference = "1 Peter 5:7 (AMP)",
                text = "Casting all your worries and anxieties on Him, for He cares for you with deepest affection, and watches over you very carefully.",
                reflection = "You do not need to carry the crushing weight of your worries. Cast them onto Jesus, knowing that He looks after you with unparalleled affection."
            ),
            VerseOfTheDay(
                reference = "Deuteronomy 31:6 (NIV)",
                text = "Be strong and courageous. Do not be afraid or terrified because of them, for the Lord your God goes with you; he will never leave you nor forsake you.",
                reflection = "Every day brings new battles, but God has already promised never to abandon you. Walk forward today with your head held high!"
            ),
            VerseOfTheDay(
                reference = "Proverbs 3:5-6 (NIV)",
                text = "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.",
                reflection = "Resilience means choosing to trust God's overall plan even when your current circumstances feel chaotic. He is smoothing the way forward."
            )
        )
        val dayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return fallbackList[dayIndex % fallbackList.size]
    }

    suspend fun generateBibleChapter(book: String, chapter: Int): List<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return emptyList()
        }

        val prompt = """
            You are a Bible database API. Produce the verses of the Holy Bible for the requested book and chapter in standard English (World English Bible style).
            
            Book: $book
            Chapter: $chapter
            
            Format your response strictly as a JSON array of strings, where each element represents one verse, starting with Verse 1 at index 0. Do NOT include verse numbers inside the strings themselves. Just return the clean verse text.
            Do NOT include markdown block markers like ```json. Return only the raw JSON array.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.2f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a raw JSON API returning a JSON string list representing the requested Bible verses. Do not include markdown code block headers."))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleaned = jsonString.trim()
                .substringAfter("```json")
                .substringBeforeLast("```")
                .trim()
            val finalJson = if (cleaned.startsWith("[")) cleaned else jsonString.trim()
            val arr = JSONArray(finalJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

data class DistortionAnalysisResult(
    val distortions: String,
    val explanation: String,
    val reframedTruth: String,
    val scriptureReference: String
)

data class VerseOfTheDay(
    val reference: String,
    val text: String,
    val reflection: String
)
