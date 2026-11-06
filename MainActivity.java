package com.jarvis.app;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    TextToSpeech tts;
    SpeechRecognizer sr;
    TextView status, log;

    static final String[][] ALIAS = {
        {"यूट्यूब", "youtube"}, {"युटुब", "youtube"}, {"युट्यूब", "youtube"}, {"यू ट्यूब", "youtube"}, {"व्हाट्स", "whatsapp"}, {"वाट्स", "whatsapp"}, {"वॉट्स", "whatsapp"}, {"व्हॉट्स", "whatsapp"}, {"व्हाट्सएप", "whatsapp"}, {"वाट्सएप", "whatsapp"},
        {"कैमरा", "camera"}, {"गैलरी", "gallery"}, {"क्रोम", "chrome"}, {"सेटिंग", "settings"},
        {"मैप", "maps"}, {"फोटो", "photos"}, {"जीमेल", "gmail"}, {"इंस्टाग्राम", "instagram"},
        {"फेसबुक", "facebook"}, {"कैलकुलेटर", "calculator"}, {"घड़ी", "clock"}, {"गूगल", "google"}
    };

    TextView tv(String s, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color); v.setGravity(Gravity.CENTER);
        return v;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        float d = getResources().getDisplayMetrics().density;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF02060D);
        root.setPadding((int) (20 * d), (int) (60 * d), (int) (20 * d), (int) (20 * d));

        root.addView(tv("J A R V I S", 28, 0xFF3DF2FF));
        status = tv("गोले को दबाइए", 16, 0xFF7FB8C0);
        root.addView(status);

        TextView orb = tv("बोलिए", 22, 0xFF02060D);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(0xFF3DF2FF);
        g.setStroke((int) (6 * d), 0xFF0A6F86);
        orb.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (190 * d), (int) (190 * d));
        lp.setMargins(0, (int) (30 * d), 0, (int) (30 * d));
        root.addView(orb, lp);
        orb.setOnClickListener(v -> listen());

        log = tv("", 17, 0xFFD8FBFF);
        log.setGravity(Gravity.START);
        ScrollView sv = new ScrollView(this);
        sv.addView(log);
        root.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);

        tts = new TextToSpeech(this, st -> {
            if (st == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("hi", "IN"));
        });
    }

    void listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("इस फ़ोन में आवाज़ पहचान नहीं है");
            return;
        }
        if (sr != null) sr.destroy();
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle p) { status.setText("सुन रहा हूँ… बोलिए"); }
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float v) {}
            public void onBufferReceived(byte[] bf) {}
            public void onEndOfSpeech() { status.setText("समझ रहा हूँ…"); }
            public void onPartialResults(Bundle p) {}
            public void onEvent(int t, Bundle p) {}
            public void onError(int e) {
                String m = e == 9 ? "माइक की अनुमति नहीं है। Settings → Apps → JARVIS → Permissions में माइक Allow करें।"
                    : (e == 1 || e == 2 || e == 4) ? "इंटरनेट की समस्या है। नेट चालू करके फिर दबाइए।"
                    : (e == 6 || e == 7) ? "कुछ सुनाई नहीं दिया। फिर दबाकर साफ़ बोलिए।"
                    : (e == 12 || e == 13) ? "इस फ़ोन में हिन्दी आवाज़ पहचान उपलब्ध नहीं है।"
                    : "आवाज़ सेवा में समस्या है। Play Store में Google ऐप अपडेट करें।";
                status.setText(m + " (कोड " + e + ")");
            }
            public void onResults(Bundle r) {
                ArrayList<String> l = r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (l != null && !l.isEmpty()) handle(l.get(0));
            }
        });
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        sr.startListening(i);
    }

    @Override
    public void onRequestPermissionsResult(int c, String[] p, int[] r) {
        if (r.length > 0 && r[0] == PackageManager.PERMISSION_GRANTED) listen();
        else status.setText("माइक की अनुमति ज़रूरी है");
    }

    void handle(String raw) {
        log.append("आप: " + raw + "\n");
        String r = reply(raw.toLowerCase(Locale.ROOT));
        log.append("JARVIS: " + r + "\n\n");
        status.setText("गोले को दबाइए");
        tts.speak(r, TextToSpeech.QUEUE_FLUSH, null, "j");
    }

    boolean has(String t, String... w) {
        for (String x : w) if (t.contains(x)) return true;
        return false;
    }

    String reply(String t) {
        try {
            if (has(t, "टॉर्च", "टार्च", "टोर्च", "torch", "फ्लैश", "flash", "लाइट")) {
                boolean on = !has(t, "बंद", "off", "बुझा");
                CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
                cm.setTorchMode(cm.getCameraIdList()[0], on);
                return on ? "टॉर्च चालू।" : "टॉर्च बंद।";
            }
            if (has(t, "आवाज", "वॉल्यूम", "volume")) {
                boolean up = has(t, "बढ़ा", "बढा", "ज्यादा", "ज़्यादा", "up");
                AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return up ? "आवाज़ बढ़ा दी।" : "आवाज़ घटा दी।";
            }
            if (has(t, "कॉल", "call", "फोन करो", "फ़ोन करो")) {
                String dg = t.replaceAll("[^0-9]", "");
                if (dg.length() >= 10) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + dg)));
                    return "डायलर खोल रहा हूँ। हरा बटन दबाइए।";
                }
                return "नंबर बोलिए, जैसे, कॉल करो 9876543210।";
            }
            if (has(t, "अलार्म", "alarm")) {
                java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d{1,2})(?:[:.](\\d{2}))?").matcher(t);
                if (!mm.find()) return "कितने बजे? जैसे, सुबह 6 बजे का अलार्म लगाओ।";
                int h = Integer.parseInt(mm.group(1));
                int mi = mm.group(2) != null ? Integer.parseInt(mm.group(2)) : 0;
                if (h < 12 && has(t, "शाम", "रात", "दोपहर")) h += 12;
                if (h > 23 || mi > 59) return "समय समझ नहीं आया।";
                Intent a = new Intent(AlarmClock.ACTION_SET_ALARM);
                a.putExtra(AlarmClock.EXTRA_HOUR, h);
                a.putExtra(AlarmClock.EXTRA_MINUTES, mi);
                startActivity(a);
                return h + " बजकर " + mi + " मिनट का अलार्म लगा रहा हूँ।";
            }
            if (has(t, "खोजो", "सर्च", "search")) {
                String q = t.replace("गूगल पर", "").replace("खोजो", "").replace("सर्च करो", "").replace("सर्च", "").replace("search", "").trim();
                Intent s = new Intent(Intent.ACTION_WEB_SEARCH);
                s.putExtra(SearchManager.QUERY, q);
                startActivity(s);
                return q + " खोज रहा हूँ।";
            }
            if (has(t, "खोल", "ओपन", "open", "चलाओ", "चलाइए", "चलाएं", "चालू करो", "चालू करिए")) {
                String n = t;
                for (String w : new String[]{"जार्विस", " ऐप", " एप", " app", "खोलिए", "खोलें", "खोल दो", "खोलो", "खोल", "चलाइए", "चलाएं", "चलाओ", "चालू करो", "चालू करिए", "ओपन", "open", "करो", "करिए"}) n = n.replace(w, "");
                n = n.trim();
                for (String[] a : ALIAS) if (n.contains(a[0])) n = a[1];
                if (n.isEmpty()) return "कौन सा ऐप खोलूँ?";
                PackageManager pm = getPackageManager();
                Intent li = new Intent(Intent.ACTION_MAIN);
                li.addCategory(Intent.CATEGORY_LAUNCHER);
                for (ResolveInfo ri : pm.queryIntentActivities(li, 0)) {
                    String lab = ri.loadLabel(pm).toString().toLowerCase(Locale.ROOT);
                    if (lab.contains(n) || (lab.length() > 2 && n.contains(lab))) {
                        startActivity(pm.getLaunchIntentForPackage(ri.activityInfo.packageName));
                        return lab + " खोल रहा हूँ।";
                    }
                }
                return n + " नाम का ऐप नहीं मिला।";
            }
            if (has(t, "समय", "टाइम", "बजे")) {
                return "अभी समय " + new SimpleDateFormat("h:mm a", new Locale("hi", "IN")).format(new Date()) + " है।";
            }
            if (has(t, "तारीख", "तारिख", "date")) {
                return "आज " + new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("hi", "IN")).format(new Date()) + " है।";
            }
            if (has(t, "नमस्ते", "हेलो", "हैलो", "हाय", "जार्विस", "hello"))
                return "नमस्कार सर। बताइए, मैं क्या करूँ?";
            if (has(t, "धन्यवाद", "शुक्रिया", "थैंक")) return "आपका स्वागत है सर।";
        } catch (Exception e) {
            return "माफ़ कीजिए, यह काम नहीं हो पाया। (" + e.getClass().getSimpleName() + ")";
        }
        return "माफ़ कीजिए, यह मुझे समझ नहीं आया।";
    }

    @Override
    protected void onDestroy() {
        if (sr != null) sr.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
