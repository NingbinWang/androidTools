package com.asus.loguploader.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.util.Log;

/**
 * This FileUtils support some file operations. (ex. copy, del)
 *
 * -------------------
 *
 * At present, You can use this utilities to:
 *
 * 1. copy file or folder :
 *
 *      Note. you also can use 3 callback functions:
 *      a. setOnAllOpEndListener      : call by all operations end
 *      b. setOnFileStaryCopyListener : call by a file start to copy
 *      c. setOnFileCopiedListener    : call by a file copy end
 *
 *      This function include interrupt feature, you can use
 *          'interruptCopy()' to set the interrupt flag true for interrupt.
 *
 *      This function include a progress information, you can use
 *          'getFileProgress()' to get a progress double value (0.0 ~ 1.0).
 *      The progress value will not reset to 0.0 while copying end,
 *          the progress value only set to 0.0 while starting to copy.
 *      You can use 'resetFileProgress()' to reset it to 0.0.
 *
 *      If you want to get the File information that are copying, you can use
 *          'getProgressSrcFile()' and 'getProgressDesFile()', they will return
 *          the File for you to get informations about them.
 *
 *      You can use 'setBufferSize()' to adjust buffer size.
 *      Note: The size only be changed on next copying operation.
 *          If a copying is running currently, call this function only change
 *          the mBufferSize value, the buffer size will be changed on next copying operation.
 *
 * 2. delete file or folder
 *
 *      This function include interrupt feature, you can use
 *      'interruptDel()' to set the interrupt flag true for interrupt.
 *
 * -------------------
 *
 * @author Jason_Uang
 *
 */
public final class FileUtils {

    static final boolean DEBUG = true;
    static final String TAG = "CopyFile_Util";

    // interrupt Copy & Del Flag, if true for interrupt operation.
    private boolean mInterruptCopyFlag = false;
    private boolean mInterruptDelFlag = false;
    // Because on destination existed case, remove them when copying
    // interrupted, will cause original existed file or folder disappear. So we
    // don't use this feature temporary :
    //      Temporary reserve the destinationPath path, for removing them when
    //      copying operation is interrupted.
    //      private String mDestinationPath = null;

    // for progress
    private double mCopyFileProgress = 0.0;
    private File mCopyingSrcFile = null;
    private File mCopyingDesFile = null;

    // for callback function
    protected OnAllOpEndListener mOnAllOpEndListener;
    protected OnFileStaryCopyListener mOnFileStaryCopyListener;
    protected OnFileCopiedListener mOnFileCopiedListener;

    private int mBufferSize = 2048; // the default cluster size of FAT32 is 32KB

