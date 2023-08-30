package ghasemi.abbas.applockwatcher.builder;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.Nullable;
import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.R;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppStatus extends SQLiteOpenHelper {

    private static AppStatus appStatus;

    private AppStatus(@Nullable Context context) {
        super(context, "AppLock", null, 1);
    }

    public static AppStatus open() {
        if (appStatus == null) appStatus = new AppStatus(ApplicationLoader.context);
        return appStatus;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table apps (id INTEGER PRIMARY KEY AUTOINCREMENT, packageId TEXT, lockStatus INTEGER DEFAULT 1,unlockedAt TEXT DEFAULT '0')");
        db.execSQL("create table logins (ID INTEGER PRIMARY KEY AUTOINCREMENT," + "packageId" + " TEXT," + "loginStatus" + " INTEGER," + "loginAt" + " TEXT" + ")");
        db.execSQL("create table recordedImages (ID INTEGER PRIMARY KEY AUTOINCREMENT," + "packageId" + " TEXT," + "imagePath" + " TEXT," + "takenAt" + " TEXT" + ")");
        db.execSQL("create table files (ID INTEGER PRIMARY KEY AUTOINCREMENT," + "filePath" + " TEXT," + "fileType" + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean has() {
        return has(null);
    }

    public boolean has(@Nullable String packageId) {
        return info(packageId) != null;
    }

    public void add(@Nullable String packageId) {
        if (!has(packageId)) {
            ContentValues values = new ContentValues();
            values.put("packageId", packageId);
            getWritableDatabase().insert("apps", null, values);
        }
    }

    public void remove(@Nullable String packageId) {
        getWritableDatabase().delete("apps", "packageId = ?", new String[]{packageId});
    }

    public Boolean info(@Nullable String packageId) {
        Boolean bool = null;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT lockStatus,unlockedAt FROM apps WHERE " + (packageId == null ? "1 = ?" : "packageId = ?"), new String[]{packageId == null ? "1" : packageId});
        if (cursor.moveToFirst()) {
            boolean lockStatus = cursor.getInt(0) == 1;
            long unlockedAt = Long.parseLong(cursor.getString(1));
            long diff = Math.abs(System.currentTimeMillis() - unlockedAt);
            if (!lockStatus) {
                diff /= 1000;
                if (diff > 2 * 60 * 60) {
                    lockStatus = true;
                    update(packageId, true);
                }
            } else {
                if (diff < 1000) {
                    lockStatus = false;
                }
            }
            bool = lockStatus ? Boolean.TRUE : Boolean.FALSE;
        }
        cursor.close();
        return bool;
    }

    public void update(@Nullable String packageId, boolean lock) {
        ContentValues values = new ContentValues();
        values.put("lockStatus", lock ? 1 : 0);
        values.put("unlockedAt", String.valueOf(System.currentTimeMillis()));
        getWritableDatabase().update("apps", values, packageId == null ? "lockStatus = ?" : "packageId = ?", new String[]{packageId == null ? "0" : packageId});
    }


    public void addHistory(String appId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packageId", appId);
        values.put("loginStatus", Integer.parseInt(status));
        DateHelper.YearMonthDate helper = DateHelper.getCurrentJalaliDate();
        values.put("loginAt",helper.toString());
        db.insert("logins", null, values);
        db.close();
    }

    public Single<ArrayList<HashMap<String, Object>>> getHistory() {
        return Single.create((SingleOnSubscribe<ArrayList<HashMap<String, Object>>>) emitter -> {
            ArrayList<HashMap<String, Object>> lists = new ArrayList<>();
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from logins ORDER BY ID DESC", null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Object> list = new HashMap<>();
                    String packageId = cursor.getString(1);
                    try {
                        PackageManager packageManager = ApplicationLoader.context.getPackageManager();
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageId, 0);
                        list.put("name", packageManager.getApplicationLabel(applicationInfo));
                        list.put("icon", packageManager.getApplicationIcon(packageId));
                    } catch (PackageManager.NameNotFoundException e) {
                        list.put("name", packageId);
                        list.put("icon", ApplicationLoader.context.getResources().getDrawable(R.drawable.fingerprint_dialog_error));
                    }
                    list.put("type", String.valueOf(cursor.getInt(2)));
                    list.put("date", cursor.getString(3));
                    lists.add(list);
                } while (cursor.moveToNext());
            }
            sqLiteDatabase.close();
            cursor.close();
            if (emitter.isDisposed()) return;
            emitter.onSuccess(lists);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteHistory() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete("logins", null, null);
        sqLiteDatabase.close();
    }

    public void lookFile(String path, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("filePath", path);
        values.put("fileType", type);
        db.insert("files", null, values);
        db.close();
    }

    public ArrayList<FileParser> file(String type) {
        ArrayList<FileParser> lists = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor;
        if (type.equals("file")) {
            cursor = sqLiteDatabase.rawQuery("select filePath from files", null);
        } else {
            cursor = sqLiteDatabase.rawQuery("select filePath from files where fileType = ?", new String[]{type});
        }
        if (cursor.moveToFirst()) {
            do {
                FileParser fileParser = new FileParser();
                fileParser.mFile = new File(cursor.getString(0));
                fileParser.selected = true;
                lists.add(fileParser);
            } while (cursor.moveToNext());
        }
        sqLiteDatabase.close();
        cursor.close();
        return lists;
    }

    public void unlockFile(String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete("files", "filePath = ?", new String[]{path});
        sqLiteDatabase.close();
    }

    public void addImgfoucault(String appId, Bitmap bitmap) throws Exception {
        Random random = new Random();
        String u = saveAndGetName(bitmap, "" + random.nextInt(999999999));
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imagePath", u);
        contentValues.put("packageId", appId);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);dateFormat.format(new Date())
        DateHelper.YearMonthDate helper = DateHelper.getCurrentJalaliDate();
        contentValues.put("takenAt", helper.toString());
        sqLiteDatabase.insert("recordedImages", null, contentValues);
        sqLiteDatabase.close();

    }

    public Single<ArrayList<HashMap<String, Object>>> getImgSave() {
        return Single.create((SingleOnSubscribe<ArrayList<HashMap<String, Object>>>) emitter -> {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ArrayList<HashMap<String, Object>> lists = new ArrayList<>();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from recordedImages order by ID desc", null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Object> list = new HashMap<>();
                    list.put("id", cursor.getInt(0));
                    String packageId = cursor.getString(1);
                    try {
                        PackageManager packageManager = ApplicationLoader.context.getPackageManager();
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageId, 0);
                        list.put("name", packageManager.getApplicationLabel(applicationInfo));
                        list.put("icon", packageManager.getApplicationIcon(packageId));
                    } catch (PackageManager.NameNotFoundException e) {
                        list.put("name", packageId);
                        list.put("icon", ApplicationLoader.context.getResources().getDrawable(R.drawable.fingerprint_dialog_error));
                    }
                    list.put("img", cursor.getString(2));
                    list.put("date", cursor.getString(3));
                    lists.add(list);
                } while (cursor.moveToNext());
            }
            sqLiteDatabase.close();
            cursor.close();
            if (emitter.isDisposed()) return;
            emitter.onSuccess(lists);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteImgSave(int id, String url) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        if (id == -1) {
            sqLiteDatabase.delete("recordedImages", null, null);
            File file = new File(ApplicationLoader.context.getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath());
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                sqLiteDatabase.close();
                return;
            }
            for (File child : files) {
                child.delete();
            }
            file.delete();
        } else {
            sqLiteDatabase.delete("recordedImages", "ID = ?", new String[]{String.valueOf(id)});
            new File(url).delete();
        }
        sqLiteDatabase.close();
    }

    private String saveAndGetName(Bitmap bitmap, String name) throws Exception {
        String stringBuilder = ApplicationLoader.context.getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath();
        File file = new File(new File(stringBuilder), "");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(new File(stringBuilder), name + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        OutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file.getAbsolutePath();
    }
}