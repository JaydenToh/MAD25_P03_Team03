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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardColor = Color(0xFF2F2F45)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)

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
        else -> "Need Practice"
    }


    val mainColor = if (isWin) Color.White else MaterialTheme.colorScheme.error

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
            color = Color.White
        )

        Spacer(Modifier.height(24.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("FINAL SCORE", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
        }

        Spacer(Modifier.height(24.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Accuracy Card
            StatCard(
                modifier = Modifier.weight(1f),
                //color = CardColor,
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

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp) // FIX: Adds 16dp gap between the top row and bottom button
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // 按钮间的间距
            ) {
                // 1. Play Again Button
                OutlinedButton(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play Again", fontSize = 16.sp)
                }

                // 2. Back to Rules Button
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
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
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Back to Menu")
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: String
) {
    Card(
        modifier = modifier,
        // FIX: Explicitly set the background to your requested color
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite // Text must be white to be visible on dark card
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 1
            )
        }
    }
}