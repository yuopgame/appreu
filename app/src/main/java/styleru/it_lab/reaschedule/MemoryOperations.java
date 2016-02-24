package styleru.it_lab.reaschedule;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MemoryOperations {
    public static final String DEBUG_TAG = "MEMORY_OPERATIONS_DEBUG";

    /*SHARED PREFERENCES*/
    public static void putSharedPreferences(Context context, int ID, String name, String who)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.shared_preferences_id_key), ID);
        editor.putString(context.getString(R.string.shared_preferences_name_key), name);
        editor.putString(context.getString(R.string.shared_preferences_who_key), who);
        editor.commit();

        Log.i(DEBUG_TAG, "Preferences have been created and commited");
    }

    public static Map<String, String> getSharedPreferences(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE
        );
        int ID = sharedPref.getInt(context.getString(R.string.shared_preferences_id_key), 0);
        String name = sharedPref.getString(context.getString(R.string.shared_preferences_name_key), "");
        String who = sharedPref.getString(context.getString(R.string.shared_preferences_who_key), "");

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("ID", Integer.toString(ID));
        returnMap.put("name", name);
        returnMap.put("who", who);

        return returnMap;
    }



    /*РАБОТА С SQLite*/

    public static class ScheduleDBHelper extends SQLiteOpenHelper {

        public static final String DATABASE_TABLE_GROUPS = "sch_groups";
        public static final String MEMBERS_NAME_COLUMN = "name";
        public static final String MEMBERS_ID_COLUMN = "id";

        public static final String DATABASE_TABLE_LECTORS = "sch_lectors";

        public ScheduleDBHelper (Context context)
        {
            super(context, "scheduleDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("create table " + DATABASE_TABLE_GROUPS + "("
                    + "id integer primary key,"
                    + MEMBERS_NAME_COLUMN + " text" + ");");

            db.execSQL("create table " + DATABASE_TABLE_LECTORS + "("
                    + "id integer primary key,"
                    + MEMBERS_NAME_COLUMN + " text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE_GROUPS);
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE_LECTORS);
            // Создаём новую таблицу
            onCreate(db);
        }

    }

    public static void DBMembersSet(Context context, Map<Integer, String> members, String table_name)
    {
        if (members.isEmpty())
            return;

        Runnable setMembersRunnable = new MembersDBAddRunnable(context, members, table_name);
        AsyncTask.execute(setMembersRunnable);
    }

    public static Map<Integer, String> DBMembersGet(Context context, String table_name)
    {
        Map<Integer, String> members = new HashMap<Integer, String>();

        ScheduleDBHelper dbHelper = new ScheduleDBHelper(context);
        SQLiteDatabase membersDB = dbHelper.getWritableDatabase();

        Cursor c = membersDB.query(table_name, null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");

            do {
                int id = c.getInt(idColIndex);
                String name = c.getString(nameColIndex);
                members.put(id, name);

                Log.d(DEBUG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                        ", name = " + c.getString(nameColIndex));
            } while (c.moveToNext());
        } else
            Log.d(DEBUG_TAG, "0 rows");

        c.close();
        membersDB.close();

        return members;
    }

    private static class MembersDBAddRunnable implements Runnable {
        private Context context;
        private Map<Integer, String> members;
        String table_name;

        MembersDBAddRunnable(Context _context, Map<Integer, String> _members, String _table_name) {
            this.context = _context;
            this.members = _members;
            this.table_name = _table_name;
        }

        public void run() {
            ScheduleDBHelper dbHelper = new ScheduleDBHelper(context);
            SQLiteDatabase membersDB = dbHelper.getWritableDatabase();
            try {
                membersDB.beginTransaction();

                for (Map.Entry<Integer, String> e : members.entrySet()) {
                    int key = e.getKey();
                    String value = e.getValue();

                    ContentValues cv = new ContentValues();

                    cv.put(ScheduleDBHelper.MEMBERS_ID_COLUMN, key);
                    cv.put(ScheduleDBHelper.MEMBERS_NAME_COLUMN, value);

                    long rowID = membersDB.insert(table_name, null, cv);
                    Log.d(DEBUG_TAG, "Inserted rowID: " + rowID);
                }

                membersDB.setTransactionSuccessful();
            }
            finally {
                if (membersDB != null && membersDB.inTransaction()) {
                    membersDB.endTransaction();
                    membersDB.close();
                }
            }
        }
    }
}