package com.udemy.contact.manager.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.udemy.contact.manager.app.clickHandlers.ContactClickListener
import com.udemy.contact.manager.app.database.Contact
import com.udemy.contact.manager.app.database.ContactViewModel
import com.udemy.contact.manager.app.databinding.ItemContactBinding

class ContactAdapter(
    private val activity: AppCompatActivity,
    private val contactViewModel: ContactViewModel,
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private var contactList: ArrayList<Contact> = ArrayList()

    class ViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact: Contact = contactList[position]
        holder.binding.activity = activity
        holder.binding.contact = contact
        holder.binding.viewModel = contactViewModel
        holder.binding.clickHandler = ContactClickListener()
    }

    fun setContactList(contactList: ArrayList<Contact>) {
        this.contactList = contactList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        Log.d("--list--", "getItemCount: " + contactList.size)
        return contactList.size
    }
}