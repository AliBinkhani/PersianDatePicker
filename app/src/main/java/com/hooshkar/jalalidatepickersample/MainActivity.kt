package com.hooshkar.jalalidatepickersample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hooshkar.jalalidatepicker.DatePicker
import com.hooshkar.jalalidatepicker.DatePickerDialog
import com.hooshkar.jalalidatepicker.rememberDatePickerState
import com.hooshkar.jalalidatepickersample.ui.theme.JalaliDatePickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JalaliDatePickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { JalaliDatePickerDemo() }
            }
        }
    }
}

@Composable
fun JalaliDatePickerDemo() {
    val state = rememberDatePickerState()
    var dialogIsVisible by remember { mutableStateOf(true) }
    if (dialogIsVisible) {
        DatePickerDialog(
            onDismissRequest = {
                dialogIsVisible = false
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogIsVisible = false
                }) {
                    Text("OK")
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JalaliDatePickerDemoPreview() {
    JalaliDatePickerTheme { JalaliDatePickerDemo() }
}
