"use client";

import { useTransition } from "react";

import { EntitySelect } from "@/components/pickers/EntitySelect";
import { useI18n } from "@/i18n/I18nProvider";
import { setActor } from "./actorActions";
import { useActor } from "./ActorProvider";
import styles from "./shell.module.css";

export function ActorSelect() {
  const { t } = useI18n();
  const { actor, employees } = useActor();
  const [pending, startTransition] = useTransition();

  return (
    <label className={styles.language}>
      <span className={styles.languageLabel}>{t.shell.actingAs}</span>
      <EntitySelect
        allowClear
        placeholder={t.shell.nobody}
        value={actor?.id}
        onChange={(value) => startTransition(() => setActor(value ?? null))}
        loading={pending}
        disabled={pending}
        options={employees.map((employee) => ({ id: employee.id, label: employee.name }))}
        style={{ width: "100%" }}
      />
    </label>
  );
}
