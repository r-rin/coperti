"use client";

import { MoreOutlined } from "@ant-design/icons";
import { App, Button, Dropdown, Space, Table } from "antd";
import Link from "next/link";
import { useState } from "react";

import type { EntityOption } from "@/components/pickers/EntitySelect";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useI18n } from "@/i18n/I18nProvider";
import type { Item, StockBalance } from "@/lib/types";
import { rebuildBalances } from "./actions";
import { MovementDialog } from "./MovementDialog";
import { QualityTag } from "./tags";

export interface StockRow extends StockBalance {
  itemName: string;
  locationName: string;
}

export function StockTable({
  rows,
  pickableLocations,
  item,
}: {
  rows: StockRow[];
  pickableLocations: EntityOption[];
  /** the item the view is filtered to, preselected in the movement dialog */
  item: Item | null;
}) {
  const { t, qty } = useI18n();
  const { modal } = App.useApp();
  const { pending, run } = useServerAction();
  const [recording, setRecording] = useState(false);

  function confirmRebuild() {
    modal.confirm({
      title: t.inventory.rebuild,
      content: t.inventory.rebuildNote,
      onOk: () => run(() => rebuildBalances(), t.inventory.rebuilt),
    });
  }

  return (
    <>
      <TableCard
        title={t.inventory.balancesTitle}
        count={rows.length}
        extra={
          <Space>
            <Dropdown
              trigger={["click"]}
              menu={{ items: [{ key: "rebuild", label: t.inventory.rebuild }], onClick: confirmRebuild }}
            >
              <Button icon={<MoreOutlined />} aria-label={t.inventory.maintenance} />
            </Dropdown>
            <Button type="primary" onClick={() => setRecording(true)} disabled={!pickableLocations.length}>
              {t.inventory.recordMovement}
            </Button>
          </Space>
        }
      >
        <Table<StockRow>
          rowKey={(row) => `${row.itemId}|${row.locationId}|${row.quality}`}
          dataSource={rows}
          size="middle"
          loading={pending}
          pagination={{ pageSize: 50, hideOnSinglePage: true, showSizeChanger: false }}
          locale={{ emptyText: pickableLocations.length ? t.inventory.noStock : t.inventory.noLocationsYet }}
          columns={[
            {
              title: t.common.item,
              dataIndex: "itemName",
              sorter: (a, b) => a.itemName.localeCompare(b.itemName),
              render: (name: string, row) => (
                <Link href={`/inventory/movements?itemId=${row.itemId}`} title={t.inventory.showMovements}>
                  {name}
                </Link>
              ),
            },
            {
              title: t.common.location,
              dataIndex: "locationName",
              sorter: (a, b) => a.locationName.localeCompare(b.locationName),
            },
            {
              title: t.inventory.quality,
              dataIndex: "quality",
              width: 140,
              render: (quality: StockRow["quality"]) => <QualityTag quality={quality} />,
            },
            {
              title: t.common.quantity,
              dataIndex: "qty",
              align: "right",
              width: 130,
              sorter: (a, b) => a.qty - b.qty,
              render: (value: number) => <span className={ui.num}>{qty(value)}</span>,
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
    </>
  );
}
