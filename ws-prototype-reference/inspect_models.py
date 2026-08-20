import joblib
import numpy as np

# Inspect sklearn models
m1 = joblib.load('models/s_phase1.sav')
m2 = joblib.load('models/s_phase2.sav')
print('Phase 1 type:', type(m1).__name__)
print('Phase 1 params:', m1.get_params())
if hasattr(m1, 'n_estimators'):
    print('Phase 1 n_estimators:', m1.n_estimators)
if hasattr(m1, 'classes_'):
    print('Phase 1 classes:', m1.classes_)
if hasattr(m1, 'n_features_in_'):
    print('Phase 1 n_features_in:', m1.n_features_in_)
print()
print('Phase 2 type:', type(m2).__name__)
print('Phase 2 params:', m2.get_params())
if hasattr(m2, 'n_estimators'):
    print('Phase 2 n_estimators:', m2.n_estimators)
if hasattr(m2, 'classes_'):
    print('Phase 2 classes:', m2.classes_)
if hasattr(m2, 'n_features_in_'):
    print('Phase 2 n_features_in:', m2.n_features_in_)

# Test with dummy input
dummy = np.random.randn(1, 40).astype(np.float32)
print('\nPhase 1 predict on dummy:', m1.predict(dummy))
print('Phase 2 predict on dummy:', m2.predict(dummy))
