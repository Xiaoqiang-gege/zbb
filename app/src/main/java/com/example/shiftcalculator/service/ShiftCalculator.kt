package com.example.shiftcalculator.service

import com.example.shiftcalculator.model.ShiftType
import java.util.Calendar

object ShiftCalculator {
    fun getShiftType(date: Calendar, startDate: Calendar, shifts: List<ShiftType>): ShiftType {
        if (shifts.isEmpty()) {
            return ShiftType.getDefaultShifts().first()
        }
        
        val start = Calendar.getInstance().apply {
            timeInMillis = startDate.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val target = Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diffInMillis = target.timeInMillis - start.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        
        val cycle = if (diffInDays < 0) {
            (shifts.size + (diffInDays % shifts.size)) % shifts.size
        } else {
            diffInDays % shifts.size
        }
        
        return shifts[cycle]
    }
}

