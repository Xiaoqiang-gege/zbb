package com.example.shiftcalculator.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shiftcalculator.model.ShiftType
import com.example.shiftcalculator.service.ShiftCalculator
import com.example.shiftcalculator.utils.Storage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var showSettings by remember { mutableStateOf(false) }
    var startDateMillis by remember { mutableStateOf(0L) }
    var shifts by remember { mutableStateOf<List<ShiftType>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val savedDate = Storage.getStartDate(context).first()
        if (savedDate != null) {
            startDateMillis = savedDate
        }
        shifts = Storage.getShifts(context).first()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "${currentMonth.get(Calendar.YEAR)}年${currentMonth.get(Calendar.MONTH) + 1}月",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween  // 均匀分布空间
        ) {
            // 星期标题
            WeekDayHeaders()
            
            // 日历网格 - 自适应剩余空间，确保完整显示
            CalendarGrid(
                month = currentMonth,
                startDateMillis = startDateMillis,
                shifts = shifts,
                onMonthChange = { newMonth ->
                    currentMonth = newMonth
                },
                modifier = Modifier.weight(1f)  // 占据剩余空间
            )
            
            // 图例 - 底部
            Legend(shifts = shifts)
        }
    }
    
    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false },
            onConfirm = { date ->
                scope.launch {
                    Storage.saveStartDate(context, date)
                    startDateMillis = date
                }
                showSettings = false
            },
            onClearDate = {
                scope.launch {
                    Storage.clearStartDate(context)
                    startDateMillis = 0L
                }
                showSettings = false
            },
            shifts = shifts,
            onShiftsUpdate = { newShifts ->
                scope.launch {
                    Storage.saveShifts(context, newShifts)
                    shifts = newShifts
                }
            }
        )
    }
}

