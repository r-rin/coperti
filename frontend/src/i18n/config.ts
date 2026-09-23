// Shared by server and client — no server-only or client-only imports here.

export const LOCALES = ["en", "uk"] as const;
export type Locale = (typeof LOCALES)[number];

export const DEFAULT_LOCALE: Locale = "en";
export const LOCALE_COOKIE = "locale";

/** Each language is named in itself, so a reader can find theirs whatever is active. */
export const LOCALE_LABELS: Record<Locale, string> = {
  en: "English",
  uk: "Українська",
};

/** BCP 47 tags for Intl formatting. */
export const INTL_LOCALE: Record<Locale, string> = {
  en: "en-US",
  uk: "uk-UA",
};

export function isLocale(value: unknown): value is Locale {
  return typeof value === "string" && (LOCALES as readonly string[]).includes(value);
}
