export interface ApiErrorResponse {
  status: number;
  code: string;
  message: string;
  errors: Record<string, string> | null;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
