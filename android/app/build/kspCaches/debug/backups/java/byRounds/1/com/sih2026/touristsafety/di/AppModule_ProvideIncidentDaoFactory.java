package com.sih2026.touristsafety.di;

import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.IncidentDao;
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
public final class AppModule_ProvideIncidentDaoFactory implements Factory<IncidentDao> {
  private final Provider<TouristSafetyDatabase> dbProvider;

  public AppModule_ProvideIncidentDaoFactory(Provider<TouristSafetyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public IncidentDao get() {
    return provideIncidentDao(dbProvider.get());
  }

  public static AppModule_ProvideIncidentDaoFactory create(
      Provider<TouristSafetyDatabase> dbProvider) {
    return new AppModule_ProvideIncidentDaoFactory(dbProvider);
  }

  public static IncidentDao provideIncidentDao(TouristSafetyDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideIncidentDao(db));
  }
}
