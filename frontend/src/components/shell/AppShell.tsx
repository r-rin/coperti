import { getI18n } from "@/i18n/server";
import { getActorId } from "@/lib/actor";
import { getEmployees } from "@/lib/queries";
import type { Employee } from "@/lib/types";
import { ActorProvider } from "./ActorProvider";
import { ActorSelect } from "./ActorSelect";
import { LanguageSelect } from "./LanguageSelect";
import { Sidebar } from "./Sidebar";
import styles from "./shell.module.css";

/** The shell renders on every page; a backend outage must not take it down, only the page's own data. */
async function employeesOrNone(): Promise<Employee[]> {
  try {
    return await getEmployees();
  } catch {
    return [];
  }
}

export async function AppShell({ children }: { children: React.ReactNode }) {
  const [{ t }, employees, actorId] = await Promise.all([getI18n(), employeesOrNone(), getActorId()]);
  // a deleted employee left in the cookie simply means nobody is chosen
  const actor = employees.find((employee) => employee.id === actorId) ?? null;

  return (
    <ActorProvider value={{ actor, employees }}>
      <div className={styles.root}>
        <div className={styles.shell}>
          <aside className={styles.sidebar}>
            <div className={styles.brand}>
              Coperti
              <span className={styles.brandSub}>{t.shell.tagline}</span>
            </div>
            <Sidebar />
            <div className={styles.sidebarFoot}>
              <ActorSelect />
              <LanguageSelect />
            </div>
          </aside>
          <main className={styles.main}>{children}</main>
        </div>
      </div>
    </ActorProvider>
  );
}
