// Rendered only in the browser, and only once a report is requested: see download.tsx.

import { Document, Page, StyleSheet, Text, View } from "@react-pdf/renderer";

import type { Dictionary } from "@/i18n/dictionaries";
import type { AdvanceCoverage } from "@/lib/types";
import { type AdvanceReport, type ReportTotals, usedPercent } from "./summary";

const INK = "#1f2328";
const MUTED = "#656d76";
const RULE = "#d0d7de";
const SHADE = "#f6f8fa";

const s = StyleSheet.create({
  page: { fontSize: 9, color: INK, padding: 36, paddingBottom: 48 },
  title: { fontSize: 15, fontWeight: "bold", marginBottom: 6 },
  meta: { color: MUTED, marginBottom: 2 },
  metaValue: { color: INK },
  tiles: { flexDirection: "row", marginTop: 14, marginBottom: 10 },
  tile: { flex: 1, borderWidth: 1, borderColor: RULE, padding: 8, marginRight: 6 },
  tileLast: { marginRight: 0 },
  tileLabel: { color: MUTED, fontSize: 8, marginBottom: 3 },
  tileValue: { fontSize: 13, fontWeight: "bold" },
  tileNote: { color: MUTED, fontSize: 8, marginTop: 2 },
  line: { marginBottom: 3 },
  label: { color: MUTED },
  strong: { fontWeight: "bold" },
  h2: { fontSize: 11, fontWeight: "bold", marginTop: 16, marginBottom: 6 },
  h3: { fontSize: 9.5, fontWeight: "bold", marginTop: 10, marginBottom: 4 },
  workerTotals: { color: MUTED, marginBottom: 4 },
  table: { borderTopWidth: 1, borderColor: RULE },
  row: { flexDirection: "row", borderBottomWidth: 1, borderColor: RULE, paddingVertical: 3 },
  headRow: { backgroundColor: SHADE },
  headCell: { color: MUTED, fontSize: 8, fontWeight: "bold" },
  totalRow: { fontWeight: "bold" },
  cell: { paddingHorizontal: 4 },
  num: { textAlign: "right" },
  empty: { color: MUTED, paddingVertical: 3 },
  footer: {
    position: "absolute",
    bottom: 20,
    left: 36,
    right: 36,
    flexDirection: "row",
    justifyContent: "space-between",
    color: MUTED,
    fontSize: 7.5,
  },
});

interface Format {
  t: Dictionary;
  money: (value: number) => string;
}

type Column = { width: string; numeric?: boolean };

function Row({ cells, columns, head, total }: { cells: string[]; columns: Column[]; head?: boolean; total?: boolean }) {
  return (
    <View style={[s.row, head ? s.headRow : {}, total ? s.totalRow : {}]} wrap={false}>
      {cells.map((cell, i) => (
        <Text
          key={i}
          style={[s.cell, { width: columns[i].width }, columns[i].numeric ? s.num : {}, head ? s.headCell : {}]}
        >
          {cell}
        </Text>
      ))}
    </View>
  );
}

function Tile({ label, value, note, last }: { label: string; value: string; note: string; last?: boolean }) {
  return (
    <View style={[s.tile, last ? s.tileLast : {}]}>
      <Text style={s.tileLabel}>{label}</Text>
      <Text style={s.tileValue}>{value}</Text>
      <Text style={s.tileNote}>{note}</Text>
    </View>
  );
}

/** Draft B: the reconciliation header, for the whole selection. */
function Header({ report, generatedOn, f }: { report: AdvanceReport; generatedOn: string; f: Format }) {
  const { t, money } = f;
  const { totals } = report;
  const many = report.workers.length > 1;
  const showWrittenOff = totals.writtenOff > 0;

  return (
    <View>
      <Text style={s.title}>{t.report.title}</Text>
      <Text style={s.meta}>
        {many ? t.report.workers : t.report.worker}:{" "}
        <Text style={s.metaValue}>{report.workers.map((w) => w.name).join(", ")}</Text>
      </Text>
      <Text style={s.meta}>
        {t.report.period}: <Text style={s.metaValue}>{`${report.from ?? "—"} – ${report.to ?? "—"}`}</Text>
      </Text>
      <Text style={s.meta}>
        {t.report.generated}:{" "}
        <Text style={s.metaValue}>{`${generatedOn} · ${t.report.advancesSelected(totals.advances)}`}</Text>
      </Text>

      <View style={s.tiles}>
        <Tile label={t.report.given} value={money(totals.given)} note={t.report.advancesCount(totals.advances)} />
        <Tile label={t.report.covered} value={money(totals.covered)} note={t.report.percentUsed(usedPercent(totals))} />
        <Tile
          label={t.report.unspent}
          value={money(totals.unspent)}
          note={t.report.stillHeld}
          last={!showWrittenOff}
        />
        {/* only when it exists, so Given = Covered + Unspent (+ Written off) always reconciles on the page */}
        {showWrittenOff ? (
          <Tile label={t.report.writtenOff} value={money(totals.writtenOff)} note={t.report.writtenOffNote} last />
        ) : null}
      </View>

      <Text style={s.line}>
        <Text style={s.label}>{t.report.status}: </Text>
        {(["OPEN", "CLOSED", "CANCELLED"] as const)
          .map((status) => `${t.status.disbursement[status]} ${totals.byStatus[status]}`)
          .join(" · ")}
      </Text>
      <Text style={s.line}>
        <Text style={s.label}>{t.report.expenses}: </Text>
        {t.report.expensesCovered(totals.expenses, totals.partlyCovered)}
      </Text>
      <Text style={s.line}>
        <Text style={s.label}>{t.report.owed(many)}: </Text>
        <Text style={s.strong}>{money(totals.owed)}</Text>
      </Text>
    </View>
  );
}

