package com.project.reach.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.project.reach.R

val alSans = FontFamily(
    Font(R.font.al_sans_bold, FontWeight.Bold),
    Font(R.font.al_sans_medium, FontWeight.Medium),
    Font(R.font.al_sans_semibold, FontWeight.SemiBold),
    Font(R.font.al_sans_regular, FontWeight.Normal),
)
val cabin = FontFamily(
    Font(R.font.cabin_bold, FontWeight.Bold),
    Font(R.font.cabin_medium, FontWeight.Medium),
    Font(R.font.cabin_regular, FontWeight.Normal),
    Font(R.font.cabin_semibold, FontWeight.SemiBold),
)
val inter = FontFamily(
    Font(R.font.inter_18pt_light, FontWeight.Light),
    Font(R.font.inter_18pt_regular, FontWeight.Normal),
    Font(R.font.inter_18pt_semibold, FontWeight.SemiBold),
)

val MultiFontTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

