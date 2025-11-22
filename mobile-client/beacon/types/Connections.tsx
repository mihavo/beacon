export type Connection = {
    userId: string;
    fullName: string;
    username: string;
    status: 'FRIENDS_WITH' | 'PENDING' | 'BLOCKED';
    lastConnectionTimestamp: string;
};

export type GetFriendsResponse = {
    connections: Connection[];
    numOfConnections: number;
};
export type GetConnectionsResponse = {
    connections: Connection[];
    numOfConnections: number;
};
export type GetUserResponse = {
    id: string;
    username: string;
    fullName: string;
};

export type ConnectResponse = {
    message: string;
    timestamp: string;
};

export type DeleteFriendResponse = {
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

export type SearchResponse = GetUserResponse;