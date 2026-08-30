export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface Address {
  id: string;
  title: string;
  recipientName: string;
  phoneNumber: string;
  city: string;
  district: string;
  neighborhood?: string;
  fullAddress: string;
  postalCode?: string;
  defaultDelivery: boolean;
  defaultBilling: boolean;
}

export interface CreateAddressRequest {
  title: string;
  recipientName: string;
  phoneNumber: string;
  city: string;
  district: string;
  neighborhood?: string;
  fullAddress: string;
  postalCode?: string;
  defaultDelivery?: boolean;
  defaultBilling?: boolean;
}

export interface UpdateAddressRequest extends CreateAddressRequest {}
