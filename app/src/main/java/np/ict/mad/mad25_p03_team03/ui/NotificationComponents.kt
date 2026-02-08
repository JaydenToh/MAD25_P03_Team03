package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Variable - Color Theme - Custom colors for the dark theme UI
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val GrayText = Color.Gray

// Class - Data Model - Structure for notification content
data class NotificationData(
    val senderName: String,
    val message: String,
    val chatRoomId: String,
    val senderId: String
)

// Function - UI Component - Displays a popup banner for new messages
// Flow 1.0: Component Entry Point
@Composable
fun NotificationBanner(
    data: NotificationData, // Variable - Input - Data object containing sender and message info
    onClick: () -> Unit,    // Variable - Input - Callback when the banner is clicked
    onDismiss: () -> Unit   // Variable - Input - Callback to dismiss the banner
) {
    // Flow 1.1: Main Card Container
    Card(
        modifier = Modifier // Flow 1.2: Modifier Setup
            .fillMaxWidth() // Flow 1.3: Width Constraint
            .height(80.dp)  // Flow 1.4: Fixed Height
            .padding(horizontal = 8.dp) // Flow 1.5: External Padding
            .shadow(12.dp, RoundedCornerShape(16.dp)) // Flow 1.6: Drop Shadow
            .clickable { onClick() }, // Flow 1.7: Click Interaction
        shape = RoundedCornerShape(16.dp), // Flow 1.8: Rounded Corners
        colors = CardDefaults.cardColors( // Flow 1.9: Color Definition
            containerColor = CardColor1 // Variable - Color - Dark Card Background
        )
    ) {
        // Flow 2.0: Internal Layout
        Row(
            modifier = Modifier // Flow 2.1: Modifier Setup
                .fillMaxSize() // Flow 2.2: Fill Card Size
                .padding(16.dp), // Flow 2.3: Internal Padding
            verticalAlignment = Alignment.CenterVertically // Flow 2.4: Vertical Centering
        ) {
            // Flow 3.0: Icon Container
            Surface(
                modifier = Modifier.size(40.dp), // Flow 3.1: Size Constraint
                shape = RoundedCornerShape(8.dp), // Flow 3.2: Icon Shape
                color = PurpleAccent.copy(alpha = 0.1f) // Variable - Color - Subtle Accent Background
            ) {
                // Flow 3.3: Icon Placement
                Box(contentAlignment = Alignment.Center) { // Flow 3.4: Center Alignment
                    Icon(
                        imageVector = Icons.Default.ChatBubble, // Flow 3.5: Icon Resource
                        contentDescription = null, // Flow 3.6: Accessibility
                        tint = PurpleAccent, // Variable - Color - Icon Tint
                        modifier = Modifier.size(24.dp) // Flow 3.7: Icon Size
                    )
                }
            }

            // Flow 4.0: Spacing
            Spacer(modifier = Modifier.width(16.dp)) // Flow 4.1: Gap between Icon and Text

            // Flow 5.0: Text Content Column
            Column(modifier = Modifier.weight(1f)) { // Flow 5.1: Weight to fill remaining space
                // Flow 5.2: Sender Name
                Text(
                    text = data.senderName, // Flow 5.3: Name Data
                    style = MaterialTheme.typography.labelLarge, // Flow 5.4: Text Style
                    fontWeight = FontWeight.Bold, // Flow 5.5: Font Weight
                    color = PurpleAccent // Variable - Color - Accent Text Color
                )
                // Flow 5.6: Message Preview
                Text(
                    text = data.message, // Flow 5.7: Message Data
                    style = MaterialTheme.typography.bodyMedium, // Flow 5.8: Text Style
                    maxLines = 1, // Flow 5.9: Line Limit
                    overflow = TextOverflow.Ellipsis, // Flow 5.10: Truncation
                    color = TextWhite // Variable - Color - Primary Text Color
                )
            }

            // Flow 6.0: Time Stamp
            Text(
                text = "Now", // Flow 6.1: Static Time Text
                style = MaterialTheme.typography.labelSmall, // Flow 6.2: Text Style
                color = GrayText // Variable - Color - Secondary Text Color
            )
        }
    }
}