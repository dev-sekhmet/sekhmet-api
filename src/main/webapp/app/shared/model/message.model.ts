import dayjs from 'dayjs';
import { IChat } from 'app/shared/model/chat.model';

export interface IMessage {
  id?: string;
  text?: string | null;
  createdAt?: string | null;
  image?: string | null;
  video?: string | null;
  audio?: string | null;
  system?: boolean | null;
  sent?: boolean | null;
  received?: boolean | null;
  pending?: boolean | null;
  chat?: IChat | null;
}

export const defaultValue: Readonly<IMessage> = {
  system: false,
  sent: false,
  received: false,
  pending: false,
};
