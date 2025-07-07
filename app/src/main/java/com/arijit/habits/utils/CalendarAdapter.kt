package com.arijit.habits.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arijit.habits.R
import com.arijit.habits.models.DateTaskStatus
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarAdapter(
    private var dates: List<DateTaskStatus>,
    private val onDateClick: (DateTaskStatus) -> Unit,
    private var selectedDateString: String? = null
): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_SELECTED = 1
    }

    fun updateData(newData: List<DateTaskStatus>) {
        dates = newData
        notifyDataSetChanged()
    }

    fun updateSelectedDate(selected: String?) {
        selectedDateString = selected
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val item = dates[position]
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val itemDateString = dateFormat.format(item.date)
        return if (itemDateString == selectedDateString) VIEW_TYPE_SELECTED else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SELECTED) R.layout.item_calendar_date_selected else R.layout.item_calendar_date
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CalendarViewHolder,
        position: Int
    ) {
        val item = dates[position]
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        holder.tvDay.text = dayFormat.format(item.date)
        holder.tvDate.text = dateFormat.format(item.date)

        val isComplete = item.totalTasks > 0 && item.finishedTasks == item.totalTasks
        holder.statusDot.setBackgroundResource(
            if (isComplete) R.drawable.dot_green else R.drawable.dot_grey
        )

        holder.itemView.isEnabled = true
        holder.itemView.isClickable = true
        holder.itemView.setOnClickListener { onDateClick(item) }
    }

    override fun getItemCount(): Int = dates.size

    class CalendarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val statusDot: View = itemView.findViewById(R.id.status_dot)
    }
}