import { IChatMember } from 'app/shared/model/chat-member.model';
import { IMessage } from 'app/shared/model/message.model';

export interface IChat {
  id?: number;
  guid?: string;
  icon?: string | null;
  name?: string | null;
  members?: IChatMember[] | null;
  messsages?: IMessage[] | null;
}

export const defaultValue: Readonly<IChat> = {};
