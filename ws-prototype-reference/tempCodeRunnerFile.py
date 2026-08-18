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
