import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  Address,
  CreateAddressRequest,
  UpdateAddressRequest,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from "@/types";

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    return apiClient<AuthResponse>(API_ENDPOINTS.AUTH_LOGIN, {
      method: "POST",
      body: JSON.stringify(request),
      skipAuth: true,
    });
  },

  register: async (request: RegisterRequest): Promise<AuthResponse> => {
    return apiClient<AuthResponse>(API_ENDPOINTS.AUTH_REGISTER, {
      method: "POST",
      body: JSON.stringify(request),
      skipAuth: true,
    });
  },

  getMe: async (): Promise<User> => {
    return apiClient<User>(API_ENDPOINTS.AUTH_ME);
  },

  updateProfile: async (request: UpdateProfileRequest): Promise<User> => {
    return apiClient<User>(API_ENDPOINTS.USER_PROFILE, {
      method: "PUT",
      body: JSON.stringify(request),
    });
  },

  changePassword: async (request: ChangePasswordRequest): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.USER_PASSWORD, {
      method: "PATCH",
      body: JSON.stringify(request),
    });
  },

  getAddresses: async (): Promise<Address[]> => {
    return apiClient<Address[]>(API_ENDPOINTS.USER_ADDRESSES);
  },

  getAddressById: async (id: string): Promise<Address> => {
    return apiClient<Address>(API_ENDPOINTS.USER_ADDRESS_BY_ID(id));
  },

  createAddress: async (request: CreateAddressRequest): Promise<Address> => {
    return apiClient<Address>(API_ENDPOINTS.USER_ADDRESSES, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  updateAddress: async (id: string, request: UpdateAddressRequest): Promise<Address> => {
    return apiClient<Address>(API_ENDPOINTS.USER_ADDRESS_BY_ID(id), {
      method: "PUT",
      body: JSON.stringify(request),
    });
  },

  deleteAddress: async (id: string): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.USER_ADDRESS_BY_ID(id), {
      method: "DELETE",
    });
  },

  setDefaultAddress: async (
    id: string,
    params: { defaultDelivery?: boolean; defaultBilling?: boolean }
  ): Promise<Address> => {
    return apiClient<Address>(API_ENDPOINTS.USER_ADDRESS_DEFAULT(id), {
      method: "PATCH",
      params: {
        defaultDelivery: params.defaultDelivery,
        defaultBilling: params.defaultBilling,
      },
    });
  },
};
