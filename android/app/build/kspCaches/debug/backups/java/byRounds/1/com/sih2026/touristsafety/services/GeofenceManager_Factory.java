package com.sih2026.touristsafety.services;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GeofenceManager_Factory implements Factory<GeofenceManager> {
  private final Provider<Context> contextProvider;

  public GeofenceManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GeofenceManager get() {
    return newInstance(contextProvider.get());
  }

  public static GeofenceManager_Factory create(Provider<Context> contextProvider) {
    return new GeofenceManager_Factory(contextProvider);
  }

  public static GeofenceManager newInstance(Context context) {
    return new GeofenceManager(context);
  }
}
