"use client";

import { Button } from "antd";

import { EntitySelect, type EntityOption } from "@/components/pickers/EntitySelect";
import { ItemSelect } from "@/components/pickers/ItemSelect";
import { useActor } from "@/components/shell/ActorProvider";
import { FilterBar, FilterField } from "@/components/ui/FilterBar";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import type { Item } from "@/lib/types";

/** Each filter applies as soon as it changes; they live in the URL like every other list. */
export function StockFilters({ item, locations }: { item: Item | null; locations: EntityOption[] }) {
  const { t } = useI18n();
  const { employees } = useActor();
  const { params, setFilters, reset } = useUrlParams();

  return (
    <FilterBar>
      <FilterField label={t.common.item}>
        <ItemSelect
          allowClear
          initial={item}
          placeholder={t.inventory.anyItem}
          value={params.get("itemId") ?? undefined}
          onChange={(value) => setFilters({ itemId: value })}
          style={{ width: 240 }}
        />
      </FilterField>
      <FilterField label={t.common.location}>
        <EntitySelect
          allowClear
          placeholder={t.inventory.anyLocation}
          value={params.get("locationId") ?? undefined}
          onChange={(value) => setFilters({ locationId: value })}
          options={locations}
          style={{ width: 220 }}
        />
      </FilterField>
      <FilterField label={t.inventory.heldBy}>
        <EntitySelect
          allowClear
          placeholder={t.common.anyone}
          value={params.get("employeeId") ?? undefined}
          onChange={(value) => setFilters({ employeeId: value })}
          options={employees.map((e) => ({ id: e.id, label: e.name }))}
          style={{ width: 200 }}
        />
      </FilterField>
      <Button onClick={reset}>{t.common.reset}</Button>
    </FilterBar>
  );
}
