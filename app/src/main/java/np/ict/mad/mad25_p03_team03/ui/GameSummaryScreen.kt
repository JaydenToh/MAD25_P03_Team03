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

// Function - UI Component - Displays final stats after game ends
// Flow 1.0: Screen Entry Point
@Composable
fun GameSummaryScreen(
    score: Int,              // Variable - Input - Final score achieved
    totalQuestions: Int,     // Variable - Input - Number of questions attempted
    correctCount: Int,       // Variable - Input - Number of correct answers
    longestStreak: Int,      // Variable - Input - Highest streak of correct answers
    avgTime: Float,          // Variable - Input - Average time taken per question
    isWin: Boolean,          // Variable - Input - True if lives > 0, False if lives = 0
    onPlayAgain: () -> Unit, // Variable - Input - Callback to restart game
    onBack: () -> Unit       // Variable - Input - Callback to return to menu
) {

    // Flow 1.1: Calculate Accuracy
    // Calculates percentage of correct answers (0-100)
    val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).toInt() else 0


    // Flow 1.2: Determine Rank Title
    // Assigns a fun title based on accuracy percentage
    val rankTitle = when {
        accuracy == 100 -> "🎵 Music God 🎵"
        accuracy >= 80 -> "🎸 Rock Star"
        accuracy >= 50 -> "🎤 Karaoke Singer"
        else -> "Need Practice"
    }


    // Flow 1.3: Determine Theme Color
    // Sets color to Primary (Green-ish usually) if win, Error (Red) if lose
    val mainColor = if (isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    // Flow 2.0: Main Layout Container
    // Vertical column to stack elements (Icon -> Title -> Score -> Stats -> Buttons)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // Flow 2.1: Outcome Icon
        // Shows Trophy if win, Sad Face if lose
        Icon(
            imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(80.dp)
        )
        // Flow 2.2: Spacing
        Spacer(Modifier.height(12.dp))

        // Flow 2.3: Victory/Defeat Text
        Text(
            text = if (isWin) "Victory!" else "Game Over",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = mainColor
        )
        // Flow 2.4: Rank Title Text
        Text(
            text = rankTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        // Flow 2.5: Spacing
        Spacer(Modifier.height(24.dp))


        // Flow 3.0: Score Card
        // Highlights the final numeric score
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

        // Flow 3.1: Spacing
        Spacer(Modifier.height(16.dp))


        // Flow 4.0: Statistics Row
        // Displays Accuracy, Streak, and Avg Time horizontally
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flow 4.1: Accuracy Card
            StatCard(
                modifier = Modifier.weight(1f),
                //color = CardColor,
                label = "Accuracy",
                value = "$accuracy%",
                icon = "🎯"
            )
            // Flow 4.2: Streak Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Best Streak",
                value = "$longestStreak",
                icon = "🔥"
            )
            // Flow 4.3: Time Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Avg Speed",
                value = String.format("%.1fs", avgTime),
                icon = "⚡"
            )
        }

        // Flow 5.0: Flexible Spacing
        // Pushes buttons to the bottom
        Spacer(Modifier.weight(1.5f))

        // Flow 6.0: Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 按钮间的间距
        ) {
            // Flow 6.1: Play Again Button
            // Resets game state via onPlayAgain callback
            Button(
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


        }

        // Flow 6.2: Spacing
        Spacer(Modifier.height(2.dp))

        // Flow 6.3: Back Button
        // Navigates back to setup screen
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to Rules")
        }
    }
}


// Function - UI Component - Reusable card for a single statistic
// Flow 7.0: Stat Card Component
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
            // Flow 7.1: Icon Emoji
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            // Flow 7.2: Stat Value
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            // Flow 7.3: Stat Label
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}