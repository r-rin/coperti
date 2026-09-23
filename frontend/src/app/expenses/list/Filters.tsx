"use client";

import { Button, Input, Select } from "antd";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";

import { useI18n } from "@/i18n/I18nProvider";
import type { Employee } from "@/lib/types";
import styles from "../expenses.module.css";

/**
 * Filters live in the URL, so applying one is a server round-trip that re-renders the
 * page with fresh rows. No client-side fetching, and every filtered view is linkable.
 */
export function Filters({ employees }: { employees: Employee[] }) {
  const router = useRouter();
  const params = useSearchParams();
  const { t } = useI18n();

  const [fromDate, setFromDate] = useState(params.get("fromDate") ?? "");
  const [toDate, setToDate] = useState(params.get("toDate") ?? "");
  const [search, setSearch] = useState(params.get("search") ?? "");
  const [employee, setEmployee] = useState(params.get("issuedByEmployee") ?? "");

  function apply() {
    const next = new URLSearchParams();
    if (fromDate) next.set("fromDate", fromDate);
    if (toDate) next.set("toDate", toDate);
    if (search) next.set("search", search);
    if (employee) next.set("issuedByEmployee", employee);
    router.push(`/expenses/list?${next.toString()}`);
  }

  function reset() {
    setFromDate("");
    setToDate("");
    setSearch("");
    setEmployee("");
    router.push("/expenses/list");
  }

  return (
    <div className={styles.filters}>
      <label className={styles.filterField}>
        <span className={styles.filterLabel}>{t.common.from}</span>
        <Input
          type="date"
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
          style={{ width: 160 }}
        />
      </label>
      <label className={styles.filterField}>
        <span className={styles.filterLabel}>{t.expenses.filterTo}</span>
        <Input
          type="date"
          value={toDate}
          onChange={(e) => setToDate(e.target.value)}
          style={{ width: 160 }}
        />
      </label>
      <label className={styles.filterField}>
        <span className={styles.filterLabel}>{t.common.worker}</span>
        <Select
          allowClear
          placeholder={t.common.anyone}
          value={employee || undefined}
          onChange={(value) => setEmployee(value ?? "")}
          options={employees.map((e) => ({ value: e.id, label: e.name }))}
          style={{ width: 180 }}
        />
      </label>
      <label className={styles.filterField}>
        <span className={styles.filterLabel}>{t.common.description}</span>
        <Input
          placeholder={t.expenses.containsPlaceholder}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onPressEnter={apply}
          style={{ width: 200 }}
        />
      </label>
      <Button type="primary" onClick={apply}>
        {t.expenses.apply}
      </Button>
      <Button onClick={reset}>{t.expenses.reset}</Button>
    </div>
  );
}
