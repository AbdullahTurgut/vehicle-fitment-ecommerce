import { ApiErrorResponse } from "@/types";

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined | null>;
  skipAuth?: boolean;
}

export class ApiError extends Error {
  status: number;
  code: string;
  errors: Record<string, string> | null;

  constructor(errorResponse: ApiErrorResponse) {
    super(errorResponse.message || "Bir hata oluştu.");
    this.name = "ApiError";
    this.status = errorResponse.status;
    this.code = errorResponse.code || "UNKNOWN_ERROR";
    this.errors = errorResponse.errors || null;
  }
}

function getStoredAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return localStorage.getItem("carmats_access_token");
  } catch {
    return null;
  }
}

function getStoredGuestToken(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return localStorage.getItem("carmats_guest_token");
  } catch {
    return null;
  }
}

export function buildUrl(
  path: string,
  params?: Record<string, string | number | boolean | undefined | null>
): string {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
  const url = new URL(path.startsWith("http") ? path : `${baseUrl}${path}`);

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.append(key, String(value));
      }
    });
  }

  return url.toString();
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const url = buildUrl(endpoint, options.params);

  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  // Add JWT Token if available and not skipped
  if (!options.skipAuth) {
    const token = getStoredAccessToken();
    if (token && !headers.has("Authorization")) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  // Add Guest Token if available
  const guestToken = getStoredGuestToken();
  if (guestToken && !headers.has("X-Guest-Token")) {
    headers.set("X-Guest-Token", guestToken);
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (response.status === 204) {
      return {} as T;
    }

    if (!response.ok) {
      let errorData: ApiErrorResponse;
      try {
        errorData = await response.json();
      } catch {
        errorData = {
          status: response.status,
          code: "HTTP_ERROR",
          message: response.statusText || "Sunucu hatası oluştu.",
          errors: null,
          timestamp: new Date().toISOString(),
        };
      }
      throw new ApiError(errorData);
    }

    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError({
      status: 0,
      code: "NETWORK_ERROR",
      message: (error as Error).message || "Sunucuya bağlanılamadı. Lütfen bağlantınızı kontrol edin.",
      errors: null,
      timestamp: new Date().toISOString(),
    });
  }
}
