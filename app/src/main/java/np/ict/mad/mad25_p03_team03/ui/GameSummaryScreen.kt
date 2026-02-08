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

// Variable - Color - Background color for cards
private val CardColor = Color(0xFF2F2F45)
// Variable - Color - Primary white text color
private val TextWhite = Color(0xFFFFFFFF)
// Variable - Color - Secondary gray text color
private val TextGray = Color(0xFFB0B0B0)

// Function - UI Component - Displays final game statistics and user performance
// Flow 1.0: Screen Entry Point
@Composable
fun GameSummaryScreen(
    score: Int,              // Variable - Input - Final numeric score achieved
    totalQuestions: Int,     // Variable - Input - Total number of questions played
    correctCount: Int,       // Variable - Input - Number of correct guesses
    longestStreak: Int,      // Variable - Input - Highest consecutive correct answers
    avgTime: Float,          // Variable - Input - Average speed per question
    isWin: Boolean,          // Variable - Input - Game outcome status
    onPlayAgain: () -> Unit, // Variable - Input - Callback to restart the game
    onBack: () -> Unit       // Variable - Input - Callback to return to menu
) {

    // Logic - Calculation - Determines the percentage of correct answers
    // Flow 1.1: Calculate Accuracy
    val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).toInt() else 0


    // Logic - Selection - Assigns a title based on user performance
    // Flow 1.2: Determine Performance Rank
    val rankTitle = when {
        accuracy == 100 -> "🎵 Music God 🎵"
        accuracy >= 80 -> "🎸 Rock Star"
        accuracy >= 50 -> "🎤 Karaoke Singer"
        else -> "Need Practice"
    }


    // Variable - UI Color - Sets main theme color based on win/loss
    // Flow 1.3: Set Theme Color
    val mainColor = if (isWin) Color.White else MaterialTheme.colorScheme.error

    // UI - Layout - Root container for the summary content
    // Flow 2.0: Root Layout Setup
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // UI - Icon - Displays victory trophy or defeat face
        // Flow 2.1: Outcome Icon Rendering
        Icon(
            imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(80.dp)
        )
        // Flow 2.2: Add Vertical Spacing
        Spacer(Modifier.height(12.dp))
        // UI - Text - Displays the outcome header
        // Flow 2.3: Status Text Rendering
        Text(
            text = if (isWin) "Victory!" else "Game Over",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = mainColor
        )
        // UI - Text - Displays the user's earned rank title
        // Flow 2.4: Rank Title Rendering
        Text(
            text = rankTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        // Flow 2.5: Add Vertical Spacing
        Spacer(Modifier.height(24.dp))


        // UI - Card - Highlights the final total score
        // Flow 3.0: Score Card Construction
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // UI - Text - Label for the score
                // Flow 3.1: Final Score Label
                Text("FINAL SCORE", color = Color.White, style = MaterialTheme.typography.labelLarge)
                // UI - Text - The actual score value
                // Flow 3.2: Score Value Rendering
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
        }

        // Flow 3.3: Add Vertical Spacing
        Spacer(Modifier.height(16.dp))


        // UI - Row - Container for multiple horizontal statistic cards
        // Flow 4.0: Stats Row Construction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flow 4.1: Accuracy Card Rendering
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Accuracy",
                value = "$accuracy%",
                icon = "🎯"
            )
            // Flow 4.2: Best Streak Card Rendering
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Best Streak",
                value = "$longestStreak",
                icon = "🔥"
            )
            // Flow 4.3: Average Speed Card Rendering
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Avg Speed",
                value = String.format("%.1fs", avgTime),
                icon = "⚡"
            )
        }

        // Flow 4.4: Flexible Spacing for alignment
        Spacer(Modifier.weight(1.5f))

        // UI - Row - Layout for action buttons
        // Flow 5.0: Action Buttons Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // UI - Button - Triggers game restart
            // Flow 5.1: Play Again Button Rendering
            OutlinedButton(
                onClick = onPlayAgain,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                // Flow 5.2: Refresh Icon
                Icon(Icons.Default.Refresh, null)
                // Flow 5.3: Horizontal Gap
                Spacer(Modifier.width(8.dp))
                // Flow 5.4: Button Text
                Text("Play Again", fontSize = 16.sp)
            }


        }

        // Flow 5.5: Add Vertical Spacing
        Spacer(Modifier.height(2.dp))

        // UI - Button - Triggers return to main menu
        // Flow 5.6: Back to Menu Button Rendering
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            // Flow 5.7: Button Text
            Text("Back to Menu")
        }
    }
}

// Function - UI Component - Reusable card for displaying a single metric
// Flow 6.0: Stat Card Entry Point
@Composable
fun StatCard(
    modifier: Modifier = Modifier, // Variable - Input - Layout modifiers
    label: String,                 // Variable - Input - Text description of the stat
    value: String,                 // Variable - Input - Numeric or string value of the stat
    icon: String                   // Variable - Input - Emoji icon for the stat
) {
    // UI - Card - Surface for individual stat display
    // Flow 6.1: Card Container Setup
    Card(
        modifier = modifier,
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
            // UI - Text - Stat Emoji Icon
            // Flow 6.2: Icon Rendering
            Text(text = icon, fontSize = 24.sp)

            // Flow 6.3: Vertical Gap
            Spacer(Modifier.height(8.dp))

            // UI - Text - Stat Value
            // Flow 6.4: Value Text Rendering
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            // UI - Text - Stat Label
            // Flow 6.5: Label Text Rendering
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 1
            )
        }
    }
}