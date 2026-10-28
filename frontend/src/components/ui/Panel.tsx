import styles from "./ui.module.css";

/** A padded block with a small title, for charts and forms (tables go in TableCard). */
export function Panel({ title, note, children }: { title: string; note?: string; children?: React.ReactNode }) {
  return (
    <section className={styles.card}>
      <h3 className={styles.cardTitle}>{title}</h3>
      {note ? <p className={styles.cardNote}>{note}</p> : null}
      {children}
    </section>
  );
}
