"use server";

import { ApiError } from "@/lib/api";
import { getAdvanceCoverage } from "@/lib/queries";
import type { AdvanceCoverage } from "@/lib/types";

/**
 * Reads a Client Component triggers on demand (expanding a row, building a report).
 * Kept apart from actions.ts, which holds only mutations. One call fans out on the server,
 * since the client awaits Server Functions one at a time.
 */

export type CoverageResult = { ok: true; data: AdvanceCoverage[] } | { ok: false; error: string };

export async function loadCoverage(ids: string[]): Promise<CoverageResult> {
  try {
    return { ok: true, data: await Promise.all(ids.map(getAdvanceCoverage)) };
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
}
