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
import com.sih2026.touristsafety.data.local.entities.IncidentEntity;
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
public final class IncidentDao_Impl implements IncidentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<IncidentEntity> __insertionAdapterOfIncidentEntity;

  public IncidentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIncidentEntity = new EntityInsertionAdapter<IncidentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `incidents` (`id`,`reporterId`,`incidentType`,`description`,`aiStructuredFir`,`latitude`,`longitude`,`address`,`state`,`district`,`status`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IncidentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getReporterId());
        statement.bindString(3, entity.getIncidentType());
        statement.bindString(4, entity.getDescription());
        if (entity.getAiStructuredFir() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAiStructuredFir());
        }
        statement.bindDouble(6, entity.getLatitude());
        statement.bindDouble(7, entity.getLongitude());
        if (entity.getAddress() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAddress());
        }
        statement.bindString(9, entity.getState());
        statement.bindString(10, entity.getDistrict());
        statement.bindString(11, entity.getStatus());
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insertIncident(final IncidentEntity incident,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIncidentEntity.insert(incident);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<IncidentEntity>> getAllIncidents() {
    final String _sql = "SELECT * FROM incidents ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incidents"}, new Callable<List<IncidentEntity>>() {
      @Override
      @NonNull
      public List<IncidentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfReporterId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporterId");
          final int _cursorIndexOfIncidentType = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentType");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAiStructuredFir = CursorUtil.getColumnIndexOrThrow(_cursor, "aiStructuredFir");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfDistrict = CursorUtil.getColumnIndexOrThrow(_cursor, "district");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<IncidentEntity> _result = new ArrayList<IncidentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IncidentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpReporterId;
            _tmpReporterId = _cursor.getString(_cursorIndexOfReporterId);
            final String _tmpIncidentType;
            _tmpIncidentType = _cursor.getString(_cursorIndexOfIncidentType);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAiStructuredFir;
            if (_cursor.isNull(_cursorIndexOfAiStructuredFir)) {
              _tmpAiStructuredFir = null;
            } else {
              _tmpAiStructuredFir = _cursor.getString(_cursorIndexOfAiStructuredFir);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpDistrict;
            _tmpDistrict = _cursor.getString(_cursorIndexOfDistrict);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new IncidentEntity(_tmpId,_tmpReporterId,_tmpIncidentType,_tmpDescription,_tmpAiStructuredFir,_tmpLatitude,_tmpLongitude,_tmpAddress,_tmpState,_tmpDistrict,_tmpStatus,_tmpCreatedAt);
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
