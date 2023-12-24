package com.teratail.q_3mipiypmm6w99c;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.*;
import androidx.lifecycle.*;

import java.util.*;
import java.util.concurrent.*;

interface GradesStorage {
  void save(List<Grades> list);
  LiveData<List<Grades>> load();
}

//本来ならDBアクセスはバックグラウンドで行う
class SQLiteGradesStorage implements GradesStorage, DefaultLifecycleObserver {
  private static final String LOG_TAG = "SQLiteGradesStorage";

  private static final String GRADES_TABLE = "grades";
  private static final String GRADES_COLUMN_SUBJECT = "subject";
  private static final String ELEMENTS_TABLE = "elements";
  private static final String ELEMENTS_COLUMN_GRADES_ID = "grade_id";
  private static final String ELEMENTS_COLUMN_TYPE = "type";
  private static final String ELEMENTS_COLUMN_VALID = "valid";
  private static final String ELEMENTS_COLUMN_WEIGHT = "weight";
  private static final String ELEMENTS_COLUMN_ACHIEVED = "achieved";

  private static DBOpenHelper helper; //シングルトン
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  SQLiteGradesStorage(Context context) {
    if(helper == null) helper = new DBOpenHelper(context);
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    executor.shutdownNow();
  }

  @Override
  public void save(List<Grades> list) {
    SQLiteDatabase db = helper.getWritableDatabase(); //closeしないこと
    db.beginTransaction();
    try {
      //全部消して入れ直し
      db.delete(ELEMENTS_TABLE, null, null);
      db.delete(GRADES_TABLE, null, null);

      ContentValues gradeValues = new ContentValues();
      for(Grades grades : list) {
        gradeValues.put(GRADES_COLUMN_SUBJECT, grades.subjectName);
        long id = db.insert(GRADES_TABLE, null, gradeValues);

        ContentValues elementValues = new ContentValues();
        elementValues.put(ELEMENTS_COLUMN_GRADES_ID, id);
        for(Grades.Type type : Grades.Type.values()) {
          elementValues.put(ELEMENTS_COLUMN_TYPE, type.ordinal());
          Grades.Element element = grades.getElement(type);
          elementValues.put(ELEMENTS_COLUMN_VALID, element.valid ? 1 : 0); //有効=1/無効=0
          elementValues.put(ELEMENTS_COLUMN_WEIGHT, element.weight);
          elementValues.put(ELEMENTS_COLUMN_ACHIEVED, element.achieved);

          db.insert(ELEMENTS_TABLE, null, elementValues);
        }
      }

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public LiveData<List<Grades>> load() {
    MutableLiveData<List<Grades>> gradesListLiveData = new MutableLiveData<>(Collections.emptyList());
    executor.execute(() -> gradesListLiveData.postValue(loadImmediately()));
    return gradesListLiveData;
  }

  private List<Grades> loadImmediately() {
    List<Grades> list = new ArrayList<>();

    SQLiteDatabase db = helper.getReadableDatabase(); //closeしないこと

    try(Cursor gcur = db.query(GRADES_TABLE,
            new String[]{BaseColumns._ID, GRADES_COLUMN_SUBJECT},
            null, //selection
            null, //selectionArgs
            null, null, null)) {
      int idIndex = gcur.getColumnIndex(BaseColumns._ID);
      int subjectIndex = gcur.getColumnIndex(GRADES_COLUMN_SUBJECT);
      while(gcur.moveToNext()) {
        long gradeId = gcur.getLong(idIndex);
        String subject = gcur.getString(subjectIndex);
        Log.d(LOG_TAG, "gradeId=" + gradeId + ", subject=" + subject);

        Grades grades = new Grades(subject);

        try(Cursor ecur = db.query(ELEMENTS_TABLE,
                new String[]{ELEMENTS_COLUMN_TYPE, ELEMENTS_COLUMN_VALID, ELEMENTS_COLUMN_WEIGHT, ELEMENTS_COLUMN_ACHIEVED},
                ELEMENTS_COLUMN_GRADES_ID + "=?", //selection
                new String[]{String.valueOf(gradeId)}, //selectionArgs
                null, null, null)) {
          int typeIndex = ecur.getColumnIndex(ELEMENTS_COLUMN_TYPE);
          int validIndex = ecur.getColumnIndex(ELEMENTS_COLUMN_VALID);
          int weightIndex = ecur.getColumnIndex(ELEMENTS_COLUMN_WEIGHT);
          int achievedIndex = ecur.getColumnIndex(ELEMENTS_COLUMN_ACHIEVED);
          while(ecur.moveToNext()) {
            Grades.Type type = Grades.Type.values()[ecur.getInt(typeIndex)];
            boolean valid = ecur.getInt(validIndex) == 1;
            int weight = ecur.getInt(weightIndex);
            int achieved = ecur.getInt(achievedIndex);
            Log.d(LOG_TAG, "Element: type=" + type + ", valid=" + valid + ", weight=" + weight + ", achieved=" + achieved);

            grades.setElement(type, new Grades.Element(valid, weight, achieved));
          }
        }

        list.add(grades);
      }
    }

    return list;
  }

  private static class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(@Nullable Context context) {
      super(context, "Grades.db", null, 1);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
      super.onConfigure(db);
      db.setForeignKeyConstraintsEnabled(true); //外部キー制約
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(new StringJoiner(",", "CREATE TABLE " + GRADES_TABLE + " (", ")")
              .add(BaseColumns._ID + " INTEGER PRIMARY KEY")
              .add(GRADES_COLUMN_SUBJECT + " TEXT")
              .toString()
      );
      db.execSQL(new StringJoiner(",", "CREATE TABLE " + ELEMENTS_TABLE + " (", ")")
              .add(ELEMENTS_COLUMN_GRADES_ID + " INTEGER")
              .add(ELEMENTS_COLUMN_TYPE + " INTEGER")
              .add(ELEMENTS_COLUMN_VALID + " INTEGER")
              .add(ELEMENTS_COLUMN_WEIGHT + " INTEGER")
              .add(ELEMENTS_COLUMN_ACHIEVED + " INTEGER")
              .add("FOREIGN KEY (" + ELEMENTS_COLUMN_GRADES_ID + ") REFERENCES " + GRADES_TABLE + "(" + BaseColumns._ID + ")")
              .toString()
      );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + ELEMENTS_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + GRADES_TABLE);
      onCreate(db);
    }
  }
}

