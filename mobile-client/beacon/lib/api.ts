const BASE = process.env.EXPO_PUBLIC_API_URL;

export async function login(username: string, password: string) {

    const res = await fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
        body: JSON.stringify({username, password}),
  });
    return await res.json();
}

export async function registerUser(dto: any) {
  const res = await fetch(`${BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
    return await res.json();
}
