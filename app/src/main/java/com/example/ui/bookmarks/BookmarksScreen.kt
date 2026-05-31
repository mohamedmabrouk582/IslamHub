package com.example.ui.bookmarks

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.navigation.HomeKey
import com.example.ui.navigation.LocalAppNavigator
import com.example.ui.navigation.ReaderKey
import com.example.viewmodel.QuranViewModel
import com.example.data.model.Surah
import com.example.ui.common.UiState
import com.example.ui.components.RubElHizbIcon

@Composable
fun BookmarksScreen(viewModel: QuranViewModel) {
    val navigator = LocalAppNavigator.current
    val bookmarks by viewModel.bookmarks.collectAsState()
    val context = LocalContext.current
    val surahsState by viewModel.surahs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSurahs()
    }

    fun resolveSurah(number: Int): Surah? {
        return when (val state = surahsState) {
            is UiState.Success -> state.data.find { it.number == number }
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Simple Header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            IconButton(onClick = { navigator.navigateToTab(HomeKey) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                "My Bookmarks",
                style = HeadlineLgMobile,
                color = MaterialTheme.colorScheme.primary
            )
            Box(Modifier.width(48.dp)) // empty spacer placeholder
        }

        if (bookmarks.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your bookmark list is empty.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 64.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookmarks) { bookmark ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val surahObj = resolveSurah(bookmark.surahNumber) ?: return@clickable
                                viewModel.selectSurah(surahObj, bookmark.ayahNumber ?: 1)
                                navigator.push(ReaderKey)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RubElHizbIcon(number = bookmark.surahNumber)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(bookmark.surahNameEnglish, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            if (bookmark.ayahNumber != null) "Verse ${bookmark.ayahNumber}" else "Full Surah Bookmarked",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    viewModel.toggleBookmark(
                                        Surah(
                                            number = bookmark.surahNumber,
                                            nameArabic = bookmark.surahName,
                                            nameEnglish = bookmark.surahNameEnglish,
                                            translation = "",
                                            type = "",
                                            versesCount = bookmark.numberOfVerses,
                                        ),
                                        bookmark.ayahNumber
                                    )
                                }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Delete", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            if (bookmark.ayahNumber != null && bookmark.arabicText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    bookmark.arabicText,
                                    style = QuranVerseSm,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    bookmark.translationText,
                                    style = BodyMdCustom,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
