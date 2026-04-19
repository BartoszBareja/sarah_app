"""
Sarah App – backend server
- Przy starcie generuje QR kod z adresem IP:port
- Aplikacja Android skanuje QR i wie gdzie wysyłać zapytania
- Przekazuje wiadomości do Claude (Anthropic API) i zwraca odpowiedzi
"""

import os
import socket
import json
import uuid
from datetime import datetime
from typing import Optional

import anthropic
import qrcode
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

# ---------------------------------------------------------------------------
# Konfiguracja
# ---------------------------------------------------------------------------

ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
MODEL = "claude-sonnet-4-20250514"
PORT = 8000

SARAH_SYSTEM_PROMPT = """You are Sarah, a warm and empathetic AI companion designed to support
emotional wellbeing. You speak in a calm, friendly, and non-judgmental tone.
You listen actively, ask thoughtful follow-up questions, and help users
explore their feelings. You never diagnose or replace professional therapy.
Keep responses concise — 2 to 4 sentences — unless the user clearly wants more depth.
Always respond in the same language the user writes in."""

# ---------------------------------------------------------------------------
# Pobierz lokalne IP (nie localhost)
# ---------------------------------------------------------------------------

def get_local_ip() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

# ---------------------------------------------------------------------------
# Generuj QR kod przy starcie
# ---------------------------------------------------------------------------

def print_qr_to_terminal(data: str):
    qr = qrcode.QRCode(border=1)
    qr.add_data(data)
    qr.make(fit=True)
    qr.print_ascii(invert=True)

def generate_qr_image(data: str, filename: str = "sarah_qr.png"):
    img = qrcode.make(data)
    img.save(filename)
    print(f"  QR zapisany jako plik: {filename}")

# ---------------------------------------------------------------------------
# FastAPI app
# ---------------------------------------------------------------------------

app = FastAPI(title="Sarah API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)

@app.on_event("startup")
async def startup():
    ip = get_local_ip()
    server_url = f"http://{ip}:{PORT}"

    print("\n" + "=" * 50)
    print("  Sarah Server uruchomiony!")
    print(f"  Adres: {server_url}")
    print("=" * 50)
    print("\n  Zeskanuj kod QR aplikacja Sarah:\n")
    print_qr_to_terminal(server_url)
    generate_qr_image(server_url)
    print("\n" + "=" * 50 + "\n")

# ---------------------------------------------------------------------------
# Modele danych
# ---------------------------------------------------------------------------

class Message(BaseModel):
    role: str       # "user" | "assistant"
    content: str

class ChatRequest(BaseModel):
    session_id: Optional[str] = None
    messages: list[Message]
    user_name: Optional[str] = None

class ChatResponse(BaseModel):
    session_id: str
    reply: str
    timestamp: str

# ---------------------------------------------------------------------------
# Endpointy
# ---------------------------------------------------------------------------

@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL}


@app.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    session_id = req.session_id or str(uuid.uuid4())

    system = SARAH_SYSTEM_PROMPT
    if req.user_name:
        system += f"\nThe user's name is {req.user_name}."

    messages = [{"role": m.role, "content": m.content} for m in req.messages]

    try:
        response = client.messages.create(
            model=MODEL,
            max_tokens=512,
            system=system,
            messages=messages,
        )
        reply = response.content[0].text
    except anthropic.APIError as e:
        raise HTTPException(status_code=502, detail=f"Anthropic API error: {e}")

    ts = datetime.now().strftime('%H:%M:%S')
    print(f"[{ts}] User: {req.messages[-1].content[:60]}")
    print(f"[{ts}] Sarah: {reply[:60]}")

    return ChatResponse(
        session_id=session_id,
        reply=reply,
        timestamp=datetime.utcnow().isoformat(),
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=PORT, reload=False)
