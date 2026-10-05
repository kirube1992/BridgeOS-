from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


class UserRef(BaseModel):
    id: int
    name: str
    email: str | None = None


class TranslateRequest(BaseModel):
    text: str = Field(min_length=3)
    projectId: int | None = None
    projectName: str | None = None


class TranslateResponse(BaseModel):
    originalText: str
    whatToBuild: str
    whyItMatters: str
    acceptanceCriteria: list[str]
    edgeCases: list[str]
    technicalNotes: str


class TranslateTextRequest(BaseModel):
    text: str = Field(min_length=1)
    targetLocale: str = "en"
    sourceLocale: str | None = None


class TranslateTextResponse(BaseModel):
    originalText: str
    translatedText: str
    sourceLocale: str
    targetLocale: str


class ExtractMeetingRequest(BaseModel):
    notes: str = Field(min_length=3)
    projectId: int | None = None
    users: list[UserRef] = Field(default_factory=list)


class SuggestedAssignee(BaseModel):
    id: int | None = None
    name: str | None = None
    confidence: float = 0.0


class ActionItem(BaseModel):
    description: str
    suggestedAssignee: SuggestedAssignee | None = None
    suggestedDueDate: str | None = None
    priority: str = "MEDIUM"


class ExtractMeetingResponse(BaseModel):
    actionItems: list[ActionItem]


class AskContextItem(BaseModel):
    type: str
    id: int
    summary: str


class AskRequest(BaseModel):
    question: str = Field(min_length=3)
    projectId: int | None = None
    context: list[AskContextItem] = Field(default_factory=list)


class AskSource(BaseModel):
    type: str
    id: int
    summary: str


class AskResponse(BaseModel):
    question: str
    answer: str
    sources: list[AskSource] = Field(default_factory=list)


class HealthResponse(BaseModel):
    status: str
    provider: str
    model: str | None = None
