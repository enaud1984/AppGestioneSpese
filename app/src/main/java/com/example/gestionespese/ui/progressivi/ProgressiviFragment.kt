package com.example.gestionespese.ui.progressivi

import PazientiDbHelper
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestioneContabilita.db.SpeseFisseDbHelper
import com.example.gestionespese.databinding.FragmentProgressiviBinding
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ProgressiviViewModel::class.java)

        _binding = FragmentProgressiviBinding.inflate(inflater, container, false)
        val root: View = binding.root

        showProgressivi()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dbHelperPazienti.close()
        dbHelperSpeseFisse.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showProgressivi(){
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val todayString=today.format(formatter)
        val firstDayOfWeek = today.with(DayOfWeek.MONDAY)
        val firstDayOfWeekString = firstDayOfWeek.format(formatter)
        val firstDayOfMonth=today.withDayOfMonth(1)
        val firstDayOfMonthString=firstDayOfMonth.format(formatter)
        val firstDayOfYear=today.withDayOfYear(1)
        val firstDayOfYearString=firstDayOfYear.format(formatter)
        val incassoSettimana=dbHelperPazienti.getIncassoForDate(firstDayOfWeekString,todayString)
        val incassoMese=dbHelperPazienti.getIncassoForDate(firstDayOfMonthString,todayString)
        val incassoAnnuo=dbHelperPazienti.getIncassoForDate(firstDayOfYearString,todayString)

        val speseMensili=dbHelperSpeseFisse.getSumSpeseFisse(SpeseFisseDbHelper.Frequenza.MENSILE)
        val speseAnnue =dbHelperSpeseFisse.getSumSpeseFisse(SpeseFisseDbHelper.Frequenza.ANNUALE)

        val progressivoMese=incassoMese-speseMensili
        val progressivoAnnuo=incassoAnnuo-speseAnnue
        binding.testoUltimaSettimana.text= "${incassoSettimana}€"
        binding.testoUltimoMese.text= "${progressivoMese}€"
        binding.testoUltimoAnno.text= "${progressivoAnnuo}€"
    }
}