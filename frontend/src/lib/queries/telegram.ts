import "server-only";

import { api } from "@/lib/api";
import type { TelegramAccount } from "@/lib/types";

export const getTelegramAccounts = () => api.get<TelegramAccount[]>("/telegram-account");

/**
 * The bot's @username, if configured, turns a link code into a one-tap t.me link that sends
 * `/start CODE` by itself. Without it the code is simply shown for typing.
 */
export function telegramBotUsername(): string | null {
  return process.env.TELEGRAM_BOT_USERNAME?.replace(/^@/, "").trim() || null;
}
