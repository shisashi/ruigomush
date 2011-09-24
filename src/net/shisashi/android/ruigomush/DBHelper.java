/**
 * DBHelper
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public final class DBHelper {
    public static final File DB_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "ruigomush");
    public static final File DB_FILE = new File(DB_DIRECTORY, "dictionary.db");
    public SQLiteDatabase db = null;

    public List<Synset> search(String query) {
        List<Synset> result = new ArrayList<Synset>();
        Pattern tab = Pattern.compile("\\t");

        try {
            Log.i("RUIGO", query);
            SQLiteDatabase db = openDatabase();
            String sql = "SELECT id, definition, words FROM synset WHERE id IN (SELECT sid FROM wordset WHERE word=?);";

            Cursor cursor = db.rawQuery(sql, new String[] { query });
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String definition = cursor.getString(1);
                String words = cursor.getString(2);
                result.add(new Synset(id, definition, tab.split(words)));
            }
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    public synchronized void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (db != null && db.isOpen()) {
            return db;
        }
        db = SQLiteDatabase.openDatabase(DB_FILE.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        return db;
    }
}
