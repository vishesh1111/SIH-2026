from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from services.gemini_service import get_gemini_service

router = APIRouter()

class TranslationRequest(BaseModel):
    text: str
    source_lang: str
    target_lang: str

class TranslationResponse(BaseModel):
    translated_text: str

@router.post("/", response_model=TranslationResponse)
async def translate(request: TranslationRequest):
    try:
        service = get_gemini_service()
        translated = await service.translate(request.text, request.source_lang, request.target_lang)
        return TranslationResponse(translated_text=translated)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
