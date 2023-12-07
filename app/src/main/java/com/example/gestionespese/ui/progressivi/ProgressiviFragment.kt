package com.example.gestionespese.ui.progressivi

import PazientiDbHelper
import android.R
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.gestioneContabilita.db.SpeseFisseDbHelper
import com.example.gestionespese.databinding.FragmentProgressiviBinding
import com.example.gestionespese.db.SpeseDbHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProgressiviFragment : Fragment() {

    private var _binding: FragmentProgressiviBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val dbHelperPazienti by lazy { PazientiDbHelper(requireContext()) }
    val dbHelperSpeseFisse by lazy { SpeseFisseDbHelper(requireContext()) }
    val dbHelperSpese by lazy{SpeseDbHelper(requireContext())}

    var entrateSettimana:Double = 0.0
    var entrateMese:Double = 0.0
    var entrateAnno:Double = 0.0

    var speseSettimana:Double=0.0
    var speseMese:Double=0.0
    var speseAnno:Double=0.0

    var speseMensiliFisse:Double=0.0
    var speseAnnueFisse:Double=0.0

    var progressivoSettimana:Double=0.0
    var progressivoMese:Double=0.0
    var progressivoAnno:Double=0.0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProgressiviBinding.inflate(inflater, container, false)
        val root: View = binding.root

        creaSpinner()
        calcola()
        showProgressivi()


        //showEntrate()
        //showSpese()
        return root
    }

    private fun creaSpinner() {
        val spinnerTipoDati = binding.spinnerTipoDati
        val options = arrayOf("Entrate", "Uscite", "Progressivi")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoDati.adapter = adapter
        spinnerTipoDati.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NewApi")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                when (position) {
                    0 -> showEntrate()
                    1 -> showSpese()
                    2 -> showProgressivi()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Fai qualcosa se nessuna opzione è selezionata (se necessario)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dbHelperPazienti.close()
        dbHelperSpeseFisse.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcola(){
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val todayString=today.format(formatter)
        val firstDayOfWeek = today.with(DayOfWeek.MONDAY)
        val firstDayOfWeekString = firstDayOfWeek.format(formatter)
        val firstDayOfMonth=today.withDayOfMonth(1)
        val firstDayOfMonthString=firstDayOfMonth.format(formatter)
        val firstDayOfYear=today.withDayOfYear(1)
        val firstDayOfYearString=firstDayOfYear.format(formatter)

        entrateSettimana=dbHelperPazienti.getIncassoForDate(firstDayOfWeekString,todayString)
        entrateMese=dbHelperPazienti.getIncassoForDate(firstDayOfMonthString,todayString)
        entrateAnno=dbHelperPazienti.getIncassoForDate(firstDayOfYearString,todayString)

        speseSettimana=dbHelperSpese.getSpeseForDateRange(firstDayOfWeekString,todayString)
        speseMese=dbHelperSpese.getSpeseForDateRange(firstDayOfMonthString,todayString)
        speseAnno=dbHelperSpese.getSpeseForDateRange(firstDayOfYearString,todayString)

        speseMensiliFisse=dbHelperSpeseFisse.getSumSpeseFisse(SpeseFisseDbHelper.Frequenza.MENSILE)
        speseAnnueFisse =dbHelperSpeseFisse.getSumSpeseFisse(SpeseFisseDbHelper.Frequenza.ANNUALE)

        progressivoSettimana=entrateSettimana-speseSettimana
        progressivoMese=entrateMese-speseMese-speseMensiliFisse
        progressivoAnno=entrateAnno-speseAnno-speseAnnueFisse
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun showProgressivi(){

        binding.testoUltimaSettimana.text= "${progressivoSettimana}€"
        binding.testoUltimoMese.text= "${progressivoMese}€"
        binding.testoUltimoAnno.text= "${progressivoAnno}€"
    }
    fun showEntrate(){
        binding.testoUltimaSettimana.text= "${entrateSettimana}€"
        binding.testoUltimoMese.text= "${entrateMese}€"
        binding.testoUltimoAnno.text= "${entrateAnno}€"
    }

    private fun showSpese() {
        binding.testoUltimaSettimana.text= "${speseSettimana}€"
        binding.testoUltimoMese.text= "${speseMese}€"
        binding.testoUltimoAnno.text= "${speseAnno}€"
    }
}