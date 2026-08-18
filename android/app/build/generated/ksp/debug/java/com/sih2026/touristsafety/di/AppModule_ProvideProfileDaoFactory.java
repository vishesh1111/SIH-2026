package com.sih2026.touristsafety.di;

import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.ProfileDao;
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
public final class AppModule_ProvideProfileDaoFactory implements Factory<ProfileDao> {
  private final Provider<TouristSafetyDatabase> dbProvider;

  public AppModule_ProvideProfileDaoFactory(Provider<TouristSafetyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProfileDao get() {
    return provideProfileDao(dbProvider.get());
  }

  public static AppModule_ProvideProfileDaoFactory create(
      Provider<TouristSafetyDatabase> dbProvider) {
    return new AppModule_ProvideProfileDaoFactory(dbProvider);
  }

  public static ProfileDao provideProfileDao(TouristSafetyDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideProfileDao(db));
  }
}
