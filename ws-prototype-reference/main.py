import pyaudio
import os
import wave
import librosa
import numpy as np
from sys import byteorder
from array import array
from struct import pack
import joblib
import time
from scipy.io import wavfile 


THRESHOLD = 500
CHUNK_SIZE = 1024
FORMAT = pyaudio.paInt16
RATE = 16000

SILENCE = 30


def is_silent(snd_data):
    "Returns 'True' if below the 'silent' threshold"
    return max(snd_data) < THRESHOLD

def normalize(snd_data):
    "Average the volume out"
    MAXIMUM = 16384
    times = float(MAXIMUM)/max(abs(i) for i in snd_data)

    r = array('h')
    for i in snd_data:
        r.append(int(i*times))
    return r

def trim(snd_data):
    "Trim the blank spots at the start and end"
    def _trim(snd_data):
        snd_started = False
        r = array('h')

        for i in snd_data:
            if not snd_started and abs(i)>THRESHOLD:
                snd_started = True
                r.append(i)

            elif snd_started:
                r.append(i)
        return r

    # Trim to the left
    snd_data = _trim(snd_data)

    # Trim to the right
    snd_data.reverse()
    snd_data = _trim(snd_data)
    snd_data.reverse()
    return snd_data

def add_silence(snd_data, seconds):
    "Add silence to the start and end of 'snd_data' of length 'seconds' (float)"
    r = array('h', [0 for i in range(int(seconds*RATE))])
    r.extend(snd_data)
    r.extend([0 for i in range(int(seconds*RATE))])
    return r

def record():
    """
    Record a word or words from the microphone and 
    return the data as an array of signed shorts.
    Normalizes the audio, trims silence from the 
    start and end, and pads with 0.5 seconds of 
    blank sound to make sure VLC et al can play 
    it without getting chopped off.
    """
    p = pyaudio.PyAudio()
    stream = p.open(format=FORMAT, channels=1, rate=RATE,
        input=True, output=True,
        frames_per_buffer=CHUNK_SIZE)

    num_silent = 0
    snd_started = False

    r = array('h')
    start_time = time.time()
    while 1:
        if(time.time() - start_time > 5):
            break
        # little endian, signed short
        snd_data = array('h', stream.read(CHUNK_SIZE))
        if byteorder == 'big':
            snd_data.byteswap()
        r.extend(snd_data)

        silent = is_silent(snd_data)

        if silent and snd_started:
            num_silent += 1
        elif not silent and not snd_started:
            snd_started = True

        if snd_started and num_silent > SILENCE:
            break

    sample_width = p.get_sample_size(FORMAT)
    stream.stop_stream()
    stream.close()
    p.terminate()

    r = normalize(r)
    r = trim(r)
    r = add_silence(r, 0.5)
    return sample_width, r

def record_to_file(path):
    "Records from the microphone and outputs the resulting data to 'path'"
    sample_width, data = record()
    data = pack('<' + ('h'*len(data)), *data)

    wf = wave.open(path, 'wb')
    wf.setnchannels(1)
    wf.setsampwidth(sample_width)
    wf.setframerate(RATE)
    wf.writeframes(data)
    wf.close()



