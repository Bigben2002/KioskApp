package com.example.kiosk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 라이트 모드 색상표
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    error = md_theme_light_error,
    onError = md_theme_light_onError
)

// 다크 모드 색상표 (현재는 라이트 모드와 동일하게 설정, 필요시 변경)
private val DarkColorScheme = darkColorScheme(
    primary = Slate900,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    // ... 추가 다크모드 색상 정의
)

@Composable
fun KioskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 Android 12+ 에서 사용 가능
    dynamicColor: Boolean = false, // 키오스크 앱 특성상 고정된 브랜드 컬러 사용을 위해 false 권장
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 상단 상태바 색상 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 상태바 색상을 테마의 primary 색상으로 설정 (원하면 Color.Transparent 등으로 변경 가능)
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}