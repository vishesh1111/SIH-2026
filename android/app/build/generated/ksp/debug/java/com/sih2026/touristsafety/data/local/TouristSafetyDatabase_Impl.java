package com.sih2026.touristsafety.data.local;

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
import com.sih2026.touristsafety.data.local.dao.ChatMessageDao;
import com.sih2026.touristsafety.data.local.dao.ChatMessageDao_Impl;
import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao;
import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao_Impl;
import com.sih2026.touristsafety.data.local.dao.DocumentDao;
import com.sih2026.touristsafety.data.local.dao.DocumentDao_Impl;
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao;
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao_Impl;
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao;
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao_Impl;
import com.sih2026.touristsafety.data.local.dao.IncidentDao;
import com.sih2026.touristsafety.data.local.dao.IncidentDao_Impl;
import com.sih2026.touristsafety.data.local.dao.ProfileDao;
import com.sih2026.touristsafety.data.local.dao.ProfileDao_Impl;
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
public final class TouristSafetyDatabase_Impl extends TouristSafetyDatabase {
  private volatile ProfileDao _profileDao;

  private volatile EmergencyContactDao _emergencyContactDao;

  private volatile GeofenceZoneDao _geofenceZoneDao;

  private volatile IncidentDao _incidentDao;

  private volatile DisasterAlertDao _disasterAlertDao;

  private volatile DocumentDao _documentDao;

