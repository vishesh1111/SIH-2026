package com.sih2026.touristsafety.presentation.screens.safety;

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
public final class WomenSafetyViewModel_Factory implements Factory<WomenSafetyViewModel> {
  @Override
  public WomenSafetyViewModel get() {
    return newInstance();
  }

  public static WomenSafetyViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WomenSafetyViewModel newInstance() {
    return new WomenSafetyViewModel();
  }

  private static final class InstanceHolder {
    private static final WomenSafetyViewModel_Factory INSTANCE = new WomenSafetyViewModel_Factory();
  }
}
