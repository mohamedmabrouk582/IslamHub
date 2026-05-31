package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.ui.theme.TitleMd

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = TitleMd,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
fun ToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = "",
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier,
        )
    }
}
