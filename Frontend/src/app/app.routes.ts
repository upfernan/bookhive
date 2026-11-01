import { Routes } from '@angular/router';
import { PermissionCode } from './session/permission-code';
import {
  requireAcceptedDataPolicy,
  requirePendingRegistration,
  requirePermission,
  requireSession,
} from './session/session-guards';

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
    path: 'crear-cuenta',
    title: 'Crear cuenta · BookHive',
    loadComponent: () => import('./session/sign-up/sign-up').then((m) => m.SignUp),
  },
  {
    path: 'completar-registro',
    title: 'Termine de crear su cuenta · BookHive',
    canMatch: [requirePendingRegistration],
    loadComponent: () =>
      import('./session/complete-registration/complete-registration').then((m) => m.CompleteRegistration),
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
        path: 'catalogo/titulos/nuevo',
        title: 'Registrar un libro · BookHive',
        canMatch: [requirePermission(PermissionCode.manageCatalog)],
        loadComponent: () => import('./catalog-admin/title-editor').then((m) => m.TitleEditor),
      },
      {
        path: 'catalogo/titulos/:id',
        title: 'Editar libro · BookHive',
        canMatch: [requirePermission(PermissionCode.manageCatalog)],
        loadComponent: () => import('./catalog-admin/title-editor').then((m) => m.TitleEditor),
      },
      {
        path: 'categorias',
        title: 'Categorías · BookHive',
        canMatch: [requirePermission(PermissionCode.manageCategories)],
        loadComponent: () => import('./catalog-admin/categories').then((m) => m.Categories),
      },
      {
        path: 'ejemplares',
        title: 'Ubicar ejemplares · BookHive',
        canMatch: [requirePermission(PermissionCode.locateBookCopy)],
        loadComponent: () => import('./administration/book-copies').then((m) => m.BookCopies),
      },
      {
        path: 'lectores',
        title: 'Administrar lectores · BookHive',
        canMatch: [requirePermission(PermissionCode.manageUsers)],
        loadComponent: () => import('./administration/readers').then((m) => m.Readers),
      },
      {
        path: 'configuracion',
        title: 'Configuración · BookHive',
        canMatch: [requirePermission(PermissionCode.configureInstitution)],
        loadComponent: () => import('./administration/settings').then((m) => m.Settings),
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
      {
        path: 'pagos/resultado/:referencia',
        title: 'Resultado del pago · BookHive',
        canMatch: [requirePermission(PermissionCode.payFine)],
        loadComponent: () => import('./payments/payment-result').then((m) => m.PaymentResult),
      },
      {
        path: 'pagos/resultado',
        title: 'Resultado del pago · BookHive',
        canMatch: [requirePermission(PermissionCode.payFine)],
        loadComponent: () => import('./payments/payment-result').then((m) => m.PaymentResult),
      },
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
    ],
  },
  { path: '**', redirectTo: 'inicio' },
];
