"""
Export all ML model parameters for Android integration.

- SVM models (Phase 1 & 2): Export support vectors, dual coefficients, intercept as JSON
- Keras models (Gender & Distress): Convert to TFLite format

The SVM parameters will be embedded directly in Kotlin code.
The TFLite files will go into android/app/src/main/assets/models/
"""
import joblib
import numpy as np
import json
import os
import warnings
warnings.filterwarnings('ignore')

OUTPUT_DIR = '../android/app/src/main/assets/models'
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ============================================================
# 1. Export SVM Parameters as JSON
# ============================================================
print("=" * 60)
print("EXPORTING SVM MODELS")
print("=" * 60)

# Phase 1: Linear SVC (noise vs human)
m1 = joblib.load('models/s_phase1.sav')
phase1_data = {
    'kernel': m1.kernel,
    'gamma': float(m1._gamma) if hasattr(m1, '_gamma') else m1.gamma,
    'classes': m1.classes_.tolist(),
    'support_vectors': m1.support_vectors_.tolist(),
    'dual_coef': m1.dual_coef_.tolist(),
    'intercept': m1.intercept_.tolist(),
    'n_support': m1.n_support_.tolist(),
}
with open(os.path.join(OUTPUT_DIR, 'svm_phase1.json'), 'w') as f:
    json.dump(phase1_data, f)
print(f"Phase 1 SVM: kernel={m1.kernel}, support_vectors shape={m1.support_vectors_.shape}")
print(f"  Classes: {m1.classes_}")
print(f"  Saved to {OUTPUT_DIR}/svm_phase1.json")

# Phase 2: RBF SVC (scream vs speech)
m2 = joblib.load('models/s_phase2.sav')

# For RBF kernel, we need to compute gamma properly
if m2.gamma == 'scale':
    gamma_val = 1.0 / (m2.n_features_in_ * m2.support_vectors_.var())
else:
    gamma_val = float(m2.gamma) if isinstance(m2.gamma, (int, float)) else 1e-05

phase2_data = {
    'kernel': m2.kernel,
    'gamma': gamma_val,
    'classes': m2.classes_.tolist(),
    'support_vectors': m2.support_vectors_.tolist(),
    'dual_coef': m2.dual_coef_.tolist(),
    'intercept': m2.intercept_.tolist(),
    'n_support': m2.n_support_.tolist(),
}
with open(os.path.join(OUTPUT_DIR, 'svm_phase2.json'), 'w') as f:
    json.dump(phase2_data, f)
print(f"Phase 2 SVM: kernel={m2.kernel}, gamma={gamma_val}, support_vectors shape={m2.support_vectors_.shape}")
print(f"  Classes: {m2.classes_}")
print(f"  Saved to {OUTPUT_DIR}/svm_phase2.json")

# Verify: test with random input
dummy = np.random.randn(1, 40).astype(np.float64)
print(f"\nVerification - Phase 1 predict: {m1.predict(dummy)}")
print(f"Verification - Phase 2 predict: {m2.predict(dummy)}")

# ============================================================
# 2. Convert Keras Models to TFLite
# ============================================================
print("\n" + "=" * 60)
print("CONVERTING KERAS MODELS TO TFLITE")
print("=" * 60)

try:
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
    import tensorflow as tf
    
    # --- Gender Model ---
    from utils import create_model
    gender_model = create_model()
    gender_model.load_weights("models/gender.h5")
    
    converter = tf.lite.TFLiteConverter.from_keras_model(gender_model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_gender = converter.convert()
    
    gender_path = os.path.join(OUTPUT_DIR, 'gender.tflite')
    with open(gender_path, 'wb') as f:
        f.write(tflite_gender)
    print(f"Gender model converted: {len(tflite_gender)} bytes -> {gender_path}")
    
    # --- Distress Model ---
    from utils import create_model2
    distress_model = create_model2()
    distress_model.load_weights("models/distress.h5")
    
    converter = tf.lite.TFLiteConverter.from_keras_model(distress_model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_distress = converter.convert()
    
    distress_path = os.path.join(OUTPUT_DIR, 'distress.tflite')
    with open(distress_path, 'wb') as f:
        f.write(tflite_distress)
    print(f"Distress model converted: {len(tflite_distress)} bytes -> {distress_path}")
    
    print("\nTFLite conversion complete!")
    
except ImportError as e:
    print(f"\nTensorFlow not installed: {e}")
    print("Will install tensorflow and retry...")
    import subprocess
    subprocess.check_call(['pip3', 'install', 'tensorflow'])
    print("TensorFlow installed. Please re-run this script.")

except Exception as e:
    print(f"\nError during TFLite conversion: {e}")
    import traceback
    traceback.print_exc()

print("\n" + "=" * 60)
print("DONE - Files in:", OUTPUT_DIR)
print("=" * 60)
os.system(f'ls -la {OUTPUT_DIR}')
