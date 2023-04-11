package com.example.cameraroomrecyclerview

import android.app.Application
import androidx.room.Room
import com.example.cameraroomrecyclerview.data.AppDataBase

class App:Application() {
    lateinit var dataBase:AppDataBase

    override fun onCreate() {
        super.onCreate()
        //Экзеипляр БД
        dataBase = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,
            "fileDataBase"
        )
            //.fallbackToDestructiveMigration() //Удаляет БД если нет инструкции по мигнрации данных
            .build()
    }

}