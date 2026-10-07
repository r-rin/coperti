// Client-only. The PDF engine is heavy, so it is fetched on the first report request, not with the page.

import type { Locale } from "@/i18n/config";
import { dictionaries } from "@/i18n/dictionaries";
import { formatMoney } from "@/lib/format";
import type { AdvanceCoverage } from "@/lib/types";
import { buildReport } from "./summary";

let renders = 0;

/** Today in the viewer's own calendar, as the yyyy-mm-dd the rest of the app shows. */
function today(): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

export async function downloadAdvanceReport(
  advances: AdvanceCoverage[],
  names: Map<string, string>,
  locale: Locale,
): Promise<void> {
  const [{ Font, pdf }, { AdvanceReportDocument }] = await Promise.all([
    import("@react-pdf/renderer"),
    import("./AdvanceReportDocument"),
  ]);

  // The built-in PDF fonts have no Cyrillic, so Ukrainian needs an embedded one. A fresh family per
  // report: react-pdf subsets a loaded font on first use, so reusing it drops every glyph the
  // previous report did not contain (Font.reset() crashes instead). The file comes from HTTP cache.
  const fontFamily = `report-font-${++renders}`;
  const base = `${window.location.origin}/fonts`;
  Font.register({
    family: fontFamily,
    fonts: [
      { src: `${base}/DejaVuSansCondensed.ttf` },
      { src: `${base}/DejaVuSansCondensed-Bold.ttf`, fontWeight: "bold" },
    ],
  });
  // never break a word mid-way; names and descriptions read worse hyphenated
  Font.registerHyphenationCallback((word) => [word]);

  const t = dictionaries[locale];
  const generatedOn = today();
  const blob = await pdf(
    <AdvanceReportDocument
      report={buildReport(advances, names)}
      generatedOn={generatedOn}
      fontFamily={fontFamily}
      t={t}
      money={(value) => formatMoney(value, locale)}
    />,
  ).toBlob();

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = t.report.fileName(generatedOn);
  link.click();
  // revoked on the next tick: some browsers start the download only after click() returns
  setTimeout(() => URL.revokeObjectURL(url), 0);
}
