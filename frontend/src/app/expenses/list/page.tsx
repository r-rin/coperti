import { getI18n } from "@/i18n/server";
import {
  getEmployeeBalances,
  getEmployees,
  getExpenseSum,
  getExpenses,
  getFundingByExpense,
  getShortfall,
} from "@/lib/queries";

import { PageHeader } from "@/components/ui/PageHeader";
import { KpiRow, StatTile } from "@/components/ui/StatTile";
import { ExpenseTable } from "./ExpenseTable";
import { Filters } from "./Filters";

export const dynamic = "force-dynamic";

function one(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export default async function ExpenseListPage(props: PageProps<"/expenses/list">) {
  const params = await props.searchParams;

  const query = {
    fromDate: one(params.fromDate),
    toDate: one(params.toDate),
    search: one(params.search),
    issuedByEmployee: one(params.issuedByEmployee),
    uncoveredOnly: one(params.uncoveredOnly) === "true" || undefined,
    page: Number(one(params.page) ?? 0),
    size: 20,
  };

  const [page, employees, filteredSum, balances] = await Promise.all([
    getExpenses(query),
    getEmployees(),
    getExpenseSum(query),
    getEmployeeBalances(),
  ]);
  const { t, money } = await getI18n();

  // coverage for the rows actually on screen, so the table can show what each expense still needs
  const coverageEntries = await Promise.all(
    page.data.map(async (expense) => {
      const [fundings, owed] = await Promise.all([
        getFundingByExpense(expense.id),
        getShortfall(expense.id),
      ]);
      const covered = fundings.reduce((sum, funding) => sum + funding.amountCovered, 0);
      return [expense.id, { covered, owed }] as const;
    }),
  );
  const coverage = Object.fromEntries(coverageEntries);

  const owedOnPage = coverageEntries.reduce((sum, [, value]) => sum + value.owed, 0);

  // a payment spends the worker's unspent advance first, so the pay dialog needs to know it
  const heldByEmployee = Object.fromEntries(balances.map((row) => [row.employeeId, row.unspent]));

  return (
    <>
      <PageHeader title={t.expenses.title} subtitle={t.expenses.subtitle} />

      <KpiRow>
        <StatTile
          label={t.expenses.matching}
          value={String(page.totalItems)}
          note={t.expenses.matchingNote}
        />
        <StatTile label={t.common.filteredTotal} value={money(filteredSum)} note={t.common.sumOfMatching} />
        <StatTile
          label={t.expenses.owedOnPage}
          value={money(owedOnPage)}
          accent={owedOnPage > 0 ? "critical" : undefined}
          note={t.expenses.owedOnPageNote}
        />
      </KpiRow>

      <Filters employees={employees} />
      <ExpenseTable
        page={page}
        employees={employees}
        coverage={coverage}
        heldByEmployee={heldByEmployee}
      />
    </>
  );
}
