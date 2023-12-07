package com.example.gestionespese.ui.grafici

import PazientiDbHelper
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestionespese.R
import com.example.gestionespese.databinding.FragmentGraficiBinding
import com.example.gestionespese.db.SpeseDbHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GraficiFragment : Fragment() {

    private var _binding: FragmentGraficiBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private lateinit var dbHelperPazienti: PazientiDbHelper
    private lateinit var dbHelperSpese: SpeseDbHelper

    private var startDate:Date = Calendar.getInstance().time
    private var endDate:Date = Calendar.getInstance().time
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dbHelperPazienti=PazientiDbHelper(requireContext())
        dbHelperSpese= SpeseDbHelper(requireContext())
        _binding = FragmentGraficiBinding.inflate(inflater, container, false)
        val root: View = binding.root
        editTextStartDate = root.findViewById(R.id.editTextStartDate)
        editTextEndDate = root.findViewById(R.id.editTextEndDate)
        editTextStartDate.setOnClickListener {
            showDatePickerDialog(editTextStartDate)
        }

        editTextEndDate.setOnClickListener {
            onEndDateClick()
        }

        binding.barChart.setNoDataText("")
        return root
    }

    private fun onEndDateClick() {
        // Mostra il DatePickerDialog solo se la data di inizio è stata impostata
        val startDateText = editTextStartDate.text.toString()
        if (startDateText.isNotEmpty()) {
            showDatePickerDialog(editTextEndDate)
        } else {
            // Messaggio di avviso o altra gestione nel caso in cui la data di inizio non sia stata impostata
            Toast.makeText(requireContext(),
                "Imposta prima la data di inizio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Verifica che la data di fine sia maggiore o uguale a quella di inizio
                if (editText.id==binding.editTextEndDate.id){
                    startDate = parseDate(editTextStartDate.text.toString())
                    endDate = parseDate("$selectedDay-${selectedMonth + 1}-$selectedYear")

                    if (endDate >= startDate) {
                        // Data di fine valida, aggiorna il testo dell'EditText
                        val formattedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                        editText.setText(formattedDate)
                        getEntrate()
                    } else {
                        // Messaggio di avviso o altra gestione nel caso in cui la data di fine sia precedente a quella di inizio
                        Toast.makeText(
                            requireContext(),
                            "La data di fine deve essere maggiore " +
                                    "o uguale a quella di inizio",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else if(editText.id==binding.editTextStartDate.id){
                    val formattedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                    editText.setText(formattedDate)
                }},
                year,
                month,
                day
        )
        datePickerDialog.show()
    }

    private fun calculateDates():List<String>{
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        while (calendar.time.before(endDate) || calendar.time == endDate) {
            dateList.add(calendar.time)
            calendar.add(Calendar.DATE, 1)
        }
        val listDateStrings = dateList.map { formatter.format(it) }
        return listDateStrings
    }
    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
    fun getEntrate(){
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val start=formatter.format(startDate)
        val end=formatter.format(endDate)
        val listDateStrings=calculateDates()

        val entrateMap=dbHelperPazienti.getIncassoForDateGroupByDay(start,end,)
        if(entrateMap.isEmpty())
            return
        else
            drawPlot(entrateMap,listDateStrings)
    }

    private fun getSpese() {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val start=formatter.format(startDate)
        val end=formatter.format(endDate)

        val speseMap=dbHelperSpese.getSpeseForDateGroupByDay(start,end)
        //drawPlot(speseMap,listDateStrings)
    }

    private fun drawPlot(mapValues:HashMap<String,Double>,listDateStrings:List<String>) {
        val barChart: BarChart = binding.barChart
        val mapFact=HashMap<String, Double>()
        val listKeys=mapValues.keys
        for (date in listDateStrings){
            if (!listKeys.contains(date))
            {
                mapFact.put(date, 0.0)
            }
            else{
                mapFact.put(date,mapValues.get(date)!!)
            }
        }

        val entries = mapFact.entries
            .sortedBy { it.key }  // Ordina le voci in base alla chiave (String)
            .mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }

        // Creazione di un set di dati per il grafico a barre
        val barDataSet = BarDataSet(entries, "Valori")
        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        // Creazione di BarData e associandola al grafico
        val barData = BarData(barDataSet)
        barChart.data = barData


        val labels = listDateStrings//mapValues.values.toTypedArray()
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels) // dateStrings è la lista di date convertite in stringhe
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        barChart.setDrawGridBackground(false)
        barChart.setFitBars(false)

        barChart.legend.isEnabled=false
        barChart.setPinchZoom(false);
        barChart.description.isEnabled = false
        //barChart.setFitBars(true)
        barChart.animateY(1500)
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}