"use server";

import { revalidatePath } from "next/cache";

import { ApiError, api } from "@/lib/api";
import type {
  AllocationResponse,
  AllocationSelection,
  DisbursementStatus,
  SettlementResponse,
} from "@/lib/types";

/**
 * Every mutation the UI can perform. These run on the Next server and are the only
 * writers to the backend — a Client Component imports the function, not the API.
 */

export interface ActionResult {
  ok: boolean;
  error?: string;
}

async function run(work: () => Promise<unknown>, ...paths: string[]): Promise<ActionResult> {
  try {
    await work();
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
  // the ledger touches several screens at once: an allocation changes an advance,
  // an expense's coverage and the facility float together
  paths.forEach((path) => revalidatePath(path));
  return { ok: true };
}

const ALL = ["/expenses", "/expenses/list", "/expenses/disbursements", "/expenses/budgets"];

// ---------------------------------------------------------------- employees

export async function createEmployee(name: string): Promise<ActionResult> {
  return run(() => api.post("/employee", { name }), ...ALL);
}

// ---------------------------------------------------------------- expenses

export interface ExpenseInput {
  id?: string;
  employeeId: string;
  amount: number;
  date: string;
  description?: string;
}

export async function createExpense(input: ExpenseInput): Promise<ActionResult> {
  return run(() => api.post("/expense", input), ...ALL);
}

export async function updateExpense(input: ExpenseInput): Promise<ActionResult> {
  return run(() => api.put("/expense", input), ...ALL);
}

export async function deleteExpense(id: string): Promise<ActionResult> {
  return run(() => api.delete(`/expense/${id}`), ...ALL);
}

// ---------------------------------------------------------------- disbursements

export interface DisbursementInput {
  employeeId: string;
  amount: number;
  date: string;
}

export async function createDisbursement(input: DisbursementInput): Promise<ActionResult> {
  return run(() => api.post("/disbursement", input), ...ALL);
}

export async function updateDisbursementStatus(
  id: string,
  status: DisbursementStatus,
): Promise<ActionResult> {
  return run(() => api.put("/disbursement/status", { id, status }), ...ALL);
}

// ---------------------------------------------------------------- allocation

export async function allocate(
  disbursementId: string,
  selections: AllocationSelection[],
): Promise<ActionResult & { result?: AllocationResponse }> {
  try {
    const result = await api.post<AllocationResponse>("/allocation", { disbursementId, selections });
    ALL.forEach((path) => revalidatePath(path));
    revalidatePath(`/expenses/disbursements/${disbursementId}`);
    return { ok: true, result };
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
}

// ---------------------------------------------------------------- settlement

export interface SettlementInput {
  employeeId: string;
  /** new cash handed over now; the worker's unspent advances are spent first either way */
  amount: number;
  date: string;
  expenseIds: string[];
}

export async function settle(
  input: SettlementInput,
): Promise<ActionResult & { result?: SettlementResponse }> {
  try {
    const result = await api.post<SettlementResponse>("/settlement", input);
    ALL.forEach((path) => revalidatePath(path));
    return { ok: true, result };
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
}

// ---------------------------------------------------------------- budgets

export interface BudgetInput {
  amount: number;
  givenBy?: string;
  description?: string;
  date: string;
}

export async function createBudget(input: BudgetInput): Promise<ActionResult> {
  return run(() => api.post("/budget", input), ...ALL);
}
