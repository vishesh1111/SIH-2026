package com.sih2026.touristsafety.presentation.screens.translator;

import com.sih2026.touristsafety.services.TranslationService;
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
public final class TranslatorViewModel_Factory implements Factory<TranslatorViewModel> {
  private final Provider<TranslationService> translationServiceProvider;

  public TranslatorViewModel_Factory(Provider<TranslationService> translationServiceProvider) {
    this.translationServiceProvider = translationServiceProvider;
  }

  @Override
  public TranslatorViewModel get() {
    return newInstance(translationServiceProvider.get());
  }

  public static TranslatorViewModel_Factory create(
      Provider<TranslationService> translationServiceProvider) {
    return new TranslatorViewModel_Factory(translationServiceProvider);
  }

  public static TranslatorViewModel newInstance(TranslationService translationService) {
    return new TranslatorViewModel(translationService);
  }
}