def extract_feature(file_name, **kwargs):
    """
    Extract feature from audio file `file_name`
        Features supported:
            - MFCC (mfcc)
            - Chroma (chroma)
            - MEL Spectrogram Frequency (mel)
            - Contrast (contrast)
            - Tonnetz (tonnetz)
        e.g:
        `features = extract_feature(path, mel=True, mfcc=True)`
    """
    mfcc = kwargs.get("mfcc")
    chroma = kwargs.get("chroma")
    mel = kwargs.get("mel")
    contrast = kwargs.get("contrast")
    tonnetz = kwargs.get("tonnetz")
    X, sample_rate = librosa.core.load(file_name)
    if chroma or contrast:
        stft = np.abs(librosa.stft(X))
    result = np.array([])
    if mfcc:
        mfccs = np.mean(librosa.feature.mfcc(y=X, sr=sample_rate, n_mfcc=40).T, axis=0)
        result = np.hstack((result, mfccs))
    if chroma:
        chroma = np.mean(librosa.feature.chroma_stft(S=stft, sr=sample_rate).T,axis=0)
        result = np.hstack((result, chroma))
    if mel:
        mel = np.mean(librosa.feature.melspectrogram(y=X, sr=sample_rate).T,axis=0)
        result = np.hstack((result, mel))
    if contrast:
        contrast = np.mean(librosa.feature.spectral_contrast(S=stft, sr=sample_rate).T,axis=0)
        result = np.hstack((result, contrast))
    if tonnetz:
        tonnetz = np.mean(librosa.feature.tonnetz(y=librosa.effects.harmonic(X), sr=sample_rate).T,axis=0)
        result = np.hstack((result, tonnetz))
    return result

# Function for command line usage
def argpass_to_file():
    import argparse
    parser = argparse.ArgumentParser(description="""Gender recognition script, this will load the model you trained, 
                                and perform inference on a sample you provide (either using your voice or a file)""")
    parser.add_argument("-f", "--file", help="The path to the file, preferred to be in WAV format")
    args = parser.parse_args()
    file = args.file

    b = True
    # load the saved/trained weights
    if not file or not os.path.isfile(file):
        b = False
        # if file not provided, or it doesn't exist, use your voice
        print("Please talk")
        # put the file name here
        file = "test.wav"
        # record the file (start talking)
        record_to_file(file)
    return file, b



# Main function to detect scream
def scream(filename):  
    """  
    Detects whether the sound in the given WAV file is a scream, speech, or noise.  

    Parameters:  
    - filename: str, path to the WAV file.  
    - model1_path: str, path to the first model (noise vs speech).  
    - model2_path: str, path to the second model (scream vs speech).  

    Returns:  
    - None  
    """  
    # Load the wave file with the default sample rate  
    test, sr = librosa.load(filename, sr=None)  # sr=None preserves the original sample rate  

    # Extract MFCC features  
    mfccs = np.mean(librosa.feature.mfcc(y=test, sr=sr, n_mfcc=40).T, axis=0)  
    tester = np.array([mfccs])  # Create an array for the MFCCs  

    # Load the phase_1 model (noise vs speech)  
    load_model = joblib.load('models/s_phase1.sav')  

    # Predict if the result indicates noise or human sound  
    result = load_model.predict(tester)  

    # Load the phase_2 model  
    load_model2 = joblib.load('models/s_phase2.sav')  

    if result[0] == 2:  # Check if the sound is noise or human  
        print("Phase-1 clear")  
        ok = load_model2.predict(tester)  # Use the second phase model  
        if ok[0] == 1:  
            print("Scream detected")
            return 1  
        else:  
            print("Speech detected")
            return 0  
    else:  
        print("Noise detected")
        return -1  


# Main function to check gender
def check_gender(file):
    # load the saved model (after training)
    # model = pickle.load(open("result/mlp_classifier.model", "rb"))
    from utils import create_model
    # construct the model
    model = create_model()
    #load the saved/trained weights
    model.load_weights("models/gender.h5")
    features = extract_feature(file, mel=True).reshape(1, -1)
    # predict the gender!
    male_prob = model.predict(features)[0][0]
    female_prob = 1 - male_prob
    gender = "male" if male_prob > female_prob else "female"
    #show the result!
    print("Result:", gender)
    print(f"Probabilities:     Male: {male_prob*100:.2f}%    Female: {female_prob*100:.2f}%")

    if(male_prob > female_prob):
        return 'male',features
    else:
        return 'female',features

