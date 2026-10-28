/** An employee's linked Telegram account; the bot acts as that employee for this Telegram user. */
export interface TelegramAccount {
  id: string;
  employeeId: string;
  telegramUserId: number;
  linkedAt: string;
}

/** A one-time code the employee sends to the bot as `/start CODE`. */
export interface LinkCode {
  code: string;
  employeeId: string;
  expiresAt: string;
}
