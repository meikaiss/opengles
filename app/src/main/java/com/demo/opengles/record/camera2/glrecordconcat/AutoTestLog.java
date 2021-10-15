package com.demo.opengles.record.camera2.glrecordconcat;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AutoTestLog {

    private Context context;
    private String fileName;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
    private File logFile;

    //最多缓存的mp4文件个数
    public int maxMp4CacheCount = 10;
    private int writeCount;
    private int allDeleteCount;

    public void createFile(Context context) {
        this.context = context;
        String dateString = format.format(new Date());
        fileName = dateString + ".txt";
        logFile = new File(context.getExternalCacheDir(), fileName);

        if (logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int writeAppend(String msg, Object... obj) {
        return writeAppend(String.format(msg, obj));
    }

    public int writeAppend(String msg) {
        String dateString = format.format(new Date());

        FileWriter fw = null;
        try {
            fw = new FileWriter(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(dateString + "  " + msg);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ++writeCount;
    }

    public void manageCache() {
        File[] listFiles = context.getExternalCacheDir().listFiles();
        if (listFiles == null || listFiles.length == 0) {
            writeAppend("当前缓存的所有文件个数为0");
            return;
        }

        int txtCount = 0;
        int mp4Count = 0;
        List<File> mp4FileList = new ArrayList<>();
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].getName().endsWith("txt")) {
                txtCount++;
            } else if (listFiles[i].getName().endsWith("mp4")) {
                mp4Count++;
                mp4FileList.add(listFiles[i]);
            }
        }
        writeAppend("当前缓存的txt文件数=" + txtCount + "，视频文件数=" + mp4Count);

        if (mp4Count > maxMp4CacheCount) {
            Collections.sort(mp4FileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            int deleteCount = mp4Count - maxMp4CacheCount;
            String deleteFileName = "";
            for (int i = 0; i < deleteCount; i++) {
                mp4FileList.get(i).delete();
                deleteFileName += mp4FileList.get(i).getName();
                allDeleteCount++;
            }
            writeAppend("删除最早的 " + deleteCount + " 个视频，删除的文件名=" + deleteFileName + "，共已删除 " + allDeleteCount + " 个视频文件");
        }
    }
}