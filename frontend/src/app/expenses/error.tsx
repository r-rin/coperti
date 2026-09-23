"use client";

import { Button } from "antd";

import { useI18n } from "@/i18n/I18nProvider";
import styles from "./expenses.module.css";

export default function ExpensesError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const { t } = useI18n();

  return (
    <div className={styles.errorBox}>
      <h2 className={styles.errorTitle}>{t.error.title}</h2>
      <p className={styles.errorBody}>{error.message}</p>
      <p className={styles.errorBody} style={{ marginTop: 12 }}>
        {t.error.backendHintBefore}{" "}
        <code>.\gradlew.bat :backend:bootRun</code>.
      </p>
      <Button style={{ marginTop: 16 }} onClick={reset}>
        {t.error.retry}
      </Button>
    </div>
  );
}
