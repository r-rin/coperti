import { getI18n } from "@/i18n/server";
import { LanguageSelect } from "./LanguageSelect";
import { Sidebar } from "./Sidebar";
import styles from "./shell.module.css";

export async function AppShell({ children }: { children: React.ReactNode }) {
  const { t } = await getI18n();

  return (
    <div className={styles.root}>
      <div className={styles.shell}>
        <aside className={styles.sidebar}>
          <div className={styles.brand}>
            Coperti
            <span className={styles.brandSub}>{t.shell.tagline}</span>
          </div>
          <Sidebar />
          <LanguageSelect />
        </aside>
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
}
