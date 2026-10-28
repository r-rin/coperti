"use client";

import { StatusTag } from "@/components/ui/StatusTag";
import { useI18n } from "@/i18n/I18nProvider";
import type { DisbursementStatus, ItemType, LocationKind, MovementType, OtherUseStatus, QualityState } from "@/lib/types";

import { STATUS_TONE } from "../../expenses/disbursements/DisbursementTable";
import { LocationKindTag, MovementTag, OtherUseTag, QualityTag } from "../../inventory/tags";

export type GlossaryGroup = "advance" | "otherUse" | "quality" | "movement" | "location" | "itemType" | "telegram";

/**
 * A value drawn exactly as the app draws it — same component, same colour, same label — so the glossary
 * reads as "this tag means …". A client component because those tags are.
 */
export function GlossaryTag({ group, value }: { group: GlossaryGroup; value: string }) {
  const { t } = useI18n();
  switch (group) {
    case "advance":
      return (
        <StatusTag
          label={t.status.disbursement[value as DisbursementStatus]}
          tone={STATUS_TONE[value as DisbursementStatus]}
        />
      );
    case "otherUse":
      return <OtherUseTag status={value as OtherUseStatus} />;
    case "quality":
      return <QualityTag quality={value as QualityState} />;
    case "movement":
      return <MovementTag type={value as MovementType} />;
    case "location":
      return <LocationKindTag kind={value as LocationKind} />;
    case "itemType":
      return <StatusTag label={t.status.itemType[value as ItemType]} />;
    case "telegram":
      return value === "LINKED" ? (
        <StatusTag label={t.people.linkedAs(123456789)} tone="good" />
      ) : (
        <StatusTag label={t.people.notLinked} />
      );
  }
}