  private volatile ChatMessageDao _chatMessageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` TEXT NOT NULL, `fullName` TEXT NOT NULL, `nationality` TEXT NOT NULL, `gender` TEXT NOT NULL, `phone` TEXT NOT NULL, `email` TEXT NOT NULL, `passportNumber` TEXT, `visaNumber` TEXT, `profilePhotoUrl` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `relationship` TEXT NOT NULL, `countryCode` TEXT NOT NULL, `residentAddress` TEXT, `isPrimary` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `geofence_zones` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `zoneType` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `radius` REAL NOT NULL, `description` TEXT NOT NULL, `alertMessage` TEXT NOT NULL, `severity` INTEGER NOT NULL, `state` TEXT NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `incidents` (`id` TEXT NOT NULL, `reporterId` TEXT NOT NULL, `incidentType` TEXT NOT NULL, `description` TEXT NOT NULL, `aiStructuredFir` TEXT, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `address` TEXT, `state` TEXT NOT NULL, `district` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `disaster_alerts` (`id` TEXT NOT NULL, `capIdentifier` TEXT NOT NULL, `hazardType` TEXT NOT NULL, `severity` TEXT NOT NULL, `urgency` TEXT NOT NULL, `headline` TEXT NOT NULL, `description` TEXT NOT NULL, `instructions` TEXT, `affectedStates` TEXT NOT NULL, `source` TEXT NOT NULL, `expiresAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `documents` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `docType` TEXT NOT NULL, `docNumber` TEXT NOT NULL, `fileUrl` TEXT NOT NULL, `expiryDate` INTEGER, `verificationStatus` TEXT NOT NULL, `metadata` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `metadata` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '5d860cb421c7cc084936d11829061b97')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `profiles`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        db.execSQL("DROP TABLE IF EXISTS `geofence_zones`");
        db.execSQL("DROP TABLE IF EXISTS `incidents`");
        db.execSQL("DROP TABLE IF EXISTS `disaster_alerts`");
        db.execSQL("DROP TABLE IF EXISTS `documents`");
        db.execSQL("DROP TABLE IF EXISTS `chat_messages`");
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
        final HashMap<String, TableInfo.Column> _columnsProfiles = new HashMap<String, TableInfo.Column>(10);
        _columnsProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("fullName", new TableInfo.Column("fullName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("nationality", new TableInfo.Column("nationality", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("gender", new TableInfo.Column("gender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("passportNumber", new TableInfo.Column("passportNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("visaNumber", new TableInfo.Column("visaNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("profilePhotoUrl", new TableInfo.Column("profilePhotoUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProfiles = new TableInfo("profiles", _columnsProfiles, _foreignKeysProfiles, _indicesProfiles);
        final TableInfo _existingProfiles = TableInfo.read(db, "profiles");
        if (!_infoProfiles.equals(_existingProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "profiles(com.sih2026.touristsafety.data.local.entities.ProfileEntity).\n"
                  + " Expected:\n" + _infoProfiles + "\n"
                  + " Found:\n" + _existingProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(8);
        _columnsEmergencyContacts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("relationship", new TableInfo.Column("relationship", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("countryCode", new TableInfo.Column("countryCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("residentAddress", new TableInfo.Column("residentAddress", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("isPrimary", new TableInfo.Column("isPrimary", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsGeofenceZones = new HashMap<String, TableInfo.Column>(11);
        _columnsGeofenceZones.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("zoneType", new TableInfo.Column("zoneType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("radius", new TableInfo.Column("radius", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("alertMessage", new TableInfo.Column("alertMessage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("severity", new TableInfo.Column("severity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofenceZones.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGeofenceZones = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGeofenceZones = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGeofenceZones = new TableInfo("geofence_zones", _columnsGeofenceZones, _foreignKeysGeofenceZones, _indicesGeofenceZones);
        final TableInfo _existingGeofenceZones = TableInfo.read(db, "geofence_zones");
        if (!_infoGeofenceZones.equals(_existingGeofenceZones)) {
          return new RoomOpenHelper.ValidationResult(false, "geofence_zones(com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity).\n"
                  + " Expected:\n" + _infoGeofenceZones + "\n"
                  + " Found:\n" + _existingGeofenceZones);
        }
        final HashMap<String, TableInfo.Column> _columnsIncidents = new HashMap<String, TableInfo.Column>(12);
        _columnsIncidents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("reporterId", new TableInfo.Column("reporterId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("incidentType", new TableInfo.Column("incidentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("aiStructuredFir", new TableInfo.Column("aiStructuredFir", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("address", new TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("district", new TableInfo.Column("district", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIncidents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesIncidents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoIncidents = new TableInfo("incidents", _columnsIncidents, _foreignKeysIncidents, _indicesIncidents);
        final TableInfo _existingIncidents = TableInfo.read(db, "incidents");
        if (!_infoIncidents.equals(_existingIncidents)) {
          return new RoomOpenHelper.ValidationResult(false, "incidents(com.sih2026.touristsafety.data.local.entities.IncidentEntity).\n"
                  + " Expected:\n" + _infoIncidents + "\n"
                  + " Found:\n" + _existingIncidents);
        }
        final HashMap<String, TableInfo.Column> _columnsDisasterAlerts = new HashMap<String, TableInfo.Column>(12);
        _columnsDisasterAlerts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("capIdentifier", new TableInfo.Column("capIdentifier", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("hazardType", new TableInfo.Column("hazardType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("severity", new TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("urgency", new TableInfo.Column("urgency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("headline", new TableInfo.Column("headline", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("instructions", new TableInfo.Column("instructions", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("affectedStates", new TableInfo.Column("affectedStates", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDisasterAlerts.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDisasterAlerts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDisasterAlerts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDisasterAlerts = new TableInfo("disaster_alerts", _columnsDisasterAlerts, _foreignKeysDisasterAlerts, _indicesDisasterAlerts);
        final TableInfo _existingDisasterAlerts = TableInfo.read(db, "disaster_alerts");
        if (!_infoDisasterAlerts.equals(_existingDisasterAlerts)) {
          return new RoomOpenHelper.ValidationResult(false, "disaster_alerts(com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity).\n"
                  + " Expected:\n" + _infoDisasterAlerts + "\n"
                  + " Found:\n" + _existingDisasterAlerts);
        }
        final HashMap<String, TableInfo.Column> _columnsDocuments = new HashMap<String, TableInfo.Column>(9);
        _columnsDocuments.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("docType", new TableInfo.Column("docType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("docNumber", new TableInfo.Column("docNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("fileUrl", new TableInfo.Column("fileUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("expiryDate", new TableInfo.Column("expiryDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("verificationStatus", new TableInfo.Column("verificationStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("metadata", new TableInfo.Column("metadata", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocuments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDocuments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDocuments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDocuments = new TableInfo("documents", _columnsDocuments, _foreignKeysDocuments, _indicesDocuments);
        final TableInfo _existingDocuments = TableInfo.read(db, "documents");
        if (!_infoDocuments.equals(_existingDocuments)) {
          return new RoomOpenHelper.ValidationResult(false, "documents(com.sih2026.touristsafety.data.local.entities.DocumentEntity).\n"
                  + " Expected:\n" + _infoDocuments + "\n"
                  + " Found:\n" + _existingDocuments);
        }
        final HashMap<String, TableInfo.Column> _columnsChatMessages = new HashMap<String, TableInfo.Column>(7);
        _columnsChatMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("metadata", new TableInfo.Column("metadata", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChatMessages.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChatMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChatMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChatMessages = new TableInfo("chat_messages", _columnsChatMessages, _foreignKeysChatMessages, _indicesChatMessages);
        final TableInfo _existingChatMessages = TableInfo.read(db, "chat_messages");
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "chat_messages(com.sih2026.touristsafety.data.local.entities.ChatMessageEntity).\n"
                  + " Expected:\n" + _infoChatMessages + "\n"
                  + " Found:\n" + _existingChatMessages);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "5d860cb421c7cc084936d11829061b97", "c8b33bfa9f4836fc9f9efe650ba0d16b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "profiles","emergency_contacts","geofence_zones","incidents","disaster_alerts","documents","chat_messages");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `profiles`");
      _db.execSQL("DELETE FROM `emergency_contacts`");
      _db.execSQL("DELETE FROM `geofence_zones`");
      _db.execSQL("DELETE FROM `incidents`");
      _db.execSQL("DELETE FROM `disaster_alerts`");
      _db.execSQL("DELETE FROM `documents`");
      _db.execSQL("DELETE FROM `chat_messages`");
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
    _typeConvertersMap.put(ProfileDao.class, ProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GeofenceZoneDao.class, GeofenceZoneDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IncidentDao.class, IncidentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DisasterAlertDao.class, DisasterAlertDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DocumentDao.class, DocumentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChatMessageDao.class, ChatMessageDao_Impl.getRequiredConverters());
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
  public ProfileDao profileDao() {
    if (_profileDao != null) {
      return _profileDao;
    } else {
      synchronized(this) {
        if(_profileDao == null) {
          _profileDao = new ProfileDao_Impl(this);
        }
        return _profileDao;
      }
    }
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }

  @Override
  public GeofenceZoneDao geofenceZoneDao() {
    if (_geofenceZoneDao != null) {
      return _geofenceZoneDao;
    } else {
      synchronized(this) {
        if(_geofenceZoneDao == null) {
          _geofenceZoneDao = new GeofenceZoneDao_Impl(this);
        }
        return _geofenceZoneDao;
      }
    }
  }

  @Override
  public IncidentDao incidentDao() {
    if (_incidentDao != null) {
      return _incidentDao;
    } else {
      synchronized(this) {
        if(_incidentDao == null) {
          _incidentDao = new IncidentDao_Impl(this);
        }
        return _incidentDao;
      }
    }
  }

  @Override
  public DisasterAlertDao disasterAlertDao() {
    if (_disasterAlertDao != null) {
      return _disasterAlertDao;
    } else {
      synchronized(this) {
        if(_disasterAlertDao == null) {
          _disasterAlertDao = new DisasterAlertDao_Impl(this);
        }
        return _disasterAlertDao;
      }
    }
  }

  @Override
  public DocumentDao documentDao() {
    if (_documentDao != null) {
      return _documentDao;
    } else {
      synchronized(this) {
        if(_documentDao == null) {
          _documentDao = new DocumentDao_Impl(this);
        }
        return _documentDao;
      }
    }
  }

  @Override
  public ChatMessageDao chatMessageDao() {
    if (_chatMessageDao != null) {
      return _chatMessageDao;
    } else {
      synchronized(this) {
        if(_chatMessageDao == null) {
          _chatMessageDao = new ChatMessageDao_Impl(this);
        }
        return _chatMessageDao;
      }
    }
  }
}
