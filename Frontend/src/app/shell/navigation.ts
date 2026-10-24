import { messages } from '../messages';
import { PermissionCode } from '../session/permission-code';

export type TaskIcon = 'lend' | 'search' | 'reserve';

export interface Section {
  readonly path: string;
  readonly label: string;
  readonly permission: PermissionCode;
}

export interface Task {
  readonly path: string;
  readonly title: string;
  readonly description: string;
  readonly icon: TaskIcon;
  readonly permission: PermissionCode;
}

export const sections: readonly Section[] = [
  { path: '/prestamos', label: messages.shell.loans, permission: PermissionCode.lendBook },
  { path: '/catalogo', label: messages.shell.catalog, permission: PermissionCode.searchCatalog },
  { path: '/reservas', label: messages.shell.reservations, permission: PermissionCode.reserveBook },
];

export const tasks: readonly Task[] = [
  {
    path: '/prestamos',
    title: messages.tasks.lend.title,
    description: messages.tasks.lend.description,
    icon: 'lend',
    permission: PermissionCode.lendBook,
  },
  {
    path: '/catalogo',
    title: messages.tasks.search.title,
    description: messages.tasks.search.description,
    icon: 'search',
    permission: PermissionCode.searchCatalog,
  },
  {
    path: '/reservas',
    title: messages.tasks.reservations.title,
    description: messages.tasks.reservations.description,
    icon: 'reserve',
    permission: PermissionCode.reserveBook,
  },
];
