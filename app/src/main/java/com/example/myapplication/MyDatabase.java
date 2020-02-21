package com.example.myapplication;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Recolt.class}, version = 8,exportSchema = false)
abstract class MyDatabase extends RoomDatabase {
    private static MyDatabase database;
    public static synchronized MyDatabase instance(Context context){
        if (database == null){
            database =Room.databaseBuilder(context.getApplicationContext(), MyDatabase.class, MyDatabase.DB_Racolt).allowMainThreadQueries().fallbackToDestructiveMigration().build();

        }
        return database;
    }
    public static final String DB_Racolt = "app_db";
    public static final String Racolt = "todo";
     abstract DaoAccess daoAccess();
}
