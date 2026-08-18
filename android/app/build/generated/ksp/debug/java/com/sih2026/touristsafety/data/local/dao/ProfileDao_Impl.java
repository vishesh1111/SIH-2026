package com.sih2026.touristsafety.data.local.dao;

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
import com.sih2026.touristsafety.data.local.entities.ProfileEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class ProfileDao_Impl implements ProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProfileEntity> __insertionAdapterOfProfileEntity;

  public ProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProfileEntity = new EntityInsertionAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `profiles` (`id`,`fullName`,`nationality`,`gender`,`phone`,`email`,`passportNumber`,`visaNumber`,`profilePhotoUrl`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFullName());
        statement.bindString(3, entity.getNationality());
        statement.bindString(4, entity.getGender());
        statement.bindString(5, entity.getPhone());
        statement.bindString(6, entity.getEmail());
        if (entity.getPassportNumber() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPassportNumber());
        }
        if (entity.getVisaNumber() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getVisaNumber());
        }
        if (entity.getProfilePhotoUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getProfilePhotoUrl());
        }
        statement.bindLong(10, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insertProfile(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProfile(final String id, final Continuation<? super ProfileEntity> $completion) {
    final String _sql = "SELECT * FROM profiles WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProfileEntity>() {
      @Override
      @Nullable
      public ProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNationality = CursorUtil.getColumnIndexOrThrow(_cursor, "nationality");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassportNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "passportNumber");
          final int _cursorIndexOfVisaNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "visaNumber");
          final int _cursorIndexOfProfilePhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePhotoUrl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final String _tmpNationality;
            _tmpNationality = _cursor.getString(_cursorIndexOfNationality);
            final String _tmpGender;
            _tmpGender = _cursor.getString(_cursorIndexOfGender);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpPassportNumber;
            if (_cursor.isNull(_cursorIndexOfPassportNumber)) {
              _tmpPassportNumber = null;
            } else {
              _tmpPassportNumber = _cursor.getString(_cursorIndexOfPassportNumber);
            }
            final String _tmpVisaNumber;
            if (_cursor.isNull(_cursorIndexOfVisaNumber)) {
              _tmpVisaNumber = null;
            } else {
              _tmpVisaNumber = _cursor.getString(_cursorIndexOfVisaNumber);
            }
            final String _tmpProfilePhotoUrl;
            if (_cursor.isNull(_cursorIndexOfProfilePhotoUrl)) {
              _tmpProfilePhotoUrl = null;
            } else {
              _tmpProfilePhotoUrl = _cursor.getString(_cursorIndexOfProfilePhotoUrl);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ProfileEntity(_tmpId,_tmpFullName,_tmpNationality,_tmpGender,_tmpPhone,_tmpEmail,_tmpPassportNumber,_tmpVisaNumber,_tmpProfilePhotoUrl,_tmpCreatedAt);
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
