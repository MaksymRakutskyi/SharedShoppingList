package com.example.sharedshoppinglist.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharedshoppinglist.R
import com.example.sharedshoppinglist.adapters.ShoppingListAdapter
import com.example.sharedshoppinglist.databinding.FragmentFirstBinding
import com.example.sharedshoppinglist.models.ShoppingItem
import com.google.firebase.database.*
import com.google.firebase.database.ktx.childEvents
import com.google.gson.Gson

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private lateinit var _firebaseRef : DatabaseReference
    private var _products : MutableList<ShoppingItem> = mutableListOf()
    private var _adapter = ShoppingListAdapter(::onItemClick, ::onCheckedChange)
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        _binding!!.shoppingList.layoutManager = LinearLayoutManager(this.activity)
        _binding!!.shoppingList.adapter = _adapter

        _firebaseRef = FirebaseDatabase.getInstance().getReference("Items")
        _firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _products.clear()
                if (snapshot.exists()) {
                    for (itemSnap in snapshot.children) {
                        val itemData = itemSnap.getValue(ShoppingItem::class.java)
                        if (itemData != null) {
                            _products.add(itemData)
                        }
                    }
                }
                _adapter.setNewData(_products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_refresh -> refreshList()
                    R.id.action_crate_demo_list -> createDemoList()
                    R.id.action_mark_all -> markAll()
                    R.id.action_delete -> deleteAll()
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: ShoppingItem) {
        val bundle = Bundle()
        val itemJson = Gson().toJson(item)
        bundle.putString("item", itemJson)
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
    }

    private fun onCheckedChange(item: ShoppingItem) {
        _firebaseRef.child(item.id).setValue(item)
    }

    fun refreshList(): Boolean {
        _firebaseRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                var snapshot = it.result
                if (snapshot != null) {
                    _products.clear()
                    if (snapshot.exists()) {
                        for (itemSnap in snapshot.children) {
                            var itemData = itemSnap.getValue(ShoppingItem::class.java)
                            if (itemData != null) {
                                _products.add(itemData)
                            }
                        }
                    }
                    _adapter.setNewData(_products)
                    Toast.makeText(this.context, "Список оновлено!", Toast.LENGTH_LONG).show()
                }
            }
        }
        return true
    }

    fun createDemoList(): Boolean {
        if (_products.isNotEmpty())
        {
            Toast.makeText(this.context, "У вас уже є список покупок!", Toast.LENGTH_LONG).show()
            return false
        }

        var demoProducts = mutableListOf(
            ShoppingItem(name = "Milk", quantity = 1.0),
            ShoppingItem(name = "Bread", quantity = 2.0, isMarked = true),
            ShoppingItem(name = "Eggs", quantity = 12.0),
            ShoppingItem(name = "Apples", quantity = 3.0),
            ShoppingItem(name = "Butter", quantity = 1.0, isMarked = true),
            ShoppingItem(name = "Cheese", quantity = 1.5),
            ShoppingItem(name = "Chicken", quantity = 2.5),
            ShoppingItem(name = "Tomatoes", quantity = 5.0, isMarked = true),
            ShoppingItem(name = "Potatoes", quantity = 2.0),
            ShoppingItem(name = "Onions", quantity = 1.8),
            ShoppingItem(name = "Bananas", quantity = 6.0, isMarked = true),
            ShoppingItem(name = "Orange Juice", quantity = 2.0),
            ShoppingItem(name = "Coffee", quantity = 0.5),
            ShoppingItem(name = "Tea", quantity = 1.2),
            ShoppingItem(name = "Salmon", quantity = 0.8),
            ShoppingItem(name = "Pasta", quantity = 1.0),
            ShoppingItem(name = "Yogurt", quantity = 4.0),
            ShoppingItem(name = "Cereal", quantity = 2.2, isMarked = true),
            ShoppingItem(name = "Ice Cream", quantity = 3.5)
        )

        for(product in demoProducts) {
            _firebaseRef.child(product.id).setValue(product)
        }

        return true
    }

    fun markAll(): Boolean {
        for (product in _products) {
            if (!product.isMarked){
                product.isMarked = true
                _firebaseRef.child(product.id).setValue(product)
            }
        }
        return true
    }

    fun deleteAll(): Boolean {
        if (_products.all { p -> !p.isMarked}) {
            Toast.makeText(this.context, "Немає відмічених товарів", Toast.LENGTH_LONG).show()
            return false
        }

        var builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("Ви впевнені що хочете видалити всі відмічені товари?")
            .setPositiveButton("Видалити") { _, _ ->
                var products : MutableList<ShoppingItem> = mutableListOf()
                products.addAll(_products)
                for (product in products) {
                    if (product.isMarked) {
                        _firebaseRef.child(product.id).removeValue()
                    }
                }
            }
            .setNegativeButton("Скасувати") { _, _ ->
            }

        var dialog: AlertDialog = builder.create()
        dialog.show()

        return true
    }
}