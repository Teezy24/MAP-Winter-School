package com.example.studybuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    Scaffold()
    { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Hello, Student 👋", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Coming up", fontSize = 20.sp)
            LazyColumn(modifier = Modifier.height(100.dp)) {
                items(2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Math Revision", fontSize = 16.sp)
                            Text("Today at 4:00 PM", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Calendar", fontSize = 20.sp)

            val currentMonth = remember { YearMonth.now() }
            val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }
            val firstDayOfMonth = remember(currentMonth) { currentMonth.atDay(1).dayOfWeek }
            val daysList = remember(currentMonth) {
                (1..daysInMonth).map { day -> currentMonth.atDay(day) }
            }

            // Month header
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                modifier = Modifier.padding(8.dp),
                fontSize = 18.sp
            )
            // Days of week row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DayOfWeek.values().forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            // Calendar days grid
            val totalGridCount = daysInMonth + firstDayOfMonth.value - 1
            val rows = (totalGridCount / 7) + 1
            Column {
                var dayIndex = 0
                for (row in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            if (cellIndex < firstDayOfMonth.value - 1 || dayIndex >= daysList.size) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {}
                            } else {
                                val date = daysList[dayIndex]
                                val isSelected = date == selectedDate
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                dayIndex++
                            }
                        }
                    }
                }
            }
        }
    }
}
