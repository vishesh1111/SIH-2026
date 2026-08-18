from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from services.gemini_service import get_gemini_service

router = APIRouter()

class ChatMessage(BaseModel):
    message: str
    location: Optional[str] = None
    language: Optional[str] = "en"

class ActionButton(BaseModel):
    label: str
    action: str

class ChatResponse(BaseModel):
    text: str
    image_urls: Optional[List[str]] = []
    action_buttons: Optional[List[ActionButton]] = []

@router.post("/", response_model=ChatResponse)
async def chat(request: ChatMessage):
    try:
        service = get_gemini_service()
        response = await service.chat(request.message, request.location, request.language)
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
