"use client";

import { Alert, Table } from "antd";
import { useEffect, useState } from "react";

import { useI18n } from "@/i18n/I18nProvider";
import type { AdvanceCoverage, CoveredExpense } from "@/lib/types";
import ui from "@/components/ui/ui.module.css";
import styles from "../expenses.module.css";
import { loadCoverage } from "./coverage";

/** The expanded part of an advance row: loaded when first unfolded, not with the page. */
export function CoveredExpenses({ disbursementId }: { disbursementId: string }) {
  const { t, money } = useI18n();
  const [coverage, setCoverage] = useState<AdvanceCoverage | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let live = true;
    loadCoverage([disbursementId]).then((result) => {
      if (!live) return;
      if (result.ok) setCoverage(result.data[0]);
      else setError(result.error);
    });
    return () => {
      live = false;
    };
  }, [disbursementId]);

  if (error) return <Alert type="error" title={error} showIcon />;

  return (
    <div className={styles.coveredBox}>
      <p className={styles.coveredTitle}>{t.advances.coveredTitle}</p>
      <Table<CoveredExpense>
        rowKey="expenseId"
        size="small"
        loading={!coverage}
        dataSource={coverage?.expenses ?? []}
        pagination={false}
        locale={{ emptyText: t.advances.noneCovered }}
        columns={[
          { title: t.common.date, dataIndex: "date", width: 116, render: (v: string | null) => v ?? "—" },
          { title: t.common.description, dataIndex: "description", render: (v: string | null) => v ?? "—" },
          {
            title: t.advances.expenseAmount,
            dataIndex: "amount",
            align: "right",
            width: 130,
            render: (v: number) => <span className={ui.num}>{money(v)}</span>,
          },
          {
            title: t.advances.coveredHere,
            dataIndex: "coveredHere",
            align: "right",
            width: 170,
            render: (v: number) => <span className={ui.num}>{money(v)}</span>,
          },
          {
            title: t.advances.stillOwed,
            dataIndex: "shortfall",
            align: "right",
            width: 116,
            render: (v: number) => <span className={ui.num}>{money(v)}</span>,
          },
        ]}
      />
    </div>
  );
}
