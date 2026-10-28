import "server-only";

import { api } from "@/lib/api";
import type { Employee } from "@/lib/types";

export const getEmployees = () => api.get<Employee[]>("/employee");

/** Name lookup for tables that only carry an employee id. */
export async function employeeNameMap(): Promise<Map<string, string>> {
  const employees = await getEmployees();
  return new Map(employees.map((e) => [e.id, e.name]));
}
