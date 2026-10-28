import { PageHeader } from "@/components/ui/PageHeader";
import { getI18n } from "@/i18n/server";
import { isVirtualKind } from "@/lib/types";

import { inventoryLookups } from "../lookups";
import { LocationTable, type LocationRow } from "./LocationTable";

export const dynamic = "force-dynamic";

export default async function LocationsPage() {
  const { locations, locationNames } = await inventoryLookups();
  const { t } = await getI18n();

  const rows: LocationRow[] = locations.map((location) => ({ ...location, label: locationNames[location.id] }));
  // what people manage first; then everyone's own location, then the virtual sinks
  const rank = (row: LocationRow) => (isVirtualKind(row.kind) ? 2 : row.kind === "EMPLOYEE" ? 1 : 0);
  rows.sort((a, b) => rank(a) - rank(b) || a.label.localeCompare(b.label));

  return (
    <>
      <PageHeader title={t.inventory.locationsTitle} subtitle={t.inventory.locationsSubtitle} />
      <LocationTable rows={rows} />
    </>
  );
}
