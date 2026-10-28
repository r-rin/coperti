import { PageHeader } from "@/components/ui/PageHeader";
import { KpiRow, StatTile } from "@/components/ui/StatTile";
import { getI18n } from "@/i18n/server";
import { getEmployees, getTelegramAccounts, telegramBotUsername } from "@/lib/queries";

import { PeopleTable } from "./PeopleTable";

export const dynamic = "force-dynamic";

export default async function PeoplePage() {
  const [employees, accounts] = await Promise.all([getEmployees(), getTelegramAccounts()]);
  const { t } = await getI18n();

  return (
    <>
      <PageHeader title={t.people.title} subtitle={t.people.subtitle} />

      <KpiRow>
        <StatTile label={t.people.kpiEmployees} value={String(employees.length)} note={t.people.kpiEmployeesNote} />
        <StatTile label={t.people.kpiLinked} value={String(accounts.length)} note={t.people.kpiLinkedNote} />
      </KpiRow>

      <PeopleTable employees={employees} accounts={accounts} botUsername={telegramBotUsername()} />
    </>
  );
}
