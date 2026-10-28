"use client";

import { Button, Input, Select } from "antd";

import { EntitySelect, type EntityOption } from "@/components/pickers/EntitySelect";
import { ItemSelect } from "@/components/pickers/ItemSelect";
import { useActor } from "@/components/shell/ActorProvider";
import { FilterBar, FilterField } from "@/components/ui/FilterBar";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import { type Item, MOVEMENT_TYPES } from "@/lib/types";

/**
 * Slices of the ledger: "what did Ann receive this week" is the employee's location + Issue + a period.
 * Every control applies immediately and lives in the URL.
 */
export function MovementFilters({ item, locations }: { item: Item | null; locations: EntityOption[] }) {
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
          style={{ width: 220 }}
        />
      </FilterField>
      <FilterField label={t.common.location}>
        <EntitySelect
          allowClear
          placeholder={t.inventory.anyLocation}
          value={params.get("locationId") ?? undefined}
          onChange={(value) => setFilters({ locationId: value })}
          options={locations}
          style={{ width: 200 }}
        />
      </FilterField>
      <FilterField label={t.inventory.movementType}>
        <Select
          mode="multiple"
          allowClear
          maxTagCount="responsive"
          placeholder={t.inventory.anyType}
          value={params.getAll("types")}
          onChange={(value: string[]) => setFilters({ types: value })}
          options={MOVEMENT_TYPES.map((type) => ({ value: type, label: t.status.movement[type] }))}
          style={{ width: 240 }}
        />
      </FilterField>
      <FilterField label={t.inventory.performedBy}>
        <EntitySelect
          allowClear
          placeholder={t.common.anyone}
          value={params.get("performedById") ?? undefined}
          onChange={(value) => setFilters({ performedById: value })}
          options={employees.map((e) => ({ id: e.id, label: e.name }))}
          style={{ width: 180 }}
        />
      </FilterField>
      <FilterField label={t.common.from}>
        <Input
          type="date"
          value={params.get("fromDate") ?? ""}
          onChange={(e) => setFilters({ fromDate: e.target.value })}
          style={{ width: 150 }}
        />
      </FilterField>
      <FilterField label={t.inventory.until}>
        <Input
          type="date"
          value={params.get("toDate") ?? ""}
          onChange={(e) => setFilters({ toDate: e.target.value })}
          style={{ width: 150 }}
        />
      </FilterField>
      <Button onClick={reset}>{t.common.reset}</Button>
    </FilterBar>
  );
}
