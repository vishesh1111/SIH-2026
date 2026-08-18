package com.sih2026.touristsafety.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class EmergencyContactDao_Impl implements EmergencyContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContactEntity> __insertionAdapterOfEmergencyContactEntity;

  private final EntityDeletionOrUpdateAdapter<EmergencyContactEntity> __deletionAdapterOfEmergencyContactEntity;

  private final EntityDeletionOrUpdateAdapter<EmergencyContactEntity> __updateAdapterOfEmergencyContactEntity;

  public EmergencyContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContactEntity = new EntityInsertionAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergency_contacts` (`id`,`userId`,`name`,`phone`,`relationship`,`countryCode`,`residentAddress`,`isPrimary`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContactEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getPhone());
        statement.bindString(5, entity.getRelationship());
        statement.bindString(6, entity.getCountryCode());
        if (entity.getResidentAddress() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getResidentAddress());
        }
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__deletionAdapterOfEmergencyContactEntity = new EntityDeletionOrUpdateAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `emergency_contacts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContactEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfEmergencyContactEntity = new EntityDeletionOrUpdateAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `emergency_contacts` SET `id` = ?,`userId` = ?,`name` = ?,`phone` = ?,`relationship` = ?,`countryCode` = ?,`residentAddress` = ?,`isPrimary` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContactEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getPhone());
        statement.bindString(5, entity.getRelationship());
        statement.bindString(6, entity.getCountryCode());
        if (entity.getResidentAddress() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getResidentAddress());
        }
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getId());
      }
    };
  }

  @Override
  public Object insertContact(final EmergencyContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEmergencyContactEntity.insert(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteContact(final EmergencyContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEmergencyContactEntity.handle(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateContact(final EmergencyContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEmergencyContactEntity.handle(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EmergencyContactEntity>> getContactsForUser(final String userId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contacts"}, new Callable<List<EmergencyContactEntity>>() {
      @Override
      @NonNull
      public List<EmergencyContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfResidentAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "residentAddress");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContactEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpResidentAddress;
            if (_cursor.isNull(_cursorIndexOfResidentAddress)) {
              _tmpResidentAddress = null;
            } else {
              _tmpResidentAddress = _cursor.getString(_cursorIndexOfResidentAddress);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _item = new EmergencyContactEntity(_tmpId,_tmpUserId,_tmpName,_tmpPhone,_tmpRelationship,_tmpCountryCode,_tmpResidentAddress,_tmpIsPrimary);
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
  public Object getContactById(final String id,
      final Continuation<? super EmergencyContactEntity> $completion) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EmergencyContactEntity>() {
      @Override
      @Nullable
      public EmergencyContactEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfResidentAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "residentAddress");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final EmergencyContactEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpResidentAddress;
            if (_cursor.isNull(_cursorIndexOfResidentAddress)) {
              _tmpResidentAddress = null;
            } else {
              _tmpResidentAddress = _cursor.getString(_cursorIndexOfResidentAddress);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _result = new EmergencyContactEntity(_tmpId,_tmpUserId,_tmpName,_tmpPhone,_tmpRelationship,_tmpCountryCode,_tmpResidentAddress,_tmpIsPrimary);
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
  public Flow<List<EmergencyContactEntity>> getAllContacts() {
    final String _sql = "SELECT * FROM emergency_contacts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contacts"}, new Callable<List<EmergencyContactEntity>>() {
      @Override
      @NonNull
      public List<EmergencyContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "countryCode");
          final int _cursorIndexOfResidentAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "residentAddress");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContactEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            final String _tmpResidentAddress;
            if (_cursor.isNull(_cursorIndexOfResidentAddress)) {
              _tmpResidentAddress = null;
            } else {
              _tmpResidentAddress = _cursor.getString(_cursorIndexOfResidentAddress);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            _item = new EmergencyContactEntity(_tmpId,_tmpUserId,_tmpName,_tmpPhone,_tmpRelationship,_tmpCountryCode,_tmpResidentAddress,_tmpIsPrimary);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
