package dev.mars.openslesdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kotlin.jvm.Throws;

public class MainActivity extends AppCompatActivity {
    String[] pers = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
    private OpenSLRecorder recorder;
    private OpenSLPlayer player;
    private SpeexUtils speexUtils;
    private AudioUtils audioUtils;
    CheckBox cb1,cb2;

    private String LOG_TAG = "Audio Demo OpenSLES";
    // in kotlin, this asset manager works fine, however in java it is untested due to Kotlin
    // specific functions needing to be implemented/ported to java

    /**
    inner class assetsManager {

        fun copyAssetFolder(assetManager: AssetManager): Boolean = copyAssetFolder(assetManager, null, ASSETS)

        fun copyAssetFolder(
                assetManager: AssetManager,
                toPath: String
        ): Boolean = copyAssetFolder(assetManager, null, toPath)

        fun copyAssetFolder(
                assetManager: AssetManager,
                fromAssetPath: String?,
                toPath: String
        ): Boolean {
            try {
                val files: Array<String>? = assetManager.list(if (fromAssetPath.isNullOrBlank()) "" else fromAssetPath)
                if (files == null) return false else if (files.isEmpty()) return false
                Log.i(LOG_TAG, "obtained a list of assets")
                File(toPath).mkdirs()
                var res = true
                for (file in files)
                    if (file.contains("."))
                        res = res and copyAsset(
                        assetManager,
                if (fromAssetPath.isNullOrBlank()) file else "$fromAssetPath/$file",
                        "$toPath/$file"
                            )
                        else
                res = res and copyAssetFolder(
                        assetManager,
                if (fromAssetPath.isNullOrBlank()) file else "$fromAssetPath/$file",
                        "$toPath/$file"
                            )
                return res
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }

        fun copyAsset(
                assetManager: AssetManager,
                fromAssetPath: String, toPath: String
        ): Boolean {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                Log.i(LOG_TAG, "copying \"$fromAssetPath\" to \"$toPath\"")
                `in` = assetManager.open(fromAssetPath)
                File(toPath).createNewFile()
                out = FileOutputStream(toPath)
                copyFile(`in`!!, out)
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        @Throws(IOException::class)
        fun copyFile(`in`: InputStream, out: OutputStream) {
            val buffer = ByteArray(1024)
            var read = `in`.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = `in`.read(buffer)
            }
        }
    }
     */
    class assetsManager {

        String ASSETS = "";

        Boolean copyAssetFolder(AssetManager assetManager) {
            return copyAssetFolder(assetManager, null, ASSETS);
        }

        Boolean copyAssetFolder(AssetManager assetManager, String toPath) {
            return copyAssetFolder(assetManager, null, toPath);
        }

        Boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
            Log.i(LOG_TAG, "copying \"" + fromAssetPath + "\" to \"" + toPath + "\"");
            try {
                String[] files;
                boolean fromAssetPathNullOrEmpty = false;
                if (fromAssetPath == null) fromAssetPathNullOrEmpty = true;
                else fromAssetPathNullOrEmpty = fromAssetPath.isEmpty() || fromAssetPath.trim().isEmpty();
                if (fromAssetPathNullOrEmpty) files = assetManager.list("");
                else files = assetManager.list(fromAssetPath);
                if (files == null) return false;
                String f = files.toString();
                if (f.isEmpty() || f.trim().isEmpty()) return false;
                Log.i(LOG_TAG, "obtained a list of assets: " + f);
                new File(toPath).mkdirs();
                Boolean res = true;
                for(String file : files) {
                    if (file.contains(".")) {
                        if (fromAssetPathNullOrEmpty) {
                            res = copyAsset(assetManager, file, toPath + "/" + file);
                            if (!res) break;
                        } else {
                            res = copyAsset(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                            if (!res) break;
                        }
                    } else {
                        if (fromAssetPathNullOrEmpty) {
                            res = copyAssetFolder(assetManager, file, toPath + "/" + file);
                            if (!res) break;
                        } else {
                            res = copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                            if (!res) break;
                        }
                    }
                }
                return res;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        Boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
            InputStream in = null;
            OutputStream out = null;
            try {
                Log.i(LOG_TAG, "copying \"" + fromAssetPath + "\" to \"" + toPath + "\"");
                in = assetManager.open(fromAssetPath);
                new File(toPath).createNewFile();
                out = new FileOutputStream(toPath);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        }
    }

    String FILE_PLAYED1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.wav";
    String FILE_PLAYED2;
    String FILE_PLAYED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assetsManager am = new assetsManager();
        am.ASSETS = getFilesDir().getAbsolutePath() + "/ASSETS";

        FILE_PLAYED2 = am.ASSETS + "/00001313.raw";
        FILE_PLAYED = FILE_PLAYED2;

        Log.i(LOG_TAG, "getFilesDir().getAbsolutePath() + \"/ASSETS\" = " + getFilesDir().getAbsolutePath() + "/ASSETS" + ")");
        Log.i(LOG_TAG, "am.ASSETS = " + am.ASSETS);
        Log.i(LOG_TAG, "FILE_PLAYED = " + FILE_PLAYED);

        Log.i(LOG_TAG, "copying assets folder");
        am.copyAssetFolder(getAssets());

        setContentView(R.layout.activity_main);
        cb1 = (CheckBox) findViewById(R.id.cb1);
        cb2 = (CheckBox) findViewById(R.id.cb2);
        recorder = new OpenSLRecorder();
        player = new OpenSLPlayer();
        speexUtils = new SpeexUtils();
        audioUtils= new AudioUtils();
    }


