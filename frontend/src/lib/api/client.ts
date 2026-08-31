import { ApiErrorResponse, AuthResponse } from "@/types";
import { useAuthStore } from "@/stores/auth-store";

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined | null>;
  skipAuth?: boolean;
  _retry?: boolean;
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
  const configuredBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
  let baseUrl: string;
  if (configuredBaseUrl !== undefined && configuredBaseUrl !== null && configuredBaseUrl !== "") {
    baseUrl = configuredBaseUrl;
  } else if (typeof window !== "undefined") {
    baseUrl = window.location.origin;
  } else {
    baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:8080";
  }

  const url = new URL(path.startsWith("http") ? path : `${baseUrl}${path.startsWith("/") ? "" : "/"}${path}`);

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.append(key, String(value));
      }
    });
  }

  return url.toString();
}

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
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

  // Add JWT Token from Zustand in-memory store if available and not skipped
  if (!options.skipAuth) {
    const token = useAuthStore.getState().accessToken;
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
      credentials: "same-origin",
      ...options,
      headers,
    });

    if (response.status === 204) {
      return {} as T;
    }

    // Handle 401 Unauthorized for authenticated endpoints
    if (response.status === 401 && !options.skipAuth && !options._retry && !endpoint.includes("/auth/login") && !endpoint.includes("/auth/refresh")) {
      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const refreshUrl = buildUrl("/api/v1/auth/refresh");
          const refreshRes = await fetch(refreshUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
          });

          if (refreshRes.ok) {
            const authData: AuthResponse = await refreshRes.json();
            useAuthStore.getState().setAuth(authData);
            isRefreshing = false;
            onRefreshed(authData.accessToken);
            return apiClient<T>(endpoint, { ...options, _retry: true });
          } else {
            isRefreshing = false;
            useAuthStore.getState().logout();
          }
        } catch {
          isRefreshing = false;
          useAuthStore.getState().logout();
        }
      } else {
        // Wait for token refresh to complete
        return new Promise<T>((resolve, reject) => {
          subscribeTokenRefresh((newToken) => {
            const retryHeaders = new Headers(options.headers || {});
            retryHeaders.set("Authorization", `Bearer ${newToken}`);
            apiClient<T>(endpoint, { ...options, headers: retryHeaders, _retry: true })
              .then(resolve)
              .catch(reject);
          });
        });
      }
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
