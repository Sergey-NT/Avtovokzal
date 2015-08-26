package com.www.avtovokzal.org;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.www.avtovokzal.org.Object.AutoCompleteObject;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Версия базы данных
    private static final int DATABASE_VERSION = 5;
    private final static boolean LOG_ON = true;
    // Имя базы данных
    protected static final String DATABASE_NAME = "Avtovokzal";
    // Имена таблицы и полей в базе данных
    private String tableNameNT = "stations";
    private String tableNameEkb = "stations_ekb";
    public String fieldObjectId = "id";
    public String fieldObjectName = "name";
    public String fieldObjectSum = "sum";
    public String fieldObjectCode = "code";

    // Конструктор
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Создание таблицы
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "";
        String sqlEkb = "";

        sql += "CREATE TABLE IF NOT EXISTS " + tableNameNT;
        sql += " ( ";
        sql += fieldObjectId + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
        sql += fieldObjectName + " TEXT, ";
        sql += fieldObjectSum + " INTEGER, ";
        sql += fieldObjectCode + " INTEGER";
        sql += " ) ";

        db.execSQL(sql);

        sqlEkb += "CREATE TABLE IF NOT EXISTS " + tableNameEkb;
        sqlEkb += " ( ";
        sqlEkb += fieldObjectId + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
        sqlEkb += fieldObjectName + " TEXT, ";
        sqlEkb += fieldObjectSum + " INTEGER, ";
        sqlEkb += fieldObjectCode + " INTEGER";
        sqlEkb += " ) ";

        db.execSQL(sqlEkb);
    }

    // При обновлении базы данных произойдет удаление текущих таблиц и создание новых
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + tableNameNT;
        db.execSQL(sql);

        String sqlEkb = "DROP TABLE IF EXISTS " + tableNameEkb;
        db.execSQL(sqlEkb);

        onCreate(db);
    }

    public boolean checkIfExistsRowTable(String tableName) {
        boolean recordExists = false;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT Count(*) FROM " + tableName, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(0) > 0) {
                Log.v("Count row", " " + cursor.getInt(0));
                recordExists = true;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        Log.v("Check Row Table", " " + recordExists);
        return recordExists;
    }

    // Создание новой записи
    // @param myObj содержит данных которые будут добавлены в строку таблицы
    public boolean create(AutoCompleteObject myObj, String tableName) {
        boolean createSuccessful = false;

        if(!checkIfExists(myObj.objectName, tableName)) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(fieldObjectName, myObj.objectName);
            values.put(fieldObjectSum, myObj.objectSum);
            values.put(fieldObjectCode, myObj.objectCode);
            createSuccessful = db.insert(tableName, null, values) > 0;

            db.close();

            if(createSuccessful){
                if (LOG_ON) Log.v("Station", myObj.objectName + " created.");
                if (LOG_ON) Log.v("Station", myObj.objectSum + " created.");
                if (LOG_ON) Log.v("Station", myObj.objectCode + " created");
            }
        }
        return createSuccessful;
    }

    // Проверка существует ли запись
    public boolean checkIfExists(String objectName, String tableName) {
        boolean recordExists = false;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + fieldObjectId + " FROM " + tableName + " WHERE " + fieldObjectName + " = ?", new String[] {objectName});

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                recordExists = true;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return recordExists;
    }

    // Чтение строки из поискового запроса
    public AutoCompleteObject[] read(String searchTerm, String tableName) {
        String sql = "";
        sql += "SELECT " + fieldObjectName + ", " + fieldObjectSum + ", " + fieldObjectCode + " FROM " + tableName;
        sql += " WHERE " + fieldObjectName + " LIKE '%" + searchTerm + "%' OR " + fieldObjectName + " LIKE '%" + Character.toUpperCase(searchTerm.charAt(0)) + searchTerm.substring(1) + "%'";
        sql += " ORDER BY " + fieldObjectSum + " DESC";
        sql += " LIMIT 0,10";

        SQLiteDatabase db = this.getWritableDatabase();

        // Запуск запроса
        Cursor cursor = db.rawQuery(sql, null);

        int recCount = cursor.getCount();

        AutoCompleteObject[] ObjectItemData = new AutoCompleteObject[recCount];
        int x=0;

        // Цикл по всем строкам с добавлением к списку
        if(cursor.moveToFirst()) {
            do {
                String objectName = cursor.getString(cursor.getColumnIndex(fieldObjectName));
                long objectSum = cursor.getLong(cursor.getColumnIndex(fieldObjectSum));
                long objectCode = cursor.getLong(cursor.getColumnIndex(fieldObjectCode));

                if (LOG_ON) {Log.v("Result", "objectName: " + objectName);}
                if (LOG_ON) {Log.v("Result", "objectSum: " + objectSum);}
                if (LOG_ON) {Log.v("Result", "objectCode: " + objectCode);}

                AutoCompleteObject myObject = new AutoCompleteObject(objectName, objectSum, objectCode);
                ObjectItemData[x] = myObject;
                x++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return ObjectItemData;
    }
}
