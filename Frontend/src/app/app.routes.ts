import { Routes } from '@angular/router';
import { PermissionCode } from './session/permission-code';
import { requirePermission, requireSession } from './session/session-guards';

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
    path: '',
    canMatch: [requireSession],
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
        path: 'catalogo',
        title: 'Catálogo · BookHive',
        canMatch: [requirePermission(PermissionCode.searchCatalog)],
        loadComponent: () => import('./catalog/catalog').then((m) => m.Catalog),
      },
      {
        path: 'catalogo/actualizar',
        title: 'Actualizar el buscador · BookHive',
        canMatch: [requirePermission(PermissionCode.syncCatalog)],
        loadComponent: () => import('./catalog/catalog-sync').then((m) => m.CatalogSync),
      },
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
    ],
  },
  { path: '**', redirectTo: 'inicio' },
];
