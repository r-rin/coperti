"use client";

import { Button, Form, Input, InputNumber, Popconfirm, Space, Table } from "antd";
import Link from "next/link";
import { useState } from "react";

import { EntitySelect } from "@/components/pickers/EntitySelect";
import { FormModal } from "@/components/ui/FormModal";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import { shortId } from "@/lib/format";
import type { Employee, Expense, PaginatedResponse } from "@/lib/types";
import { createExpense, deleteExpense, updateExpense } from "../actions";
import styles from "../expenses.module.css";
import { PayDialog, type PayLine } from "./PayDialog";

interface FormValues {
  employeeId: string;
  amount: number;
  date: string;
  description?: string;
}

export function ExpenseTable({
  page,
  employees,
  coverage,
  heldByEmployee,
}: {
  page: PaginatedResponse<Expense>;
  employees: Employee[];
  coverage: Record<string, { covered: number; owed: number }>;
  /** unspent cash each worker holds on open advances */
  heldByEmployee: Record<string, number>;
}) {
  const { goToPage } = useUrlParams();
  const { pending, run } = useServerAction();
  const { t, money } = useI18n();
  const [editing, setEditing] = useState<Expense | null>(null);
  const [open, setOpen] = useState(false);
  // kept across pages, so a worker's receipts can be gathered from several pages into one payment
  const [selected, setSelected] = useState<Record<string, PayLine>>({});
  const [paying, setPaying] = useState(false);

  const names = new Map(employees.map((e) => [e.id, e.name]));
  const owedOf = (row: Expense) => coverage[row.id]?.owed ?? 0;

  const lines = Object.values(selected);
  // one payment is one cash hand-over, which always goes to a single person
  const selectedWorker = lines[0]?.employeeId ?? null;
  const selectedWorkerName = selectedWorker ? (names.get(selectedWorker) ?? shortId(selectedWorker)) : "";

  function toggle(row: Expense, checked: boolean) {
    setSelected((current) => {
      const next = { ...current };
      if (checked && row.employeeId) {
        next[row.id] = {
          id: row.id,
          employeeId: row.employeeId,
          date: row.date,
          description: row.description,
          owed: owedOf(row),
        };
      } else {
        delete next[row.id];
      }
      return next;
    });
  }

  function openCreate() {
    setEditing(null);
    setOpen(true);
  }

  function openEdit(expense: Expense) {
    setEditing(expense);
    setOpen(true);
  }

  function submit(values: FormValues) {
    const work = editing
      ? () => updateExpense({ ...values, id: editing.id })
      : () => createExpense(values);
    run(work, editing ? t.expenses.updated : t.expenses.recorded, () => setOpen(false));
  }

  return (
    <>
      <TableCard
        title={t.expenses.tableTitle}
        count={page.totalItems}
        extra={
          lines.length ? (
            <div className={styles.selectionBar}>
              <span className={styles.selectionHint}>{t.expenses.oneWorkerHint(selectedWorkerName)}</span>
              <Button onClick={() => setSelected({})}>{t.expenses.clearSelection}</Button>
              <Button type="primary" onClick={() => setPaying(true)}>
                {t.expenses.paySelected(lines.length)}
              </Button>
            </div>
          ) : (
            <Space>
              {/* employees are managed on the People page; recording needs at least one */}
              <Link href="/people">
                <Button>{t.expenses.manageWorkers}</Button>
              </Link>
              <Button type="primary" onClick={openCreate} disabled={!employees.length}>
                {t.expenses.record}
              </Button>
            </Space>
          )
        }
      >
        <Table<Expense>
          rowKey="id"
          dataSource={page.data}
          size="middle"
          loading={pending}
          rowSelection={{
            selectedRowKeys: Object.keys(selected),
            preserveSelectedRowKeys: true,
            // select-all could mix workers on one page, and a payment goes to one person
            hideSelectAll: true,
            getCheckboxProps: (row) => ({
              disabled: owedOf(row) <= 0 || (selectedWorker !== null && row.employeeId !== selectedWorker),
            }),
            onSelect: (row, checked) => toggle(row, checked),
          }}
          pagination={{
            current: page.currentPage + 1,
            pageSize: page.pageSize,
            total: page.totalItems,
            showSizeChanger: false,
            onChange: goToPage,
          }}
          columns={[
            { title: t.common.date, dataIndex: "date", width: 116, render: (v: string | null) => v ?? "—" },
            {
              title: t.common.worker,
              dataIndex: "employeeId",
              render: (id: string | null) => (id ? (names.get(id) ?? shortId(id)) : "—"),
            },
            { title: t.common.description, dataIndex: "description", render: (v: string | null) => v || "—" },
            {
              title: t.common.amount,
              dataIndex: "amount",
              align: "right",
              width: 116,
              render: (v: number) => <span className={ui.num}>{money(v)}</span>,
            },
            {
              title: t.expenses.covered,
              key: "covered",
              align: "right",
              width: 116,
              render: (_, row) => (
                <span className={ui.num}>{money(coverage[row.id]?.covered ?? 0)}</span>
              ),
            },
            {
              title: t.expenses.owed,
              key: "owed",
              align: "right",
              width: 116,
              render: (_, row) => {
                const owed = owedOf(row);
                return (
                  <span className={`${ui.num} ${owed > 0 ? styles.owedText : ""}`}>{money(owed)}</span>
                );
              },
            },
            {
              title: "",
              key: "actions",
              width: 150,
              render: (_, row) => (
                <Space size="small">
                  <Button size="small" onClick={() => openEdit(row)}>
                    {t.common.edit}
                  </Button>
                  <Popconfirm
                    title={t.expenses.deleteConfirm}
                    onConfirm={() => run(() => deleteExpense(row.id), t.expenses.deleted)}
                  >
                    <Button size="small" danger>
                      {t.common.delete}
                    </Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </TableCard>

      <PayDialog
        open={paying}
        workerName={selectedWorkerName}
        lines={lines}
        held={selectedWorker ? (heldByEmployee[selectedWorker] ?? 0) : 0}
        onClose={() => setPaying(false)}
        onPaid={() => {
          setPaying(false);
          setSelected({});
        }}
      />

      <FormModal<FormValues>
        title={editing ? t.expenses.editTitle : t.expenses.record}
        open={open}
        onClose={() => setOpen(false)}
        onSubmit={submit}
        pending={pending}
        initialValues={
          editing
            ? {
                employeeId: editing.employeeId ?? undefined,
                amount: editing.amount,
                date: editing.date ?? "",
                description: editing.description ?? "",
              }
            : undefined
        }
      >
        <Form.Item
          name="employeeId"
          label={t.common.worker}
          rules={[{ required: true, message: t.expenses.pickWorker }]}
        >
          <EntitySelect
            disabled={!!editing}
            options={employees.map((e) => ({ id: e.id, label: e.name }))}
            placeholder={t.expenses.whoPaid}
          />
        </Form.Item>
        <Form.Item
          name="amount"
          label={t.common.amount}
          rules={[{ required: true, message: t.common.amountRequired }]}
        >
          <InputNumber min={0.01} step={0.01} style={{ width: "100%" }} placeholder="0.00" />
        </Form.Item>
        <Form.Item name="date" label={t.common.date} rules={[{ required: true, message: t.common.dateRequired }]}>
          <Input type="date" />
        </Form.Item>
        <Form.Item name="description" label={t.common.description}>
          <Input.TextArea rows={2} placeholder={t.expenses.whatBought} />
        </Form.Item>
      </FormModal>
    </>
  );
}
