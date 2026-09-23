// Shared by server and client components — must stay free of any backend import.

import { INTL_LOCALE, type Locale } from "@/i18n/config";

const moneyFormats = new Map<Locale, Intl.NumberFormat>();

function moneyFormat(locale: Locale) {
  let format = moneyFormats.get(locale);
  if (!format) {
    format = new Intl.NumberFormat(INTL_LOCALE[locale], {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
    moneyFormats.set(locale, format);
  }
  return format;
}

export function formatMoney(value: number | null | undefined, locale: Locale): string {
  if (value === null || value === undefined) return "—";
  return moneyFormat(locale).format(value);
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  return value;
}

export function shortId(value: string | null | undefined): string {
  if (!value) return "—";
  return value.slice(0, 8);
}
