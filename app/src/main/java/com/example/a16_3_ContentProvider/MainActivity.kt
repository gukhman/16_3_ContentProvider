package com.example.a16_3_ContentProvider

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.a16_3_ContentProvider.databinding.ActivityMainBinding


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets(R.id.main)

        setupToolbar(R.id.toolbar, false)

        // Стартовый фрагмент, его не добавляем в backstack
        if (savedInstanceState == null) {
            replaceFragment(MainFragment(), false)
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        // Настроить Toolbar после замены фрагмента
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment is MainFragment) {
                setupToolbar(R.id.toolbar, false)
            } else {
                setupToolbar(R.id.toolbar, true)
            }
        }
    }
}