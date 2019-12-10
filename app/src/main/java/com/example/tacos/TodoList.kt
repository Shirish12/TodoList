package com.example.tacos


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class TodoList(val list_items: MutableList<String>) : RecyclerView.Adapter<TodoList.MainViewHolder>(){

    private var removedPosition: Int = 0
    private var removedItem: String = ""

    var ClickItem: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {


        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_items, parent, false);
        return MainViewHolder(view)
    }

    override fun getItemCount() = list_items.size


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.eachItem.text = list_items[position]

    }

    fun removeItem(view: RecyclerView.ViewHolder){
        removedPosition = view.adapterPosition
        removedItem = list_items[view.adapterPosition]


        list_items.removeAt(view.adapterPosition)
        notifyItemRemoved(view.adapterPosition)

        Snackbar.make(view.itemView, "$removedItem" + "deleted", Snackbar.LENGTH_LONG).setAction("UNDO"){
            list_items.add(removedPosition,removedItem)
            notifyItemInserted(removedPosition)
        }.show()
    }

    fun insertTodoItems(item: String){
        list_items.add(item)
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        init{
            itemView.setOnClickListener(this)
        }
        val eachItem: TextView = itemView.findViewById(R.id.eachItem)

        override fun onClick(v: View) {

            ClickItem?.invoke(list_items[adapterPosition])
        }
    }



}