const WORKER_COLUMNS: Column[] = [
  { width: "28%" },
  { width: "12%", numeric: true },
  { width: "15%", numeric: true },
  { width: "15%", numeric: true },
  { width: "15%", numeric: true },
  { width: "15%", numeric: true },
];

/** Draft C: one row per worker, shown only when the selection spans several. */
function WorkerSummary({ report, f }: { report: AdvanceReport; f: Format }) {
  const { t, money } = f;
  const cells = (name: string, totals: ReportTotals) => [
    name,
    String(totals.advances),
    money(totals.given),
    money(totals.covered),
    money(totals.unspent),
    money(totals.owed),
  ];

  return (
    <View>
      <Text style={s.h2}>{t.report.byWorker}</Text>
      <View style={s.table}>
        <Row
          head
          columns={WORKER_COLUMNS}
          cells={[
            t.report.worker,
            t.report.advancesColumn,
            t.report.given,
            t.report.covered,
            t.report.unspent,
            t.report.owedColumn,
          ]}
        />
        {report.workers.map((worker) => (
          <Row key={worker.employeeId} columns={WORKER_COLUMNS} cells={cells(worker.name, worker.totals)} />
        ))}
        <Row total columns={WORKER_COLUMNS} cells={cells(t.report.total, report.totals)} />
      </View>
    </View>
  );
}

const EXPENSE_COLUMNS: Column[] = [
  { width: "14%" },
  { width: "41%" },
  { width: "15%", numeric: true },
  { width: "15%", numeric: true },
  { width: "15%", numeric: true },
];

function AdvanceBlock({ advance, f }: { advance: AdvanceCoverage; f: Format }) {
  const { t, money } = f;
  const { disbursement, remaining, expenses } = advance;
  const coveredHere = expenses.reduce((sum, expense) => sum + expense.coveredHere, 0);

  return (
    <View>
      <View wrap={false}>
        <Text style={s.h3}>
          {t.report.advance(disbursement.date ?? "—", money(disbursement.amount))}
          <Text style={s.label}>
            {`  ·  ${t.status.disbursement[disbursement.status]}  ·  ${t.report.remaining(money(remaining))}`}
          </Text>
        </Text>
        {expenses.length ? (
          <Row
            head
            columns={EXPENSE_COLUMNS}
            cells={[t.common.date, t.common.description, t.report.expenseAmount, t.report.coveredHere, t.report.stillOwed]}
          />
        ) : (
          <Text style={s.empty}>{t.report.noExpenses}</Text>
        )}
      </View>
      {expenses.length ? (
        <View>
          {expenses.map((expense) => (
            <Row
              key={expense.expenseId}
              columns={EXPENSE_COLUMNS}
              cells={[
                expense.date ?? "—",
                expense.description ?? "—",
                money(expense.amount),
                money(expense.coveredHere),
                money(expense.shortfall),
              ]}
            />
          ))}
          <Row total columns={EXPENSE_COLUMNS} cells={["", t.report.subtotal, "", money(coveredHere), ""]} />
        </View>
      ) : null}
    </View>
  );
}

export function AdvanceReportDocument({
  report,
  generatedOn,
  fontFamily,
  t,
  money,
}: {
  report: AdvanceReport;
  generatedOn: string;
  /** set on the page and inherited by every text in it */
  fontFamily: string;
  t: Dictionary;
  money: (value: number) => string;
}) {
  const f: Format = { t, money };
  const many = report.workers.length > 1;

  return (
    <Document title={t.report.title} creator="Coperti" producer="Coperti">
      <Page size="A4" style={[s.page, { fontFamily }]}>
        <Header report={report} generatedOn={generatedOn} f={f} />
        {many ? <WorkerSummary report={report} f={f} /> : null}

        {report.workers.map((worker) => (
          <View key={worker.employeeId}>
            <Text style={s.h2} minPresenceAhead={60}>
              {worker.name}
            </Text>
            <Text style={s.workerTotals}>
              {[
                `${t.report.given} ${money(worker.totals.given)}`,
                `${t.report.covered} ${money(worker.totals.covered)}`,
                `${t.report.unspent} ${money(worker.totals.unspent)}`,
                `${t.report.owedColumn} ${money(worker.totals.owed)}`,
              ].join("  ·  ")}
            </Text>
            {worker.advances.map((advance) => (
              <AdvanceBlock key={advance.disbursement.id} advance={advance} f={f} />
            ))}
          </View>
        ))}

        <View style={s.footer} fixed>
          <Text>{`${t.report.title} · ${generatedOn}`}</Text>
          <Text render={({ pageNumber, totalPages }) => t.report.page(pageNumber, totalPages)} />
        </View>
      </Page>
    </Document>
  );
}
