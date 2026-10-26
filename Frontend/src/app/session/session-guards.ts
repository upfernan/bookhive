import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { CurrentSession } from './current-session';
import { DataPolicy } from './data-policy';
import { PermissionCode } from './permission-code';

const LOGIN_PATH = '/ingresar';
const HOME_PATH = '/inicio';
const DATA_POLICY_PATH = '/politica-datos';
const RETURN_PARAMETER = 'volver';

export const requireSession: CanMatchFn = async () => {
  const router = inject(Router);
  const status = await inject(CurrentSession).load();
  return status === 'signed-out' ? router.parseUrl(LOGIN_PATH) : true;
};

export const requireAcceptedDataPolicy: CanMatchFn = async () => {
  const router = inject(Router);
  const dataPolicy = inject(DataPolicy);

  if ((await inject(CurrentSession).load()) === 'signed-out') {
    return true;
  }

  if ((await dataPolicy.check()) !== 'pending') {
    return true;
  }

  const navigation = router.getCurrentNavigation();
  const target = navigation ? router.serializeUrl(navigation.extractedUrl) : HOME_PATH;
  return router.createUrlTree([DATA_POLICY_PATH], { queryParams: { [RETURN_PARAMETER]: target } });
};

export function requirePermission(permission: PermissionCode): CanMatchFn {
  return () => inject(CurrentSession).allows(permission) || inject(Router).parseUrl(HOME_PATH);
}

export function safeReturnPath(requested: string | null): string {
  const path = (requested ?? '').trim();
  return path.startsWith('/') && !path.startsWith('//') && !path.startsWith(DATA_POLICY_PATH) ? path : HOME_PATH;
}
