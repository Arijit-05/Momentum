package com.arijit.habits.utils

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arijit.habits.R
import com.arijit.habits.models.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onToggle: (Habit) -> Unit,
    private val onLongPress: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HabitViewHolder,
        position: Int
    ) {
        val habit = habits[position]
        holder.emoji.text = habit.emoji
        holder.name.text = habit.name

        if (habit.isDone) {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.layout.alpha = 0.4f
        } else {
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.layout.alpha = 1.0f
        }

        holder.layout.setOnClickListener {
            onToggle(habit)
        }

        holder.layout.setOnLongClickListener {
            if (!habit.isDone) onLongPress(habit)
            true
        }
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji = view.findViewById<TextView>(R.id.habit_emoji)
        val name = view.findViewById<TextView>(R.id.habit_name)
        val layout = view.findViewById<LinearLayout>(R.id.habit_item_layout)
    }
}