package com.example.gestionespese.ui.home


import PazientiDbHelper
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.gestionespese.R
import com.example.gestionespese.databinding.FragmentHomeBinding
import com.example.gestionespese.db.SpeseDbHelper
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //private val dbHelperPazienti by lazy { PazientiDbHelper(requireContext()) }
    //private val dbHelperSpesa by lazy { SpeseDbHelper(requireContext()) }
    private lateinit var dbHelperPazienti: PazientiDbHelper
    private lateinit var dbHelperSpesa: SpeseDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dbHelperSpesa=SpeseDbHelper(requireContext())
        dbHelperPazienti=PazientiDbHelper(requireContext())
        //context?.deleteDatabase("contabilita.db")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        evidenziaDate()


        val calendar:MaterialCalendarView =binding.calendarView
        val day=CalendarDay.today()
        calendar.setDateSelected(day,true)
        //alla partenza punta alla data corrente
        /*
        val selectedDateInMillis = calendar.g
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.timeInMillis = selectedDateInMillis

        // Ottieni il mese dalla data selezionata
        val year= selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val day=selectedCalendar.get(Calendar.DAY_OF_MONTH)
        */
        val selectedDate: CalendarDay? = calendar.selectedDate

        if (selectedDate != null) {
            val year: Int = selectedDate.year
            val month: Int = selectedDate.month
            val day: Int = selectedDate.day
            handleDateClick(day, month, year)
        }

        /*calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Quando si clicca su una data nel CalendarView
            handleDateClick(dayOfMonth, month, year)
        }*/
        calendar.setOnDateChangedListener { widget, date, selected -> // Ottieni la data selezionata
            val year = date.year
            val month = date.month
            val day = date.day
            handleDateClick(day, month, year)
        }
        return root
    }

    private fun evidenziaDate(){
        val dateList=dbHelperPazienti.getAllPazientiDates()
        for (date in dateList) {
            val calendar = Calendar.getInstance()
            calendar.time=date
            val day=CalendarDay.from(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            binding.calendarView.addDecorator(SpecificDayDecorator(day, requireContext()))
        }
    }
    private fun handleDateClick(dayOfMonth: Int, month: Int, year: Int) {
        val formattedDate = formatDate(dayOfMonth, month, year)
        // Implementa l'azione di tocco
        mostraContabilitaPerData(formattedDate)
    }

    private fun mostraModuloInserimentoUscite(dataCorrente: String,spesa: SpeseDbHelper.Spesa? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.item_uscite, null)

        val nomeSpesaEditText = dialogView.findViewById<EditText>(R.id.nomeSpesaEditText)
        val uscitaEditText = dialogView.findViewById<EditText>(R.id.uscitaEditText)

        dialogView.findViewById<EditText>(R.id.dataEditText).setText(dataCorrente)

        if (spesa !=null){
            nomeSpesaEditText.setText(spesa.nome_spesa)
            uscitaEditText.setText(spesa.uscita.toString())

            builder.setView(dialogView)
                .setTitle("Modifica Spesa")
                .setPositiveButton("Aggiorna") { _, _ ->
                    val nome_spesa = nomeSpesaEditText.text.toString()
                    val uscita = uscitaEditText.text.toString().toDoubleOrNull() ?: 0.0

                    // Crea un oggetto Paziente con i dati inseriti
                    val spesa = SpeseDbHelper.Spesa(
                        spesa.id,
                        nome_spesa,
                        uscita,
                        SimpleDateFormat("dd-MM-yyyy").parse(dataCorrente)
                    )
                    modificaSpesa(spesa, dataCorrente)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
        else {
            builder.setView(dialogView)
                .setTitle("Inserisci Spesa")
                .setPositiveButton("Salva") { _, _ ->
                    val nome_spesa = nomeSpesaEditText.text.toString()
                    val uscita = uscitaEditText.text.toString().toDoubleOrNull() ?: 0.0

                    // Crea un oggetto Paziente con i dati inseriti
                    val spesa = SpeseDbHelper.Spesa(
                        0,
                        nome_spesa,
                        uscita,
                        SimpleDateFormat("dd-MM-yyyy").parse(dataCorrente)
                    )

                    inserisciSpesa(spesa, dataCorrente)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }

    private fun mostraModuloInserimentoPaziente(dataCorrente: String,paziente: PazientiDbHelper.Paziente? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.item_paziente, null)

        val nomeEditText = dialogView.findViewById<EditText>(R.id.nomeEditText)
        val cognomeEditText = dialogView.findViewById<EditText>(R.id.cognomeEditText)
        val entrataEditText = dialogView.findViewById<EditText>(R.id.entrataEditText)
        val isBlackCheckBox = dialogView.findViewById<CheckBox>(R.id.blackCheckBox)
        dialogView.findViewById<EditText>(R.id.dataEditText).setText(dataCorrente)

        if (paziente !=null){
            nomeEditText.setText(paziente.nome)
            cognomeEditText.setText(paziente.cognome)
            entrataEditText.setText(paziente.entrata.toString())
            isBlackCheckBox.setChecked(paziente.isBlack)
            builder.setView(dialogView)
                .setTitle("Modifica")
                .setPositiveButton("Aggiorna") { _, _ ->
                    val nome = nomeEditText.text.toString()
                    val cognome = cognomeEditText.text.toString()
                    val entrata = entrataEditText.text.toString().toDoubleOrNull() ?: 0.0
                    val isBlack = isBlackCheckBox.isChecked

                    // Crea un oggetto Paziente con i dati inseriti
                    val paziente = PazientiDbHelper.Paziente(
                        paziente.id,
                        nome,
                        cognome,
                        entrata,
                        isBlack,
                        SimpleDateFormat("dd-MM-yyyy").parse(dataCorrente)
                    )

                    // Chiamata alla funzione per inserire il paziente nel database
                    modificaPaziente(paziente, dataCorrente)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
        else {
            builder.setView(dialogView)
                .setTitle("Inserisci")
                .setPositiveButton("Salva") { _, _ ->
                    val nome = nomeEditText.text.toString()
                    val cognome = cognomeEditText.text.toString()
                    val entrata = entrataEditText.text.toString().toDoubleOrNull() ?: 0.0
                    val isBlack = isBlackCheckBox.isChecked

                    // Crea un oggetto Paziente con i dati inseriti
                    val paziente = PazientiDbHelper.Paziente(
                        0,
                        nome,
                        cognome,
                        entrata,
                        isBlack,
                        SimpleDateFormat("dd-MM-yyyy").parse(dataCorrente)
                    )

                    // Chiamata alla funzione per inserire il paziente nel database
                    inserisciPaziente(paziente, dataCorrente)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }

    private fun mostraContabilitaPerData(data: String) {
        // Ottieni tutti i pazienti per la data specificata dal database

        val pazientiList = dbHelperPazienti.getPazientiForDate(data)
        val speseList=dbHelperSpesa.getSpeseForDate(data)

        binding.dataTitle.text="Entrate e Uscite del $data"
        binding.dataTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        binding.dataTitle.setTypeface(null, Typeface.BOLD)
        binding.dataTitle.setTextColor(Color.BLACK)

        binding.addExpenseButton.setOnClickListener{view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_insert, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_entrate -> {
                        mostraModuloInserimentoPaziente(data)
                        true
                    }
                    R.id.menu_uscite -> {
                        mostraModuloInserimentoUscite(data)
                        true
                    }
                    else -> false
                }
            }
            // Mostra il menu popup
            popupMenu.show()
        }

        val cardContainer = binding.cardContainer
        cardContainer.removeAllViews()
        for (paziente in pazientiList) {
            val cardView = createCardView(paziente)
            cardContainer.addView(cardView)
        }
        for (spesa in speseList) {
            val cardView=createCardView(spesa)
            cardContainer.addView(cardView)
        }
    }


    //private fun createCardView(paziente:PazientiDbHelper.Paziente): View? {
    private fun createCardView(item:Any): View? {
        when {
            item is PazientiDbHelper.Paziente -> {
                val cardView = CardView(requireContext())
                val data=SimpleDateFormat("dd-MM-yyyy").format(item.data)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.bottomMargin = 5
                cardView.layoutParams = layoutParams
                cardView.radius = resources.getDimensionPixelSize(R.dimen.card_corner_radius).toFloat()
                cardView.cardElevation = 10F

                val linearLayout = LinearLayout(requireContext())
                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.setPadding(
                    16,
                    5,
                    16,
                    5
                )

                val textViewNome = TextView(requireContext())
                textViewNome.text = "${item.nome} ${item.cognome}"
                textViewNome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                textViewNome.setTextColor(Color.BLACK)

                val textViewEntrata = TextView(requireContext())
                textViewEntrata.text = "+ ${item.entrata} €"
                textViewEntrata.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                textViewEntrata.setTextColor(Color.BLACK)

                val isBlack=CheckedTextView(requireContext())
                isBlack.setText("Black")
                isBlack.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                isBlack.setTextColor(Color.BLACK)
                isBlack.setChecked(item.isBlack)

                //gestione edit
                val editButton = ImageButton(requireContext())
                val editButtonLayoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                editButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                editButtonLayoutParams.addRule(RelativeLayout.ALIGN_TOP, textViewNome.id)
                editButton.layoutParams = editButtonLayoutParams
                editButton.setImageResource(R.drawable.ic_edit)
                editButton.setBackgroundResource(android.R.color.transparent)

                editButton.setOnClickListener {
                    Toast.makeText(requireContext(), "Modifica: ${item.nome}", Toast.LENGTH_SHORT).show()
                    mostraModuloInserimentoPaziente(data,item)
                }

                //gestione delete
                val deleteButton = ImageButton(requireContext())
                val deleteButtonLayoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                deleteButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                deleteButtonLayoutParams.addRule(RelativeLayout.BELOW, editButton.id)
                deleteButton.layoutParams = deleteButtonLayoutParams
                deleteButton.setImageResource(R.drawable.ic_delete)
                deleteButton.setBackgroundResource(android.R.color.transparent)

                deleteButton.setOnClickListener {
                    Toast.makeText(requireContext(), "Cancella: ${item.nome}", Toast.LENGTH_SHORT).show()
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder.setTitle("Conferma eliminazione")
                    alertDialogBuilder.setMessage("Vuoi eliminare il paziente?")
                    alertDialogBuilder.setPositiveButton("Si") { _, _ ->
                        // Elimina l'elemento dal database
                        eliminaPaziente(item,data)

                        // Aggiorna la lista del CardContainer
                        //speseFisseList.remove(spesaFissa)
                        //updateUIWithSpeseFisse(speseFisseList)

                    }
                    alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }


                val relativeLayout = RelativeLayout(requireContext())
                relativeLayout.addView(textViewNome)
                relativeLayout.addView(editButton)

                val relativeLayout2 = RelativeLayout(requireContext())
                relativeLayout2.addView(textViewEntrata)
                relativeLayout2.addView(deleteButton)

                val relativeLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Imposta la distanza tra i layout
                val margin = resources.getDimensionPixelSize(R.dimen.layout_margin_bottom)
                relativeLayoutParams.setMargins(0, 0, 0, margin)
                relativeLayout.layoutParams = relativeLayoutParams
                relativeLayout2.layoutParams = relativeLayoutParams
                editButton.setPadding(0, 0, 0, 0)
                deleteButton.setPadding(0, 0, 0, 0)

                linearLayout.addView(relativeLayout)
                linearLayout.addView(relativeLayout2)

                cardView.setCardBackgroundColor(Color.parseColor("#99ffbb"))
                cardView.addView(linearLayout)

                return cardView
            }
            item is SpeseDbHelper.Spesa ->{
                val cardView = CardView(requireContext())
                val data=SimpleDateFormat("dd-MM-yyyy").format(item.data)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.bottomMargin = 5
                cardView.layoutParams = layoutParams
                cardView.radius = resources.getDimensionPixelSize(R.dimen.card_corner_radius).toFloat()
                cardView.cardElevation = 10F

                val linearLayout = LinearLayout(requireContext())
                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.setPadding(
                    16,
                    5,
                    16,
                    5
                )

                val textViewNomeSpesa = TextView(requireContext())
                textViewNomeSpesa.text = "${item.nome_spesa}"
                textViewNomeSpesa.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                textViewNomeSpesa.setTextColor(Color.BLACK)

                val textViewUscita = TextView(requireContext())
                textViewUscita.text = "- ${item.uscita} €"
                textViewUscita.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                textViewUscita.setTextColor(Color.BLACK)

                //gestione edit
                val editButton = ImageButton(requireContext())
                val editButtonLayoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                editButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                editButtonLayoutParams.addRule(RelativeLayout.ALIGN_TOP, textViewNomeSpesa.id)
                editButton.layoutParams = editButtonLayoutParams
                editButton.setImageResource(R.drawable.ic_edit)
                editButton.setBackgroundResource(android.R.color.transparent)

                editButton.setOnClickListener {
                    Toast.makeText(requireContext(), "Modifica: ${item.nome_spesa}", Toast.LENGTH_SHORT).show()
                    mostraModuloInserimentoUscite(data,item)
                }

                //gestione delete
                val deleteButton = ImageButton(requireContext())
                val deleteButtonLayoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                deleteButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                deleteButtonLayoutParams.addRule(RelativeLayout.BELOW, editButton.id)
                deleteButton.layoutParams = deleteButtonLayoutParams
                deleteButton.setImageResource(R.drawable.ic_delete)
                deleteButton.setBackgroundResource(android.R.color.transparent)

                deleteButton.setOnClickListener {
                    Toast.makeText(requireContext(), "Cancella: ${item.nome_spesa}", Toast.LENGTH_SHORT).show()
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder.setTitle("Conferma eliminazione")
                    alertDialogBuilder.setMessage("Vuoi eliminare la spesa?")
                    alertDialogBuilder.setPositiveButton("Si") { _, _ ->
                        // Elimina l'elemento dal database
                        eliminaSpesa(item,data)
                    }
                    alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

                val relativeLayout = RelativeLayout(requireContext())
                relativeLayout.addView(textViewNomeSpesa)
                relativeLayout.addView(editButton)

                val relativeLayout2 = RelativeLayout(requireContext())
                relativeLayout2.addView(textViewUscita)
                relativeLayout2.addView(deleteButton)

                val relativeLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Imposta la distanza tra i layout
                val margin = resources.getDimensionPixelSize(R.dimen.layout_margin_bottom)
                relativeLayoutParams.setMargins(0, 0, 0, margin)
                relativeLayout.layoutParams = relativeLayoutParams
                relativeLayout2.layoutParams = relativeLayoutParams
                editButton.setPadding(0, 0, 0, 0)
                deleteButton.setPadding(0, 0, 0, 0)

                linearLayout.addView(relativeLayout)
                linearLayout.addView(relativeLayout2)

                cardView.addView(linearLayout)
                cardView.setCardBackgroundColor(Color.parseColor("#ff9999"))
                return cardView

            }
            else -> throw IllegalArgumentException("Tipo di elemento non valido")

        }
    }

    private fun inserisciPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente:String) {
        dbHelperPazienti.insertPaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo l'inserimento
        mostraContabilitaPerData(dataCorrente) // Assumi che dataCorrente sia la data corrente selezionata nel calendario
    }
    private fun inserisciSpesa(spesa: SpeseDbHelper.Spesa,dataCorrente:String) {
        dbHelperSpesa.insertSpesa(spesa)
        // Aggiorna la visualizzazione dei pazienti dopo l'inserimento
        mostraContabilitaPerData(dataCorrente) // Assumi che dataCorrente sia la data corrente selezionata nel calendario
    }

    private fun modificaPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente:String) {
        dbHelperPazienti.updatePaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo la modifica
        mostraContabilitaPerData(dataCorrente)
    }
    private fun modificaSpesa(spesa: SpeseDbHelper.Spesa,dataCorrente:String) {
        dbHelperSpesa.updateSpesa(spesa)
        // Aggiorna la visualizzazione dei pazienti dopo la modifica
        mostraContabilitaPerData(dataCorrente)
    }

    private fun eliminaPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente: String) {
        dbHelperPazienti.deletePaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo l'eliminazione
        mostraContabilitaPerData(dataCorrente)
    }
    private fun eliminaSpesa(spesa: SpeseDbHelper.Spesa,dataCorrente: String) {
        dbHelperSpesa.deleteSpesa(spesa)
        // Aggiorna la visualizzazione dei pazienti dopo l'eliminazione
        mostraContabilitaPerData(dataCorrente)
    }

    private fun formatDate(dayOfMonth: Int, month: Int, year: Int): String {
        val monthString = (month).toString().padStart(2, '0') // Aggiunge uno zero iniziale se necessario
        val dayString = dayOfMonth.toString().padStart(2, '0') // Aggiunge uno zero iniziale se necessario
        return "$dayString-$monthString-$year"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelperSpesa.close()
        dbHelperPazienti.close()
        _binding = null
    }
    companion object {
        private var isLongPress = false
    }
}