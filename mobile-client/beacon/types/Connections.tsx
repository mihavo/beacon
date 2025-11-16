export type FriendConnection = {
    userId: string;
    fullName: string;
    username: string;
    status: 'FRIENDS_WITH' | 'PENDING' | 'BLOCKED';
    lastConnectionTimestamp: string;
};

export type GetFriendsResponse = {
    connections: FriendConnection[];
    numOfConnections: number;
};
export type GetConnectionsResponse = {
    connections: FriendConnection[];
    numOfConnections: number;
};

export type ConnectResponse = {
    message: string;
    timestamp: string;
};

export type AcceptFriendRequest = {
    targetUserId: string;
}

export type AcceptFriendResponse = {
    message: string;
}

export type DeclineFriendRequest = {
    targetUserId: string;
}

export type DeclineFriendResponse = {}