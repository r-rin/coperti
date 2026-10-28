import { getI18n } from "@/i18n/server";
import {
  getDisbursementSum,
  getDisbursements,
  getEmployeeBalances,
  getEmployees,
  getRemainingBalance,
} from "@/lib/queries";
import type { DisbursementStatus } from "@/lib/types";

import { PageHeader } from "@/components/ui/PageHeader";
import { KpiRow, StatTile } from "@/components/ui/StatTile";
import { DisbursementTable } from "./DisbursementTable";

export const dynamic = "force-dynamic";

function one(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export default async function DisbursementsPage(props: PageProps<"/expenses/disbursements">) {
  const params = await props.searchParams;
  const status = one(params.statuses) as DisbursementStatus | undefined;

  const query = {
    givenToEmployee: one(params.givenToEmployee),
    statuses: status ? [status] : undefined,
    page: Number(one(params.page) ?? 0),
    size: 20,
  };

  const [page, employees, workerBalances, filteredSum] = await Promise.all([
    getDisbursements(query),
    getEmployees(),
    getEmployeeBalances(),
    getDisbursementSum(query),
  ]);
  const { t, money } = await getI18n();

  const balanceEntries = await Promise.all(
    page.data.map(async (row) => [row.id, await getRemainingBalance(row.id)] as const),
  );
  const balances = Object.fromEntries(balanceEntries);
  // what is still cash in hand, not the face value of open advances
  const unspent = workerBalances.reduce((sum, row) => sum + row.unspent, 0);

  return (
    <>
      <PageHeader title={t.advances.title} subtitle={t.advances.subtitle} />

      <KpiRow>
        <StatTile label={t.advances.unspent} value={money(unspent)} note={t.advances.unspentNote} />
        <StatTile label={t.common.filteredTotal} value={money(filteredSum)} note={t.common.sumOfMatching} />
      </KpiRow>

      <DisbursementTable page={page} employees={employees} balances={balances} />
    </>
  );
}
