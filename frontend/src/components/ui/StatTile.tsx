import styles from "./ui.module.css";

/** A row of headline numbers. */
export function KpiRow({ children }: { children: React.ReactNode }) {
  return <div className={styles.kpiRow}>{children}</div>;
}

/** One headline number. `hero` makes it the page's dominant figure; `accent` colours it by meaning. */
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
  const accentClass = accent === "good" ? styles.accentGood : accent === "critical" ? styles.accentCritical : "";
  return (
    <div className={styles.card}>
      <p className={styles.tileLabel}>{label}</p>
      <div className={`${styles.tileValue} ${hero ? styles.tileHero : ""} ${accentClass}`}>{value}</div>
      {note ? <p className={styles.tileNote}>{note}</p> : null}
    </div>
  );
}
