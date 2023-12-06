

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.gestionespese.db.Contabilita
import com.example.gestionespese.db.Contabilita.DATABASE_NOME
import com.example.gestionespese.db.Contabilita.DATABASE_VERSIONE
import java.text.SimpleDateFormat
import java.util.*

class PazientiDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NOME, null, DATABASE_VERSIONE) {

    init {
        Log.d("PazientiDbHelper", "Costruttore chiamato")
        Log.d("DB", context.getDatabasePath(DATABASE_NOME).path)
    }

    override fun onCreate(db: SQLiteDatabase) {

        val CREATE_PAZIENTI_TABLE = ("CREATE TABLE ${Contabilita.PazienteEntry.TABELLA_NOME} (" +
                "${Contabilita.PazienteEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${Contabilita.PazienteEntry.COLONNA_NOME} TEXT," +
                "${Contabilita.PazienteEntry.COLONNA_COGNOME} TEXT," +
                "${Contabilita.PazienteEntry.COLONNA_ENTRATA} REAL," +
                "${Contabilita.PazienteEntry.COLONNA_DATA} DATE," +
                "${Contabilita.PazienteEntry.COLONNA_IS_BLACK} INTEGER)")
        db.execSQL(CREATE_PAZIENTI_TABLE)
        Log.d("PazientiDbHelper", " creazione della tabella pazienti")

        var SQL_CREATE_TABELLA =
            "CREATE TABLE ${Contabilita.SpeseEntry.TABELLA_NOME} (" +
                    "${Contabilita.SpeseEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${Contabilita.SpeseEntry.COLONNA_NOME_SPESA} TEXT NOT NULL, " +
                    "${Contabilita.SpeseEntry.COLONNA_USCITA} REAL NOT NULL, " +
                    "${Contabilita.SpeseEntry.COLONNA_DATA} DATE)"
        db.execSQL(SQL_CREATE_TABELLA)

        Log.d("PazientiDbHelper", " creazione della tabella spese")
        SQL_CREATE_TABELLA =
            "CREATE TABLE ${Contabilita.SpeseFissaEntry.TABELLA_NOME} (" +
                    "${Contabilita.SpeseFissaEntry.COLONNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_NOME} TEXT NOT NULL, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_COSTO} REAL NOT NULL, " +
                    "${Contabilita.SpeseFissaEntry.COLONNA_FREQUENZA} TEXT NOT NULL)"
        db.execSQL(SQL_CREATE_TABELLA)

        Log.d("PazientiDbHelper", " creazione della tabella spese Fisse")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Contabilita.PazienteEntry.TABELLA_NOME}")
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
        values.put(Contabilita.PazienteEntry.COLONNA_NOME, nome)
        values.put(Contabilita.PazienteEntry.COLONNA_COGNOME,cognome)
        values.put(Contabilita.PazienteEntry.COLONNA_ENTRATA,entrata)
        values.put(Contabilita.PazienteEntry.COLONNA_IS_BLACK, if (isBlack) 1 else 0)
        values.put(Contabilita.PazienteEntry.COLONNA_DATA, data)

        val ret= db.insert(Contabilita.PazienteEntry.TABELLA_NOME, null, values)
        //db.close()
        return ret
    }

    // Metodo per ottenere tutti i pazienti per una data specifica

    @SuppressLint("Range")
    fun getPazientiForDate(date: String): List<Paziente> {
        val pazientiList = mutableListOf<Paziente>()
        val db = this.readableDatabase
        val query = "SELECT * FROM ${Contabilita.PazienteEntry.TABELLA_NOME} WHERE ${Contabilita.PazienteEntry.COLONNA_DATA} =?"
        val cursor: Cursor = db.rawQuery(query,
            arrayOf(date)
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_ID))
                val nome = cursor.getString(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_NOME))
                val cognome = cursor.getString(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_COGNOME))
                val entrata = cursor.getDouble(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_ENTRATA))
                val isBlack = cursor.getInt(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_IS_BLACK)) == 1
                pazientiList.add(Paziente(id, nome, cognome, entrata, isBlack, SimpleDateFormat("dd-MM-yyyy").parse(date)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        //db.close()
        return pazientiList
    }

    @SuppressLint("Range")
    fun getIncassoForDate(date1: String,date2:String):Double{
        var ret=0.0;
        val db = this.readableDatabase
        val query = "SELECT sum(${Contabilita.PazienteEntry.COLONNA_ENTRATA}) as entrate FROM ${Contabilita.PazienteEntry.TABELLA_NOME} WHERE ${Contabilita.PazienteEntry.COLONNA_DATA} between ? and ?"
        val cursor: Cursor = db.rawQuery(query,arrayOf(date1,date2))
        if (cursor.moveToFirst()) {
            do {
                val entrate=cursor.getDouble(cursor.getColumnIndex("entrate"))
                ret=entrate
               }while (cursor.moveToNext())
            }
        cursor.close()
        return ret
    }

    @SuppressLint("Range")
    fun getAllPazientiDates():List<Date>{
        val pazientiDates = mutableListOf<Date>()
        val db = this.readableDatabase
        val query = "SELECT distinct ${Contabilita.PazienteEntry.COLONNA_DATA} FROM ${Contabilita.PazienteEntry.TABELLA_NOME}"
        val cursor: Cursor = db.rawQuery(query,null)
        if (cursor.moveToFirst()) {
            do {
                val date=cursor.getString(cursor.getColumnIndex(Contabilita.PazienteEntry.COLONNA_DATA))
                val dateT = SimpleDateFormat("dd-MM-yyyy").parse(date)
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
            Contabilita.PazienteEntry.TABELLA_NOME,
            "${Contabilita.PazienteEntry.COLONNA_ID} = ?",
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
            put(Contabilita.PazienteEntry.COLONNA_NOME, nome)
            put(Contabilita.PazienteEntry.COLONNA_COGNOME, cognome)
            put(Contabilita.PazienteEntry.COLONNA_ENTRATA, entrata)
            put(Contabilita.PazienteEntry.COLONNA_IS_BLACK, isBlack)
            put(Contabilita.PazienteEntry.COLONNA_DATA, data)
        }

        db.update(
            Contabilita.PazienteEntry.TABELLA_NOME,
            contentValues,
            "${Contabilita.PazienteEntry.COLONNA_ID} = ?",
            arrayOf(id.toString())
        )
        //db.close()
    }

    data class Paziente(var id: Long, var nome: String, var cognome: String, var entrata: Double, var isBlack: Boolean,var data:Date)
}
