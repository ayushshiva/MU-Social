package com.mu.social.presentation.live.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mu.social.domain.model.live.LiveGift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    onGiftSelected: (LiveGift) -> Unit,
    onRecharge: () -> Unit,
    onDismiss: () -> Unit
) {
    val gifts = listOf(
        LiveGift("1", "Rose", "https://cdn-icons-png.flaticon.com/512/2504/2504814.png", 5),
        LiveGift("2", "Ice Cream", "https://cdn-icons-png.flaticon.com/512/2504/2504812.png", 10),
        LiveGift("3", "Heart", "https://cdn-icons-png.flaticon.com/512/2504/2504811.png", 20),
        LiveGift("4", "Rocket", "https://cdn-icons-png.flaticon.com/512/2504/2504818.png", 100),
        LiveGift("5", "Crown", "https://cdn-icons-png.flaticon.com/512/2504/2504810.png", 500)
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Send a Gift",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(250.dp)
            ) {
                items(gifts) { gift ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                onGiftSelected(gift)
                                onDismiss()
                            }
                    ) {
                        AsyncImage(
                            model = gift.iconUrl,
                            contentDescription = gift.name,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(text = gift.name, fontSize = 12.sp)
                        Text(
                            text = "${gift.coinValue} 🪙",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    onDismiss()
                    onRecharge()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Recharge Coins")
            }
        }
    }
}
