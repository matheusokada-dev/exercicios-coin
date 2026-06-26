export interface ErrorObject<T> {
  codError: number;
  msgError: string;
  value?: T;
}