import SecureStore, { getItemAsync } from "expo-secure-store";
import React, { createContext, useContext, useEffect, useState } from "react";
import { Platform } from "react-native";

type AuthContextType = {
  isLoggedIn: boolean;
  user?: { email: string };
  login: (token: string, user: { email: string }) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType>({
  isLoggedIn: false,
  login: async () => {},
  logout: async () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<{ email: string } | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  // Check token on app launch
  useEffect(() => {
    (async () => {
      if (Platform.OS === "web") return null; // or fallback to localStorage
      const token = await getItemAsync("token");
      if (token) {
        // optionally verify token with backend
        setIsLoggedIn(true);
        setUser({ email: "example@beacon.com" }); // fetch real profile if needed
      }
      setLoading(false);
    })();
  }, []);

  const login = async (token: string, user: { email: string }) => {
    await SecureStore.setItemAsync("token", token);
    setUser(user);
    setIsLoggedIn(true);
  };

  const logout = async () => {
    await SecureStore.deleteItemAsync("token");
    setUser(undefined);
    setIsLoggedIn(false);
  };

  if (loading) return null; // or a loading screen

  return (
    <AuthContext.Provider value={{ isLoggedIn, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// ✅ The hook you import
export const useAuth = () => useContext(AuthContext);
