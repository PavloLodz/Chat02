import { get, post, put, del } from './http.js';

const BASE = '/v1/chat-rooms';
const MEMBERS_BASE = '/v1/room-members';

export function getRooms(params) {
  return get(BASE, params);
}

export function getRoom(id) {
  return get(`${BASE}/${id}`);
}

export function createRoom(data) {
  return post(BASE, data);
}

export function updateRoom(id, data) {
  return put(`${BASE}/${id}`, data);
}

export function deleteRoom(id) {
  return del(`${BASE}/${id}`);
}

// Room members
export function getRoomMembers(roomId) {
  return get(`${MEMBERS_BASE}/room/${roomId}`);
}

export function joinRoom(roomId, userId) {
  return post(MEMBERS_BASE, { roomId, userId, role: 'MEMBER' });
}

export function leaveRoom(memberId) {
  return del(`${MEMBERS_BASE}/${memberId}`);
}

export function getMembership(params) {
  return get(MEMBERS_BASE, params);
}
