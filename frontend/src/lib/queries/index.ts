import "server-only";

/**
 * Server-side reads, one file per backend module. Import from "@/lib/queries"; this index keeps
 * that path stable as modules are added.
 */
export * from "./expenses";
export * from "./identity";
export * from "./telegram";
