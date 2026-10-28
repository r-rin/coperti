import type { Locale } from "@/i18n/config";
import { getLocale } from "@/i18n/server";

/** Renders the guide page written for the viewer's language (each language is its own JSX file). */
export async function Localized({ pages }: { pages: Record<Locale, React.ComponentType> }) {
  const Page = pages[await getLocale()];
  return <Page />;
}
