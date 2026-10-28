"use client";

import { Button, Form, Input, InputNumber, Table } from "antd";
import { useState } from "react";

import { FormModal } from "@/components/ui/FormModal";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useUrlParams } from "@/components/ui/useUrlParams";
import { useI18n } from "@/i18n/I18nProvider";
import type { Budget, PaginatedResponse } from "@/lib/types";
import { createBudget } from "../actions";

interface FormValues {
  amount: number;
  givenBy?: string;
  description?: string;
  date: string;
}

export function BudgetTable({ page }: { page: PaginatedResponse<Budget> }) {
  const { goToPage } = useUrlParams();
  const { pending, run } = useServerAction();
  const { t, money } = useI18n();
  const [open, setOpen] = useState(false);

  function submit(values: FormValues) {
    run(() => createBudget(values), t.budgets.recorded, () => setOpen(false));
  }

  return (
    <>
      <TableCard
        title={t.budgets.tableTitle}
        count={page.totalItems}
        extra={
          <Button type="primary" onClick={() => setOpen(true)}>
            {t.budgets.record}
          </Button>
        }
      >
        <Table<Budget>
          rowKey="id"
          dataSource={page.data}
          size="middle"
          loading={pending}
          pagination={{
            current: page.currentPage + 1,
            pageSize: page.pageSize,
            total: page.totalItems,
            showSizeChanger: false,
            onChange: goToPage,
          }}
          columns={[
            { title: t.common.date, dataIndex: "date", width: 116, render: (v: string | null) => v ?? "—" },
            { title: t.common.from, dataIndex: "givenBy", render: (v: string | null) => v || "—" },
            { title: t.common.description, dataIndex: "description", render: (v: string | null) => v || "—" },
            {
              title: t.common.amount,
              dataIndex: "amount",
              align: "right",
              width: 130,
              render: (v: number) => <span className={ui.num}>{money(v)}</span>,
            },
          ]}
        />
      </TableCard>

      <FormModal<FormValues>
        title={t.budgets.record}
        open={open}
        onClose={() => setOpen(false)}
        onSubmit={submit}
        pending={pending}
      >
        <Form.Item name="amount" label={t.common.amount} rules={[{ required: true, message: t.common.amountRequired }]}>
          <InputNumber min={0.01} step={0.01} style={{ width: "100%" }} placeholder="0.00" />
        </Form.Item>
        <Form.Item name="date" label={t.common.date} rules={[{ required: true, message: t.common.dateRequired }]}>
          <Input type="date" />
        </Form.Item>
        <Form.Item name="givenBy" label={t.common.from}>
          <Input placeholder={t.budgets.whoProvided} />
        </Form.Item>
        <Form.Item name="description" label={t.common.description}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </FormModal>
    </>
  );
}
