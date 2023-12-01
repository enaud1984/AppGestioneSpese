import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.gestionespese.db.Spese
import java.text.SimpleDateFormat

import java.util.*

class PazientiDbHelper(context: Context) : SQLiteOpenHelper(context, Spese.DATABASE_NOME, null, Spese.DATABASE_VERSIONE) {

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("PazientiDbHelper", "onCreate chiamato, creazione della tabella pazienti")
        val CREATE_PAZIENTI_TABLE = ("CREATE TABLE ${Spese.PazienteEntry.TABELLA_NOME} (" +
                "${Spese.PazienteEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${Spese.PazienteEntry.COLONNA_NOME} TEXT," +
                "${Spese.PazienteEntry.COLONNA_COGNOME} TEXT," +
                "${Spese.PazienteEntry.COLONNA_ENTRATA} REAL," +
                "${Spese.PazienteEntry.COLONNA_DATA} DATE," +
                "${Spese.PazienteEntry.COLONNA_IS_BLACK} INTEGER)")
        db.execSQL(CREATE_PAZIENTI_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Spese.PazienteEntry.TABELLA_NOME}")
        onCreate(db)
    }

    // Metodo per aggiungere un nuovo paziente al database

    fun insertPaziente(paziente: Paziente): Long {
        val nome=paziente.nome
        val cognome=paziente.cognome
        val entrata=paziente.entrata
        val isBlack=paziente.isBlack
        val data=SimpleDateFormat("dd-MM-yyyy").format(paziente.data)
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Spese.PazienteEntry.COLONNA_NOME, nome)
        values.put(Spese.PazienteEntry.COLONNA_COGNOME,cognome)
        values.put(Spese.PazienteEntry.COLONNA_ENTRATA,entrata)
        values.put(Spese.PazienteEntry.COLONNA_IS_BLACK, if (isBlack) 1 else 0)
        values.put(Spese.PazienteEntry.COLONNA_DATA, data)

        val ret= db.insert(Spese.PazienteEntry.TABELLA_NOME, null, values)
        //db.close()
        return ret
    }

    // Metodo per ottenere tutti i pazienti per una data specifica

    @SuppressLint("Range")
    fun getPazientiForDate(date: String): List<Paziente> {
        val pazientiList = mutableListOf<Paziente>()
        val db = this.readableDatabase
        val query = "SELECT * FROM ${Spese.PazienteEntry.TABELLA_NOME} WHERE ${Spese.PazienteEntry.COLONNA_DATA} =?"
        val cursor: Cursor = db.rawQuery(query,
            arrayOf(date)
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_ID))
                val nome = cursor.getString(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_NOME))
                val cognome = cursor.getString(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_COGNOME))
                val entrata = cursor.getDouble(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_ENTRATA))
                val isBlack = cursor.getInt(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_IS_BLACK)) == 1
                pazientiList.add(Paziente(id, nome, cognome, entrata, isBlack, SimpleDateFormat("dd-MM-yyyy").parse(date)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        //db.close()
        return pazientiList
    }
    @SuppressLint("Range")
    fun getAllPazientiDates():List<Date>{
        val pazientiDates = mutableListOf<Date>()
        val db = this.readableDatabase
        val query = "SELECT distinct ${Spese.PazienteEntry.COLONNA_DATA} FROM ${Spese.PazienteEntry.TABELLA_NOME}"
        val cursor: Cursor = db.rawQuery(query,null)
        if (cursor.moveToFirst()) {
            do {
                val date=cursor.getLong(cursor.getColumnIndex(Spese.PazienteEntry.COLONNA_DATA))
                val dateT = Date(date)
                pazientiDates.add(dateT)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return pazientiDates
    }
    fun deletePaziente(paziente:Paziente) {
        val id=paziente.id
        val db = writableDatabase
        db.delete(
            Spese.PazienteEntry.TABELLA_NOME,
            "${Spese.PazienteEntry.COLONNA_ID} = ?",
            arrayOf(id.toString())
        )
        //db.close()
    }

    // Aggiungi una funzione per modificare i dati di un paziente nel database
    fun updatePaziente(paziente: Paziente) {
        val id=paziente.id
        val nome=paziente.nome
        val cognome=paziente.cognome
        val entrata=paziente.entrata
        val isBlack=paziente.isBlack
        val data=SimpleDateFormat("dd-MM-yyyy").format(paziente.data)
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(Spese.PazienteEntry.COLONNA_NOME, nome)
            put(Spese.PazienteEntry.COLONNA_COGNOME, cognome)
            put(Spese.PazienteEntry.COLONNA_ENTRATA, entrata)
            put(Spese.PazienteEntry.COLONNA_IS_BLACK, isBlack)
            put(Spese.PazienteEntry.COLONNA_DATA, data)
        }

        db.update(
            Spese.PazienteEntry.TABELLA_NOME,
            contentValues,
            "${Spese.PazienteEntry.COLONNA_ID} = ?",
            arrayOf(id.toString())
        )
        //db.close()
    }

    data class Paziente(var id: Long, var nome: String, var cognome: String, var entrata: Double, var isBlack: Boolean,var data:Date)
}
