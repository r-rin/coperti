"use server";

import { type ActionResult, runAction } from "@/lib/actions";
import { api } from "@/lib/api";
import type { Location, LocationKind, ManualMovementType, QualityState } from "@/lib/types";

/**
 * Mutations of the inventory pages. A stock movement changes balances, the ledger and other-use
 * screens at once, so every write refreshes the whole module.
 */

const INVENTORY = ["/inventory", "/inventory/movements", "/inventory/locations", "/inventory/other-use"];

// ---------------------------------------------------------------- locations

export interface LocationInput {
  id?: string;
  code: string;
  name?: string;
  kind: LocationKind;
}

export async function createLocation(input: LocationInput): Promise<ActionResult> {
  return runAction(async () => {
    await api.post("/location", input);
  }, ...INVENTORY);
}

export async function updateLocation(input: LocationInput): Promise<ActionResult> {
  return runAction(async () => {
    await api.put("/location", input);
  }, ...INVENTORY);
}

export async function deleteLocation(id: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.delete(`/location/${id}`);
  }, ...INVENTORY);
}

// ---------------------------------------------------------------- movements

/**
 * What a person fills in. The ends a person never picks — the supplier, the scrap bin, an employee's
 * own location — are resolved here, so the form only asks for what the person actually knows.
 */
export interface MovementInput {
  movementType: ManualMovementType;
  itemId: string;
  qty: number;
  /** the stock's quality on both ends; QUALITY_CHANGE uses fromQuality/toQuality instead */
  quality?: QualityState;
  fromQuality?: QualityState;
  toQuality?: QualityState;
  fromLocationId?: string;
  toLocationId?: string;
  /** recipient of an issue, or who returns stock */
  employeeId?: string;
  performedById?: string;
  note?: string;
}

const virtual = async (kind: LocationKind) => (await api.get<Location>(`/location/virtual/${kind}`)).id;
const employeeLocation = async (employeeId: string) =>
  (await api.get<Location>(`/location/employee/${employeeId}`)).id;

async function ends(input: MovementInput): Promise<{ from: string | undefined; to: string | undefined }> {
  switch (input.movementType) {
    case "RECEIPT":
      return { from: await virtual("V_SUPPLIER"), to: input.toLocationId };
    case "RETURN_TO_SUPPLIER":
      return { from: input.fromLocationId, to: await virtual("V_SUPPLIER") };
    case "SCRAP":
      return { from: input.fromLocationId, to: await virtual("V_SCRAP") };
    case "ISSUE_TO_EMPLOYEE":
      return { from: input.fromLocationId, to: input.employeeId && (await employeeLocation(input.employeeId)) };
    case "RETURN_FROM_EMPLOYEE":
      return { from: input.employeeId && (await employeeLocation(input.employeeId)), to: input.toLocationId };
    case "QUALITY_CHANGE":
      // in place: one location, the quality is what changes
      return { from: input.fromLocationId, to: input.fromLocationId };
    case "TRANSFER":
      return { from: input.fromLocationId, to: input.toLocationId };
  }
}

export async function postMovement(input: MovementInput): Promise<ActionResult> {
  return runAction(async () => {
    const { from, to } = await ends(input);
    const qualityChange = input.movementType === "QUALITY_CHANGE";
    await api.post("/stock-movement", {
      movementType: input.movementType,
      itemId: input.itemId,
      qty: input.qty,
      fromLocationId: from,
      toLocationId: to,
      fromQuality: qualityChange ? input.fromQuality : input.quality,
      toQuality: qualityChange ? input.toQuality : input.quality,
      performedById: input.performedById,
      note: input.note,
    });
  }, ...INVENTORY);
}

export async function reverseMovement(id: string, performedById?: string, note?: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.post(`/stock-movement/${id}/reverse`, { performedById, note });
  }, ...INVENTORY);
}

/** Maintenance: recompute the balance cache from the ledger. */
export async function rebuildBalances(): Promise<ActionResult> {
  return runAction(async () => {
    await api.post("/stock-balance/rebuild");
  }, ...INVENTORY);
}

// ---------------------------------------------------------------- other use

export interface OtherUseInput {
  requestedById: string;
  reason?: string;
  lines: { itemId: string; fromLocationId: string; quality?: QualityState; qty: number }[];
}

export async function createOtherUse(input: OtherUseInput): Promise<ActionResult> {
  return runAction(async () => {
    await api.post("/other-use", input);
  }, ...INVENTORY);
}

export async function approveOtherUse(id: string, employeeId: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.post(`/other-use/${id}/approve`, { employeeId });
  }, ...INVENTORY);
}

export async function rejectOtherUse(id: string, employeeId: string): Promise<ActionResult> {
  return runAction(async () => {
    await api.post(`/other-use/${id}/reject`, { employeeId });
  }, ...INVENTORY);
}
