package com.sih2026.touristsafety.presentation.screens.alerts;

import com.sih2026.touristsafety.data.repository.DisasterAlertRepository;
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
public final class DisasterAlertsViewModel_Factory implements Factory<DisasterAlertsViewModel> {
  private final Provider<DisasterAlertRepository> repositoryProvider;

  public DisasterAlertsViewModel_Factory(Provider<DisasterAlertRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DisasterAlertsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DisasterAlertsViewModel_Factory create(
      Provider<DisasterAlertRepository> repositoryProvider) {
    return new DisasterAlertsViewModel_Factory(repositoryProvider);
  }

  public static DisasterAlertsViewModel newInstance(DisasterAlertRepository repository) {
    return new DisasterAlertsViewModel(repository);
  }
}
