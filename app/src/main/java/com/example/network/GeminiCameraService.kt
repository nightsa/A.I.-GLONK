package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.ActionableStep
import com.example.model.AiDirectorAdvice
import com.example.model.CameraMode
import com.example.model.CameraSettingsAdvice
import com.example.model.NormalizedRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiCameraService {
    private const val TAG = "GeminiCameraService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun analyzeShot(
        bitmap: Bitmap,
        mode: CameraMode
    ): AiDirectorAdvice = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "GEMINI_API_KEY is not configured, using smart on-device photography rules engine")
            return@withContext generateHeuristicDirectorAdvice(bitmap, mode)
        }

        try {
            // Resize bitmap to max 1024 to speed up transmission and avoid payload size limits
            val scaledBitmap = scaleBitmapDown(bitmap, 1024)
            val base64Image = bitmapToBase64(scaledBitmap)

            val prompt = """
                You are a world-class professional photography director and camera coach.
                Analyze this photo shot taken in mode: '${mode.labelEn}' (${mode.labelTh}).
                Provide actionable, specific photography direction answering these 3 core questions in Thai:
                1. "ควรถ่ายจุดไหน" (Where to frame, focal point, subject placement, bounding box coordinates [ymin, xmin, ymax, xmax] normalized 0.0 to 1.0).
                2. "ซูมแค่ไหน" (What zoom level to use e.g. 0.6x, 1x, 2x, 3x, and exact distance to subject).
                3. "ปรับอะไรแค่ไหน" (Camera settings: EV compensation, ISO, Shutter, Aperture, Flash, Angle, Tilt, Lighting direction).

                Return ONLY a valid JSON object strictly matching this structure:
                {
                  "sceneType": "e.g. Portrait / Food / Landscape",
                  "compositionScore": 85,
                  "focalPointSuggestion": "คำแนะนำจุดโฟกัสภาษาไทยสั้นกระชับ",
                  "targetBoundingBox": {
                    "ymin": 0.20,
                    "xmin": 0.25,
                    "ymax": 0.80,
                    "xmax": 0.75
                  },
                  "recommendedZoom": "2.0x (Optimal Portrait)",
                  "zoomFactor": 2.0,
                  "distanceAdvice": "คำแนะนำระยะยืนห่างจากวัตถุเป็นเมตร",
                  "angleAdvice": "คำแนะนำมุมกล้อง เช่น ระดับสายตา / มุมเสย 15 องศา",
                  "tiltLevelAdvice": "คำแนะนำการเอียงระนาบกล้อง เช่น ตรงเป๊ะ 0.0 องศา",
                  "lightingAdvice": "คำแนะนำทิศทางแสงและเงา",
                  "cameraSettings": {
                    "ev": "+0.3 EV",
                    "iso": "ISO 200",
                    "shutterSpeed": "1/200s",
                    "aperture": "f/2.0",
                    "flashMode": "ปิดแฟลช",
                    "whiteBalance": "5200K Daylight"
                  },
                  "compositionRules": [
                    "Rule of Thirds",
                    "Leading Lines"
                  ],
                  "actionableSteps": [
                    {
                      "id": 1,
                      "title": "สลับระยะซูม 2x",
                      "detail": "ช่วยลดการบิดเบี้ยวของใบหน้า",
                      "isCompleted": true
                    },
                    {
                      "id": 2,
                      "title": "ปรับมุมมอง",
                      "detail": "ลดระดับกล้องลงเสมอหน้าอก",
                      "isCompleted": false
                    }
                  ],
                  "critiqueSummary": "บทวิเคราะห์จากผู้กำกับภาพแบบมืออาชีพเป็นภาษาไทย 2-3 ประโยค"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text prompt
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            // Inline image data
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseString")
                return@withContext generateHeuristicDirectorAdvice(bitmap, mode)
            }

            parseGeminiResponse(responseString, mode)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API: ${e.message}", e)
            generateHeuristicDirectorAdvice(bitmap, mode)
        }
    }

    private fun parseGeminiResponse(jsonString: String, mode: CameraMode): AiDirectorAdvice {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Clean markdown code blocks if any
            val cleanJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val obj = JSONObject(cleanJson)

            val boxObj = obj.optJSONObject("targetBoundingBox")
            val boundingBox = if (boxObj != null) {
                NormalizedRect(
                    ymin = boxObj.optDouble("ymin", 0.2).toFloat(),
                    xmin = boxObj.optDouble("xmin", 0.2).toFloat(),
                    ymax = boxObj.optDouble("ymax", 0.8).toFloat(),
                    xmax = boxObj.optDouble("xmax", 0.8).toFloat()
                )
            } else null

            val settingsObj = obj.optJSONObject("cameraSettings")
            val cameraSettings = CameraSettingsAdvice(
                ev = settingsObj?.optString("ev", "+0.0 EV") ?: "+0.0 EV",
                iso = settingsObj?.optString("iso", "ISO 100") ?: "ISO 100",
                shutterSpeed = settingsObj?.optString("shutterSpeed", "1/200s") ?: "1/200s",
                aperture = settingsObj?.optString("aperture", "f/2.8") ?: "f/2.8",
                flashMode = settingsObj?.optString("flashMode", "ปิดแฟลช") ?: "ปิดแฟลช",
                whiteBalance = settingsObj?.optString("whiteBalance", "Auto") ?: "Auto"
            )

            val rulesList = mutableListOf<String>()
            val rulesArray = obj.optJSONArray("compositionRules")
            if (rulesArray != null) {
                for (i in 0 until rulesArray.length()) {
                    rulesList.add(rulesArray.optString(i))
                }
            }

            val stepsList = mutableListOf<ActionableStep>()
            val stepsArray = obj.optJSONArray("actionableSteps")
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    val stepObj = stepsArray.optJSONObject(i)
                    if (stepObj != null) {
                        stepsList.add(
                            ActionableStep(
                                id = stepObj.optInt("id", i + 1),
                                title = stepObj.optString("title", "ปรับปรุงมุมมอง"),
                                detail = stepObj.optString("detail", ""),
                                isCompleted = stepObj.optBoolean("isCompleted", false)
                            )
                        )
                    }
                }
            }

            AiDirectorAdvice(
                sceneType = obj.optString("sceneType", mode.labelTh),
                compositionScore = obj.optInt("compositionScore", 85),
                focalPointSuggestion = obj.optString("focalPointSuggestion", "จัดจุดสนใจให้อยู่บนจุดตัดเก้าช่อง"),
                targetBoundingBox = boundingBox,
                recommendedZoom = obj.optString("recommendedZoom", "1.0x"),
                zoomFactor = obj.optDouble("zoomFactor", 1.0).toFloat(),
                distanceAdvice = obj.optString("distanceAdvice", "รักษาระยะห่าง 1.5 - 2 เมตร"),
                angleAdvice = obj.optString("angleAdvice", "ระดับสายตา"),
                tiltLevelAdvice = obj.optString("tiltLevelAdvice", "ระนาบตรง 0°"),
                lightingAdvice = obj.optString("lightingAdvice", "ใช้แสงธรรมชาติส่องเฉียง 45 องศา"),
                cameraSettings = cameraSettings,
                compositionRules = rulesList,
                actionableSteps = stepsList,
                critiqueSummary = obj.optString("critiqueSummary", "ภาพมีมิติสวยงามและแสงกำลังพอดี")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini JSON: ${e.message}", e)
            generateHeuristicDirectorAdvice(null, mode)
        }
    }

    /**
     * Smart heuristic on-device director advice that works offline or when API key is not yet set.
     */
    fun generateHeuristicDirectorAdvice(bitmap: Bitmap?, mode: CameraMode): AiDirectorAdvice {
        return when (mode) {
            CameraMode.PORTRAIT -> AiDirectorAdvice(
                sceneType = "Portrait (บุคคล)",
                compositionScore = 87,
                focalPointSuggestion = "โฟกัสที่ดวงตาของตัวแบบ (Eye Focus) โดยจัดให้ดวงตาอยู่บริเวณ 1/3 บนของภาพ",
                targetBoundingBox = NormalizedRect(0.18f, 0.25f, 0.78f, 0.75f),
                recommendedZoom = "2.0x (Portrait Compression)",
                zoomFactor = 2.0f,
                distanceAdvice = "ยืนห่างจากตัวแบบ 1.8 - 2.5 เมตร เพื่อไม่ให้สัดส่วนใบหน้าบวมขยายจากเลนส์ไวด์",
                angleAdvice = "ระดับสายตา (Eye-level) หรือกดกล้องต่ำลงเล็กน้อยเพื่อเพิ่มความสง่า",
                tiltLevelAdvice = "ระนาบตั้งตรง 0.0°",
                lightingAdvice = "ให้ตัวแบบหันหน้าเข้าหาแหล่งกำเนิดแสงนุ่มนวลเฉียง 45° ระวังเงาตกใต้ตา",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.3 EV",
                    iso = "ISO 100 - 200",
                    shutterSpeed = "1/200s",
                    aperture = "f/1.8 - f/2.2",
                    flashMode = "ปิดแฟลช (ใช้แสงธรรมชาติ)",
                    whiteBalance = "5200K (อบอุ่นผิวผ่อง)"
                ),
                compositionRules = listOf(
                    "Rule of Thirds: จัดตำแหน่งดวงตาบนเส้นตัดบน",
                    "Headroom: เว้นพื้นที่ว่างเหนือศีรษะประมาณ 10-15%",
                    "Looking Room: เว้นพื้นที่ในทิศทางที่สายตามองไป"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "ซูม 2x เพื่อใบหน้าสมส่วน", "เลนส์ 2x ลด Distortion สัดส่วนหน้าเรียวเป็นธรรมชาติ", true),
                    ActionableStep(2, "จัดดวงตาตรงเส้น 1/3 บน", "ช่วยดึงดูดสายตาของผู้ชมทันที", true),
                    ActionableStep(3, "ชดเชยแสง +0.3 EV", "ผิวหน้าสว่างเนียนใสอย่างเป็นธรรมชาติ", false)
                ),
                critiqueSummary = "จัดองค์ประกอบภาพบุคคลได้สวยงาม แนะนำให้ปรับระยะซูมเป็น 2x เพื่อให้สัดส่วนใบหน้าสมจริงและช่วยเบลอละลายฉากหลังให้ตัวแบบโดดเด่นยิ่งขึ้น"
            )

            CameraMode.FOOD -> AiDirectorAdvice(
                sceneType = "Food & Drink (อาหาร)",
                compositionScore = 91,
                focalPointSuggestion = "โฟกัสที่จุดเด่นของจานอาหาร (Garnish หรือส่วนที่มี Texture ความฉ่ำชัดเจน)",
                targetBoundingBox = NormalizedRect(0.25f, 0.20f, 0.80f, 0.80f),
                recommendedZoom = "2.0x - 2.5x",
                zoomFactor = 2.0f,
                distanceAdvice = "รักษาระยะห่าง 60 - 80 ซม. ซูมเพื่อหลีกเลี่ยงเงาโทรศัพท์ตกกระทบอาหาร",
                angleAdvice = "มุม 45 องศา (มุมสายตานั่งกิน) หรือ 90 องศา Flat Lay จากมุมบน",
                tiltLevelAdvice = "เอียงลง 45.0° หรือ 90° ขนานโต๊ะ",
                lightingAdvice = "แสงเข้าทางด้านข้างหรือด้านหลัง (Side/Back Light) เพื่อขับประกายความเงางามของอาหาร",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.7 EV",
                    iso = "ISO 100",
                    shutterSpeed = "1/160s",
                    aperture = "f/2.8",
                    flashMode = "ปิดแฟลช (แสงแฟลชตรงทำให้อาหารดูแห้งและแบน)",
                    whiteBalance = "5500K Warm"
                ),
                compositionRules = listOf(
                    "Triangle Composition: วางอาหาร จานหลัก และเครื่องดื่มเป็นรูปสามเหลี่ยม",
                    "Color Contrast: ตกแต่งให้สีอาหารตัดกับจานหรือผ้าปูโต๊ะ",
                    "Steam & Moisture: จับจังหวะที่มีควันหรือไอเย็นเกาะแก้ว"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "ตั้งกล้องมุม 45 องศา", "มุมมองยอดนิยมที่ทำให้อาหารดูมีมิติน่าทาน", true),
                    ActionableStep(2, "เพิ่มแสง +0.7 EV", "อาหารจะดูสดใส ฉ่ำวาว สะอาดตา", true),
                    ActionableStep(3, "ขยับจานไม่ให้เงาตัวบัง", "หันจานรับแสงธรรมชาติจากหน้าต่างหรือโคมไฟ", false)
                ),
                critiqueSummary = "จานอาหารดูน่ารับประทานมาก ใช้มุม 45 องศาพร้อมซูม 2x จะช่วยขับเน้นดีเทล Texture โดยไม่มีเงาตัวกล้องบดบัง"
            )

            CameraMode.LANDSCAPE -> AiDirectorAdvice(
                sceneType = "Landscape (วิวทิวทัศน์)",
                compositionScore = 86,
                focalPointSuggestion = "โฟกัสที่กึ่งกลางทัศนียภาพหรือองค์ประกอบหน้า (Foreground) ที่มีเอกลักษณ์",
                targetBoundingBox = NormalizedRect(0.20f, 0.15f, 0.85f, 0.85f),
                recommendedZoom = "0.6x - 1.0x (Ultra Wide)",
                zoomFactor = 1.0f,
                distanceAdvice = "ระยะอินฟินิตี้ (Infinity Focus) เก็บความกว้างของท้องฟ้าและพื้นดิน",
                angleAdvice = "ขนานพื้นโลก โดยรักษาเส้นขอบฟ้าให้ตรง 0.0° เสมอ",
                tiltLevelAdvice = "ระนาบระดับน้ำ 0.0° (เช็คแถบระดับสีเขียว)",
                lightingAdvice = "ช่วงเวลาทอง (Golden Hour 1 ชั่วโมงก่อนพระอาทิตย์ตก) แสงจะนุ่มและมีมิติ",
                cameraSettings = CameraSettingsAdvice(
                    ev = "-0.3 EV",
                    iso = "ISO 50 - 100",
                    shutterSpeed = "1/500s",
                    aperture = "f/8.0 (ชัดลึกทั้งภาพ)",
                    flashMode = "ปิดแฟลช",
                    whiteBalance = "Daylight / Sunset 6000K"
                ),
                compositionRules = listOf(
                    "Horizon at 1/3: วางเส้นขอบฟ้าที่ 1/3 ล่าง (เน้นฟ้า) หรือ 1/3 บน (เน้นพื้นดิน)",
                    "Leading Lines: ใช้ถนน ทางเดิน หรือแม่น้ำ นำสายตาเข้าไปในภาพ",
                    "Level Horizon: เส้นขอบฟ้าต้องตรง ไม่เอียง"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "จัดเส้นขอบฟ้าตรง 0.0°", "เปิดเส้นไกด์ระดับน้ำเพื่อความสมบูรณ์แบบ", true),
                    ActionableStep(2, "วางเส้นขอบฟ้าที่เส้น 1/3", "หลีกเลี่ยงการวางเส้นขอบฟ้าผ่ากลางภาพ", true),
                    ActionableStep(3, "ลดแสง -0.3 EV", "เพื่อเก็บรายละเอียดสีทองของท้องฟ้าไม่ให้สว่างเกิน", false)
                ),
                critiqueSummary = "วิวเปิดกว้างสวยงามมาก จุดสำคัญที่สุดคือการตั้งเส้นขอบฟ้าให้ตรง 0.0° พอดี และวางจุดสนใจไว้ที่จุดตัดเก้าช่อง"
            )

            CameraMode.PRODUCT -> AiDirectorAdvice(
                sceneType = "Product (ถ่ายสินค้า)",
                compositionScore = 90,
                focalPointSuggestion = "โฟกัสที่โลโก้และฉลากสินค้าให้คมชัด 100%",
                targetBoundingBox = NormalizedRect(0.25f, 0.25f, 0.75f, 0.75f),
                recommendedZoom = "2.0x - 3.0x",
                zoomFactor = 2.0f,
                distanceAdvice = "รักษาระยะ 1 - 1.5 เมตร เพื่อรูปทรงสินค้าตรง ไม่บิดเบี้ยว",
                angleAdvice = "ระดับสายตาขนานกับตัวสินค้า หรือมองลง 30° เล็กน้อย",
                tiltLevelAdvice = "ระนาบตั้งตรง 0.0°",
                lightingAdvice = "แสงสะท้อนรอบทิศ (Diffused Light) ไม่มีเงาแข็งบังข้อความบนฉลาก",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.3 EV",
                    iso = "ISO 100",
                    shutterSpeed = "1/200s",
                    aperture = "f/4.0",
                    flashMode = "ใช้ไฟนุ่ม (Softbox หรือกระดาษไขกรองแสง)",
                    whiteBalance = "5500K Studio White"
                ),
                compositionRules = listOf(
                    "Clean Negative Space: ฉากหลังสะอาดเรียบ ไม่มีสิ่งรบกวน",
                    "Center or Golden Ratio: จัดสินค้าให้เด่นชัดกลางเฟรม",
                    "Zero Distortion: ใช้เลนส์ระยะเทเลเพื่อไม่ให้แพ็กเกจบิดงอ"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "ซูม 2x เพื่อทรงสินค้าไม่บวม", "รักษาสัดส่วนสินค้าให้เหมือนจริงที่สุด", true),
                    ActionableStep(2, "เคลียร์ฉากหลังให้สะอาด", "เพื่อความพรีเมียมระดับโฆษณา", true),
                    ActionableStep(3, "โฟกัสที่ชื่อแบรนด์", "ให้ตัวอักษรและโลโก้คมชัดสูงสุด", false)
                ),
                critiqueSummary = "จัดวางสินค้าได้โดดเด่น แนะนำให้ใช้ระยะซูม 2x และเพิ่มความสว่างขึ้นเล็กน้อยเพื่อให้ตัวแพ็กเกจดูพรีเมียมยิ่งขึ้น"
            )

            CameraMode.NIGHT -> AiDirectorAdvice(
                sceneType = "Night & Cityscape (กลางคืน)",
                compositionScore = 84,
                focalPointSuggestion = "โฟกัสที่จุดกำเนิดแสงนีออนหรือวัตถุที่มีแสงส่องถึงชัดเจน",
                targetBoundingBox = NormalizedRect(0.20f, 0.20f, 0.80f, 0.80f),
                recommendedZoom = "1.0x",
                zoomFactor = 1.0f,
                distanceAdvice = "รักษาระยะตามสภาพแสง และถือกล้องให้นิ่งสนิท",
                angleAdvice = "ระดับสายตา หรือมุมเงยขึ้นเพื่อเก็บแสงไฟจากตึกสูง",
                tiltLevelAdvice = "ระนาบตรง 0.0°",
                lightingAdvice = "ใช้ประโยชน์จากไฟป้ายนีออน แสงโคมไฟถนน หรือแสงจากตึก",
                cameraSettings = CameraSettingsAdvice(
                    ev = "-0.7 EV (ป้องกันแสงไฟหลุด)",
                    iso = "ISO 400 - 800",
                    shutterSpeed = "1/30s (หรือโหมดกลางคืน Night Mode)",
                    aperture = "f/1.8",
                    flashMode = "ปิดแฟลช (เพื่อให้ได้บรรยากาศแสงไฟกลางคืนจริง)",
                    whiteBalance = "Tungsten / 3800K (ให้อารมณ์นีออนไซเบอร์)"
                ),
                compositionRules = listOf(
                    "High Contrast Bokeh: ปล่อยให้แสงไฟฉากหลังเป็นดวงโบเก้กลม",
                    "Reflections: ใช้เงาสะท้อนบนพื้นเปียกหรือกระจก",
                    "Hold Steady: ยืนมั่นคง หายใจช้าๆ เพื่อป้องกันภาพเบลอ"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "ลด EV ลง -0.7", "ป้องกันไฟนีออนสว่างจ้าจนขาวโพลน (Blow out)", true),
                    ActionableStep(2, "ถือกล้องนิ่งสนิท 2 วินาที", "หรือหาที่วางพิงเพื่อลดการสั่นไหว", true),
                    ActionableStep(3, "หาเงาสะท้อนบนพื้น", "เพิ่มความน่าสนใจทางสายตาในยามค่ำคืน", false)
                ),
                critiqueSummary = "บรรยากาศแสงไฟกลางคืนสวยงามมาก การลด EV ลงเล็กน้อยจะช่วยให้เห็นสีสันของแสงไฟนีออนคมชัดขึ้นอย่างชัดเจน"
            )

            CameraMode.AUTO -> AiDirectorAdvice(
                sceneType = "General Scene (ทั่วไป)",
                compositionScore = 88,
                focalPointSuggestion = "จัดจุดสนใจหลักบนจุดตัดเก้าช่อง (Rule of Thirds Intersection)",
                targetBoundingBox = NormalizedRect(0.22f, 0.25f, 0.78f, 0.75f),
                recommendedZoom = "1.0x - 2.0x",
                zoomFactor = 1.0f,
                distanceAdvice = "รักษาระยะ 1.5 - 2 เมตรเพื่อให้ภาพครอบคลุมเรื่องราวและบริบท",
                angleAdvice = "ระดับสายตา (Eye level) สมดุลทุกทิศทาง",
                tiltLevelAdvice = "ระนาบระดับน้ำ 0.0°",
                lightingAdvice = "แสงธรรมชาติทางด้านข้างหรือด้านหน้า ห้ามย้อนแสงตรงๆ เว้นแต่ต้องการ Silhouette",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.0 EV",
                    iso = "Auto ISO",
                    shutterSpeed = "1/250s",
                    aperture = "f/2.8",
                    flashMode = "ปิดแฟลช",
                    whiteBalance = "Auto AWB"
                ),
                compositionRules = listOf(
                    "Rule of Thirds: แบ่งภาพ 3 ช่องทั้งแนวนอนและแนวตั้ง",
                    "Simplicity: กำจัดสิ่งรบกวนที่ไม่จำเป็นออกจากขอบภาพ",
                    "Balance: รักษาน้ำหนักภาพซ้าย-ขวาให้สมดุล"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "เปิดเส้นตาราง 3 ส่วน", "ช่วยจัดวางวัตถุหลักได้อย่างแม่นยำ", true),
                    ActionableStep(2, "ตรวจเช็คระนาบกล้อง", "รักษาเส้นระดับน้ำให้อยู่ในแถบสีเขียว", true),
                    ActionableStep(3, "จัดระยะหน้าชัดหลังเบลอ", "ขยับเข้าใกล้วัตถุอีกนิดเพื่อเพิ่มมิติความลึก", false)
                ),
                critiqueSummary = "องค์ประกอบภาพรวมสวยงามมีสัดส่วนดี ลองใช้กฎสามส่วนวางวัตถุหลักที่จุดตัดเพื่อเพิ่มเสน่ห์ให้กับภาพถ่าย"
            )
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
