package com.example.gestionespese.ui.speseFisse

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.gestioneContabilita.db.SpeseFisseDbHelper
import com.example.gestionespese.R
import com.example.gestionespese.databinding.FragmentSpesefisseBinding


class SpeseFisseFragment : Fragment() {

    private var _binding: FragmentSpesefisseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var speseFisseList = mutableListOf<SpeseFisseDbHelper.SpesaFissa>();
    private val dbHelper by lazy { SpeseFisseDbHelper(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val dashboardViewModel =
        //    ViewModelProvider(this).get(SpeseFisseViewModel::class.java)

        _binding = FragmentSpesefisseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        speseFisseList = dbHelper.getAllSpeseFisse().toMutableList()
        val cardContainer = root.findViewById<LinearLayout>(R.id.cardContainer)
        for (spesaFissa in speseFisseList) {
            val cardView = createCardView(spesaFissa)
            cardContainer.addView(cardView)
        }
        val space = Space(context)
        space.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.spazio_inferiore)
        )
        binding.cardContainer.addView(space)


        val btnAddExpense: ImageButton = root.findViewById(R.id.addExpenseButton)
        btnAddExpense.setOnClickListener{showAddExpenseDialog()}

        return root
    }

    private fun createCardView(spesaFissa: SpeseFisseDbHelper.SpesaFissa): View? {
        val cardView = CardView(requireContext())
        //cardView.setTag(1,spesaFissa.id)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.card_bottom_margin)
        cardView.layoutParams = layoutParams
        cardView.radius = resources.getDimensionPixelSize(R.dimen.card_corner_radius).toFloat()
        cardView.cardElevation = resources.getDimensionPixelSize(R.dimen.card_elevation).toFloat()

        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(
            resources.getDimensionPixelSize(R.dimen.card_padding),
            resources.getDimensionPixelSize(R.dimen.card_padding),
            resources.getDimensionPixelSize(R.dimen.card_padding),
            resources.getDimensionPixelSize(R.dimen.card_padding)
        )

        val textViewNome = TextView(requireContext())
        textViewNome.text = "Spesa: ${spesaFissa.nome}"
        textViewNome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textViewNome.setTextColor(Color.BLACK)

        val textViewCosto = TextView(requireContext())
        textViewCosto.text = "Costo: ${spesaFissa.costo}"
        textViewCosto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textViewCosto.setTextColor(Color.BLACK)

        val textViewFrequenza = TextView(requireContext())
        textViewFrequenza.text = "Frequenza: ${spesaFissa.frequenza}"
        textViewFrequenza.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textViewFrequenza.setTextColor(Color.BLACK)

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
            Toast.makeText(requireContext(), "Modifica: ${spesaFissa.nome}", Toast.LENGTH_SHORT).show()
        }
        editButton.setOnClickListener {
            showAddExpenseDialog(spesaFissa)

        }

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
            Toast.makeText(requireContext(), "Cancella: ${spesaFissa.nome}", Toast.LENGTH_SHORT).show()
        }
        deleteButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Conferma eliminazione")
            alertDialogBuilder.setMessage("Vuoi eliminare la spesa fissa?")
            alertDialogBuilder.setPositiveButton("Si") { _, _ ->
                // Elimina l'elemento dal database
                dbHelper.deleteSpesaFissa(spesaFissa)
                // Aggiorna la lista del CardContainer
                speseFisseList.remove(spesaFissa)
                updateUIWithSpeseFisse(speseFisseList)
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
        relativeLayout2.addView(textViewCosto)
        relativeLayout2.addView(deleteButton)
        val relativeLayout3 = RelativeLayout(requireContext())
        relativeLayout3.addView(textViewFrequenza)

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
        linearLayout.addView(relativeLayout3)

        cardView.addView(linearLayout)

        return cardView
    }

    private fun showAddExpenseDialog(spesaFissa: SpeseFisseDbHelper.SpesaFissa? = null) {
        val inputLayout = LinearLayout(requireContext())
        inputLayout.orientation = LinearLayout.VERTICAL

        val inputName = EditText(requireContext())
        inputName.hint = "Nome Spesa Fissa"

        val inputAmount = EditText(requireContext())
        inputAmount.hint = "Importo in Euro"

        val spinnerFrequency = Spinner(requireContext())
        val frequencyOptions = arrayOf("Mensile", "Annuale")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = adapter

        val space = Space(requireContext())
        space.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.space_height)
        )
        inputLayout.addView(inputName)
        inputLayout.addView(inputAmount)
        inputLayout.addView(spinnerFrequency)
        inputLayout.addView(space)

        var title=""
        if (spesaFissa != null) {
            //EDIT
            title="Modifica Spesa Fissa"
            inputName.setText(spesaFissa.nome)
            inputAmount.setText(spesaFissa.costo.toString())
            val frequenzaIndex = frequencyOptions.indexOf(spesaFissa.frequenza)
            spinnerFrequency.setSelection(if (frequenzaIndex != -1) frequenzaIndex else 0)
        }
        else{
            //INSERT
            title="Aggiungi Spesa Fissa"
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        builder.setView(inputLayout)

        builder.setPositiveButton("OK") { _, _ ->
            val spesa = inputName.text.toString()
            val costo = inputAmount.text.toString().toDoubleOrNull()
            val frequenza=spinnerFrequency.selectedItem.toString()

            if (spesa.isNotEmpty() && costo != null) {
                if (spesaFissa!= null){
                    spesaFissa.nome = spesa
                    spesaFissa.costo = costo
                    spesaFissa.frequenza = frequenza
                    val index = speseFisseList.indexOfFirst { it.id == spesaFissa.id }
                    if (index != -1) {
                        speseFisseList[index] = spesaFissa
                    }
                    dbHelper.updateSpesaFissa(spesaFissa)
                }
                else{
                    val newSpesaFissa = SpeseFisseDbHelper.SpesaFissa(0, spesa, costo, frequenza)
                    speseFisseList.add(newSpesaFissa)

                    dbHelper.insertSpesa(newSpesaFissa)
                }
                updateUIWithSpeseFisse(speseFisseList)

            } else {
                Toast.makeText(requireContext(), "Inserisci un nome e un importo validi", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annulla") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
    private fun updateUIWithSpeseFisse(speseFisseList: MutableList<SpeseFisseDbHelper.SpesaFissa>) {
        val cardContainer=binding.cardContainer
        binding.cardContainer.removeAllViews()
        for (spesaFissa in speseFisseList) {
            val cardView = createCardView(spesaFissa)
            cardContainer.addView(cardView)
        }
        val space = Space(context)
        space.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.spazio_inferiore)
        )

        binding.cardContainer.addView(space)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}