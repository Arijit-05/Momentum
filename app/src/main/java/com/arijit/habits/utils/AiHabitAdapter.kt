package com.arijit.habits.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arijit.habits.R
import android.graphics.Paint
import androidx.core.content.ContextCompat

data class AiHabit(val emoji: String, val name: String)

class AiHabitAdapter(
    private val habits: List<AiHabit>,
    private val onItemClick: ((AiHabit) -> Unit)? = null,
    private val addedHabits: Set<String> = emptySet()
) : RecyclerView.Adapter<AiHabitAdapter.HabitViewHolder>() {
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiView: TextView = itemView.findViewById(R.id.habit_emoji)
        val nameView: TextView = itemView.findViewById(R.id.habit_name)
        val layout: View = itemView.findViewById(R.id.habit_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.emojiView.text = habit.emoji
        holder.nameView.text = habit.name
        val key = habit.emoji + "|" + habit.name
        if (addedHabits.contains(key)) {
            holder.nameView.paintFlags = holder.nameView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.layout.setBackgroundColor(ContextCompat.getColor(holder.layout.context, R.color.black))
            holder.nameView.setTextColor(ContextCompat.getColor(holder.layout.context, R.color.gray))
            holder.emojiView.setTextColor(ContextCompat.getColor(holder.layout.context, R.color.gray))
        } else {
            holder.nameView.paintFlags = holder.nameView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.layout.setBackgroundResource(R.drawable.habit_item_bg)
            holder.nameView.setTextColor(ContextCompat.getColor(holder.layout.context, R.color.white))
            holder.emojiView.setTextColor(ContextCompat.getColor(holder.layout.context, R.color.white))
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(habit)
        }
    }

    override fun getItemCount(): Int = habits.size
} 