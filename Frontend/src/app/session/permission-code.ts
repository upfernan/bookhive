export const PermissionCode = {
  lendBook: 'loan.lend',
  searchCatalog: 'catalog.search',
} as const;

export type PermissionCode = (typeof PermissionCode)[keyof typeof PermissionCode];
