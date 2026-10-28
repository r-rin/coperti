export type LocationKind =
  | "WAREHOUSE"
  | "EMPLOYEE"
  | "WIP"
  | "REPAIR"
  | "QUARANTINE"
  | "FINISHED_GOODS"
  | "V_SUPPLIER"
  | "V_CONSUMED"
  | "V_SCRAP"
  | "V_OTHER_USE";

/** Kinds a person creates by hand; employee and virtual locations are created by the backend on demand. */
export const MANUAL_LOCATION_KINDS: LocationKind[] = ["WAREHOUSE", "WIP", "REPAIR", "QUARANTINE", "FINISHED_GOODS"];

export const isVirtualKind = (kind: LocationKind) => kind.startsWith("V_");

export type QualityState = "GOOD" | "DEFECTIVE" | "QUARANTINED";

export const QUALITY_STATES: QualityState[] = ["GOOD", "DEFECTIVE", "QUARANTINED"];

export type MovementType =
  | "RECEIPT"
  | "ISSUE_TO_EMPLOYEE"
  | "RETURN_FROM_EMPLOYEE"
  | "CONSUME"
  | "PRODUCE"
  | "TRANSFER"
  | "QUALITY_CHANGE"
  | "SCRAP"
  | "RETURN_TO_SUPPLIER"
  | "OTHER_USE"
  | "RECOVER_FROM_TEARDOWN";

export const MOVEMENT_TYPES: MovementType[] = [
  "RECEIPT",
  "ISSUE_TO_EMPLOYEE",
  "RETURN_FROM_EMPLOYEE",
  "TRANSFER",
  "QUALITY_CHANGE",
  "SCRAP",
  "RETURN_TO_SUPPLIER",
  "CONSUME",
  "PRODUCE",
  "OTHER_USE",
  "RECOVER_FROM_TEARDOWN",
];

/** What the backend accepts on its public movement endpoint; the rest are posted by the services that own them. */
export const MANUAL_MOVEMENT_TYPES = [
  "RECEIPT",
  "TRANSFER",
  "ISSUE_TO_EMPLOYEE",
  "RETURN_FROM_EMPLOYEE",
  "QUALITY_CHANGE",
  "RETURN_TO_SUPPLIER",
  "SCRAP",
] as const satisfies readonly MovementType[];

export type ManualMovementType = (typeof MANUAL_MOVEMENT_TYPES)[number];

export interface Location {
  id: string;
  code: string;
  name: string | null;
  kind: LocationKind;
  employeeId: string | null;
}

export interface StockBalance {
  itemId: string;
  locationId: string;
  quality: QualityState;
  qty: number;
}

export interface StockMovement {
  id: string;
  itemId: string;
  qty: number;
  fromLocationId: string;
  toLocationId: string;
  fromQuality: QualityState;
  toQuality: QualityState;
  movementType: MovementType;
  occurredAt: string;
  performedById: string | null;
  workOrderId: string | null;
  stepId: string | null;
  otherUseId: string | null;
  reversalOfId: string | null;
  note: string | null;
}

export type OtherUseStatus = "PENDING" | "APPROVED" | "REJECTED";

export const OTHER_USE_STATUSES: OtherUseStatus[] = ["PENDING", "APPROVED", "REJECTED"];

export interface OtherUseLine {
  id: string;
  itemId: string;
  fromLocationId: string;
  quality: QualityState;
  qty: number;
}

export interface OtherUse {
  id: string;
  requestedById: string;
  approvedById: string | null;
  reason: string | null;
  requestedAt: string;
  occurredAt: string | null;
  status: OtherUseStatus;
  lines: OtherUseLine[];
}
