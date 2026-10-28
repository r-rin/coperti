"use client";

import { Button, Form, Input, Popconfirm, Select, Space, Table } from "antd";
import { useState } from "react";

import { FormModal } from "@/components/ui/FormModal";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useI18n } from "@/i18n/I18nProvider";
import { type Location, MANUAL_LOCATION_KINDS } from "@/lib/types";
import { createLocation, deleteLocation, type LocationInput, updateLocation } from "../actions";
import { LocationKindTag } from "../tags";

export interface LocationRow extends Location {
  label: string;
}

export function LocationTable({ rows }: { rows: LocationRow[] }) {
  const { t } = useI18n();
  const { pending, run } = useServerAction();
  // null: closed; "new": creating; a row: editing it
  const [editing, setEditing] = useState<LocationRow | "new" | null>(null);
  const current = editing === "new" ? null : editing;

  function submit(values: LocationInput) {
    run(
      () => (current ? updateLocation({ ...values, id: current.id }) : createLocation(values)),
      current ? t.inventory.locationUpdated : t.inventory.locationAdded,
      () => setEditing(null),
    );
  }

  return (
    <>
      <TableCard
        title={t.inventory.locationsTable}
        count={rows.length}
        note={t.inventory.autoCreatedNote}
        extra={
          <Button type="primary" onClick={() => setEditing("new")}>
            {t.inventory.addLocation}
          </Button>
        }
      >
        <Table<LocationRow>
          rowKey="id"
          dataSource={rows}
          size="middle"
          loading={pending}
          pagination={false}
          locale={{ emptyText: t.inventory.noLocationsYet }}
          columns={[
            { title: t.common.code, dataIndex: "code", width: 220, render: (code: string) => <span className={ui.mono}>{code}</span> },
            { title: t.common.name, dataIndex: "label" },
            {
              title: t.inventory.kind,
              dataIndex: "kind",
              width: 180,
              render: (kind: Location["kind"]) => <LocationKindTag kind={kind} />,
            },
            {
              title: "",
              key: "actions",
              width: 170,
              // employee and virtual locations are the backend's: created on demand, never edited by hand
              render: (_, row) =>
                MANUAL_LOCATION_KINDS.includes(row.kind) ? (
                  <Space size="small">
                    <Button size="small" onClick={() => setEditing(row)}>
                      {t.common.edit}
                    </Button>
                    <Popconfirm
                      title={t.inventory.deleteLocationConfirm}
                      onConfirm={() => run(() => deleteLocation(row.id), t.inventory.locationDeleted)}
                    >
                      <Button size="small" danger>
                        {t.common.delete}
                      </Button>
                    </Popconfirm>
                  </Space>
                ) : (
                  <span className={ui.mono}>{t.inventory.automatic}</span>
                ),
            },
          ]}
        />
      </TableCard>

      <FormModal<LocationInput>
        title={current ? t.inventory.editLocation : t.inventory.addLocation}
        open={editing !== null}
        pending={pending}
        onClose={() => setEditing(null)}
        onSubmit={submit}
        initialValues={current ? { code: current.code, name: current.name ?? "", kind: current.kind } : { kind: "WAREHOUSE" }}
      >
        <Form.Item
          name="code"
          label={t.common.code}
          rules={[{ required: true, whitespace: true, message: t.inventory.codeRequired }]}
        >
          <Input placeholder="W1" autoFocus />
        </Form.Item>
        <Form.Item name="name" label={t.common.name}>
          <Input placeholder={t.inventory.locationNamePlaceholder} />
        </Form.Item>
        <Form.Item name="kind" label={t.inventory.kind} rules={[{ required: true }]}>
          <Select options={MANUAL_LOCATION_KINDS.map((kind) => ({ value: kind, label: t.status.locationKind[kind] }))} />
        </Form.Item>
      </FormModal>
    </>
  );
}
