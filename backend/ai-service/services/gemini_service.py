import google.generativeai as genai
import json
from typing import Optional

SYSTEM_PROMPT = """You are an expert Indian tourism assistant and safety advisor.
You help tourists with queries about monuments, local customs, safety tips, and general navigation in India.
Your responses should be helpful, culturally aware, and prioritize safety.
Return JSON ONLY, following this format:
{
  "text": "Your detailed text response",
  "image_urls": ["optional_url1", "optional_url2"],
  "action_buttons": [
    {"label": "button text", "action": "action_url_or_intent"}
  ]
}
"""

class GeminiService:
    def __init__(self):
        self.model = genai.GenerativeModel('gemini-2.0-flash')
        self.vision_model = genai.GenerativeModel('gemini-2.0-flash')
        
    async def chat(self, message: str, location: Optional[str], language: str):
        prompt = f"System: {SYSTEM_PROMPT}\nUser Location: {location}\nLanguage: {language}\nUser Message: {message}"
        response = self.model.generate_content(prompt)
        try:
            return json.loads(response.text.strip('```json\n').strip('```'))
        except json.JSONDecodeError:
            return {"text": response.text, "image_urls": [], "action_buttons": []}

    async def structure_fir(self, description: str):
        prompt = f"""Analyze this incident description and output a structured JSON for an e-FIR.
Extract BNS (Bharatiya Nyaya Sanhita) sections instead of IPC.
Format:
{{
    "complainant_details": "extracted or unknown",
    "incident_type": "type of incident",
    "bns_sections": ["section 1", "section 2"],
    "date_time_of_occurrence": "extracted or unknown",
    "place_of_occurrence": "extracted or unknown",
    "description_of_accused": "extracted or none",
    "property_lost": "extracted or none",
    "witnesses": "extracted or none",
    "narrative": "formalized narrative of {description}"
}}
Description: {description}"""
        response = self.model.generate_content(prompt)
        try:
            return json.loads(response.text.strip('```json\n').strip('```'))
        except json.JSONDecodeError:
            raise ValueError("Failed to structure FIR data")

    async def analyze_image(self, image):
        prompt = """Analyze this image for an incident report. Return ONLY JSON format:
{
    "scene_description": "detailed description",
    "detected_objects": ["obj1", "obj2"],
    "potential_incident_type": "type",
    "severity_assessment": "low/medium/high/critical"
}"""
        response = self.vision_model.generate_content([prompt, image])
        try:
            return json.loads(response.text.strip('```json\n').strip('```'))
        except json.JSONDecodeError:
            raise ValueError("Failed to parse vision response")

    async def translate(self, text: str, source_lang: str, target_lang: str):
        prompt = f"Translate the following text from {source_lang} to {target_lang}. Return ONLY the translated text.\nText: {text}"
        response = self.model.generate_content(prompt)
        return response.text.strip()

_service_instance = None

def get_gemini_service():
    global _service_instance
    if _service_instance is None:
        _service_instance = GeminiService()
    return _service_instance
