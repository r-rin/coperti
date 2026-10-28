import Link from "next/link";

import styles from "./ui.module.css";

/**
 * Title, one-line explanation and the page's primary actions; `back` links to the parent page.
 * Works in Server and Client Components.
 */
export function PageHeader({
  title,
  subtitle,
  actions,
  back,
}: {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
  back?: { href: string; label: string };
}) {
  return (
    <header className={styles.pageHead}>
      <div>
        {back && (
          <Link href={back.href} className={`${styles.mono} ${styles.back}`}>
            {back.label}
          </Link>
        )}
        <h1 className={styles.title}>{title}</h1>
        {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
      </div>
      {actions && <div className={styles.actions}>{actions}</div>}
    </header>
  );
}
