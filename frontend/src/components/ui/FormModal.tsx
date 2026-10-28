"use client";

import { Form } from "antd";
import { useState } from "react";

import { Modal } from "@/components/Modal";

interface FormModalProps<T> {
  open: boolean;
  title: string;
  /** values the form starts from on this opening, e.g. the record being edited */
  initialValues?: Partial<T>;
  pending?: boolean;
  okText?: string;
  onSubmit: (values: T) => void;
  onClose: () => void;
  children: React.ReactNode;
}

/**
 * A dialog holding an antd form that starts fresh every time it opens.
 *
 * antd keeps field values in the store created by `Form.useForm`, which outlives the dialog closing,
 * and a remounted form keeps those values over new `initialValues` — so a dialog reused for "create"
 * and "edit row X" showed the previous row's values. Each opening here mounts a new body with its own
 * store, so `initialValues` always win.
 */
export function FormModal<T>(props: FormModalProps<T>) {
  const [openings, setOpenings] = useState(0);
  const [wasOpen, setWasOpen] = useState(props.open);
  // adjust state during render when `open` flips to true (React's documented pattern, no effect needed)
  if (props.open !== wasOpen) {
    setWasOpen(props.open);
    if (props.open) setOpenings((count) => count + 1);
  }
  return <FormModalBody<T> key={openings} {...props} />;
}

function FormModalBody<T>({
  open,
  title,
  initialValues,
  pending,
  okText,
  onSubmit,
  onClose,
  children,
}: FormModalProps<T>) {
  const [form] = Form.useForm<T>();

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onClose}
      onOk={() => form.submit()}
      okText={okText}
      confirmLoading={pending}
      destroyOnHidden
    >
      <Form<T>
        form={form}
        layout="vertical"
        onFinish={onSubmit}
        initialValues={initialValues}
        style={{ marginTop: 16 }}
      >
        {children}
      </Form>
    </Modal>
  );
}
