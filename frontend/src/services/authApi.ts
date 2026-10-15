import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

export type SessionUser = {
  id: number;
  nome: string;
  login: string;
  tipo: 'PROFESSOR' | 'ALUNO';
  role: 'ROLE_PROFESSOR' | 'ROLE_ALUNO';
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: SessionUser;
};

const ACCESS_KEY = 'mathinclui.accessToken';
const REFRESH_KEY = 'mathinclui.refreshToken';

export const api = axios.create({ baseURL: '/api/v1' });

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_KEY);
}

export function saveSession(auth: AuthResponse) {
  sessionStorage.setItem(ACCESS_KEY, auth.accessToken);
  sessionStorage.setItem(REFRESH_KEY, auth.refreshToken);
}

export function clearSession() {
  sessionStorage.removeItem(ACCESS_KEY);
  sessionStorage.removeItem(REFRESH_KEY);
}

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const accessToken = getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  try {
    const response = await axios.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken });
    saveSession(response.data);
    return response.data.accessToken;
  } catch {
    clearSession();
    return null;
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    if (error.response?.status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }

    original._retry = true;
    refreshPromise ??= refreshAccessToken().finally(() => {
      refreshPromise = null;
    });
    const newAccessToken = await refreshPromise;
    if (!newAccessToken) return Promise.reject(error);

    original.headers.Authorization = `Bearer ${newAccessToken}`;
    return api.request(original);
  }
);

export async function registerProfessor(nome: string, email: string, password: string) {
  const response = await api.post<AuthResponse>('/auth/professor/register', { nome, email, password });
  saveSession(response.data);
  return response.data;
}

export async function loginProfessor(email: string, password: string) {
  const response = await api.post<AuthResponse>('/auth/professor/login', { email, password });
  saveSession(response.data);
  return response.data;
}

export async function loginAluno(username: string, pin: string) {
  const response = await api.post<AuthResponse>('/auth/aluno/login', { username, pin });
  saveSession(response.data);
  return response.data;
}

export async function requestPasswordReset(email: string) {
  await api.post('/auth/professor/forgot-password', { email });
}

export async function resetPassword(token: string, newPassword: string) {
  await api.post('/auth/professor/reset-password', { token, newPassword });
}

export async function fetchSession() {
  const response = await api.get<SessionUser>('/auth/me');
  return response.data;
}

export async function logout() {
  const refreshToken = getRefreshToken();
  try {
    if (refreshToken) {
      await axios.post('/api/v1/auth/logout', { refreshToken });
    }
  } finally {
    clearSession();
  }
}

export function authErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const detail = (error.response?.data as { detail?: string } | undefined)?.detail;
    if (detail) return detail;
    if (error.response?.status === 429) return 'Muitas tentativas. Aguarde alguns minutos e tente novamente.';
  }
  return 'Não foi possível concluir a operação. Tente novamente.';
}
