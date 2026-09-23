"use client";

import { App, ConfigProvider, theme } from "antd";
import enUS from "antd/locale/en_US";
import ukUA from "antd/locale/uk_UA";
import { useSyncExternalStore } from "react";

import type { Locale } from "@/i18n/config";
import { useI18n } from "@/i18n/I18nProvider";

// Ant Design's built-in texts: pagination, Popconfirm/Modal buttons, empty states
const ANTD_LOCALE: Record<Locale, typeof enUS> = { en: enUS, uk: ukUA };

const QUERY = "(prefers-color-scheme: dark)";

function subscribe(onChange: () => void) {
  const query = window.matchMedia(QUERY);
  query.addEventListener("change", onChange);
  return () => query.removeEventListener("change", onChange);
}

const getSnapshot = () => window.matchMedia(QUERY).matches;

// the server has no media preference to read; light keeps the first client render identical
const getServerSnapshot = () => false;

/**
 * Keeps Ant Design's algorithm in step with the OS preference that the CSS modules
 * already follow, so tables and charts never disagree about the theme. matchMedia is an
 * external store, so it is subscribed to rather than mirrored into state by an effect.
 */
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const dark = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const { locale } = useI18n();

  return (
    <ConfigProvider
      locale={ANTD_LOCALE[locale]}
      // no glow spreading out of buttons on click (Popconfirm OK, primary actions, …)
      wave={{ disabled: true }}
      theme={{
        algorithm: dark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: dark ? "#3987e5" : "#2a78d6",
          borderRadius: 6,
          fontFamily: "var(--font-geist-sans), system-ui, sans-serif",
        },
      }}
    >
      <App>{children}</App>
    </ConfigProvider>
  );
}
