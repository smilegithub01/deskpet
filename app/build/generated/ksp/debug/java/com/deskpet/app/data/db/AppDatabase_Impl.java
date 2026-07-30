package com.deskpet.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MoodLogDao _moodLogDao;

  private volatile PeriodLogDao _periodLogDao;

  private volatile PetDao _petDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `mood_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `mood` TEXT NOT NULL, `note` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `period_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `isPeriodStart` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pet_state` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `species` TEXT NOT NULL, `color` TEXT NOT NULL, `level` INTEGER NOT NULL, `hunger` INTEGER NOT NULL, `mood` INTEGER NOT NULL, `intimacy` INTEGER NOT NULL, `diamonds` INTEGER NOT NULL, `personalityTags` TEXT NOT NULL, `equippedHead` TEXT, `equippedGlasses` TEXT, `equippedCollar` TEXT, `equippedClothing` TEXT, `equippedTail` TEXT, `equippedAccessory` TEXT, `createdAt` INTEGER NOT NULL, `lastInteractionTime` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6038d03d40e6f38bc67f1ae7551f5e2a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `mood_logs`");
        db.execSQL("DROP TABLE IF EXISTS `period_logs`");
        db.execSQL("DROP TABLE IF EXISTS `pet_state`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMoodLogs = new HashMap<String, TableInfo.Column>(4);
        _columnsMoodLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoodLogs.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoodLogs.put("mood", new TableInfo.Column("mood", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMoodLogs.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMoodLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMoodLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMoodLogs = new TableInfo("mood_logs", _columnsMoodLogs, _foreignKeysMoodLogs, _indicesMoodLogs);
        final TableInfo _existingMoodLogs = TableInfo.read(db, "mood_logs");
        if (!_infoMoodLogs.equals(_existingMoodLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "mood_logs(com.deskpet.app.data.model.MoodLog).\n"
                  + " Expected:\n" + _infoMoodLogs + "\n"
                  + " Found:\n" + _existingMoodLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsPeriodLogs = new HashMap<String, TableInfo.Column>(3);
        _columnsPeriodLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriodLogs.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriodLogs.put("isPeriodStart", new TableInfo.Column("isPeriodStart", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPeriodLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPeriodLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPeriodLogs = new TableInfo("period_logs", _columnsPeriodLogs, _foreignKeysPeriodLogs, _indicesPeriodLogs);
        final TableInfo _existingPeriodLogs = TableInfo.read(db, "period_logs");
        if (!_infoPeriodLogs.equals(_existingPeriodLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "period_logs(com.deskpet.app.data.model.PeriodLog).\n"
                  + " Expected:\n" + _infoPeriodLogs + "\n"
                  + " Found:\n" + _existingPeriodLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsPetState = new HashMap<String, TableInfo.Column>(18);
        _columnsPetState.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("species", new TableInfo.Column("species", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("level", new TableInfo.Column("level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("hunger", new TableInfo.Column("hunger", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("mood", new TableInfo.Column("mood", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("intimacy", new TableInfo.Column("intimacy", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("diamonds", new TableInfo.Column("diamonds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("personalityTags", new TableInfo.Column("personalityTags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedHead", new TableInfo.Column("equippedHead", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedGlasses", new TableInfo.Column("equippedGlasses", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedCollar", new TableInfo.Column("equippedCollar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedClothing", new TableInfo.Column("equippedClothing", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedTail", new TableInfo.Column("equippedTail", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("equippedAccessory", new TableInfo.Column("equippedAccessory", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPetState.put("lastInteractionTime", new TableInfo.Column("lastInteractionTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPetState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPetState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPetState = new TableInfo("pet_state", _columnsPetState, _foreignKeysPetState, _indicesPetState);
        final TableInfo _existingPetState = TableInfo.read(db, "pet_state");
        if (!_infoPetState.equals(_existingPetState)) {
          return new RoomOpenHelper.ValidationResult(false, "pet_state(com.deskpet.app.data.model.PetEntity).\n"
                  + " Expected:\n" + _infoPetState + "\n"
                  + " Found:\n" + _existingPetState);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6038d03d40e6f38bc67f1ae7551f5e2a", "8d073fc2b175bf0c874ce395766d588e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "mood_logs","period_logs","pet_state");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `mood_logs`");
      _db.execSQL("DELETE FROM `period_logs`");
      _db.execSQL("DELETE FROM `pet_state`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MoodLogDao.class, MoodLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PeriodLogDao.class, PeriodLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PetDao.class, PetDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MoodLogDao moodLogDao() {
    if (_moodLogDao != null) {
      return _moodLogDao;
    } else {
      synchronized(this) {
        if(_moodLogDao == null) {
          _moodLogDao = new MoodLogDao_Impl(this);
        }
        return _moodLogDao;
      }
    }
  }

  @Override
  public PeriodLogDao periodLogDao() {
    if (_periodLogDao != null) {
      return _periodLogDao;
    } else {
      synchronized(this) {
        if(_periodLogDao == null) {
          _periodLogDao = new PeriodLogDao_Impl(this);
        }
        return _periodLogDao;
      }
    }
  }

  @Override
  public PetDao petDao() {
    if (_petDao != null) {
      return _petDao;
    } else {
      synchronized(this) {
        if(_petDao == null) {
          _petDao = new PetDao_Impl(this);
        }
        return _petDao;
      }
    }
  }
}
