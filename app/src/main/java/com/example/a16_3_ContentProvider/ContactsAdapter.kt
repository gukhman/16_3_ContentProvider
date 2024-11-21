package com.example.a16_3_ContentProvider

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val onCallClick: (String) -> Unit,
    private val onSmsClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {


    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contactName: TextView = itemView.findViewById(R.id.contactName)
        private val contactPhoneNumber: TextView = itemView.findViewById(R.id.contactPhoneNumber)
        private val callBTN: MaterialButton = itemView.findViewById(R.id.callBTN)
        private val smsBTN: MaterialButton = itemView.findViewById(R.id.smsBTN)

        private val scaleUp = AnimationUtils.loadAnimation(itemView.context, R.anim.scale_up)
        private val scaleDown = AnimationUtils.loadAnimation(itemView.context, R.anim.scale_down)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(contact: Contact) {
            contactName.text = contact.name
            contactPhoneNumber.text = contact.phoneNumber

            // Устанавливаем обработчики для кнопок
            callBTN.setOnTouchListener { view, event -> handleTouchEvent(view, event) { onCallClick(contact.phoneNumber) } }
            smsBTN.setOnTouchListener { view, event -> handleTouchEvent(view, event) { onSmsClick(contact) } }
        }

        private fun handleTouchEvent(view: View, event: android.view.MotionEvent, onClick: () -> Unit): Boolean {
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> view.startAnimation(scaleUp)
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.startAnimation(scaleDown)
                    if (event.action == android.view.MotionEvent.ACTION_UP) onClick()
                }
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount() = contacts.size
}
