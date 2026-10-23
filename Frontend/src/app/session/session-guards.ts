import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { CurrentSession } from './current-session';
import { PermissionCode } from './permission-code';

const LOGIN_PATH = '/ingresar';
const HOME_PATH = '/inicio';

export const requireSession: CanMatchFn = async () => {
  const router = inject(Router);
  const status = await inject(CurrentSession).load();
  return status === 'signed-out' ? router.parseUrl(LOGIN_PATH) : true;
};

export function requirePermission(permission: PermissionCode): CanMatchFn {
  return () => inject(CurrentSession).allows(permission) || inject(Router).parseUrl(HOME_PATH);
}
