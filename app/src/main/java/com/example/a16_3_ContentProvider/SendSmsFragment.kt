package com.example.a16_3_ContentProvider

import android.annotation.SuppressLint
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.a16_3_ContentProvider.databinding.FragmentSendSmsBinding

class SendSmsFragment : Fragment() {

    private var _binding: FragmentSendSmsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем объект Contact из Bundle
        val contact = arguments?.getSerializable("contact") as? Contact

        contact?.let {
            // Устанавливаем текст в TextView для отображения данных контакта
            binding.contactNameTextView.text = it.name
            binding.contactPhoneNumberTextView.text = it.phoneNumber

            val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            val scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)

            // Обработчик нажатия на кнопку отправки SMS
            binding.sendSMS.setOnTouchListener { view, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        view.startAnimation(scaleUp)
                    }

                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        view.startAnimation(scaleDown)
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            val message = binding.smsMessageEditText.text.toString()
                            if (message.isNotEmpty()) {
                                sendSms(it.phoneNumber, message)
                                parentFragmentManager.popBackStack()
                                showSnackbar("Сообщение отправлено контакту ${it.name}")
                            }
                        }
                    }
                }
                true
            }

        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }
}