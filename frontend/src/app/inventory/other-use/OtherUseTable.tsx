"use client";

import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { App, Button, Form, Input, InputNumber, Select, Space, Table, Tooltip } from "antd";
import { useState } from "react";

import { EntitySelect, type EntityOption } from "@/components/pickers/EntitySelect";
import { ItemSelect } from "@/components/pickers/ItemSelect";
import { useActor } from "@/components/shell/ActorProvider";
import { FilterBar, FilterField } from "@/components/ui/FilterBar";
import { FormModal } from "@/components/ui/FormModal";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import { OTHER_USE_STATUSES, type OtherUse, type OtherUseLine, type PaginatedResponse, QUALITY_STATES } from "@/lib/types";
import { approveOtherUse, createOtherUse, type OtherUseInput, rejectOtherUse } from "../actions";
import { OtherUseTag, QualityTag } from "../tags";

export interface RequestRow extends OtherUse {
  requestedByName: string;
  decidedByName: string | null;
  lineRows: (OtherUseLine & { itemName: string; fromName: string })[];
}

export function OtherUseTable({
  page,
  pickableLocations,
}: {
  page: PaginatedResponse<RequestRow>;
  pickableLocations: EntityOption[];
}) {
  const { t, qty, dateTime } = useI18n();
  const { modal } = App.useApp();
  const { actor, employees } = useActor();
  const { params, setFilters, goToPage } = useUrlParams();
  const { pending, run } = useServerAction();
  const [creating, setCreating] = useState(false);
  const employeeOptions = employees.map((e) => ({ id: e.id, label: e.name }));

  // approving moves stock in the approver's name, so it needs to know who that is
  function decide(row: RequestRow, approve: boolean) {
    if (!actor) return;
    modal.confirm({
      title: approve ? t.inventory.approveConfirm(actor.name) : t.inventory.rejectConfirm(actor.name),
      content: approve ? t.inventory.approveNote : undefined,
      okButtonProps: { danger: !approve },
      onOk: () =>
        run(
          () => (approve ? approveOtherUse(row.id, actor.id) : rejectOtherUse(row.id, actor.id)),
          approve ? t.inventory.approved : t.inventory.rejected,
        ),
    });
  }

  return (
    <>
      <FilterBar>
        <FilterField label={t.common.state}>
          <Select
            allowClear
            placeholder={t.common.anyState}
            value={params.get("status") ?? undefined}
            onChange={(value) => setFilters({ status: value })}
            options={OTHER_USE_STATUSES.map((status) => ({ value: status, label: t.status.otherUse[status] }))}
            style={{ width: 170 }}
          />
        </FilterField>
        <FilterField label={t.inventory.requestedBy}>
          <EntitySelect
            allowClear
            placeholder={t.common.anyone}
            value={params.get("requestedById") ?? undefined}
            onChange={(value) => setFilters({ requestedById: value })}
            options={employeeOptions}
            style={{ width: 200 }}
          />
        </FilterField>
      </FilterBar>

      <TableCard
        title={t.inventory.requestsTitle}
        count={page.totalItems}
        extra={
          <Button type="primary" onClick={() => setCreating(true)} disabled={!pickableLocations.length}>
            {t.inventory.newRequest}
          </Button>
        }
      >
        <Table<RequestRow>
          rowKey="id"
          dataSource={page.data}
          size="middle"
          loading={pending}
          locale={{ emptyText: t.inventory.noRequests }}
          pagination={{
            current: page.currentPage + 1,
            pageSize: page.pageSize,
            total: page.totalItems,
            showSizeChanger: false,
            onChange: goToPage,
          }}
          expandable={{ expandedRowRender: (row) => <Lines lines={row.lineRows} /> }}
          columns={[
            {
              title: t.inventory.requestedAt,
              dataIndex: "requestedAt",
              width: 170,
              render: (value: string) => <span className={ui.num}>{dateTime(value)}</span>,
            },
            { title: t.inventory.requestedBy, dataIndex: "requestedByName", width: 170 },
            { title: t.inventory.reason, dataIndex: "reason", render: (reason: string | null) => reason || "—" },
            {
              title: t.inventory.lines,
              key: "lines",
              width: 220,
              render: (_, row) =>
                row.lineRows.map((line) => `${qty(line.qty)} × ${line.itemName}`).join(", "),
            },
            {
              title: t.common.state,
              dataIndex: "status",
              width: 130,
              render: (status: RequestRow["status"]) => <OtherUseTag status={status} />,
            },
            {
              title: t.inventory.decidedBy,
              key: "decided",
              width: 190,
              render: (_, row) =>
                row.decidedByName ? (
                  <span>
                    {row.decidedByName}
                    {row.occurredAt && <span className={ui.mono}> · {dateTime(row.occurredAt)}</span>}
                  </span>
                ) : (
                  "—"
                ),
            },
            {
              title: "",
              key: "actions",
              width: 190,
              render: (_, row) =>
                row.status === "PENDING" ? (
                  <Tooltip title={actor ? undefined : t.inventory.chooseActorFirst}>
                    <Space size="small">
                      <Button size="small" type="primary" ghost disabled={!actor} onClick={() => decide(row, true)}>
                        {t.inventory.approve}
                      </Button>
                      <Button size="small" danger disabled={!actor} onClick={() => decide(row, false)}>
                        {t.inventory.reject}
                      </Button>
                    </Space>
                  </Tooltip>
                ) : null,
            },
          ]}
        />
      </TableCard>

      <FormModal<OtherUseInput>
        title={t.inventory.newRequest}
        open={creating}
        pending={pending}
        onClose={() => setCreating(false)}
        onSubmit={(values) => run(() => createOtherUse(values), t.inventory.requestCreated, () => setCreating(false))}
        initialValues={{ requestedById: actor?.id, lines: [{ itemId: "", fromLocationId: "", quality: "GOOD", qty: 1 }] }}
      >
        <Form.Item
          name="requestedById"
          label={t.inventory.requestedBy}
          rules={[{ required: true, message: t.inventory.pickEmployee }]}
        >
          <EntitySelect options={employeeOptions} />
        </Form.Item>
        <Form.Item name="reason" label={t.inventory.reason}>
          <Input placeholder={t.inventory.reasonPlaceholder} />
        </Form.Item>
        <Form.List
          name="lines"
          rules={[
            {
              validator: async (_, lines) => {
                if (!lines?.length) throw new Error(t.inventory.linesRequired);
              },
            },
          ]}
        >
          {(fields, { add, remove }, { errors }) => (
            <>
              {fields.map(({ key, name }) => (
                <Space key={key} align="start" style={{ display: "flex", flexWrap: "wrap" }}>
                  <Form.Item name={[name, "itemId"]} rules={[{ required: true, message: t.inventory.pickItem }]}>
                    <ItemSelect placeholder={t.common.item} style={{ width: 190 }} />
                  </Form.Item>
                  <Form.Item name={[name, "fromLocationId"]} rules={[{ required: true, message: t.inventory.pickLocation }]}>
                    <EntitySelect placeholder={t.inventory.fromLocation} options={pickableLocations} style={{ width: 150 }} />
                  </Form.Item>
                  <Form.Item name={[name, "quality"]}>
                    <Select
                      options={QUALITY_STATES.map((quality) => ({ value: quality, label: t.status.quality[quality] }))}
                      style={{ width: 120 }}
                    />
                  </Form.Item>
                  <Form.Item name={[name, "qty"]} rules={[{ required: true, message: t.inventory.qtyRequired }]}>
                    <InputNumber min={0.001} precision={3} style={{ width: 90 }} />
                  </Form.Item>
                  <Button icon={<DeleteOutlined />} onClick={() => remove(name)} aria-label={t.common.delete} />
                </Space>
              ))}
              <Button
                type="dashed"
                icon={<PlusOutlined />}
                onClick={() => add({ quality: "GOOD", qty: 1 })}
                style={{ marginBottom: 8 }}
              >
                {t.inventory.addLine}
              </Button>
              <Form.ErrorList errors={errors} />
            </>
          )}
        </Form.List>
      </FormModal>
    </>
  );
}

function Lines({ lines }: { lines: RequestRow["lineRows"] }) {
  const { t, qty } = useI18n();
  return (
    <Table
      rowKey="id"
      size="small"
      pagination={false}
      dataSource={lines}
      columns={[
        { title: t.common.item, dataIndex: "itemName" },
        { title: t.inventory.fromLocation, dataIndex: "fromName" },
        { title: t.inventory.quality, dataIndex: "quality", render: (quality) => <QualityTag quality={quality} /> },
        {
          title: t.common.quantity,
          dataIndex: "qty",
          align: "right",
          render: (value: number) => <span className={ui.num}>{qty(value)}</span>,
        },
      ]}
    />
  );
}
