import "server-only";

import { cookies, headers } from "next/headers";
import { cache } from "react";

import { formatMoney } from "@/lib/format";
import { DEFAULT_LOCALE, isLocale, LOCALE_COOKIE, type Locale } from "./config";
import { dictionaries } from "./dictionaries";

/**
 * An explicit choice (the cookie) wins; otherwise the browser's preferred languages are
 * tried in order. Cached so every component in one render agrees on the answer.
 */
export const getLocale = cache(async (): Promise<Locale> => {
  const chosen = (await cookies()).get(LOCALE_COOKIE)?.value;
  if (isLocale(chosen)) return chosen;

  const accepted = (await headers()).get("accept-language") ?? "";
  for (const entry of accepted.split(",")) {
    const language = entry.split(";")[0].trim().toLowerCase().split("-")[0];
    if (isLocale(language)) return language;
  }
  return DEFAULT_LOCALE;
});

export async function getI18n() {
  const locale = await getLocale();
  return {
    locale,
    t: dictionaries[locale],
    money: (value: number | null | undefined) => formatMoney(value, locale),
  };
}
