"use client";

import { MoreOutlined } from "@ant-design/icons";
import { App, Button, Dropdown, Form, Input, InputNumber, Popconfirm, Space, Table, Typography } from "antd";
import { useState } from "react";

import { Modal } from "@/components/Modal";
import { FormModal } from "@/components/ui/FormModal";
import { StatusTag } from "@/components/ui/StatusTag";
import { TableCard } from "@/components/ui/TableCard";
import ui from "@/components/ui/ui.module.css";
import { useServerAction } from "@/components/ui/useServerAction";
import { useI18n } from "@/i18n/I18nProvider";
import type { Employee, LinkCode, TelegramAccount } from "@/lib/types";
import { createEmployee, deleteEmployee, issueLinkCode, linkTelegram, renameEmployee, unlinkTelegram } from "./actions";

/** An employee row with their Telegram link, if any. */
interface Row extends Employee {
  account: TelegramAccount | null;
}

export function PeopleTable({
  employees,
  accounts,
  botUsername,
}: {
  employees: Employee[];
  accounts: TelegramAccount[];
  /** the bot's @username, when configured, for one-tap link codes */
  botUsername: string | null;
}) {
  const { t, dateTime } = useI18n();
  const { modal } = App.useApp();
  const { pending, run } = useServerAction();

  const [adding, setAdding] = useState(false);
  const [renaming, setRenaming] = useState<Row | null>(null);
  const [linking, setLinking] = useState<Row | null>(null);
  const [issued, setIssued] = useState<{ name: string; code: LinkCode } | null>(null);

  const byEmployee = new Map(accounts.map((account) => [account.employeeId, account]));
  const rows: Row[] = employees.map((employee) => ({ ...employee, account: byEmployee.get(employee.id) ?? null }));

  function issueCode(row: Row) {
    run(() => issueLinkCode(row.id), undefined, (code) => {
      if (code) setIssued({ name: row.name, code });
    });
  }

  function confirmUnlink(row: Row) {
    modal.confirm({
      title: t.people.unlink,
      content: t.people.unlinkConfirm,
      okButtonProps: { danger: true },
      onOk: () => run(() => unlinkTelegram(row.id), t.people.unlinked),
    });
  }

  return (
    <>
      <TableCard
        title={t.people.tableTitle}
        count={rows.length}
        extra={
          <Button type="primary" onClick={() => setAdding(true)}>
            {t.people.add}
          </Button>
        }
      >
        <Table<Row>
          rowKey="id"
          dataSource={rows}
          size="middle"
          loading={pending}
          pagination={false}
          locale={{ emptyText: t.people.noEmployees }}
          columns={[
            { title: t.common.name, dataIndex: "name" },
            {
              title: t.people.telegram,
              key: "telegram",
              width: 200,
              render: (_, row) =>
                row.account ? (
                  <StatusTag label={t.people.linkedAs(row.account.telegramUserId)} tone="good" />
                ) : (
                  <StatusTag label={t.people.notLinked} />
                ),
            },
            {
              title: t.people.linkedSince,
              key: "linkedAt",
              width: 190,
              render: (_, row) => <span className={ui.num}>{row.account ? dateTime(row.account.linkedAt) : "—"}</span>,
            },
            {
              title: "",
              key: "actions",
              width: 230,
              render: (_, row) => (
                <Space size="small">
                  <Button size="small" onClick={() => setRenaming(row)}>
                    {t.people.rename}
                  </Button>
                  <Dropdown
                    trigger={["click"]}
                    menu={{
                      items: [
                        { key: "code", label: t.people.issueCode },
                        { key: "link", label: row.account ? t.people.relinkById : t.people.linkById },
                        { type: "divider" },
                        { key: "unlink", label: t.people.unlink, danger: true, disabled: !row.account },
                      ],
                      onClick: ({ key }) => {
                        if (key === "code") issueCode(row);
                        if (key === "link") setLinking(row);
                        if (key === "unlink") confirmUnlink(row);
                      },
                    }}
                  >
                    <Button size="small" icon={<MoreOutlined />} aria-label={t.people.telegramMenu}>
                      {t.people.telegram}
                    </Button>
                  </Dropdown>
                  <Popconfirm
                    title={t.people.deleteConfirm}
                    onConfirm={() => run(() => deleteEmployee(row.id), t.people.deleted)}
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

      <FormModal<{ name: string }>
        title={t.people.add}
        open={adding}
        pending={pending}
        onClose={() => setAdding(false)}
        onSubmit={({ name }) => run(() => createEmployee(name), t.people.added, () => setAdding(false))}
      >
        <NameField />
      </FormModal>

      <FormModal<{ name: string }>
        title={t.people.renameTitle}
        open={renaming !== null}
        pending={pending}
        initialValues={{ name: renaming?.name }}
        onClose={() => setRenaming(null)}
        onSubmit={({ name }) =>
          renaming && run(() => renameEmployee(renaming.id, name), t.people.renamed, () => setRenaming(null))
        }
      >
        <NameField />
      </FormModal>

      <FormModal<{ telegramUserId: number }>
        title={linking ? `${linking.name} · ${t.people.telegramId}` : t.people.linkById}
        open={linking !== null}
        pending={pending}
        initialValues={{ telegramUserId: linking?.account?.telegramUserId }}
        onClose={() => setLinking(null)}
        onSubmit={({ telegramUserId }) =>
          linking && run(() => linkTelegram(linking.id, telegramUserId), t.people.linked, () => setLinking(null))
        }
      >
        <Form.Item
          name="telegramUserId"
          label={t.people.telegramId}
          extra={t.people.telegramIdHint}
          rules={[{ required: true, message: t.people.telegramIdRequired }]}
        >
          <InputNumber min={1} precision={0} controls={false} style={{ width: "100%" }} autoFocus />
        </Form.Item>
      </FormModal>

      <Modal
        title={issued ? t.people.codeTitle(issued.name) : ""}
        open={issued !== null}
        onCancel={() => setIssued(null)}
        footer={<Button onClick={() => setIssued(null)}>{t.common.close}</Button>}
      >
        {issued && <IssuedCode code={issued.code} botUsername={botUsername} />}
      </Modal>
    </>
  );
}

function NameField() {
  const { t } = useI18n();
  return (
    <Form.Item
      name="name"
      label={t.common.name}
      rules={[{ required: true, whitespace: true, message: t.people.nameRequired }]}
    >
      <Input autoFocus />
    </Form.Item>
  );
}

/** Shown once after issuing: the code exists nowhere else the administrator can see it again. */
function IssuedCode({ code, botUsername }: { code: LinkCode; botUsername: string | null }) {
  const { t, dateTime } = useI18n();
  const command = `/start ${code.code}`;
  const deepLink = botUsername ? `https://t.me/${botUsername}?start=${code.code}` : null;

  return (
    <div style={{ marginTop: 12 }}>
      <Typography.Paragraph>{t.people.codeSend}</Typography.Paragraph>
      <Typography.Paragraph copyable={{ text: command }} style={{ fontSize: 22 }}>
        <code>{command}</code>
      </Typography.Paragraph>
      {deepLink && (
        <>
          <Typography.Paragraph>{t.people.codeOneTap}</Typography.Paragraph>
          <Typography.Paragraph copyable={{ text: deepLink }}>
            <a href={deepLink} target="_blank" rel="noreferrer">
              {deepLink}
            </a>
          </Typography.Paragraph>
        </>
      )}
      <Typography.Paragraph type="secondary">{t.people.codeValidUntil(dateTime(code.expiresAt))}</Typography.Paragraph>
    </div>
  );
}
