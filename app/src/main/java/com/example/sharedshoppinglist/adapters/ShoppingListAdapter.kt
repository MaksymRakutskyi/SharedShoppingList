package com.example.sharedshoppinglist.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedshoppinglist.R
import com.example.sharedshoppinglist.models.ShoppingItem

class ShoppingListAdapter(private val onItemClickFun : (ShoppingItem) -> Unit,
    private val onCheckedClickFun : (ShoppingItem) -> Unit) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder>() {

    private var _list: MutableList<ShoppingItem> = mutableListOf()

    override fun onBindViewHolder(holder: ShoppingItemViewHolder, position: Int) {
        val item = _list[position]
        holder.nameTextView.text = item.name
        holder.quantityTextView.text = "x ${item.quantity}"
        holder.isMarkedCheckBox.isChecked = item.isMarked
        holder.isMarkedCheckBox.setOnClickListener {
            item.isMarked = holder.isMarkedCheckBox.isChecked
            onCheckedClickFun(item)
        }
        holder.itemLayout.setOnClickListener {
            onItemClickFun(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        return ShoppingItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_shopping_list,
                parent, false
            )
        )
    }

    class ShoppingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.shoppingListItem_productName)
        val quantityTextView: TextView = itemView.findViewById(R.id.shoppingListItem_productQuantity)
        val isMarkedCheckBox: CheckBox = itemView.findViewById(R.id.shoppingListItem_markedChB)
        val itemLayout: ConstraintLayout = itemView.findViewById(R.id.shoppingListItem)
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(newData: MutableList<ShoppingItem>) {
        newData.sortBy { p -> p.name }
        _list.clear()
        _list.addAll(newData)
        notifyDataSetChanged()
    }
}