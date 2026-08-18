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
import com.sih2026.touristsafety.data.local.entities.DocumentEntity;
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
public final class DocumentDao_Impl implements DocumentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DocumentEntity> __insertionAdapterOfDocumentEntity;

  private final EntityDeletionOrUpdateAdapter<DocumentEntity> __deletionAdapterOfDocumentEntity;

  public DocumentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDocumentEntity = new EntityInsertionAdapter<DocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `documents` (`id`,`userId`,`docType`,`docNumber`,`fileUrl`,`expiryDate`,`verificationStatus`,`metadata`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DocumentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getDocType());
        statement.bindString(4, entity.getDocNumber());
        statement.bindString(5, entity.getFileUrl());
        if (entity.getExpiryDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getExpiryDate());
        }
        statement.bindString(7, entity.getVerificationStatus());
        if (entity.getMetadata() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMetadata());
        }
        statement.bindLong(9, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfDocumentEntity = new EntityDeletionOrUpdateAdapter<DocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `documents` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DocumentEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertDocument(final DocumentEntity document,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDocumentEntity.insert(document);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDocument(final DocumentEntity document,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDocumentEntity.handle(document);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DocumentEntity>> getDocumentsForUser(final String userId) {
    final String _sql = "SELECT * FROM documents WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"documents"}, new Callable<List<DocumentEntity>>() {
      @Override
      @NonNull
      public List<DocumentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDocType = CursorUtil.getColumnIndexOrThrow(_cursor, "docType");
          final int _cursorIndexOfDocNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "docNumber");
          final int _cursorIndexOfFileUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "fileUrl");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfVerificationStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "verificationStatus");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<DocumentEntity> _result = new ArrayList<DocumentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DocumentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDocType;
            _tmpDocType = _cursor.getString(_cursorIndexOfDocType);
            final String _tmpDocNumber;
            _tmpDocNumber = _cursor.getString(_cursorIndexOfDocNumber);
            final String _tmpFileUrl;
            _tmpFileUrl = _cursor.getString(_cursorIndexOfFileUrl);
            final Long _tmpExpiryDate;
            if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
              _tmpExpiryDate = null;
            } else {
              _tmpExpiryDate = _cursor.getLong(_cursorIndexOfExpiryDate);
            }
            final String _tmpVerificationStatus;
            _tmpVerificationStatus = _cursor.getString(_cursorIndexOfVerificationStatus);
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new DocumentEntity(_tmpId,_tmpUserId,_tmpDocType,_tmpDocNumber,_tmpFileUrl,_tmpExpiryDate,_tmpVerificationStatus,_tmpMetadata,_tmpCreatedAt);
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
  public Object getDocumentById(final String id,
      final Continuation<? super DocumentEntity> $completion) {
    final String _sql = "SELECT * FROM documents WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DocumentEntity>() {
      @Override
      @Nullable
      public DocumentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDocType = CursorUtil.getColumnIndexOrThrow(_cursor, "docType");
          final int _cursorIndexOfDocNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "docNumber");
          final int _cursorIndexOfFileUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "fileUrl");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfVerificationStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "verificationStatus");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final DocumentEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDocType;
            _tmpDocType = _cursor.getString(_cursorIndexOfDocType);
            final String _tmpDocNumber;
            _tmpDocNumber = _cursor.getString(_cursorIndexOfDocNumber);
            final String _tmpFileUrl;
            _tmpFileUrl = _cursor.getString(_cursorIndexOfFileUrl);
            final Long _tmpExpiryDate;
            if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
              _tmpExpiryDate = null;
            } else {
              _tmpExpiryDate = _cursor.getLong(_cursorIndexOfExpiryDate);
            }
            final String _tmpVerificationStatus;
            _tmpVerificationStatus = _cursor.getString(_cursorIndexOfVerificationStatus);
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new DocumentEntity(_tmpId,_tmpUserId,_tmpDocType,_tmpDocNumber,_tmpFileUrl,_tmpExpiryDate,_tmpVerificationStatus,_tmpMetadata,_tmpCreatedAt);
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
  public Flow<List<DocumentEntity>> getAllDocuments() {
    final String _sql = "SELECT * FROM documents";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"documents"}, new Callable<List<DocumentEntity>>() {
      @Override
      @NonNull
      public List<DocumentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDocType = CursorUtil.getColumnIndexOrThrow(_cursor, "docType");
          final int _cursorIndexOfDocNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "docNumber");
          final int _cursorIndexOfFileUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "fileUrl");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfVerificationStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "verificationStatus");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<DocumentEntity> _result = new ArrayList<DocumentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DocumentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDocType;
            _tmpDocType = _cursor.getString(_cursorIndexOfDocType);
            final String _tmpDocNumber;
            _tmpDocNumber = _cursor.getString(_cursorIndexOfDocNumber);
            final String _tmpFileUrl;
            _tmpFileUrl = _cursor.getString(_cursorIndexOfFileUrl);
            final Long _tmpExpiryDate;
            if (_cursor.isNull(_cursorIndexOfExpiryDate)) {
              _tmpExpiryDate = null;
            } else {
              _tmpExpiryDate = _cursor.getLong(_cursorIndexOfExpiryDate);
            }
            final String _tmpVerificationStatus;
            _tmpVerificationStatus = _cursor.getString(_cursorIndexOfVerificationStatus);
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new DocumentEntity(_tmpId,_tmpUserId,_tmpDocType,_tmpDocNumber,_tmpFileUrl,_tmpExpiryDate,_tmpVerificationStatus,_tmpMetadata,_tmpCreatedAt);
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
