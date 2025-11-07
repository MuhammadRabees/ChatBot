package com.example.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.chatbot.databinding.ItemBotMessageBinding
import com.example.chatbot.databinding.ItemUserMessageBinding

class ChatAdapter(private val items: MutableList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isUser) TYPE_USER else TYPE_BOT
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val binding = ItemUserMessageBinding.inflate(inflater, parent, false)
            UserVH(binding)
        } else {
            val binding = ItemBotMessageBinding.inflate(inflater, parent, false)
            BotVH(binding)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = items[position]
        if (holder is UserVH) holder.bind(m)
        else if (holder is BotVH) holder.bind(m)
    }
    override fun getItemCount(): Int = items.size

    fun addMessage(msg: Message) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    fun removeLastMessage() {
        if (items.isNotEmpty()) {
            val lastIndex = items.size - 1
            if (!items[lastIndex].isUser) {
                items.removeAt(lastIndex)
                notifyItemRemoved(lastIndex)
            }
        }
    }

    // Function to load previous history
    fun setMessages(messages: List<Message>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    // Function for new chat button
    fun clearMessages() {
        items.clear()
        notifyDataSetChanged()
    }

    class UserVH(private val binding: ItemUserMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            if (msg.text.isNotEmpty()) {
                binding.tvUserMessage.text = msg.text
                binding.tvUserMessage.visibility = View.VISIBLE
            } else {
                binding.tvUserMessage.visibility = View.GONE
            }
            if (msg.imageUri != null) {
                binding.ivUserImage.load(msg.imageUri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_camera)
                }
                binding.ivUserImage.visibility = View.VISIBLE
            } else {
                binding.ivUserImage.visibility = View.GONE
            }
        }
    }
    class BotVH(private val binding: ItemBotMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            binding.tvBotMessage.text = msg.text
            binding.ivBotImage.visibility = View.GONE
        }
    }
}