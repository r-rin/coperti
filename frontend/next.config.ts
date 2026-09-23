import type { NextConfig } from "next";

/**
 * There is deliberately no rewrite to the backend.
 *
 * The browser must never reach Spring, not even through a proxy on this origin, so
 * BACKEND_URL is read only by `src/lib/api.ts` — which is marked `server-only` and
 * therefore cannot be imported into a Client Component. Reads happen while rendering
 * on the server; writes go through the Server Actions in `src/app/expenses/actions.ts`.
 */
const nextConfig: NextConfig = {
  output: "standalone",
};

export default nextConfig;
