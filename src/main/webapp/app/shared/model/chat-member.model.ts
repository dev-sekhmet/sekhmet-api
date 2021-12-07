import { ChatMemberScope } from 'app/shared/model/enumerations/chat-member-scope.model';

export interface IChatMember {
  id?: number;
  uid?: string;
  scope?: ChatMemberScope | null;
}

export const defaultValue: Readonly<IChatMember> = {};
