import dayjs from 'dayjs';

export interface IMessage {
  id?: number;
  uid?: string;
  createdAt?: string;
  image?: string | null;
  video?: string | null;
  audio?: string | null;
  system?: boolean | null;
  sent?: boolean | null;
  received?: boolean | null;
  pending?: boolean | null;
}

export const defaultValue: Readonly<IMessage> = {
  system: false,
  sent: false,
  received: false,
  pending: false,
};
