package com.sih2026.touristsafety;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.sih2026.touristsafety.data.local.TouristSafetyDatabase;
import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao;
import com.sih2026.touristsafety.data.local.dao.DocumentDao;
import com.sih2026.touristsafety.data.local.dao.EmergencyContactDao;
import com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao;
import com.sih2026.touristsafety.data.repository.DisasterAlertRepository;
import com.sih2026.touristsafety.di.AppModule_ProvideDatabaseFactory;
import com.sih2026.touristsafety.di.AppModule_ProvideDisasterAlertDaoFactory;
import com.sih2026.touristsafety.di.AppModule_ProvideDocumentDaoFactory;
import com.sih2026.touristsafety.di.AppModule_ProvideEmergencyContactDaoFactory;
import com.sih2026.touristsafety.di.AppModule_ProvideGeofenceZoneDaoFactory;
import com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsViewModel;
import com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotViewModel;
import com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsViewModel;
import com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.efir.EFirViewModel;
import com.sih2026.touristsafety.presentation.screens.efir.EFirViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.eprofile.EProfileViewModel;
import com.sih2026.touristsafety.presentation.screens.eprofile.EProfileViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportViewModel;
import com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.map.TouristMapViewModel;
import com.sih2026.touristsafety.presentation.screens.map.TouristMapViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyViewModel;
import com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.sos.SOSViewModel;
import com.sih2026.touristsafety.presentation.screens.sos.SOSViewModel_HiltModules;
import com.sih2026.touristsafety.presentation.screens.translator.TranslatorViewModel;
import com.sih2026.touristsafety.presentation.screens.translator.TranslatorViewModel_HiltModules;
import com.sih2026.touristsafety.services.GeofenceManager;
import com.sih2026.touristsafety.services.GeofencingService;
import com.sih2026.touristsafety.services.GeofencingService_MembersInjector;
import com.sih2026.touristsafety.services.SOSManager;
import com.sih2026.touristsafety.services.TranslationService;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerTouristSafetyApp_HiltComponents_SingletonC {
  private DaggerTouristSafetyApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TouristSafetyApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TouristSafetyApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TouristSafetyApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TouristSafetyApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TouristSafetyApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TouristSafetyApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TouristSafetyApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TouristSafetyApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TouristSafetyApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TouristSafetyApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TouristSafetyApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TouristSafetyApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TouristSafetyApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(10).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel, ChatbotViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel, DisasterAlertsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel, EFirViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel, EProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel, EmergencyContactsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel, IncidentReportViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel, SOSViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel, TouristMapViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel, TranslatorViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel, WomenSafetyViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel = "com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsViewModel";

      static String com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel = "com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyViewModel";

      static String com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel = "com.sih2026.touristsafety.presentation.screens.efir.EFirViewModel";

      static String com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel = "com.sih2026.touristsafety.presentation.screens.map.TouristMapViewModel";

      static String com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel = "com.sih2026.touristsafety.presentation.screens.sos.SOSViewModel";

      static String com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel = "com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotViewModel";

      static String com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel = "com.sih2026.touristsafety.presentation.screens.eprofile.EProfileViewModel";

      static String com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel = "com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsViewModel";

      static String com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel = "com.sih2026.touristsafety.presentation.screens.translator.TranslatorViewModel";

      static String com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel = "com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportViewModel";

      @KeepFieldType
      DisasterAlertsViewModel com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel2;

      @KeepFieldType
      WomenSafetyViewModel com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel2;

      @KeepFieldType
      EFirViewModel com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel2;

      @KeepFieldType
      TouristMapViewModel com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel2;

      @KeepFieldType
      SOSViewModel com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel2;

      @KeepFieldType
      ChatbotViewModel com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel2;

      @KeepFieldType
      EProfileViewModel com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel2;

      @KeepFieldType
      EmergencyContactsViewModel com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel2;

      @KeepFieldType
      TranslatorViewModel com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel2;

      @KeepFieldType
      IncidentReportViewModel com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel2;
    }
  }

  private static final class ViewModelCImpl extends TouristSafetyApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ChatbotViewModel> chatbotViewModelProvider;

    private Provider<DisasterAlertsViewModel> disasterAlertsViewModelProvider;

    private Provider<EFirViewModel> eFirViewModelProvider;

    private Provider<EProfileViewModel> eProfileViewModelProvider;

    private Provider<EmergencyContactsViewModel> emergencyContactsViewModelProvider;

    private Provider<IncidentReportViewModel> incidentReportViewModelProvider;

    private Provider<SOSViewModel> sOSViewModelProvider;

    private Provider<TouristMapViewModel> touristMapViewModelProvider;

    private Provider<TranslatorViewModel> translatorViewModelProvider;

    private Provider<WomenSafetyViewModel> womenSafetyViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.chatbotViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.disasterAlertsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.eFirViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.eProfileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.emergencyContactsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.incidentReportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.sOSViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.touristMapViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.translatorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.womenSafetyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(10).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel, ((Provider) chatbotViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel, ((Provider) disasterAlertsViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel, ((Provider) eFirViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel, ((Provider) eProfileViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel, ((Provider) emergencyContactsViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel, ((Provider) incidentReportViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel, ((Provider) sOSViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel, ((Provider) touristMapViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel, ((Provider) translatorViewModelProvider)).put(LazyClassKeyProvider.com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel, ((Provider) womenSafetyViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel = "com.sih2026.touristsafety.presentation.screens.eprofile.EProfileViewModel";

      static String com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel = "com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsViewModel";

      static String com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel = "com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotViewModel";

      static String com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel = "com.sih2026.touristsafety.presentation.screens.sos.SOSViewModel";

      static String com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel = "com.sih2026.touristsafety.presentation.screens.map.TouristMapViewModel";

      static String com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel = "com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportViewModel";

      static String com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel = "com.sih2026.touristsafety.presentation.screens.efir.EFirViewModel";

      static String com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel = "com.sih2026.touristsafety.presentation.screens.translator.TranslatorViewModel";

      static String com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel = "com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsViewModel";

      static String com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel = "com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyViewModel";

      @KeepFieldType
      EProfileViewModel com_sih2026_touristsafety_presentation_screens_eprofile_EProfileViewModel2;

      @KeepFieldType
      EmergencyContactsViewModel com_sih2026_touristsafety_presentation_screens_contacts_EmergencyContactsViewModel2;

      @KeepFieldType
      ChatbotViewModel com_sih2026_touristsafety_presentation_screens_chatbot_ChatbotViewModel2;

      @KeepFieldType
      SOSViewModel com_sih2026_touristsafety_presentation_screens_sos_SOSViewModel2;

      @KeepFieldType
      TouristMapViewModel com_sih2026_touristsafety_presentation_screens_map_TouristMapViewModel2;

      @KeepFieldType
      IncidentReportViewModel com_sih2026_touristsafety_presentation_screens_incidents_IncidentReportViewModel2;

      @KeepFieldType
      EFirViewModel com_sih2026_touristsafety_presentation_screens_efir_EFirViewModel2;

      @KeepFieldType
      TranslatorViewModel com_sih2026_touristsafety_presentation_screens_translator_TranslatorViewModel2;

      @KeepFieldType
      DisasterAlertsViewModel com_sih2026_touristsafety_presentation_screens_alerts_DisasterAlertsViewModel2;

      @KeepFieldType
      WomenSafetyViewModel com_sih2026_touristsafety_presentation_screens_safety_WomenSafetyViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotViewModel 
          return (T) new ChatbotViewModel();

          case 1: // com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsViewModel 
          return (T) new DisasterAlertsViewModel(singletonCImpl.disasterAlertRepositoryProvider.get());

          case 2: // com.sih2026.touristsafety.presentation.screens.efir.EFirViewModel 
          return (T) new EFirViewModel();

          case 3: // com.sih2026.touristsafety.presentation.screens.eprofile.EProfileViewModel 
          return (T) new EProfileViewModel(singletonCImpl.documentDao());

          case 4: // com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsViewModel 
          return (T) new EmergencyContactsViewModel(singletonCImpl.emergencyContactDao());

          case 5: // com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportViewModel 
          return (T) new IncidentReportViewModel();

          case 6: // com.sih2026.touristsafety.presentation.screens.sos.SOSViewModel 
          return (T) new SOSViewModel(singletonCImpl.sOSManagerProvider.get());

          case 7: // com.sih2026.touristsafety.presentation.screens.map.TouristMapViewModel 
          return (T) new TouristMapViewModel();

          case 8: // com.sih2026.touristsafety.presentation.screens.translator.TranslatorViewModel 
          return (T) new TranslatorViewModel(singletonCImpl.translationServiceProvider.get());

          case 9: // com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyViewModel 
          return (T) new WomenSafetyViewModel();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TouristSafetyApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TouristSafetyApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectGeofencingService(GeofencingService geofencingService) {
      injectGeofencingService2(geofencingService);
    }

    private GeofencingService injectGeofencingService2(GeofencingService instance) {
      GeofencingService_MembersInjector.injectGeofenceManager(instance, singletonCImpl.geofenceManagerProvider.get());
      GeofencingService_MembersInjector.injectGeofenceZoneDao(instance, singletonCImpl.geofenceZoneDao());
      return instance;
    }
  }

  private static final class SingletonCImpl extends TouristSafetyApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TouristSafetyDatabase> provideDatabaseProvider;

    private Provider<DisasterAlertRepository> disasterAlertRepositoryProvider;

    private Provider<SOSManager> sOSManagerProvider;

    private Provider<TranslationService> translationServiceProvider;

    private Provider<GeofenceManager> geofenceManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private DisasterAlertDao disasterAlertDao() {
      return AppModule_ProvideDisasterAlertDaoFactory.provideDisasterAlertDao(provideDatabaseProvider.get());
    }

    private DocumentDao documentDao() {
      return AppModule_ProvideDocumentDaoFactory.provideDocumentDao(provideDatabaseProvider.get());
    }

    private EmergencyContactDao emergencyContactDao() {
      return AppModule_ProvideEmergencyContactDaoFactory.provideEmergencyContactDao(provideDatabaseProvider.get());
    }

    private GeofenceZoneDao geofenceZoneDao() {
      return AppModule_ProvideGeofenceZoneDaoFactory.provideGeofenceZoneDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<TouristSafetyDatabase>(singletonCImpl, 1));
      this.disasterAlertRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DisasterAlertRepository>(singletonCImpl, 0));
      this.sOSManagerProvider = DoubleCheck.provider(new SwitchingProvider<SOSManager>(singletonCImpl, 2));
      this.translationServiceProvider = DoubleCheck.provider(new SwitchingProvider<TranslationService>(singletonCImpl, 3));
      this.geofenceManagerProvider = DoubleCheck.provider(new SwitchingProvider<GeofenceManager>(singletonCImpl, 4));
    }

    @Override
    public void injectTouristSafetyApp(TouristSafetyApp touristSafetyApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.sih2026.touristsafety.data.repository.DisasterAlertRepository 
          return (T) new DisasterAlertRepository(singletonCImpl.disasterAlertDao());

          case 1: // com.sih2026.touristsafety.data.local.TouristSafetyDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.sih2026.touristsafety.services.SOSManager 
          return (T) new SOSManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.emergencyContactDao());

          case 3: // com.sih2026.touristsafety.services.TranslationService 
          return (T) new TranslationService();

          case 4: // com.sih2026.touristsafety.services.GeofenceManager 
          return (T) new GeofenceManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
