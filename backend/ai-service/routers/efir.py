from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from fastapi.responses import FileResponse
from services.gemini_service import get_gemini_service
from services.fir_generator import generate_fir_pdf
import tempfile
import os

router = APIRouter()

class IncidentDescription(BaseModel):
    description: str

class StructuredFIR(BaseModel):
    complainant_details: str
    incident_type: str
    bns_sections: List[str]
    date_time_of_occurrence: str
    place_of_occurrence: str
    description_of_accused: str
    property_lost: str
    witnesses: str
    narrative: str

@router.post("/generate", response_model=StructuredFIR)
async def generate_efir(request: IncidentDescription):
    try:
        service = get_gemini_service()
        structured_data = await service.structure_fir(request.description)
        return structured_data
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/pdf")
async def generate_pdf(fir_data: StructuredFIR):
    try:
        # Create temporary file
        fd, temp_path = tempfile.mkstemp(suffix=".pdf")
        os.close(fd)
        
        # Generate PDF
        generate_fir_pdf(fir_data.model_dump(), temp_path)
        
        return FileResponse(
            path=temp_path,
            filename="e-FIR.pdf",
            media_type="application/pdf",
            background=None
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
