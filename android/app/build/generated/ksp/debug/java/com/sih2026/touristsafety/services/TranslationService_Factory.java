package com.sih2026.touristsafety.services;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class TranslationService_Factory implements Factory<TranslationService> {
  @Override
  public TranslationService get() {
    return newInstance();
  }

  public static TranslationService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TranslationService newInstance() {
    return new TranslationService();
  }

  private static final class InstanceHolder {
    private static final TranslationService_Factory INSTANCE = new TranslationService_Factory();
  }
}
