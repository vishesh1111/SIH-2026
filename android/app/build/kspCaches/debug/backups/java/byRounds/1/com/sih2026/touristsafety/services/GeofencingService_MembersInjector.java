package com.sih2026.touristsafety.services;

import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class GeofencingService_MembersInjector implements MembersInjector<GeofencingService> {
  private final Provider<GeofenceManager> geofenceManagerProvider;

  private final Provider<GeofenceZoneDao> geofenceZoneDaoProvider;

  public GeofencingService_MembersInjector(Provider<GeofenceManager> geofenceManagerProvider,
      Provider<GeofenceZoneDao> geofenceZoneDaoProvider) {
    this.geofenceManagerProvider = geofenceManagerProvider;
    this.geofenceZoneDaoProvider = geofenceZoneDaoProvider;
  }

  public static MembersInjector<GeofencingService> create(
      Provider<GeofenceManager> geofenceManagerProvider,
      Provider<GeofenceZoneDao> geofenceZoneDaoProvider) {
    return new GeofencingService_MembersInjector(geofenceManagerProvider, geofenceZoneDaoProvider);
  }

  @Override
  public void injectMembers(GeofencingService instance) {
    injectGeofenceManager(instance, geofenceManagerProvider.get());
    injectGeofenceZoneDao(instance, geofenceZoneDaoProvider.get());
  }

  @InjectedFieldSignature("com.sih2026.touristsafety.services.GeofencingService.geofenceManager")
  public static void injectGeofenceManager(GeofencingService instance,
      GeofenceManager geofenceManager) {
    instance.geofenceManager = geofenceManager;
  }

  @InjectedFieldSignature("com.sih2026.touristsafety.services.GeofencingService.geofenceZoneDao")
  public static void injectGeofenceZoneDao(GeofencingService instance,
      GeofenceZoneDao geofenceZoneDao) {
    instance.geofenceZoneDao = geofenceZoneDao;
  }
}
