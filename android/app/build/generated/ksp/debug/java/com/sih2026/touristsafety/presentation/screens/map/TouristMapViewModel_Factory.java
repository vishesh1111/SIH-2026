package com.sih2026.touristsafety.presentation.screens.map;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class TouristMapViewModel_Factory implements Factory<TouristMapViewModel> {
  @Override
  public TouristMapViewModel get() {
    return newInstance();
  }

  public static TouristMapViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TouristMapViewModel newInstance() {
    return new TouristMapViewModel();
  }

  private static final class InstanceHolder {
    private static final TouristMapViewModel_Factory INSTANCE = new TouristMapViewModel_Factory();
  }
}
