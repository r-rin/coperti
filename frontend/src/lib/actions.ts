import "server-only";

import { revalidatePath } from "next/cache";

import { ApiError } from "@/lib/api";

/**
 * Shared plumbing for every module's Server Actions (each module keeps its own `actions.ts`
 * with the "use server" directive and calls this). Backend rejections come back as a result
 * the UI can show; anything else is a real failure and is rethrown.
 */

export interface ActionResult<T = void> {
  ok: boolean;
  error?: string;
  data?: T;
}

export async function runAction<T>(work: () => Promise<T>, ...paths: string[]): Promise<ActionResult<T>> {
  let data: T;
  try {
    data = await work();
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
  // one write often shows on several screens at once, so each action names every page it touches
  paths.forEach((path) => revalidatePath(path));
  return { ok: true, data };
}
