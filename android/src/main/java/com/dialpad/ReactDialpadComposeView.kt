package  com.dialpad


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ReactDialpadComposeView() {
  var dialedNumber by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Display dialed number
    Text(
      text = dialedNumber,
      fontSize = 36.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(16.dp)
    )

    // Number grid
    DialPad(onNumberClick = { number ->
      dialedNumber += number
    }, onDeleteClick = {
      if (dialedNumber.isNotEmpty()) {
        dialedNumber = dialedNumber.dropLast(1)
      }
    })

    // Dial button (can implement a call function here)
    Button(onClick = { /* Add dial functionality */ }) {
      Text("Dial")
    }
  }
}

@Composable
fun DialPad(
  onNumberClick: (String) -> Unit,
  onDeleteClick: () -> Unit
) {
  val buttons = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("*", "0", "#")
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    buttons.forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        row.forEach { number ->
          DialPadButton(number) { onNumberClick(number) }
        }
      }
    }

    // Delete button row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      Spacer(modifier = Modifier.weight(1f))
      IconButton(onClick = { onDeleteClick() }) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Delete")
      }
    }
  }
}

@Composable
fun DialPadButton(number: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .size(80.dp)
      .background(Color.Gray, shape = CircleShape)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = number,
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DialPadScreenPreview() {
  ReactDialpadComposeView()
}
