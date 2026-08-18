package com.hooshkar.persiandatepickersample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hooshkar.persiandatepicker.DatePicker
import com.hooshkar.persiandatepicker.DatePickerDialog
import com.hooshkar.persiandatepicker.rememberDatePickerState
import com.hooshkar.persiandatepicker.toGregorian
import com.hooshkar.persiandatepickersample.ui.theme.PersianDatePickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersianDatePickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PersianDatePickerDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PersianDatePickerDemo(modifier: Modifier = Modifier) {
    // No initial selection: the picker starts empty, and the user opens it via the button below.
    val state = rememberDatePickerState()
    var dialogIsVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        val selectedDate = state.selectedDate
        Text(
            text =
                selectedDate?.let { "${it.year}/${it.month}/${it.dayOfMonth} (Persian)" }
                    ?: "No date selected",
            style = MaterialTheme.typography.titleLarge,
        )
        // Demonstrates converting the picker's Persian CalendarDate to its Gregorian equivalent.
        if (selectedDate != null) {
            val gregorian = selectedDate.toGregorian()
            Text(
                text = "${gregorian.year}/${gregorian.month}/${gregorian.dayOfMonth} (Gregorian)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(onClick = { dialogIsVisible = true }) { Text("Select date") }
    }

    if (dialogIsVisible) {
        DatePickerDialog(
            onDismissRequest = { dialogIsVisible = false },
            confirmButton = { TextButton(onClick = { dialogIsVisible = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { dialogIsVisible = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersianDatePickerDemoPreview() {
    PersianDatePickerTheme { PersianDatePickerDemo() }
}
