import { get, post } from './http.js';

const BASE = '/v1/personal-chats';

export function getPersonalChats(params) {
  return get(BASE, params);
}

export function getPersonalChat(id) {
  return get(`${BASE}/${id}`);
}

export function createPersonalChat(user1Id, user2Id) {
  return post(BASE, { user1Id, user2Id });
}
