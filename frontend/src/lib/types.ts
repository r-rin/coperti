/**
 * Mirrors of the backend response DTOs. Money arrives as a JSON number (Jackson
 * serialises BigDecimal unquoted); these figures are small enough that float
 * display is safe, but never do arithmetic on them here — the backend owns the maths.
 */

export type DisbursementStatus = "OPEN" | "CLOSED" | "CANCELLED";

export const DISBURSEMENT_STATUSES: DisbursementStatus[] = ["OPEN", "CLOSED", "CANCELLED"];

export interface PaginatedResponse<T> {
  data: T[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
  pageSize: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export interface Employee {
  id: string;
  name: string;
}

export interface Expense {
  id: string;
  employeeId: string | null;
  amount: number;
  date: string | null;
  description: string | null;
}

export interface Disbursement {
  id: string;
  employeeId: string;
  amount: number;
  date: string | null;
  status: DisbursementStatus;
}

export interface Budget {
  id: string;
  amount: number;
  givenBy: string | null;
  description: string | null;
  date: string | null;
}

export interface Funding {
  id: string;
  disbursementId: string | null;
  expenseId: string | null;
  amountCovered: number;
}

export interface AllocationSelection {
  expenseId: string;
  amount: number;
}

export interface AllocationResponse {
  fundings: Funding[];
  remainingBalance: number;
  disbursementStatus: DisbursementStatus;
}

/**
 * Where the facility stands with one employee. `owed` is the uncovered part of their expenses;
 * `unspent` is what is left on their open advances.
 */
export interface EmployeeBalance {
  employeeId: string;
  employeeName: string;
  spent: number;
  covered: number;
  owed: number;
  advanced: number;
  unspent: number;
}

export interface SettlementResponse {
  fundings: Funding[];
  fromAdvances: number;
  fromNewCash: number;
  newDisbursementId: string | null;
  leftOnNewAdvance: number;
  stillOwed: number;
}
