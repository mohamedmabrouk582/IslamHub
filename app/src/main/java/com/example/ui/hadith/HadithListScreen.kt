package com.example.ui.hadith

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.data.model.Hadith
import com.example.ui.components.HtmlText
import com.example.ui.components.PagingAppendContent
import com.example.ui.components.PagingRefreshContent
import com.example.ui.theme.BodyMdCustom
import com.example.ui.theme.HeadlineLgMobile
import com.example.ui.theme.QuranVerseSm
import com.example.util.HtmlUtils
import com.example.ui.adaptive.LocalWindowLayout
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.showHadithListBackButton
import com.example.viewmodel.QuranViewModel

@Composable
fun HadithListScreen(
    viewModel: QuranViewModel,
    collectionSlug: String,
    bookNumber: String,
) {
    val navigator = LocalAppNavigator.current
    val windowLayout = LocalWindowLayout.current
    val showBackButton = navigator.showHadithListBackButton(windowLayout.supportsListDetail)
    val hadithsFlow = remember(collectionSlug, bookNumber) {
        viewModel.hadithsPagingFlow(collectionSlug, bookNumber)
    }
    val hadiths = hadithsFlow.collectAsLazyPagingItems()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        ) {
            if (showBackButton) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Box(Modifier.size(48.dp))
            }
            Text(
                "Hadiths",
                style = HeadlineLgMobile,
                color = MaterialTheme.colorScheme.primary,
            )
            Box(Modifier.size(48.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        ) {
            if (showBackButton) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Book", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = "Book: $bookNumber",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        PagingRefreshContent(
            loadState = hadiths.loadState.refresh,
            onRetry = { hadiths.retry() },
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                count = hadiths.itemCount,
                key = hadiths.itemKey { it.id },
            ) { index ->
                hadiths[index]?.let { hadith ->
                    HadithItemCard(hadith = hadith, context = context)
                }
            }
            item {
                PagingAppendContent(loadState = hadiths.loadState.append)
            }
        }
    }
}

@Composable
private fun HadithItemCard(
    hadith: Hadith,
    context: android.content.Context,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    hadith.collection,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                )
                Row {
                    IconButton(onClick = {
                        val plainText = buildString {
                            append(HtmlUtils.fromHtml(hadith.textArabic))
                            append("\n\n")
                            append(HtmlUtils.fromHtml(hadith.textEnglish))
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("hadith", plainText))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HtmlText(
                html = hadith.textArabic,
                style = QuranVerseSm,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            HtmlText(
                html = hadith.textEnglish,
                style = BodyMdCustom,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (hadith.narrator.isNotBlank()) {
                HtmlText(
                    html = hadith.narrator,
                    style = BodyMdCustom.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
