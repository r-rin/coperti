import Link from "next/link";

import { getI18n } from "@/i18n/server";
import type { EmployeeBalance } from "@/lib/types";
import styles from "./expenses.module.css";

/**
 * Server-rendered presentation only — no client JS. Forms follow the data's job:
 * headline numbers are stat tiles (never a one-bar chart), part-to-whole is a single
 * stacked bar, and per-worker balances are a table with a covered/owed bar per row.
 */

export function StatTile({
  label,
  value,
  note,
  hero = false,
  accent,
}: {
  label: string;
  value: string;
  note?: string;
  hero?: boolean;
  accent?: "good" | "critical";
}) {
  const accentClass =
    accent === "good" ? styles.tileAccentGood : accent === "critical" ? styles.tileAccentCritical : "";
  return (
    <div className={styles.card}>
      <p className={styles.tileLabel}>{label}</p>
      <div className={`${styles.tileValue} ${hero ? styles.tileHero : ""} ${accentClass}`}>{value}</div>
      {note ? <p className={styles.tileNote}>{note}</p> : null}
    </div>
  );
}

export interface Segment {
  label: string;
  value: number;
  color: string;
}

/**
 * Part-to-whole. Identity never rests on colour alone: every segment is named and
 * valued in the legend, which is also the relief for the light-surface contrast warning
 * on the third categorical slot.
 */
export async function StackedBar({
  title,
  note,
  segments,
}: {
  title: string;
  note?: string;
  segments: Segment[];
}) {
  const { t, money } = await getI18n();
  const total = segments.reduce((sum, segment) => sum + segment.value, 0);

  return (
    <section className={styles.card}>
      <h3 className={styles.cardTitle}>{title}</h3>
      {note ? <p className={styles.cardNote}>{note}</p> : null}
      {total > 0 ? (
        <>
          <div
            className={styles.stack}
            role="img"
            aria-label={segments.map((s) => `${s.label}: ${money(s.value)}`).join(", ")}
          >
            {segments
              .filter((segment) => segment.value > 0)
              .map((segment) => (
                <div
                  key={segment.label}
                  className={styles.stackSeg}
                  style={{
                    width: `${(segment.value / total) * 100}%`,
                    background: segment.color,
                  }}
                />
              ))}
          </div>
          <div className={styles.legend}>
            {segments.map((segment) => (
              <span key={segment.label} className={styles.legendItem}>
                <span className={styles.legendSwatch} style={{ background: segment.color }} />
                {segment.label}
                <span className={styles.legendValue}>{money(segment.value)}</span>
              </span>
            ))}
          </div>
        </>
      ) : (
        <p className={styles.empty}>{t.common.nothingRecorded}</p>
      )}
    </section>
  );
}

/**
 * Who owes whom, per worker. The table is the primary read — every figure is a column, so the
 * bar never has to carry a number on its own. Each bar is one worker's spending split into
 * covered and owed, drawn on a shared scale so bar length also compares workers.
 */
export async function WorkerBalances({ balances }: { balances: EmployeeBalance[] }) {
  const { t, money } = await getI18n();

  const rows = [...balances].sort((a, b) => b.owed - a.owed || b.spent - a.spent);
  const maxSpent = Math.max(0, ...rows.map((row) => row.spent));
  const sum = (pick: (row: EmployeeBalance) => number) => rows.reduce((acc, row) => acc + pick(row), 0);

  const covered = { label: t.expenses.covered, color: "var(--series-1)" };
  const owed = { label: t.expenses.owed, color: "var(--series-2)" };

  return (
    <section className={styles.card}>
      <h3 className={styles.cardTitle}>{t.overview.workersTitle}</h3>
      <p className={styles.cardNote}>{t.overview.workersNote}</p>

      {rows.length ? (
        <>
          <div className={styles.legend} style={{ marginTop: 0, marginBottom: 12 }}>
            {[covered, owed].map((series) => (
              <span key={series.label} className={styles.legendItem}>
                <span className={styles.legendSwatch} style={{ background: series.color }} />
                {series.label}
              </span>
            ))}
          </div>

          <div className={styles.tableScroll}>
            <table className={styles.balanceTable}>
              <thead>
                <tr>
                  <th>{t.common.worker}</th>
                  <th className={styles.numCell}>{t.overview.spent}</th>
                  <th className={styles.numCell}>{t.expenses.covered}</th>
                  <th className={styles.numCell}>{t.expenses.owed}</th>
                  <th className={styles.numCell}>{t.overview.unspentColumn}</th>
                  <th className={styles.coverCell}>{t.overview.coverage}</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.employeeId}>
                    <td>
                      <Link
                        href={`/expenses/list?issuedByEmployee=${row.employeeId}`}
                        className={styles.workerLink}
                        title={t.overview.showExpenses(row.employeeName)}
                      >
                        {row.employeeName}
                      </Link>
                    </td>
                    <td className={styles.numCell}>{money(row.spent)}</td>
                    <td className={styles.numCell}>{money(row.covered)}</td>
                    <td className={`${styles.numCell} ${row.owed > 0 ? styles.owedCell : ""}`}>
                      {money(row.owed)}
                    </td>
                    <td className={styles.numCell}>{money(row.unspent)}</td>
                    <td className={styles.coverCell}>
                      {row.spent > 0 ? (
                        <div
                          className={styles.coverTrack}
                          style={{ width: `${(row.spent / maxSpent) * 100}%` }}
                          role="img"
                          aria-label={`${covered.label}: ${money(row.covered)}, ${owed.label}: ${money(row.owed)}`}
                        >
                          {[
                            { ...covered, value: row.covered },
                            { ...owed, value: row.owed },
                          ]
                            .filter((segment) => segment.value > 0)
                            .map((segment) => (
                              <span
                                key={segment.label}
                                className={styles.coverSeg}
                                style={{ flexGrow: segment.value, background: segment.color }}
                                title={`${segment.label}: ${money(segment.value)}`}
                              />
                            ))}
                        </div>
                      ) : (
                        <span className={styles.mono}>{t.overview.nothingSpent}</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <td>{t.overview.total}</td>
                  <td className={styles.numCell}>{money(sum((row) => row.spent))}</td>
                  <td className={styles.numCell}>{money(sum((row) => row.covered))}</td>
                  <td className={styles.numCell}>{money(sum((row) => row.owed))}</td>
                  <td className={styles.numCell}>{money(sum((row) => row.unspent))}</td>
                  <td />
                </tr>
              </tfoot>
            </table>
          </div>
        </>
      ) : (
        <p className={styles.empty}>{t.overview.noEmployees}</p>
      )}
    </section>
  );
}
