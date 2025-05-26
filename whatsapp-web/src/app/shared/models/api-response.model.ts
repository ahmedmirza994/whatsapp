export interface ApiResponse<T> {
  status: number;
  error: string | null;
  data: T | null;
}
