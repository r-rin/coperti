import { PageHeader } from "@/components/ui/PageHeader";
import { KpiRow, StatTile } from "@/components/ui/StatTile";
import { getI18n } from "@/i18n/server";
import { getAvailable, getBalances, itemsById } from "@/lib/queries";
import { isVirtualKind } from "@/lib/types";

import { inventoryLookups, one } from "./lookups";
import { StockFilters } from "./StockFilters";
import { StockTable, type StockRow } from "./StockTable";

export const dynamic = "force-dynamic";

export default async function StockPage(props: PageProps<"/inventory">) {
  const params = await props.searchParams;
  const filter = {
    itemId: one(params.itemId),
    locationId: one(params.locationId),
    employeeId: one(params.employeeId),
  };

  const [balances, lookups, available] = await Promise.all([
    getBalances(filter),
    inventoryLookups(),
    filter.itemId ? getAvailable(filter.itemId) : Promise.resolve(null),
  ]);
  const { t, qty } = await getI18n();

  const kindOf = new Map(lookups.locations.map((location) => [location.id, location.kind]));
  // virtual sinks hold no real stock (the supplier "owns" minus everything ever received), so they are
  // left out unless the user explicitly asks for one of them
  const visible = balances.filter(
    (balance) => balance.locationId === filter.locationId || !isVirtualKind(kindOf.get(balance.locationId) ?? "WAREHOUSE"),
  );
  const items = await itemsById([...visible.map((b) => b.itemId), ...(filter.itemId ? [filter.itemId] : [])]);

  const rows: StockRow[] = visible.map((balance) => ({
    ...balance,
    itemName: items.get(balance.itemId)?.name ?? balance.itemId,
    locationName: lookups.locationNames[balance.locationId] ?? balance.locationId,
  }));

  const onHandInWarehouses = visible
    .filter((b) => b.quality === "GOOD" && kindOf.get(b.locationId) === "WAREHOUSE")
    .reduce((sum, b) => sum + b.qty, 0);

  return (
    <>
      <PageHeader title={t.inventory.stockTitle} subtitle={t.inventory.stockSubtitle} />

      <KpiRow>
        {available !== null ? (
          <>
            <StatTile
              label={t.inventory.available}
              value={qty(available)}
              hero
              accent={available <= 0 ? "critical" : undefined}
              note={t.inventory.availableNote}
            />
            <StatTile label={t.inventory.inWarehouses} value={qty(onHandInWarehouses)} note={t.inventory.inWarehousesNote} />
            <StatTile
              label={t.inventory.reserved}
              value={qty(onHandInWarehouses - available)}
              note={t.inventory.reservedNote}
            />
          </>
        ) : (
          <>
            <StatTile
              label={t.inventory.itemsInStock}
              value={String(new Set(rows.map((row) => row.itemId)).size)}
              note={t.inventory.itemsInStockNote}
            />
            <StatTile
              label={t.inventory.locationsHolding}
              value={String(new Set(rows.map((row) => row.locationId)).size)}
              note={t.inventory.locationsHoldingNote}
            />
          </>
        )}
      </KpiRow>

      <StockFilters
        item={filter.itemId ? (items.get(filter.itemId) ?? null) : null}
        locations={lookups.locations.map((location) => ({ id: location.id, label: lookups.locationNames[location.id] }))}
      />
      <StockTable
        rows={rows}
        pickableLocations={lookups.pickableLocations}
        item={filter.itemId ? (items.get(filter.itemId) ?? null) : null}
      />
    </>
  );
}
