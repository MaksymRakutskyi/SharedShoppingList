package com.example.sharedshoppinglist.fragments

import android.app.AlertDialog
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.sharedshoppinglist.MainActivity
import com.example.sharedshoppinglist.R
import com.example.sharedshoppinglist.databinding.FragmentSecondBinding
import com.example.sharedshoppinglist.models.ShoppingItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlin.reflect.typeOf

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 * (private val item: ShoppingItem)
 */
class SecondFragment : Fragment() {
    private lateinit var _firebaseRef : DatabaseReference
    private var _shoppingItem: ShoppingItem = ShoppingItem()
    private var _binding: FragmentSecondBinding? = null
    private var editMode = false


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        _firebaseRef = FirebaseDatabase.getInstance().getReference("Items")

        val itemJson = arguments?.getString("item")
        if (!itemJson.isNullOrEmpty()) {
            val item = Gson().fromJson(itemJson, ShoppingItem::class.java)
            if (item != null) {
                _shoppingItem = item
                _binding?.buttonDelete?.visibility = View.VISIBLE
                editMode = true
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.productName.setText(_shoppingItem.name)
        binding.productQuantity.setText(
            if (!editMode && _shoppingItem.quantity == 0.0) ""
            else _shoppingItem.quantity.toString())
        binding.productMarked.isChecked = _shoppingItem.isMarked

        binding.buttonSave.setOnClickListener {
            var name : String = _binding?.productName?.text.toString()
            var quantity : String = _binding?.productQuantity?.text.toString()
            var isMarked : Boolean? = _binding?.productMarked?.isChecked

            if (!name.isNullOrEmpty() && !quantity.isNullOrEmpty() && isMarked != null) {
                _shoppingItem.name = name
                _shoppingItem.quantity = quantity.toDouble()
                _shoppingItem.isMarked = isMarked

                _firebaseRef.child(_shoppingItem.id).setValue(_shoppingItem).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this.context, "Успішно збережено!", Toast.LENGTH_LONG).show()
                    }
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
            else {
                Toast.makeText(this.context, "Заповніть усі поля вводу!", Toast.LENGTH_LONG).show()
            }
        }

        if (editMode) {
            binding.buttonDelete.setOnClickListener {
                var builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder
                    .setMessage("Ви впевнені що хочете видалити товар \"${_shoppingItem.name}\"?")
                    .setPositiveButton("Видалити") { _, _ ->
                        _firebaseRef.child(_shoppingItem.id).removeValue().addOnCompleteListener {
                            Toast.makeText(this.context, "Успішно видалено!", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        }
                    }
                    .setNegativeButton("Скасувати") { _, _ ->
                    }
                var dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
}