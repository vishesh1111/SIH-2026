import numpy as np  
import librosa  
import pickle
import joblib  

# Specify the path to your WAV file  
filename = r'D:\Shivang\SIH\FINAL SCRIPTS\Testing\samples\talk.wav'  # Replace with your actual file path  

# Load the wave file with the default sample rate  
test, sr = librosa.load(filename, sr=None)  # sr=None preserves the original sample rate  

# Extract MFCC features  
mfccs = np.mean(librosa.feature.mfcc(y=test, sr=sr, n_mfcc=40).T, axis=0)  
tester = np.array([mfccs])  # Create an array for the MFCCs  

# Load the phase_1 model (noise vs speech)  
load_model = joblib.load(open('model1.sav', 'rb'))  

# Predict if the result indicates noise or human sound  
result = load_model.predict(tester)  

# Load the phase_2 model  
load_model2 = joblib.load(open('model2.sav', 'rb'))  

if result[0] == 2:  # Check if the sound is noise or human  
    print("Phase-1 clear")  
    ok = load_model2.predict(tester)  # Use the second phase model  
    if ok[0] == 1:  
        print("Scream detected")  
    else:  
        print("Speech detected")  
else:  
    print("Noise detected")





# import numpy as np  
# import librosa  
# import joblib  

# def detect_sound(filename, model1_path='model1.sav', model2_path='model2.sav'):  
#     """  
#     Detects whether the sound in the given WAV file is a scream, speech, or noise.  

#     Parameters:  
#     - filename: str, path to the WAV file.  
#     - model1_path: str, path to the first model (noise vs speech).  
#     - model2_path: str, path to the second model (scream vs speech).  

#     Returns:  
#     - None  
#     """  
#     # Load the wave file with the default sample rate  
#     test, sr = librosa.load(filename, sr=None)  # sr=None preserves the original sample rate  

#     # Extract MFCC features  
#     mfccs = np.mean(librosa.feature.mfcc(y=test, sr=sr, n_mfcc=40).T, axis=0)  
#     tester = np.array([mfccs])  # Create an array for the MFCCs  

#     # Load the phase_1 model (noise vs speech)  
#     load_model = joblib.load(model1_path)  

#     # Predict if the result indicates noise or human sound  
#     result = load_model.predict(tester)  

#     # Load the phase_2 model  
#     load_model2 = joblib.load(model2_path)  

#     if result[0] == 2:  # Check if the sound is noise or human  
#         print("Phase-1 clear")  
#         ok = load_model2.predict(tester)  # Use the second phase model  
#         if ok[0] == 1:  
#             print("Scream detected")  
#         else:  
#             print("Speech detected")  
#     else:  
#         print("Noise detected")  

# # Example usage  
# if __name__ == "__main__":  
#     filename = r'D:\Shivang\SIH\FINAL SCRIPTS\Testing\samples\femaleScream-007.wav'  # Replace with your actual file path  
#     detect_sound(filename)