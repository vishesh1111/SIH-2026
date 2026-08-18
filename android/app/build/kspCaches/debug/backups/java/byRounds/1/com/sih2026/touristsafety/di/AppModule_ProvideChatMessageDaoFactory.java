package com.sih2026.touristsafety.di;

import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.ChatMessageDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideChatMessageDaoFactory implements Factory<ChatMessageDao> {
  private final Provider<TouristSafetyDatabase> dbProvider;

  public AppModule_ProvideChatMessageDaoFactory(Provider<TouristSafetyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChatMessageDao get() {
    return provideChatMessageDao(dbProvider.get());
  }

  public static AppModule_ProvideChatMessageDaoFactory create(
      Provider<TouristSafetyDatabase> dbProvider) {
    return new AppModule_ProvideChatMessageDaoFactory(dbProvider);
  }

  public static ChatMessageDao provideChatMessageDao(TouristSafetyDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideChatMessageDao(db));
  }
}
