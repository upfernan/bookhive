import { Routes } from '@angular/router';
import { PermissionCode } from './session/permission-code';
import { requireAcceptedDataPolicy, requirePermission, requireSession } from './session/session-guards';

export const routes: Routes = [
  {
    path: 'ingresar',
    title: 'Ingresar · BookHive',
    loadComponent: () => import('./session/login/login').then((m) => m.Login),
  },
  {
    path: 'recuperar-contrasena',
    title: 'Recuperar contraseña · BookHive',
    loadComponent: () =>
      import('./session/password-reset/password-reset').then((m) => m.PasswordReset),
  },
  {
    path: 'politica-datos',
    title: 'Política de datos · BookHive',
    canMatch: [requireSession],
    loadComponent: () =>
      import('./session/data-policy-page/data-policy-page').then((m) => m.DataPolicyPage),
  },
  {
    path: '',
    canMatch: [requireSession, requireAcceptedDataPolicy],
    loadComponent: () => import('./shell/shell').then((m) => m.Shell),
    children: [
      {
        path: 'inicio',
        title: 'Inicio · BookHive',
        loadComponent: () => import('./home/home').then((m) => m.Home),
      },
      {
        path: 'prestamos',
        title: 'Préstamos · BookHive',
        canMatch: [requirePermission(PermissionCode.lendBook)],
        loadComponent: () => import('./loans/loans').then((m) => m.Loans),
      },
      {
        path: 'devoluciones',
        title: 'Devoluciones · BookHive',
        canMatch: [requirePermission(PermissionCode.returnBook)],
        loadComponent: () => import('./returns/returns').then((m) => m.Returns),
      },
      {
        path: 'catalogo',
        title: 'Catálogo · BookHive',
        canMatch: [requirePermission(PermissionCode.searchCatalog)],
        loadComponent: () => import('./catalog/catalog').then((m) => m.Catalog),
      },
      {
        path: 'reservas',
        title: 'Mis reservas · BookHive',
        canMatch: [requirePermission(PermissionCode.reserveBook)],
        loadComponent: () => import('./reservations/reservations').then((m) => m.Reservations),
      },
      {
        path: 'mi-cuenta',
        title: 'Mi cuenta · BookHive',
        canMatch: [requirePermission(PermissionCode.viewAccount)],
        loadComponent: () => import('./account/account').then((m) => m.Account),
      },
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
    ],
  },
  { path: '**', redirectTo: 'inicio' },
];
