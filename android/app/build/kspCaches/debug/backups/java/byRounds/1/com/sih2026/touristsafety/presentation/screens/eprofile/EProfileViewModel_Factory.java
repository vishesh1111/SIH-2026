package com.sih2026.touristsafety.presentation.screens.eprofile;

import com.sih2026.touristsafety.data.local.dao.DocumentDao;
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
public final class EProfileViewModel_Factory implements Factory<EProfileViewModel> {
  private final Provider<DocumentDao> documentDaoProvider;

  public EProfileViewModel_Factory(Provider<DocumentDao> documentDaoProvider) {
    this.documentDaoProvider = documentDaoProvider;
  }

  @Override
  public EProfileViewModel get() {
    return newInstance(documentDaoProvider.get());
  }

  public static EProfileViewModel_Factory create(Provider<DocumentDao> documentDaoProvider) {
    return new EProfileViewModel_Factory(documentDaoProvider);
  }

  public static EProfileViewModel newInstance(DocumentDao documentDao) {
    return new EProfileViewModel(documentDao);
  }
}
