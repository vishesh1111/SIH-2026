package com.sih2026.touristsafety.presentation.screens.efir;

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
public final class EFirViewModel_Factory implements Factory<EFirViewModel> {
  @Override
  public EFirViewModel get() {
    return newInstance();
  }

  public static EFirViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EFirViewModel newInstance() {
    return new EFirViewModel();
  }

  private static final class InstanceHolder {
    private static final EFirViewModel_Factory INSTANCE = new EFirViewModel_Factory();
  }
}
