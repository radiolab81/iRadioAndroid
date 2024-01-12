package com.example.iradioandroid.iRadioSDR;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class KiwiDB {
    private static final String TAG = "KiwiDB";
    
    public static enum KiwiDBResult { OK, ERROR };

    private static Vector kiwisdr_url = new Vector();
    private static Vector kiwisdr_name = new Vector();

    /* load kiwis.db Database from Downloads folder */
    public KiwiDBResult loadDB() {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/kiwis.db");

            try {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    Log.i(TAG, line);
                    // " " is seperator -> URL[ ]Metainfo
                    kiwisdr_url.add(line.substring(0,line.indexOf(" ")));
                    kiwisdr_name.add(line.substring(line.indexOf(" ")));
                    line = reader.readLine();
                }
                Log.i(TAG, kiwisdr_url.size() + "servers in kiwidb now ");
                reader.close();
                return KiwiDBResult.OK;

            } catch (IOException ex) {
                Log.w(TAG, ex.toString());
                return KiwiDBResult.ERROR;
            }
    }

    /* save actual content of vectors to kiwis.db Database in Downloads folder */
    /* Format:   URL separator Metainfo \n
    /* where separator is " "
     */
    public KiwiDBResult saveDB() {
        FileOutputStream os;
        BufferedWriter writer;
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/kiwis.db");

        try {
            os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os));
            for (int i=0; i< kiwisdr_url.size(); i++) {
                writer.write(kiwisdr_url.elementAt(i) + " " + kiwisdr_name.elementAt(i) + "\n");
            }
            writer.close();
            return KiwiDBResult.OK;

        } catch (IOException ex) {
            Log.w(TAG, ex.toString());
            return KiwiDBResult.ERROR;
        }
    };

    public String getKiwiSDRElementAt(int id) {
        return (String) kiwisdr_url.elementAt(id);
    }

    public String getKiwiSDRInfoElementAt(int id) {
        return (String) kiwisdr_name.elementAt(id);
    }

    public Vector<String> getKiwiSDRUrlDB() {
        return (Vector<String>) kiwisdr_url.clone();
    }

    public Vector<String> getKiwiSDRInfoDB() {
        return (Vector<String>) kiwisdr_name.clone();
    }


    /* updates the kiwis.db database from http://kiwisdr.com/public/ */
    public void updateDBfromWeb() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Vector kiwisdr_url_temp = new Vector();
                Vector kiwisdr_name_temp = new Vector();
                Log.i(TAG,"try to download kiwi server db");
                try {
                    // Your code goes here
                    final String textSource = "http://kiwisdr.com/public/";
                    URL textUrl;
                    try {
                        textUrl = new URL(textSource);
                        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(textUrl.openStream()));
                        String StringBuffer;
                        while ((StringBuffer = bufferReader.readLine()) != null) {
                            if (StringBuffer.toString().contains("<span class='cl-name'>")) {
                                String name = StringBuffer.toString().replace("<span class='cl-name'>","");
                                name = name.replace("</span> <br>","");
                                kiwisdr_name_temp.add(name);
                            }

                            if (StringBuffer.toString().contains("target='_blank'>")) {
                                String url = StringBuffer.toString();
                                int first_cut = url.lastIndexOf("target='_blank'>") + new String("target='_blank'>").length();
                                int last_cut = url.lastIndexOf("</a> <br>");
                                url = url.substring(first_cut,last_cut);
                                kiwisdr_url_temp.add(url);
                            }

                        }
                        bufferReader.close();
                        if (kiwisdr_name_temp.size() == kiwisdr_url_temp.size()) {
                            Log.i(TAG, "found servers " + kiwisdr_url.size());
                            kiwisdr_url.clear();
                            kiwisdr_url = (Vector) kiwisdr_url_temp.clone();
                            kiwisdr_name.clear();
                            kiwisdr_name = (Vector) kiwisdr_name_temp.clone();

                            saveDB();
                        } else {
                            Log.e(TAG, "online database is corrupt, update process failed");
                        }
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        thread.start();
    }
}
