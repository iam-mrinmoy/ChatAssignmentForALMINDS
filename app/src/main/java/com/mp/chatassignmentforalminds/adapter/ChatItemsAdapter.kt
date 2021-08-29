package com.mp.chatassignmentforalminds.adapter

import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mp.chatassignmentforalminds.databinding.ChatItemBinding


class ChatItemsAdapter(
    var arrayList: ArrayList<String>
) :
    RecyclerView.Adapter<ChatItemsAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder) {
            with(arrayList[holder.adapterPosition]) {
                binding.tvChatText.text=this

            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    class MyViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

}