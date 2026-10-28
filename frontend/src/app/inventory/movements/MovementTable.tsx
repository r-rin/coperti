"use client";

import { ArrowRightOutlined } from "@ant-design/icons";
import { Button, Form, Input, Space, Table, Tooltip } from "antd";
import { useState } from "react";

import { EntitySelect, type EntityOption } from "@/components/pickers/EntitySelect";
import { useActor } from "@/components/shell/ActorProvider";
import { FormModal } from "@/components/ui/FormModal";
import { StatusTag } from "@/components/ui/StatusTag";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import type { Item, PaginatedResponse, StockMovement } from "@/lib/types";
import { reverseMovement } from "../actions";
import { MovementDialog } from "../MovementDialog";
import { MovementTag, QualityTag } from "../tags";

export interface LedgerRow extends StockMovement {
  itemName: string;
  fromName: string;
  toName: string;
  performedByName: string | null;
  /** another row on this page reverses this one */
  reversedHere: boolean;
}

export function MovementTable({
  page,
  pickableLocations,
  item,
}: {
  page: PaginatedResponse<LedgerRow>;
  pickableLocations: EntityOption[];
  item: Item | null;
}) {
  const { t, qty, dateTime } = useI18n();
  const { actor, employees } = useActor();
  const { goToPage } = useUrlParams();
  const { pending, run } = useServerAction();
  const [recording, setRecording] = useState(false);
  const [reversing, setReversing] = useState<LedgerRow | null>(null);

  /** Why a row can't be reversed here, or null when it can (the backend has the final word). */
  function whyNotReversible(row: LedgerRow): string | null {
    if (row.reversalOfId) return t.inventory.isReversal;
    if (row.reversedHere) return t.inventory.alreadyReversed;
    if (row.workOrderId || row.otherUseId) return t.inventory.correctedElsewhere;
    return null;
  }

  return (
    <>
      <TableCard
        title={t.inventory.ledgerTitle}
        count={page.totalItems}
        extra={
          <Button type="primary" onClick={() => setRecording(true)} disabled={!pickableLocations.length}>
            {t.inventory.recordMovement}
          </Button>
        }
      >
        <Table<LedgerRow>
          rowKey="id"
          dataSource={page.data}
          size="middle"
          loading={pending}
          locale={{ emptyText: t.inventory.noMovements }}
          pagination={{
            current: page.currentPage + 1,
            pageSize: page.pageSize,
            total: page.totalItems,
            showSizeChanger: false,
            onChange: goToPage,
          }}
          columns={[
            {
              title: t.common.date,
              dataIndex: "occurredAt",
              width: 160,
              render: (value: string) => <span className={ui.num}>{dateTime(value)}</span>,
            },
            {
              title: t.inventory.movementType,
              dataIndex: "movementType",
              width: 170,
              render: (_, row) => (
                <Space size={4} wrap>
                  <MovementTag type={row.movementType} />
                  {row.reversalOfId && <StatusTag label={t.inventory.reversalTag} tone="warning" />}
                  {row.reversedHere && <StatusTag label={t.inventory.reversedTag} />}
                </Space>
              ),
            },
            { title: t.common.item, dataIndex: "itemName" },
            {
              title: t.common.quantity,
              dataIndex: "qty",
              align: "right",
              width: 100,
              render: (value: number) => <span className={ui.num}>{qty(value)}</span>,
            },
            {
              title: t.inventory.fromTo,
              key: "route",
              render: (_, row) => (
                <Space size={6} wrap>
                  <span>{row.fromName}</span>
                  {row.fromQuality !== row.toQuality || row.fromQuality !== "GOOD" ? (
                    <QualityTag quality={row.fromQuality} />
                  ) : null}
                  <ArrowRightOutlined style={{ color: "var(--text-muted)", fontSize: 11 }} />
                  <span>{row.toName}</span>
                  {row.fromQuality !== row.toQuality ? <QualityTag quality={row.toQuality} /> : null}
                </Space>
              ),
            },
            {
              title: t.inventory.performedBy,
              dataIndex: "performedByName",
              width: 150,
              render: (name: string | null) => name ?? "—",
            },
            { title: t.inventory.note, dataIndex: "note", render: (note: string | null) => note || "—" },
            {
              title: "",
              key: "actions",
              width: 110,
              render: (_, row) => {
                const reason = whyNotReversible(row);
                return (
                  <Tooltip title={reason}>
                    <Button size="small" disabled={reason !== null} onClick={() => setReversing(row)}>
                      {t.inventory.reverse}
                    </Button>
                  </Tooltip>
                );
              },
            },
          ]}
        />
      </TableCard>

      <MovementDialog
        open={recording}
        onClose={() => setRecording(false)}
        locations={pickableLocations}
        item={item}
      />

      <FormModal<{ performedById?: string; note?: string }>
        title={t.inventory.reverseTitle}
        open={reversing !== null}
        pending={pending}
        okText={t.inventory.reverse}
        initialValues={{ performedById: actor?.id }}
        onClose={() => setReversing(null)}
        onSubmit={({ performedById, note }) =>
          reversing &&
          run(() => reverseMovement(reversing.id, performedById, note), t.inventory.reversed, () => setReversing(null))
        }
      >
        {reversing && (
          <p style={{ marginTop: 0 }}>
            {t.inventory.reverseExplain(qty(reversing.qty), reversing.itemName, reversing.toName, reversing.fromName)}
          </p>
        )}
        <Form.Item name="performedById" label={t.inventory.performedBy}>
          <EntitySelect allowClear options={employees.map((e) => ({ id: e.id, label: e.name }))} />
        </Form.Item>
        <Form.Item name="note" label={t.inventory.note}>
          <Input placeholder={t.inventory.reverseNotePlaceholder} />
        </Form.Item>
      </FormModal>
    </>
  );
}
