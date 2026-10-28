import { type NextRequest, NextResponse } from "next/server";

import { ApiError, api } from "@/lib/api";
import type { Item } from "@/lib/types";

/**
 * Type-ahead item search for client pickers. A GET Route Handler rather than a Server Action:
 * actions are dispatched one at a time, while lookups fire on every keystroke.
 */
export async function GET(request: NextRequest) {
  const search = request.nextUrl.searchParams.get("search")?.trim() ?? "";
  if (!search) return NextResponse.json([]);
  try {
    return NextResponse.json(await api.get<Item[]>("/item", { search }));
  } catch (error) {
    if (error instanceof ApiError) return NextResponse.json({ error: error.message }, { status: error.status });
    throw error;
  }
}
