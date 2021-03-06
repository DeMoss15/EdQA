package com.demoss.edqa.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.demoss.edqa.data.local.room.dao.AnswerRoomDao
import com.demoss.edqa.data.local.room.dao.QuestionRoomDao
import com.demoss.edqa.data.local.room.dao.TestRoomDao
import com.demoss.edqa.data.local.room.entities.AnswerRoomEntity
import com.demoss.edqa.data.local.room.entities.QuestionRoomEntity
import com.demoss.edqa.data.local.room.entities.TestRoomEntity

@Database(
    entities = [
        TestRoomEntity::class,
        QuestionRoomEntity::class,
        AnswerRoomEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestRoomDao
    abstract fun questionDao(): QuestionRoomDao
    abstract fun answerDao(): AnswerRoomDao
}