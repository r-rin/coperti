import "server-only";

import { api } from "@/lib/api";
import type { Item } from "@/lib/types";

/** Items for the ids a table carries, keyed by id; one request however many rows. */
export async function itemsById(ids: Iterable<string>): Promise<Map<string, Item>> {
  const unique = [...new Set(ids)];
  if (!unique.length) return new Map();
  const items = await api.get<Item[]>("/item/by-ids", { ids: unique });
  return new Map(items.map((item) => [item.id, item]));
}
