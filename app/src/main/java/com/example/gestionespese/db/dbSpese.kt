package com.example.gestionespese.db

object Contabilita {
    // Definizione delle costanti del database
    const val DATABASE_NOME = "contabilita.db"
    const val DATABASE_VERSIONE = 2

    object SpeseFissaEntry {
        const val TABELLA_NOME = "spese_fisse"
        const val COLONNA_ID = "_id"
        const val COLONNA_NOME = "nome"
        const val COLONNA_COSTO = "costo"
        const val COLONNA_FREQUENZA = "frequenza"
    }
    object PazienteEntry {
        const val TABELLA_NOME = "pazienti"
        const val COLONNA_ID = "_id"
        const val COLONNA_NOME = "nome"
        const val COLONNA_COGNOME = "cognome"
        const val COLONNA_ENTRATA = "entrata"
        const val COLONNA_DATA = "data"
        const val COLONNA_IS_BLACK  = "is_black"
    }
    object SpeseEntry {
        const val TABELLA_NOME = "spese"
        const val COLONNA_ID = "_id"
        const val COLONNA_NOME_SPESA = "nome_spesa"
        const val COLONNA_USCITA = "uscita"
        const val COLONNA_DATA = "data"
    }
}