package com.example.a16_3_ContentProvider

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

open class BaseActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    fun setupToolbar(toolbarId: Int, needBackArrow: Boolean) {
        toolbar = findViewById(toolbarId)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        backButton = toolbar.findViewById(R.id.back_button) ?: return

        if (needBackArrow) {
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener {
                onBackPressed()
            }
        } else {
            backButton.visibility = View.GONE
        }
    }

    protected fun setupWindowInsets(viewId: Int) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(viewId)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Если в back stack есть фрагменты, вернуться к предыдущему
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()  // Закрыть активность, если фрагментов нет
                }
                true
            }

            R.id.action_exit -> {
                finishAffinity()  // Закрыть все активности
                true
            }

            R.id.action_search -> {
                val searchView = item.actionView as androidx.appcompat.widget.SearchView
                searchView.setOnQueryTextListener(object :
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                        if (fragment is MainFragment) {
                            fragment.filterContacts(query)
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                        if (fragment is MainFragment) {
                            fragment.filterContacts(newText)
                        }
                        return true
                    }
                })
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.exit, menu)
        return true
    }
}