from fastapi import APIRouter, HTTPException, UploadFile, File
from pydantic import BaseModel
from typing import List
from services.gemini_service import get_gemini_service
from PIL import Image
import io

router = APIRouter()

class ImageAnalysisResult(BaseModel):
    scene_description: str
    detected_objects: List[str]
    potential_incident_type: str
    severity_assessment: str

@router.post("/analyze-image", response_model=ImageAnalysisResult)
async def analyze_image(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        
        service = get_gemini_service()
        result = await service.analyze_image(image)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
