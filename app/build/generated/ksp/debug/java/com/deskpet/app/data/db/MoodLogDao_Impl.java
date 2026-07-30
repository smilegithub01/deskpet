package com.deskpet.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.deskpet.app.data.model.MoodLevel;
import com.deskpet.app.data.model.MoodLog;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MoodLogDao_Impl implements MoodLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MoodLog> __insertionAdapterOfMoodLog;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public MoodLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMoodLog = new EntityInsertionAdapter<MoodLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `mood_logs` (`id`,`date`,`mood`,`note`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoodLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDate());
        final String _tmp = __converters.fromMoodLevel(entity.getMood());
        statement.bindString(3, _tmp);
        statement.bindString(4, entity.getNote());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM mood_logs";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final MoodLog moodLog, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMoodLog.insertAndReturnId(moodLog);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MoodLog>> getAll() {
    final String _sql = "SELECT * FROM mood_logs ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"mood_logs"}, new Callable<List<MoodLog>>() {
      @Override
      @NonNull
      public List<MoodLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<MoodLog> _result = new ArrayList<MoodLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoodLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final MoodLevel _tmpMood;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMood);
            _tmpMood = __converters.toMoodLevel(_tmp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new MoodLog(_tmpId,_tmpDate,_tmpMood,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MoodLog>> getRecent(final int limit) {
    final String _sql = "SELECT * FROM mood_logs ORDER BY date DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"mood_logs"}, new Callable<List<MoodLog>>() {
      @Override
      @NonNull
      public List<MoodLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<MoodLog> _result = new ArrayList<MoodLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoodLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final MoodLevel _tmpMood;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMood);
            _tmpMood = __converters.toMoodLevel(_tmp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new MoodLog(_tmpId,_tmpDate,_tmpMood,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByDate(final long date, final Continuation<? super MoodLog> $completion) {
    final String _sql = "SELECT * FROM mood_logs WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MoodLog>() {
      @Override
      @Nullable
      public MoodLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final MoodLog _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final MoodLevel _tmpMood;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMood);
            _tmpMood = __converters.toMoodLevel(_tmp);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _result = new MoodLog(_tmpId,_tmpDate,_tmpMood,_tmpNote);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
