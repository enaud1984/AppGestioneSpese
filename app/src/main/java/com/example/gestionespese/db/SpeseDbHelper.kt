package com.example.gestionespese.db
// SpeseDbHelper.kt

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.gestionespese.db.Contabilita.DATABASE_NOME
import com.example.gestionespese.db.Contabilita.DATABASE_VERSIONE
import com.example.gestionespese.db.Contabilita.SpeseEntry
import java.text.SimpleDateFormat
import java.util.Date

class SpeseDbHelper(context: Context) :SQLiteOpenHelper(context, DATABASE_NOME, null, DATABASE_VERSIONE) {

    init {
        Log.d("SpeseDbHelper", "Costruttore chiamato SpeseDbHelper")
        Log.d("DB", context.getDatabasePath(DATABASE_NOME).path)
    }
    override fun onCreate(db: SQLiteDatabase) {
        Log.i("SpeseDbHelper","CREO TABELLA SPESE")
        // Creazione della tabella
        val SQL_CREARE_TABELLA =
            "CREATE TABLE ${SpeseEntry.TABELLA_NOME} (" +
                    "${SpeseEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${SpeseEntry.COLONNA_NOME_SPESA} TEXT NOT NULL, " +
                    "${SpeseEntry.COLONNA_USCITA} REAL NOT NULL, " +
                    "${SpeseEntry.COLONNA_DATA} DATE)"
        db.execSQL(SQL_CREARE_TABELLA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${SpeseEntry.TABELLA_NOME}")
        onCreate(db)
    }

    fun insertSpesa(spesa: Spesa) {
        val nome_spesa= spesa.nome_spesa
        val uscita=spesa.uscita
        val data= SimpleDateFormat("dd-MM-yyyy").format(spesa.data)

        val db = writableDatabase
        val values = ContentValues().apply {
            put(SpeseEntry.COLONNA_NOME_SPESA, nome_spesa)
            put(SpeseEntry.COLONNA_USCITA, uscita)
            put(SpeseEntry.COLONNA_DATA, data)
        }
        val newRowId = db.insert(SpeseEntry.TABELLA_NOME, null, values)
        Log.d("SpeseDbHelper", "Record inserito con ID: $newRowId")
        //db.close()
    }

    fun deleteSpesa(spesa:Spesa) {
        val id=spesa.id
        val db = writableDatabase
        db.delete(
            SpeseEntry.TABELLA_NOME,
            "${SpeseEntry.COLONNA_ID} = ?",
            arrayOf(id.toString())
        )
        //db.close()
    }

    // Aggiungi una funzione per modificare i dati di una spesa nel database
    fun updateSpesa(spesa:Spesa) {
        val id=spesa.id
        val nome_spesa=spesa.nome_spesa
        val uscita=spesa.uscita
        val data=SimpleDateFormat("dd-MM-yyyy").format(spesa.data)
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(SpeseEntry.COLONNA_NOME_SPESA, nome_spesa)
            put(SpeseEntry.COLONNA_USCITA, uscita)
            put(SpeseEntry.COLONNA_DATA, data)
        }

        db.update(
            SpeseEntry.TABELLA_NOME,
            contentValues,
            "${SpeseEntry.COLONNA_ID} = ?",
            arrayOf(id.toString())
        )
        //db.close()
    }

    @SuppressLint("Range")
    fun getSpeseForDate(date: String): List<Spesa> {
        val speseList = mutableListOf<Spesa>()
        val db = this.readableDatabase
        val query = "SELECT * FROM ${SpeseEntry.TABELLA_NOME} WHERE ${SpeseEntry.COLONNA_DATA} = ?"
        val cursor: Cursor = db.rawQuery(query,
            arrayOf(date)
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(SpeseEntry.COLONNA_ID))
                val nome_spesa = cursor.getString(cursor.getColumnIndex(SpeseEntry.COLONNA_NOME_SPESA))
                val uscita = cursor.getDouble(cursor.getColumnIndex(SpeseEntry.COLONNA_USCITA))
                speseList.add(Spesa(id, nome_spesa, uscita, SimpleDateFormat("dd-MM-yyyy").parse(date)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        //db.close()
        return speseList
    }


    @SuppressLint("Range")
    fun getSpeseForDateRange(date1: String, date2:String):Double{
        var ret=0.0;
        val db = this.readableDatabase
        val query = "SELECT sum(${SpeseEntry.COLONNA_USCITA}) as uscita FROM ${SpeseEntry.TABELLA_NOME} WHERE ${SpeseEntry.COLONNA_DATA} between ? and ?"
        val cursor: Cursor = db.rawQuery(query,arrayOf(date1,date2))
        if (cursor.moveToFirst()) {
            do {
                val spese=cursor.getDouble(cursor.getColumnIndex("uscita"))
                ret=spese
            }while (cursor.moveToNext())
        }
        cursor.close()
        return ret
    }

    @SuppressLint("Range")
    fun getSpeseForDateGroupByDay(date1: String, date2:String):HashMap<Double,String>{
        val speseListForDay = HashMap<Double,String>()
        val db = this.readableDatabase
        val query = "SELECT sum(${SpeseEntry.COLONNA_USCITA}) as uscita,data FROM ${SpeseEntry.TABELLA_NOME} WHERE ${SpeseEntry.COLONNA_DATA} between ? and ? group by data"
        val cursor: Cursor = db.rawQuery(query,arrayOf(date1,date2))
        if (cursor.moveToFirst()) {
            do {
                val spese=cursor.getDouble(cursor.getColumnIndex("uscita"))
                val data=cursor.getString(cursor.getColumnIndex("data"))
                speseListForDay.put(spese,data)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return speseListForDay
    }

    data class Spesa(var id: Long, var nome_spesa: String, var uscita: Double,var data: Date)

}
