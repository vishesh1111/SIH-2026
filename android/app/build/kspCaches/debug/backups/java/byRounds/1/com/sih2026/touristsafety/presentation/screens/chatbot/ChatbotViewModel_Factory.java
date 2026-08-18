package com.sih2026.touristsafety.presentation.screens.chatbot;

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
public final class ChatbotViewModel_Factory implements Factory<ChatbotViewModel> {
  @Override
  public ChatbotViewModel get() {
    return newInstance();
  }

  public static ChatbotViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChatbotViewModel newInstance() {
    return new ChatbotViewModel();
  }

  private static final class InstanceHolder {
    private static final ChatbotViewModel_Factory INSTANCE = new ChatbotViewModel_Factory();
  }
}