@Composable
fun WeekDayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CalendarGrid(
    month: Calendar,
    startDateMillis: Long,
    shifts: List<ShiftType>,
    onMonthChange: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = remember(month) {
        generateCalendarDays(month)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // 日历网格 - 自适应剩余空间高度，无需滚动
        // 使用 BoxWithConstraints 获取可用高度，动态计算单元格高度
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  // 占据剩余空间
        ) {
            // 计算单元格高度：可用高度减去上下padding(16dp)和5个行间距(5*4dp=20dp)，除以6行
            // 确保最小高度为50dp，以保证值班信息可见
            val availableHeight = maxHeight
            val totalPadding = 16.dp  // 上下padding各8dp
            val totalSpacing = 20.dp   // 5个行间距，每个4dp
            val minCellHeight = 50.dp
            val calculatedHeight = (availableHeight - totalPadding - totalSpacing) / 6
            val cellHeight = calculatedHeight.coerceAtLeast(minCellHeight)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(days.size) { index ->
                    CalendarDayCell(
                        dayInfo = days[index],
                        startDateMillis = startDateMillis,
                        currentMonth = month,
                        shifts = shifts,
                        cellHeight = cellHeight  // 传入动态计算的高度
                    )
                }
            }
        }
        
        // 月份导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                val newMonth = month.clone() as Calendar
                newMonth.add(Calendar.MONTH, -1)
                onMonthChange(newMonth)
            }) {
                Text("上个月")
            }
            
            Button(onClick = {
                val newMonth = Calendar.getInstance()
                onMonthChange(newMonth)
            }) {
                Text("今天")
            }
            
            Button(onClick = {
                val newMonth = month.clone() as Calendar
                newMonth.add(Calendar.MONTH, 1)
                onMonthChange(newMonth)
            }) {
                Text("下个月")
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayInfo: DayInfo,
    startDateMillis: Long,
    currentMonth: Calendar,
    shifts: List<ShiftType>,
    cellHeight: androidx.compose.ui.unit.Dp = 75.dp  // 默认高度，如果未传入
) {
    val today = Calendar.getInstance()
    val isToday = dayInfo.date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                  dayInfo.date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                  dayInfo.date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    
    val shiftType = if (startDateMillis > 0 && dayInfo.isCurrentMonth && shifts.isNotEmpty()) {
        val startDate = Calendar.getInstance().apply { timeInMillis = startDateMillis }
        ShiftCalculator.getShiftType(dayInfo.date, startDate, shifts)
    } else {
        null
    }
    
    val backgroundColor = if (shiftType != null) {
        Color(shiftType.color)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (shiftType != null) {
        Color.White
    } else if (dayInfo.isCurrentMonth) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }
    
    // 使用动态高度，根据可用空间自适应
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellHeight)  // 使用传入的动态高度
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isToday) {
                    Modifier.border(2.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),  // 内边距
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${dayInfo.date.get(Calendar.DAY_OF_MONTH)}",
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(bottom = 3.dp)  // 日期和值班信息之间的间距
            )
            if (shiftType != null) {
                Text(
                    text = shiftType.label,
                    color = textColor,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,  // 行高，确保完整显示
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun Legend(shifts: List<ShiftType>) {
    val displayShifts = if (shifts.isEmpty()) ShiftType.getDefaultShifts() else shifts
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "图例",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                displayShifts.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(type.color), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = type.label,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onClearDate: () -> Unit,
    shifts: List<ShiftType>,
    onShiftsUpdate: (List<ShiftType>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var editingShifts by remember { mutableStateOf(shifts) }
    
    LaunchedEffect(shifts) {
        editingShifts = shifts
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("起始日期") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("班次管理") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTab) {
                    0 -> {
                        DateSettingsContent(
                            onConfirm = onConfirm,
                            onClearDate = onClearDate,
                            onDismiss = onDismiss
                        )
                    }
                    1 -> {
                        ShiftSettingsContent(
                            shifts = editingShifts,
                            onShiftsChange = { editingShifts = it },
                            onSave = {
                                onShiftsUpdate(editingShifts)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSettingsContent(
    onConfirm: (Long) -> Unit,
    onClearDate: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("请选择排班起始日期：")
        
        DatePicker(
            selectedDate = selectedDate,
            onDateChange = { newDate ->
                selectedDate = newDate
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    selectedDate.set(Calendar.HOUR_OF_DAY, 0)
                    selectedDate.set(Calendar.MINUTE, 0)
                    selectedDate.set(Calendar.SECOND, 0)
                    selectedDate.set(Calendar.MILLISECOND, 0)
                    onConfirm(selectedDate.timeInMillis)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("确定")
            }
            
            OutlinedButton(
                onClick = onClearDate,
                modifier = Modifier.weight(1f)
            ) {
                Text("清除")
            }
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
        }
    }
}

@Composable
fun ShiftSettingsContent(
    shifts: List<ShiftType>,
    onShiftsChange: (List<ShiftType>) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "班次配置",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = "可以添加、编辑或删除班次。每个班次可以自定义名称和颜色。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shifts.size, key = { shifts[it].id }) { index ->
                ShiftItemEditor(
                    shift = shifts[index],
                    onShiftChange = { newShift ->
                        val updated = shifts.toMutableList()
                        updated[index] = newShift
                        onShiftsChange(updated)
                    },
                    onDelete = {
                        val updated = shifts.toMutableList()
                        updated.removeAt(index)
                        onShiftsChange(updated)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        val newShift = ShiftType(
                            id = UUID.randomUUID().toString(),
                            label = "新班次",
                            color = 0xFF757575
                        )
                        onShiftsChange(shifts + newShift)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加班次")
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (shifts.isEmpty()) {
                Button(
                    onClick = {
                        onShiftsChange(ShiftType.getDefaultShifts())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("恢复默认")
                }
            }
            
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = shifts.isNotEmpty()
            ) {
                Text("保存")
            }
        }
    }
}

@Composable
fun ShiftItemEditor(
    shift: ShiftType,
    onShiftChange: (ShiftType) -> Unit,
    onDelete: () -> Unit
) {
    var label by remember(shift.id) { mutableStateOf(shift.label) }
    var showColorPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        onShiftChange(shift.copy(label = it))
                    },
                    label = { Text("班次名称") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(shift.color),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { showColorPicker = true }
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                )
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = shift.color,
            onColorSelected = { color ->
                onShiftChange(shift.copy(color = color))
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val presetColors = listOf(
        0xFF2196F3, // 蓝色
        0xFF9C27B0, // 紫色
        0xFF4CAF50, // 绿色
        0xFFFF9800, // 橙色
        0xFFF44336, // 红色
        0xFF00BCD4, // 青色
        0xFFE91E63, // 粉色
        0xFF795548, // 棕色
        0xFF607D8B, // 蓝灰色
        0xFF9E9E9E, // 灰色
        0xFF3F51B5, // 靛蓝色
        0xFF009688  // 青绿色
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetColors.size) { index ->
                    val color = presetColors[index]
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Color(color),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onColorSelected(color) }
                            .then(
                                if (color == currentColor) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                }
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
fun DatePicker(
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit
) {
    var year by remember(selectedDate) { mutableStateOf(selectedDate.get(Calendar.YEAR)) }
    var month by remember(selectedDate) { mutableStateOf(selectedDate.get(Calendar.MONTH)) }
    var day by remember(selectedDate) { mutableStateOf(selectedDate.get(Calendar.DAY_OF_MONTH)) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Text("年", fontSize = 12.sp)
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { y ->
                            year = y
                            val newDate = Calendar.getInstance().apply {
                                set(year, month, day)
                            }
                            onDateChange(newDate)
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
            }
            Column {
                Text("月", fontSize = 12.sp)
                OutlinedTextField(
                    value = (month + 1).toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { m ->
                            if (m in 1..12) {
                                month = m - 1
                                val newDate = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                onDateChange(newDate)
                            }
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
            }
            Column {
                Text("日", fontSize = 12.sp)
                OutlinedTextField(
                    value = day.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { d ->
                            if (d in 1..31) {
                                day = d
                                val newDate = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                onDateChange(newDate)
                            }
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

data class DayInfo(
    val date: Calendar,
    val isCurrentMonth: Boolean
)

fun generateCalendarDays(month: Calendar): List<DayInfo> {
    val days = mutableListOf<DayInfo>()
    
    val firstDay = month.clone() as Calendar
    firstDay.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK)
    
    // 上个月的日期
    val prevMonth = month.clone() as Calendar
    prevMonth.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - Calendar.SUNDAY
    
    for (i in startOffset - 1 downTo 0) {
        val date = Calendar.getInstance()
        date.set(month.get(Calendar.YEAR), month.get(Calendar.MONTH) - 1, daysInPrevMonth - i)
        days.add(DayInfo(date, false))
    }
    
    // 当前月的日期
    val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..daysInMonth) {
        val date = Calendar.getInstance()
        date.set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), day)
        days.add(DayInfo(date, true))
    }
    
    // 下个月的日期（填满网格）
    val remainingDays = 42 - days.size
    for (day in 1..remainingDays) {
        val date = Calendar.getInstance()
        date.set(month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1, day)
        days.add(DayInfo(date, false))
    }
    
    return days
}

