package com.example.a16_3_ContentProvider

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a16_3_ContentProvider.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val allContacts = mutableListOf<Contact>()
    private var filteredContacts = mutableListOf<Contact>()

    //объект для запроса разрешений
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) loadContacts() else showSnackbar(getString(R.string.permission_denied))
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //запрос разрешения на загрузку контактов
        checkPermissionsAndLoadContacts()
        // добавление нового контакта
        binding.addContactBTN.setOnClickListener { checkNewContact() }
    }

    // вызов проверки разрешения на чтение контактов, loadContacts передаем в лямбду
    private fun checkPermissionsAndLoadContacts() {
        checkAndRequestPermission(Manifest.permission.READ_CONTACTS) { loadContacts() }
    }

    // если есть разрешение, то загружаем контакты loadContacts(), в противном случае запрашиваем разрешение через permissionLauncher
    private fun checkAndRequestPermission(permission: String, onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> onGranted()

            else -> permissionLauncher.launch(permission)
        }
    }

    //проверка полей при добавлении контакта
    private fun checkNewContact() {
        val name = binding.nameET.text.toString()
        val phone = binding.phoneET.text.toString()

        if (name.isNotEmpty() && phone.isNotEmpty()) {
            addContact(name, phone)
        } else {
            showSnackbar(getString(R.string.fill_all_fields))
        }
    }

    // Добавление контакта через contentResolver
    private fun addContact(name: String, phone: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val contentResolver = requireContext().contentResolver
            // Добавляем новый контакт
            val rawContactUri = contentResolver.insert(
                ContactsContract.RawContacts.CONTENT_URI,
                ContentValues()
            )

            rawContactUri?.lastPathSegment?.toLong()?.let { rawContactId ->
                // Добавляем имя и телефон в контакт
                addContactDetails(rawContactId, name, phone)
            }

            showSnackbar(getString(R.string.contact_added))
            loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
        }
    }

    // Добавление имени и телефона в контакт
    private fun addContactDetails(rawContactId: Long, name: String, phone: String) {
        val contentResolver = requireContext().contentResolver

        // Добавляем имя
        val nameValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

        // Добавляем телефон
        val phoneValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
            put(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
    }

    // Получение контактов
    private fun loadContacts() {
        val contacts = mutableListOf<Contact>()
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                contacts.add(Contact(name, number))
            }
        }
        allContacts.clear()
        allContacts.addAll(contacts)

        // Обновляем список и отображаем
        updateContactsList(allContacts)
    }

    fun filterContacts(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(query, ignoreCase = true) }
        }

        // Обновляем список и отображаем
        updateContactsList(filtered)
    }

    private fun updateContactsList(contacts: List<Contact>) {
        filteredContacts.clear()
        filteredContacts.addAll(contacts)
        setupRecyclerView(filteredContacts)
    }

    //Отображение контактов
    private fun setupRecyclerView(contacts: List<Contact>) {
        val sortedContacts = contacts.toSet().sortedBy { it.name }
        binding.contactsRV.apply {
            adapter = ContactsAdapter(
                sortedContacts,
                onCallClick = { phoneNumber ->
                    checkAndRequestPermission(Manifest.permission.CALL_PHONE) {
                        makeCall(phoneNumber)
                    }
                },
                onSmsClick = { contact ->
                    checkAndRequestPermission(Manifest.permission.SEND_SMS) {
                        sendSms(contact)
                    }
                }
            )
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }

    private fun sendSms(contact: Contact) {
        val fragment = SendSmsFragment().apply {
            arguments = Bundle().apply { putSerializable("contact", contact) }
        }
        (activity as MainActivity).replaceFragment(fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
