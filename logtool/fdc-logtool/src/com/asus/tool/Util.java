package com.asus.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;




import android.R.integer;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;

public class Util {
	
	private static final String TAG = "Util";
	public static final int MSG_UPDATE_COPY_FILE=1;
	private static final Uri mUri = MediaStore.Files.getContentUri("external");
	private static final boolean DEBUG=true;
	public static void copyFile(AsyncTask task,Context context,Handler handler,File src, File targetFile){
		
		if(src.isDirectory()){
			targetFile.mkdir();
			UpdateMedia.addfolder(context,targetFile);
			File[] childrenFiles=src.listFiles();
//			if(src.getAbsolutePath().equals("/data/gps/log")){
//				log("srcfile="+src.getAbsolutePath());
//				if(childrenFiles!=null){
//					log("childrenFiles len="+childrenFiles.length);
//					for(File srcfile:childrenFiles){
//						log("childrenFiles path="+srcfile.getAbsolutePath());
//					}
//				}else{
//					log("childrenFiles null");
//				}
//				
//			}
			if(childrenFiles!=null){
				for(File srcfile:childrenFiles){
					if(task !=null && task.isCancelled()){
						return;
					}
					if(srcfile.isHidden()==true || srcfile.getName().equals("lost+found")){
						continue;
					}
					String newtargetPath=targetFile.getAbsolutePath()+"/"+srcfile.getName();
					File newtargetFile=new File(newtargetPath);
					copyFile(task,context,handler,srcfile,  newtargetFile);
				}
			}
			
			
		}else{
			if(task !=null && task.isCancelled()){
				return;
			}
			Message msg=new Message();
			msg.what=MSG_UPDATE_COPY_FILE;
			msg.obj=targetFile.getAbsolutePath();
			handler.sendMessage(msg);
			 copy( src,  targetFile) ;
			 UpdateMedia.addFile(context,targetFile);
		}
	}
	
	private static boolean sendCommand(Context context,String cmd){
		 LocalSocket socket = null;
		 PrintWriter out=null;
		 BufferedReader in = null;
         try {
        	 socket=Util.getSocketInstance(DumpService.SOCKET_NAME);
 			if(socket==null){
 				return false;
 			}
			out = new PrintWriter(socket.getOutputStream());
			in =new  BufferedReader( new  InputStreamReader(socket.getInputStream())); 
			DumpService.sendCMD( out, in,cmd);
			socket.shutdownOutput();
			socket.close();
			} catch (IOException e) {					
				e.printStackTrace();
				Log.e(TAG,"socket connect fail "+e.getMessage());
				return false;
			}
        
         return true;
	}
	
