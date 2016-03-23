package com.asus.tool;

import java.io.File;
import java.io.IOException;



import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class UpdateMedia {
	public 	static String FORMAT="format"; 
	
	private static Uri mUri = MediaStore.Files.getContentUri("external");
	
	private static final String TAG = "UpdateMedia";

	
	
	public static void addRecursiveFile(Context context,File newFile,boolean dir)
	{
		
		if(dir)
		{
			String path="";
			try {
				path=newFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				path=newFile.getAbsolutePath();
			}
			
			
			addfolder(context,newFile);
			if (dir) {
                File[] file = newFile.listFiles();
                for (int i = 0; file != null && i < file.length; i++)
                	addRecursiveFile(context,file[i] ,true);
            }
		}else{
			try {
				if(newFile.isFile()){
					Uri u = Uri.parse("file://" + newFile.getCanonicalPath());
					context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, u));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "error="+e.getMessage());
			}
			
		}
		
		
	}
	
	public static void addFile(Context context,File newFile){
		try {
			if(newFile!=null && newFile.isFile() && newFile.exists()){
				Uri u = Uri.parse("file://" + newFile.getCanonicalPath());
				context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, u));
			}
		} catch (Exception e) {
			// TODO: handle exception
			
		}
		
	}
	
	public static void addFile(Context context,String path){
		try {
			Uri u = Uri.parse("file://" + path);
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, u));
		} catch (Exception e) {
			
		}
		
	}
	
//	private boolean addFolder(Context context,File file) throws IOException {
//		ContentResolver cr=context.getContentResolver();
//        ContentValues values = new ContentValues();
//        String path = file.getCanonicalPath();
//        values.put(MediaStore.Files.FileColumns.DATA, path);
//        values.put(Files_FileColumns_FORMAT, FORMAT_ASSOCIATION);
//        values.put(MediaStore.Files.FileColumns.TITLE, file.getName());
//        values.put(MediaStore.Files.FileColumns.PARENT, getFileID(context,file.getParentFile()));
//        if (cr.update(mUri, values, MediaStore.Files.FileColumns.DATA + "=?", new String[] {path}) == 0) {
//        	cr.insert(mUri, values);
//        }
//        return true;
//    }
	public static synchronized void addfolder(Context context,File file ){
		String path="";
		ContentResolver cr=context.getContentResolver();
		try {
			path=file.getCanonicalPath();
		} catch (Exception e) {
			// TODO: handle exception
			path=file.getAbsolutePath();
		}
		addfolder( cr,  path );
	}
	
	public static synchronized int deleteMtpFilebyID(ContentResolver cr,int id){
		return cr.delete(mUri, MediaStore.Files.FileColumns._ID+"=?", new String[]{ Integer.toString(id) });
	}
  
	
	public static synchronized void addfolder(ContentResolver cr,String  path )
	{
		path=getValidatePath( path);
		ContentValues values=new ContentValues();
		values.put(MediaStore.Files.FileColumns.DATA, path);
		
		//Log.v(TAG,  "field"+getFileID(cr,path)+",path="+path);
		
		int parentID=getFileID(cr,getParentPath( path));
		if(parentID==-1){
			return;
		}
		values.put(MediaStore.Files.FileColumns.PARENT, parentID);
		values.put(FORMAT, MtpConstants.FORMAT_ASSOCIATION);
		values.put(MediaStore.Files.FileColumns.TITLE,getName( path) );
		
		
		String[] selectionArgs=new String[]{path};
		log("path="+path);
		
		if(cr.update(mUri, values, MediaStore.Files.FileColumns.DATA+" =?", selectionArgs)==0){
			cr.insert(mUri, values);
		}
		//dump( path);
	}
	
	public static String getName(String path){
		if (path == null || path.length() == 0 || path.length() == 1) {
			return null;
		}
		if (path.substring(path.length() - 1, path.length()).equals("/")) {
			path = path.substring(0, path.length() - 1);
		}
		path=getValidatePath( path);
		String parent = getParentPath( path);
		return path.substring(parent.length()+1);
		
	}
	
	public static String getValidatePath(String path){
		if (path.substring(path.length() - 1, path.length()).equals("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	
	public static String getParentPath(String path) {
		if (path == null || path.length() == 0 || path.length() == 1) {
			return null;
		}
		
		path=getValidatePath( path);
		int lastindex = path.lastIndexOf("/");

		if (lastindex >= 0) {
			String result =path.substring(0, lastindex);
		
			log("parent="+result);
			if (result.length() == 0) {
				return "/";
			} else {
				return result;
			}

		} else {
			return "";
		}
	}
	
	
	 public static void deleteRecusiveFiles(ContentResolver cr,String oldPath,boolean dir,boolean keep_dir){
			int id=getFileID(cr,oldPath);
			if(id==-1){
				return;
			}
			if(id!=0){
				deleteFileID(cr,id, dir,keep_dir);
			}
		}
		
	 public static int getFileID(ContentResolver cr,String canonicalPath) 
	 {
		 String path=canonicalPath;
		 if(path.startsWith("/sdcard")){
			String ecternelString=Environment.getExternalStorageDirectory().getAbsolutePath();
			path=path.replace("/sdcard", ecternelString);
		 }
		 
		 if(canonicalPath.length()>1 && canonicalPath.substring(canonicalPath.length()-1, canonicalPath.length()).equals("/")){
				canonicalPath=canonicalPath.substring(0,canonicalPath.length()-1);
			}
			String selection=MediaStore.Files.FileColumns.DATA+"=?";
			String[] selectionArgs=null;
			try {
				selectionArgs=new String[]{canonicalPath};
			} catch (Exception e) {
				
				log("getFileID error="+e.getMessage());
			}
			
			Cursor cursor=cr.query(mUri, new String[]{MediaStore.Files.FileColumns._ID}, selection, selectionArgs, null);
			int result=0;
			if(cursor==null){
				return -1;
			}
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
	       
	        log("deleteFileID id="+id+",dir="+dir);
	        
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
	
	private static void log(String msg) {
		Log.v(TAG, msg);
		
	}
}
