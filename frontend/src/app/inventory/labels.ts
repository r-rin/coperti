// Shared by server and client components — no backend imports.

import type { Location, LocationKind } from "@/lib/types";

/** Readable names for the virtual sinks, from the dictionary (their codes are just the kind names). */
export type SinkNames = Partial<Record<LocationKind, string>>;

/**
 * How a location reads in tables and pickers: "W1 · Main warehouse"; the person's current name for an
 * employee location (its stored name is a snapshot from when it was created); "Scrap" for a virtual sink.
 */
export function locationLabel(location: Location, employeeNames: Record<string, string>, sinks: SinkNames): string {
  if (location.employeeId) return employeeNames[location.employeeId] ?? location.name ?? location.code;
  const sink = sinks[location.kind];
  if (sink) return sink;
  return location.name && location.name !== location.code ? `${location.code} · ${location.name}` : location.code;
}

/** id → label for every location, built once per page on the server and passed to client tables. */
export function locationLabels(
  locations: Location[],
  employeeNames: Record<string, string>,
  sinks: SinkNames,
): Record<string, string> {
  return Object.fromEntries(locations.map((location) => [location.id, locationLabel(location, employeeNames, sinks)]));
}
