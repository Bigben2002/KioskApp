package com.example.kiosk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =================================================================
// 1. Button 컴포넌트 (ui/button.tsx 대체)
// =================================================================
enum class ButtonVariant {
    DEFAULT, DESTRUCTIVE, OUTLINE, SECONDARY, GHOST, LINK
}

@Composable
fun KioskButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    when (variant) {
        // shadcn의 'default' 스타일
        ButtonVariant.DEFAULT -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A), // slate-900 (React의 primary 색상 유사)
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = contentPadding,
                content = content
            )
        }
        // shadcn의 'destructive' 스타일 (빨간색)
        ButtonVariant.DESTRUCTIVE -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444), // red-500
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = contentPadding,
                content = content
            )
        }
        // shadcn의 'outline' 스타일 (테두리만)
        ButtonVariant.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // slate-200
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF0F172A) // slate-900
                ),
                contentPadding = contentPadding,
                content = content
            )
        }
        // 나머지 variant도 필요하면 추가...
        else -> {
            // 기본형으로 대체
            Button(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        }
    }
}

// =================================================================
// 2. Card 컴포넌트 (ui/card.tsx 대체)
// =================================================================
@Composable
fun KioskCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFE2E8F0), // slate-200
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp), // rounded-xl
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        // shadow-sm 효과를 위해 elevation 추가
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

// CardHeader, CardTitle 등은 Compose에서는 그냥 Text 등으로 대체하는 게 더 자연스럽지만,
// 굳이 만든다면 아래와 같이 할 수 있습니다.
@Composable
fun KioskCardHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(24.dp), content = content)
}

@Composable
fun KioskCardContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 0.dp), content = content)
}

// =================================================================
// 3. Badge 컴포넌트 (ui/badge.tsx 대체)
// =================================================================
enum class BadgeVariant {
    DEFAULT, SECONDARY, DESTRUCTIVE, OUTLINE
}

@Composable
fun KioskBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.DEFAULT
) {
    val (backgroundColor, contentColor) = when (variant) {
        BadgeVariant.DEFAULT -> Color(0xFF0F172A) to Color.White // slate-900
        BadgeVariant.DESTRUCTIVE -> Color(0xFFEF4444) to Color.White // red-500
        BadgeVariant.SECONDARY -> Color(0xFFF1F5F9) to Color(0xFF0F172A) // slate-100
        BadgeVariant.OUTLINE -> Color.Transparent to Color(0xFF0F172A)
    }

    val border = if (variant == BadgeVariant.OUTLINE) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(percent = 50), // 완전히 둥글게 (rounded-full)
        border = border
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}