    /**
     * Delete :
     *
     * This function is responsible for delete file or folder.
     *
     * The algorithm is:
     *
     * 1. if @fileOrDirectoryPath is a Directory, recursively running del() for
     * every childFile.
     *
     * 2. if @fileOrDirectoryPath isn't a directory, delete it, and return
     * result(true or false).
     *
     * Note that "File.delete()" method does not throw IOException on failure.
     * Callers must check the return value. So, if someone fails to delete, the
     * return "flag" will be false.
     *
     * @param fileOrDirectoryPath
     * @return True : all folders and files in Path are deleted successfully.
     *         False : some folders or files in Path aren't deleted
     *         successfully.
     * @throws InterruptedException
     */
    public boolean del(String fileOrDirectoryPath) throws InterruptedException {
        checkDelInterrupt();
        if (fileOrDirectoryPath != null) {
            File fileOrDirectory = new File(fileOrDirectoryPath);
            boolean flag = true;
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    checkDelInterrupt();
                    flag = flag & del(child.getAbsolutePath());
                }
                flag = flag & fileOrDirectory.delete();
            } else {
                if (mInterruptDelFlag != true) {
                    flag = fileOrDirectory.delete();
                }
            }
            return flag;
        } else {
            return false;
        }
    }

    /**
     * Copy :
     *
     * Because on destination existed case, remove them when copying
     * interrupted, will cause original existed file or folder disappear. So we
     * don't use this feature temporary : (This function will record destination
     * path (with 'mDestinationPath' variable) in order to delete them when a
     * exception thrown. Finally the 'mDestinationPath' variable will be null.)
     *
     * This function use "recursiveCopy" to copy file or folder recursively. You
     * can see "recursiveCopy()" for more information about copying operation.
     *
     * @param from
     * @param to
     * @return
     * @throws FileNotFoundException
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean copy(String from, String to) throws FileNotFoundException, InterruptedException, IOException {
        boolean result = false;
        /*if (mDestinationPath == null) {
            mDestinationPath = to;
        }*/
        long StartTime = System.currentTimeMillis(); // for count process time
        try {
            result = recursiveCopy(from, to);
        } catch (FileNotFoundException e) {
            //del(mDestinationPath);
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            //del(mDestinationPath);
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            //del(mDestinationPath);
            e.printStackTrace();
            throw e;
        } finally {
            //mDestinationPath = null;
        }
        long ProcessTime = System.currentTimeMillis() - StartTime;
        if (mOnAllOpEndListener != null) {
            mOnAllOpEndListener.onAllOpEnd();
        }
        if (DEBUG)
            Log.i(TAG,
                    "All operations end : " + String.valueOf(ProcessTime) + " msec. '" + from + "' to '" + to + "'");
        return result;
    }

    /**
     * This function is responsible for copying folder or file to destination.
     * you can:
     *
     * 1. copy a File to a File. ex. copy(fromFilePath, toFilePath)
     *
     * 2. copy a File into a Folder. ex. copy(fromFilePath, toFolderPath)
     *
     * 3. copy a Folder into other Folder. (you can't copy a folder to itself)
     * ex. copy(fromFolderPath, toFolderPath)
     *
     * 4. you can't copy a Folder to a existing File.
     *
     * ------------------
     *
     * The rule of copying is :
     *
     * 1. If source is Folder and destination don't exist : create destination
     * folder and then copy source into destination.
     *
     * 2. If source is Folder and destination exist : If destination is File,
     * return false. If destination is Folder, copy source into destination.
     *
     * 3. If source is File and destination don't exist : copy source to
     * destination file. (destination will be a file, not folder. But a
     * exception is: destination is like "a/b/s/", the end has a "/" symbol. If
     * we don't need this feature, we can use 'getPather()' instead of
     * 'getFolderPath()')
     *
     * 4. If source is File and destination exist : If destination is File, copy
     * source to cover the destination. If destination is Folder, copy source
     * into the folder.
     *
     * Note:
     *
     * 1. you can't copy a Folder into itself or into its child folder, this
     * will log a error record, and return false;
     *
     * 2. The incomplete file on stream.write() section, copyFile() will remove
     * it.
     *
     * @param from
     *            : the source path string
     * @param to
     *            : the destination path string
     * @return True : all operations are completed. False : some operations
     *         fail.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    private boolean recursiveCopy(String from, String to) throws FileNotFoundException, IOException, InterruptedException {
        checkCopyInterrupt();
        File srcfileOrDirectory = new File(from);
        File desfileOrDirectory = new File(to);
        boolean flag = true;
        if (srcfileOrDirectory.isDirectory()) {
            // For case, Source: folder & Destination: file, return false.
            if (desfileOrDirectory.exists() && !desfileOrDirectory.isDirectory()) {
                if (DEBUG)
                    Log.e(TAG, "a folder '" + srcfileOrDirectory.getAbsolutePath() + "' can't copy to a existed file '"
                            + desfileOrDirectory.getAbsolutePath() + "'");
                return false;
            }
            // For case, Source: folder & destination: no exist || folder,
            // both case all will copy source into destination folder.
            // The only difference of both case is: if destination folder
            // existed, mkdirs() will return false(but don't care this).
            if (!desfileOrDirectory.exists() || desfileOrDirectory.isDirectory()) {
                // We will change destination for copying file of source folder
                // INTO the destination Folder
                desfileOrDirectory = new File(to + "/" + srcfileOrDirectory.getName());
                // Note, Folder can't copy into itself and it's child folder.
                // We check this rule first. If pass, make dir, otherwise return
                // false.
                String sf = srcfileOrDirectory.getAbsolutePath();
                String df = desfileOrDirectory.getAbsolutePath();
                if (sf.equals(df) || srcfileOrDirectory.getParent().equals(df) || (sf+"/").regionMatches(0, df, 0, sf.length()+1)) {
                    // Above script checks the both folder path, It's true if source is a subset of destination.
                    if (DEBUG)
                        Log.e(TAG, "srouce '" + srcfileOrDirectory.getAbsolutePath()
                                + "' can't be the same with destination '" + desfileOrDirectory.getAbsolutePath()
                                + "' , or recursively copy into itself or it's child folder");
                    return false;
                } else {
                    mkdir(desfileOrDirectory.getAbsolutePath());
                }
            }
            // source and destination are prepared, start copying.
            for (File child : srcfileOrDirectory.listFiles()) {
                flag = flag & recursiveCopy(child.getAbsolutePath(), desfileOrDirectory.getAbsolutePath());
            }
        } else {
            // if destination folder don't exist, create it.
            // Note: In here, only care the part of "Folder" in path and create
            // them.
            if (!desfileOrDirectory.exists()) {
                String desFolderPath = getFolderPath(to);
                //String desFolderPath = desfileOrDirectory.getParent();
                // If 'to' is like "a/b/c/", use getFolderPath() will copy source into "c" FOLDER,
                // use getPather() will copy source to "c" FILE, because getPather() returns "a/b/".
                if (desFolderPath != null) {
                    mkdir(desFolderPath);
                }
            }
            if (desfileOrDirectory.exists() && desfileOrDirectory.isDirectory()) {
                desfileOrDirectory = new File(to + "/" + srcfileOrDirectory.getName());
                flag = flag & copyFile(srcfileOrDirectory.getAbsolutePath(), desfileOrDirectory.getAbsolutePath());
            } else {
                flag = flag & copyFile(srcfileOrDirectory.getAbsolutePath(), desfileOrDirectory.getAbsolutePath());
            }
        }
        return flag;
    }

    /**
     * This function is responsible for copying File purely, not handling Folder.
     *
     * Note:
     *
     * 1. When source and destination are same path, will do nothing and return true.
     *
     * 2. When interrupt occur in Stream.write() section, we will remove
     * incomplete file. The previously copied file we don't remove them.
     *
     * 3. There are 2 callback function in here.
     *
     * @param fromFile
     * @param toFile
     * @return
     * @throws InterruptedException
     * @throws Exception
     */
    private boolean copyFile(String fromFile, String toFile) throws FileNotFoundException, IOException,
            InterruptedException {
        checkCopyInterrupt();
        BufferedInputStream bufferedInputStream;
        BufferedOutputStream bufferedOutputStream;
        File srcFile = new File(fromFile);
        File desFile = new File(toFile);

        if (srcFile.getAbsolutePath().equals(desFile.getAbsolutePath())) {
            if (DEBUG)
                Log.d(TAG, "source and destination are same");
            return true;
        }

        // BufferedInputStream will throw exception if the file does not exist,
        // is a directory rather than a regular file, or for some other reason
        // cannot be opened for reading.
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(srcFile));
        } catch (FileNotFoundException e1) {
            if (DEBUG)
                Log.e(TAG, "Create InputStream Fail. the source maybe was a folder or not exist.", e1);
            e1.printStackTrace();
            throw e1;
        }
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(desFile));
        } catch (FileNotFoundException e1) {
            if (DEBUG)
                Log.e(TAG, "Create OutputStream Fail. the destination maybe was a folder", e1);
            e1.printStackTrace();
            throw e1;
        }

        try {
            byte[] data = new byte[mBufferSize];
            try {
                if (DEBUG)
                    Log.i(TAG,
                            "copy file '" + srcFile.getAbsolutePath() + "' (" + srcFile.length() + ") to '"
                                    + desFile.getAbsolutePath() + "'");
                mCopyingSrcFile = srcFile;
                mCopyingDesFile = desFile;
                mCopyFileProgress = 0.0;
                // If has a listener, invoke it
                if (mOnFileStaryCopyListener != null) {
                    mOnFileStaryCopyListener.onFileStartCopy();
                }
                long StartTime = System.currentTimeMillis(); // for count process time
                long i = 0; // a count for reduce
                while (bufferedInputStream.read(data) != -1) {
                    checkCopyInterrupt();
                    bufferedOutputStream.write(data);
                    if (i++ % 100 == 0) // for reduce load on CPU, so we just compute progress each 100 times.
                        mCopyFileProgress = Math.round(100.0 * (double) desFile.length() / (double) srcFile.length()) / 100.0;
                }
                // Flushes this stream to ensure all pending data is written out to the target stream.
                checkCopyInterrupt();
                bufferedOutputStream.flush();
                checkCopyInterrupt();
                long ProcessTime = System.currentTimeMillis() - StartTime;
                // After copied, set the progress to 1.0
                // Note, the progress don't reset while copied. You must reset it by yourself if you want.
                // The progress is reseted only at start of copying.
                mCopyFileProgress = 1.0;
                // If has a listener, invoke it.
                if (mOnFileCopiedListener != null) {
                    mOnFileCopiedListener.onFileCopied();
                }
                // set mCopyingSrcFile and mCopyingDesFile to Null after OnFileCopiedListener,
                // avoid getProgressSrcFile() or getProgressDesFile() return null on OnFileCopiedListener.
                mCopyingSrcFile = null;
                mCopyingDesFile = null;
                if (DEBUG)
                    Log.i(TAG,
                            "Copy completed : " + String.valueOf(ProcessTime) + " msec. '" + srcFile.getAbsolutePath() + "' (" + srcFile.length() + ") to '"
                                    + desFile.getAbsolutePath() + "'");
            } catch (InterruptedException e) {
                desFile.delete();
                if (DEBUG)
                    Log.i(TAG, "Remove an incomplete file '" + desFile + "', because interrupted.");
                e.printStackTrace();
                throw e;
            } catch (IOException e) {
                desFile.delete();
                if (DEBUG)
                    Log.d(TAG, "Stream.read() or write() fail");
                e.printStackTrace();
                throw e;
            }
            data = null;
            // close Streams
            bufferedInputStream.close();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            bufferedInputStream = null;
            bufferedOutputStream = null;
        }
        return true;
    }

    /**
     * This function is responsible for check interrupt flag,
     *
     * use it before some maybe blocking operations or after, and start of some function.
     * ex.  checkInterrupt();
     *      del();
     *      checkInterrupt();
     *
     * @throws InterruptedException
     */
    private void checkDelInterrupt() throws InterruptedException {
        if (mInterruptDelFlag == true) {
            if (DEBUG)
                Log.i(TAG, "Receive a interrupted flag when deleting.");
            setDelInterrupt(false);
            throw new InterruptedException("The delete operation has been interrupted.");
        }
    }

    /**
     * This function is responsible for check interrupt flag,
     *
     * use it before some maybe blocking operations or after, and start of some function.
     * ex.  checkInterrupt();
     *      bufferedOutputStream.flush();
     *      checkInterrupt();
     *
     * @throws InterruptedException
     */
    private void checkCopyInterrupt() throws InterruptedException {
        if (mInterruptCopyFlag == true) {
            if (DEBUG)
                Log.i(TAG, "Receive a interrupted flag when copying.");
            setCopyInterrupt(false);
            throw new InterruptedException("The copy operation has been interrupted.");
        }
    }

    /**
     * This function is responsible for making folder.
     *
     * Because mkdir is faster than mkdirs, so we use mkdirs only when path isn't a single folder.
     *
     * @param path
     * @return
     * @throws InterruptedException
     */
    public boolean mkdir(String path) throws InterruptedException {
        checkCopyInterrupt();
        boolean mkcheck = true;
        File preCreateFolder = new File(path);
        if (!preCreateFolder.exists() && !preCreateFolder.mkdir()) {
            // If folder don't exist and use 'mkdir' fail to make it, then use 'mkdirs'.
            checkCopyInterrupt();
            mkcheck = preCreateFolder.mkdirs();
            checkCopyInterrupt();
        }
        if (DEBUG) {
            if (mkcheck) {
                Log.i(TAG, "Created folder: '" + preCreateFolder.getAbsolutePath() + "'");
            } else {
                Log.e(TAG, "Created folder fail: '" + preCreateFolder.getAbsolutePath() + "'");
            }
        }
        return mkcheck;
    }

    /**
     * This function is responsible for get Folder Path from a path string.
     *
     * This is example:
     *
     * Path = "/fo1/fo2/f.x": return "/fo1/fo2/"
     * Path = "fo1/fo2/f.x" : return "fo1/fo2/"
     * Path = "/fo1/fo2" : return "/fo1/"
     * Path = "/fo1/fo2/" : return "/fo1/fo2/"
     *
     */
    @TargetApi(9)
	private String getFolderPath(String Path) {
        // This regular expressions includes 2 group, (folder/) and (file)
        // Below, maybe this pattern : "^(.+)/([^/]+)$" is ok, too.
        Pattern p = Pattern.compile("^(.*/)([^/]*)$");
        // Matcher will return a array, for example, "a((b)c)" matching "abc"
        // would give the following groups:
        // 0->"abc" ; 1->"bc" ; 2->"b"
        // for below script, group 1 will be Folder path, group 2 is FileName.
        Matcher m = p.matcher((CharSequence) Path);
        // create folder
        if (m.find() && m.group(1) != null && !m.group(1).isEmpty()) {
            return m.group(1).toString();
            // Note:
            // mkdirs() will create the directory named by the trailing
            // filename of this file, including the complete directory path
            // required to create this directory.
            // Note that this method does not throw IOException on failure.
            // Callers must check the return value.
        }
        return null;
    }

    // Overload copy method
    public boolean copy(File fromFile, File toFile) throws FileNotFoundException, IOException, InterruptedException {
        return copy(fromFile.getAbsolutePath(), toFile.getAbsolutePath());
    }

    // Overload copy method
    public boolean copy(File fromFile, String toFile) throws FileNotFoundException, IOException, InterruptedException {
        return copy(fromFile.getAbsolutePath(), toFile);
    }

    // Overload copy method
    public boolean copy(String fromFile, File toFile) throws FileNotFoundException, IOException, InterruptedException {
        return copy(fromFile, toFile.getAbsolutePath());
    }

    /**
     * Interface definition for a callback to be invoked when all operations end.
     * Note: if result is false, still invoked this callback.
     */
    public interface OnAllOpEndListener {
        // Called when all operations end.
        void onAllOpEnd();
    }

    /**
     * Register a callback to be invoked when all operations end.
     * @param l The callback that will run
     */
    public void setOnAllOpEndListener(OnAllOpEndListener l) {
        mOnAllOpEndListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when a single file start to copy.
     */
    public interface OnFileStaryCopyListener {
        // Called when a file has been copied.
        void onFileStartCopy();
    }

    /**
     * Register a callback to be invoked when a file starts to copy.
     * @param l The callback that will run
     */
    public void setOnFileStaryCopyListener(OnFileStaryCopyListener l) {
        mOnFileStaryCopyListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when a single file is copied.
     */
    public interface OnFileCopiedListener {
        // Called when a file has been copied.
        void onFileCopied();
    }

    /**
     * Register a callback to be invoked when a file is copied.
     * @param l The callback that will run
     */
    public void setOnFileCopiedListener(OnFileCopiedListener l) {
        mOnFileCopiedListener = l;
    }

    public void resetFileProgress() {
        mCopyFileProgress = 0.0;
    }

    public double getFileProgress() {
        return mCopyFileProgress;
    }

    public File getProgressSrcFile() {
        return mCopyingSrcFile;
    }

    public File getProgressDesFile() {
        return mCopyingDesFile;
    }

    /**
     * Adjust buffer size.
     *
     * Note: The size only be changed on next copying operation.
     * If a copying is running currently, call this function only change the mBufferSize value,
     * the buffer size will be changed on next copying operation.
     *
     * @param size
     */
    public void setBufferSize(int size) {
        mBufferSize = size;
    }

    public int getBufferSize() {
        return mBufferSize;
    }

    private void setCopyInterrupt(boolean flag) {
        mInterruptCopyFlag = flag;
    }

    private void setDelInterrupt(boolean flag) {
        mInterruptDelFlag = flag;
    }

    public void interruptCopy() {
        setCopyInterrupt(true);
    }

    public void interruptDel() {
        setDelInterrupt(true);
    }
}
