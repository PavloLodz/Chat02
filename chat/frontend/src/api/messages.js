import { get, post, put, del } from './http.js';

const MSG_BASE = '/v1/messages';
const ATTACH_BASE = '/v1/attachments';

export function getMessages(params) {
  return get(MSG_BASE, params);
}

export function sendMessage(data) {
  return post(MSG_BASE, data);
}

export function updateMessage(id, data) {
  return put(`${MSG_BASE}/${id}`, data);
}

export function deleteMessage(id) {
  return del(`${MSG_BASE}/${id}`);
}

// Attachments
export function uploadAttachment(formData) {
  return post(ATTACH_BASE, formData);
}

export function getAttachment(id) {
  return get(`${ATTACH_BASE}/${id}`);
}

export function getAttachments(params) {
  return get(ATTACH_BASE, params);
}
