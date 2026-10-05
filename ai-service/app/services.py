from __future__ import annotations

import json
import os
import re
from datetime import date, timedelta
from difflib import SequenceMatcher
from typing import Any

from .models import (
    ActionItem,
    AskContextItem,
    AskResponse,
    AskSource,
    ExtractMeetingResponse,
    SuggestedAssignee,
    TranslateResponse,
    UserRef,
)

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY", "")
AI_PROVIDER = os.getenv("AI_PROVIDER", "auto").lower()
AI_MODEL = os.getenv("AI_MODEL", "")

_provider_cache: str | None = None


def _detect_provider() -> tuple[str, str]:
    global _provider_cache
    if AI_PROVIDER not in ("auto", "", "mock"):
        provider = AI_PROVIDER
    elif OPENAI_API_KEY:
        provider = "openai"
    elif ANTHROPIC_API_KEY:
        provider = "anthropic"
    elif GOOGLE_API_KEY:
        provider = "google"
    else:
        provider = "mock"

    if provider == "openai" and not AI_MODEL:
        model = "gpt-4o-mini"
    elif provider == "anthropic" and not AI_MODEL:
        model = "claude-3-5-sonnet-20241022"
    elif provider == "google" and not AI_MODEL:
        model = "gemini-1.5-flash"
    else:
        model = AI_MODEL or "rules-engine"

    _provider_cache = f"{provider}/{model}"
    return provider, model


def provider_name() -> str:
    if _provider_cache is None:
        _detect_provider()
    return _provider_cache or "mock/rules-engine"


def _call_openai(system_prompt: str, user_prompt: str) -> dict[str, Any] | None:
    if not OPENAI_API_KEY:
        return None
    try:
        from openai import OpenAI

        client = OpenAI(api_key=OPENAI_API_KEY)
        _, model = _detect_provider()
        response = client.chat.completions.create(
            model=model or "gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            response_format={"type": "json_object"},
            temperature=0.2,
        )
        content = response.choices[0].message.content or "{}"
        return json.loads(content)
    except Exception as e:
        print(f"[ai] OpenAI call failed: {e}")
        return None


def _call_anthropic(system_prompt: str, user_prompt: str) -> dict[str, Any] | None:
    if not ANTHROPIC_API_KEY:
        return None
    try:
        import anthropic

        client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)
        _, model = _detect_provider()
        message = client.messages.create(
            model=model or "claude-3-5-sonnet-20241022",
            max_tokens=4096,
            system=system_prompt,
            messages=[
                {"role": "user", "content": user_prompt + "\n\nReturn ONLY valid JSON. No markdown, no prose."},
            ],
            temperature=0.2,
        )
        raw = message.content[0].text if message.content else "{}"
        raw = re.sub(r"^```json\s*|\s*```$", "", raw.strip(), flags=re.S)
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            match = re.search(r"\{[\s\S]*\}", raw)
            if match:
                return json.loads(match.group(0))
            return None
    except Exception as e:
        print(f"[ai] Anthropic call failed: {e}")
        return None


def _call_google(system_prompt: str, user_prompt: str) -> dict[str, Any] | None:
    if not GOOGLE_API_KEY:
        return None
    try:
        import google.generativeai as genai

        genai.configure(api_key=GOOGLE_API_KEY)
        _, model = _detect_provider()
        model_obj = genai.GenerativeModel(
            model_name=model or "gemini-1.5-flash",
            generation_config={
                "temperature": 0.2,
                "response_mime_type": "application/json",
            },
            system_instruction=system_prompt,
        )
        response = model_obj.generate_content(user_prompt)
        raw = response.text or "{}"
        raw = re.sub(r"^```json\s*|\s*```$", "", raw.strip(), flags=re.S)
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            match = re.search(r"\{[\s\S]*\}", raw)
            if match:
                return json.loads(match.group(0))
            return None
    except Exception as e:
        print(f"[ai] Google call failed: {e}")
        return None


def _call_llm(system_prompt: str, user_prompt: str) -> dict[str, Any] | None:
    provider, _ = _detect_provider()
    if provider == "openai":
        return _call_openai(system_prompt, user_prompt)
    if provider == "anthropic":
        return _call_anthropic(system_prompt, user_prompt)
    if provider == "google":
        return _call_google(system_prompt, user_prompt)
    return None


