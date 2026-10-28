"use client";

import { Button, Checkbox, Input } from "antd";
import { useState } from "react";

import { EntitySelect } from "@/components/pickers/EntitySelect";
import { FilterBar, FilterField } from "@/components/ui/FilterBar";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import type { Employee } from "@/lib/types";

/**
 * Filters live in the URL, so applying one is a server round-trip that re-renders the
 * page with fresh rows. No client-side fetching, and every filtered view is linkable.
 * They apply on the button (not per keystroke), so the inputs keep local state until then.
 */
export function Filters({ employees }: { employees: Employee[] }) {
  const { params, setFilters, reset: resetUrl } = useUrlParams();
  const { t } = useI18n();

  const [fromDate, setFromDate] = useState(params.get("fromDate") ?? "");
  const [toDate, setToDate] = useState(params.get("toDate") ?? "");
  const [search, setSearch] = useState(params.get("search") ?? "");
  const [employee, setEmployee] = useState(params.get("issuedByEmployee") ?? "");
  const [uncoveredOnly, setUncoveredOnly] = useState(params.get("uncoveredOnly") === "true");

  function apply() {
    setFilters({
      fromDate,
      toDate,
      search,
      issuedByEmployee: employee,
      uncoveredOnly: uncoveredOnly ? "true" : undefined,
    });
  }

  function reset() {
    setFromDate("");
    setToDate("");
    setSearch("");
    setEmployee("");
    setUncoveredOnly(false);
    resetUrl();
  }

  return (
    <FilterBar>
      <FilterField label={t.common.from}>
        <Input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} style={{ width: 160 }} />
      </FilterField>
      <FilterField label={t.expenses.filterTo}>
        <Input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} style={{ width: 160 }} />
      </FilterField>
      <FilterField label={t.common.worker}>
        <EntitySelect
          allowClear
          placeholder={t.common.anyone}
          value={employee || undefined}
          onChange={(value) => setEmployee(value ?? "")}
          options={employees.map((e) => ({ id: e.id, label: e.name }))}
          style={{ width: 180 }}
        />
      </FilterField>
      <FilterField label={t.common.description}>
        <Input
          placeholder={t.expenses.containsPlaceholder}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onPressEnter={apply}
          style={{ width: 200 }}
        />
      </FilterField>
      <Checkbox
        checked={uncoveredOnly}
        onChange={(e) => setUncoveredOnly(e.target.checked)}
        style={{ lineHeight: "32px" }}
      >
        {t.expenses.uncoveredOnly}
      </Checkbox>
      <Button type="primary" onClick={apply}>
        {t.expenses.apply}
      </Button>
      <Button onClick={reset}>{t.expenses.reset}</Button>
    </FilterBar>
  );
}
