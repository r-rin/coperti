import "server-only";

import { api } from "@/lib/api";
import type {
  Budget,
  Disbursement,
  DisbursementStatus,
  Employee,
  EmployeeBalance,
  Expense,
  Funding,
  PaginatedResponse,
} from "@/lib/types";

export interface ExpenseQuery {
  fromDate?: string;
  toDate?: string;
  search?: string;
  issuedByEmployee?: string;
  page?: number;
  size?: number;
}

export interface DisbursementQuery {
  givenToEmployee?: string;
  fromDate?: string;
  toDate?: string;
  statuses?: DisbursementStatus[];
  page?: number;
  size?: number;
}

export interface BudgetQuery {
  fromDate?: string;
  toDate?: string;
  givenBy?: string;
  search?: string;
  page?: number;
  size?: number;
}

export const getEmployees = () => api.get<Employee[]>("/employee");

export const getExpenses = (q: ExpenseQuery = {}) =>
  api.get<PaginatedResponse<Expense>>("/expense", { ...q });

export const getExpenseSum = (q: ExpenseQuery = {}) =>
  api.get<number>("/expense/sum", { ...q, page: undefined, size: undefined });

export const getDisbursements = (q: DisbursementQuery = {}) =>
  api.get<PaginatedResponse<Disbursement>>("/disbursement", { ...q });

export const getDisbursement = (id: string) => api.get<Disbursement>(`/disbursement/${id}`);

export const getDisbursementSum = (q: DisbursementQuery = {}) =>
  api.get<number>("/disbursement/sum", { ...q, page: undefined, size: undefined });

export const getBudgets = (q: BudgetQuery = {}) =>
  api.get<PaginatedResponse<Budget>>("/budget", { ...q });

export const getBudgetSum = (q: BudgetQuery = {}) =>
  api.get<number>("/budget/sum", { ...q, page: undefined, size: undefined });

export const getFacilityFloat = () => api.get<number>("/budget/float");

/** One row per employee — the dashboard's source for who owes whom. */
export const getEmployeeBalances = () => api.get<EmployeeBalance[]>("/balance/employee");

export const getFundingByDisbursement = (id: string) =>
  api.get<Funding[]>(`/funding/disbursement/${id}`);

export const getFundingByExpense = (id: string) => api.get<Funding[]>(`/funding/expense/${id}`);

export const getRemainingBalance = (id: string) =>
  api.get<number>(`/funding/disbursement/${id}/remaining`);

export const getShortfall = (expenseId: string) =>
  api.get<number>(`/funding/expense/${expenseId}/shortfall`);

/** Name lookup for tables that only carry an employee id. */
export async function employeeNameMap(): Promise<Map<string, string>> {
  const employees = await getEmployees();
  return new Map(employees.map((e) => [e.id, e.name]));
}
