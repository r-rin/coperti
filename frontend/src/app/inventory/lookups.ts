import "server-only";

import { getI18n } from "@/i18n/server";
import { getEmployees, getLocations } from "@/lib/queries";
import { isVirtualKind, type Location } from "@/lib/types";
import { locationLabels } from "./labels";

/**
 * Names every inventory page needs to turn ids into text: locations (all kinds), employees, and the
 * real locations a person may pick in forms (not virtual sinks, not anyone's hands).
 */
export async function inventoryLookups() {
  const [locations, employees, { t }] = await Promise.all([getLocations(), getEmployees(), getI18n()]);
  const employeeNames = Object.fromEntries(employees.map((e) => [e.id, e.name]));
  const labels = locationLabels(locations, employeeNames, t.inventory.sink);
  const pickable = (location: Location) => !isVirtualKind(location.kind) && location.kind !== "EMPLOYEE";

  return {
    locations,
    employeeNames,
    locationNames: labels,
    pickableLocations: locations.filter(pickable).map((location) => ({ id: location.id, label: labels[location.id] })),
  };
}

export function one(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export function many(value: string | string[] | undefined): string[] {
  return value === undefined ? [] : Array.isArray(value) ? value : [value];
}
