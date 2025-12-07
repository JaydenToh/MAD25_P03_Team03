package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun OTPScreen(onContinueToApp: () -> Unit) {

    var otp by remember { mutableStateOf(generateOTP()) }
    var seconds by remember { mutableStateOf(15) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds--
            if (seconds <= 0) {
                seconds = 15
                otp = generateOTP()
            }
        }
    }

    val gradientBrush = Brush.linearGradient(
        listOf(Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(gradientBrush).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("OTP Verification", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .shadow(6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Text(otp, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text("Refreshing in $seconds sec", color = Color.Gray)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onContinueToApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

fun generateOTP(): String = Random.nextInt(100000, 999999).toString()
