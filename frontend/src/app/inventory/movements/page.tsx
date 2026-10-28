import { PageHeader } from "@/components/ui/PageHeader";
import { getI18n } from "@/i18n/server";
import { facilityDayBound } from "@/lib/format";
import { getMovements, itemsById } from "@/lib/queries";
import { MOVEMENT_TYPES, type MovementType } from "@/lib/types";

import { inventoryLookups, many, one } from "../lookups";
import { MovementFilters } from "./MovementFilters";
import { type LedgerRow, MovementTable } from "./MovementTable";

export const dynamic = "force-dynamic";

export default async function MovementsPage(props: PageProps<"/inventory/movements">) {
  const params = await props.searchParams;
  const fromDate = one(params.fromDate);
  const toDate = one(params.toDate);
  const query = {
    itemId: one(params.itemId),
    locationId: one(params.locationId),
    types: many(params.types).filter((type): type is MovementType => MOVEMENT_TYPES.includes(type as MovementType)),
    performedById: one(params.performedById),
    // the dates are facility days; the ledger stores instants
    from: fromDate ? facilityDayBound(fromDate, "start") : undefined,
    to: toDate ? facilityDayBound(toDate, "end") : undefined,
    page: Number(one(params.page) ?? 0),
    size: 25,
  };

  const [page, lookups] = await Promise.all([getMovements(query), inventoryLookups()]);
  const { t } = await getI18n();
  const items = await itemsById([...page.data.map((m) => m.itemId), ...(query.itemId ? [query.itemId] : [])]);

  // a row reversed by another row on this page is marked; elsewhere the backend's refusal explains it
  const reversedHere = new Set(page.data.map((m) => m.reversalOfId).filter(Boolean));
  const rows: LedgerRow[] = page.data.map((movement) => ({
    ...movement,
    itemName: items.get(movement.itemId)?.name ?? movement.itemId,
    fromName: lookups.locationNames[movement.fromLocationId] ?? movement.fromLocationId,
    toName: lookups.locationNames[movement.toLocationId] ?? movement.toLocationId,
    performedByName: movement.performedById ? (lookups.employeeNames[movement.performedById] ?? null) : null,
    reversedHere: reversedHere.has(movement.id),
  }));

  return (
    <>
      <PageHeader title={t.inventory.movementsTitle} subtitle={t.inventory.movementsSubtitle} />
      <MovementFilters
        item={query.itemId ? (items.get(query.itemId) ?? null) : null}
        locations={lookups.locations.map((location) => ({ id: location.id, label: lookups.locationNames[location.id] }))}
      />
      <MovementTable
        page={{ ...page, data: rows }}
        pickableLocations={lookups.pickableLocations}
        item={query.itemId ? (items.get(query.itemId) ?? null) : null}
      />
    </>
  );
}
