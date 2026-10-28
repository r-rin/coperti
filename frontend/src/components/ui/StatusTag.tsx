"use client";

import { Tag } from "antd";

/** Status colours by meaning, so the same state looks the same in every module. */
export type Tone = "neutral" | "active" | "good" | "warning" | "critical";

const COLOR: Record<Tone, string | undefined> = {
  neutral: undefined,
  active: "processing",
  good: "success",
  warning: "warning",
  critical: "error",
};

export function StatusTag({ label, tone = "neutral" }: { label: string; tone?: Tone }) {
  return (
    <Tag color={COLOR[tone]} style={{ marginInlineEnd: 0 }}>
      {label}
    </Tag>
  );
}
