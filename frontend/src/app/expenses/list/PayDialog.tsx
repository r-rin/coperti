"use client";

import { App, Button, Input, InputNumber, Space, Table } from "antd";
import { useState, useTransition } from "react";

import { Modal } from "@/components/Modal";
import { useI18n } from "@/i18n/I18nProvider";
import { settle } from "../actions";
import styles from "../expenses.module.css";

export interface PayLine {
  id: string;
  employeeId: string;
  date: string | null;
  description: string | null;
  owed: number;
}

const cents = (value: number) => Math.round(value * 100);

const byDateOldestFirst = (a: PayLine, b: PayLine) =>
  a.date === b.date ? a.id.localeCompare(b.id) : a.date === null ? 1 : b.date === null ? -1 : a.date.localeCompare(b.date);

/**
 * Mirrors SettlementServiceImpl so the preview is what the backend will do: the worker's unspent
 * advance is spent first, then new cash, each covering the oldest receipt first. Worked in whole
 * cents so the preview never disagrees with the backend's BigDecimal by a rounding step.
 */
function preview(lines: PayLine[], held: number, cash: number) {
  let advance = cents(held);
  let fresh = cents(cash);
  let fromAdvances = 0;
  let fromNewCash = 0;

  const rows = [...lines].sort(byDateOldestFirst).map((line) => {
    let need = cents(line.owed);
    const fromAdvance = Math.min(advance, need);
    advance -= fromAdvance;
    need -= fromAdvance;
    const fromCash = Math.min(fresh, need);
    fresh -= fromCash;
    need -= fromCash;
    fromAdvances += fromAdvance;
    fromNewCash += fromCash;
    return { ...line, owedAfter: need / 100 };
  });

  return {
    rows,
    fromAdvances: fromAdvances / 100,
    fromNewCash: fromNewCash / 100,
    stillOwed: rows.reduce((sum, row) => sum + cents(row.owedAfter), 0) / 100,
    leftWithWorker: fresh / 100,
  };
}

export function PayDialog({
  open,
  workerName,
  lines,
  held,
  onClose,
  onPaid,
}: {
  open: boolean;
  workerName: string;
  lines: PayLine[];
  /** unspent cash the worker already holds on open advances */
  held: number;
  onClose: () => void;
  onPaid: () => void;
}) {
  const { t } = useI18n();

  return (
    <Modal
      title={t.expenses.payTitle(workerName)}
      open={open}
      onCancel={onClose}
      footer={null}
      width={640}
      destroyOnHidden
    >
      {/* mounted per open, so the suggested amount is recomputed from the current selection */}
      <PayBody lines={lines} held={held} onClose={onClose} onPaid={onPaid} />
    </Modal>
  );
}

function PayBody({
  lines,
  held,
  onClose,
  onPaid,
}: {
  lines: PayLine[];
  held: number;
  onClose: () => void;
  onPaid: () => void;
}) {
  const { t, money } = useI18n();
  const { message } = App.useApp();
  const [pending, startTransition] = useTransition();

  const owedTotal = lines.reduce((sum, line) => sum + cents(line.owed), 0) / 100;
  // suggest exactly what closes the debt once the held advance is spent
  const [cash, setCash] = useState<number | null>(Math.max(cents(owedTotal) - cents(held), 0) / 100);
  // sv-SE formats as YYYY-MM-DD in the browser's own timezone, unlike toISOString
  const [date, setDate] = useState(() => new Date().toLocaleDateString("sv-SE"));

  const plan = preview(lines, held, cash ?? 0);
  const paysSomething = plan.fromAdvances + plan.fromNewCash > 0;

  function submit() {
    startTransition(async () => {
      const outcome = await settle({
        employeeId: lines[0].employeeId,
        amount: cash ?? 0,
        date,
        expenseIds: lines.map((line) => line.id),
      });
      if (!outcome.ok || !outcome.result) {
        message.error(outcome.error ?? t.common.somethingWentWrong);
        return;
      }
      const { stillOwed, leftOnNewAdvance } = outcome.result;
      const parts = [stillOwed > 0 ? t.expenses.paidPartly(money(stillOwed)) : t.expenses.paidFull];
      if (leftOnNewAdvance > 0) parts.push(t.expenses.leftAsAdvance(money(leftOnNewAdvance)));
      message.success(parts.join(". "));
      onPaid();
    });
  }

  return (
    <div className={styles.pay}>
      <div className={styles.payFigures}>
        <div>
          <p className={styles.tileLabel}>{t.expenses.owedOnSelection}</p>
          <div className={styles.payValue}>{money(owedTotal)}</div>
          <p className={styles.tileNote}>{t.expenses.receipts(lines.length)}</p>
        </div>
        <div>
          <p className={styles.tileLabel}>{t.expenses.heldAdvance}</p>
          <div className={styles.payValue}>{money(held)}</div>
          <p className={styles.tileNote}>{t.expenses.heldAdvanceNote}</p>
        </div>
      </div>

      <div className={styles.payFields}>
        <label className={styles.filterField}>
          <span className={styles.filterLabel}>{t.expenses.newCash}</span>
          <InputNumber
            min={0}
            step={0.01}
            precision={2}
            value={cash}
            onChange={setCash}
            style={{ width: 180 }}
            autoFocus
          />
        </label>
        <label className={styles.filterField}>
          <span className={styles.filterLabel}>{t.common.date}</span>
          <Input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 160 }} />
        </label>
      </div>

      <Table
        rowKey="id"
        size="small"
        pagination={false}
        dataSource={plan.rows}
        columns={[
          { title: t.common.date, dataIndex: "date", width: 110, render: (v: string | null) => v ?? "—" },
          { title: t.common.description, dataIndex: "description", render: (v: string | null) => v || "—" },
          {
            title: t.expenses.owedNow,
            dataIndex: "owed",
            align: "right",
            width: 110,
            render: (v: number) => <span className={styles.num}>{money(v)}</span>,
          },
          {
            title: t.expenses.owedAfter,
            dataIndex: "owedAfter",
            align: "right",
            width: 110,
            render: (v: number) => (
              <span className={`${styles.num} ${v > 0 ? styles.owedText : ""}`}>{money(v)}</span>
            ),
          },
        ]}
      />

      <dl className={styles.paySummary}>
        <dt>{t.expenses.fromAdvances}</dt>
        <dd>{money(plan.fromAdvances)}</dd>
        <dt>{t.expenses.fromNewCash}</dt>
        <dd>{money(plan.fromNewCash)}</dd>
        <dt>{t.expenses.stillOwed}</dt>
        <dd className={plan.stillOwed > 0 ? styles.owedText : undefined}>{money(plan.stillOwed)}</dd>
        {plan.leftWithWorker > 0 ? (
          <>
            <dt>{t.expenses.staysWithWorker}</dt>
            <dd>{money(plan.leftWithWorker)}</dd>
          </>
        ) : null}
      </dl>

      {!paysSomething ? <p className={styles.payHint}>{t.expenses.nothingToPayWith}</p> : null}

      <Space style={{ justifyContent: "flex-end", width: "100%" }}>
        <Button onClick={onClose}>{t.common.cancel}</Button>
        <Button type="primary" loading={pending} disabled={!paysSomething || !date} onClick={submit}>
          {t.expenses.payOk}
        </Button>
      </Space>
    </div>
  );
}
