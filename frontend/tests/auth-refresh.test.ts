import { describe, it, expect, beforeEach } from "vitest";
import { useAuthStore } from "@/stores/auth-store";

describe("Auth Store & In-Memory Token Handling", () => {
  beforeEach(() => {
    useAuthStore.getState().logout();
  });

  it("stores access token in memory only", () => {
    expect(useAuthStore.getState().accessToken).toBeNull();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);

    const mockAuthResponse = {
      accessToken: "mock-access-token-jwt",
      refreshToken: "mock-refresh-token",
      tokenType: "Bearer",
      expiresIn: 86400000,
      user: {
        id: "user-123",
        email: "test@carmats.local",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_CUSTOMER"],
      },
    };

    useAuthStore.getState().setAuth(mockAuthResponse);
    expect(useAuthStore.getState().accessToken).toBe("mock-access-token-jwt");
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
  });

  it("sets and clears user profile on login and logout", () => {
    const mockAuthResponse = {
      accessToken: "mock-token-xyz",
      refreshToken: "mock-refresh-token",
      tokenType: "Bearer",
      expiresIn: 86400000,
      user: {
        id: "user-123",
        email: "test@carmats.local",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_CUSTOMER"],
      },
    };

    useAuthStore.getState().setAuth(mockAuthResponse);
    expect(useAuthStore.getState().user?.email).toBe("test@carmats.local");
    expect(useAuthStore.getState().accessToken).toBe("mock-token-xyz");
    expect(useAuthStore.getState().isAuthenticated).toBe(true);

    useAuthStore.getState().logout();
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().accessToken).toBeNull();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });

  it("correctly identifies admin role", () => {
    const adminAuthResponse = {
      accessToken: "token-admin",
      refreshToken: "mock-refresh-token",
      tokenType: "Bearer",
      expiresIn: 86400000,
      user: {
        id: "admin-1",
        email: "admin@carmats.local",
        firstName: "Admin",
        lastName: "Master",
        roles: ["ROLE_ADMIN"],
      },
    };

    useAuthStore.getState().setAuth(adminAuthResponse);
    expect(useAuthStore.getState().isAdmin()).toBe(true);

    const customerAuthResponse = {
      ...adminAuthResponse,
      user: {
        ...adminAuthResponse.user,
        roles: ["ROLE_CUSTOMER"],
      },
    };

    useAuthStore.getState().setAuth(customerAuthResponse);
    expect(useAuthStore.getState().isAdmin()).toBe(false);
  });
});
