package com.sih2026.touristsafety.di;

import android.content.Context;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
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
public final class AppModule_ProvideDataStoreFactory implements Factory<DataStore<Preferences>> {
  private final Provider<Context> appContextProvider;

  public AppModule_ProvideDataStoreFactory(Provider<Context> appContextProvider) {
    this.appContextProvider = appContextProvider;
  }

  @Override
  public DataStore<Preferences> get() {
    return provideDataStore(appContextProvider.get());
  }

  public static AppModule_ProvideDataStoreFactory create(Provider<Context> appContextProvider) {
    return new AppModule_ProvideDataStoreFactory(appContextProvider);
  }

  public static DataStore<Preferences> provideDataStore(Context appContext) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDataStore(appContext));
  }
}
