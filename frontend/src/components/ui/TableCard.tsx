import styles from "./ui.module.css";

/**
 * A titled panel around a table (or any block). `count` shows next to the title in mono;
 * `padded` adds inner padding for non-table content. Works in Server and Client Components.
 */
export function TableCard({
  title,
  count,
  note,
  extra,
  padded,
  children,
}: {
  title: string;
  count?: number;
  note?: string;
  extra?: React.ReactNode;
  padded?: boolean;
  children: React.ReactNode;
}) {
  return (
    <section className={styles.tableCard}>
      <div className={styles.tableHead}>
        <div>
          <h2 className={styles.tableTitle}>
            {title} {count !== undefined && <span className={styles.mono}>({count})</span>}
          </h2>
          {note && <p className={styles.tableNote}>{note}</p>}
        </div>
        {extra}
      </div>
      {padded ? <div className={styles.tableBody}>{children}</div> : children}
    </section>
  );
}
