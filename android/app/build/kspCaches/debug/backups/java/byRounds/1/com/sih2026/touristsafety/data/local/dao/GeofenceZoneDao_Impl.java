package com.sih2026.touristsafety.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity;
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
public final class GeofenceZoneDao_Impl implements GeofenceZoneDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GeofenceZoneEntity> __insertionAdapterOfGeofenceZoneEntity;

  public GeofenceZoneDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGeofenceZoneEntity = new EntityInsertionAdapter<GeofenceZoneEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `geofence_zones` (`id`,`name`,`zoneType`,`latitude`,`longitude`,`radius`,`description`,`alertMessage`,`severity`,`state`,`isActive`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GeofenceZoneEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getZoneType());
        statement.bindDouble(4, entity.getLatitude());
        statement.bindDouble(5, entity.getLongitude());
        statement.bindDouble(6, entity.getRadius());
        statement.bindString(7, entity.getDescription());
        statement.bindString(8, entity.getAlertMessage());
        statement.bindLong(9, entity.getSeverity());
        statement.bindString(10, entity.getState());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(11, _tmp);
      }
    };
  }

  @Override
  public Object insertZone(final GeofenceZoneEntity zone,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGeofenceZoneEntity.insert(zone);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertZones(final List<GeofenceZoneEntity> zones,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGeofenceZoneEntity.insert(zones);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<GeofenceZoneEntity>> getActiveZones() {
    final String _sql = "SELECT * FROM geofence_zones WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"geofence_zones"}, new Callable<List<GeofenceZoneEntity>>() {
      @Override
      @NonNull
      public List<GeofenceZoneEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zoneType");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadius = CursorUtil.getColumnIndexOrThrow(_cursor, "radius");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAlertMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "alertMessage");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<GeofenceZoneEntity> _result = new ArrayList<GeofenceZoneEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GeofenceZoneEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpZoneType;
            _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadius;
            _tmpRadius = _cursor.getFloat(_cursorIndexOfRadius);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAlertMessage;
            _tmpAlertMessage = _cursor.getString(_cursorIndexOfAlertMessage);
            final int _tmpSeverity;
            _tmpSeverity = _cursor.getInt(_cursorIndexOfSeverity);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new GeofenceZoneEntity(_tmpId,_tmpName,_tmpZoneType,_tmpLatitude,_tmpLongitude,_tmpRadius,_tmpDescription,_tmpAlertMessage,_tmpSeverity,_tmpState,_tmpIsActive);
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
  public Object getActiveZonesList(
      final Continuation<? super List<GeofenceZoneEntity>> $completion) {
    final String _sql = "SELECT * FROM geofence_zones WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<GeofenceZoneEntity>>() {
      @Override
      @NonNull
      public List<GeofenceZoneEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zoneType");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadius = CursorUtil.getColumnIndexOrThrow(_cursor, "radius");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAlertMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "alertMessage");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<GeofenceZoneEntity> _result = new ArrayList<GeofenceZoneEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GeofenceZoneEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpZoneType;
            _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadius;
            _tmpRadius = _cursor.getFloat(_cursorIndexOfRadius);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAlertMessage;
            _tmpAlertMessage = _cursor.getString(_cursorIndexOfAlertMessage);
            final int _tmpSeverity;
            _tmpSeverity = _cursor.getInt(_cursorIndexOfSeverity);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new GeofenceZoneEntity(_tmpId,_tmpName,_tmpZoneType,_tmpLatitude,_tmpLongitude,_tmpRadius,_tmpDescription,_tmpAlertMessage,_tmpSeverity,_tmpState,_tmpIsActive);
            _result.add(_item);
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
  public Flow<List<GeofenceZoneEntity>> getAllZones() {
    final String _sql = "SELECT * FROM geofence_zones";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"geofence_zones"}, new Callable<List<GeofenceZoneEntity>>() {
      @Override
      @NonNull
      public List<GeofenceZoneEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zoneType");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadius = CursorUtil.getColumnIndexOrThrow(_cursor, "radius");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAlertMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "alertMessage");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<GeofenceZoneEntity> _result = new ArrayList<GeofenceZoneEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GeofenceZoneEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpZoneType;
            _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadius;
            _tmpRadius = _cursor.getFloat(_cursorIndexOfRadius);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpAlertMessage;
            _tmpAlertMessage = _cursor.getString(_cursorIndexOfAlertMessage);
            final int _tmpSeverity;
            _tmpSeverity = _cursor.getInt(_cursorIndexOfSeverity);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new GeofenceZoneEntity(_tmpId,_tmpName,_tmpZoneType,_tmpLatitude,_tmpLongitude,_tmpRadius,_tmpDescription,_tmpAlertMessage,_tmpSeverity,_tmpState,_tmpIsActive);
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
