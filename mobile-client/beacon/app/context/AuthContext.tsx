import {createContext, useContext, useEffect, useState} from "react";
import * as SecureStore from "expo-secure-store";

type AuthContextType = {
    isLoggedIn: boolean;
    isLoading: boolean;
    isReady: boolean;
    login: (token: string) => Promise<void>;
    setIsLoading: (loading: boolean) => void;
    logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | null>(null);

export default function AuthProvider({children}: { children: React.ReactNode }) {
    const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isReady, setIsReady] = useState(false);     // for initial token check

    // Load token from SecureStore on app start
    useEffect(() => {
        (async () => {
            const token = await SecureStore.getItemAsync("token");
            const loggedIn = !!token;
            setIsLoggedIn(loggedIn);
            console.log("Token exists? ", loggedIn);
            setIsReady(true);
        })();
    }, []);

    async function login(token: string) {
        await SecureStore.setItemAsync("token", token);
        setIsLoading(false);
        setIsLoggedIn(true);
    }

    async function logout() {
        await SecureStore.deleteItemAsync("token");
        setIsLoading(false);
        setIsLoggedIn(false);
    }

    return (
        <AuthContext.Provider value={{isLoggedIn, isLoading, isReady, setIsLoading, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}

export async function getToken() {
    try {
        return await SecureStore.getItemAsync("token");
    } catch (error) {
        console.error(`Error returning token: ${error}`);
        return null;
    }
}