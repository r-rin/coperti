"use client";

import { createContext, useContext } from "react";

import type { Employee } from "@/lib/types";

interface ActorContext {
  /** the employee the user is acting as, or null when none is chosen */
  actor: Employee | null;
  /** everyone the user could act as (also handy for employee pickers) */
  employees: Employee[];
}

const Context = createContext<ActorContext>({ actor: null, employees: [] });

export function ActorProvider({ value, children }: { value: ActorContext; children: React.ReactNode }) {
  return <Context.Provider value={value}>{children}</Context.Provider>;
}

/** Forms read this to prefill "performed by", "requested by" and "approved by". */
export function useActor() {
  return useContext(Context);
}
