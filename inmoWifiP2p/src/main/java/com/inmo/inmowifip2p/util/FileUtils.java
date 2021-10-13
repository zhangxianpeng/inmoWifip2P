package com.inmo.inmowifip2p.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    public static final String DEFAULT_PATH = Environment.DIRECTORY_DOWNLOADS;

    /**
     * 获取指定文件夹下的所有文件
     *
     * @return 文件夹路径
     */
    public static String geTransferFilePath(Context context) {
        String filePath = context.getExternalFilesDir(null).getAbsolutePath() + "/FileTransfer";
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            boolean mkdirs = dirFile.mkdirs();
            if (!mkdirs) {
                Log.i(TAG, "需要创建创建：" + mkdirs);
            } else {
                Log.i(TAG, "创建成功");
            }
        }
        return filePath;
    }

    /**
     * 获取图片和视频文件
     *
     * @param path 路径
     * @return 文件列表
     */
    public static Vector<String> getImageAndVideo(String path) {
        Vector<String> fileNames = new Vector<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (isVideo(files[i]) || isImage(files[i])) {
                    fileNames.add(files[i].getAbsolutePath());
                }
            }
        }
        return fileNames;
    }


    /**
     * 获取目录下所有文件(按时间排序)
     *
     * @param path
     * @return
     */
    public static List<File> listFileSortByModifyTime(String path) {
        List<File> list = getFiles(path, new ArrayList<File>());
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        }
        return list;
    }

    /**
     * 获取目录下所有文件
     *
     * @param realpath
     * @param files
     * @return
     */
    private static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    if (!file.getName().contains("course1") && isVideo(file) || isImage(file)) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    public static boolean isVideo(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return file.getName().endsWith(".mp4");
    }

    public static List<File> isVideo(List<File> src) {
        List<File> result = new ArrayList<>();
        for (int i = 0; i < src.size(); i++) {
            File file = src.get(i);
            if (file != null && file.getName().endsWith(".mp4")) {
                result.add(file);
            }
        }
        return result;
    }

    public static boolean isImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")
                || file.getName().endsWith(".gif");
    }


    public static boolean isVideo(String file) {
        if (TextUtils.isEmpty(file)) {
            return false;
        }
        return file.endsWith(".mp4");
    }

    public static boolean isImage(String file) {
        if (TextUtils.isEmpty(file)) {
            return false;
        }
        return file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".png") || file.endsWith(".gif");
    }

    public static boolean isGif(String file) {
        if (TextUtils.isEmpty(file)) {
            return false;
        }
        return file.endsWith(".gif");
    }

    public static boolean deleteFile(File file) {
        boolean result = false;
        if (file != null && file.exists()) {
            file.delete();
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static boolean deleteFiles(List<File> fileList) {
        boolean result = false;
        for (File file : fileList) {
            if (file != null && file.exists()) {
                file.delete();
            }
            result = true;
        }
        return result;
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = true;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getFileType(String filePath) {
        int start = filePath.lastIndexOf(".");
        if (start != -1) {
            return filePath.substring(start + 1);
        } else {
            return null;
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }
}
