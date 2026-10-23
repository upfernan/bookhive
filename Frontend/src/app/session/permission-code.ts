export const PermissionCode = {
  lendBook: 'loan.lend',
  searchCatalog: 'catalog.search',
  syncCatalog: 'catalog.sync',
} as const;

export type PermissionCode = (typeof PermissionCode)[keyof typeof PermissionCode];
