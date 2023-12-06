package com.example.gestioneContabilita.db
// SpeseDbHelper.kt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.gestionespese.db.Contabilita


class SpeseFisseDbHelper(context: Context) :SQLiteOpenHelper(context, Contabilita.DATABASE_NOME, null, Contabilita.DATABASE_VERSIONE) {

    init {
        Log.d("SpeseFisseDbHelper", "Costruttore chiamato")
        Log.d("DB", context.getDatabasePath(Contabilita.DATABASE_NOME).path)
    }
    override fun onCreate(db: SQLiteDatabase) {
        Log.d("SpeseFisseDbHelper","CREO TABELLA SPESE_FISSE")
        // Creazione della tabella
        val SQL_CREARE_TABELLA =
            "CREATE TABLE ${Contabilita.SpeseFissaEntry.TABELLA_NOME} (" +
                    "${Contabilita.SpeseFissaEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_NOME} TEXT NOT NULL, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_COSTO} REAL NOT NULL, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA} TEXT NOT NULL)"
        db.execSQL(SQL_CREARE_TABELLA)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Contabilita.SpeseFissaEntry.TABELLA_NOME}")
        onCreate(db)
    }

    fun insertSpesa(spesaFissa: SpesaFissa) {
        val spesa= spesaFissa.nome
        val costo=spesaFissa.costo
        val frequenza=spesaFissa.frequenza

        val db = writableDatabase
        val values = ContentValues().apply {
            put(Contabilita.SpeseFissaEntry.COLONNA_NOME, spesa)
            put(Contabilita.SpeseFissaEntry.COLONNA_COSTO, costo)
            put(Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA, frequenza)
        }
        val newRowId = db.insert(Contabilita.SpeseFissaEntry.TABELLA_NOME, null, values)
        Log.d("SpeseDbHelper", "Record inserito con ID: $newRowId")
        //db.close()
    }
    fun getAllSpeseFisse(): List<SpesaFissa> {
        val speseFisseList = mutableListOf<SpesaFissa>()
        val db = readableDatabase
        val cursor = db.query(
            Contabilita.SpeseFissaEntry.TABELLA_NOME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val nome = getString(getColumnIndexOrThrow(Contabilita.SpeseFissaEntry.COLONNA_NOME))
                val costo = getDouble(getColumnIndexOrThrow(Contabilita.SpeseFissaEntry.COLONNA_COSTO))
                val frequenza = getString(getColumnIndexOrThrow(Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA))
                val id = getString(getColumnIndexOrThrow(Contabilita.SpeseFissaEntry.COLONNA_ID)).toLong()
                speseFisseList.add(SpesaFissa(id,nome, costo, frequenza))
            }
        }

        cursor.close()
        //db.close()
        return speseFisseList
    }
    enum class Frequenza(val nome: String){
        MENSILE("Mensile"),
        ANNUALE("Annuale")
    }
    fun getSumSpeseFisse(frequenza:Frequenza):Double {
        val db = readableDatabase
        var totale=0.0
        val cursor = db.query(
            Contabilita.SpeseFissaEntry.TABELLA_NOME,
            arrayOf("SUM(${Contabilita.SpeseFissaEntry.COLONNA_COSTO}) AS totale"),
            "${Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA} = ?",
            arrayOf(frequenza.nome),null,null,null
        )
        if (cursor.moveToFirst()) {
            if (cursor.getColumnIndex("totale") >= 0) {
                val x=cursor.getColumnIndex("totale")
                totale = cursor.getDouble(x)
            }
        }

        cursor.close()
        return totale
    }

    fun deleteSpesaFissa(spesaFissa: SpesaFissa) {
        writableDatabase.use { db ->
            val selection = "${Contabilita.SpeseFissaEntry.COLONNA_ID} = ?"
            val selectionArgs = arrayOf(spesaFissa.id.toString())
            db.delete(Contabilita.SpeseFissaEntry.TABELLA_NOME, selection, selectionArgs)
        }
        Log.d("SpeseFisseDbHelper", "Record cancellato con ID: ${spesaFissa.id}")

    }

    // Metodo per aggiornare una spesa fissa nel database
    fun updateSpesaFissa(spesaFissa: SpesaFissa) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(Contabilita.SpeseFissaEntry.COLONNA_NOME, spesaFissa.nome)
                put(Contabilita.SpeseFissaEntry.COLONNA_COSTO, spesaFissa.costo)
                put(Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA, spesaFissa.frequenza)
                // Aggiungi altri campi da aggiornare se necessario
            }

            val selection = "${Contabilita.SpeseFissaEntry.COLONNA_ID} = ?"
            val selectionArgs = arrayOf(spesaFissa.id.toString())

            db.update(Contabilita.SpeseFissaEntry.TABELLA_NOME, values, selection, selectionArgs)
        }
    }
    data class SpesaFissa(var id: Long, var nome: String, var costo: Double, var frequenza: String)

}
