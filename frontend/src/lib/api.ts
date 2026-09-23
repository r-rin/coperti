import "server-only";

/**
 * The only place that knows the backend exists.
 *
 * `server-only` makes importing this module from a Client Component a build
 * error, which is what guarantees the browser never talks to Spring: every
 * read happens while rendering on the server, every write through a Server Action.
 */

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

/**
 * GlobalExceptionHandler writes the message list with List.toString(), so a failure
 * body looks like `[Amount must be greater than 0, Employee id is required]`.
 */
function readErrorBody(body: string, fallback: string): string {
  const trimmed = body.trim();
  if (!trimmed) return fallback;
  if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
    const inner = trimmed.slice(1, -1).trim();
    if (inner) return inner;
  }
  return trimmed;
}

type Query = Record<string, string | number | boolean | string[] | undefined | null>;

export function toQueryString(query: Query = {}): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      // Spring binds a repeated parameter into Set<Enum>, e.g. statuses=OPEN&statuses=CLOSED
      value.filter(Boolean).forEach((entry) => params.append(key, entry));
    } else {
      params.append(key, String(value));
    }
  }
  const qs = params.toString();
  return qs ? `?${qs}` : "";
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${BACKEND_URL}/api${path}`, {
      ...init,
      headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
      // ERP figures change under the user's feet; never serve a stale ledger
      cache: "no-store",
    });
  } catch {
    throw new ApiError("Cannot reach the backend. Is it running on " + BACKEND_URL + "?", 503);
  }

  const body = await response.text();
  if (!response.ok) {
    throw new ApiError(readErrorBody(body, response.statusText), response.status);
  }
  if (!body) return undefined as T;

  try {
    return JSON.parse(body) as T;
  } catch {
    // /sum and /float return a bare number, which is still valid JSON — this is a real failure
    throw new ApiError(`Malformed response from ${path}`, 502);
  }
}

export const api = {
  get: <T>(path: string, query?: Query) => request<T>(`${path}${toQueryString(query)}`),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body === undefined ? undefined : JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
