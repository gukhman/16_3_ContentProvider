package com.example.a16_3_ContentProvider

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a16_3_ContentProvider.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionCallLauncher: ActivityResultLauncher<String>
    private lateinit var permissionSmsLauncher: ActivityResultLauncher<String>
    private lateinit var permissionContactsLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Регистрация permissionLaunchers
        permissionCallLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showSnackbar("Разрешение на звонки предоставлено.")
            } else {
                showSnackbar("Пользователь не предоставил доступ к звонкам.")
            }
        }

        permissionSmsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showSnackbar("Разрешение на отправку SMS предоставлено.")
            } else {
                showSnackbar("Пользователь не предоставил доступ к SMS.")
            }
        }

        permissionContactsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                loadContacts()
            } else {
                showSnackbar("Пользователь не предоставил доступ к контактам.")
            }
        }

        // Проверяем разрешение на доступ к контактам
        when {
            requireContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }

            else -> {
                permissionContactsLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        var contacts = mutableListOf<Contact>()
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
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

        contacts = contacts.toSet().sortedBy { it.name }.toMutableList<Contact>()
        setupRecyclerView(contacts)
    }

    private fun setupRecyclerView(contacts: List<Contact>) {
        val adapter = ContactsAdapter(contacts,
            onCallClick = { phoneNumber -> checkAndMakeCall(phoneNumber) },
            onSmsClick = { contact -> checkAndSendSms(contact) }
        )

        binding.contactsRV.adapter = adapter
        binding.contactsRV.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun checkAndMakeCall(phoneNumber: String) {
        when {
            requireContext().checkSelfPermission(Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                makeCall(phoneNumber)
            }

            else -> {
                permissionCallLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }

    private fun checkAndSendSms(contact: Contact) {
        when {
            requireContext().checkSelfPermission(Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                val fragment = SendSmsFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("contact", contact)
                    }
                }
                (activity as MainActivity).replaceFragment(fragment)
            }

            else -> {
                permissionSmsLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
