import {Button, ContextMenu, Host} from "@expo/ui/swift-ui";
import {Ionicons} from "@expo/vector-icons";
import React from "react";

export function ProfileMenu() {
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
                        onPress={() => console.log('Logging out')}>
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
