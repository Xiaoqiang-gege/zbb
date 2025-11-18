package com.example.shiftcalculator.model

import kotlinx.serialization.Serializable

@Serializable
data class ShiftType(
    val id: String,
    val label: String,
    val color: Long
) {
    companion object {
        // 默认班次配置
        fun getDefaultShifts(): List<ShiftType> {
            return listOf(
                ShiftType("day", "白班", 0xFF2196F3),
                ShiftType("night", "夜班", 0xFF9C27B0),
                ShiftType("rest", "休班", 0xFF4CAF50)
            )
        }
    }
}