    public void startRecord(View view) {
        if (hasPermission()) {
            startToRecord();
        } else {
            requestPermissions();
        }
    }

    public void startToRecord(){
        if(!recorder.startToRecord(Common.SAMPLERATE,Common.PERIOD_TIME,Common.CHANNELS,Common.DEFAULT_PCM_FILE_PATH)){
            Toast.makeText(MainActivity.this,"Already in recording state!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Start recording!",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecord(View view) {
        if(!recorder.stopRecording()){
            Toast.makeText(MainActivity.this,"Not in recording state!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Recording stopped!",Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions(){
        if(isLollipop()) {
            requestPermissions(pers, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==0){
            if(hasPermission()){
                startToRecord();
            }else{
                Toast.makeText(MainActivity.this,"Unable to get permissions",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasPermission() {
        return hasPermission(pers);
    }


    private boolean hasPermission(String[] pers) {
        if(isLollipop()){
            for(String per:pers){
                if(checkSelfPermission(per)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isLollipop(){
        return Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public void startPlay(View view) {
        if(!player.startToPlay(Common.SAMPLERATE,Common.PERIOD_TIME,Common.CHANNELS,FILE_PLAYED)){
            Toast.makeText(MainActivity.this,"Is playing!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Start playing!",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopPlay(View view) {
        if(!player.stopPlaying()){
            Toast.makeText(MainActivity.this,"Not playing!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Playing stopped!",Toast.LENGTH_SHORT).show();
        }
    }

    public void encodeWithSpeex(View view) {
        speexUtils.encode(Common.DEFAULT_PCM_FILE_PATH,Common.DEFAULT_SPEEX_FILE_PATH);
    }

    public void decodeWithSpeex(View view) {
        speexUtils.decode(Common.DEFAULT_SPEEX_FILE_PATH,Common.DEFAULT_PCM_OUTPUT_FILE_PATH);
    }

    public void playOutputPCM(View view) {
        if(!player.startToPlay(Common.SAMPLERATE,Common.PERIOD_TIME,Common.CHANNELS,FILE_PLAYED)){
            Toast.makeText(MainActivity.this,"Is playing!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Start playing!",Toast.LENGTH_SHORT).show();
        }
    }

    public void recordAndPlayPCM(View view) {
        if(!audioUtils.recordAndPlayPCM(cb1.isChecked(),cb2.isChecked())){
            Toast.makeText(MainActivity.this,"Is recording and playing!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Start recording and playing!",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecordAndPlayPCM(View view) {
        if(!audioUtils.stopRecordAndPlay()){
            Toast.makeText(MainActivity.this,"not in recording and playing state!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"recording and playing stoped!",Toast.LENGTH_SHORT).show();
        }
    }
}
