import { getI18n } from "@/i18n/server";
import { getBudgetSum, getBudgets, getEmployeeBalances, getFacilityFloat } from "@/lib/queries";

import styles from "../expenses.module.css";
import { StackedBar, StatTile } from "../viz";
import { BudgetTable } from "./BudgetTable";

export const dynamic = "force-dynamic";

function one(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export default async function BudgetsPage(props: PageProps<"/expenses/budgets">) {
  const params = await props.searchParams;

  const query = { page: Number(one(params.page) ?? 0), size: 20 };

  const [page, fundedIn, facilityFloat, balances] = await Promise.all([
    getBudgets(query),
    getBudgetSum(),
    getFacilityFloat(),
    getEmployeeBalances(),
  ]);
  const { t, money } = await getI18n();

  // handed out = open + closed advances; of that, only the undrawn part of open ones is still cash
  const handedOut = balances.reduce((sum, row) => sum + row.advanced, 0);
  const withWorkers = balances.reduce((sum, row) => sum + row.unspent, 0);

  return (
    <>
      <header className={styles.pageHead}>
        <div>
          <h1 className={styles.title}>{t.budgets.title}</h1>
          <p className={styles.subtitle}>{t.budgets.subtitle}</p>
        </div>
      </header>

      <div className={styles.kpiRow}>
        <StatTile
          label={t.common.facilityFloat}
          value={money(facilityFloat)}
          hero
          note={t.budgets.floatNote}
        />
        <StatTile label={t.common.fundedIn} value={money(fundedIn)} note={t.common.allBudgetRecords} />
        <StatTile
          label={t.budgets.handedOut}
          value={money(handedOut)}
          note={t.budgets.handedOutNote}
        />
      </div>

      <div className={styles.chartGrid}>
        <StackedBar
          title={t.budgets.whereMoneySits}
          note={t.budgets.whereMoneySitsNote}
          segments={[
            { label: t.budgets.inTill, value: facilityFloat, color: "var(--series-1)" },
            { label: t.budgets.withWorkers, value: withWorkers, color: "var(--series-2)" },
            { label: t.budgets.usedUp, value: handedOut - withWorkers, color: "var(--series-3)" },
          ]}
        />
      </div>

      <BudgetTable page={page} />
    </>
  );
}
