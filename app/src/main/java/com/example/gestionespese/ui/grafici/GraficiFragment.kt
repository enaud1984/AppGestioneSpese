package com.example.gestionespese.ui.grafici

import PazientiDbHelper
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GraficiFragment : Fragment() {

    private var _binding: FragmentGraficiBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var editTextRangeDate: EditText

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
        creaSpinner()
        editTextRangeDate = root.findViewById(R.id.editTextRangeDate)
        editTextRangeDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
        editTextRangeDate.typeface=Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)

        editTextRangeDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.barChart.setNoDataText("")
        return root
    }

    private fun showDatePickerDialog() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            val startDateL = selection.first
            val endDateL = selection.second
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            // Fai qualcosa con le date selezionate
            startDate = Date(startDateL)
            endDate = Date(endDateL)
            editTextRangeDate.setText("${formatter.format(startDate)} - ${formatter.format(endDate)}")
            when
                (binding.spinnerTipoDati.selectedItemPosition){
                    0->getEntrate()
                    1->getSpese()
                }

        }
        picker.show(childFragmentManager, picker.toString())


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
            drawPlot(entrateMap,listDateStrings,"entrate")
    }
    private fun getSpese() {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val start=formatter.format(startDate)
        val end=formatter.format(endDate)
        val listDateStrings=calculateDates()
        val speseMap=dbHelperSpese.getSpeseForDateGroupByDay(start,end)
        if(speseMap.isEmpty())
            return
        else
            drawPlot(speseMap,listDateStrings,"uscite")
    }

    private fun getProgressivi(){
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val start=formatter.format(startDate)
        val end=formatter.format(endDate)
        val listDateStrings=calculateDates()
        val progressiviMap=null
        //val speseMap=dbHelperSpese.getSpeseForDateGroupByDay(start,end)
        TODO("FARE I CALCOLI")
        //if(progressiviMap.isEmpty())
         //   return
        //else
            //drawPlot(progressiviMap,listDateStrings,"progressivi")
    }
    private fun creaSpinner() {
        val spinnerTipoDati = binding.spinnerTipoDati
        val options = arrayOf("Entrate", "Uscite", "Ricavi")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
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
                if (startDate==null || endDate==null)
                    return
                when (position) {
                    0 -> getEntrate()
                    1 -> getSpese()
                    //2 -> getProgressivi()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Fai qualcosa se nessuna opzione è selezionata (se necessario)
            }
        }
    }
    private fun drawPlot(mapValues:HashMap<String,Double>,listDateStrings:List<String>,tipo:String) {
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
        if (tipo=="entrate")
            barDataSet.color= Color.parseColor("#99ffbb")
        if (tipo=="uscite")
            barDataSet.color= Color.parseColor("#ff9999")
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