"use server";

import { type ActionResult, runAction } from "@/lib/actions";
import { api } from "@/lib/api";
import type { LinkCode } from "@/lib/types";

/**
 * Mutations of the People page. Employee names show across the app (expenses, advances, …),
 * so name changes refresh those pages too.
 */

const PEOPLE = "/people";
const EVERYWHERE_NAMES_SHOW = [PEOPLE, "/expenses", "/expenses/list", "/expenses/disbursements"];

export async function createEmployee(name: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.post("/employee", { name });
  }, ...EVERYWHERE_NAMES_SHOW);
}

export async function renameEmployee(id: string, name: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.put("/employee", { id, name });
  }, ...EVERYWHERE_NAMES_SHOW);
}

export async function deleteEmployee(id: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.delete(`/employee/${id}`);
  }, ...EVERYWHERE_NAMES_SHOW);
}

/** The code is returned to show once; it is valid for 15 minutes and works a single time. */
export async function issueLinkCode(employeeId: string): Promise<ActionResult<LinkCode>> {
  return runAction(() => api.post<LinkCode>(`/telegram-account/employee/${employeeId}/link-code`), PEOPLE);
}

export async function linkTelegram(employeeId: string, telegramUserId: number): Promise<ActionResult> {
  return runAction(async () => {
    await api.put("/telegram-account", { employeeId, telegramUserId });
  }, PEOPLE);
}

export async function unlinkTelegram(employeeId: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.delete(`/telegram-account/employee/${employeeId}`);
  }, PEOPLE);
}
