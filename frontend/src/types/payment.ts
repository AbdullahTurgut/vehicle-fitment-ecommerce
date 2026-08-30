export type PaymentMethod = "CREDIT_CARD" | "BANK_TRANSFER" | "CASH_ON_DELIVERY";
export type PaymentStatus = "PENDING" | "SUCCESS" | "FAILED" | "CANCELLED" | "REFUNDED";

export interface ProcessPaymentRequest {
  orderNumber: string;
  paymentMethod: PaymentMethod;
  cardHolderName: string;
  cardNumber: string;
  expireMonth: string;
  expireYear: string;
  cvc: string;
  installment?: number;
}

export interface PaymentTransaction {
  id: string;
  transactionType: string;
  amount: number;
  status: string;
  transactionId?: string;
  errorMessage?: string;
  createdAt: string;
}

export interface PaymentResponse {
  id: string;
  orderNumber: string;
  paymentMethod: PaymentMethod;
  provider: string;
  status: PaymentStatus;
  amount: number;
  paidAt?: string;
  transactions: PaymentTransaction[];
}
