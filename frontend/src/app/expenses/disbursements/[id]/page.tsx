import Link from "next/link";

import { getI18n } from "@/i18n/server";
import { shortId } from "@/lib/format";
import {
  getDisbursement,
  getEmployees,
  getExpenses,
  getFundingByDisbursement,
  getRemainingBalance,
  getShortfall,
} from "@/lib/queries";

import styles from "../../expenses.module.css";
import { StatTile } from "../../viz";
import { AllocateForm, type Candidate } from "./AllocateForm";

export const dynamic = "force-dynamic";

export default async function AllocatePage(props: PageProps<"/expenses/disbursements/[id]">) {
  const { id } = await props.params;

  const [disbursement, remaining, fundings, employees] = await Promise.all([
    getDisbursement(id),
    getRemainingBalance(id),
    getFundingByDisbursement(id),
    getEmployees(),
  ]);
  const { t, money } = await getI18n();

  const names = new Map(employees.map((e) => [e.id, e.name]));
  const workerName = names.get(disbursement.employeeId) ?? shortId(disbursement.employeeId);

  // only this worker's expenses can be covered by their own advance
  const expensePage = await getExpenses({ issuedByEmployee: disbursement.employeeId, size: 100 });
  const withShortfall = await Promise.all(
    expensePage.data.map(async (expense) => ({
      ...expense,
      workerName,
      shortfall: await getShortfall(expense.id),
    })),
  );
  const candidates: Candidate[] = withShortfall.filter((expense) => expense.shortfall > 0);

  const drawn = fundings.reduce((sum, funding) => sum + funding.amountCovered, 0);
  const allocatable = disbursement.status === "OPEN";

  return (
    <>
      <header className={styles.pageHead}>
        <div>
          <Link href="/expenses/disbursements" className={styles.mono}>
            {t.allocate.back}
          </Link>
          <h1 className={styles.title} style={{ marginTop: 6 }}>
            {t.allocate.title(workerName)}
          </h1>
          <p className={styles.subtitle}>{t.allocate.subtitle}</p>
        </div>
      </header>

      <div className={styles.kpiRow}>
        <StatTile
          label={t.allocate.advance}
          value={money(disbursement.amount)}
          note={disbursement.date ?? "—"}
        />
        <StatTile
          label={t.allocate.drawnDown}
          value={money(drawn)}
          note={t.allocate.fundingRows(fundings.length)}
        />
        <StatTile
          label={t.common.remaining}
          value={money(remaining)}
          hero
          accent={remaining > 0 ? undefined : "good"}
          note={remaining > 0 ? t.allocate.stillToAccount : t.common.fullyAccountedFor}
        />
        <StatTile
          label={t.common.state}
          value={t.status.disbursement[disbursement.status]}
          note={t.allocate.settlementState}
        />
      </div>

      <section className={styles.card}>
        <h3 className={styles.cardTitle}>{t.allocate.cardTitle}</h3>
        <p className={styles.cardNote}>
          {allocatable
            ? t.allocate.cardNote
            : t.allocate.notAllocatable(t.status.disbursement[disbursement.status])}
        </p>
        {allocatable ? (
          <AllocateForm disbursementId={id} remaining={remaining} candidates={candidates} />
        ) : null}
      </section>
    </>
  );
}
