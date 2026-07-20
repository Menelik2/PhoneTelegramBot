package com.example.telegrambot.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.telegrambot.data.MessageEntity
import com.example.telegrambot.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messages: List<MessageEntity> = emptyList()
    private val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun submitList(newMessages: List<MessageEntity>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageEntity) {
            binding.apply {
                tvAvatar.text = if (message.from.isNotEmpty()) message.from.take(2).uppercase() else "?"
                tvUsername.text = "@${message.from}"
                tvTime.text = dateFormat.format(Date(message.timestamp))
                tvMessage.text = message.content
            }
        }
    }
}
