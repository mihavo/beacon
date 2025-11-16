import {useAuth} from "@/app/context/AuthContext";
import {SplashScreen} from "expo-router";

export default function SplashController() {
    const {isLoading} = useAuth();

    if (!isLoading) {
        SplashScreen.hide();
    }

    return null;
}
