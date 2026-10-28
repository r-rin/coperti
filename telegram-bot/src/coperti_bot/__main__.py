"""Entry point: python -m coperti_bot"""

import logging
import sys

from .app import build_application
from .config import ConfigError, load_settings


def main() -> None:
    logging.basicConfig(format="%(asctime)s %(levelname)s %(name)s: %(message)s", level=logging.INFO)
    # httpx logs every request URL at INFO, and Telegram URLs contain the bot token
    logging.getLogger("httpx").setLevel(logging.WARNING)
    try:
        settings = load_settings()
    except ConfigError as exc:
        print(f"Configuration error: {exc}", file=sys.stderr)
        sys.exit(2)
    logging.getLogger(__name__).info("Using Coperti API at %s", settings.api_url)
    build_application(settings).run_polling()


if __name__ == "__main__":
    main()
