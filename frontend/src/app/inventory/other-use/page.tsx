import { PageHeader } from "@/components/ui/PageHeader";
import { getI18n } from "@/i18n/server";
import { getOtherUses, itemsById } from "@/lib/queries";
import { OTHER_USE_STATUSES, type OtherUseStatus } from "@/lib/types";

import { inventoryLookups, one } from "../lookups";
import { OtherUseTable, type RequestRow } from "./OtherUseTable";

export const dynamic = "force-dynamic";

export default async function OtherUsePage(props: PageProps<"/inventory/other-use">) {
  const params = await props.searchParams;
  const status = one(params.status);
  const query = {
    status: OTHER_USE_STATUSES.includes(status as OtherUseStatus) ? (status as OtherUseStatus) : undefined,
    requestedById: one(params.requestedById),
    page: Number(one(params.page) ?? 0),
    size: 20,
  };

  const [page, lookups] = await Promise.all([getOtherUses(query), inventoryLookups()]);
  const { t } = await getI18n();
  const items = await itemsById(page.data.flatMap((request) => request.lines.map((line) => line.itemId)));
  const name = (id: string | null) => (id ? (lookups.employeeNames[id] ?? id) : null);

  const rows: RequestRow[] = page.data.map((request) => ({
    ...request,
    requestedByName: name(request.requestedById) ?? "—",
    decidedByName: name(request.approvedById),
    lineRows: request.lines.map((line) => ({
      ...line,
      itemName: items.get(line.itemId)?.name ?? line.itemId,
      fromName: lookups.locationNames[line.fromLocationId] ?? line.fromLocationId,
    })),
  }));

  return (
    <>
      <PageHeader title={t.inventory.otherUseTitle} subtitle={t.inventory.otherUseSubtitle} />
      <OtherUseTable page={{ ...page, data: rows }} pickableLocations={lookups.pickableLocations} />
    </>
  );
}
