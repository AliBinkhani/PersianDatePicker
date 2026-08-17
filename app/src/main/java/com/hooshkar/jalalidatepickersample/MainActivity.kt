package com.hooshkar.jalalidatepickersample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hooshkar.jalalidatepicker.DatePicker
import com.hooshkar.jalalidatepicker.rememberDatePickerState
import com.hooshkar.jalalidatepickersample.ui.theme.JalaliDatePickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JalaliDatePickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JalaliDatePickerDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun JalaliDatePickerDemo(modifier: Modifier = Modifier) {
    val state = rememberDatePickerState()
    DatePicker(state = state, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun JalaliDatePickerDemoPreview() {
    JalaliDatePickerTheme { JalaliDatePickerDemo() }
}
