package com.deskpet.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.deskpet.app.data.model.PersonalityTag;
import com.deskpet.app.data.model.PetColor;
import com.deskpet.app.data.model.PetEntity;
import com.deskpet.app.data.model.PetSpecies;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PetDao_Impl implements PetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PetEntity> __insertionAdapterOfPetEntity;

  private final Converters __converters = new Converters();

  public PetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPetEntity = new EntityInsertionAdapter<PetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pet_state` (`id`,`name`,`species`,`color`,`level`,`hunger`,`mood`,`intimacy`,`diamonds`,`personalityTags`,`equippedHead`,`equippedGlasses`,`equippedCollar`,`equippedClothing`,`equippedTail`,`equippedAccessory`,`createdAt`,`lastInteractionTime`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromPetSpecies(entity.getSpecies());
        statement.bindString(3, _tmp);
        final String _tmp_1 = __converters.fromPetColor(entity.getColor());
        statement.bindString(4, _tmp_1);
        statement.bindLong(5, entity.getLevel());
        statement.bindLong(6, entity.getHunger());
        statement.bindLong(7, entity.getMood());
        statement.bindLong(8, entity.getIntimacy());
        statement.bindLong(9, entity.getDiamonds());
        final String _tmp_2 = __converters.fromPersonalityTagList(entity.getPersonalityTags());
        statement.bindString(10, _tmp_2);
        if (entity.getEquippedHead() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getEquippedHead());
        }
        if (entity.getEquippedGlasses() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getEquippedGlasses());
        }
        if (entity.getEquippedCollar() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getEquippedCollar());
        }
        if (entity.getEquippedClothing() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEquippedClothing());
        }
        if (entity.getEquippedTail() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getEquippedTail());
        }
        if (entity.getEquippedAccessory() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getEquippedAccessory());
        }
        statement.bindLong(17, entity.getCreatedAt());
        statement.bindLong(18, entity.getLastInteractionTime());
      }
    };
  }

  @Override
  public Object upsert(final PetEntity pet, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPetEntity.insert(pet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPet(final Continuation<? super PetEntity> $completion) {
    final String _sql = "SELECT * FROM pet_state WHERE id = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PetEntity>() {
      @Override
      @Nullable
      public PetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSpecies = CursorUtil.getColumnIndexOrThrow(_cursor, "species");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfHunger = CursorUtil.getColumnIndexOrThrow(_cursor, "hunger");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfIntimacy = CursorUtil.getColumnIndexOrThrow(_cursor, "intimacy");
          final int _cursorIndexOfDiamonds = CursorUtil.getColumnIndexOrThrow(_cursor, "diamonds");
          final int _cursorIndexOfPersonalityTags = CursorUtil.getColumnIndexOrThrow(_cursor, "personalityTags");
          final int _cursorIndexOfEquippedHead = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedHead");
          final int _cursorIndexOfEquippedGlasses = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedGlasses");
          final int _cursorIndexOfEquippedCollar = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedCollar");
          final int _cursorIndexOfEquippedClothing = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedClothing");
          final int _cursorIndexOfEquippedTail = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedTail");
          final int _cursorIndexOfEquippedAccessory = CursorUtil.getColumnIndexOrThrow(_cursor, "equippedAccessory");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastInteractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastInteractionTime");
          final PetEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final PetSpecies _tmpSpecies;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSpecies);
            _tmpSpecies = __converters.toPetSpecies(_tmp);
            final PetColor _tmpColor;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfColor);
            _tmpColor = __converters.toPetColor(_tmp_1);
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpHunger;
            _tmpHunger = _cursor.getInt(_cursorIndexOfHunger);
            final int _tmpMood;
            _tmpMood = _cursor.getInt(_cursorIndexOfMood);
            final int _tmpIntimacy;
            _tmpIntimacy = _cursor.getInt(_cursorIndexOfIntimacy);
            final int _tmpDiamonds;
            _tmpDiamonds = _cursor.getInt(_cursorIndexOfDiamonds);
            final List<PersonalityTag> _tmpPersonalityTags;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPersonalityTags);
            _tmpPersonalityTags = __converters.toPersonalityTagList(_tmp_2);
            final String _tmpEquippedHead;
            if (_cursor.isNull(_cursorIndexOfEquippedHead)) {
              _tmpEquippedHead = null;
            } else {
              _tmpEquippedHead = _cursor.getString(_cursorIndexOfEquippedHead);
            }
            final String _tmpEquippedGlasses;
            if (_cursor.isNull(_cursorIndexOfEquippedGlasses)) {
              _tmpEquippedGlasses = null;
            } else {
              _tmpEquippedGlasses = _cursor.getString(_cursorIndexOfEquippedGlasses);
            }
            final String _tmpEquippedCollar;
            if (_cursor.isNull(_cursorIndexOfEquippedCollar)) {
              _tmpEquippedCollar = null;
            } else {
              _tmpEquippedCollar = _cursor.getString(_cursorIndexOfEquippedCollar);
            }
            final String _tmpEquippedClothing;
            if (_cursor.isNull(_cursorIndexOfEquippedClothing)) {
              _tmpEquippedClothing = null;
            } else {
              _tmpEquippedClothing = _cursor.getString(_cursorIndexOfEquippedClothing);
            }
            final String _tmpEquippedTail;
            if (_cursor.isNull(_cursorIndexOfEquippedTail)) {
              _tmpEquippedTail = null;
            } else {
              _tmpEquippedTail = _cursor.getString(_cursorIndexOfEquippedTail);
            }
            final String _tmpEquippedAccessory;
            if (_cursor.isNull(_cursorIndexOfEquippedAccessory)) {
              _tmpEquippedAccessory = null;
            } else {
              _tmpEquippedAccessory = _cursor.getString(_cursorIndexOfEquippedAccessory);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastInteractionTime;
            _tmpLastInteractionTime = _cursor.getLong(_cursorIndexOfLastInteractionTime);
            _result = new PetEntity(_tmpId,_tmpName,_tmpSpecies,_tmpColor,_tmpLevel,_tmpHunger,_tmpMood,_tmpIntimacy,_tmpDiamonds,_tmpPersonalityTags,_tmpEquippedHead,_tmpEquippedGlasses,_tmpEquippedCollar,_tmpEquippedClothing,_tmpEquippedTail,_tmpEquippedAccessory,_tmpCreatedAt,_tmpLastInteractionTime);
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

  @Override
  public Object getLastInteractionTime(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT lastInteractionTime FROM pet_state WHERE id = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getLong(0);
            }
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
