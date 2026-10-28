import styles from "./ui.module.css";

/** The strip of filters above a table. */
export function FilterBar({ children }: { children: React.ReactNode }) {
  return <div className={styles.filters}>{children}</div>;
}

/** A labelled control; also used for compact fields outside antd forms. */
export function FilterField({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className={styles.filterField}>
      <span className={styles.filterLabel}>{label}</span>
      {children}
    </label>
  );
}
