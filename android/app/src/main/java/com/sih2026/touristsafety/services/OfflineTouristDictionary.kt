package com.sih2026.touristsafety.services

object OfflineTouristDictionary {

    private val englishToHindi = mapOf(
        "help" to "मदद (Madad)",
        "please help me" to "कृपया मेरी मदद करें (Kripya meri madad karein)",
        "i need help" to "मुझे मदद चाहिए (Mujhe madad chahiye)",
        "where is the police station" to "पुलिस स्टेशन कहाँ है? (Police station kahan hai?)",
        "where is the hospital" to "अस्पताल कहाँ है? (Hospital kahan hai?)",
        "call the police" to "पुलिस को बुलाओ (Police ko bulao)",
        "call an ambulance" to "एम्बुलेंस बुलाओ (Ambulance bulao)",
        "i am in danger" to "मैं खतरे में हूँ (Main khatre mein hoon)",
        "someone is following me" to "कोई मेरा पीछा कर रहा है (Koi mera peecha kar raha hai)",
        "my bag was stolen" to "मेरा बैग चोरी हो गया (Mera bag chori ho gaya)",
        "my phone is stolen" to "मेरा फोन चोरी हो गया (Mera phone chori ho gaya)",
        "i lost my passport" to "मेरा पासपोर्ट खो गया (Mera passport kho gaya)",
        "i am lost" to "मैं रास्ता भटक गया हूँ (Main rasta bhatak gaya hoon)",
        "take me to the hotel" to "मुझे होटल ले चलो (Mujhe hotel le chalo)",
        "where is the metro station" to "मेट्रो स्टेशन कहाँ है? (Metro station kahan hai?)",
        "how much is this" to "यह कितने का है? (Yeh kitne ka hai?)",
        "how much does this cost" to "इसकी कीमत क्या है? (Iski keemat kya hai?)",
        "water" to "पानी (Paani)",
        "food" to "खाना (Khaana)",
        "doctor" to "डॉक्टर (Doctor)",
        "yes" to "हाँ (Haan)",
        "no" to "नहीं (Nahi)",
        "thank you" to "धन्यवाद (Dhanyawad)",
        "please" to "कृपया (Kripya)",
        "hello" to "नमस्ते (Namaste)",
        "goodbye" to "अलविदा (Alvida)",
        "stop" to "रुको (Ruko)",
        "danger" to "खतरा (Khatra)",
        "emergency" to "आपातकाल (Aapatkaal)",
        "stolen" to "चोरी हो गया (Chori ho gaya)",
        "thief" to "चोर (Chor)",
        "fire" to "आग (Aag)",
        "accident" to "दुर्घटना (Durghatna)",
        "embassy" to "दूतावास (Dootavas)",
        "taxi" to "टैक्सी (Taxi)",
        "bus" to "बस (Bus)",
        "train" to "ट्रेन (Train)",
        "airport" to "हवाई अड्डा (Hawai Adda)",
        "hotel" to "होटल (Hotel)",
        "room" to "कमरा (Kamra)",
        "medicine" to "दवा (Dawa)",
        "money" to "पैसे (Paise)",
        "safe" to "सुरक्षित (Surakshit)",
        "unsafe" to "असुरक्षित (Asurakshit)",
        "night" to "रात (Raat)",
        "morning" to "सुबह (Subah)",
        "road" to "सड़क (Sadak)",
        "left" to "बाएं (Baayein)",
        "right" to "दाएं (Daayein)",
        "straight" to "सीधे (Seedhe)"
    )

    private val hindiToEnglish = englishToHindi.entries.associate { (k, v) ->
        val cleanHindi = v.split("(")[0].trim()
        cleanHindi to k.replaceFirstChar { it.uppercase() }
    }

    fun translateOffline(text: String, sourceLang: String, targetLang: String): String? {
        val cleanText = text.trim().lowercase()

        if (sourceLang == "en" && targetLang == "hi") {
            // Direct exact phrase match
            englishToHindi[cleanText]?.let { return it }

            // Partial matching for words
            var translated = text
            var hasMatch = false
            englishToHindi.forEach { (en, hi) ->
                if (cleanText.contains(en)) {
                    translated = translated.replace(Regex("(?i)\\b$en\\b"), hi)
                    hasMatch = true
                }
            }
            if (hasMatch) return translated
        }

        if (sourceLang == "hi" && targetLang == "en") {
            hindiToEnglish[cleanText]?.let { return it }
            var translated = text
            var hasMatch = false
            hindiToEnglish.forEach { (hi, en) ->
                if (cleanText.contains(hi)) {
                    translated = translated.replace(hi, en)
                    hasMatch = true
                }
            }
            if (hasMatch) return translated
        }

        return null
    }
}
