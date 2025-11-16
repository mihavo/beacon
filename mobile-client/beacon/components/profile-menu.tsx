import {Button, ContextMenu, Host} from "@expo/ui/swift-ui";
import {Ionicons} from "@expo/vector-icons";
import React from "react";
import {useAuth} from "@/app/context/AuthContext";
import {router} from "expo-router";

export function ProfileMenu() {
    const auth = useAuth();

    const handleLogout = async () => {
        console.log('Logging out');
        await auth.logout();
        router.replace('/auth/login');
    }
    return (
        <Host>
            <ContextMenu>
                <ContextMenu.Items>
                    <Button
                        systemImage="person.crop.circle.badge.xmark"
                        onPress={() => console.log('Redirecting to Account')}>
                        Account
                    </Button>
                    <Button
                        variant="bordered"
                        systemImage="arrow.right.to.line.square"
                        onPress={handleLogout}>
                        Log Out
                    </Button>
                </ContextMenu.Items>
                <ContextMenu.Trigger>
                    <Ionicons name="person-circle" size={36} color="#007aff"/>
                </ContextMenu.Trigger>
            </ContextMenu>
        </Host>
    );
}
