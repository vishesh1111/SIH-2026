package com.sih2026.touristsafety.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity;
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
public final class DisasterAlertDao_Impl implements DisasterAlertDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DisasterAlertEntity> __insertionAdapterOfDisasterAlertEntity;

  public DisasterAlertDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDisasterAlertEntity = new EntityInsertionAdapter<DisasterAlertEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `disaster_alerts` (`id`,`capIdentifier`,`hazardType`,`severity`,`urgency`,`headline`,`description`,`instructions`,`affectedStates`,`source`,`expiresAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DisasterAlertEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCapIdentifier());
        statement.bindString(3, entity.getHazardType());
        statement.bindString(4, entity.getSeverity());
        statement.bindString(5, entity.getUrgency());
        statement.bindString(6, entity.getHeadline());
        statement.bindString(7, entity.getDescription());
        if (entity.getInstructions() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getInstructions());
        }
        statement.bindString(9, entity.getAffectedStates());
        statement.bindString(10, entity.getSource());
        statement.bindLong(11, entity.getExpiresAt());
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insertAlert(final DisasterAlertEntity alert,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDisasterAlertEntity.insert(alert);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAlerts(final List<DisasterAlertEntity> alerts,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDisasterAlertEntity.insert(alerts);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DisasterAlertEntity>> getActiveAlerts() {
    final String _sql = "SELECT * FROM disaster_alerts ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"disaster_alerts"}, new Callable<List<DisasterAlertEntity>>() {
      @Override
      @NonNull
      public List<DisasterAlertEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCapIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "capIdentifier");
          final int _cursorIndexOfHazardType = CursorUtil.getColumnIndexOrThrow(_cursor, "hazardType");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfUrgency = CursorUtil.getColumnIndexOrThrow(_cursor, "urgency");
          final int _cursorIndexOfHeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "headline");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfInstructions = CursorUtil.getColumnIndexOrThrow(_cursor, "instructions");
          final int _cursorIndexOfAffectedStates = CursorUtil.getColumnIndexOrThrow(_cursor, "affectedStates");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<DisasterAlertEntity> _result = new ArrayList<DisasterAlertEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DisasterAlertEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCapIdentifier;
            _tmpCapIdentifier = _cursor.getString(_cursorIndexOfCapIdentifier);
            final String _tmpHazardType;
            _tmpHazardType = _cursor.getString(_cursorIndexOfHazardType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpUrgency;
            _tmpUrgency = _cursor.getString(_cursorIndexOfUrgency);
            final String _tmpHeadline;
            _tmpHeadline = _cursor.getString(_cursorIndexOfHeadline);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpInstructions;
            if (_cursor.isNull(_cursorIndexOfInstructions)) {
              _tmpInstructions = null;
            } else {
              _tmpInstructions = _cursor.getString(_cursorIndexOfInstructions);
            }
            final String _tmpAffectedStates;
            _tmpAffectedStates = _cursor.getString(_cursorIndexOfAffectedStates);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new DisasterAlertEntity(_tmpId,_tmpCapIdentifier,_tmpHazardType,_tmpSeverity,_tmpUrgency,_tmpHeadline,_tmpDescription,_tmpInstructions,_tmpAffectedStates,_tmpSource,_tmpExpiresAt,_tmpCreatedAt);
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
  public Flow<List<DisasterAlertEntity>> getAllAlerts() {
    final String _sql = "SELECT * FROM disaster_alerts ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"disaster_alerts"}, new Callable<List<DisasterAlertEntity>>() {
      @Override
      @NonNull
      public List<DisasterAlertEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCapIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "capIdentifier");
          final int _cursorIndexOfHazardType = CursorUtil.getColumnIndexOrThrow(_cursor, "hazardType");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfUrgency = CursorUtil.getColumnIndexOrThrow(_cursor, "urgency");
          final int _cursorIndexOfHeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "headline");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfInstructions = CursorUtil.getColumnIndexOrThrow(_cursor, "instructions");
          final int _cursorIndexOfAffectedStates = CursorUtil.getColumnIndexOrThrow(_cursor, "affectedStates");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<DisasterAlertEntity> _result = new ArrayList<DisasterAlertEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DisasterAlertEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCapIdentifier;
            _tmpCapIdentifier = _cursor.getString(_cursorIndexOfCapIdentifier);
            final String _tmpHazardType;
            _tmpHazardType = _cursor.getString(_cursorIndexOfHazardType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpUrgency;
            _tmpUrgency = _cursor.getString(_cursorIndexOfUrgency);
            final String _tmpHeadline;
            _tmpHeadline = _cursor.getString(_cursorIndexOfHeadline);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpInstructions;
            if (_cursor.isNull(_cursorIndexOfInstructions)) {
              _tmpInstructions = null;
            } else {
              _tmpInstructions = _cursor.getString(_cursorIndexOfInstructions);
            }
            final String _tmpAffectedStates;
            _tmpAffectedStates = _cursor.getString(_cursorIndexOfAffectedStates);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new DisasterAlertEntity(_tmpId,_tmpCapIdentifier,_tmpHazardType,_tmpSeverity,_tmpUrgency,_tmpHeadline,_tmpDescription,_tmpInstructions,_tmpAffectedStates,_tmpSource,_tmpExpiresAt,_tmpCreatedAt);
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
  public Flow<List<DisasterAlertEntity>> getAlertsByHazardType(final String type) {
    final String _sql = "SELECT * FROM disaster_alerts WHERE hazardType = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"disaster_alerts"}, new Callable<List<DisasterAlertEntity>>() {
      @Override
      @NonNull
      public List<DisasterAlertEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCapIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "capIdentifier");
          final int _cursorIndexOfHazardType = CursorUtil.getColumnIndexOrThrow(_cursor, "hazardType");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfUrgency = CursorUtil.getColumnIndexOrThrow(_cursor, "urgency");
          final int _cursorIndexOfHeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "headline");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfInstructions = CursorUtil.getColumnIndexOrThrow(_cursor, "instructions");
          final int _cursorIndexOfAffectedStates = CursorUtil.getColumnIndexOrThrow(_cursor, "affectedStates");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<DisasterAlertEntity> _result = new ArrayList<DisasterAlertEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DisasterAlertEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCapIdentifier;
            _tmpCapIdentifier = _cursor.getString(_cursorIndexOfCapIdentifier);
            final String _tmpHazardType;
            _tmpHazardType = _cursor.getString(_cursorIndexOfHazardType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final String _tmpUrgency;
            _tmpUrgency = _cursor.getString(_cursorIndexOfUrgency);
            final String _tmpHeadline;
            _tmpHeadline = _cursor.getString(_cursorIndexOfHeadline);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpInstructions;
            if (_cursor.isNull(_cursorIndexOfInstructions)) {
              _tmpInstructions = null;
            } else {
              _tmpInstructions = _cursor.getString(_cursorIndexOfInstructions);
            }
            final String _tmpAffectedStates;
            _tmpAffectedStates = _cursor.getString(_cursorIndexOfAffectedStates);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new DisasterAlertEntity(_tmpId,_tmpCapIdentifier,_tmpHazardType,_tmpSeverity,_tmpUrgency,_tmpHeadline,_tmpDescription,_tmpInstructions,_tmpAffectedStates,_tmpSource,_tmpExpiresAt,_tmpCreatedAt);
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
