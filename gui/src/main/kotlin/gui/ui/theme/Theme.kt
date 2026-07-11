package gui.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette
val DeepBlue800 = Color(0xFF102A43)
val DeepBlue900 = Color(0xFF0C1B2A)
val CyanPrimary = Color(0xFF48BBF7)
val LightBlue50 = Color(0xFFF0F4F8)
val SoftBluePrimary = Color(0xFF2B6CB0)
val DangerRed = Color(0xFFE53E3E)

// Modern Typography
val ModernTypography =
    Typography(
        h3 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        h4 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        h5 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        h6 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        subtitle1 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
        body1 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        body2 = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        button = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    )

val ModernShapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
    )

@Composable
fun ClustEngineTheme(
    isDarkMode: Boolean,
    content: @Composable () -> Unit,
) {
    val colors =
        if (isDarkMode) {
            darkColors(
                primary = CyanPrimary,
                background = DeepBlue900,
                surface = DeepBlue800,
                onBackground = Color.White,
                onSurface = Color.White,
                error = DangerRed,
            )
        } else {
            lightColors(
                primary = SoftBluePrimary,
                background = LightBlue50,
                surface = Color.White,
                onBackground = DeepBlue900,
                onSurface = DeepBlue900,
                error = DangerRed,
            )
        }

    MaterialTheme(
        colors = colors,
        typography = ModernTypography,
        shapes = ModernShapes,
        content = content,
    )
}
