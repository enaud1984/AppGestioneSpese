package com.example.gestionespese.db
// SpeseDbHelper.kt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class SpeseDbHelper(context: Context) :SQLiteOpenHelper(context, Spese.DATABASE_NOME, null, Spese.DATABASE_VERSIONE) {

    init {
        Log.d("SpeseDbHelper", "CREO DATABASE")
        Log.d("DB",context.getDatabasePath("spese.db").path)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Creazione della tabella
        val SQL_CREARE_TABELLA =
            "CREATE TABLE ${Spese.SpesaEntry.TABELLA_NOME} (" +
                    "${Spese.SpesaEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${Spese.SpesaEntry.COLONNA_NOME} TEXT NOT NULL, " +
                    "${Spese.SpesaEntry.COLONNA_COSTO} REAL NOT NULL, " +
                    "${Spese.SpesaEntry.COLONNA_FREQUENZA} TEXT NOT NULL)"
        db?.execSQL(SQL_CREARE_TABELLA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Spese.SpesaEntry.TABELLA_NOME}")
        onCreate(db)
    }

    fun insertSpesa(spesaFissa: SpesaFissa) {
        val spesa= spesaFissa.nome
        val costo=spesaFissa.costo
        val frequenza=spesaFissa.frequenza

        val db = writableDatabase
        val values = ContentValues().apply {
            put(Spese.SpesaEntry.COLONNA_NOME, spesa)
            put(Spese.SpesaEntry.COLONNA_COSTO, costo)
            put(Spese.SpesaEntry.COLONNA_FREQUENZA, frequenza)
        }
        val newRowId = db.insert(Spese.SpesaEntry.TABELLA_NOME, null, values)
        Log.d("SpeseDbHelper", "Record inserito con ID: $newRowId")
        //db.close()
    }
    fun getAllSpeseFisse(): List<SpesaFissa> {
        val speseFisseList = mutableListOf<SpesaFissa>()
        val db = readableDatabase
        val cursor = db.query(
            Spese.SpesaEntry.TABELLA_NOME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val nome = getString(getColumnIndexOrThrow(Spese.SpesaEntry.COLONNA_NOME))
                val costo = getDouble(getColumnIndexOrThrow(Spese.SpesaEntry.COLONNA_COSTO))
                val frequenza = getString(getColumnIndexOrThrow(Spese.SpesaEntry.COLONNA_FREQUENZA))
                val id = getString(getColumnIndexOrThrow(Spese.SpesaEntry.COLONNA_ID)).toLong()
                speseFisseList.add(SpesaFissa(id,nome, costo, frequenza))
            }
        }

        cursor.close()
        //db.close()
        return speseFisseList
    }

    fun deleteSpesaFissa(spesaFissa: SpesaFissa) {
        writableDatabase.use { db ->
            val selection = "${Spese.SpesaEntry.COLONNA_ID} = ?"
            val selectionArgs = arrayOf(spesaFissa.id.toString())
            db.delete(Spese.SpesaEntry.TABELLA_NOME, selection, selectionArgs)
        }
        Log.d("SpeseDbHelper", "Record cancellato con ID: ${spesaFissa.id}")

    }

    // Metodo per aggiornare una spesa fissa nel database
    fun updateSpesaFissa(spesaFissa: SpesaFissa) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(Spese.SpesaEntry.COLONNA_NOME, spesaFissa.nome)
                put(Spese.SpesaEntry.COLONNA_COSTO, spesaFissa.costo)
                put(Spese.SpesaEntry.COLONNA_FREQUENZA, spesaFissa.frequenza)
                // Aggiungi altri campi da aggiornare se necessario
            }

            val selection = "${Spese.SpesaEntry.COLONNA_ID} = ?"
            val selectionArgs = arrayOf(spesaFissa.id.toString())

            db.update(Spese.SpesaEntry.TABELLA_NOME, values, selection, selectionArgs)
        }
    }
    data class SpesaFissa(var id: Long, var nome: String, var costo: Double, var frequenza: String)

}
