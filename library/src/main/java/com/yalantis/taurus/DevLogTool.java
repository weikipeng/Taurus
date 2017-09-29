package com.yalantis.taurus;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by wikipeng on 2017/9/22.
 */
public class DevLogTool {
    private static DevLogTool sDevLogTool;
    private        Context    context;
    private static boolean    isPrintPath;

    private DevLogTool(Context context) {
        this.context = context;
    }

    public static DevLogTool getInstance(Context context) {
        if (sDevLogTool == null) {
            sDevLogTool = new DevLogTool(context.getApplicationContext());
        }
        return sDevLogTool;
    }

    public synchronized void saveLog(String log) {
        String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        String className     = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        int    lineNumber    = Thread.currentThread().getStackTrace()[3].getLineNumber();

        String codeMessage = "(" + className + ":" + lineNumber + "@" + Thread.currentThread().getName() + ") : ";

        File externalFilesDir = context.getExternalFilesDir(null);
        File logFile          = new File(externalFilesDir, "jzb.log");

        if (!isPrintPath) {
            isPrintPath = true;
            Log.e("jzb", "jzbFocus debug saveLog======>path :" + logFile.getAbsolutePath());
        }
        Log.e("jzb", "jzbFocus debug " + codeMessage + log);

        try {
            if (!logFile.exists() || logFile.isDirectory()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            //            FileOutputStream fileOutputStream = new FileOutputStream(logFile);
            //            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            //            bufferedOutputStream.flush();
            FileWriter     fileWriter     = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String         timeStampText  = DateTool.getDetailTimeText(System.currentTimeMillis());
            bufferedWriter.append(timeStampText);
            bufferedWriter.append(codeMessage);
            bufferedWriter.append(log);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveLogFile(String logFileName, String log) {
        File externalFilesDir = context.getExternalFilesDir(null);
        File logFile          = new File(externalFilesDir, logFileName);
        //        FOpenLog.e("jzbFocus debug saveLog======>path :" + logFile.getAbsolutePath() + "log:" + log);
        try {
            if (!logFile.exists() || logFile.isDirectory()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } else {
                logFile.delete();
            }
            //            FileOutputStream fileOutputStream = new FileOutputStream(logFile);
            //            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            //            bufferedOutputStream.flush();
            FileWriter     fileWriter     = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String         timeStampText  = DateTool.getDetailTimeText(System.currentTimeMillis());
            bufferedWriter.append(timeStampText);
            bufferedWriter.append(":");
            bufferedWriter.append(log);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