def translate_requirement(text: str, project_name: str | None = None) -> TranslateResponse:
    system = (
        "You are a senior product engineer. Convert vague requirements into structured JSON with keys: "
        "whatToBuild, whyItMatters, acceptanceCriteria (array), edgeCases (array), technicalNotes."
    )
    user = f"Project: {project_name or 'General'}\nRequirement:\n{text}"
    payload = _call_llm(system, user)
    if payload:
        return TranslateResponse(
            originalText=text,
            whatToBuild=payload.get("whatToBuild", ""),
            whyItMatters=payload.get("whyItMatters", ""),
            acceptanceCriteria=list(payload.get("acceptanceCriteria", [])),
            edgeCases=list(payload.get("edgeCases", [])),
            technicalNotes=payload.get("technicalNotes", ""),
        )

    topic = text.strip().rstrip(".")
    project_hint = f" for {project_name}" if project_name else ""
    return TranslateResponse(
        originalText=text,
        whatToBuild=(
            f"Implement a focused solution{project_hint} that addresses: {topic}. "
            "Deliver a backend API endpoint, frontend UI control, and validation for the described workflow."
        ),
        whyItMatters=(
            "This requirement reduces manual coordination overhead and gives stakeholders a reliable, "
            "repeatable way to complete the requested workflow with clear accountability."
        ),
        acceptanceCriteria=[
            "User can trigger the requested capability from the relevant BridgeOS screen",
            "Success and failure states are visible with actionable error messages",
            "Data is persisted and visible to authorized team members",
            "Authorization rules are enforced for the acting user",
        ],
        edgeCases=[
            "Empty or malformed input is rejected with validation feedback",
            "Concurrent updates do not corrupt shared project data",
            "Large payloads are handled without breaking the UI",
        ],
        technicalNotes=(
            "Reuse existing Spring Boot REST patterns and Vue form components. "
            "Add integration tests for the happy path and one failure path."
        ),
    )


def translate_text(text: str, target_locale: str = "en", source_locale: str | None = None) -> dict[str, Any]:
    normalized_text = (text or "").strip()
    if not normalized_text:
        return {"originalText": "", "translatedText": "", "sourceLocale": source_locale or "en", "targetLocale": target_locale}

    detected_source = (source_locale or "zh" if any(ord(ch) > 127 for ch in normalized_text) else "en").lower()
    if target_locale.lower() == detected_source:
        translated = normalized_text
    else:
        system = (
            "Translate the provided text into the requested locale. "
            "Return valid JSON with a single key: translatedText."
        )
        user = (
            f"Source locale: {detected_source}\nTarget locale: {target_locale.lower()}\nText:\n{normalized_text}"
        )
        payload = _call_llm(system, user)
        translated = payload.get("translatedText", normalized_text) if payload else normalized_text

    return {
        "originalText": normalized_text,
        "translatedText": translated,
        "sourceLocale": detected_source,
        "targetLocale": target_locale.lower(),
    }


def _fuzzy_match_user(name_hint: str, users: list[UserRef]) -> SuggestedAssignee | None:
    if not name_hint or not users:
        return None
    best: tuple[float, UserRef] | None = None
    hint = name_hint.lower()
    for user in users:
        candidates = [user.name.lower(), (user.email or "").lower()]
        for candidate in candidates:
            if not candidate:
                continue
            score = SequenceMatcher(None, hint, candidate).ratio()
            if hint in candidate or candidate in hint:
                score = max(score, 0.82)
            if best is None or score > best[0]:
                best = (score, user)
    if best and best[0] >= 0.55:
        return SuggestedAssignee(id=best[1].id, name=best[1].name, confidence=round(best[0], 2))
    return None


def _infer_priority(line: str) -> str:
    lowered = line.lower()
    if any(word in lowered for word in ["urgent", "asap", "immediately", "critical", "blocker"]):
        return "URGENT"
    if any(word in lowered for word in ["soon", "priority", "important", "high"]):
        return "HIGH"
    if any(word in lowered for word in ["later", "nice to have", "low"]):
        return "LOW"
    return "MEDIUM"


def _infer_due_date(line: str) -> str | None:
    lowered = line.lower()
    today = date.today()
    if "tomorrow" in lowered:
        return (today + timedelta(days=1)).isoformat()
    if "next week" in lowered:
        return (today + timedelta(days=7)).isoformat()
    if any(word in lowered for word in ["soon", "asap", "urgent"]):
        return (today + timedelta(days=3)).isoformat()
    if "friday" in lowered:
        days_ahead = (4 - today.weekday()) % 7 or 7
        return (today + timedelta(days=days_ahead)).isoformat()
    return (today + timedelta(days=5)).isoformat()


