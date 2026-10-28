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

const qtyFormats = new Map<Locale, Intl.NumberFormat>();

/** Stock quantities: the backend keeps three decimals, but whole numbers show without any. */
export function formatQty(value: number | null | undefined, locale: Locale): string {
  if (value === null || value === undefined) return "—";
  let format = qtyFormats.get(locale);
  if (!format) {
    format = new Intl.NumberFormat(INTL_LOCALE[locale], { maximumFractionDigits: 3 });
    qtyFormats.set(locale, format);
  }
  return format.format(value);
}

/**
 * Timestamps render in one fixed zone, the facility's, rather than the viewer's: the same page is
 * rendered on the server and hydrated in the browser, and the two must print the same text.
 */
export const FACILITY_TIME_ZONE = "Europe/Kyiv";

const dateTimeFormats = new Map<Locale, Intl.DateTimeFormat>();

export function formatDateTime(value: string | null | undefined, locale: Locale): string {
  if (!value) return "—";
  let format = dateTimeFormats.get(locale);
  if (!format) {
    format = new Intl.DateTimeFormat(INTL_LOCALE[locale], {
      dateStyle: "medium",
      timeStyle: "short",
      timeZone: FACILITY_TIME_ZONE,
    });
    dateTimeFormats.set(locale, format);
  }
  return format.format(new Date(value));
}

/**
 * A calendar day picked in the UI means a day at the facility. Turns `YYYY-MM-DD` into the exact
 * instant that day starts (or ends) there, for filters the backend applies to timestamps.
 */
export function facilityDayBound(date: string, edge: "start" | "end"): string {
  // the zone's offset that day, read at noon so a midnight DST switch can't shift it
  const offset =
    new Intl.DateTimeFormat("en-US", { timeZone: FACILITY_TIME_ZONE, timeZoneName: "longOffset" })
      .formatToParts(new Date(`${date}T12:00:00Z`))
      .find((part) => part.type === "timeZoneName")
      ?.value.replace("GMT", "") || "+00:00";
  const time = edge === "start" ? "00:00:00.000" : "23:59:59.999";
  return new Date(`${date}T${time}${offset}`).toISOString();
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  return value;
}

export function shortId(value: string | null | undefined): string {
  if (!value) return "—";
  return value.slice(0, 8);
}
