package com.sih2026.touristsafety.services;

import android.content.Context;
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao;
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
public final class SOSManager_Factory implements Factory<SOSManager> {
  private final Provider<Context> contextProvider;

  private final Provider<EmergencyContactDao> contactDaoProvider;

  public SOSManager_Factory(Provider<Context> contextProvider,
      Provider<EmergencyContactDao> contactDaoProvider) {
    this.contextProvider = contextProvider;
    this.contactDaoProvider = contactDaoProvider;
  }

  @Override
  public SOSManager get() {
    return newInstance(contextProvider.get(), contactDaoProvider.get());
  }

  public static SOSManager_Factory create(Provider<Context> contextProvider,
      Provider<EmergencyContactDao> contactDaoProvider) {
    return new SOSManager_Factory(contextProvider, contactDaoProvider);
  }

  public static SOSManager newInstance(Context context, EmergencyContactDao contactDao) {
    return new SOSManager(context, contactDao);
  }
}
