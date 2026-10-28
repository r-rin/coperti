"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";

/**
 * Filters and paging live in the URL, so every filtered view is linkable and applying one is a
 * server re-render with fresh rows. Changing any filter returns to the first page.
 */
export function useUrlParams() {
  const router = useRouter();
  const pathname = usePathname();
  const params = useSearchParams();

  function push(next: URLSearchParams) {
    const qs = next.toString();
    router.push(qs ? `${pathname}?${qs}` : pathname);
  }

  function setFilters(values: Record<string, string | string[] | undefined | null>) {
    const next = new URLSearchParams(params.toString());
    next.delete("page");
    for (const [key, value] of Object.entries(values)) {
      next.delete(key);
      if (Array.isArray(value)) value.filter(Boolean).forEach((entry) => next.append(key, entry));
      else if (value) next.set(key, value);
    }
    push(next);
  }

  /** antd pagination is 1-based, the backend 0-based. */
  function goToPage(oneBased: number) {
    const next = new URLSearchParams(params.toString());
    next.set("page", String(oneBased - 1));
    push(next);
  }

  function reset() {
    router.push(pathname);
  }

  return { params, setFilters, goToPage, reset };
}
