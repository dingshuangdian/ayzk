package com.kingtangdata.inventoryassis.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.kingtangdata.inventoryassis.util.SDCardUtil;

public class CrashHandler implements UncaughtExceptionHandler {

	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		saveCrashInfo2File(ex);
		ActivityManager.closeAll();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private void saveCrashInfo2File(Throwable ex) {
		try {
			StringBuffer sb = new StringBuffer();
			String time = formatter.format(new Date());
			sb.append("\n crash time -  " + time + "\n");

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			String result = writer.toString();
			sb.append(result);
			sb.append("-----------------------------------------\n");

			String fileName = "log.txt";
			String path = SDCardUtil.getLogPath();
			
			String file = path +"/" + fileName;
			if (SDCardUtil.isSDCardExist()) {
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(file, true);
				Log.e("CrashHandler", "CrashHandler:"+sb.toString());
				fos.write(sb.toString().getBytes());
				fos.close();
			}
		} catch (Exception e) {
			Log.e(e.getMessage(), "an error occured while writing file...", e);
		}
	}
}
