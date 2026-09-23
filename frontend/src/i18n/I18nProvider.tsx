"use client";

import { createContext, useContext, useMemo } from "react";

import { formatMoney } from "@/lib/format";
import type { Locale } from "./config";
import { dictionaries } from "./dictionaries";

const LocaleContext = createContext<Locale | null>(null);

/**
 * Only the locale crosses the server/client boundary: dictionaries hold functions for
 * interpolated strings, which cannot be serialised, so the client bundles its own copy.
 */
export function I18nProvider({ locale, children }: { locale: Locale; children: React.ReactNode }) {
  return <LocaleContext.Provider value={locale}>{children}</LocaleContext.Provider>;
}

export function useI18n() {
  const locale = useContext(LocaleContext);
  if (!locale) throw new Error("useI18n must be used inside I18nProvider");

  return useMemo(
    () => ({
      locale,
      t: dictionaries[locale],
      money: (value: number | null | undefined) => formatMoney(value, locale),
    }),
    [locale],
  );
}
