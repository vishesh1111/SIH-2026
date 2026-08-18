package com.sih2026.touristsafety.di;

import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideGeofenceZoneDaoFactory implements Factory<GeofenceZoneDao> {
  private final Provider<TouristSafetyDatabase> dbProvider;

  public AppModule_ProvideGeofenceZoneDaoFactory(Provider<TouristSafetyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GeofenceZoneDao get() {
    return provideGeofenceZoneDao(dbProvider.get());
  }

  public static AppModule_ProvideGeofenceZoneDaoFactory create(
      Provider<TouristSafetyDatabase> dbProvider) {
    return new AppModule_ProvideGeofenceZoneDaoFactory(dbProvider);
  }

  public static GeofenceZoneDao provideGeofenceZoneDao(TouristSafetyDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGeofenceZoneDao(db));
  }
}
