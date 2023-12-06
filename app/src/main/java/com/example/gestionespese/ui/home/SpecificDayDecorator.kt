package com.example.gestionespese.ui.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.gestionespese.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SpecificDayDecorator(private val specificDay: CalendarDay,
                           private val context: Context
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == specificDay
    }

    override fun decorate(view: DayViewFacade) {
        val drawable: Drawable? = ContextCompat.getDrawable(context,
            R.drawable.day_background_evidenziato)
        if (drawable != null) {
            view.setBackgroundDrawable(drawable)
        } else {
            // Fai qualcosa se la risorsa è nulla, ad esempio, gestisci l'errore o fornisci un fallback
        }
        view.setSelectionDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}