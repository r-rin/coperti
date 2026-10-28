import "server-only";

import { cookies } from "next/headers";

/**
 * Who is using the app right now. There is no login yet, so the person picks themselves in the
 * sidebar ("Acting as") and forms prefill "performed by", "requested by" and "approved by" with it.
 * When real authentication arrives, this is the one place to switch to the logged-in user.
 */
export const ACTOR_COOKIE = "actor";

export async function getActorId(): Promise<string | null> {
  return (await cookies()).get(ACTOR_COOKIE)?.value || null;
}
