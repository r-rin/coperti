import "server-only";

import { api } from "@/lib/api";
import type {
  AdvanceCoverage,
  Budget,
  Disbursement,
  DisbursementStatus,
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
  uncoveredOnly?: boolean;
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

export const getExpenses = (q: ExpenseQuery = {}) =>
  api.get<PaginatedResponse<Expense>>("/expense", { ...q });

export const getExpense = (id: string) => api.get<Expense>(`/expense/${id}`);

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

/** An advance with the expenses it paid for, joined here because the backend serves them apart. */
export async function getAdvanceCoverage(id: string): Promise<AdvanceCoverage> {
  const [disbursement, remaining, fundings] = await Promise.all([
    getDisbursement(id),
    getRemainingBalance(id),
    getFundingByDisbursement(id),
  ]);
  const expenses = await Promise.all(
    fundings
      .filter((funding) => funding.expenseId)
      .map(async (funding) => {
        const expenseId = funding.expenseId as string;
        const [expense, shortfall] = await Promise.all([getExpense(expenseId), getShortfall(expenseId)]);
        return {
          expenseId,
          date: expense.date,
          description: expense.description,
          amount: expense.amount,
          coveredHere: funding.amountCovered,
          shortfall,
        };
      }),
  );
  expenses.sort((a, b) => (a.date ?? "").localeCompare(b.date ?? ""));
  return { disbursement, remaining, expenses };
}
