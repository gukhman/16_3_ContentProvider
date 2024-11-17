package com.example.a16_2_Permissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a16_2_Permissions.databinding.FragmentMainBinding
import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var currentPermission: String? = null

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Регистрация permissionLauncher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handlePermissionResult(isGranted)
        }

        binding.cameraBTN.setOnClickListener {
            val permission = Manifest.permission.CAMERA
            currentPermission = permission
            permissionLauncher.launch(permission)
        }

        binding.contactsBTN.setOnClickListener {
            val permission = Manifest.permission.READ_CONTACTS
            currentPermission = permission
            permissionLauncher.launch(permission)
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {

        var message: String? = null

        currentPermission?.let { permission ->
            when (permission) {
                Manifest.permission.CAMERA -> {
                    if (isGranted) (activity as MainActivity).replaceFragment(CameraFragment())
                    else message = "Пользователь не предоставил доступ к камере"
                }

                Manifest.permission.READ_CONTACTS -> {
                    if (isGranted) (activity as MainActivity).replaceFragment(ContactsFragment())
                    else message = "Пользователь не предоставил доступ к контактам"
                }

                else -> return
            }
            message?.let { showSnackbar(it) }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Освобождение ресурсов ViewBinding
        _binding = null
    }
}
