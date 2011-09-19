/**
 * DBHelper
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public final class DBHelper extends SQLiteOpenHelper {
    public static final File DB_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "ruigomush");
    public static final File DB_FILE = new File(DB_DIRECTORY, "dictionary.db");

    public DBHelper(Context context) {
        super(context, DB_FILE.getAbsolutePath(), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL("CREATE TABLE IF NOT EXISTS synset (id INTEGER PRIMARY KEY, definition TEXT, words TEXT);");
        //db.execSQL("CREATE TABLE IF NOT EXISTS wordset (sid INTEGER, word text, PRIMARY KEY(sid, word), FOREIGN KEY(sid) REFERENCES synset(id))");
        //db.execSQL("CREATE INDEX IF NOT EXISTS wordset_word ON wordset(word)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<Synset> search(String query) {
        List<Synset> result = new ArrayList<Synset>();
        Pattern tab = Pattern.compile("\\t");

        try {
            Log.i("RUIGO", query);
            SQLiteDatabase db = getReadableDatabase();
            String sql = "SELECT id, definition, words FROM synset WHERE id IN (SELECT sid FROM wordset WHERE word=?);";

            Cursor cursor = db.rawQuery(sql, new String[] { query });
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String definition = cursor.getString(1);
                String words = cursor.getString(2);
                result.add(new Synset(id, definition, tab.split(words)));
            }
            db.close();
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }
}