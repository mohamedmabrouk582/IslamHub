package com.example.ui.hadith

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HadithCollection
import com.example.ui.components.PagingRefreshContent
import com.example.ui.theme.BodyMdCustom
import com.example.ui.theme.HeadlineLgMobile
import com.example.ui.theme.QuranVerseSm
import com.example.ui.theme.SacredTeal
import com.example.ui.theme.TitleMd
import com.example.ui.navigation.HomeKey
import com.example.ui.navigation.HadithBooksKey
import com.example.ui.navigation.LocalAppNavigator
import com.example.viewmodel.QuranViewModel

@Composable
fun HadithScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val collections = viewModel.collectionsPaging.collectAsLazyPagingItems()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                IconButton(onClick = { navigator.navigateToTab(HomeKey) }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "الأحاديث النبوية",
                    style = HeadlineLgMobile,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("حديث اليوم", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "«الدِّينُ النَّصِيحَةُ»",
                        style = QuranVerseSm,
                        color = Color.White,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "\"The religion is sincerity (Nasihah).\"",
                        style = BodyMdCustom,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        "Narrated by Muslim",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        IconButton(onClick = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(onClick = { Toast.makeText(context, "Hadith Saved!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    "المجموعات (Collections)",
                    style = TitleMd,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                PagingRefreshContent(
                    loadState = collections.loadState.refresh,
                    onRetry = { collections.retry() },
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(
                        count = collections.itemCount,
                        key = collections.itemKey { it.slug },
                    ) { index ->
                        collections[index]?.let { collection ->
                            HadithCollectionCard(
                                collection = collection,
                                onClick = { navigator.push(HadithBooksKey(collection.slug)) },
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6ZUmhZRb2T61qiVuAk4bYWyw3K8AmkGoG4h-ST_bjqKBhK7rduu0h_CZH3sZwBxVzkR9V-V9XNyyFAdXdTMYpAIHMdlOQQ59uRBgtUNICWgk95Qe5PwPmQFJQ1hEPJ2QF4zLEyXAA5FnrYyc87XEOblamtmCbAWDWC4T5iA2dLILDmsLQ165R72N015p0TzIoJNzAfzwRBvi0yCsTOaXfAwzdQ49GNpeySRhrLqcffg9ksvKAPWXfyhoLDYGoZ7E2Bp6mfXgiUBev",
                        contentDescription = "Quran manuscript",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, SacredTeal.copy(alpha = 0.85f)),
                                ),
                            ),
                    )
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                    ) {
                        Text(
                            "رحلة البحث في سنة خير الأنام",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithCollectionCard(
    collection: HadithCollection,
    onClick: () -> Unit,
) {
    val icon = when {
        collection.slug.contains("bukhari", ignoreCase = true) -> Icons.Default.MenuBook
        collection.slug.contains("muslim", ignoreCase = true) -> Icons.Default.MenuBook
        collection.slug.contains("nawawi", ignoreCase = true) -> Icons.Default.AutoStories
        else -> Icons.Default.Spa
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .size(width = 150.dp, height = 150.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                collection.nameArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                collection.nameEnglish,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
