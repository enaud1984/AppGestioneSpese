package com.example.gestionespese.ui.home


import PazientiDbHelper
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionespese.R
import com.example.gestionespese.databinding.FragmentHomeBinding
import com.example.gestionespese.db.SpeseDbHelper
import java.text.SimpleDateFormat
import java.util.Calendar


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val dbHelper by lazy { PazientiDbHelper(requireContext()) }
    //private lateinit var pazientiAdapter: PazientiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calendar:CalendarView =binding.calendarView

        //alla partenza punta alla data corrente
        val selectedDateInMillis = calendar.date
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.timeInMillis = selectedDateInMillis

        // Ottieni il mese dalla data selezionata
        val year= selectedCalendar.get(Calendar.YEAR)+1
        val month = selectedCalendar.get(Calendar.MONTH)+1
        val day=selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val formattedDate = formatDate(day, month, year)
        mostraPazientiPerData(formattedDate)

        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Quando si clicca su una data nel CalendarView
            handleDateClick(dayOfMonth, month, year)
        }

        return root
    }


    private fun handleDateClick(dayOfMonth: Int, month: Int, year: Int) {
        val formattedDate = formatDate(dayOfMonth, month, year)
        // Implementa l'azione di tocco
        mostraPazientiPerData(formattedDate)
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

    private fun mostraPazientiPerData(data: String) {
        // Ottieni tutti i pazienti per la data specificata dal database

        val pazientiList = dbHelper.getPazientiForDate(data)

        binding.dataTitle.text="Entrate del $data"
        binding.dataTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        binding.dataTitle.setTypeface(null, Typeface.BOLD)
        binding.dataTitle.setTextColor(Color.BLACK)

        binding.addExpenseButton.setOnClickListener{
            mostraModuloInserimentoPaziente(data)
        }

        val cardContainer = binding.cardContainer
        cardContainer.removeAllViews()
        for (paziente in pazientiList) {
            val cardView = createCardView(paziente)
            cardContainer.addView(cardView)
        }

        /*val space = Space(context)
        space.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.spazio_inferiore)
        )
        cardContainer.addView(space)
        */
        //se clicco appare menu
        if (false) {

            // Crea una Dialog
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_pazienti_per_data)

            // Ottieni riferenze ai bottoni dalla vista della Dialog
            val aggiungiButton: Button = dialog.findViewById(R.id.aggiungiButton)
            val chiudiButton: Button = dialog.findViewById(R.id.chiudiButton)

            // Configura l'azione quando si fa clic su "Aggiungi"
            aggiungiButton.setOnClickListener {
                // Azioni da eseguire quando si fa clic su "Aggiungi"
                // Implementa qui la logica per l'aggiunta di un nuovo paziente
                mostraModuloInserimentoPaziente(data)
                //dialog.dismiss()  // Chiudi la Dialog dopo l'aggiunta
                dialog.dismiss()
            }

            // Configura l'azione quando si fa clic su "Chiudi"
            chiudiButton.setOnClickListener {
                // Azioni da eseguire quando si fa clic su "Chiudi"
                dialog.dismiss()  // Chiudi la Dialog
            }

            // Imposta il testo dei pazienti nella Dialog
            //val pazientiTextView: TextView = dialog.findViewById(R.id.pazientiTextView)
            //pazientiTextView.text = pazientiStringBuilder.toString()

            // Mostra la Dialog
            dialog.show()
        }
    }

    private fun createCardView(paziente:PazientiDbHelper.Paziente): View? {
        val cardView = CardView(requireContext())
        val data=SimpleDateFormat("dd-MM-yyyy").format(paziente.data)
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
        textViewNome.text = "${paziente.nome} ${paziente.cognome}"
        textViewNome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textViewNome.setTextColor(Color.BLACK)

        val textViewEntrata = TextView(requireContext())
        textViewEntrata.text = "${paziente.entrata} €"
        textViewEntrata.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textViewEntrata.setTextColor(Color.BLACK)

        val isBlack=CheckedTextView(requireContext())
        isBlack.setText("Black")
        isBlack.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        isBlack.setTextColor(Color.BLACK)
        isBlack.setChecked(paziente.isBlack)

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
            Toast.makeText(requireContext(), "Modifica: ${paziente.nome}", Toast.LENGTH_SHORT).show()
            mostraModuloInserimentoPaziente(data,paziente)
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
            Toast.makeText(requireContext(), "Cancella: ${paziente.nome}", Toast.LENGTH_SHORT).show()
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Conferma eliminazione")
            alertDialogBuilder.setMessage("Vuoi eliminare la spesa fissa?")
            alertDialogBuilder.setPositiveButton("Si") { _, _ ->
                // Elimina l'elemento dal database
                eliminaPaziente(paziente,data)

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

        cardView.addView(linearLayout)

        return cardView
    }

    private fun inserisciPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente:String) {
        dbHelper.insertPaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo l'inserimento
        mostraPazientiPerData(dataCorrente) // Assumi che dataCorrente sia la data corrente selezionata nel calendario
    }

    private fun modificaPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente:String) {
        dbHelper.updatePaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo la modifica
        mostraPazientiPerData(dataCorrente)
    }

    private fun eliminaPaziente(paziente: PazientiDbHelper.Paziente,dataCorrente: String) {
        dbHelper.deletePaziente(paziente)
        // Aggiorna la visualizzazione dei pazienti dopo l'eliminazione
        mostraPazientiPerData(dataCorrente)
    }

    private fun formatDate(dayOfMonth: Int, month: Int, year: Int): String {
        val monthString = (month + 1).toString().padStart(2, '0') // Aggiunge uno zero iniziale se necessario
        val dayString = dayOfMonth.toString().padStart(2, '0') // Aggiunge uno zero iniziale se necessario
        return "$dayString-$monthString-$year"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private var isLongPress = false
    }
}