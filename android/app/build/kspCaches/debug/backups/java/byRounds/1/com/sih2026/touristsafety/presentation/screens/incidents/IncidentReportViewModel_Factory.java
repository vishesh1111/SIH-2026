package com.sih2026.touristsafety.presentation.screens.incidents;

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
public final class IncidentReportViewModel_Factory implements Factory<IncidentReportViewModel> {
  @Override
  public IncidentReportViewModel get() {
    return newInstance();
  }

  public static IncidentReportViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static IncidentReportViewModel newInstance() {
    return new IncidentReportViewModel();
  }

  private static final class InstanceHolder {
    private static final IncidentReportViewModel_Factory INSTANCE = new IncidentReportViewModel_Factory();
  }
}
