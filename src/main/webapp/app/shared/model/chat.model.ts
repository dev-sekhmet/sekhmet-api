export interface IChat {
  id?: number;
  guid?: string;
  icon?: string | null;
  name?: string | null;
}

export const defaultValue: Readonly<IChat> = {};
