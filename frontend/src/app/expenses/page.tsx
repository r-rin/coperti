import { getI18n } from "@/i18n/server";
import { getEmployeeBalances, getFacilityFloat } from "@/lib/queries";

import styles from "./expenses.module.css";
import { StatTile, WorkerBalances } from "./viz";

// the ledger moves constantly; never serve a cached balance sheet
export const dynamic = "force-dynamic";

export default async function OverviewPage() {
  const [facilityFloat, balances] = await Promise.all([getFacilityFloat(), getEmployeeBalances()]);
  const { t, money } = await getI18n();

  // the float and every per-worker figure are computed by the backend — this page only adds up rows
  const owed = balances.reduce((sum, row) => sum + row.owed, 0);
  const unspent = balances.reduce((sum, row) => sum + row.unspent, 0);
  const afterPayingBack = facilityFloat - owed;

  return (
    <>
      <header className={styles.pageHead}>
        <div>
          <h1 className={styles.title}>{t.overview.title}</h1>
          <p className={styles.subtitle}>{t.overview.subtitle}</p>
        </div>
      </header>

      <div className={styles.kpiRow}>
        <StatTile
          label={t.common.facilityFloat}
          value={money(facilityFloat)}
          hero
          note={t.overview.tillNote}
        />
        <StatTile
          label={t.overview.owedToWorkers}
          value={money(owed)}
          accent={owed > 0 ? "critical" : undefined}
          note={t.overview.owedNote}
        />
        <StatTile
          label={t.overview.unspentWithWorkers}
          value={money(unspent)}
          note={t.overview.unspentNote}
        />
        <StatTile
          label={t.overview.afterPayingBack}
          value={money(afterPayingBack)}
          accent={afterPayingBack < 0 ? "critical" : undefined}
          note={afterPayingBack < 0 ? t.overview.tillShort : t.overview.afterPayingBackNote}
        />
      </div>

      <WorkerBalances balances={balances} />
    </>
  );
}
