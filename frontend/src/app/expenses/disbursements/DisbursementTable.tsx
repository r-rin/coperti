"use client";

import { DownOutlined, FilePdfOutlined, MoreOutlined, RightOutlined } from "@ant-design/icons";
import { App, Button, Dropdown, Form, Input, InputNumber, Select, Space, Table, Tag } from "antd";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, useTransition } from "react";

import { Modal } from "@/components/Modal";
import { useI18n } from "@/i18n/I18nProvider";
import { shortId } from "@/lib/format";
import {
  DISBURSEMENT_STATUSES,
  type Disbursement,
  type DisbursementStatus,
  type Employee,
  type PaginatedResponse,
} from "@/lib/types";
import { createDisbursement, updateDisbursementStatus } from "../actions";
import styles from "../expenses.module.css";
import { loadCoverage } from "./coverage";
import { CoveredExpenses } from "./CoveredExpenses";
import { downloadAdvanceReport } from "./report/download";

const STATUS_COLOR: Record<DisbursementStatus, string> = {
  OPEN: "blue",
  CLOSED: "default",
  CANCELLED: "warning",
};

export function DisbursementTable({
  page,
  employees,
  balances,
}: {
  page: PaginatedResponse<Disbursement>;
  employees: Employee[];
  balances: Record<string, number>;
}) {
  const router = useRouter();
  const params = useSearchParams();
  const { message, modal } = App.useApp();
  const { t, money, locale } = useI18n();
  const [pending, startTransition] = useTransition();
  const [open, setOpen] = useState(false);
  // kept across pages, so one report can gather advances from several pages and workers
  const [selected, setSelected] = useState<string[]>([]);
  const [reporting, setReporting] = useState(false);
  const [form] = Form.useForm<{ employeeId: string; amount: number; date: string }>();

  const names = new Map(employees.map((e) => [e.id, e.name]));

  function setParam(key: string, value: string | undefined) {
    const next = new URLSearchParams(params.toString());
    if (value) next.set(key, value);
    else next.delete(key);
    next.delete("page");
    router.push(`/expenses/disbursements?${next.toString()}`);
  }

  function act(work: () => Promise<{ ok: boolean; error?: string }>, success: string) {
    startTransition(async () => {
      const result = await work();
      if (result.ok) {
        message.success(success);
        router.refresh();
      } else {
        message.error(result.error ?? t.common.somethingWentWrong);
      }
    });
  }

  function confirmStatus(row: Disbursement, status: "CLOSED" | "CANCELLED") {
    const closing = status === "CLOSED";
    modal.confirm({
      title: closing ? t.advances.closeConfirm : t.advances.cancelConfirm,
      content: closing ? t.advances.closeConfirmNote : t.advances.cancelConfirmNote,
      okButtonProps: { danger: !closing },
      onOk: () =>
        act(() => updateDisbursementStatus(row.id, status), closing ? t.advances.closed : t.advances.cancelled),
    });
  }

  async function report(ids: string[]) {
    setReporting(true);
    try {
      const result = await loadCoverage(ids);
      if (!result.ok) {
        message.error(result.error);
        return;
      }
      await downloadAdvanceReport(result.data, names, locale);
    } catch {
      message.error(t.advances.reportFailed);
    } finally {
      setReporting(false);
    }
  }

  function submit(values: { employeeId: string; amount: number; date: string }) {
    startTransition(async () => {
      const result = await createDisbursement(values);
      if (result.ok) {
        message.success(t.advances.handedOut);
        setOpen(false);
        router.refresh();
      } else {
        message.error(result.error ?? t.common.somethingWentWrong);
      }
    });
  }

  return (
    <>
      <div className={styles.filters}>
        <label className={styles.filterField}>
          <span className={styles.filterLabel}>{t.common.worker}</span>
          <Select
            allowClear
            placeholder={t.common.anyone}
            value={params.get("givenToEmployee") ?? undefined}
            onChange={(value) => setParam("givenToEmployee", value ?? undefined)}
            options={employees.map((e) => ({ value: e.id, label: e.name }))}
            style={{ width: 180 }}
          />
        </label>
        <label className={styles.filterField}>
          <span className={styles.filterLabel}>{t.common.state}</span>
          <Select
            allowClear
            placeholder={t.common.anyState}
            value={params.get("statuses") ?? undefined}
            onChange={(value) => setParam("statuses", value ?? undefined)}
            options={DISBURSEMENT_STATUSES.map((s) => ({ value: s, label: t.status.disbursement[s] }))}
            style={{ width: 160 }}
          />
        </label>
        <Button type="primary" onClick={() => setOpen(true)} disabled={!employees.length}>
          {t.advances.handOut}
        </Button>
      </div>

      <div className={styles.tableCard}>
        <div className={styles.tableHead}>
          <h2 className={styles.tableTitle}>
            {t.advances.tableTitle} <span className={styles.mono}>({page.totalItems})</span>
          </h2>
          {selected.length ? (
            <div className={styles.selectionBar}>
              <span className={styles.selectionHint}>{t.advances.selectionHint}</span>
              <Button onClick={() => setSelected([])}>{t.advances.clearSelection}</Button>
              <Button type="primary" icon={<FilePdfOutlined />} loading={reporting} onClick={() => report(selected)}>
                {t.advances.reportSelected(selected.length)}
              </Button>
            </div>
          ) : null}
        </div>
        <Table<Disbursement>
          rowKey="id"
          dataSource={page.data}
          size="middle"
          loading={pending}
          rowSelection={{
            selectedRowKeys: selected,
            preserveSelectedRowKeys: true,
            onChange: (keys) => setSelected(keys as string[]),
          }}
          expandable={{
            expandedRowRender: (row) => <CoveredExpenses disbursementId={row.id} />,
            expandIcon: ({ expanded, onExpand, record }) => (
              <Button
                type="text"
                size="small"
                aria-label={expanded ? t.advances.collapse : t.advances.expand}
                title={expanded ? t.advances.collapse : t.advances.expand}
                icon={expanded ? <DownOutlined /> : <RightOutlined />}
                onClick={(event) => onExpand(record, event)}
              />
            ),
          }}
          pagination={{
            current: page.currentPage + 1,
            pageSize: page.pageSize,
            total: page.totalItems,
            showSizeChanger: false,
            onChange: (next) => setParam("page", String(next - 1)),
          }}
          columns={[
            { title: t.common.date, dataIndex: "date", width: 116, render: (v: string | null) => v ?? "—" },
            {
              title: t.common.worker,
              dataIndex: "employeeId",
              render: (id: string) => names.get(id) ?? shortId(id),
            },
            {
              title: t.common.state,
              dataIndex: "status",
              width: 118,
              render: (status: DisbursementStatus) => <Tag color={STATUS_COLOR[status]}>{t.status.disbursement[status]}</Tag>,
            },
            {
              title: t.common.amount,
              dataIndex: "amount",
              align: "right",
              width: 116,
              render: (v: number) => <span className={styles.num}>{money(v)}</span>,
            },
            {
              title: t.common.remaining,
              key: "remaining",
              align: "right",
              width: 116,
              render: (_, row) => (
                <span className={styles.num}>{money(balances[row.id] ?? 0)}</span>
              ),
            },
            {
              title: "",
              key: "actions",
              width: 150,
              render: (_, row) => (
                <Space size="small">
                  <Link href={`/expenses/disbursements/${row.id}`}>
                    <Button size="small" type="primary" ghost>
                      {t.advances.allocate}
                    </Button>
                  </Link>
                  <Dropdown
                    trigger={["click"]}
                    menu={{
                      items: [
                        { key: "report", icon: <FilePdfOutlined />, label: t.advances.report, disabled: reporting },
                        { type: "divider" },
                        { key: "close", label: t.advances.close, disabled: row.status !== "OPEN" },
                        { key: "cancel", label: t.advances.cancel, danger: true, disabled: row.status !== "OPEN" },
                      ],
                      onClick: ({ key }) => {
                        if (key === "report") report([row.id]);
                        if (key === "close") confirmStatus(row, "CLOSED");
                        if (key === "cancel") confirmStatus(row, "CANCELLED");
                      },
                    }}
                  >
                    <Button size="small" type="text" icon={<MoreOutlined />} aria-label={t.advances.more} />
                  </Dropdown>
                </Space>
              ),
            },
          ]}
        />
      </div>

      <Modal
        title={t.advances.handOut}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={pending}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={submit} preserve={false} style={{ marginTop: 16 }}>
          <Form.Item
            name="employeeId"
            label={t.common.worker}
            rules={[{ required: true, message: t.advances.pickRecipient }]}
          >
            <Select options={employees.map((e) => ({ value: e.id, label: e.name }))} />
          </Form.Item>
          <Form.Item name="amount" label={t.common.amount} rules={[{ required: true, message: t.common.amountRequired }]}>
            <InputNumber min={0.01} step={0.01} style={{ width: "100%" }} placeholder="0.00" />
          </Form.Item>
          <Form.Item name="date" label={t.common.date} rules={[{ required: true, message: t.common.dateRequired }]}>
            <Input type="date" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
