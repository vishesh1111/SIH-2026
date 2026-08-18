package com.sih2026.touristsafety.di;

import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.DocumentDao;
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
public final class AppModule_ProvideDocumentDaoFactory implements Factory<DocumentDao> {
  private final Provider<TouristSafetyDatabase> dbProvider;

  public AppModule_ProvideDocumentDaoFactory(Provider<TouristSafetyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DocumentDao get() {
    return provideDocumentDao(dbProvider.get());
  }

  public static AppModule_ProvideDocumentDaoFactory create(
      Provider<TouristSafetyDatabase> dbProvider) {
    return new AppModule_ProvideDocumentDaoFactory(dbProvider);
  }

  public static DocumentDao provideDocumentDao(TouristSafetyDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDocumentDao(db));
  }
}
