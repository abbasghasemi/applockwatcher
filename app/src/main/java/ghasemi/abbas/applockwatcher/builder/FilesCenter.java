package ghasemi.abbas.applockwatcher.builder;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FilesCenter {

    public final static File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

    public static void hiddenFile(File file, String type) {
        if (type.equals("file")) {
            String[] ext = file.getName().split("\\.");
            if (ext.length > 1) type = FilesCenter.getTypeFile(ext[1]);
        }
        if (file.exists()) {
            File to = new File(file.getParent(), "." + file.getName() + ".lock");
            file.renameTo(to);
        }
        saveFile(file, type);
    }

    private static void saveFile(final File file, final String type) {
        AppStatus.open().unlockFile(file.getAbsolutePath());
        AppStatus.open().lookFile(file.getAbsolutePath(), type);
    }

    public static void showFile(final File file) {
        File from = new File(file.getParent(), "." + file.getName() + ".lock");
        if (from.exists()) {
            from.renameTo(file);
        }
        AppStatus.open().unlockFile(file.getAbsolutePath());
    }

    public static File createHiddenFile(File file) {
        return new File(file.getParent(), "." + file.getName() + ".lock");
    }

    public static Single<List<FileParser>> allFiles(String type) {
        return Single.create((SingleOnSubscribe<List<FileParser>>) emitter -> {
            List<FileParser> list = AppStatus.open().file(type);
            if (!emitter.isDisposed()) emitter.onSuccess(list);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<FileParser>> collectionsFiles(File root, String type, boolean filter) {
        return Observable.create((ObservableOnSubscribe<List<FileParser>>) emitter -> {
            if (type.equals("file")) {
                List<FileParser> files = FilesCenter.allFiles(root);
                if (!emitter.isDisposed()) {
                    emitter.onNext(files);
                }
            } else {
                receiveCollectionsFiles(emitter, root, type, filter);
            }
            if (!emitter.isDisposed()) emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static void receiveCollectionsFiles(ObservableEmitter<List<FileParser>> emitter, File root, String type, boolean filter) {
        if (emitter.isDisposed()) return;
        boolean directoryChecking = true;
        File[] listFile = root.listFiles();
        List<FileParser> fileParsers = new ArrayList<>();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    if (filter) {
                        continue;
                    }
                    receiveCollectionsFiles(emitter, file, type, false);
                } else if (directoryChecking) {
                    String name = file.getName();
                    if (validateFileFormat(name, type)) {
                        FileParser fp = new FileParser();
                        if (filter) {
                            fp.mFile = file;
                        } else {
                            directoryChecking = false;
                            fp.mFile = root;
                        }
                        fileParsers.add(fp);
                    }
                }
            }
        }
        if (!emitter.isDisposed()) emitter.onNext(fileParsers);
    }

    private static boolean validateFileFormat(String name, String type) {
        switch (type) {
            case "video":
                return name.endsWith(".mp4") || name.endsWith(".wmv") || name.endsWith(".mkv") || name.endsWith(".avi") || name.endsWith(".3gp");
            case "audio":
                return name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".aac") || name.endsWith(".m4a") || name.endsWith(".wav") || name.endsWith(".flac");
            case "image":
                return name.endsWith(".jpeg") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
        }
        return false;
    }

    public static List<FileParser> allFiles(File root) {
        List<FileParser> fileList = new ArrayList<>();
        File[] f = root.listFiles();
        if (f == null || f.length == 0) {
            return fileList;
        }
        for (File file : f) {
            if (file.getName().charAt(0) != '.') {
                FileParser fp = new FileParser();
                fp.mFile = file;
                fileList.add(fp);
            }
        }
        Collections.sort(fileList, (o1, o2) -> {
            boolean o11 = o1.mFile.isDirectory();
            boolean o12 = o2.mFile.isDirectory();
            if (o11 == o12) {
                return o1.mFile.getName().substring(0, 1).toLowerCase().compareTo(o2.mFile.getName().substring(0, 1).toLowerCase());
            } else if (o11) {
                return -1;
            } else {
                return 1;
            }
        });
        return fileList;
    }

    public static void findFileHidden(File root) {
        File[] listFile = root.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    findFileHidden(file);
                } else {
                    String name = file.getName();
                    if (name.startsWith(".") && name.endsWith(".lock")) {
                        String[] n = name.split("\\.");
                        String type = FilesCenter.getTypeFile(n[n.length - (n.length > 1 ? 2 : 1)]);
                        saveFile(new File(file.getParent(), file.getName().substring(1).replace(".lock", "")), type);
                    }
                }

            }
        }
    }

    public static int countItemWithFilter(File root, String type) {
        int count = 0;
        File[] l = root.listFiles();
        if (l == null) {
            return 0;
        }
        if (type.equals("file")) {
            for (File file : l) {
                if (file.getName().charAt(0) != '.') {
                    count++;
                }
            }
            return count;
        }
        for (File file : l) {
            String[] n = file.getName().split("\\.");
            if (!file.isDirectory() && type.equals(FilesCenter.getTypeFile(n[n.length - 1]))) {
                count++;
            }
        }
        return count;
    }

    private static final int[] documentIcons = {R.drawable.media_doc_blue, R.drawable.media_doc_green, R.drawable.media_doc_red, R.drawable.media_doc_yellow};

    private static final int[] documentMediaIcons = {R.drawable.media_doc_blue_b, R.drawable.media_doc_green_b, R.drawable.media_doc_red_b, R.drawable.media_doc_yellow_b};

    public static int getThumbForNameOrMime(String name, boolean media) {
        if (name != null && name.length() != 0) {
            int color = -1;
            if (name.contains("doc") || name.contains("txt") || name.contains("psd")) {
                color = 0;
            } else if (name.contains("xls") || name.contains("csv")) {
                color = 1;
            } else if (name.contains("pdf") || name.contains("ppt") || name.contains("key")) {
                color = 2;
            } else if (name.contains("zip") || name.contains("rar") || name.contains("ai") || name.contains("mp3") || name.contains("mov") || name.contains("avi")) {
                color = 3;
            }
            if (color == -1) {
                int idx;
                String ext = (idx = name.lastIndexOf('.')) == -1 ? "" : name.substring(idx + 1);
                if (ext.length() != 0) {
                    color = ext.charAt(0) % documentIcons.length;
                } else {
                    color = name.charAt(0) % documentIcons.length;
                }
            }
            return media ? documentMediaIcons[color] : documentIcons[color];
        }
        return media ? documentMediaIcons[0] : documentIcons[0];
    }

    public static String getTypeFile(String name) {
        switch (name) {
            case "jpeg":
            case "jpg":
            case "png":
            case "gif":
            case "ico":
            case "tif":
            case "tiff":
                return "image";
            case "mp4":
            case "wmv":
            case "mkv":
            case "avi":
            case "3gp":
                return "video";
            case "mp3":
            case "ogg":
            case "ogx":
            case "aac":
            case "m4a":
            case "wav":
            case "flac":
                return "audio";
            default:
                return "file";
        }
    }

    public static void openFile(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(ApplicationLoader.context, ApplicationLoader.context.getPackageName() + ".fileProvider", file);
        String mime = ApplicationLoader.context.getContentResolver().getType(uri);
        if (filePath.endsWith(".lock")) {
            String[] fileExt = filePath.substring(0, filePath.length() - 5).split("\\.");
            switch (getTypeFile(fileExt[fileExt.length - 1])) {
                case "image":
                    mime = "image/png";
                    break;
                case "video":
                    mime = "video/mp4";
                    break;
                case "audio":
                    mime = "audio/mp4";
                    break;
                default:
                    switch (fileExt[fileExt.length - 1]) {
                        case "rar":
                        case "zip":
                        case "7z":
                        case "tar":
                        case "gz":
                            mime = "application/zip";
                            break;
                        case "doc":
                        case "docx":
                            mime = "application/msword";
                            break;
                        case "ppt":
                        case "pptx":
                            mime = "application/vnd.ms-powerpoint";
                            break;
                        case "csv":
                        case "xls":
                        case "xlsx":
                            mime = "application/vnd.ms-excel";
                            break;
                        case "pdf":
                        case "xps":
                            mime = "application/pdf";
                            break;
                        case "txt":
                        case "xml":
                        case "htm":
                        case "html":
                        case "php":
                            mime = "text/html";
                            break;
                        case "json":
                            mime = "application/json";
                            break;
                        case "svg":
                            mime = "application/image/svg+xml";
                            break;
                    }
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ApplicationLoader.context.startActivity(intent);
        } catch (Exception e) {
            //
        }
    }
}
