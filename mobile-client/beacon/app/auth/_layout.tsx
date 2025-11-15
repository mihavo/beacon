import React from "react";
import {Icon, Label, NativeTabs} from "expo-router/unstable-native-tabs";


export default function AuthLayout() {
  return (
    <NativeTabs
    >
        <NativeTabs.Trigger name="login">
                <Label>Login</Label>
            <Icon sf={"arrow.right.square"}></Icon>
        </NativeTabs.Trigger>

        <NativeTabs.Trigger name="register">
                <Label>Register</Label>
            <Icon sf={"person.badge.plus"}></Icon>
        </NativeTabs.Trigger>
    </NativeTabs>
  );
}