package com.vasilis.ilunch;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

//import com.mpvasilis.mpvasilis.app.R;

public class Data extends Activity {

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i = 0; i < files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(s, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        s = p.applicationInfo.dataDir;

        final String sfinal = s;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        final ListView lv;
        try {
            ArrayList<String> FilesInFolder = new ArrayList<String>();
            FilesInFolder = GetFiles(s + "/files");
            lv = (ListView) findViewById(R.id.filelist);
            final ArrayList<String> files2 = new ArrayList<String>();
            files2.addAll(FilesInFolder);
            lv.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, FilesInFolder));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    File file = new File(sfinal + "/files/"+files2.get(position));
                    int totallines=0;
                    FileInputStream is= null;
                    try {
                        is = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader reader;

                    reader = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (line != null) {
                        try {
                            line = reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    totallines++;
                    }
                    Toast.makeText(getApplicationContext(), "Size " +humanReadableByteCount(file.length(),true) + ". Lines: "+totallines, Toast.LENGTH_SHORT).show();

                }
            });
        }
        catch (Throwable t){
            Toast.makeText(getApplicationContext(), "Δεν υπάρχουν δεδομένα!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
