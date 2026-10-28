"use client";

import { Form, Input, InputNumber, Select } from "antd";

import { EntitySelect, type EntityOption } from "@/components/pickers/EntitySelect";
import { ItemSelect } from "@/components/pickers/ItemSelect";
import { useActor } from "@/components/shell/ActorProvider";
import { FormModal } from "@/components/ui/FormModal";
import { useServerAction } from "@/components/ui/useServerAction";
import { useI18n } from "@/i18n/I18nProvider";
import { type Item, MANUAL_MOVEMENT_TYPES, type ManualMovementType, QUALITY_STATES } from "@/lib/types";
import { type MovementInput, postMovement } from "./actions";

/** Which fields each movement type needs; the supplier, scrap bin and employee locations are filled in by the action. */
const FIELDS: Record<ManualMovementType, { from?: boolean; to?: boolean; employee?: "recipient" | "returner"; qualityChange?: boolean }> = {
  RECEIPT: { to: true },
  TRANSFER: { from: true, to: true },
  ISSUE_TO_EMPLOYEE: { from: true, employee: "recipient" },
  RETURN_FROM_EMPLOYEE: { employee: "returner", to: true },
  QUALITY_CHANGE: { from: true, qualityChange: true },
  RETURN_TO_SUPPLIER: { from: true },
  SCRAP: { from: true },
};

/**
 * Records one manual movement. The fields follow the chosen type, so a receipt only asks where the
 * supplies went and an issue only asks who received them.
 */
export function MovementDialog({
  open,
  onClose,
  locations,
  item,
}: {
  open: boolean;
  onClose: () => void;
  /** real locations a person can pick (warehouses, benches, …) */
  locations: EntityOption[];
  /** preselect an item, e.g. when opened from a filtered stock view */
  item?: Item | null;
}) {
  const { t } = useI18n();
  const { actor, employees } = useActor();
  const { pending, run } = useServerAction();
  const employeeOptions = employees.map((e) => ({ id: e.id, label: e.name }));
  const required = (message: string) => [{ required: true, message }];

  return (
    <FormModal<MovementInput>
      title={t.inventory.recordMovement}
      open={open}
      pending={pending}
      onClose={onClose}
      onSubmit={(values) => run(() => postMovement(values), t.inventory.movementRecorded, onClose)}
      initialValues={{
        movementType: "RECEIPT",
        itemId: item?.id,
        quality: "GOOD",
        fromQuality: "GOOD",
        toQuality: "DEFECTIVE",
        performedById: actor?.id,
      }}
    >
      <Form.Item name="movementType" label={t.inventory.movementType}>
        <Select
          options={MANUAL_MOVEMENT_TYPES.map((type) => ({ value: type, label: t.status.movement[type] }))}
        />
      </Form.Item>
      <Form.Item noStyle shouldUpdate={(before, after) => before.movementType !== after.movementType}>
        {({ getFieldValue }) => {
          const type: ManualMovementType = getFieldValue("movementType");
          const fields = FIELDS[type];
          return (
            <>
              <p style={{ marginTop: -12, marginBottom: 16, color: "var(--text-muted)", fontSize: 12 }}>
                {t.inventory.typeHint[type]}
              </p>
              <Form.Item name="itemId" label={t.common.item} rules={required(t.inventory.pickItem)}>
                <ItemSelect initial={item} placeholder={t.common.typeToSearch} />
              </Form.Item>
              <Form.Item name="qty" label={t.common.quantity} rules={required(t.inventory.qtyRequired)}>
                <InputNumber min={0.001} precision={3} style={{ width: "100%" }} />
              </Form.Item>
              {fields.from && (
                <Form.Item
                  name="fromLocationId"
                  label={fields.qualityChange ? t.common.location : t.inventory.fromLocation}
                  rules={required(t.inventory.pickLocation)}
                >
                  <EntitySelect options={locations} />
                </Form.Item>
              )}
              {fields.employee && (
                <Form.Item
                  name="employeeId"
                  label={fields.employee === "recipient" ? t.inventory.recipient : t.inventory.returnedBy}
                  rules={required(t.inventory.pickEmployee)}
                >
                  <EntitySelect options={employeeOptions} />
                </Form.Item>
              )}
              {fields.to && (
                <Form.Item name="toLocationId" label={t.inventory.toLocation} rules={required(t.inventory.pickLocation)}>
                  <EntitySelect options={locations} />
                </Form.Item>
              )}
              {fields.qualityChange ? (
                <div style={{ display: "flex", gap: 12 }}>
                  <Form.Item name="fromQuality" label={t.inventory.fromQuality} style={{ flex: 1 }}>
                    <Select options={QUALITY_STATES.map((q) => ({ value: q, label: t.status.quality[q] }))} />
                  </Form.Item>
                  <Form.Item name="toQuality" label={t.inventory.toQuality} style={{ flex: 1 }}>
                    <Select options={QUALITY_STATES.map((q) => ({ value: q, label: t.status.quality[q] }))} />
                  </Form.Item>
                </div>
              ) : (
                <Form.Item name="quality" label={t.inventory.quality}>
                  <Select options={QUALITY_STATES.map((q) => ({ value: q, label: t.status.quality[q] }))} />
                </Form.Item>
              )}
            </>
          );
        }}
      </Form.Item>
      <Form.Item name="performedById" label={t.inventory.performedBy}>
        <EntitySelect allowClear options={employeeOptions} />
      </Form.Item>
      <Form.Item name="note" label={t.inventory.note}>
        <Input />
      </Form.Item>
    </FormModal>
  );
}
