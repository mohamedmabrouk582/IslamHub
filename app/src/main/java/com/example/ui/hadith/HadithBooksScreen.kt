package com.example.ui.hadith

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.data.model.HadithBook
import com.example.ui.components.PagingAppendContent
import com.example.ui.components.PagingRefreshContent
import com.example.ui.adaptive.LocalWindowLayout
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.showHadithBooksBackButton
import com.example.ui.theme.HeadlineLgMobile
import com.example.ui.theme.TitleMd
import com.example.viewmodel.QuranViewModel

@Composable
fun HadithBooksScreen(
    viewModel: QuranViewModel,
    collectionSlug: String,
) {
    val navigator = LocalAppNavigator.current
    val windowLayout = LocalWindowLayout.current
    val showBackButton = navigator.showHadithBooksBackButton(windowLayout.supportsListDetail)
    val booksFlow = remember(collectionSlug) { viewModel.booksPagingFlow(collectionSlug) }
    val books = booksFlow.collectAsLazyPagingItems()

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
                "Books",
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
                    Icon(Icons.Default.Clear, contentDescription = "Clear Collection", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = collectionSlug.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        PagingRefreshContent(
            loadState = books.loadState.refresh,
            onRetry = { books.retry() },
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(
                count = books.itemCount,
                key = books.itemKey { "${it.collectionSlug}-${it.bookNumber}" },
            ) { index ->
                val book = books[index] ?: return@items
                HadithBookCard(
                    book = book,
                    onClick = {
                        navigator.openHadithList(collectionSlug, book.bookNumber)
                    },
                )
            }
            item {
                PagingAppendContent(loadState = books.loadState.append)
            }
        }
    }
}

@Composable
private fun HadithBookCard(book: HadithBook, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp),
                )
                Column {
                    Text(book.nameEnglish, style = TitleMd, fontWeight = FontWeight.Bold)
                    Text(
                        "${book.hadithCount} hadiths",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
