import { IChat } from 'app/shared/model/chat.model';
import { ChatMemberScope } from 'app/shared/model/enumerations/chat-member-scope.model';

export interface IChatMember {
  id?: string;
  scope?: ChatMemberScope;
  chat?: IChat | null;
}

export const defaultValue: Readonly<IChatMember> = {};
