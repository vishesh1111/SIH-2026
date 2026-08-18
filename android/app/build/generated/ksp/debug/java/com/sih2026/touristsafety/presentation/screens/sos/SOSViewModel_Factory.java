package com.sih2026.touristsafety.presentation.screens.sos;

import com.sih2026.touristsafety.services.SOSManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SOSViewModel_Factory implements Factory<SOSViewModel> {
  private final Provider<SOSManager> sosManagerProvider;

  public SOSViewModel_Factory(Provider<SOSManager> sosManagerProvider) {
    this.sosManagerProvider = sosManagerProvider;
  }

  @Override
  public SOSViewModel get() {
    return newInstance(sosManagerProvider.get());
  }

  public static SOSViewModel_Factory create(Provider<SOSManager> sosManagerProvider) {
    return new SOSViewModel_Factory(sosManagerProvider);
  }

  public static SOSViewModel newInstance(SOSManager sosManager) {
    return new SOSViewModel(sosManager);
  }
}
