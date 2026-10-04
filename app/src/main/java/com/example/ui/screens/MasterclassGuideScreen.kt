package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MasterclassGuideScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E3A8A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "คัมภีร์จัดองค์ประกอบภาพ AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "หลักการถ่ายภาพระดับสากล: ควรถ่ายจุดไหน ซูมแค่ไหน ปรับอะไร",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: ควรถ่ายจุดไหน (Framing & Composition)
        GuideSectionCard(
            sectionTitle = "1. ควรถ่ายจุดไหน (Framing & Composition)",
            icon = Icons.Default.CropFree,
            iconColor = Color(0xFF10B981)
        ) {
            GuideItem(
                title = "กฎสามส่วน (Rule of Thirds)",
                body = "แบ่งหน้าจอเป็นตาราง 9 ช่อง นำจุดสนใจ เช่น ดวงตาของคน หรือเส้นขอบฟ้า ไปวางทับบน 'จุดตัด' หรือ 'เส้นตาราง' แทนที่จะวางกลางภาพทื่อๆ จะทำให้ภาพดูมีเรื่องราวและเคลื่อนไหว"
            )
            GuideItem(
                title = "การเว้นระยะสายตา (Looking Room & Headroom)",
                body = "หากตัวแบบหันหน้าหรือมองไปทางใด ให้เว้นพื้นที่ว่างในทิศทางนั้นเสมอ ส่วนด้านบนศีรษะ (Headroom) ควรเหลือพื้นที่ว่างประมาณ 10-15% ไม่ควรชิดขอบหรือเว้นโล่งเกินไป"
            )
            GuideItem(
                title = "เส้นนำสายตา (Leading Lines)",
                body = "ใช้แนวถนน สะพาน ราวระเบียง หรือแนวตึก ลากสายตาของผู้ชมให้พุ่งตรงไปยังจุดเด่นของภาพ"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: ซูมแค่ไหน (Zoom & Distance Secrets)
        GuideSectionCard(
            sectionTitle = "2. ซูมแค่ไหน (Zoom & Distance Secrets)",
            icon = Icons.Default.ZoomIn,
            iconColor = Color(0xFFF472B6)
        ) {
            GuideItem(
                title = "ซูม 2.0x - 3.0x: สำหรับภาพบุคคล (Portrait)",
                body = "เคล็ดลับสำคัญที่สุด! เลนส์ 1x บนมือถือคือเลนส์มุมกว้าง (Wide) หากยื่นถ่ายใกล้หน้า หน้าจะบวมและจมูกโต การ 'ถอยหลัง 2 ก้าว แล้วซูม 2x' จะทำให้สัดส่วนใบหน้าสมจริงและฉากหลังละลายสวยงาม"
            )
            GuideItem(
                title = "ซูม 2.0x - 2.5x: สำหรับอาหาร & กาแฟ",
                body = "ช่วยให้ตัวผู้ถ่ายไม่บังแสง และป้องกันเงาโทรศัพท์ตกกระทบลงบนจานอาหาร สามารถถ่ายมุม 45 องศาได้สวยงามระดับคาเฟ่รีวิว"
            )
            GuideItem(
                title = "ซูม 0.6x - 1.0x: สำหรับวิวทิวทัศน์ & สถาปัตยกรรม",
                body = "เก็บความกว้างใหญ่ของธรรมชาติ ท้องฟ้า และตึกสูงได้อย่างครบถ้วน"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: ปรับอะไรแค่ไหน (Camera Settings & Lighting)
        GuideSectionCard(
            sectionTitle = "3. ปรับอะไรแค่ไหน (Settings & Lighting)",
            icon = Icons.Default.Tune,
            iconColor = Color(0xFF60A5FA)
        ) {
            GuideItem(
                title = "การชดเชยแสง (Exposure Value - EV)",
                body = "• ถ่ายคนในคาเฟ่ / ถ่ายอาหาร: ชดเชยแสง +0.3 ถึง +0.7 EV เพื่อให้ภาพดูสว่าง สะอาด ผิวผ่อง\n• ถ่ายพระอาทิตย์ตก / กลางคืน: ชดเชยแสง -0.3 ถึง -0.7 EV เพื่อรักษาสีทองของท้องฟ้าและป้องกันไฟนีออนสว่างจ้าเกินไป (Highlight Clipping)"
            )
            GuideItem(
                title = "ทิศทางแสง (Lighting Direction)",
                body = "• แสงด้านข้าง 45°: แสงที่ดีที่สุดในการสร้างมิติแสงและเงา (Rembrandt Lighting)\n• แสงธรรมชาติริมหน้าต่าง: ดีกว่าแสงไฟหลอดฟลูออเรสเซนต์บนเพดานเสมอ\n• หลีกเลี่ยงการเปิดแฟลชตรงใส่หน้าหรืออาหารโดยตรง"
            )
            GuideItem(
                title = "ระนาบระดับน้ำ (Electronic Leveler 0.0°)",
                body = "การถ่ายวิวทะเลหรือตึก หากเส้นขอบฟ้าเอียงแม้เพียง 1-2 องศา จะทำให้ภาพดูไม่เป็นมืออาชีพ ใช้แถบระดับสีเขียวในหน้ากล้องเพื่อล็อกให้ตรง 0.0° เสมอ"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuideSectionCard(
    sectionTitle: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131B2E)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = sectionTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun GuideItem(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = "• $title",
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
