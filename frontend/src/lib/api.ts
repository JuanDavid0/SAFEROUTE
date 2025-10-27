import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";

// Avoid requiring @types/node in editor; declare process minimally
declare const process: any;

const baseURL = (process && process.env && process.env.NEXT_PUBLIC_API_BASE_URL) || "http://localhost:8080";

const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000,
});

// Request interceptor to attach token if present
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  try {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      if (token && config.headers) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        config.headers["Authorization"] = `Bearer ${token}`;
      }
    }
  } catch (e) {
    // ignore on server
  }
  return config;
});

export default api;
