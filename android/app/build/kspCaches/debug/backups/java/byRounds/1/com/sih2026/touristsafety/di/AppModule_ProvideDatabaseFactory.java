package com.sih2026.touristsafety.di;

import android.content.Context;
import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideDatabaseFactory implements Factory<TouristSafetyDatabase> {
  private final Provider<Context> appContextProvider;

  public AppModule_ProvideDatabaseFactory(Provider<Context> appContextProvider) {
    this.appContextProvider = appContextProvider;
  }

  @Override
  public TouristSafetyDatabase get() {
    return provideDatabase(appContextProvider.get());
  }

  public static AppModule_ProvideDatabaseFactory create(Provider<Context> appContextProvider) {
    return new AppModule_ProvideDatabaseFactory(appContextProvider);
  }

  public static TouristSafetyDatabase provideDatabase(Context appContext) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDatabase(appContext));
  }
}
