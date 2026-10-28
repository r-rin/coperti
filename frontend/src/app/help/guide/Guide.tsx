import { BulbOutlined, ExperimentOutlined, FunctionOutlined, InfoCircleOutlined, WarningOutlined } from "./icons";
import Link from "next/link";

import styles from "./guide.module.css";

/**
 * Building blocks of the user guide. Pages are written as plain JSX with these, one file per language,
 * so the prose reads naturally in each language instead of being stitched from translated fragments.
 */

export function Guide({ children }: { children: React.ReactNode }) {
  return <article className={styles.guide}>{children}</article>;
}

export function GuideHero({
  icon,
  eyebrow,
  title,
  lead,
}: {
  icon: React.ReactNode;
  eyebrow: string;
  title: string;
  lead: React.ReactNode;
}) {
  return (
    <header className={styles.hero}>
      <span className={styles.heroIcon} aria-hidden="true">
        {icon}
      </span>
      <p className={styles.eyebrow}>{eyebrow}</p>
      <h1 className={styles.heroTitle}>{title}</h1>
      <p className={styles.lead}>{lead}</p>
    </header>
  );
}

export function Section({
  id,
  icon,
  title,
  children,
}: {
  id?: string;
  icon?: React.ReactNode;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section id={id} className={styles.section}>
      <div className={styles.sectionHead}>
        {icon && (
          <span className={styles.sectionIcon} aria-hidden="true">
            {icon}
          </span>
        )}
        <h2 className={styles.sectionTitle}>{title}</h2>
      </div>
      {children}
    </section>
  );
}

export function Steps({ children }: { children: React.ReactNode }) {
  return <ol className={styles.steps}>{children}</ol>;
}

export function Step({ title, children }: { title: string; children?: React.ReactNode }) {
  return (
    <li className={styles.step}>
      <strong className={styles.stepTitle}>{title}</strong>
      {children && <div className={styles.stepBody}>{children}</div>}
    </li>
  );
}

const CALLOUT = {
  note: { className: "", Icon: InfoCircleOutlined },
  tip: { className: styles.calloutTip, Icon: BulbOutlined },
  warning: { className: styles.calloutWarning, Icon: WarningOutlined },
};

export function Callout({
  tone = "note",
  title,
  children,
}: {
  tone?: keyof typeof CALLOUT;
  title?: string;
  children: React.ReactNode;
}) {
  const { className, Icon } = CALLOUT[tone];
  return (
    <aside className={`${styles.callout} ${className}`}>
      <span className={styles.calloutIcon} aria-hidden="true">
        <Icon />
      </span>
      <div>
        {title && <strong className={styles.calloutTitle}>{title}</strong>}
        <div className={styles.calloutBody}>{children}</div>
      </div>
    </aside>
  );
}

/** A formula shown as named quantities joined by operators: <V result>Owed</V> <Op>=</Op> <V>Amount</V> … */
export function Formula({ name, note, children }: { name: string; note?: React.ReactNode; children: React.ReactNode }) {
  return (
    <figure className={styles.formula} role="math" aria-label={name}>
      <figcaption className={styles.formulaName}>
        <FunctionOutlined aria-hidden="true" />
        {name}
      </figcaption>
      <div className={styles.formulaBody}>{children}</div>
      {note && <p className={styles.formulaNote}>{note}</p>}
    </figure>
  );
}

/** A quantity in a formula; `result` marks the left-hand side. */
export function V({ result, children }: { result?: boolean; children: React.ReactNode }) {
  return <span className={`${styles.var} ${result ? styles.result : ""}`}>{children}</span>;
}

export function Op({ children }: { children: React.ReactNode }) {
  return <span className={styles.op}>{children}</span>;
}

/** A function name such as max or Σ. */
export function Fn({ children }: { children: React.ReactNode }) {
  return <span className={styles.fn}>{children}</span>;
}

export function Example({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className={styles.example}>
      <p className={styles.exampleTitle}>
        <ExperimentOutlined aria-hidden="true" />
        {title}
      </p>
      {children}
    </div>
  );
}

/** Label/value rows of a worked example; the last row is the result. */
export function ExampleTable({ rows }: { rows: [React.ReactNode, React.ReactNode][] }) {
  return (
    <table className={styles.exampleTable}>
      <tbody>
        {rows.map(([label, value], index) => (
          <tr key={index}>
            <td>{label}</td>
            <td>{value}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

/** A button, field or menu label exactly as the user sees it on screen. */
export function Ui({ children }: { children: React.ReactNode }) {
  return <span className={styles.ui}>{children}</span>;
}

export function Cards({ children }: { children: React.ReactNode }) {
  return <div className={styles.cards}>{children}</div>;
}

export function GuideCard({
  href,
  icon,
  title,
  children,
}: {
  href: string;
  icon: React.ReactNode;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <Link href={href} className={styles.card}>
      <span className={styles.cardIcon} aria-hidden="true">
        {icon}
      </span>
      <span className={styles.cardTitle}>{title}</span>
      <span className={styles.cardText}>{children}</span>
    </Link>
  );
}

/** A glossary: the value as it appears in the app (usually a tag) next to what it means. */
export function Terms({ items }: { items: { key: string; term: React.ReactNode; meaning: React.ReactNode }[] }) {
  return (
    <div className={styles.terms}>
      {items.map((item) => (
        <div key={item.key} className={styles.term}>
          <div>{item.term}</div>
          <div className={styles.termMeaning}>{item.meaning}</div>
        </div>
      ))}
    </div>
  );
}