def check_distress(file):
    from utils import create_model2
    from scipy.io.wavfile import read
    import pandas as pd

    arr = []
    data, rs = read(file)
    file = open("input dimension for model.txt", "r")
    suitable_length_for_model = int(file.read())
    file.close()
    rs = rs.astype(float)
    rs = rs[0:suitable_length_for_model+1]
    a = pd.Series(rs)
    arr.append(a)
    df = pd.DataFrame(arr)
    X2 = df.iloc[0:, 1:]
    model = create_model2()
    model.load_weights("models/distress.h5")

    predictions = model.predict(X2)
    rounded = [round(x[0]) for x in predictions]
    print('Distress')
    print(predictions)
    #print("predicted value is" + str(rounded))
    if predictions > 0.5:
        return True
    else:
        return False


def vad(file_path, energy_threshold=0.1, min_duration=0.5, frame_size=1024):  
    """  
    Perform Voice Activity Detection (VAD) on a WAV file.  
    
    Parameters:  
    - file_path: str, path to the WAV file.  
    - energy_threshold: float, energy threshold for detecting speech.  
    - min_duration: float, minimum duration of speech to be considered valid (in seconds).  
    - frame_size: int, number of samples per frame for short-time energy calculation.  
    
    Returns:  
    - bool: True if speech is detected, False otherwise.  
    """  
    # Read the WAV file  
    sample_rate, data = wavfile.read(file_path)  

    # Normalize the audio signal  
    data = data / np.max(np.abs(data))  

    # Calculate short-time energy  
    num_frames = len(data) // frame_size  
    energies = np.array([  
        np.sum(data[i * frame_size:(i + 1) * frame_size] ** 2)  
        for i in range(num_frames)  
    ])  

    # Check if any frame's energy exceeds the threshold  
    speech_detected = np.any(energies > energy_threshold)  

    # If speech is detected, check the duration  
    if speech_detected:  
        # Calculate the duration of detected speech  
        speech_frames = energies > energy_threshold  
        speech_duration = np.sum(speech_frames) * (frame_size / sample_rate)  

        return speech_duration >= min_duration  

    return False
 

# Initialize variable states  
unique_men = set()  # To track unique men speakers  
unique_women = set()  # To track unique women speakers  
women_scream_detected = False  # Initialize as False  
men_shout_detected = False  # Initialize as False  
distress = 0

def threat():  
    global women_scream_detected, men_shout_detected, distress  # Declare global variables to modify them  
    # Updating the labels according to the threat  
    file,b = argpass_to_file()
    if not vad(file):
        return   
    scream_result = scream(file)  # Store the result of scream(file) to avoid redundant calls  
    y = check_distress(file)
    if y :
        distress = 1
    else :
        distress = 0
    print(y)
    if scream_result != -1:  
        gender, features = check_gender(file) 
        features_tuple = tuple(np.ndarray.flatten(np.round(features,decimals=5)))  # Convert features to a tuple
         
        if gender == 'male':  
            unique_men.add(features_tuple)  # Add the tuple to the set  
        else:  
            unique_women.add(features_tuple)  # Add the tuple to the set  
        
        if scream_result == 1:  # Check the scream result  
            if gender == 'male':  
                men_shout_detected = True  
            else:  
                women_scream_detected = True  
    
    return np.array([len(unique_men), len(unique_women), women_scream_detected, men_shout_detected, distress]),b  

def run_threat_repeatedly():  
    start_time = time.time()  # Record the start time  
    while True:  
        result,b = threat()  # Execute the threat function
        print("Current Detection Status:", result)  # Print the current detection status
        if b:
            break  
          
        
        # Check if 10 seconds have passed to reset the labels  
        # if time.time() - start_time >= 10:  
        #     reset_labels()  # Reset the labels  
        #     start_time = time.time()  # Reset the start time  
        reset_labels()
        time.sleep(4)  # Wait for 2 seconds before the next check  

def reset_labels():  
    global unique_men, unique_women, women_scream_detected, men_shout_detected  
    unique_men.clear()  # Clear the set of unique men  
    unique_women.clear()  # Clear the set of unique women  
    women_scream_detected = False  # Reset scream detection  
    men_shout_detected = False  # Reset shout detection 
    distress = False 
    print("Labels have been reset.")  

# Example of running the repeated threat function  
run_threat_repeatedly()  




