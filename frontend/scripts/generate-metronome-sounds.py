"""Genera los sonidos del metrónomo (WAV mono 16 bits, 44.1 kHz) en frontend/assets/sounds."""
import math
import os
import random
import struct
import wave

RATE = 44100
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets", "sounds")
os.makedirs(OUT, exist_ok=True)


def write(name, samples):
    with wave.open(os.path.join(OUT, name), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s)) * 32767)) for s in samples))


def click(freq, length=0.05, decay=60.0, gain=0.9):
    """Golpe tipo 'woodblock': seno con un armónico y caída exponencial rápida."""
    n = int(RATE * length)
    out = []
    for i in range(n):
        t = i / RATE
        env = math.exp(-decay * t)
        attack = min(1.0, i / 40)  # 1 ms de ataque para evitar el "pop"
        s = math.sin(2 * math.pi * freq * t) + 0.35 * math.sin(2 * math.pi * freq * 2.7 * t)
        out.append(gain * attack * env * s / 1.35)
    return out


def hihat(length=0.06, decay=70.0, gain=0.7):
    """Hi-hat cerrado: ruido blanco con filtro paso alto de primer orden y caída rápida."""
    random.seed(7)
    n = int(RATE * length)
    out, prev_x, prev_y = [], 0.0, 0.0
    alpha = 0.92  # paso alto: deja pasar sobre todo los agudos
    for i in range(n):
        t = i / RATE
        x = random.uniform(-1, 1)
        y = alpha * (prev_y + x - prev_x)
        prev_x, prev_y = x, y
        attack = min(1.0, i / 20)
        out.append(gain * attack * math.exp(-decay * t) * y)
    return out


write("click.wav", click(1600))
write("click_accent.wav", click(2400, gain=1.0))
write("hihat.wav", hihat())
write("hihat_accent.wav", hihat(length=0.08, decay=45.0, gain=0.95))
print(sorted(os.listdir(OUT)))
