"""Bot settings, read from the environment with an optional .env file underneath."""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from typing import Mapping

# telegram-bot/.env, independent of the working directory the bot is started from
DEFAULT_ENV_FILE = Path(__file__).resolve().parents[2] / ".env"
DEFAULT_API_URL = "http://localhost:8080/api"


class ConfigError(Exception):
    """Raised at startup when the settings are missing or malformed."""


@dataclass(frozen=True)
class Settings:
    token: str
    api_url: str
    # shared secret the backend expects on /api/bot/**; must match the backend's coperti.bot.token
    bot_token: str


def read_env_file(path: Path) -> dict[str, str]:
    """Parses KEY=VALUE lines; blank lines and # comments are skipped, surrounding quotes stripped."""
    if not path.is_file():
        return {}
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = value[1:-1]
        values[key.strip()] = value
    return values


def load_settings(environ: Mapping[str, str] | None = None, env_file: Path = DEFAULT_ENV_FILE) -> Settings:
    """Real environment variables win over the .env file, unless they are set but empty."""
    env = os.environ if environ is None else environ
    values = {**read_env_file(env_file), **{key: value for key, value in env.items() if value.strip()}}

    token = values.get("TELEGRAM_BOT_TOKEN", "").strip()
    if not token:
        raise ConfigError("TELEGRAM_BOT_TOKEN is not set")

    bot_token = values.get("COPERTI_BOT_TOKEN", "").strip()
    if not bot_token:
        raise ConfigError("COPERTI_BOT_TOKEN is not set; it must match the backend's coperti.bot.token")

    api_url = values.get("COPERTI_API_URL", "").strip() or DEFAULT_API_URL
    return Settings(token=token, api_url=api_url.rstrip("/"), bot_token=bot_token)
