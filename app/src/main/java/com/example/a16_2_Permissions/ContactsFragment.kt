package com.example.a16_2_Permissions

import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a16_2_Permissions.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadContacts()
    }

    private fun loadContacts() {
        val contacts = mutableListOf<Contact>()
        // ContentResolver для доступа к контактам
        val contentResolver = requireContext().contentResolver
        // Запрос контактов через ContentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        // Извлекаем данные
        cursor?.use {
            // Получаем индексы
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                contacts.add(Contact(name, number))
            }
        }
        // Настройка RecyclerView с загруженными контактами
        setupRecyclerView(contacts)
    }

    // Метод для настройки RecyclerView
    private fun setupRecyclerView(contacts: List<Contact>) {
        val adapter = ContactsAdapter(contacts)

        binding.contactsRV.adapter = adapter
        binding.contactsRV.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