class SharedPreferencesGradesStorage implements GradesStorage {
  private static final String PREF_KEY_SIZE = "grades_size";
  private static final String PREF_KEY_GRADES_SUBJECT_FORMAT = "grades_%d_name";
  private static final String PREF_KEY_ELEMENTS_FORMAT = "grades_%d_elements";
  private static final String SEPARATOR = ",";

  private final SharedPreferences preferences;

  SharedPreferencesGradesStorage(Context context) {
    this.preferences = context.getSharedPreferences("grades", Context.MODE_PRIVATE);
  }

  @Override
  public void save(List<Grades> list) {
    SharedPreferences.Editor editor = preferences.edit();

    editor.putInt(PREF_KEY_SIZE, list.size());

    for (int i=0; i<list.size(); i++) {
      Grades grades = list.get(i);

      editor.putString(String.format(Locale.getDefault(), PREF_KEY_GRADES_SUBJECT_FORMAT, i), grades.subjectName);

      StringJoiner sj = new StringJoiner(SEPARATOR);
      for (Grades.Type type : Grades.Type.values()) {
        Grades.Element element = grades.getElement(type);
        sj.add(String.valueOf(element.valid));
        sj.add(String.valueOf(element.weight));
        sj.add(String.valueOf(element.achieved));
      }
      editor.putString(String.format(Locale.getDefault(), PREF_KEY_ELEMENTS_FORMAT, i), sj.toString());
    }

    editor.apply();
  }

  @Override
  public LiveData<List<Grades>> load() {
    List<Grades> list = new ArrayList<>();

    int size = preferences.getInt(PREF_KEY_SIZE, 0);

    for (int i=0; i<size; i++) {
      String subject = preferences.getString(String.format(Locale.getDefault(), PREF_KEY_GRADES_SUBJECT_FORMAT, i), "");

      Grades grades = new Grades(subject);

      String str = preferences.getString(String.format(Locale.getDefault(), PREF_KEY_ELEMENTS_FORMAT, i), null);
      if(str == null) break;

      String[] tokens = str.split(SEPARATOR);
      int j = 0;
      for (Grades.Type type : Grades.Type.values()) {
        Grades.Element element = grades.getElement(type);
        element.valid = Boolean.parseBoolean(tokens[j++]);
        element.weight = Integer.parseInt(tokens[j++]);
        element.achieved = Integer.parseInt(tokens[j++]);
        grades.setElement(type, element);
      }

      list.add(grades);
    }

    return new MutableLiveData<>(list);
  }
}
