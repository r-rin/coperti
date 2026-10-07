// Pure figures for the advances report — shared by nothing server-only, so the PDF can run in the browser.

import type { AdvanceCoverage, DisbursementStatus } from "@/lib/types";

export interface ReportTotals {
  advances: number;
  /** cash handed out; CANCELLED advances never left the till, matching the backend's balances */
  given: number;
  covered: number;
  /** left on OPEN advances — still cash in the worker's hands */
  unspent: number;
  /** left on CLOSED advances — the remainder written off when it was closed by hand */
  writtenOff: number;
  byStatus: Record<DisbursementStatus, number>;
  expenses: number;
  partlyCovered: number;
  /** still owed on the covered expenses, after every advance (selected or not) that paid them */
  owed: number;
}

export interface WorkerSection {
  employeeId: string;
  name: string;
  totals: ReportTotals;
  advances: AdvanceCoverage[];
}

export interface AdvanceReport {
  from: string | null;
  to: string | null;
  totals: ReportTotals;
  workers: WorkerSection[];
}

function summarize(advances: AdvanceCoverage[]): ReportTotals {
  const totals: ReportTotals = {
    advances: advances.length,
    given: 0,
    covered: 0,
    unspent: 0,
    writtenOff: 0,
    byStatus: { OPEN: 0, CLOSED: 0, CANCELLED: 0 },
    expenses: 0,
    partlyCovered: 0,
    owed: 0,
  };
  // one expense may be paid by several selected advances; count it, and its shortfall, once
  const shortfalls = new Map<string, number>();

  for (const { disbursement, remaining, expenses } of advances) {
    totals.byStatus[disbursement.status] += 1;
    if (disbursement.status !== "CANCELLED") totals.given += disbursement.amount;
    if (disbursement.status === "OPEN") totals.unspent += remaining;
    if (disbursement.status === "CLOSED") totals.writtenOff += remaining;
    for (const expense of expenses) {
      totals.covered += expense.coveredHere;
      shortfalls.set(expense.expenseId, expense.shortfall);
    }
  }

  totals.expenses = shortfalls.size;
  for (const shortfall of shortfalls.values()) {
    if (shortfall > 0) totals.partlyCovered += 1;
    totals.owed += shortfall;
  }
  return totals;
}

const byDate = (a: AdvanceCoverage, b: AdvanceCoverage) =>
  (a.disbursement.date ?? "").localeCompare(b.disbursement.date ?? "");

export function buildReport(advances: AdvanceCoverage[], names: Map<string, string>): AdvanceReport {
  const sorted = [...advances].sort(byDate);
  const dates = sorted.map((a) => a.disbursement.date).filter((d): d is string => Boolean(d));

  const grouped = new Map<string, AdvanceCoverage[]>();
  for (const advance of sorted) {
    const id = advance.disbursement.employeeId;
    grouped.set(id, [...(grouped.get(id) ?? []), advance]);
  }

  const workers = [...grouped.entries()]
    .map(([employeeId, own]) => ({
      employeeId,
      name: names.get(employeeId) ?? employeeId.slice(0, 8),
      totals: summarize(own),
      advances: own,
    }))
    .sort((a, b) => a.name.localeCompare(b.name));

  return {
    from: dates[0] ?? null,
    to: dates[dates.length - 1] ?? null,
    totals: summarize(sorted),
    workers,
  };
}

/** Share of handed-out cash already spent against receipts, as a whole percent. */
export function usedPercent(totals: ReportTotals): number {
  return totals.given > 0 ? Math.round((totals.covered / totals.given) * 100) : 0;
}
