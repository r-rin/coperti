# Coperti Telegram bot

Lets warehouse staff record stock from Telegram:

- `/receive` — supplies arrived: pick the item, the warehouse and the quantity → a `RECEIPT` movement from the
  supplier into the warehouse.
- `/give` — stock handed out: pick the item, a warehouse that holds it, the employee and the quantity → an
  `ISSUE_TO_EMPLOYEE` movement into that employee's location.
- `/cancel` stops the current dialog; `/help` lists the commands.

The bot is a thin client of the Coperti REST API. Every rule (no negative stock, movement shapes, the ledger)
is enforced by the backend; the bot only shows its answers.

## Who may use it

The backend decides. Each employee can be linked to one Telegram account; the bot forwards the Telegram user id
(which Telegram itself authenticated) and the backend maps it to the employee, who is then recorded as
"performed by". Unlinked users are refused and told how to get linked. Two ways to link:

- **One-time code** (recommended): `POST /api/telegram-account/employee/{employeeId}/link-code` returns an
  8-character code valid for 15 minutes. The employee sends `/start CODE` to the bot. Issuing a new code cancels
  the previous unused one.
- **Directly by id**: `PUT /api/telegram-account` with `{"employeeId": ..., "telegramUserId": ...}`. An unlinked
  user who writes to the bot is shown their Telegram id for this.

`DELETE /api/telegram-account/employee/{employeeId}` unlinks.

The bot authenticates itself to the backend with a shared secret sent as `X-Bot-Token` on every request; the
backend only serves `/api/bot/**` when it matches. Without a configured secret the backend refuses all bot calls.
The rest of the API has no authentication yet.

## Setup

1. Create a bot with [@BotFather](https://t.me/BotFather) and copy its token.
2. Generate a shared secret, e.g. `python -c "import secrets; print(secrets.token_urlsafe(32))"`.
3. Give the secret to the backend as the `COPERTI_BOT_TOKEN` environment variable (property `coperti.bot.token`).
4. Copy `.env.example` to `.env` (git-ignored) and fill it in, or set the same variables in the environment:
   - `TELEGRAM_BOT_TOKEN` — the BotFather token.
   - `COPERTI_BOT_TOKEN` — the same shared secret as the backend.
   - `COPERTI_API_URL` — backend base URL, default `http://localhost:8080/api`.
5. Run the backend, then the bot (from the repo root):

```
$env:COPERTI_BOT_TOKEN = "<secret>"; .\gradlew.bat :backend:bootRun
.\gradlew.bat :telegram-bot:run
```

6. Link employees (see above).

The bot uses long polling, so it needs no public URL.

## Gradle tasks

| Task | What it does |
|---|---|
| `:telegram-bot:venv` | creates `.venv` with the interpreter from `-PpythonExe=...` (default `python`) |
| `:telegram-bot:pipInstall` | installs `requirements-dev.txt` into `.venv` |
| `:telegram-bot:pytest` | runs the unit tests (also part of `check`/`build`) |
| `:telegram-bot:run` | starts the bot |
| `:telegram-bot:clean` | removes `.venv` and caches |

Requires Python 3.11+.
