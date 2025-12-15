import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameSummaryScreen(
    score: Int,
    totalQuestions: Int,
    correctCount: Int,
    longestStreak: Int,
    avgTime: Float,
    isWin: Boolean,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {

    val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).toInt() else 0


    val rankTitle = when {
        accuracy == 100 -> "🎵 Music God 🎵"
        accuracy >= 80 -> "🎸 Rock Star"
        accuracy >= 50 -> "🎤 Karaoke Singer"
        else -> "👂 Need Practice"
    }


    val mainColor = if (isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Icon(
            imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isWin) "Victory!" else "Game Over",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = mainColor
        )
        Text(
            text = rankTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("FINAL SCORE", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accuracy Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Accuracy",
                value = "$accuracy%",
                icon = "🎯"
            )
            // Streak Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Best Streak",
                value = "$longestStreak",
                icon = "🔥"
            )
            // Time Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Avg Speed",
                value = String.format("%.1fs", avgTime),
                icon = "⚡"
            )
        }

        Spacer(Modifier.weight(1.5f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 按钮间的间距
        ) {
            // 1. Play Again Button
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainColor)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Play Again", fontSize = 16.sp)
            }

            // 2. Back to Rules Button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                // Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                Text("Back to Rules", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(2.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to Menu")
        }
    }
}


@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, icon: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}