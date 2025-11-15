import * as SecureStore from "expo-secure-store";

const BASE = "https://your-beacon-api";

export async function login(email: string, password: string) {
  const res = await fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) return false;

  const data = await res.json();
  await SecureStore.setItemAsync("token", data.token);
  return true;
}

export async function registerUser(dto: any) {
  const res = await fetch(`${BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  return res.ok;
}
