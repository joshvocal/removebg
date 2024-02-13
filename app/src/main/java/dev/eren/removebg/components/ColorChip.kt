package dev.eren.removebg.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T>HorizontalColorCarousel(colors: List<T>, onItemClick: (T) -> Unit) {

    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            "Colors",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
            ) {
                items(colors.size) { index ->
                    ColorChip(
                        color = colors[index],
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {
                            onItemClick(colors[index])
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun <T> ColorChip(
    modifier: Modifier = Modifier,
    color: T,
    height: Dp = 44.dp,
    width: Dp = 60.dp,
    roundedCorner: Dp = 12.dp,
    onClick: () -> Unit = {},
) {
    val modifiedModifier = when (color) {
        is Color -> modifier.background(
            color = color,
            shape = RoundedCornerShape(size = roundedCorner)
        )
        is Brush -> modifier.background(
            brush = color,
            shape = RoundedCornerShape(size = roundedCorner)
        )
        else -> modifier
    }

    Box(
        modifier = modifiedModifier
            .width(width)
            .height(height)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(size = roundedCorner)
            )
            .clickable { onClick() }
    )
}
