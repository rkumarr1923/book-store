import axiosClient from './axiosClient';

export const contactApi = {
  /**
   * Submits the Contact Us form to the backend, which forwards it via email.
   * No authentication required.
   */
  sendMessage: (payload) =>
    axiosClient.post('/contact', payload).then((r) => r.data),
};
