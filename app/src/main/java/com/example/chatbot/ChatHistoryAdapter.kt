package com.example.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.databinding.ItemChatSessionBinding

// This file is for side panel drawer in activity_main.xml
class ChatHistoryAdapter(
    private val onSessionClicked: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatHistoryAdapter.SessionVH>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionVH {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionVH(binding, onSessionClicked)
    }

    override fun onBindViewHolder(holder: SessionVH, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionVH(
        private val binding: ItemChatSessionBinding,
        private val onSessionClicked: (ChatSession) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            binding.tvSessionTitle.text = session.title
            binding.root.setOnClickListener {
                onSessionClicked(session)
            }
        }
    }

    class SessionDiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem.sessionId == newItem.sessionId
        }
        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem == newItem
        }
    }
}