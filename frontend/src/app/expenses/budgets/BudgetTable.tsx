"use client";

import { App, Button, Form, Input, InputNumber, Table } from "antd";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, useTransition } from "react";

import { Modal } from "@/components/Modal";
import { useI18n } from "@/i18n/I18nProvider";
import type { Budget, PaginatedResponse } from "@/lib/types";
import { createBudget } from "../actions";
import styles from "../expenses.module.css";

interface FormValues {
  amount: number;
  givenBy?: string;
  description?: string;
  date: string;
}

export function BudgetTable({ page }: { page: PaginatedResponse<Budget> }) {
  const router = useRouter();
  const params = useSearchParams();
  const { message } = App.useApp();
  const { t, money } = useI18n();
  const [pending, startTransition] = useTransition();
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<FormValues>();

  function goToPage(next: number) {
    const search = new URLSearchParams(params.toString());
    search.set("page", String(next - 1));
    router.push(`/expenses/budgets?${search.toString()}`);
  }

  function submit(values: FormValues) {
    startTransition(async () => {
      const result = await createBudget(values);
      if (result.ok) {
        message.success(t.budgets.recorded);
        setOpen(false);
        router.refresh();
      } else {
        message.error(result.error ?? t.common.somethingWentWrong);
      }
    });
  }

  return (
    <>
      <div className={styles.tableCard}>
        <div className={styles.tableHead}>
          <h2 className={styles.tableTitle}>
            {t.budgets.tableTitle} <span className={styles.mono}>({page.totalItems})</span>
          </h2>
          <Button type="primary" onClick={() => setOpen(true)}>
            {t.budgets.record}
          </Button>
        </div>
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
              render: (v: number) => <span className={styles.num}>{money(v)}</span>,
            },
          ]}
        />
      </div>

      <Modal
        title={t.budgets.record}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={pending}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={submit} preserve={false} style={{ marginTop: 16 }}>
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
        </Form>
      </Modal>
    </>
  );
}
