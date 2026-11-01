import { messages } from '../messages';
import { PermissionCode } from '../session/permission-code';

export type TaskIcon = 'lend' | 'return' | 'search' | 'reserve' | 'account' | 'add-book' | 'categories' | 'book-copies' | 'readers' | 'settings';

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
  { path: '/devoluciones', label: messages.shell.returns, permission: PermissionCode.returnBook },
  { path: '/catalogo', label: messages.shell.catalog, permission: PermissionCode.searchCatalog },
  { path: '/reservas', label: messages.shell.reservations, permission: PermissionCode.reserveBook },
  { path: '/mi-cuenta', label: messages.shell.myAccount, permission: PermissionCode.viewAccount },
  { path: '/categorias', label: messages.shell.categories, permission: PermissionCode.manageCategories },
  { path: '/ejemplares', label: messages.shell.bookCopies, permission: PermissionCode.locateBookCopy },
  { path: '/lectores', label: messages.shell.readers, permission: PermissionCode.manageUsers },
  { path: '/configuracion', label: messages.shell.settings, permission: PermissionCode.configureInstitution },
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
    path: '/devoluciones',
    title: messages.tasks.returnBook.title,
    description: messages.tasks.returnBook.description,
    icon: 'return',
    permission: PermissionCode.returnBook,
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
  {
    path: '/mi-cuenta',
    title: messages.tasks.account.title,
    description: messages.tasks.account.description,
    icon: 'account',
    permission: PermissionCode.viewAccount,
  },
  {
    path: '/catalogo/titulos/nuevo',
    title: messages.tasks.registerTitle.title,
    description: messages.tasks.registerTitle.description,
    icon: 'add-book',
    permission: PermissionCode.manageCatalog,
  },
  {
    path: '/categorias',
    title: messages.tasks.categories.title,
    description: messages.tasks.categories.description,
    icon: 'categories',
    permission: PermissionCode.manageCategories,
  },
  {
    path: '/ejemplares',
    title: messages.tasks.bookCopies.title,
    description: messages.tasks.bookCopies.description,
    icon: 'book-copies',
    permission: PermissionCode.locateBookCopy,
  },
  {
    path: '/lectores',
    title: messages.tasks.readers.title,
    description: messages.tasks.readers.description,
    icon: 'readers',
    permission: PermissionCode.manageUsers,
  },
  {
    path: '/configuracion',
    title: messages.tasks.settings.title,
    description: messages.tasks.settings.description,
    icon: 'settings',
    permission: PermissionCode.configureInstitution,
  },
];
