"use client";

import { Select, type SelectProps } from "antd";

export interface EntityOption {
  id: string;
  label: string;
}

/**
 * A searchable picker over a list the page already loaded on the server (employees, locations, …).
 * Works inside an antd Form.Item, which injects value/onChange.
 */
export function EntitySelect({
  options,
  ...props
}: { options: EntityOption[] } & Omit<SelectProps<string>, "options" | "showSearch">) {
  return (
    <Select<string>
      showSearch={{ optionFilterProp: "label" }}
      options={options.map((option) => ({ value: option.id, label: option.label }))}
      {...props}
    />
  );
}
