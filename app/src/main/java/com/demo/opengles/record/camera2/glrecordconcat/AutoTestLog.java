package com.demo.opengles.record.camera2.glrecordconcat;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

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

    private static final String TAG = "AutoTestLog";

    private Context context;
    private ActivityManager activityManager;

    private String fileName;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
    private File logFile;

    //最多缓存的mp4文件个数
    public int maxMp4CacheCount = 10;
    private int writeCount;
    private int allDeleteCount;

    public void createFile(Context context) {
        this.context = context;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

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

    public double logRunningMemory() {
        double mem = 0.0D;
        try {
            // 统计进程的内存信息 totalPss

            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
            if (memInfo.length > 0) {

                /**
                 * 读取内存信息,跟Android Profiler 分析一致
                 */
                String java_mem = memInfo[0].getMemoryStat("summary.java-heap");

                String native_mem = memInfo[0].getMemoryStat("summary.native-heap");

                String graphics_mem = memInfo[0].getMemoryStat("summary.graphics");

                String stack_mem = memInfo[0].getMemoryStat("summary.stack");

                String code_mem = memInfo[0].getMemoryStat("summary.code");

                String others_mem = memInfo[0].getMemoryStat("summary.system");

                final int dalvikPss = convertToInt(java_mem, 0)
                        + convertToInt(native_mem, 0)
                        + convertToInt(graphics_mem, 0)
                        + convertToInt(stack_mem, 0)
                        + convertToInt(code_mem, 0)
                        + convertToInt(others_mem, 0);

                if (dalvikPss >= 0) {
                    // Mem in MB
                    mem = dalvikPss / 1024.0D;
                }

                String nameTag = "java-heap, native-heap, graphics, stack, code, system";

                String memInfoLog = "";
                memInfoLog += java_mem + ", ";
                memInfoLog += native_mem + ", ";
                memInfoLog += graphics_mem + ", ";
                memInfoLog += stack_mem + ", ";
                memInfoLog += code_mem + ", ";
                memInfoLog += others_mem + ", ";

                Log.e(TAG, nameTag);
                Log.e(TAG, memInfoLog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mem;
    }

    public static int convertToInt(Object value, int defaultValue) {
        if (value == null || "".equals(value.toString().trim())) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value.toString());
        } catch (Exception e) {

            try {
                return Integer.valueOf(String.valueOf(value));
            } catch (Exception e1) {

                try {
                    return Double.valueOf(value.toString()).intValue();
                } catch (Exception e2) {
                    return defaultValue;
                }
            }
        }
    }

}