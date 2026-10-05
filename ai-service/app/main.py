from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

load_dotenv()

from app.models import (
    AskRequest,
    AskResponse,
    ExtractMeetingRequest,
    ExtractMeetingResponse,
    HealthResponse,
    TranslateRequest,
    TranslateResponse,
    TranslateTextRequest,
    TranslateTextResponse,
)
from app.services import (
    ask_question,
    extract_meeting_notes,
    provider_name,
    translate_requirement,
    translate_text,
)

app = FastAPI(title="BridgeOS AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    name = provider_name()
    return HealthResponse(status="ok", provider=name.split("/")[0], model=name)


@app.post("/translate", response_model=TranslateResponse)
def translate(request: TranslateRequest) -> TranslateResponse:
    return translate_requirement(request.text, request.projectName)


@app.post("/translate-text", response_model=TranslateTextResponse)
def translate_text_route(request: TranslateTextRequest) -> TranslateTextResponse:
    return translate_text(request.text, request.targetLocale, request.sourceLocale)


@app.post("/extract-meeting", response_model=ExtractMeetingResponse)
def extract_meeting(request: ExtractMeetingRequest) -> ExtractMeetingResponse:
    return extract_meeting_notes(request.notes, request.users)


@app.post("/ask", response_model=AskResponse)
def ask(request: AskRequest) -> AskResponse:
    return ask_question(request.question, request.context)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8090, reload=True)
