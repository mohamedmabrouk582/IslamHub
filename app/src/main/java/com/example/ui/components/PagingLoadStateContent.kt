package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState

@Composable
fun PagingRefreshContent(
    loadState: LoadState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (loadState) {
        is LoadState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is LoadState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
fun PagingAppendContent(
    loadState: LoadState,
    modifier: Modifier = Modifier,
) {
    if (loadState is LoadState.Loading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
