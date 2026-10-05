# BridgeOS AI Sidecar

Python FastAPI service that powers requirement translation, meeting extraction, and the project assistant.

## Run locally

```bash
cd ai-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8090
```

## Optional LLM provider

Set environment variables to use OpenAI instead of the built-in rules engine:

```bash
set OPENAI_API_KEY=your-key
set AI_MODEL=gpt-4o-mini
```

Without an API key, the service returns deterministic structured outputs so BridgeOS can be tested end-to-end.

## Endpoints

- `GET /health`
- `POST /translate`
- `POST /extract-meeting`
- `POST /ask`
