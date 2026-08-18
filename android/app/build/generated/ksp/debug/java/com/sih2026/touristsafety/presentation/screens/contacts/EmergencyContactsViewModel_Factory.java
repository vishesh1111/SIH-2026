package com.sih2026.touristsafety.presentation.screens.contacts;

import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao;
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
public final class EmergencyContactsViewModel_Factory implements Factory<EmergencyContactsViewModel> {
  private final Provider<EmergencyContactDao> contactDaoProvider;

  public EmergencyContactsViewModel_Factory(Provider<EmergencyContactDao> contactDaoProvider) {
    this.contactDaoProvider = contactDaoProvider;
  }

  @Override
  public EmergencyContactsViewModel get() {
    return newInstance(contactDaoProvider.get());
  }

  public static EmergencyContactsViewModel_Factory create(
      Provider<EmergencyContactDao> contactDaoProvider) {
    return new EmergencyContactsViewModel_Factory(contactDaoProvider);
  }

  public static EmergencyContactsViewModel newInstance(EmergencyContactDao contactDao) {
    return new EmergencyContactsViewModel(contactDao);
  }
}