	public static void copy(File src, File dst) 
	{
	    byte[] buffer = new byte[20480];
	    int len;
	    BufferedInputStream bis = null;
	    BufferedOutputStream bos= null;
		try {
			bis = new BufferedInputStream(new FileInputStream(src));
			bos = new BufferedOutputStream(new FileOutputStream(dst));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			Log.e(TAG, "FileNotFoundException");
			return;
		}
        
		
	    try {
	    	//rd = bis.read(buffer, 0, buffer.length);
	    	
			while ((len = bis.read(buffer, 0, buffer.length)) > 0) 
			{
			    bos.write(buffer, 0, len);
			}
			bos.flush();
			bis.close();
            bos.close();

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("msg_error","copy error");
		}finally{
			
		}
	    
	}
	
	
	public static String writeCommand(String cmd,String path) throws IOException {

		File logFile = new File(path);
		 if (!logFile.exists())
		   {
		    
			   try 
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		         Log.e(TAG, "logFile not exist="+ e.getMessage());
		         return null;
		      }
		   }
		  BufferedWriter buf  = new BufferedWriter(new FileWriter(logFile, true)); 
		  
		
		
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			log(e1.getMessage());
			e1.printStackTrace();
			return null;
		}
		//File file =new F
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
		String lineStr="";
		 try {
			 while ((lineStr = inBr.readLine()) != null) {
				 buf.write(lineStr+"\n") ;
				 buf.flush();
			}
			
		 } catch (IOException e) 
		 {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
			
		 }finally{
			 in.close();
			 inBr.close();
			 buf.close();
			
		 }

		try {

			if (p.waitFor() != 0 && p.exitValue() == 1) {
				return getError(p.getErrorStream());

			}
			return "";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	public static String getCommand(String cmd,String path) {

		Process p = null;
		try {
			String sendString=cmd +" > "+path;
			log(sendString);
			p = Runtime.getRuntime().exec(cmd);
			
			log("finish");
			// p = Runtime.getRuntime().exec("id");
		
			// p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			log(e1.getMessage());
			e1.printStackTrace();
			return null;
		}
		
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
//		StringBuilder stringBuilder = new StringBuilder();
//		String lineStr = "";
//		try {
//			while ((lineStr = inBr.readLine()) != null) {
//				stringBuilder.append(lineStr+"\n");
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		log("test");
		try {

			if (p.waitFor() != 0 && p.exitValue() == 1) {
				return getError(p.getErrorStream());

			}
			if (p.exitValue() == 0) {
				return null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	private static void log(String message) {
		if(DEBUG)
			Log.e(TAG, message);
	}
	
	public static long toSecond(String time)
	{
		String[] times=time.split("_");
		Time cacluteTime=new Time();
		if(isDigitIgnoreDash(times[0]))
		{
			try {
				int year=Integer.valueOf(times[0]);
				int month=Integer.valueOf(times[1].substring(0, 2));
				int monthDay=Integer.valueOf(times[1].substring(2, 4));
				int hour=Integer.valueOf(times[2].substring(0, 2));
				int minute=Integer.valueOf(times[2].substring(2, 4));
				int second=Integer.valueOf(times[2].substring(4, 6));
				//log("year="+year+",month="+month+",monthDay="+monthDay+",hour="+hour+",minute="+minute+",second="+second);
				cacluteTime.set(second, minute, hour, monthDay, month, year);
				
			} catch (Exception e) {
				return 0;
			}
		}
		return (cacluteTime.toMillis(false)/1000);
	}
	
	public static boolean isDigitIgnoreDash(String value)
	{
		String digit="0123456789";
		if(value==null || value.length()==0){
			return false;
		}
		
		for(int i=0;i<value.length();i++){
			if(value.substring(i, i+1).equals("_")){
				continue;
			}
			if(digit.contains(value.substring(i, i+1))==false){
				return false;
			}
		}
		
		return true;
	}
	
	public static void dumplogtool(String text){
		DumpSyslog.dumpsys("echo "+text+" >> " +DumpService.getLogRootpath()+"/logtool.history");
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
		String currentDateandTime = sdf.format(new Date());
		return currentDateandTime;	
	}

	public static String getError(InputStream p) {
		BufferedInputStream in = new BufferedInputStream(p);
		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();
		String lineStr = "";
		try {
			while ((lineStr = inBr.readLine()) != null) {
				stringBuilder.append(lineStr);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}
	
	public static boolean isMicroSDMount(Context context){
		String mount=getVolumeState( context,getMicroSDPath( context));
		if(mount !=null ){
			if(mount.equals(Environment.MEDIA_MOUNTED)){
				return true;
			}
		}
		return false;
	}
	
	public static String getMicroSDPath(Context context)
	{

		StorageManager storageManager = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);

		Method method = null;
		StorageVolume[] storageVolume = null;
		try {
			method = storageManager.getClass().getMethod("getVolumeList");
			try {
				storageVolume = (StorageVolume[]) method.invoke(storageManager);
				for (StorageVolume volume : storageVolume) {
					//Log.v(TAG, "volume=" + volume.getPath());
					if (volume.isRemovable()) {
						return volume.getPath();
					}
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "IllegalArgumentException");
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "IllegalAccessException");
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "InvocationTargetException");
			}

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "NoSuchMethodException");
		}

		return null;

	}
	
	 public static String getVolumeState(Context context,String path){
			String result="";
			  StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			    try {
					Method method= mStorageManager.getClass().getMethod("getVolumeState", String.class);
					try {
						result =(String) method.invoke(mStorageManager, path);
						
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//	Log.v(TAG, "method="+method);
			    } catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    return result;
		}
	 
	 public static void deleteRecusiveFiles(ContentResolver cr,String oldPath,boolean dir,boolean keep_dir){
			int id=getFileID(cr,oldPath);
			if(id!=0){
				deleteFileID(cr,id, dir,keep_dir);
			}
		}
		
	 private static int getFileID(ContentResolver cr,String canonicalPath)  {
			
			String selection=MediaStore.Files.FileColumns.DATA+"=?";
			String[] selectionArgs=null;
			try {
				selectionArgs=new String[]{canonicalPath};
			} catch (Exception e) {
				
				log("getFileID error="+e.getMessage());
			}
			
			Cursor cursor=cr.query(mUri, new String[]{MediaStore.Files.FileColumns._ID}, selection, selectionArgs, null);
			int result=0;
			cursor.moveToFirst();
			if(cursor !=null && cursor.getCount()==1){
				result=cursor.getInt(0);
				log("result="+result);
				
			}
			if(cursor!=null){
				cursor.close();
			}
			
			return result;
		}
		
		private static void deleteFileID(ContentResolver cr,int id, boolean dir,boolean keep) 
	    {
	       
	       // log("deleteFileID id="+id+",dir="+dir);
	        
	        if(dir)
	        {
	        	Cursor c = cr.query(mUri, new String[]{MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DATA}, "parent=?" , new String[]{ Integer.toString(id) }, null);
	        	if(c!=null && c.getCount()>0 )
	        	{
	    			if(c.moveToFirst())
	    			{
	    				do {
	    					deleteFileID(cr,c.getInt(0), true,false);
	    					log("id="+c.getInt(0)+",path="+c.getString(1));
	    				} while (c.moveToNext());
	    			}
	    			
	    		}
	        	 if(c!=null){
	 				c.close();
	 			}
			}
	        if(keep==false){
	        	int value=cr.delete(mUri, MediaStore.Files.FileColumns._ID+"=?", new String[]{ Integer.toString(id) });
	        	log("value="+value);
	        }
	       
	      
	
	    }
		
		private static class FileInfo{
			int mId;
			long mLastModifyed;
			public FileInfo(int id,long lastModifyed){
				mId=id;
				mLastModifyed=lastModifyed;
			}
		}
		
		public synchronized static void updateMtpByFolder(Context context,String root_path){
			HashMap<String, FileInfo> hashMap=new HashMap<String, FileInfo>();
			ContentResolver cr=context.getContentResolver();
			//change path sdcard to mtp path
			String path=root_path;
			if(root_path.startsWith("/sdcard")){
				String ecternelString=Environment.getExternalStorageDirectory().getAbsolutePath();
				path=root_path.replace("/sdcard", ecternelString);
			}
			log("updateMtpByFolder :"+path);
			getSpecMediaFiles(cr,hashMap,path);
			
			File file=new File(path); 
			//log("hash size="+hashMap.size());
			if(file.exists()){
				updateMtp(context,cr,hashMap,file);
			}
			//log("hash size finish="+hashMap.size());
			hashMap.keySet();
			Iterator<Entry<String, FileInfo>> it = hashMap.entrySet().iterator();
		    while (it.hasNext()) 
		    {
		        Map.Entry pairs = (Map.Entry)it.next();
		        FileInfo fileInfo=(FileInfo) pairs.getValue();
		        UpdateMedia.deleteMtpFilebyID(cr, fileInfo.mId);
		        log("incosist path="+pairs.getKey()+",id="+fileInfo.mId);
		     
		    }
			
		}
		
		public static void getSpecMediaFiles(ContentResolver cr,HashMap<String, FileInfo> hashMap,String query_path){
			String query=query_path;
			
			String selection=MediaStore.Files.FileColumns.DATA+  " like ?";//例子 %.apk
			String[] selectionArgs=new  String[]{ query+"%" };
			Cursor cursor=cr.query(mUri, new String[]{MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DATA,MediaStore.Files.FileColumns.DATE_MODIFIED}, selection, selectionArgs, null);
			if(cursor.getCount()>0){
				cursor.moveToFirst();
				do {
					int id=cursor.getInt(0);
					String path=cursor.getString(1);
					long lastModifyed=cursor.getLong(2);
					hashMap.put(path,new FileInfo(id, lastModifyed));
				} while (cursor.moveToNext());
			}
			if(cursor!=null){
				cursor.close();
			}
		}
		
		public static long getFolderSize(File dir) {
	        if (dir.exists()) {
	            long result = 0;
	            File[] fileList = dir.listFiles();
	            for(int i = 0; i < fileList.length; i++) {
	                // Recursive call if it's a directory
	                if(fileList[i].isDirectory()) {
	                    result += getFolderSize(fileList[i]);
	                } else {
	                    // Sum the file size in bytes
	                    result += fileList[i].length();
	                }
	            }
	            return result; // return the file size
	        }
	        return 0;
	    } 
	
		public static boolean isUserBuild(){
			
			String sku=SystemProperties.get("ro.build.type","user");
			log("sku="+sku);
			if(sku.equals("user")){
				return true;
			}
			return false;
		}
		
		public static int getDiskSize(String path)
		{
			File data =new File(path); 
	        StatFs sf = new StatFs(path);
	        long availableBlocks = sf.getAvailableBlocksLong();
	        long size = sf.getBlockSizeLong();
	        int value=(int) (availableBlocks*size/1024/1024);
	        return value;
		}
		
		public static long getReminderLowSpace(String path){
			File data =new File(path); 
	        StatFs sf = new StatFs(path);
	        long upbound=  sf.getTotalBytes()/1024/1024/10;
			return upbound;
		}
		
		public static long getReminderLowClose(String path){
			File data =new File(path); 
	        StatFs sf = new StatFs(path);
	        long upbound=  sf.getTotalBytes()/1024/1024/10/2;
			return upbound;
		}
		
		private static void sendCmdBySocket(Context context, String cmd)
		{
			LocalSocket socket = null;
			PrintWriter out = null;
			BufferedReader in = null;
			socket = Util.getSocketInstance(DumpService.SOCKET_NAME);
			if (socket == null)
			{
				return;
			}
			try {
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DumpService.sendCMD(out, in, cmd);
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "socket connect fail " + e.getMessage());
				return;
			}
		}
		
		public static void copyAssetFile( AssetManager assets ,Context context,String srcName,String destPath){
			if(assets==null){
				return;
			}  
			
		   byte buf[] = new byte [1024];
	       int len=0;
	       try 
	       {
	    	   	 FileOutputStream	outputStream = new FileOutputStream(destPath);
		         InputStream inputStream=assets.open(srcName);
				
		         while ((len = inputStream.read(buf)) != -1)
				 {
					
		        	 outputStream.write(buf, 0, len);
				 }
				 inputStream.close();
				 outputStream.flush();
				 outputStream.close();
				File file=new File(destPath);
				file.setExecutable(true, false);
				file.setReadable(true, false);
				file.setWritable(true, false);
	       }
	       catch (IOException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
	       }
	      
			
		}
		
		
		public static boolean isDiskAllowOpen(String path){
			
			if(getDiskSize(path)<getReminderLowSpace(path)){
				return false;
			}
			return true;
		}

		
		private static void updateMtp(Context  context,ContentResolver cr,HashMap<String, FileInfo> hashMap,File file){
			
			String path=file.getAbsolutePath();
			if(file.isDirectory())
			{
				
				
				//int id=UpdateMedia.getFileID(cr,path );
				FileInfo fileInfo=hashMap.remove(path);
				if(fileInfo==null){
					UpdateMedia.addfolder(context.getContentResolver(), path);
				}
				if(file!=null){
					File[] childs=file.listFiles();
					if(childs!=null){
						for(File child:childs){
							updateMtp( context,cr,hashMap ,child);
						}
					}
				}
				
			}else
			{
				FileInfo fileInfo=hashMap.remove(path);
				if(fileInfo==null){
					//log("fileInfo==null");
					UpdateMedia.addFile(context, path);
				}else{
				
					int last=(int) (file.lastModified()/1000);
					if(last!=fileInfo.mLastModifyed){
					//	log("path="+path+",database modified"+fileInfo.mLastModifyed+"/"+last);
						UpdateMedia.deleteMtpFilebyID(cr, fileInfo.mId);
						UpdateMedia.addFile(context, file);
					}
				}
				hashMap.remove(path);	
			}
		}

		public synchronized static boolean setCmd(String cmd){
			File file=new File("/system/bin/logcommand");
			if(file.exists()==false){
				return false;
			}
			for(;;){
				String state=SystemProperties.get("init.svc.logcommand");
				
				if(state!=null && state.equals("running"))
				{
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						// TODO: handle exception
					}
					continue;
				}else{
					break;
				}
				
			}
			//Log.e("==logcommand==",cmd);
			SystemProperties.set("persist.asuslog.logcmd", cmd);
			SystemProperties.set("ctl.start","logcommand");
			return true;
		}
	 
	 public static LocalSocket getSocketInstance(String name){
			
			LocalSocket socket=new LocalSocket();
			LocalSocketAddress address = new LocalSocketAddress(name,LocalSocketAddress.Namespace.RESERVED);
			 try {
					socket.connect(address);
					
			} catch (IOException e) {					
					e.printStackTrace();
					Log.e(TAG,"socket connect fail "+e.getMessage());
					return null;
			}
			return socket;
		}

}
