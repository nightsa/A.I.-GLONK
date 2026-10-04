package com.example.util

import com.example.R
import com.example.model.ActionableStep
import com.example.model.AiDirectorAdvice
import com.example.model.CameraMode
import com.example.model.CameraSettingsAdvice
import com.example.model.NormalizedRect
import com.example.model.SampleSceneItem

object SampleScenesProvider {
    val sampleScenes: List<SampleSceneItem> = listOf(
        SampleSceneItem(
            id = "portrait_cafe",
            titleTh = "ภาพบุคคลริมหน้าต่างคาเฟ่",
            category = CameraMode.PORTRAIT,
            drawableRes = R.drawable.sample_portrait,
            description = "การถ่ายภาพพอร์ตเทรตด้วยแสงธรรมชาติ แสงด้านข้างนุ่มนวล พร้อมฉากหลังเบลอ (Bokeh)",
            initialAdvice = AiDirectorAdvice(
                sceneType = "Portrait (ภาพบุคคล)",
                compositionScore = 88,
                focalPointSuggestion = "โฟกัสที่ดวงตาของตัวแบบ (Eye-level focus) โดยวางตาซ้ายบนจุดตัดเก้าช่องบนขวา",
                targetBoundingBox = NormalizedRect(
                    ymin = 0.18f,
                    xmin = 0.22f,
                    ymax = 0.78f,
                    xmax = 0.78f
                ),
                recommendedZoom = "2.0x (Telephoto Portrait)",
                zoomFactor = 2.0f,
                distanceAdvice = "ยืนห่างจากตัวแบบประมาณ 1.8 - 2.2 เมตร เพื่อลดการบิดเบี้ยวของเลนส์มุมกว้าง (Facial Compression ดีขึ้นอย่างเห็นได้ชัด)",
                angleAdvice = "ระดับสายตา (Eye-level) หรือมุมก้มลง 5° เล็กน้อยเพื่อให้ใบหน้าดูเรียวและคางได้รูป",
                tiltLevelAdvice = "ระนาบตรง 0° แนวตั้ง",
                lightingAdvice = "ให้ตัวแบบหันหน้าเฉียงหาหน้าต่าง 45 องศา (แสง Rembrandt Light) ทำให้เกิดมิติแสงเงาเป็นธรรมชาติ ปิดแฟลชหัวกล้อง",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.3 EV",
                    iso = "ISO 200",
                    shutterSpeed = "1/200s",
                    aperture = "f/2.0 (รูรับแสงกว้าง ละลายหลัง)",
                    flashMode = "ปิดแฟลช (ใช้แสงธรรมชาติริมหน้าต่าง)",
                    whiteBalance = "Daylight / Warm (5200K)"
                ),
                compositionRules = listOf(
                    "Rule of Thirds (กฎสามส่วน) วางสายตาที่เส้น 1/3 บน",
                    "Headroom Balance (เว้นระยะเหนือศีรษะพอดี ไม่ชิดขอบบนเกินไป)",
                    "Looking Room (เว้นพื้นที่ฝั่งที่ตัวแบบมองไป)"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "สลับเป็นระยะซูม 2x", "ช่วยให้ใบหน้าไม่บวมจากมุมกว้างและเบลอฉากหลังคาเฟ่", true),
                    ActionableStep(2, "จัดให้ดวงตาตรงจุดตัดบนขวา", "สร้างความน่าสนใจทางสายตาตามกฎสามส่วน", true),
                    ActionableStep(3, "ชดเชยแสง +0.3 EV", "ผิวหน้าจะดูผ่องใสขึ้นท่ามกลางแสงข้างหน้าต่าง", false)
                ),
                critiqueSummary = "ภาพนี้องค์ประกอบดีมาก แสงธรรมชาติจากหน้าต่างขับเน้นผิวพรรณอย่างเป็นธรรมชาติ แนะนำให้ใช้ระยะ 2x และเว้นระยะ Headroom เหนือศีรษะประมาณ 10% ของเฟรม เพื่อให้ภาพดูโปร่งโล่งสบายตา"
            )
        ),
        SampleSceneItem(
            id = "food_matcha",
            titleTh = "เครื่องดื่ม & ขนมเบเกอรี่คาเฟ่",
            category = CameraMode.FOOD,
            drawableRes = R.drawable.sample_food,
            description = "การถ่ายภาพอาหารและเครื่องดื่มแบบมีมิติ จับคู่ความน่ารับประทานกับแสงสะท้อนของเนื้อแก้วและเนื้อแป้ง",
            initialAdvice = AiDirectorAdvice(
                sceneType = "Food & Beverage (อาหาร/เครื่องดื่ม)",
                compositionScore = 92,
                focalPointSuggestion = "โฟกัสที่ชั้นฟองนมและเลเยอร์ชาเขียวมัทฉะ พร้อมให้ครัวซองต์อยู่โฟกัสระนาบรอง",
                targetBoundingBox = NormalizedRect(
                    ymin = 0.25f,
                    xmin = 0.20f,
                    ymax = 0.82f,
                    xmax = 0.80f
                ),
                recommendedZoom = "2.5x (Food Close-up)",
                zoomFactor = 2.5f,
                distanceAdvice = "นั่งหรือยืนห่างโต๊ะประมาณ 60 - 80 ซม. ซูม 2.5x เพื่อหลีกเลี่ยงเงาของตัวผู้ถ่ายตกกระทบอาหาร",
                angleAdvice = "มุม 45 องศา (Diner's Perspective) ซึ่งเป็นมุมมองเดียวกับสายตาเวลานั่งรับประทานจริง",
                tiltLevelAdvice = "เอียงกล้องลง 40° - 45° เล็งเฉียงเข้าหาหน้าจาน",
                lightingAdvice = "แสงด้านข้าง (Side Light) ช่วยดึง Texture ความกรอบของครัวซองต์และความใสของน้ำแข็งให้โดดเด่น",
                cameraSettings = CameraSettingsAdvice(
                    ev = "+0.7 EV",
                    iso = "ISO 100",
                    shutterSpeed = "1/160s",
                    aperture = "f/2.8",
                    flashMode = "ปิดแฟลชเด็ดขาด (แฟลชตรงทำให้อาหารดูแบนและมันเยิ้ม)",
                    whiteBalance = "Shade / Warm 5500K (ให้อาหารดูอุ่นน่ากิน)"
                ),
                compositionRules = listOf(
                    "Diagonal / Triangular Composition (จัดวางวัตถุ 2-3 ชิ้นเป็นรูปสามเหลี่ยม)",
                    "Depth of Field (หน้าชัดหลังเบลอพอประมาณให้เห็นบรรยากาศโต๊ะ)",
                    "Texture Accents (เน้นสะท้อนแสงบนหน้าขนม)"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "ปรับมุมกล้องเฉียง 45°", "มุมมองเสมือนจริงชวนรับประทานที่สุด", true),
                    ActionableStep(2, "ชดเชยแสง +0.7 EV", "ขนมและเครื่องดื่มจะดูสดใส ไม่หม่นหมอง", true),
                    ActionableStep(3, "หมุนแก้วให้เห็นเลเยอร์ชัดเจน", "เน้นจุดตัดของสีเขียวมัทฉะและนมสด", false)
                ),
                critiqueSummary = "การจัดวางแก้วคู่กับจานขนมสร้างมิติสามเหลี่ยมที่สมบูรณ์แบบมาก การชดเชยแสงให้สว่างขึ้นเล็กน้อยจะทำให้รูปดูแพงระดับนิตยสารอาหาร"
            )
        ),
        SampleSceneItem(
            id = "landscape_sunset",
            titleTh = "พระอาทิตย์ตกริมหน้าผาและทะเล",
            category = CameraMode.LANDSCAPE,
            drawableRes = R.drawable.sample_landscape,
            description = "การถ่ายภาพวิวทิวทัศน์ช่วง Golden Hour จัดเส้นขอบฟ้าและเส้นนำสายตาของคลื่น",
            initialAdvice = AiDirectorAdvice(
                sceneType = "Landscape (วิวทิวทัศน์)",
                compositionScore = 85,
                focalPointSuggestion = "โฟกัสดวงอาทิตย์หรือแนวสันหน้าผาหิน และจัดเส้นขอบฟ้าให้อยู่ที่เส้น 1/3 ล่าง (เน้นท้องฟ้า)",
                targetBoundingBox = NormalizedRect(
                    ymin = 0.20f,
                    xmin = 0.10f,
                    ymax = 0.85f,
                    xmax = 0.90f
                ),
                recommendedZoom = "1.0x (Wide Angle)",
                zoomFactor = 1.0f,
                distanceAdvice = "ใช้มุมกว้างเพื่อเก็บความยิ่งใหญ่ของแนวชายฝั่งและท้องฟ้ายามเย็น",
                angleAdvice = "ระดับสายตา แนวนอนขนานพื้นราบ โดยตรวจจับระนาบให้เส้นขอบฟ้าตรง 0.0° เสมอ",
                tiltLevelAdvice = "ระนาบระดับน้ำ 0.0° (ห้ามเอียงเด็ดขาด เพื่อไม่ให้น้ำดูไหลออกนอกจอ)",
                lightingAdvice = "แสงย้อน (Backlight ช่วง Golden Hour) เปิดโหมด HDR เพื่อเก็บรายละเอียดทั้งในเงาหน้าผาและแสงท้องฟ้า",
                cameraSettings = CameraSettingsAdvice(
                    ev = "-0.3 EV",
                    iso = "ISO 50 - 100",
                    shutterSpeed = "1/500s",
                    aperture = "f/8.0 (ชัดลึกทั้งภาพ)",
                    flashMode = "ปิดแฟลช",
                    whiteBalance = "Cloudy / Sunset (6000K ขับสีทองและส้ม)"
                ),
                compositionRules = listOf(
                    "Rule of Thirds (เส้นขอบฟ้าที่ 1/3 หรือ 2/3)",
                    "Leading Lines (แนวหน้าผาและคลื่นนำสายตาสู่ดวงอาทิตย์)",
                    "Foreground Interest (มีโขดหินหน้าเพื่อสร้างมิติมิติภาพ)"
                ),
                actionableSteps = listOf(
                    ActionableStep(1, "เช็คเส้นระนาบน้ำ (Horizon Level)", "เปิด Leveler เพื่อให้เส้นขอบฟ้าตรงเป๊ะ 0°", true),
                    ActionableStep(2, "ลดค่าชดเชยแสง -0.3 EV", "เพื่อเก็บสีส้มทองของท้องฟ้าไม่ให้ขาวโพลน (Highlight Clipping)", true),
                    ActionableStep(3, "จัดดวงอาทิตย์ไว้ที่จุดตัดซ้ายหรือขวา", "หลีกเลี่ยงการวางไว้ตรงกลางเพื่อเพิ่มความน่าสนใจ", false)
                ),
                critiqueSummary = "โทนสีท้องฟ้างดงามมาก แนะนำให้ใช้เส้นวัดระดับน้ำ (Electronic Leveler) ให้เป็น 0° พอดี และวางเส้นขอบฟ้าไว้ที่ 1/3 ล่าง เพื่อขับเน้นความอลังการของก้อนเมฆและแสงอาทิตย์อัสดง"
            )
        )
    )
}
