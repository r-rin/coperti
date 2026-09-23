"use client";

import { App, Alert, Button, InputNumber, Table } from "antd";
import { useRouter } from "next/navigation";
import { useMemo, useState, useTransition } from "react";

import { useI18n } from "@/i18n/I18nProvider";
import type { Expense } from "@/lib/types";
import { allocate } from "../../actions";
import styles from "../../expenses.module.css";

export interface Candidate extends Expense {
  shortfall: number;
  workerName: string;
}

/**
 * Disbursement-centric: the advance is fixed and the user ticks off which receipts it
 * paid for. The running total is checked here so an over-allocation is caught before a
 * round-trip — the backend rejects the whole selection anyway, which is the real guarantee.
 */
export function AllocateForm({
  disbursementId,
  remaining,
  candidates,
}: {
  disbursementId: string;
  remaining: number;
  candidates: Candidate[];
}) {
  const router = useRouter();
  const { message } = App.useApp();
  const { t, money } = useI18n();
  const [pending, startTransition] = useTransition();
  const [amounts, setAmounts] = useState<Record<string, number | null>>({});

  const selections = useMemo(
    () =>
      Object.entries(amounts)
        .filter(([, amount]) => typeof amount === "number" && amount > 0)
        .map(([expenseId, amount]) => ({ expenseId, amount: amount as number })),
    [amounts],
  );

  const total = selections.reduce((sum, selection) => sum + selection.amount, 0);
  const over = total > remaining + 1e-9;

  function fillRemainder(expenseId: string, shortfall: number) {
    const otherTotal = selections
      .filter((selection) => selection.expenseId !== expenseId)
      .reduce((sum, selection) => sum + selection.amount, 0);
    const room = Math.max(remaining - otherTotal, 0);
    setAmounts((current) => ({ ...current, [expenseId]: Math.min(shortfall, room) }));
  }

  function submit() {
    startTransition(async () => {
      const result = await allocate(disbursementId, selections);
      if (result.ok) {
        const closed = result.result?.disbursementStatus === "CLOSED";
        message.success(
          closed
            ? t.allocate.allocatedClosed
            : t.allocate.allocatedRemaining(money(result.result?.remainingBalance ?? 0)),
        );
        setAmounts({});
        router.refresh();
      } else {
        message.error(result.error ?? t.common.somethingWentWrong);
      }
    });
  }

  if (!candidates.length) {
    return (
      <Alert
        type="info"
        showIcon
        title={t.allocate.nothingTitle}
        description={t.allocate.nothingNote}
      />
    );
  }

  return (
    <>
      <Table<Candidate>
        rowKey="id"
        dataSource={candidates}
        size="middle"
        pagination={false}
        columns={[
          { title: t.common.date, dataIndex: "date", width: 116, render: (v: string | null) => v ?? "—" },
          { title: t.common.description, dataIndex: "description", render: (v: string | null) => v || "—" },
          {
            title: t.allocate.expense,
            dataIndex: "amount",
            align: "right",
            width: 110,
            render: (v: number) => <span className={styles.num}>{money(v)}</span>,
          },
          {
            title: t.allocate.stillNeeds,
            dataIndex: "shortfall",
            align: "right",
            width: 116,
            render: (v: number) => <span className={styles.num}>{money(v)}</span>,
          },
          {
            title: t.allocate.cover,
            key: "cover",
            width: 230,
            render: (_, row) => (
              <div style={{ display: "flex", gap: 8 }}>
                <InputNumber
                  min={0}
                  max={row.shortfall}
                  step={0.01}
                  placeholder="0.00"
                  value={amounts[row.id] ?? null}
                  onChange={(value) => setAmounts((current) => ({ ...current, [row.id]: value }))}
                  style={{ width: 130 }}
                />
                <Button size="small" onClick={() => fillRemainder(row.id, row.shortfall)}>
                  {t.allocate.max}
                </Button>
              </div>
            ),
          },
        ]}
      />

      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 16,
          marginTop: 16,
          flexWrap: "wrap",
        }}
      >
        <div>
          <div className={styles.tileLabel}>{t.allocate.selected}</div>
          <div className={`${styles.tileValue} ${over ? styles.tileAccentCritical : ""}`}>
            {money(total)}
            <span className={styles.tileNote} style={{ marginLeft: 8 }}>
              {t.allocate.ofAvailable(money(remaining))}
            </span>
          </div>
        </div>
        <Button
          type="primary"
          size="large"
          loading={pending}
          disabled={!selections.length || over}
          onClick={submit}
        >
          {t.allocate.submit(selections.length)}
        </Button>
      </div>

      {over ? (
        <Alert
          style={{ marginTop: 12 }}
          type="error"
          showIcon
          title={t.allocate.overTitle(money(total - remaining))}
          description={t.allocate.overNote}
        />
      ) : null}
    </>
  );
}
