"use server";

import { cookies } from "next/headers";

import { ACTOR_COOKIE } from "@/lib/actor";

/** Setting the cookie in a Server Action re-renders the page, so every form picks up the new actor. */
export async function setActor(employeeId: string | null): Promise<void> {
  const jar = await cookies();
  if (employeeId) {
    jar.set(ACTOR_COOKIE, employeeId, { path: "/", maxAge: 60 * 60 * 24 * 365, sameSite: "lax" });
  } else {
    jar.delete(ACTOR_COOKIE);
  }
}
