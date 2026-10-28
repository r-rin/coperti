"use client";

import { Select, type SelectProps } from "antd";
import { useEffect, useRef, useState } from "react";

import { useI18n } from "@/i18n/I18nProvider";
import type { Item } from "@/lib/types";

const DEBOUNCE_MS = 250;

/**
 * Type-ahead item picker: the catalog can be large, so it searches by name through /lookup/items
 * instead of loading every item. `initial` labels a value chosen earlier (e.g. when editing).
 */
export function ItemSelect({
  initial,
  onItem,
  ...props
}: {
  initial?: Item | null;
  /** the whole item, for callers that need more than its id */
  onItem?: (item: Item | undefined) => void;
} & Omit<SelectProps<string>, "options" | "showSearch" | "loading">) {
  const { t } = useI18n();
  const [items, setItems] = useState<Item[]>(initial ? [initial] : []);
  const [loading, setLoading] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const latest = useRef(0);

  useEffect(() => () => clearTimeout(timer.current), []);

  function search(text: string) {
    clearTimeout(timer.current);
    if (!text.trim()) return;
    timer.current = setTimeout(async () => {
      // only the newest request may update the list, so a slow early reply can't overwrite a later one
      const ticket = ++latest.current;
      setLoading(true);
      try {
        const response = await fetch(`/lookup/items?search=${encodeURIComponent(text.trim())}`);
        const found: Item[] = response.ok ? await response.json() : [];
        if (ticket === latest.current) setItems(found);
      } finally {
        if (ticket === latest.current) setLoading(false);
      }
    }, DEBOUNCE_MS);
  }

  return (
    <Select<string>
      showSearch={{ filterOption: false, onSearch: search }}
      loading={loading}
      notFoundContent={loading ? null : t.common.typeToSearch}
      options={items.map((item) => ({ value: item.id, label: item.name }))}
      {...props}
      onChange={(value, option) => {
        props.onChange?.(value, option);
        onItem?.(items.find((item) => item.id === value));
      }}
    />
  );
}
