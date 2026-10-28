import pytest

from coperti_bot.config import ConfigError, load_settings, read_env_file

BOTH = {"TELEGRAM_BOT_TOKEN": "t", "COPERTI_BOT_TOKEN": "b"}


def test_load_settings_requires_telegram_token(tmp_path):
    with pytest.raises(ConfigError, match="TELEGRAM_BOT_TOKEN"):
        load_settings({"COPERTI_BOT_TOKEN": "b"}, env_file=tmp_path / "none")


def test_load_settings_requires_backend_token(tmp_path):
    with pytest.raises(ConfigError, match="COPERTI_BOT_TOKEN"):
        load_settings({"TELEGRAM_BOT_TOKEN": "t"}, env_file=tmp_path / "none")


def test_environment_overrides_env_file(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text(
        "# comment\n"
        "TELEGRAM_BOT_TOKEN='from-file'\n"
        "COPERTI_BOT_TOKEN=secret-from-file\n"
        "COPERTI_API_URL=http://file/api/\n",
        encoding="utf-8")

    settings = load_settings({"TELEGRAM_BOT_TOKEN": "from-env"}, env_file=env_file)

    assert settings.token == "from-env"
    assert settings.bot_token == "secret-from-file"
    assert settings.api_url == "http://file/api"


def test_empty_environment_value_does_not_hide_env_file(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text("TELEGRAM_BOT_TOKEN=from-file\nCOPERTI_BOT_TOKEN=b\n", encoding="utf-8")

    assert load_settings({"TELEGRAM_BOT_TOKEN": " "}, env_file=env_file).token == "from-file"


def test_default_api_url(tmp_path):
    assert load_settings(BOTH, env_file=tmp_path / "none").api_url == "http://localhost:8080/api"


def test_read_env_file_skips_noise(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text("\n# x=1\nNO_EQUALS\nA = \"quoted value\"\n", encoding="utf-8")
    assert read_env_file(env_file) == {"A": "quoted value"}
