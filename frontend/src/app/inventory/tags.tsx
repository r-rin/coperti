"use client";

import { StatusTag, type Tone } from "@/components/ui/StatusTag";
import { useI18n } from "@/i18n/I18nProvider";
import type { LocationKind, MovementType, OtherUseStatus, QualityState } from "@/lib/types";

const QUALITY_TONE: Record<QualityState, Tone> = { GOOD: "good", DEFECTIVE: "critical", QUARANTINED: "warning" };

// stock arriving reads differently from stock leaving or being lost
const MOVEMENT_TONE: Record<MovementType, Tone> = {
  RECEIPT: "good",
  PRODUCE: "good",
  RECOVER_FROM_TEARDOWN: "good",
  ISSUE_TO_EMPLOYEE: "active",
  RETURN_FROM_EMPLOYEE: "active",
  TRANSFER: "neutral",
  QUALITY_CHANGE: "warning",
  CONSUME: "neutral",
  OTHER_USE: "warning",
  RETURN_TO_SUPPLIER: "warning",
  SCRAP: "critical",
};

const OTHER_USE_TONE: Record<OtherUseStatus, Tone> = { PENDING: "active", APPROVED: "good", REJECTED: "neutral" };

export function QualityTag({ quality }: { quality: QualityState }) {
  const { t } = useI18n();
  return <StatusTag label={t.status.quality[quality]} tone={QUALITY_TONE[quality]} />;
}

export function MovementTag({ type }: { type: MovementType }) {
  const { t } = useI18n();
  return <StatusTag label={t.status.movement[type]} tone={MOVEMENT_TONE[type]} />;
}

export function OtherUseTag({ status }: { status: OtherUseStatus }) {
  const { t } = useI18n();
  return <StatusTag label={t.status.otherUse[status]} tone={OTHER_USE_TONE[status]} />;
}

export function LocationKindTag({ kind }: { kind: LocationKind }) {
  const { t } = useI18n();
  return <StatusTag label={t.status.locationKind[kind]} tone={kind.startsWith("V_") ? "neutral" : "active"} />;
}
