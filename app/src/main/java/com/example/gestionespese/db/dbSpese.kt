package com.example.gestionespese.db

object Spese {
    // Definizione delle costanti del database
    const val DATABASE_NOME = "spese.db"
    const val DATABASE_VERSIONE = 2

    object SpesaEntry {
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
}