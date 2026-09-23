"use client";

import { Select } from "antd";
import { useTransition } from "react";

import { setLocale } from "@/i18n/actions";
import { LOCALE_LABELS, LOCALES, type Locale } from "@/i18n/config";
import { useI18n } from "@/i18n/I18nProvider";
import styles from "./shell.module.css";

export function LanguageSelect() {
  const { locale, t } = useI18n();
  const [pending, startTransition] = useTransition();

  // a Server Action that sets a cookie re-renders the current page and its layouts,
  // so the server-rendered text switches language in the same round trip
  function change(next: Locale) {
    startTransition(() => setLocale(next));
  }

  return (
    <label className={styles.language}>
      <span className={styles.languageLabel}>{t.shell.language}</span>
      <Select<Locale>
        value={locale}
        onChange={change}
        loading={pending}
        disabled={pending}
        options={LOCALES.map((value) => ({ value, label: LOCALE_LABELS[value] }))}
        style={{ width: "100%" }}
      />
    </label>
  );
}
