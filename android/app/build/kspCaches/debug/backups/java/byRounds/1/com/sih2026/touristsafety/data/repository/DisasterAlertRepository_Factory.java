package com.sih2026.touristsafety.data.repository;

import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DisasterAlertRepository_Factory implements Factory<DisasterAlertRepository> {
  private final Provider<DisasterAlertDao> alertDaoProvider;

  public DisasterAlertRepository_Factory(Provider<DisasterAlertDao> alertDaoProvider) {
    this.alertDaoProvider = alertDaoProvider;
  }

  @Override
  public DisasterAlertRepository get() {
    return newInstance(alertDaoProvider.get());
  }

  public static DisasterAlertRepository_Factory create(
      Provider<DisasterAlertDao> alertDaoProvider) {
    return new DisasterAlertRepository_Factory(alertDaoProvider);
  }

  public static DisasterAlertRepository newInstance(DisasterAlertDao alertDao) {
    return new DisasterAlertRepository(alertDao);
  }
}
