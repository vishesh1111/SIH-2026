import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import google.generativeai as genai

from routers import chatbot, efir, incidents, translator

load_dotenv()

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize Gemini client
    gemini_key = os.getenv("GEMINI_API_KEY")
    if gemini_key:
        genai.configure(api_key=gemini_key)
    yield
    # Cleanup if needed

app = FastAPI(title="Smart Tourist Safety AI Service", lifespan=lifespan)

# CORS setup
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chatbot.router, prefix="/api/chat", tags=["Chatbot"])
app.include_router(efir.router, prefix="/api/efir", tags=["e-FIR"])
app.include_router(incidents.router, prefix="/api/incidents", tags=["Incidents"])
app.include_router(translator.router, prefix="/api/translate", tags=["Translator"])

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "AI Service"}
