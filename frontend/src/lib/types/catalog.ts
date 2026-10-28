export type ItemType = "PART" | "SEMI_FINISHED" | "FINISHED";

export const ITEM_TYPES: ItemType[] = ["PART", "SEMI_FINISHED", "FINISHED"];

export interface Item {
  id: string;
  name: string;
  categoryId: string | null;
  type: ItemType | null;
}
