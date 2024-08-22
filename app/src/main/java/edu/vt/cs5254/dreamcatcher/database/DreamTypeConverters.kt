package edu.vt.cs5254.dreamcatcher.database

import androidx.room.TypeConverter
import java.util.Date

class DreamTypeConverters {

    @TypeConverter
    fun getLongFromDate(date: Date): Long = date.time

    @TypeConverter
    fun getDateFromLong(millis: Long): Date = Date(millis)

}