package com.nullmessenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NullMessengerApp() {
    var message by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateOf(
            listOf(
                "Welcome to Null Messenger",
                "Add friends first."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "NULL MESSENGER",
            color = Color.White,
            fontSize = 22.sp
        )

        Text(
            text = "SYSTEM ONLINE",
            color = Color.Gray,
            fontSize = 12.sp
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { text ->
                Text(
                    text = "> $text",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                label = {
                    Text("Message")
                }
            )

            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        messages.value = messages.value + message
                        message = ""
                    }
                }
            ) {
                Text("SEND")
            }
        }
    }
}
