import "server-only";

import { api } from "@/lib/api";
import type {
  Location,
  LocationKind,
  MovementType,
  OtherUse,
  OtherUseStatus,
  PaginatedResponse,
  StockBalance,
  StockMovement,
} from "@/lib/types";

export interface MovementQuery {
  itemId?: string;
  locationId?: string;
  types?: MovementType[];
  performedById?: string;
  /** ISO instants */
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface OtherUseQuery {
  status?: OtherUseStatus;
  requestedById?: string;
  page?: number;
  size?: number;
}

export const getLocations = (kind?: LocationKind) => api.get<Location[]>("/location", { kind });

/** Non-zero balances, narrowed by item, location or the employee's own location. */
export const getBalances = (q: { itemId?: string; locationId?: string; employeeId?: string } = {}) =>
  api.get<StockBalance[]>("/stock-balance", { ...q });

/** GOOD warehouse stock minus reservations of open work orders. */
export const getAvailable = (itemId: string) => api.get<number>(`/stock-balance/available/${itemId}`);

export const getMovements = (q: MovementQuery = {}) =>
  api.get<PaginatedResponse<StockMovement>>("/stock-movement", { ...q });

export const getOtherUses = (q: OtherUseQuery = {}) =>
  api.get<PaginatedResponse<OtherUse>>("/other-use", { ...q });