def _extract_assignee_hint(line: str) -> str | None:
    match = re.search(r"(?:assign(?:ed)? to|owner:|@)\s*([A-Za-z][A-Za-z\s.-]{1,40})", line, re.I)
    if match:
        return match.group(1).strip()
    return None


def extract_meeting_notes(notes: str, users: list[UserRef]) -> ExtractMeetingResponse:
    system = (
        "Extract action items from meeting notes. Return JSON: "
        '{"actionItems":[{"description":"","assigneeHint":"","priority":"","dueDateHint":""}]}'
    )
    payload = _call_llm(system, notes)
    items: list[ActionItem] = []

    if payload and isinstance(payload.get("actionItems"), list):
        for raw in payload["actionItems"]:
            hint = raw.get("assigneeHint") or raw.get("assignee")
            items.append(
                ActionItem(
                    description=raw.get("description", "").strip(),
                    suggestedAssignee=_fuzzy_match_user(str(hint or ""), users),
                    suggestedDueDate=raw.get("dueDate") or raw.get("suggestedDueDate"),
                    priority=(raw.get("priority") or "MEDIUM").upper(),
                )
            )
        return ExtractMeetingResponse(actionItems=[item for item in items if item.description])

    lines = [line.strip(" -\t•*") for line in notes.splitlines() if line.strip()]
    bullet_lines = [line for line in lines if len(line) > 4]
    if not bullet_lines:
        bullet_lines = [notes.strip()]

    for line in bullet_lines[:12]:
        assignee_hint = _extract_assignee_hint(line)
        clean = re.sub(r"(?i)(action:|todo:|task:)\s*", "", line).strip()
        items.append(
            ActionItem(
                description=clean,
                suggestedAssignee=_fuzzy_match_user(assignee_hint or "", users),
                suggestedDueDate=_infer_due_date(line),
                priority=_infer_priority(line),
            )
        )

    return ExtractMeetingResponse(actionItems=items)


def ask_question(question: str, context: list[AskContextItem]) -> AskResponse:
    system = (
        "Answer using only provided BridgeOS context. Return JSON with keys answer and citedSourceIds "
        "(array of ids from context). Be concise and practical."
    )
    context_blob = "\n".join(f"[{item.type}#{item.id}] {item.summary}" for item in context[:20])
    payload = _call_llm(system, f"Context:\n{context_blob}\n\nQuestion: {question}")
    sources: list[AskSource] = []

    if payload:
        cited_ids = {int(value) for value in payload.get("citedSourceIds", []) if str(value).isdigit()}
        for item in context:
            if item.id in cited_ids:
                sources.append(AskSource(type=item.type, id=item.id, summary=item.summary))
        return AskResponse(
            question=question,
            answer=payload.get("answer", "I could not find enough context to answer."),
            sources=sources,
        )

    lowered = question.lower()
    matched = [item for item in context if any(token in item.summary.lower() for token in lowered.split() if len(token) > 3)]
    if not matched and context:
        matched = context[:3]

    for item in matched[:3]:
        sources.append(AskSource(type=item.type, id=item.id, summary=item.summary))

    if "block" in lowered:
        blocked = [item for item in context if "block" in item.summary.lower() or item.type == "work_item"]
        answer = (
            "Based on current project records, the most likely blockers are open work items or pending decisions. "
            "Review the cited items and confirm owners on the tasks still in TODO or IN_PROGRESS."
        )
        sources = [AskSource(type=item.type, id=item.id, summary=item.summary) for item in blocked[:3]] or sources
    elif "decision" in lowered or "summarize" in lowered:
        decisions = [item for item in context if item.type == "decision"]
        if decisions:
            answer = "Recent decisions indicate the following themes: " + "; ".join(item.summary for item in decisions[:3])
            sources = [AskSource(type=item.type, id=item.id, summary=item.summary) for item in decisions[:3]]
        else:
            answer = "I did not find decision log entries in the available context for this question."
    else:
        answer = (
            "I reviewed the available BridgeOS project context. "
            "Use the cited records for the latest task status, ownership, and decision history."
        )

    return AskResponse(question=question, answer=answer, sources=sources)
